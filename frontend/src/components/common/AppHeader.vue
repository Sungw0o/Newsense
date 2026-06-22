<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUserStore } from '../../stores/useUserStore'

const router = useRouter()
const userStore = useUserStore()
const { isAuthenticated, userInfo } = storeToRefs(userStore)

const showDropdown = ref(false)
const showMobileMenu = ref(false)

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value
}

const toggleMobileMenu = () => {
  showMobileMenu.value = !showMobileMenu.value
}

const handleLogout = async () => {
  if (confirm('정말로 로그아웃 하시겠습니까?')) {
    await userStore.logout()
    showDropdown.value = false
    router.push('/login')
  }
}

// removed duplicate toggleDropdown

const nicknameFirstLetter = computed(() => {
  if (userInfo.value?.nickname) {
    return userInfo.value.nickname.substring(0, 1)
  }
  return 'U'
})
</script>

<template>
  <nav class="sticky top-0 z-50 glass-panel border-b border-slate-200/60 dark:border-white/5 bg-white/50 dark:bg-dark-950/50 backdrop-blur-md">
    <div class="max-w-6xl mx-auto px-4">
      <div class="flex items-center justify-between h-16">
        
        <!-- Logo -->
        <div class="flex items-center gap-8">
          <router-link to="/" class="text-xl font-extrabold bg-gradient-to-r from-primary-600 to-secondary-600 bg-clip-text text-transparent flex items-center gap-1.5">
            <span>🛸</span> Newsense
          </router-link>

          <!-- Nav Menu (Desktop) -->
          <div class="hidden md:flex items-center gap-1 text-sm font-semibold">
            <router-link 
              to="/" 
              class="px-4 py-2 text-slate-600 dark:text-slate-300 hover:text-primary-600 dark:hover:text-primary-400 hover:bg-slate-100/50 dark:hover:bg-white/5 rounded-xl transition-all duration-200"
              active-class="text-primary-600 dark:text-primary-400 bg-primary-50/50 dark:bg-primary-950/20"
            >
              뉴스 피드
            </router-link>
            <router-link 
              to="/history" 
              class="px-4 py-2 text-slate-600 dark:text-slate-300 hover:text-primary-600 dark:hover:text-primary-400 hover:bg-slate-100/50 dark:hover:bg-white/5 rounded-xl transition-all duration-200"
              active-class="text-primary-600 dark:text-primary-400 bg-primary-50/50 dark:bg-primary-950/20"
            >
              학습 이력
            </router-link>
            <router-link 
              to="/wrong-notes" 
              class="px-4 py-2 text-slate-600 dark:text-slate-300 hover:text-primary-600 dark:hover:text-primary-400 hover:bg-slate-100/50 dark:hover:bg-white/5 rounded-xl transition-all duration-200"
              active-class="text-primary-600 dark:text-primary-400 bg-primary-50/50 dark:bg-primary-950/20"
            >
              오답노트
            </router-link>
          </div>
        </div>

        <!-- Right: Auth controls -->
        <div class="flex items-center gap-4">
          <!-- Logged In profile dropdown -->
          <div v-if="isAuthenticated" class="relative">
            <button 
              @click="toggleDropdown"
              class="flex items-center gap-2 px-3 py-1.5 rounded-xl border border-slate-200 dark:border-white/5 bg-white dark:bg-white/5 hover:border-slate-300 dark:hover:border-white/10 hover:bg-slate-50 dark:hover:bg-white/10 text-slate-700 dark:text-slate-300 transition-all duration-200"
            >
              <span class="w-6 h-6 bg-gradient-to-tr from-primary-500 to-secondary-500 rounded-full flex items-center justify-center text-white text-xs font-bold">
                {{ nicknameFirstLetter }}
              </span>
              <span class="text-xs font-bold text-slate-700 dark:text-slate-300 hidden sm:inline">{{ userInfo?.nickname || '회원' }}</span>
              <span class="text-slate-400 dark:text-slate-500 text-xxs hidden sm:inline">&darr;</span>
            </button>

            <!-- Dropdown menu -->
            <div 
              v-if="showDropdown"
              class="absolute right-0 mt-2 w-48 glass-panel rounded-2xl shadow-lg py-2 z-50 animate-[slideDown_0.2s_ease-out]"
            >
              <div class="px-4 py-2 border-b border-slate-100 dark:border-white/10 mb-1">
                <p class="text-xs text-slate-400 dark:text-slate-500 font-light">로그인 계정</p>
                <p class="text-sm font-bold text-slate-700 dark:text-slate-300 truncate">{{ userInfo?.email }}</p>
              </div>
              <router-link 
                to="/mypage" 
                @click="showDropdown = false"
                class="block px-4 py-2 text-sm text-slate-600 dark:text-slate-300 hover:bg-slate-100/50 dark:hover:bg-white/5 hover:text-primary-600 dark:hover:text-primary-400 font-medium"
              >
                마이페이지 / 설정
              </router-link>
              <button 
                @click="handleLogout"
                class="w-full text-left px-4 py-2 text-sm text-accent-600 dark:text-accent-400 hover:bg-slate-100/50 dark:hover:bg-white/5 font-medium"
              >
                로그아웃
              </button>
            </div>
          </div>

          <!-- Logged Out login/signup buttons -->
          <div v-else class="flex items-center gap-2">
            <router-link 
              to="/login" 
              class="px-4 py-2 text-sm font-semibold text-slate-600 dark:text-slate-300 hover:text-primary-600 dark:hover:text-primary-400 transition-colors duration-200"
            >
              로그인
            </router-link>
            <router-link 
              to="/register" 
              class="px-4 py-2 text-sm font-bold text-white bg-primary-600 hover:bg-primary-700 rounded-xl shadow-md shadow-primary-100 transition-all duration-200"
            >
              회원가입
            </router-link>
          </div>

          <!-- Mobile Menu Toggle Button -->
          <button 
            @click="toggleMobileMenu" 
            class="md:hidden p-2 rounded-xl text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100/50 dark:hover:bg-white/5 transition-colors duration-200"
            aria-label="Toggle mobile menu"
          >
            <span class="text-xl">{{ showMobileMenu ? '✕' : '☰' }}</span>
          </button>
        </div>

      </div>

      <!-- Mobile Nav Menu (Collapsible) -->
      <div 
        v-if="showMobileMenu" 
        class="md:hidden py-4 border-t border-slate-100 dark:border-white/10 animate-[slideDown_0.2s_ease-out] flex flex-col gap-1 text-sm font-semibold"
      >
        <router-link 
          to="/" 
          @click="showMobileMenu = false"
          class="px-4 py-2 text-slate-600 dark:text-slate-300 hover:text-primary-600 dark:hover:text-primary-400 hover:bg-slate-100/50 dark:hover:bg-white/5 rounded-xl transition-all duration-200"
          active-class="text-primary-600 dark:text-primary-400 bg-primary-50/50 dark:bg-primary-950/20"
        >
          뉴스 피드
        </router-link>
        <router-link 
          to="/history" 
          @click="showMobileMenu = false"
          class="px-4 py-2 text-slate-600 dark:text-slate-300 hover:text-primary-600 dark:hover:text-primary-400 hover:bg-slate-100/50 dark:hover:bg-white/5 rounded-xl transition-all duration-200"
          active-class="text-primary-600 dark:text-primary-400 bg-primary-50/50 dark:bg-primary-950/20"
        >
          학습 이력
        </router-link>
        <router-link 
          to="/wrong-notes" 
          @click="showMobileMenu = false"
          class="px-4 py-2 text-slate-600 dark:text-slate-300 hover:text-primary-600 dark:hover:text-primary-400 hover:bg-slate-100/50 dark:hover:bg-white/5 rounded-xl transition-all duration-200"
          active-class="text-primary-600 dark:text-primary-400 bg-primary-50/50 dark:bg-primary-950/20"
        >
          오답노트
        </router-link>
      </div>
    </div>
  </nav>
</template>

<style scoped>
@keyframes slideDown {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
