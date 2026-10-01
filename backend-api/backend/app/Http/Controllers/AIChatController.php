<?php

namespace App\Http\Controllers;

use App\Models\Karya;
use App\Models\Pameran;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Http;

class AIChatController extends Controller
{
    public function ask(Request $request)
    {
        $validated = $request->validate([
            'pertanyaan' => ['required', 'string', 'max:1000'],
        ]);

        $context = collect([
            'karya' => Karya::latest('created_at')->limit(10)->pluck('judul')->all(),
            'pameran' => Pameran::orderByDesc('id_pameran')->limit(10)->pluck('judul')->all(),
        ])->map(fn ($items, $key) => $key.': '.(implode(', ', $items) ?: '-'))
            ->implode("\n");

        $jawaban = $this->askAI($validated['pertanyaan'], $context);

        return $this->ok(['jawaban' => $jawaban]);
    }

    private function askAI(string $pertanyaan, string $context): string
    {
        if (! config('services.ai.key')) {
            return 'Layanan AI belum dikonfigurasi. Hubungi administrator.';
        }

        $response = Http::withToken(config('services.ai.key'))
            ->timeout(30)
            ->post(config('services.ai.url'), [
                'model' => config('services.ai.model'),
                'messages' => [
                    [
                        'role' => 'system',
                        'content' => 'Anda asisten untuk platform pameran virtual 3D bernama VEN. '
                            .'Jawab singkat dan jelas dalam Bahasa Indonesia, seputar karya dan pameran. '
                            ."Gunakan konteks berikut bila relevan:\n".$context,
                    ],
                    ['role' => 'user', 'content' => $pertanyaan],
                ],
            ]);

        if ($response->failed()) {
            return 'Maaf, layanan AI sedang tidak tersedia. Coba lagi nanti.';
        }

        return $response->json('choices.0.message.content', 'Tidak ada jawaban dari layanan AI.');
    }
}
