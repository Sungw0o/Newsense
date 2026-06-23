# Docker Compose 실행 가이드

Newsense 백엔드, MySQL 8.0, MongoDB, Redis, Nginx를 하나의 Compose 프로젝트로 실행합니다.

## 네트워크 격리 원칙

- 외부 호스트 포트는 Nginx만 바인딩합니다.
- MySQL, MongoDB, Redis, Spring Boot 애플리케이션은 호스트 포트를 열지 않고 Compose 내부 브리지 네트워크에서만 통신합니다.
- 기본 Nginx 바인딩은 `127.0.0.1:8080`입니다. Cloudflare Tunnel은 이 로컬 엔드포인트를 origin으로 사용하면 됩니다.

## 환경변수 준비

```bash
cp .env.example .env
```

필수로 바꿔야 하는 값:

- `MYSQL_PASSWORD`
- `MYSQL_ROOT_PASSWORD`
- `MONGO_INITDB_ROOT_PASSWORD`
- `JWT_SECRET`
- `OPENAI_API_KEY`

## 실행

```bash
docker compose up -d --build
```

## 중지

```bash
docker compose down
```

볼륨까지 삭제하려면 다음 명령을 사용합니다.

```bash
docker compose down -v
```

## 로그 확인

```bash
docker compose logs -f app
docker compose logs -f nginx
docker compose logs -f mysql
docker compose logs -f mongodb
docker compose logs -f redis
```

## 접속 확인

```bash
curl http://127.0.0.1:8080/api/v1/health
```

## Cloudflare Tunnel 연결 예시

Cloudflare Tunnel은 호스트의 로컬 Nginx 포트로 연결합니다.

```yaml
tunnel: <tunnel-id>
credentials-file: /etc/cloudflared/<tunnel-id>.json

ingress:
  - hostname: api.example.com
    service: http://127.0.0.1:8080
  - service: http_status:404
```

운영 서버 방화벽에서는 외부에서 `3306`, `27017`, `6379`, `8080`에 직접 접근하지 못하도록 막고, Cloudflare Tunnel 프로세스만 origin에 접근하게 두는 구성을 권장합니다.
