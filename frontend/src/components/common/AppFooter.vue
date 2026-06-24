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

    <div class="footer-bottom">
      <p class="copyright">© {{ currentYear }} Newsense. 삼성 청년 SW 아카데미 관통 프로젝트.</p>
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
  .footer-bottom { flex-direction: column; align-items: flex-start; gap: 10px; }
  .footer-legal { gap: 4px; }
}
</style>
