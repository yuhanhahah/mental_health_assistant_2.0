<template>
  <div class="healing-diary-detail-page">
    <div class="ambient-light light-purple"></div>
    <div class="ambient-light light-green"></div>

    <div class="container">
      <header class="page-header">
        <div class="header-content">
          <div class="header-left">
            <div class="seedling-icon-box">
              <i class="fas fa-seedling"></i>
            </div>
            <div class="header-text">
              <h2>时光切片</h2>
              <p>{{ diaryData ? formatDateTime(diaryData.diaryDate) : '记忆读取中...' }} · 深入了解此刻的你</p>
            </div>
          </div>
          <div class="header-actions">
            <button class="glass-btn" @click="goBack">
              <i class="fas fa-arrow-left"></i> 返回列表
            </button>
            <button class="glass-btn primary-purple" @click="enterEditMode" v-if="!isEditMode && diaryData">
              <i class="fas fa-edit"></i> 补充记录
            </button>
            <button class="glass-btn primary-green" @click="saveDiary" v-if="isEditMode" :disabled="saving">
              <i class="fas fa-save" v-if="!saving"></i>
              <i class="fas fa-spinner fa-spin" v-else></i>
              保存更改
            </button>
            <button class="glass-btn danger-text" @click="cancelEdit" v-if="isEditMode">
              放弃
            </button>
          </div>
        </div>
      </header>

      <div v-if="loading" class="loading-container glass-card">
        <el-skeleton :rows="8" animated />
      </div>

      <div v-else-if="diaryData" class="detail-content">
        <div class="content-layout">
          
          <div class="diary-main-section">
            
            <div class="glass-card">
              <h3 class="card-title"><i class="fas fa-temperature-half"></i> 情绪感知温度</h3>
              
              <div class="emotion-section">
                <div class="mood-score-display" v-if="!isEditMode">
                  <div class="score-crystal" :class="getMoodClass(diaryData.moodScore)">
                    {{ diaryData.moodScore }}
                  </div>
                  <div class="score-info">
                    <div class="score-text">{{ getMoodDescription(diaryData.moodScore) }}</div>
                    <div class="score-range">情绪指数评估 (1-10)</div>
                  </div>
                </div>
                
                <div v-if="isEditMode" class="mood-edit-wrapper">
                  <span class="mood-range-label">低落</span>
                  <div class="mood-buttons">
                    <div
                      v-for="score in 10"
                      :key="score"
                      class="mood-bubble"
                      :class="{ 'is-selected': editForm.moodScore === score }"
                      @click="editForm.moodScore = score"
                    >
                      {{ score }}
                    </div>
                  </div>
                  <span class="mood-range-label">极好</span>
                </div>
              </div>

              <div class="emotion-section">
                <h4 class="sub-title">主要情绪色调</h4>
                <div class="emotion-display" v-if="!isEditMode">
                  <div class="emotion-pill" :style="{ color: getEmotionColor(diaryData.dominantEmotion), backgroundColor: getEmotionBg(diaryData.dominantEmotion) }">
                    <i :class="getEmotionIcon(diaryData.dominantEmotion)"></i>
                    {{ diaryData.dominantEmotion || '未记录' }}
                  </div>
                </div>
                
                <div v-else class="emotion-grid">
                  <div
                    v-for="emotion in emotionOptions"
                    :key="emotion.name"
                    class="emotion-item"
                    :class="{ 'is-active': editForm.dominantEmotion === emotion.name }"
                    :style="editForm.dominantEmotion === emotion.name ? { '--emo-color': emotion.color, '--emo-bg': emotion.bg } : {}"
                    @click="editForm.dominantEmotion = emotion.name"
                  >
                    <i :class="emotion.icon" :style="{ color: emotion.color }"></i>
                    <span>{{ emotion.name }}</span>
                  </div>
                </div>
              </div>

              <div class="metrics-grid">
                <div class="metric-box">
                  <span class="metric-label"><i class="fas fa-moon"></i> 睡眠质量</span>
                  <div v-if="!isEditMode" class="metric-value">
                    <span class="status-pill">{{ getSleepQualityText(diaryData.sleepQuality) }}</span>
                  </div>
                  <el-rate v-else v-model="editForm.sleepQuality" :max="5" show-text :texts="['很差', '较差', '一般', '良好', '很好']" class="custom-rate" />
                </div>
                
                <div class="metric-box">
                  <span class="metric-label"><i class="fas fa-compress-arrows-alt"></i> 压力水平</span>
                  <div v-if="!isEditMode" class="metric-value">
                    <span class="status-pill">{{ getStressLevelText(diaryData.stressLevel) }}</span>
                  </div>
                  <el-rate v-else v-model="editForm.stressLevel" :max="5" show-text :texts="['很低', '较低', '中等', '较高', '很高']" class="custom-rate" />
                </div>
              </div>
            </div>

            <div class="glass-card">
              <h3 class="card-title"><i class="fas fa-book-open"></i> 倾听内心的声音</h3>
              
              <div class="content-section">
                <h4 class="sub-title">情绪触发点</h4>
                <div v-if="!isEditMode" class="text-content">
                  {{ diaryData.emotionTriggers || '（暂无记录）' }}
                </div>
                <textarea
                  v-else
                  v-model="editForm.emotionTriggers"
                  class="clean-textarea"
                  rows="3"
                  placeholder="今天什么事情影响了您的情绪？"
                  maxlength="1000"
                ></textarea>
              </div>

              <div class="content-section">
                <h4 class="sub-title">今日独白</h4>
                <div v-if="!isEditMode" class="text-content diary-text">
                  <div v-if="diaryData.diaryContent" v-html="formatDiaryContent(diaryData.diaryContent)"></div>
                  <div v-else class="empty-text">（暂无记录）</div>
                </div>
                <textarea
                  v-else
                  v-model="editForm.diaryContent"
                  class="clean-textarea large-textarea"
                  rows="8"
                  placeholder="记录您今天的感受、想法和体验..."
                  maxlength="2000"
                ></textarea>
              </div>
              
              <div class="record-meta">
                <span>记录于: {{ diaryData.diaryDate }}</span>
                <span v-if="diaryData.updatedAt !== diaryData.createdAt">· 更新于: {{ formatDateTime(diaryData.updatedAt) }}</span>
              </div>
            </div>
          </div>

          <div class="analysis-side-section">
            
            <div class="glass-card ai-insight-card">
              <div class="card-header-flex">
                <h3 class="card-title ai-title"><i class="fas fa-sparkles"></i> AI 心境感知</h3>
                <div class="analysis-status-tags">
                  <span v-if="diaryData.aiAnalysisStatus === 'PENDING'" class="pill-tag pending"><i class="fas fa-sync fa-spin"></i> 感知中</span>
                  <span v-else-if="diaryData.aiAnalysisStatus === 'COMPLETED'" class="pill-tag success"><i class="fas fa-check"></i> 已完成</span>
                  <span v-else-if="diaryData.aiAnalysisStatus === 'FAILED'" class="pill-tag danger"><i class="fas fa-exclamation"></i> 失败</span>
                </div>
              </div>

              <div class="ai-content-body">
                <div v-if="aiAnalysisLoading" class="ai-loading-box">
                  <div class="sparkle-loader"><i class="fas fa-sparkles"></i></div>
                  <p>小暖正在为您解读情绪...</p>
                </div>

                <div v-else-if="aiAnalysis && diaryData.aiAnalysisStatus === 'COMPLETED'" class="analysis-ready-content">
                  
                  <div class="emotion-spotlight">
                    <div class="spotlight-icon" :style="{ color: getEmotionColor(aiAnalysis.primaryEmotion), backgroundColor: getEmotionBg(aiAnalysis.primaryEmotion) }">
                      <i :class="getEmotionIcon(aiAnalysis.primaryEmotion)"></i>
                    </div>
                    <div class="spotlight-info">
                      <h4>{{ aiAnalysis.primaryEmotion }}</h4>
                      <p>情绪强度: {{ aiAnalysis.emotionScore }}/100</p>
                    </div>
                  </div>

                  <div class="risk-bar-container">
                    <div class="risk-label">状态评估</div>
                    <div class="risk-pill" :class="getRiskTagType(aiAnalysis.riskLevel)">
                      {{ getRiskLevelText(aiAnalysis.riskLevel) }}
                    </div>
                  </div>
                  <p class="risk-desc" v-if="aiAnalysis.riskDescription">{{ aiAnalysis.riskDescription }}</p>

                  <div class="ai-suggestion-box" v-if="aiAnalysis.suggestion">
                    <i class="fas fa-lightbulb"></i>
                    <p>{{ aiAnalysis.suggestion }}</p>
                  </div>

                  <div class="improvement-box" v-if="aiAnalysis.improvementSuggestions && aiAnalysis.improvementSuggestions.length > 0">
                    <h4><i class="fas fa-seedling"></i> 愈疗建议</h4>
                    <ul>
                      <li v-for="(suggestion, index) in aiAnalysis.improvementSuggestions" :key="index">
                        <i class="fas fa-arrow-right"></i> {{ suggestion }}
                      </li>
                    </ul>
                  </div>
                </div>

                <div v-else class="ai-empty-box">
                  <div class="empty-icon"><i class="fas fa-inbox"></i></div>
                  <p>{{ diaryData.aiAnalysisStatus === 'FAILED' ? '感知过程遇到阻碍' : '暂无分析数据' }}</p>
                  <button class="ghost-outline-btn" @click="triggerAnalysis">
                    <i class="fas fa-magic"></i> {{ diaryData.aiAnalysisStatus === 'FAILED' ? '重新感知' : '开始感知' }}
                  </button>
                </div>
              </div>
            </div>

            <div class="glass-card tips-card">
              <div class="tip-icon"><i class="fas fa-seedling"></i></div>
              <div class="tip-content">
                <h4>成长与愈疗</h4>
                <ul>
                  <li>记录情绪是提高自我觉察的第一步</li>
                  <li>接纳一切感受，哪怕是负面的波澜</li>
                  <li>如果觉得难熬，请记得寻求专业支持</li>
                </ul>
              </div>
            </div>

          </div>
        </div>
      </div>

      <div v-else class="error-state">
        <div class="glass-card text-center">
          <i class="fas fa-box-open" style="font-size: 40px; color: #94A3B8; margin-bottom: 16px;"></i>
          <p>记忆片段不存在或已被遗忘</p>
          <button class="glass-btn primary-purple" @click="goBack" style="margin-top: 16px;">返回列表</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  getEmotionDiaryById, updateEmotionDiary,
  getAiEmotionAnalysis, triggerAiEmotionAnalysis 
} from '@/api/emotionDiary'
import { formatDateTime } from '@/utils/dateUtils'

