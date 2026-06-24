-- ============================================================
-- 회원 정보(users, user_interests)를 제외한 전체 데이터 초기화
-- 크롤링 품질 고도화 적용 후 데이터 재수집 시 사용
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE wrong_note_term;
TRUNCATE TABLE wrong_note;
TRUNCATE TABLE quiz_answer;
TRUNCATE TABLE quiz_option;
TRUNCATE TABLE quiz;
TRUNCATE TABLE learning_history;
TRUNCATE TABLE review_difficult_term;
TRUNCATE TABLE review;
TRUNCATE TABLE article_read;
TRUNCATE TABLE bookmark;
TRUNCATE TABLE post_reaction;
TRUNCATE TABLE post_comment;
TRUNCATE TABLE post;
TRUNCATE TABLE article_term;
TRUNCATE TABLE article_meta;
TRUNCATE TABLE term;

SET FOREIGN_KEY_CHECKS = 1;

-- MongoDB (mongosh에서 별도 실행)
-- use newsense;
-- db.article_content.drop();
