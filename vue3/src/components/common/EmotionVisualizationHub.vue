<template>
  <div class="emotion-visualization-hub glass-card">
    <div class="visualization-selector">
      <div class="selector-header">
        <h3 class="selector-title">
          <i class="fas fa-chart-pie"></i>
          情绪维度透视
        </h3>
        <div class="selector-subtitle">选择最适合的数据展示方式</div>
      </div>
      
      <div class="mode-tabs">
        <div 
          v-for="mode in visualizationModes" 
          :key="mode.key"
          class="mode-tab"
          :class="{ active: currentMode === mode.key }"
          @click="switchMode(mode.key)"
        >
          <i :class="mode.icon"></i>
          <span class="tab-name">{{ mode.name }}</span>
        </div>
      </div>
    </div>

    <div class="visualization-content">
      <div v-if="currentMode === 'calendar'" class="visualization-panel fade-in">
        <div class="panel-header">
          <h4><i class="fas fa-calendar-day"></i> 情绪日历</h4>
        </div>
        <div class="calendar-container">
          <div class="calendar-controls">
            <div class="calendar-header inner-glass">
              <button @click="previousMonth" class="nav-btn prev"><i class="fas fa-chevron-left"></i></button>
              <div class="month-info">
                <h5 class="month-title">{{ currentMonthYear }}</h5>
                <div class="month-stats">本月记录 {{ monthlyEmotionCount }} 天 · 平均心情 {{ monthlyAvgMood }}/10</div>
              </div>
              <button @click="nextMonth" class="nav-btn next"><i class="fas fa-chevron-right"></i></button>
            </div>
            
            <div class="emotion-legend inner-glass">
              <div class="legend-items">
                <div class="legend-item"><div class="legend-color excellent"></div><span>极佳</span></div>
                <div class="legend-item"><div class="legend-color good"></div><span>良好</span></div>
                <div class="legend-item"><div class="legend-color normal"></div><span>一般</span></div>
                <div class="legend-item"><div class="legend-color low"></div><span>较低</span></div>
                <div class="legend-item"><div class="legend-color poor"></div><span>不佳</span></div>
              </div>
            </div>
          </div>

          <div class="calendar-grid inner-glass">
            <div class="weekday-header">
              <div class="weekday" v-for="day in weekdays" :key="day">
                <span class="weekday-text">{{ day }}</span>
              </div>
            </div>
            <div class="calendar-body">
              <div 
                v-for="date in calendarDates" 
                :key="date.key"
                class="calendar-date"
                :class="{ 
                  'other-month': date.isOtherMonth,
                  'today': date.isToday,
                  'has-data': date.emotionData,
                  'excellent-mood': date.emotionData && date.emotionData.avgMoodScore >= 9,
                  'good-mood': date.emotionData && date.emotionData.avgMoodScore >= 7 && date.emotionData.avgMoodScore < 9,
                  'normal-mood': date.emotionData && date.emotionData.avgMoodScore >= 5 && date.emotionData.avgMoodScore < 7,
                  'low-mood': date.emotionData && date.emotionData.avgMoodScore >= 3 && date.emotionData.avgMoodScore < 5,
                  'poor-mood': date.emotionData && date.emotionData.avgMoodScore < 3
                }"
                @click="showDateDetail(date)"
              >
                <div class="date-content">
                  <div class="date-number">{{ date.day }}</div>
                  <div v-if="date.emotionData" class="emotion-display">
                    <div class="emotion-emoji">{{ getEmotionEmoji(date.emotionData.dominantEmotion, date.emotionData.avgMoodScore) }}</div>
                  </div>
                  <div v-if="date.isToday" class="today-marker"></div>
                </div>
              </div>
            </div>
          </div>

          <div class="monthly-summary inner-glass">
            <div class="summary-cards">
              <div class="summary-card">
                <div class="card-icon"><i class="fas fa-crown" style="color:#F59E0B"></i></div>
                <div class="card-content">
                  <div class="card-label">最佳一天</div>
                  <div class="card-value">{{ bestDayOfMonth }}</div>
                </div>
              </div>
              <div class="summary-card">
                <div class="card-icon" style="font-size:20px;">{{ mostFrequentEmotionEmoji }}</div>
                <div class="card-content">
                  <div class="card-label">主要情绪</div>
                  <div class="card-value">{{ mostFrequentEmotion }}</div>
                </div>
              </div>
              <div class="summary-card">
                <div class="card-icon"><i class="fas fa-pen-nib" style="color:#3B82F6"></i></div>
                <div class="card-content">
                  <div class="card-label">记录天数</div>
                  <div class="card-value">{{ monthlyEmotionCount }} 天</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="currentMode === 'stream'" class="visualization-panel fade-in">
        <div class="panel-header">
          <h4><i class="fas fa-water"></i> 情绪流动图</h4>
        </div>
        <div class="stream-container inner-glass">
          <div ref="streamChart" class="stream-chart"></div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showDetailDialog" title="情绪碎片" width="400px" class="glass-dialog">
      <div v-if="selectedEmotionData" class="emotion-detail-content">
        <div class="detail-header">
          <div class="detail-date">{{ formatDetailDate(selectedEmotionData.date) }}</div>
          <div class="detail-emotion">
            {{ getEmotionEmoji(selectedEmotionData.dominantEmotion, selectedEmotionData.avgMoodScore) }}
            {{ selectedEmotionData.dominantEmotion }}
          </div>
        </div>
        <div class="detail-metrics">
          <div class="metric-item">
            <div class="metric-label">综合评分</div>
            <div class="metric-value" style="color:#3B82F6">{{ selectedEmotionData.avgMoodScore?.toFixed(1) || '0.0' }}</div>
          </div>
          <div class="metric-item">
            <div class="metric-label">倾诉次数</div>
            <div class="metric-value">{{ selectedEmotionData.recordCount || 0 }}</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  heatmapData: {
    type: Object,
    default: () => ({ gridData: [], emotionDistribution: {}, emotionTrend: [] })
  }
})

