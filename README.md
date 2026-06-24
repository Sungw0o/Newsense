# 📌 뉴센스 (Newsense)
> **청년층과 학생의 금융·경제 문해력 향상을 위한 뉴스 기반 자기주도 경제 학습 플랫폼**

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.0-brightgreen?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot 4.1.0" />
  <img src="https://img.shields.io/badge/Vue-3-4FC08D?style=flat-square&logo=vue.js&logoColor=white" alt="Vue 3" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/MongoDB-7.0-47A248?style=flat-square&logo=mongodb&logoColor=white" alt="MongoDB" />
  <img src="https://img.shields.io/badge/Cloudflare-F38020?style=flat-square&logo=cloudflare&logoColor=white" alt="Cloudflare" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white" alt="Docker" />
</p>

---

## 서비스 소개

**뉴센스(Newsense)**는 실생활 경제 뉴스를 핵심 학습 재료로 삼아, 금융·경제 개념이 충분히 형성되지 않은 **10대 후반 ~ 20대 초반 학습자(중고생, 대학생, 청년층)**가 자기주도적으로 경제 지식을 학습하고 문해력을 기를 수 있도록 돕는 서비스입니다.

단순히 경제 뉴스를 요약해서 소비하는 것에 의존하지 않고, **뉴스 속 핵심 경제 개념 학습 → 이해도 진단(퀴즈) → 자기주도 요약 리뷰 → 오답노트·취약 개념 분석**으로 이어지는 유기적인 학습 순환 고리를 설계하여 지속적인 복습을 지원합니다.

---

## 주요 기능

### 1. 다채널 경제 뉴스 자동 수집 파이프라인

실제 구현된 수집 채널은 **공공기관 채널 2개**와 **포털/글로벌 채널 4개**로 구성됩니다.

| 채널 유형 | 클래스 | 수집 대상 |
|---|---|---|
| 공공기관 (JSoup 크롤링) | `PublicNewsCrawlerClient` | 한국은행 (BOK), 기획재정부 (MOEF) |
| 네이버 뉴스 (HTML 크롤링) | `NaverNewsCrawler` | 네이버 경제 섹션 및 인기 기사 |
| 네이버 검색 API | `NaverSearchNewsApiCollector` | 경제·금융·투자·정책·산업 키워드 검색 결과 |
| NewsAPI.org | `NewsApiCollector` | 한국 비즈니스 헤드라인 |
| Google News RSS | `GoogleNewsRssCollector` | 경제·금융·투자 키워드 RSS 피드 |

- 공공기관 채널은 PDF 첨부파일을 자동 감지하여 PDF OCR 파이프라인을 거쳐 본문을 추출합니다.
- 수집된 기사는 SHA-256 해시 기반 중복 검사를 통해 동일 기사의 중복 저장을 방지합니다.

### 2. PDF OCR 이중화 파이프라인

공공기관 보도자료는 종종 스캔 PDF 형태로 제공됩니다. 이를 처리하기 위해 두 단계 파이프라인을 구성합니다.

- **1차 텍스트 추출**: `PdfTextExtractor` (Apache PDFBox 3.0.1) — 텍스트 레이어가 있는 PDF에서 직접 텍스트를 추출합니다.
- **2차 OCR 추출**: `PdfOcrExtractor` (Tess4J 5.8.0 / Tesseract) — 1차 추출 텍스트가 `crawler.pdf-min-text-length` 기준(기본 500자)에 미달하면 PDF 페이지를 250 DPI 이미지로 렌더링하여 한국어 OCR을 수행합니다.

### 3. AI 기사 분류 및 퀴즈 자동 생성

**기사 분류** (`OpenAiArticleClassifierClient`): 수집된 기사를 저장할 때 SSAFY GMS(gpt-4o-mini)를 사용하여 카테고리(거시경제 / 금융·투자 / 정책·제도 / 기업·산업 / 글로벌경제), 난이도(초급 / 중급 / 고급), 3줄 요약을 자동 생성합니다.

**퀴즈 자동 생성** (`OpenAiQuizClient`): 사용자가 기사 퀴즈를 처음 요청할 때(Lazy 방식) SSAFY GMS(gpt-4o-mini)가 기사 본문, RAG 근거 청크, 기재부 경제 용어 사전을 결합하여 OX 1문항 + 객관식 2문항을 생성합니다. 생성된 퀴즈는 MySQL에 저장되고 이후 요청 시 재사용됩니다.

### 4. RAG 기반 기사 검색 및 취약 개념 추천

`RagRetrievalService`는 MongoDB에 저장된 기사 청크를 키워드 스코어링 방식으로 검색합니다.

