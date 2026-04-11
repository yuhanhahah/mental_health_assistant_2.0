<template>
  <div class="emotion-heatmap-container glass-card">
    <div class="heatmap-header">
      <div class="header-left">
        <h3 class="heatmap-title">
          <i class="fas fa-border-all"></i>
          心灵织锦 · 光谱阵
        </h3>
        <div class="heatmap-subtitle">横跨昼夜的光谱，编织出你独一无二的情绪纹理</div>
      </div>
      <div class="header-right">
        <div class="info-tag">
          <i class="fas fa-calendar-alt"></i> {{ dateRange }}
        </div>
        <div class="info-tag highlight">
          <i class="fas fa-fire-alt"></i> 活跃: {{ peakEmotionTime }}
        </div>
      </div>
    </div>
    
    <div class="heatmap-content">
      <div class="spectrum-matrix">
        <div class="matrix-x-axis">
          <div class="axis-corner"></div>
          <div v-for="hour in 24" :key="`hx-${hour}`" class="x-label">
            {{ hour % 4 === 0 || hour === 1 ? (hour - 1) + 'h' : '' }}
          </div>
        </div>

        <div class="matrix-body">
          <div v-for="(dayData, dayIndex) in processedGrid" :key="`day-${dayIndex}`" class="matrix-row">
            <div class="y-label">{{ ['周日','周一','周二','周三','周四','周五','周六'][dayIndex] }}</div>
            
            <div 
              v-for="(point, hourIndex) in dayData" 
              :key="`cell-${dayIndex}-${hourIndex}`"
              class="matrix-cell"
              :class="{ 
                'has-data': point.hasData,
                'dimmed': point.hasData && !emotionVisibility[point.dominantEmotion]
              }"
              :style="getCellStyle(point)"
              @mouseenter="handleMouseEnter($event, point, dayIndex, hourIndex)"
              @mouseleave="handleMouseLeave"
            >
              <div v-if="point.hasData" class="cell-inner-glow"></div>
            </div>
          </div>
        </div>
      </div>
      
      <div 
        v-if="hoveredData" 
        class="hover-tooltip glass-tooltip fade-in" 
        :style="{ left: mousePos.x + 'px', top: mousePos.y + 'px' }"
      >
        <div class="tooltip-header">
          <span class="tooltip-dot" :style="{ background: getEmotionColorString(hoveredData.dominantEmotion) }"></span>
          <span class="tooltip-emotion">{{ hoveredData.dominantEmotion }}</span>
        </div>
        <div class="tooltip-body">
          <div class="stat-row">
            <span class="stat-label">感知强度</span>
            <span class="stat-val score">{{ (hoveredData.avgMoodScore || 0).toFixed(1) }}</span>
          </div>
          <div class="stat-row">
            <span class="stat-label">记录次数</span>
            <span class="stat-val">{{ hoveredData.value }} 次</span>
          </div>
        </div>
        <div class="tooltip-footer">
          {{ ['周日','周一','周二','周三','周四','周五','周六'][hoveredData.day] }} {{ hoveredData.hour }}:00
        </div>
      </div>
    </div>

    <div class="emotion-distribution">
      <h4 class="distribution-title">
        <i class="fas fa-fingerprint"></i>
        情绪光谱印记
      </h4>
      <div class="emotion-garden">
        <div 
          v-for="(count, emotion) in emotionDistribution" 
          :key="emotion"
          class="emotion-aura"
          :class="{ 'hidden': !emotionVisibility[emotion] }"
          :style="{ 
            '--aura-size': Math.min(count / maxEmotionCount * 1.2 + 0.8, 1.8),
            '--emotion-color': getEmotionColorString(emotion)
          }"
          @click="toggleEmotionVisibility(emotion)"
        >
          <div class="aura-core"></div>
          <div class="aura-ring ring-1"></div>
          <div class="aura-ring ring-2"></div>
          <div class="aura-info">
            <span class="emotion-name">{{ emotion }}</span>
            <span class="emotion-count">{{ count }}次</span>
          </div>
          <div class="visibility-indicator">
            <i :class="emotionVisibility[emotion] ? 'fas fa-eye' : 'fas fa-eye-slash'"></i>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  heatmapData: {
    type: Object,
    default: () => ({
      gridData: [],
      emotionDistribution: {},
      peakEmotionTime: '00:00',
      dateRange: ''
    })
  }
})

