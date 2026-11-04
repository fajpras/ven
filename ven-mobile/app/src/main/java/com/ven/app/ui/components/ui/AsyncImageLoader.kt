package com.ven.app.ui.components.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.ven.app.data.cache.ImageCacheManager

/**
 * Helper pemuatan gambar asynchronous dengan sistem 2-Level Cache (Memory LruCache + Disk Cache),
 * Semaphore Throttling, dan streaming chunked download berkinerja tinggi tanpa dependensi eksternal.
 */
@Composable
fun rememberAsyncImageBitmap(
    url: String?,
    maxWidth: Int = 1080,
    maxHeight: Int = 1350,
): ImageBitmap? {
    val context = LocalContext.current.applicationContext

    // Inisialisasi disk cache satu kali
    remember {
        ImageCacheManager.init(context)
        true
    }

    // Cek seketika di memory cache (0ms delay jika sudah pernah diakses)
    val cachedMemory = remember(url, maxWidth, maxHeight) {
        ImageCacheManager.getMemory(url, maxWidth, maxHeight)?.asImageBitmap()
    }

    var bitmap by remember(url, maxWidth, maxHeight) { mutableStateOf(cachedMemory) }

    LaunchedEffect(url, maxWidth, maxHeight) {
        if (cachedMemory != null) {
            bitmap = cachedMemory
            return@LaunchedEffect
        }

        if (!url.isNullOrBlank()) {
            val bmp = ImageCacheManager.loadBitmap(url, maxWidth, maxHeight)
            if (bmp != null) {
                bitmap = bmp.asImageBitmap()
            }
        } else {
            bitmap = null
        }
    }

    return bitmap
}
