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

const emailStatus = ref('idle')
const nicknameStatus = ref('idle')

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/
const nicknamePattern = /^[가-힣a-zA-Z0-9_]{2,20}$/

const normalizedEmail = computed(() => email.value.trim().toLowerCase())
const normalizedNickname = computed(() => nickname.value.trim())
const isEmailValid = computed(() => emailPattern.test(normalizedEmail.value))
const isNicknameValid = computed(() => nicknamePattern.test(normalizedNickname.value))

const checkEmailDuplicate = async () => {
  if (!isEmailValid.value) {
    emailStatus.value = normalizedEmail.value ? 'invalid' : 'idle'
    return
  }

  emailStatus.value = 'checking'
  try {
    const res = await authApi.checkEmail(normalizedEmail.value)
    emailStatus.value = res?.data?.available ? 'available' : 'taken'
  } catch {
    emailStatus.value = 'error'
  }
}

const checkNicknameDuplicate = async () => {
  if (!isNicknameValid.value) {
    nicknameStatus.value = normalizedNickname.value ? 'invalid' : 'idle'
    return
  }

  nicknameStatus.value = 'checking'
  try {
    const res = await authApi.checkNickname(normalizedNickname.value)
    nicknameStatus.value = res?.data?.available ? 'available' : 'taken'
  } catch {
    nicknameStatus.value = 'error'
  }
}

watch(email, () => {
  emailStatus.value = 'idle'
})

