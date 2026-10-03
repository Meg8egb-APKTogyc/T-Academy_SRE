#!/bin/sh

set -eu

# --- Локации ---------------------------------------------------------------
# Схема (таблицы, индексы) — всегда.
# Демо-данные — только если SEED_DEMO=1 (регистронезависимо: 1/true/yes).
LOCATIONS="filesystem:/flyway/sql/migration"
case "$(echo "${SEED_DEMO:-}" | tr '[:upper:]' '[:lower:]')" in
    1|true|yes)
        LOCATIONS="${LOCATIONS},filesystem:/flyway/sql/seed"
        echo "[migrate] SEED_DEMO включён — демо-данные будут накатаны"
        ;;
    *)
        echo "[migrate] SEED_DEMO выключен — только схема"
        ;;
esac

echo "[migrate] FLYWAY_URL = ${FLYWAY_URL}"
echo "[migrate] command    = $*"

exec /flyway/flyway -locations="${LOCATIONS}" "$@"