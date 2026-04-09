<template>
  <div class="healing-room-page">
    <div class="container">
      <div class="room-layout">
        <div class="sidebar">
          
          <div class="glass-card ai-presence-card">
            <div class="premium-ai-avatar">
              <div class="glass-ring ring-1"></div>
              <div class="glass-ring ring-2"></div>
              <div class="glow-core">
                <i class="fas fa-sparkles"></i>
              </div>
            </div>
            <h3 class="ai-name">小暖 · 心理愈疗伴侣</h3>
            <div class="ai-status">
              <div class="pulse-dot"></div>
              安静倾听中
            </div>
          </div>

          <div class="glass-card mood-radar-card">
            <div class="card-header">
              <span class="card-title">心境雷达</span>
            </div>
            
            <div class="mood-visualizer-area">
              <div class="crystal-shape" :class="currentEmotion.isNegative ? 'crystal-warm' : 'crystal-calm'">
                <i class="fas fa-leaf" v-if="!currentEmotion.isNegative"></i>
                <i class="fas fa-wind" v-else></i>
              </div>
              <div class="mood-text-group">
                <div class="mood-primary">{{ currentEmotion.primaryEmotion || '内心宁静' }}</div>
                <div class="mood-secondary">
                  状态: <span>{{ getEmotionLevelText(currentEmotion.emotionScore) }}</span>
                </div>
              </div>
            </div>

            <div class="assessment-metrics">
              <div class="metric-row">
                <span class="metric-label">综合评估</span>
                <span class="metric-value" :class="currentEmotion.isNegative ? 'text-warm' : 'text-calm'">
                  {{ currentEmotion.isNegative ? '建议适度舒缓' : '状态平稳自如' }}
                </span>
              </div>
            </div>

            <div class="insight-box" v-if="currentEmotion.suggestion">
              <div class="insight-text">{{ currentEmotion.suggestion }}</div>
            </div>
          </div>

          <div class="glass-card memory-card">
            <div class="card-header">
              <span class="card-title">记忆碎片</span>
              <button class="ghost-icon-btn" @click="refreshSessionList" title="刷新记录">
                <i class="fas fa-sync-alt" :class="{'fa-spin': sessionListLoading}"></i>
              </button>
            </div>
            
            <div class="memory-list" v-loading="sessionListLoading">
              <div 
                v-for="session in sessionList" 
                :key="session.id"
                class="memory-item"
                :class="{ 'is-active': currentSession && currentSession.dbSessionId == session.id }"
                @click="switchToSession(session)"
              >
                <div class="emotion-bar"></div>
                <div class="memory-content">
                  <div class="memory-title" @dblclick="startEditTitle(session)">
                    <span v-if="!session.isEditing">{{ session.sessionTitle || '未命名对话' }}</span>
                    <input 
                      v-else
                      v-model="session.editingTitle"
                      class="stealth-input"
                      @blur="saveSessionTitle(session)"
                      @keyup.enter="saveSessionTitle(session)"
                      @keyup.esc="cancelEditTitle(session)"
                    />
                  </div>
                  <div class="memory-preview">{{ session.lastMessageContent || '暂无内容...' }}</div>
                  <div class="memory-meta">
                    <span class="memory-time">{{ formatSessionTime(session.startedAt) }}</span>
                    <button class="delete-btn" @click.stop="confirmDeleteSession(session)">
                      <i class="fas fa-trash-alt"></i>
                    </button>
                  </div>
                </div>
              </div>

              <div v-if="sessionList.length === 0 && !sessionListLoading" class="empty-memory">
                留白，等待故事的书写
              </div>
              
              <div v-if="sessionList.length > 0 && hasMoreSessions" class="load-more-box">
                <button class="text-btn" @click="loadMoreSessions" :disabled="loadingMore">
                  {{ loadingMore ? '读取中...' : '查看更早记忆' }}
                </button>
              </div>
            </div>
          </div>

          <div class="glass-card human-support-card">
            <div class="support-title"><i class="fas fa-hands-helping"></i> 专业支持网络</div>
            <p class="support-desc">如果你希望与专业心理咨询师交流，我们可以为你提供支持。</p>
            <button class="support-action-btn" @click="showEmergencyDialog">
              获取支持
            </button>
          </div>
        </div>

        <div class="glass-card chat-space">
          <div class="chat-header">
            <div class="header-info">
              <div class="title-wrapper">
                <h2 v-if="!currentSession || !isEditingHeaderTitle" class="main-title" @dblclick="startEditHeaderTitle">
                  {{ currentSession ? (currentSession.sessionTitle || '新对话') : '静谧空间' }}
                </h2>
                <input 
                  v-else
                  v-model="headerTitleEdit"
                  class="stealth-header-input"
                  @blur="saveHeaderTitle"
                  @keyup.enter="saveHeaderTitle"
                  @keyup.esc="cancelEditHeaderTitle"
                  ref="headerTitleInput"
                />
              </div>
              <div class="privacy-note"><i class="fas fa-lock"></i> 端到端加密保护</div>
            </div>
            <button class="outline-btn" @click="createNewFrontendSession">
              <i class="fas fa-plus"></i> 新对话
            </button>
          </div>

          <div class="chat-flow" ref="messagesContainer">
            <div v-if="messages.length === 0" class="welcome-screen">
              <div class="welcome-icon-box">
                <i class="fas fa-seedling"></i>
              </div>
              <h3 class="welcome-title">你好，我是小暖。</h3>
              <p class="welcome-text">这是一个安全、私密、不带任何评判的空间。<br>你可以慢慢整理思绪，随时与我分享。</p>
            </div>

            <div
              v-for="message in messages"
              :key="message.id"
              class="message-row"
              :class="message.senderType === 1 ? 'is-user' : 'is-ai'"
            >
              <div class="chat-avatar">
                <div v-if="message.senderType === 2" class="ai-frosted-orb">
                  <i class="fas fa-seedling"></i>
                </div>
                <div v-else class="user-simple-avatar">
                  <i class="fas fa-user"></i>
                </div>
              </div>
              
              <div class="message-content">
                <div class="bubble" :class="message.senderType === 1 ? 'user-bubble' : 'ai-bubble'">
                  <div v-if="message.senderType === 2 && message.isTyping && !message.content" class="typing-dots">
                    <span></span><span></span><span></span>
                  </div>
                  <div v-else-if="message.isError" class="error-msg">
                    {{ message.content }}
                  </div>
                  <MarkdownRenderer
                    v-else-if="message.senderType === 2 && message.isComplete && !message.isError"
                    :content="message.content"
                    :is-ai-message="true"
                  />
                  <p v-else-if="message.content" v-html="formatMessageContent(message.content)"></p>
                </div>
                <div class="message-time">
                  {{ message.senderType === 2 && message.isTyping ? '正在回应...' : formatTime(message.createdAt) }}
                </div>
              </div>
            </div>
          </div>

          <div class="input-area">
            <div class="input-box">
              <textarea
                v-model="userMessage"
                class="clean-textarea"
                rows="1"
                placeholder="倾听你的声音..."
                :disabled="isLoading || isAiTyping"
                @keydown="handleKeyDown"
                @input="autoResize"
                ref="textareaRef"
              ></textarea>
              <div class="input-tools">
                <el-popover v-model:visible="emojiVisible" placement="top-start" width="280" trigger="click" popper-class="clean-popover">
                  <template #reference>
                    <button class="emoji-btn"><i class="far fa-smile"></i></button>
                  </template>
                  <div class="emoji-scroll">
                    <div class="emoji-grid">
                      <span v-for="emoji in emojiList" :key="emoji" class="emoji-item" @click="insertEmoji(emoji)">{{ emoji }}</span>
                    </div>
                  </div>
                </el-popover>
              </div>
            </div>
            <button 
              class="send-btn"
              :class="{ 'can-send': userMessage.trim().length > 0 && !isLoading && !isAiTyping }"
              :disabled="!userMessage.trim() || userMessage.length > 500 || isLoading || isAiTyping"
              @click="sendMessage"
            >
              <i class="fas fa-paper-plane"></i>
            </button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="showEmergency" width="400px" class="professional-dialog" :show-close="true">
      <div class="dialog-header-clean">
        <h2>专业支持网络</h2>
      </div>
      <div class="dialog-body-clean">
        <p class="dialog-desc">如果您希望与专业心理工作者进行交流，以下渠道24小时为您敞开：</p>
        <div class="contact-card">
          <div class="contact-name">全国心理危机干预热线</div>
          <div class="contact-number">400-161-9995</div>
        </div>
        <div class="contact-card">
          <div class="contact-name">希望24小时生命支持</div>
          <div class="contact-number">400-1619-995</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
