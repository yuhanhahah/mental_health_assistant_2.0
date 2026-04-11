<template>
  <div class="data-analytics-container">
    <div class="ambient-light light-1"></div>
    <div class="ambient-light light-2"></div>
    <div class="ambient-light light-3"></div>

    <div class="content-wrapper">
      <div class="overview-cards" v-loading="loading">
        <div class="overview-card glass-card">
          <div class="card-icon-wrapper soft-blue">
            <i class="fas fa-users"></i>
          </div>
          <div class="card-content">
            <div class="card-title">总用户数</div>
            <div class="card-value">{{ formatNumber(systemOverview.totalUsers) }}</div>
            <div class="card-subtitle">活跃用户: {{ formatNumber(systemOverview.activeUsers) }}</div>
          </div>
        </div>

        <div class="overview-card glass-card">
          <div class="card-icon-wrapper soft-pink">
            <i class="fas fa-heart"></i>
          </div>
          <div class="card-content">
            <div class="card-title">情绪日记</div>
            <div class="card-value">{{ formatNumber(systemOverview.totalDiaries) }}</div>
            <div class="card-subtitle">今日新增: {{ formatNumber(systemOverview.todayNewDiaries) }}</div>
          </div>
        </div>

        <div class="overview-card glass-card">
          <div class="card-icon-wrapper soft-purple">
            <i class="fas fa-comments"></i>
          </div>
          <div class="card-content">
            <div class="card-title">咨询会话</div>
            <div class="card-value">{{ formatNumber(systemOverview.totalSessions) }}</div>
            <div class="card-subtitle">今日新增: {{ formatNumber(systemOverview.todayNewSessions) }}</div>
          </div>
        </div>

        <div class="overview-card glass-card">
          <div class="card-icon-wrapper soft-orange">
            <i class="fas fa-smile"></i>
          </div>
          <div class="card-content">
            <div class="card-title">平均情绪</div>
            <div class="card-value">{{ systemOverview.avgMoodScore || '0.0' }}<span class="unit">/10</span></div>
            <div class="card-subtitle">情绪健康指数</div>
          </div>
        </div>
      </div>

      <div class="chart-section glass-card">
        <div class="visualization-toggle">
          <div class="pill-nav">
            <div 
              class="pill-btn" 
              :class="{ 'active': visualizationMode === 'classic' }"
              @click="visualizationMode = 'classic'"
            >
              <i class="fas fa-cubes"></i> 经典热力图
            </div>
            <div 
              class="pill-btn" 
              :class="{ 'active': visualizationMode === 'hub' }"
              @click="visualizationMode = 'hub'"
            >
              <i class="fas fa-magic"></i> 多元可视化
            </div>
          </div>
        </div>
        
        <EmotionHeatmap3D v-if="visualizationMode === 'classic'" :heatmapData="emotionHeatmap" />
        <EmotionVisualizationHub v-if="visualizationMode === 'hub'" :heatmapData="{ ...emotionHeatmap, emotionTrend: emotionTrend }" />
      </div>

      <div class="charts-grid">
        <div class="chart-card glass-card">
          <div class="chart-header">
            <h3><i class="fas fa-wave-square"></i> 情绪趋势分析</h3>
          </div>
          <div class="chart-content">
            <div ref="emotionTrendChartRef" style="width: 100%; height: 320px;"></div>
          </div>
        </div>

        <div class="chart-card glass-card">
          <div class="chart-header">
            <h3><i class="fas fa-chart-pie"></i> 咨询会话统计</h3>
          </div>
          <div class="chart-content">
            <div class="consultation-stats">
              <div class="stat-item">
                <div class="stat-label">总会话数</div>
                <div class="stat-value">{{ formatNumber(consultationStats.totalSessions) }}</div>
              </div>
              <div class="divider"></div>
              <div class="stat-item">
                <div class="stat-label">平均时长</div>
                <div class="stat-value">{{ consultationStats.avgDurationMinutes || '0' }}<span class="unit">m</span></div>
              </div>
              <div class="divider"></div>
              <div class="stat-item">
                <div class="stat-label">活跃用户</div>
                <div class="stat-value">{{ formatNumber(systemOverview.activeUsers) }}</div>
              </div>
            </div>
            <div ref="consultationChartRef" style="width: 100%; height: 260px;"></div>
          </div>
        </div>

        <div class="chart-card glass-card full-width">
          <div class="chart-header">
            <h3><i class="fas fa-chart-line"></i> 用户活跃度趋势</h3>
          </div>
          <div class="chart-content">
            <div ref="userActivityChartRef" style="width: 100%; height: 320px;"></div>
          </div>
        </div>
      </div>

      <div class="emotion-tags-section glass-card" v-if="consultationStats.topEmotionTags">
        <div class="chart-header" style="border: none; padding-bottom: 0;">
          <h3><i class="fas fa-hashtag"></i> 高频情绪标签</h3>
        </div>
        <div class="emotion-tags-cloud">
          <div 
            v-for="(count, tag) in consultationStats.topEmotionTags" 
            :key="tag"
            class="emotion-tag glass-pill"
            :style="{ 
              '--tag-scale': Math.min(count / maxTagCount * 0.5 + 0.8, 1.3),
              '--tag-opacity': Math.min(count / maxTagCount * 0.6 + 0.4, 1)
            }"
          >
            <span class="tag-name">{{ tag }}</span>
            <span class="tag-count">{{ count }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import EmotionHeatmap3D from '@/components/common/EmotionHeatmap3D.vue'
import EmotionVisualizationHub from '@/components/common/EmotionVisualizationHub.vue'
import { getDataAnalytics } from '@/api/dataAnalytics'

// 重构 ECharts 奶油质感主题配置
const creamTheme = {
  backgroundColor: 'transparent',
  textStyle: {
    fontFamily: '-apple-system, BlinkMacSystemFont, "PingFang SC", sans-serif',
    color: '#64748B'
  },
  tooltip: {
    backgroundColor: 'rgba(255, 255, 255, 0.9)',
    borderColor: 'rgba(59, 130, 246, 0.1)',
    borderWidth: 1,
    padding: [12, 16],
    textStyle: { color: '#334155' },
    extraCssText: 'box-shadow: 0 8px 32px rgba(31, 38, 135, 0.08); border-radius: 12px; backdrop-filter: blur(10px);'
  },
  grid: {
    left: '2%', right: '2%', bottom: '2%', top: '15%', containLabel: true
  },
  categoryAxis: {
    axisLine: { lineStyle: { color: '#E2E8F0', width: 1 } },
    axisTick: { show: false },
    axisLabel: { color: '#94A3B8', margin: 12 },
    splitLine: { show: false }
  },
  valueAxis: {
    axisLine: { show: false },
    axisTick: { show: false },
    axisLabel: { color: '#94A3B8' },
    splitLine: { lineStyle: { color: '#F1F5F9', type: 'dashed' } }
  }
}

// 响应式数据
const loading = ref(false)
const visualizationMode = ref('classic')

const analyticsData = ref({
  systemOverview: {
    totalUsers: 0, activeUsers: 0, totalDiaries: 0, totalSessions: 0, avgMoodScore: 0, todayNewUsers: 0, todayNewDiaries: 0, todayNewSessions: 0
  },
  emotionHeatmap: { gridData: [], emotionDistribution: {}, peakEmotionTime: '00:00', dateRange: '' },
  emotionTrend: [],
  consultationStats: { totalSessions: 0, avgDurationMinutes: 0, dailyTrend: [], topEmotionTags: {} },
  userActivity: []
})

// 计算属性
const systemOverview = computed(() => analyticsData.value.systemOverview)
const emotionHeatmap = computed(() => analyticsData.value.emotionHeatmap)
const emotionTrend = computed(() => analyticsData.value.emotionTrend)
const consultationStats = computed(() => analyticsData.value.consultationStats)
const userActivity = computed(() => analyticsData.value.userActivity)

const maxTagCount = computed(() => {
  const counts = Object.values(consultationStats.value.topEmotionTags || {})
  return counts.length > 0 ? Math.max(...counts) : 1
})

// ECharts图表实例
let emotionTrendChart = null
let consultationChart = null
let userActivityChart = null

const emotionTrendChartRef = ref(null)
const consultationChartRef = ref(null)
const userActivityChartRef = ref(null)

const formatNumber = (num) => {
  if (num === undefined || num === null) return '0'
  return num.toLocaleString()
}

const fetchData = async () => {
  loading.value = true
  try {
    await getDataAnalytics({}, {
      onSuccess: (res) => {
        analyticsData.value = res
        nextTick(() => { initCharts() })
      },
      onError: (error) => {
        console.error('获取数据分析失败:', error)
        ElMessage.error('获取数据失败，请稍后重试')
      }
    })
  } finally {
    loading.value = false
  }
}

const initCharts = () => {
  initEmotionTrendChart()
  initConsultationChart()
  initUserActivityChart()
  
  // 监听窗口大小变化调整图表
  window.addEventListener('resize', () => {
    emotionTrendChart?.resize()
    consultationChart?.resize()
    userActivityChart?.resize()
  })
}

// 初始化情绪趋势图 (柔和折线)
const initEmotionTrendChart = () => {
  if (!emotionTrendChartRef.value) return
  if (emotionTrendChart) emotionTrendChart.dispose()

  echarts.registerTheme('creamTheme', creamTheme)
  emotionTrendChart = echarts.init(emotionTrendChartRef.value, 'creamTheme')
  const trendData = emotionTrend.value

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { 
      data: ['平均情绪评分', '记录数量'], 
      top: 0, right: 0,
      icon: 'circle', itemWidth: 8, itemHeight: 8,
      textStyle: { color: '#64748B' }
    },
    xAxis: { type: 'category', data: trendData.map(item => item.date), boundaryGap: false },
    yAxis: [
      { type: 'value', name: '评分', min: 0, max: 10 },
      { type: 'value', name: '数量', splitLine: { show: false } }
    ],
    series: [
      {
        name: '平均情绪评分',
        type: 'line',
        data: trendData.map(item => item.avgMoodScore),
        smooth: 0.4,
        symbol: 'none',
        lineStyle: { width: 4, color: '#A78BFA' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(167, 139, 250, 0.4)' },
            { offset: 1, color: 'rgba(167, 139, 250, 0.05)' }
          ])
        }
      },
      {
        name: '记录数量',
        type: 'line',
        yAxisIndex: 1,
        data: trendData.map(item => item.recordCount),
        smooth: 0.4,
        symbol: 'none',
        lineStyle: { width: 3, color: '#F472B6', type: 'dashed' }
      }
    ]
  }
  emotionTrendChart.setOption(option)
}