const route = useRoute()
const router = useRouter()

const diaryData = ref(null)
const loading = ref(true)
const saving = ref(false)
const isEditMode = ref(false)
const aiAnalysis = ref(null)
const aiAnalysisLoading = ref(false)

const editForm = reactive({
  moodScore: null, dominantEmotion: '', emotionTriggers: '',
  diaryContent: '', sleepQuality: null, stressLevel: null
})

// 柔和低饱和情绪色卡
const emotionOptions = [
  { name: '开心', icon: 'fas fa-sun', color: '#D97706', bg: '#FEF3C7' },
  { name: '平静', icon: 'fas fa-water', color: '#3B82F6', bg: '#EFF6FF' },
  { name: '焦虑', icon: 'fas fa-wind', color: '#4F46E5', bg: '#EEF2FF' },
  { name: '悲伤', icon: 'fas fa-cloud-rain', color: '#475569', bg: '#F1F5F9' },
  { name: '兴奋', icon: 'fas fa-star', color: '#059669', bg: '#D1FAE5' },
  { name: '疲惫', icon: 'fas fa-battery-quarter', color: '#7C3AED', bg: '#F5F3FF' },
  { name: '惊讶', icon: 'fas fa-bolt', color: '#C2410C', bg: '#FFEDD5' },
  { name: '困惑', icon: 'fas fa-question', color: '#64748B', bg: '#F8FAFC' }
]