// ==========================================
// 逻辑层保持绝对不变
// ==========================================
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { startChatSession, getSessionMessages, getSessionEmotion, getSessionsPage, deleteSession, updateSessionTitle } from '@/api/consultationSession'
import { fetchEventSource } from '@microsoft/fetch-event-source'
import { useUserStore } from '@/store/user'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const userStore = useUserStore()
const currentSession = ref(null)
const messages = ref([])
const userMessage = ref('')
const isLoading = ref(false)
const isAiTyping = ref(false)
const currentEmotion = ref({})
const messagesContainer = ref(null)
const textareaRef = ref(null)

const emotionPollingInterval = ref(null)
const emotionPollingCount = ref(0)
const maxEmotionPollingCount = 30

const showEmergency = ref(false)
const emojiVisible = ref(false)
const isEditingHeaderTitle = ref(false)
const headerTitleEdit = ref('')
const headerTitleInput = ref(null)

const sessionList = ref([])
const sessionListLoading = ref(false)
const loadingMore = ref(false)
const hasMoreSessions = ref(true)
const sessionQuery = reactive({ currentPage: 1, size: 10, keyword: '' })

const emojiList = [
  '😀','😃','😄','😁','😅','😂','😊','😇','🙂','🙃','😉','😌','😍','🥰','😘',
  '😋','😛','😝','😜','🤪','🤨','🧐','🤓','😎','🤩','🥳','😏','😒','😞','😔',
  '😟','😕','🙁','☹️','😣','😖','😫','😩','🥺','😢','😭','😤','😠','😡','🤬',
  '🤯','😳','🤗','🤔','🤭','🤫','🤥','😶','😐','❤️','💔','✨','🌟','🌧️','⛅','🌈','🌻','☕'
]

