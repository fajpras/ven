<?php

namespace App\Http\Controllers\Pengguna;

use App\Http\Controllers\Controller;
use App\Models\Karya;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Storage;

class KaryaController extends Controller
{
    public function index(Request $request)
    {
        $karya = Karya::with(['kategori', 'pengguna', 'pameran'])
            ->when($request->integer('id_kategori'), fn ($q, $v) => $q->where('id_kategori', $v))
            ->when($request->integer('id_pameran'), fn ($q, $v) => $q->where('id_pameran', $v))
            ->when($request->enum('jenis', ['poster', 'video']), fn ($q, $v) => $q->where('jenis', $v))
            ->when($request->string('judul'), fn ($q, $v) => $q->where('judul', 'like', "%{$v}%"))
            ->latest('created_at')
            ->paginate(24);

        return $this->ok($karya);
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'id_kategori' => ['required', 'integer', 'exists:kategori,id_kategori'],
            'id_pameran' => ['nullable', 'integer', 'exists:pameran,id_pameran'],
            'id_objek' => ['nullable', 'integer', 'exists:objek,id_objek'],
            'judul' => ['required', 'string', 'max:255'],
            'jenis' => ['required', 'string', 'in:poster,video'],
            'deskripsi' => ['required', 'string'],
            'file' => ['required', 'file', 'max:51200'],
        ]);

        $validated['file'] = $request->file('file')->store('karya', 'local');
        $validated['id_pengguna'] = $request->user()->id_pengguna;

        $karya = Karya::create($validated);

        return $this->created($karya, 'Karya dibuat.');
    }

    public function show(Request $request, Karya $karya)
    {
        return $this->ok($karya->load(['kategori', 'pengguna', 'pameran', 'objek']));
    }

    public function update(Request $request, string $karya)
    {
        $karya = $request->user()->karya()->whereUuid('id_karya', $karya)->firstOrFail();

        $validated = $request->validate([
            'id_kategori' => ['sometimes', 'integer', 'exists:kategori,id_kategori'],
            'id_pameran' => ['nullable', 'integer', 'exists:pameran,id_pameran'],
            'id_objek' => ['nullable', 'integer', 'exists:objek,id_objek'],
            'judul' => ['sometimes', 'string', 'max:255'],
            'jenis' => ['sometimes', 'string', 'in:poster,video'],
            'deskripsi' => ['sometimes', 'string'],
            'file' => ['sometimes', 'file', 'max:51200'],
        ]);

        if (isset($validated['file'])) {
            Storage::disk('local')->delete($karya->file);
            $validated['file'] = $request->file('file')->store('karya', 'local');
        }

        $karya->forceFill($validated)->save();

        return $this->ok($karya, 'Karya diperbarui.');
    }

    public function destroy(Request $request, string $karya)
    {
        $karya = $request->user()->karya()->whereUuid('id_karya', $karya)->firstOrFail();

        $karya->delete();

        return $this->ok(null, 'Karya dihapus.');
    }
}
