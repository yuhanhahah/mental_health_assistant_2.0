<template>
  <div class="knowledge-page">
    <div class="page-header">
      <div class="page-header-inner">
        <div class="page-title-block">
          <div class="page-icon">
            <i class="fas fa-book-open"></i>
          </div>
          <div>
            <h2 class="page-title">知识库</h2>
            <p class="page-subtitle">在这里，每一篇文章都是通往更好自己的路径</p>
          </div>
        </div>

        <div class="search-wrapper">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索心理健康知识..."
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
            @focus="showSearchHistory = true"
            @blur="setTimeout(() => showSearchHistory = false, 200)"
            class="search-input"
          >
            <template #prefix>
              <i class="fas fa-search search-prefix-icon"></i>
            </template>
          </el-input>

          <div v-if="showSearchHistory && searchHistory.length > 0" class="search-history">
            <div class="history-header">
              <span>搜索历史</span>
              <span class="clear-btn" @click="clearSearchHistory">清除</span>
            </div>
            <div
              v-for="item in searchHistory"
              :key="item"
              class="history-item"
              @click="selectHistoryItem(item)"
            >
              <i class="fas fa-history"></i>
              <span class="history-text">{{ item }}</span>
              <i class="fas fa-times remove-btn" @click.stop="removeHistoryItem(item)"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="page-body">
      <div class="body-inner">
        <div class="layout">

          <aside class="sidebar">

            <div class="sidebar-block solid-card">
              <div class="block-title">
                <i class="fas fa-th-large"></i>
                知识分类
              </div>
              <div class="category-list">
                <div
                  v-for="category in categories"
                  :key="category.id"
                  class="category-item"
                  :class="[getCategoryColorClass(category.categoryName), { active: selectedCategoryId === category.id }]"
                  @click="selectCategory(category.id)"
                >
                  <i :class="category.icon" :style="{ color: getCategoryIconColor(category.categoryName) }"></i>
                  <span class="category-name">{{ category.categoryName }}</span>
                  <span class="category-count">{{ category.articleCount || 0 }}</span>
                </div>
              </div>
            </div>

            <div class="sidebar-block solid-card">
              <div class="block-title">
                <i class="fas fa-star"></i>
                推荐阅读
              </div>
              <div class="recommend-list">
                <div
                  v-for="article in recommendArticles"
                  :key="article.id"
                  class="recommend-item"
                  @click="goToArticle(article.id)"
                >
                  <p class="recommend-title">{{ article.title }}</p>
                  <span class="recommend-meta">
                    <i class="fas fa-eye"></i>
                    {{ formatReadCount(article.readCount) }} 次阅读
                  </span>
                </div>
              </div>
            </div>

            <div class="sidebar-block solid-card tip-block">
              <div class="block-title">
                <i class="fas fa-lightbulb"></i>
                每日心理小贴士
              </div>
              <p class="tip-text">{{ dailyTip }}</p>
            </div>

          </aside>

          <main class="main-content">

            <div class="filter-bar solid-card">
              <div class="filter-left">
                <span class="filter-label">排序方式：</span>
                <el-select
                  v-model="searchForm.sortField"
                  @change="handleSearch"
                  placeholder="选择排序方式"
                  size="large"
                  class="clean-select"
                >
                  <el-option label="最新发布" value="publishedAt" />
                  <el-option label="最多阅读" value="readCount" />
                  <el-option label="相关度" value="relevance" />
                </el-select>
              </div>
              <div class="filter-right">
                共找到 <strong>{{ total }}</strong> 篇文章
              </div>
            </div>

            <div class="article-list" v-loading="loading">
              <ArticleCard
                v-for="article in articles"
                :key="article.id"
                :article="article"
                @favorite-changed="handleFavoriteChanged"
              />

              <div v-if="!loading && articles.length === 0" class="empty-state solid-card">
                <div class="empty-icon"><i class="fas fa-search"></i></div>
                <p class="empty-main">暂无相关文章</p>
                <p class="empty-sub">尝试调整搜索条件或浏览其他分类</p>
              </div>
            </div>

            <div class="pagination-wrapper" v-if="total > 0">
              <el-pagination
                :current-page="searchForm.currentPage"
                :page-size="searchForm.size"
                :page-sizes="[6, 12, 18, 24]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                background
                class="clean-pagination"
              />
            </div>

          </main>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import ArticleCard from '@/components/frontend/ArticleCard.vue'
