<script setup>
import InputBar from './InputBar.vue'
import { Menu } from 'lucide-vue-next'
import { useSidebarStore } from '../store/sidebar'
import { ref, watch, onMounted, nextTick, onUnmounted } from 'vue'
import { getMessageList } from '@/api/message'
import { useUserStore } from '@/store/user'
import { showToast } from 'vant'
import { extractAndNormalizeMessages, normalizeMessage } from '@/utils/messageNormalization'
import MarkdownIt from 'markdown-it'

// 初始化 markdown-it
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true
})

// 等待动画状态
const isWaiting = ref(false)
const waitTime = ref(0)
let waitTimer = null

// 开始等待
function startWaiting() {
  isWaiting.value = true
  waitTime.value = 0
  if (waitTimer) clearInterval(waitTimer)
  waitTimer = setInterval(() => {
    waitTime.value++
  }, 1000)
}

// 结束等待
function stopWaiting() {
  isWaiting.value = false
  if (waitTimer) {
    clearInterval(waitTimer)
    waitTimer = null
  }
}

const props = defineProps({
  conversationId: {
    type: Number,
    default: null
  },
  conversationTitle: {
    type: String,
    default: ''
  }
})

const sidebarStore = useSidebarStore()
const userStore = useUserStore()
const hasUserInteracted = ref(false)
const messages = ref([])
const messagesContainer = ref(null)

// 定时器存储
let streamingTimers = {}
// 滚动防抖定时器
let scrollTimer = null

// 菜单点击
const handleMenuClick = (event) => {
  event.preventDefault()
  event.stopPropagation()
  if (!hasUserInteracted.value) hasUserInteracted.value = true
  if (hasUserInteracted.value && 'vibrate' in navigator) {
    try { navigator.vibrate(10) } catch (e) {}
  }
  sidebarStore.toggle()
}

onMounted(() => {
  // 只依赖模板上的 click 事件，避免 touchstart + click 触发两次切换导致侧边栏“卡住”
})

// 清理所有流
function clearAllStreaming() {
  Object.values(streamingTimers).forEach(timer => clearInterval(timer))
  streamingTimers = {}
}

function retractMessage(messageId) {
  if (!messageId) return
  messages.value = messages.value.filter(msg => String(msg.id) !== String(messageId))
  if (messages.value.length === 0) {
    stopWaiting()
  }
}

onUnmounted(() => {
  clearAllStreaming()
  if (scrollTimer) clearTimeout(scrollTimer)
  if (waitTimer) clearInterval(waitTimer)
})

// 平滑滚动到底部（防抖）
function scrollToBottom() {
  if (scrollTimer) clearTimeout(scrollTimer)
  scrollTimer = setTimeout(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  }, 10)
}

// 加载消息
async function loadMessages(conversationId) {
  console.log('[loadMessages] conversationId:', conversationId)

  if (!conversationId || conversationId <= 0) {
    messages.value = []
    return
  }

  if (!userStore.userId) {
    console.warn('[loadMessages] userId not ready yet, skip loading messages')
    messages.value = []
    return
  }

  clearAllStreaming() // 切换会话必须清空旧流
  stopWaiting() // 停止等待动画
  messages.value = []

  try {
    const res = await getMessageList(conversationId, userStore.userId)
    const normalizedMessages = extractAndNormalizeMessages(res)
    console.log('[loadMessages] 获取消息成功:', res)
    messages.value = normalizedMessages.map(msg => ({
      ...msg,
      fullContent: msg.content || '',
      displayContent: msg.content || '',
      isStreaming: false
    }))

    await nextTick()
    scrollToBottom()
  } catch (e) {
    console.error('[loadMessages] 加载消息失败:', e)
    if (e.message && e.message.includes('对话不存在') || e.message.includes('无权限')) {
      //showToast('对话不存在或您没有权限访问')
      messages.value = []
    } else {
      showToast(e.message || '加载消息失败')
    }
  }
}

// 流式输出核心（已修复所有BUG）
function simulateStreaming(msg) {
  const fullText = msg.fullContent?.trim()
  if (!fullText) {
    msg.isStreaming = false
    stopWaiting() // 确保停止动画
    return
  }

  // ✅ 修复：唯一ID 语法正确（无空格）
  const uniqueId = msg.id || `temp_${Date.now()}_${Math.random()}`
  msg.id = uniqueId

  // 清除旧定时器
  if (streamingTimers[uniqueId]) {
    clearInterval(streamingTimers[uniqueId])
  }

  let currentLength = 0
  const speed = 40
  const chunkSize = 2
  let hasStoppedWaiting = false // 防止重复停止等待动画

  streamingTimers[uniqueId] = setInterval(() => {
    currentLength += chunkSize
    if (currentLength >= fullText.length) {
      currentLength = fullText.length
      clearInterval(streamingTimers[uniqueId])
      delete streamingTimers[uniqueId]
      msg.isStreaming = false
    }

    msg.displayContent = fullText.slice(0, currentLength)
    
    // ✅ 修复: 在第一次显示内容时平滑停止等待动画
    if (currentLength >= chunkSize && !hasStoppedWaiting) {
      hasStoppedWaiting = true
      // 延迟停止等待动画，让AI回复开始显示后再隐藏等待指示器
      setTimeout(() => {
        stopWaiting()
      }, 200) // 200ms 延迟确保平滑过渡
    }
    
    scrollToBottom()
  }, speed)
}

