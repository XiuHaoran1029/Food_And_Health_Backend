<script setup>
import Sidebar from '../components/Sidebar.vue'
import MainContent from '../components/MainContent.vue'
import { useSidebarStore } from '../store/sidebar'
import { storeToRefs } from 'pinia'
import { useMediaQuery } from '@vueuse/core'
import { watch, onMounted, ref, computed, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const currentConversationId = ref(null)

function handleSelectConversation(id) {
  console.log('[Home] Selected conversation ID:', id)
  currentConversationId.value = id
}

// 监听路由参数中的 conversationId
watch(() => route.query.conversationId, (newId) => {
  if (newId) {
    console.log('[Home] 路由参数中有 conversationId:', newId)
    handleSelectConversation(parseInt(newId))
  } else {
    // 当路由参数中没有 conversationId 时，重置为 null
    currentConversationId.value = null
  }
}, { immediate: true })

const sidebarStore = useSidebarStore()
const { isOpen } = storeToRefs(sidebarStore)
const isLargeScreen = useMediaQuery('(min-width: 768px)')

// 添加Android WebView检测
const isAndroidWebView = computed(() => {
  return /android/i.test(navigator.userAgent) && /wv/i.test(navigator.userAgent)
})

// 添加屏幕宽度检测作为备选方案
const screenWidth = ref(window.innerWidth)
const updateScreenWidth = () => {
  screenWidth.value = window.innerWidth
}

// 使用更可靠的响应式检测
const effectiveIsLargeScreen = computed(() => {
  // 如果媒体查询可用，使用媒体查询结果
  if (isLargeScreen.value !== null) {
    return isLargeScreen.value
  }
  // 否则使用屏幕宽度
  return screenWidth.value >= 768
})

// 初始化侧边栏状态
onMounted(() => {
  // 监听窗口大小变化
  window.addEventListener('resize', updateScreenWidth)

  // 设置初始状态
  if (!effectiveIsLargeScreen.value) {
    sidebarStore.setOpen(false)
  }

  // Android WebView特殊处理
  if (isAndroidWebView.value) {
    // 强制触发一次重绘，确保样式正确应用
    setTimeout(() => {
      sidebarStore.setOpen(effectiveIsLargeScreen.value)
    }, 100)
  }
})

// 监听屏幕大小变化
watch(effectiveIsLargeScreen, (newVal) => {
  sidebarStore.setOpen(newVal)
})

// 组件卸载时清理事件监听
onUnmounted(() => {
  window.removeEventListener('resize', updateScreenWidth)
})
</script>

<template>
  <div class="flex h-screen w-full bg-gray-50 overflow-hidden relative">
    <!-- Mobile Overlay -->
    <div
        v-if="!effectiveIsLargeScreen && isOpen"
        class="fixed inset-0 bg-black/50 z-20 transition-opacity"
        @click="sidebarStore.setOpen(false)"
        :style="{
        display: !effectiveIsLargeScreen && isOpen ? 'block' : 'none',
        opacity: !effectiveIsLargeScreen && isOpen ? '0.5' : '0'
      }"
    ></div>

    <!-- Sidebar -->
    <Sidebar
        class="duration-300 ease-in-out z-30"
        :class="[
        effectiveIsLargeScreen ? 'relative translate-x-0' : 'fixed inset-y-0 left-0',
        isOpen ? 'translate-x-0' : '-translate-x-full'
      ]"
        :style="{
        transform: isOpen ? 'translateX(0)' : 'translateX(-100%)',
        visibility: isOpen ? 'visible' : 'hidden',
        transition: 'transform 0.3s ease-in-out, visibility 0.3s ease-in-out'
      }"
        @select-conversation="handleSelectConversation"
    />

    <!-- Main Content -->
    <div class="flex-1 flex flex-col h-full w-full relative transition-all duration-300">
      <MainContent :conversation-id="currentConversationId" />
    </div>
  </div>
</template>