import { getArticlePage } from '@/api/knowledgeArticle'
import { getCategoryTree } from '@/api/knowledgeCategory'
import { favoriteArticle, unfavoriteArticle, checkFavoriteStatus } from '@/api/userFavorite'
import { formatDate } from '@/utils/dateUtils'

const router = useRouter()

const loading = ref(false)
const articles = ref([])
const categories = ref([])
const recommendArticles = ref([])
const total = ref(0)
const selectedCategoryId = ref(null)
const searchHistory = ref([])
const showSearchHistory = ref(false)

const searchForm = reactive({
  keyword: '',
  categoryId: null,
  sortField: 'publishedAt',
  sortDirection: 'desc',
  currentPage: 1,
  size: 12
})

const defaultCover = 'https://images.unsplash.com/photo-1559757148-5c350d0d3c56?ixlib=rb-4.0.3&auto=format&fit=crop&w=400&q=80'
const dailyTip = ref('深呼吸是最简单有效的放松技巧。当感到压力时，尝试4-7-8呼吸法：吸气4秒，屏息7秒，呼气8秒。')

const isLoggedIn = computed(() => localStorage.getItem('token'))

const fetchArticles = () => {
  loading.value = true
  const params = { ...searchForm, categoryId: selectedCategoryId.value }
  getArticlePage(params, {
    onSuccess: (response) => {
      if (response && response.records) {
        articles.value = response.records.map(a => ({ ...a, isFavorited: false }))
        total.value = response.total || 0
        if (isLoggedIn.value && articles.value.length > 0) checkArticlesFavoriteStatus()
      } else {
        articles.value = []
        total.value = 0
      }
      loading.value = false
    },
    onError: () => {
      ElMessage.error('获取文章列表失败')
      articles.value = []
      total.value = 0
      loading.value = false
    }
  })
}

const checkArticlesFavoriteStatus = () => {
  articles.value.forEach(article => {
    checkFavoriteStatus(article.id, {
      onSuccess: (s) => { article.isFavorited = s },
      onError: () => { article.isFavorited = false }
    })
  })
}

const fetchCategories = () => {
  getCategoryTree({
    onSuccess: (response) => {
      if (response && Array.isArray(response)) {
        const totalCount = response.reduce((s, c) => s + (c.articleCount || 0), 0)
        categories.value = [
          { id: null, categoryName: '全部分类', icon: 'fas fa-th-large', articleCount: totalCount },
          ...response.map(c => ({ ...c, icon: getCategoryIcon(c.categoryName) }))
        ]
      }
    },
    onError: () => ElMessage.error('获取分类列表失败')
  })
}

const fetchRecommendArticles = () => {
  getArticlePage({ sortField: 'readCount', sortDirection: 'desc', currentPage: 1, size: 3 }, {
    onSuccess: (response) => {
      if (response && response.records) recommendArticles.value = response.records
    },
    onError: () => {}
  })
}

const selectCategory = (categoryId) => {
  selectedCategoryId.value = categoryId
  searchForm.currentPage = 1
  handleSearch()
}

const handleSearch = () => {
  searchForm.categoryId = selectedCategoryId.value
  if (searchForm.keyword?.trim()) addSearchHistory(searchForm.keyword.trim())
  showSearchHistory.value = false
  fetchArticles()
}

const addSearchHistory = (keyword) => {
  const filtered = searchHistory.value.filter(i => i !== keyword)
  searchHistory.value = [keyword, ...filtered].slice(0, 10)
  localStorage.setItem('knowledge_search_history', JSON.stringify(searchHistory.value))
}

const loadSearchHistory = () => {
  try {
    const saved = localStorage.getItem('knowledge_search_history')
    if (saved) searchHistory.value = JSON.parse(saved)
  } catch { searchHistory.value = [] }
}

const clearSearchHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('knowledge_search_history')
  showSearchHistory.value = false
}

const selectHistoryItem = (keyword) => {
  searchForm.keyword = keyword
  handleSearch()
}

const removeHistoryItem = (keyword) => {
  searchHistory.value = searchHistory.value.filter(i => i !== keyword)
  localStorage.setItem('knowledge_search_history', JSON.stringify(searchHistory.value))
}

