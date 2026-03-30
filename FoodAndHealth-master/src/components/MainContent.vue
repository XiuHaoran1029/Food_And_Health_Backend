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
  const menuButton = document.querySelector('.menu-button')
  if (menuButton) {
    menuButton.addEventListener('touchstart', handleMenuClick, { passive: false })
    menuButton.addEventListener('click', () => { hasUserInteracted.value = true })
  }
})

// 清理所有流
function clearAllStreaming() {
  Object.values(streamingTimers).forEach(timer => clearInterval(timer))
  streamingTimers = {}
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
    console.error('[loadMessages] Invalid conversationId:', conversationId)
    showToast('请选择有效的对话')
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
      img: msg.img || null
    })
    scrollToBottom()
    // 开始等待AI回复
    startWaiting()
  } else if (messageRole === 'assistant') {
    // 处理AI消息 - 调用appendMessage
    appendMessage(msg)
  }
}

defineExpose({appendMessage, startWaiting, stopWaiting, onMessageSent})

watch(() => props.conversationId, (newId) => {
  loadMessages(newId)
}, { immediate: true })
</script>

<template>
  <div class="flex flex-col h-full relative">
    <!-- 头部菜单 -->
    <div class="absolute top-0 left-0 p-4 z-10 md:hidden">
      <button
          @click="handleMenuClick"
          class="menu-button p-2 -ml-2 text-gray-600 hover:bg-gray-100 rounded-lg transition-colors select-none"
          style="touch-action: manipulation; -webkit-tap-highlight-color: transparent;"
      >
        <Menu :size="24" />
      </button>
    </div>

    <!-- 消息区域 -->
    <div
        ref="messagesContainer"
        class="flex-1 overflow-y-auto px-4 pt-16 pb-2 space-y-4"
    >
      <!-- 空状态 -->
      <div v-if="!conversationId || messages.length === 0" class="flex flex-col items-center justify-center h-full">
        <div class="text-center mb-12">
          <div class="w-16 h-16 bg-primary rounded-2xl flex items-center justify-center text-white font-bold text-3xl mx-auto mb-6 shadow-lg shadow-primary/30">
            A
          </div>
          <h1 class="text-2xl font-bold text-gray-800 mb-2">你好，我是AAA</h1>
          <p class="text-gray-500">我可以帮你写代码、解答问题、激发灵感</p>
        </div>
      </div>

      <!-- 消息列表 -->
      <template v-else>
        <div v-for="msg in messages" :key="msg.id" class="flex"
        :class="msg.role === 'user' ? 'justify-end' : 'justify-start'"
        >
        <!-- AI头像 -->
        <div v-if="msg.role !== 'user'" class="w-8 h-8 bg-primary rounded-full flex items-center justify-center text-white font-bold text-sm mr-2 shrink-0 self-end">
          A
        </div>

        <div
            class="max-w-[75%] px-4 py-2.5 rounded-2xl text-sm leading-relaxed break-words shadow-sm"
            :class="msg.role === 'user'
              ? 'bg-primary text-white rounded-br-sm'
              : 'bg-white border border-gray-200 text-gray-800 rounded-bl-sm'"
        >
          <img v-if="msg.img && msg.role === 'user'" :src="msg.img" class="max-w-full rounded-lg mb-2" />

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
          <div class="w-9 h-9 rounded-full bg-primary flex items-center justify-center text-white font-bold text-sm mr-1.5 border border-gray-200 shadow-sm">
            A
          </div>
        </div>
        <div class="flex flex-col min-w-0 items-start">
          <div class="text-[10px] font-bold text-gray-600 mb-1 select-none px-1.5 py-0.5 rounded-md bg-white/50 backdrop-blur-sm border border-white/20 w-fit shadow-sm mr-auto ml-1 truncate max-w-[150px] md:max-w-[250px]">
            AAA
          </div>
          <div class="min-w-[4rem] min-h-[4rem] px-3 py-3 rounded-2xl bg-white border border-gray-100 shadow-card flex flex-col items-center justify-center gap-2">
            <div class="typing-indicator scale-125">
              <span class="bg-primary-400"></span>
              <span class="bg-primary-400"></span>
              <span class="bg-primary-400"></span>
            </div>
            <div class="flex items-center gap-1.5 text-[11px] text-gray-400 font-mono bg-gray-50 px-2 py-0.5 rounded-full border border-gray-100 animate-pulse mt-0.5">
              <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
              <span class="whitespace-nowrap">{{ waitTime }}s</span>
            </div>
          </div>
        </div>
      </div>
    </div>
</template>
</div>

<!-- 输入框 -->
<InputBar :conversation-id="conversationId" @message-sent="onMessageSent" />
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

.typing-indicator.fade-out {
  opacity: 0;
  transform: translateY(10px);
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

.animate-message-in.fade-out {
  opacity: 0;
  transform: translateY(10px) scale(0.95);
}

/* 气泡阴影 */
.shadow-card {
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.03), 0 2px 8px rgba(0, 0, 0, 0.04);
}
</style>
