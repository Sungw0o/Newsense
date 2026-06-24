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
- `GMS_KEY`

### 운영 DB 스키마 전략

`SPRING_JPA_DDL_AUTO`의 기본값은 `validate`입니다. 안정 운영에서는 애플리케이션이 DB 스키마를 임의로 변경하지 않도록 `validate`를 유지합니다.

초기 MVP 서버처럼 빈 MySQL 볼륨에 최초 스키마를 만들어야 하는 경우에만 `.env`에서 일시적으로 다음 값을 사용할 수 있습니다.

```bash
SPRING_JPA_DDL_AUTO=update
```

최초 기동 후 테이블 생성이 확인되면 반드시 다시 `validate`로 되돌린 뒤 재기동합니다.

```bash
SPRING_JPA_DDL_AUTO=validate
docker compose up -d --build
```

장기 운영 단계에서는 `update` 대신 Flyway 또는 Liquibase 같은 마이그레이션 도구로 스키마 변경 이력을 관리하는 것을 권장합니다.

## 실행

```bash
docker compose up -d --build
```

## SSAFY GMS AI 설정

AI 퀴즈 생성은 SSAFY GMS의 OpenAI 호환 Chat Completions 엔드포인트를 기본값으로 사용합니다.

```bash
GMS_KEY=<your-gms-key>
GMS_BASE_URL=https://gms.ssafy.io/gmsapi/api.openai.com/v1
GMS_MODEL=gpt-4o-mini
AI_PIPELINE_MAX_QUIZ_RETRIES=2
AI_FACT_EXTRACTOR_MODEL=gemini-2.5-flash-lite
AI_ARTICLE_CLASSIFIER_MODEL=gpt-4o-mini
AI_QUIZ_GENERATOR_MODEL=o3-mini
AI_QUIZ_CRITIC_MODEL=gpt-4o-mini
```

기존 `OPENAI_API_KEY`, `OPENAI_BASE_URL`, `OPENAI_MODEL` 환경변수도 하위 호환용으로 남아 있지만, 새 설정에서는 `GMS_*` 값을 우선 사용합니다. 실제 키는 `.env`에만 저장하고 Git에는 올리지 않습니다.
AI 파이프라인 모델은 역할별 환경변수로 분리되어 있으며, 별도 지정이 없으면 팩트 추출, 요약/분류, 퀴즈 생성, 퀴즈 검증의 기본 모델 조합을 사용합니다.

## 로컬 기사 자동 적재

`local` 프로필에서는 기본적으로 `CRAWLER_BOOTSTRAP_ENABLED=true`이며, 앱 시작 시 `article_meta`가 `CRAWLER_MINIMUM_ARTICLES`보다 적으면 목표치에 도달할 때까지 크롤러를 제한적으로 재시도합니다. 기본 목표치는 30개입니다.

```bash
CRAWLER_BOOTSTRAP_ENABLED=true
CRAWLER_MINIMUM_ARTICLES=30
CRAWLER_MAX_ITEMS=30
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