onMounted(() => { loadSessionList(); createNewFrontendSession(false) })
onUnmounted(() => stopEmotionPolling())

const getEmotionLevelText = (score) => {
  const num = parseInt(score);
  if (isNaN(num)) return '正在感知';
  if (num >= 70) return '高活跃';
  if (num >= 40) return '中等平和';
  return '低沉内敛';
}

const insertEmoji = (emoji) => {
  userMessage.value += emoji
  emojiVisible.value = false
  nextTick(() => { if (textareaRef.value) { textareaRef.value.focus(); autoResize() } })
}

const autoResize = () => {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

const startEmotionPolling = (sessionId) => {
  stopEmotionPolling()
  if (!sessionId) return
  emotionPollingCount.value = 0
  loadSessionEmotion(sessionId)
  emotionPollingInterval.value = setInterval(() => {
    emotionPollingCount.value++
    if (emotionPollingCount.value >= maxEmotionPollingCount) { stopEmotionPolling(); return }
    loadSessionEmotion(sessionId)
  }, 2000)
}

const stopEmotionPolling = () => {
  if (emotionPollingInterval.value) { clearInterval(emotionPollingInterval.value); emotionPollingInterval.value = null; emotionPollingCount.value = 0 }
}

const startNewSession = async (initialMessage, customTitle = null) => {
  try {
    isLoading.value = true
    const sessionParams = { initialMessage: initialMessage }
    if (customTitle) sessionParams.sessionTitle = customTitle
    else if (currentSession.value && currentSession.value.status === 'TEMP' && currentSession.value.sessionTitle !== '新对话') sessionParams.sessionTitle = currentSession.value.sessionTitle
    else sessionParams.sessionTitle = `咨询 - ${new Date().toLocaleDateString()}`
    
    await startChatSession(sessionParams, {
      onSuccess: async (data) => {
        const sessionData = { sessionId: data.sessionId, userHash: data.userHash, status: 'ACTIVE', dbSessionId: data.sessionId.replace('session_', ''), sessionTitle: data.sessionTitle }
        if (currentSession.value && currentSession.value.status === 'TEMP') Object.assign(currentSession.value, sessionData)
        else currentSession.value = sessionData
        refreshSessionList()
        messages.value.push({ id: Date.now(), senderType: 1, content: initialMessage, createdAt: new Date().toISOString(), isComplete: true })
        await startAIResponse(data.sessionId, initialMessage)
      },
      onError: async (error) => ElMessage.error(error.message || '连接时遇到了小问题')
    })
  } catch (error) { ElMessage.error('连接时遇到了小问题') } finally { isLoading.value = false }
}

const sendMessage = async () => {
  if (!userMessage.value.trim()) return
  if (isAiTyping.value) { return }
  const message = userMessage.value.trim()
  userMessage.value = ''
  if (textareaRef.value) textareaRef.value.style.height = 'auto'

  try {
    if (!currentSession.value || currentSession.value.status === 'TEMP') await startNewSession(message)
    else {
      messages.value.push({ id: `user_${Date.now()}`, senderType: 1, content: message, createdAt: new Date().toISOString(), isComplete: true })
      scrollToBottom()
      await startAIResponse(currentSession.value.sessionId, message)
    }
  } catch (error) { ElMessage.error('消息发送失败，请重试') }
}

function getAuthToken() { return userStore.token || localStorage.getItem('token') }

const startAIResponse = async (sessionId, userMessage) => {
  if (isAiTyping.value) return
  isAiTyping.value = true
  const token = getAuthToken()
  const aiMessageId = `ai_${Date.now()}`
  const aiMessage = { id: aiMessageId, senderType: 2, content: '', createdAt: new Date().toISOString(), isComplete: false, isTyping: true }
  messages.value.push(aiMessage)
  
  const findAiMessage = () => messages.value.find(msg => msg.id === aiMessageId)
  const ctrl = new AbortController()
  
  const startSessionEmotionPolling = () => { if (currentSession.value) startEmotionPolling(currentSession.value.dbSessionId || currentSession.value.sessionId) }
  
  const cleanup = (markComplete = false) => {
    isAiTyping.value = false
    const msg = findAiMessage()
    if (msg) { msg.isTyping = false; if (markComplete) msg.isComplete = true }
    if (markComplete) startSessionEmotionPolling()
  }
  
  const handleError = (error, shouldRemoveMessage = false) => {
    cleanup(false)
    if (shouldRemoveMessage) {
      const idx = messages.value.findIndex(msg => msg.id === aiMessageId)
      if (idx !== -1) messages.value.splice(idx, 1)
    } else {
      const msg = findAiMessage()
      if (msg) { msg.content = '网络似乎断开了连接，请稍后再试。'; msg.isComplete = true; msg.isError = true }
    }
  }
  
  try {
    await fetchEventSource('/api/psychological-chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Token': token, 'Accept': 'text/event-stream' },
      body: JSON.stringify({ sessionId, userMessage }),
      signal: ctrl.signal,
      onopen: async (response) => { if (!response.ok) throw new Error(`HTTP ${response.status}`) },
      onclose: () => { cleanup(true); nextTick(() => scrollToBottom()) },
      onmessage: (event) => {
        let raw = (event.data || '').trim()
        if (!raw) return
        let evtName = event.event
        let payloadText = raw
        if (!evtName && raw.startsWith('event:')) {
          const lines = raw.split('\n')
          evtName = (lines.find(l => l.startsWith('event:')) || '').slice(6).trim()
          payloadText = (lines.find(l => l.startsWith('data:')) || '').slice(5).trim()
        }
        if (evtName === 'done') { cleanup(true); nextTick(() => scrollToBottom()); ctrl.abort(); return }
        
        try {
          const payload = JSON.parse(payloadText)
          if (String(payload.code) === '200' && payload.data && payload.data.content) {
            if (evtName !== 'risk-warning' && payload.data.type !== 'risk') {
              const msg = findAiMessage()
              if (msg) { msg.content += payload.data.content; scrollToBottom() }
            }
          }
        } catch (e) {}
      },
      onerror: (err) => { handleError(err, false); throw err }
    })
  } catch (error) { handleError(error, true) } finally { if (isAiTyping.value) cleanup(false) }
}

