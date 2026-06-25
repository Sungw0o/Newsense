# Newsense

> AI 요약·퀴즈·RAG 기반 경제 뉴스 학습 플랫폼 — 뉴스를 읽고, 개념을 익히고, 퀴즈로 점검합니다.

<p align="center">
  <a href="https://sonarcloud.io/summary/new_code?id=Sungw0o_Newsense">
    <img src="https://sonarcloud.io/api/project_badges/measure?project=Sungw0o_Newsense&metric=alert_status" alt="Quality Gate Status" />
  </a>
  <a href="https://sonarcloud.io/summary/new_code?id=Sungw0o_Newsense">
    <img src="https://sonarcloud.io/api/project_badges/measure?project=Sungw0o_Newsense&metric=coverage" alt="Coverage" />
  </a>
  <a href="https://sonarcloud.io/summary/new_code?id=Sungw0o_Newsense">
    <img src="https://sonarcloud.io/api/project_badges/measure?project=Sungw0o_Newsense&metric=bugs" alt="Bugs" />
  </a>
  <a href="https://sonarcloud.io/summary/new_code?id=Sungw0o_Newsense">
    <img src="https://sonarcloud.io/api/project_badges/measure?project=Sungw0o_Newsense&metric=vulnerabilities" alt="Vulnerabilities" />
  </a>
  <a href="https://sonarcloud.io/summary/new_code?id=Sungw0o_Newsense">
    <img src="https://sonarcloud.io/api/project_badges/measure?project=Sungw0o_Newsense&metric=code_smells" alt="Code Smells" />
  </a>
</p>

## 서비스 소개

**Newsense**는 청년층과 학생이 경제 뉴스를 읽으며 핵심 개념을 익히고, AI 퀴즈와 오답노트로 이해도를 점검할 수 있는 자기주도 경제 학습 서비스입니다.

뉴스 수집 → 본문 정제 → AI 멀티 모델 파이프라인(요약·분류·퀴즈) → RAG 기반 경제 용어 검색 → 학습 이력·오답노트 루프까지, 경제 문해력 향상을 위한 전 과정을 하나의 흐름으로 연결합니다.

## 주요 기능

| 기능 | 설명 |
| --- | --- |
| 뉴스 피드 | 카테고리·난이도 필터와 함께 경제 기사 목록 제공 |
| AI 3줄 요약 | Gemini + GPT 멀티 모델로 본문 핵심 수치·인과관계 포함 요약 생성 |
| AI 퀴즈 | 개념 이해 / 사실 확인 / 인과 추론 3단계 OX·객관식 문항 자동 생성 |
| Generator-Critic 자가 교정 | 퀴즈 생성 후 별도 모델이 정답·근거·선택지 품질 검증 및 재출제 |
| RAG 추천 | 기사 청크 + 기재부 경제사전 임베딩 기반 하이브리드 검색 및 취약 개념 추천 |
| 학습 이력 | 읽기 완료·퀴즈 풀이·리뷰 작성 이력 기록 및 통계 |
| 오답노트 | 틀린 문제와 취약 개념 누적 관리, 복습 흐름 제공 |
| 커뮤니티 | 경제 이슈 게시글·댓글·좋아요 |
| 소셜 로그인 | Google · Naver · Kakao OAuth2 + JWT (Access Token + HttpOnly Refresh Cookie) |
| 관리자 | 기사 수집 제어, AI 요약 상태 모니터링, 회원 관리, 권한 기반 접근 제어 |
| 경제 지표 | 주요 경제 지표 자동 수집 및 노출 |

## 기술 스택

| 구분 | 스택 |
| --- | --- |
| 언어 | ![Java 21](https://img.shields.io/badge/Java_21-ED8B00?style=flat-square&logo=openjdk&logoColor=white) ![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=flat-square&logo=javascript&logoColor=black) |
| 백엔드 | ![Spring Boot 4.1](https://img.shields.io/badge/Spring_Boot_4.1-6DB33F?style=flat-square&logo=springboot&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white) ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat-square&logo=spring&logoColor=white) |
| 프론트엔드 | ![Vue 3](https://img.shields.io/badge/Vue_3-4FC08D?style=flat-square&logo=vuedotjs&logoColor=white) ![Vite](https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white) ![Pinia](https://img.shields.io/badge/Pinia-FFD859?style=flat-square&logo=pinia&logoColor=black) ![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white) |
| 데이터베이스 | ![MySQL 8](https://img.shields.io/badge/MySQL_8-4479A1?style=flat-square&logo=mysql&logoColor=white) ![MongoDB 7](https://img.shields.io/badge/MongoDB_7-47A248?style=flat-square&logo=mongodb&logoColor=white) ![Redis 7](https://img.shields.io/badge/Redis_7-DC382D?style=flat-square&logo=redis&logoColor=white) |
| AI | ![OpenAI](https://img.shields.io/badge/GPT--4o_mini-412991?style=flat-square&logo=openai&logoColor=white) ![OpenAI](https://img.shields.io/badge/o3--mini-412991?style=flat-square&logo=openai&logoColor=white) ![Google Gemini](https://img.shields.io/badge/Gemini_2.5_Flash_Lite-4285F4?style=flat-square&logo=google&logoColor=white) ![OpenAI](https://img.shields.io/badge/text--embedding--3--large-412991?style=flat-square&logo=openai&logoColor=white) |
| 인프라 | ![AWS EC2](https://img.shields.io/badge/AWS_EC2-FF9900?style=flat-square&logo=amazonec2&logoColor=white) ![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white) ![Nginx](https://img.shields.io/badge/Nginx-009639?style=flat-square&logo=nginx&logoColor=white) ![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=flat-square&logo=amazons3&logoColor=white) ![CloudFront](https://img.shields.io/badge/CloudFront-FF9900?style=flat-square&logo=amazonaws&logoColor=white) |
| 외부 API | ![Naver](https://img.shields.io/badge/Naver_API-03C75A?style=flat-square&logo=naver&logoColor=white) ![NewsAPI](https://img.shields.io/badge/NewsAPI-FFA500?style=flat-square&logo=rss&logoColor=white) ![OAuth2](https://img.shields.io/badge/OAuth2-EB5424?style=flat-square&logo=auth0&logoColor=white) |
| 품질 | ![SonarCloud](https://im