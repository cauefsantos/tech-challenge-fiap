$envFile = Join-Path $PSScriptRoot ".env"
if (-not (Test-Path -LiteralPath $envFile -PathType Leaf)) {
    throw "Arquivo .env nao encontrado. Copie .env.example para .env e preencha os valores antes de carregar."
}

$allowedNames = @(
    "JWT_SECRET", "JWT_TOKEN_VALIDITY", "ADMIN_USERNAME", "ADMIN_PASSWORD",
    "SERVER_PORT", "SPRING_PROFILES_ACTIVE", "DB_URL", "DB_USERNAME", "DB_PASSWORD",
    "DB_POOL_MIN_IDLE", "DB_POOL_MAX_SIZE", "DB_CONNECTION_TIMEOUT_MS",
    "DB_VALIDATION_TIMEOUT_MS", "DB_IDLE_TIMEOUT_MS", "DB_MAX_LIFETIME_MS",
    "FLYWAY_CONNECT_RETRIES", "OPENAPI_ENABLED", "SWAGGER_UI_ENABLED"
)
$values = @{}

foreach ($line in Get-Content -LiteralPath $envFile) {
    if ([string]::IsNullOrWhiteSpace($line) -or $line.TrimStart().StartsWith("#")) {
        continue
    }
    if ($line -notmatch '^([A-Za-z_][A-Za-z0-9_]*)=(.*)$') {
        throw "Linha invalida no .env. Use somente KEY=VALUE sem aspas."
    }

    $name = $Matches[1]
    if ($allowedNames -notcontains $name) {
        throw "Variavel nao reconhecida no .env: $name"
    }
    $values[$name] = $Matches[2]
}

foreach ($name in @("JWT_SECRET", "ADMIN_USERNAME", "ADMIN_PASSWORD", "DB_URL", "DB_USERNAME")) {
    if (-not $values.ContainsKey($name) -or [string]::IsNullOrWhiteSpace($values[$name]) -or $values[$name] -like "SUBSTITUA_*") {
        throw "Preencha a variavel obrigatoria $name no arquivo .env."
    }
}

if ($values["ADMIN_PASSWORD"].Length -lt 12 -or [Text.Encoding]::UTF8.GetByteCount($values["ADMIN_PASSWORD"]) -gt 72) {
    throw "ADMIN_PASSWORD deve conter pelo menos 12 caracteres e no maximo 72 bytes UTF-8."
}

foreach ($name in $values.Keys) {
    [Environment]::SetEnvironmentVariable($name, $values[$name], "Process")
}

Write-Host "Variaveis de ambiente carregadas do .env neste processo."
