#!/usr/bin/env bash
set -euo pipefail

EC2_HOST="${EC2_HOST:-54.80.196.186}"
EC2_USER="${EC2_USER:-ubuntu}"
EC2_KEY="${EC2_KEY:-$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/newsense.pem}"
REMOTE_APP_DIR="${REMOTE_APP_DIR:-/home/ubuntu/newsense}"

APP_SERVICE="${APP_SERVICE:-app}"
MYSQL_SERVICE="${MYSQL_SERVICE:-mysql}"
MONGO_SERVICE="${MONGO_SERVICE:-mongodb}"
CRAWL_TARGET="${CRAWL_TARGET:-200}"
CRAWL_TIMEOUT_SECONDS="${CRAWL_TIMEOUT_SECONDS:-1800}"
CRAWL_POLL_SECONDS="${CRAWL_POLL_SECONDS:-15}"
CONFIRM="${CONFIRM:-}"

if [[ ! -f "$EC2_KEY" ]]; then
  echo "EC2 key not found: $EC2_KEY" >&2
  exit 1
fi

if [[ "$CONFIRM" != "RESET-CRAWL" ]]; then
  echo "This will delete every MySQL table listed in docs/wipe-db-except-users.sql"
  echo "and drop MongoDB article_content on EC2 host $EC2_HOST."
  echo "User tables are preserved by the SQL file."
  read -r -p "Type RESET-CRAWL to continue: " CONFIRM
fi

if [[ "$CONFIRM" != "RESET-CRAWL" ]]; then
  echo "Aborted."
  exit 1
fi

ssh -i "$EC2_KEY" "$EC2_USER@$EC2_HOST" \
  "REMOTE_APP_DIR='$REMOTE_APP_DIR' APP_SERVICE='$APP_SERVICE' MYSQL_SERVICE='$MYSQL_SERVICE' MONGO_SERVICE='$MONGO_SERVICE' CRAWL_TARGET='$CRAWL_TARGET' CRAWL_TIMEOUT_SECONDS='$CRAWL_TIMEOUT_SECONDS' CRAWL_POLL_SECONDS='$CRAWL_POLL_SECONDS' bash -se" <<'REMOTE'
set -euo pipefail

cd "$REMOTE_APP_DIR"

if [[ ! -f docker-compose.yml ]]; then
  echo "docker-compose.yml not found in $REMOTE_APP_DIR" >&2
  exit 1
fi

if [[ ! -f docs/wipe-db-except-users.sql ]]; then
  echo "docs/wipe-db-except-users.sql not found in $REMOTE_APP_DIR" >&2
  exit 1
fi

set -a
if [[ -f .env ]]; then
  # shellcheck disable=SC1091
  . ./.env
fi
set +a

: "${MYSQL_DATABASE:=newsense}"
: "${MYSQL_USER:=newsense}"
: "${MYSQL_PASSWORD:?MYSQL_PASSWORD is required in remote .env}"
: "${MONGO_DATABASE:=newsense}"
: "${MONGO_INITDB_ROOT_USERNAME:?MONGO_INITDB_ROOT_USERNAME is required in remote .env}"
: "${MONGO_INITDB_ROOT_PASSWORD:?MONGO_INITDB_ROOT_PASSWORD is required in remote .env}"

echo "[1/5] Checking compose services..."
docker compose ps

echo "[2/5] Ensuring MySQL database and tables use utf8mb4..."
docker compose exec -T "$MYSQL_SERVICE" sh -lc \
  'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "ALTER DATABASE \`$MYSQL_DATABASE\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"'

docker compose exec -T "$MYSQL_SERVICE" sh -lc \
  'mysql -N -s -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SELECT CONCAT(\"ALTER TABLE \`\", TABLE_NAME, \"\` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\") FROM information_schema.TABLES WHERE TABLE_SCHEMA = \"$MYSQL_DATABASE\" AND TABLE_TYPE = \"BASE TABLE\";" | mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"'

echo "[3/6] Wiping MySQL non-user data..."
docker compose exec -T "$MYSQL_SERVICE" sh -lc \
  'mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"' \
  < docs/wipe-db-except-users.sql

echo "[4/6] Dropping MongoDB article_content collection..."
docker compose exec -T "$MONGO_SERVICE" sh -lc \
  'mongosh --quiet -u "$MONGO_INITDB_ROOT_USERNAME" -p "$MONGO_INITDB_ROOT_PASSWORD" --authenticationDatabase admin "$MONGO_INITDB_DATABASE" --eval "db.article_content.drop()"'

echo "[5/6] Starting one-off crawler container for target ${CRAWL_TARGET} articles..."
docker rm -f newsense-crawl-once >/dev/null 2>&1 || true

SPRING_MONGO_URI="mongodb://${MONGO_INITDB_ROOT_USERNAME}:${MONGO_INITDB_ROOT_PASSWORD}@mongodb:27017/${MONGO_DATABASE}?authSource=admin"

docker compose run -d --name newsense-crawl-once --no-deps \
  -e SPRING_PROFILES_ACTIVE=local \
  -e SERVER_PORT=18080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql:3306/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8" \
  -e SPRING_DATASOURCE_USERNAME="$MYSQL_USER" \
  -e SPRING_DATASOURCE_PASSWORD="$MYSQL_PASSWORD" \
  -e SPRING_JPA_DDL_AUTO=update \
  -e SPRING_MONGODB_URI="$SPRING_MONGO_URI" \
  -e SPRING_DATA_MONGODB_URI="$SPRING_MONGO_URI" \
  -e SPRING_DATA_REDIS_HOST=redis \
  -e SPRING_DATA_REDIS_PORT=6379 \
  -e CRAWLER_ENABLED=false \
  -e CRAWLER_BOOTSTRAP_ENABLED=true \
  -e CRAWLER_MINIMUM_ARTICLES="$CRAWL_TARGET" \
  -e CRAWLER_MAX_ITEMS="$CRAWL_TARGET" \
  "$APP_SERVICE" >/dev/null

deadline=$((SECONDS + CRAWL_TIMEOUT_SECONDS))
article_count=0

while (( SECONDS < deadline )); do
  article_count="$(docker compose exec -T "$MYSQL_SERVICE" sh -lc 'mysql -N -s -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SELECT COUNT(*) FROM article_meta;"' | tr -d '\r' || echo 0)"
  article_count="${article_count:-0}"
  echo "Current article_meta count: ${article_count}/${CRAWL_TARGET}"

  if [[ "$article_count" =~ ^[0-9]+$ ]] && (( article_count >= CRAWL_TARGET )); then
    break
  fi

  if ! docker ps --format '{{.Names}}' | grep -qx 'newsense-crawl-once'; then
    echo "Crawler container stopped before reaching target." >&2
    break
  fi

  sleep "$CRAWL_POLL_SECONDS"
done

echo "[6/6] Crawler logs tail..."
docker logs --tail 200 newsense-crawl-once || true
docker rm -f newsense-crawl-once >/dev/null 2>&1 || true

if [[ ! "$article_count" =~ ^[0-9]+$ ]] || (( article_count < CRAWL_TARGET )); then
  echo "Article crawl did not reach target: ${article_count}/${CRAWL_TARGET}" >&2
  exit 1
fi

echo "Done. Article count reached ${article_count}/${CRAWL_TARGET}."
REMOTE
