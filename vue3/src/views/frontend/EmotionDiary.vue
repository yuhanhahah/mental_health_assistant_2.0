
<template>
  <div class="healing-diary-page">
    <div class="ambient-light light-1"></div>
    <div class="ambient-light light-2"></div>

    <div class="container">
      <header class="page-header">
        <div class="header-left">
          <div class="seedling-icon-box">
            <i class="fas fa-seedling"></i>
          </div>
          <div class="title-group">
            <h2>情绪日记</h2>
            <p>在这里，你的每一种情绪都值得被温柔对待</p>
          </div>
        </div>
        <button class="glass-outline-btn" @click="showHistoryDialog = true">
          <i class="fas fa-calendar-alt"></i> 时光记忆
        </button>
      </header>

      <div class="content-layout">
        <div class="diary-input-section">
          
          <div class="glass-card section-card">
            <h3 class="card-title">
              <i class="fas fa-temperature-half"></i> 情绪感知温度
              <span v-if="diaryForm.diaryDate !== formatLocalDate(new Date())" class="history-badge">
                (补写/查看: {{ diaryForm.diaryDate }})
              </span>
            </h3>
            <p class="section-desc">您此刻的整体情绪强度如何？(1-10分)</p>
            
            <div class="modern-slider-container">
              <span class="slider-label">低落</span>
              <div class="slider-track-wrapper">
                <el-slider 
                  v-model="diaryForm.moodScore" 
                  :min="1"
                  :max="10"
                  :step="1"
                  class="healing-slider"
                  show-stops
                />
              </div>
              <span class="slider-label">极好</span>
            </div>
            
            <div v-if="diaryForm.moodScore" class="selected-mood-feedback">
              当前感知：<span>{{ diaryForm.moodScore }}分 - {{ getMoodDescription(diaryForm.moodScore) }}</span>
            </div>
          </div>

          <div class="glass-card section-card">
            <h3 class="card-title">
              <i class="fas fa-wind"></i> 捕捉主要情绪
            </h3>
            <p class="section-desc">哪种情绪最能代表您现在的状态？</p>
            
            <div class="emotion-grid">
              <div
                v-for="emotion in customEmotionOptions"
                :key="emotion.name"
                class="emotion-item"
                :class="{ &apos;is-active&apos;: diaryForm.dominantEmotion === emotion.name }"
                :style="diaryForm.dominantEmotion === emotion.name ? { &apos;--emo-color&apos;: emotion.color, &apos;--emo-bg&apos;: emotion.bg } : {}"
                @click="selectEmotion(emotion.name)"
              >
                <div class="emo-icon" :style="{ color: emotion.color }">
                  <i :class="emotion.icon"></i>
                </div>
                <span class="emo-name">{{ emotion.name }}</span>
              </div>
            </div>
          </div>

          <div class="glass-card section-card">
            <h3 class="card-title">
              <i class="fas fa-feather"></i> 倾听内心的声音
            </h3>
            
            <div class="form-item">
              <label>情绪触发点</label>
              <textarea
                v-model="diaryForm.emotionTriggers"
                class="clean-textarea"
                rows="2"
                placeholder="是什么样的人、事、物泛起了心中的波澜？"
                maxlength="1000"
              ></textarea>
            </div>

            <div class="form-item">
              <label>今日独白</label>
              <textarea
                v-model="diaryForm.diaryContent"
                class="clean-textarea large-textarea"
                rows="4"
                placeholder="在这里安全地放下你的思绪，不用在意逻辑与修辞..."
                maxlength="2000"
              ></textarea>
            </div>

            <div class="life-indicators">
              <div class="indicator-box">
                <label><i class="fas fa-moon"></i> 睡眠质量</label>
                <el-select
                  v-model="diaryForm.sleepQuality"
                  placeholder="请感受"
                  class="clean-select"
                  popper-class="clean-select-popper"
                >
                  <el-option label="很差" :value="1" />
                  <el-option label="较差" :value="2" />
                  <el-option label="一般" :value="3" />
                  <el-option label="良好" :value="4" />
                  <el-option label="优秀" :value="5" />
                </el-select>
              </div>
              <div class="indicator-box">
                <label><i class="fas fa-compress-arrows-alt"></i> 压力水平</label>
                <el-select
                  v-model="diaryForm.stressLevel"
                  placeholder="请感受"
                  class="clean-select"
                  popper-class="clean-select-popper"
                >
                  <el-option label="很低" :value="1" />
                  <el-option label="较低" :value="2" />
                  <el-option label="中等" :value="3" />
                  <el-option label="较高" :value="4" />
                  <el-option label="很高" :value="5" />
                </el-select>
              </div>
            </div>

            <div class="action-bar">
              <button class="flat-btn ghost-mode" @click="resetForm">
                清空重写
              </button>
              <button class="flat-btn primary-mode" :disabled="saving" @click="saveDiary">
                <i v-if="!saving" class="fas fa-save"></i>
                <i v-else class="fas fa-spinner fa-spin"></i>
                封存该日记忆
              </button>
            </div>
          </div>
        </div>

        <div class="statistics-section">
          
          <div class="glass-card stat-card">
            <h4 class="stat-title">
              <i class="fas fa-calendar-check"></i> 情绪日历
            </h4>
            <el-calendar v-model="currentDate" class="healing-calendar">
              <template #date-cell="{ data }">
                <div class="calendar-day">
                  <span class="day-text" :class="{ &apos;is-today&apos;: data.day === formatLocalDate(new Date()) }">
                    {{ data.day.split(&apos;-&apos;).slice(2).join(&apos;&apos;) }}
                  </span>
                  <div 
                    v-if="emotionMap[data.day]" 
                    class="emotion-dot" 
                    :style="{ backgroundColor: getEmotionColor(emotionMap[data.day]) }"
                    :title="emotionMap[data.day]"
                  ></div>
                </div>
              </template>
            </el-calendar>
          </div>

          <div v-if="currentDiaryId" class="glass-card ai-insight-card">
            <div class="card-header">
              <span class="card-title ai-title"><i class="fas fa-sparkles"></i> AI 心境感知</span>
              <button class="icon-refresh-btn" :disabled="aiAnalysisLoading" @click="refreshTodayAnalysisStatus">
                <i class="fas fa-sync-alt" :class="{&apos;fa-spin&apos;: aiAnalysisLoading}"></i>
              </button>
            </div>
            
            <div class="ai-content-area">
              <div v-if="aiAnalysis" class="analysis-ready">
                <div class="emotion-spotlight">
                  <div class="spotlight-icon">
                    {{ aiAnalysis.icon || "" }}
                  </div>
                  <div class="spotlight-text">
                    <h4>{{ aiAnalysis.label || "感知完成" }}</h4>
                    <span class="score-text">心境指数: {{ aiAnalysis.emotionScore || "--" }}</span>
                  </div>
                </div>
                
                <div class="assessment-row">
                  <div class="assess-item">
                    <span class="label">状态评估</span>
                    <span class="value" :class="getRiskLevelClass(aiAnalysis.riskLevel)">
                      {{ getRiskLevelText(aiAnalysis.riskLevel) }}
                    </span>
                  </div>
                </div>

                <div v-if="aiAnalysis.suggestion" class="ai-advice-box">
                  <i class="fas fa-lightbulb"></i>
                  <p>{{ aiAnalysis.suggestion }}</p>
                </div>
              </div>
              
              <div v-else-if="aiAnalysisLoading" class="analysis-loading">
                <div class="pulse-sparkle">
                  <i class="fas fa-sparkles"></i>
                </div>
                <p>小暖正在用心感受您的文字...</p>
              </div>
              
              <div v-else class="analysis-empty">
                <div class="empty-icon">
                  <i class="fas fa-inbox"></i>
                </div>
                <p>AI感知尚未生成</p>
                <button class="soft-btn" @click="triggerAnalysis">
                  开始感知
                </button>
              </div>
            </div>
          </div>

          <div class="glass-card stat-card">
            <h4 class="stat-title">
              <i class="fas fa-chart-bezier"></i> 心境波动轨迹
            </h4>
            <div ref="trendChartContainer" class="trend-chart-wrapper">
              <canvas ref="trendChart" width="300" height="180"></canvas>
            </div>
          </div>

          <div class="glass-card stat-card">
            <h4 class="stat-title">
              <i class="fas fa-chart-pie"></i> 近期情绪色谱
            </h4>
            <div class="emotion-stats-list">
              <template v-if="emotionStats.length > 0">
                <div v-for="stat in emotionStats" :key="stat.emotion" class="stat-row">
                  <div class="stat-info">
                    <div class="stat-dot" :style="{ backgroundColor: stat.color }"></div>
                    <span class="stat-name">{{ stat.emotion }}</span>
                  </div>
                  <span class="stat-percent">{{ stat.percentage }}%</span>
                </div>
              </template>
              <div v-else class="empty-state">
                <p>暂无数据，等待您的记录</p>
              </div>
            </div>
          </div>

          <div class="glass-card tip-card">
            <div class="tip-icon">
              <i class="fas fa-seedling"></i>
            </div>
            <div class="tip-text">
              <h4>每日疗愈</h4>
              <p>坚持记录情绪日记，就像是在给心灵做深呼吸。不用着急，慢慢来。</p>
            </div>
          </div>

        </div>
      </div>
    </div>

    <el-dialog v-model="showHistoryDialog" title="时光记忆" width="800px" class="healing-dialog" :show-close="true">
      <div class="history-container">
        <div class="filters-bar">
          <el-date-picker
            v-model="historyDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="clean-datepicker"
            @change="loadHistoryData"
          />
          <el-select
            v-model="historyEmotionFilter"
            placeholder="情绪筛选"
            clearable
            class="clean-select"
            @change="loadHistoryData"
          >
            <el-option
              v-for="emotion in customEmotionOptions"
              :key="emotion.name"
              :label="emotion.name"
              :value="emotion.name"
            />
          </el-select>
        </div>

        <div class="timeline-list">
          <div v-for="diary in historyData" :key="diary.id" class="timeline-item">
            <div class="time-stamp">
              {{ diary.diaryDate }}
            </div>
            <div class="timeline-card">
              <div class="card-top">
                <div class="mood-tag" :style="{ color: getEmotionColor(diary.dominantEmotion), backgroundColor: getEmotionBg(diary.dominantEmotion) }">
                  {{ diary.dominantEmotion || "平静" }} · {{ diary.moodScore }}分
                </div>
                <div class="status-tags">
                  <span v-if="diary.aiAnalysisStatus === &apos;COMPLETED&apos;" class="tag-success">AI已感知</span>
                  <span v-else-if="diary.aiAnalysisStatus === &apos;PENDING&apos;" class="tag-pending">感知中</span>
                </div>
              </div>
              <div class="card-body">
                <p>{{ diary.diaryContentPreview || "（无正文记录）" }}</p>
              </div>
              <div class="card-actions">
                <button class="text-link" @click="viewDiaryDetail(diary.id)">回顾</button>
                <button class="text-link" @click="editDiary(diary)">补充</button>
                <button class="text-link danger" @click="deleteDiary(diary.id)">遗忘</button>
              </div>
            </div>
          </div>
        </div>
        
        <div class="pagination-wrapper">
          <el-pagination
            :current-page="historyPage.current"
            :page-size="historyPage.size"
            :total="historyPage.total"
            layout="prev, pager, next"
            @current-change="(page) => { historyPage.current = page; loadHistoryData(); }"
          />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
