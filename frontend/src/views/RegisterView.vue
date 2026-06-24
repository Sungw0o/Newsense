<script setup>
import { ref, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/useUserStore'
import { authApi } from '../api/authApi'

const router = useRouter()
const userStore = useUserStore()

const email = ref('')
const nickname = ref('')
const password = ref('')
const passwordConfirm = ref('')
const isLoading = ref(false)
const errorMsg = ref('')
const apiOrigin = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1').replace('/api/v1', '')

// 'idle' | 'checking' | 'available' | 'taken' | 'error'
const emailStatus = ref('idle')

function debounce(fn, delay) {
  let timer
  return (...args) => {
    clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}

let currentCheckSeq = 0

const checkEmail = debounce(async (val) => {
  if (!val || !val.includes('@')) {
    emailStatus.value = 'idle'
    return
  }
  const seq = ++currentCheckSeq
  emailStatus.value = 'checking'
  try {
    const res = await authApi.checkUsername(val)
    if (seq !== currentCheckSeq) return // 더 최신 요청이 있으면 무시
    emailStatus.value = res?.data?.available ? 'available' : 'taken'
  } catch {
    if (seq !== currentCheckSeq) return
    emailStatus.value = 'error'
  }
}, 300)

watch(email, (val) => {
  emailStatus.value = 'idle'
  checkEmail(val)
})

const isPasswordTouched = computed(() => password.value.length > 0)
const isPasswordConfirmTouched = computed(() => passwordConfirm.value.length > 0)
const isPasswordLengthValid = computed(() => password.value.length >= 8 && password.value.length <= 72)
const isPasswordMatch = computed(() => password.value === passwordConfirm.value)
const isPasswordValid = computed(() => isPasswordLengthValid.value && isPasswordMatch.value)

const passwordInputClass = computed(() => ({
  'input-ok': isPasswordTouched.value && isPasswordLengthValid.value,
  'input-err': isPasswordTouched.value && !isPasswordLengthValid.value,
}))

const passwordConfirmInputClass = computed(() => ({
  'input-ok': isPasswordConfirmTouched.value && isPasswordMatch.value,
  'input-err': isPasswordConfirmTouched.value && !isPasswordMatch.value,
}))

const canSubmit = computed(() =>
  email.value.trim()
  && nickname.value.trim()
  && emailStatus.value === 'available'
  && isPasswordValid.value
  && !isLoading.value
)

const handleRegister = async () => {
  if (!email.value || !nickname.value || !password.value || !passwordConfirm.value) {
    errorMsg.value = '모든 필드를 입력해 주세요.'
    return
  }
  if (emailStatus.value !== 'available') {
    errorMsg.value = '이메일 중복 확인을 완료해 주세요.'
    return
  }
  if (password.value !== passwordConfirm.value) {
    errorMsg.value = '비밀번호가 일치하지 않습니다.'
    return
  }
  if (password.value.length < 8) {
    errorMsg.value = '비밀번호는 최소 8자 이상이어야 합니다.'
    return
  }
  errorMsg.value = ''
  isLoading.value = true
  try {
    await userStore.register({ email: email.value, nickname: nickname.value, password: password.value })
    alert('회원가입이 완료되었습니다! 로그인 해 주세요.')
    router.push('/login')
  } catch (err) {
    errorMsg.value = err.response?.data?.message || '회원가입 중 오류가 발생했습니다. 다시 시도해 주세요.'
  } finally {
    isLoading.value = false
  }
}

const startSocialLogin = (provider) => {
  window.location.href = `${apiOrigin}/oauth2/authorization/${provider}`
}
</script>

<template>
  <div class="auth-wrap">
    <!-- Left showcase -->
    <div class="showcase">
      <router-link to="/" class="brand-mini">
        <span class="brand-mark"></span>
        <span class="brand-name">Newsense</span>
      </router-link>

      <h1 class="sc-title">
        새로운<br>
        <span class="accent">경제 학습</span>의<br>
        시작
      </h1>
      <p class="sc-sub">
        가입하면 AI 퀴즈, 오답노트, 맞춤 피드 등 모든 기능을 무료로 이용할 수 있습니다.
      </p>

      <div class="sc-points">
        <div class="sc-point">
          <span class="ic">📰</span>
          <div>
            <b>매일 새로운 기사</b>
            <span>기획재정부·한국은행 경제 기사가 매일 업데이트됩니다</span>
          </div>
        </div>
        <div class="sc-point">
          <span class="ic">🧠</span>
          <div>
            <b>AI 퀴즈 자동 생성</b>
            <span>기사를 읽은 직후 핵심 개념을 점검하는 퀴즈</span>
          </div>
        </div>
        <div class="sc-point">
          <span class="ic">📈</span>
          <div>
            <b>학습 통계 대시보드</b>
            <span>연속 학습 스트릭과 정답률로 성장 과정을 추적</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Right form card -->
    <div class="auth-card">
      <h2 class="auth-title">회원가입</h2>
      <p class="auth-desc">무료 계정을 만들어 바로 시작하세요.</p>

      <form @submit.prevent="handleRegister">
        <div class="field">
          <label for="nickname">닉네임</label>
          <input id="nickname" type="text" v-model="nickname" placeholder="경제왕" autocomplete="nickname" required />
        </div>

        <div class="field">
          <label for="email">이메일</label>
          <div class="input-wrap">
            <input
              id="email"
              type="email"
              v-model="email"
              placeholder="name@example.com"
              autocomplete="email"
              required
              :class="{ 'input-ok': emailStatus === 'available', 'input-err': emailStatus === 'taken' }"
            />
            <span v-if="emailStatus === 'checking'" class="input-spinner"></span>
          </div>
          <p v-if="emailStatus === 'available'" class="field-feedback ok">사용 가능한 이메일입니다.</p>
          <p v-else-if="emailStatus === 'taken'" class="field-feedback err">이미 사용 중인 이메일입니다.</p>
          <p v-else-if="emailStatus === 'error'" class="field-feedback err">확인 중 오류가 발생했습니다.</p>
        </div>

        <div class="field">
          <label for="password">비밀번호 <span class="field-hint">(8자 이상)</span></label>
          <input
            id="password"
            type="password"
            v-model="password"
            placeholder="••••••••"
            autocomplete="new-password"
            required
            :class="passwordInputClass"
          />
          <p v-if="isPasswordTouched && isPasswordLengthValid" class="field-feedback ok">사용 가능한 비밀번호입니다.</p>
          <p v-else-if="isPasswordTouched" class="field-feedback err">비밀번호는 8자 이상 72자 이하여야 합니다.</p>
        </div>

        <div class="field">
          <label for="passwordConfirm">비밀번호 확인</label>
          <input
            id="passwordConfirm"
            type="password"
            v-model="passwordConfirm"
            placeholder="••••••••"
            autocomplete="new-password"
            required
            :class="passwordConfirmInputClass"
          />
          <p v-if="isPasswordConfirmTouched && isPasswordMatch" class="field-feedback ok">비밀번호가 일치합니다.</p>
          <p v-else-if="isPasswordConfirmTouched" class="field-feedback err">비밀번호가 일치하지 않습니다.</p>
        </div>

        <div v-if="errorMsg" class="error-msg">
          <span>⚠</span> {{ errorMsg }}
        </div>

        <button type="submit" class="btn-primary submit-btn" :disabled="!canSubmit">
          <span v-if="isLoading" class="spinner-sm"></span>
          <span>{{ isLoading ? '가입 중…' : '회원가입' }}</span>
          <span v-if="!isLoading" class="submit-arrow">→</span>
        </button>
      </form>

      <div class="social-login">
        <div class="social-divider"><span>소셜 계정으로 간편 시작</span></div>
        <div class="social-buttons">
          <button type="button" class="social-btn kakao" @click="startSocialLogin('kakao')">
            <img class="social-favicon" src="https://www.kakaocorp.com/page/favicon.ico" alt="" />
            카카오
          </button>
          <button type="button" class="social-btn naver" @click="startSocialLogin('naver')">
            <img class="social-favicon" src="https://www.naver.com/favicon.ico" alt="" />
            네이버
          </button>
          <button type="button" class="social-btn google" @click="startSocialLogin('google')">
            <img class="social-favicon" src="https://www.google.com/favicon.ico" alt="" />
            Google
          </button>
        </div>
      </div>

      <div class="auth-foot">
        이미 계정이 있으신가요?
        <router-link to="/login">로그인</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-wrap {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 60px;
  max-width: 1100px;
  margin: 0 auto;
  align-items: center;
  padding: 40px 0 80px;
  min-height: 70vh;
}

.brand-mini {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--ink, #0a0d12);
  margin-bottom: 32px;
}
.brand-mark {
  width: 28px; height: 28px;
  border-radius: 9px;
  background: radial-gradient(circle at 30% 30%, #9CCBFF 0%, #0084ff 60%, #0a4a99 100%);
  box-shadow: inset 0 2px 3px rgba(255,255,255,0.6), 0 2px 8px rgba(0,132,255,0.40);
}
.brand-name {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 20px;
  letter-spacing: -0.5px;
  color: var(--ink, #0a0d12);
}
.dark .brand-name { color: #f4f6fa; }

.sc-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 48px;
  line-height: 1.08;
  letter-spacing: -1.5px;
  margin: 0 0 16px;
  color: var(--ink, #0a0d12);
}
.dark .sc-title { color: #f4f6fa; }

.accent {
  background: linear-gradient(95deg, #0084ff 0%, #4FB3FF 60%, #0a4a99 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.dark .accent {
  background: linear-gradient(95deg, #6CB8FF 0%, #B8DAFF 60%, #fff 100%);
  -webkit-background-clip: text;
  background-clip: text;
}

.sc-sub {
  font-size: 15px;
  color: var(--ink-2, #4a5161);
  line-height: 1.55;
  margin: 0 0 30px;
}
.dark .sc-sub { color: #a4adbf; }

.sc-points { display: flex; flex-direction: column; gap: 16px; }
.sc-point { display: flex; align-items: flex-start; gap: 14px; }
.ic {
  width: 36px; height: 36px;
  flex-shrink: 0;
  border-radius: 10px;
  background: rgba(255,255,255,0.65);
  border: 1px solid rgba(0,0,0,0.07);
  backdrop-filter: blur(20px);
  display: flex; align-items: center; justify-content: center;
  font-size: 16px;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.6);
}
.dark .ic { background: rgba(20,24,34,0.55); border-color: rgba(255,255,255,0.12); }
.sc-point b { font-family: 'Fustat', sans-serif; font-weight: 700; font-size: 14.5px; color: var(--ink, #0a0d12); display: block; margin-bottom: 2px; }
.dark .sc-point b { color: #f4f6fa; }
.sc-point span { font-size: 13px; color: var(--ink-2, #4a5161); line-height: 1.45; }
.dark .sc-point span { color: #a4adbf; }

.auth-card {
  padding: 36px 36px 32px;
  background: rgba(255,255,255,0.70);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 24px;
  backdrop-filter: blur(48px) saturate(180%);
  -webkit-backdrop-filter: blur(48px) saturate(180%);
  box-shadow:
    inset 0 1px 0 0 rgba(255,255,255,0.85),
    inset 0 4px 8px 0 rgba(255,255,255,0.35),
    0 30px 80px -22px rgba(20,40,80,0.25);
}
.dark .auth-card {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.12);
  box-shadow: inset 0 1px 0 0 rgba(255,255,255,0.10), 0 30px 80px -22px rgba(0,0,0,0.6);
}

.auth-title { font-family: 'Fustat', sans-serif; font-weight: 800; font-size: 26px; letter-spacing: -0.8px; margin: 0 0 6px; color: var(--ink, #0a0d12); }
.dark .auth-title { color: #f4f6fa; }
.auth-desc { font-size: 14px; color: var(--ink-2, #4a5161); margin: 0 0 26px; }
.dark .auth-desc { color: #a4adbf; }

form { display: flex; flex-direction: column; gap: 14px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 13px; font-weight: 500; color: var(--ink-2, #4a5161); display: flex; align-items: center; gap: 6px; }
.dark .field label { color: #a4adbf; }
.field-hint { font-size: 11.5px; color: var(--ink-3, #8a93a3); font-family: 'Nanum Gothic', monospace; }

.field input {
  appearance: none;
  padding: 12px 16px;
  background: rgba(255,255,255,0.85);
  border: 1.5px solid rgba(0,0,0,0.08);
  border-radius: 12px;
  font: inherit;
  font-size: 14px;
  color: var(--ink, #0a0d12);
  outline: none;
  transition: all .15s;
  width: 100%;
}
.field input:focus { border-color: #0084ff; background: #fff; box-shadow: 0 0 0 4px rgba(0,132,255,0.12); }
.field input::placeholder { color: var(--ink-3, #8a93a3); }
.dark .field input { background: rgba(20,24,34,0.70); border-color: rgba(255,255,255,0.12); color: #f4f6fa; }
.dark .field input:focus { background: rgba(20,24,34,0.90); }

.input-wrap { position: relative; }
.input-wrap input { width: 100%; }

.input-ok { border-color: #1a9e5c !important; }
.input-ok:focus { box-shadow: 0 0 0 4px rgba(26,158,92,0.12) !important; }
.dark .input-ok { border-color: #3ad07b !important; }

.input-err { border-color: #b02a2a !important; }
.input-err:focus { box-shadow: 0 0 0 4px rgba(176,42,42,0.12) !important; }
.dark .input-err { border-color: #ff6a6a !important; }

.input-spinner {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  width: 14px; height: 14px;
  border: 2px solid rgba(0,132,255,0.2);
  border-top-color: #0084ff;
  border-radius: 50%;
  animation: spin .65s linear infinite;
  pointer-events: none;
}

.field-feedback {
  font-size: 12px;
  margin: 4px 0 0;
  font-family: 'Nanum Gothic', monospace;
}
.field-feedback.ok { color: #1a9e5c; }
.field-feedback.err { color: #b02a2a; }
.dark .field-feedback.ok { color: #3ad07b; }
.dark .field-feedback.err { color: #ff8a8a; }

.error-msg {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 14px;
  background: rgba(255,237,237,0.85);
  border: 1px solid rgba(176,42,42,0.25);
  border-radius: 10px;
  font-size: 13.5px;
  color: #b02a2a;
}
.dark .error-msg { background: rgba(255,106,106,0.13); border-color: rgba(255,106,106,0.40); color: #ff8a8a; }

.submit-btn { width: 100%; justify-content: center; padding: 12px 20px; font-size: 14.5px; border-radius: 13px; gap: 8px; }
.submit-btn:disabled { opacity: 0.65; cursor: default; }
.submit-arrow { font-size: 14px; }

.spinner-sm {
  width: 16px; height: 16px;
  border: 2.5px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .65s linear infinite;
  flex-shrink: 0;
}
@keyframes spin { to { transform: rotate(360deg); } }

.auth-foot { margin-top: 22px; padding-top: 18px; border-top: 1px solid rgba(0,0,0,0.07); text-align: center; font-size: 13.5px; color: var(--ink-2, #4a5161); }
.dark .auth-foot { border-color: rgba(255,255,255,0.10); color: #a4adbf; }
.auth-foot a { color: #0084ff; font-weight: 600; text-decoration: none; margin-left: 4px; }
.auth-foot a:hover { text-decoration: underline; }

.social-login { margin-top: 18px; }
.social-divider {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  font-size: 12px;
  color: var(--ink-3, #8a93a3);
}
.social-divider::before,
.social-divider::after {
  content: "";
  flex: 1;
  height: 1px;
  background: rgba(0,0,0,0.08);
}
.dark .social-divider::before,
.dark .social-divider::after { background: rgba(255,255,255,0.10); }
.social-buttons {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.social-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  min-height: 42px;
  border-radius: 12px;
  border: 1px solid rgba(0,0,0,0.08);
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  transition: all .2s;
}
.social-btn:hover { transform: translateY(-1px); box-shadow: 0 12px 24px -16px rgba(20,40,80,0.25); }
.social-btn.kakao { background: #FEE500; color: #191600; }
.social-btn.naver { background: #03C75A; color: #fff; }
.social-btn.google { background: #fff; color: #1f2937; }
.dark .social-btn.google { background: rgba(255,255,255,0.08); color: #f4f6fa; border-color: rgba(255,255,255,0.12); }
.social-favicon {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  object-fit: cover;
  flex-shrink: 0;
}

@media (max-width: 900px) {
  .auth-wrap { grid-template-columns: 1fr; gap: 40px; min-height: auto; }
  .sc-title { font-size: 36px; }
  .auth-card { padding: 28px 22px; }
}
</style>
