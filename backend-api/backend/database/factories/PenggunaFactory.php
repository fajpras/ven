<?php

namespace Database\Factories;

use App\Models\Pengguna;
use Illuminate\Database\Eloquent\Factories\Factory;
use Illuminate\Support\Facades\Hash;

/**
 * @extends Factory<Pengguna>
 */
class PenggunaFactory extends Factory
{
    protected $model = Pengguna::class;

    public function definition(): array
    {
        return [
            'nama' => fake()->name(),
            'nama_panggilan' => fake()->unique()->userName(),
            'email' => fake()->unique()->safeEmail(),
            'kata_sandi' => Hash::make('password'),
            'role' => 'pengguna',
            'status' => 'aktif',
        ];
    }

    public function admin(): static
    {
        return $this->state(fn (array $attributes) => [
            'role' => 'admin',
        ]);
    }

    /**
     * Akun yang masuk melalui Google tidak memiliki kata sandi lokal.
     */
    public function denganGoogle(): static
    {
        return $this->state(fn (array $attributes) => [
            'kata_sandi' => null,
        ]);
    }
}
