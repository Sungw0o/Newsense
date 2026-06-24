<script setup>
import AppHeader from './AppHeader.vue'
import AppFooter from './AppFooter.vue'
</script>

<template>
  <div class="min-h-screen flex flex-col w-full">
    <AppHeader />

    <main class="flex-1 w-full max-w-6xl mx-auto px-4 pb-12 pt-10 relative z-10">
      <router-view />
    </main>

    <AppFooter />
  </div>
</template>