const diaryId = computed(() => route.params.id)

const loadDiaryDetail = async () => {
  loading.value = true
  await getEmotionDiaryById(diaryId.value, {
    onSuccess: (data) => {
      diaryData.value = data
      if (data.aiAnalysisStatus === 'COMPLETED') loadAiAnalysis()
    },
    onError: (error) => { ElMessage.error('读取记忆失败：' + error.message) }
  })
  loading.value = false
}

const loadAiAnalysis = async () => {
  if (!diaryId.value) return
  aiAnalysisLoading.value = true
  await getAiEmotionAnalysis(diaryId.value, {
    onSuccess: (data) => { aiAnalysis.value = data },
    onError: (error) => { console.error(error); aiAnalysis.value = null }
  })
  aiAnalysisLoading.value = false
}

const refreshAnalysisStatus = async () => {
  if (!diaryId.value) return
  aiAnalysisLoading.value = true
  try {
    await getEmotionDiaryById(diaryId.value, { onSuccess: (data) => { diaryData.value = data } })
    if (diaryData.value && diaryData.value.aiAnalysisStatus === 'COMPLETED') {
      await getAiEmotionAnalysis(diaryId.value, {
        onSuccess: (data) => { aiAnalysis.value = data; ElMessage.success('感知结果已更新') },
        onError: () => { aiAnalysis.value = null }
      })
    } else {
      aiAnalysis.value = null
      if (diaryData.value?.aiAnalysisStatus === 'PENDING') ElMessage.info('小暖仍在感知中')
      else if (diaryData.value?.aiAnalysisStatus === 'FAILED') ElMessage.warning('感知失败')
    }
  } catch (error) { ElMessage.error('刷新失败') }
  aiAnalysisLoading.value = false
}

