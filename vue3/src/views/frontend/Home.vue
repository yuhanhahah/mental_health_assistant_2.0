<template>
  <div class="healing-home-page">
    <div class="ambient-light light-purple"></div>
    <div class="ambient-light light-green"></div>

    <section class="hero-section">
      <div class="container">
        <div class="hero-content">
          <div class="hero-text-area">
            <div class="badge-tag">
              <i class="fas fa-sparkles"></i> 你的专属数字心理愈疗空间
            </div>
            <h1 class="hero-title">
              一次温暖的对话<br>
              <span class="gradient-text">化孤独为慰藉</span>
            </h1>
            <p class="hero-desc">
              每个深夜，每个焦虑的时刻，小暖都在这里。不必独自承受，让心与心的连接温暖您的每一天。
            </p>
            <div class="action-group">
              <router-link :to="isLoggedIn ? '/consultation' : '/auth/login'" class="glass-btn primary-purple-btn">
                <i class="fas fa-comments"></i> 开始倾诉，获得陪伴
              </router-link>
              <router-link :to="isLoggedIn ? '/emotion-diary' : '/auth/login'" class="glass-btn outline-btn">
                <i class="fas fa-pen-nib"></i> 记录心情，释放情感
              </router-link>
            </div>
          </div>
          
          <div class="hero-visual-area">
            <div class="crystal-orb">
              <div class="orb-inner"></div>
              <i class="fas fa-seedling visual-seedling-icon"></i>
              <div class="orbit-dot dot-2"></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="features-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">我们的服务</h2>
          <p class="section-subtitle">用心倾听每一个故事，用爱温暖每一颗心灵</p>
        </div>
        
        <div class="features-grid">
          <div class="glass-card feature-card">
            <div class="feature-icon-box purple-glow">
              <i class="fas fa-comments"></i>
            </div>
            <h3>心灵倾诉</h3>
            <p>无论何时何地，都有一颗温暖的心在倾听。分享您的烦恼，收获专业的心理支持与无条件接纳。</p>
            <router-link :to="isLoggedIn ? '/consultation' : '/auth/login'" class="text-link purple-text">
              开启对话 <i class="fas fa-arrow-right"></i>
            </router-link>
          </div>

          <div class="glass-card feature-card">
            <div class="feature-icon-box green-glow">
              <i class="fas fa-temperature-half"></i>
            </div>
            <h3>情感记录</h3>
            <p>每一种情绪都值得被珍视。让我们陪您一起观察内心的温度，生成专属的心境雷达与时光记忆。</p>
            <router-link :to="isLoggedIn ? '/emotion-diary' : '/auth/login'" class="text-link green-text">
              记录此刻 <i class="fas fa-arrow-right"></i>
            </router-link>
          </div>

          <div class="glass-card feature-card">
            <div class="feature-icon-box blue-glow">
              <i class="fas fa-book-open"></i>
            </div>
            <h3>心灵成长</h3>
            <p>在知识的花园中漫步，发现内心的力量。精选心理学文章与自助指南，助你通往更好的自己。</p>
            <router-link to="/knowledge" class="text-link blue-text">
              探索知识 <i class="fas fa-arrow-right"></i>
            </router-link>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
// ==========================================
// 逻辑层完全不变
// ==========================================
import { computed } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => !!userStore.token)
</script>

<style scoped>
/* =========================================================================
   现代沉浸式主页 (Modern Healing Minimalist Home) - 最终视觉微调版
========================================================================= */

:root {
  --bg-page: #F7F9FC;
  --glass-bg: rgba(255, 255, 255, 0.6);
  --glass-border: rgba(255, 255, 255, 0.9);
  --primary-purple: #8B5CF6;
  --primary-green: #10B981;
  --primary-blue: #3B82F6;
  --text-main: #1E293B;
  --text-sub: #64748B;
  --shadow-soft: 0 8px 32px rgba(31, 38, 135, 0.04);
}

* { box-sizing: border-box; }