const currentMode = ref('calendar')
const showDetailDialog = ref(false)
const selectedEmotionData = ref(null)
const currentMonth = ref(new Date().getMonth())
const currentYear = ref(new Date().getFullYear())

const streamChart = ref(null)

const visualizationModes = [
  { key: 'calendar', name: '情绪日历', icon: 'fas fa-calendar-alt' },
  { key: 'stream', name: '情绪流谱', icon: 'fas fa-water' }
]

const monthlyEmotionCount = computed(() => calendarDates.value.filter(date => !date.isOtherMonth && date.emotionData).length)

const monthlyAvgMood = computed(() => {
  const currentMonthDates = calendarDates.value.filter(date => !date.isOtherMonth && date.emotionData)
  if (currentMonthDates.length === 0) return '0.0'
  const totalMood = currentMonthDates.reduce((sum, date) => sum + (date.emotionData.avgMoodScore || 0), 0)
  return (totalMood / currentMonthDates.length).toFixed(1)
})

const bestDayOfMonth = computed(() => {
  const currentMonthDates = calendarDates.value.filter(date => !date.isOtherMonth && date.emotionData)
  if (currentMonthDates.length === 0) return '-'
  const bestDay = currentMonthDates.reduce((best, date) => (date.emotionData.avgMoodScore || 0) > (best.emotionData?.avgMoodScore || 0) ? date : best)
  return `${bestDay.day}日`
})

const mostFrequentEmotion = computed(() => {
  const currentMonthDates = calendarDates.value.filter(date => !date.isOtherMonth && date.emotionData)
  if (currentMonthDates.length === 0) return '-'
  const emotionCount = {}
  currentMonthDates.forEach(date => {
    const emotion = date.emotionData.dominantEmotion
    if (emotion && emotion !== '未知') emotionCount[emotion] = (emotionCount[emotion] || 0) + 1
  })
  if (Object.keys(emotionCount).length === 0) return '-'
  const mostFrequent = Object.entries(emotionCount).reduce((a, b) => emotionCount[a[0]] > emotionCount[b[0]] ? a : b)
  return mostFrequent ? mostFrequent[0] : '-'
})

