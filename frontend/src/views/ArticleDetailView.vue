<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseButton from '../components/common/BaseButton.vue'
import BaseBadge from '../components/common/BaseBadge.vue'

const route = useRoute()
const router = useRouter()
const articleId = ref(route.params.id)

const isBookmarked = ref(false)
const selectedTerm = ref(null)

// 모의 기사 상세 데이터
const article = ref({
  id: articleId.value,
  title: '기준금리 인하가 청년 전세대출에 미치는 영향',
  category: '금융',
  difficulty: '초급',
  createdAt: '2026-06-22',
  readTime: '3분',
  content: `한국은행 금융통화위원회가 이번 달 [기준금리]를 기존 3.50%에서 3.25%로 0.25%p 인하하기로 결정했습니다. 이는 오랜 기간 지속된 고금리 기조에서 완화책으로 선회한 첫 신호탄입니다. 

금리 인하의 주된 배경에는 최근 안정세를 보이고 있는 소비자물가 상승률과 내수 경제 활성화 필요성이 자리하고 있습니다. 

이번 금리 인하로 가장 직접적인 영향을 받는 부문 중 하나가 바로 [전세자금대출]입니다. 시중 은행의 대출 금리는 한국은행의 기준금리와 시장 금리(코픽스 등)에 연동되어 움직이기 때문입니다. 대출 금리가 하락하면 전세 세입자의 이자 부담이 줄어들어 전세 수요가 자극될 가능성이 있습니다.

특히 경제적 기반이 취약한 청년층의 경우, 정부가 지원하는 [버팀목전세대출] 등 정책 자금 대출의 금리 인하 여부에도 관심이 쏠리고 있습니다. 보통 이러한 정책 금융 상품은 일반 시중 은행 대출보다 낮게 책정되지만, 시장 상황을 반영해 추가적인 우대 금리 조정을 거치기도 합니다. 

부동산 전문가들은 "금리 인하 폭이 0.25%p로 다소 미미하게 느껴질 수 있으나, 향후 금리가 추가 인하될 수 있다는 기대 심리가 시장에 반영된다면 가계 부채 증가와 수도권 집값 상승 요인으로 작용할 수도 있다"며 신중한 태도를 견지하고 있습니다.`
})

// 기사 내 매핑된 용어 사전
const terms = ref([
  {
    name: '기준금리',
    definition: '한 나라의 금리를 대표하는 정책 금리로, 한국은행 금융통화위원회에서 결정하는 금리 기준입니다. 시중 은행들의 예금 및 대출 금리의 기준점이 됩니다.'
  },
  {
    name: '전세자금대출',
    definition: '주택 전세계약 시 부족한 전세 보증금을 마련하기 위해 시중 은행이나 금융기관으로부터 전세 보증금을 담보로 받거나 신용으로 빌리는 대출 상품입니다.'
  },
  {
    name: '버팀목전세대출',
    definition: '서민과 청년층의 주거 안정을 위해 주택도시기금을 재원으로 국가가 저리로 지원해 주는 대표적인 정책 전세 대출 상품입니다.'
  }
])

// 텍스트에서 대괄호 [용어] 패턴을 감지하여 클릭 가능한 엘리먼트로 변환
const formattedParagraphs = ref([])

const processContent = () => {
  const paragraphs = article.value.content.split('\n\n')
  formattedParagraphs.value = paragraphs.map(p => {
    // [용어]를 감지하여 파싱
    const parts = []
    let remaining = p
    const regex = /\[(.*?)\]/g
    let match

    let lastIndex = 0
    while ((match = regex.exec(p)) !== null) {
      const termName = match[1]
      const startIndex = match.index
      
      // 용어 매칭 이전 텍스트 추가
      if (startIndex > lastIndex) {
        parts.push({
          type: 'text',
          value: p.substring(lastIndex, startIndex)
        })
      }
      
      // 용어 텍스트 추가
      parts.push({
        type: 'term',
        value: termName,
        hasDefinition: terms.value.some(t => t.name === termName)
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
}

const showTermDefinition = (termName) => {
  const found = terms.value.find(t => t.name === termName)
  if (found) {
    selectedTerm.value = found
  }
}

const toggleBookmark = () => {
  isBookmarked.value = !isBookmarked.value
}

const startQuiz = () => {
  router.push(`/articles/${articleId.value}/quiz`)
}

const writeReview = () => {
  router.push(`/articles/${articleId.value}/review`)
}

onMounted(() => {
  processContent()
})
</script>

<template>
  <div class="max-w-6xl mx-auto px-4 py-8">
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
    <div class="grid lg:grid-cols-3 gap-8">
      <!-- Main Content (2/3 width) -->
      <article class="lg:col-span-2 bg-white rounded-3xl border border-slate-200/80 p-8 shadow-premium">
        <!-- Meta -->
        <div class="flex items-center gap-2 mb-4">
          <BaseBadge :value="article.category" />
          <span 
            class="text-xs font-semibold px-2.5 py-0.5 rounded-full"
            :class="{
              'bg-brand-100 text-brand-700': article.difficulty === '초급',
              'bg-secondary-100 text-secondary-700': article.difficulty === '중급',
              'bg-accent-100 text-accent-700': article.difficulty === '고급'
            }"
          >
            난이도 {{ article.difficulty }}
          </span>
          <span class="text-xs text-slate-400 font-light ml-auto">{{ article.createdAt }} • 읽기 {{ article.readTime }}</span>
        </div>

        <!-- Title -->
        <h1 class="text-2xl md:text-3xl font-extrabold text-slate-900 mb-8 leading-snug">
          {{ article.title }}
        </h1>

        <!-- Body Text with Clickable Terms -->
        <div class="space-y-6 text-slate-700 leading-relaxed font-light text-base md:text-lg">
          <p 
            v-for="(parts, pIdx) in formattedParagraphs" 
            :key="pIdx"
            class="whitespace-pre-line"
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

        <!-- Call to Action Buttons -->
        <div class="border-t border-slate-100 mt-12 pt-8 flex flex-col sm:flex-row gap-4 justify-end">
          <BaseButton 
            variant="outline" 
            @click="writeReview"
            class="py-3 px-6 rounded-xl font-bold"
          >
            ✏️ 학습 요약/리뷰 작성
          </BaseButton>
          <BaseButton 
            variant="primary" 
            @click="startQuiz"
            class="py-3 px-8 rounded-xl font-bold bg-primary-600 hover:bg-primary-700 text-white shadow-md shadow-primary-100"
          >
            🧩 퀴즈 풀고 지식 검증
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
              <span>기획재정부 시사경제사전 연동</span>
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
                v-for="t in terms" 
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
</template>

<style scoped>
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
