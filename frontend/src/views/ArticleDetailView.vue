<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useArticleStore } from '../stores/useArticleStore'
import BaseButton from '../components/common/BaseButton.vue'
import BaseBadge from '../components/common/BaseBadge.vue'
import LoadingSpinner from '../components/common/LoadingSpinner.vue'
import { splitArticleParagraphs, splitSummaryItems } from '../utils/articleText'
import stockApi from '../api/stockApi'

const route = useRoute()
const router = useRouter()
const articleStore = useArticleStore()

const articleId = computed(() => route.params.id)
const selectedTerm = ref(null)
const isArticleExpanded = ref(false)
const stockQuotes = ref({})

const { selectedArticle, selectedArticleTerms, isLoading } = storeToRefs(articleStore)

const isBookmarked = computed(() => selectedArticle.value?.isBookmarked ?? false)

const escapeRegExp = (value) => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')

const termPattern = computed(() => {
  const names = selectedArticleTerms.value
    .map(term => term.name)
    .filter(Boolean)
    .sort((left, right) => right.length - left.length)

  if (names.length === 0) return null
  return new RegExp(`(${names.map(escapeRegExp).join('|')})`, 'g')
})

const summaryItems = computed(() => splitSummaryItems(selectedArticle.value?.summary))
const articleParagraphTexts = computed(() => splitArticleParagraphs(selectedArticle.value?.content))
const canExpandArticle = computed(() => articleParagraphTexts.value.length > 5)
const relatedStocks = computed(() => {
  const stocks = selectedArticle.value?.relatedStocks ?? []
  return stocks.map(stock => ({
    ...stock,
    ...(stockQuotes.value[stock.stockCode] ?? {})
  }))
})
const visibleArticleParagraphs = computed(() => {
  if (isArticleExpanded.value || !canExpandArticle.value) {
    return articleParagraphTexts.value
  }
  return articleParagraphTexts.value.slice(0, 5)
})
// 텍스트에서 대괄호 [용어] 패턴을 감지하여 클릭 가능한 엘리먼트로 변환
const formattedParagraphs = computed(() => {
  if (visibleArticleParagraphs.value.length === 0) return []
  
  const highlightedTerms = new Set()
  return visibleArticleParagraphs.value.map(p => {
    const regex = termPattern.value
    if (!regex) {
      return [{ type: 'text', value: p }]
    }

    const parts = []
    let match

    let lastIndex = 0
    regex.lastIndex = 0
    while ((match = regex.exec(p)) !== null) {
      const termName = match[0]
      const startIndex = match.index
      
      // 용어 매칭 이전 텍스트 추가
      if (startIndex > lastIndex) {
        parts.push({
          type: 'text',
          value: p.substring(lastIndex, startIndex)
        })
      }
      
      // 용어 텍스트 추가
      const shouldHighlight = !highlightedTerms.has(termName)
      if (shouldHighlight) highlightedTerms.add(termName)

      parts.push({
        type: shouldHighlight ? 'term' : 'text',
        value: termName,
        hasDefinition: shouldHighlight
      })
      
      lastIndex = regex.lastIndex
    }

    if (lastIndex < p.length) {
      parts.push({
        type: 'text',
        value: p.substring(lastIndex)
      })
    }

    return parts
  })
})

const showTermDefinition = (termName) => {
  const found = selectedArticleTerms.value.find(t => t.name === termName)
  if (found) {
    selectedTerm.value = found
  }
}

const toggleBookmark = async () => {
  try {
    await articleStore.toggleBookmark(articleId.value)
  } catch (err) {
    alert('북마크 처리에 실패했습니다.')
  }
}

const writeReview = () => {
  router.push(`/articles/${articleId.value}/review`)
}

const goToQuiz = () => {
  router.push(`/articles/${articleId.value}/quiz`)
}

const loadStockQuotes = async () => {
  const stocks = selectedArticle.value?.relatedStocks ?? []
  stockQuotes.value = {}
  await Promise.all(stocks.map(async (stock) => {
    if (!stock.stockCode || stock.price != null) return
    try {
      const res = await stockApi.getQuote(stock.stockCode)
      const quote = res?.data ?? res
      if (quote) {
        stockQuotes.value = {
          ...stockQuotes.value,
          [stock.stockCode]: quote
        }
      }
    } catch {
      // 기존 관련 기업 카드 표시를 유지한다.
    }
  }))
}

