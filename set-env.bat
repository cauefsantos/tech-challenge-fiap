@echo off
setlocal DisableDelayedExpansion
set "ENV_FILE=%~dp0.env"

if not exist "%ENV_FILE%" (
    echo Arquivo .env nao encontrado. Copie .env.example para .env e preencha os valores.
    endlocal & exit /b 1
)

for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%ENV_FILE%") do (
    set "%%A=%%B"
)

if not defined JWT_SECRET (
    echo Preencha JWT_SECRET no arquivo .env.
    endlocal & exit /b 1
)
if not defined ADMIN_USERNAME (
    echo Preencha ADMIN_USERNAME no arquivo .env.
    endlocal & exit /b 1
)
if not defined ADMIN_PASSWORD (
    echo Preencha ADMIN_PASSWORD no arquivo .env.
    endlocal & exit /b 1
)
if not defined DB_URL (
    echo Preencha DB_URL no arquivo .env.
    endlocal & exit /b 1
)
if not defined DB_USERNAME (
    echo Preencha DB_USERNAME no arquivo .env.
    endlocal & exit /b 1
)
if /I "%JWT_SECRET:~0,9%"=="SUBSTITUA" (
    echo Substitua o valor de JWT_SECRET no arquivo .env.
    endlocal & exit /b 1
)
if /I "%ADMIN_USERNAME:~0,9%"=="SUBSTITUA" (
    echo Substitua o valor de ADMIN_USERNAME no arquivo .env.
    endlocal & exit /b 1
)
if /I "%ADMIN_PASSWORD:~0,9%"=="SUBSTITUA" (
    echo Substitua o valor de ADMIN_PASSWORD no arquivo .env.
    endlocal & exit /b 1
)

endlocal & (
    for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%ENV_FILE%") do set "%%A=%%B"
)
echo Variaveis de ambiente carregadas do .env nesta janela.
