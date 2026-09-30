<?php

namespace App\Models\Concerns;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Support\Str;
use Ramsey\Uuid\Uuid;

/**
 * Kolom BINARY(16) berisi UUIDv7.
 *
 * - Atribut disimpan mentah (16 byte) supaya relasi Eloquent tetap jalan.
 * - Mengisi atribut dengan string UUID otomatis diubah jadi biner.
 * - toArray()/JSON otomatis menampilkan string UUID.
 * - Primary key non-increment otomatis diisi UUIDv7 saat create.
 *
 * Model wajib mendeklarasikan: protected array $uuidColumns = [...];
 */
trait HasBinaryUuid
{
    public static function bootHasBinaryUuid(): void
    {
        static::creating(function ($model) {
            $key = $model->getKeyName();

            if (! $model->getIncrementing()
                && in_array($key, $model->getUuidColumns(), true)
                && empty($model->getAttributes()[$key])) {
                $model->setAttribute($key, Str::uuid7()->getBytes());
            }
        });
    }

    public function getUuidColumns(): array
    {
        return property_exists($this, 'uuidColumns') ? $this->uuidColumns : [];
    }

    public static function uuidToBytes(string $uuid): string
    {
        return Uuid::fromString($uuid)->getBytes();
    }

    public static function bytesToUuid(string $bytes): string
    {
        return Uuid::fromBytes($bytes)->toString();
    }

    /** Ambil nilai kolom UUID sebagai string, mis. $model->uuid('id_pengguna'). */
    public function uuid(?string $column = null): ?string
    {
        $value = $this->getAttributes()[$column ?? $this->getKeyName()] ?? null;

        return is_string($value) && strlen($value) === 16 ? static::bytesToUuid($value) : $value;
    }

    public function setAttribute($key, $value)
    {
        if (is_string($value)
            && in_array($key, $this->getUuidColumns(), true)
            && Uuid::isValid($value)) {
            $value = Uuid::fromString($value)->getBytes();
        }

        return parent::setAttribute($key, $value);
    }

    public function attributesToArray()
    {
        $attributes = parent::attributesToArray();

        foreach ($this->getUuidColumns() as $column) {
            if (isset($attributes[$column])
                && is_string($attributes[$column])
                && strlen($attributes[$column]) === 16) {
                $attributes[$column] = static::bytesToUuid($attributes[$column]);
            }
        }

        return $attributes;
    }

    public function getRouteKey()
    {
        return $this->uuid($this->getRouteKeyName());
    }

    public function resolveRouteBinding($value, $field = null)
    {
        $field ??= $this->getRouteKeyName();

        if (in_array($field, $this->getUuidColumns(), true)) {
            if (! Uuid::isValid((string) $value)) {
                return null;
            }
            $value = Uuid::fromString($value)->getBytes();
        }

        return $this->where($field, $value)->first();
    }

    /** Model::whereUuid('id_pengguna', $uuidString)->first() */
    public function scopeWhereUuid(Builder $query, string $column, string $uuid): Builder
    {
        return $query->where($column, static::uuidToBytes($uuid));
    }

    /** Model::findUuid($uuidString) */
    public static function findUuid(string $uuid): ?static
    {
        if (! Uuid::isValid($uuid)) {
            return null;
        }

        return static::query()->find(static::uuidToBytes($uuid));
    }
}