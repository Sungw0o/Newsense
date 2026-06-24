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

onMounted(async () => {
  await store.fetchStats()
  await store.fetchUsers()
})

const tabs = [
  { key: 'stats', label: '통계' },
  { key: 'users', label: '사용자 관리' },
  { key: 'reports', label: '신고 목록' },
]

const roleLabel = (role) => role === 'ADMIN' ? '관리자' : '일반 사용자'
const roleClass = (role) => role === 'ADMIN' ? 'badge-admin' : 'badge-user'

const handleRoleToggle = async (userId) => {
  try {
    await store.changeUserRole(userId)
  } catch {
    // error already stored in store.error
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

const handleTabChange = (key) => {
  activeTab.value = key
  if (key === 'reports') loadReports()
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
</script>

<template>
  <div class="admin-shell">
    <header class="admin-head">
      <div>
        <p class="eyebrow">관리자 전용</p>
        <h1 class="admin-title">관리자 페이지</h1>
      </div>
    </header>

    <!-- Tabs -->
    <div class="tab-row">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-btn"
        :class="{ active: activeTab === tab.key }"
        @click="handleTabChange(tab.key)"
      >{{ tab.label }}</button>
    </div>

    <!-- Error banner -->
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
    </section>

    <!-- Reports tab -->
    <section v-if="activeTab === 'reports'" class="tab-content">
      <div v-if="isLoadingReports" class="loading-state"><div class="spinner"></div></div>
      <div v-else-if="reports.length === 0" class="empty"><p class="eyebrow" style="text-align:center;">신고된 게시글이 없습니다</p></div>
      <div v-else class="user-table-wrap">
        <table class="user-table">
          <thead>
            <tr>
              <th>신고 ID</th>
              <th>게시글 제목</th>
              <th>신고자</th>
              <th>사유</th>
              <th>일시</th>
              <th>작업</th>
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

    <!-- Users tab -->
    <section v-if="activeTab === 'users'" class="tab-content">
      <div v-if="store.isLoadingUsers" class="loading-state">
        <div class="spinner"></div>
      </div>
      <div v-else class="user-table-wrap">
        <table class="user-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>닉네임</th>
              <th>이메일</th>
              <th>역할</th>
              <th>레벨</th>
              <th>작업</th>
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
              <td>{{ user.level }}</td>
              <td>
                <button class="action-btn" @click="handleRoleToggle(user.id)">
                  {{ user.role === 'ADMIN' ? '권한 해제' : '관리자 지정' }}
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

.admin-head {
  margin-bottom: 28px;
}

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
  padding-bottom: 0;
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

.error-banner {
  padding: 12px 16px;
  background: rgba(239, 68, 68, 0.08);
  border: 1px solid rgba(239, 68, 68, 0.20);
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
@media (max-width: 640px) {
  .stats-grid { grid-template-columns: 1fr; }
}

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
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.10), 0 4px 20px -8px rgba(0,0,0,0.4);
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
.stat-label {
  font-size: 13px;
  color: var(--ink-3, #8a93a3);
  font-weight: 500;
}

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
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.10), 0 4px 20px -8px rgba(0,0,0,0.4);
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
.badge-user { background: rgba(0,0,0,0.06); color: var(--ink-3, #8a93a3); }
.dark .badge-admin { background: rgba(99,102,241,0.20); color: #a5b4fc; }
.dark .badge-user { background: rgba(255,255,255,0.08); }

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
.action-btn:hover { background: rgba(0,132,255,0.14); border-color: rgba(0,132,255,0.35); }
.dark .action-btn { color: #4FB3FF; background: rgba(0,132,255,0.10); border-color: rgba(0,132,255,0.25); }
.dark .action-btn:hover { background: rgba(0,132,255,0.18); }
.action-btn.danger { color: #dc2626; background: rgba(239,68,68,0.07); border-color: rgba(239,68,68,0.25); }
.action-btn.danger:hover { background: rgba(239,68,68,0.15); border-color: rgba(239,68,68,0.45); }

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

@media (max-width: 640px) {
  .admin-shell { padding: 32px 0 60px; }
  .admin-title { font-size: 34px; }
}
</style>