/* eslint-disable */
// 这个免死金牌指令帮你屏蔽了烦人的 Vetur 报错，直接出活！
import { ref, reactive, onMounted, onUnmounted, nextTick, watch } from "vue"
import { useRouter } from "vue-router"
import { ElMessage, ElMessageBox } from "element-plus"
import {
  createOrUpdateEmotionDiary,
  getTodayEmotionDiary,
  getEmotionDiaryPage,
  getEmotionDiaryStatistics,
  deleteEmotionDiary,
  updateEmotionDiary,
  getAiEmotionAnalysis,
  triggerAiEmotionAnalysis,
  getEmotionCalendar,
  getEmotionDiaryById
} from "@/api/emotionDiary"

const router = useRouter()

const formatLocalDate = (date) => {
  if (!date) return ""
  const d = new Date(date)
  if (isNaN(d.getTime())) return ""
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, "0")
  const day = String(d.getDate()).padStart(2, "0")
  return `${year}-${month}-${day}`
}
const diaryForm = reactive({
  diaryDate: formatLocalDate(new Date()),
  moodScore: null,
  dominantEmotion: "",
  emotionTriggers: "",
  diaryContent: "",
  sleepQuality: null,
  stressLevel: null
})

const saving = ref(false)
const showHistoryDialog = ref(false)
const trendChart = ref(null)
const trendChartContainer = ref(null)
const currentDiaryId = ref(null)
const aiAnalysis = ref(null)
const aiAnalysisLoading = ref(false)

