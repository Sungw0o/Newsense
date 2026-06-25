-- Newsense demo schema reference.
-- Runtime schema is managed by Spring JPA; this file documents the core tables
-- used by the demo seed dataset.

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255),
  nickname VARCHAR(50) NOT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'USER',
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS article_meta (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(500) NOT NULL,
  source VARCHAR(100),
  source_url VARCHAR(1000),
  category VARCHAR(50) NOT NULL,
  difficulty VARCHAR(30),
  summary TEXT,
  published_at DATE,
  view_count INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS article_content (
  id VARCHAR(64) PRIMARY KEY,
  article_meta_id BIGINT NOT NULL,
  content LONGTEXT NOT NULL,
  FOREIGN KEY (article_meta_id) REFERENCES article_meta(id)
);

CREATE TABLE IF NOT EXISTS article_related_stock (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  article_meta_id BIGINT NOT NULL,
  stock_name VARCHAR(100) NOT NULL,
  stock_code VARCHAR(20) NOT NULL,
  relation_reason VARCHAR(500),
  FOREIGN KEY (article_meta_id) REFERENCES article_meta(id)
);

CREATE TABLE IF NOT EXISTS quiz (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  article_meta_id BIGINT NOT NULL,
  question TEXT NOT NULL,
  type VARCHAR(30) NOT NULL,
  correct_answer VARCHAR(255) NOT NULL,
  explanation TEXT,
  FOREIGN KEY (article_meta_id) REFERENCES article_meta(id)
);
