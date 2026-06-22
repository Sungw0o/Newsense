# Codex Harness

Newsense에서 Codex가 이슈를 구현하고 GitHub에 전달할 때 따르는 실행 규칙이다.

## 1. 기준 문서 우선순위

1. 현재 작업할 GitHub 이슈의 완료 조건
2. 관통(신)의 기능 명세서와 API 명세서 v1
3. 관통(신)의 컨벤션
4. 저장소의 기존 코드와 테스트 패턴
5. 기획 산출물 v2와 IA

문서끼리 기술 스택, 패키지명, API 경로가 충돌하면 임의로 대규모 전환하지 않는다. 현재 이슈와 코드가 합의한 계약을 유지하고, 충돌 내용을 이슈나 PR에 기록한 뒤 별도 이슈로 정합화한다.

## 2. 이슈 처리 기본 순서

1. 이슈 본문과 체크박스를 읽는다.
2. 관련 Notion 기능/API 명세와 로컬 코드를 대조한다.
3. `develop`에서 `feature/{기능명}` 브랜치를 만든다.
4. 다른 작업이 섞여 있으면 별도 Git worktree에서 격리한다.
5. 이슈 범위의 코드와 문서만 수정한다.
6. 관련 테스트와 빌드를 실행한다.
7. 실제로 검증된 항목만 이슈 체크박스에 체크한다.
8. `:gitmoji: type : subject (#이슈번호)` 형식으로 커밋한다.
9. 원격 브랜치에 push하고 `develop` 대상 PR을 연다.
10. PR 링크와 검증 결과를 보고하고 다음 지시를 기다린다.

## 3. Git 규칙

- `main`: 배포 브랜치, 직접 push 금지
- `develop`: 기능 PR 대상 브랜치
- `feature/*`: 이슈 하나 또는 기능 하나만 담당
- 사용자 작업이 섞인 상태에서 `git add -A`를 사용하지 않는다.
- 사용자 변경을 reset, checkout, stash로 치우지 않는다.
- 커밋 예시: `✨ feat : 백엔드 초기 구조 설정 (#13)`
- PR에는 연관 이슈, 작업 내용, 검증 결과, 문서 영향, 남은 위험을 적는다.

## 4. Backend 규칙

- 기준 패키지: `com.newsense.backend`
- 도메인 우선 패키지: `auth`, `user`, `article`, `quiz`, `review`, `wrongnote`, `learning`, `bookmark`, `term`, `notification`, `ai`, `common`
- Controller는 HTTP 계약, Service는 비즈니스 로직과 트랜잭션, Repository는 DB 접근을 담당한다.
- 생성자 주입과 `@RequiredArgsConstructor`를 사용하고 필드 주입은 금지한다.
- DTO는 `record` 또는 getter 전용 클래스를 사용한다.
- 모든 API는 `ApiResponse<T>`로 응답한다.
- Swagger 매핑과 문서 어노테이션은 `{Domain}ApiDocs` 인터페이스에 둔다.
- API Base URL은 `/api/v1`이다.
- 예외는 `GlobalExceptionHandler`에서 공통 응답으로 변환하고 내부 스택 트레이스를 응답에 노출하지 않는다.

## 5. Frontend 규칙

- Vue 3 Composition API와 `<script setup>`을 사용한다.
- 컴포넌트는 PascalCase, View는 `*View.vue`, Pinia store는 `use*Store.js`, API 모듈은 `*Api.js`를 사용한다.
- API 호출은 store 또는 composable로 분리한다.
- `v-for`에는 `:key`를 지정하고 같은 요소에 `v-if`와 `v-for`를 함께 쓰지 않는다.
- Pinia state를 구조 분해할 때 `storeToRefs`를 사용한다.

## 6. 연결 문서

- 관통(신): https://app.notion.com/p/52153756e6ba8325a7808129840ca3b7
- 컨벤션: https://app.notion.com/p/c3653756e6ba821fac1c01d126e2697c
- 기능 명세서: https://app.notion.com/p/c8753756e6ba83ff802b01144c56c499
- API 명세서 v1: https://app.notion.com/p/f4253756e6ba822c839f8127a35430f1
- 기획 산출물 v2: https://app.notion.com/p/d2753756e6ba835e818401c46659a91a
