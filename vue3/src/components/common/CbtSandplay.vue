<template>
  <div class="cbt-sandplay-container glass-card">
    <div class="sandplay-header">
      <h3 class="card-title">
        <i class="fas fa-capsules title-icon"></i> 电子布洛芬
      </h3>
<p class="section-desc">提取多维心理学理论，为您配制专属“心灵解药”。请放心倾诉，处方将自动同步至您的 <strong style="color: #3b82f6">[AI 咨询] - [记忆碎片]</strong> 档案库，方便随时翻阅回顾。</p>
    </div>

    <div class="sandplay-stage">
      <div v-if="stage === 'input'" class="stage-input fade-in">
        <textarea
          v-model="negativeThought"
          class="clean-textarea cbt-textarea custom-scrollbar"
          rows="6"
          placeholder="你今天过得好吗？遇到了什么过不去的事情？别怕，哪怕写下几千字的碎碎念，我也会在这里稳稳接住你。请尽情倒出你的情绪垃圾..."
          maxlength="5000"
        ></textarea>
        <div class="word-count">{{ negativeThought.length }} / 5000</div>
        
        <button class="flat-btn primary-mode cbt-btn" :disabled="!negativeThought" @click="transformToStone">
          <i class="fas fa-prescription-bottle-alt"></i> 封装情绪胶囊
        </button>
      </div>

      <div v-else-if="stage === 'stone' || stage === 'shattering'" class="stage-stone fade-in">
        <div class="stone-object ibuprofen-capsule" :class="{ 'is-shattering': stage === 'shattering' }">
          <div class="capsule-inner-split"></div>
          <div class="capsule-inner-glow"></div>
          <p class="stone-text">" {{ negativeThought.length > 20 ? negativeThought.substring(0, 20) + '...' : negativeThought }} "</p>
        </div>
        <div class="action-area" v-if="stage === 'stone'">
          <button class="flat-btn primary-mode shatter-btn" @click="triggerReframe">
            <i class="fas fa-bolt"></i> 服下胶囊，生成深度疗愈处方
          </button>
          <button class="flat-btn ghost-mode" @click="resetSandplay">撤销</button>
        </div>
        <div class="loading-text" v-if="stage === 'shattering'">
          <i class="fas fa-spinner fa-spin"></i> 药效发作中，正在提取心理学模型，为你调配专属深度处方...
        </div>
      </div>

      <div v-else-if="stage === 'orb'" class="stage-orb fade-in">
        <div class="orb-object custom-scrollbar">
          
          <div class="prescription-nav" v-if="reframedThought.length > 100">
            <div class="nav-chip" @click="scrollToSection('情绪抱持')">🫂 情绪抱持</div>
            <div class="nav-chip" @click="scrollToSection('叙事解构')">🪞 叙事解构</div>
            <div class="nav-chip" @click="scrollToSection('心理科普')">💡 心理科普</div>
            <div class="nav-chip" @click="scrollToSection('重塑视野')">🦋 重塑视野</div>
            <div class="nav-chip" @click="scrollToSection('精神处方')">💊 精神处方</div>
            <div class="nav-chip" @click="scrollToSection('行动指南')">🏃‍♂️ 行动指南</div>
          </div>

          <div class="orb-content">
            <div class="orb-text formatted-text" v-html="parsedReframedThought"></div>
            <span class="cursor-blink" v-if="isTyping">|</span>
          </div>
        </div>
        
        <div class="action-area">
              <button class="flat-btn ghost-mode" @click="resetSandplay">呼出这口气，完成本次疗愈</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import request from '@/utils/request'
import { useUserStore } from '@/store/user'

const stage = ref('input')
const negativeThought = ref('')
const reframedThought = ref('')
const currentSessionId = ref(null)
const isTyping = ref(false)

