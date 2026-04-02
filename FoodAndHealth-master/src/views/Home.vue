<script setup>
import Sidebar from '../components/Sidebar.vue'
import MainContent from '../components/MainContent.vue'
import { useSidebarStore } from '../store/sidebar'
import { storeToRefs } from 'pinia'
import { useMediaQuery } from '@vueuse/core'
import { watch, onMounted, ref, computed, onUnmounted, onActivated } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const currentConversationId = ref(null)
const currentConversationTitle = ref('')
const conversationList = ref([])
const sidebarStore = useSidebarStore()
const { isOpen } = storeToRefs(sidebarStore)
const isLargeScreen = useMediaQuery('(min-width: 768px)')

// 选择会话
function handleSelectConversation(payload) {
  const selectedConversation = typeof payload === 'object' && payload !== null
    ? payload
    : conversationList.value.find(conv => String(conv.id) === String(payload))

  const selectedId = selectedConversation?.id ?? payload ?? null
  console.log('[Home] Selected conversation ID:', selectedId)

  currentConversationId.value = selectedId
  currentConversationTitle.value = selectedConversation?.title || ''
}

function handleConversationsLoaded(list) {
  conversationList.value = Array.isArray(list) ? list : []

  if (!currentConversationId.value) {
    currentConversationTitle.value = ''
    return
  }

  const matchedConversation = conversationList.value.find(
    conv => String(conv.id) === String(currentConversationId.value)
  )

  currentConversationTitle.value = matchedConversation?.title || currentConversationTitle.value || ''
}

// 监听路由
watch(() => route.query.conversationId, (newId) => {
  if (newId) {
    console.log('[Home] 路由参数中有 conversationId:', newId)
    handleSelectConversation(parseInt(newId))
  } else {
    currentConversationId.value = null
    currentConversationTitle.value = ''
  }
}, { immediate: true })

// Android WebView 检测
const isAndroidWebView = computed(() => {
  return /android/i.test(navigator.userAgent) && /wv/i.test(navigator.userAgent)
})

// 屏幕宽度
const screenWidth = ref(window.innerWidth)
const updateScreenWidth = () => {
  screenWidth.value = window.innerWidth
}

const effectiveIsLargeScreen = computed(() => {
  if (isLargeScreen.value !== null) {
    return isLargeScreen.value
  }
  return screenWidth.value >= 768
})

// =============================================
// 👇 核心：进入页面就刷新的逻辑
// =============================================
function initPage() {
  console.log('✅ 页面已刷新/重新初始化')
  currentConversationId.value = null       // 重置会话
  currentConversationTitle.value = ''
  sidebarStore.setOpen(effectiveIsLargeScreen.value) // 重置侧边栏
}

// 首次加载
onMounted(() => {
  window.addEventListener('resize', updateScreenWidth)
  initPage()

  if (isAndroidWebView.value) {
    setTimeout(() => {
      sidebarStore.setOpen(effectiveIsLargeScreen.value)
    }, 100)
  }
})

// 从其他页面返回时也刷新
onActivated(() => {
  initPage()
})

// 监听屏幕大小
watch(effectiveIsLargeScreen, (newVal) => {
  sidebarStore.setOpen(newVal)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateScreenWidth)
})
</script>

<template>
  <div class="flex h-screen w-full bg-gray-50 overflow-hidden relative">
    <div
        v-if="!effectiveIsLargeScreen && isOpen"
        class="fixed inset-0 bg-black/50 z-20 transition-opacity"
        @click="sidebarStore.setOpen(false)"
    ></div>

    <Sidebar
        class="duration-300 ease-in-out z-30"
        :active-conversation-id="currentConversationId"
        :class="[
        effectiveIsLargeScreen ? 'relative translate-x-0' : 'fixed inset-y-0 left-0',
        isOpen ? 'translate-x-0' : '-translate-x-full'
      ]"
        @select-conversation="handleSelectConversation"
        @conversations-loaded="handleConversationsLoaded"
    />

    <div class="flex-1 flex flex-col h-full w-full relative transition-all duration-300">
      <MainContent
        :conversation-id="currentConversationId"
        :conversation-title="currentConversationTitle"
      />
    </div>
  </div>
</template>
