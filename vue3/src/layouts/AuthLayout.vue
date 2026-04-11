<template>
  <div class="auth-layout">
    <div class="ambient-light light-1"></div>
    <div class="ambient-light light-2"></div>

    <div class="auth-main-wrapper">
      <router-view />
    </div>
  </div>
</template>

<style scoped>
/* 确保父容器铺满整个可视区域，并且隐藏任何溢出 */
.auth-layout {
  position: relative;
  width: 100vw;
  height: 100vh;
  min-height: 100vh;
  background-color: #F4F7F9; /* 基础底色 */
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden; /* 防止氛围灯超出屏幕产生滚动条 */
}

/* 氛围光效（从 Login.vue 移出，成为全局背景） */
.ambient-light { 
  position: absolute; 
  border-radius: 50%; 
  filter: blur(100px); 
  opacity: 0.6; 
  z-index: 0; 
  animation: float 15s infinite alternate; 
  pointer-events: none;
}
.light-1 { 
  width: 50vw; 
  height: 50vw; 
  background: #E0E7FF; 
  top: -10%; 
  left: -10%; 
}
.light-2 { 
  width: 40vw; 
  height: 40vw; 
  background: #FCE7F3; 
  bottom: -5%; 
  right: -5%; 
  animation-delay: -5s; 
}
@keyframes float { 
  0% { transform: translate(0, 0); } 
  100% { transform: translate(5%, 5%); } 
}

/* 主内容容器：确保内容在 z-index 上层 */
.auth-main-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  box-sizing: border-box;
}

/* 全局链接过渡效果 */
a {
  transition: all 0.3s ease;
}
</style>