const loadSessionMessages = async (sessionId) => {
  try { await getSessionMessages(sessionId, { onSuccess: (data) => { messages.value = data.map(msg => ({ ...msg, isComplete: true })); nextTick(() => scrollToBottom()) } }) } catch (error) {}
}

const loadSessionEmotion = async (sessionId) => {
  try {
    const formattedSessionId = sessionId.toString().startsWith('session_') ? sessionId : `session_${sessionId}`
    await getSessionEmotion(formattedSessionId, {
      onSuccess: (data) => {
        if ((data.timestamp || Date.now()) > (currentEmotion.value.timestamp || 0) && emotionPollingCount.value > 0) stopEmotionPolling()
        currentEmotion.value = {
          primaryEmotion: data.primaryEmotion || '宁静', emotionScore: data.emotionScore || 50, isNegative: data.isNegative || false,
          suggestion: data.suggestion || '生活是一场漫长的旅行，按自己的节奏走就好。', icon: '', timestamp: data.timestamp || Date.now()
        }
      },
      onError: () => { if (Object.keys(currentEmotion.value).length === 0) setEmptyEmotion() }
    })
  } catch (error) { if (Object.keys(currentEmotion.value).length === 0) setEmptyEmotion() }
}

const setEmptyEmotion = () => { currentEmotion.value = { primaryEmotion: '正在感知...', emotionScore: '--', isNegative: false, suggestion: '随时分享你的感受，我在这里倾听。', icon: '', timestamp: Date.now() } }

const loadSessionList = async (reset = true) => {
  if (reset) { sessionQuery.currentPage = 1; sessionListLoading.value = true } else loadingMore.value = true
  try { await getSessionsPage(sessionQuery, { onSuccess: (data) => { if (reset) sessionList.value = data.records || []; else sessionList.value.push(...(data.records || [])); hasMoreSessions.value = sessionQuery.currentPage < data.pages } }) } catch (error) {} finally { sessionListLoading.value = false; loadingMore.value = false }
}

const refreshSessionList = () => loadSessionList(true)
const loadMoreSessions = () => { if (hasMoreSessions.value && !loadingMore.value) { sessionQuery.currentPage++; loadSessionList(false) } }

const switchToSession = async (session) => {
  if (session.id === currentSession.value?.dbSessionId) return
  currentSession.value = { sessionId: "session_" + session.id, status: 'ACTIVE', dbSessionId: session.id, sessionTitle: session.sessionTitle }
  await loadSessionMessages(session.id)
  await loadSessionEmotion(session.id)
  startEmotionPolling(session.id)
  refreshSessionList()
}

const confirmDeleteSession = async (session) => {
  const result = await ElMessageBox.confirm('确认要移除这段记录吗？移除后将无法恢复。', '', { confirmButtonText: '确认移除', cancelButtonText: '取消', type: 'warning' }).catch(() => 'cancel')
  if (result !== 'confirm') return
  await deleteSession(session.id, { onSuccess: () => { if (currentSession.value?.dbSessionId === session.id) createNewFrontendSession(false); refreshSessionList() } })
}

const formatSessionTime = (timeStr) => {
  if (!timeStr) return ''
  const diff = new Date() - new Date(timeStr)
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return new Date(timeStr).toLocaleDateString()
}

const showEmergencyDialog = () => showEmergency.value = true

