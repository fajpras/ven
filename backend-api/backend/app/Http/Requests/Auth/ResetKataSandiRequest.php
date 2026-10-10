<?php

namespace App\Http\Requests\Auth;

use App\Http\Requests\Concerns\MenormalkanEmail;
use App\Support\AturanKataSandi;
use Illuminate\Foundation\Http\FormRequest;

final class ResetKataSandiRequest extends FormRequest
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
            'email' => ['required', 'email:rfc', 'max:254'],
            'otp' => ['required', 'digits:'.config('otp.panjang')],
            'kata_sandi' => AturanKataSandi::baru(),
        ];
    }
}