const triggerAnalysis = async () => {
  await triggerAiEmotionAnalysis(diaryId.value, {
    onSuccess: () => {
      ElMessage.success('小暖开始感知...')
      diaryData.value.aiAnalysisStatus = 'PENDING'
      startAiAnalysisPolling()
    }
  })
}

const enterEditMode = () => {
  isEditMode.value = true
  Object.assign(editForm, {
    moodScore: diaryData.value.moodScore, dominantEmotion: diaryData.value.dominantEmotion,
    emotionTriggers: diaryData.value.emotionTriggers, diaryContent: diaryData.value.diaryContent,
    sleepQuality: diaryData.value.sleepQuality, stressLevel: diaryData.value.stressLevel
  })
}

const cancelEdit = () => {
  isEditMode.value = false
  Object.assign(editForm, { moodScore: null, dominantEmotion: '', emotionTriggers: '', diaryContent: '', sleepQuality: null, stressLevel: null })
}

const saveDiary = async () => {
  if (!editForm.moodScore) { ElMessage.warning('请选择情绪温度'); return }
  saving.value = true
  await updateEmotionDiary(diaryId.value, editForm, {
    onSuccess: (data) => {
      ElMessage.success('记忆已更新')
      diaryData.value = data; isEditMode.value = false; aiAnalysis.value = null
      startAiAnalysisPolling()
    },
    onError: (error) => { ElMessage.error('更新失败：' + error.message) }
  })
  saving.value = false
}

const goBack = () => { router.go(-1) }

let pollingTimer = null; let pollingAttempts = 0; const maxPollingAttempts = 20
const startAiAnalysisPolling = () => {
  if (!diaryId.value) return
  pollingAttempts = 0; clearPollingTimer()
  pollingTimer = setInterval(async () => {
    pollingAttempts++
    try {
      await refreshAnalysisStatus()
      if (aiAnalysis.value && diaryData.value.aiAnalysisStatus === 'COMPLETED') { clearPollingTimer(); ElMessage.success('感知完成！'); return }
      if (pollingAttempts >= maxPollingAttempts) { clearPollingTimer(); return }
    } catch (error) { if (pollingAttempts >= maxPollingAttempts) clearPollingTimer() }
  }, 30000)
}

const clearPollingTimer = () => { if (pollingTimer) { clearInterval(pollingTimer); pollingTimer = null } }

const getMoodDescription = (score) => {
  const descriptions = { 1: '极度低落', 2: '糟糕', 3: '阴霾', 4: '略感疲惫', 5: '平静如水', 6: '微风和煦', 7: '舒畅', 8: '明媚', 9: '充满活力', 10: '光芒万丈' }
  return descriptions[score] || '未知'
}
const getMoodClass = (score) => {
  if (score <= 3) return 'mood-bad'
  if (score <= 5) return 'mood-neutral'
  if (score <= 7) return 'mood-good'
  return 'mood-excellent'
}
const getEmotionIcon = (emotionName) => { const e = emotionOptions.find(opt => opt.name === emotionName); return e ? e.icon : 'fas fa-leaf' }
const getEmotionColor = (emotionName) => { const e = emotionOptions.find(opt => opt.name === emotionName); return e ? e.color : '#64748B' }
const getEmotionBg = (emotionName) => { const e = emotionOptions.find(opt => opt.name === emotionName); return e ? e.bg : '#F1F5F9' }
const formatDiaryContent = (content) => content.replace(/\n/g, '<br>')
const getSleepQualityText = (quality) => { const texts = ['未评分', '很差', '较差', '一般', '良好', '很好']; return texts[quality] || '未评分' }
const getStressLevelText = (level) => { const texts = ['未评分', '很低', '较低', '中等', '较高', '很高']; return texts[level] || '未评分' }

