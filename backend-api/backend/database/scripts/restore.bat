@echo off
setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "BACKEND_DIR=%SCRIPT_DIR%.."
set "ENV_FILE=%BACKEND_DIR%\.env"

set "SQL_FILE=%~1"
set "FORCE_FLAG=%~2"

if "%SQL_FILE%"=="" (
    echo [ERROR] Masukkan file SQL yang ingin di-restore!
    echo Contoh penggunaan:
    echo   restore.bat "C:\path\ke\file.sql"
    echo   restore.bat "C:\path\ke\file.sql" --force
    exit /b 1
)

if not exist "%SQL_FILE%" (
    echo [ERROR] File SQL tidak ditemukan: "%SQL_FILE%"
    exit /b 1
)

:: Normalisasi path absolut file SQL
for %%I in ("%SQL_FILE%") do set "SQL_FILE=%%~fI"

:: Default values
set "DB_NAME=ven_restore"
set "DB_USER=ven_dba"
set "DB_PASS=123"

:: Baca konfigurasi dari .env
@REM  if exist "%ENV_FILE%" (
@REM      for /f "usebackq tokens=1,* delims==" %%A in ("%ENV_FILE%") do (
@REM          set "KEY=%%A"
@REM          set "VAL=%%B"
@REM          for /f "tokens=* delims= " %%K in ("!KEY!") do set "KEY=%%K"
@REM          for /f "tokens=* delims= " %%V in ("!VAL!") do set "VAL=%%V"
@REM          set "VAL=!VAL:"=!"

@REM          if "!KEY!"=="DB_DATABASE" if not "!VAL!"=="" set "DB_NAME=!VAL!"
@REM          if "!KEY!"=="DB_DBA_USERNAME" if not "!VAL!"=="" set "DB_USER=!VAL!"
@REM          if "!KEY!"=="DB_DBA_PASSWORD" if not "!VAL!"=="" set "DB_PASS=!VAL!"
@REM      )
@REM  )

:: Cari binary mysql.exe
set "MYSQL=C:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysql.exe"
if not exist "%MYSQL%" (
    for /f "delims=" %%I in ('dir /b /s "C:\laragon\bin\mysql\mysql.exe" 2^>nul') do (
        set "MYSQL=%%I"
        goto :found_mysql
    )
)
:found_mysql

if not exist "%MYSQL%" (
    echo [ERROR] mysql.exe tidak ditemukan di C:\laragon\bin\mysql\
    exit /b 1
)

:: Peringatan dan konfirmasi
if not "%FORCE_FLAG%"=="--force" (
    echo [PERINGATAN] Restore akan menimpa seluruh tabel pada database [%DB_NAME%]!
    set /p "CONFIRM=Ketik 'YA' untuk melanjutkan: "
    if not "!CONFIRM!"=="YA" (
        echo Operasi dibatalkan.
        exit /b 0
    )
)

echo Mengimpor "%SQL_FILE%" ke [%DB_NAME%] menggunakan user [%DB_USER%]...

:: Ganti backslash dengan forward slash untuk source command MySQL
set "SQL_FILE_FOR_MYSQL=%SQL_FILE:\=/%"

"%MYSQL%" -h 127.0.0.1 -P 3306 -u %DB_USER% -p%DB_PASS% --default-character-set=utf8mb4 -e "source %SQL_FILE_FOR_MYSQL%" %DB_NAME%

if %ERRORLEVEL% equ 0 (
    echo [SUKSES] Restore ke database [%DB_NAME%] selesai!
    exit /b 0
) else (
    echo [ERROR] Restore gagal dengan exit code: %ERRORLEVEL%
    exit /b %ERRORLEVEL%
)
