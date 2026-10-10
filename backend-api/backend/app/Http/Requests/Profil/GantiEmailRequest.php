<?php

namespace App\Http\Requests\Profil;

use App\Http\Requests\Concerns\MenormalkanEmail;
use Illuminate\Foundation\Http\FormRequest;

final class GantiEmailRequest extends FormRequest
{
    use MenormalkanEmail;

    public function authorize(): bool
    {
        return true;
    }

    protected function kolomEmail(): array
    {
        return ['email_baru'];
    }

    public function rules(): array
    {
        return [
            'email_baru' => ['required', 'email:rfc', 'max:254', 'unique:pengguna,email'],
            'otp' => ['required', 'digits:'.config('otp.panjang')],
        ];
    }
}
