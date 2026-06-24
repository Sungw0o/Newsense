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

    // 한국은행 공공 채널에서 기사가 아닌 게시물 제목 키워드 (공고, 채용, 입찰 등)
    private static final Set<String> BOK_NON_ARTICLE_KEYWORDS = Set.of(
            "공고", "채용", "입찰", "구인", "인사발령", "인사 발령",
            "정정", "취소", "설명회", "행사 안내", "행사안내",
            "위촉식", "시상식", "수상", "개최 안내", "개최안내",
            "결과 공고", "선정", "공모"
    );

    // "[공고]", "[채용]" 등 제목 앞 대괄호 태그
    private static final Pattern BOK_BRACKET_TAG = Pattern.compile("^\\s*[\\[（（【]");

    private final CrawlerProperties properties;
    private final PublicNewsCrawlerClient crawlerClient;
    private final ArticlePersistenceService persistenceService;

    public CrawlRunResult collectAll() {
        CrawlRunResult total = CrawlRunResult.empty();
        for (CrawlerProperties.Source source : properties.sources()) {
            if (!source.enabled()) {
                continue;
            }
            total = total.add(collectSource(source));
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

    private CrawlRunResult collectSource(CrawlerProperties.Source source) {
        try {
            List<ArticleCandidate> candidates = crawlerClient.fetchCandidates(source);
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
        if (!"bok".equals(source.key()) && !"moef".equals(source.key())) {
            return false;
        }
        String title = candidate.title();
        if (title == null || title.isBlank()) {
            return true;
        }
        // 대괄호 태그로 시작하는 경우 (예: [공고], [채용])
        if (BOK_BRACKET_TAG.matcher(title).find()) {
            return true;
        }
        // 비기사 키워드 포함 여부
        String lower = title;
        return BOK_NON_ARTICLE_KEYWORDS.stream().anyMatch(lower::contains);
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