- **기사 검색**: `GET /api/v1/rag/search?query=금리&limit=5`
- **취약 개념 기반 추천**: `GET /api/v1/rag/recommendations?limit=5` — 사용자의 오답노트에서 취약 경제 용어를 추출하여 관련 기사를 추천합니다.
- **퀴즈 근거 검색**: 퀴즈 생성 시 기사 제목 및 경제 용어 키워드로 청크를 검색하여 증거로 제공합니다.
- `guidelines/importance_criteria.md` 문서는 RAG LLM이 수집 기사의 중요도를 평가하는 기준으로 사용됩니다(70점 이상 = 중요 기사).

### 5. 학습 피드 및 기사 읽기

- 카테고리·난이도 필터, 정렬 기능이 포함된 기사 목록 페이지
- 기사 상세 페이지에서 경제 용어 자동 추출 및 기재부 경제 용어 사전 툴팁 제공
- 기사 읽음 완료 처리(`ArticleReadCompletedEvent`) 및 북마크 기능

### 6. 오답노트 및 학습 이력

- 퀴즈 오답 시 자동으로 `WrongNote` 생성 및 관련 경제 용어 연결
- 학습 이력 타임라인(읽기 완료 / 퀴즈 완료 / 리뷰 작성) 제공
- 일별 학습 통계 및 학습 이력 조회

### 7. 자기주도 리뷰 작성

기사를 읽은 후 자신만의 표현으로 요약 리뷰를 작성하고, 새롭게 알게 된 점과 이해하기 어려웠던 용어를 기록합니다.

### 8. 커뮤니티

사용자 간 경제 이슈 토론 게시판. 게시글 작성, 댓글, 반응(좋아요 등) 기능을 제공합니다.

### 9. 마이페이지 및 계정 관리

프로필(닉네임, 관심사) 수정, 회원 탈퇴(소프트 삭제) 기능을 제공합니다.

---

## 시스템 아키텍처

```mermaid
flowchart TD
    subgraph 수집채널["뉴스 수집 채널"]
        BOK["한국은행 보도자료\n(JSoup 크롤링)"]
        MOEF["기획재정부 보도자료\n(JSoup 크롤링)"]
        NAVER["네이버 뉴스\n(HTML 크롤링)"]
        NAVER_API["네이버 검색 API\n(NaverSearchNewsApiCollector)"]
        NEWSAPI["NewsAPI.org\n(NewsApiCollector)"]
        GNEWS["Google News RSS\n(GoogleNewsRssCollector)"]
    end

    subgraph PDF파이프라인["PDF OCR 파이프라인"]
        PDF1["1차: PDFBox 텍스트 추출\n(PdfTextExtractor)"]
        PDF2["2차: Tess4J OCR 추출\n(PdfOcrExtractor)\n텍스트 부족 시 자동 폴백"]
        PDF1 -->|"텍스트 >= pdf-min-text-length"| PDF_OK["텍스트 확보"]
        PDF1 -->|"텍스트 부족"| PDF2
        PDF2 --> PDF_OK
    end

    subgraph 스케줄러["PublicNewsCrawlerScheduler"]
        SCH_PUB["공공기관 크롤 \ncron.public-source\n평일 11:00, 17:00"]
        SCH_PORTAL["포털 크롤\ncron.portal-source\n매일 08:00, 14:00, 20:00"]
    end

    subgraph 저장파이프라인["ArticlePersistenceService"]
        HASH["SHA-256 중복 검사"]
        AI_CLASSIFY["AI 기사 분류\nOpenAiArticleClassifierClient\n카테고리 / 난이도 / 요약"]
        CHUNK["청크 분할\nSentenceChunker"]
        MONGO["MongoDB\nArticleContent\n(원문 + 청크)"]
        MYSQL["MySQL\nArticleMeta\n(메타 + 요약)"]
    end

    subgraph AI서비스["SSAFY GMS / gpt-4o-mini"]
        GMS["SSAFY GMS\ngpt-4o-mini"]
    end

    subgraph 백엔드["Spring Boot 백엔드 (Java 21)"]
        API["REST API\n/api/v1/**"]
        RAG["RagRetrievalService\n키워드 스코어링 검색"]
        QUIZ_SVC["QuizService\nLazy 퀴즈 생성"]
    end

    subgraph 데이터저장소["데이터 저장소"]
        MYSQL2[("MySQL 8.0\n회원 / 퀴즈 / 오답노트\n학습이력 / 리뷰")]
        MONGO2[("MongoDB 7.0\n기사 원문 + 청크")]
        REDIS["Redis 7.4\nRefresh Token 관리"]
    end

    subgraph 프론트엔드["Vue 3 SPA (Vite + Pinia)"]
        HOME["홈 피드\n기사 목록 + 필터"]
        DETAIL["기사 상세\n용어 툴팁"]
        QUIZ_UI["퀴즈 풀기"]
        WRONG["오답노트"]
        HISTORY["학습 이력"]
        REVIEW["리뷰 작성"]
        COMMUNITY["커뮤니티"]
        MYPAGE["마이페이지"]
    end

    subgraph 인프라["인프라 (Docker Compose)"]
        NGINX["Nginx 1.27-alpine\n리버스 프록시"]
        CF["Cloudflare Tunnel\nSSL 보안 터널"]
    end

    SCH_PUB -->|"평일 2회"| BOK
    SCH_PUB -->|"평일 2회"| MOEF
    SCH_PORTAL -->|"매일 3회"| NAVER
    SCH_PORTAL -->|"매일 3회"| NAVER_API
    SCH_PORTAL -->|"매일 3회"| NEWSAPI
    SCH_PORTAL -->|"매일 3회"| GNEWS

    BOK -->|"PDF 포함 시"| PDF파이프라인
    MOEF -->|"PDF 포함 시"| PDF파이프라인
    PDF파이프라인 --> HASH
    BOK -->|"HTML 본문"| HASH
    MOEF -->|"HTML 본문"| HASH
    NAVER --> HASH
    NAVER_API --> HASH
    NEWSAPI --> HASH
    GNEWS --> HASH

    HASH -->|"신규 기사"| AI_CLASSIFY
    AI_CLASSIFY -->|"분류 결과"| GMS
    GMS -->|"카테고리/난이도/요약"| AI_CLASSIFY
    AI_CLASSIFY --> CHUNK
    CHUNK --> MONGO
    AI_CLASSIFY --> MYSQL

    백엔드 --> MYSQL2
    백엔드 --> MONGO2
    백엔드 --> REDIS

    MYSQL --> MYSQL2
    MONGO --> MONGO2

    QUIZ_SVC -->|"Lazy 퀴즈 생성"| GMS
    RAG -->|"청크 키워드 검색"| MONGO2
    QUIZ_SVC --> RAG

    프론트엔드 -->|"REST API 호출"| NGINX
    NGINX --> API
    CF --> NGINX
```

