<script setup>
import AppHeader from './AppHeader.vue'
</script>

<template>
  <div class="min-h-screen flex flex-col w-full">
    <AppHeader />

    <main class="flex-1 w-full max-w-6xl mx-auto px-4 pb-20 pt-10 relative z-10">
      <router-view />
    </main>
  </div>
</template>
