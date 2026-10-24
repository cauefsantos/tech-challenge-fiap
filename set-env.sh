#!/usr/bin/env bash

if [[ "${BASH_SOURCE[0]}" == "$0" ]]; then
  echo "Este script precisa ser carregado no shell: source ./set-env.sh" >&2
  exit 1
fi

env_file="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)/.env"
if [[ ! -f "$env_file" ]]; then
  echo "Arquivo .env não encontrado. Copie .env.example para .env e preencha os valores." >&2
  return 1
fi

while IFS= read -r line || [[ -n "$line" ]]; do
  line="${line%$'\r'}"
  [[ -z "${line//[[:space:]]/}" || "$line" =~ ^[[:space:]]*# ]] && continue
  if [[ "$line" != *=* ]]; then
    echo "Linha inválida no .env. Use somente KEY=VALUE sem aspas." >&2
    return 1
  fi

  key="${line%%=*}"
  value="${line#*=}"
  case "$key" in
    JWT_SECRET|JWT_TOKEN_VALIDITY|ADMIN_USERNAME|ADMIN_PASSWORD|SERVER_PORT|SPRING_PROFILES_ACTIVE|DB_URL|DB_USERNAME|DB_PASSWORD|DB_POOL_MIN_IDLE|DB_POOL_MAX_SIZE|DB_CONNECTION_TIMEOUT_MS|DB_VALIDATION_TIMEOUT_MS|DB_IDLE_TIMEOUT_MS|DB_MAX_LIFETIME_MS|FLYWAY_CONNECT_RETRIES|OPENAPI_ENABLED|SWAGGER_UI_ENABLED)
      export "$key=$value"
      ;;
    *)
      echo "Variável não reconhecida no .env: $key" >&2
      return 1
      ;;
  esac
done < "$env_file"

for required in JWT_SECRET ADMIN_USERNAME ADMIN_PASSWORD DB_URL DB_USERNAME; do
  if [[ -z "${!required}" || "${!required}" == SUBSTITUA_* ]]; then
    echo "Preencha a variável obrigatória $required no arquivo .env." >&2
    return 1
  fi
done

password_bytes="$(printf '%s' "$ADMIN_PASSWORD" | wc -c | tr -d '[:space:]')"
if (( ${#ADMIN_PASSWORD} < 12 || password_bytes > 72 )); then
  echo "ADMIN_PASSWORD deve conter pelo menos 12 caracteres e no máximo 72 bytes UTF-8." >&2
  return 1
fi

echo "Variáveis de ambiente carregadas do .env neste shell."
