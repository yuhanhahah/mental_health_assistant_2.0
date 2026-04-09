<template>
  <div class="consultation-management">
    <div class="page-header glass-card">
      <div class="header-content">
        <div class="header-left">
          <h2 class="page-title">咨询记录管理</h2>
        </div>
        <div class="header-actions">
          </div>
      </div>
    </div>

    <div class="search-area glass-card">
      <el-form 
        :model="queryForm" 
        inline 
        label-width="80px"
        @submit.prevent="fetchSessions"
        class="clean-form"
      >
        <el-form-item label="情绪标签">
          <el-select v-model="queryForm.emotionTag" placeholder="选择情绪" clearable class="clean-select">
            <el-option label="全部情绪" value=""></el-option>
            <el-option label="焦虑" value="焦虑"></el-option>
            <el-option label="抑郁" value="抑郁"></el-option>
            <el-option label="压力" value="压力"></el-option>
            <el-option label="愤怒" value="愤怒"></el-option>
            <el-option label="孤独" value="孤独"></el-option>
            <el-option label="迷茫" value="迷茫"></el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            @change="handleDateRangeChange"
            clearable
            class="clean-date-picker"
          />
        </el-form-item>
        
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="搜索用户或内容"
            @clear="fetchSessions"
            @keyup.enter="fetchSessions"
            clearable
            class="clean-input"
          >
            <template #prefix>
              <i class="fas fa-search"></i>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" class="dark-btn" @click="fetchSessions">搜索</el-button>
          <el-button class="ghost-btn" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="glass-card table-wrapper">
      <el-table 
        :data="sessionList" 
        v-loading="loading"
        stripe
        style="width: 100%"
        class="custom-table"
      >
        <el-table-column prop="userNickname" label="用户信息" width="200">
          <template #default="scope">
            <div class="user-info">
              <el-avatar :src="scope.row.userAvatar" :size="36" class="clean-avatar">
                <i class="fas fa-user"></i>
              </el-avatar>
              <div class="user-details">
                <div class="user-name">{{ scope.row.userNickname || '未知用户' }}</div>
                <div class="user-id">ID: {{ scope.row.userId }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="sessionTitle" label="会话内容" min-width="250">
          <template #default="scope">
            <div class="session-content">
              <div class="session-title">{{ scope.row.sessionTitle || '未命名会话' }}</div>
              <div class="session-preview">{{ scope.row.lastMessageContent || '暂无消息' }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="messageCount" label="数据统计" width="150">
          <template #default="scope">
            <div class="session-meta">
               <span class="message-count"><i class="fas fa-comment-dots"></i> {{ scope.row.messageCount || 0 }} 条</span>
               <span class="duration-time"><i class="fas fa-clock"></i> {{ scope.row.durationMinutes || 0 }} 分钟</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="startedAt" label="发生时间" width="180">
          <template #default="scope">
            <div class="time-info">
              <i class="fas fa-calendar-alt"></i> {{ formatDateTime(scope.row.startedAt) }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                class="ghost-btn" 
                size="small" 
                @click="viewSessionDetail(scope.row)"
                title="查看详情"
              >
                <i class="fas fa-eye" style="margin-right: 4px;"></i>详情
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          :current-page="queryForm.currentPage"
          :page-size="queryForm.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
          class="clean-pagination"
        />
      </div>
    </div>

    <el-dialog 
      v-model="showDetailDialog" 
      title="咨询会话详情" 
      width="70%"
      :close-on-click-modal="false"
      class="clean-dialog"
    >
      <div v-if="selectedSession" class="session-detail">
        <div class="detail-header">
          <div class="detail-row">
            <span class="detail-label">用户：</span>
            <span class="detail-value">{{ selectedSession.userNickname || '未知用户' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">时间：</span>
            <span class="detail-value">{{ formatDateTime(selectedSession.startedAt) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">统计：</span>
            <span class="detail-value">{{ selectedSession.messageCount || 0 }}条 · {{ selectedSession.durationMinutes || 0 }}分钟</span>
          </div>
        </div>

        <div class="messages-container">
          <div class="messages-header">
            <h4>对话记录</h4>
          </div>
          <div class="messages-list" v-loading="loadingMessages">
            <div
              v-for="message in sessionMessages"
              :key="message.id"
              class="message-item"
              :class="message.senderType === 1 ? 'user-message' : 'ai-message'"
            >
              <div class="message-header">
                <span class="sender">
                  <i :class="message.senderType === 1 ? 'fas fa-user' : 'fas fa-robot'"></i>
                  {{ message.senderType === 1 ? '用户' : 'AI助手' }}
                </span>
                <span class="time">{{ formatTime(message.createdAt) }}</span>
              </div>
              <div class="message-content">{{ message.content }}</div>
            </div>
            <div v-if="!sessionMessages.length && !loadingMessages" class="no-messages">
              <i class="fas fa-inbox"></i>
              <p>暂无对话记录</p>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button class="dark-btn" @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
// [JS逻辑代码保持不变]
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSessionsPage, getSessionDetail, getSessionMessages } from '@/api/consultationSession'

const loading = ref(false)
const loadingMessages = ref(false)
const sessionList = ref([])
const total = ref(0)
const dateRange = ref([])
const showDetailDialog = ref(false)
const selectedSession = ref(null)
const sessionMessages = ref([])

const queryForm = reactive({
  currentPage: 1,
  size: 20,
  emotionTag: '',
  startDate: '',
  endDate: '',
  keyword: ''
})

onMounted(() => { fetchSessions() })

const fetchSessions = async () => {
  loading.value = true
  try {
    await getSessionsPage(queryForm, {
      onSuccess: (data) => { sessionList.value = data.records || []; total.value = data.total || 0 },
      onError: (error) => { ElMessage.error('获取咨询记录失败: ' + error.message) }
    })
  } catch (error) { console.error('获取咨询记录失败:', error); ElMessage.error('获取咨询记录失败') } 
  finally { loading.value = false }
}

const handleDateRangeChange = (dates) => {
  if (dates && dates.length === 2) { queryForm.startDate = dates[0]; queryForm.endDate = dates[1] } 
  else { queryForm.startDate = ''; queryForm.endDate = '' }
}

const handleReset = () => {
  Object.assign(queryForm, { currentPage: 1, size: 20, emotionTag: '', startDate: '', endDate: '', keyword: '' })
  dateRange.value = []
  fetchSessions()
}

const handleSizeChange = (newSize) => { queryForm.size = newSize; fetchSessions() }
const handleCurrentChange = (newPage) => { queryForm.currentPage = newPage; fetchSessions() }

const viewSessionDetail = async (session) => {
  selectedSession.value = session
  showDetailDialog.value = true
  loadingMessages.value = true
  try {
    await getSessionMessages(session.id, {
      onSuccess: (messages) => { sessionMessages.value = messages || [] },
      onError: (error) => { ElMessage.error('获取会话消息失败: ' + error.message) }
    })
  } catch (error) { console.error('获取会话消息失败:', error); ElMessage.error('获取会话消息失败') } 
  finally { loadingMessages.value = false }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const formatTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
/* 极简黑白灰 Slate 色系 */
:root {
  --primary: #1f2937;
  --bg-page: #f3f4f6;
  --card-bg: #ffffff;
  --border-color: #e5e7eb;
  --text-main: #111827;
  --text-sub: #6b7280;
}

.consultation-management {
  padding: 2rem;
  background: #f3f4f6; /* 统一底色 */
  min-height: 100vh;
  font-family: 'Noto Sans SC', -apple-system, sans-serif;
  color: #111827;
}

/* 统一卡片风格 */
.glass-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  margin-bottom: 1.5rem;
}

.page-header {
  padding: 1.5rem 2rem;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

/* 按钮覆写 */
.dark-btn {
  background-color: #1f2937 !important;
  border-color: #1f2937 !important;
  color: #fff !important;
  font-weight: 500;
  transition: all 0.2s;
}
.dark-btn:hover {
  background-color: #374151 !important;
  border-color: #374151 !important;
  transform: translateY(-1px);
}

.ghost-btn {
  background: transparent !important;
  border: 1px solid #e5e7eb !important;
  color: #111827 !important;
  font-weight: 500;
  transition: all 0.2s;
}
.ghost-btn:hover {
  background: #f9fafb !important;
  border-color: #1f2937 !important;
  color: #1f2937 !important;
}

/* 搜索区域黑白灰化 */
.search-area {
  padding: 1.5rem 2rem;
}

.search-area :deep(.el-form) { margin: 0; }
.search-area :deep(.el-form-item) { margin-bottom: 1rem; margin-right: 1.5rem; }
.search-area :deep(.el-form-item__label) { color: #111827; font-weight: 600; font-size: 0.875rem; }

.search-area :deep(.el-input__wrapper), 
.search-area :deep(.el-select .el-input__wrapper),
.search-area :deep(.el-date-editor .el-input__wrapper) {
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
  box-shadow: none !important;
}

.search-area :deep(.el-input__wrapper:focus-within), 
.search-area :deep(.el-select .el-input__wrapper:focus-within),
.search-area :deep(.el-date-editor .el-input__wrapper:focus-within) {
  background: #fff;
  border-color: #1f2937;
  box-shadow: 0 0 0 1px rgba(31, 41, 55, 0.1) !important;
}

/* 表格样式覆盖 */
.table-wrapper {
  padding: 0;
  overflow: hidden;
}

.custom-table {
  --el-table-border-color: #e5e7eb;
  --el-table-header-bg-color: #f9fafb;
  --el-table-header-text-color: #4b5563;
  --el-table-text-color: #111827;
}

.custom-table :deep(th.el-table__cell) {
  font-weight: 600;
  font-size: 0.875rem;
}

/* 表格内部元素 */
.user-info { display: flex; align-items: center; gap: 12px; }
.clean-avatar { background: #f3f4f6; color: #9ca3af; border: 1px solid #e5e7eb; }
.user-name { font-weight: 600; color: #111827; font-size: 14px; }
.user-id { font-size: 12px; color: #6b7280; }

.session-title { font-weight: 600; color: #111827; margin-bottom: 4px; font-size: 14px; }
.session-preview { font-size: 13px; color: #6b7280; display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-height: 1.5; }

.session-meta { display: flex; flex-direction: column; gap: 4px; font-size: 13px; color: #4b5563; }
.session-meta i { width: 14px; color: #9ca3af; text-align: center; margin-right: 4px;}

.time-info { font-size: 13px; color: #4b5563; display: flex; align-items: center;}
.time-info i { margin-right: 6px; color: #9ca3af; }

.action-buttons { display: flex; justify-content: center; }

/* 分页 */
.pagination-container { padding: 1.5rem; display: flex; justify-content: center; background: #fff; border-top: 1px solid #e5e7eb; }
:deep(.clean-pagination .el-pager li.is-active) { background-color: #1f2937 !important; color: white !important; }

/* === 极简对话框样式 === */
:deep(.clean-dialog) { border-radius: 16px; overflow: hidden; }
:deep(.clean-dialog .el-dialog__header) { padding: 1.5rem; border-bottom: 1px solid #e5e7eb; margin-right: 0; }
:deep(.clean-dialog .el-dialog__title) { font-weight: 700; color: #111827; }
:deep(.clean-dialog .el-dialog__body) { padding: 1.5rem; background: #f9fafb; }
:deep(.clean-dialog .el-dialog__footer) { padding: 1rem 1.5rem; border-top: 1px solid #e5e7eb; background: #fff; }

.session-detail { display: flex; flex-direction: column; gap: 1.5rem; }

.detail-header {
  padding: 1rem 1.5rem;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.detail-row { display: flex; align-items: center; font-size: 13px;}
.detail-label { font-weight: 600; color: #6b7280; margin-right: 4px; }
.detail-value { color: #111827; font-weight: 500; }

.messages-container { display: flex; flex-direction: column; gap: 1rem; }
.messages-header h4 { margin: 0; color: #111827; font-size: 15px; font-weight: 700; }

.messages-list {
  max-height: 450px;
  overflow-y: auto;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 1.5rem;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
/* 滚动条极简 */
.messages-list::-webkit-scrollbar { width: 6px; }
.messages-list::-webkit-scrollbar-thumb { background: #d1d5db; border-radius: 3px; }

/* 聊天气泡极简配色 */
.message-item {
  padding: 1rem 1.25rem;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  width: 85%;
}

.user-message { background: #f3f4f6; align-self: flex-end; border-color: transparent; }
.ai-message { background: #ffffff; align-self: flex-start; }

.message-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; border-bottom: 1px solid rgba(0,0,0,0.05); padding-bottom: 8px;}
.sender { font-weight: 600; color: #111827; font-size: 13px; display: flex; align-items: center; gap: 6px; }
.sender i { color: #1f2937; opacity: 0.8; }
.time { font-size: 12px; color: #9ca3af; }

.message-content { color: #374151; line-height: 1.6; font-size: 14px; word-break: break-word;}

.no-messages { text-align: center; color: #9ca3af; padding: 3rem 0; display: flex; flex-direction: column; gap: 1rem; align-items: center; }
.no-messages i { font-size: 2rem; opacity: 0.5; }
.no-messages p { margin: 0; font-size: 14px; }

@media (max-width: 768px) {
  .consultation-management { padding: 1rem; }
  .detail-header { flex-direction: column; gap: 0.5rem; }
  .message-item { width: 100%; }
}
</style>