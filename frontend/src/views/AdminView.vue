<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '../stores/useAdminStore'
import { useUserStore } from '../stores/useUserStore'
import adminApi from '../api/adminApi'

const router = useRouter()
const store = useAdminStore()
const userStore = useUserStore()

if (userStore.userInfo?.role !== 'ADMIN') {
  router.replace('/')
}

const activeTab = ref('stats')
const reports = ref([])
const isLoadingReports = ref(false)
const inquiries = ref([])
const isLoadingInquiries = ref(false)
const articleQuery = ref({
  page: 0,
  size: 20,
  sort: 'collectedAt,desc',
})

onMounted(async () => {
  await store.fetchStats()
  await store.fetchUsers()
})

const tabs = [
  { key: 'stats',     label: '통계' },
  { key: 'articles',  label: '기사 관리' },
  { key: 'users',     label: '사용자 관리' },
  { key: 'reports',   label: '신고 목록' },
  { key: 'inquiries', label: '문의' },
]

const roleLabel = (role) => role === 'ADMIN' ? '관리자' : '일반 사용자'
const roleClass  = (role) => role === 'ADMIN' ? 'badge-admin' : 'badge-user'

const handleRoleToggle = async (userId) => {
  try {
    await store.changeUserRole(userId)
  } catch { /* stored in store.error */ }
}

const handleDeactivateUser = async (user) => {
  if (user.active === false) return
  if (!confirm(`${user.nickname} 사용자를 탈퇴 처리하시겠습니까?`)) return
  try {
    await store.deactivateUser(user.id)
  } catch {
    alert(store.error || '탈퇴 처리에 실패했습니다.')
  }
}

const loadReports = async () => {
  if (reports.value.length > 0) return
  isLoadingReports.value = true
  try {
    const res = await adminApi.getReports({ size: 50 })
    const data = res?.data?.data ?? res?.data ?? {}
    reports.value = data.content ?? []
  } catch {
    reports.value = []
  } finally {
    isLoadingReports.value = false
  }
}

const loadInquiries = async (force = false) => {
  if (!force && inquiries.value.length > 0) return
  isLoadingInquiries.value = true
  try {
    const res = await adminApi.getInquiries({ size: 50 })
    const data = res?.data?.data ?? res?.data ?? {}
    inquiries.value = data.content ?? []
  } catch {
    inquiries.value = []
  } finally {
    isLoadingInquiries.value = false
  }
}

const handleResolveInquiry = async (inquiryId) => {
  try {
    await adminApi.resolveInquiry(inquiryId)
    const idx = inquiries.value.findIndex(i => i.id === inquiryId)
    if (idx !== -1) inquiries.value[idx].resolved = true
  } catch {
    alert('처리에 실패했습니다.')
  }
}

const handleTabChange = (key) => {
  activeTab.value = key
  if (key === 'reports')   loadReports()
  if (key === 'inquiries') loadInquiries()
  if (key === 'articles' && store.articles.length === 0) loadArticles()
}

const formatArticleDate = (value) => {
  if (!value) return '-'
  return String(value).substring(0, 10).replaceAll('-', '.')
}

const formatLength = (length) => `${Number(length ?? 0).toLocaleString()}자`
const canRequestSummary = (article) => !article.hasAiSummary
const loadArticles = (patch = {}) => {
  articleQuery.value = { ...articleQuery.value, ...patch }
  return store.fetchArticles(articleQuery.value)
}

const handleRefreshSummary = async (articleId) => {
  try {
    await store.refreshArticleSummary(articleId)
  } catch {
    alert(store.error || 'AI 요약 생성에 실패했습니다.')
  }
}

const handleDeleteArticle = async (article) => {
  if (!confirm(`"${article.title}" 기사를 삭제하시겠습니까?`)) return
  try {
    await store.deleteArticle(article.articleId)
    if (store.articles.length === 0 && articleQuery.value.page > 0) {
      await loadArticles({ page: articleQuery.value.page - 1 })
      return
    }
    await loadArticles()
  } catch {
    alert(store.error || '기사 삭제에 실패했습니다.')
  }
}

const handleArticlePage = (page) => {
  const nextPage = Math.max(0, Math.min(page, Math.max(0, store.articlePage.totalPages - 1)))
  if (nextPage === articleQuery.value.page) return
  loadArticles({ page: nextPage })
}

