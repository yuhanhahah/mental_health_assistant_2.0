<template>
  <div 
    class="article-card"
    @click="goToArticle"
  >
    <div class="article-image">
      <img 
        :src="article.coverImage || defaultCover" 
        :alt="article.title"
        @error="handleImageError"
      >
      <div class="image-overlay">
        <i class="fas fa-eye"></i>
      </div>
    </div>
    
    <div class="article-content">
      <div class="article-meta">
        <el-tag 
          :style="{ backgroundColor: getCategoryTagBg(article.categoryName), borderColor: getCategoryTagBorder(article.categoryName), color: getCategoryTagColor(article.categoryName) }"
          size="small"
          effect="plain"
        >
          {{ article.categoryName }}
        </el-tag>
        <span class="read-time">{{ getReadTime(article.content) }}分钟阅读</span>
      </div>
      
      <h3 class="article-title">{{ article.title }}</h3>
      
      <p class="article-summary">
        {{ article.summary || getAutoSummary(article.content) }}
      </p>
      
      <div class="article-footer">
        <div class="author-info">
          <span class="author-avatar">
            <i class="fas fa-user-md"></i>
          </span>
          <span class="author-name">{{ article.authorName || '心理健康助手' }}</span>
        </div>
        
        <div class="article-actions">
          <span class="action-item date-action">
            <i class="far fa-calendar"></i>
            <span>{{ formatDate(article.publishedAt) }}</span>
          </span>
          <span class="action-item">
            <i class="far fa-eye"></i>
            <span>{{ formatReadCount(article.readCount) }}</span>
          </span>
          <span 
            class="action-item favorite-action"
            :class="{ favorited: article.isFavorited }"
            @click.stop="toggleFavorite"
          >
            <i :class="article.isFavorited ? 'fas fa-heart' : 'far fa-heart'"></i>
            <span>{{ article.favoriteCount || 0 }}</span>
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { favoriteArticle, unfavoriteArticle } from '@/api/userFavorite'
import { formatDate } from '@/utils/dateUtils'