### 데이터 흐름 요약

> - **수집**: `PublicNewsCrawlerScheduler`가 이원화 크론 스케줄에 따라 공공기관(평일 2회)과 포털(매일 3회)을 각각 수집합니다.
> - **PDF 처리**: 공공기관 보도자료의 PDF 첨부파일을 PDFBox로 1차 추출하고, 텍스트가 부족하면 Tess4J OCR로 2차 추출합니다.
> - **저장**: `ArticlePersistenceService`가 SHA-256 중복 검사 → AI 분류(카테고리/난이도/요약) → 청크 분할 → MongoDB(원문) + MySQL(메타) 이중 저장합니다.
> - **퀴즈**: 사용자가 최초 요청 시 `QuizService`가 RAG 근거 청크와 경제 용어 사전을 조합하여 SSAFY GMS로 퀴즈를 Lazy 생성합니다.
> - **보안**: DB 포트는 외부에 노출하지 않고 Compose 내부 네트워크에서만 통신합니다. 인바운드 트래픽은 Cloudflare Tunnel을 통해서만 수신합니다.

---

## 기술 스택

**Backend**

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-Hibernate-59666C?style=flat-square&logo=hibernate&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-build-02303A?style=flat-square&logo=gradle&logoColor=white)
![OpenAI](https://img.shields.io/badge/OpenAI-gpt--4o--mini-412991?style=flat-square&logo=openai&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger_UI-3.0.3-85EA2D?style=flat-square&logo=swagger&logoColor=black)
![JUnit5](https://img.shields.io/badge/JUnit5-test-25A162?style=flat-square&logo=junit5&logoColor=white)

> Jsoup 1.22.2 (HTML 크롤링) · Apache PDFBox 3.0.1 (PDF 추출) · Tess4J 5.8.0 (OCR)

**Frontend**

![Vue 3](https://img.shields.io/badge/Vue-3-4FC08D?style=flat-square&logo=vue.js&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-bundler-646CFF?style=flat-square&logo=vite&logoColor=white)
![Pinia](https://img.shields.io/badge/Pinia-state-FFD859?style=flat-square&logo=pinia&logoColor=black)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-utility-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white)
![Axios](https://img.shields.io/badge/Axios-http-5A29E4?style=flat-square&logo=axios&logoColor=white)

**Infra**

![Docker](https://img.shields.io/badge/Docker_Compose-orchestration-2496ED?style=flat-square&logo=docker&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-1.27--alpine-009639?style=flat-square&logo=nginx&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-7.0-47A248?style=flat-square&logo=mongodb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.4--alpine-DC382D?style=flat-square&logo=redis&logoColor=white)
![Cloudflare](https://img.shields.io/badge/Cloudflare-Tunnel_+_DNS-F38020?style=flat-square&logo=cloudflare&logoColor=white)
![AWS EC2](https://img.shields.io/badge/AWS-EC2-FF9900?style=flat-square&logo=amazonec2&logoColor=white)

---

## 프로젝트 구조

```text
newsense/
├── backend/                          # Spring Boot 4.1.0 + Java 21 백엔드
│   ├── Dockerfile
│   ├── build.gradle
│   └── src/main/java/com/newsense/backend/
│       ├── ai/                       # AI 클라이언트 (OpenAI 기사 분류 / 퀴즈 생성)
│       │   ├── article/              # OpenAiArticleClassifierClient
│       │   ├── quiz/                 # OpenAiQuizClient, GeneratedQuiz
│       │   └── config/               # OpenAiProperties
│       ├── article/                  # 기사 도메인
│       │   ├── controller/           # ArticleFeedController, ArticleDetailController
│       │   ├── crawler/              # 수집 파이프라인 전체
│       │   │   ├── client/           # 5개 채널 수집 클라이언트
│       │   │   ├── config/           # CrawlerProperties, CrawlerConfig
│       │   │   ├── scheduler/        # PublicNewsCrawlerScheduler
│       │   │   ├── service/          # PublicNewsCrawlerService, PortalNewsCrawlerService
│       │   │   │                     # ArticlePersistenceService, LocalArticleBootstrapRunner
│       │   │   └── util/             # PdfTextExtractor, PdfOcrExtractor, SentenceChunker
│       │   ├── document/             # ArticleContent (MongoDB 도큐먼트)
│       │   ├── domain/               # ArticleMeta (MySQL 엔티티)
│       │   └── service/              # ArticleDetailService
│       ├── auth/                     # 인증·인가 (JWT, Spring Security)
│       ├── bookmark/                 # 북마크 도메인
│       ├── community/                # 커뮤니티 게시판 (Post, Comment, Reaction)
│       ├── learning/                 # 학습 이력 (LearningHistory, LearningHistoryService)
│       ├── quiz/                     # 퀴즈 도메인 (QuizService, QuizController)
│       ├── rag/                      # RAG 검색 (RagRetrievalService, RagController)
│       ├── review/                   # 리뷰 도메인
│       ├── term/                     # 경제 용어 사전 (Term, ArticleTerm)
│       ├── user/                     # 회원 도메인 (UserService, UserController)
│       ├── wrongnote/                # 오답노트 (WrongNoteService, WrongNoteRecorder)
│       └── common/                   # 공통 응답, 예외 처리, 설정
├── frontend/                         # Vue 3 + Vite + Pinia 프론트엔드
│   ├── src/
│   │   ├── api/                      # Axios 기반 API 클라이언트 (articleApi, quizApi 등)
│   │   ├── components/               # 재사용 컴포넌트 (article/, quiz/, common/)
│   │   ├── router/                   # Vue Router 라우트 정의
│   │   ├── stores/                   # Pinia 스토어 (useUserStore, useQuizStore 등)
│   │   └── views/                    # 페이지 컴포넌트 (HomeView, QuizView 등)
│   └── package.json
├── infra/
│   └── nginx/                        # Nginx 리버스 프록시 설정
├── docs/                             # 운영 가이드 문서
├── docker-compose.yml                # 통합 실행 구성
├── .env.example                      # 환경변수 예시
└── README.md
```

---

## 로컬 환경 실행 방법

### 0. 사전 요구사항

| 도구 | 최소 버전 |
|---|---|
| Java JDK | 21 |
| Node.js | LTS (18 이상) |
| Docker Desktop | 최신 권장 |
| Tesseract OCR | 5.x (한국어 `kor.traineddata` 포함) |

### 1. Docker Compose 통합 실행 (권장)

백엔드, MySQL 8.0, MongoDB 7.0, Redis 7.4, Nginx를 한 번에 실행합니다.

```bash
cp .env.example .env
# .env 파일에 필수 환경변수 입력 후
docker compose up -d --build
```

- **Nginx 진입 주소**: `http://127.0.0.1:8080`
- **헬스 체크**: `http://127.0.0.1:8080/api/v1/health`
- MySQL, MongoDB, Redis는 Compose 내부 네트워크에서만 접근합니다.
- 상세 절차: [`docs/docker-compose.md`](docs/docker-compose.md)

### 2. 백엔드 단독 실행

`application-local.yml` 기준으로 로컬 환경에서 실행합니다.

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

**`application-local.yml` 필수 설정값**:

```yaml
crawler:
  # 크롤러 활성화 여부 (로컬 기본값: false)
  enabled: false

  # 앱 시작 시 최소 기사 수 자동 수집 여부 (로컬 기본값: true)
  bootstrap-enabled: true
  minimum-articles: 30

  # 소스당 최대 수집 건수
  max-items-per-source: 30

  # PDF 1차 추출 최소 텍스트 길이 (미달 시 OCR 폴백)
  pdf-min-text-length: 500

  # Tesseract 한국어 traineddata 경로 (스캔 PDF OCR 필수)
  # 예: /usr/share/tessdata  또는  C:/Program Files/Tesseract-OCR/tessdata
  tess-data-path: ""

  # 네이버 검색 API (https://developers.naver.com)
  naver-client-id: ""
  naver-client-secret: ""

  # NewsAPI.org API Key (https://newsapi.org)
  news-api-key: ""

  # 이원화 크론 스케줄 (기본값)
  cron:
    public-source: "0 0 11,17 * * MON-FRI"   # 공공기관: 평일 11:00, 17:00
    portal-source: "0 0 8,14,20 * * *"        # 포털: 매일 08:00, 14:00, 20:00

openai:
  # SSAFY GMS API Key (기사 분류 및 퀴즈 생성 필수)
  api-key: ""
  base-url: "https://gms.ssafy.io/gmsapi/api.openai.com/v1"
  model: "gpt-4o-mini"

jwt:
  secret: "newsense-local-jwt-secret-key-at-least-32-bytes"
  access-expiration: 1800000    # 30분 (ms)
  refresh-expiration: 1209600000 # 14일 (ms)
```

> **참고**: `tess-data-path`가 비어 있으면 스캔 PDF OCR 기능이 비활성화됩니다. `naver-client-id`, `naver-client-secret`, `news-api-key`가 비어 있으면 해당 채널의 수집이 건너뜁니다.

### 3. 데이터베이스 준비 (개별 실행 시)

```sql
-- MySQL
CREATE DATABASE newsense CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

MongoDB는 `newsense` 데이터베이스를 자동 생성합니다(`auto-index-creation: true`).

### 4. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
# 로컬 개발 서버: http://localhost:5173
```

---

## 이원화 스케줄링 정책

`PublicNewsCrawlerScheduler`는 수집 채널 특성에 따라 두 가지 크론 스케줄을 독립적으로 운영합니다.

| 스케줄 키 | 기본 크론 표현식 | 실행 주기 | 대상 채널 |
|---|---|---|---|
| `crawler.cron.public-source` | `0 0 11,17 * * MON-FRI` | 평일 11:00, 17:00 (1일 2회) | 한국은행, 기획재정부 |
| `crawler.cron.portal-source` | `0 0 8,14,20 * * *` | 매일 08:00, 14:00, 20:00 (1일 3회) | 네이버 뉴스, 네이버 검색 API, NewsAPI.org, Google News RSS |

- **공공기관 채널**은 업무 시간(평일)에만 보도자료를 발행하므로 평일 2회만 수집합니다.
- **포털 채널**은 매일 실시간으로 뉴스가 업데이트되므로 하루 3회 수집합니다.
- 크론 표현식은 환경변수(`SPRING_CRAWLER_CRON_PUBLIC`, `SPRING_CRAWLER_CRON_PORTAL`)로 재정의할 수 있습니다.
- `crawler.enabled: false`로 설정하면 스케줄러 전체가 비활성화됩니다.
- 로컬 환경에서는 `crawler.bootstrap-enabled: true`로 설정하면 앱 시작 시 `LocalArticleBootstrapRunner`가 최소 기사 수(기본 30건)를 자동으로 수집합니다.

---

## API 주요 엔드포인트

[![Swagger UI](https://img.shields.io/badge/Swagger_UI-전체_API_명세_확인-85EA2D?style=flat-square&logo=swagger&logoColor=black)](http://localhost:8080/swagger-ui/index.html)

`http://localhost:8080/swagger-ui/index.html`

---

## 개발 컨벤션

Git 브랜치 전략, 커밋 메시지, PR 규칙, Java/Vue 코딩 컨벤션, DB 컨벤션은 [하네스.md](하네스.md)를 참조하세요.
