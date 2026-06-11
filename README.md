# 📌 핀업 (PinUp)
> **청년층과 학생의 금융·경제 문해력 향상을 위한 뉴스 기반 자기주도 학습 플랫폼**

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.3.0-brightgreen?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot 3.3.0" />
  <img src="https://img.shields.io/badge/Vue-3-4FC08D?style=flat-square&logo=vue.js&logoColor=white" alt="Vue 3" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-4169E1?style=flat-square&logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/MongoDB-7.0-47A248?style=flat-square&logo=mongodb&logoColor=white" alt="MongoDB" />
</p>

---

## 💡 서비스 소개
**핀업(PinUp)**은 실생활 경제 뉴스를 학습 교재로 활용하여, 청년층과 학생들이 어려운 경제 지식을 자기주도적으로 학습하고 문해력을 기를 수 있도록 돕는 서비스입니다. 

단순히 경제 뉴스를 나열하는 것을 넘어, **뉴스 속 경제 개념 학습 ➔ 이해도 진단(퀴즈) ➔ 자기주도 요약 및 리뷰 ➔ AI 오답노트**로 이어지는 유기적인 학습 순환 고리를 제공합니다.

---

## 🛠️ 핵심 기능

* **뉴스 기반 경제 학습 흐름**: 
  * 수준 및 관심 주제별 경제 뉴스 추천 피드 제공.
  * 기사 본문 속 어려운 용어를 하이라이트하고 클릭 시 쉬운 설명 제공.
* **이해도 진단 (AI 기반 퀴즈)**:
  * 기재부 경제 용어 사전 및 AI를 활용해 해당 기사 맥락에 맞는 객관식/단답형 퀴즈 자동 출제.
* **자기주도 리뷰 & AI 피드백**:
  * 읽은 뉴스를 자신만의 언어로 요약/정리하고, AI가 작성 내용을 분석하여 개념 정합성 피드백 제공.
* **성장 관리 및 오답노트**:
  * 틀린 퀴즈 문제 및 오답 원인을 파악할 수 있는 자동 오답노트 기능.
  * 일 단위 학습을 추적하는 학습 캘린더와 취약 도메인 개념 시각화 통계 제공.

---

## 🏗️ 시스템 아키텍처 및 데이터 흐름

핀업은 **RAG(검색 증강 생성) 기법**을 사용하여 신뢰도 높은 경제 지식을 학습자에게 전달하며, 성능과 확장성을 극대화하기 위해 이중 데이터베이스 레이어(PostgreSQL + MongoDB)를 채택하고 있습니다.

```mermaid
graph TD
    %% 클라이언트 레이어
    Client["📱 웹 브라우저 (Vue 3 / SPA)"]

    %% 보안 및 프록시 레이어
    CF["☁️ Cloudflare (DNS/SSL/보안)"]

    %% 백엔드 애플리케이션 레이어
    subgraph ECS["AWS EC2 / Docker Compose"]
        Backend["☕ Spring Boot 3 (Java 21)"]
        
        %% 데이터베이스 레이어
        Postgres[("🐘 PostgreSQL (RDB)<br>유저/퀴즈이력/리뷰<br>+ pgvector")]
        Mongo[("🍃 MongoDB (NoSQL)<br>뉴스 본문 원문 적재<br>+ 벡터 청크")]
        Redis[("⚡ Redis (Cache)<br>JWT Blacklist / 세션")]
    end

    %% 외부 AI 서비스
    OpenAI["🤖 OpenAI API<br>(GPT-4o / text-embedding-3)"]

    %% 흐름 연결
    Client <-->|HTTPS / REST API| CF
    CF <--> Backend
    Backend <--> Postgres
    Backend <--> Mongo
    Backend <--> Redis
    Backend <-->|Embeddings / LLM Prompt| OpenAI
```

> [!NOTE]
> * **PostgreSQL**: 비즈니스 로직과 사용자 학습 이력, 퀴즈 결과 등의 RDB 트랜잭션을 관리하며, `pgvector`를 통해 텍스트 벡터 임베딩 유사도 검색을 수행합니다.
> * **MongoDB**: 공공 기관(기재부, 한은 등) 보도자료 및 뉴스 원문을 Document 형식으로 대량 적재하고, 문단별 청킹 데이터를 관리하는 데 사용됩니다.

---

## 📂 프로젝트 폴더 구조

```text
pinup/
├── backend/                  # Spring Boot 3 + Java 21 백엔드 프로젝트
│   ├── src/                  # 백엔드 소스 코드
│   └── build.gradle          # 백엔드 의존성 및 빌드 설정
├── frontend/                 # Vue 3 + Vite 프론트엔드 프로젝트
│   ├── src/                  # 프론트엔드 컴포넌트 및 앱 소스 코드
│   └── package.json          # 프론트엔드 의존성 설정
├── docker-compose.yml        # 로컬 개발용 인프라 (Postgres, MongoDB, Redis)
├── .gitignore                # 모노레포 관리 통합 Git Ignore
└── README.md                 # 프로젝트 통합 가이드 (본 문서)
```

---

## 🚀 로컬 실행 방법

### 1. 로컬 데이터베이스 구동 (Docker)
프로젝트 루트 디렉토리에서 Docker Compose를 사용하여 로컬 개발 환경용 데이터베이스를 생성 및 실행합니다.
```bash
# PostgreSQL (pgvector 포함), MongoDB, Redis 컨테이너 백그라운드 구동
docker-compose up -d
```

### 2. 백엔드 실행 (Spring Boot)
```bash
cd backend
# Gradle 의존성 빌드 및 구동 (Java 21 필요)
./gradlew bootRun
```
* **API base URL**: `http://localhost:8080`

### 3. 프론트엔드 실행 (Vue 3)
```bash
cd frontend
# 의존성 패키지 설치
npm install

# 로컬 개발 서버 기동
npm run dev
```
* **로컬 웹 주소**: `http://localhost:5173`

---

## 🎨 Git 커밋 메시지 컨벤션 (Gitmoji)

핀업 프로젝트는 작업의 직관성을 높이기 위해 **깃모지(Gitmoji)** 기반의 커밋 메시지 컨벤션을 따릅니다.

### 📝 메시지 작성 기본 규격
```text
:gitmoji: subject (#이슈번호)

body (선택 - 변경 이유나 상세 내용)
```
*예시: `✨ 퀴즈 결과 저장 API 구현 (#42)`*

### 📌 주요 Gitmoji 목록

| Gitmoji | 코드 명명 | 용도 및 설명 |
| :---: | :--- | :--- |
| ✨ | `:sparkles:` | 새로운 기능 개발 및 추가 (feat) |
| 🐛 | `:bug:` | 버그 현상 수정 및 해결 (fix) |
| 📝 | `:memo:` | 문서 추가 및 수정 (README, 주석, 위키 등) |
| 💄 | `:lipstick:` | UI 레이아웃, 스타일, CSS 코드 수정 (style) |
| ♻️ | `:recycle:` | 비즈니스 로직 리팩토링 (refactor) |
| ✅ | `:white_check_mark:` | 테스트 코드 작성 및 수정 (test) |
| 🔧 | `:wrench:` | 빌드 설정 파일, 의존성 설정 파일 추가/수정 (chore) |
| ⚡️ | `:zap:` | 성능 개선 및 최적화 진행 (perf) |
| 💚 | `:green_heart:` | CI/CD 파이프라인 빌드/배포 설정 변경 |
| ⏪️ | `:rewind:` | 이전 커밋 및 작업 내용 되돌리기 (revert) |