const createNewFrontendSession = () => {
  currentSession.value = { sessionId: `temp_${Date.now()}`, status: 'TEMP', sessionTitle: '新对话', isNew: true }
  messages.value = []
  setEmptyEmotion()
  stopEmotionPolling()
  nextTick(() => { scrollToBottom(); if(textareaRef.value) textareaRef.value.focus() })
}

const startEditTitle = (session) => { session.isEditing = true; session.editingTitle = session.sessionTitle || '' }
const saveSessionTitle = async (session) => {
  if (!session.isEditing) return
  const newTitle = (session.editingTitle || '').trim()
  if (newTitle === (session.sessionTitle || '')) return cancelEditTitle(session)
  await updateSessionTitle(session.id, { sessionTitle: newTitle || null }, { onSuccess: () => { session.sessionTitle = newTitle || `咨询 - ${formatSessionTime(session.startedAt)}`; session.isEditing = false } })
}
const cancelEditTitle = (session) => { session.isEditing = false; session.editingTitle = '' }

const startEditHeaderTitle = () => { if (!currentSession.value) return; isEditingHeaderTitle.value = true; headerTitleEdit.value = currentSession.value.sessionTitle || ''; nextTick(() => { if (headerTitleInput.value) headerTitleInput.value.focus() }) }
const saveHeaderTitle = async () => {
  if (!currentSession.value || !isEditingHeaderTitle.value) return
  const newTitle = headerTitleEdit.value.trim()
  if (newTitle === (currentSession.value.sessionTitle || '')) return cancelEditHeaderTitle()
  if (currentSession.value.status === 'TEMP') { currentSession.value.sessionTitle = newTitle || '新对话'; isEditingHeaderTitle.value = false; return }
  await updateSessionTitle(currentSession.value.dbSessionId, { sessionTitle: newTitle || null }, { onSuccess: () => { currentSession.value.sessionTitle = newTitle || `咨询 - ${new Date().toLocaleDateString()}`; const sessionInList = sessionList.value.find(s => s.id === currentSession.value.dbSessionId); if (sessionInList) sessionInList.sessionTitle = currentSession.value.sessionTitle; isEditingHeaderTitle.value = false } })
}
const cancelEditHeaderTitle = () => { isEditingHeaderTitle.value = false; headerTitleEdit.value = '' }
const handleKeyDown = (event) => { if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); sendMessage() } }
const scrollToBottom = () => nextTick(() => { if (messagesContainer.value) messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight })
const formatMessageContent = (content) => content.replace(/\n/g, '<br>')
const formatTime = (timestamp) => new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
</script>

<style scoped>
/* =========================================================================
   清透特效版毛玻璃设计 (Clear Glassmorphism)
   干净背景 + 灵动光效头像 + 高级质感
========================================================================= */

:root {
  /* 背景极度干净，带一点点蓝灰的呼吸感 */
  --bg-base: #F4F7F9;
  
  /* 清透卡片 */
  --glass-bg: rgba(255, 255, 255, 0.65);
  --glass-border: rgba(255, 255, 255, 0.8);
  --glass-shadow: 0 8px 32px rgba(31, 38, 135, 0.05);
  
  /* 文字清晰 */
  --text-main: #334155;
  --text-sub: #64748B;
  
  /* 品牌色 */
  --primary: #6366F1;
  --primary-light: #EEF2FF;
}

* { box-sizing: border-box; }

.healing-room-page {
  min-height: 100vh;
  background-color: var(--bg-base);
  padding: 24px 0;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
  color: var(--text-main);
  position: relative;
  overflow: hidden;
}

.container { max-width: 1400px; margin: 0 auto; padding: 0 24px; position: relative; z-index: 10; }
.room-layout { display: grid; grid-template-columns: 280px 1fr; gap: 24px; height: calc(100vh - 48px); }

/* --- 高级清透卡片 --- */
.glass-card {
  background: var(--glass-bg);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border-radius: 20px;
  border: 1px solid var(--glass-border);
  box-shadow: var(--glass-shadow);
  display: flex; flex-direction: column; overflow: hidden;
  transition: box-shadow 0.3s ease;
}
.glass-card:hover { box-shadow: 0 12px 40px rgba(31, 38, 135, 0.08); }

/* ================== 左侧模块 ================== */
.sidebar { display: flex; flex-direction: column; gap: 16px; overflow-y: auto; padding-right: 4px; padding-bottom: 20px; }
.sidebar::-webkit-scrollbar { width: 4px; }
.sidebar::-webkit-scrollbar-thumb { background: rgba(99,102,241,0.2); border-radius: 4px; }