function appendMessage(msg) {
  // ✅ 修复: 处理 null 消息情况，确保停止等待动画
  if (!msg) {
    stopWaiting()
    return
  }

  const normalizedMsg = normalizeMessage(msg)

  if (!normalizedMsg) {
    stopWaiting()
    return
  }

  // ✅ 修复: 不立即停止等待动画，让 simulateStreaming 控制过渡

  const tempId = `new_${Date.now()}`

  const newMsgObj = {
    ...normalizedMsg,
    role: 'assistant', // 强制覆盖
    id: normalizedMsg.id || tempId,
    fullContent: normalizedMsg.content || '',
    displayContent: '',
    isStreaming: true
  }

  messages.value.push(newMsgObj)

  nextTick(() => {
    const lastMsg = messages.value[messages.value.length - 1]

    if (lastMsg) {
      simulateStreaming(lastMsg)
    }
  })
}

// 发送消息时开始等待
function onMessageSent(msg) {
  if (!msg) return
  
  // ✅ 关键修复：根据消息角色区分用户消息和AI消息
  const messageRole = msg.role || 'user'
  
  if (messageRole === 'user') {
    // 处理用户消息
    const tempId = `user_${Date.now()}`
    messages.value.push({
      id: tempId,
      role: 'user',
      content: msg.content || msg,
      img: msg.img || null,
      pending: !!msg.pending
    })
    scrollToBottom()
    // 开始等待AI回复
    startWaiting()
  } else if (messageRole === 'assistant') {
    // 处理AI消息 - 调用appendMessage
    appendMessage(msg)
  }
}

defineExpose({appendMessage, startWaiting, stopWaiting, onMessageSent, retractMessage})

watch(() => props.conversationId, (newId) => {
  if (newId) {
    loadMessages(newId)
  } else {
    messages.value = []
  }
}, { immediate: true })
</script>