const goToArticle = (articleId) => router.push(`/knowledge/article/${articleId}`)

const handleFavoriteChanged = (article) => {
  console.log('收藏状态变化:', article.id, article.isFavorited)
}

const toggleFavorite = (article) => {
  if (!isLoggedIn.value) {
    ElMessageBox.confirm('需要登录才能收藏文章，是否前往登录？', '提示', {
      confirmButtonText: '去登录', cancelButtonText: '取消', type: 'info'
    }).then(() => router.push('/auth/login')).catch(() => {})
    return
  }
  if (article.isFavorited) {
    unfavoriteArticle(article.id, {
      successMsg: '已取消收藏',
      onSuccess: () => { article.isFavorited = false; article.favoriteCount = Math.max(0, (article.favoriteCount || 1) - 1) },
      onError: () => ElMessage.error('取消收藏失败')
    })
  } else {
    favoriteArticle(article.id, {
      successMsg: '收藏成功',
      onSuccess: () => { article.isFavorited = true; article.favoriteCount = (article.favoriteCount || 0) + 1 },
      onError: () => ElMessage.error('收藏失败')
    })
  }
}

const getCategoryIcon = (name) => ({
  '情绪管理': 'fas fa-heart',
  '焦虑抑郁': 'fas fa-brain',
  '工作压力': 'fas fa-briefcase',
  '压力缓解': 'fas fa-spa',
  '人际关系': 'fas fa-users',
  '睡眠健康': 'fas fa-bed',
  '儿童心理': 'fas fa-child',
  '创伤康复': 'fas fa-heart-broken',
  '放松技巧': 'fas fa-leaf',
  '心理健康基础': 'fas fa-heartbeat',
  '情绪调节': 'fas fa-smile',
  '人际沟通': 'fas fa-comments',
  '自我成长': 'fas fa-seedling'
}[name] || 'fas fa-bookmark')

// 分类图标颜色 - 低饱和度配色
const getCategoryIconColor = (name) => ({
  '情绪管理': '#f472b6',    // 粉色
  '焦虑抑郁': '#818cf8',    // 紫蓝色
  '工作压力': '#fb923c',    // 橙色
  '压力缓解': '#f97316',    // 深橙色
  '人际关系': '#60a5fa',    // 蓝色
  '睡眠健康': '#a78bfa',    // 紫色
  '儿童心理': '#34d399',    // 绿色
  '创伤康复': '#fb7185',    // 红色
  '放松技巧': '#4ade80',    // 浅绿色
  '心理健康基础': '#3b82f6',  // 科技蓝
  '情绪调节': '#ec4899',     // 粉红色
  '人际沟通': '#06b6d4',    // 青色
  '自我成长': '#22c55e'     // 翠绿色
}[name] || '#3b82f6')

// 分类背景色类名
const getCategoryColorClass = (name) => ({
  '情绪管理': 'color-pink',
  '焦虑抑郁': 'color-purple',
  '工作压力': 'color-orange',
  '压力缓解': 'color-orange',
  '人际关系': 'color-blue',
  '睡眠健康': 'color-violet',
  '儿童心理': 'color-teal',
  '创伤康复': 'color-rose',
  '放松技巧': 'color-green',
  '心理健康基础': 'color-blue',
  '情绪调节': 'color-pink',
  '人际沟通': 'color-cyan',
  '自我成长': 'color-teal'
}[name] || '')

const formatReadCount = (count) => {
  if (!count) return '0'
  if (count < 1000) return count.toString()
  if (count < 10000) return (count / 1000).toFixed(1) + 'k'
  return (count / 10000).toFixed(1) + 'w'
}

const handleSizeChange = (size) => { searchForm.size = size; searchForm.currentPage = 1; fetchArticles() }
const handleCurrentChange = (page) => { searchForm.currentPage = page; fetchArticles() }

onMounted(() => {
  loadSearchHistory()
  fetchCategories()
  fetchArticles()
  fetchRecommendArticles()
})
</script>

