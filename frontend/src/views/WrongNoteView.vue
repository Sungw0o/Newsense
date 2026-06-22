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
      <h1 class="text-3xl font-extrabold text-slate-900 mb-2">오답노트</h1>
      <p class="text-slate-500 font-light">틀린 문제를 정답/해설과 함께 다시 학습하고, 완전히 이해했다면 해결 완료 상태로 변경해 보세요.</p>
    </header>

    <!-- Error State -->
    <div v-if="error" class="mb-8 p-4 bg-accent-50 border border-accent-200 rounded-2xl text-accent-700 text-sm">
      ⚠️ {{ error }}
    </div>

    <!-- Tab filter -->
    <div class="flex gap-2 border-b border-slate-200 pb-4 mb-6">
      <button 
        @click="setTab('unresolved')"
        class="px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-300"
        :class="activeTab === 'unresolved' ? 'bg-primary-600 text-white shadow-md' : 'text-slate-500 hover:bg-slate-100'"
      >
        미해결 오답 ({{ wrongNotes?.filter(n => !n.isResolved).length || 0 }})
      </button>
      <button 
        @click="setTab('resolved')"
        class="px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-300"
        :class="activeTab === 'resolved' ? 'bg-primary-600 text-white shadow-md' : 'text-slate-500 hover:bg-slate-100'"
      >
        해결 완료 ({{ wrongNotes?.filter(n => n.isResolved).length || 0 }})
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
      <!-- Loading state -->
      <div v-if="isLoading" class="space-y-4">
        <div v-for="n in 2" :key="n" class="bg-white rounded-2xl border p-6 shadow-sm animate-pulse space-y-4">
          <div class="flex justify-between items-center">
            <div class="h-4 bg-slate-100 rounded w-1/4"></div>
            <div class="h-8 bg-slate-100 rounded w-24"></div>
          </div>
          <div class="h-6 bg-slate-100 rounded w-3/4"></div>
          <div class="grid grid-cols-2 gap-3">
            <div class="h-10 bg-slate-50 rounded"></div>
            <div class="h-10 bg-slate-50 rounded"></div>
          </div>
          <div class="h-16 bg-slate-50 rounded"></div>
        </div>
      </div>

      <!-- Real Data List -->
      <template v-else>
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
      </template>
    </div>
  </div>
</template>
