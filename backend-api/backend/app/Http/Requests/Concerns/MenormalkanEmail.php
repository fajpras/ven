<?php

namespace App\Http\Requests\Concerns;

use Illuminate\Support\Str;

trait MenormalkanEmail
{
    abstract protected function kolomEmail(): array;

    protected function prepareForValidation(): void
    {
        $this->merge(
            collect($this->kolomEmail())
                ->filter(fn (string $kolom): bool => is_string($this->input($kolom)))
                ->mapWithKeys(fn (string $kolom): array => [$kolom => Str::lower(trim($this->input($kolom)))])
                ->all()
        );
    }
}
