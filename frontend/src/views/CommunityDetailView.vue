<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCommunityStore } from '../stores/useCommunityStore'
import { useUserStore } from '../stores/useUserStore'

const route = useRoute()
const router = useRouter()
const store = useCommunityStore()
const userStore = useUserStore()

const newComment = ref('')
const isSubmittingComment = ref(false)

const post = computed(() => store.currentPost)

onMounted(async () => {
  await store.fetchPost(Number(route.params.id))
  await store.fetchComments(Number(route.params.id))
})

const formatDate = (iso) => {
  if (!iso) return ''
  const d = new Date(iso)
  return `${d.getFullYear()}. ${d.getMonth() + 1}. ${d.getDate()}. ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const handleReaction = (type) => {
  if (!userStore.isAuthenticated) { alert('로그인이 필요합니다.'); return }
  store.toggleReaction(post.value.postId, type)
}

const handleCommentSubmit = async () => {
  if (!newComment.value.trim()) return
  if (!userStore.isAuthenticated) { alert('로그인이 필요합니다.'); return }
  isSubmittingComment.value = true
  try {
    await store.createComment(post.value.postId, newComment.value.trim())
    newComment.value = ''
  } catch {
    alert('댓글 작성에 실패했습니다.')
  } finally {
    isSubmittingComment.value = false
  }
}

const categoryColor = {
  금융: '#0084ff', 부동산: '#7c3aed', 주식: '#059669', 환율: '#d97706', 거시경제: '#db2777', 통화정책: '#0891b2',
}
</script>

<template>
  <div class="detail-shell">
    <!-- Topbar -->
    <nav class="topbar">
      <button class="back-btn" @click="router.push('/community')">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
        커뮤니티
      </button>
    </nav>

    <!-- Loading -->
    <div v-if="store.isPostLoading" class="loading-state">
      <div class="spinner"></div>
    </div>

    <template v-else-if="post">
      <!-- Article scrap -->
      <div v-if="post.articleScrap" class="scrap-card" @click="router.push(`/articles/${post.articleScrap.articleId}`)">
        <div class="scrap-head">
          <span class="scrap-cat-badge" :style="{ background: (categoryColor[post.articleScrap.category] || '#0084ff') + '18', color: categoryColor[post.articleScrap.category] || '#0084ff' }">
            {{ post.articleScrap.category }}
          </span>
          <span class="scrap-label eyebrow" style="margin:0;">기사 스크랩</span>
        </div>
        <p class="scrap-title">{{ post.articleScrap.title }}</p>
        <p class="scrap-summary">{{ post.articleScrap.summary }}</p>
      </div>

      <!-- Post body -->
      <div class="post-body">
        <h1 class="post-title">{{ post.title }}</h1>

        <div class="post-meta">
          <span class="mini-avatar">{{ post.author.avatarInitial }}</span>
          <span class="author-name">{{ post.author.nickname }}</span>
          <span class="meta-dot">·</span>
          <span class="meta-date">{{ formatDate(post.createdAt) }}</span>
          <span class="meta-dot">·</span>
          <span class="meta-views">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
            {{ post.viewCount }}
          </span>
        </div>

        <div class="post-content">{{ post.content }}</div>

        <!-- Reaction bar -->
        <div class="reaction-bar">
          <button
            class="reaction-btn"
            :class="{ active: post.userReaction === 'like' }"
            @click="handleReaction('like')"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3H14z"/><path d="M7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3"/></svg>
            <span>{{ post.likeCount }}</span>
          </button>
          <button
            class="reaction-btn dislike"
            :class="{ active: post.userReaction === 'dislike' }"
            @click="handleReaction('dislike')"
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10 15v4a3 3 0 0 0 3 3l4-9V2H5.72a2 2 0 0 0-2 1.7l-1.38 9a2 2 0 0 0 2 2.3H10z"/><path d="M17 2h2.67A2.31 2.31 0 0 1 22 4v7a2.31 2.31 0 0 1-2.33 2H17"/></svg>
            <span>{{ post.dislikeCount }}</span>
          </button>
        </div>
      </div>

      <!-- Comments -->
      <section class="comments-section">
        <p class="section-eyebrow eyebrow">댓글 {{ post.commentCount }}개</p>

        <!-- Comment write -->
        <div class="comment-write">
          <textarea
            v-model="newComment"
            class="comment-input"
            placeholder="댓글을 입력하세요..."
            rows="3"
            @keydown.ctrl.enter="handleCommentSubmit"
          ></textarea>
          <div class="comment-foot">
            <span class="comment-hint">Ctrl+Enter로 제출</span>
            <button
              class="btn-primary comment-submit"
              :disabled="!newComment.trim() || isSubmittingComment"
              @click="handleCommentSubmit"
            >
              <span v-if="isSubmittingComment" class="spinner-sm"></span>
              {{ isSubmittingComment ? '제출 중…' : '댓글 작성' }}
            </button>
          </div>
        </div>

        <!-- Comment list -->
        <div v-if="store.isCommentLoading" class="loading-state" style="padding:40px 0;">
          <div class="spinner"></div>
        </div>
        <div v-else class="comment-list">
          <div
            v-for="comment in store.comments"
            :key="comment.commentId"
            class="comment-card"
          >
            <div class="comment-author">
              <span class="mini-avatar">{{ comment.author.avatarInitial }}</span>
              <span class="author-name">{{ comment.author.nickname }}</span>
              <span class="meta-date">{{ formatDate(comment.createdAt) }}</span>
            </div>
            <p class="comment-body">{{ comment.content }}</p>
          </div>

          <div v-if="store.comments.length === 0" class="empty-comments">
            <p style="text-align:center; color:var(--ink-3,#8a93a3); font-size:13.5px;">첫 번째 댓글을 작성해 보세요.</p>
          </div>
        </div>
      </section>
    </template>

    <div v-else class="empty">
      <p style="text-align:center; color:var(--ink-3,#8a93a3);">게시글을 찾을 수 없습니다.</p>
    </div>
  </div>
</template>

<style scoped>
.detail-shell {
  max-width: 720px;
  margin: 0 auto;
  padding: 32px 0 80px;
}

.eyebrow {
  font-family: 'Nanum Gothic', monospace;
  font-size: 11.5px;
  letter-spacing: 1px;
  color: var(--ink-3, #8a93a3);
  text-transform: uppercase;
  display: block;
}

.topbar { margin-bottom: 24px; }

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: none;
  border: none;
  color: var(--ink-2, #4a5161);
  font-size: 13.5px;
  font-weight: 500;
  cursor: pointer;
  padding: 6px 0;
  transition: color .15s;
}
.back-btn:hover { color: var(--ink, #0a0d12); }
.dark .back-btn { color: #a4adbf; }
.dark .back-btn:hover { color: #f4f6fa; }

/* Scrap card */
.scrap-card {
  padding: 20px 22px;
  background: rgba(0,132,255,0.05);
  border: 1px solid rgba(0,132,255,0.18);
  border-radius: 18px;
  margin-bottom: 20px;
  cursor: pointer;
  transition: background .15s;
}
.scrap-card:hover { background: rgba(0,132,255,0.09); }
.dark .scrap-card { background: rgba(0,132,255,0.10); border-color: rgba(0,132,255,0.25); }

.scrap-head { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.scrap-cat-badge {
  display: inline-flex;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 600;
  font-family: 'Nanum Gothic', monospace;
}

.scrap-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 700;
  font-size: 15px;
  color: #0056cc;
  margin: 0 0 6px;
}
.dark .scrap-title { color: #9BCBFF; }

.scrap-summary {
  font-size: 13px;
  color: var(--ink-2, #4a5161);
  line-height: 1.55;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.dark .scrap-summary { color: #a4adbf; }

/* Post body */
.post-body {
  padding: 28px 32px;
  background: rgba(255,255,255,0.65);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 22px;
  backdrop-filter: blur(40px) saturate(160%);
  -webkit-backdrop-filter: blur(40px) saturate(160%);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.85), 0 10px 40px -16px rgba(20,40,80,0.12);
  margin-bottom: 24px;
}
.dark .post-body {
  background: rgba(20,24,34,0.55);
  border-color: rgba(255,255,255,0.10);
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.10), 0 10px 40px -16px rgba(0,0,0,0.4);
}

.post-title {
  font-family: 'Fustat', sans-serif;
  font-weight: 800;
  font-size: 26px;
  letter-spacing: -0.6px;
  color: var(--ink, #0a0d12);
  margin: 0 0 14px;
  line-height: 1.3;
}
.dark .post-title { color: #f4f6fa; }

.post-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  padding-bottom: 18px;
  border-bottom: 1px dashed rgba(0,0,0,0.08);
  flex-wrap: wrap;
}
.dark .post-meta { border-color: rgba(255,255,255,0.10); }

.mini-avatar {
  width: 26px; height: 26px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #9CCBFF 0%, #0084ff 60%, #0a4a99 100%);
  color: #fff;
  font-family: 'Fustat', sans-serif;
  font-weight: 700;
  font-size: 11px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}

.author-name { font-size: 13px; font-weight: 600; color: var(--ink, #0a0d12); }
.dark .author-name { color: #e0e4ef; }

.meta-dot { color: var(--ink-3, #8a93a3); }

.meta-date, .meta-views {
  font-size: 12.5px;
  color: var(--ink-3, #8a93a3);
  font-family: 'Nanum Gothic', monospace;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.post-content {
  font-size: 15px;
  color: var(--ink, #0a0d12);
  line-height: 1.75;
  white-space: pre-wrap;
}
.dark .post-content { color: #d4d9e8; }

/* Reaction */
.reaction-bar {
  display: flex;
  gap: 10px;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px dashed rgba(0,0,0,0.08);
}
.dark .reaction-bar { border-color: rgba(255,255,255,0.10); }

.reaction-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 18px;
  background: rgba(255,255,255,0.6);
  border: 1.5px solid rgba(0,0,0,0.08);
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--ink-2, #4a5161);
  cursor: pointer;
  transition: all .15s;
}
.reaction-btn:hover { border-color: #0084ff; color: #0084ff; }
.reaction-btn.active { background: #E8F2FF; border-color: rgba(0,132,255,0.4); color: #0084ff; }
.dark .reaction-btn { background: rgba(20,24,34,0.55); border-color: rgba(255,255,255,0.12); color: #a4adbf; }
.dark .reaction-btn:hover { border-color: #0084ff; color: #9BCBFF; }
.dark .reaction-btn.active { background: rgba(0,132,255,0.15); border-color: rgba(0,132,255,0.4); color: #9BCBFF; }
.reaction-btn.dislike:hover { border-color: #ff6a6a; color: #ff6a6a; }
.reaction-btn.dislike.active { background: rgba(255,106,106,0.10); border-color: rgba(255,106,106,0.4); color: #ff6a6a; }

/* Comments */
.comments-section { }
.section-eyebrow { margin-bottom: 16px; }

.comment-write {
  padding: 18px;
  background: rgba(255,255,255,0.65);
  border: 1px solid rgba(0,0,0,0.07);
  border-radius: 18px;
  backdrop-filter: blur(40px);
  -webkit-backdrop-filter: blur(40px);
  margin-bottom: 16px;
}
.dark .comment-write { background: rgba(20,24,34,0.55); border-color: rgba(255,255,255,0.10); }

.comment-input {
  width: 100%;
  padding: 10px 14px;
  background: rgba(255,255,255,0.85);
  border: 1.5px solid rgba(0,0,0,0.08);
  border-radius: 10px;
  font: inherit;
  font-size: 14px;
  color: var(--ink, #0a0d12);
  outline: none;
  resize: none;
  transition: border-color .15s;
  box-sizing: border-box;
}
.comment-input:focus { border-color: #0084ff; box-shadow: 0 0 0 3px rgba(0,132,255,0.10); }
.comment-input::placeholder { color: var(--ink-3, #8a93a3); }
.dark .comment-input { background: rgba(20,24,34,0.70); border-color: rgba(255,255,255,0.12); color: #f4f6fa; }

.comment-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}
.comment-hint { font-size: 11.5px; color: var(--ink-3, #8a93a3); font-family: 'Nanum Gothic', monospace; }
.comment-submit { padding: 9px 18px; font-size: 13px; display: inline-flex; align-items: center; gap: 6px; }
.comment-submit:disabled { opacity: 0.5; cursor: default; }

.comment-list { display: flex; flex-direction: column; gap: 10px; }

.comment-card {
  padding: 16px 18px;
  background: rgba(255,255,255,0.55);
  border: 1px solid rgba(0,0,0,0.06);
  border-radius: 14px;
}
.dark .comment-card { background: rgba(20,24,34,0.45); border-color: rgba(255,255,255,0.08); }

.comment-author {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.comment-body {
  font-size: 14px;
  color: var(--ink, #0a0d12);
  line-height: 1.65;
  margin: 0;
  white-space: pre-wrap;
}
.dark .comment-body { color: #d4d9e8; }

.loading-state { display: flex; justify-content: center; padding: 80px 0; }
.spinner {
  width: 32px; height: 32px;
  border: 3px solid rgba(0,132,255,0.15);
  border-top-color: #0084ff;
  border-radius: 50%;
  animation: spin .7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.spinner-sm {
  width: 14px; height: 14px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin .65s linear infinite;
}

@media (max-width: 640px) {
  .detail-shell { padding: 24px 0 60px; }
  .post-body { padding: 20px 18px; }
  .post-title { font-size: 21px; }
}
</style>