// 状态管理
const hoveredData = ref(null)
const mousePos = ref({ x: 0, y: 0 }) 
const emotionVisibility = ref({})
const emotionColorMap = ref({})

// 计算属性
const dateRange = computed(() => props.heatmapData.dateRange || '暂无数据')
const peakEmotionTime = computed(() => props.heatmapData.peakEmotionTime || '00:00')
const emotionDistribution = computed(() => props.heatmapData.emotionDistribution || {})
const maxEmotionCount = computed(() => {
  const counts = Object.values(emotionDistribution.value)
  return counts.length > 0 ? Math.max(...counts) : 1
})

// 生成马卡龙明亮色系（纯正的 CSS 颜色，绝对不可能发灰）
const generateVibrantColor = () => {
  const hue = Math.random() 
  const saturation = 0.75 + Math.random() * 0.25 
  const lightness = 0.6 + Math.random() * 0.15   
  return { h: hue, s: saturation, l: lightness }
}

const initColorsAndVisibility = () => {
  const vMap = {}
  const cMap = {}
  Object.keys(emotionDistribution.value).forEach(emotion => {
    vMap[emotion] = true 
    cMap[emotion] = generateVibrantColor()
  })
  emotionVisibility.value = vMap
  emotionColorMap.value = cMap
}

const getEmotionColorString = (emotion) => {
  const c = emotionColorMap.value[emotion] || { h: 0.6, s: 0.5, l: 0.8 }
  return `hsl(${c.h * 360}, ${c.s * 100}%, ${c.l * 100}%)`
}

const toggleEmotionVisibility = (emotion) => {
  emotionVisibility.value[emotion] = !emotionVisibility.value[emotion]
}

// 【真相大白的地方】：正确处理网格数据，使用 .value > 0
const processedGrid = computed(() => {
  const rawData = props.heatmapData.gridData
  const grid = []
  for (let day = 0; day < 7; day++) {
    const dayRow = []
    for (let hour = 0; hour < 24; hour++) {
      let cellData = { hasData: false }
      // 核心修复：原来错写成了 recordCount，现在改回真实的 value
      if (rawData && rawData[day] && rawData[day][hour] && rawData[day][hour].value > 0) {
        cellData = {
          hasData: true,
          ...rawData[day][hour], 
          day,
          hour
        }
      }
      dayRow.push(cellData)
    }
    grid.push(dayRow)
  }
  return grid
})

// 动态计算每个胶囊的样式
const getCellStyle = (point) => {
  if (!point.hasData) return {}
  
  const colorStr = getEmotionColorString(point.dominantEmotion)
  const isVisible = emotionVisibility.value[point.dominantEmotion]
  
  // 评分越高，颜色越深邃
  const intensity = 0.4 + (point.avgMoodScore / 10) * 0.6 
  
  if (!isVisible) {
    return { background: 'rgba(226, 232, 240, 0.5)' }
  }

  return {
    '--cell-color': colorStr,
    background: colorStr,
    opacity: intensity,
    boxShadow: `0 2px 10px ${colorStr.replace('hsl', 'hsla').replace(')', ', 0.4)')}`
  }
}

// 悬浮交互
const handleMouseEnter = (event, point, day, hour) => {
  if (!point.hasData) return
  
  const rect = event.target.getBoundingClientRect()
  const containerRect = event.target.closest('.heatmap-content').getBoundingClientRect()
  
  mousePos.value = {
    x: rect.left - containerRect.left + rect.width / 2,
    y: rect.top - containerRect.top - 10
  }
  hoveredData.value = point
}

