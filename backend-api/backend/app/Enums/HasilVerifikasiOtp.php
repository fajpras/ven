<?php

namespace App\Enums;

enum HasilVerifikasiOtp
{
    case Berhasil;
    case KodeSalah;
    case Terkunci;
    case TidakDitemukan;
}
