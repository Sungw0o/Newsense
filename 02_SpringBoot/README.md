# Spring Boot 실행 메모

## 로컬 실행

```bash
cd backend
./gradlew bootRun
```

## 테스트

```bash
cd backend
./gradlew test
```

## 데모 데이터

`../04_Dataset/demo_articles.json`, `../04_Dataset/demo_quizzes.json`를 DB seed 입력으로 사용한다. AI 토큰이 부족한 경우에도 서버는 DB에 적재된 데모 기사와 퀴즈를 우선 조회한다.