const handleMouseLeave = () => {
  hoveredData.value = null
}

watch(() => props.heatmapData, () => {
  initColorsAndVisibility()
}, { deep: true, immediate: true })

</script>

<style lang="scss" scoped>
.glass-card {
  background: rgba(255, 255, 255, 0.7); 
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 24px;
  box-shadow: 0 10px 40px rgba(31, 38, 135, 0.04);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.heatmap-header {
  padding: 24px 30px;
  border-bottom: 1px solid rgba(59, 130, 246, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  
  .header-left {
    .heatmap-title {
      margin: 0 0 8px 0;
      font-size: 20px;
      font-weight: 700;
      color: #1E293B;
      display: flex;
      align-items: center;
      gap: 10px;
      i { color: #3B82F6; }
    }
    .heatmap-subtitle {
      font-size: 13px;
      color: #64748B;
    }
  }
  
  .header-right {
    display: flex;
    gap: 12px;
    
    .info-tag {
      display: flex;
      align-items: center;
      gap: 6px;
      background: rgba(241, 245, 249, 0.8);
      padding: 6px 14px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      color: #64748B;
      border: 1px solid #E2E8F0;
      
      &.highlight {
        background: rgba(59, 130, 246, 0.08);
        color: #3B82F6;
        border-color: rgba(59, 130, 246, 0.2);
      }
    }
  }
}

.heatmap-content {
  padding: 40px 30px;
  position: relative;
  background: radial-gradient(circle at center, rgba(255,255,255,0.8) 0%, rgba(248,250,252,0.4) 100%);
  display: flex;
  justify-content: center;
}

.spectrum-matrix {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 900px; 

  .matrix-x-axis {
    display: flex;
    margin-bottom: 8px;
    .axis-corner { width: 40px; flex-shrink: 0; } 
    .x-label {
      flex: 1;
      text-align: center;
      font-size: 11px;
      color: #94A3B8;
      font-weight: 500;
    }
  }

  .matrix-body {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .matrix-row {
    display: flex;
    align-items: center;
    height: 24px; 
    
    .y-label {
      width: 40px;
      flex-shrink: 0;
      font-size: 12px;
      color: #64748B;
      font-weight: 600;
      text-align: right;
      padding-right: 12px;
    }

    .matrix-cell {
      flex: 1;
      height: 100%;
      margin: 0 3px;
      border-radius: 12px; 
      background: rgba(226, 232, 240, 0.4); 
      border: 1px solid rgba(255, 255, 255, 0.5);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;
      overflow: hidden;

      &.has-data {
        cursor: pointer;
        border-color: rgba(255, 255, 255, 0.8);
        animation: floatBreath 4s infinite alternate ease-in-out;
        
        .cell-inner-glow {
          position: absolute;
          top: 0; left: 0; right: 0; bottom: 0;
          background: linear-gradient(135deg, rgba(255,255,255,0.4) 0%, rgba(255,255,255,0) 100%);
          border-radius: inherit;
        }

        &:hover {
          transform: scaleY(1.6) scaleX(1.1);
          z-index: 10;
          box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1), 0 0 15px var(--cell-color) !important;
          border-color: #fff;
          opacity: 1 !important;
        }
      }

      &.dimmed {
        opacity: 0.1 !important;
        filter: grayscale(100%);
      }
    }
  }
}

@keyframes floatBreath {
  0% { transform: scale(1); }
  100% { transform: scale(1.05); }
}
.matrix-row:nth-child(even) .matrix-cell.has-data { animation-delay: -2s; }
.matrix-cell:nth-child(even) { animation-duration: 5s !important; }

.glass-tooltip {
  position: absolute;
  transform: translate(-50%, -100%);
  margin-top: -15px; 
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 1);
  box-shadow: 0 10px 30px rgba(31, 38, 135, 0.1);
  border-radius: 16px;
  padding: 16px;
  width: 160px;
  pointer-events: none;
  z-index: 100;

  &::after {
    content: '';
    position: absolute;
    bottom: -6px;
    left: 50%;
    margin-left: -6px;
    width: 12px;
    height: 12px;
    background: rgba(255, 255, 255, 0.9);
    border-right: 1px solid rgba(255, 255, 255, 1);
    border-bottom: 1px solid rgba(255, 255, 255, 1);
    transform: rotate(45deg);
  }

  .tooltip-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    padding-bottom: 10px;
    border-bottom: 1px solid #F1F5F9;
    
    .tooltip-dot {
      width: 10px; height: 10px; border-radius: 50%;
      box-shadow: 0 0 8px currentColor;
    }
    .tooltip-emotion { font-size: 15px; font-weight: 700; color: #1E293B; }
  }

  .tooltip-body {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 12px;

    .stat-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      .stat-label { font-size: 12px; color: #64748B; }
      .stat-val { font-size: 12px; font-weight: 600; color: #334155; }
      .score { color: #3B82F6; font-size: 14px; }
    }
  }

  .tooltip-footer {
    font-size: 11px;
    color: #94A3B8;
    text-align: center;
  }
}

.fade-in { animation: softFade 0.2s cubic-bezier(0.4, 0, 0.2, 1); }
@keyframes softFade {
  from { opacity: 0; transform: translate(-50%, -90%) scale(0.9); }
  to { opacity: 1; transform: translate(-50%, -100%) scale(1); }
}

.emotion-distribution {
  padding: 20px 30px 30px;
  border-top: 1px solid rgba(59, 130, 246, 0.05);
  background: rgba(255, 255, 255, 0.4);
  
  .distribution-title {
    margin: 0 0 25px 0;
    color: #334155;
    font-size: 15px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
    i { color: #8B5CF6; }
  }
  
  .emotion-garden {
    display: flex;
    flex-wrap: wrap;
    gap: 30px;
    justify-content: center;
  }
  
  .emotion-aura {
    position: relative;
    width: calc(45px * var(--aura-size));
    height: calc(45px * var(--aura-size));
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    
    &.hidden { opacity: 0.4; filter: grayscale(100%); }
    
    .aura-core {
      position: absolute;
      width: 35%; height: 35%;
      background: var(--emotion-color);
      border-radius: 50%;
      box-shadow: 0 0 20px var(--emotion-color), inset 0 0 8px #fff;
      z-index: 2;
      transition: 0.3s;
    }
    
    &:hover .aura-core { transform: scale(1.2); }
    
    .aura-ring {
      position: absolute;
      width: 100%; height: 100%;
      border-radius: 50%;
      background: radial-gradient(circle, var(--emotion-color) 0%, transparent 70%);
      opacity: 0.5;
      z-index: 1;
      
      &.ring-1 { animation: pulseAura 3s ease-in-out infinite alternate; }
      &.ring-2 { width: 75%; height: 75%; animation: pulseAura 4s ease-in-out infinite alternate-reverse; }
    }
    
    .aura-info {
      position: absolute;
      bottom: -28px;
      text-align: center;
      width: 100%;
      .emotion-name { display: block; font-size: 12px; font-weight: 600; color: #475569; }
      .emotion-count { display: block; font-size: 11px; color: #94A3B8; }
    }
    
    .visibility-indicator {
      position: absolute;
      top: -5px; right: -5px;
      z-index: 3;
      background: #fff;
      border-radius: 50%;
      width: 22px; height: 22px;
      display: flex; align-items: center; justify-content: center;
      font-size: 10px; color: #94A3B8;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      transition: 0.3s;
    }
    &:hover .visibility-indicator { color: #3B82F6; }
  }
}

@keyframes pulseAura {
  0% { transform: scale(0.8); opacity: 0.4; }
  100% { transform: scale(1.2); opacity: 0.7; }
}

@media (max-width: 768px) {
  .heatmap-header { flex-direction: column; gap: 16px; }
  .spectrum-matrix { overflow-x: auto; padding-bottom: 20px; }
  .matrix-body { min-width: 600px; }
}
</style>