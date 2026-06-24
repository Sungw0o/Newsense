<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/useUserStore'

const router = useRouter()
const userStore = useUserStore()

const step = ref(1) // 1: 관심사, 2: 난이도

const categories = [
  { id: 'MACRO_ECONOMY',      name: '거시경제',   icon: '📈', desc: 'GDP, 금리, 물가' },
  { id: 'FINANCE_INVESTMENT', name: '금융/투자',   icon: '💹', desc: '주식, 채권, 펀드' },
  { id: 'POLICY_SYSTEM',      name: '정책/제도',   icon: '🏛️', desc: '정부정책, 부동산' },
  { id: 'COMPANY_INDUSTRY',   name: '기업/산업',   icon: '🏢', desc: '기업실적, 산업동향' },
  { id: 'GLOBAL_ECONOMY',     name: '글로벌경제',  icon: '🌐', desc: '환율, 무역, 해외시황' },
]

const levels = [
  {
    id: 'BASIC',
    label: '초급',
    icon: '🌱',
    desc: '경제가 처음이에요. 쉬운 개념부터 시작할게요.',
  },
  {
    id: 'INTERMEDIATE',
    label: '중급',
    icon: '📘',
    desc: '기본 개념은 알아요. 더 깊이 파고들고 싶어요.',
  },
  {
    id: 'ADVANCED',
    label: '고급',
    icon: '🎓',
    desc: '심화 분석과 전문 용어도 편하게 소화해요.',
  },
]

const selectedCategories = ref(new Set())
const selectedLevel = ref('BASIC')
const isSaving = ref(false)
const errorMsg = ref('')

const canGoNext = computed(() => selectedCategories.value.size >= 1)

const toggleCategory = (id) => {
  const s = new Set(selectedCategories.value)
  s.has(id) ? s.delete(id) : s.add(id)
  selectedCategories.value = s
}

const goNext = () => {
  if (!canGoNext.value) {
    errorMsg.value = '관심 카테고리를 최소 1개 이상 선택해 주세요.'
    return
  }
  errorMsg.value = ''
  step.value = 2
}

const handleSubmit = async () => {
  if (selectedCategories.value.size === 0) {
    step.value = 1
    errorMsg.value = '관심 카테고리를 최소 1개 이상 선택해 주세요.'
    return
  }
  isSaving.value = true
  errorMsg.value = ''
  try {
    await userStore.updateUserProfile({
      interests: [...selectedCategories.value],
      level: selectedLevel.value,
    })
    router.replace('/')
  } catch {
    errorMsg.value = '저장에 실패했습니다. 다시 시도해 주세요.'
  } finally {
    isSaving.value = false
  }
}
</script>

<template>
  <div class="onboarding-shell">
    <!-- Progress -->
    <div class="progress-bar">
      <div class="progress-track">
        <div class="progress-fill" :style="{ width: step === 1 ? '50%' : '100%' }"></div>
      </div>
      <span class="progress-label">{{ step }} / 2</span>
    </div>

    <!-- Step 1: 관심사 -->
    <Transition name="slide" mode="out-in">
      <div v-if="step === 1" key="step1" class="onboarding-card">
        <span class="eyebrow">STEP 1</span>
        <h1 class="onboarding-title">어떤 경제 뉴스에 관심 있으신가요?</h1>
        <p class="onboarding-sub">선택한 카테고리 위주로 개인화 피드가 구성됩니다. <strong>최소 1개</strong> 이상 선택해 주세요.</p>

        <div class="cat-grid">
          <button
            v-for="cat in categories"
            :key="cat.id"
            type="button"
            class="cat-btn"
            :class="{ active: selectedCategories.has(cat.id) }"
            @click="toggleCategory(cat.id)"
          >
            <span class="cat-icon">{{ cat.icon }}</span>
            <div class="cat-info">
              <strong>{{ cat.name }}</strong>
              <span>{{ cat.desc }}</span>
            </div>
            <span class="cat-check" :class="{ checked: selectedCategories.has(cat.id) }">
              <svg v-if="selectedCategories.has(cat.id)" width="13" height="13" viewBox="0 0 13 13" fill="none">
                <path d="M2 6.5L5.5 10L11 3" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </span>
          </button>
        </div>

        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

        <div class="onboarding-foot">
          <button class="btn-primary" :disabled="!canGoNext" @click="goNext">
            다음 단계로 →
          </button>
        </div>
      </div>

      <!-- Step 2: 난이도 -->
      <div v-else key="step2" class="onboarding-card">
        <span class="eyebrow">STEP 2</span>
        <h1 class="onboarding-title">경제 지식 수준을 알려주세요.</h1>
        <p class="onboarding-sub">수준에 맞는 기사와 퀴즈를 추천해 드립니다. 나중에 마이페이지에서 변경 가능해요.</p>

        <div class="level-list">
          <button
            v-for="lv in levels"
            :key="lv.id"
            type="button"
            class="level-btn"
            :class="{ active: selectedLevel === lv.id }"
            @click="selectedLevel = lv.id"
          >
            <span class="level-icon">{{ lv.icon }}</span>
            <div class="level-info">
              <strong>{{ lv.label }}</strong>
              <span>{{ lv.desc }}</span>
            </div>
            <span class="level-radio" :class="{ selected: selectedLevel === lv.id }"></span>
          </button>
        </div>

        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

        <div class="onboarding-foot two-btn">
          <button class="btn-ghost" @click="step = 1">← 이전</button>
          <button class="btn-primary" :disabled="isSaving" @click="handleSubmit">
            <span v-if="isSaving" class="spinner-sm"></span>
            {{ isSaving ? '저장 중…' : '시작하기 🚀' }}
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.onboarding-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
}

