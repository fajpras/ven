<?php

/*
 * ---------------------------------------------------------------
 *  Command: ven:backup-db
 * ---------------------------------------------------------------
 * Usage via CLI:
 *   php artisan ven:backup-db                     # backup ke folder default
 *   php artisan ven:backup-db --path=storage/app/custom   # backup ke folder custom
 *
 * Usage programmatically (e.g. from another command):
 *   \Illuminate\Support\Facades\Artisan::call('ven:backup-db');
 *   // atau dengan opsi:
 *   \Illuminate\Support\Facades\Artisan::call('ven:backup-db', ['--path' => 'storage/app/custom']);
 *
 * Penjelasan:
 *   - Membuat dump MySQL dengan mysqldump.
 *   - Lokasi default diambil dari config('backup.path').
 *   - File bernama backup_{db}_{timestamp}.sql.
 * ---------------------------------------------------------------
 */

namespace App\Console\Commands;

use Illuminate\Console\Command;
use PDO;
use Throwable;

class BackupDatabase extends Command
{
    /**
     * The name and signature of the console command.
     *
     * Options:
     *   --path : directory where the backup file will be stored (default storage/app/backups)
     */
    protected $signature = 'ven:backup-db {--path= : Directory to store the backup file}';

    /**
     * The console command description.
     */
    protected $description = 'Backup MySQL database using mysqldump. Stores the dump in the specified directory.';

    public function handle(): int
    {
        $host = env('DB_HOST', '127.0.0.1');
        $port = env('DB_PORT', '3306');
        $db   = env('DB_DATABASE');
        $user = env('DB_DBA_USERNAME', 'dba');

        if (! $db) {
            $this->error('Variable DB_DATABASE tidak di‑set di .env');
            return self::FAILURE;
        }

        $pass = env('DB_DBA_PASSWORD');
        if ($pass === null || $pass === '') {
            $pass = (string) $this->secret("Password MySQL untuk user '{$user}' (kosongkan bila tidak ada)");
        }

        $dir = $this->option('path') ?? config('backup.path', storage_path('app/backups'));
        if (! is_dir($dir)) {
            if (! mkdir($dir, 0755, true) && ! is_dir($dir)) {
                $this->error("Tidak dapat membuat direktori backup: {$dir}");
                return self::FAILURE;
            }
        }
        $timestamp = now()->format('Ymd_His');
        $file = rtrim($dir, DIRECTORY_SEPARATOR) . DIRECTORY_SEPARATOR . "backup_{$db}_{$timestamp}.sql";

        $binary = env('MYSQL_DUMP_PATH');
        if (! $binary) {
            $laragonDumps = glob('C:/laragon/bin/mysql/*/bin/mysqldump.exe');
            $binary = ! empty($laragonDumps) ? end($laragonDumps) : 'mysqldump';
        }

        $command = escapeshellarg($binary) . " -h {$host} -P {$port} -u {$user} " .
            (empty($pass) ? '' : "-p{$pass} ") .
            "{$db} > " . escapeshellarg($file);

        $this->info('Menjalankan backup…');
        exec($command, $output, $returnVar);

        if ($returnVar !== 0) {
            $this->error('Backup gagal. Periksa apakah mysqldump terinstal dan kredensial benar.');
            return self::FAILURE;
        }

        $this->info("Backup berhasil: {$file}");
        return self::SUCCESS;
    }
}
?>