// Props
const props = defineProps({
  article: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['favorite-changed'])

const router = useRouter()

// 默认封面图片
const defaultCover = 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'

// 计算属性
const isLoggedIn = computed(() => {
  return localStorage.getItem('token')
})

// 方法
const goToArticle = () => {
  router.push(`/knowledge/article/${props.article.id}`)
}

const toggleFavorite = async () => {
  if (!isLoggedIn.value) {
    ElMessageBox.confirm('需要登录才能收藏文章，是否前往登录？', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'info'
    }).then(() => {
      router.push('/auth/login')
    }).catch(() => {})
    return
  }

  try {
    if (props.article.isFavorited) {
      await unfavoriteArticle(props.article.id)
      props.article.isFavorited = false
      props.article.favoriteCount = Math.max(0, (props.article.favoriteCount || 1) - 1)
      ElMessage.success('已取消收藏')
    } else {
      await favoriteArticle(props.article.id)
      props.article.isFavorited = true
      props.article.favoriteCount = (props.article.favoriteCount || 0) + 1
      ElMessage.success('收藏成功')
    }
    
    // 通知父组件收藏状态变化
    emit('favorite-changed', props.article)
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

// 工具方法
// 分类标签背景色
const getCategoryTagBg = (name) => ({
  '情绪管理': 'rgba(244, 114, 182, 0.15)',
  '焦虑抑郁': 'rgba(74, 89, 223, 0.15)',
  '工作压力': 'rgba(251, 146, 60, 0.15)',
  '压力缓解': 'rgba(249, 115, 22, 0.15)',
  '人际关系': 'rgba(96, 165, 250, 0.15)',
  '睡眠健康': 'rgba(167, 139, 250, 0.15)',
  '儿童心理': 'rgba(52, 211, 153, 0.15)',
  '创伤康复': 'rgba(251, 113, 133, 0.15)',
  '放松技巧': 'rgba(74, 222, 128, 0.15)',
  '心理健康基础': 'rgba(59, 130, 246, 0.15)',
  '情绪调节': 'rgba(236, 72, 153, 0.15)',
  '人际沟通': 'rgba(6, 182, 212, 0.15)',
  '自我成长': 'rgba(34, 197, 94, 0.15)'
}[name] || 'rgba(59, 130, 246, 0.15)')

// 分类标签边框色
const getCategoryTagBorder = (name) => ({
  '情绪管理': '#f472b6',
  '焦虑抑郁': '#818cf8',
  '工作压力': '#fb923c',
  '压力缓解': '#f97316',
  '人际关系': '#60a5fa',
  '睡眠健康': '#a78bfa',
  '儿童心理': '#34d399',
  '创伤康复': '#fb7185',
  '放松技巧': '#4ade80',
  '心理健康基础': '#3b82f6',
  '情绪调节': '#ec4899',
  '人际沟通': '#06b6d4',
  '自我成长': '#22c55e'
}[name] || '#3b82f6')

// 分类标签文字色
const getCategoryTagColor = (name) => ({
  '情绪管理': '#db2777',
  '焦虑抑郁': '#6366f1',
  '工作压力': '#ea580c',
  '压力缓解': '#c2410c',
  '人际关系': '#2563eb',
  '睡眠健康': '#7c3aed',
  '儿童心理': '#059669',
  '创伤康复': '#e11d48',
  '放松技巧': '#16a34a',
  '心理健康基础': '#2563eb',
  '情绪调节': '#db2777',
  '人际沟通': '#0891b2',
  '自我成长': '#16a34a'
}[name] || '#2563eb')

const getReadTime = (content) => {
  if (!content) return 5
  const wordCount = content.length
  return Math.max(1, Math.ceil(wordCount / 300))
}

const getAutoSummary = (content) => {
  if (!content) return ''
  const plainText = content.replace(/<[^>]+>/g, '')
  return plainText.length > 120 ? plainText.substring(0, 120) + '...' : plainText
}

const formatReadCount = (count) => {
  if (!count) return '0'
  if (count < 1000) return count.toString()
  if (count < 10000) return (count / 1000).toFixed(1) + 'k'
  return (count / 10000).toFixed(1) + 'w'
}

const handleImageError = (event) => {
  event.target.src = defaultCover
}
</script>

<style scoped>
.article-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.4s ease;
  display: flex;
  min-height: 180px;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 40px rgba(245, 158, 11, 0.15);
}

.article-card:hover .image-overlay {
  opacity: 1;
}

.article-image {
  width: 240px;
  height: 100%;
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
}

.article-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.article-card:hover .article-image img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-overlay i {
  color: white;
  font-size: 1.5rem;
}

.article-content {
  flex: 1;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}

.read-time {
  font-size: 0.75rem;
  color: #6b7280;
}

.article-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.75rem;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.3s ease;
}

.article-card:hover .article-title {
  color: #f59e0b;
}

.article-summary {
  color: #6b7280;
  font-size: 0.875rem;
  line-height: 1.5;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 0.75rem;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 0.75rem;
  border-top: 1px solid #f0f0f0;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.author-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.75rem;
}

.author-avatar i {
  margin: 0;
}

.author-name {
  font-size: 0.8rem;
  color: #6b7280;
  font-weight: 500;
}

.article-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.35rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  color: #6b7280;
  background: #f5f5f5;
  transition: all 0.3s ease;
}

.action-item i {
  font-size: 0.7rem;
}

.date-action i {
  color: #a78bfa;
}

.action-item:hover {
  background: #e8e8e8;
  color: #374151;
}

.action-item.favorited {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.favorite-action {
  cursor: pointer;
}

.favorite-action:hover {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

.favorite-action.favorited i {
  color: #ef4444;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-card {
    flex-direction: column;
    height: auto;
  }
  
  .article-image {
    width: 100%;
    height: 150px;
  }
  
  .article-content {
    padding: 1rem;
  }
  
  .article-footer {
    flex-direction: column;
    gap: 0.75rem;
    align-items: flex-start;
  }
  
  .article-actions {
    align-self: flex-end;
  }
  
  .action-item {
    padding: 0.25rem 0.5rem;
    font-size: 0.7rem;
  }
}
</style>