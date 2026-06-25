<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { reviewApi } from '../api/reviewApi'
import BaseButton from '../components/common/BaseButton.vue'
import ErrorMessage from '../components/common/ErrorMessage.vue'

const route = useRoute()
const router = useRouter()
const articleId = route.params.id

const summary = ref('')
const learnings = ref('')
const newTerm = ref('')
const termsList = ref([])
const isLoading = ref(false)
const errorMsg = ref('')
const reviewId = ref(null)
const isEditMode = ref(false)

const addTerm = () => {
  const trimmed = newTerm.value.trim()
  if (trimmed && !termsList.value.includes(trimmed)) {
    termsList.value.push(trimmed)
    newTerm.value = ''
  }
}

const removeTerm = (index) => {
  termsList.value.splice(index, 1)
}

const handleSaveReview = async () => {
  if (!summary.value.trim()) {
    errorMsg.value = '핵심 요약을 작성해 주세요.'
    return
  }

  errorMsg.value = ''
  isLoading.value = true

  const payload = {
    articleId: Number(articleId),
    summary: summary.value.trim(),
    learned: learnings.value.trim(),
    difficultTerms: termsList.value
  }

  try {
    if (isEditMode.value && reviewId.value) {
      await reviewApi.updateReview(reviewId.value, {
        summary: payload.summary,
        learned: payload.learned,
        difficultTerms: payload.difficultTerms
      })
      alert('성공적으로 리뷰를 수정했습니다!')
    } else {
      await reviewApi.createReview(payload)
      alert('성공적으로 리뷰를 등록했습니다!')
    }
    router.push(`/articles/${articleId}`)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || '리뷰 저장에 실패했습니다. 다시 시도해 주세요.'
  } finally {
    isLoading.value = false
  }
}

onMounted(async () => {
  isLoading.value = true
  errorMsg.value = ''
  try {
    const response = await reviewApi.getReview(articleId)
    const data = response.data || response
    if (data) {
      reviewId.value = data.id
      summary.value = data.summary || ''
      learnings.value = data.learned || ''
      termsList.value = data.difficultTerms || []
      isEditMode.value = true
    }
  } catch (err) {
    if (err.response?.status === 404) {
      console.log('No existing review found. Ready to create a new one.')
    } else {
      errorMsg.value = err.response?.data?.message || '기존 리뷰 정보를 확인하는 중 오류가 발생했습니다.'
    }
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <div class="flex items-center justify-between mb-6">
      <button 
        @click="router.push(`/articles/${articleId}`)" 
        class="text-sm font-semibold text-slate-500 hover:text-primary-600 transition-colors duration-200"
      >
        &larr; 기사 본문으로
      </button>
      <h1 class="text-xl font-bold text-slate-800">학습 리뷰 작성</h1>
    </div>

    <div class="bg-white rounded-3xl border border-slate-200/80 p-8 shadow-premium space-y-6">
      <h2 class="text-sm text-slate-400 font-semibold uppercase tracking-wider mb-2">
        ✏️ NEWS SUMMARY & REVIEW
      </h2>

      <!-- Summary -->
      <div>
        <label for="summary" class="block text-sm font-bold text-slate-700 mb-2">
          1. 핵심 요약 (자유 텍스트)
        </label>
        <textarea 
          id="summary" 
          v-model="summary"
          rows="4"
          placeholder="방금 읽은 기사의 핵심 내용을 자신의 언어로 직접 요약해 보세요..."
          class="w-full p-4 rounded-xl border border-slate-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-100 outline-none transition-all duration-300 placeholder:text-slate-300 text-slate-800 text-sm"
        ></textarea>
        <div class="text-right text-xs text-slate-400 mt-1 font-light">
          {{ summary.length }} 자 작성됨
        </div>
      </div>

      <!-- Learnings -->
      <div>
        <label for="learnings" class="block text-sm font-bold text-slate-700 mb-2">
          2. 새로 알게 된 점
        </label>
        <textarea 
          id="learnings" 
          v-model="learnings"
          rows="3"
          placeholder="기사를 읽고 새롭게 깨달은 지식이나 통찰을 남겨보세요..."
          class="w-full p-4 rounded-xl border border-slate-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-100 outline-none transition-all duration-300 placeholder:text-slate-300 text-slate-800 text-sm"
        ></textarea>
      </div>

      <!-- Vocabulary tags -->
      <div>
        <label class="block text-sm font-bold text-slate-700 mb-2">
          3. 어려웠거나 기록하고 싶은 용어 태그
        </label>
        <div class="flex gap-2 mb-3">
          <input 
            type="text" 
            v-model="newTerm" 
            placeholder="단어를 입력하고 추가 버튼을 누르세요..."
            @keyup.enter="addTerm"
            class="flex-1 px-4 py-2.5 rounded-xl border border-slate-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-100 outline-none transition-all duration-300 text-slate-800 text-sm"
          />
          <BaseButton 
            variant="outline" 
            @click="addTerm"
            class="py-2.5 px-4 rounded-xl font-bold text-sm"
          >
            추가
          </BaseButton>
        </div>

        <!-- Render tags -->
        <div class="flex flex-wrap gap-2">
          <span 
            v-for="(t, idx) in termsList" 
            :key="t"
            class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-amber-50 text-amber-800 border border-amber-200 rounded-lg text-xs font-semibold"
          >
            # {{ t }}
            <button 
              @click="removeTerm(idx)"
              class="text-amber-400 hover:text-amber-600 ml-1 font-bold"
              title="삭제"
            >
              &times;
            </button>
          </span>
          <span v-if="termsList.length === 0" class="text-xs text-slate-400 font-light">
            아직 추가된 태그가 없습니다.
          </span>
        </div>
      </div>

      <ErrorMessage v-if="errorMsg" :message="errorMsg" />

      <!-- Submit -->
      <div class="border-t border-slate-100 pt-6 flex justify-end gap-3">
        <BaseButton 
          variant="outline" 
          @click="router.push(`/articles/${articleId}`)"
          class="py-2.5 px-5 rounded-xl font-bold text-sm"
        >
          취소
        </BaseButton>
        <BaseButton 
          variant="primary" 
          :loading="isLoading"
          @click="handleSaveReview"
          class="py-2.5 px-6 rounded-xl font-bold text-sm bg-primary-600 hover:bg-primary-700 text-white shadow-md shadow-primary-100"
        >
          리뷰 저장하기
        </BaseButton>
      </div>
    </div>
  </div>
</template>