// 初始化咨询统计图 (圆角柱状图)
const initConsultationChart = () => {
  if (!consultationChartRef.value) return
  if (consultationChart) consultationChart.dispose()

  consultationChart = echarts.init(consultationChartRef.value, 'creamTheme')
  const dailyTrend = consultationStats.value.dailyTrend || []

  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { 
      data: ['会话数量', '参与用户'], 
      top: 0, right: 0,
      icon: 'roundRect', itemWidth: 12, itemHeight: 4,
    },
    xAxis: { type: 'category', data: dailyTrend.map(item => item.date) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '会话数量',
        type: 'bar',
        barWidth: '25%',
        data: dailyTrend.map(item => item.sessionCount),
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#60A5FA' },
            { offset: 1, color: '#3B82F6' }
          ])
        }
      },
      {
        name: '参与用户',
        type: 'bar',
        barWidth: '25%',
        data: dailyTrend.map(item => item.userCount),
        itemStyle: {
          borderRadius: [6, 6, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#FBBF24' },
            { offset: 1, color: '#F59E0B' }
          ])
        }
      }
    ]
  }
  consultationChart.setOption(option)
}

// 初始化用户活跃度图 (堆叠面积图)
const initUserActivityChart = () => {
  if (!userActivityChartRef.value) return
  if (userActivityChart) userActivityChart.dispose()

  userActivityChart = echarts.init(userActivityChartRef.value, 'creamTheme')
  const activityData = userActivity.value

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { 
      data: ['活跃用户', '新增用户', '日记记录', '发起咨询'], 
      top: 0, right: 0,
      icon: 'circle'
    },
    xAxis: { type: 'category', boundaryGap: false, data: activityData.map(item => item.date) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '活跃用户', type: 'line', smooth: true, symbol: 'none',
        lineStyle: { width: 3, color: '#818CF8' },
        data: activityData.map(item => item.activeUsers)
      },
      {
        name: '新增用户', type: 'line', smooth: true, symbol: 'none',
        lineStyle: { width: 3, color: '#34D399' },
        data: activityData.map(item => item.newUsers)
      },
      {
        name: '日记记录', type: 'line', smooth: true, symbol: 'none',
        lineStyle: { width: 2, color: '#F472B6' },
        data: activityData.map(item => item.diaryUsers)
      },
      {
        name: '发起咨询', type: 'line', smooth: true, symbol: 'none',
        lineStyle: { width: 2, color: '#FCD34D' },
        data: activityData.map(item => item.consultationUsers)
      }
    ]
  }
  userActivityChart.setOption(option)
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.data-analytics-container {
  min-height: 100vh;
  background-color: #F4F7F9; /* 统一的灰蓝色底层 */
  position: relative;
  overflow: hidden;
  padding: 32px 24px;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", sans-serif;
}