/* Progress */
.progress-bar {
  width: 100%;
  max-width: 560px;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}
.progress-track {
  flex: 1;
  height: 5px;
  background: rgba(0,0,0,0.08);
  border-radius: 999px;
  overflow: hidden;
}
.dark .progress-track { background: rgba(255,255,255,0.10); }
.progress-fill {
  height: 100%;
  background: #0084ff;
  border-radius: 999px;
  transition: width 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.progress-label {
  font-family: 'Nanum Gothic', monospace;
  font-size: 12px;
  color: var(--ink-3, #8a93a3);
  letter-spacing: 1px;
}

/* Card */
.onboarding-card {
  width: 100%;
  max-width: 560px;
  padding: 40px;
  background: rgba(255,255,255,0.70);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 28px;
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,0.90),
    0 24px 60px -20px rgba(20,40,80,0.18);
}
.dark .onboarding-card {
  background: rgba(20,24,34,0.60);
  border-color: rgba(255,255,255,0.10);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.08), 0 24px 60px -20px rgba(0,0,0,0.55);
}

.eyebrow {
  font-family: 'Nanum Gothic', monospace;
  font-size: 11px;
  letter-spacing: 2px;
  color: #0084ff;
  text-transform: uppercase;
  margin: 0 0 12px;
  display: block;
}

