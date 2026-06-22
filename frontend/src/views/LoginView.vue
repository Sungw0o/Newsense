<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/useUserStore'
import BaseButton from '../components/common/BaseButton.vue'
import ErrorMessage from '../components/common/ErrorMessage.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const email = ref('')
const password = ref('')
const isLoading = ref(false)
const errorMsg = ref('')

const handleLogin = async () => {
  if (!email.value || !password.value) {
    errorMsg.value = '이메일과 비밀번호를 입력해주세요.'
    return
  }

  errorMsg.value = ''
  isLoading.value = true

  try {
    await userStore.login({
      email: email.value,
      password: password.value
    })
    
    // Redirect to the originally requested route, or home '/' after validating it's a safe internal relative path
    let redirectPath = '/'
    const rawRedirect = route.query.redirect
    const target = Array.isArray(rawRedirect) ? rawRedirect[0] : rawRedirect

    if (typeof target === 'string' && target.startsWith('/') && !target.startsWith('//') && !target.startsWith('/\\')) {
      redirectPath = target
    }

    router.push(redirectPath)
  } catch (err) {
    errorMsg.value = err.response?.data?.message || '로그인 중 오류가 발생했습니다. 다시 시도해 주세요.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-[80vh] flex items-center justify-center px-4 py-12">
    <div class="w-full max-w-md bg-white rounded-3xl border border-slate-200/80 p-8 shadow-premium relative overflow-hidden">
      <!-- Background accent gradient decoration -->
      <div class="absolute -top-12 -right-12 w-32 h-32 bg-primary-100 rounded-full blur-2xl opacity-60"></div>
      <div class="absolute -bottom-12 -left-12 w-32 h-32 bg-secondary-100 rounded-full blur-2xl opacity-60"></div>

      <div class="relative z-10">
        <!-- Brand Title -->
        <div class="text-center mb-8">
          <h2 class="text-3xl font-extrabold bg-gradient-to-r from-primary-600 to-secondary-600 bg-clip-text text-transparent">
            Newsense
          </h2>
          <p class="text-slate-400 text-sm mt-2">지혜로운 경제 공부의 첫걸음</p>
        </div>

        <form @submit.prevent="handleLogin" class="space-y-6">
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
            <label for="password" class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-2">비밀번호</label>
            <input 
              id="password" 
              type="password" 
              v-model="password" 
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
            로그인
          </BaseButton>
        </form>

        <!-- Navigation link to Register -->
        <div class="text-center mt-8 pt-6 border-t border-slate-100 text-sm text-slate-500">
          아직 회원이 아니신가요? 
          <router-link to="/register" class="text-primary-600 hover:text-primary-700 font-bold ml-1 transition-colors duration-200">
            회원가입 하기
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>
