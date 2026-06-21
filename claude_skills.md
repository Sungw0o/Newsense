# 🎨 Claude 프론트엔드 개발 & UI/UX 스킬 지침 (Claude Skills)

본 문서는 **뉴스 기반 경제 학습 서비스(Newsense)** 프로젝트에서 프론트엔드(Vue 3, Pinia, Tailwind CSS) 구현 및 비즈니스 로직 리팩토링을 담당하는 **Claude AI 어시스턴트**를 위한 전용 **행동 강령 및 고도화 스킬 명세서**입니다.

Claude는 모든 프론트엔드 작업([FE-XXX] 이슈 등)을 수행할 때 본 지침을 완벽히 숙지하고 준수해야 합니다.

---

## 💅 1. 프리미엄 UI/UX & 디자인 에스테틱 (Visual Fidelity)

Claude는 사용자가 첫 화면에서 감동을 느낄 수 있도록 **최상의 시각적 완성도(Visual Fidelity)**를 확보해야 합니다.

* **세련된 컬러 파레트 (No Plain Colors)**: 
  - 브라우저 기본 원색(Plain Blue, Red, Green 등)의 사용을 엄격히 금지합니다.
  - `tailwind.config.js`에 설정된 뉴스엔스 테마 색상(Harmonious Gradients, Sleek Gray, HSL 기반의 정돈된 소프트 톤)을 전적으로 사용합니다.
* **마이크로 인터랙션 (Micro-animations)**:
  - 모든 버튼, 카드, 링크 요소에 호버(Hover) 시 부드러운 스케일링(`scale-102`), 트랜지션 효과(`transition-all duration-300`), 그리고 섀도우 입체감(`hover:shadow-lg`)을 부여합니다.
  - 단순 텍스트 로딩 대신 생동감 있는 스피너(`animate-spin`) 또는 스켈레톤 UI를 제공합니다.
* **플레이스홀더 배제 (No Raw Placeholders)**:
  - 성의 없는 회색 네모 상자나 "이미지 들어갈 곳"과 같은 텍스트 플레이스홀더 배치를 절대 금지합니다.
  - 이미지나 아이콘이 필요한 곳에는 실제 UI에 부합하는 아이콘(Heroicons 등) 또는 아름답게 카드 컴포넌트로 스타일링된 더미 데이터를 배치하여 실제 배포 환경과 동일한 핏을 만듭니다.

---

## ⚡ 2. Vue 3 (Composition API) 반응형 최적화 규칙

* **Reactivity 유실 방지**:
  - Pinia 스토어의 상태(state) 값을 구조분해 할당(Destructuring)할 때 반응성이 소실되는 현상을 완벽히 차단합니다.
  - 상태를 구조분해 할 때는 반드시 `storeToRefs`를 사용해야 합니다.
    ```javascript
    // BAD
    const { isAuthenticated, userInfo } = useUserStore();
    
    // GOOD
    import { storeToRefs } from 'pinia';
    const userStore = useUserStore();
    const { isAuthenticated, userInfo } = storeToRefs(userStore);
    ```
* **Props / Emits 엄격 정의**:
  - `props`는 `defineProps()`, `emits`는 `defineEmits()`를 사용해 부모-자식 간 인터페이스를 명확하게 선언합니다.
  - 모든 props에는 필요 시 `default` 값과 `required` 속성을 정의하여 Vue 개발 도구 콘솔에 경고가 찍히지 않도록 사전 차단합니다.
* **디렉티브 혼용 금지 (v-if & v-for)**:
  - 동일한 HTML 태그 엘리먼트에 `v-if`와 `v-for`를 절대 같이 적지 않습니다.
  - 가급적 `computed` 속성을 사용해 필터링이 완료된 리스트를 `v-for`로 순회하거나, 상위에 `<template>` 태그를 두어 분기합니다.
* **v-for Key 바인딩 필수**:
  - `v-for`를 돌릴 때 고유한 `:key` 값을 명시하지 않는 실수를 절대 금지합니다. (JPA/MongoDB ID를 적극 활용)

---

## 📐 3. UI/UX 컴포넌트 설계 및 도메인 분리

* **컴포넌트 크기 제어 (200줄 Rule)**:
  - 단일 Vue 컴포넌트 파일의 길이가 200줄을 초과하거나 템플릿 마크업과 비즈니스 쿼리 로직이 과도하게 얽히는 경우, 즉시 화면 단위 컴포넌트(Views)와 기능형 서브 컴포넌트(Components)로 쪼갭니다.
* **디렉토리 구조 준수**:
  - 공통적인 UI 프레임워크(버튼, 모달, 배지 등)는 `src/components/common/` 에 위치시킵니다.
  - 도메인 특화 비즈니스 UI는 `article/`, `quiz/`, `review/` 디렉토리에 맞게 나누어 저장합니다.

---

## 🌐 4. API 통신 및 예외/상태 처리 스킬

* **비동기 상태 대응 UI**:
  - 서버 API를 호출하는 모든 구간에 `isLoading` 상태값과 연동된 로딩 스피너 및 비활성화(Disabled) 버튼 처리를 확실히 렌더링합니다.
* **에러 바인딩**:
  - API 호출이 실패(`catch` 블록)했을 때 조용히 묻어버리는 코딩을 절대 금지합니다.
  - 사용자가 이해할 수 있는 자연스러운 한국어 에러 메시지를 공통 토스트(Toast) 또는 에러 바인딩 UI 컴포넌트에 매핑해 노출합니다.
* **API 호출 분리**:
  - 컴포넌트 내부에서 Axios 인스턴스를 직접 호출해 URL을 기재하는 것을 지양합니다.
  - 모든 API 통신은 `src/api/*Api.js` 내 모듈화된 함수를 호출하는 방식으로 처리합니다.

---

## 🎨 5. Git 협업 및 커밋 규칙 준수

* **커밋 메시지 형식**:
  - 아래의 `:gitmoji: type : subject (#이슈번호)` 콜론 띄어쓰기 형식을 100% 지켜 메시지를 만듭니다.
  - *예시: `✨ feat : 회원 로그인 상태 유지 및 JWT 자동 갱신 (#2)`*
* **브랜치 활용**:
  - 임의로 `main` 브랜치에 코드 수정을 가하지 않으며, `develop` 브랜치에서 분기한 `feature/*` (또는 한글 기능명) 브랜치 내에서 구현을 완료한 후 PR을 요청해야 합니다.