.onboarding-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 26px;
  letter-spacing: -0.5px;
  color: var(--ink, #0a0d12);
  margin: 0 0 8px;
  line-height: 1.3;
}
.dark .onboarding-title { color: #f4f6fa; }

.onboarding-sub {
  font-size: 14px;
  color: var(--ink-2, #4a5161);
  margin: 0 0 28px;
  line-height: 1.6;
}
.dark .onboarding-sub { color: #a4adbf; }
.onboarding-sub strong { color: #0084ff; }

/* Category grid */
.cat-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 4px;
}

.cat-btn {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: rgba(255,255,255,0.55);
  border: 1.5px solid rgba(0,0,0,0.08);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.15s;
  text-align: left;
  width: 100%;
}
.cat-btn:hover { border-color: rgba(0,132,255,0.30); transform: translateY(-1px); }
.cat-btn.active {
  background: #E8F2FF;
  border-color: rgba(0,132,255,0.45);
}
.dark .cat-btn { background: rgba(20,24,34,0.50); border-color: rgba(255,255,255,0.10); }
.dark .cat-btn:hover { border-color: rgba(0,132,255,0.40); }
.dark .cat-btn.active { background: rgba(0,132,255,0.15); border-color: rgba(0,132,255,0.45); }

.cat-icon { font-size: 24px; flex-shrink: 0; }

.cat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.cat-info strong {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink, #0a0d12);
}
.dark .cat-info strong { color: #f4f6fa; }
.cat-btn.active .cat-info strong { color: #0056cc; }
.dark .cat-btn.active .cat-info strong { color: #9BCBFF; }
.cat-info span {
  font-size: 12px;
  color: var(--ink-3, #8a93a3);
}

.cat-check {
  width: 22px; height: 22px;
  border-radius: 7px;
  border: 1.5px solid rgba(0,0,0,0.15);
  background: rgba(255,255,255,0.6);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  transition: all 0.15s;
}
.cat-check.checked {
  background: #0084ff;
  border-color: #0084ff;
}
.dark .cat-check { background: rgba(20,24,34,0.50); border-color: rgba(255,255,255,0.20); }
.dark .cat-check.checked { background: #0084ff; border-color: #0084ff; }

/* Level list */
.level-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 4px;
}

.level-btn {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: rgba(255,255,255,0.55);
  border: 1.5px solid rgba(0,0,0,0.08);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.15s;
  text-align: left;
  width: 100%;
}
.level-btn:hover { border-color: rgba(0,132,255,0.30); transform: translateY(-1px); }
.level-btn.active {
  background: #E8F2FF;
  border-color: rgba(0,132,255,0.45);
}
.dark .level-btn { background: rgba(20,24,34,0.50); border-color: rgba(255,255,255,0.10); }
.dark .level-btn:hover { border-color: rgba(0,132,255,0.40); }
.dark .level-btn.active { background: rgba(0,132,255,0.15); border-color: rgba(0,132,255,0.45); }

.level-icon { font-size: 28px; flex-shrink: 0; }
.level-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.level-info strong {
  font-size: 15px;
  font-weight: 700;
  color: var(--ink, #0a0d12);
}
.dark .level-info strong { color: #f4f6fa; }
.level-btn.active .level-info strong { color: #0056cc; }
.dark .level-btn.active .level-info strong { color: #9BCBFF; }
.level-info span { font-size: 13px; color: var(--ink-2, #4a5161); line-height: 1.4; }
.dark .level-info span { color: #a4adbf; }

.level-radio {
  width: 20px; height: 20px;
  border-radius: 50%;
  border: 2px solid rgba(0,0,0,0.15);
  flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.15s;
  position: relative;
}
.level-radio.selected {
  border-color: #0084ff;
  background: #0084ff;
}
.level-radio.selected::after {
  content: '';
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #fff;
}
.dark .level-radio { border-color: rgba(255,255,255,0.25); }

/* Footer */
.onboarding-foot {
  margin-top: 28px;
  padding-top: 24px;
  border-top: 1px solid rgba(0,0,0,0.07);
  display: flex;
  justify-content: flex-end;
}
.dark .onboarding-foot { border-color: rgba(255,255,255,0.10); }

.onboarding-foot.two-btn { justify-content: space-between; }

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  background: #0084ff;
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-primary:hover:not(:disabled) { background: #006fd6; transform: translateY(-1px); box-shadow: 0 4px 14px rgba(0,132,255,0.35); }
.btn-primary:disabled { opacity: 0.55; cursor: default; }

.btn-ghost {
  display: inline-flex;
  align-items: center;
  padding: 12px 20px;
  background: transparent;
  color: var(--ink-2, #4a5161);
  border: 1.5px solid rgba(0,0,0,0.10);
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
}
.btn-ghost:hover { border-color: rgba(0,0,0,0.20); background: rgba(0,0,0,0.03); }
.dark .btn-ghost { color: #a4adbf; border-color: rgba(255,255,255,0.12); }
.dark .btn-ghost:hover { border-color: rgba(255,255,255,0.25); background: rgba(255,255,255,0.05); }

.error-msg {
  margin: 12px 0 0;
  font-size: 13px;
  color: #c0392b;
  font-weight: 500;
}
.dark .error-msg { color: #ff8a8a; }

.spinner-sm {
  width: 14px; height: 14px;
  border: 2.5px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .65s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Slide transition */
.slide-enter-active,
.slide-leave-active { transition: all 0.28s cubic-bezier(0.4, 0, 0.2, 1); }
.slide-enter-from { opacity: 0; transform: translateX(30px); }
.slide-leave-to   { opacity: 0; transform: translateX(-30px); }

@media (max-width: 600px) {
  .onboarding-card { padding: 28px 20px; }
  .onboarding-title { font-size: 22px; }
}
</style>
