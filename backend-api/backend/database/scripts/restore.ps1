param(
    [Parameter(Mandatory = $true, Position = 0)]
    [string]$File,
    [string]$DbName,
    [string]$User,
    [string]$Password,
    [switch]$Force
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

$mysql = "C:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysql.exe"

if (-not (Test-Path $mysql)) {
    $mysql = (Get-ChildItem "C:\laragon\bin\mysql" -Recurse -Filter "mysql.exe" | Select-Object -First 1).FullName
}

if (-not (Test-Path $File)) {
    Write-Error "File SQL tidak ditemukan: $File"
    exit 1
}

$fullPath = (Resolve-Path $File).Path
$sqlSourcePath = $fullPath -replace '\\', '/'

if (-not $Force) {
    Write-Warning "Restore akan menimpa seluruh data pada database '$DbName' menggunakan user [$User]."
    $konfirmasi = Read-Host "Ketik 'YA' untuk melanjutkan"
    if ($konfirmasi -ne 'YA') {
        Write-Host "Operasi dibatalkan."
        exit 0
    }
}

Write-Host "Mengimpor $fullPath ke [$DbName] menggunakan user [$User]..."

$baseArgs = @(
    "-h", "127.0.0.1",
    "-P", "3306",
    "-u", $User,
    "--default-character-set=utf8mb4"
)

if ($Password) {
    $baseArgs = @("-p$Password") + $baseArgs
}

& $mysql @baseArgs -e "source $sqlSourcePath" $DbName

if ($LASTEXITCODE -eq 0) {
    Write-Host "Restore database [$DbName] berhasil!" -ForegroundColor Green
} else {
    Write-Error "Restore gagal dengan exit code $LASTEXITCODE."
}