.healing-home-page {
  min-height: 100vh;
  background-color: var(--bg-page);
  color: var(--text-main);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
  position: relative;
  overflow: hidden;
}

/* 环境光：紫色与绿色交织 */
.ambient-light { position: absolute; border-radius: 50%; filter: blur(140px); opacity: 0.4; z-index: 0; pointer-events: none; animation: float 15s infinite alternate ease-in-out; }
.light-purple { width: 50vw; height: 50vw; background: #E0E7FF; top: -10%; left: -10%; }
.light-green { width: 45vw; height: 45vw; background: #D1FAE5; bottom: 0%; right: -5%; animation-delay: -5s;}
@keyframes float { 0% { transform: translate(0,0); } 100% { transform: translate(5%, 5%); } }

.container { max-width: 1200px; margin: 0 auto; padding: 0 24px; position: relative; z-index: 10; }

/* ==================== 首屏英雄区 ==================== */
.hero-section {
  padding: 100px 0 80px 0;
  min-height: 75vh;
  display: flex;
  align-items: center;
}
.hero-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
}

/* 优化 1：彻底删除边框、背景和阴影，让标签极简自然 */
.badge-tag {
  display: inline-flex; align-items: center; gap: 8px;
  padding: 0; /* 移除 padding */
  background: transparent; /* 透明背景 */
  border: none; /* 移除边框 */
  color: var(--primary-purple); font-size: 13px; font-weight: 600; margin-bottom: 24px; 
  box-shadow: none; /* 移除阴影 */
}
.hero-title { font-size: 52px; font-weight: 800; line-height: 1.2; letter-spacing: -1px; margin: 0 0 24px 0; color: var(--text-main); }
.gradient-text { background: linear-gradient(135deg, var(--primary-purple), var(--primary-green)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.hero-desc { font-size: 18px; color: var(--text-sub); line-height: 1.6; margin: 0 0 40px 0; max-width: 480px; }

/* 按钮组 */
.action-group { display: flex; gap: 16px; flex-wrap: wrap; }
.glass-btn { 
  display: inline-flex; align-items: center; gap: 10px;
  padding: 14px 28px; border-radius: 16px; font-size: 15px; font-weight: 600; cursor: pointer; transition: all 0.3s ease; text-decoration: none;
}
/* 左侧实色渐变按钮 */
.glass-btn.primary-purple-btn { 
  background: linear-gradient(135deg, #A5B4FC, var(--primary-purple)); 
  color: #000000;
  border: none; 
  box-shadow: 0 8px 24px rgba(201, 141, 231, 0.25); 
}
.glass-btn.primary-purple-btn:hover { transform: translateY(-3px); box-shadow: 0 12px 32px rgba(139,92,246,0.35); }

/* 优化 2：为右侧按钮加上统一的紫色线框效果 */
.glass-btn.outline-btn { 
  background: rgba(255,255,255,0.6); 
  border: 2px solid var(--primary-purple); /* 加上紫色线框 */
  color: var(--text-main); 
  box-shadow: 0 8px 24px rgba(201, 141, 231, 0.25); 
}
.glass-btn.outline-btn:hover { 
  background: white; 
  border-color: var(--primary-green); /* Hover时变为绿色线框 */
  color: var(--primary-green); 
  transform: translateY(-3px); 
}

/* 右侧水晶球视觉 */
.hero-visual-area { display: flex; justify-content: center; align-items: center; position: relative; }
.crystal-orb {
  width: 320px; height: 320px; border-radius: 50%;
  background: linear-gradient(135deg, rgba(255,255,255,0.4), rgba(255,255,255,0.1));
  backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.8);
  box-shadow: 0 32px 64px rgba(31,38,135,0.1), inset 0 2px 20px rgba(255,255,255,0.8);
  display: flex; justify-content: center; align-items: center; position: relative;
  animation: float-orb 6s infinite alternate ease-in-out;
}
.orb-inner {
  position: absolute; width: 100%; height: 100%; border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, rgba(52,211,153,0.3), rgba(139,92,246,0.1)); filter: blur(10px);
}

/* 优化 3：彻底消除外层白圈，绿色嫩芽图标直接悬浮在水晶球内，极具清透感 */
.visual-seedling-icon { 
  font-size: 96px; /* 适当放大图标，使其在球内更饱满 */
  color: var(--primary-green); 
  z-index: 2;
  filter: drop-shadow(0 8px 16px rgba(16,185,129,0.3)); /* 加上更自然的绿色投影 */
}

@keyframes float-orb { 0% { transform: translateY(0); } 100% { transform: translateY(-20px); } }

/* 轨道点缀 */
.orbit-dot { position: absolute; border-radius: 50%; background: white; }
.dot-1 { width: 12px; height: 12px; top: 10%; right: 20%; box-shadow: 0 0 10px rgba(255,255,255,0.8); animation: pulse-dot 2s infinite alternate;}
.dot-2 { width: 8px; height: 8px; bottom: 15%; left: 15%; background: var(--primary-purple); opacity: 0.6; animation: pulse-dot 3s infinite alternate reverse;}
@keyframes pulse-dot { 0% { transform: scale(1); } 100% { transform: scale(1.5); } }

/* ==================== 核心功能区 ==================== */
.features-section { padding: 60px 0 100px 0; position: relative; z-index: 10; }
.section-header { text-align: center; margin-bottom: 60px; }
.section-title { font-size: 32px; font-weight: 700; color: var(--text-main); margin: 0 0 12px 0; }
.section-subtitle { font-size: 16px; color: var(--text-sub); }

.features-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 32px; }

/* 玻璃卡片 */
.glass-card {
  background: var(--glass-bg); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px);
  border-radius: 24px; border: 1px solid var(--glass-border); box-shadow: var(--shadow-soft);
  padding: 40px 32px; transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}
