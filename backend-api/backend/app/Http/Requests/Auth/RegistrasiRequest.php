<?php

namespace App\Http\Requests\Auth;

use App\Http\Requests\Concerns\MenormalkanEmail;
use App\Support\AturanKataSandi;
use Illuminate\Foundation\Http\FormRequest;

final class RegistrasiRequest extends FormRequest
{
    use MenormalkanEmail;

    public function authorize(): bool
    {
        return true;
    }

    protected function kolomEmail(): array
    {
        return ['email'];
    }

    public function rules(): array
    {
        return [
            'nama' => ['required', 'string', 'max:30'],
            'nama_panggilan' => [
                'required',
                'string',
                'min:3',
                'max:30',
                'regex:/^[A-Za-z0-9._]+$/',
                'unique:pengguna,nama_panggilan',
            ],
            'email' => ['required', 'email:rfc', 'max:254'],
            'kata_sandi' => AturanKataSandi::baru(),
            'otp' => ['required', 'digits:'.config('otp.panjang')],
        ];
    }

    public function messages(): array
    {
        return [
            'nama.required' => 'Nama wajib diisi.',
            'nama.max' => 'Nama maksimal 30 karakter.',
            'nama_panggilan.required' => 'Username wajib diisi.',
            'nama_panggilan.min' => 'Username minimal 3 karakter.',
            'nama_panggilan.max' => 'Username maksimal 30 karakter.',
            'nama_panggilan.regex' => 'Username hanya boleh berisi huruf, angka, titik, dan garis bawah.',
            'nama_panggilan.unique' => 'Username sudah digunakan.',
        ];
    }
}
