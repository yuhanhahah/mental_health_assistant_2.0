<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="back-nav">
        <router-link to="/" class="back-link">
          <i class="fas fa-arrow-left"></i> 返回首页
        </router-link>
      </div>
      
      <div class="form-header">
        <div class="logo-box">
          <i class="fas fa-user-plus"></i>
        </div>
        <h2 class="form-title">创建您的账户</h2>
        <p class="form-subtitle">填写以下信息，开启您的专属空间</p>
      </div>

      <form class="auth-form" @submit.prevent="handleRegister">
        <div class="form-field">
          <label for="username" class="field-label">用户名</label>
          <div class="input-wrapper">
            <div class="input-prefix"><i class="fas fa-user"></i></div>
            <input 
              id="username" 
              v-model="registerForm.username"
              name="username" 
              type="text" 
              required 
              class="clean-input"
              placeholder="请输入您的用户名"
            >
          </div>
        </div>

        <div class="form-field">
          <label for="email" class="field-label">邮箱</label>
          <div class="input-wrapper">
            <div class="input-prefix"><i class="fas fa-envelope"></i></div>
            <input 
              id="email" 
              v-model="registerForm.email"
              name="email" 
              type="email" 
              required 
              class="clean-input"
              placeholder="请输入有效的邮箱地址"
            >
          </div>
        </div>

        <div class="form-grid">
          <div class="form-field">
            <label for="nickname" class="field-label">昵称 (可选)</label>
            <div class="input-wrapper">
              <div class="input-prefix"><i class="fas fa-tag"></i></div>
              <input 
                id="nickname" 
                v-model="registerForm.nickname"
                name="nickname" 
                type="text" 
                class="clean-input"
                placeholder="起个好听的名字"
              >
            </div>
          </div>
          <div class="form-field">
            <label for="phone" class="field-label">手机号 (可选)</label>
            <div class="input-wrapper">
              <div class="input-prefix"><i class="fas fa-mobile-alt"></i></div>
              <input 
                id="phone" 
                v-model="registerForm.phone"
                name="phone" 
                type="tel" 
                class="clean-input"
                placeholder="您的手机号"
              >
            </div>
          </div>
        </div>

        <div class="form-field">
          <label for="password" class="field-label">密码</label>
          <div class="input-wrapper">
            <div class="input-prefix"><i class="fas fa-lock"></i></div>
            <input 
              id="password" 
              v-model="registerForm.password"
              name="password" 
              :type="showPassword ? 'text' : 'password'"
              required 
              class="clean-input"
              placeholder="请设置您的密码 (至少6位)"
            >
            <button 
              type="button" 
              class="password-toggle"
              @click="togglePasswordVisibility"
            >
              <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </button>
          </div>
        </div>

        <div class="form-field">
          <label for="confirmPassword" class="field-label">确认密码</label>
          <div class="input-wrapper">
            <div class="input-prefix"><i class="fas fa-shield-alt"></i></div>
            <input 
              id="confirmPassword" 
              v-model="registerForm.confirmPassword"
              name="confirmPassword" 
              :type="showConfirmPassword ? 'text' : 'password'"
              required 
              class="clean-input"
              placeholder="请再次输入您的密码"
            >
            <button 
              type="button" 
              class="password-toggle"
              @click="toggleConfirmPasswordVisibility"
            >
              <i :class="showConfirmPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </button>
          </div>
        </div>

        <div class="agreement-section">
          <label class="remember-label">
            <input 
              id="agree-terms" 
              v-model="agreeTerms"
              type="checkbox" 
              class="clean-checkbox"
            >
            <span>我已阅读并同意 <a href="#" class="switch-link">用户协议</a> 和 <a href="#" class="switch-link">隐私政策</a></span>
          </label>
        </div>

        <button 
          type="submit" 
          :disabled="loading || !agreeTerms"
          class="submit-btn"
        >
          <i v-if="loading" class="fas fa-spinner fa-spin btn-icon"></i>
          <i v-else class="fas fa-arrow-right btn-icon"></i>
          {{ loading ? '注册中...' : '立即注册' }}
        </button>

        <div class="auth-switch">
          已有账户？
          <router-link to="/auth/login" class="switch-link">立即登录</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()
const loading = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const agreeTerms = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  nickname: '', 
  gender: 0, 
  userType: 1 
})

const togglePasswordVisibility = () => { showPassword.value = !showPassword.value }
const toggleConfirmPasswordVisibility = () => { showConfirmPassword.value = !showConfirmPassword.value }

