<template>
  <div class="profile-container">
    <section class="profile-header">
      <div class="header-content">
        <div class="profile-info">
          <div class="avatar-section">
            <div class="avatar-wrapper">
              <el-avatar :size="96" :src="currentAvatarUrl" @error="() => false">
                <i class="fas fa-user"></i>
              </el-avatar>
              <el-upload
                class="avatar-upload"
                action="#"
                :show-file-list="false"
                :http-request="handleUploadAvatar"
                :before-upload="beforeAvatarUpload"
                :disabled="uploadingAvatar"
              >
                <el-button circle class="upload-btn" :loading="uploadingAvatar">
                  <i v-if="!uploadingAvatar" class="fas fa-camera"></i>
                </el-button>
              </el-upload>
            </div>
            <div class="user-details">
              <h2 class="user-name">{{ userProfile.displayName }}</h2>
              <p class="user-email">{{ userProfile.email }}</p>
              <p class="join-date">加入时间：{{ formatDate(userProfile.createdAt) }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <div class="main-content">
      <div class="content-wrapper">
        <div class="sidebar">
          <div class="nav-card">
            <h3 class="nav-title">个人中心</h3>
            <nav class="nav-menu">
              <button 
                v-for="tab in tabs" 
                :key="tab.key"
                :class="['tab-item', activeTab === tab.key ? 'active' : '']"
                @click="activeTab = tab.key"
              >
                <i :class="tab.icon"></i>{{ tab.label }}
              </button>
            </nav>
          </div>
        </div>

        <div class="main-section">

          <div v-show="activeTab === 'personal'" class="content-card">
            <h3 class="section-title">
              <i class="fas fa-user"></i>个人信息
            </h3>
            
            <el-form 
              ref="userFormRef" 
              :model="userForm" 
              :rules="userFormRules" 
              label-width="120px"
              class="user-form"
            >
              <div class="form-grid">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="userForm.username" disabled />
                </el-form-item>
                
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
                </el-form-item>
                
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="userForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="userForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                
                <el-form-item label="性别" prop="gender" class="gender-item">
                  <el-radio-group v-model="userForm.gender">
                    <el-radio :label="0">未知</el-radio>
                    <el-radio :label="1">男</el-radio>
                    <el-radio :label="2">女</el-radio>
                  </el-radio-group>
                </el-form-item>
                
                <el-form-item label="生日" prop="birthday">
                  <el-date-picker
                    v-model="userForm.birthday"
                    type="date"
                    placeholder="选择生日"
                    style="width: 100%"
                  />
                </el-form-item>
              </div>
              
              <el-form-item class="form-actions">
                <el-button type="primary" :loading="updateLoading" @click="updateUserInfo" class="save-btn">
                  <i class="fas fa-save"></i> 保存修改
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <div v-show="activeTab === 'password'" class="content-card">
            <h3 class="section-title">
              <i class="fas fa-key"></i>修改密码
            </h3>
            
            <el-form 
              ref="passwordFormRef" 
              :model="passwordForm" 
              :rules="passwordFormRules" 
              label-width="120px"
              class="password-form"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input 
                  v-model="passwordForm.oldPassword" 
                  type="password" 
                  placeholder="请输入原密码"
                  show-password 
                />
              </el-form-item>
              
              <el-form-item label="新密码" prop="newPassword">
                <el-input 
                  v-model="passwordForm.newPassword" 
                  type="password" 
                  placeholder="请输入新密码"
                  show-password 
                />
              </el-form-item>
              
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input 
                  v-model="passwordForm.confirmPassword" 
                  type="password" 
                  placeholder="请再次输入新密码"
                  show-password 
                />
              </el-form-item>
              
              <el-form-item class="form-actions">
                <el-button type="primary" :loading="passwordLoading" @click="updatePassword" class="save-btn">
                  <i class="fas fa-shield-alt"></i> 修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// [脚本逻辑完全保持不变，确保你的上传和更新功能正常工作]
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getCurrentUser, updateUser, updatePassword as updateUserPassword } from '@/api/user'
import { uploadTempBusinessFile, confirmTempFile } from '@/api/FileApi'
import { formatDate } from '@/utils/dateUtils'

const userStore = useUserStore()
const activeTab = ref('personal')
const updateLoading = ref(false)
const passwordLoading = ref(false)
const userFormRef = ref()
const passwordFormRef = ref()

