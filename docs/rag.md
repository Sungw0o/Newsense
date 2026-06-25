# RAG 검색 및 취약 개념 추천 가이드

Newsense의 1차 RAG 기능은 별도 벡터 DB 없이 현재 저장 구조를 활용합니다.

- MongoDB `article_content.chunks`: 기사 본문 청크 저장소
- MySQL `article_meta`: 기사 메타데이터
- MySQL `wrong_note` / `wrong_note_term`: 사용자별 취약 개념 신호
- SSAFY GMS `gpt-4o-mini` 퀴즈 생성: 검색된 근거 청크를 프롬프트에 함께 전달

## 기사 청크 검색

```http
GET /api/v1/rag/search?query=금리&limit=5
```

인증 없이 사용할 수 있습니다. 검색어와 매칭된 기사, 매칭 키워드, 근거 청크 일부를 반환합니다.

응답 데이터 구조:

```json
{
  "query": "금리",
  "keywords": ["금리"],
  "results": [
    {
      "article": {
        "articleId": 1,
        "title": "..."
      },
      "matchedChunks": [
        {
          "chunkIndex": 0,
          "snippet": "...금리...",
          "score": 4
        }
      ],
      "matchedKeywords": ["금리"],
      "score": 12,
      "reason": "검색어와 기사 청크가 매칭되었습니다."
    }
  ]
}
```

## 취약 개념 기반 추천

```http
GET /api/v1/rag/recommendations?limit=5
Authorization: Bearer <access-token>
```

로그인 사용자의 미해결 오답노트에서 자주 틀린 경제 용어를 모아 관련 기사 청크를 검색합니다.

오답노트 신호가 아직 없으면 `weaknessTerms`와 `recommendations`가 빈 배열로 반환됩니다.

## 퀴즈 생성 RAG 보강

기사 퀴즈 생성 시 전체 본문만 SSAFY GMS에 전달하지 않고, 기사 제목과 경제 용어를 기반으로 관련 청크를 먼저 검색한 뒤 `검색된 근거 청크`로 함께 전달합니다.

이로써 퀴즈는 다음 우선순위를 갖습니다.

1. 검색된 근거 청크의 핵심 사실
2. 기사 본문 전체 맥락
3. 기사에서 매칭된 기재부 경제 용어 사전

## 2차 고도화 후보

- OpenAI embeddings 또는 별도 임베딩 모델 도입
- MongoDB Atlas Vector Search, pgvector, Elasticsearch/OpenSearch 중 하나로 벡터 검색 인덱스 구성
- 사용자별 추천 사유를 카테고리/오답 횟수/최근 학습 이력까지 반영
- 검색 로그를 활용한 랭킹 튜닝
