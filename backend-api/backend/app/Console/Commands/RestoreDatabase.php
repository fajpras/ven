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
    protected $signature = 'ven:restore-db {--path= : Full path to the .sql dump file to restore}';

    /**
     * The console command description.
     */
    protected $description = 'Restore MySQL database from a .sql dump file.';

    public function handle(): int
    {
        $host = env('DB_HOST', '127.0.0.1');
        $port = env('DB_PORT', '3306');
        $db   = env('DB_DATABASE');
        $user = env('MYSQL_ROOT_USER', 'root');

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

        // Prompt for root password (allow empty)
        $rootPass = $this->secret("Password MySQL untuk user '{$user}' (kosongkan bila tidak ada)");

        $this->info('Menjalankan restore…');
        // Build mysql command
        $command = "mysql -h {$host} -P {$port} -u {$user} " .
            (empty($rootPass) ? '' : "-p{$rootPass} ") .
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
