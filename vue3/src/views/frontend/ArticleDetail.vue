<template>
  <div class="healing-article-detail-page">
    <div class="ambient-light light-1"></div>
    <div class="ambient-light light-2"></div>

    <div class="container">
      <header class="page-header">
        <div class="header-left">
          <div class="seedling-icon-box">
            <i class="fas fa-book-open"></i>
          </div>
          <div class="title-group">
            <h2>知识文章详情</h2>
            <p v-if="article">{{ article.categoryName }} · 深度解读心理健康知识</p>
            <p v-else>加载中...</p>
          </div>
        </div>
        <div class="header-actions">
          <button type="button" class="glass-outline-btn" @click="goBack">
            <i class="fas fa-arrow-left"></i>
            返回知识库
          </button>
        </div>
      </header>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="8" animated />
      </div>

      <!-- 详情内容 -->
      <div v-else-if="article" class="detail-content">
        <div class="content-layout">
          <div class="article-detail-section">
            <!-- 文章信息卡片 -->
            <div class="glass-card article-info-card">
              <h3 class="card-title">
                <i class="fas fa-newspaper"></i>
                文章信息
              </h3>
              <div class="article-info-layout">
                <div class="article-info-content">
                  <div class="article-meta">
                    <span class="category-tag">{{ article.categoryName }}</span>
                    <span class="meta-item">
                      <i class="fas fa-calendar"></i>
                      {{ formatDate(article.publishedAt) }}
                    </span>
                    <span class="meta-item">
                      <i class="fas fa-clock"></i>
                      约 {{ getReadTime(article.content) }} 分钟阅读
                    </span>
                  </div>
                  <h1 class="article-title">{{ article.title }}</h1>
                  <div class="article-summary" v-if="article.summary">
                    <div class="summary-content">
                      <i class="fas fa-quote-left"></i>
                      <p>{{ article.summary }}</p>
                    </div>
                  </div>
                  <div class="article-stats">
                    <div class="stats-left">
                      <span class="author">
                        <i class="fas fa-user-md"></i>
                        {{ article.authorName || '心理健康助手' }}
                      </span>
                      <span class="read-count">
                        <i class="fas fa-eye"></i>
                        {{ formatReadCount(article.readCount) }} 次阅读
                      </span>
                    </div>
                    <div class="stats-right">
                      <button
                        type="button"
                        class="flat-btn"
                        :class="article.isFavorited ? 'primary-mode favorited' : 'primary-mode'"
                        @click="toggleFavorite"
                      >
                        <i :class="article.isFavorited ? 'fas fa-heart' : 'far fa-heart'"></i>
                        {{ article.isFavorited ? '已收藏' : '收藏' }}
                      </button>
                      <button type="button" class="flat-btn ghost-mode" @click="shareArticle">
                        <i class="fas fa-share-alt"></i>
                        分享
                      </button>
                    </div>
                  </div>
                </div>
                <div class="article-cover-section" v-if="article.coverImage">
                  <img
                    :src="article.coverImage"
                    :alt="article.title"
                    @error="handleImageError"
                    class="cover-image"
                  />
                </div>
              </div>
            </div>

            <!-- 正文内容卡片 -->
            <div class="glass-card content-card">
              <h3 class="card-title">
                <i class="fas fa-file-alt"></i>
                正文内容
              </h3>
              <div class="content-wrapper" v-html="formatContent(article.content)"></div>
              <div class="tags-content" v-if="article.tags">
                <h4 class="tags-title">
                  <i class="fas fa-tags"></i>
                  相关标签
                </h4>
                <div class="tags-list">
                  <span
                    v-for="tag in getTagArray(article.tags)"
                    :key="tag"
                    class="tag-item"
                  >
                    <i class="fas fa-tag"></i>
                    {{ tag }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <aside class="sidebar-section">
            <div class="glass-card related-articles-card" v-if="relatedArticles.length > 0">
              <h3 class="card-title">
                <i class="fas fa-lightbulb"></i>
                相关文章推荐
              </h3>
              <div class="related-list">
                <div
                  v-for="relatedArticle in relatedArticles"
                  :key="relatedArticle.id"
                  class="related-item"
                  @click="goToArticle(relatedArticle.id)"
                >
                  <div class="related-image">
                    <img
                      :src="relatedArticle.coverImage || defaultCover"
                      :alt="relatedArticle.title"
                      @error="handleImageError"
                    />
                  </div>
                  <div class="related-content">
                    <h4>{{ relatedArticle.title }}</h4>
                    <p>{{ getAutoSummary(relatedArticle.content) }}</p>
                    <div class="related-meta">
                      <span><i class="fas fa-eye"></i>{{ formatReadCount(relatedArticle.readCount) }}</span>
                      <span><i class="fas fa-calendar"></i>{{ formatDate(relatedArticle.publishedAt) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="glass-card tip-card">
              <div class="tip-icon"><i class="fas fa-info-circle"></i></div>
              <div class="tip-text">
                <h4>温馨提示</h4>
                <div class="tips-content">
                  <div class="tip-item">
                    <i class="fas fa-book-reader"></i>
                    定期阅读心理健康知识有助于提升心理素养
                  </div>
                  <div class="tip-item">
                    <i class="fas fa-heart"></i>
                    收藏喜欢的文章，方便日后查阅
                  </div>
                  <div class="tip-item">
                    <i class="fas fa-share-alt"></i>
                    分享有价值的内容，帮助更多人受益
                  </div>
                  <div class="tip-item">
                    <i class="fas fa-user-friends"></i>
                    如需专业帮助，请及时寻求心理咨询
                  </div>
                </div>
              </div>
            </div>
          </aside>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else class="error-state">
        <div class="empty-icon"><i class="fas fa-exclamation-circle"></i></div>
        <h3>文章不存在</h3>
        <p>抱歉，您访问的文章不存在或已被删除</p>
        <button type="button" class="flat-btn primary-mode" @click="goBack">返回知识库</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { readArticle } from '@/api/knowledgeArticle'
import { favoriteArticle, unfavoriteArticle, checkFavoriteStatus } from '@/api/userFavorite'
import { formatDate } from '@/utils/dateUtils'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const article = ref(null)
const relatedArticles = ref([])
const userRating = ref(null)

const defaultCover = 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'

const isLoggedIn = computed(() => localStorage.getItem('token'))

const fetchArticle = async () => {
  const articleId = route.params.id
  if (!articleId) {
    ElMessage.error('文章ID不存在')
    return
  }
  loading.value = true
  try {
    const response = await readArticle(articleId)
    if (response) {
      article.value = response
      if (isLoggedIn.value) {
        try {
          const favoriteStatus = await checkFavoriteStatus(articleId)
          article.value.isFavorited = favoriteStatus
        } catch (error) {
          article.value.isFavorited = false
        }
      }
      await fetchRelatedArticles()
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
    ElMessage.error('获取文章详情失败')
  } finally {
    loading.value = false
  }
}

const fetchRelatedArticles = async () => {
  if (!article.value) return
  try {
    relatedArticles.value = []
  } catch (error) {
    console.error('获取相关文章失败:', error)
  }
}

const toggleFavorite = async () => {
  if (!isLoggedIn.value) {
    ElMessageBox.confirm('需要登录才能收藏文章，是否前往登录？', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'info'
    }).then(() => router.push('/auth/login')).catch(() => {})
    return
  }
  if (!article.value) return
  try {
    if (article.value.isFavorited) {
      await unfavoriteArticle(article.value.id)
      article.value.isFavorited = false
      ElMessage.success('已取消收藏')
    } else {
      await favoriteArticle(article.value.id)
      article.value.isFavorited = true
      ElMessage.success('收藏成功')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败，请稍后重试')
  }
}

const shareArticle = async () => {
  if (navigator.share) {
    try {
      await navigator.share({
        title: article.value.title,
        text: article.value.summary,
        url: window.location.href
      })
    } catch (error) {
      if (error.name !== 'AbortError') fallbackShare()
    }
  } else {
    fallbackShare()
  }
}

const fallbackShare = () => {
  navigator.clipboard.writeText(window.location.href).then(() => {
    ElMessage.success('文章链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.info('请手动复制当前页面链接进行分享')
  })
}

const goBack = () => {
  if (window.history.length > 1) {
    router.go(-1)
  } else {
    router.push('/knowledge')
  }
}

const goToArticle = (articleId) => {
  router.push(`/knowledge/article/${articleId}`)
}

const getReadTime = (content) => {
  if (!content) return 5
  return Math.max(1, Math.ceil(content.length / 300))
}

const formatReadCount = (count) => {
  if (!count) return '0'
  if (count < 1000) return count.toString()
  if (count < 10000) return (count / 1000).toFixed(1) + 'k'
  return (count / 10000).toFixed(1) + 'w'
}

const getTagArray = (tags) => {
  if (!tags) return []
  return tags.split(',').map(tag => tag.trim()).filter(tag => tag)
}

const getAutoSummary = (content) => {
  if (!content) return ''
  const plainText = content.replace(/<[^>]+>/g, '')
  return plainText.length > 100 ? plainText.substring(0, 100) + '...' : plainText
}

const formatContent = (content) => {
  if (!content) return ''
  return content
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
}

const handleImageError = (event) => {
  event.target.src = defaultCover
}

onMounted(() => {
  fetchArticle()
})
</script>

<style scoped>
/* 与 Home / EmotionDiary / Knowledge 一致的疗愈风 */
:root {
  --bg-page: #F7F9FC;
  --glass-bg: rgba(255, 255, 255, 0.65);
  --glass-border: rgba(255, 255, 255, 0.9);
  --text-main: #334155;
  --text-sub: #64748B;
  --primary: #8B5CF6;
  --primary-light: #EEF2FF;
  --shadow-soft: 0 8px 32px rgba(31, 38, 135, 0.04);
  --radius-card: 24px;
}

* { box-sizing: border-box; }

.healing-article-detail-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  color: var(--text-main);
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", sans-serif;
  position: relative;
  overflow-x: hidden;
  padding-bottom: 60px;
}

.ambient-light {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.4;
  z-index: 0;
  pointer-events: none;
  animation: float 15s infinite alternate;
}
.light-1 {
  width: 40vw;
  height: 40vw;
  background: #E0E7FF;
  top: -5%;
  left: -5%;
}
.light-2 {
  width: 50vw;
  height: 50vw;
  background: #F5F3FF;
  bottom: 10%;
  right: -10%;
  animation-delay: -5s;
}
@keyframes float {
  0% { transform: translate(0, 0); }
  100% { transform: translate(5%, 5%); }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  position: relative;
  z-index: 10;
}

/* ==================== 页面头部 ==================== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40px 0 32px 0;
  flex-wrap: wrap;
  gap: 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.seedling-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: linear-gradient(135deg, #eff2ff, #eff2ff);
  color: var(--primary);
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.2);
}
.title-group h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: var(--text-main);
}
.title-group p {
  font-size: 13px;
  color: var(--text-sub);
  margin: 0;
}
.glass-outline-btn {
  padding: 10px 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid var(--glass-border);
  color: var(--text-main);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: 0.3s;
  box-shadow: var(--shadow-soft);
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.glass-outline-btn:hover {
  background: white;
  color: var(--primary);
  border-color: var(--primary);
}

/* ==================== 加载 ==================== */
.loading-container {
  padding: 24px 0;
}

/* ==================== 内容布局 ==================== */
.detail-content {
  padding-bottom: 40px;
}
.content-layout {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
  align-items: start;
}
.article-detail-section,
.sidebar-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.glass-card {
  background: var(--glass-bg);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-soft);
  padding: 28px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.glass-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(31, 38, 135, 0.06);
}
.card-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 20px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-main);
}
.card-title i {
  color: var(--primary);
  font-size: 18px;
}

/* 文章信息区 */
.article-info-layout {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}
.article-info-content {
  flex: 1;
  min-width: 0;
}
.article-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.category-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  background: var(--primary-light);
  color: var(--primary);
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--text-sub);
  font-size: 13px;
}
.meta-item i {
  color: var(--primary);
  opacity: 0.8;
}
.article-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-main);
  margin: 0 0 16px 0;
  line-height: 1.35;
}
.article-summary {
  margin-bottom: 20px;
}
.summary-content {
  background: var(--primary-light);
  border-left: 4px solid var(--primary);
  padding: 16px 20px;
  border-radius: 0 14px 14px 0;
  position: relative;
}
.summary-content i {
  color: var(--primary);
  font-size: 18px;
  position: absolute;
  top: 16px;
  left: 20px;
  opacity: 0.4;
}
.summary-content p {
  color: var(--text-main);
  font-size: 15px;
  line-height: 1.6;
  margin: 0;
  padding-left: 28px;
}
.article-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}
.stats-left {
  display: flex;
  align-items: center;
  gap: 20px;
}
.author,
.read-count {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: var(--text-sub);
  font-size: 13px;
}
.author i,
.read-count i {
  color: var(--primary);
  opacity: 0.8;
}
.stats-right {
  display: flex;
  gap: 10px;
}
.flat-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}
.flat-btn.primary-mode {
  background: white;
  border: 1px solid var(--primary);
  color: var(--primary);
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.1);
}
.flat-btn.primary-mode:hover {
  background: var(--primary-light);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.2);
}
.flat-btn.primary-mode.favorited {
  color: #DC2626;
  border-color: #DC2626;
}
.flat-btn.primary-mode.favorited:hover {
  background: #FEF2F2;
}
.flat-btn.ghost-mode {
  background: transparent;
  border: 1px solid #E2E8F0;
  color: var(--text-sub);
}
.flat-btn.ghost-mode:hover {
  background: #F1F5F9;
  color: var(--text-main);
}

