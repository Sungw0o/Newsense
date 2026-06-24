# Newsense deployment secrets

This project deploys the frontend to S3/CloudFront and the backend to EC2 Docker Compose.
Do not commit real secret values. Store sensitive values in GitHub Actions Secrets.

## GitHub Actions Secrets

### Backend deploy

- `EC2_HOST`
- `EC2_USERNAME`
- `EC2_SSH_KEY`

### Backend runtime

- `MYSQL_USER`
- `MYSQL_PASSWORD`
- `MYSQL_ROOT_PASSWORD`
- `MONGO_INITDB_ROOT_USERNAME`
- `MONGO_INITDB_ROOT_PASSWORD`
- `JWT_SECRET`
- `GMS_KEY`
- `NAVER_CLIENT_ID`
- `NAVER_CLIENT_SECRET`
- `NEWS_API_KEY`
- `NAVER_OAUTH_CLIENT_ID`
- `NAVER_OAUTH_CLIENT_SECRET`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `KAKAO_CLIENT_ID`
- `KAKAO_CLIENT_SECRET`

### Frontend deploy

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION`
- `AWS_S3_BUCKET_NAME`
- `AWS_CLOUDFRONT_DISTRIBUTION_ID`

### SonarCloud

- `SONAR_TOKEN`

## GitHub Actions Variables

### Domains

- `APP_FRONTEND_URL=https://new5ense.site`
- `VITE_API_BASE_URL=https://api.new5ense.site/api/v1`

### SonarCloud

- `SONAR_HOST_URL=https://sonarcloud.io`
- `SONAR_PROJECT_KEY=Sungw0o_Newsense`
- `SONAR_PROJECT_NAME=Newsense`
- `SONAR_ORGANIZATION=sungw0o`

### Backend defaults

- `MYSQL_DATABASE=newsense`
- `MONGO_DATABASE=newsense`
- `SPRING_JPA_DDL_AUTO=validate`
- `JWT_ACCESS_EXPIRATION=1800000`
- `JWT_REFRESH_EXPIRATION=1209600000`
- `GMS_BASE_URL=https://gms.ssafy.io/gmsapi/api.openai.com/v1`
- `GMS_MODEL=gpt-4o-mini`
- `AI_PIPELINE_MAX_QUIZ_RETRIES=2`
- `AI_EMBEDDING_MODEL=text-embedding-3-large`
- `AI_FACT_EXTRACTOR_MODEL=gemini-2.5-flash-lite`
- `AI_ARTICLE_CLASSIFIER_MODEL=gpt-4o-mini`
- `AI_QUIZ_GENERATOR_MODEL=o3-mini`
- `AI_QUIZ_CRITIC_MODEL=gpt-4o-mini`
- `CRAWLER_ENABLED=true`
- `CRAWLER_BOOTSTRAP_ENABLED=false`
- `CRAWLER_MINIMUM_ARTICLES=30`
- `CRAWLER_MAX_ITEMS=30`
- `BOK_CRAWLER_LIST_URL=https://www.bok.or.kr/portal/singl/newsData/listCont.do?pageIndex=1&targetDepth=3&menuNo=201263&syncMenuChekKey=1&searchCnd=1&searchKwd=`
- `MOEF_CRAWLER_LIST_URL=https://www.moef.go.kr/nw/nes/nesdta.do?menuNo=4010100`
- `RAG_VECTOR_SEARCH_ENABLED=true`
- `RAG_VECTOR_SEARCH_MODE=local`
- `RAG_VECTOR_SEARCH_INDEX_NAME=article_embedding_index`
- `RAG_VECTOR_SEARCH_NUM_CANDIDATES=200`
- `RAG_VECTOR_SEARCH_DIMENSIONS=3072`
- `RAG_EMBEDDING_BACKFILL_ENABLED=false`

## OAuth provider redirect URIs

Use these backend callback URLs in Google, Naver, and Kakao developer consoles:

- `https://api.new5ense.site/login/oauth2/code/google`
- `https://api.new5ense.site/login/oauth2/code/naver`
- `https://api.new5ense.site/login/oauth2/code/kakao`

For local testing, also register:

- `http://127.0.0.1:8080/login/oauth2/code/google`
- `http://127.0.0.1:8080/login/oauth2/code/naver`
- `http://127.0.0.1:8080/login/oauth2/code/kakao`
- `http://localhost:8080/login/oauth2/code/google`
- `http://localhost:8080/login/oauth2/code/naver`
- `http://localhost:8080/login/oauth2/code/kakao`
