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
            'nama_panggilan' => ['nullable', 'string', 'max:40', 'alpha_dash:ascii', 'unique:pengguna,nama_panggilan'],
            'email' => ['required', 'email:rfc', 'max:254'],
            'kata_sandi' => AturanKataSandi::baru(),
            'otp' => ['required', 'digits:'.config('otp.panjang')],
        ];
    }
}