const articleMetaText = computed(() => {
  if (!selectedArticle.value) return ''
  const published = selectedArticle.value.publishedAt
    ? String(selectedArticle.value.publishedAt).replaceAll('-', '.')
    : '작성일 미상'
  const views = Number(selectedArticle.value.viewCount ?? 0).toLocaleString()
  return `${published} · 조회수 ${views}회`
})

watch(articleId, async (newId) => {
  if (!newId) return
  selectedTerm.value = null
  isArticleExpanded.value = false
  try {
    await articleStore.fetchArticleDetail(newId)
    await loadStockQuotes()
    // 기사 읽음 완료 처리 API 호출
    await articleStore.markArticleAsRead(newId)
  } catch (err) {
    console.error('Failed to load article detail:', err)
  }
}, { immediate: true })
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 py-8">
    <!-- Loading State -->
    <div v-if="isLoading" class="flex flex-col items-center justify-center py-20 gap-4">
      <LoadingSpinner />
      <p class="text-sm text-slate-400 font-light select-none">기사를 준비 중입니다...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="!selectedArticle" class="text-center py-20 text-slate-400">
      ⚠️ 기사를 불러오는 데 실패했거나 존재하지 않습니다.
    </div>

    <!-- Main Content -->
    <div v-else>
      <!-- Back Button & Bookmark -->
      <div class="flex items-center justify-between mb-6">
        <button 
          @click="router.push('/')" 
          class="flex items-center text-sm font-semibold text-slate-500 hover:text-primary-600 transition-colors duration-200"
        >
          <span class="mr-2">&larr;</span> 뉴스 목록으로 돌아가기
        </button>

        <button 
          @click="toggleBookmark"
          class="flex items-center gap-1.5 px-4 py-2 rounded-full border text-sm font-medium transition-all duration-300"
          :class="isBookmarked 
            ? 'bg-amber-50 border-amber-200 text-amber-600 shadow-sm' 
            : 'bg-white border-slate-200 text-slate-500 hover:border-amber-200 hover:text-amber-500'"
        >
          <span>★</span> {{ isBookmarked ? '북마크 취소' : '북마크 저장' }}
        </button>
      </div>

      <!-- Article & Term Split Layout -->
      <div class="grid xl:grid-cols-[minmax(0,760px)_minmax(280px,1fr)] lg:grid-cols-[minmax(0,720px)_minmax(260px,1fr)] gap-8 justify-center">
        <!-- Main Content (2/3 width) -->
        <article class="bg-white rounded-3xl border border-slate-200/80 p-6 md:p-8 shadow-premium">
          <!-- Meta -->
          <div class="flex items-center gap-2 mb-4 flex-wrap">
            <BaseBadge :value="selectedArticle.category" />
            <span class="text-xs text-slate-400 font-light ml-auto">{{ articleMetaText }}</span>
          </div>

          <!-- Title -->
          <h1 class="text-2xl md:text-3xl font-extrabold text-slate-900 mb-4 leading-snug">
            {{ selectedArticle.title }}
          </h1>

          <!-- Source URL -->
          <div v-if="selectedArticle.sourceUrl" class="mb-6">
            <a
              :href="selectedArticle.sourceUrl"
              target="_blank"
              rel="noopener noreferrer"
              class="inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-primary-600 transition-colors duration-200 border border-slate-200 hover:border-primary-300 rounded-full px-3 py-1.5 bg-slate-50 hover:bg-primary-50"
            >
              <span class="font-medium">{{ selectedArticle.source }}</span>
              <span class="opacity-60">·</span>
              <span>원문 보기 ↗</span>
            </a>
          </div>

          <!-- AI 3-line Summary -->
          <div v-if="summaryItems.length" class="mb-8 p-5 bg-amber-50 border border-amber-200/70 rounded-2xl">
            <div class="flex items-center gap-2 mb-2">
              <span class="text-base">💡</span>
              <span class="text-xs font-bold text-amber-700 uppercase tracking-wider">AI 3줄 요약</span>
            </div>
            <ol class="summary-list">
              <li v-for="(item, index) in summaryItems" :key="`${index}-${item}`">
                {{ item }}
              </li>
            </ol>
          </div>

          <!-- Body Text with Clickable Terms -->
          <div class="article-body">
            <p 
              v-for="(parts, pIdx) in formattedParagraphs" 
              :key="pIdx"
              class="article-paragraph"
            >
              <template v-for="(part, partIdx) in parts" :key="partIdx">
                <span v-if="part.type === 'text'">{{ part.value }}</span>
                <button 
                  v-else-if="part.type === 'term' && part.hasDefinition"
                  @click="showTermDefinition(part.value)"
                  class="mx-1 px-1 bg-amber-100 hover:bg-amber-200 text-amber-900 font-medium rounded border-b-2 border-amber-400 transition-colors duration-200"
                  title="단어 설명 보기"
                >
                  {{ part.value }}
                </button>
                <span v-else class="font-medium text-slate-900">{{ part.value }}</span>
              </template>
            </p>
          </div>

          <div v-if="canExpandArticle" class="mt-8 flex justify-center">
            <button
              type="button"
              class="rounded-full border border-slate-200 bg-slate-50 px-5 py-2 text-sm font-semibold text-slate-600 transition-colors duration-200 hover:border-primary-300 hover:bg-primary-50 hover:text-primary-700"
              @click="isArticleExpanded = !isArticleExpanded"
            >
              {{ isArticleExpanded ? '기사 접기' : '전체 기사 펼치기' }}
            </button>
          </div>

          <!-- Related Listed Companies -->
          <div v-if="relatedStocks.length" class="mt-10 pt-8 border-t border-slate-100">
            <div class="flex items-center gap-2 mb-4">
              <span class="text-base">🏢</span>
              <span class="text-sm font-bold text-slate-700">뉴스 연관 기업 정보</span>
            </div>
            <div class="flex flex-wrap gap-3">
              <a
                v-for="stock in relatedStocks"
                :key="stock.stockCode"
                :href="`https://finance.naver.com/item/main.naver?code=${stock.stockCode}`"
                target="_blank"
                rel="noopener noreferrer"
                class="flex flex-col gap-1.5 px-4 py-3 bg-slate-50 border border-slate-200 hover:border-primary-300 hover:bg-primary-50 rounded-xl transition-all duration-200 cursor-pointer min-w-[160px]"
              >
                <div class="flex items-center gap-2">
                  <span class="text-xs font-bold text-slate-800">{{ stock.stockName }}</span>
                  <span class="text-xs font-mono text-slate-400 bg-slate-100 px-1.5 py-0.5 rounded">{{ stock.stockCode }}</span>
                  <span class="text-xs text-slate-400 ml-auto">&#8599;</span>
                </div>
                <div v-if="stock.price != null" class="flex items-center gap-2">
                  <span class="text-sm font-bold text-slate-900">{{ stock.price.toLocaleString() }}원</span>
                  <span
                    class="text-xs font-semibold px-1.5 py-0.5 rounded"
                    :class="{
                      'text-red-600 bg-red-50': stock.trend === 'UP',
                      'text-blue-600 bg-blue-50': stock.trend === 'DOWN',
                      'text-slate-500 bg-slate-100': stock.trend === 'FLAT'
                    }"
                  >{{ stock.trend === 'UP' ? '▲' : stock.trend === 'DOWN' ? '▼' : '━' }}
                  {{ stock.changePct != null ? Math.abs(stock.changePct).toFixed(2) + '%' : '' }}</span>
                  <span
                    v-if="stock.marketReaction"
                    class="text-xs px-1.5 py-0.5 rounded font-medium"
                    :class="{
                      'text-red-700 bg-red-100': stock.marketReaction === '급등',
                      'text-orange-700 bg-orange-100': stock.marketReaction === '상승',
                      'text-slate-600 bg-slate-100': stock.marketReaction === '보합',
                      'text-blue-700 bg-blue-100': stock.marketReaction === '하락',
                      'text-indigo-700 bg-indigo-100': stock.marketReaction === '급락'
                    }"
                  >{{ stock.marketReaction }}</span>
                </div>
                <div v-if="stock.volume != null" class="text-xs text-slate-400">
                  거래량 {{ stock.volume.toLocaleString() }}
                </div>
                <p v-if="stock.relationReason" class="text-xs text-slate-500 leading-relaxed">{{ stock.relationReason }}</p>
              </a>
            </div>
            <p class="text-xs text-slate-400 mt-3">* AI가 추출한 연관 종목입니다. 투자 판단의 근거로 삼지 마세요.</p>
          </div>

          <!-- Call to Action Buttons -->
          <div class="border-t border-slate-100 mt-12 pt-8 flex flex-col sm:flex-row gap-4 justify-end">
            <BaseButton
              @click="goToQuiz"
              variant="outline"
              class="py-3 px-6 rounded-xl font-bold"
            >
              🧠 퀴즈 풀기
            </BaseButton>
            <BaseButton 
              @click="writeReview"
              class="py-3 px-6 rounded-xl font-bold"
            >
              ✏️ 학습 요약/리뷰 작성
            </BaseButton>
          </div>
        </article>

        <!-- Sidebar / Tooltip panel (1/3 width) -->
        <aside class="lg:col-span-1">
          <div class="bg-slate-50 rounded-3xl border border-slate-200/80 p-6 sticky top-8 shadow-sm">
            <div class="flex items-center gap-2 mb-4 border-b border-slate-200 pb-3">
              <span class="text-lg">📖</span>
              <h3 class="font-bold text-slate-800 text-base">경제 용어 해설</h3>
            </div>

            <!-- Dynamic Term Details -->
            <div v-if="selectedTerm" class="space-y-4 animate-[fadeIn_0.3s_ease-out]">
              <h4 class="text-lg font-bold text-amber-800 bg-amber-50 px-3 py-1.5 rounded-lg border border-amber-200/50 inline-block">
                {{ selectedTerm.name }}
              </h4>
              <p class="text-slate-600 text-sm leading-relaxed font-light">
                {{ selectedTerm.definition }}
              </p>
              <div class="text-xs text-slate-400 pt-2 flex items-center gap-1.5">
                <span>ⓘ</span>
                <span>{{ selectedTerm.source }}</span>
              </div>
            </div>

            <!-- Default State (no term selected) -->
            <div v-else class="text-center py-12 text-slate-400">
              <div class="text-4xl mb-3 opacity-60">💡</div>
              <p class="text-sm font-light leading-relaxed">
                본문 속 노란색으로 강조된<br>
                <strong>[경제 용어]</strong>를 클릭하시면<br>
                이곳에서 상세한 뜻풀이가 나타납니다.
              </p>
            </div>

            <!-- Term Checklist index -->
            <div class="mt-8 border-t border-slate-200 pt-6">
              <h4 class="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">이 기사의 핵심 용어</h4>
              <ul class="space-y-2">
                <li 
                  v-for="t in selectedArticleTerms" 
                  :key="t.name"
                  @click="showTermDefinition(t.name)"
                  class="text-sm text-slate-600 hover:text-primary-600 cursor-pointer flex items-center justify-between p-2 rounded-lg hover:bg-white border border-transparent hover:border-slate-100 transition-all duration-200"
                >
                  <span>{{ t.name }}</span>
                  <span class="text-xs text-slate-300">&rarr;</span>
                </li>
              </ul>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.summary-list {
  display: grid;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
  color: #78350f;
  font-size: 14px;
  line-height: 1.7;
  word-break: keep-all;
}

.summary-list li {
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr);
  gap: 8px;
  align-items: start;
}

.summary-list li::before {
  content: counter(list-item);
  display: inline-flex;
  width: 24px;
  height: 24px;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: #f59e0b;
  color: #fff7ed;
  font-size: 12px;
  font-weight: 800;
  line-height: 1;
}

.article-body {
  max-width: 720px;
  color: #334155;
  font-size: 16px;
  font-weight: 400;
  line-height: 1.85;
  word-break: keep-all;
  overflow-wrap: anywhere;
}

.article-paragraph {
  margin: 0;
}

.article-paragraph + .article-paragraph {
  margin-top: 26px;
}

@media (min-width: 768px) {
  .article-body {
    font-size: 17px;
    line-height: 1.9;
  }
}
</style>
