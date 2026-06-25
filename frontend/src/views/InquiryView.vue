<script setup>
import { ref, onMounted } from 'vue'
import inquiryApi from '../api/inquiryApi'

const title   = ref('')
const content = ref('')
const submitting = ref(false)
const submitted  = ref(false)
const myInquiries = ref([])
const loadingMy   = ref(true)

const formatDate = (iso) => {
  if (!iso) return ''
  const d = new Date(iso)
  return `${d.getFullYear()}.${String(d.getMonth()+1).padStart(2,'0')}.${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

const loadMy = async () => {
  try {
    const res = await inquiryApi.getMyInquiries({ size: 20 })
    const data = res?.data?.data ?? res?.data ?? {}
    myInquiries.value = data.content ?? []
  } catch {
    myInquiries.value = []
  } finally {
    loadingMy.value = false
  }
}

onMounted(loadMy)

const handleSubmit = async () => {
  if (!title.value.trim() || !content.value.trim()) {
    alert('제목과 내용을 모두 입력해 주세요.')
    return
  }
  submitting.value = true
  try {
    await inquiryApi.submit({ title: title.value, content: content.value })
    submitted.value = true
    title.value   = ''
    content.value = ''
    await loadMy()
  } catch {
    alert('문의 접수에 실패했습니다. 다시 시도해 주세요.')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="inquiry-shell">
    <header class="inquiry-head">
      <p class="eyebrow">고객 지원</p>
      <h1 class="inquiry-title">문의하기</h1>
    </header>

    <div class="inquiry-layout">
      <!-- 문의 폼 -->
      <section class="inquiry-form-card">
        <h2 class="card-title">새 문의 작성</h2>
        <div v-if="submitted" class="success-banner">
          ✅ 문의가 정상적으로 접수되었습니다. 빠르게 확인 후 답변 드리겠습니다.
        </div>
        <form @submit.prevent="handleSubmit" class="form">
          <div class="field">
            <label class="field-label" for="inq-title">제목</label>
            <input
              id="inq-title"
              v-model="title"
              type="text"
              class="field-input"
              placeholder="문의 제목을 입력하세요"
              maxlength="200"
              required
            />
          </div>
          <div class="field">
            <label class="field-label" for="inq-content">내용</label>
            <textarea
              id="inq-content"
              v-model="content"
              class="field-textarea"
              placeholder="문의 내용을 상세히 입력해 주세요."
              rows="7"
              required
            ></textarea>
          </div>
          <button type="submit" class="submit-btn" :disabled="submitting">
            <span v-if="submitting">접수 중...</span>
            <span v-else>문의 접수</span>
          </button>
        </form>
      </section>

      <!-- 내 문의 목록 -->
      <section class="my-inquiries-card">
        <h2 class="card-title">내 문의 내역</h2>
        <div v-if="loadingMy" class="loading-state"><div class="spinner"></div></div>
        <div v-else-if="myInquiries.length === 0" class="empty-hint">접수된 문의가 없습니다.</div>
        <ul v-else class="inq-list">
          <li v-for="inq in myInquiries" :key="inq.id" class="inq-item">
            <div class="inq-top">
              <span class="inq-title">{{ inq.title }}</span>
              <span class="inq-badge" :class="inq.resolved ? 'resolved' : 'pending'">
                {{ inq.resolved ? '처리 완료' : '처리 중' }}
              </span>
            </div>
            <p class="inq-content">{{ inq.content }}</p>
            <p class="inq-date">{{ formatDate(inq.createdAt) }}</p>
          </li>
        </ul>
      </section>
    </div>
  </div>
</template>

<style scoped>
.inquiry-shell {
  max-width: 900px;
  margin: 0 auto;
  padding: 48px 0 80px;
}

.eyebrow {
  font-size: 11.5px;
  font-weight: 800;
  letter-spacing: 1px;
  color: var(--ink-3, #8a93a3);
  text-transform: uppercase;
  margin: 0 0 10px;
  display: block;
}

.inquiry-head { margin-bottom: 32px; }

.inquiry-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 40px;
  letter-spacing: -1px;
  color: var(--ink, #0a0d12);
  margin: 0;
}
.dark .inquiry-title { color: #f4f6fa; }

.inquiry-layout {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: start;
}

@media (max-width: 768px) {
  .inquiry-layout { grid-template-columns: 1fr; }
}

.inquiry-form-card,
.my-inquiries-card {
  background: rgba(255,255,255,0.78);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 20px;
  padding: 28px;
  backdrop-filter: blur(32px) saturate(160%);
  -webkit-backdrop-filter: blur(32px) saturate(160%);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.85), 0 4px 20px -8px rgba(20,40,80,0.09);
}
.dark .inquiry-form-card,
.dark .my-inquiries-card {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.10);
}

.card-title {
  font-size: 15px;
  font-weight: 800;
  color: var(--ink, #0a0d12);
  margin: 0 0 20px;
}
.dark .card-title { color: #f4f6fa; }

.success-banner {
  margin-bottom: 16px;
  padding: 12px 14px;
  background: rgba(22,163,74,0.09);
  border: 1px solid rgba(22,163,74,0.22);
  border-radius: 10px;
  font-size: 13.5px;
  color: #15803d;
}
.dark .success-banner { color: #86efac; background: rgba(22,163,74,0.12); }

.form { display: flex; flex-direction: column; gap: 16px; }

.field { display: flex; flex-direction: column; gap: 6px; }
.field-label { font-size: 12.5px; font-weight: 700; color: var(--ink-2, #4a5161); }
.dark .field-label { color: #a4adbf; }

.field-input,
.field-textarea {
  padding: 10px 14px;
  border: 1px solid rgba(0,0,0,0.12);
  border-radius: 10px;
  font-size: 14px;
  color: var(--ink);
  background: rgba(255,255,255,0.9);
  outline: none;
  resize: vertical;
  transition: border-color .15s;
  font-family: inherit;
}
.field-input:focus,
.field-textarea:focus {
  border-color: #0084ff;
  box-shadow: 0 0 0 3px rgba(0,132,255,0.12);
}
.dark .field-input,
.dark .field-textarea {
  background: rgba(20,24,34,0.7);
  border-color: rgba(255,255,255,0.14);
  color: #f4f6fa;
}

.submit-btn {
  padding: 11px;
  background: #0084ff;
  color: #fff;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: background .15s;
}
.submit-btn:hover { background: #006fdd; }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.loading-state { display: flex; justify-content: center; padding: 40px 0; }
.spinner {
  width: 28px; height: 28px;
  border: 3px solid rgba(0,132,255,0.15);
  border-top-color: #0084ff;
  border-radius: 50%;
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.empty-hint { font-size: 13px; color: var(--ink-3); text-align: center; padding: 32px 0; }

.inq-list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 12px; }

.inq-item {
  padding: 14px 16px;
  background: rgba(0,0,0,0.02);
  border: 1px solid rgba(0,0,0,0.06);
  border-radius: 12px;
}
.dark .inq-item { background: rgba(255,255,255,0.03); border-color: rgba(255,255,255,0.07); }

.inq-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}
.inq-title {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--ink, #0a0d12);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dark .inq-title { color: #e0e4ef; }
.inq-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 999px;
  white-space: nowrap;
}
.inq-badge.resolved { background: rgba(99,102,241,0.12); color: #6366f1; }
.inq-badge.pending  { background: rgba(251,191,36,0.15); color: #b45309; }
.dark .inq-badge.resolved { background: rgba(99,102,241,0.22); color: #a5b4fc; }
.dark .inq-badge.pending  { background: rgba(251,191,36,0.15); color: #fcd34d; }
.inq-content {
  font-size: 13px;
  color: var(--ink-2, #4a5161);
  margin: 0 0 6px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.dark .inq-content { color: #a4adbf; }
.inq-date { font-size: 11.5px; color: var(--ink-3); margin: 0; }
</style>
