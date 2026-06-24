<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUserStore } from '../../stores/useUserStore'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const { isAuthenticated, userInfo } = storeToRefs(userStore)

const showDropdown = ref(false)
const showStartMenu = ref(false)
const showMobileMenu = ref(false)

const nicknameFirstLetter = computed(() => {
  return userInfo.value?.nickname?.substring(0, 1) ?? 'U'
})

const isActive = (path) => route.path === path

const closeMenus = () => {
  showDropdown.value = false
  showStartMenu.value = false
  showMobileMenu.value = false
}

const handleLogout = async () => {
  if (confirm('정말로 로그아웃 하시겠습니까?')) {
    await userStore.logout()
    closeMenus()
    router.push('/login')
  }
}

const handleMobileNav = (path) => {
  closeMenus()
  router.push(path)
}
</script>

<template>
  <div class="nav-wrap">
    <nav class="nav glass-panel">
      <!-- Brand -->
      <router-link to="/" class="brand" @click="closeMenus">
        <span class="brand-mark"></span>
        Newsense
      </router-link>

      <!-- Nav links (desktop) -->
      <div class="nav-links">
        <router-link to="/" :class="{ active: isActive('/') }">뉴스 피드</router-link>
        <router-link to="/community" :class="{ active: route.path.startsWith('/community') }">커뮤니티</router-link>
        <router-link to="/history" :class="{ active: isActive('/history') }">학습 이력</router-link>
      </div>

      <!-- Auth area (desktop) -->
      <div v-if="isAuthenticated" class="relative desktop-auth">
        <button class="nav-cta" @click="showDropdown = !showDropdown">
          <span class="avatar">{{ nicknameFirstLetter }}</span>
          <span class="avatar-name">{{ userInfo?.nickname || '회원' }}</span>
          <span class="arrow">↓</span>
        </button>

        <div v-if="showDropdown" class="nav-drop" @click.stop>
          <div class="drop-header">
            <p class="drop-sub">로그인 계정</p>
            <p class="drop-email">{{ userInfo?.email }}</p>
          </div>
          <router-link to="/mypage" @click="showDropdown = false" class="drop-item">마이페이지</router-link>
          <router-link v-if="userInfo?.role === 'ADMIN'" to="/admin" @click="showDropdown = false" class="drop-item drop-admin">관리자 페이지</router-link>
          <button @click="handleLogout" class="drop-item drop-logout">로그아웃</button>
        </div>
      </div>

      <div v-else class="relative desktop-auth">
        <button class="btn-primary nav-start" @click="showStartMenu = !showStartMenu">
          시작하기 <span class="arrow">▾</span>
        </button>

        <div v-if="showStartMenu" class="nav-drop" @click.stop>
          <div class="drop-header">
            <p class="drop-sub">Newsense 시작</p>
            <p class="drop-email">계정으로 학습 기록을 이어가세요</p>
          </div>
          <router-link to="/login" @click="showStartMenu = false" class="drop-item">로그인</router-link>
          <router-link to="/register" @click="showStartMenu = false" class="drop-item">회원가입</router-link>
        </div>
      </div>

      <!-- Hamburger (mobile) -->
      <button class="hamburger" @click="showMobileMenu = !showMobileMenu" aria-label="메뉴">
        <span class="ham-line" :class="{ open: showMobileMenu }"></span>
        <span class="ham-line" :class="{ open: showMobileMenu }"></span>
        <span class="ham-line" :class="{ open: showMobileMenu }"></span>
      </button>
    </nav>

    <!-- Mobile menu -->
    <div v-if="showMobileMenu" class="mobile-menu glass-panel" @click.stop>
      <button class="mobile-link" @click="handleMobileNav('/')">뉴스 피드</button>
      <button class="mobile-link" @click="handleMobileNav('/community')">커뮤니티</button>
      <button class="mobile-link" @click="handleMobileNav('/history')">학습 이력</button>
      <div class="mobile-divider"></div>
      <template v-if="isAuthenticated">
        <button class="mobile-link" @click="handleMobileNav('/mypage')">마이페이지</button>
        <button v-if="userInfo?.role === 'ADMIN'" class="mobile-link" @click="handleMobileNav('/admin')">관리자 페이지</button>
        <button class="mobile-link mobile-logout" @click="handleLogout">로그아웃</button>
      </template>
      <template v-else>
        <button class="mobile-link" @click="handleMobileNav('/login')">로그인</button>
        <button class="mobile-link" @click="handleMobileNav('/register')">회원가입</button>
      </template>
    </div>
  </div>

  <!-- Backdrop to close dropdowns -->
  <div v-if="showDropdown || showStartMenu || showMobileMenu" class="fixed inset-0 z-40" @click="closeMenus" />
</template>

<style scoped>
.nav-wrap {
  position: sticky;
  top: 20px;
  z-index: 50;
  display: flex;
  flex-direction: column;
  align-items: center;
  pointer-events: none;
  margin-top: 20px;
  padding: 0 16px;
  gap: 8px;
}

.nav {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 9px 12px 9px 20px;
  width: fit-content;
  max-width: calc(100vw - 32px);
  border-radius: 16px;
}

.brand {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 19px;
  letter-spacing: -0.5px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--ink);
  text-decoration: none;
  white-space: nowrap;
}

.nav-links {
  display: flex;
  gap: 22px;
  font-size: 13.5px;
  font-weight: 500;
}