watch(nickname, () => {
  nicknameStatus.value = 'idle'
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
  isEmailValid.value
  && isNicknameValid.value
  && emailStatus.value === 'available'
  && nicknameStatus.value === 'available'
  && isPasswordValid.value
  && !isLoading.value
)

const handleRegister = async () => {
  if (!email.value || !nickname.value || !password.value || !passwordConfirm.value) {
    errorMsg.value = '모든 항목을 입력해 주세요.'
    return
  }
  if (!isEmailValid.value) {
    errorMsg.value = '올바른 이메일 형식으로 입력해 주세요.'
    return
  }
  if (emailStatus.value !== 'available') {
    errorMsg.value = '이메일 중복 확인을 완료해 주세요.'
    return
  }
  if (!isNicknameValid.value) {
    errorMsg.value = '닉네임은 2~20자의 한글, 영문, 숫자, 밑줄만 사용할 수 있습니다.'
    return
  }
  if (nicknameStatus.value !== 'available') {
    errorMsg.value = '닉네임 중복 확인을 완료해 주세요.'
    return
  }
  if (!isPasswordLengthValid.value) {
    errorMsg.value = '비밀번호는 8자 이상 72자 이하로 입력해 주세요.'
    return
  }
  if (!isPasswordMatch.value) {
    errorMsg.value = '비밀번호가 일치하지 않습니다.'
    return
  }

  errorMsg.value = ''
  isLoading.value = true
  try {
    await userStore.register({
      email: normalizedEmail.value,
      nickname: normalizedNickname.value,
      password: password.value,
    })
    alert('회원가입이 완료되었습니다. 로그인해 주세요.')
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
    <section class="showcase" aria-label="Newsense 소개">
      <router-link to="/" class="brand-mini">
        <span class="brand-mark"></span>
        <span class="brand-name">Newsense</span>
      </router-link>

      <h1 class="sc-title">
        뉴스로 읽고<br>
        개념으로 남기는<br>
        경제 학습
      </h1>
      <p class="sc-sub">
        관심 기사와 핵심 용어, 퀴즈 기록을 한 곳에서 관리하며 금융 문해력을 꾸준히 쌓아보세요.
      </p>

      <div class="sc-points">
        <div class="sc-point">
          <span class="point-index">01</span>
          <div>
            <b>경제 기사 큐레이션</b>
            <span>분야와 난이도에 맞춰 읽을 만한 기사를 빠르게 탐색합니다.</span>
          </div>
        </div>
        <div class="sc-point">
          <span class="point-index">02</span>
          <div>
            <b>핵심 용어 정리</b>
            <span>본문 속 경제 용어를 설명과 함께 확인하고 복습 흐름으로 이어갑니다.</span>
          </div>
        </div>
        <div class="sc-point">
          <span class="point-index">03</span>
          <div>
            <b>학습 기록 관리</b>
            <span>퀴즈와 오답 기록을 모아 약한 개념을 다시 찾아볼 수 있습니다.</span>
          </div>
        </div>
      </div>
    </section>

    <section class="auth-card" aria-label="회원가입">
      <h2 class="auth-title">회원가입</h2>
      <p class="auth-desc">이메일과 닉네임은 중복 확인 후 가입할 수 있습니다.</p>

      <form @submit.prevent="handleRegister">
        <div class="field">
          <label for="nickname">닉네임</label>
          <div class="check-row">
            <input
              id="nickname"
              type="text"
              v-model="nickname"
              placeholder="경제러너"
              autocomplete="nickname"
              required
              :class="{ 'input-ok': nicknameStatus === 'available', 'input-err': nicknameStatus === 'taken' || nicknameStatus === 'invalid' }"
            />
            <button
              type="button"
              class="check-btn"
              :disabled="!isNicknameValid || nicknameStatus === 'checking'"
              @click="checkNicknameDuplicate"
            >
              {{ nicknameStatus === 'checking' ? '확인 중' : '중복확인' }}
            </button>
          </div>
          <p v-if="nicknameStatus === 'available'" class="field-feedback ok">사용 가능한 닉네임입니다.</p>
          <p v-else-if="nicknameStatus === 'taken'" class="field-feedback err">이미 사용 중인 닉네임입니다.</p>
          <p v-else-if="nicknameStatus === 'invalid'" class="field-feedback err">닉네임은 2~20자의 한글, 영문, 숫자, 밑줄만 사용할 수 있습니다.</p>
          <p v-else-if="nicknameStatus === 'error'" class="field-feedback err">확인 중 오류가 발생했습니다.</p>
        </div>

        <div class="field">
          <label for="email">이메일</label>
          <div class="check-row">
            <input
              id="email"
              type="email"
              v-model="email"
              placeholder="name@example.com"
              autocomplete="email"
              required
              :class="{ 'input-ok': emailStatus === 'available', 'input-err': emailStatus === 'taken' || emailStatus === 'invalid' }"
            />
            <button
              type="button"
              class="check-btn"
              :disabled="!isEmailValid || emailStatus === 'checking'"
              @click="checkEmailDuplicate"
            >
              {{ emailStatus === 'checking' ? '확인 중' : '중복확인' }}
            </button>
          </div>
          <p v-if="emailStatus === 'available'" class="field-feedback ok">사용 가능한 이메일입니다.</p>
          <p v-else-if="emailStatus === 'taken'" class="field-feedback err">이미 사용 중인 이메일입니다.</p>
          <p v-else-if="emailStatus === 'invalid'" class="field-feedback err">올바른 이메일 형식으로 입력해 주세요.</p>
          <p v-else-if="emailStatus === 'error'" class="field-feedback err">확인 중 오류가 발생했습니다.</p>
        </div>

        <div class="field">
          <label for="password">비밀번호 <span class="field-hint">8자 이상</span></label>
          <input
            id="password"
            type="password"
            v-model="password"
            placeholder="8자 이상 입력"
            autocomplete="new-password"
            required
            :class="passwordInputClass"
          />
          <p v-if="isPasswordTouched && isPasswordLengthValid" class="field-feedback ok">사용 가능한 비밀번호입니다.</p>
          <p v-else-if="isPasswordTouched" class="field-feedback err">비밀번호는 8자 이상 72자 이하로 입력해 주세요.</p>
        </div>

        <div class="field">
          <label for="passwordConfirm">비밀번호 확인</label>
          <input
            id="passwordConfirm"
            type="password"
            v-model="passwordConfirm"
            placeholder="비밀번호 재입력"
            autocomplete="new-password"
            required
            :class="passwordConfirmInputClass"
          />
          <p v-if="isPasswordConfirmTouched && isPasswordMatch" class="field-feedback ok">비밀번호가 일치합니다.</p>
          <p v-else-if="isPasswordConfirmTouched" class="field-feedback err">비밀번호가 일치하지 않습니다.</p>
        </div>

        <div v-if="errorMsg" class="error-msg">
          {{ errorMsg }}
        </div>

        <button type="submit" class="btn-primary submit-btn" :disabled="!canSubmit">
          <span v-if="isLoading" class="spinner-sm"></span>
          <span>{{ isLoading ? '가입 중' : '회원가입' }}</span>
        </button>
      </form>

      <div class="social-login">
        <div class="social-divider"><span>소셜 계정으로 시작</span></div>
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
    </section>
  </div>
</template>

<style scoped>
.auth-wrap {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 430px);
  gap: 56px;
  max-width: 1180px;
  margin: 0 auto;
  align-items: center;
  padding: 42px 20px 80px;
  min-height: 72vh;
}

.brand-mini {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--ink, #0a0d12);
  margin-bottom: 34px;
}

.brand-mark {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: linear-gradient(135deg, #0a4a99 0%, #0084ff 100%);
}

.brand-name {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 20px;
  color: var(--ink, #0a0d12);
}

.dark .brand-name,
.dark .sc-title,
.dark .auth-title {
  color: #f4f6fa;
}

.sc-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 48px;
  line-height: 1.12;
  margin: 0 0 18px;
  color: var(--ink, #0a0d12);
}

.sc-sub {
  max-width: 520px;
  font-size: 15px;
  color: var(--ink-2, #4a5161);
  line-height: 1.65;
  margin: 0 0 32px;
}

.dark .sc-sub,
.dark .auth-desc,
.dark .field label,
.dark .sc-point span,
.dark .auth-foot {
  color: #a4adbf;
}

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

.point-index {
  width: 36px;
  height: 36px;
  flex: 0 0 36px;
  border: 1px solid rgba(0, 132, 255, 0.2);
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #0a4a99;
  font-size: 12px;
  font-weight: 800;
  background: rgba(232, 242, 255, 0.85);
}

.dark .point-index {
  background: rgba(0, 132, 255, 0.14);
  border-color: rgba(155, 203, 255, 0.28);
  color: #9BCBFF;
}

.sc-point b {
  font-family: 'Fustat', sans-serif;
  font-weight: 700;
  font-size: 14.5px;
  color: var(--ink, #0a0d12);
  display: block;
  margin-bottom: 3px;
}

.dark .sc-point b {
  color: #f4f6fa;
}

.sc-point span {
  font-size: 13px;
  color: var(--ink-2, #4a5161);
  line-height: 1.45;
}

.auth-card {
  padding: 34px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 16px;
  box-shadow: 0 24px 64px -34px rgba(20, 40, 80, 0.32);
}

.dark .auth-card {
  background: rgba(20, 24, 34, 0.78);
  border-color: rgba(255, 255, 255, 0.12);
  box-shadow: 0 24px 70px -36px rgba(0, 0, 0, 0.72);
}

.auth-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 26px;
  margin: 0 0 6px;
  color: var(--ink, #0a0d12);
}

.auth-desc {
  font-size: 14px;
  color: var(--ink-2, #4a5161);
  margin: 0 0 24px;
}

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
  font-weight: 700;
  color: var(--ink-2, #4a5161);
}

.field-hint {
  font-size: 12px;
  color: var(--ink-3, #8a93a3);
}

.field input {
  appearance: none;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.92);
  border: 1.5px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  font: inherit;
  font-size: 14px;
  color: var(--ink, #0a0d12);
  outline: none;
  transition: all .15s;
  width: 100%;
  min-width: 0;
}

.field input:focus {
  border-color: #0084ff;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(0, 132, 255, 0.12);
}

.field input::placeholder {
  color: var(--ink-3, #8a93a3);
}

.dark .field input {
  background: rgba(20, 24, 34, 0.82);
  border-color: rgba(255, 255, 255, 0.14);
  color: #f4f6fa;
}

.check-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 94px;
  gap: 8px;
  align-items: center;
}

.check-btn {
  height: 44px;
  border-radius: 10px;
  border: 1px solid rgba(0, 132, 255, 0.22);
  background: #fff;
  color: #006fd6;
  font: inherit;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
  transition: all .15s;
}

.check-btn:hover:not(:disabled) {
  border-color: #0084ff;
  box-shadow: 0 8px 18px -14px rgba(0, 132, 255, 0.55);
}

.check-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.dark .check-btn {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(155, 203, 255, 0.26);
  color: #9BCBFF;
}

.input-ok {
  border-color: #1a9e5c !important;
}

.input-ok:focus {
  box-shadow: 0 0 0 4px rgba(26, 158, 92, 0.12) !important;
}

.dark .input-ok {
  border-color: #3ad07b !important;
}

.input-err {
  border-color: #b02a2a !important;
}

.input-err:focus {
  box-shadow: 0 0 0 4px rgba(176, 42, 42, 0.12) !important;
}

.dark .input-err {
  border-color: #ff6a6a !important;
}

.field-feedback {
  font-size: 12px;
  margin: 4px 0 0;
}

.field-feedback.ok {
  color: #1a9e5c;
}

.field-feedback.err {
  color: #b02a2a;
}

.dark .field-feedback.ok {
  color: #3ad07b;
}

.dark .field-feedback.err {
  color: #ff8a8a;
}

.error-msg {
  padding: 11px 13px;
  background: rgba(255, 237, 237, 0.9);
  border: 1px solid rgba(176, 42, 42, 0.25);
  border-radius: 10px;
  font-size: 13.5px;
  color: #b02a2a;
}

.dark .error-msg {
  background: rgba(255, 106, 106, 0.13);
  border-color: rgba(255, 106, 106, 0.4);
  color: #ff8a8a;
}

.submit-btn {
  width: 100%;
  justify-content: center;
  padding: 12px 20px;
  font-size: 14.5px;
  border-radius: 10px;
  gap: 8px;
}

.submit-btn:disabled {
  opacity: 0.65;
  cursor: default;
}

.spinner-sm {
  width: 16px;
  height: 16px;
  border: 2.5px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .65s linear infinite;
  flex-shrink: 0;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
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
.social-divi