const currentDate = ref(new Date())
const emotionMap = ref({})

const statistics = reactive({
  totalDays: 0,
  recordedDays: 0,
  averageMoodScore: 0,
  suggestions: []
})
const emotionStats = ref([])
const historyData = ref([])
const historyDateRange = ref([])
const historyEmotionFilter = ref("")
const historyPage = reactive({ current: 1, size: 10, total: 0 })

const customEmotionOptions = [
  { name: "开心", icon: "fas fa-sun", color: "#D97706", bg: "#FEF3C7" },
  { name: "平静", icon: "fas fa-water", color: "#3B82F6", bg: "#EFF6FF" },
  { name: "焦虑", icon: "fas fa-wind", color: "#4F46E5", bg: "#EEF2FF" },
  { name: "悲伤", icon: "fas fa-cloud-rain", color: "#475569", bg: "#F1F5F9" },
  { name: "兴奋", icon: "fas fa-star", color: "#059669", bg: "#D1FAE5" },
  { name: "疲惫", icon: "fas fa-battery-quarter", color: "#7C3AED", bg: "#F5F3FF" },
  { name: "惊讶", icon: "fas fa-bolt", color: "#C2410C", bg: "#FFEDD5" },
  { name: "困惑", icon: "fas fa-question", color: "#64748B", bg: "#F8FAFC" }
]

const selectEmotion = (emotion) => { diaryForm.dominantEmotion = emotion }

