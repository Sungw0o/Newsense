<template>
  <slot v-if="isAuthenticated" />
</template>

<script setup>
import { useUserStore } from '../../stores/useUserStore'
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()
const isAuthenticated = computed(() => userStore.isAuthenticated)

// Optional: redirect to login if not authenticated (router guard already handles)
</script>
