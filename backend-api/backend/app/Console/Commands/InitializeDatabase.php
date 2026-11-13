<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use Throwable;

class InitializeDatabase extends Command
{
    protected $signature = 'ven:initialize-db {--fresh : Setup ulang database dari nol} {--no-backup : Skip backup before initializing} {--path= : Directory for backup file (optional)}';
    protected $description = 'Full database initialization: optional backup, create DB/users/roles, and run migrations.';

    public function handle(): int
    {
        if (! $this->option('no-backup') && ! $this->option('fresh')) {
            $this->info('Membuat backup database sebelum inisialisasi...');
            $backupOptions = [];
            if ($this->option('path')) {
                $backupOptions['--path'] = $this->option('path');
            }
            $exitCode = $this->call('ven:backup-db', $backupOptions);
            if ($exitCode !== 0) {
                $this->error('Backup gagal – proses inisialisasi dibatalkan.');
                return self::FAILURE;
            }
        } elseif ($this->option('fresh')) {
            $this->comment('Setup dari nol (--fresh): backup dilewati.');
        } else {
            $this->comment('Backup di‑skip karena ada opsi --no-backup.');
        }

        $this->info('Menjalankan ven:setup-db…');
        $setupOptions = [];
        if ($this->option('fresh')) {
            $setupOptions['--fresh'] = true;
        }
        $exitCode = $this->call('ven:setup-db', $setupOptions);
        if ($exitCode !== 0) {
            $this->error('Setup DB gagal – proses dibatalkan.');
            return self::FAILURE;
        }

        $this->info('Menjalankan migrasi schema…');
        $exitCode = $this->call('migrate', ['--database' => 'mysql_dba']);
        if ($exitCode !== 0) {
            $this->error('Migrasi gagal – proses selesai dengan error.');
            return self::FAILURE;
        }

        $this->info('Menyelaraskan hak akses granular per tabel/view…');
        $exitCode = $this->call('ven:setup-db');
        if ($exitCode !== 0) {
            $this->warn('Peringatan: penyelarasan hak akses granular mengembalikan status non-zero.');
        }

        $this->info('Inisialisasi database selesai dengan sukses.');
        return self::SUCCESS;
    }
}
?>
