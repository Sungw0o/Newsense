<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/useUserStore'
import BaseButton from '../components/common/BaseButton.vue'
import ErrorMessage from '../components/common/ErrorMessage.vue'

const router = useRouter()
const userStore = useUserStore()

const email = ref('')
const nickname = ref('')
const password = ref('')
const passwordConfirm = ref('')
const isLoading = ref(false)
const errorMsg = ref('')

const handleRegister = async () => {
  if (!email.value || !nickname.value || !password.value || !passwordConfirm.value) {
    errorMsg.value = '모든 필드를 입력해 주세요.'
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
    await userStore.register({
      email: email.value,
      nickname: nickname.value,
      password: password.value
    })
    
    // Redirect to login upon successful registration
    alert('회원가입이 완료되었습니다! 로그인 해 주세요.')
    router.push('/login')
  } catch (err) {
    errorMsg.value = err.response?.data?.message || '회원가입 중 오류가 발생했습니다. 다시 시도해 주세요.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-[80vh] flex items-center justify-center px-4 py-12">
    <div class="w-full max-w-md bg-white rounded-3xl border border-slate-200/80 p-8 shadow-premium relative overflow-hidden">
      <!-- Background decoration -->
      <div class="absolute -top-12 -right-12 w-32 h-32 bg-primary-100 rounded-full blur-2xl opacity-60"></div>
      <div class="absolute -bottom-12 -left-12 w-32 h-32 bg-secondary-100 rounded-full blur-2xl opacity-60"></div>

      <div class="relative z-10">
        <!-- Brand Title -->
        <div class="text-center mb-8">
          <h2 class="text-3xl font-extrabold bg-gradient-to-r from-primary-600 to-secondary-600 bg-clip-text text-transparent">
            Newsense
          </h2>
          <p class="text-slate-400 text-sm mt-2">새로운 경제 학습의 시작</p>
        </div>

        <form @submit.prevent="handleRegister" class="space-y-5">
          <!-- Nickname Input -->
          <div>
            <label for="nickname" class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">닉네임</label>
            <input 
              id="nickname" 
              type="text" 
              v-model="nickname" 
              placeholder="뉴스엔서"
              required
              class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-100 outline-none transition-all duration-300 placeholder:text-slate-300 text-slate-800 text-sm"
            />
          </div>

          <!-- Email Input -->
          <div>
            <label for="email" class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">이메일 주소</label>
            <input 
              id="email" 
              type="email" 
              v-model="email" 
              placeholder="name@example.com"
              required
              class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-100 outline-none transition-all duration-300 placeholder:text-slate-300 text-slate-800 text-sm"
            />
          </div>

          <!-- Password Input -->
          <div>
            <label for="password" class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">비밀번호 (8자 이상)</label>
            <input 
              id="password" 
              type="password" 
              v-model="password" 
              placeholder="••••••••"
              required
              class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-100 outline-none transition-all duration-300 placeholder:text-slate-300 text-slate-800 text-sm"
            />
          </div>

          <!-- Password Confirm Input -->
          <div>
            <label for="passwordConfirm" class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">비밀번호 확인</label>
            <input 
              id="passwordConfirm" 
              type="password" 
              v-model="passwordConfirm" 
              placeholder="••••••••"
              required
              class="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-primary-500 focus:ring-2 focus:ring-primary-100 outline-none transition-all duration-300 placeholder:text-slate-300 text-slate-800 text-sm"
            />
          </div>

          <!-- Error Alert -->
          <ErrorMessage v-if="errorMsg" :message="errorMsg" />

          <!-- Submit Button -->
          <BaseButton 
            type="submit" 
            :loading="isLoading" 
            class="w-full py-3.5 rounded-xl font-bold transition-all duration-300 shadow-md hover:shadow-lg"
          >
            회원가입
          </BaseButton>
        </form>

        <!-- Navigation link to Login -->
        <div class="text-center mt-6 pt-6 border-t border-slate-100 text-sm text-slate-500">
          이미 계정이 있으신가요? 
          <router-link to="/login" class="text-primary-600 hover:text-primary-700 font-bold ml-1 transition-colors duration-200">
            로그인 하기
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>
