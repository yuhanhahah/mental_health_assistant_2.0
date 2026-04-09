<template>
  <div class="premium-auth-page">
    <div class="ambient-light light-1"></div>
    <div class="ambient-light light-2"></div>

    <div class="auth-glass-card">
      <div class="back-nav">
        <router-link to="/" class="ghost-btn">
          <i class="fas fa-arrow-left"></i> 首页
        </router-link>
      </div>
      
      <div class="form-header">
        <div class="brand-icon-box"><i class="fas fa-seedling"></i></div>
        <h2>欢迎来到树洞</h2>
        <p>稍作停顿，整理思绪后再出发</p>
      </div>

      <form class="auth-form" @submit.prevent="handleLogin">
        <div class="form-item">
          <label>账号 / 邮箱</label>
          <div class="glass-input-wrapper">
            <i class="fas fa-user input-icon"></i>
            <input v-model="loginForm.username" type="text" required placeholder="请输入..." />
          </div>
        </div>

        <div class="form-item">
          <label>安全密码</label>
          <div class="glass-input-wrapper">
            <i class="fas fa-lock input-icon"></i>
            <input v-model="loginForm.password" :type="showPassword ? 'text' : 'password'" required placeholder="请输入密码..." />
            <button type="button" class="eye-btn" @click="showPassword = !showPassword">
              <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </button>
          </div>
        </div>

        <div class="form-actions">
          <label class="custom-checkbox">
            <input v-model="rememberMe" type="checkbox" />
            <span class="checkmark"></span>
            <span class="label-text">记住我的设备</span>
          </label>
          <a href="#" class="forgot-pwd">忘记密码?</a>
        </div>

        <button type="submit" :disabled="loading" class="primary-gradient-btn">
          <i v-if="loading" class="fas fa-spinner fa-spin"></i>
          {{ loading ? '正在验证...' : '进入空间' }}
        </button>

        <div class="switch-mode">
          新面孔？<router-link to="/auth/register">开启心灵之旅</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { login } from '@/api/user'

const router = useRouter(); const route = useRoute(); const userStore = useUserStore()
const loading = ref(false); const rememberMe = ref(false); const showPassword = ref(false)
const loginForm = reactive({ username: '', password: '' })

onMounted(() => {
  if (localStorage.getItem('rememberMe') === 'true') {
    rememberMe.value = true; loginForm.username = localStorage.getItem('rememberedUsername') || ''
  }
})
watch(rememberMe, (newVal) => { if (!newVal) { localStorage.removeItem('rememberMe'); localStorage.removeItem('rememberedUsername') } })

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) return ElMessage.error('请填写完整信息')
  loading.value = true
  try {
    const data = await new Promise((resolve, reject) => {
      login({ username: loginForm.username.trim(), password: loginForm.password.trim() }, {
        successMsg: "欢迎回来", showDefaultMsg: true, onSuccess: resolve, onError: reject
      })
    })
    if (rememberMe.value) { localStorage.setItem('rememberMe', 'true'); localStorage.setItem('rememberedUsername', loginForm.username.trim()) }
    userStore.setUserInfo(data)
    router.push(data.userInfo.userType === 2 ? (route.query.redirect || '/back/dashboard') : (route.query.redirect || '/'))
  } catch (error) {} finally { loading.value = false }
}
</script>

<style scoped>
.premium-auth-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #F4F7F9; position: relative; overflow: hidden; font-family: -apple-system, sans-serif; }

