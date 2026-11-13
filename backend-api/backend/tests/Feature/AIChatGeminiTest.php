<?php

namespace Tests\Feature;

use App\Models\Pengguna;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\Config;
use Illuminate\Support\Facades\Http;
use Tests\TestCase;

class AIChatGeminiTest extends TestCase
{
    use DatabaseTransactions;

    protected Pengguna $user;

    protected function setUp(): void
    {
        parent::setUp();

        $this->user = Pengguna::factory()->create([
            'role' => 'pengguna',
            'status' => 'aktif',
        ]);
    }

    public function test_ai_chat_berhasil_menggunakan_google_ai_studio_gemini(): void
    {
        Config::set('services.ai.key', 'fake-gemini-api-key');
        Config::set('services.ai.model', 'gemini-1.5-flash');
        Config::set('services.ai.url', null);

        Http::fake([
            'generativelanguage.googleapis.com/*' => Http::response([
                'candidates' => [
                    [
                        'content' => [
                            'parts' => [
                                ['text' => 'Halo! Ini adalah jawaban dari Google Gemini AI Studio tentang pameran virtual VEN.'],
                            ],
                        ],
                    ],
                ],
            ], 200),
        ]);

        $response = $this->actingAs($this->user)
            ->postJson('/api/ai/tanya', [
                'pertanyaan' => 'Ceritakan tentang karya pameran 3D yang ada.',
            ]);

        $response->assertStatus(200)
            ->assertJsonPath('data.jawaban', 'Halo! Ini adalah jawaban dari Google Gemini AI Studio tentang pameran virtual VEN.');

        Http::assertSent(function ($request) {
            return str_contains($request->url(), 'generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent')
                && $request->hasHeader('x-goog-api-key', 'fake-gemini-api-key')
                && isset($request['contents'][0]['parts'][0]['text']);
        });
    }

    public function test_ai_chat_menangani_ketiadaan_api_key(): void
    {
        Config::set('services.ai.key', null);

        $response = $this->actingAs($this->user)
            ->postJson('/api/ai/tanya', [
                'pertanyaan' => 'Halo AI',
            ]);

        $response->assertStatus(200)
            ->assertJsonPath('data.jawaban', 'Layanan AI belum dikonfigurasi. Hubungi administrator.');
    }

    public function test_ai_chat_menangani_kegagalan_layanan_gemini(): void
    {
        Config::set('services.ai.key', 'fake-gemini-api-key');

        Http::fake([
            'generativelanguage.googleapis.com/*' => Http::response('Service Unavailable', 503),
        ]);

        $response = $this->actingAs($this->user)
            ->postJson('/api/ai/tanya', [
                'pertanyaan' => 'Halo',
            ]);

        $response->assertStatus(200)
            ->assertJsonPath('data.jawaban', 'Maaf, layanan AI sedang tidak tersedia. Coba lagi nanti.');
    }
}

