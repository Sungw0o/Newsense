<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUserStore } from '../stores/useUserStore'
import communityApi from '../api/communityApi'

const router = useRouter()
const userStore = useUserStore()
const { userInfo } = storeToRefs(userStore)

const nickname = ref('')
const email = ref('')
const subPlan = ref('Standard Plan (Free)')

const categories = ref([
  { id: 'MACRO_ECONOMY', name: '거시경제' },
  { id: 'FINANCE_INVESTMENT', name: '금융/투자' },
  { id: 'POLICY_SYSTEM', name: '정책/제도' },
  { id: 'COMPANY_INDUSTRY', name: '기업/산업' },
  { id: 'GLOBAL_ECONOMY', name: '글로벌경제' },
])
const selectedCategories = ref(new Set())

const isSaving = ref(false)
const isWithdrawing = ref(false)
const showS3Modal = ref(false)

const initFormData = () => {
  if (userInfo.value) {
    nickname.value = userInfo.value.nickname || ''
    email.value = userInfo.value.email || ''
    subPlan.value = userInfo.value.subPlan || 'Standard Plan (Free)'
    const interests = userInfo.value.interests || []
    selectedCategories.value = new Set(interests)
  }
}

onMounted(async () => {
  try {
    await userStore.fetchUserProfile()
    initFormData()
  } catch {
    // use existing userInfo if available
    initFormData()
  }
})

watch(userInfo, initFormData, { deep: true })

const toggleCategory = (id) => {
  const s = new Set(selectedCategories.value)
  s.has(id) ? s.delete(id) : s.add(id)
  selectedCategories.value = s
}

const handleUpdateProfile = async () => {
  if (!nickname.value.trim()) { alert('닉네임을 입력해 주세요.'); return }
  isSaving.value = true
  try {
    await userStore.updateUserProfile({
      nickname: nickname.value.trim(),
      interests: [...selectedCategories.value],
    })
    alert('설정이 저장되었습니다!')
  } catch {
    alert('프로필 저장에 실패했습니다.')
  } finally {
    isSaving.value = false
  }
}

const handleWithdraw = async () => {
  if (!confirm('정말로 회원 탈퇴하시겠습니까?')) return
  if (!confirm('다시 한번 확인합니다. 탈퇴 후 계정은 비활성화됩니다.')) return
  isWithdrawing.value = true
  try {
    await userStore.deleteAccount()
    alert('회원 탈퇴가 완료되었습니다. 감사합니다.')
    router.push('/login')
  } catch {
    alert('탈퇴 처리 중 오류가 발생했습니다.')
  } finally {
    isWithdrawing.value = false
  }
}

const nicknameFirst = () => (nickname.value || 'U').substring(0, 1)

// 문의하기
const inquiryTitle = ref('')
const inquiryContent = ref('')
const isSubmittingInquiry = ref(false)
const inquiryDone = ref(false)

const submitInquiry = async () => {
  if (!inquiryTitle.value.trim() || !inquiryContent.value.trim()) {
    alert('제목과 내용을 모두 입력해 주세요.')
    return
  }
  isSubmittingInquiry.value = true
  try {
    await communityApi.createPost({ title: inquiryTitle.value.trim(), content: inquiryContent.value.trim(), type: 'INQUIRY' })
    inquiryDone.value = true
    inquiryTitle.value = ''
    inquiryContent.value = ''
  } catch {
    alert('문의 제출에 실패했습니다.')
  } finally {
    isSubmittingInquiry.value = false
  }
}
</script>

