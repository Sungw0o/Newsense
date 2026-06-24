<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const currentYear = new Date().getFullYear()
</script>

<template>
  <footer class="footer">
    <div class="footer-inner">
      <!-- Brand -->
      <div class="footer-brand">
        <button class="brand-logo" @click="router.push('/')">
          <span class="brand-mark"></span>
          Newsense
        </button>
        <p class="brand-desc">경제 뉴스로 키우는 금융 문해력</p>
      </div>

      <!-- Nav links -->
      <nav class="footer-nav">
        <div class="footer-nav-group">
          <p class="nav-group-title">서비스</p>
          <router-link to="/" class="nav-link">뉴스 피드</router-link>
          <router-link to="/community" class="nav-link">커뮤니티</router-link>
          <router-link to="/history" class="nav-link">학습 이력</router-link>
        </div>
        <div class="footer-nav-group">
          <p class="nav-group-title">계정</p>
          <router-link to="/mypage" class="nav-link">마이페이지</router-link>
          <router-link to="/login" class="nav-link">로그인</router-link>
          <router-link to="/register" class="nav-link">회원가입</router-link>
        </div>
      </nav>
    </div>

    <div class="footer-bottom">
      <p class="copyright">© {{ currentYear }} Newsense. 삼성 청년 SW 아카데미 관통 프로젝트.</p>
      <div class="footer-legal">
        <a href="#" class="legal-link">고객센터</a>
        <span class="legal-sep">|</span>
        <a href="#" class="legal-link">개인정보처리방침</a>
        <span class="legal-sep">|</span>
        <a href="#" class="legal-link">이용자권리 및 유의사항</a>
        <span class="legal-sep">|</span>
        <a href="#" class="legal-link">금융소비자포털</a>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  width: 100%;
  border-top: 1px solid var(--line);
  background: var(--bg-soft);
  padding: 40px 24px 20px;
  margin-top: auto;
}

.footer-inner {
  max-width: 1152px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 40px;
  flex-wrap: wrap;
}

.footer-brand {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.brand-logo {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  cursor: pointer;
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 18px;
  letter-spacing: -0.5px;
  color: var(--ink);
  padding: 0;
  transition: opacity 0.15s;
}
.brand-logo:hover { opacity: 0.75; }

.brand-mark {
  display: inline-block;
  width: 20px;
  height: 20px;
  border-radius: 6px;
  background: linear-gradient(135deg, #60B1FF 0%, #0084ff 100%);
  flex-shrink: 0;
}

.brand-desc {
  font-size: 13px;
  color: var(--ink-3);
  margin: 0;
}

.footer-nav {
  display: flex;
  gap: 48px;
  flex-wrap: wrap;
}

.footer-nav-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 80px;
}

.nav-group-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--ink-3);
  text-transform: uppercase;
  letter-spacing: 0.8px;
  margin: 0 0 2px;
  font-family: 'Nanum Gothic', monospace;
}

.nav-link {
  font-size: 13.5px;
  color: var(--ink-2);
  text-decoration: none;
  transition: color 0.15s;
}
.nav-link:hover { color: #0084ff; }

.footer-bottom {
  max-width: 1152px;
  margin: 24px auto 0;
  padding-top: 16px;
  border-top: 1px solid var(--line);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.copyright {
  font-size: 12px;
  color: var(--ink-3);
  margin: 0;
}

.footer-legal {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.legal-link {
  font-size: 12px;
  color: var(--ink-3);
  text-decoration: none;
  transition: color 0.15s;
  white-space: nowrap;
}
.legal-link:hover { color: #0084ff; }

.legal-sep {
  font-size: 11px;
  color: var(--line);
}

@media (max-width: 640px) {
  .footer-inner { flex-direction: column; gap: 28px; }
  .footer-nav { gap: 28px; }
  .footer-bottom { flex-direction: column; align-items: flex-start; gap: 10px; }
  .footer-legal { gap: 4px; }
}
</style>
