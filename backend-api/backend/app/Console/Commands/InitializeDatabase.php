<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use Throwable;

class InitializeDatabase extends Command
{
    /**
     * The name and signature of the console command.
     *
     * Options:
     *   --no-backup   Skip automatic backup of the current database before initializing.
     *   --path        Directory where backup file will be stored (passed to ven:backup-db).
     */
    protected $signature = 'ven:initialize-db {--no-backup : Skip backup before initializing} {--path= : Directory for backup file (optional)}';

    /**
     * The console command description.
     */
    protected $description = 'Full database initialization: optional backup, create DB/users/roles, and run migrations.';

    public function handle(): int
    {
        // ---------- Step 1: optional backup ----------
        if (! $this->option('no-backup')) {
            $this->info('Membuat backup database sebelum inisialisasi...');
            // Forward the --path option if provided
            $backupOptions = [];
            if ($this->option('path')) {
                $backupOptions['--path'] = $this->option('path');
            }
            $exitCode = $this->call('ven:backup-db', $backupOptions);
            if ($exitCode !== 0) {
                $this->error('Backup gagal – proses inisialisasi dibatalkan.');
                return self::FAILURE;
            }
        } else {
            $this->comment('Backup di‑skip karena ada opsi --no-backup.');
        }

        // ---------- Step 2: setup DB, users, roles ----------
        $this->info('Menjalankan ven:setup-db…');
        $exitCode = $this->call('ven:setup-db');
        if ($exitCode !== 0) {
            $this->error('Setup DB gagal – proses dibatalkan.');
            return self::FAILURE;
        }

        // ---------- Step 3: migrate schema ----------
        $this->info('Menjalankan migrasi schema…');
        $exitCode = $this->call('migrate', ['--database' => 'mysql_dba']);
        if ($exitCode !== 0) {
            $this->error('Migrasi gagal – proses selesai dengan error.');
            return self::FAILURE;
        }

        $this->info('Inisialisasi database selesai dengan sukses.');
        return self::SUCCESS;
    }
}
?>