.article-cover-section {
  width: 280px;
  flex-shrink: 0;
}
.article-cover-section .cover-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 16px;
  box-shadow: var(--shadow-soft);
}

/* 正文卡片 */
.content-wrapper {
  font-size: 16px;
  line-height: 1.75;
  color: var(--text-main);
}
.content-wrapper :deep(p) {
  margin-bottom: 1rem;
}
.content-wrapper :deep(h1),
.content-wrapper :deep(h2),
.content-wrapper :deep(h3),
.content-wrapper :deep(h4),
.content-wrapper :deep(h5),
.content-wrapper :deep(h6) {
  margin: 1.25rem 0 0.6rem;
  color: var(--text-main);
  font-weight: 600;
}
.content-wrapper :deep(h2) {
  font-size: 1.35rem;
  border-bottom: 2px solid #E2E8F0;
  padding-bottom: 0.5rem;
}
.content-wrapper :deep(h3) { font-size: 1.2rem; }
.content-wrapper :deep(ul),
.content-wrapper :deep(ol) {
  padding-left: 1.5rem;
  margin-bottom: 1rem;
}
.content-wrapper :deep(li) { margin-bottom: 0.25rem; }
.content-wrapper :deep(strong) {
  font-weight: 600;
  color: var(--text-main);
}
.content-wrapper :deep(em) {
  font-style: italic;
  color: var(--text-sub);
}
.content-wrapper :deep(blockquote) {
  border-left: 4px solid var(--primary);
  background: var(--primary-light);
  padding: 1rem 1.25rem;
  margin: 1rem 0;
  border-radius: 0 12px 12px 0;
}

