<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use PDO;
use Throwable;

class RestoreDatabase extends Command
{
    /**
     * The name and signature of the console command.
     *
     * Options:
     *   --path : full path to the .sql dump file to import (required)
     */
    protected $signature = 'ven:restore-db {--path= : Full path to the .sql dump file to restore} {--db= : Target database name (default: DB_DATABASE)}';

    /**
     * The console command description.
     */
    protected $description = 'Restore MySQL database from a .sql dump file.';

    public function handle(): int
    {
        $host = env('DB_HOST', '127.0.0.1');
        $port = env('DB_PORT', '3306');
        $db   = $this->option('db') ?: env('DB_DATABASE', 'ven');
        $user = env('DB_DBA_USERNAME', 'dba');

        if (! $db) {
            $this->error('Variable DB_DATABASE tidak di‑set di .env');
            return self::FAILURE;
        }

        $path = $this->option('path');
        if (! $path) {
            $this->error('Opsi --path wajib di‑isi dengan path lengkap ke file .sql');
            return self::FAILURE;
        }
        if (! file_exists($path)) {
            $this->error("File tidak ditemukan: {$path}");
            return self::FAILURE;
        }

        $pass = env('DB_DBA_PASSWORD');
        if ($pass === null || $pass === '') {
            $pass = (string) $this->secret("Password MySQL untuk user '{$user}' (kosongkan bila tidak ada)");
        }

        $this->info('Menjalankan restore…');
        $binary = env('MYSQL_BINARY_PATH');
        if (! $binary) {
            $laragonMysql = glob('C:/laragon/bin/mysql/*/bin/mysql.exe');
            $binary = ! empty($laragonMysql) ? end($laragonMysql) : 'mysql';
        }

        $command = escapeshellarg($binary) . " -h {$host} -P {$port} -u {$user} " .
            (empty($pass) ? '' : "-p{$pass} ") .
            "{$db} < " . escapeshellarg($path);

        exec($command, $output, $returnVar);

        if ($returnVar !== 0) {
            $this->error('Restore gagal. Periksa kredensial dan pastikan file dump valid.');
            return self::FAILURE;
        }

        $this->info('Restore berhasil.');
        return self::SUCCESS;
    }
}
?>
