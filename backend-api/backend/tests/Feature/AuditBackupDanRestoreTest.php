<?php

namespace Tests\Feature;

use Illuminate\Support\Facades\Artisan;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

/**
 * Audit & Test Kepatuhan:
 * 1. Backup Database (Manual & Otomatis)
 * 2. Restore Database Verification
 */
class AuditBackupDanRestoreTest extends TestCase
{
    /**
     * Test alur backup database menggunakan Artisan command ven:backup-db.
     */
    public function test_artisan_backup_database_berhasil(): void
    {
        $customDir = storage_path('framework/testing/audit_backups');
        if (! is_dir($customDir)) {
            mkdir($customDir, 0755, true);
        }

        $exitCode = Artisan::call('ven:backup-db', [
            '--path' => $customDir,
        ]);

        $this->assertSame(0, $exitCode, 'Command ven:backup-db harus mengembalikan exit code 0.');

        // Pastikan file .sql terbentuk
        $files = glob($customDir . '/backup_*.sql');
        $this->assertNotEmpty($files, 'File backup SQL harus terbentuk di direktori target.');

        // Periksa isi file backup tidak kosong dan berformat SQL valid
        $latestBackup = end($files);
        $content = file_get_contents($latestBackup);
        $this->assertStringContainsString('MySQL dump', $content, 'File harus berisi header MySQL dump yang valid.');
        $this->assertStringContainsString('TABLE', $content, 'File dump harus memuat struktur tabel.');

        // Cleanup
        @unlink($latestBackup);
    }

    /**
     * Test alur verifikasi restore database menggunakan ven:restore-db ke database target uji.
     */
    public function test_artisan_restore_database_berhasil(): void
    {
        $customDir = storage_path('framework/testing/audit_backups');
        if (! is_dir($customDir)) {
            mkdir($customDir, 0755, true);
        }

        // 1. Buat backup dulu
        Artisan::call('ven:backup-db', ['--path' => $customDir]);
        $files = glob($customDir . '/backup_*.sql');
        $this->assertNotEmpty($files);
        $backupFile = end($files);

        // 2. Lakukan restore ke database target terisolasi
        $exitCode = Artisan::call('ven:restore-db', [
            '--path' => $backupFile,
            '--db'   => 'ven_testing',
        ]);

        $this->assertSame(0, $exitCode, 'Command ven:restore-db harus selesai dengan sukses (exit code 0).');

        // Cleanup
        @unlink($backupFile);
    }

    protected function tearDown(): void
    {
        $customDir = storage_path('framework/testing/audit_backups');
        if (is_dir($customDir)) {
            $files = glob($customDir . '/*.sql');
            foreach ($files as $file) {
                @unlink($file);
            }
        }

        parent::tearDown();
    }
}

