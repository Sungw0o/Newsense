-- Newsense full MySQL schema (DDL only).
-- Generated from the deployed MySQL schema; no seed/DML data is included.

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `article_meta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_meta` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` enum('COMPANY_INDUSTRY','FINANCE_INVESTMENT','GLOBAL_ECONOMY','MACRO_ECONOMY','POLICY_SYSTEM') COLLATE utf8mb4_unicode_ci NOT NULL,
  `collected_at` datetime(6) NOT NULL,
  `content_hash` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `difficulty` enum('ADVANCED','BASIC','INTERMEDIATE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `estimated_minutes` int NOT NULL,
  `mongo_document_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `published_at` date DEFAULT NULL,
  `source` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source_url` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `summary` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `title` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `view_count` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_article_meta_published_at` (`published_at`),
  KEY `idx_article_meta_category_difficulty` (`category`,`difficulty`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `article_read`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_read` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `read_at` datetime(6) NOT NULL,
  `article_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_read_user_article` (`user_id`,`article_id`),
  KEY `FKou3s6yeod4t7rwsowu2vxpkxp` (`article_id`),
  CONSTRAINT `FKkxkp65vnlml197nlsn4pu7rho` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKou3s6yeod4t7rwsowu2vxpkxp` FOREIGN KEY (`article_id`) REFERENCES `article_meta` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `article_related_stock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_related_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `relation_reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stock_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `stock_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `article_meta_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_related_stock_article_id` (`article_meta_id`),
  CONSTRAINT `FKhyb892uardhgxjhxedxrsumaa` FOREIGN KEY (`article_meta_id`) REFERENCES `article_meta` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `article_term`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `article_term` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL,
  `term_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_term_article_term` (`article_id`,`term_id`),
  KEY `FKhrn6nogvdckx2unt0sjjw2bhb` (`term_id`),
  CONSTRAINT `FKhrn6nogvdckx2unt0sjjw2bhb` FOREIGN KEY (`term_id`) REFERENCES `term` (`id`),
  CONSTRAINT `FKrnvwjawr0io14lugvjlb0q4ek` FOREIGN KEY (`article_id`) REFERENCES `article_meta` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `bookmark`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bookmark` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `article_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bookmark_user_article` (`user_id`,`article_id`),
  KEY `FKf2sb7g9cb0u5ev7oi0fgf963` (`article_id`),
  CONSTRAINT `FKf2sb7g9cb0u5ev7oi0fgf963` FOREIGN KEY (`article_id`) REFERENCES `article_meta` (`id`),
  CONSTRAINT `FKo4vbqvq5trl11d85bqu5kl870` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `inquiries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inquiries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `resolved` bit(1) NOT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfks94q8sobcuibrudbr3im380` (`user_id`),
  CONSTRAINT `FKfks94q8sobcuibrudbr3im380` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `learning_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `learning_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `learned_at` datetime(6) NOT NULL,
  `learning_date` date NOT NULL,
  `quiz_correct` bit(1) DEFAULT NULL,
  `reference_id` bigint NOT NULL,
  `type` enum('ARTICLE_READ','QUIZ','REVIEW') COLLATE utf8mb4_unicode_ci NOT NULL,
  `article_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_learning_history_user_type_reference` (`user_id`,`type`,`reference_id`),
  KEY `idx_learning_history_user_date` (`user_id`,`learning_date`),
  KEY `idx_learning_history_user_type` (`user_id`,`type`),
  KEY `FKn4n3h54qclpnkvq9t35nft449` (`article_id`),
  CONSTRAINT `FKn4n3h54qclpnkvq9t35nft449` FOREIGN KEY (`article_id`) REFERENCES `article_meta` (`id`),
  CONSTRAINT `FKv4awstvslfgpoowth514cu15` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `dislikes` int NOT NULL,
  `likes` int NOT NULL,
  `scrap_summary_id` bigint DEFAULT NULL,
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('GENERAL','INQUIRY','NOTICE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `view_count` bigint NOT NULL,
  `article_meta_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdpqn0rtys57qekwd3jm0v0th` (`article_meta_id`),
  KEY `FK7ky67sgi7k0ayf22652f7763r` (`user_id`),
  CONSTRAINT `FK7ky67sgi7k0ayf22652f7763r` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKdpqn0rtys57qekwd3jm0v0th` FOREIGN KEY (`article_meta_id`) REFERENCES `article_meta` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `post_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_post_comment_post_id` (`post_id`),
  KEY `FKbh2kvd72ce49c3f0bj77rxji2` (`user_id`),
  CONSTRAINT `FKbh2kvd72ce49c3f0bj77rxji2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKna4y825fdc5hw8aow65ijexm0` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `post_reaction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_reaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` enum('DISLIKE','LIKE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `post_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_reaction_user_post` (`user_id`,`post_id`),
  KEY `FKd7eopt2vpb38ybx3xhaelnhne` (`post_id`),
  CONSTRAINT `FKd7eopt2vpb38ybx3xhaelnhne` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`),
  CONSTRAINT `FKgfu9iva55gewlp604y3cgb5v5` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `post_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `post_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `reason` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reported_at` datetime(6) NOT NULL,
  `post_id` bigint NOT NULL,
  `reporter_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_report_post_reporter` (`post_id`,`reporter_id`),
  KEY `idx_post_report_post_id` (`post_id`),
  KEY `FK4hjy1u8p5c8r9kxef3ds67ats` (`reporter_id`),
  CONSTRAINT `FK4hjy1u8p5c8r9kxef3ds67ats` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKeyehd7v09u9oxijrfvw1ufof` FOREIGN KEY (`post_id`) REFERENCES `post` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `quiz`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `correct_answer` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `display_order` int NOT NULL,
  `explanation` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `generated_at` datetime(6) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `question` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('MULTIPLE','OX') COLLATE utf8mb4_unicode_ci NOT NULL,
  `article_id` bigint NOT NULL,
  `purpose` enum('BASIC_CONCEPT','CAUSAL_REASONING','FACT_CHECK','MARKET_CONTEXT') COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_level` enum('ADVANCED','BASIC','INTERMEDIATE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_quiz_article_active` (`article_id`,`is_active`),
  CONSTRAINT `FKntyod4mxaju1jnjcnvu9r5ul6` FOREIGN KEY (`article_id`) REFERENCES `article_meta` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `quiz_answer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_answer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `correct_answer` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_correct` bit(1) NOT NULL,
  `submitted_at` datetime(6) NOT NULL,
  `user_answer` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `article_id` bigint NOT NULL,
  `quiz_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_quiz_answer_user_id` (`user_id`),
  KEY `FK6vbgvr4m20xxrvb7g7h5e5vj` (`article_id`),
  KEY `FKoxi2td1x8cc3y4a0vlsg2hfnc` (`quiz_id`),
  CONSTRAINT `FK6vbgvr4m20xxrvb7g7h5e5vj` FOREIGN KEY (`article_id`) REFERENCES `article_meta` (`id`),
  CONSTRAINT `FK8mhem8k163doeg12mv24t7wd7` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKoxi2td1x8cc3y4a0vlsg2hfnc` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `quiz_option`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quiz_option` (
  `quiz_id` bigint NOT NULL,
  `option_text` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `option_order` int NOT NULL,
  PRIMARY KEY (`quiz_id`,`option_order`),
  CONSTRAINT `FK134e3ro35naxwfjqcda4ckljn` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`),
  CONSTRAINT `quiz_option_chk_1` CHECK ((`option_order` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `learned` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `summary` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `article_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_user_article` (`user_id`,`article_id`),
  KEY `FKl3lu0pm361cd18y6o3fua6ghg` (`article_id`),
  CONSTRAINT `FK6cpw2nlklblpvc7hyt7ko6v3e` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKl3lu0pm361cd18y6o3fua6ghg` FOREIGN KEY (`article_id`) REFERENCES `article_meta` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `review_difficult_term`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review_difficult_term` (
  `review_id` bigint NOT NULL,
  `term_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `term_order` int NOT NULL,
  PRIMARY KEY (`review_id`,`term_order`),
  CONSTRAINT `FKbh8x08xfpacusmqc6evjmoog4` FOREIGN KEY (`review_id`) REFERENCES `review` (`id`),
  CONSTRAINT `review_difficult_term_chk_1` CHECK ((`term_order` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `term`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `term` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `definition` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `source` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_term_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `user_interests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_interests` (
  `user_id` bigint NOT NULL,
  `interest` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  KEY `FKdv9fflrh61wyuujfwx2yn1tb4` (`user_id`),
  CONSTRAINT `FKdv9fflrh61wyuujfwx2yn1tb4` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_active` bit(1) NOT NULL,
  `level` enum('ADVANCED','BASIC','INTERMEDIATE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `profile_image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('ADMIN','USER') COLLATE utf8mb4_unicode_ci NOT NULL,
  `sub_plan` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UK2ty1xmrrgtn89xt7kyxx6ta7h` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wrong_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wrong_note` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `article_id` bigint NOT NULL,
  `category` enum('COMPANY_INDUSTRY','FINANCE_INVESTMENT','GLOBAL_ECONOMY','MACRO_ECONOMY','POLICY_SYSTEM') COLLATE utf8mb4_unicode_ci NOT NULL,
  `correct_answer` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  `explanation` varchar(2000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `first_wrong_at` datetime(6) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `is_resolved` bit(1) NOT NULL,
  `last_wrong_at` datetime(6) NOT NULL,
  `mistake_count` int NOT NULL,
  `question` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  `user_answer` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quiz_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wrong_note_user_quiz` (`user_id`,`quiz_id`),
  KEY `idx_wrong_note_user_active_resolved` (`user_id`,`is_active`,`is_resolved`),
  KEY `FKl2dr4821obr4q95g6pduqvc7k` (`quiz_id`),
  CONSTRAINT `FKa73km7fjg7y7ofqxfgbdk3m4c` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKl2dr4821obr4q95g6pduqvc7k` FOREIGN KEY (`quiz_id`) REFERENCES `quiz` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `wrong_note_term`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wrong_note_term` (
  `wrong_note_id` bigint NOT NULL,
  `term_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `term_order` int NOT NULL,
  PRIMARY KEY (`wrong_note_id`,`term_order`),
  CONSTRAINT `FK9ib2o171okrcmqkn4pnvf9tfi` FOREIGN KEY (`wrong_note_id`) REFERENCES `wrong_note` (`id`),
  CONSTRAINT `wrong_note_term_chk_1` CHECK ((`term_order` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;


