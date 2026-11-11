<?php

return [
    /*
    |--------------------------------------------------------------------------
    | Exhibition Templates Configuration
    |--------------------------------------------------------------------------
    |
    | Batas jumlah maksimal panel (wall) dan objek (floor) untuk setiap
    | template pameran virtual 3D VEN (Small, Medium, Big, Event).
    |
    */
    'templates' => [
        'template01' => [
            'name' => 'Small',
            'max_panels' => 9,
            'max_objects' => 9,
            'tipe' => 'kecil',
            'aliases' => ['tpl_small', 'small', 'template01'],
        ],
        'template02' => [
            'name' => 'Medium',
            'max_panels' => 14,
            'max_objects' => 14,
            'tipe' => 'sedang',
            'aliases' => ['tpl_medium', 'medium', 'template02'],
        ],
        'template03' => [
            'name' => 'Big',
            'max_panels' => 21,
            'max_objects' => 21,
            'tipe' => 'besar',
            'aliases' => ['tpl_big', 'big', 'template03'],
        ],
        'template04' => [
            'name' => 'Event',
            'max_panels' => 60,
            'max_objects' => 60,
            'tipe' => 'acara',
            'aliases' => ['tpl_event', 'event', 'template04'],
        ],
    ],

    /*
    |--------------------------------------------------------------------------
    | Daftar Kata Kasar / Profanity
    |--------------------------------------------------------------------------
    |
    | Daftar kata terlarang untuk validasi pameran. Jika terdeteksi,
    | server akan mengembalikan HTTP 422 dengan pesan "mengandung kata kasar".
    |
    */
    'banned_words' => [
        'anjing', 'babi', 'bangsat', 'kontol', 'memek', 'jembut', 'pantek',
        'ngentot', 'perek', 'lonte', 'bajingan', 'kampret', 'tai', 'tolol',
        'goblok', 'idiot', 'puki', 'fuck', 'shit', 'asshole', 'bitch', 'cunt',
    ],
];
