<?php

namespace Database\Factories;

use App\Models\Pengguna;
use Illuminate\Database\Eloquent\Factories\Factory;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Str;

/**
 * @extends Factory<Pengguna>
 */
class PenggunaFactory extends Factory
{
    protected $model = Pengguna::class;

    public function definition(): array
    {
        return [
            'nama'           => fake()->name(),
            'nama_panggilan' => fake()->unique()->userName(),
            'email'          => fake()->unique()->safeEmail(),
            'kata_sandi'     => Hash::make('password'),
            'role'           => 'pengguna',
            'status'         => 'aktif',
        ];
    }

    public function admin(): static
    {
        return $this->state(fn (array $attributes) => [
            'role' => 'admin',
        ]);
    }

    public function denganGoogle(): static
    {
        return $this->state(fn (array $attributes) => [
            'google_id' => (string) fake()->unique()->numerify('#####################'),
        ]);
    }
}
