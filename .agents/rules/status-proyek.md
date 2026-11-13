# Aturan Sinkronisasi Status Proyek

Aturan ini wajib ditaati oleh AI Agent setiap kali melakukan **penambahan, perubahan, atau penghapusan fitur** pada proyek VEN.

Dokumen acuan status proyek: **`docs/STATUS-PROYEK-VEN.md`**

---

## 1. Prinsip Utama

> **Setiap penambahan fitur mengubah status proyek.**

Artinya, pekerjaan pada kode **belum dianggap selesai** sebelum `docs/STATUS-PROYEK-VEN.md` diperbarui pada perubahan/commit yang sama. Dokumen status bukan pelengkap, melainkan bagian dari definisi selesai (*definition of done*).

---

## 2. Definisi "Penambahan / Perubahan Fitur"

Aturan ini berlaku untuk:

- Endpoint, route, controller, service, command, atau job baru di backend.
- Screen, composable, ViewModel, UiState, atau alur navigasi baru di Android.
- Integrasi API baru (mengubah fitur dari mock/simulasi menjadi data nyata).
- Perubahan perilaku fitur yang sudah ada (misalnya validasi, alur publish, penyimpanan data).
- Penghapusan atau penggantian fitur (*deprecation*).
- Perbaikan kendala yang tercatat pada bagian "Kendala" di dokumen status.

Perubahan yang **tidak** mengubah status (misalnya refactor internal tanpa perubahan perilaku, perbaikan typo, atau update komentar) tetap wajib memperbarui bagian hasil verifikasi bila angka test berubah.

---

## 3. Kewajiban AI Agent

Saat menambahkan atau mengubah fitur, AI Agent **wajib**:

1. **Memperbarui baris modul terkait** di `docs/STATUS-PROYEK-VEN.md` dengan status yang sesuai.
2. **Memindahkan modul** dari bagian "Masih Mock / Belum Tersambung" ke bagian "Sudah Selesai" jika fitur sudah benar-benar terhubung ke backend.
3. **Memperbarui angka hasil verifikasi**: jumlah test backend, jumlah test Android, jumlah route, dan status build.
4. **Memperbarui metadata dokumen**: tanggal verifikasi, branch aktif, dan commit terakhir.
5. **Menambah kendala baru** yang ditemukan, atau **menghapus kendala** yang sudah diperbaiki beserta buktinya.
6. **Menyertakan `docs/STATUS-PROYEK-VEN.md`** dalam perubahan/commit yang sama dengan perubahan kode fitur.

---

## 4. Arti Status

| Simbol | Arti | Syarat |
|:--:|---|---|
| ✅ | **Selesai & terverifikasi** | Kode berjalan, terhubung ke API/database, dan dibuktikan dengan test atau smoke test |
| ⚠️ | **Sebagian / butuh konfigurasi** | Kode ada tetapi bergantung pada konfigurasi (API key, kredensial) atau baru sebagian alur yang tersambung |
| ❌ | **Belum / masih mock** | Masih menggunakan data dummy, simulasi `delay()`, atau placeholder |

**Larangan:** menandai fitur dengan ✅ tanpa bukti test, build, atau smoke test yang dijalankan pada sesi tersebut.

---

## 5. Format Pembaruan

- Tambahkan/ubah baris pada tabel modul yang relevan (Backend, Android, atau Artefak Pendukung).
- Gunakan path file + nomor baris saat mencatat lokasi mock, misalnya:
  `ven-mobile/.../ScheduleScreen.kt:47`.
- Untuk kendala baru, gunakan tabel pada bagian "Kendala dan Rekomendasi Perbaikan" dengan kolom: **Kendala, Bukti, Dampak, Rekomendasi**.
- Cantumkan perintah verifikasi yang dijalankan pada bagian "Cara Verifikasi Ulang" bila ada perubahan cara menjalankan.

---

## 6. Checklist Wajib Sebelum Menyatakan Tugas Selesai

- [ ] Fitur baru sudah diimplementasikan dan terhubung ke sumber data nyata.
- [ ] Test terkait dijalankan (PHPUnit untuk backend, Gradle untuk Android) dan hasilnya dicatat.
- [ ] Build aplikasi diverifikasi (minimal `assembleDebug` untuk perubahan Android).
- [ ] `docs/STATUS-PROYEK-VEN.md` sudah diperbarui (status modul, angka test, tanggal, branch, commit).
- [ ] Kendala baru/selesai sudah dicatat atau dihapus.
- [ ] Dokumen status disertakan dalam commit lokal bersama perubahan kode.

---

## 7. Contoh Penerapan

**Kasus:** modul Schedule yang sebelumnya memakai `dummyScheduleUiState` kini tersambung ke endpoint `undangan`.

**Yang harus dilakukan:**

1. Ubah status baris *Schedule* di tabel Android dari `❌` menjadi `✅`.
2. Hapus baris *Schedule* dari tabel "Bagian yang Masih Mock".
3. Perbarui angka test Android dan backend bila ada test baru.
4. Perbarui tanggal verifikasi, branch, dan commit terakhir.
5. Sertakan `docs/STATUS-PROYEK-VEN.md` dalam commit yang sama.
