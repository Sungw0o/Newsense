<script setup>
import AppHeader from './AppHeader.vue'
</script>

<template>
  <div class="min-h-screen bg-slate-50 font-sans antialiased text-slate-800 flex flex-col">
    <!-- Navigation Header -->
    <AppHeader />

    <!-- Main Content Container -->
    <main class="flex-1 w-full max-w-6xl mx-auto py-6">
      <router-view />
    </main>

    <!-- Simple Footer -->
    <footer class="bg-white border-t border-slate-200/80 py-8 text-center text-xs text-slate-400 font-light mt-auto">
      <div class="max-w-6xl mx-auto px-4">
        &copy; 2026 Newsense. All rights reserved. 경제 문해력 근육 키우기 프로젝트.
      </div>
    </footer>
  </div>
</template>