<template>
  <div class="settings-shell">
    <!-- Page header -->
    <header class="settings-head">
      <p class="eyebrow">계정 관리</p>
      <h1 class="settings-title">마이페이지</h1>
      <p class="settings-sub">계정 정보와 관심 카테고리를 관리하세요.</p>
    </header>

    <div class="settings-grid">
      <!-- Profile card -->
      <div class="profile-card">
        <div class="avatar-wrap">
          <button class="avatar-btn" @click="showS3Modal = true" title="프로필 이미지 변경">
            <div class="avatar">{{ nicknameFirst() }}</div>
            <span class="avatar-overlay">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                <circle cx="12" cy="13" r="4"/>
              </svg>
            </span>
          </button>
        </div>
        <h2 class="profile-name">{{ nickname || '사용자' }}</h2>
        <p class="profile-email">{{ email }}</p>

        <div class="plan-wrap">
          <span class="plan-label eyebrow" style="margin:0 0 6px; font-size:10px;">구독 플랜</span>
          <span class="plan-badge">{{ subPlan }}</span>
        </div>

        <div class="profile-actions">
          <button class="withdraw-btn" @click="handleWithdraw" :disabled="isWithdrawing">
            {{ isWithdrawing ? '처리 중…' : '회원 탈퇴' }}
          </button>
        </div>
      </div>

      <!-- Settings form -->
      <div class="settings-card">
        <p class="section-eyebrow eyebrow">프로필 & 환경설정</p>

        <div class="setting-section">
          <label class="setting-label" for="s-nickname">닉네임 변경</label>
          <input
            id="s-nickname"
            type="text"
            v-model="nickname"
            class="setting-input"
            placeholder="닉네임을 입력하세요"
          />
        </div>

        <div class="setting-section">
          <label class="setting-label">관심 경제 카테고리</label>
          <p class="setting-hint">선택한 주제 위주로 추천 피드가 구성됩니다.</p>
          <div class="cat-grid">
            <button
              v-for="cat in categories"
              :key="cat.id"
              type="button"
              class="cat-btn"
              :class="{ active: selectedCategories.has(cat.id) }"
              @click="toggleCategory(cat.id)"
            >
              <span class="cat-check">{{ selectedCategories.has(cat.id) ? '✓' : '+' }}</span>
              {{ cat.name }}
            </button>
          </div>
        </div>

        <div class="settings-foot">
          <button class="btn-primary save-btn" @click="handleUpdateProfile" :disabled="isSaving">
            <span v-if="isSaving" class="spinner-sm"></span>
            {{ isSaving ? '저장 중…' : '설정 저장하기' }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- 문의하기 섹션 -->
  <div class="inquiry-section">
    <p class="eyebrow">고객 지원</p>
    <h2 class="inquiry-title">문의하기</h2>
    <p class="inquiry-sub">서비스 이용 중 불편한 점이나 문의 사항을 남겨주세요.</p>

    <div v-if="inquiryDone" class="inquiry-done">
      <span class="done-icon">✅</span>
      <p>문의가 접수되었습니다. 빠르게 답변 드리겠습니다.</p>
      <button class="btn-link" @click="inquiryDone = false">새 문의 작성</button>
    </div>
    <div v-else class="inquiry-form">
      <div class="setting-section">
        <label class="setting-label">제목</label>
        <input
          v-model="inquiryTitle"
          type="text"
          class="setting-input"
          placeholder="문의 제목을 입력하세요"
          maxlength="200"
        />
      </div>
      <div class="setting-section">
        <label class="setting-label">내용</label>
        <textarea
          v-model="inquiryContent"
          class="inquiry-textarea"
          placeholder="문의 내용을 자세히 입력해 주세요."
          rows="5"
          maxlength="2000"
        ></textarea>
      </div>
      <div class="settings-foot">
        <button class="btn-primary save-btn" @click="submitInquiry" :disabled="isSubmittingInquiry">
          <span v-if="isSubmittingInquiry" class="spinner-sm"></span>
          {{ isSubmittingInquiry ? '제출 중…' : '문의 제출하기' }}
        </button>
      </div>
    </div>
  </div>

  <!-- S3 준비 중 모달 -->
  <Teleport to="body">
    <div v-if="showS3Modal" class="modal-backdrop" @click.self="showS3Modal = false">
      <div class="modal-box">
        <div class="modal-icon">📷</div>
        <h3 class="modal-title">프로필 이미지 변경</h3>
        <p class="modal-desc">이미지 업로드 기능은 현재 준비 중입니다.<br>곧 업데이트될 예정입니다.</p>
        <button class="btn-primary" style="width:100%; justify-content:center;" @click="showS3Modal = false">확인</button>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.settings-shell {
  max-width: 960px;
  margin: 0 auto;
  padding: 40px 0 80px;
}

.settings-head { margin-bottom: 32px; }

.eyebrow {
  font-family: 'Nanum Gothic', monospace;
  font-size: 11.5px;
  letter-spacing: 1px;
  color: var(--ink-3, #8a93a3);
  text-transform: uppercase;
  margin: 0 0 10px;
  display: block;
}

.settings-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 40px;
  letter-spacing: -1px;
  color: var(--ink, #0a0d12);
  margin: 0 0 8px;
}
.dark .settings-title { color: #f4f6fa; }

.settings-sub {
  font-size: 14px;
  color: var(--ink-2, #4a5161);
  margin: 0;
}
.dark .settings-sub { color: #a4adbf; }

.settings-grid {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 24px;
  align-items: start;
}

/* Profile card */
.profile-card {
  padding: 28px 24px;
  background: rgba(255,255,255,0.65);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 22px;
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,0.85),
    0 20px 50px -22px rgba(20,40,80,0.20);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 0;
}
.dark .profile-card {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.10);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.10), 0 20px 50px -22px rgba(0,0,0,0.6);
}

.avatar-wrap { margin-bottom: 14px; }

.avatar-btn {
  position: relative;
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  border-radius: 50%;
  display: block;
}
.avatar-btn:hover .avatar-overlay,
.avatar-btn:focus-visible .avatar-overlay { opacity: 1; }
.avatar-btn:focus-visible { outline: 2px solid #0084ff; outline-offset: 3px; }

.avatar {
  width: 72px; height: 72px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #9CCBFF 0%, #0084ff 60%, #0a4a99 100%);
  box-shadow: inset 0 2px 4px rgba(255,255,255,0.5), 0 4px 14px rgba(0,132,255,0.4);
  color: #fff;
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 28px;
  display: flex; align-items: center; justify-content: center;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity .15s;
}

.profile-name {
  font-family: 'Fustat', sans-serif;
  font-weight: 700;
  font-size: 18px;
  color: var(--ink, #0a0d12);
  margin: 0 0 4px;
}
.dark .profile-name { color: #f4f6fa; }

.profile-email {
  font-size: 12.5px;
  color: var(--ink-3, #8a93a3);
  margin: 0 0 20px;
  font-family: 'Nanum Gothic', monospace;
  word-break: break-all;
}

.plan-wrap {
  width: 100%;
  padding: 16px 0;
  border-top: 1px solid rgba(0,0,0,0.07);
  border-bottom: 1px solid rgba(0,0,0,0.07);
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.dark .plan-wrap { border-color: rgba(255,255,255,0.10); }

.plan-label { display: block; }

.plan-badge {
  display: inline-flex;
  padding: 5px 12px;
  background: #E8F2FF;
  color: #0056cc;
  border-radius: 999px;
  font-size: 12.5px;
  font-weight: 600;
  font-family: 'Nanum Gothic', monospace;
}
.dark .plan-badge { background: rgba(0,132,255,0.18); color: #9BCBFF; }

.profile-actions { width: 100%; }
.withdraw-btn {
  width: 100%;
  padding: 10px;
  background: transparent;
  border: 1px solid rgba(176,42,42,0.25);
  border-radius: 12px;
  color: #b02a2a;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all .15s;
}
.withdraw-btn:hover { background: rgba(255,237,237,0.5); }
.withdraw-btn:disabled { opacity: 0.5; cursor: default; }
.dark .withdraw-btn { color: #ff8a8a; border-color: rgba(255,106,106,0.25); }
.dark .withdraw-btn:hover { background: rgba(255,106,106,0.10); }

/* Settings card */
.settings-card {
  padding: 32px;
  background: rgba(255,255,255,0.65);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 22px;
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  box-shadow:
    inset 0 1px 0 rgba(255,255,255,0.85),
    0 20px 50px -22px rgba(20,40,80,0.20);
}
.dark .settings-card {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.10);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.10), 0 20px 50px -22px rgba(0,0,0,0.6);
}

.section-eyebrow { margin-bottom: 22px; }

.setting-section { margin-bottom: 26px; }

.setting-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--ink, #0a0d12);
  margin-bottom: 8px;
}
.dark .setting-label { color: #f4f6fa; }

.setting-input {
  appearance: none;
  width: 100%;
  padding: 12px 16px;
  background: rgba(255,255,255,0.85);
  border: 1.5px solid rgba(0,0,0,0.08);
  border-radius: 12px;
  font: inherit;
  font-size: 14px;
  color: var(--ink, #0a0d12);
  outline: none;
  transition: all .15s;
}
.setting-input:focus {
  border-color: #0084ff;
  background: #fff;
  box-shadow: 0 0 0 4px rgba(0,132,255,0.12);
}
.dark .setting-input {
  background: rgba(20,24,34,0.70);
  border-color: rgba(255,255,255,0.12);
  color: #f4f6fa;
}
.dark .setting-input:focus { background: rgba(20,24,34,0.90); }

.setting-hint {
  font-size: 12.5px;
  color: var(--ink-3, #8a93a3);
  margin: 0 0 12px;
}

.cat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.cat-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: rgba(255,255,255,0.6);
  border: 1.5px solid rgba(0,0,0,0.08);
  border-radius: 12px;
  font-size: 13.5px;
  font-weight: 500;
  color: var(--ink-2, #4a5161);
  cursor: pointer;
  transition: all .15s;
  text-align: left;
}
.cat-btn:hover { border-color: rgba(0,132,255,0.35); color: var(--ink, #0a0d12); }
.cat-btn.active {
  background: #E8F2FF;
  border-color: rgba(0,132,255,0.40);
  color: #0056cc;
  font-weight: 600;
}
.dark .cat-btn {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.12);
  color: #a4adbf;
}
.dark .cat-btn:hover { border-color: rgba(0,132,255,0.45); color: #f4f6fa; }
.dark .cat-btn.active {
  background: rgba(0,132,255,0.18);
  border-color: rgba(0,132,255,0.45);
  color: #9BCBFF;
}

.cat-check {
  width: 20px; height: 20px;
  border-radius: 6px;
  background: rgba(0,0,0,0.06);
  display: flex; align-items: center; justify-content: center;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}
.cat-btn.active .cat-check {
  background: #0084ff;
  color: #fff;
}
.dark .cat-btn.active .cat-check { background: rgba(0,132,255,0.7); }

.settings-foot {
  padding-top: 24px;
  border-top: 1px solid rgba(0,0,0,0.07);
  display: flex;
  justify-content: flex-end;
}
.dark .settings-foot { border-color: rgba(255,255,255,0.10); }

.save-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  font-size: 14px;
}
.save-btn:disabled { opacity: 0.65; cursor: default; }

.spinner-sm {
  width: 15px; height: 15px;
  border: 2.5px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .65s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* S3 modal */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.modal-box {
  width: 320px;
  padding: 32px 28px;
  background: rgba(255,255,255,0.90);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 24px;
  backdrop-filter: blur(48px) saturate(180%);
  box-shadow: 0 30px 80px -20px rgba(20,40,80,0.25);
  text-align: center;
}
.dark .modal-box {
  background: rgba(20,24,34,0.88);
  border-color: rgba(255,255,255,0.12);
  box-shadow: 0 30px 80px -20px rgba(0,0,0,0.6);
}
.modal-icon { font-size: 40px; margin-bottom: 14px; }
.modal-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 20px;
  color: var(--ink, #0a0d12);
  margin: 0 0 8px;
}
.dark .modal-title { color: #f4f6fa; }
.modal-desc {
  font-size: 13.5px;
  color: var(--ink-2, #4a5161);
  line-height: 1.6;
  margin: 0 0 22px;
}
.dark .modal-desc { color: #a4adbf; }

/* Inquiry section */
.inquiry-section {
  max-width: 960px;
  margin: 32px auto 0;
  padding: 32px;
  background: rgba(255,255,255,0.65);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 22px;
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.85), 0 4px 20px -8px rgba(20,40,80,0.10);
}
.dark .inquiry-section {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.10);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.08), 0 4px 20px -8px rgba(0,0,0,0.35);
}

.inquiry-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 700;
  font-size: 22px;
  color: var(--ink, #0a0d12);
  margin: 0 0 6px;
}
.dark .inquiry-title { color: #f4f6fa; }
.inquiry-sub { font-size: 13.5px; color: var(--ink-3, #8a93a3); margin: 0 0 24px; }

.inquiry-form {}
.inquiry-textarea {
  width: 100%;
  padding: 12px 14px;
  background: rgba(255,255,255,0.85);
  border: 1.5px solid rgba(0,0,0,0.08);
  border-radius: 12px;
  font: inherit;
  font-size: 14px;
  color: var(--ink, #0a0d12);
  resize: vertical;
  outline: none;
  transition: border-color .15s;
  box-sizing: border-box;
}
.inquiry-textarea:focus { border-color: #0084ff; box-shadow: 0 0 0 3px rgba(0,132,255,0.10); }
.dark .inquiry-textarea { background: rgba(20,24,34,0.70); border-color: rgba(255,255,255,0.12); color: #f4f6fa; }

.inquiry-done {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 32px;
  text-align: center;
  color: var(--ink-2, #4a5161);
  font-size: 14px;
}
.dark .inquiry-done { color: #a4adbf; }
.done-icon { font-size: 32px; }

.btn-link {
  background: none;
  border: none;
  color: #0084ff;
  font-size: 13.5px;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
}
.btn-link:hover { text-decoration: underline; }

@media (max-width: 800px) {
  .settings-grid { grid-template-columns: 1fr; }
  .cat-grid { grid-template-columns: repeat(2, 1fr); }
  .settings-shell { padding: 30px 0 60px; }
  .inquiry-section { padding: 20px 16px; }
}
</style>
