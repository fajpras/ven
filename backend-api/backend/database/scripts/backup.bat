@echo off
setlocal enabledelayedexpansion

:: Tentukan direktori script dan root backend
set "SCRIPT_DIR=%~dp0"
set "BACKEND_DIR=%SCRIPT_DIR%.."
set "ENV_FILE=%BACKEND_DIR%\.env"
@REM  set "OUTPUT_DIR=%BACKEND_DIR%\backups"
set "OUTPUT_DIR=C:\users\%USERNAME%\Documents\ven-backup"

:: Default values
set "DB_NAME=ven"
set "DB_USER=ven_dba"
set "DB_PASS=123"

:: Baca konfigurasi dari .env jika ada
if exist "%ENV_FILE%" (
    for /f "usebackq tokens=1,* delims==" %%A in ("%ENV_FILE%") do (
        set "KEY=%%A"
        set "VAL=%%B"
        for /f "tokens=* delims= " %%K in ("!KEY!") do set "KEY=%%K"
        for /f "tokens=* delims= " %%V in ("!VAL!") do set "VAL=%%V"
        set "VAL=!VAL:"=!"

        if "!KEY!"=="DB_DATABASE" if not "!VAL!"=="" set "DB_NAME=!VAL!"
        if "!KEY!"=="DB_DBA_USERNAME" if not "!VAL!"=="" set "DB_USER=!VAL!"
        if "!KEY!"=="DB_DBA_PASSWORD" if not "!VAL!"=="" set "DB_PASS=!VAL!"
    )
)

:: Override jika diberikan via argument: backup.bat [db_name]
if not "%~1"=="" set "DB_NAME=%~1"

:: Cari binary mysqldump Laragon
set "MYSQLDUMP=C:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysqldump.exe"
if not exist "%MYSQLDUMP%" (
    for /f "delims=" %%I in ('dir /b /s "C:\laragon\bin\mysql\mysqldump.exe" 2^>nul') do (
        set "MYSQLDUMP=%%I"
        goto :found_dump
    )
)
:found_dump

if not exist "%MYSQLDUMP%" (
    echo [ERROR] mysqldump.exe tidak ditemukan di C:\laragon\bin\mysql\
    exit /b 1
)

:: Pastikan folder backups ada
if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

:: Dapatkan timestamp format YYYY-MM-DD_HH-mm-ss via wmic atau powershell fallback
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value 2^>nul') do set "LDT=%%I"
if defined LDT (
    set "TIMESTAMP=!LDT:~0,4!-!LDT:~4,2!-!LDT:~6,2!_!LDT:~8,2!-!LDT:~10,2!-!LDT:~12,2!"
) else (
    for /f %%I in ('powershell -NoProfile -Command "Get-Date -Format yyyy-MM-dd_HH-mm-ss"') do set "TIMESTAMP=%%I"
)

set "TARGET_FILE=%OUTPUT_DIR%\%DB_NAME%_%TIMESTAMP%.sql"

echo Memulai backup database [%DB_NAME%] menggunakan user [%DB_USER%]...

"%MYSQLDUMP%" -h 127.0.0.1 -P 3306 -u %DB_USER% -p%DB_PASS% --single-transaction --no-tablespaces --routines --triggers %DB_NAME% --result-file="%TARGET_FILE%"

if %ERRORLEVEL% equ 0 (
    if exist "%TARGET_FILE%" (
        echo [SUKSES] Backup tersimpan di: "%TARGET_FILE%"
        exit /b 0
    )
)

echo [ERROR] Backup gagal!
exit /b 1
