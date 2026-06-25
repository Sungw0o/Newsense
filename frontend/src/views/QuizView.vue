<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useQuizStore } from '../stores/useQuizStore'

const route = useRoute()
const router = useRouter()
const articleId = route.params.id

const quizStore = useQuizStore()
const { quizzes, currentQuizIndex, answers, isLoading } = storeToRefs(quizStore)

const currentQuiz = computed(() => quizStore.currentQuiz)
const progress = computed(() => quizStore.progressPercentage)

const optionKeys = ['A', 'B', 'C', 'D', 'E']

const selectAnswer = (answer) => {
  if (currentQuiz.value) quizStore.saveAnswer(currentQuiz.value.id, answer)
}

const nextQuiz = () => quizStore.nextQuiz()
const prevQuiz = () => quizStore.prevQuiz()

const isSubmitting = ref(false)
const fetchError = ref(false)

const submitQuiz = async () => {
  if (isSubmitting.value) return
  const unansweredCount = quizzes.value.filter(q => !answers.value[q.id]).length
  if (unansweredCount > 0 && !confirm('아직 풀지 않은 문제가 있습니다. 그래도 제출하시겠습니까?')) return

  isSubmitting.value = true
  try {
    await quizStore.submitAnswers()
    router.push(`/quiz/${articleId}/result`)
  } catch {
    alert('퀴즈 제출에 실패했습니다. 다시 시도해 주세요.')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(async () => {
  try {
    await quizStore.fetchQuizzes(articleId)
  } catch (err) {
    console.error('Failed to fetch quizzes:', err)
    fetchError.value = true
  }
})
</script>

<template>
  <div class="quiz-shell">
    <!-- Top bar -->
    <div class="topbar">
      <span class="qmeta">
        <span class="tag tag-pill tag-eco">퀴즈</span>
        <b>문제 {{ currentQuizIndex + 1 }}</b>
        <span style="opacity:0.4;">/</span>
        <span>{{ quizzes.length }}</span>
      </span>
      <button class="exit-btn" @click="router.push(`/articles/${articleId}`)">
        ← 기사로 돌아가기
      </button>
    </div>

    <!-- Progress -->
    <div class="prog">
      <div class="prog-fill" :style="{ width: `${progress}%` }"></div>
    </div>
    <div class="prog-dots">
      <span
        v-for="(q, i) in quizzes"
        :key="q.id"
        class="prog-dot"
        :class="{
          done: i < currentQuizIndex && answers[q.id],
          current: i === currentQuizIndex,
          wrong: i < currentQuizIndex && !answers[q.id],
        }"
      ></span>
    </div>

    <!-- Loading -->
    <div v-if="isLoading" class="center-state">
      <div class="spinner"></div>
      <p class="eyebrow" style="margin-top:16px; justify-content:center;">AI가 퀴즈를 생성하는 중</p>
    </div>

    <!-- Error -->
    <div v-else-if="fetchError" class="center-state">
      <p style="font-size:24px; margin-bottom:12px;">😓</p>
      <p style="font-size:15px; font-weight:700; color:var(--ink);">퀴즈를 준비하지 못했어요.</p>
      <p style="font-size:13px; color:var(--ink-2); margin-top:6px;">잠시 후 다시 시도해 주세요.</p>
      <button
        style="margin-top:20px; padding:10px 20px; border-radius:10px; border:1px solid rgba(0,0,0,0.12); background:#fff; cursor:pointer; font-size:13px; font-weight:700;"
        @click="router.push(`/articles/${articleId}`)"
      >기사로 돌아가기</button>
    </div>

    <!-- Empty -->
    <div v-else-if="quizzes.length === 0" class="center-state">
      <p style="font-size:15px; color:var(--ink-2);">이 기사에는 퀴즈가 없습니다.</p>
    </div>

    <!-- Quiz card -->
    <div v-else-if="currentQuiz">
      <div class="qcard">
        <p class="q-eyebrow">
          <span class="dot"></span>
          {{ currentQuiz.type === 'OX' ? 'OX 퀴즈' : '객관식' }}
        </p>
        <h2 class="q-text">{{ currentQuiz.question }}</h2>

        <!-- Options -->
        <div class="opts">
          <button
            v-for="(opt, idx) in currentQuiz.options"
            :key="opt"
            class="opt"
            :class="{
              selected: answers[currentQuiz.id] === opt,
              disabled: !!answers[currentQuiz.id],
            }"
            @click="selectAnswer(opt)"
            :disabled="!!answers[currentQuiz.id]"
          >
            <span class="opt-key">{{ optionKeys[idx] ?? (idx + 1) }}</span>
            <span class="opt-label">{{ opt }}</span>
            <span class="opt-ind"></span>
          </button>
        </div>
      </div>

      <!-- Footer nav -->
      <div class="quiz-foot">
        <button class="ghost-btn" @click="prevQuiz" :disabled="currentQuizIndex === 0">
          ← 이전
        </button>

        <button
          v-if="currentQuizIndex < quizzes.length - 1"
          class="ghost-btn"
          @click="nextQuiz"
        >
          다음 →
        </button>

        <button
          v-else
          class="btn-primary"
          @click="submitQuiz"
          :disabled="isSubmitting"
        >
          {{ isSubmitting ? '제출 중…' : '답안 제출하기' }}
          <span class="submit-arrow">↗</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.quiz-shell {
  max-width: 720px;
  margin: 0 auto;
  padding: 32px 0 80px;
}

