package com.newsense.backend.article.crawler.service;

import com.newsense.backend.article.crawler.client.PublicNewsCrawlerClient;
import com.newsense.backend.article.crawler.config.CrawlerProperties;
import com.newsense.backend.article.crawler.model.ArticleCandidate;
import com.newsense.backend.article.crawler.model.CrawlRunResult;
import com.newsense.backend.article.crawler.model.CrawledArticle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicNewsCrawlerService {

    // 한국은행/기재부 특수 필터
    private static final Set<String> BOK_NON_ARTICLE_KEYWORDS = Set.of(
            "공고", "채용", "입찰", "구인", "인사발령", "인사 발령",
            "정정", "취소", "설명회", "행사 안내", "행사안내",
            "위촉식", "시상식", "수상", "개최 안내", "개최안내",
            "결과 공고", "선정", "공모"
    );

    // 모든 소스에 적용할 비경제 키워드 필터
    private static final Set<String> NON_ECONOMIC_KEYWORDS = Set.of(
            "축구", "야구", "농구", "배구", "골프", "테니스", "스포츠",
            "K리그", "EPL", "프리미어리그", "분데스리가", "챔피언스리그",
            "월드컵", "올림픽", "패럴림픽", "국가대표", "선수단",
            "연예", "드라마", "영화", "아이돌", "콘서트"
    );

    private static final Pattern BOK_BRACKET_TAG = Pattern.compile("^\\s*[\\[（（【]");

    private final CrawlerProperties properties;
    private final PublicNewsCrawlerClient crawlerClient;
    private final ArticlePersistenceService persistenceService;

    public CrawlRunResult collectAll() {
        return collectAll(Integer.MAX_VALUE);
    }

    public CrawlRunResult collectAll(int maxPerSource) {
        CrawlRunResult total = CrawlRunResult.empty();
        for (CrawlerProperties.Source source : properties.sources()) {
            if (!source.enabled()) {
                continue;
            }
            total = total.add(collectSource(source, maxPerSource));
        }
        log.info(
                "News crawl completed: discovered={}, saved={}, skipped={}, failed={}",
                total.discovered(),
                total.saved(),
                total.skipped(),
                total.failed()
        );
        return total;
    }

    private CrawlRunResult collectSource(CrawlerProperties.Source source, int maxPerSource) {
        try {
            List<ArticleCandidate> candidates = crawlerClient.fetchCandidates(source);
            if (maxPerSource < Integer.MAX_VALUE) {
                candidates = candidates.stream().limit(maxPerSource).toList();
            }
            int saved = 0;
            int skipped = 0;
            int failed = 0;
            for (ArticleCandidate candidate : candidates) {
                try {
                    if (isNonArticle(source, candidate)) {
                        log.debug("Non-article filtered: source={}, title={}", source.key(), candidate.title());
                        skipped++;
                        continue;
                    }
                    if (persistenceService.alreadyExists(candidate.sourceUrl())) {
                        skipped++;
                        continue;
                    }
                    CrawledArticle article = crawlerClient.fetchArticle(source, candidate);
                    if (persistenceService.saveIfNew(article)) {
                        saved++;
                    } else {
                        skipped++;
                    }
                } catch (RuntimeException exception) {
                    failed++;
                    log.error(
                            "Article crawl failed: source={}, url={}",
                            source.key(),
                            candidate.sourceUrl(),
                            exception
                    );
                } finally {
                    pauseBetweenRequests();
                }
            }
            return new CrawlRunResult(candidates.size(), saved, skipped, failed);
        } catch (RuntimeException exception) {
            log.error("Source crawl failed: source={}, listUrl={}", source.key(), source.listUrl(), exception);
            return new CrawlRunResult(0, 0, 0, 1);
        }
    }

    private boolean isNonArticle(CrawlerProperties.Source source, ArticleCandidate candidate) {
        String title = candidate.title();
        if (title == null || title.isBlank()) {
            return true;
        }

        // 모든 소스 공통: 비경제 키워드 제목 필터
        String lowerTitle = title.toLowerCase();
        for (String keyword : NON_ECONOMIC_KEYWORDS) {
            if (lowerTitle.contains(keyword.toLowerCase())) {
                log.debug("Non-economic keyword \'{}\' found in title: {}", keyword, title);
                return true;
            }
        }

        // 한국은행/기재부 전용 공지 필터
        if ("bok".equals(source.key()) || "moef".equals(source.key())) {
            if (BOK_BRACKET_TAG.matcher(title).find()) {
                return true;
            }
            return BOK_NON_ARTICLE_KEYWORDS.stream().anyMatch(title::contains);
        }

        return false;
    }

    private void pauseBetweenRequests() {
        if (properties.requestDelay() <= 0) {
            return;
        }
        try {
            Thread.sleep(properties.requestDelay());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Crawler request delay interrupted", exception);
        }
    }
}
