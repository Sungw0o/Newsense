<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/useUserStore'
import { storeToRefs } from 'pinia'

const props = defineProps({
  /** true이면 미인증 시 /login으로 리다이렉트 */
  requiresAuth: { type: Boolean, default: true },
  /** 리다이렉트 대상 경로 (미설정 시 /login) */
  redirectTo: { type: String, default: '/login' }
})

const router = useRouter()
const userStore = useUserStore()
const { isAuthenticated } = storeToRefs(userStore)

const canRender = computed(() => {
  if (props.requiresAuth && !isAuthenticated.value) {
    router.replace(props.redirectTo)
    return false
  }
  return true
})
</script>

<template>
  <slot v-if="canRender" />
</template>