/* 氛围光效 */
.ambient-light { position: absolute; border-radius: 50%; filter: blur(100px); opacity: 0.6; z-index: 0; animation: float 15s infinite alternate; pointer-events: none;}
.light-1 { width: 50vw; height: 50vw; background: #E0E7FF; top: -10%; left: -10%; }
.light-2 { width: 40vw; height: 40vw; background: #FCE7F3; bottom: -5%; right: -5%; animation-delay: -5s; }
@keyframes float { 0% { transform: translate(0, 0); } 100% { transform: translate(5%, 5%); } }

/* 毛玻璃卡片 */
.auth-glass-card { width: 100%; max-width: 420px; background: rgba(255, 255, 255, 0.7); backdrop-filter: blur(24px); -webkit-backdrop-filter: blur(24px); border: 1px solid rgba(255, 255, 255, 0.9); border-radius: 24px; padding: 40px; box-shadow: 0 10px 40px rgba(0, 0, 0, 0.04); position: relative; z-index: 10; }

.back-nav { margin-bottom: 24px; }
.ghost-btn { background: none; border: none; color: #64748B; font-size: 14px; text-decoration: none; display: inline-flex; align-items: center; gap: 6px; transition: 0.3s; }
.ghost-btn:hover { color: #8B5CF6; transform: translateX(-4px); }

.form-header { text-align: center; margin-bottom: 32px; }
.brand-icon-box { width: 56px; height: 56px; background: #EEF2FF; border-radius: 18px; color: #8B5CF6; font-size: 24px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; box-shadow: 0 4px 12px rgba(139, 92, 246, 0.15); }
.form-header h2 { margin: 0 0 8px; font-size: 24px; font-weight: 700; color: #1E293B; }
.form-header p { margin: 0; font-size: 14px; color: #64748B; }

.auth-form { display: flex; flex-direction: column; gap: 20px; }
.form-item { display: flex; flex-direction: column; gap: 8px; }
.form-item label { font-size: 13px; font-weight: 600; color: #334155; margin-left: 4px; }

/* 拟物态输入框 */
.glass-input-wrapper { display: flex; align-items: center; background: rgba(255, 255, 255, 0.6); border: 1px solid #E2E8F0; border-radius: 14px; overflow: hidden; transition: 0.3s; box-shadow: inset 0 2px 4px rgba(0,0,0,0.01); }
.glass-input-wrapper:focus-within { background: #FFF; border-color: #8B5CF6; box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1); }
.input-icon { padding: 0 16px; color: #94A3B8; }
.glass-input-wrapper input { flex: 1; padding: 14px 16px 14px 0; border: none; background: transparent; outline: none; font-size: 15px; color: #1E293B; }
.glass-input-wrapper input::placeholder { color: #94A3B8; }
.eye-btn { background: none; border: none; padding: 0 16px; color: #94A3B8; cursor: pointer; transition: 0.2s; }
.eye-btn:hover { color: #8B5CF6; }

/* 底部操作 */
.form-actions { display: flex; justify-content: space-between; align-items: center; font-size: 13px; }
.forgot-pwd { color: #64748B; text-decoration: none; transition: 0.2s; }
.forgot-pwd:hover { color: #8B5CF6; }

/* 自定义 Checkbox */
.custom-checkbox { display: flex; align-items: center; cursor: pointer; position: relative; padding-left: 24px; }
.custom-checkbox input { position: absolute; opacity: 0; cursor: pointer; }
.checkmark { position: absolute; left: 0; height: 16px; width: 16px; background-color: #F1F5F9; border-radius: 4px; border: 1px solid #CBD5E1; transition: 0.2s; }
.custom-checkbox:hover input ~ .checkmark { background-color: #E2E8F0; }
.custom-checkbox input:checked ~ .checkmark { background-color: #8B5CF6; border-color: #8B5CF6; }
.checkmark:after { content: ""; position: absolute; display: none; left: 5px; top: 2px; width: 4px; height: 8px; border: solid white; border-width: 0 2px 2px 0; transform: rotate(45deg); }
.custom-checkbox input:checked ~ .checkmark:after { display: block; }
.label-text { color: #64748B; }

/* 炫彩按钮 */
.primary-gradient-btn { margin-top: 8px; padding: 14px; background: linear-gradient(135deg, #A5B4FC, #8B5CF6); color: white; border: none; border-radius: 14px; font-size: 15px; font-weight: 600; cursor: pointer; transition: 0.3s; box-shadow: 0 4px 12px rgba(139, 92, 246, 0.25); display: flex; justify-content: center; gap: 8px; }
.primary-gradient-btn:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 16px rgba(139, 92, 246, 0.4); }
.primary-gradient-btn:disabled { opacity: 0.7; cursor: not-allowed; }

.switch-mode { text-align: center; margin-top: 16px; font-size: 13px; color: #64748B; }
.switch-mode a { color: #8B5CF6; font-weight: 600; text-decoration: none; margin-left: 4px; }
.switch-mode a:hover { text-decoration: underline; }
</style>