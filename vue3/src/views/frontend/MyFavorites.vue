<template>
  <div class="healing-favorites-page">
    <div class="ambient-light light-purple"></div>
    <div class="ambient-light light-green"></div>

    <div class="container">
      <header class="page-header">
        <div class="header-content">
          <div class="header-left">
            <div class="seedling-icon-box">
              <i class="fas fa-heart"></i>
            </div>
            <div class="header-text">
              <h2>我的收藏</h2>
              <p>{{ favoriteCount != null ? favoriteCount : 0 }} 篇收藏文章 · 把触动收藏成你的心灵工具箱</p>
            </div>
          </div>

          <div class="header-actions">
            <button class="glass-btn" @click="refreshList">
              <i class="fas fa-sync-alt"></i> 刷新
            </button>
            <button class="glass-btn primary-purple" @click="goToKnowledge">
              <i class="fas fa-book-open"></i> 去知识库
            </button>
          </div>
        </div>
      </header>

      <div class="content-area">
        <div class="article-list" v-loading="loading">
          <div v-if="articles.length > 0" class="articles-grid">
            <div
              v-for="article in articles"
              :key="article.id"
              class="glass-card favorite-card"
              @click="viewArticle(article.id)"
            >
              <div class="article-cover" @click.stop="viewArticle(article.id)">
                <img
                  v-if="article.coverImage"
                  :src="resolveCoverUrl(article.coverImage)"
                  :alt="article.title"
                  @error="handleImageError"
                />
                <div v-else class="default-cover">
                  <i class="fas fa-file-alt"></i>
                </div>
                <div class="cover-overlay">
                  <i class="fas fa-eye"></i>
                  <span>查看详情</span>
                </div>
              </div>

              <div class="article-content">
                <h3 class="article-title" @click.stop="viewArticle(article.id)">
                  {{ article.title }}
                </h3>

                <p class="article-summary">
                  {{ article.summary || getAutoSummary(article.content) }}
                </p>

                <div class="article-meta">
                  <div class="meta-row">
                    <span class="meta-item">
                      <i class="fas fa-calendar"></i>
                      收藏于 {{ formatDateString(article.favoriteTime) }}
                    </span>
                    <span class="meta-item">
                      <i class="fas fa-eye"></i>
                      {{ formatReadCount(article.readCount) }} 次阅读
                    </span>
                  </div>
                </div>

                <div class="article-actions">
                  <button class="flat-btn primary-mode" @click.stop="viewArticle(article.id)">
                    <i class="fas fa-eye"></i> 查看
                  </button>
                  <button class="flat-btn ghost-mode danger-btn" @click.stop="confirmRemoveFavorite(article)">
                    <i class="fas fa-heart-broken"></i> 取消收藏
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="!loading && articles.length === 0" class="empty-state glass-card">
            <div class="empty-icon"><i class="fas fa-heart-broken"></i></div>
            <p class="empty-main">暂无收藏文章</p>
            <p class="empty-sub">
              去
              <router-link to="/knowledge" class="text-link">知识库</router-link>
              逛逛，把触动收藏起来吧
            </p>
          </div>
        </div>

        <div class="pagination-wrapper" v-if="total > 0">
          <el-pagination
            :current-page="searchParams.currentPage"
            :page-size="searchParams.size"
            :page-sizes="[6, 12, 18, 24]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
            class="healing-pagination"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserFavoritePage, getUserFavoriteCount, unfavoriteArticle } from '@/api/userFavorite'
import { formatDate } from '@/utils/dateUtils'

const router = useRouter()

const loading = ref(false)
const articles = ref([])
const total = ref(0)
const favoriteCount = ref(null)

const searchParams = reactive({
  currentPage: 1,
  size: 8
})

const DEFAULT_COVER =
  'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'

const formatDateString = (dateString) => {
  if (!dateString) return ''
  return formatDate(new Date(dateString))
}

