package com.ven.app.data.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * Pengelola Cache Gambar Berperforma Tinggi (Two-Level: Memory LruCache + Disk Cache)
 * dengan pemuatan chunked stream, concurrent throttling (Semaphore), dan downsampling otomatis.
 */
object ImageCacheManager {

    private const val BUFFER_SIZE = 8192 // 8 KB chunk buffer
    private const val DISK_CACHE_SUBDIR = "ven_image_cache"

    private val mutex = Mutex()
    private val downloadSemaphore = Semaphore(permits = 3) // Maksimal 3 download serentak agar tidak membebani server lokal / jaringan
    private var isInitialized = false
    private lateinit var diskCacheDir: File

    // 1. Memory Cache (LruCache) menggunakan 1/8 alokasi memori aplikasi
    private val memoryCache: LruCache<String, Bitmap> by lazy {
        val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSizeKb = (maxMemoryKb / 8).coerceAtLeast(16 * 1024) // minimal 16MB

        object : LruCache<String, Bitmap>(cacheSizeKb) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return value.byteCount / 1024
            }
        }
    }

    // In-flight dedup: mencegah request jaringan ganda untuk URL/key yang sama
    private val inFlightRequests = ConcurrentHashMap<String, CompletableDeferred<Bitmap?>>()

    /**
     * Inisialisasi direktori disk cache jika belum aktif.
     */
    fun init(context: Context) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    diskCacheDir = File(context.cacheDir, DISK_CACHE_SUBDIR).apply {
                        if (!exists()) mkdirs()
                    }
                    isInitialized = true
                }
            }
        }
    }

    /**
     * Mengambil Bitmap langsung dari Memory Cache secara sinkron (0ms).
     */
    fun getMemory(url: String?, maxWidth: Int = 1080, maxHeight: Int = 1350): Bitmap? {
        if (url.isNullOrBlank()) return null
        val memKey = buildMemoryKey(url, maxWidth, maxHeight)
        return memoryCache.get(memKey)
    }

    /**
     * Memuat Bitmap dari: Memory -> Disk -> Jaringan (Chunked download + downsampled).
     */
    suspend fun loadBitmap(
        url: String?,
        maxWidth: Int = 1080,
        maxHeight: Int = 1350,
    ): Bitmap? {
        if (url.isNullOrBlank()) return null
        val memKey = buildMemoryKey(url, maxWidth, maxHeight)
        val diskKey = normalizeCacheKey(url)

        // 1. Cek Memory Cache
        memoryCache.get(memKey)?.let { return it }

        // 2. Cegah request duplikat serentak (In-flight deduplication per memory resolution)
        var deferred: CompletableDeferred<Bitmap?>? = null
        var isInitiator = false

        mutex.withLock {
            deferred = inFlightRequests[memKey]
            if (deferred == null) {
                deferred = CompletableDeferred()
                inFlightRequests[memKey] = deferred!!
                isInitiator = true
            }
        }

        if (!isInitiator) {
            return deferred!!.await()
        }

        return try {
            val result = withContext(Dispatchers.IO) {
                // 3. Cek Disk Cache
                var bmp = loadFromDisk(diskKey, maxWidth, maxHeight)
                if (bmp != null) {
                    memoryCache.put(memKey, bmp)
                    return@withContext bmp
                }

                // 4. Unduh dari jaringan secara chunked dengan Semaphore Throttling
                val resolvedUrl = resolveNetworkUrl(url)
                val downloaded = downloadSemaphore.withPermit {
                    // Cek lagi disk cache barangkali thread lain baru saja selesai mendownload
                    if (hasValidDiskCache(diskKey)) {
                        true
                    } else {
                        downloadToDiskChunked(resolvedUrl, diskKey)
                    }
                }

                if (downloaded) {
                    bmp = loadFromDisk(diskKey, maxWidth, maxHeight)
                    if (bmp != null) {
                        memoryCache.put(memKey, bmp)
                    }
                }
                bmp
            }
            deferred!!.complete(result)
            result
        } catch (e: Exception) {
            deferred!!.complete(null)
            null
        } finally {
            inFlightRequests.remove(memKey)
        }
    }

    private fun hasValidDiskCache(key: String): Boolean {
        if (!isInitialized) return false
        val file = File(diskCacheDir, key)
        return file.exists() && file.length() > 0L
    }

    /**
     * Membaca file gambar dari disk cache dengan optimal downsampling (inSampleSize).
     */
    private fun loadFromDisk(key: String, maxWidth: Int, maxHeight: Int): Bitmap? {
        if (!isInitialized) return null
        val file = File(diskCacheDir, key)
        if (!file.exists() || file.length() == 0L) return null

        return try {
            // Langkah 1: Hanya baca ukuran gambar tanpa decode pixel ke RAM
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)

            // Langkah 2: Hitung rasio inSampleSize terbaik
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Mengunduh gambar secara streaming chunk per chunk (8KB) langsung ke disk.
     */
    private fun downloadToDiskChunked(urlStr: String, key: String): Boolean {
        if (!isInitialized) return false
        val targetFile = File(diskCacheDir, key)
        val tempFile = File(diskCacheDir, "$key.tmp")

        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 8000
                doInput = true
                instanceFollowRedirects = true
            }
            connection.connect()

            if (connection.responseCode !in 200..299) {
                return false
            }

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                }
            }

            // Atomic rename dari .tmp ke file final
            if (tempFile.exists() && tempFile.length() > 0) {
                if (targetFile.exists()) targetFile.delete()
                tempFile.renameTo(targetFile)
            } else {
                tempFile.delete()
                false
            }
        } catch (_: Exception) {
            if (tempFile.exists()) tempFile.delete()
            false
        } finally {
            connection?.disconnect()
        }
    }

    /**
     * Hitung faktor pembagian sampel (inSampleSize) untuk mencegah pemborosan RAM.
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun buildMemoryKey(url: String, maxWidth: Int, maxHeight: Int): String {
        val baseKey = normalizeCacheKey(url)
        return "${baseKey}_${maxWidth}x${maxHeight}"
    }

    /**
     * Menormalkan cache key disk agar tetap konsisten walaupun temporary signature URL berubah.
     */
    fun normalizeCacheKey(url: String): String {
        val berkasRegex = Regex("""/karya/([a-zA-Z0-9-]+)/berkas""")
        val match = berkasRegex.find(url)
        if (match != null) {
            return "karya_${match.groupValues[1]}"
        }

        if (url.contains("/storage/")) {
            val clean = url.substringAfter("/storage/").replace('/', '_')
            return "storage_$clean"
        }

        val base = url.substringBefore('?')
        return sha256(base)
    }

    /**
     * Memetakan localhost/127.0.0.1 ke host loopback emulator 10.0.2.2.
     */
    fun resolveNetworkUrl(url: String): String {
        return when {
            url.contains("localhost") -> url.replace("localhost", "10.0.2.2")
            url.contains("127.0.0.1") -> url.replace("127.0.0.1", "10.0.2.2")
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.startsWith("/") -> "http://10.0.2.2:8000$url"
            else -> "http://10.0.2.2:8000/storage/" + url.removePrefix("storage/")
        }
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