<style scoped>
/* 提取主色调，匹配 Home.vue 与 ArticleDetail.vue */
:root {
  --primary-color: #4A90E2;      /* 科技蓝 */
  --accent-color: #7ED321;       /* 生机绿 */
  --bg-color: #f9fafb;           /* 页面浅灰底色 */
  --card-bg: #ffffff;            /* 卡片纯白底色 */
  --text-main: #111827;          /* 主文本色 */
  --text-sub: #6b7280;           /* 次文本色 */
  --border-color: #e5e7eb;       /* 柔和边框色 */
  --radius-lg: 1.5rem;           /* 卡片大圆角 */
  --radius-md: 0.75rem;          /* 内部元素圆角 */
  --shadow-soft: 0 2px 8px rgba(0, 0, 0, 0.06); /* 统一柔和阴影 */
}

/* ── 全局 ── */
.knowledge-page {
  min-height: 100vh;
  background: var(--bg-color); 
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, sans-serif;
  color: var(--text-main);
}

/* ── 顶部标题栏（纯白实色，无渐变） ── */
.page-header {
  background: var(--card-bg);
  border-bottom: 1px solid var(--border-color);
  padding: 2.5rem 0;
}

.page-header-inner {
  max-width: 1200px; 
  margin: 0 auto; 
  padding: 0 2rem;
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
  gap: 24px;
}

.page-title-block { 
  display: flex; 
  align-items: center; 
  gap: 1rem; 
}

/* 标题图标 - 对应呼吸效果的生机绿 */
.page-icon {
  font-size: 2.5rem;
  color: var(--accent-color);
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-title { 
  font-size: 1.8rem; 
  font-weight: 600; 
  color: var(--text-main); 
  margin: 0 0 0.25rem 0; 
  letter-spacing: -0.25px;
}

.page-subtitle { 
  font-size: 0.95rem; 
  color: var(--text-sub); 
  margin: 0; 
}

/* 搜索框：干净无边框风格 */
.search-wrapper { 
  position: relative; 
  width: 360px; 
  flex-shrink: 0; 
  z-index: 900; 
}

.search-input :deep(.el-input__wrapper) {
  border-radius: var(--radius-md); 
  box-shadow: none; 
  transition: all 0.2s ease;
  background: #f3f4f6;
  border: 1px solid transparent;
  padding: 0.5rem 1rem;
}

.search-input :deep(.el-input__wrapper):hover,
.search-input :deep(.el-input__wrapper.is-focus) {
  background: #ffffff;
  border-color: var(--primary-color); 
  box-shadow: 0 0 0 3px rgba(74, 144, 226, 0.15);
}

.search-input :deep(.el-input__inner) { 
  font-size: 1rem; 
  color: var(--text-main); 
}

.search-prefix-icon { 
  color: var(--text-sub); 
  font-size: 1rem; 
} 

/* 搜索历史 */
.search-history {
  position: absolute; 
  top: calc(100% + 0.5rem); 
  left: 0; 
  right: 0;
  background: var(--card-bg); 
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md); 
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  z-index: 999; 
  overflow: hidden;
}

.history-header { 
  display: flex; justify-content: space-between; align-items: center; 
  padding: 1rem; font-size: 0.875rem; color: var(--text-sub); 
  border-bottom: 1px solid #f3f4f6; 
}