// 深度 Markdown 解析器：渲染加粗、并自动为标题注入锚点ID
const parsedReframedThought = computed(() => {
  let text = reframedThought.value || '';
  if (!text) return '';
  
  // 1. 将指定标题转换为带有 ID 的 h4 标签，用于锚点跳转
  text = text.replace(/(🫂|🪞|💡|🦋|💊|🏃‍♂️)\s*【(.*?)】/g, '<h4 id="nav-$2" class="prescription-header">$1 【$2】</h4>');
  
  // 2. 取消了幻觉链接的解析，直接渲染纯文本
  
  // 3. 解析 Markdown 加粗
  text = text.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
  
  // 4. 将普通的换行符转为 br 标签
  text = text.replace(/\n/g, '<br/>');
  
  return text;
})

// 平滑滚动到指定小节 (加入模糊匹配，防止大模型擅自改标题导致找不到锚点)
const scrollToSection = (sectionName) => {
  // 尝试精确匹配或包含匹配
  const el = document.getElementById(`nav-${sectionName}`) || 
             document.querySelector(`[id*="${sectionName}"]`);
             
  const container = document.querySelector('.orb-object');
  if (el && container) {
    container.scrollTo({ 
      top: el.offsetTop - container.offsetTop - 20, 
      behavior: 'smooth' 
    });
  }
}

const transformToStone = () => {
  if (!negativeThought.value.trim()) return
  stage.value = 'stone'
}