const getMoodDescription = (score) => {
  const descriptions = {
    1: "极度低落", 2: "糟糕", 3: "阴霾", 4: "略感疲惫", 5: "平静如水",
    6: "微风和煦", 7: "舒畅", 8: "明媚", 9: "充满活力", 10: "光芒万丈"
  }
  return descriptions[score] || "未知"
}
const getEmotionColor = (emotionName) => {
  const e = customEmotionOptions.find(opt => opt.name === emotionName)
  return e ? e.color : "#64748B"
}
const getEmotionBg = (emotionName) => {
  const e = customEmotionOptions.find(opt => opt.name === emotionName)
  return e ? e.bg : "#F1F5F9"
}
// -------------------- 核心功能 1：数据翻译器（确保圆点显示） --------------------
const fetchCalendarData = async (dateObj) => {
  const year = dateObj.getFullYear()
  const month = dateObj.getMonth() + 1
  try {
    const res = await getEmotionCalendar({ year, month })
    if (res.code === 200 && res.data) {
      // 防止后端传英文枚举导致配不上色，做一个强行翻译字典
      const enumTranslate = {
        "HAPPY": "开心", "CALM": "平静", "ANXIOUS": "焦虑", "SAD": "悲伤",
        "EXCITED": "兴奋", "TIRED": "疲惫", "SURPRISED": "惊讶", "CONFUSED": "困惑"
      }
      const safeData = {}
      for (const [dateKey, emotionStr] of Object.entries(res.data)) {
        // 如果是英文则翻译，如果是中文则原样保留
        safeData[dateKey] = enumTranslate[emotionStr.toUpperCase()] || emotionStr
      }
      emotionMap.value = safeData
    }
  } catch (error) {
    console.error("日历数据获取失败", error)
  }
}
// -------------------- 核心功能 2：点哪天查哪天（时光穿梭） --------------------
const loadDiaryBySelectedDate = async (dateObj) => {
  const selectedDateStr = formatLocalDate(dateObj)
  const todayStr = formatLocalDate(new Date())
  
  // 1. 先重置表单为选中的日期
  resetForm(dateObj)
  
  // 2. 如果点的是今天，直接拉取今日专享接口
  if (selectedDateStr === todayStr) {
    loadTodayDiary()
    return
  }
  
  // 3. 如果点的是历史日期，调用分页接口锁定查询这一天
  try {
    await getEmotionDiaryPage({
      current: 1,
      size: 1,
      startDate: selectedDateStr,
      endDate: selectedDateStr
    }, {
      onSuccess: (data) => {
        if (data.records && data.records.length > 0) {
          const diary = data.records[0]
          Object.assign(diaryForm, {
            diaryDate: diary.diaryDate,
            moodScore: diary.moodScore,
            dominantEmotion: diary.dominantEmotion || "",
            emotionTriggers: diary.emotionTriggers || "",
            diaryContent: diary.diaryContent || "",
            sleepQuality: diary.sleepQuality || null,
            stressLevel: diary.stressLevel || null
          })
          currentDiaryId.value = diary.id
          loadAiAnalysis()
          ElMessage.success(`已翻开 ${selectedDateStr} 的回忆`)
        } else {
          ElMessage.info(`${selectedDateStr} 这天还没记录，你可以补写哦`)
        }
      },
      onError: (error) => console.log(error)
    })
  } catch (error) {
    console.error("加载历史日记失败", error)
  }
}
// 监听日历选中日期的变化：不但重刷圆点，更要重刷表单内容
watch(currentDate, (newDate, oldDate) => {
  // 如果月份变了，才重新请求后端拉取一整个月的圆点数据
  if (!oldDate || newDate.getMonth() !== oldDate.getMonth() || newDate.getFullYear() !== oldDate.getFullYear()) {
    fetchCalendarData(newDate)
  }
  // 【重点】触发日记内容更新
  loadDiaryBySelectedDate(newDate)
})