<template>
  <div class="flex flex-col h-full relative bg-[radial-gradient(circle_at_top_right,rgba(22,119,255,0.08),transparent_24%),radial-gradient(circle_at_bottom_left,rgba(16,185,129,0.06),transparent_22%),linear-gradient(180deg,rgba(248,250,252,0.9),rgba(239,246,255,0.9))]">
    <!-- 移动端顶部栏：菜单按钮与标题同排，避免遮挡 -->
    <div class="md:hidden shrink-0 flex items-center gap-3 px-3 pt-5 pb-3 app-surface-strong border-b border-white/70 z-10">
      <button
        @click="handleMenuClick"
        class="menu-button flex-shrink-0 p-2 -ml-1 rounded-xl app-icon-action select-none"
        style="touch-action: manipulation; -webkit-tap-highlight-color: transparent;"
        aria-label="打开侧边栏"
      >
        <Menu :size="22" />
      </button>

      <div class="min-w-0 flex-1">
        <p class="text-[10px] uppercase tracking-[0.24em] text-slate-400 leading-none mb-1">当前对话</p>
        <h2 class="text-base font-semibold text-slate-900 truncate">
          {{ conversationTitle || '未命名对话' }}
        </h2>
      </div>
    </div>

    <div class="flex-1 flex flex-col min-h-0">
      <!-- 桌面端标题栏 -->
      <div v-if="conversationId" class="hidden md:block shrink-0 px-5 pt-6 pb-4 app-surface-strong border-b border-white/70 rounded-b-[28px] mx-4 mt-4">
        <div class="min-w-0">
          <p class="text-xs uppercase tracking-[0.24em] text-slate-400 mb-1">当前对话</p>
          <h2 class="text-lg font-semibold text-slate-900 truncate">
            {{ conversationTitle || '未命名对话' }}
          </h2>
        </div>
      </div>

      <!-- 消息区域 -->
      <div
        ref="messagesContainer"
        class="flex-1 overflow-y-auto px-3 sm:px-4 pb-3 space-y-4 pt-4 md:pt-6"
      >
        <!-- 空状态 -->
        <div v-if="!conversationId || messages.length === 0" class="flex flex-col items-center justify-center h-full">
          <div class="text-center mb-12 max-w-md w-full px-4">
            <div class="app-card-soft px-6 py-8 sm:px-8 sm:py-10">
              <img
                  src="/logo.png"
                  alt="Logo"
                  class="w-16 h-16 rounded-3xl object-cover mx-auto mb-6 shadow-lg shadow-primary/20 ring-1 ring-white"
              >
              <h1 class="text-2xl font-bold text-slate-900 mb-3">你好，我是康康</h1>
              <p class="text-slate-500 leading-7">我可以帮你分析健康问题，也可以直接进入三餐分析、零食分析和用药提醒。</p>
              <div class="mt-6 flex flex-wrap justify-center gap-2 text-xs text-slate-500">
                <span class="px-3 py-1.5 rounded-full bg-slate-100">AI 对话</span>
                <span class="px-3 py-1.5 rounded-full bg-slate-100">三餐分析</span>
                <span class="px-3 py-1.5 rounded-full bg-slate-100">零食记录</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <template v-else>
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="flex"
            :class="msg.role === 'user' ? 'justify-end' : 'justify-start'"
          >
            <!-- AI头像 -->
            <div v-if="msg.role !== 'user'" class="mr-2 shrink-0 self-end">
              <div class="w-9 h-9 rounded-2xl bg-white shadow-sm ring-1 ring-slate-200/80 overflow-hidden flex items-center justify-center">
                <img src="/logo.png" alt="AI" class="w-8 h-8 rounded-full object-cover">
              </div>
            </div>

            <div
              class="max-w-[78%] px-4 py-3 rounded-3xl text-sm leading-relaxed break-words shadow-sm transition-all"
              :class="msg.role === 'user'
                ? 'bg-primary text-white rounded-br-md shadow-[0_12px_28px_rgba(22,119,255,0.24)]'
                : 'bg-white/95 border border-slate-200/80 text-slate-800 rounded-bl-md shadow-[0_10px_28px_rgba(15,23,42,0.08)]'"
            >
              <img v-if="msg.img && msg.role === 'user'" :src="msg.img" alt="用户上传图片" class="max-w-full rounded-2xl mb-3 ring-1 ring-white/20" />


              <div class="message-content prose prose-sm max-w-none" :class="msg.role === 'user' ? 'prose-invert' : ''">
                <template v-if="msg.role === 'user'">
                  {{ msg.content }}
                </template>
                <template v-else>
                  <div v-html="md.render(msg.displayContent || '')"></div>
                  <span v-if="msg.isStreaming" class="inline-block w-2 h-4 ml-1 align-middle bg-gray-400 animate-pulse"></span>
                </template>
              </div>
            </div>
          </div>

          <!-- Typing Indicator 等待动画 -->
          <div v-if="isWaiting" class="flex w-full justify-start animate-message-in">
            <div class="flex max-w-[99%] md:max-w-[75%] lg:max-w-[70%] flex-row">
              <div class="flex-shrink-0">
                <div class="w-9 h-9 rounded-2xl bg-white flex items-center justify-center text-white font-bold text-sm mr-1.5 border border-slate-200/80 shadow-sm overflow-hidden">
                  <img src="/logo.png" alt="AI" class="w-8 h-8 rounded-full object-cover">
                </div>
              </div>
              <div class="flex flex-col min-w-0 items-start">
                <div class="text-[10px] font-bold text-slate-500 mb-1 select-none px-2 py-1 rounded-full bg-white/70 backdrop-blur-sm border border-slate-200/80 w-fit shadow-sm mr-auto ml-1 truncate max-w-[150px] md:max-w-[250px]">
                  康康
                </div>
                <div class="min-w-[4rem] min-h-[4rem] px-4 py-4 rounded-3xl bg-white/95 border border-slate-200/80 shadow-[0_10px_28px_rgba(15,23,42,0.08)] flex flex-col items-center justify-center gap-2">
                  <div class="typing-indicator scale-125">
                    <span class="bg-primary-400"></span>
                    <span class="bg-primary-400"></span>
                    <span class="bg-primary-400"></span>
                  </div>
                  <div class="flex items-center gap-1.5 text-[11px] text-slate-400 font-mono bg-slate-50 px-2 py-0.5 rounded-full border border-slate-100 animate-pulse mt-0.5">
                    <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                    <span class="whitespace-nowrap">{{ waitTime }}s</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- 输入框 -->
    <InputBar :conversation-id="conversationId" @message-sent="onMessageSent" @message-retracted="retractMessage" />
  </div>
</template>

<style scoped>
:deep(.message-content p) {
  margin-bottom: 0.5em;
}
:deep(.message-content p:last-child) {
  margin-bottom: 0;
}
:deep(.message-content pre) {
  background-color: #f3f4f6;
  padding: 0.5rem;
  border-radius: 0.25rem;
  overflow-x: auto;
}
:deep(.prose-invert pre) {
  background-color: rgba(255, 255, 255, 0.1);
}

/* 消息进入动画 */
.animate-message-in {
  animation: messageIn 0.4s ease-out backwards;
}

@keyframes messageIn {
  0% {
    opacity: 0;
    transform: translateY(10px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 打字指示器动画 */
.typing-indicator {
  display: flex;
  align-items: center;
  gap: 6px;
  transition: opacity 0.3s ease-out, transform 0.3s ease-out;
}

.typing-indicator span {
  display: block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

/* ✅ 修复: 等待动画容器的平滑过渡 */
.animate-message-in {
  animation: messageIn 0.4s ease-out backwards;
  transition: opacity 0.3s ease-out, transform 0.3s ease-out;
}


</style>