const fetchFavorites = async () => {
  loading.value = true
  try {
    await getUserFavoritePage(searchParams, {
      onSuccess: (res) => {
        articles.value = res.records || []
        total.value = res.total || 0
        searchParams.currentPage = res.current || 1
        searchParams.size = res.size || 8
      }
    })
  } catch (error) {
    console.error('获取收藏列表失败:', error)
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
}

const fetchFavoriteCount = async () => {
  try {
    await getUserFavoriteCount({
      onSuccess: (res) => {
        favoriteCount.value = res
      }
    })
  } catch (error) {
    console.error('获取收藏总数失败:', error)
  }
}

const getAutoSummary = (content) => {
  if (!content) return ''
  const plainText = content.replace(/<[^>]+>/g, '')
  return plainText.length > 150 ? plainText.substring(0, 150) + '...' : plainText
}

const formatReadCount = (count) => {
  if (!count) return '0'
  if (count < 1000) return count.toString()
  if (count < 10000) return (count / 1000).toFixed(1) + 'k'
  return (count / 10000).toFixed(1) + 'w'
}

/**
 * 统一封面 URL，避免出现相对路径导致“第一张正常，后面错位”的问题
 */
const resolveCoverUrl = (coverImage) => {
  if (!coverImage) return ''
  const url = String(coverImage).trim()
  if (!url) return ''
  if (/^(https?:)?\/\//i.test(url)) return url
  if (/^data:/i.test(url)) return url
  if (url.startsWith('/')) return url
  return `/${url}`
}

const handleImageError = (event) => {
  const img = event?.target
  if (!img) return
  if (img.dataset.fallbackApplied === '1') return
  img.dataset.fallbackApplied = '1'
  img.src = DEFAULT_COVER
}

const handlePageChange = (page) => {
  searchParams.currentPage = page
  fetchFavorites()
}

const handleSizeChange = (size) => {
  searchParams.size = size
  searchParams.currentPage = 1
  fetchFavorites()
}

const viewArticle = (articleId) => {
  router.push(`/knowledge/article/${articleId}`)
}

const confirmRemoveFavorite = async (article) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消收藏文章"${article.title}"吗？`,
      '取消收藏',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await removeFavorite(article.id)
  } catch (error) {}
}

const removeFavorite = async (articleId) => {
  try {
    await unfavoriteArticle(articleId, {
      onSuccess: () => {
        ElMessage.success('取消收藏成功')
        fetchFavorites()
        fetchFavoriteCount()
      }
    })
  } catch (error) {
    console.error('取消收藏失败:', error)
    ElMessage.error('取消收藏失败')
  }
}

const goToKnowledge = () => {
  router.push('/knowledge')
}

const refreshList = () => {
  fetchFavorites()
  fetchFavoriteCount()
}

onMounted(() => {
  fetchFavorites()
  fetchFavoriteCount()
})
</script>

<style scoped>
/* 与 Home / EmotionDiaryDetail / ArticleDetail 统一的疗愈风 */
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

.healing-favorites-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  color: var(--text-main);
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", sans-serif;
  position: relative;
  overflow: hidden;
  padding-bottom: 40px;
}

/* 环境光（与 Home / EmotionDiaryDetail 对齐：紫+绿） */
.ambient-light {
  position: absolute;
  border-radius: 50%;
  filter: blur(140px);
  opacity: 0.4;
  z-index: 0;
  pointer-events: none;
  animation: float 15s infinite alternate ease-in-out;
}
.light-purple {
  width: 50vw;
  height: 50vw;
  background: #E0E7FF;
  top: -10%;
  left: -10%;
}
.light-green {
  width: 45vw;
  height: 45vw;
  background: #D1FAE5;
  bottom: 0%;
  right: -5%;
  animation-delay: -5s;
}
@keyframes float {
  0% { transform: translate(0,0); }
  100% { transform: translate(5%, 5%); }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  position: relative;
  z-index: 10;
}

/* Header（参考 EmotionDiaryDetail 的结构） */
.page-header {
  padding: 40px 0 28px 0;
}
.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
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
  font-size: 22px;
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.2);
}
.header-text h2 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 4px 0;
  color: var(--text-main);
}
.header-text p {
  font-size: 13px;
  color: var(--text-sub);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

/* EmotionDiaryDetail 同款 glass-btn */
.glass-btn {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 18px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(255, 255, 255, 0.9);
  color: var(--text-main);
  box-shadow: var(--shadow-soft);
}
.glass-btn:hover {
  background: white;
  color: var(--primary);
  transform: translateY(-2px);
}
.glass-btn.primary-purple {
  background: linear-gradient(135deg, #A5B4FC, var(--primary));
  color: #000;
  border: none;
  box-shadow: 0 8px 24px rgba(139, 92, 246, 0.18);
}
.glass-btn.primary-purple:hover {
  box-shadow: 0 12px 32px rgba(139, 92, 246, 0.28);
}

.content-area { padding-bottom: 24px; }

.glass-card {
  background: var(--glass-bg);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-soft);
}

/* 列表网格 */
.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(290px, 1fr));
  gap: 24px;
}

.favorite-card {
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.favorite-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 40px rgba(31, 38, 135, 0.08);
}

/* 封面 */
.article-cover {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
}
.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}
.favorite-card:hover .article-cover img {
  transform: scale(1.05);
}
.default-cover {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #E0E7FF, #C4B5FD);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
  font-size: 2.5rem;
}
.cover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(139, 92, 246, 0.6);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
}
.favorite-card:hover .cover-overlay { opacity: 1; }

/* 内容区 */
.article-content {
  padding: 18px 20px 20px 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.article-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.article-summary {
  margin: 0;
  font-size: 13px;
  color: var(--text-sub);
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  margin-top: 4px;
  padding-top: 12px;
  border-top: 1px solid rgba(0,0,0,0.06);
}
.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.meta-item {
  font-size: 12px;
  color: var(--text-sub);
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.meta-item i {
  color: var(--primary);
  opacity: 0.85;
}

/* 按钮 */
.article-actions {
  display: flex;
  gap: 10px;
  margin-top: 6px;
}
.flat-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  flex: 1;
  justify-content: center;
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
.flat-btn.ghost-mode {
  background: transparent;
  border: 1px solid #E2E8F0;
  color: var(--text-sub);
}
.flat-btn.ghost-mode:hover {
  background: #F1F5F9;
  color: var(--text-main);
}
.flat-btn.danger-btn:hover {
  border-color: #EF4444;
  color: #EF4444;
  background: #FEF2F2;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 56px 24px;
}
.empty-icon {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: rgba(255,255,255,0.65);
  border: 1px solid var(--glass-border);
  margin: 0 auto 14px auto;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #CBD5E1;
  font-size: 30px;
}
.empty-main {
  margin: 0 0 6px 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-main);
}
.empty-sub {
  margin: 0;
  font-size: 13px;
  color: var(--text-sub);
}
.text-link {
  color: var(--primary);
  font-weight: 700;
  text-decoration: none;
}
.text-link:hover { text-decoration: underline; }

/* 分页 */
.pagination-wrapper {
  margin-top: 26px;
  display: flex;
  justify-content: center;
}
:deep(.healing-pagination .el-pager li.is-active) {
  background-color: var(--primary) !important;
}
:deep(.healing-pagination .el-pagination__total) {
  color: var(--text-sub);
  font-size: 13px;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header { padding: 28px 0 18px 0; }
  .articles-grid { grid-template-columns: 1fr; gap: 18px; }
  .glass-btn { width: 100%; justify-content: center; }
}
@media (max-width: 480px) {
  .article-cover { height: 160px; }
  .article-content { padding: 16px; }
}
</style>