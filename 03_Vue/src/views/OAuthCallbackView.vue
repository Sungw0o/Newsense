<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/useUserStore'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const errorMsg = ref('')

onMounted(async () => {
  try {
    const rawToken = route.query.accessToken
    const token = Array.isArray(rawToken) ? rawToken[0] : rawToken
    const needsOnboarding = route.query.needsOnboarding === 'true'
    await userStore.completeOAuthLogin(token)
    // 백엔드 needsOnboarding 신호 OR 프로필 로드 후 interests가 비어있으면 온보딩으로
    const interests = userStore.userInfo?.interests ?? []
    if (needsOnboarding || interests.length === 0) {
      router.replace('/onboarding')
    } else {
      router.replace('/')
    }
  } catch {
    errorMsg.value = '소셜 로그인 처리에 실패했습니다. 다시 시도해 주세요.'
  }
})
</script>

<template>
  <div class="oauth-callback">
    <div v-if="!errorMsg" class="spinner"></div>
    <p>{{ errorMsg || '소셜 로그인 처리 중입니다.' }}</p>
    <router-link v-if="errorMsg" to="/login" class="retry-link">로그인으로 돌아가기</router-link>
  </div>
</template>

<style scoped>
.oauth-callback {
  min-height: 50vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: var(--ink-2);
}
.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(0, 132, 255, 0.15);
  border-top-color: #0084ff;
  border-radius: 50%;
  animation: spin .7s linear infinite;
}
.retry-link {
  color: #0084ff;
  font-weight: 700;
  text-decoration: none;
}
@keyframes spin { to { transform: rotate(360deg); } }
</style>
