<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUserStore } from '../stores/useUserStore'
import BaseButton from '../components/common/BaseButton.vue'

const router = useRouter()
const userStore = useUserStore()
const { userInfo } = storeToRefs(userStore)

const nickname = ref('')
const email = ref('')
const subPlan = ref('Standard Plan (Free)')

const categories = ref([
  { id: 'FINANCE', name: '금융', selected: false },
  { id: 'REAL_ESTATE', name: '부동산', selected: false },
  { id: 'STOCK', name: '주식', selected: false },
  { id: 'EXCHANGE_RATE', name: '환율', selected: false },
  { id: 'ECONOMY', name: '거시경제', selected: false },
  { id: 'MONETARY_POLICY', name: '통화정책', selected: false }
])

const isSaving = ref(false)

// 스토어에서 사용자 정보 로드 시 입력값 초기화
const initFormData = () => {
  if (userInfo.value) {
    nickname.value = userInfo.value.nickname || ''
    email.value = userInfo.value.email || ''
    subPlan.value = userInfo.value.subPlan || 'Standard Plan (Free)'
    
    const interests = userInfo.value.interests || []
    categories.value.forEach(cat => {
      cat.selected = interests.includes(cat.id)
    })
  }
}

onMounted(async () => {
  try {
    await userStore.fetchUserProfile()
    initFormData()
  } catch (err) {
    console.error('Failed to load profile on mount')
  }
})

// userInfo가 변경될 때도 폼 데이터를 동기화해 줍니다.
watch(userInfo, () => {
  initFormData()
}, { deep: true })

const handleUpdateProfile = async () => {
  if (!nickname.value.trim()) {
    alert('닉네임을 입력해 주세요.')
    return
  }

  isSaving.value = true
  
  // 선택된 관심 카테고리 ID 배열 추출
  const selectedInterests = categories.value
    .filter(cat => cat.selected)
    .map(cat => cat.id)

  try {
    await userStore.updateUserProfile({
      nickname: nickname.value.trim(),
      interests: selectedInterests
    })
    alert('프로필 및 환경 설정이 저장되었습니다!')
  } catch (err) {
    alert('프로필 저장에 실패했습니다.')
  } finally {
    isSaving.value = false
  }
}

const isWithdrawing = ref(false)

const handleWithdraw = async () => {
  if (!confirm('정말로 회원 탈퇴하시겠습니까?\n탈퇴 후 계정은 비활성화되며 로그인이 불가합니다.')) return
  if (!confirm('다시 한번 확인합니다. 정말 탈퇴하시겠습니까?')) return

  isWithdrawing.value = true
  try {
    await userStore.deleteAccount()
    alert('회원 탈퇴가 완료되었습니다. 이용해 주셔서 감사합니다.')
    router.push('/login')
  } catch (err) {
    alert('회원 탈퇴 처리 중 오류가 발생했습니다. 다시 시도해 주세요.')
  } finally {
    isWithdrawing.value = false
  }
}
</script>

<template>
  <div class="max-w-4xl mx-auto px-4 py-8">
    <header class="mb-8">
      <h1 class="text-3xl font-extrabold text-slate-800 dark:text-white mb-2">마이페이지</h1>
      <p class="text-slate-500 dark:text-slate-400 font-light">내 계정 정보와 관심 경제 카테고리 등 학습 맞춤 환경을 커스텀해 보세요.</p>
    </header>

    <div class="grid md:grid-cols-3 gap-8">
      <!-- Left side: Profile brief -->
      <div class="md:col-span-1 glass-panel rounded-3xl p-6 text-center shadow-sm flex flex-col justify-between min-h-[300px]">
        <div>
          <!-- Avatar mockup -->
          <div class="w-20 h-20 bg-gradient-to-tr from-primary-500 to-secondary-500 rounded-full mx-auto flex items-center justify-center text-white text-3xl font-bold mb-4 shadow-md">
            {{ nickname.substring(0, 1) }}
          </div>
          <h2 class="text-lg font-bold text-slate-800 dark:text-slate-100">{{ nickname }}</h2>
          <p class="text-slate-400 dark:text-slate-500 text-xs mt-1 font-light">{{ email }}</p>
          
          <div class="mt-6 pt-6 border-t border-slate-100 dark:border-white/10">
            <span class="text-slate-400 dark:text-slate-500 text-xs font-semibold uppercase tracking-wider block mb-1">구독 플랜</span>
            <span class="text-sm font-bold text-primary-600 dark:text-primary-400 bg-primary-50 dark:bg-primary-950/40 px-3 py-1 rounded-full border border-primary-200/50 dark:border-primary-800/50 inline-block">
              {{ subPlan }}
            </span>
          </div>
        </div>

        <BaseButton
          variant="outline"
          :loading="isWithdrawing"
          @click="handleWithdraw"
          class="w-full py-2.5 rounded-xl font-bold border-red-200 dark:border-red-800/50 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/40 transition-colors"
        >
          회원 탈퇴
        </BaseButton>
      </div>

      <!-- Right side: Profile Form -->
      <div class="md:col-span-2 glass-panel rounded-3xl p-8 shadow-premium space-y-6">
        <h3 class="text-sm text-slate-400 dark:text-slate-500 font-semibold uppercase tracking-wider mb-2">
          ⚙️ PROFILE & PREFERENCES
        </h3>

        <!-- Nickname Edit -->
        <div>
          <label for="nickname" class="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">닉네임 변경</label>
          <input 
            id="nickname"
            type="text" 
            v-model="nickname"
            class="w-full glass-input text-sm"
          />
        </div>

        <!-- Interest Topics checkbox list -->
        <div>
          <label class="block text-sm font-bold text-slate-700 dark:text-slate-300 mb-2">관심 경제 카테고리</label>
          <p class="text-slate-400 dark:text-slate-500 text-xs mb-3 font-light">선택한 주제 위주로 추천 피드가 구성됩니다. (중복 선택 가능)</p>
          
          <div class="grid grid-cols-2 gap-3">
            <label 
              v-for="cat in categories" 
              :key="cat.id"
              class="border-2 rounded-xl p-3 flex items-center cursor-pointer select-none transition-all duration-200"
              :class="cat.selected 
                ? 'bg-primary-50 dark:bg-primary-950/40 border-primary-500 text-primary-700 dark:text-primary-400 font-semibold' 
                : 'bg-white/40 dark:bg-white/5 border-slate-200/50 dark:border-white/5 text-slate-500 dark:text-slate-400 hover:border-slate-300 dark:hover:border-white/10'"
            >
              <input 
                type="checkbox" 
                v-model="cat.selected"
                class="sr-only"
              />
              <span class="mr-2">{{ cat.selected ? '✓' : '○' }}</span>
              <span class="text-sm">{{ cat.name }}</span>
            </label>
          </div>
        </div>

        <!-- Save button -->
        <div class="border-t border-slate-100 dark:border-white/10 pt-6 flex justify-end">
          <BaseButton 
            variant="primary" 
            :loading="isSaving"
            @click="handleUpdateProfile"
            class="py-3 px-8 rounded-xl font-bold bg-primary-600 hover:bg-primary-700 text-white shadow-glass-glow hover:shadow-glass-glow-hover transition-all duration-300"
          >
            설정 저장하기
          </BaseButton>
        </div>
      </div>
    </div>
  </div>
</template>
