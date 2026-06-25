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

const nicknameFirstLetter = computed(() => userInfo.value?.nickname?.substring(0, 1) ?? 'U')

const isActive = (path) => route.path === path

const closeMenus = () => {
  showDropdown.value = false
  showStartMenu.value = false
  showMobileMenu.value = false
}

const handleLogout = async () => {
  if (confirm('정말 로그아웃 하시겠습니까?')) {
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
      <router-link to="/" class="brand" @click="closeMenus">
        <span class="brand-mark"></span>
        Newsense
      </router-link>

      <div class="nav-links">
        <router-link to="/" :class="{ active: isActive('/') }">뉴스 피드</router-link>
        <router-link to="/community" :class="{ active: route.path.startsWith('/community') }">커뮤니티</router-link>
        <router-link to="/history" :class="{ active: isActive('/history') }">학습 기록</router-link>
      </div>

      <div v-if="isAuthenticated" class="relative desktop-auth">
        <button class="nav-cta" @click="showDropdown = !showDropdown">
          <span class="avatar">{{ nicknameFirstLetter }}</span>
          <span class="avatar-name">{{ userInfo?.nickname || '회원' }}</span>
          <span class="arrow">▾</span>
        </button>

        <div v-if="showDropdown" class="nav-drop" @click.stop>
          <div class="drop-header">
            <p class="drop-sub">로그인 계정</p>
            <p class="drop-email">{{ userInfo?.email }}</p>
          </div>
          <router-link to="/mypage" @click="showDropdown = false" class="drop-item">마이페이지</router-link>
          <router-link to="/inquiry" @click="showDropdown = false" class="drop-item">문의하기</router-link>
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

      <button class="hamburger" @click="showMobileMenu = !showMobileMenu" aria-label="메뉴">
        <span class="ham-line" :class="{ open: showMobileMenu }"></span>
        <span class="ham-line" :class="{ open: showMobileMenu }"></span>
        <span class="ham-line" :class="{ open: showMobileMenu }"></span>
      </button>
    </nav>

    <div v-if="showMobileMenu" class="mobile-menu glass-panel" @click.stop>
      <button class="mobile-link" @click="handleMobileNav('/')">뉴스 피드</button>
      <button class="mobile-link" @click="handleMobileNav('/community')">커뮤니티</button>
      <button class="mobile-link" @click="handleMobileNav('/history')">학습 기록</button>
      <div class="mobile-divider"></div>
      <template v-if="isAuthenticated">
        <button class="mobile-link" @click="handleMobileNav('/mypage')">마이페이지</button>
        <button class="mobile-link" @click="handleMobileNav('/inquiry')">문의하기</button>
        <button v-if="userInfo?.role === 'ADMIN'" class="mobile-link" @click="handleMobileNav('/admin')">관리자 페이지</button>
        <button class="mobile-link mobile-logout" @click="handleLogout">로그아웃</button>
      </template>
      <template v-else>
        <button class="mobile-link" @click="handleMobileNav('/login')">로그인</button>
        <button class="mobile-link" @click="handleMobileNav('/register')">회원가입</button>
      </template>
    </div>
  </div>

  <div v-if="showDropdown || showStartMenu || showMobileMenu" class="fixed inset-0 z-40" @click="closeMenus" />
</template>

<style scoped>
.nav-wrap {
  position: sticky;
  top: 18px;
  z-index: 50;
  display: flex;
  flex-direction: column;
  align-items: center;
  pointer-events: none;
  margin-top: 18px;
  padding: 0 16px;
  gap: 8px;
}

.nav {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 9px 12px 9px 18px;
  width: fit-content;
  max-width: calc(100vw - 32px);
  border-radius: 14px;
}

.brand {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 19px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--ink);
  text-decoration: none;
  white-space: nowrap;
}

.brand-mark {
  width: 22px;
  height: 22px;
  border-radius: 7px;
  background: linear-gradient(135deg, #0a4a99 0%, #0084ff 100%);
  flex-shrink: 0;
}

.nav-links {
  display: flex;
  gap: 22px;
  font-size: 13.5px;
  font-weight: 700;
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
}

.desktop-auth {
  position: relative;
}

.nav-cta,
.nav-start {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  white-space: nowrap;
}

.nav-cta {
  padding: 7px 10px 7px 12px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 10px;
  font-weight: 700;
  font-size: 13px;
  color: var(--ink);
  cursor: pointer;
}

.dark .nav-cta {
  background: rgba(20, 24, 34, 0.65);
  border-color: rgba(255, 255, 255, 0.12);
  color: #d6dceb;
}

.avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: #0084ff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 800;
  flex-shrink: 0;
}

.avatar-name {
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.arrow {
  font-size: 11px;
  opacity: 0.7;
}

.nav-drop {
  position: absolute;
  right: 0;
  top: calc(100% + 10px);
  min-width: 220px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  box-shadow: 0 24px 54px -32px rgba(20, 40, 80, 0.48);
  padding: 8px;
  z-index: 70;
}

.dark .nav-drop {
  background: rgba(20, 24, 34, 0.96);
  border-color: rgba(255, 255, 255, 0.12);
}

.drop-header {
  padding: 10px 12px 12px;
  border-bottom: 1px solid var(--line);
  margin-bottom: 6px;
}

.drop-sub {
  margin: 0 0 2px;
  font-size: 12px;
  color: var(--ink-3);
}

.drop-email {
  margin: 0;
  font-size: 13px;
  color: var(--ink);
  word-break: break-all;
}

.drop-item {
  display: flex;
  width: 100%;
  align-items: center;
  padding: 10px 12px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--ink);
  text-decoration: none;
  font: inherit;
  font-size: 13.5px;
  font-weight: 700;
  cursor: pointer;
  text-align: left;
}

.drop-item:hover {
  background: var(--brand-soft);
  color: #006fd6;
}

.drop-logout {
  color: #b02a2a;
}

.hamburger {
  display: none;
  width: 38px;
  height: 36px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  gap: 4px;
}

.dark .hamburger {
  background: rgba(20, 24, 34, 0.65);
}

.ham-line {
  width: 16px;
  height: 2px;
  background: var(--ink);
  border-radius: 2px;
}

.mobile-menu {
  pointer-events: auto;
  width: min(360px, calc(100vw - 32px));
  padding: 8px;
  border-radius: 14px;
  z-index: 70;
}

.mobile-link {
  width: 100%;
  padding: 12px;
  border: 0;
  border-radius: 9px;
  background: transparent;
  color: var(--ink);
  font: inherit;
  font-weight: 800;
  text-align: left;
  cursor: pointer;
}

.mobile-link:hover {
  background: var(--brand-soft);
  color: #006fd6;
}

.mobile-divider {
  height: 1px;
  background: var(--line);
  margin: 6px 4px;
}

.mobile-logout {
  color: #b02a2a;
}

@media (max-width: 900px) {
  .nav {
    width: min(520px, calc(100vw - 32px));
    justify-content: space-between;
  }

  .nav-links,
  .desktop-auth {
    display: none;
  }

  .hamburger {
    display: flex;
  }
}
</style>