.feature-card:hover { transform: translateY(-8px); background: rgba(255,255,255,0.8); box-shadow: 0 20px 40px rgba(31,38,135,0.06); }

.feature-icon-box {
  width: 64px; height: 64px; border-radius: 20px; background: white;
  display: flex; justify-content: center; align-items: center; font-size: 28px; margin-bottom: 24px;
}
.purple-glow { color: var(--primary-purple); box-shadow: 0 8px 24px rgba(139,92,246,0.15); }
.green-glow { color: var(--primary-green); box-shadow: 0 8px 24px rgba(16,185,129,0.15); }
.blue-glow { color: var(--primary-blue); box-shadow: 0 8px 24px rgba(59,130,246,0.15); }

.feature-card h3 { font-size: 20px; font-weight: 600; margin: 0 0 16px 0; color: var(--text-main); }
.feature-card p { font-size: 14px; color: var(--text-sub); line-height: 1.6; margin: 0 0 24px 0; }

.text-link { display: inline-flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; text-decoration: none; transition: 0.3s; }
.purple-text { color: var(--primary-purple); }
.green-text { color: var(--primary-green); }
.blue-text { color: var(--primary-blue); }
.text-link:hover { opacity: 0.7; gap: 12px; } /* Hover时箭头向右移动一点点 */

/* ==================== 响应式设计 ==================== */
@media (max-width: 992px) {
  .hero-content { grid-template-columns: 1fr; text-align: center; gap: 40px; }
  .badge-tag { margin: 0 auto 24px auto; }
  .hero-desc { margin: 0 auto 40px auto; }
  .action-group { justify-content: center; }
  .crystal-orb { width: 100px; height: 100px; }
  .features-grid { grid-template-columns: 1fr; gap: 24px; }
  .hero-title { font-size: 40px; }
}

@media (max-width: 480px) {
  .hero-section { padding: 60px 0; }
  .hero-title { font-size: 32px; }
  .glass-btn { width: 100%; justify-content: center; }
  .glass-card { padding: 32px 24px; }
}
</style>