/* 全局梦幻氛围灯 */
.ambient-light {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.5;
  z-index: 0;
  animation: float 20s infinite alternate;
  pointer-events: none;
}
.light-1 { width: 60vw; height: 60vw; background: #E0E7FF; top: -20%; left: -10%; }
.light-2 { width: 50vw; height: 50vw; background: #FCE7F3; bottom: -10%; right: -10%; animation-delay: -5s; }
.light-3 { width: 40vw; height: 40vw; background: #FEF3C7; top: 40%; left: 30%; animation-delay: -10s; opacity: 0.3; }

@keyframes float {
  0% { transform: translate(0, 0); }
  100% { transform: translate(3%, 5%); }
}

.content-wrapper {
  position: relative;
  z-index: 10;
  max-width: 1400px;
  margin: 0 auto;
}

/* 通用毛玻璃卡片 */
.glass-card {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(31, 38, 135, 0.03);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

/* --- 概览卡片区域 --- */
.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 24px;
  margin-bottom: 32px;

  .overview-card {
    padding: 24px;
    display: flex;
    align-items: center;
    gap: 20px;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 16px 40px rgba(31, 38, 135, 0.06);
    }

    .card-icon-wrapper {
      width: 64px;
      height: 64px;
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      
      /* 冰淇淋配色 */
      &.soft-blue { background: #EEF2FF; color: #6366F1; }
      &.soft-pink { background: #FCE7F3; color: #EC4899; }
      &.soft-purple { background: #F3E8FF; color: #8B5CF6; }
      &.soft-orange { background: #FEF3C7; color: #F59E0B; }
    }

    .card-content {
      flex: 1;

      .card-title {
        font-size: 14px;
        font-weight: 600;
        color: #64748B;
        margin-bottom: 6px;
      }

      .card-value {
        font-size: 28px;
        font-weight: 800;
        color: #1E293B;
        margin-bottom: 4px;
        font-family: 'DIN Alternate', -apple-system, sans-serif;
        
        .unit { font-size: 16px; color: #94A3B8; font-weight: 500; margin-left: 2px;}
      }

      .card-subtitle {
        font-size: 12px;
        color: #94A3B8;
        font-weight: 500;
      }
    }
  }
}

/* --- 图表与内容区域 --- */
.chart-section {
  padding: 24px;
  margin-bottom: 32px;
  
  .visualization-toggle {
    display: flex;
    justify-content: center;
    margin-bottom: 24px;
    
    .pill-nav {
      display: inline-flex;
      background: rgba(255, 255, 255, 0.5);
      border: 1px solid rgba(255, 255, 255, 0.8);
      border-radius: 30px;
      padding: 6px;
      box-shadow: inset 0 2px 4px rgba(0,0,0,0.02);
      
      .pill-btn {
        padding: 10px 24px;
        border-radius: 24px;
        font-size: 14px;
        font-weight: 600;
        color: #64748B;
        cursor: pointer;
        transition: all 0.3s;
        display: flex;
        align-items: center;
        gap: 8px;
        
        &:hover { color: #3B82F6; }
        
        &.active {
          background: white;
          color: #3B82F6;
          box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
        }
      }
    }
  }
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 24px;
  margin-bottom: 32px;

  .chart-card {
    padding: 24px;

    &.full-width { grid-column: 1 / -1; }

    .chart-header {
      margin-bottom: 20px;
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 700;
        color: #334155;
        display: flex;
        align-items: center;
        gap: 10px;
        
        i {
          color: #8B5CF6;
          background: #EEF2FF;
          padding: 8px;
          border-radius: 10px;
          font-size: 14px;
        }
      }
    }

    .chart-content {
      position: relative;
      
      .consultation-stats {
        display: flex;
        justify-content: space-between;
        align-items: center;
        background: rgba(248, 250, 252, 0.5);
        border: 1px solid rgba(255,255,255,0.8);
        border-radius: 16px;
        padding: 16px 24px;
        margin-bottom: 20px;

        .divider { width: 1px; height: 30px; background: #E2E8F0; }

        .stat-item {
          text-align: center;
          .stat-label { font-size: 12px; color: #64748B; margin-bottom: 4px; font-weight: 500; }
          .stat-value { 
            font-size: 20px; font-weight: 700; color: #1E293B; 
            .unit { font-size: 14px; color: #94A3B8; margin-left: 2px;}
          }
        }
      }
    }
  }
}

/* --- 情绪标签云 --- */
.emotion-tags-section {
  padding: 24px;
  
  .emotion-tags-cloud {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    padding-top: 16px;

    .glass-pill {
      background: rgba(255, 255, 255, 0.6);
      border: 1px solid rgba(255, 255, 255, 0.9);
      padding: 10px 20px;
      border-radius: 30px;
      box-shadow: 0 4px 15px rgba(31, 38, 135, 0.03);
      display: flex;
      align-items: center;
      gap: 8px;
      transform: scale(var(--tag-scale));
      transform-origin: left center;
      opacity: var(--tag-opacity);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      cursor: default;

      &:hover {
        background: white;
        transform: scale(calc(var(--tag-scale) * 1.05));
        box-shadow: 0 8px 25px rgba(59, 130, 246, 0.1);
        z-index: 10;
        opacity: 1;
      }

      .tag-name {
        font-weight: 600;
        color: #3B82F6;
        font-size: 14px;
      }
      .tag-count {
        background: #EEF2FF;
        color: #6366F1;
        padding: 2px 8px;
        border-radius: 12px;
        font-size: 12px;
        font-weight: 700;
      }
    }
  }
}

// 响应式
@media (max-width: 1024px) {
  .charts-grid { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .data-analytics-container { padding: 16px; }
  .overview-cards { grid-template-columns: 1fr; }
}
</style>