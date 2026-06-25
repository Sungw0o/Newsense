# Newsense 실행 가이드

Newsense를 로컬 또는 Docker Compose 환경에서 실행하는 방법을 정리합니다.

## 사전 요구사항

| 도구 | 권장 버전 |
| --- | --- |
| Java JDK | 21 |
| Node.js | 22 LTS |
| Docker Desktop | 최신 권장 |
| Tesseract OCR | 5.x, 한국어 `kor.traineddata` 포함 |

## Docker Compose 통합 실행

백엔드, MySQL, MongoDB, Redis, Nginx를 한 번에 실행합니다.

```bash
cp .env.example .env
docker compose up -d --build
```

- Nginx 진입 주소: `http://127.0.0.1:8080`
- 백엔드 API: `http://127.0.0.1:8080/api/v1`
- Swagger UI: `http://127.0.0.1:8080/swagger-ui/index.html`

운영 EC2 배포에서는 `/opt/newsense/.env` 파일을 직접 관리합니다. GitHub Actions는 이 파일을 덮어쓰지 않고 존재 여부만 확인합니다.

## 백엔드 단독 실행

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

Windows PowerShell:

```powershell
cd backend
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

로컬에서 MySQL을 직접 띄우는 경우 데이터베이스를 먼저 생성합니다.

```sql
CREATE DATABASE newsense CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

- 개발 서버: `http://localhost:5173`
- API 기본값: `frontend/.env.example` 참고

## 테스트 및 빌드

백엔드:

```bash
cd backend
./gradlew test jacocoTestReport jacocoTestCoverageVerification
./gradlew bootJar
```

프론트엔드:

```bash
cd frontend
npm run test:run
npm run build
```

## 배포 메모

- 프론트엔드는 S3 + CloudFront로 배포합니다.
- 백엔드는 EC2 + Docker Compose로 배포합니다.
- GitHub Secrets에는 배포 접속 및 AWS/Sonar 값만 저장합니다.
- DB, JWT, OAuth, API 키 등 런타임 값은 EC2 `/opt/newsense/.env`에서 관리합니다.
