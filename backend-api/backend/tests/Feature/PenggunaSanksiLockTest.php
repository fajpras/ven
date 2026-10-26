<?php

namespace Tests\Feature;

use App\Models\Pengguna;
use App\Models\SanksiAkun;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class PenggunaSanksiLockTest extends TestCase
{
    protected function setUp(): void
    {
        parent::setUp();
        $this->assertSame(
            'ven_testing',
            DB::connection('mysql')->getDatabaseName(),
            'Test harus memakai database ven_testing, bukan database aplikasi.'
        );
    }

    public function test_sanksi_store_update_dan_cabut_berhasil_dengan_lock(): void
    {
        $admin = Pengguna::updateOrCreate(
            ['email' => 'admin-lock@dummy.test'],
            [
                'nama'           => 'Admin Lock',
                'nama_panggilan' => 'adminlock',
                'kata_sandi'     => Hash::make('password123'),
                'role'           => 'admin',
                'status'         => 'aktif',
            ]
        );

        $target = Pengguna::updateOrCreate(
            ['email' => 'target-lock@dummy.test'],
            [
                'nama'           => 'Target Lock',
                'nama_panggilan' => 'targetlock',
                'kata_sandi'     => Hash::make('password123'),
                'role'           => 'pengguna',
                'status'         => 'aktif',
            ]
        );

        DB::connection('mysql')->table('sanksi_akun')->where('id_pengguna', $target->id_pengguna)->delete();
        $target->forceFill(['status' => 'aktif'])->save();

        // 1. Terapkan sanksi (lock row target)
        $storeRes = $this->actingAs($admin)->postJson("/api/admin/pengguna/{$target->id_pengguna}/sanksi", [
            'jenis_sanksi' => 'ditangguhkan',
            'alasan'       => 'Pelanggaran pertama.',
            'mulai'        => now()->subMinute()->toDateTimeString(),
            'permanen'     => false,
            'berakhir'     => now()->addDays(3)->toDateTimeString(),
        ]);
        $storeRes->assertStatus(201);
        $this->assertSame('ditangguhkan', $target->fresh()->status);

        $idSanksi = $storeRes->json('data.id_sanksi');

        // 2. Perbarui sanksi jadi diblokir (lock row target)
        $updateRes = $this->actingAs($admin)->patchJson("/api/admin/sanksi/{$idSanksi}", [
            'jenis_sanksi' => 'diblokir',
            'alasan'       => 'Eskalasi ke pemblokiran.',
        ]);
        $updateRes->assertStatus(200);
        $this->assertSame('nonaktifkan', $target->fresh()->status);

        // 3. Cabut sanksi (lock row target)
        $cabutRes = $this->actingAs($admin)->deleteJson("/api/admin/sanksi/{$idSanksi}");
        $cabutRes->assertStatus(200);
        $this->assertSame('aktif', $target->fresh()->status);
    }
}
