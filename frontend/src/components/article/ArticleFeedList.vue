<script setup>
import ArticleCard from './ArticleCard.vue'
import LoadingSpinner from '../common/LoadingSpinner.vue'
import EmptyState from '../common/EmptyState.vue'
import BaseButton from '../common/BaseButton.vue'

const props = defineProps({
  articles: {
    type: Array,
    required: true
  },
  isLoading: {
    type: Boolean,
    default: false
  },
  hasMore: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['load-more', 'card-click'])

const handleLoadMore = () => {
  emit('load-more')
}

const handleCardClick = (id) => {
  emit('card-click', id)
}
</script>

<template>
  <div class="space-y-10">
    <!-- Empty State -->
    <EmptyState 
      v-if="articles.length === 0 && !isLoading" 
      message="선택한 카테고리나 필터에 맞는 경제 뉴스가 아직 없습니다."
    />

    <!-- Grid List -->
    <div 
      v-else
      class="grid md:grid-cols-2 lg:grid-cols-3 gap-6"
    >
      <ArticleCard 
        v-for="article in articles" 
        :key="article.articleId"
        :article="article"
        @click="handleCardClick"
      />
    </div>

    <!-- Loading Spinner -->
    <div v-if="isLoading" class="flex justify-center py-6">
      <LoadingSpinner />
    </div>

    <!-- Load More Button -->
    <div 
      v-if="hasMore && !isLoading" 
      class="flex justify-center pt-4"
    >
      <BaseButton 
        variant="outline" 
        @click="handleLoadMore"
        class="w-full max-w-xs py-3 border-white/10 hover:border-primary-500/30 hover:bg-white/5 text-slate-300 rounded-xl"
      >
        더 보기
      </BaseButton>
    </div>
  </div>
</template>