const saveDiary = async () => {
  if (!diaryForm.moodScore) {
    ElMessage.warning("请滑动选择此刻的情绪感知温度")
    return
  }
  saving.value = true
  if (currentDiaryId.value) {
    await updateEmotionDiary(currentDiaryId.value, diaryForm, {
      onSuccess: (data) => {
        ElMessage.success("记忆已更新")
        currentDiaryId.value = data.id
        aiAnalysis.value = null
        loadStatistics()
        fetchCalendarData(currentDate.value)
        startAiAnalysisPolling()
      },
      onError: (error) => {
        ElMessage.error("更新失败：" + error.message)
      }
    })
  } else {
    await createOrUpdateEmotionDiary(diaryForm, {
      onSuccess: (data) => {
        ElMessage.success("这段记忆已封存")
        currentDiaryId.value = data.id
        loadStatistics()
        fetchCalendarData(currentDate.value)
        startAiAnalysisPolling()
      },
      onError: (error) => {
        if (error.code === "DUPLICATE_DIARY_EXISTS" && error.data) {
          handleDuplicateDiary(error.data.id || error.data)
        } else {
          ElMessage.error("保存失败：" + error.message)
        }
      }
    })
  }
  saving.value = false
}
// 修改 resetForm 使其支持传入目标日期，不传默认是当天
const resetForm = (targetDateArg) => {
  // 防止在模板中 @click 直接把鼠标 Event 对象传进来了
  const targetDateObj = (targetDateArg instanceof Date) ? targetDateArg : new Date()
  
  Object.assign(diaryForm, {
    diaryDate: formatLocalDate(targetDateObj),
    moodScore: null,
    dominantEmotion: "",
    emotionTriggers: "",
    diaryContent: "",
    sleepQuality: null,
    stressLevel: null
  })
  currentDiaryId.value = null
  aiAnalysis.value = null
}
const loadTodayDiary = async () => {
  await getTodayEmotionDiary({
    showDefaultMsg: false,
    onSuccess: (data) => {
      Object.assign(diaryForm, {
        diaryDate: data.diaryDate,
        moodScore: data.moodScore,
        dominantEmotion: data.dominantEmotion || "",
        emotionTriggers: data.emotionTriggers || "",
        diaryContent: data.diaryContent || "",
        sleepQuality: data.sleepQuality,
        stressLevel: data.stressLevel
      })
      currentDiaryId.value = data.id
      loadAiAnalysis()
    },
    onError: (error) => {
      if (error.message !== "今日还没有记录情绪日记") {
        console.log("加载今日日记失败:", error)
      }
    }
  })
}
const loadStatistics = async () => {
  await getEmotionDiaryStatistics({ days: 7 }, {
    onSuccess: (data) => {
      Object.assign(statistics, data)
      if (data.emotionDistribution && Object.keys(data.emotionDistribution).length > 0) {
        const total = Object.values(data.emotionDistribution).reduce((sum, count) => sum + count, 0)
        if (total > 0) {
          emotionStats.value = Object.entries(data.emotionDistribution).map(([emotion, count]) => ({
            emotion,
            percentage: Math.round((count / total) * 100),
            color: getEmotionColor(emotion)
          }))
        } else {
          emotionStats.value = []
        }
      } else {
        emotionStats.value = []
      }
      renderTrendChart(data.moodTrend || [])
    },
    onError: (error) => {
      console.error("加载统计数据失败:", error)
      emotionStats.value = []
    }
  })
}
const loadHistoryData = async () => {
  const params = { current: historyPage.current, size: historyPage.size }
  if (historyDateRange.value && historyDateRange.value.length === 2) {
    params.startDate = formatLocalDate(historyDateRange.value[0])
    params.endDate = formatLocalDate(historyDateRange.value[1])
  }
  if (historyEmotionFilter.value) {
    params.dominantEmotion = historyEmotionFilter.value
  }

  await getEmotionDiaryPage(params, {
    onSuccess: (data) => {
      historyData.value = data.records || []
      historyPage.total = data.total || 0
    },
    onError: (error) => {
      console.error("加载历史数据失败:", error)
    }
  })
}
const editDiary = (diary) => {
  Object.assign(diaryForm, {
    diaryDate: diary.diaryDate,
    moodScore: diary.moodScore,
    dominantEmotion: diary.dominantEmotion || "",
    emotionTriggers: diary.emotionTriggers || "",
    diaryContent: diary.diaryContent || "",
    sleepQuality: diary.sleepQuality || null,
    stressLevel: diary.stressLevel || null
  })
  currentDiaryId.value = diary.id
  showHistoryDialog.value = false
  // 同步更新日历高亮选中块
  currentDate.value = new Date(diary.diaryDate)
  ElMessage.success("已回到那天的记忆，请修改")
}
const deleteDiary = async (diaryId) => {
  try {
    await ElMessageBox.confirm("确定要遗忘这段记忆吗？", "提示", {
      confirmButtonText: "遗忘",
      cancelButtonText: "保留",
      type: "warning"
    })
    await deleteEmotionDiary(diaryId, {
      onSuccess: () => {
        ElMessage.success("已清除")
        loadHistoryData()
        loadStatistics()
        fetchCalendarData(currentDate.value)
      }
    })
  } catch (error) {
    console.log("取消删除", error)
  }
}
const handleDuplicateDiary = async (existingDiaryId) => {
  try {
    const action = await ElMessageBox.confirm("今日已有记录，是否补充？", "提示", {
      confirmButtonText: "补充记录",
      cancelButtonText: "取消",
      type: "warning"
    })
    if (action === "confirm") {
      await getEmotionDiaryById(existingDiaryId, {
        onSuccess: (diary) => { editDiary(diary) },
        onError: (error) => { ElMessage.error("加载失败" + error) }
      })
    }
  } catch (error) {
    console.log("取消操作", error)
  }
}
const viewDiaryDetail = (diaryId) => {
  router.push({ name: "EmotionDiaryDetail", params: { id: diaryId } })
}
const renderTrendChart = (trendData) => {
  nextTick(() => {
    if (!trendChart.value || !trendData.length) return
    const ctx = trendChart.value.getContext("2d")
    const canvas = trendChart.value
    const container = trendChartContainer.value
    canvas.width = container.clientWidth
    canvas.height = 180
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    if (trendData.length > 1) {
      const padding = 20
      const width = canvas.width - padding * 2
      const height = canvas.height - padding * 2
      ctx.strokeStyle = "#8B5CF6"
      ctx.lineWidth = 3
      ctx.lineCap = "round"
      ctx.lineJoin = "round"
      ctx.beginPath()
      trendData.forEach((point, index) => {
        const x = padding + (index / (trendData.length - 1)) * width
        const y = padding + height - (point.moodScore / 10) * height
        if (index === 0) { ctx.moveTo(x, y) } else { ctx.lineTo(x, y) }
      })
      ctx.stroke()
      
      trendData.forEach((point, index) => {
        const x = padding + (index / (trendData.length - 1)) * width
        const y = padding + height - (point.moodScore / 10) * height
        ctx.fillStyle = "#FFFFFF"
        ctx.strokeStyle = "#8B5CF6"
        ctx.lineWidth = 2
        ctx.beginPath()
        ctx.arc(x, y, 5, 0, Math.PI * 2)
        ctx.fill()
        ctx.stroke()
      })
    }
  })
}
const loadAiAnalysis = async () => {
  if (!currentDiaryId.value) return
  
  aiAnalysisLoading.value = true
  await getAiEmotionAnalysis(currentDiaryId.value, {
    onSuccess: (data) => {
      aiAnalysis.value = data
      aiAnalysisLoading.value = false
    },
    onError: () => {
      aiAnalysis.value = null
      aiAnalysisLoading.value = false
    }
  })
}
const refreshTodayAnalysisStatus = async () => {
  if (!currentDiaryId.value) return
  aiAnalysisLoading.value = true
  try {
    await getAiEmotionAnalysis(currentDiaryId.value, {
      onSuccess: (data) => {
        aiAnalysis.value = data
        ElMessage.success("感知已更新")
      },
      onError: () => { aiAnalysis.value = null }
    })
  } catch (error) { console.error(error) }
  aiAnalysisLoading.value = false
}
const triggerAnalysis = async () => {
  if (!currentDiaryId.value) {
    ElMessage.warning("请先保存日记")
    return
  }
  await triggerAiEmotionAnalysis(currentDiaryId.value, {
    onSuccess: () => { startAiAnalysisPolling() }
  })
}
let pollingTimer = null
let pollingAttempts = 0
const maxPollingAttempts = 20