/* 1. AI 头像：呼吸微光球 + 旋转玻璃环 (科技愈疗感) */
.ai-presence-card { padding: 32px 20px; align-items: center; flex-shrink: 0; }
.premium-ai-avatar {
  position: relative; width: 64px; height: 64px; margin-bottom: 16px;
  display: flex; align-items: center; justify-content: center;
}
.glass-ring {
  position: absolute; border-radius: 50%; border: 1px solid rgba(99,102,241,0.3);
  animation: spin linear infinite;
}
.ring-1 { width: 100%; height: 100%; animation-duration: 8s; border-left-color: var(--primary); }
.ring-2 { width: 80%; height: 80%; animation-duration: 5s; animation-direction: reverse; border-right-color: #8B5CF6; opacity: 0.6;}
@keyframes spin { to { transform: rotate(360deg); } }
.glow-core {
  width: 36px; height: 36px; border-radius: 50%;
  background: linear-gradient(135deg, #A5B4FC, var(--primary));
  box-shadow: 0 0 16px rgba(99,102,241,0.5);
  display: flex; align-items: center; justify-content: center;
  color: white; font-size: 14px;
  animation: core-breath 3s ease-in-out infinite alternate;
}
@keyframes core-breath { 0% { transform: scale(0.9); box-shadow: 0 0 12px rgba(99,102,241,0.4); } 100% { transform: scale(1.1); box-shadow: 0 0 24px rgba(99,102,241,0.7); } }

.ai-name { font-size: 16px; font-weight: 600; margin: 0 0 6px; color: var(--text-main); }
.ai-status { font-size: 12px; color: var(--text-sub); display: flex; align-items: center; gap: 6px; }
.pulse-dot { width: 6px; height: 6px; background: #10B981; border-radius: 50%; box-shadow: 0 0 8px #34D399; animation: dot-pulse 2s infinite; }
@keyframes dot-pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.4; } }

/* 2. 心境感知雷达 */
.mood-radar-card { padding: 20px; flex-shrink: 0; }
.card-header { margin-bottom: 16px; }
.card-title { font-size: 14px; font-weight: 600; color: var(--text-main); }

.mood-visualizer-area { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
.crystal-shape {
  width: 44px; height: 44px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; font-size: 20px;
  background: rgba(255,255,255,0.8); border: 1px solid rgba(255,255,255,1);
  box-shadow: inset 0 2px 6px rgba(255,255,255,0.8), 0 4px 12px rgba(0,0,0,0.04);
}
.crystal-calm { color: #647B91; } .crystal-warm { color: #A68B7C; }

.mood-text-group { flex: 1; min-width: 0; word-break: break-word; }
.mood-primary { font-size: 16px; font-weight: 600; margin-bottom: 4px; }
.mood-secondary { font-size: 12px; color: var(--text-sub); }
.mood-secondary span { font-weight: 600; color: var(--text-main); }

.assessment-metrics { margin-bottom: 16px; }
.metric-row { display: flex; justify-content: space-between; align-items: center; padding: 10px 12px; background: rgba(255,255,255,0.5); border-radius: 8px; }
.metric-label { font-size: 12px; color: var(--text-sub); }
.metric-value { font-size: 12px; font-weight: 600; }
.text-warm { color: #D97706; } .text-calm { color: #059669; }

.insight-box { padding: 12px; background: rgba(99,102,241,0.05); border-radius: 8px; border: 1px solid rgba(99,102,241,0.1); }
.insight-text { font-size: 12px; color: var(--text-main); line-height: 1.6; word-break: break-word; white-space: normal;}

/* 3. 记忆碎片 */
.memory-card { padding: 20px; flex-shrink: 0; display: flex; flex-direction: column; }
.ghost-icon-btn { background: none; border: none; color: var(--text-sub); cursor: pointer; transition: 0.2s; }

.memory-list { max-height: 240px; overflow-y: auto; padding-right: 4px; }
.memory-list::-webkit-scrollbar { width: 2px; }
.memory-item {
  position: relative; display: flex; padding: 12px;
  border-radius: 12px; border: 1px solid transparent; margin-bottom: 8px; cursor: pointer; transition: 0.2s;
}
.memory-item:hover { background: rgba(255,255,255,0.8); }
.memory-item.is-active { background: rgba(255,255,255,0.9); border-color: rgba(99,102,241,0.2); box-shadow: 0 2px 8px rgba(99,102,241,0.05);}

.emotion-bar { position: absolute; left: 0; top: 12px; bottom: 12px; width: 3px; background: transparent; border-radius: 0 4px 4px 0; transition: 0.2s; }
.memory-item.is-active .emotion-bar { background: var(--primary); }

.memory-content { flex: 1; min-width: 0; }
.memory-title { font-size: 13px; font-weight: 600; margin-bottom: 4px; }
.stealth-input { width: 100%; border: none; border-bottom: 1px solid var(--primary); outline: none; background: transparent; font-size: 13px; color: var(--text-main);}
.memory-preview { font-size: 12px; color: var(--text-sub); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 6px; }
.memory-meta { display: flex; justify-content: space-between; align-items: center; }
.memory-time { font-size: 11px; color: var(--text-sub); }
.delete-btn { background: none; border: none; color: var(--text-sub); cursor: pointer; opacity: 0; transition: 0.2s;}
.memory-item:hover .delete-btn { opacity: 1; }
.delete-btn:hover { color: #EF4444; }

.load-more-box { text-align: center; margin-top: 8px; }
.text-btn { background: none; border: none; color: var(--text-sub); font-size: 12px; cursor: pointer; transition: 0.2s; }
.text-btn:hover { color: var(--primary); }

/* 4. 人工介入 */
.human-support-card { padding: 20px; background: linear-gradient(135deg, rgba(255,255,255,0.8), rgba(245,243,255,0.8)); flex-shrink: 0; }
.support-title { font-size: 14px; font-weight: 600; color: var(--text-main); margin-bottom: 8px; display: flex; align-items: center; gap: 6px;}
.support-title i { color: var(--primary); }
.support-desc { font-size: 12px; color: var(--text-sub); line-height: 1.6; margin-bottom: 16px; }
.support-action-btn {
  width: 100%; padding: 10px; border-radius: 12px; background: white;
  border: 1px solid rgba(99,102,241,0.2); color: var(--primary); font-size: 13px; font-weight: 500; cursor: pointer; transition: 0.2s; box-shadow: 0 2px 6px rgba(99,102,241,0.05);
}
.support-action-btn:hover { border-color: var(--primary); box-shadow: 0 4px 12px rgba(99,102,241,0.1); }


/* ================== 右侧聊天区 ================== */
.chat-space { display: flex; flex-direction: column; overflow: hidden; }

/* 顶部 */
.chat-header { padding: 20px 32px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(0,0,0,0.03); }
.header-info { display: flex; flex-direction: column; gap: 4px; }
.main-title { font-size: 16px; font-weight: 600; margin: 0; }
.stealth-header-input { font-size: 16px; font-weight: 600; color: var(--text-main); border: none; border-bottom: 1px solid var(--primary); outline: none; background: transparent; }
.privacy-note { font-size: 11px; color: #10B981; display: flex; align-items: center; gap: 4px;}
.outline-btn { padding: 8px 16px; border-radius: 20px; background: white; border: 1px solid rgba(99,102,241,0.2); color: var(--primary); font-size: 12px; font-weight: 500; cursor: pointer; transition: 0.2s; box-shadow: 0 2px 6px rgba(0,0,0,0.02);}
.outline-btn:hover { border-color: var(--primary); }

/* 消息流：添加了 overflow-x: hidden 解决底部横向灰色滚动条问题 */
.chat-flow { flex: 1; padding: 32px; overflow-y: auto; overflow-x: hidden; scroll-behavior: smooth; }
.chat-flow::-webkit-scrollbar { width: 6px; } .chat-flow::-webkit-scrollbar-thumb { background: #E5E7EB; border-radius: 3px; }

/* 全新极简欢迎屏（去除了所有的横线和光效 bug） */
.welcome-screen { text-align: center; padding: 60px 0; }
.welcome-icon-box { 
  width: 64px; height: 64px; margin: 0 auto 20px; border-radius: 50%;
  background: #EEF2FF; color: var(--primary); font-size: 28px;
  display: flex; justify-content: center; align-items: center;
  animation: soft-bounce 3s infinite ease-in-out;
}
@keyframes soft-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}
.welcome-title { font-size: 18px; font-weight: 600; margin-bottom: 12px; }
.welcome-text { font-size: 13px; color: var(--text-sub); line-height: 1.8; }

.message-row { display: flex; gap: 12px; margin-bottom: 24px; animation: slide-up 0.3s ease forwards; }
@keyframes slide-up { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }

/* 统一头像容器 */
.chat-avatar { 
  width: 36px; height: 36px; display: flex; justify-content: center; align-items: center; flex-shrink: 0;
}
/* 修改3：紫色的渐变磨砂球 AI 头像 */
.ai-frosted-orb { 
  width: 100%; height: 100%; border-radius: 50%; 
  background: linear-gradient(135deg, #A78BFA, #7C3AED); 
  box-shadow: inset 0 2px 4px rgba(255,255,255,0.4), 0 2px 8px rgba(89, 69, 122, 0.3);
  display: flex; justify-content: center; align-items: center; color: white; font-size: 16px;
}
/* 干净的用户头像 */
.user-simple-avatar {
  width: 100%; height: 100%; border-radius: 50%; 
  background: rgba(255,255,255,0.8); border: 1px solid rgba(255,255,255,1); 
  display: flex; justify-content: center; align-items: center; color: var(--text-sub); box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.message-content { max-width: 75%; }
.is-user { flex-direction: row-reverse; }
.is-user .message-content { display: flex; flex-direction: column; align-items: flex-end; }

/* 气泡 */
.bubble { padding: 14px 20px; font-size: 14px; line-height: 1.7; word-break: break-word; white-space: normal; max-width: 100%;}
.ai-bubble { background: rgba(255, 255, 255, 0.9); border: 1px solid rgba(255,255,255,1); color: var(--text-main); border-radius: 4px 16px 16px 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.03); }
.user-bubble { background: linear-gradient(135deg, #F5F7FA, #EEF2FF); border: 1px solid #FFFFFF; color: var(--text-main); border-radius: 16px 4px 16px 16px; box-shadow: 0 2px 12px rgba(0,0,0,0.02);}

.message-time { font-size: 11px; color: #8C867D; margin-top: 8px; padding: 0 4px; }

/* 打字动画 */
.typing-dots { display: flex; gap: 4px; padding: 4px; align-items: center; height: 20px;}
.typing-dots span { width: 5px; height: 5px; background: var(--primary); border-radius: 50%; animation: blink 1.4s infinite; opacity: 0.4;}
.typing-dots span:nth-child(2) { animation-delay: 0.2s; } .typing-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes blink { 0%, 100% { opacity: 0.4; transform: scale(1); } 50% { opacity: 1; transform: scale(1.2); } }
.error-msg { color: #EF4444; font-size: 13px;}

/* 输入区 (修改1：去掉了 input-footer 等多余元素，完全贴底) */
.input-area { padding: 20px 32px 32px 32px; background: transparent; border-top: 1px solid rgba(0,0,0,0.03); display: flex; gap: 16px; align-items: flex-end; }
.input-box { flex: 1; background: rgba(255,255,255,0.8); border-radius: 16px; padding: 12px 16px; border: 1px solid rgba(255,255,255,1); transition: 0.3s; box-shadow: inset 0 2px 6px rgba(0,0,0,0.01), 0 2px 12px rgba(0,0,0,0.02);}
.input-box:focus-within { border-color: rgba(99,102,241,0.3); background: #FFFFFF; box-shadow: 0 4px 16px rgba(99,102,241,0.05);}
.clean-textarea { width: 100%; border: none; outline: none; font-size: 16px; font-weight: 600; color: #1e293b; resize: none; max-height: 100px; line-height: 1.6; background: transparent;}
.clean-textarea::placeholder { color: var(--text-sub); }

.input-tools { margin-top: 4px; }
.emoji-btn { background: none; border: none; color: var(--text-sub); font-size: 18px; cursor: pointer; transition: 0.2s;}
.emoji-btn:hover { color: var(--primary); }

.send-btn {
  width: 46px; height: 46px; border-radius: 14px; background: rgba(255,255,255,0.8); color: var(--text-sub);
  font-size: 16px; border: 1px solid rgba(255,255,255,1); cursor: not-allowed; transition: all 0.3s; flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
}
.send-btn.can-send { background: linear-gradient(135deg, #A5B4FC, var(--primary)); color: rgb(162, 103, 206); border: none; cursor: pointer; box-shadow: 0 4px 12px rgba(99,102,241,0.2);}
.send-btn.can-send:hover { opacity: 0.9; transform: translateY(-2px); box-shadow: 0 6px 16px rgba(99,102,241,0.3);}

/* 极简弹窗 */
:deep(.professional-dialog) { border-radius: 20px; background: rgba(255,255,255,0.95); backdrop-filter: blur(20px);}
:deep(.el-dialog__header) { padding: 0; }
:deep(.el-dialog__body) { padding: 0; }
.dialog-header-clean { padding: 24px 24px 16px; border-bottom: 1px solid rgba(0,0,0,0.03); }
.dialog-header-clean h2 { font-size: 16px; font-weight: 600; color: var(--text-main); margin: 0; }
.dialog-body-clean { padding: 20px 24px 32px; }
.dialog-desc { font-size: 13px; color: var(--text-sub); margin-bottom: 24px; line-height: 1.6; }
.contact-card { padding: 16px; border-radius: 12px; background: var(--bg-base); border: 1px solid rgba(0,0,0,0.03); margin-bottom: 12px; transition: 0.3s;}
.contact-card:hover { background: #FFFFFF; box-shadow: 0 4px 12px rgba(0,0,0,0.03); }
.contact-name { font-size: 13px; color: var(--text-main); margin-bottom: 4px; }
.contact-number { font-size: 18px; font-weight: 600; color: var(--primary); }

/* 表情面板 */
.clean-popover { border-radius: 16px !important; padding: 12px !important; border: 1px solid rgba(255,255,255,1) !important; background: rgba(255,255,255,0.9) !important; backdrop-filter: blur(20px) !important; box-shadow: 0 8px 32px rgba(31, 38, 135, 0.1) !important; }
.emoji-scroll { max-height: 200px; overflow-y: auto; padding-right: 4px; }
.emoji-scroll::-webkit-scrollbar { width: 4px; } .emoji-scroll::-webkit-scrollbar-thumb { background: #E5E7EB; border-radius: 4px; }
.emoji-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 8px; }
.emoji-item { font-size: 20px; cursor: pointer; text-align: center; padding: 4px; border-radius: 8px; transition: 0.2s;}
.emoji-item:hover { background: rgba(99,102,241,0.1); transform: scale(1.1);}

@media (max-width: 1024px) { .room-layout { grid-template-columns: 240px 1fr; } }
@media (max-width: 768px) { .room-layout { grid-template-columns: 1fr; height: auto; } .chat-space { height: 75vh; } }
</style>