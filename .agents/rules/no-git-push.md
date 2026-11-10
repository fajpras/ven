# Git Workflow Rules - Larangan Git Push Otomatis untuk AI

## ATURAN KHUSUS AI AGENT: DILARANG MELAKUKAN GIT PUSH OTOMATIS
- **DILARANG KERAS BAGI AI AGENT** untuk mengeksekusi perintah `git push` secara otomatis dalam bentuk apa pun.
- Tugas AI Agent dalam manajemen Git hanya sebatas:
  - Menyunting dan merapikan kode.
  - Menjalankan pengetesan (testing).
  - Melakukan staging (`git add`) dan commit secara lokal (`git commit`) atau squash commit bila diminta.
- **HAK PUSH SEPENUHNYA MILIK PENGGUNA (DEVELOPER)**:
  - Tindakan `git push` ke remote repository adalah keputusan manual yang dilakukan secara eksklusif oleh Pengguna / Developer sendiri.
  - Jangan pernah membuat git hook atau mekanisme apa pun yang memblokir perintah `git push` manual milik pengguna di terminal lokal.
  - Riwayat commit masa lalu dibiarkan apa adanya.
