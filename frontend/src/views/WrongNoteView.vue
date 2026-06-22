<script setup>
import { ref } from 'vue'

// 모의 오답 데이터
const wrongNotes = ref([
  {
    id: 201,
    question: '이번 본문에서 설명한 한국은행 금리 인하의 직접적인 배경으로 올바르지 않은 것은 무엇인가요?',
    userAns: '소비자물가 상승률의 안정세',
    correctAns: '부동산 거래량 폭증을 유도하기 위한 정책적 목적',
    explanation: '한국은행의 이번 금리 인하 배경은 소비자물가 안정을 기반으로 한 실물 내수 경제 활성화이며, 인위적인 부동산 거래 폭증 유도는 정부의 공식 목적이 아닙니다.',
    category: '금융',
    isResolved: false,
    date: '2026-06-22'
  },
  {
    id: 202,
    question: '다음 중 총부채원리금상환비율(DSR)에 대한 설명으로 옳은 것은 무엇인가요?',
    userAns: '주택가격 대비 대출 가능 금액을 한정하는 비율',
    correctAns: '연간 총 소득 대비 모든 가계 대출의 연간 원리금 상환액 비율',
    explanation: 'DSR은 연간 소득에서 차지하는 모든 대출(주담대 + 신용대출 등)의 원리금 상환 비율을 말하며, 주택가격 대비 비율은 LTV입니다.',
    category: '부동산',
    isResolved: true,
    date: '2026-06-21'
  }
])

const activeTab = ref('unresolved')

const filteredNotes = ref(wrongNotes.value)

const setTab = (tab) => {
  activeTab.value = tab
  if (tab === 'unresolved') {
    filteredNotes.value = wrongNotes.value.filter(n => !n.isResolved)
  } else if (tab === 'resolved') {
    filteredNotes.value = wrongNotes.value.filter(n => n.isResolved)
  } else {
    filteredNotes.value = wrongNotes.value
  }
}

const toggleResolve = (note) => {
  note.isResolved = !note.isResolved
  setTab(activeTab.value) // 갱신
}

const deleteNote = (id) => {
  if (confirm('정말로 이 오답 노트를 삭제하시겠습니까?')) {
    wrongNotes.value = wrongNotes.value.filter(n => n.id !== id)
    setTab(activeTab.value)
  }
}

// 초기 세팅
setTab('unresolved')
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <header class="mb-8">
      <h1 class="text-3xl font-extrabold text-slate-900 mb-2">오답노트</h1>
      <p class="text-slate-500 font-light">틀린 문제를 정답/해설과 함께 다시 학습하고, 완전히 이해했다면 해결 완료 상태로 변경해 보세요.</p>
    </header>

    <!-- Tab filter -->
    <div class="flex gap-2 border-b border-slate-200 pb-4 mb-6">
      <button 
        @click="setTab('unresolved')"
        class="px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-300"
        :class="activeTab === 'unresolved' ? 'bg-primary-600 text-white shadow-md' : 'text-slate-500 hover:bg-slate-100'"
      >
        미해결 오답 ({{ wrongNotes.filter(n => !n.isResolved).length }})
      </button>
      <button 
        @click="setTab('resolved')"
        class="px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-300"
        :class="activeTab === 'resolved' ? 'bg-primary-600 text-white shadow-md' : 'text-slate-500 hover:bg-slate-100'"
      >
        해결 완료 ({{ wrongNotes.filter(n => n.isResolved).length }})
      </button>
      <button 
        @click="setTab('all')"
        class="px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-300"
        :class="activeTab === 'all' ? 'bg-primary-600 text-white shadow-md' : 'text-slate-500 hover:bg-slate-100'"
      >
        전체 보기
      </button>
    </div>

    <!-- Notes List -->
    <div class="space-y-6">
      <div 
        v-for="note in filteredNotes" 
        :key="note.id"
        class="bg-white rounded-2xl border p-6 shadow-sm relative group"
        :class="note.isResolved ? 'border-brand-200 bg-brand-50/10' : 'border-slate-200'"
      >
        <!-- Card Header Info -->
        <div class="flex items-center justify-between gap-4 mb-4">
          <div class="flex items-center gap-2">
            <span class="text-xs font-semibold px-2 py-0.5 rounded bg-slate-100 text-slate-500">{{ note.category }}</span>
            <span class="text-xs text-slate-400 font-light">{{ note.date }}</span>
          </div>

          <div class="flex gap-2">
            <button 
              @click="toggleResolve(note)"
              class="text-xs px-2.5 py-1 rounded-full border transition-all duration-200"
              :class="note.isResolved 
                ? 'bg-brand-50 border-brand-200 text-brand-700 font-semibold' 
                : 'bg-white border-slate-200 text-slate-400 hover:border-brand-300 hover:text-brand-600'"
            >
              ✓ {{ note.isResolved ? '해결 완료됨' : '해결 처리하기' }}
            </button>
            <button 
              @click="deleteNote(note.id)"
              class="text-xs px-2.5 py-1 rounded-full border border-slate-200 bg-white text-slate-400 hover:border-accent-300 hover:text-accent-600 transition-all duration-200"
            >
              삭제
            </button>
          </div>
        </div>

        <!-- Question -->
        <h3 class="text-base font-bold text-slate-800 mb-4 leading-snug">
          {{ note.question }}
        </h3>

        <!-- Answers -->
        <div class="grid sm:grid-cols-2 gap-3 mb-4 text-xs md:text-sm">
          <div class="p-3 bg-slate-50 rounded-xl flex items-center justify-between">
            <span class="text-slate-400">내가 고른 오답:</span>
            <span class="font-bold text-accent-600">{{ note.userAns }}</span>
          </div>
          <div class="p-3 bg-slate-50 rounded-xl flex items-center justify-between">
            <span class="text-slate-400">올바른 정답:</span>
            <span class="font-bold text-brand-700">{{ note.correctAns }}</span>
          </div>
        </div>

        <!-- Explanation -->
        <div class="bg-amber-50/50 border border-amber-100 rounded-xl p-4 text-xs md:text-sm leading-relaxed text-slate-600">
          <strong class="block text-amber-800 font-bold mb-1">복습 해설:</strong>
          {{ note.explanation }}
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="filteredNotes.length === 0" class="text-center py-20 bg-white rounded-2xl border border-slate-200">
        <div class="text-4xl mb-4">🎉</div>
        <p class="text-slate-500 font-light">여기에 보관된 오답이 없습니다!</p>
      </div>
    </div>
  </div>
</template>