.clear-btn { cursor: pointer; transition: color 0.2s; }
.clear-btn:hover { color: #ef4444; }

.history-item { 
  display: flex; align-items: center; gap: 0.75rem; 
  padding: 0.875rem 1rem; cursor: pointer; font-size: 0.9rem; 
  color: var(--text-main); transition: background 0.15s; 
}
.history-item:hover { background: #f3f4f6; }
.history-item .fa-history { color: #d1d5db; font-size: 0.875rem; }
.history-text { flex: 1; }
.remove-btn { color: #d1d5db; font-size: 0.75rem; opacity: 0; transition: opacity 0.15s; }
.history-item:hover .remove-btn { opacity: 1; }
.remove-btn:hover { color: #ef4444; }

/* ── 主体区域 ── */
.page-body { 
  padding: 2rem 0; 
  position: relative; 
  z-index: 10; 
}

.body-inner { 
  max-width: 1200px; 
  margin: 0 auto; 
  padding: 0 2rem; 
}

.layout { 
  display: grid; 
  grid-template-columns: 280px 1fr; 
  gap: 2rem; 
  align-items: start; 
}

/* ── 侧边栏 ── */
.sidebar { 
  display: flex; 
  flex-direction: column; 
  gap: 1.5rem; 
}

/* 统一风格实白卡片 */
.solid-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  padding: 1.5rem;
}

/* 侧边栏小标题 */
.block-title { 
  font-size: 1.1rem; 
  font-weight: 600; 
  color: var(--text-main); 
  margin: 0 0 1.25rem 0; 
  display: flex; 
  align-items: center; 
  gap: 0.5rem; 
}
.block-title i { color: var(--primary-color); font-size: 1.1rem; }

/* 分类列表 */
.category-list { display: flex; flex-direction: column; gap: 0.25rem; }
.category-item { 
  display: flex; align-items: center; gap: 0.75rem; 
  padding: 0.75rem 1rem; border-radius: var(--radius-md); 
  cursor: pointer; font-size: 0.95rem; color: var(--text-sub); 
  font-weight: 500; transition: all 0.2s ease; 
}
.category-item i { font-size: 1rem; width: 1.25rem; text-align: center; transition: color 0.2s ease; }
.category-item:hover { background: #f3f4f6; color: var(--text-main); }
.category-item:hover i { color: inherit; }

/* 不同分类图标的低饱和度背景色 */
.category-item.color-pink:hover { background: rgba(244, 114, 182, 0.1); color: #db2777; }
.category-item.color-pink:hover i { color: #f472b6; }

.category-item.color-purple:hover { background: rgba(129, 140, 248, 0.1); color: #6366f1; }
.category-item.color-purple:hover i { color: #818cf8; }

.category-item.color-orange:hover { background: rgba(251, 146, 60, 0.1); color: #ea580c; }
.category-item.color-orange:hover i { color: #fb923c; }

.category-item.color-blue:hover { background: rgba(96, 165, 250, 0.1); color: #2563eb; }
.category-item.color-blue:hover i { color: #60a5fa; }

.category-item.color-violet:hover { background: rgba(167, 139, 250, 0.1); color: #7c3aed; }
.category-item.color-violet:hover i { color: #a78bfa; }

.category-item.color-teal:hover { background: rgba(52, 211, 153, 0.1); color: #059669; }
.category-item.color-teal:hover i { color: #34d399; }

.category-item.color-rose:hover { background: rgba(251, 113, 133, 0.1); color: #e11d48; }
.category-item.color-rose:hover i { color: #fb7185; }

.category-item.color-green:hover { background: rgba(74, 222, 128, 0.1); color: #16a34a; }
.category-item.color-green:hover i { color: #4ade80; }

.category-item.color-cyan:hover { background: rgba(6, 182, 212, 0.1); color: #0891b2; }
.category-item.color-cyan:hover i { color: #06b6d4; }

/* 激活状态 */
.category-item.active { 
  font-weight: 600; 
}
.category-item.color-pink.active { background: rgba(244, 114, 182, 0.15); color: #db2777; }
.category-item.color-pink.active i { color: #f472b6; }

.category-item.color-purple.active { background: rgba(129, 140, 248, 0.15); color: #6366f1; }
.category-item.color-purple.active i { color: #818cf8; }

.category-item.color-orange.active { background: rgba(251, 146, 60, 0.15); color: #ea580c; }
.category-item.color-orange.active i { color: #fb923c; }

.category-item.color-blue.active { background: rgba(96, 165, 250, 0.15); color: #2563eb; }
.category-item.color-blue.active i { color: #60a5fa; }

.category-item.color-violet.active { background: rgba(167, 139, 250, 0.15); color: #7c3aed; }
.category-item.color-violet.active i { color: #a78bfa; }

.category-item.color-teal.active { background: rgba(52, 211, 153, 0.15); color: #059669; }
.category-item.color-teal.active i { color: #34d399; }

.category-item.color-rose.active { background: rgba(251, 113, 133, 0.15); color: #e11d48; }
.category-item.color-rose.active i { color: #fb7185; }

.category-item.color-green.active { background: rgba(74, 222, 128, 0.15); color: #16a34a; }
.category-item.color-green.active i { color: #4ade80; }

.category-item.color-cyan.active { background: rgba(6, 182, 212, 0.15); color: #0891b2; }
.category-item.color-cyan.active i { color: #06b6d4; }

/* 默认分类的激活态 */
.category-item:not([class*="color-"]).active { 
  background: rgba(74, 144, 226, 0.1); 
  color: var(--primary-color); 
}
.category-name { flex: 1; }
.category-count { 
  font-size: 0.75rem; color: #9ca3af; 
  background: #f3f4f6; padding: 0.1rem 0.6rem; 
  border-radius: 999px; 
}
.category-item.active .category-count { 
  background: var(--primary-color); 
  color: white; 
}

/* 推荐列表 */
.recommend-list { display: flex; flex-direction: column; gap: 0.5rem; }
.recommend-item { 
  padding: 0.75rem; border-radius: var(--radius-md); 
  cursor: pointer; transition: all 0.2s; border: 1px solid transparent; 
}
.recommend-item:hover { background: #f9fafb; border-color: var(--border-color); }
.recommend-title { 
  font-size: 0.95rem; color: var(--text-main); 
  line-height: 1.4; margin: 0 0 0.4rem 0; font-weight: 500; 
}
.recommend-meta { 
  font-size: 0.8rem; color: var(--text-sub); 
  display: flex; align-items: center; gap: 0.4rem; 
}

/* 每日贴士卡片 - 纯色无渐变 (选用 ArticleDetail 中的浅绿色) */
.sidebar-block.tip-block { 
  background: #F0FDF4; 
  border-color: #BBF7D0; 
}
.sidebar-block.tip-block .block-title i { color: #22C55E; }
.sidebar-block.tip-block .block-title { color: #166534; }
.tip-text { font-size: 0.9rem; color: #15803D; line-height: 1.6; margin: 0; }

/* ── 主内容区 ── */
.main-content { display: flex; flex-direction: column; gap: 1.5rem; }

/* 排序栏 */
.filter-bar { 
  padding: 1rem 1.5rem; 
  display: flex; 
  align-items: center; 
  justify-content: space-between; 
  flex-wrap: wrap; 
  gap: 1rem; 
}
.filter-left { display: flex; align-items: center; gap: 1rem; }
.filter-label { font-size: 0.95rem; color: var(--text-main); font-weight: 500; }
.filter-left .el-select { width: 140px; }

/* 内部 Select 样式 */
:deep(.clean-select .el-input__wrapper) { 
  border-radius: 0.5rem !important; 
  box-shadow: none !important; 
  border: 1px solid var(--border-color) !important; 
}
:deep(.clean-select .el-input__wrapper.is-focus) {
  border-color: var(--primary-color) !important;
}

.filter-right { font-size: 0.9rem; color: var(--text-sub); }
.filter-right strong { color: var(--primary-color); font-size: 1rem; font-weight: 600; margin: 0 0.25rem; }

/* 文章列表 */
.article-list { display: flex; flex-direction: column; gap: 1.5rem; }

/* 空状态 */
.empty-state { text-align: center; padding: 4rem 2rem; }
.empty-icon { 
  width: 4rem; height: 4rem; border-radius: 50%; 
  background: #f3f4f6; display: flex; align-items: center; 
  justify-content: center; margin: 0 auto 1.5rem; 
  color: #9ca3af; font-size: 2rem; 
}
.empty-main { font-size: 1.1rem; font-weight: 500; color: var(--text-main); margin: 0 0 0.5rem; }
.empty-sub { font-size: 0.9rem; color: var(--text-sub); margin: 0; }

/* 分页器 */
.pagination-wrapper { display: flex; justify-content: center; margin-top: 1rem; }
:deep(.clean-pagination .el-pager li.is-active) { 
  background-color: var(--primary-color) !important; 
  color: white !important; 
}

/* ── 响应式 ── */
@media (max-width: 1024px) {
  .layout { grid-template-columns: 1fr; }
  .sidebar { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); }
}
@media (max-width: 768px) {
  .page-header { padding: 1.5rem 0; }
  .page-header-inner { flex-direction: column; align-items: flex-start; gap: 1rem; padding: 0 1rem; }
  .search-wrapper { width: 100%; }
  .body-inner { padding: 0 1rem; }
  .page-body { padding: 1.5rem 0; }
  .sidebar { grid-template-columns: 1fr; }
  .filter-bar { flex-direction: column; align-items: flex-start; }
}
</style>