const validateForm = () => {
  if (!registerForm.username) { ElMessage.error('请输入用户名'); return false }
  if (registerForm.username.length < 3 || registerForm.username.length > 50) { ElMessage.error('用户名长度必须在3到50个字符之间'); return false }
  if (!registerForm.email) { ElMessage.error('请输入邮箱'); return false }
  const emailRegex = /^[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/
  if (!emailRegex.test(registerForm.email)) { ElMessage.error('邮箱格式不正确'); return false }
  if (registerForm.phone && !/^1[3-9]\d{9}$/.test(registerForm.phone)) { ElMessage.error('手机号格式不正确'); return false }
  if (!registerForm.password) { ElMessage.error('请输入密码'); return false }
  if (registerForm.password.length < 6 || registerForm.password.length > 100) { ElMessage.error('密码长度必须在6到100个字符之间'); return false }
  if (!registerForm.confirmPassword) { ElMessage.error('请确认密码'); return false }
  
  const password = registerForm.password.trim()
  const confirmPassword = registerForm.confirmPassword.trim()
  if (password !== confirmPassword) { ElMessage.error('两次输入密码不一致'); return false }
  if (!agreeTerms.value) { ElMessage.error('请同意用户协议和隐私政策'); return false }
  return true
}

const handleRegister = async () => {
  if (!validateForm()) return
  loading.value = true
  try {
    const cleanedForm = {
      ...registerForm,
      password: registerForm.password.trim(),
      confirmPassword: registerForm.confirmPassword.trim()
    }
    const registerData = {
      username: cleanedForm.username,
      email: cleanedForm.email,
      password: cleanedForm.password,
      confirmPassword: cleanedForm.confirmPassword,
      nickname: cleanedForm.nickname,
      phone: cleanedForm.phone,
      gender: cleanedForm.gender,
      userType: cleanedForm.userType
    }
    
    await register(registerData, {
      successMsg: "注册成功",
      showDefaultMsg: true,
      onSuccess: () => {
        ElMessage.success('注册成功，请登录')
        router.push('/auth/login')
      },
      onError: (error) => { console.error('注册失败:', error) }
    })
  } catch (error) {
    console.error('注册异常:', error)
    ElMessage.error('注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 极简黑白灰风格变量 - 完全与 Login 统一 */
:root {
  --primary: #1f2937;       
  --bg-page: #f3f4f6;       
  --card-bg: #ffffff;       
  --border-color: #e5e7eb;  
  --text-main: #111827;     
  --text-sub: #6b7280;      
  --text-placeholder: #9ca3af;
}

/* 页面容器 */
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-page);
  padding: 2rem 1rem;
  font-family: 'Noto Sans SC', -apple-system, sans-serif;
}

/* 居中卡片：稍微比登录框宽一点以容纳双列 */
.auth-card {
  width: 100%;
  max-width: 520px;
  background: var(--card-bg);
  border-radius: 20px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 10px 15px -3px rgba(0, 0, 0, 0.05);
  border: 1px solid var(--border-color);
  padding: 2.5rem;
}

.back-nav { margin-bottom: 2rem; }
.back-link {
  display: inline-flex; align-items: center; gap: 0.5rem;
  font-size: 0.875rem; font-weight: 500; color: var(--text-sub);
  text-decoration: none; transition: 0.2s;
}
.back-link:hover { color: var(--primary); }

.form-header { text-align: center; margin-bottom: 2.5rem; }
.logo-box {
  width: 48px; height: 48px; margin: 0 auto 1rem;
  background: var(--bg-page); border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.5rem; color: var(--primary); border: 1px solid var(--border-color);
}
.form-title { font-size: 1.5rem; font-weight: 700; color: var(--text-main); margin: 0 0 0.5rem; }
.form-subtitle { font-size: 0.875rem; color: var(--text-sub); margin: 0; }

.auth-form { display: flex; flex-direction: column; gap: 1.25rem; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.form-field { display: flex; flex-direction: column; gap: 0.5rem; }
.field-label { font-size: 0.875rem; font-weight: 600; color: var(--text-main); }

/* 输入框组合 */
.input-wrapper {
  position: relative; display: flex; align-items: center;
  background: #f9fafb; border: 1px solid var(--border-color);
  border-radius: 10px; transition: all 0.2s; overflow: hidden;
}
.input-wrapper:focus-within {
  background: #ffffff; border-color: var(--primary);
  box-shadow: 0 0 0 1px var(--primary);
}
.input-prefix {
  padding-left: 1rem; color: var(--text-sub); font-size: 1rem;
  display: flex; align-items: center; justify-content: center;
}
.clean-input {
  flex: 1; width: 100%; border: none; background: transparent;
  padding: 0.875rem 1rem 0.875rem 0.75rem; font-size: 0.95rem; color: var(--text-main); outline: none;
}
.clean-input::placeholder { color: var(--text-placeholder); }
.password-toggle {
  background: none; border: none; padding: 0 1rem; cursor: pointer;
  color: var(--text-sub); font-size: 1rem; transition: 0.2s;
  display: flex; align-items: center; justify-content: center;
}
.password-toggle:hover { color: var(--primary); }

/* 协议 */
.agreement-section { margin-top: 0.5rem; font-size: 0.875rem; }
.remember-label { display: flex; align-items: flex-start; gap: 0.5rem; cursor: pointer; color: var(--text-sub); line-height: 1.4;}
.clean-checkbox {
  width: 1rem; height: 1rem; accent-color: var(--primary);
  border: 1px solid var(--border-color); border-radius: 4px; cursor: pointer; margin-top: 0.1rem; flex-shrink: 0;
}

/* 按钮与链接 */
.submit-btn {
  width: 100%; padding: 0.875rem; background: var(--primary);
  color: white; border: none; border-radius: 10px; font-size: 1rem;
  font-weight: 600; cursor: pointer; display: flex; justify-content: center;
  align-items: center; gap: 0.5rem; transition: all 0.2s; margin-top: 0.5rem;
}
.submit-btn:hover:not(:disabled) { background: #374151; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(31, 41, 55, 0.15); }
.submit-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-icon { font-size: 1rem; }

.auth-switch { text-align: center; font-size: 0.875rem; color: var(--text-sub); margin-top: 0.5rem; }
.switch-link { color: var(--primary); font-weight: 600; text-decoration: none; margin: 0 0.25rem; transition: 0.2s;}
.switch-link:hover { text-decoration: underline; }

@media (max-width: 640px) {
  .form-grid { grid-template-columns: 1fr; gap: 1.25rem;}
  .auth-card { padding: 1.5rem; }
}
</style>