const triggerReframe = async () => {
  stage.value = 'shattering'
  reframedThought.value = '' 
  isTyping.value = true
  
  try {
    const userStore = useUserStore()
    const token = userStore.token || localStorage.getItem('token') || ''
    
    if (!token) throw new Error("未获取到认证Token");

 const sessionRes = await request.post('/psychological-chat/session/start', {
      sessionTitle: `💊 电子布洛芬处方 | ${new Date().toLocaleDateString()}`,
      emotionState: '焦虑' 
    });
    
    currentSessionId.value = sessionRes?.sessionId || sessionRes?.data?.sessionId || sessionRes;
    if (!currentSessionId.value) throw new Error("未能获取到临时会话ID");

    // 【终极理论支撑版 Prompt】：修复了幻觉链接问题
    const aiPrompt = `[系统指令]：你现在是具备顶尖医学素养和极高共情能力的心理咨询专家，代号“电子布洛芬”。请基于CBT理论、温尼科特的【抱持性环境】以及【叙事疗法】对外化问题的理念，深度重构用户的消极日记。

【绝对强制指令：动态篇幅匹配】：如果用户输入了几百甚至几千字，说明TA极其渴望被接住。你必须逐句拆解TA的心事，给出至少是输入量 1.5 倍的极深度长篇回复。即使输入少，也要给出极其充实的内容。

【格式强制指令】：严格按照以下6个模块输出，每个模块以 Emoji+【标题】 开头，不要客套开场白。对核心概念必须使用 **加粗**。

🫂 【情绪抱持】：用几百字深度共情、无条件接纳TA此刻所有的痛苦与不堪，创造“抱持性环境”。

🪞 【叙事解构】：将TA与问题“外化”剥离。结合CBT指出日记中隐蔽的**认知扭曲**，详细剖析这种心理机制是如何运作的。

💡 【心理科普】：用温暖的人话，为TA科普一个与目前状态匹配的心理学效应/知识（如反刍思维、存在性焦虑等）。

🦋 【重塑视野】：顺着科普，重新书写TA的故事视角，赋予这段经历成长的意义。

💊 【精神处方】：精准推荐：1位对口心理疗愈博主、1本经典书籍、1部电影、1首歌曲或播客。必须写明深度的推荐理由，指出这怎么帮到TA。绝对不要编造或提供任何URL链接，只给名字即可。

🏃‍♂️ 【行动指南】：安排具体的【运动建议】、【饮食建议】（如促血清素分泌的食物概念）、以及一个1分钟极简正念物理动作。

用户的日记原声（绝对保密，请深情回复）：${negativeThought.value}`;

    stage.value = 'orb';

    const baseURL = import.meta.env.VITE_APP_BASE_API || '/api'; 
    const response = await fetch(`${baseURL}/psychological-chat/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'token': token 
      },
      body: JSON.stringify({
        sessionId: currentSessionId.value,
        userMessage: aiPrompt
      })
    });

    if (!response.ok) throw new Error("流式接口请求失败");

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) {
        isTyping.value = false;
        break;
      }

      if (value) {
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop(); 
        
        for (let line of lines) {
          if (line.startsWith('data:')) {
            const dataStr = line.replace('data:', '').trim();
            if (dataStr && dataStr !== '{}') { 
              try {
                const dataObj = JSON.parse(dataStr);
                let contentText = null;
                if (dataObj.data?.content) {
                  contentText = dataObj.data.content;
                } else if (dataObj.data?.data?.content) {
                  contentText = dataObj.data.data.content;
                } else if (typeof dataObj.data === 'string') {
                  contentText = dataObj.data;
                }

                if (contentText) {
                  reframedThought.value += contentText;
                }
              } catch (e) {}
            }
          }
        }
      }
    }

  } catch (error) {
    console.error("沙盘干预失败:", error);
    isTyping.value = false;
    reframedThought.value = "🫂 【情绪抱持】\n网络暂时走丢了，此刻的焦躁不安我感受到了。我在这里稳稳接住你。\n\n🪞 【叙事解构】\n系统暂时的断连只是一个技术插曲，并不代表你无法获得内心的平静。\n\n💊 【精神处方】\n你可以去听听窗外的声音，或者看看《当幸福来敲门》。\n\n🏃‍♂️ 【行动指南】\n去喝杯温水，伸个懒腰吧。";
    stage.value = 'orb';
} finally {
    // 存档版：不再发送 delete 请求，仅重置当前 ID
    currentSessionId.value = null;
  }
}

const resetSandplay = () => {
  negativeThought.value = ''
  reframedThought.value = ''
  stage.value = 'input'
  currentSessionId.value = null
  isTyping.value = false
}
</script>

<style scoped>
.title-icon { color: #3b82f6; }

.cbt-sandplay-container {
  margin-bottom: 24px;
  background: linear-gradient(145deg, rgba(255,255,255,0.9), rgba(240, 247, 255, 0.8));
  border: 1px solid rgba(255, 255, 255, 0.9);
  position: relative;
  overflow: hidden;
  padding: 30px;
  border-radius: 20px;
}
.sandplay-header {
  margin-bottom: 16px;
}
.stage-input {
  width: 100%;
  max-width: 700px; /* 和输出卡片宽度完全一致 */
  display: flex;
  flex-direction: column;
  align-items: center;
}
.cbt-textarea {
  width: 100%;
  max-width: 700px; /* 统一宽度 */
  box-sizing: border-box;
  font-size: 16px;
  padding: 24px;
  /* 奶油毛玻璃质感 */
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(59, 130, 246, 0.15); 
  border-radius: 24px;
  resize: none;
  outline: none;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  line-height: 1.8;
  color: #334155;
  box-shadow: inset 0 2px 10px rgba(0, 0, 0, 0.02), 0 8px 32px rgba(31, 38, 135, 0.04);
}
.cbt-textarea:focus {
  background: rgba(255, 255, 255, 0.8);
  border-color: #3b82f6;
  box-shadow: 0 10px 40px rgba(59, 130, 246, 0.08);
}
.word-count {
  width: 100%;
  text-align: right;
  font-size: 12px;
  color: #94a3b8;
  margin-top: 8px;
  padding-right: 10px;
}
.cbt-btn {
  margin-top: 24px;
  width: auto;      /* 宽度自适应内容 */
  min-width: 220px; /* 给个最小宽度保持体面 */
  padding: 14px 40px;
  border-radius: 20px;
  background: #783e84;
  color: white;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(188, 196, 210, 0.2);
  transition: all 0.3s;
}
.cbt-btn:hover:not(:disabled) {
  background: #4c2d7b;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(235, 236, 239, 0.3);
}
.cbt-btn:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
}
.sandplay-stage {
  min-height: 280px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.ibuprofen-capsule {
  width: 280px; 
  height: 90px; 
  border-radius: 45px; 
  background: linear-gradient(to right, rgba(255,255,255,0.95) 0%, rgba(255,255,255,0.9) 49%, rgba(96, 165, 250, 0.85) 51%, rgba(59, 130, 246, 0.75) 100%);
  border: 1px solid rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 24px;
  position: relative;
  transition: all 0.5s ease;
  animation: float-pill 5s ease-in-out infinite; 
  overflow: hidden;
  box-shadow: inset -4px -4px 10px rgba(0,0,0,0.02), 0 10px 25px rgba(59, 130, 246, 0.15); 
}

.capsule-inner-split {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 2px;
  height: 100%;
  background: rgba(255,255,255,0.9);
  box-shadow: 0 0 10px rgba(255,255,255,1);
  z-index: 1;
}

.capsule-inner-glow {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 100px;
  height: 100px;
  background: radial-gradient(circle, rgba(255,255,255,0.9) 0%, transparent 70%);
  border-radius: 50%;
  z-index: 2;
  opacity: 0.9;
}

.stone-text {
  color: #334155; 
  font-size: 15px;
  font-weight: 500;
  text-align: center;
  line-height: 1.4;
  z-index: 3;
  letter-spacing: 0.5px;
  word-break: break-all;
  max-width: 90%; 
  font-style: italic;
  text-shadow: 0 1px 1px rgba(255,255,255,0.8);
}

.is-shattering {
  animation: shake-shatter-capsule 2.2s forwards cubic-bezier(.36,.07,.19,.97);
}

.orb-object {
  width: 100%;
  max-width: 650px; 
  box-sizing: border-box; 
  max-height: 500px; 
  overflow-y: auto;  
  overflow-x: hidden; 
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  position: relative;
  box-shadow: 0 10px 40px rgba(59, 130, 246, 0.08), inset 0 0 15px rgba(255,255,255,1);
  border: 1px solid rgba(59, 130, 246, 0.15); 
  animation: float-light 6s ease-in-out infinite;
  display: flex;
  flex-direction: column;
}

.prescription-nav {
  position: sticky;
  top: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(12px);
  z-index: 10;
  display: flex;
  flex-wrap: nowrap; /* 核心：严禁换行 */
  justify-content: center; /* 核心：水平居中 */
  align-items: center;
  gap: 6px; /* 紧凑间距 */
  padding: 16px 12px 14px 12px;
  /* 核心修改：取消明显细线，换成极淡的阴影 */
  border-bottom: 1px solid rgba(59, 130, 246, 0.05);
  box-shadow: 0 4px 10px rgba(59, 130, 246, 0.02);
  border-radius: 20px 20px 0 0;
  box-sizing: border-box;
  width: 100%;
  overflow-x: auto; /* 屏幕极小时可滑动保底 */
}
/* 隐藏滚动条 */
.prescription-nav::-webkit-scrollbar { display: none; }
.nav-chip {
  flex-shrink: 0; /* 严禁挤压 */
  font-size: 12px; /* 稍微缩小文字，给图标让位 */
  color: #3b82f6;
  background: rgba(59, 130, 246, 0.06); 
  padding: 8px 10px; /* 压缩宽度 */
  border-radius: 18px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex; /* 新增 */
  align-items: center; /* 新增 */
  gap: 4px; /* 图标文字间距 */
  white-space: nowrap;
}
/* 新增：专门针对胶囊内 Emoji/图标的大小设置 */
.nav-chip i, .nav-chip span {
  font-size: 16px; /* 放大图标，更有视觉重心 */
}
.nav-chip:hover {
  background: #3b82f6;
  color: white;
  transform: translateY(-1px);
}
.nav-chip:hover {
  background: #3b82f6;
  color: white;
  transform: translateY(-1px);
}

.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; margin: 15px 0; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #bfdbfe; border-radius: 10px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #93c5fd; }

.orb-content {
  text-align: left;
  z-index: 2;
  width: 100%;
  box-sizing: border-box; 
  padding: 10px 30px 32px 30px;
}

.orb-text.formatted-text {
  color: #334155;
  font-size: 15px;
  line-height: 1.8; 
  font-weight: 500;
  white-space: normal; 
  overflow-wrap: anywhere; 
  word-break: normal;
}

:deep(.prescription-header) {
  font-size: 18px;
  color: #1e293b;
  margin-top: 24px;
  margin-bottom: 12px;
  border-bottom: 2px solid rgba(59, 130, 246, 0.1);
  padding-bottom: 6px;
}
:deep(.healing-link) {
  color: #3b82f6; 
  text-decoration: underline;
  text-underline-offset: 4px;
  font-weight: 600;
  transition: all 0.2s;
  padding: 0 2px;
  word-break: break-all; 
}
:deep(.healing-link:hover) {
  color: #2563eb;
  background: rgba(59, 130, 246, 0.1);
  border-radius: 4px;
}
:deep(strong) {
  color: #0f172a;
  font-weight: 700;
  background: linear-gradient(180deg, transparent 60%, rgba(147, 197, 253, 0.4) 40%); 
}

.cursor-blink {
  display: inline-block;
  width: 2px;
  color: #3b82f6;
  font-weight: bold;
  animation: blink 1s step-end infinite;
  margin-left: 2px;
  vertical-align: text-bottom;
}

.action-area {
  margin-top: 28px;
  display: flex;
  gap: 16px;
}
.shatter-btn {
  background: linear-gradient(135deg, #60a5fa, #3b82f6); 
  color: white;
  border: none;
  padding: 12px 28px;
  border-radius: 20px;
  cursor: pointer;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.25);
  transition: all 0.3s;
}
.shatter-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.35);
}
.ghost-mode {
  background: rgba(255, 255, 255, 0.6); /* 半透明背景 */
  border: 1px solid rgba(59, 130, 246, 0.2); /* 淡蓝色描边 */
  color: #3b82f6; /* 品牌蓝文字 */
  padding: 12px 32px;
  border-radius: 24px; /* 圆润胶囊形 */
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.05); /* 极淡的蓝色投影 */
  display: flex;
  align-items: center;
  gap: 8px;
}

.ghost-mode:hover {
  background: white; /* 悬浮时变白 */
  border-color: #3b82f6;
  color: #2563eb;
  transform: translateY(-2px); /* 轻微上浮 */
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.15); /* 投影增强 */
}
.loading-text {
  margin-top: 24px;
  font-size: 14px;
  color: #3b82f6;
  font-weight: 500;
}

@keyframes float-pill { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(5px); } }
@keyframes float-light { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-4px); } }
@keyframes shake-shatter-capsule {
  0% { transform: translateX(0); filter: brightness(1); }
  15% { transform: translateX(-4px) rotate(-1deg); filter: brightness(1.05); }
  30% { transform: translateX(4px) rotate(1deg); filter: brightness(1.1); }
  45% { transform: translateX(-6px) scale(0.98); filter: brightness(1.2); }
  60% { transform: translateX(6px) scale(0.95); filter: blur(1px) brightness(1.5); opacity: 0.9; }
  80% { transform: scale(1.05); filter: blur(4px) brightness(2); opacity: 0.5; }
  100% { transform: scale(1.3); opacity: 0; filter: blur(8px) brightness(3); display: none; }
}
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }
.fade-in { animation: fadeIn 0.6s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(15px); } to { opacity: 1; transform: translateY(0); } }
</style>