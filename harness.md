# Newsense — Agent Shared Harness

> 모든 워크트리(newsense, newsense-be-*, newsense-article-filter 등)에 공통 적용되는 단일 행동 강령.
> 각 프로젝트의 `claude_skills.md`는 이 파일로 대체된다.

---

## 프로젝트 개요

- **서비스명**: Newsense — 뉴스 기반 경제 학습 서비스 (청년 금융 문해력 향상)
- **GitHub**: https://github.com/Sungw0o/Newsense
- **기술 스택**
  - 백엔드: Java 21 / Spring Boot 3, MySQL 8.0, MongoDB(기사 원문·청크), Redis(인증·캐시)
  - 프론트엔드: Vue 3 (Composition API), Pinia, Vite, Tailwind CSS
  - AI: Spring AI + OpenAI (GPT-4o-mini), SSAFY GMS, RAG (MongoDB 청크 + MySQL 오답노트)
  - 인프라: EC2 + Docker Compose, Cloudflare DNS/HTTPS, S3+CloudFront(프론트)

---

## 1. Git 브랜치 전략 (Git Flow)

### 1.1 브랜치 구조
- **main**: 배포용 최종 안정 브랜치 (직접 push 금지)
- **develop**: 개발 통합 브랜치 (기능 개발 PR 대상)
- **feature/***: 기능 단위 개발 브랜치

### 1.2 브랜치 네이밍 및 규칙
- 브랜치 이름 형식: `feature/{기능명}` (예: `feature/user-login`, `feature/로그인` - 한글 기능명도 허용)
- `feature/*` 브랜치는 반드시 `develop`에서 분기하고 `develop`으로 병합한다.
- `main` 브랜치는 `develop`에서만 병합 가능하다 (직접 push 금지).
- 에이전트 작업 브랜치는 `codex/{작업-요약}-{이슈번호}` 형식을 기본으로 사용하되, 기존 PR 브랜치가 존재하면 그 형식을 따른다.
- 원격 브랜치는 작업 완료 후 삭제를 권장한다 (GitLab MR Merge 시 자동 삭제).
- 커밋 히스토리 간소화를 위해 **Squash Merge** 사용을 권장한다.

---

## 2. Git & 커밋 규칙

### 2.1 기본 형식
```plain text
{gitmoji} {type} : {subject} (#{이슈번호})

body (선택 — 변경 이유나 상세 내용)

footer (선택 — 관련 이슈, Breaking Change 등)
```

### 2.2 Gitmoji 종류
- ✨ `(:sparkles:)`: 새로운 기능 추가 (feat)
- 🐛 `(:bug:)`: 버그 수정 (fix)
- 📝 `(:memo:)`: 문서 수정 (docs, README, 주석)
- 💄 `(:lipstick:)`: UI 스타일/CSS 수정 (style)
- ♻️ `(:recycle:)`: 코드 리팩토링 (refactor)
- ✅ `(:white_check_mark:)`: 테스트 코드 추가/수정 (test)
- 🔧 `(:wrench:)`: 빌드 설정, 패키지/설정 파일 업데이트 (chore)
- ⚡ `(:zap:)`: 성능 개선 (perf)
- ⏪ `(:rewind:)`: 이전 커밋 되돌리기 (revert)

### 2.3 커밋 규칙
- gitmoji 필수. 콜론 앞뒤 공백 1칸. 이슈번호 필수. subject 50자 이내, 마침표 생략.
- subject 첫 글자는 소문자로 작성하며 마침표는 생략한다.
- 예: `✨ feat : 회원 로그인 상태 유지 및 JWT 자동 갱신 (#2)`
- WIP(작업중) 커밋도 형식을 반드시 준수한다.
- 자잘한 작업인 경우 지시받고 적절히 판단하여 GitHub `origin`의 `develop`에 바로 푸시할 수 있다 (PR 생략 허용).
- GitHub 이슈를 생성할 때는 저장소에 이미 존재하는 라벨만 사용한다. 새 라벨이 필요해 보여도 임의 생성하지 말고 이슈 본문이나 코멘트에 제안으로 남긴다.

---

## 3. PR(Merge Request) & 코드 리뷰 (CodeRabbit)

### 3.1 PR 기본 원칙
- PR은 하나의 기능 또는 하나의 버그 수정 단위로 분리한다.
- 변경 라인은 **300줄 이하**를 권장하며 초과 시 분리 검토한다.
- CI 파이프라인(빌드 + 테스트)이 통과한 후 Merge한다.
- **draft PR로 열지 않아야 한다.**
- CodeRabbit AI 리뷰 완료 확인 후 Merge를 진행한다 (PR 생성 시 자동 트리거됨).
- CodeRabbit의 major 지적 사항은 반드시 반영하거나 사유 코멘트를 남긴 후 Merge한다.

### 3.2 PR 및 이슈 템플릿
- 작업 전에는 이슈 템플릿에 맞추어 이슈를 생성하고, PR을 열 때 연결하여 닫히도록 한다.

### 3.3 CodeRabbit 활용
- 코멘트에 `@coderabbitai` 멘션으로 추가 질문 또는 재분석 요청이 가능하다.
- `@coderabbitai review` 명령어로 특정 파일/함수에 집중 리뷰를 요청할 수 있다.

---

## 4. 카테고리 기준 (백엔드 enum 동기화)

| enum 상수          | displayName |
|--------------------|-------------|
| MACRO_ECONOMY      | 거시경제    |
| FINANCE_INVESTMENT | 금융/투자   |
| POLICY_SYSTEM      | 정책/제도   |
| COMPANY_INDUSTRY   | 기업/산업   |
| GLOBAL_ECONOMY     | 글로벌경제  |

- 프론트엔드 카테고리 문자열은 위 displayName만 사용하며, 구 6개(금융, 부동산, 주식, 환율, 거시경제, 통화정책) 사용은 금지한다.

---

## 5. 백엔드 — Java / Spring Boot

### 5.1 네이밍 규칙
- 클래스: **PascalCase** (예: `QuizResultService`, `ArticleMetaRepository`)
- 메서드/변수: **camelCase** (예: `findByUserIdAndQuizId()`, `totalScore`)
- DTO: `record` 또는 `@Getter` 전용 — **setter 금지**
- 상수: **UPPER_SNAKE_CASE** (예: `MAX_RETRY_COUNT`, `DEFAULT_CATEGORY`)
- 패키지: 소문자 (하이픈 금지)

### 5.2 패키지 구조
`com.newsense.backend`
- `auth`: 인증/인가 (JWT, OAuth)
- `user`: 회원 관리
- `article`: 뉴스 기사 메타 및 본문
- `quiz`: 퀴즈 생성 및 응답
- `review`: 리뷰 작성 및 자가 Grade
- `wrongnote`: 오답노트
- `learning`: 학습 이력
- `bookmark`: 북마크
- `term`: 경제 용어 사전
- `notification`: 알림
- `ai`: Spring AI / GMS RAG 연동
- `common`: 공통 (응답 형식, 예외, 유틸)

### 5.3 레이어 구조 및 규칙
- Controller(요청·응답만 처리, 비즈니스 로직 금지) / Service(핵심 비즈니스 로직, `@Transactional` 관리) / Repository(DB 접근만 담당, JPA/QueryDSL 사용)
- 의존성 주입: `@RequiredArgsConstructor` 생성자 주입 (**필드 주입 금지**)
- Entity에서 직접 DTO 변환 메서드(`toDto()`) 제공이 가능하다.

### 5.4 API 공통 응답 형식
- 모든 API 응답: `ApiResponse<T>` 래퍼 클래스 사용 -> `{ success, code, message, data }`
- HTTP Status Code와 응답 body의 code 필드가 일치해야 한다.
- 예외: `@ControllerAdvice` + 커스텀 예외 클래스로 중앙 처리한다.
- 에러 메시지는 한국어로 작성하되 스택 트레이스는 응답 바디 노출을 금지한다.

---

## 6. 프론트엔드 — Vue 3 / Pinia

### 6.1 파일 네이밍
- 컴포넌트: **PascalCase** (예: `QuizCard.vue`, `ArticleFeedList.vue`)
- 페이지 컴포넌트 (`views/`): **PascalCase + View 접미사** (예: `HomeView.vue`, `QuizResultView.vue`)
- 스토어 (Pinia): **camelCase** (예: `useUserStore.js`, `useArticleStore.js`)
- API 모듈: **camelCase + Api 접미사** (예: `quizApi.js`, `articleApi.js`)

### 6.2 프로젝트 구조
`src/`
- `assets/`: 정적 이미지, 아이콘
- `components/`: 재사용 공통 컴포넌트 (`common/`, `article/`, `quiz/`, `review/`)
- `views/`: 라우터 연결 페이지 컴포넌트
- `router/`: Vue Router 설정
- `stores/`: Pinia 상태 관리
- `api/`: Axios API 모듈
- `utils/`: 공통 유틸 함수

### 6.3 컴포넌트 작성 규칙
- `<script setup>` (Composition API) 필수
- props는 `defineProps()`, emits는 `defineEmits()` 명시적 정의 필수 (`default`/`required` 명시)
- Pinia state 구조분해 시 반드시 `storeToRefs` 사용
  ```js
  // GOOD
  const { isAuthenticated } = storeToRefs(useUserStore())
  ```
- `v-if` + `v-for` 동일 엘리먼트 혼용 금지 -> `<template>` 또는 `computed`로 분리
- `v-for`에 `:key` 바인딩 필수 (DB ID 우선)
- 단일 컴포넌트 200줄 초과 시 Views / Components로 분리 검토

---

## 7. 프론트엔드 — API / 상태 처리

- 컴포넌트에서 Axios 직접 호출 금지 -> 반드시 `src/api/*Api.js` 모듈 경유
- 모든 API 호출에 `isLoading` 스피너 + disabled 버튼 처리
- `catch` 블록에서 에러를 묻지 말고 한국어로 사용자에게 노출
- Mock fallback 금지 — API 장애 시 error state를 화면에 표시
- store에 `error: null` state 추가, 필터·리셋 시 함께 초기화
- Axios 인터셉터를 통한 Access Token 주입 및 401 에러 시 토큰 자동 갱신 처리

---

## 8. 프론트엔드 — UI/UX

- 브라우저 기본 원색(plain blue/red/green) 사용 금지 -> `tailwind.config.js` 테마 색상 사용
- 버튼·카드·링크에 hover 트랜지션(`transition-all duration-300`), 섀도우, scale 효과 필수
- 로딩 중 스피너 또는 스켈레톤 UI 제공 — 텍스트/회색 박스 플레이스홀더 금지
- 폰트: Fustat(제목), Nanum Gothic(모노/숫자), Inter(본문)
- 브랜드 컬러: `#0084ff` (primary), 다크모드 `darkMode: 'class'` 방식 (기본값 dark 강제)

---

## 9. DB 컨벤션

### 9.1 네이밍 규칙
- 테이블명: **snake_case, 단수형** (예: `user`, `article_meta`, `quiz`, `wrong_note`)
- 컬럼명: **snake_case** (예: `user_id`, `created_at`, `is_active`)
- PK: `id` / FK: `{테이블명}_id` / Boolean: `is_` 접두사
- 소프트 삭제: `is_active = FALSE`
- 인덱스명: `idx_{테이블}_{컬럼}` (예: `idx_quiz_user_id`)

### 9.2 공통 규칙
- 모든 테이블에 `id`, `created_at` 필수, 변경 추적 필요 시 `updated_at` 추가
- FK는 `ON DELETE CASCADE` 또는 `ON DELETE SET NULL` 명시
- 인덱스는 WHERE, JOIN, ORDER BY에 자주 쓰이는 컬럼 기준으로 추가
- 직접 테이블 스키마 변경 시 반드시 팀 공유 후 진행
- N+1 문제 방지: FETCH JOIN 또는 `@BatchSize` 활용

---

## 10. 기타 협업 및 환경 변수 규칙

### 10.1 환경 변수 관리
- `.env` 파일은 절대 Git에 push하지 않는다.
- 환경 변수 목록은 `.env.example` 파일에 키 이름만 명시한다.
- 시크릿 정보는 GitLab CI/CD Variables 또는 팀 채널을 통해 전달한다.

### 10.2 주석 가이드
- 코드가 '무엇'을 하는지가 아닌 **'왜'** 하는지를 주석으로 작성한다.
- TODO/FIXME 주석은 반드시 이슈 번호를 포함한다 (예: `// TODO: #42 퀴즈 정답률 갱신 로직`).
- Dead code(주석 처리된 코드)는 커밋에 포함하지 않는다.

---

## 11. AI 역할 분담

| 에이전트     | 담당                                           |
|-------------|------------------------------------------------|
| Claude      | UI/UX 화면 구현, 복잡한 비즈니스 로직 리팩토링  |
| Antigravity | 프로젝트 메인 아키텍트 & 프로젝트 허브(노션) 실시간 동기화, 로컬 파일 탐색 및 코드 수정 |
| ChatGPT     | AI 프롬프트 엔지니어링, RAG 아키텍처 자문      |
| Codex       | 인라인 코드 자동완성, 보일러플레이트 생성       |