const getRiskTagType = (riskLevel) => {
  const typeMap = { 0: 'status-safe', 1: 'status-safe', 2: 'status-warn', 3: 'status-danger' }
  return typeMap[riskLevel] || 'status-safe'
}
const getRiskLevelText = (riskLevel) => {
  const textMap = { 0: '状态平稳', 1: '轻微波动', 2: '需要关注', 3: '建议支持' }
  return textMap[riskLevel] || '未知'
}

onUnmounted(() => clearPollingTimer())
onMounted(() => loadDiaryDetail())
</script>

<style scoped>
/* 修复：将变量从 :root 移动到最外层 class 内，解决 Vue scoped 失效导致按钮变白板的问题 
*/
.healing-diary-detail-page {
  --bg-page: #F4F7F9;
  --glass-bg: rgba(255, 255, 255, 0.7);
  --glass-border: rgba(255, 255, 255, 0.9);
  --primary-purple: #8B5CF6;
  --primary-green: #10B981;
  --text-main: #334155;
  --text-sub: #64748B;
  --shadow-soft: 0 8px 32px rgba(31, 38, 135, 0.04);

  min-height: 100vh;
  background-color: var(--bg-page);
  color: var(--text-main);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  position: relative;
  overflow: hidden;
  padding-bottom: 40px;
}

* { box-sizing: border-box; }