/* Top bar */
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.qmeta {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-family: 'Nanum Gothic', monospace;
  font-size: 13px;
  color: var(--ink-2, #4a5161);
}
.qmeta b { color: var(--ink, #0a0d12); font-weight: 600; }
.dark .qmeta b { color: #f4f6fa; }

.exit-btn {
  font-size: 13px;
  color: var(--ink-2, #4a5161);
  background: rgba(255,255,255,0.6);
  border: 1px solid rgba(0,0,0,0.08);
  border-radius: 999px;
  padding: 6px 14px;
  cursor: pointer;
  font-weight: 500;
  backdrop-filter: blur(20px);
  transition: color .15s, border-color .15s;
}
.exit-btn:hover { color: var(--ink, #0a0d12); border-color: rgba(0,0,0,0.2); }
.dark .exit-btn {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.12);
  color: #a4adbf;
}
.dark .exit-btn:hover { color: #f4f6fa; }

/* Progress */
.prog {
  height: 8px;
  background: rgba(0,0,0,0.06);
  border-radius: 999px;
  overflow: hidden;
  margin-bottom: 10px;
}
.dark .prog { background: rgba(255,255,255,0.08); }

.prog-fill {
  height: 100%;
  background: linear-gradient(90deg, #4FB3FF 0%, #0084ff 100%);
  border-radius: 999px;
  box-shadow: 0 0 12px rgba(0,132,255,0.45);
  transition: width .35s ease;
}

.prog-dots {
  display: flex;
  gap: 6px;
  margin-bottom: 28px;
}
.prog-dot {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background: rgba(0,0,0,0.08);
  transition: background .25s;
}
.dark .prog-dot { background: rgba(255,255,255,0.10); }
.prog-dot.done    { background: #0084ff; }
.prog-dot.current { background: var(--ink, #0a0d12); }
.dark .prog-dot.current { background: #f4f6fa; }
.prog-dot.wrong   { background: #b02a2a; }

/* Question card */
.qcard {
  padding: 32px 32px 28px;
  background: rgba(255,255,255,0.65);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 24px;
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  box-shadow:
    inset 0 4px 4px 0 rgba(255,255,255,0.4),
    0 30px 60px -22px rgba(20,40,80,0.20);
  margin-bottom: 24px;
}
.dark .qcard {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.10);
  box-shadow:
    inset 0 1px 0 0 rgba(255,255,255,0.10),
    0 24px 60px -22px rgba(0,0,0,0.6);
}

.q-eyebrow {
  font-family: 'Nanum Gothic', monospace;
  font-size: 11px;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  color: #0084ff;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #0084ff;
  box-shadow: 0 0 0 3px rgba(0,132,255,0.18);
}

.q-text {
  font-family: 'Fustat', sans-serif;
  font-weight: 700;
  font-size: 24px;
  line-height: 1.35;
  letter-spacing: -0.5px;
  margin: 0 0 24px;
  color: var(--ink, #0a0d12);
}
.dark .q-text { color: #f4f6fa; }

/* Options */
.opts { display: flex; flex-direction: column; gap: 10px; }

.opt {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 15px 18px;
  background: rgba(255,255,255,0.85);
  border: 1.5px solid rgba(0,0,0,0.08);
  border-radius: 16px;
  cursor: pointer;
  font: inherit;
  text-align: left;
  width: 100%;
  transition: all .18s ease;
}
.opt:hover:not(:disabled) {
  border-color: rgba(0,132,255,0.45);
  background: #fff;
  transform: translateY(-1px);
  box-shadow: 0 8px 20px -10px rgba(0,132,255,0.30);
}

.dark .opt {
  background: rgba(20,24,34,0.70);
  border-color: rgba(255,255,255,0.10);
  color: #f4f6fa;
}
.dark .opt:hover:not(:disabled) {
  background: rgba(30,38,56,0.85);
  border-color: rgba(0,132,255,0.55);
}

.opt.selected {
  border-color: #0084ff;
  background: #F4F8FF;
  box-shadow: inset 0 0 0 1px #0084ff;
}
.dark .opt.selected {
  background: rgba(0,132,255,0.12);
  border-color: rgba(0,132,255,0.70);
}

.opt:disabled { cursor: default; }

.opt-key {
  width: 34px; height: 34px;
  flex-shrink: 0;
  border-radius: 10px;
  background: #F4F8FF;
  color: #0084ff;
  border: 1px solid rgba(0,132,255,0.15);
  display: flex; align-items: center; justify-content: center;
  font-family: 'Nanum Gothic', monospace;
  font-weight: 600;
  font-size: 13px;
}
.dark .opt-key { background: rgba(0,132,255,0.15); color: #6CB8FF; border-color: rgba(0,132,255,0.30); }

.opt-label {
  flex: 1;
  font-size: 15px;
  color: var(--ink, #0a0d12);
  font-weight: 500;
  line-height: 1.4;
}
.dark .opt-label { color: #f4f6fa; }

.opt.selected .opt-key { background: #0084ff; color: #fff; border-color: #0084ff; }
.opt.selected .opt-label { color: #0056cc; }
.dark .opt.selected .opt-label { color: #9BCBFF; }

.opt-ind {
  width: 22px; height: 22px;
  flex-shrink: 0;
  border-radius: 50%;
  border: 2px solid rgba(0,0,0,0.12);
  display: flex; align-items: center; justify-content: center;
  color: #fff;
  font-size: 12px;
  transition: all .18s;
}
.dark .opt-ind { border-color: rgba(255,255,255,0.20); }
.opt.selected .opt-ind { background: #0084ff; border-color: #0084ff; }
.opt.selected .opt-ind::after { content: "✓"; }

/* Footer nav */
.quiz-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.ghost-btn {
  padding: 10px 20px;
  background: rgba(255,255,255,0.6);
  border: 1px solid rgba(0,0,0,0.08);
  border-radius: 12px;
  font-size: 13.5px;
  font-weight: 600;
  color: var(--ink-2, #4a5161);
  cursor: pointer;
  transition: all .15s;
}
.ghost-btn:hover:not(:disabled) { color: var(--ink, #0a0d12); border-color: rgba(0,0,0,0.18); }
.ghost-btn:disabled { opacity: 0.35; cursor: default; }
.dark .ghost-btn {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.12);
  color: #a4adbf;
}

.submit-arrow {
  font-size: 11px;
}

/* Center state (loading/empty) */
.center-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
  gap: 12px;
  text-align: center;
}

@media (max-width: 640px) {
  .qcard { padding: 22px 18px 20px; }
  .q-text { font-size: 20px; }
  .quiz-shell { padding: 24px 0 60px; }
}
</style>