.tags-content {
  margin-top: 28px;
  padding-top: 20px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}
.tags-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
}
.tags-title i {
  color: var(--primary);
}
.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: var(--primary-light);
  border: 1px solid rgba(139, 92, 246, 0.2);
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
  color: var(--primary);
}
.tag-item i {
  opacity: 0.8;
}

/* 相关文章 */
.related-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.related-item {
  display: flex;
  gap: 12px;
  cursor: pointer;
  padding: 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid transparent;
  transition: 0.2s;
}
.related-item:hover {
  background: rgba(255, 255, 255, 0.9);
  border-color: var(--primary);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.08);
}
.related-image {
  width: 90px;
  height: 64px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
}
.related-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.related-content {
  flex: 1;
  min-width: 0;
}
.related-content h4 {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-main);
  margin: 0 0 6px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.related-content p {
  color: var(--text-sub);
  font-size: 12px;
  line-height: 1.4;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.related-meta {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: var(--text-sub);
}
.related-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 温馨提示卡片（绿色愈疗风） */
.tip-card {
  background: linear-gradient(135deg, #F0FDF4, #DCFCE7);
  border-color: #A7F3D0;
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
}
.tip-icon {
  font-size: 24px;
  color: #059669;
}
.tip-text h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #065F46;
}
.tips-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 12px;
  color: #047857;
  line-height: 1.5;
}
.tip-item i {
  color: #059669;
  margin-top: 2px;
  flex-shrink: 0;
}

/* 错误状态 */
.error-state {
  text-align: center;
  padding: 80px 24px;
}
.error-state .empty-icon {
  font-size: 56px;
  color: #F87171;
  margin-bottom: 20px;
}
.error-state h3 {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 10px 0;
  color: var(--text-main);
}
.error-state p {
  font-size: 14px;
  color: var(--text-sub);
  margin: 0 0 24px 0;
}

/* 响应式 */
@media (max-width: 1024px) {
  .content-layout {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .article-info-layout {
    flex-direction: column;
    gap: 20px;
  }
  .article-cover-section {
    width: 100%;
  }
  .article-cover-section .cover-image {
    height: 220px;
  }
  .article-title {
    font-size: 22px;
  }
  .article-stats {
    flex-direction: column;
    align-items: flex-start;
  }
  .stats-right {
    width: 100%;
  }
  .flat-btn {
    flex: 1;
  }
  .related-item {
    flex-direction: column;
  }
  .related-image {
    width: 100%;
    height: 120px;
  }
}
</style>