const mostFrequentEmotionEmoji = computed(() => {
  const currentMonthDates = calendarDates.value.filter(date => !date.isOtherMonth && date.emotionData && date.emotionData.dominantEmotion === mostFrequentEmotion.value)
  if (currentMonthDates.length === 0) return '☁️'
  const avgScore = currentMonthDates.reduce((sum, date) => sum + date.emotionData.avgMoodScore, 0) / currentMonthDates.length
  return getEmotionEmoji(mostFrequentEmotion.value, avgScore)
})

const currentMonthYear = computed(() => new Date(currentYear.value, currentMonth.value).toLocaleDateString('zh-CN', { year: 'numeric', month: 'long' }))

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const calendarDates = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value
  const firstDay = new Date(year, month, 1)
  const startDate = new Date(firstDay)
  startDate.setDate(startDate.getDate() - firstDay.getDay())
  
  const dates = []
  const currentDate = new Date(startDate)
  const today = new Date()
  
  for (let i = 0; i < 42; i++) {
    dates.push({
      key: `${currentDate.getFullYear()}-${currentDate.getMonth()}-${currentDate.getDate()}`,
      day: currentDate.getDate(),
      date: new Date(currentDate),
      isOtherMonth: currentDate.getMonth() !== month,
      isToday: currentDate.toDateString() === today.toDateString(),
      emotionData: getEmotionDataForDate(currentDate)
    })
    currentDate.setDate(currentDate.getDate() + 1)
  }
  return dates
})

const switchMode = (mode) => {
  currentMode.value = mode
  nextTick(() => { initCurrentModeChart() })
}

const getEmotionEmoji = (emotion, avgMoodScore) => {
  if (avgMoodScore === undefined || avgMoodScore === null) return ''
  const score = Number(avgMoodScore)
  if (score >= 9) return '🤩'
  if (score >= 8) return '😄'
  if (score >= 7) return '😊'
  if (score >= 6) return '🙂'
  if (score >= 5) return '😐'
  if (score >= 4) return '😔'
  if (score >= 3) return '😞'
  if (score >= 2) return '😢'
  return '😰'
}

const formatLocalDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const getEmotionDataForDate = (date) => {
  if (!props.heatmapData || !Array.isArray(props.heatmapData.emotionTrend)) return null
  const dateString = formatLocalDate(date)
  const emotionData = props.heatmapData.emotionTrend?.find(item => {
    let itemDate
    if (typeof item.date === 'string') itemDate = item.date.split('T')[0]
    else if (item.date instanceof Date) itemDate = formatLocalDate(item.date)
    else if (Array.isArray(item.date) && item.date.length >= 3) itemDate = `${item.date[0]}-${String(item.date[1]).padStart(2, '0')}-${String(item.date[2]).padStart(2, '0')}`
    else return false
    return itemDate === dateString
  })
  
  if (emotionData) {
    return {
      avgMoodScore: Number(emotionData.avgMoodScore) || 0,
      dominantEmotion: emotionData.dominantEmotion || '未知',
      recordCount: Number(emotionData.recordCount) || 1
    }
  }
  return null
}

const showDateDetail = (date) => {
  if (date.emotionData) {
    selectedEmotionData.value = { date: date.date, day: date.day, ...date.emotionData }
    showDetailDialog.value = true
  }
}

const formatDetailDate = (date) => {
  if (!date) return ''
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' })
}

const previousMonth = () => {
  if (currentMonth.value === 0) { currentMonth.value = 11; currentYear.value-- } else currentMonth.value--
}
const nextMonth = () => {
  if (currentMonth.value === 11) { currentMonth.value = 0; currentYear.value++ } else currentMonth.value++
}

