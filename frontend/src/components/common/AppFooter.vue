<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <footer class="footer">
    <div class="footer-inner">
      <button class="brand-logo" @click="router.push('/')">
        <span class="brand-mark"></span>
        Newsense
      </button>

      <div class="footer-line">
        <span class="copyright">© 2026 Newsense. 삼성 청년 SW 아카데미 관통 프로젝트.</span>
        <a href="https://new5ense.site/articles/13#" class="legal-link">고객센터</a>
        <span class="legal-sep">|</span>
        <a href="https://new5ense.site/articles/13#" class="legal-link">개인정보처리방침</a>
        <span class="legal-sep">|</span>
        <a href="https://new5ense.site/articles/13#" class="legal-link">이용자권리 및 유의사항</a>
        <span class="legal-sep">|</span>
        <a href="https://new5ense.site/articles/13#" class="legal-link">금융소비자포털</a>
      </div>
    </div>
  </footer>
</template>

<style scoped>
.footer {
  width: 100%;
  border-top: 1px solid var(--line);
  background: var(--bg-soft);
  padding: 24px clamp(18px, 3vw, 40px);
  margin-top: auto;
}

.footer-inner {
  max-width: 1480px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
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
  color: var(--ink);
  padding: 0;
  white-space: nowrap;
}

.brand-mark {
  display: inline-block;
  width: 20px;
  height: 20px;
  border-radius: 6px;
  background: linear-gradient(135deg, #0a4a99 0%, #0084ff 100%);
  flex-shrink: 0;
}

.footer-line {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  min-width: 0;
  white-space: nowrap;
  overflow-x: auto;
  scrollbar-width: none;
}

.footer-line::-webkit-scrollbar {
  display: none;
}

.copyright,
.legal-link {
  font-size: 12px;
  color: var(--ink-3);
  white-space: nowrap;
}

.legal-link {
  text-decoration: none;
}

.legal-link:hover {
  color: #0084ff;
}

.legal-sep {
  font-size: 11px;
  color: var(--ink-3);
  opacity: 0.55;
}

@media (max-width: 760px) {
  .footer-inner {
    flex-direction: column;
    align-items: flex-start;
  }

  .footer-line {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
