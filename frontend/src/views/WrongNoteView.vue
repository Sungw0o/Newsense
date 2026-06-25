<script setup>
import { ref, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useWrongNoteStore } from '../stores/useWrongNoteStore'

const wrongNoteStore = useWrongNoteStore()
const { wrongNotes, isLoading, error } = storeToRefs(wrongNoteStore)

const activeTab = ref('unresolved')

onMounted(async () => {
  try {
    await wrongNoteStore.fetchWrongNotes()
  } catch (err) {
    console.error('Failed to load wrong notes:', err)
  }
})

// computed를 사용하여 탭에 따른 필터링을 동적으로 처리
const filteredNotes = computed(() => {
  if (activeTab.value === 'unresolved') {
    return wrongNotes.value.filter(n => !n.isResolved)
  } else if (activeTab.value === 'resolved') {
    return wrongNotes.value.filter(n => n.isResolved)
  } else {
    return wrongNotes.value
  }
})

const setTab = (tab) => {
  activeTab.value = tab
}

const toggleResolve = async (note) => {
  try {
    await wrongNoteStore.toggleResolve(note.id)
  } catch (err) {
    alert('해결 상태 변경에 실패했습니다.')
  }
}

const deleteNote = async (id) => {
  if (confirm('정말로 이 오답 노트를 삭제하시겠습니까?')) {
    try {
      await wrongNoteStore.deleteWrongNote(id)
    } catch (err) {
      alert('오답 노트 삭제에 실패했습니다.')
    }
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <header class="mb-8">
      <h1 class="text-3xl font-extrabold text-slate-800 dark:text-white mb-2">오답노트</h1>
      <p class="text-slate-500 dark:text-slate-400 font-light">틀린 문제를 정답/해설과 함께 다시 학습하고, 완전히 이해했다면 해결 완료 상태로 변경해 보세요.</p>
    </header>

    <!-- Error State -->
    <div v-if="error" class="mb-8 p-4 bg-accent-50/50 dark:bg-accent-950/20 border border-accent-200 dark:border-accent-800/30 rounded-2xl text-accent-700 dark:text-accent-400 text-sm">
      ⚠️ {{ error }}
    </div>

    <!-- Tab filter -->
    <div class="flex gap-2 border-b border-slate-200/60 dark:border-white/5 pb-4 mb-6">
      <button 
        @click="setTab('unresolved')"
        class="px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-300 select-none"
        :class="activeTab === 'unresolved' 
          ? 'bg-primary-600 text-white shadow-glass-glow' 
          : 'text-slate-500 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-white/5'"
      >
        미해결 오답 ({{ wrongNotes?.filter(n => !n.isResolved).length || 0 }})
      </button>
      <button 
        @click="setTab('resolved')"
        class="px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-300 select-none"
        :class="activeTab === 'resolved' 
          ? 'bg-primary-600 text-white shadow-glass-glow' 
          : 'text-slate-500 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-white/5'"
      >
        해결 완료 ({{ wrongNotes?.filter(n => n.isResolved).length || 0 }})
      </button>
      <button 
        @click="setTab('all')"
        class="px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-300 select-none"
        :class="activeTab === 'all' 
          ? 'bg-primary-600 text-white shadow-glass-glow' 
          : 'text-slate-500 hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-white/5'"
      >
        전체 보기
      </button>
    </div>

    <!-- Notes List -->
    <div class="space-y-6">
      <!-- Loading state -->
      <div v-if="isLoading" class="space-y-4">
        <div v-for="n in 2" :key="n" class="glass-panel rounded-2xl p-6 shadow-sm animate-pulse space-y-4">
          <div class="flex justify-between items-center">
            <div class="h-4 bg-slate-100 dark:bg-slate-800 rounded w-1/4"></div>
            <div class="h-8 bg-slate-100 dark:bg-slate-800 rounded w-24"></div>
          </div>
          <div class="h-6 bg-slate-100 dark:bg-slate-800 rounded w-3/4"></div>
          <div class="grid grid-cols-2 gap-3">
            <div class="h-10 bg-slate-50/50 dark:bg-white/5 rounded"></div>
            <div class="h-10 bg-slate-50/50 dark:bg-white/5 rounded"></div>
          </div>
          <div class="h-16 bg-slate-50/50 dark:bg-white/5 rounded"></div>
        </div>
      </div>

      <!-- Real Data List -->
      <template v-else>
        <div 
          v-for="note in filteredNotes" 
          :key="note.id"
          class="glass-panel rounded-2xl p-6 shadow-sm relative group transition-all duration-300"
          :class="note.isResolved 
            ? 'border-brand-500/30 dark:border-brand-400/30 bg-brand-50/10 dark:bg-brand-500/5' 
            : 'border-slate-200/50 dark:border-white/5'"
        >
          <!-- Card Header Info -->
          <div class="flex items-center justify-between gap-4 mb-4">
            <div class="flex items-center gap-2">
              <span class="text-xs font-semibold px-2 py-0.5 rounded bg-slate-100 dark:bg-white/5 text-slate-500 dark:text-slate-400">{{ note.category }}</span>
              <span class="text-xs text-slate-400 dark:text-slate-500 font-light">{{ note.date }}</span>
            </div>

            <div class="flex gap-2">
              <button 
                @click="toggleResolve(note)"
                class="text-xs px-2.5 py-1 rounded-full border transition-all duration-200"
                :class="note.isResolved 
                  ? 'bg-brand-50 border-brand-200 text-brand-700 dark:bg-brand-950/40 dark:border-brand-800 dark:text-brand-400 font-semibold' 
                  : 'bg-white dark:bg-white/5 border-slate-200 dark:border-white/5 text-slate-400 dark:text-slate-500 hover:border-brand-300 dark:hover:border-brand-500 hover:text-brand-600 dark:hover:text-brand-400'"
              >
                ✓ {{ note.isResolved ? '해결 완료됨' : '해결 처리하기' }}
              </button>
              <button 
                @click="deleteNote(note.id)"
                class="text-xs px-2.5 py-1 rounded-full border border-slate-200 dark:border-white/5 bg-white dark:bg-white/5 text-slate-400 dark:text-slate-500 hover:border-accent-300 dark:hover:border-accent-500 hover:text-accent-600 dark:hover:text-accent-400 transition-all duration-200"
              >
                삭제
              </button>
            </div>
          </div>

          <!-- Question -->
          <h3 class="text-base font-bold text-slate-800 dark:text-slate-100 mb-4 leading-snug">
            {{ note.question }}
          </h3>

          <!-- Answers -->
          <div class="grid sm:grid-cols-2 gap-3 mb-4 text-xs md:text-sm">
            <div class="p-3 bg-slate-50/50 dark:bg-white/5 rounded-xl flex items-center justify-between">
              <span class="text-slate-400 dark:text-slate-500">내가 고른 오답:</span>
              <span class="font-bold text-accent-600 dark:text-accent-400">{{ note.userAns }}</span>
            </div>
            <div class="p-3 bg-slate-50/50 dark:bg-white/5 rounded-xl flex items-center justify-between">
              <span class="text-slate-400 dark:text-slate-500">올바른 정답:</span>
              <span class="font-bold text-brand-700 dark:text-brand-400">{{ note.correctAns }}</span>
            </div>
          </div>

          <!-- Explanation -->
          <div class="bg-amber-50/50 dark:bg-amber-950/20 border border-amber-100 dark:border-amber-900/30 rounded-xl p-4 text-xs md:text-sm leading-relaxed text-slate-600 dark:text-slate-300">
            <strong class="block text-amber-800 dark:text-amber-400 font-bold mb-1">복습 해설:</strong>
            {{ note.explanation }}
          </div>
        </div>

        <!-- Empty State -->
        <div v-if="filteredNotes.length === 0" class="text-center py-20 glass-panel rounded-2xl shadow-sm">
          <div class="text-4xl mb-4">🎉</div>
          <p class="text-slate-500 dark:text-slate-400 font-light">여기에 보관된 오답이 없습니다!</p>
        </div>
      </template>
    </div>
  </div>
</template>