.nav-links a {
  color: var(--ink);
  text-decoration: none;
  opacity: 0.75;
  transition: opacity .15s, color .15s;
  white-space: nowrap;
}

.nav-links a:hover,
.nav-links a.active {
  color: #0084ff;
  opacity: 1;
  font-weight: 700;
}

.nav-cta {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 10px 7px 12px;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 10px;
  font-weight: 600;
  font-size: 13px;
  color: var(--ink);
  cursor: pointer;
  box-shadow: inset 0 2px 3px 0 rgba(255, 255, 255, 0.5);
  transition: background .15s;
  white-space: nowrap;
}
.nav-cta:hover { background: rgba(255,255,255,0.75); }

.dark .nav-cta {
  background: rgba(20, 24, 34, 0.55);
  border-color: rgba(255, 255, 255, 0.12);
  color: #d6dceb;
  box-shadow: inset 0 1px 0 0 rgba(255, 255, 255, 0.08);
}

.avatar {
  width: 24px; height: 24px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #9CCBFF 0%, #0084ff 60%, #0a4a99 100%);
  color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 700;
  flex-shrink: 0;
}

.avatar-name {
  max-width: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.arrow { font-size: 10px; opacity: 0.5; }

.nav-start {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 13px;
  border-radius: 10px;
  white-space: nowrap;
}

/* Hamburger */
.hamburger {
  display: none;
  flex-direction: column;
  gap: 4px;
  padding: 8px;
  background: none;
  border: none;
  cursor: pointer;
  margin-left: auto;
}
.ham-line {
  display: block;
  width: 20px; height: 2px;
  border-radius: 2px;
  background: var(--ink);
  transition: transform .2s, opacity .2s;
}
.ham-line.open:nth-child(1) { transform: translateY(6px) rotate(45deg); }
.ham-line.open:nth-child(2) { opacity: 0; }
.ham-line.open:nth-child(3) { transform: translateY(-6px) rotate(-45deg); }
.dark .ham-line { background: #f4f6fa; }

/* Mobile menu */
.mobile-menu {
  pointer-events: auto;
  display: flex;
  flex-direction: column;
  width: calc(100vw - 32px);
  max-width: 360px;
  border-radius: 16px;
  overflow: hidden;
  animation: dropIn .15s ease-out;
}

.mobile-link {
  display: block;
  width: 100%;
  padding: 13px 20px;
  background: none;
  border: none;
  text-align: left;
  font-size: 14px;
  font-weight: 500;
  color: var(--ink);
  cursor: pointer;
  transition: background .12s, color .12s;
}
.mobile-link:hover { background: rgba(0,132,255,0.07); color: #0084ff; }
.dark .mobile-link { color: #f4f6fa; }
.dark .mobile-link:hover { background: rgba(0,132,255,0.14); color: #4FB3FF; }

.mobile-logout { color: #b02a2a; }
.dark .mobile-logout { color: #ff8a8a; }
.mobile-logout:hover { background: rgba(176,42,42,0.08) !important; color: #b02a2a !important; }

.mobile-divider {
  height: 1px;
  background: rgba(0,0,0,0.08);
  margin: 4px 0;
}
.dark .mobile-divider { background: rgba(255,255,255,0.10); }

/* Dropdown */
.nav-drop {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  min-width: 180px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.90);
  backdrop-filter: blur(40px) saturate(180%);
  -webkit-backdrop-filter: blur(40px) saturate(180%);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 14px;
  box-shadow: 0 18px 42px -22px rgba(20, 40, 80, 0.28);
  z-index: 60;
  animation: dropIn .15s ease-out;
}

.dark .nav-drop {
  background: rgba(20, 24, 34, 0.92);
  border-color: rgba(255, 255, 255, 0.12);
}

.drop-header {
  padding: 8px 10px 10px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.07);
  margin-bottom: 4px;
}
.dark .drop-header { border-color: rgba(255, 255, 255, 0.10); }

.drop-sub {
  font-size: 11px;
  color: var(--ink-3);
  margin: 0 0 2px;
  font-family: 'Nanum Gothic', monospace;
  letter-spacing: 0.5px;
  text-transform: uppercase;
}

.drop-email {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--ink);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.drop-item {
  display: block;
  width: 100%;
  padding: 9px 10px;
  border-radius: 9px;
  color: var(--ink);
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  transition: background .12s;
}
.drop-item:hover {
  background: rgba(0, 132, 255, 0.08);
  color: #0084ff;
}

.dark .drop-item { color: #f4f6fa; }
.dark .drop-item:hover { background: rgba(0, 132, 255, 0.16); color: #4FB3FF; }

.drop-admin { color: #6366f1; }
.dark .drop-admin { color: #a5b4fc; }
.drop-admin:hover { background: rgba(99, 102, 241, 0.08) !important; color: #6366f1 !important; }
.drop-logout { color: #b02a2a; }
.dark .drop-logout { color: #ff8a8a; }
.drop-logout:hover { background: rgba(176, 42, 42, 0.08) !important; color: #b02a2a !important; }

@keyframes dropIn {
  from { opacity: 0; transform: translateY(-6px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* Responsive */
@media (max-width: 640px) {
  .nav { gap: 8px; padding: 8px 8px 8px 14px; }
  .nav-links { display: none; }
  .desktop-auth { display: none; }
  .hamburger { display: flex; }
  .brand { font-size: 17px; }
}
</style>