const tabs = [
  { key: 'personal', label: '个人信息', icon: 'fas fa-user' },
  { key: 'password', label: '修改密码', icon: 'fas fa-key' }
]

const userProfile = reactive({ id: '', username: '', nickname: '', email: '', phone: '', gender: 0, birthday: '', avatar: '', displayName: '', userType: 1, status: 1, createdAt: '' })
const userForm = reactive({ username: '', nickname: '', email: '', phone: '', gender: 0, birthday: '', avatar: '' })
const tempFileId = ref(null)
const uploadingAvatar = ref(false)
const tempAvatarUrl = ref(null)
const currentAvatarUrl = computed(() => tempAvatarUrl.value || userProfile.avatar)
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const userFormRules = reactive({
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }, { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱地址', trigger: 'blur' }, { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }]
})

const passwordFormRules = reactive({
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请再次输入新密码', trigger: 'blur' }, { validator: (rule, value, callback) => { if (value !== passwordForm.newPassword) { callback(new Error('两次输入密码不一致')) } else { callback() } }, trigger: 'blur' }]
})

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isJPG) { ElMessage.error('头像图片只能是 JPG/PNG 格式!'); return false }
  if (!isLt2M) { ElMessage.error('头像图片大小不能超过 2MB!'); return false }
  return true
}

const handleUploadAvatar = async ({ file, onSuccess, onError }) => {
  try {
    uploadingAvatar.value = true
    const previewUrl = URL.createObjectURL(file)
    await uploadTempBusinessFile(file, { businessType: 'USER_AVATAR', businessField: 'avatar' }, {
      onSuccess: (res) => { tempFileId.value = res.id; tempAvatarUrl.value = previewUrl; ElMessage.success('头像预上传成功，请点击保存修改'); onSuccess(res) },
      onError: (err) => { URL.revokeObjectURL(previewUrl); ElMessage.error('头像上传失败'); onError(err) }
    })
  } catch (error) { onError(error) } finally { uploadingAvatar.value = false }
}

const updateUserInfo = async () => {
  if (!userFormRef.value) return
  try {
    await userFormRef.value.validate()
    updateLoading.value = true
    await updateUser(userForm, {
      onSuccess: async (res) => {
        Object.assign(userProfile, res); userStore.setUserInfo(res)
        if (tempFileId.value) {
          await confirmTempFile(tempFileId.value, { businessType: 'USER_AVATAR', businessId: res.id, businessField: 'avatar' }, {
            onSuccess: async (fileRes) => {
              const newAvatarPath = fileRes.filePath
              await updateUser({ avatar: newAvatarPath }, {
                onSuccess: (updateRes) => { Object.assign(userProfile, updateRes); userStore.setUserInfo(updateRes); tempAvatarUrl.value = null; tempFileId.value = null }
              })
            }
          })
        }
        ElMessage.success('个人信息更新成功')
      }
    })
  } finally { updateLoading.value = false }
}

const updatePassword = async () => {
  if (!passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true
    await updateUserPassword(passwordForm, {
      onSuccess: () => { passwordForm.oldPassword = ''; passwordForm.newPassword = ''; passwordForm.confirmPassword = ''; passwordFormRef.value.resetFields(); ElMessage.success('密码修改成功') }
    })
  } finally { passwordLoading.value = false }
}

onMounted(async () => {
  getCurrentUser(null, { onSuccess: (res) => { Object.assign(userProfile, res); Object.assign(userForm, res); userProfile.displayName = res.nickname || res.username } })
})
</script>

<style scoped>
/* 核心配色：深蓝灰 Slate 色系 */
:root {
  --primary: #1f2937;      /* 核心黑/深灰 */
  --text-main: #111827;
  --text-sub: #4b5563;
  --border-strong: #d1d5db; /* 增强边框色 */
  --border-light: #e5e7eb;
  --bg-input: #f9fafb;
}

.profile-container {
  min-height: 100vh;
  background-color: #f8fafc;
  font-family: 'Noto Sans SC', -apple-system, sans-serif;
}

/* 头部视觉增强 */
.profile-header {
  background: white;
  border-bottom: 1px solid var(--border-strong);
  padding: 3.5rem 0;
}

.profile-info { display: flex; align-items: center; justify-content: center; }
.avatar-section { display: flex; align-items: center; gap: 2rem; }

