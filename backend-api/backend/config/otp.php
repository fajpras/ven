<?php

return [
    'panjang' => 6,
    'jeda_kirim_ulang' => 60,
    'maksimum_percobaan' => 5,
    'maksimum_permintaan_per_jam' => 5,
    'tujuan' => [
        'registrasi' => [
            'berlaku' => 15,
            'subjek' => 'Kode Verifikasi Pendaftaran VEN',
            'peruntukan' => 'menyelesaikan pendaftaran akun',
        ],
        'ganti_email' => [
            'berlaku' => 10,
            'subjek' => 'Kode Verifikasi Penggantian Email VEN',
            'peruntukan' => 'mengganti alamat email akun',
        ],
        'reset_kata_sandi' => [
            'berlaku' => 10,
            'subjek' => 'Kode Reset Kata Sandi VEN',
            'peruntukan' => 'mengatur ulang kata sandi akun',
        ],
    ],
];