const handleArticleSize = (event) => {
  loadArticles({ page: 0, size: Number(event.target.value) })
}

const formatDate = (iso) => {
  if (!iso) return ''
  const d = new Date(iso)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const handleDeleteReportedPost = async (postId) => {
  if (!confirm('해당 게시글을 삭제하시겠습니까?')) return
  try {
    await store.deletePost(postId)
    reports.value = reports.value.filter(r => r.postId !== postId)
  } catch {
    alert('삭제에 실패했습니다.')
  }
}

// ── 크롤링 ──────────────────────────────────────────────────────────────
const isCrawling   = ref(false)
const crawlResult  = ref(null)
const maxPerSource = ref(50)

const handleTriggerCrawl = async () => {
  if (!confirm(`소스당 최대 ${maxPerSource.value}건 기준으로 크롤링을 실행합니다. 계속할까요?`)) return
  isCrawling.value  = true
  crawlResult.value = null
  try {
    const res = await adminApi.triggerCrawl(maxPerSource.value)
    crawlResult.value = res?.data?.data ?? res?.data ?? {}
    await store.fetchStats()
  } catch {
    alert('크롤링 실행에 실패했습니다.')
  } finally {
    isCrawling.value = false
  }
}
</script>

<template>
  <div class="admin-shell">
    <header class="admin-head">
      <div>
        <p class="eyebrow">관리자 전용</p>
        <h1 class="admin-title">관리자 페이지</h1>
      </div>
    </header>

    <div class="tab-row">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-btn"
        :class="{ active: activeTab === tab.key }"
        @click="handleTabChange(tab.key)"
      >{{ tab.label }}</button>
    </div>

    <div v-if="store.error" class="error-banner">{{ store.error }}</div>

    <!-- Stats tab -->
    <section v-if="activeTab === 'stats'" class="tab-content">
      <div v-if="store.isLoadingStats" class="loading-state">
        <div class="spinner"></div>
      </div>
      <div v-else-if="store.stats" class="stats-grid">
        <div class="stat-card">
          <span class="stat-icon">👤</span>
          <span class="stat-num">{{ store.stats.totalUsers.toLocaleString() }}</span>
          <span class="stat-label">전체 사용자</span>
        </div>
        <div class="stat-card">
          <span class="stat-icon">📝</span>
          <span class="stat-num">{{ store.stats.totalPosts.toLocaleString() }}</span>
          <span class="stat-label">전체 게시글</span>
        </div>
        <div class="stat-card">
          <span class="stat-icon">📰</span>
          <span class="stat-num">{{ store.stats.totalArticles.toLocaleString() }}</span>
          <span class="stat-label">전체 기사</span>
        </div>
      </div>

      <div class="crawl-section">
        <div class="crawl-header">
          <span class="crawl-title">수동 크롤링</span>
          <span class="crawl-desc">공공기관(한국은행·기획재정부) + 포털(네이버·NewsAPI·Google)을 즉시 수집합니다.</span>
        </div>
        <div class="crawl-controls">
          <div class="crawl-limit-wrap">
            <label class="crawl-limit-label" for="max-per-source">소스당 최대</label>
            <input
              id="max-per-source"
              v-model.number="maxPerSource"
              type="number"
              class="crawl-limit-input"
              min="1"
              max="200"
            />
            <span class="crawl-limit-unit">건</span>
          </div>
          <button class="crawl-btn" :disabled="isCrawling" @click="handleTriggerCrawl">
            <span v-if="isCrawling" class="btn-spinner"></span>
            <span>{{ isCrawling ? '크롤링 중...' : '지금 크롤링 실행' }}</span>
          </button>
        </div>
        <div v-if="crawlResult" class="crawl-result">
          <span class="cr-item">발견 <strong>{{ crawlResult.discovered }}</strong></span>
          <span class="cr-item saved">저장 <strong>{{ crawlResult.saved }}</strong></span>
          <span class="cr-item">건너뜀 <strong>{{ crawlResult.skipped }}</strong></span>
          <span v-if="crawlResult.failed > 0" class="cr-item failed">실패 <strong>{{ crawlResult.failed }}</strong></span>
        </div>
      </div>
    </section>

    <!-- Reports tab -->
    <section v-if="activeTab === 'reports'" class="tab-content">
      <div v-if="isLoadingReports" class="loading-state"><div class="spinner"></div></div>
      <div v-else-if="reports.length === 0" class="empty"><p class="eyebrow" style="text-align:center;">신고된 게시글이 없습니다</p></div>
      <div v-else class="user-table-wrap">
        <table class="user-table">
          <thead>
            <tr>
              <th>신고 ID</th><th>게시글 제목</th><th>신고자</th><th>사유</th><th>일시</th><th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in reports" :key="r.reportId">
              <td class="td-id">{{ r.reportId }}</td>
              <td class="td-nick" style="max-width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">{{ r.postTitle }}</td>
              <td>{{ r.reporterNickname }}</td>
              <td style="max-width:180px;color:var(--ink-3,#8a93a3);font-size:12.5px;">{{ r.reason || '—' }}</td>
              <td class="td-email">{{ formatDate(r.reportedAt) }}</td>
              <td>
                <button class="action-btn danger" @click="handleDeleteReportedPost(r.postId)">게시글 삭제</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- Inquiries tab -->
    <section v-if="activeTab === 'inquiries'" class="tab-content">
      <div class="section-toolbar">
        <p class="section-help">사용자가 남긴 문의를 확인하고 처리 완료 표시를 할 수 있습니다.</p>
        <button class="action-btn" :disabled="isLoadingInquiries" @click="loadInquiries(true)">새로고침</button>
      </div>
      <div v-if="isLoadingInquiries" class="loading-state"><div class="spinner"></div></div>
      <div v-else-if="inquiries.length === 0" class="empty">
        <p class="eyebrow" style="text-align:center;">접수된 문의가 없습니다</p>
      </div>
      <div v-else class="user-table-wrap">
        <table class="user-table">
          <thead>
            <tr>
              <th>ID</th><th>작성자</th><th>제목</th><th>내용</th><th>접수일</th><th>처리 여부</th><th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="inq in inquiries" :key="inq.id">
              <td class="td-id">{{ inq.id }}</td>
              <td class="td-nick">{{ inq.authorName }}</td>
              <td class="td-article" style="max-width:180px;">
                <strong style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;display:block;">{{ inq.title }}</strong>
              </td>
              <td style="max-width:240px;font-size:12.5px;color:var(--ink-2,#4a5161);">
                <span style="display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;">{{ inq.content }}</span>
              </td>
              <td class="td-email">{{ formatDate(inq.createdAt) }}</td>
              <td>
                <span class="role-badge" :class="inq.resolved ? 'badge-admin' : 'badge-user'">
                  {{ inq.resolved ? '처리 완료' : '미처리' }}
                </span>
              </td>
              <td>
                <button
                  class="action-btn"
                  :disabled="inq.resolved"
                  @click="handleResolveInquiry(inq.id)"
                >처리 완료</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- Articles tab -->
    <section v-if="activeTab === 'articles'" class="tab-content">
      <div class="section-toolbar">
        <p class="section-help">본문 길이와 AI 요약본 저장 상태를 확인하고, 누락된 요약을 수동으로 생성할 수 있습니다.</p>
        <div class="toolbar-actions">
          <label class="sr-only" for="article-page-size">기사 목록 페이지 크기</label>
          <select id="article-page-size" class="page-size" :value="articleQuery.size" @change="handleArticleSize">
            <option :value="10">10개</option>
            <option :value="20">20개</option>
            <option :value="50">50개</option>
          </select>
          <button class="action-btn" :disabled="store.isLoadingArticles" @click="loadArticles()">새로고침</button>
        </div>
      </div>
      <div v-if="store.isLoadingArticles" class="loading-state"><div class="spinner"></div></div>
      <div v-else class="user-table-wrap">
        <table class="user-table">
          <thead>
            <tr>
              <th>ID</th><th>기사</th><th>출처</th><th>수집일</th><th>본문 길이</th><th>AI 요약</th><th>조회수</th><th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="article in store.articles" :key="article.articleId">
              <td class="td-id">{{ article.articleId }}</td>
              <td class="td-article">
                <strong>{{ article.title }}</strong>
                <span>{{ article.category }}</span>
              </td>
              <td class="td-email">{{ article.source }}</td>
              <td class="td-email">{{ formatArticleDate(article.publishedAt) }}</td>
              <td>{{ formatLength(article.contentLength) }}</td>
              <td>
                <span class="role-badge" :class="article.hasAiSummary ? 'badge-admin' : 'badge-user'">
                  {{ article.hasAiSummary ? '있음' : '없음' }}
                </span>
              </td>
              <td>{{ Number(article.viewCount ?? 0).toLocaleString() }}</td>
              <td>
                <button
                  class="action-btn"
                  :disabled="!canRequestSummary(article) || store.summarizingArticleIds.includes(article.articleId)"
                  @click="handleRefreshSummary(article.articleId)"
                >
                  {{ store.summarizingArticleIds.includes(article.articleId) ? '생성 중' : '요약 생성' }}
                </button>
                <button class="action-btn danger" @click="handleDeleteArticle(article)">삭제</button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="store.articles.length === 0" class="empty">
          <p class="eyebrow" style="text-align:center;">수집된 기사가 없습니다</p>
        </div>
      </div>
      <div v-if="store.articlePage.totalElements > 0" class="pager">
        <span class="pager-count">
          총 {{ Number(store.articlePage.totalElements).toLocaleString() }}건 ·
          {{ store.articlePage.number + 1 }} / {{ Math.max(1, store.articlePage.totalPages) }}페이지
        </span>
        <div class="pager-controls">
          <button class="action-btn" :disabled="store.articlePage.first" @click="handleArticlePage(0)">처음</button>
          <button class="action-btn" :disabled="store.articlePage.first" @click="handleArticlePage(store.articlePage.number - 1)">이전</button>
          <button class="action-btn" :disabled="store.articlePage.last" @click="handleArticlePage(store.articlePage.number + 1)">다음</button>
          <button class="action-btn" :disabled="store.articlePage.last" @click="handleArticlePage(store.articlePage.totalPages - 1)">끝</button>
        </div>
      </div>
    </section>

    <!-- Users tab -->
    <section v-if="activeTab === 'users'" class="tab-content">
      <div v-if="store.isLoadingUsers" class="loading-state"><div class="spinner"></div></div>
      <div v-else class="user-table-wrap">
        <table class="user-table">
          <thead>
            <tr>
              <th>ID</th><th>닉네임</th><th>이메일</th><th>역할</th><th>상태</th><th>레벨</th><th>작업</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in store.users" :key="user.id">
              <td class="td-id">{{ user.id }}</td>
              <td class="td-nick">{{ user.nickname }}</td>
              <td class="td-email">{{ user.email }}</td>
              <td>
                <span class="role-badge" :class="roleClass(user.role)">{{ roleLabel(user.role) }}</span>
              </td>
              <td>
                <span class="role-badge" :class="user.active === false ? 'badge-withdrawn' : 'badge-active'">
                  {{ user.active === false ? '탈퇴' : '활성' }}
                </span>
              </td>
              <td>{{ user.level }}</td>
              <td>
                <button class="action-btn" :disabled="user.active === false" @click="handleRoleToggle(user.id)">
                  {{ user.role === 'ADMIN' ? '권한 해제' : '관리자 지정' }}
                </button>
                <button class="action-btn danger" :disabled="user.active === false" @click="handleDeactivateUser(user)">
                  탈퇴 처리
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-if="store.users.length === 0" class="empty">
          <p class="eyebrow" style="text-align:center;">사용자가 없습니다</p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.admin-shell {
  max-width: 960px;
  margin: 0 auto;
  padding: 48px 0 80px;
}

.eyebrow {
  font-family: 'Nanum Gothic', monospace;
  font-size: 11.5px;
  letter-spacing: 1px;
  color: var(--ink-3, #8a93a3);
  text-transform: uppercase;
  margin: 0 0 10px;
  display: block;
}

.admin-head { margin-bottom: 28px; }

.admin-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 44px;
  letter-spacing: -1.5px;
  color: var(--ink, #0a0d12);
  margin: 0 0 6px;
}
.dark .admin-title { color: #f4f6fa; }

.tab-row {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  border-bottom: 1px solid rgba(0,0,0,0.08);
  flex-wrap: wrap;
}
.dark .tab-row { border-color: rgba(255,255,255,0.10); }

.tab-btn {
  padding: 10px 20px;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-3, #8a93a3);
  cursor: pointer;
  transition: color .15s, border-color .15s;
  margin-bottom: -1px;
}
.tab-btn:hover { color: var(--ink, #0a0d12); }
.tab-btn.active { color: #0084ff; border-bottom-color: #0084ff; }
.dark .tab-btn:hover { color: #f4f6fa; }
.dark .tab-btn.active { color: #4FB3FF; border-bottom-color: #4FB3FF; }

.tab-content { margin-top: 8px; }

.section-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size {
  height: 31px;
  padding: 0 9px;
  border: 1px solid rgba(0,0,0,0.12);
  border-radius: 8px;
  background: rgba(255,255,255,0.8);
  color: var(--ink, #0a0d12);
  font-size: 12.5px;
  font-weight: 700;
}

.dark .page-size {
  background: rgba(20,24,34,0.65);
  border-color: rgba(255,255,255,0.14);
  color: #f4f6fa;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.section-help {
  margin: 0;
  font-size: 13px;
  color: var(--ink-3, #8a93a3);
}

.error-banner {
  padding: 12px 16px;
  background: rgba(239,68,68,0.08);
  border: 1px solid rgba(239,68,68,0.20);
  border-radius: 10px;
  color: #dc2626;
  font-size: 13.5px;
  margin-bottom: 16px;
}
.dark .error-banner { color: #fca5a5; background: rgba(239,68,68,0.12); }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
@media (max-width: 640px) { .stats-grid { grid-template-columns: 1fr; } }

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 32px 24px;
  background: rgba(255,255,255,0.65);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 18px;
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.85), 0 4px 20px -8px rgba(20,40,80,0.10);
  text-align: center;
}
.dark .stat-card {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.10);
}

.stat-icon { font-size: 28px; }
.stat-num {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 36px;
  color: var(--ink, #0a0d12);
  letter-spacing: -1px;
}
.dark .stat-num { color: #f4f6fa; }
.stat-label { font-size: 13px; color: var(--ink-3, #8a93a3); font-weight: 500; }

.user-table-wrap {
  overflow-x: auto;
  border-radius: 16px;
  border: 1px solid rgba(0,0,0,0.07);
  background: rgba(255,255,255,0.65);
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.85), 0 4px 20px -8px rgba(20,40,80,0.10);
}
.dark .user-table-wrap {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.10);
}

.user-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13.5px;
}

.user-table th {
  padding: 12px 16px;
  text-align: left;
  font-family: 'Nanum Gothic', monospace;
  font-size: 11.5px;
  letter-spacing: 0.5px;
  color: var(--ink-3, #8a93a3);
  border-bottom: 1px solid rgba(0,0,0,0.08);
  white-space: nowrap;
}
.dark .user-table th { border-color: rgba(255,255,255,0.10); }

.user-table td {
  padding: 13px 16px;
  border-bottom: 1px solid rgba(0,0,0,0.05);
  color: var(--ink, #0a0d12);
  vertical-align: middle;
}
.dark .user-table td { border-color: rgba(255,255,255,0.07); color: #e0e4ef; }
.user-table tbody tr:last-child td { border-bottom: none; }

.td-id { color: var(--ink-3, #8a93a3); font-size: 12px; font-family: 'Nanum Gothic', monospace; }
.td-nick { font-weight: 600; }
.td-email { color: var(--ink-2, #4a5161); font-size: 13px; }
.dark .td-email { color: #a4adbf; }

.td-article { max-width: 320px; }
.td-article strong {
  display: block;
  font-size: 13.5px;
  line-height: 1.35;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.td-article span {
  display: block;
  margin-top: 3px;
  font-size: 11.5px;
  color: var(--ink-3, #8a93a3);
}

.role-badge {
  display: inline-flex;
  align-items: center;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 700;
  font-family: 'Nanum Gothic', monospace;
}
.badge-admin { background: rgba(99,102,241,0.12); color: #6366f1; }
.badge-user  { background: rgba(0,0,0,0.06);        color: var(--ink-3, #8a93a3); }
.badge-active { background: rgba(34,197,94,0.12); color: #15803d; }
.badge-withdrawn { background: rgba(239,68,68,0.10); color: #dc2626; }
.dark .badge-admin { background: rgba(99,102,241,0.20); color: #a5b4fc; }
.dark .badge-user  { background: rgba(255,255,255,0.08); }
.dark .badge-active { background: rgba(34,197,94,0.18); color: #86efac; }
.dark .badge-withdrawn { background: rgba(239,68,68,0.18); color: #fca5a5; }

.action-btn {
  padding: 6px 12px;
  background: rgba(0,132,255,0.08);
  border: 1px solid rgba(0,132,255,0.20);
  border-radius: 8px;
  font-size: 12.5px;
  font-weight: 600;
  color: #0084ff;
  cursor: pointer;
  transition: background .12s, border-color .12s;
  white-space: nowrap;
}
.action-btn:disabled { opacity: 0.45; cursor: not-allowed; }
.action-btn:hover { background: rgba(0,132,255,0.14); border-color: rgba(0,132,255,0.35); }
.dark .action-btn { color: #4FB3FF; background: rgba(0,132,255,0.10); border-color: rgba(0,132,255,0.25); }
.dark .action-btn:hover { background: rgba(0,132,255,0.18); }
.action-btn.danger { color: #dc2626; background: rgba(239,68,68,0.07); border-color: rgba(239,68,68,0.25); }
.action-btn.danger:hover { background: rgba(239,68,68,0.15); border-color: rgba(239,68,68,0.45); }

td .action-btn + .action-btn {
  margin-left: 6px;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 14px;
  flex-wrap: wrap;
}

.pager-count {
  font-size: 12.5px;
  color: var(--ink-3, #8a93a3);
}

.pager-controls {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.loading-state { display: flex; justify-content: center; padding: 80px 0; }
.spinner {
  width: 32px; height: 32px;
  border: 3px solid rgba(0,132,255,0.15);
  border-top-color: #0084ff;
  border-radius: 50%;
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.empty { padding: 60px 0; }

/* ── 수동 크롤링 섹션 ─────────────────────────────── */
.crawl-section {
  margin-top: 28px;
  padding: 20px 22px;
  background: rgba(0,132,255,0.04);
  border: 1px solid rgba(0,132,255,0.14);
  border-radius: 16px;
}
.dark .crawl-section {
  background: rgba(0,132,255,0.06);
  border-color: rgba(0,132,255,0.22);
}
.crawl-header {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.crawl-title { font-size: 14px; font-weight: 700; color: var(--ink); }
.dark .crawl-title { color: #f4f6fa; }
.crawl-desc { font-size: 12.5px; color: var(--ink-3, #8a93a3); }

.crawl-controls {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.crawl-limit-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
}
.crawl-limit-label { font-size: 13px; color: var(--ink-2, #4a5161); font-weight: 600; }
.dark .crawl-limit-label { color: #a4adbf; }
.crawl-limit-input {
  width: 68px;
  padding: 6px 10px;
  border: 1px solid rgba(0,0,0,0.14);
  border-radius: 8px;
  font-size: 14px;
  font-weight: 700;
  text-align: center;
  background: rgba(255,255,255,0.9);
  color: var(--ink);
  outline: none;
}
.crawl-limit-input:focus { border-color: #0084ff; box-shadow: 0 0 0 2px rgba(0,132,255,0.14); }
.dark .crawl-limit-input { background: rgba(20,24,34,0.6); border-color: rgba(255,255,255,0.15); color: #f4f6fa; }
.crawl-limit-unit { font-size: 13px; color: var(--ink-3); }

.crawl-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 9px 20px;
  background: #0084ff;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  transition: background .15s, opacity .15s;
}
.crawl-btn:hover { background: #006fdd; }
.crawl-btn:disabled { opacity: 0.55; cursor: not-allowed; }
.btn-spinner {
  display: inline-block;
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .65s linear infinite;
}
.crawl-result {
  display: flex;
  gap: 16px;
  margin-top: 14px;
  flex-wrap: wrap;
}
.cr-item { font-size: 13px; color: var(--ink-2, #4a5161); }
.dark .cr-item { color: #a4adbf; }
.cr-item strong { color: var(--ink); font-weight: 700; margin-left: 4px; }
.dark .cr-item strong { color: #f4f6fa; }
.cr-item.saved strong  { color: #16a34a; }
.cr-item.failed strong { color: #dc2626; }

@media (max-width: 640px) {
  .admin-shell { padding: 32px 0 60px; }
  .admin-title { font-size: 34px; }
}
</style>
