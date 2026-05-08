#!/bin/bash
# Создаёт дополнительные БД для микросервисов.
# Выполняется ТОЛЬКО при первом старте postgres на пустом volume.
# Если volume уже создан — нужно его удалить, чтобы скрипт сработал.

set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE auth_db;
    GRANT ALL PRIVILEGES ON DATABASE auth_db TO $POSTGRES_USER;
    CREATE DATABASE user_db;
    GRANT ALL PRIVILEGES ON DATABASE user_db TO $POSTGRES_USER;
    CREATE DATABASE nutrition_db;
    GRANT ALL PRIVILEGES ON DATABASE nutrition_db TO $POSTGRES_USER;
EOSQL

echo "[init] auth_db, user_db and nutrition_db created and granted to $POSTGRES_USER"