const initCurrentModeChart = () => {
  if (currentMode.value === 'stream') initStreamChart()
}

const initStreamChart = () => {
  if (!streamChart.value) return
  const chart = echarts.init(streamChart.value)
  const streamData = []
  const emotions = Object.keys(props.heatmapData.emotionDistribution || {})
  
  emotions.forEach((emotion, index) => {
    const data = Array(24).fill().map((_, hour) => Math.random() * 50 + Math.sin(hour / 24 * Math.PI * 2) * 10)
    streamData.push({
      name: emotion, type: 'line', stack: 'emotion', smooth: 0.5,
      areaStyle: { opacity: 0.4 }, symbol: 'none', data
    })
  })
  
  chart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.9)', borderColor: 'transparent', textStyle: {color: '#334155'} },
    xAxis: { type: 'category', boundaryGap: false, data: Array(24).fill().map((_, i) => i.toString().padStart(2, '0') + ':00'), axisLine:{show:false}, axisTick:{show:false}, axisLabel:{color:'#94A3B8'} },
    yAxis: { type: 'value', splitLine: { show: false }, axisLabel: {show:false} },
    series: streamData,
    grid: { left: 0, right: 0, top: 20, bottom: 20 }
  })
}

onMounted(() => initCurrentModeChart())
watch(() => currentMode.value, () => nextTick(() => initCurrentModeChart()))
watch(() => props.heatmapData, () => nextTick(() => initCurrentModeChart()), { deep: true })
</script>

<style lang="scss" scoped>
/* 全局基础毛玻璃卡片 */
.glass-card {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(31, 38, 135, 0.03);
  overflow: hidden;
}

/* 内部嵌套的浅色玻璃区 */
.inner-glass {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  padding: 20px;
}