const clearPollingTimer = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}
const startAiAnalysisPolling = () => {
  if (!currentDiaryId.value) return
  pollingAttempts = 0
  clearPollingTimer()
  pollingTimer = setInterval(async () => {
    pollingAttempts++
    try {
      await refreshTodayAnalysisStatus()
      if (aiAnalysis.value) {
        clearPollingTimer()
        ElMessage.success("感知已完成")
        return
      }
      if (pollingAttempts >= maxPollingAttempts) {
        clearPollingTimer()
        return
      }
    } catch (error) {
      if (pollingAttempts >= maxPollingAttempts) clearPollingTimer()
    }
  }, 30000)
}
const getRiskLevelClass = (riskLevel) => {
  const classes = { 0: "text-stable", 1: "text-stable", 2: "text-warning", 3: "text-danger" }
  return classes[riskLevel] || "text-stable"
}
const getRiskLevelText = (riskLevel) => {
  const texts = { 0: "状态平稳", 1: "轻微波动", 2: "需要关注", 3: "建议寻求支持" }
  return texts[riskLevel] || "未知"
}
watch(showHistoryDialog, (newValue) => {
  if (newValue) loadHistoryData()
})

onUnmounted(() => {
  clearPollingTimer()
})

onMounted(() => {
  loadTodayDiary()
  loadStatistics()
  fetchCalendarData(currentDate.value)
})
</script>

<style scoped>
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
* {
  box-sizing: border-box;
}
.healing-diary-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  color: var(--text-main);
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", sans-serif;
  position: relative;
  overflow: hidden;
  padding-bottom: 40px;
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
.light-1 { width: 40vw; height: 40vw; background: #E0E7FF; top: -5%; left: -5%; }
.light-2 { width: 50vw; height: 50vw; background: #F5F3FF; bottom: 10%; right: -10%; animation-delay: -5s; }

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
.page-header { display: flex; justify-content: space-between; align-items: center; padding: 40px 0 32px 0; }
.header-left { display: flex; align-items: center; gap: 16px; }

.seedling-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: linear-gradient(135deg, #eff2ff, #eff2ff);
  color: rgb(21, 5, 5);
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  box-shadow: 0 4px 12px rgba(240, 155, 235, 0.3);
}
.title-group h2 { font-size: 24px; font-weight: 600; margin: 0 0 4px 0; color: var(--text-main); }
.title-group p { font-size: 13px; color: var(--text-sub); margin: 0; }

.glass-outline-btn {
  padding: 10px 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid white;
  color: var(--text-main);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: 0.3s;
  box-shadow: var(--shadow-soft);
}
.glass-outline-btn:hover {
  background: white;
  color: var(--primary);
}
.content-layout { display: grid; grid-template-columns: 1.6fr 1fr; gap: 24px; }

.glass-card {
  background: var(--glass-bg);
  backdrop-filter: blur(24px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-soft);
  padding: 28px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.history-badge {
  font-size: 12px;
  color: #8B5CF6;
  background: #F5F3FF;
  padding: 2px 8px;
  border-radius: 12px;
  margin-left: 8px;
  font-weight: normal;
}
.section-card { margin-bottom: 24px; }
.section-card:hover { transform: translateY(-2px); box-shadow: 0 12px 40px rgba(31, 38, 135, 0.06); }

.card-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 6px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-main);
}.card-title i { color: var(--primary); font-size: 18px; }
.section-desc { font-size: 13px; color: var(--text-sub); margin-bottom: 24px; }

.modern-slider-container {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 255, 255, 0.5);
  padding: 16px 24px;
  border-radius: 16px;
  margin-bottom: 16px;
}
.slider-label { font-size: 13px; color: var(--text-sub); font-weight: 500; }
.slider-track-wrapper { flex: 1; }