.avatar-wrapper { position: relative; }
:deep(.el-avatar) { 
  border: 2px solid var(--border-strong); 
  background-color: #f3f4f6;
  color: #9ca3af;
}

/* 痛点修复：相机按钮高亮显示 */
.upload-btn {
  position: absolute;
  bottom: 0px;
  right: 0px;
  width: 32px !important;
  height: 32px !important;
  background: #1f2937 !important; /* 强制背景为深色 */
  color: white !important;         /* 强制图标为白色 */
  border: 2px solid white !important;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  display: flex !important;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}
.upload-btn:hover {
  background: #374151 !important;
  transform: scale(1.1);
}
.upload-btn i { font-size: 14px; }

.user-name { font-size: 2rem; font-weight: 800; color: var(--text-main); margin-bottom: 0.25rem; }
.user-email, .join-date { font-size: 0.95rem; color: var(--text-sub); }

.main-content { max-width: 1200px; margin: 0 auto; padding: 2rem 1.5rem; }
.content-wrapper { display: grid; grid-template-columns: 240px 1fr; gap: 2.5rem; align-items: start; }

/* 卡片边框加固 */
.nav-card, .content-card {
  background: white;
  border-radius: 12px;
  border: 1px solid var(--border-strong); /* 加深边框 */
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04); /* 增加微投影 */
}

.nav-card { padding: 1.25rem; position: sticky; top: 2rem; }
.nav-title { font-size: 1rem; font-weight: 700; color: var(--text-main); margin-bottom: 1rem; border-left: 4px solid var(--primary); padding-left: 0.75rem;}

.nav-menu { display: flex; flex-direction: column; gap: 0.5rem; }
.tab-item {
  width: 100%; text-align: left; padding: 0.85rem 1rem; border-radius: 8px;
  font-weight: 600; color: var(--text-sub); background: transparent; border: none; cursor: pointer;
  display: flex; align-items: center; transition: all 0.2s;
}
.tab-item i { margin-right: 0.75rem; font-size: 1rem; }
.tab-item:hover { background: #f3f4f6; color: var(--primary); }
.tab-item.active { background: #1f2937; color: white; box-shadow: 0 4px 12px rgba(31, 41, 55, 0.2); }

/* 主内容卡片 */
.content-card { padding: 2.5rem; }
.section-title { font-size: 1.4rem; font-weight: 800; color: var(--text-main); margin-bottom: 2rem; display: flex; align-items: center; gap: 0.75rem; }
.section-title i { color: var(--primary); }

/* 表单输入框增强 */
:deep(.el-input__wrapper) {
  background-color: var(--bg-input) !important;
  border: 1px solid var(--border-light) !important;
  box-shadow: none !important;
  padding: 6px 12px !important;
  border-radius: 8px !important;
}
:deep(.el-input__wrapper.is-focus) {
  border-color: var(--primary) !important;
  background-color: white !important;
  box-shadow: 0 0 0 1px var(--primary) !important;
}
:deep(.el-form-item__label) { font-weight: 600; color: var(--text-main); }

/* 痛点修复：单选框选中高亮 */
:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #1f2937 !important;
  border-color: #1f2937 !important;
  width: 16px;
  height: 16px;
}
:deep(.el-radio__inner::after) {
  width: 6px;
  height: 6px;
  background-color: white !important;
}
:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #1f2937 !important;
  font-weight: 700;
}
:deep(.el-radio__inner) {
  border-color: var(--border-strong);
  width: 16px;
  height: 16px;
}

/* 按钮样式 */
.save-btn {
  background: #1f2937 !important;
  border: none !important;
  height: 44px;
  padding: 0 2rem !important;
  font-weight: 700 !important;
  border-radius: 8px !important;
}
.save-btn:hover { background: #374151 !important; transform: translateY(-1px); }

.form-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 1rem 2rem; }
.form-actions { margin-top: 2rem; padding-top: 1.5rem; border-top: 1px solid #f3f4f6; }

@media (max-width: 1024px) {
  .content-wrapper { grid-template-columns: 1fr; }
  .nav-menu { flex-direction: row; }
  .tab-item { width: auto; }
}
@media (max-width: 768px) {
  .form-grid { grid-template-columns: 1fr; }
  .avatar-section { flex-direction: column; text-align: center; }
}
</style>