<template>
  <div class="premium-sidebar" :class="{ 'is-collapsed': isCollapsed }">
    <div class="brand-section">
      <div class="brand-logo">
        <i class="fas fa-leaf brand-icon"></i>  
        <div class="brand-info" v-show="!isCollapsed">
          <h1 class="brand-title">心理愈疗 Admin</h1>
        </div>
      </div>
    </div>

    <div class="admin-card" v-show="!isCollapsed">
      <el-avatar :size="44" :src="adminAvatar" class="avatar-shadow">
        {{ adminInfo?.name?.charAt(0)?.toUpperCase() || 'A' }}
      </el-avatar>
      <div class="admin-info">
        <h3 class="admin-name">{{ adminInfo?.name || '主理人' }}</h3>
        <p class="admin-role">最高权限节点</p>
      </div>
    </div>

    <div class="menu-scroll-area">
      <nav class="premium-nav">
        <router-link to="/back/dashboard" class="nav-pill" :class="{ active: activeMenu === '/back/dashboard' }">
          <i class="fas fa-chart-line nav-icon"></i><span class="nav-text" v-show="!isCollapsed">数据星盘</span>
        </router-link>
        <router-link to="/back/user" class="nav-pill" :class="{ active: activeMenu === '/back/user' }">
          <i class="fas fa-users nav-icon"></i><span class="nav-text" v-show="!isCollapsed">用户管理</span>
        </router-link>
        <router-link to="/back/knowledge" class="nav-pill" :class="{ active: activeMenu === '/back/knowledge' }">
          <i class="fas fa-book-open nav-icon"></i><span class="nav-text" v-show="!isCollapsed">愈疗文库</span>
        </router-link>
        <router-link to="/back/knowledge-category" class="nav-pill" :class="{ active: activeMenu === '/back/knowledge-category' }">
          <i class="fas fa-layer-group nav-icon"></i><span class="nav-text" v-show="!isCollapsed">知识类目</span>
        </router-link>
        <router-link to="/back/consultations" class="nav-pill" :class="{ active: activeMenu === '/back/consultations' }">
          <i class="fas fa-comment-dots nav-icon"></i><span class="nav-text" v-show="!isCollapsed">树洞记录</span>
        </router-link>
     <router-link to="/back/emotion-diary" class="nav-pill" :class="{ active: activeMenu === '/back/emotion-diary' }">
          <i class="fas fa-heart nav-icon"></i><span class="nav-text" v-show="!isCollapsed">情绪日记</span>
        </router-link>
        <router-link to="/back/ai-analysis-queue" class="nav-pill" :class="{ active: activeMenu === '/back/ai-analysis-queue' }">
          <i class="fas fa-microchip nav-icon"></i><span class="nav-text" v-show="!isCollapsed">AI 队列监测</span>
        </router-link>
      </nav>
    </div>

    <div class="logout-wrapper">
      <button @click="handleLogout" class="logout-btn">
        <i class="fas fa-sign-out-alt"></i><span v-show="!isCollapsed">安全退出</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const isCollapsed = computed(() => appStore.sidebarCollapsed)
const activeMenu = computed(() => route.meta.activeMenu || route.path)
const adminInfo = computed(() => userStore.userInfo)
const adminAvatar = computed(() => adminInfo.value?.avatar || '')

const handleLogout = () => {
  ElMessageBox.confirm('准备离开管理控制台了吗?', '提示', { confirmButtonText: '离开', cancelButtonText: '留下', type: 'warning' })
    .then(async () => { await userStore.logout(); router.push('/auth/login') })
    .catch(() => {})
}
</script>

<style scoped>
.premium-sidebar {
  height: 100vh; width: 280px; background: #FFFFFF; display: flex; flex-direction: column;
  border-right: 1px solid #F1F5F9; transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.02); z-index: 100;
}
.premium-sidebar.is-collapsed { width: 80px; }

/* 头部品牌 */
.brand-section { padding: 24px; }
.brand-logo { display: flex; align-items: center; justify-content: center; gap: 12px; }
.brand-icon { font-size: 28px; color: #8B5CF6; }
.brand-title { font-size: 18px; font-weight: 700; color: #1E293B; margin: 0; white-space: nowrap; letter-spacing: 0.5px;}

/* 个人名片 */
.admin-card { margin: 0 24px 24px; padding: 16px; background: #F8FAFC; border-radius: 16px; display: flex; align-items: center; gap: 12px; border: 1px solid #F1F5F9; }
.avatar-shadow { box-shadow: 0 4px 10px rgba(139, 92, 246, 0.2); }
.admin-info { flex: 1; overflow: hidden; }
.admin-name { font-size: 14px; font-weight: 600; color: #0F172A; margin: 0 0 4px; }
.admin-role { font-size: 12px; color: #64748B; margin: 0; }

/* 菜单列表 */
.menu-scroll-area { flex: 1; overflow-y: auto; padding: 0 16px; }
.menu-scroll-area::-webkit-scrollbar { width: 0; }
.premium-nav { display: flex; flex-direction: column; gap: 8px; }

.nav-pill {
  display: flex; align-items: center; padding: 14px 16px; border-radius: 14px;
  color: #64748B; text-decoration: none; font-size: 14px; font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); position: relative;
}
.nav-pill:hover { background: #F8FAFC; color: #1E293B; transform: translateX(4px); }
.nav-icon { width: 24px; font-size: 16px; text-align: center; margin-right: 12px; transition: 0.3s; }
.nav-text { white-space: nowrap; }

/* 选中态特效：紫色胶囊 */
.nav-pill.active { background: #F5F3FF; color: #8B5CF6; font-weight: 600; }
.nav-pill.active .nav-icon { color: #8B5CF6; transform: scale(1.1); }
.nav-pill.active::before {
  content: ''; position: absolute; left: -16px; top: 50%; transform: translateY(-50%);
  height: 24px; width: 4px; background: #8B5CF6; border-radius: 0 4px 4px 0;
}

/* 退出区域 */
.logout-wrapper { padding: 24px; border-top: 1px solid #F1F5F9; }
.logout-btn {
  width: 100%; display: flex; justify-content: center; align-items: center; gap: 8px;
  padding: 12px; background: #FFF1F2; color: #E11D48; border: none; border-radius: 12px;
  font-size: 14px; font-weight: 600; cursor: pointer; transition: 0.3s;
}
.logout-btn:hover { background: #FFE4E6; transform: translateY(-2px); box-shadow: 0 4px 12px rgba(225, 29, 72, 0.1); }

/* 折叠态兼容 */
.is-collapsed .brand-section { padding: 24px 0; }
.is-collapsed .nav-pill { padding: 14px; justify-content: center; }
.is-collapsed .nav-icon { margin-right: 0; }
.is-collapsed .nav-pill.active::before { left: 0; }
.is-collapsed .logout-wrapper { padding: 16px 8px; }
.is-collapsed .logout-btn { padding: 12px 0; }
</style>