.ambient-light { position: absolute; border-radius: 50%; filter: blur(140px); opacity: 0.35; z-index: 0; pointer-events: none; animation: float 15s infinite alternate ease-in-out; }
.light-purple { width: 45vw; height: 45vw; background: #E0E7FF; top: -10%; left: -5%; }
.light-green { width: 40vw; height: 40vw; background: #D1FAE5; bottom: 10%; right: -5%; animation-delay: -5s;}
@keyframes float { 0% { transform: translate(0,0); } 100% { transform: translate(5%, 5%); } }

.container { max-width: 1200px; margin: 0 auto; padding: 0 24px; position: relative; z-index: 10; }

.glass-card {
  background: var(--glass-bg); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px);
  border-radius: 20px; border: 1px solid var(--glass-border); box-shadow: var(--shadow-soft);
  padding: 24px; display: flex; flex-direction: column; overflow: hidden; margin-bottom: 24px;
}

.page-header { padding: 40px 0 32px 0; }
.header-content { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 16px; }

/* 时光切片 Logo 图标 */
.seedling-icon-box { 
  width: 48px; height: 48px; border-radius: 16px; background: linear-gradient(135deg, #34D399, #10B981); color: white;
  display: flex; justify-content: center; align-items: center; font-size: 24px; box-shadow: 0 4px 12px rgba(16,185,129,0.3);
}

.header-text h2 { font-size: 22px; font-weight: 600; margin: 0 0 4px 0; color: var(--text-main); }
.header-text p { font-size: 13px; color: var(--text-sub); margin: 0; }

/* 修复：按钮样式采用硬编码颜色确保绝不再变白板 */
.header-actions { display: flex; gap: 12px; }
.glass-btn { 
  display: flex; align-items: center; gap: 8px;
  padding: 10px 20px; border-radius: 14px; background: rgba(255,255,255,0.6); border: 1px solid white; 
  color: var(--text-main); font-size: 13px; font-weight: 500; cursor: pointer; transition: 0.3s; box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.glass-btn:hover { background: white; transform: translateY(-1px); }
.glass-btn.primary-purple { color: white; background: linear-gradient(135deg, #A5B4FC, #8B5CF6); border: none; box-shadow: 0 4px 12px rgba(139,92,246,0.2); }
.glass-btn.primary-green { color: white; background: linear-gradient(135deg, #34D399, #10B981); border: none; box-shadow: 0 4px 12px rgba(16,185,129,0.2); }
.glass-btn.danger-text { color: #EF4444; }

.content-layout { display: grid; grid-template-columns: 1.6fr 1fr; gap: 24px; }
.card-title { font-size: 16px; font-weight: 600; margin: 0 0 20px 0; display: flex; align-items: center; gap: 8px; }
.card-title i { color: var(--primary-purple); }
.sub-title { font-size: 13px; font-weight: 600; color: var(--text-main); margin: 0 0 12px 0; }

.emotion-section { margin-bottom: 24px; padding-bottom: 24px; border-bottom: 1px solid rgba(0,0,0,0.04); }

.mood-score-display { display: flex; align-items: center; gap: 16px; }
.score-crystal { 
  width: 56px; height: 56px; border-radius: 16px; display: flex; justify-content: center; align-items: center; 
  font-size: 24px; font-weight: bold; color: white; box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

/* 修复：使用硬编码渐变，避免白板 */
.mood-bad { background: linear-gradient(135deg, #FCA5A5, #EF4444); color: white; }
.mood-neutral { background: linear-gradient(135deg, #94A3B8, #64748B); color: white; } 
.mood-good { background: linear-gradient(135deg, #6EE7B7, #10B981); color: white; }
.mood-excellent { background: linear-gradient(135deg, #A5B4FC, #8B5CF6); color: white; }
.score-text { font-size: 16px; font-weight: 600; margin: 0 0 4px 0; color: var(--text-main);}
.score-range { font-size: 12px; color: var(--text-sub); }

.mood-edit-wrapper { display: flex; align-items: center; gap: 12px; background: rgba(255,255,255,0.4); padding: 16px; border-radius: 16px; }
.mood-range-label { font-size: 12px; color: var(--text-sub); font-weight: 500; }
.mood-buttons { display: flex; gap: 6px; flex: 1; justify-content: space-between; }

/* 修复：大幅提高 1-10 分数气泡的对比度和清晰度 */
.mood-bubble { 
  width: 36px; height: 36px; border-radius: 50%; background: white; border: 1px solid #CBD5E1; 
  display: flex; justify-content: center; align-items: center; font-size: 14px; font-weight: 500; color: #475569; cursor: pointer; transition: 0.2s;
}
.mood-bubble:hover { border-color: #10B981; color: #10B981; background: #ECFDF5; }
.mood-bubble.is-selected { background: #10B981; color: white; border-color: #10B981; box-shadow: 0 4px 12px rgba(16,185,129,0.3); transform: scale(1.1); font-weight: bold; }

.emotion-pill { display: inline-flex; align-items: center; gap: 8px; padding: 8px 16px; border-radius: 12px; font-size: 14px; font-weight: 600; }

.emotion-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.emotion-item { 
  display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 12px 0; 
  background: white; border-radius: 12px; border: 1px solid transparent; cursor: pointer; transition: 0.3s;
}
.emotion-item:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.03); transform: translateY(-2px); }
.emotion-item i { font-size: 20px; transition: 0.3s; opacity: 0.8; }
.emotion-item span { font-size: 12px; color: var(--text-sub); }
.emotion-item.is-active { background: var(--emo-bg); border-color: var(--emo-color); box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.emotion-item.is-active i { transform: scale(1.2); opacity: 1; }
.emotion-item.is-active span { color: var(--emo-color); font-weight: 600; }

.metrics-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.metric-box { background: rgba(255,255,255,0.4); padding: 16px; border-radius: 16px; display: flex; flex-direction: column; gap: 8px; }
.metric-label { font-size: 13px; color: var(--text-sub); display: flex; align-items: center; gap: 6px; }
.status-pill { background: white; padding: 4px 12px; border-radius: 8px; font-size: 13px; font-weight: 500; display: inline-block; border: 1px solid rgba(0,0,0,0.03);}

/* 星星颜色重置为柔和主题色 */
:deep(.custom-rate .el-rate__icon) { font-size: 18px; color: #E2E8F0 !important; }
:deep(.custom-rate .el-rate__icon.is-active) { color: #8B5CF6 !important; }

.content-section { margin-bottom: 24px; }
.text-content { font-size: 14px; line-height: 1.7; color: var(--text-main); background: rgba(255,255,255,0.4); padding: 16px; border-radius: 16px; }
.empty-text { color: #94A3B8; font-style: italic; }
.clean-textarea { width: 100%; background: white; border: 1px solid transparent; border-radius: 16px; padding: 16px; font-size: 14px; color: var(--text-main); line-height: 1.6; resize: none; transition: 0.3s; box-shadow: 0 2px 8px rgba(0,0,0,0.02); }
.clean-textarea:focus { outline: none; border-color: #C4B5FD; }
.record-meta { font-size: 11px; color: #94A3B8; text-align: right; }


/* ==================== 右侧分析区 ==================== */
.card-header-flex { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.ai-title i { color: var(--primary-purple); }
.pill-tag { padding: 4px 10px; border-radius: 12px; font-size: 11px; font-weight: 500; }
.pill-tag.pending { background: #FEF3C7; color: #D97706; }
.pill-tag.success { background: #D1FAE5; color: #059669; }
.pill-tag.danger { background: #FEE2E2; color: #DC2626; }

.analysis-ready-content { display: flex; flex-direction: column; gap: 16px; }
.emotion-spotlight { display: flex; align-items: center; gap: 16px; background: white; padding: 16px; border-radius: 16px; border: 1px solid rgba(0,0,0,0.02); }
.spotlight-icon { width: 48px; height: 48px; border-radius: 16px; display: flex; justify-content: center; align-items: center; font-size: 24px; }
.spotlight-info h4 { margin: 0 0 4px 0; font-size: 16px; font-weight: 600; }
.spotlight-info p { margin: 0; font-size: 12px; color: var(--text-sub); }

.risk-bar-container { display: flex; justify-content: space-between; align-items: center; background: rgba(255,255,255,0.5); padding: 12px 16px; border-radius: 12px; }
.risk-label { font-size: 13px; color: var(--text-sub); }
.risk-pill { font-size: 12px; font-weight: 600; padding: 4px 10px; border-radius: 8px; }
.status-safe { background: #D1FAE5; color: #059669; } .status-warn { background: #FEF3C7; color: #D97706; } .status-danger { background: #FEE2E2; color: #DC2626; }
.risk-desc { font-size: 12px; color: var(--text-sub); line-height: 1.5; margin: 0; padding: 0 4px;}

.ai-suggestion-box { background: #F5F3FF; border-radius: 12px; padding: 16px; font-size: 13px; line-height: 1.6; color: var(--text-main); position: relative; }
.ai-suggestion-box i { position: absolute; top: 18px; left: 16px; color: var(--primary-purple); }
.ai-suggestion-box p { margin: 0 0 0 24px; }

.improvement-box h4 { font-size: 14px; font-weight: 600; margin: 0 0 12px 0; display: flex; align-items: center; gap: 6px; color: var(--primary-green);}
.improvement-box ul { padding: 0; margin: 0; list-style: none; display: flex; flex-direction: column; gap: 8px; }
.improvement-box li { font-size: 12px; color: var(--text-sub); background: white; padding: 10px 12px; border-radius: 8px; display: flex; align-items: flex-start; gap: 8px;}
.improvement-box li i { color: var(--primary-green); margin-top: 2px;}

.ai-loading-box, .ai-empty-box { text-align: center; padding: 40px 0; color: var(--text-sub); font-size: 13px; }
.sparkle-loader { font-size: 32px; color: var(--primary-purple); margin-bottom: 16px; animation: gentle-pulse 2s infinite alternate; }
.empty-icon { font-size: 32px; color: #CBD5E1; margin-bottom: 16px; }
.ghost-outline-btn { margin-top: 16px; padding: 8px 16px; border-radius: 12px; background: white; border: 1px solid #E2E8F0; color: var(--text-main); font-size: 12px; cursor: pointer; transition: 0.3s;}
.ghost-outline-btn:hover { color: var(--primary-purple); border-color: var(--primary-purple); }

.tips-card { background: linear-gradient(135deg, #ECFDF5, #D1FAE5); border-color: #A7F3D0; flex-direction: row; gap: 16px; align-items: flex-start;}
.tip-icon { font-size: 24px; color: var(--primary-green); }
.tip-content h4 { margin: 0 0 8px 0; font-size: 14px; color: #065F46; }
.tip-content ul { margin: 0; padding-left: 16px; font-size: 12px; color: #047857; line-height: 1.6; }

.text-center { text-align: center; padding: 60px 0; }

@media (max-width: 1024px) { .content-layout { grid-template-columns: 1fr; } }
</style>