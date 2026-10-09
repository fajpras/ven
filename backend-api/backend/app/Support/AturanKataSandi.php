<?php

namespace App\Support;

use Illuminate\Validation\Rules\Password;

final class AturanKataSandi
{
    public static function baru(): array
    {
        return ['required', 'string', 'max:72', Password::min(8)->letters()->numbers()];
    }
}
