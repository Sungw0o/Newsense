<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/useUserStore'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const email = ref('')
const password = ref('')
const isLoading = ref(false)
const errorMsg = ref('')
const showPassword = ref(false)
const apiOrigin = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1').replace('/api/v1', '')

const handleLogin = async () => {
  if (!email.value || !password.value) {
    errorMsg.value = '이메일과 비밀번호를 입력해주세요.'
    return
  }
  errorMsg.value = ''
  isLoading.value = true
  try {
    await userStore.login({ email: email.value, password: password.value })
    const rawRedirect = route.query.redirect
    const target = Array.isArray(rawRedirect) ? rawRedirect[0] : rawRedirect
    const redirectPath = (typeof target === 'string' && target.startsWith('/') && !target.startsWith('//') && !target.startsWith('/\\')) ? target : '/'
    router.push(redirectPath)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || '로그인 중 오류가 발생했습니다. 다시 시도해 주세요.'
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
        경제 뉴스로<br>
        <span class="accent">문해력</span>을<br>
        키우세요
      </h1>
      <p class="sc-sub">
        매일 엄선된 경제 기사를 읽고, AI 퀴즈로 핵심 개념을 내 것으로 만들어 보세요.
      </p>

      <div class="sc-points">
        <div class="sc-point">
          <span class="ic">
            <svg viewBox="0 0 18 18" fill="none" stroke-width="2" xmlns="http://www.w3.org/2000/svg">
              <path d="M9 2L11 7h5l-4 3 1.5 5L9 12l-4.5 3L6 10 2 7h5z" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </span>
          <div>
            <b>AI 맞춤 퀴즈</b>
            <span>기사를 읽은 직후 핵심 개념을 확인하는 퀴즈</span>
          </div>
        </div>
        <div class="sc-point">
          <span class="ic">
            <svg viewBox="0 0 18 18" fill="none" stroke-width="2" xmlns="http://www.w3.org/2000/svg">
              <rect x="2" y="3" width="14" height="12" rx="2"/>
              <path d="M6 7h6M6 10h4" stroke-linecap="round"/>
            </svg>
          </span>
          <div>
            <b>오답노트 자동 정리</b>
            <span>틀린 문제를 모아두고 반복 학습으로 완성</span>
          </div>
        </div>
        <div class="sc-point">
          <span class="ic">
            <svg viewBox="0 0 18 18" fill="none" stroke-width="2" xmlns="http://www.w3.org/2000/svg">
              <path d="M9 2v14M2 9h14" stroke-linecap="round"/>
            </svg>
          </span>
          <div>
            <b>학습 이력 추적</b>
            <span>연속 학습 스트릭과 통계로 성장을 확인</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Right form card -->
    <div class="auth-card">
      <h2 class="auth-title">로그인</h2>
      <p class="auth-desc">계속하려면 이메일과 비밀번호를 입력하세요.</p>

      <form @submit.prevent="handleLogin">
        <div class="field">
          <label for="email">
            이메일
          </label>
          <input
            id="email"
            type="email"
            v-model="email"
            placeholder="name@example.com"
            autocomplete="email"
            required
          />
        </div>

        <div class="field">
          <label for="password">
            비밀번호
          </label>
          <div class="input-wrap">
            <input
              id="password"
              :type="showPassword ? 'text' : 'password'"
              v-model="password"
              placeholder="••••••••"
              autocomplete="current-password"
              required
            />
            <button type="button" class="toggle-pw" @click="showPassword = !showPassword">
              {{ showPassword ? '숨기기' : '보기' }}
            </button>
          </div>
        </div>

        <!-- Error message -->
        <div v-if="errorMsg" class="error-msg">
          <span>⚠</span> {{ errorMsg }}
        </div>

        <button type="submit" class="btn-primary submit-btn" :disabled="isLoading">
          <span v-if="isLoading" class="spinner-sm"></span>
          <span>{{ isLoading ? '로그인 중…' : '로그인' }}</span>
          <span v-if="!isLoading" class="submit-arrow">→</span>
        </button>
      </form>

      <div class="social-login">
        <div class="social-divider"><span>또는 소셜 계정으로 계속</span></div>
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
        아직 계정이 없으신가요?
        <router-link to="/register">회원가입</router-link>
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

/* Showcase (left) */
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
}
.dark .brand-name { color: #f4f6fa; }

.sc-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 52px;
  line-height: 1.08;
  letter-spacing: -1.6px;
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
  max-width: 420px;
}
.dark .sc-sub { color: #a4adbf; }

.sc-points {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.sc-point {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}
.ic {
  width: 36px; height: 36px;
  flex-shrink: 0;
  border-radius: 10px;
  background: rgba(255,255,255,0.65);
  border: 1px solid rgba(0,0,0,0.07);
  backdrop-filter: blur(20px);
  display: flex; align-items: center; justify-content: center;
  color: #0084ff;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.6);
}
.dark .ic { background: rgba(20,24,34,0.55); border-color: rgba(255,255,255,0.12); }
.ic svg { width: 17px; height: 17px; stroke: #0084ff; fill: none; stroke-width: 2; }

.sc-point b {
  font-family: 'Fustat', sans-serif;
  font-weight: 700;
  font-size: 14.5px;
  color: var(--ink, #0a0d12);
  display: block;
  margin-bottom: 2px;
}
.dark .sc-point b { color: #f4f6fa; }
.sc-point span {
  font-size: 13px;
  color: var(--ink-2, #4a5161);
  line-height: 1.45;
}
.dark .sc-point span { color: #a4adbf; }

/* Auth card (right) */
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
  box-shadow:
    inset 0 1px 0 0 rgba(255,255,255,0.10),
    0 30px 80px -22px rgba(0,0,0,0.6);
}

.auth-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 26px;
  letter-spacing: -0.8px;
  margin: 0 0 6px;
  color: var(--ink, #0a0d12);
}
.dark .auth-title { color: #f4f6fa; }

.auth-desc {
  font-size: 14px;
  color: var(--ink-2, #4a5161);
  margin: 0 0 26px;
}
.dark .auth-desc { color: #a4adbf; }

form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.field label {
  font-size: 13px;
  font-weight: 500;
  color: var(--ink-2, #4a5161);
}
.dark .field label { color: #a4adbf; }

.field input {
  appearance: none;
  padding: 13px 16px;
  background: rgba(255,255,255,0.85);
  border: 1.5px solid rgba(0,0,0,0.08);
  border-radius: 12px;
  font: inherit;
  font-size: 14.5px;
  color: var(--ink, #0a0d12);
  outline: none;
  transition: all .15s;
  width: 100%;
}
.field input:focus {
  border-color: #0084ff;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(0,132,255,0.12);
}
.field input::placeholder { color: var(--ink-3, #8a93a3); }
.dark .field input {
  background: rgba(20,24,34,0.70);
  border-color: rgba(255,255,255,0.12);
  color: #f4f6fa;
}
.dark .field input:focus { background: rgba(20,24,34,0.90); }

.input-wrap { position: relative; }
.toggle-pw {
  position: absolute;
  right: 10px; top: 50%;
  transform: translateY(-50%);
  background: transparent;
  border: none;
  color: var(--ink-3, #8a93a3);
  font-size: 12px;
  cursor: pointer;
  padding: 6px 8px;
  font-family: 'Nanum Gothic', monospace;
}

.error-msg {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 14px;
  background: rgba(255,237,237,0.85);
  border: 1px solid rgba(176,42,42,0.25);
  border-radius: 10px;
  font-size: 13.5px;
  color: #b02a2a;
}
.dark .error-msg {
  background: rgba(255,106,106,0.13);
  border-color: rgba(255,106,106,0.40);
  color: #ff8a8a;
}

.submit-btn {
  width: 100%;
  justify-content: center;
  padding: 13px 20px;
  font-size: 15px;
  border-radius: 13px;
  gap: 8px;
}
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

.auth-foot {
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px solid rgba(0,0,0,0.07);
  text-align: center;
  font-size: 13.5px;
  color: var(--ink-2, #4a5161);
}
.social-login {
  margin-top: 18px;
}
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
.dark .auth-foot { border-color: rgba(255,255,255,0.10); color: #a4adbf; }
.auth-foot a {
  color: #0084ff;
  font-weight: 600;
  text-decoration: none;
  margin-left: 4px;
}
.auth-foot a:hover { text-decoration: underline; }

/* Responsive */
@media (max-width: 900px) {
  .auth-wrap {
    grid-template-columns: 1fr;
    gap: 40px;
    min-height: auto;
  }
  .sc-title { font-size: 38px; }
  .auth-card { padding: 28px 24px; }
}
</style>