.visualization-selector {
  padding: 24px 30px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.05);

  .selector-header {
    text-align: center;
    margin-bottom: 20px;

    .selector-title {
      margin: 0 0 4px 0;
      font-size: 18px;
      font-weight: 700;
      color: #1E293B;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      i { color: #8B5CF6; }
    }
    .selector-subtitle { color: #94A3B8; font-size: 12px; }
  }

  .mode-tabs {
    display: flex;
    gap: 12px;
    justify-content: center;

    .mode-tab {
      background: rgba(59, 130, 246, 0.04);
      padding: 10px 24px;
      border-radius: 20px;
      cursor: pointer;
      transition: all 0.3s ease;
      color: #64748B;
      font-size: 14px;
      display: flex;
      align-items: center;
      gap: 8px;

      &:hover { background: rgba(59, 130, 246, 0.08); color: #3B82F6; }

      &.active {
        background: #fff;
        color: #3B82F6;
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
        font-weight: 600;
      }
    }
  }
}

.visualization-content {
  padding: 24px 30px;
}

.panel-header {
  margin-bottom: 20px;
  h4 {
    margin: 0;
    font-size: 16px;
    color: #334155;
    display: flex;
    align-items: center;
    gap: 8px;
    i { color: #3B82F6; }
  }
}

/* 日历样式重构 */
.calendar-controls {
  margin-bottom: 20px;
  .calendar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
    padding: 16px 20px;

    .nav-btn {
      background: #fff;
      border: 1px solid #E2E8F0;
      border-radius: 50%;
      width: 36px;
      height: 36px;
      color: #64748B;
      cursor: pointer;
      transition: 0.3s;
      &:hover { background: #F8FAFC; color: #3B82F6; border-color: #BFDBFE; }
    }

    .month-info {
      text-align: center;
      .month-title { margin: 0 0 4px 0; font-size: 18px; color: #1E293B; font-weight: 700; }
      .month-stats { font-size: 12px; color: #94A3B8; }
    }
  }

  .emotion-legend {
    padding: 12px 20px;
    .legend-items {
      display: flex; gap: 16px; justify-content: center; flex-wrap: wrap;
      .legend-item {
        display: flex; align-items: center; gap: 6px; font-size: 12px; color: #64748B;
        .legend-color {
          width: 12px; height: 12px; border-radius: 50%;
          &.excellent { background: #34D399; }
          &.good { background: #FBBF24; }
          &.normal { background: #94A3B8; }
          &.low { background: #F472B6; }
          &.poor { background: #9CA3AF; }
        }
      }
    }
  }
}

.calendar-grid {
  padding: 20px;
  margin-bottom: 20px;

  .weekday-header {
    display: grid; grid-template-columns: repeat(7, 1fr); gap: 8px; margin-bottom: 12px;
    .weekday {
      text-align: center; padding: 8px 0;
      .weekday-text { font-size: 13px; color: #94A3B8; font-weight: 600; }
    }
  }

  .calendar-body {
    display: grid; grid-template-columns: repeat(7, 1fr); gap: 8px;
    .calendar-date {
      aspect-ratio: 1;
      background: #fff;
      border-radius: 12px;
      cursor: pointer;
      transition: all 0.2s;
      border: 1px solid #F1F5F9;
      
      &:hover { transform: scale(1.05); box-shadow: 0 4px 12px rgba(0,0,0,0.05); z-index: 10; }
      &.other-month { opacity: 0.4; background: transparent; border-color: transparent;}
      &.today { 
        .today-marker { width: 4px; height: 4px; background: #EF4444; border-radius: 50%; margin: 2px auto 0; }
      }

      &.excellent-mood { background: rgba(52, 211, 153, 0.15); border-color: rgba(52, 211, 153, 0.3); }
      &.good-mood { background: rgba(251, 191, 36, 0.15); border-color: rgba(251, 191, 36, 0.3); }
      &.normal-mood { background: rgba(148, 163, 184, 0.1); border-color: rgba(148, 163, 184, 0.2); }
      &.low-mood { background: rgba(244, 114, 182, 0.15); border-color: rgba(244, 114, 182, 0.3); }
      &.poor-mood { background: rgba(156, 163, 175, 0.15); border-color: rgba(156, 163, 175, 0.3); }

      .date-content {
        padding: 6px; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: center;
        .date-number { font-size: 13px; font-weight: 600; color: #475569; position: absolute; top: 6px; left: 8px;}
        .emotion-display { margin-top: 10px; .emotion-emoji { font-size: 20px; } }
      }
    }
  }
}

.monthly-summary {
  padding: 20px;
  .summary-cards {
    display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 16px;
    .summary-card {
      display: flex; align-items: center; gap: 12px; background: #fff; padding: 16px; border-radius: 12px;
      .card-icon { width: 36px; height: 36px; background: #F8FAFC; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
      .card-content {
        .card-label { font-size: 12px; color: #94A3B8; margin-bottom: 2px;}
        .card-value { font-size: 15px; font-weight: 600; color: #1E293B; }
      }
    }
  }
}

.stream-container { padding: 20px; .stream-chart { height: 350px; } }

.fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

/* 弹窗覆盖 */
:deep(.glass-dialog) {
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(20px);
  .el-dialog__header { border-bottom: 1px solid #F1F5F9; padding: 20px 24px; }
  .el-dialog__title { font-weight: 600; color: #1E293B; }
}

.emotion-detail-content {
  .detail-header {
    text-align: center; padding: 20px; background: #F8FAFC; border-radius: 16px; margin-bottom: 20px;
    .detail-date { font-size: 14px; color: #64748B; margin-bottom: 8px;}
    .detail-emotion { font-size: 24px; font-weight: 600; color: #1E293B; }
  }
  .detail-metrics {
    display: flex; justify-content: space-around;
    .metric-item {
      text-align: center;
      .metric-label { font-size: 12px; color: #94A3B8; margin-bottom: 6px; }
      .metric-value { font-size: 20px; font-weight: 700; color: #475569; }
    }
  }
}
</style>