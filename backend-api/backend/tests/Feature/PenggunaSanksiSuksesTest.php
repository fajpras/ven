<?php

namespace Tests\Feature;

use App\Models\Pengguna;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class PenggunaSanksiSuksesTest extends TestCase
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

    public function test_berhasil_memberikan_sanksi_kepada_pengguna(): void
    {
        $admin = Pengguna::updateOrCreate(
            ['email' => 'admin@dummy.test'],
            [
                'nama'           => 'Admin Uji',
                'nama_panggilan' => 'adminuji',
                'kata_sandi'     => Hash::make('password123'),
                'role'           => 'admin',
                'status'         => 'aktif',
            ]
        );

        $target = Pengguna::updateOrCreate(
            ['email' => 'target@dummy.test'],
            [
                'nama'           => 'Target Uji',
                'nama_panggilan' => 'targetuji',
                'kata_sandi'     => Hash::make('password123'),
                'role'           => 'pengguna',
                'status'         => 'aktif',
            ]
        );

        DB::connection('mysql')->table('sanksi_akun')
            ->where('id_pengguna', $target->id_pengguna)->delete();
        $target->forceFill(['status' => 'aktif'])->save();

        $payload = [
            'jenis_sanksi' => 'ditangguhkan', 
            'alasan'       => 'Melanggar syarat dan ketentuan.',
            'mulai'        => now()->toDateTimeString(),
            'permanen'     => false,
            'berakhir'     => now()->addDays(5)->toDateTimeString(),
        ];

        $response = $this->actingAs($admin)
            ->postJson("/api/admin/pengguna/{$target->id_pengguna}/sanksi", $payload);

        $response->assertStatus(201);
        $this->assertDatabaseHas('sanksi_akun', [
            'id_pengguna'  => $target->id_pengguna,
            'jenis_sanksi' => 'ditangguhkan',
            'alasan'       => 'Melanggar syarat dan ketentuan.',
        ], 'mysql');

        $this->assertDatabaseHas('pengguna', [
            'id_pengguna' => $target->id_pengguna,
            'status'      => 'ditangguhkan',
        ], 'mysql');

    }
}