:deep(.healing-slider .el-slider__runway) { background-color: #E2E8F0; height: 8px; border-radius: 4px; }
:deep(.healing-slider .el-slider__bar) { background: linear-gradient(90deg, #A5B4FC, #8B5CF6); height: 8px; border-radius: 4px; }
:deep(.healing-slider .el-slider__button) {
  border: 4px solid white; background-color: #8B5CF6; width: 20px; height: 20px; box-shadow: 0 2px 8px rgba(139, 92, 246, 0.3);
}
.selected-mood-feedback { text-align: center; font-size: 13px; color: var(--text-sub); }
.selected-mood-feedback span { font-weight: 600; color: var(--primary); font-size: 15px; }

.emotion-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }

.emotion-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px 0;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.emotion-item:hover { background: white; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03); }
.emo-icon { font-size: 24px; opacity: 0.8; transition: 0.3s; }
.emo-name { font-size: 13px; color: var(--text-sub); font-weight: 500; transition: 0.3s; }

.emotion-item.is-active {
  background: var(--emo-bg);
  border-color: var(--emo-color);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  transform: scale(1.02) translateY(-2px);
}
.emotion-item.is-active .emo-icon { opacity: 1; transform: scale(1.2); }
.emotion-item.is-active .emo-name { color: var(--emo-color); font-weight: 600; }

.form-item { display: flex; flex-direction: column; gap: 8px; margin-bottom: 20px; }
.form-item label { font-size: 13px; font-weight: 600; color: var(--text-main); }

.clean-textarea {
  width: 100%;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  padding: 16px;
  font-size: 16px;          /* 字体调大 */
  font-weight: 600;         /* 字体加粗 */
  color: #1e293b;           /* 字体颜色加深 */
  line-height: 1.6;
  resize: none;
  transition: 0.3s;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.01);
}
.clean-textarea:focus { outline: none; background: white; border-color: #C4B5FD; box-shadow: 0 4px 12px rgba(139, 92, 246, 0.05); }
.clean-textarea::placeholder { color: #94A3B8; }

.life-indicators { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 24px; }
.indicator-box { display: flex; flex-direction: column; gap: 8px; }
.indicator-box label { font-size: 13px; font-weight: 500; color: var(--text-sub); display: flex; align-items: center; gap: 6px; }

:deep(.clean-select .el-input__wrapper) { background: rgba(255, 255, 255, 0.5); border-radius: 12px; box-shadow: none; border: 1px solid white; }
:deep(.clean-select .el-input__wrapper.is-focus) { background: white; border-color: #C4B5FD; }

.action-bar { display: flex; justify-content: flex-end; gap: 12px; margin-top: 16px; }

.flat-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 12px 24px; border-radius: 12px; font-size: 14px; font-weight: 500; cursor: pointer; transition: all 0.3s ease;
}
.flat-btn.ghost-mode { background: transparent; border: 1px solid #E2E8F0; color: var(--text-sub); }
.flat-btn.ghost-mode:hover { background: #F1F5F9; color: var(--text-main); }
.flat-btn.primary-mode { background: white; border: 1px solid var(--primary); color: var(--primary); box-shadow: 0 2px 8px rgba(139, 92, 246, 0.1); }
.flat-btn.primary-mode:hover { background: var(--primary-light); transform: translateY(-2px); box-shadow: 0 4px 12px rgba(139, 92, 246, 0.2); }

.statistics-section { display: flex; flex-direction: column; gap: 24px; }

.ai-insight-card { padding: 24px; }
.ai-title { color: var(--primary) !important; }
.icon-refresh-btn { background: none; border: none; color: var(--text-sub); cursor: pointer; transition: 0.3s; }
.icon-refresh-btn:hover { color: var(--primary); }

.analysis-ready {
  background: white; border-radius: 16px; padding: 20px; border: 1px solid white; box-shadow: 0 2px 12px rgba(0, 0, 0, 0.02);
}
.emotion-spotlight { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
.spotlight-icon {
  width: 56px; height: 56px; border-radius: 16px; background: #EEF2FF; color: var(--primary);
  font-size: 28px; display: flex; justify-content: center; align-items: center;
}.spotlight-text h4 { font-size: 18px; font-weight: 600; margin: 0 0 4px 0; }
.score-text { font-size: 13px; color: var(--text-sub); }

.assessment-row { margin-bottom: 20px; }
.assess-item { display: flex; justify-content: space-between; align-items: center; }
.assess-item .label { font-size: 13px; color: var(--text-sub); }
.assess-item .value { font-size: 14px; font-weight: 600; }

.text-stable { color: #3B82F6; }
.text-warning { color: #D97706; }
.text-danger { color: #EF4444; }

.ai-advice-box { background: #F8FAFC; border-radius: 12px; padding: 16px; font-size: 13px; line-height: 1.6; color: var(--text-main); position: relative; }
.ai-advice-box i { position: absolute; top: 16px; left: 16px; color: #F59E0B; }
.ai-advice-box p { margin: 0 0 0 24px; }

.analysis-loading, .analysis-empty { text-align: center; padding: 40px 0; color: var(--text-sub); font-size: 13px; }
.pulse-sparkle { font-size: 32px; color: var(--primary); margin-bottom: 16px; animation: gentle-pulse 2s infinite alternate; }
.empty-icon { font-size: 32px; color: #CBD5E1; margin-bottom: 16px; }

.soft-btn {
  margin-top: 12px; padding: 8px 16px; border-radius: 12px; background: white; border: 1px solid #E2E8F0; color: var(--text-main); font-size: 12px; cursor: pointer; transition: 0.2s;
}.soft-btn:hover { border-color: var(--primary); color: var(--primary); }

.stat-card { padding: 24px; }
.stat-title { font-size: 15px; font-weight: 600; margin: 0 0 16px 0; display: flex; align-items: center; gap: 8px; color: var(--text-main); }
.stat-title i { color: var(--primary); }

.trend-chart-wrapper { width: 100%; height: 180px; }

.emotion-stats-list { display: flex; flex-direction: column; gap: 12px; }

.stat-row { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: rgba(255, 255, 255, 0.5); border-radius: 12px; }
.stat-info { display: flex; align-items: center; gap: 12px; }
.stat-dot { width: 10px; height: 10px; border-radius: 50%; }
.stat-name { font-size: 13px; color: var(--text-sub); }
.stat-percent { font-size: 13px; font-weight: 600; color: var(--text-main); }

.empty-state { text-align: center; padding: 20px; font-size: 13px; color: #94A3B8; }

.tip-card { background: linear-gradient(135deg, #F0FDF4, #DCFCE7); border-color: #A7F3D0; display: flex; align-items: flex-start; gap: 16px; padding: 20px; }
.tip-icon { font-size: 24px; color: #059669; }
.tip-text h4 { margin: 0 0 4px 0; font-size: 14px; color: #065F46; }
.tip-text p { margin: 0; font-size: 12px; color: #047857; line-height: 1.5; }

.healing-calendar { --el-calendar-cell-width: auto; background-color: transparent; }

:deep(.healing-calendar .el-calendar-table .el-calendar-day) { height: 48px; padding: 4px; }
:deep(.healing-calendar .el-calendar__header) { padding: 8px 0; border-bottom: none; display: flex; justify-content: space-between; }
:deep(.healing-calendar .el-calendar__title) { color: var(--text-main); font-weight: 500; font-size: 14px; }
:deep(.healing-calendar .el-button) { background: rgba(255, 255, 255, 0.5); border: 1px solid var(--glass-border); color: var(--text-sub); }

.calendar-day { display: flex; flex-direction: column; align-items: center; height: 100%; }
.day-text { font-size: 13px; color: var(--text-main); font-weight: 500; }
.day-text.is-today { color: var(--primary); font-weight: bold; }
.emotion-dot { width: 6px; height: 6px; border-radius: 50%; margin-top: 4px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); }

:deep(.healing-dialog) { border-radius: 24px; background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(20px); }
:deep(.el-dialog__header) { padding: 24px 32px; border-bottom: 1px solid #F1F5F9; }
:deep(.el-dialog__title) { font-size: 18px; font-weight: 600; color: var(--text-main); }

.history-container { padding: 10px; }
.filters-bar { display: flex; gap: 16px; margin-bottom: 24px; }

.timeline-list { display: flex; flex-direction: column; gap: 20px; max-height: 50vh; overflow-y: auto; padding-right: 10px; }
.timeline-list::-webkit-scrollbar { width: 4px; }
.timeline-list::-webkit-scrollbar-thumb { background: #E2E8F0; border-radius: 4px; }

.timeline-item { display: flex; gap: 20px; }
.time-stamp { width: 100px; font-size: 13px; font-weight: 600; color: var(--text-sub); padding-top: 16px; text-align: right; }
.timeline-card { flex: 1; background: #F8FAFC; border-radius: 16px; padding: 20px; border: 1px solid #E2E8F0; transition: 0.3s; }
.timeline-card:hover { background: white; box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04); }

.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.mood-tag { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }

.tag-success { font-size: 11px; color: #059669; background: #D1FAE5; padding: 2px 8px; border-radius: 12px; }
.tag-pending { font-size: 11px; color: #D97706; background: #FEF3C7; padding: 2px 8px; border-radius: 12px; }

.card-body p { font-size: 13px; color: var(--text-main); line-height: 1.6; margin: 0 0 16px 0; }
.card-actions { display: flex; gap: 16px; }

.text-link { background: none; border: none; color: var(--primary); font-size: 12px; cursor: pointer; transition: 0.2s; padding: 0; }
.text-link:hover { opacity: 0.7; }
.text-link.danger { color: #EF4444; }

.pagination-wrapper { margin-top: 24px; display: flex; justify-content: center; }

@media (max-width: 1024px) {
  .content-layout { grid-template-columns: 1fr; }
}</style>

```