# Demo Dataset Contract

이 폴더는 프론트엔드 하드코딩용 데이터가 아니라, AI 토큰 부족이나 시연 환경에서 DB/seed로 적재해 API가 조회할 수 있도록 만든 fallback 데이터셋입니다.

## 구성

| 파일 | 내용 |
| --- | --- |
| `demo_articles.json` | 경제 기사 본문, 3줄 요약, 분류, 조회수, 연관 종목 |
| `demo_quizzes.json` | 기사별 3문항 퀴즈와 Generator-Critic 검증 근거 |
| `demo_terms.json` | RAG 추천과 오답노트에 연결되는 경제 용어 |

## 작성 기준

- `docs/ai-pipeline-strategy.md`의 요약, 분류, 퀴즈 생성 규칙을 `promptContract`로 함께 보관합니다.
- 스포츠, 정치 단독 이슈, 연예, 사건사고 등 비경제 기사는 포함하지 않습니다.
- 실제 화면은 이 JSON을 직접 import하지 않고, DB 적재 후 백엔드 API 응답을 통해 사용합니다.
