param(
    [string]$DbName,
    [string]$OutputDir = "$PSScriptRoot\..\backups",
    [string]$User,
    [string]$Password
)

$ErrorActionPreference = "Stop"

# Ambil konfigurasi dari .env jika parameter tidak diisi
$envFile = Join-Path $PSScriptRoot "..\.env"
function Get-EnvVal($key, $default) {
    if (Test-Path $envFile) {
        $line = Get-Content $envFile | Where-Object { $_ -match "^\s*$key\s*=\s*(.*)$" } | Select-Object -First 1
        if ($line -match "^\s*$key\s*=\s*`"?(.*?)`"?\s*$") { return $matches[1] }
    }
    return $default
}

if (-not $DbName) { $DbName = Get-EnvVal "DB_DATABASE" "ven" }
if (-not $User) { $User = Get-EnvVal "DB_DBA_USERNAME" "ven_dba" }
if (-not $Password) { $Password = Get-EnvVal "DB_DBA_PASSWORD" "123" }

$timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$mysqldump = "C:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysqldump.exe"

if (-not (Test-Path $mysqldump)) {
    $mysqldump = (Get-ChildItem "C:\laragon\bin\mysql" -Recurse -Filter "mysqldump.exe" | Select-Object -First 1).FullName
}

if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
}

$targetFile = Join-Path (Resolve-Path $OutputDir).Path "${DbName}_${timestamp}.sql"

Write-Host "Memulai backup database [$DbName] menggunakan user [$User]..."

# Parameter dump:
# --no-tablespaces dibutuhkan jika user dba tidak memiliki hak global PROCESS
$cmdArgs = @(
    "-h", "127.0.0.1",
    "-P", "3306",
    "-u", $User,
    "--single-transaction",
    "--no-tablespaces",
    "--routines",
    "--triggers",
    $DbName,
    "--result-file=$targetFile"
)

if ($Password) {
    $cmdArgs = @("-p$Password") + $cmdArgs
}

& $mysqldump @cmdArgs

if ($LASTEXITCODE -eq 0 -and (Test-Path $targetFile)) {
    $sizeMB = (Get-Item $targetFile).Length / 1MB
    Write-Host ("Backup selesai: {0} ({1:N2} MB)" -f $targetFile, $sizeMB) -ForegroundColor Green
} else {
    Write-Error "Backup gagal."
}
