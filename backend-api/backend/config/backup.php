<?php
return [
    /*
    |--------------------------------------------------------------------------
    | Backup Configuration
    |--------------------------------------------------------------------------
    | Define a global backup directory that can be referenced throughout the
    | application (including the `ven:backup-db` Artisan command).  The value
    | can be overridden via the `BACKUP_PATH` environment variable.
    */
    'path' => env('BACKUP_PATH', storage_path('app/backups')),
];
?>
