<script setup>
import { ref, onMounted } from 'vue'
import { MessageSquarePlus, MessageSquare, User, Trash2, CalendarDays } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useMediaQuery } from '@vueuse/core'
import { getConversationList, createConversation, deleteConversation } from '@/api/conversation'
import { useSidebarStore } from '@/store/sidebar'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()
const sidebarStore = useSidebarStore()
const isLargeScreen = useMediaQuery('(min-width: 768px)')

const props = defineProps({
  activeConversationId: {
    type: [Number, String],
    default: null
  }
})

const emit = defineEmits(['select-conversation', 'conversations-loaded'])

const conversations = ref([])

onMounted(async () => {
  await loadConversations()
})

async function loadConversations() {
  try {
    const res = await getConversationList(userStore.userId)

    // 调试打印，确认后端返回的具体结构
    console.log('API Response:', res)

    // 修正逻辑：
    // 1. 优先检查 res.data.content (根据你的后端返回)
    // 2. 兼容 res.data.records (如果是其他后端风格)
    // 3. 如果 res.data 本身就是数组，则使用 res.data
    // 4. 否则默认为空数组

    conversations.value = res.data?.content || res.data?.records || (Array.isArray(res.data) ? res.data : [])
    emit('conversations-loaded', conversations.value)

    console.log('Processed List:', conversations.value) // 确认这里确实是 []

    // 如果没有对话，自动创建一个新对话
    if (conversations.value.length === 0) {
      await handleNewConversation()
    } else {
      // 如果有对话，优先选择路由/父组件当前指定的对话
      const matchedConv = conversations.value.find(
        conv => String(conv.id) === String(props.activeConversationId)
      )
      emit('select-conversation', matchedConv || conversations.value[0])
    }
  } catch (e) {
    showToast(e.message || '加载对话列表失败')
  }
}
async function handleNewConversation() {
  try {
    // 1. 获取当前时间对象
    const now = new Date();

    // 2. 格式化时间 (例如: 2026-03-21 22:20)
    // 注意：月份需要 +1，分钟和小时如果小于10建议补0
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');

    const timeString = `${year}-${month}-${day} ${hours}:${minutes}`;

    // 3. 组合名称
    const conversationName = `对话—${timeString}`;

    // 4. 调用接口
    const res = await createConversation(userStore.userId, conversationName);

    const newConv = res.data;
    conversations.value.unshift(newConv);
    emit('conversations-loaded', conversations.value)
    emit('select-conversation', newConv);
    if (!isLargeScreen.value) sidebarStore.setOpen(false)
  } catch (e) {
    showToast(e.message || '创建对话失败');
  }
}

async function handleDeleteConversation(conv, event) {
  event.stopPropagation()
  try {
    await deleteConversation(conv.id, userStore.userId)
    conversations.value = conversations.value.filter(c => c.id !== conv.id)
  } catch (e) {
    showToast(e.message || '删除对话失败')
  }
}

function handleSelectConversation(conv) {
  emit('select-conversation', conv)
  if (!isLargeScreen.value) sidebarStore.setOpen(false)
}

function isActiveConversation(conv) {
  return String(conv?.id) === String(props.activeConversationId)
}

const handleClick = (buttonText) => {
  if (buttonText === 'Settings') {
    router.push({ name: 'Settings' })
  } else if (buttonText === 'MealCalendar') {
    router.push({ name: 'MealCalendar' })
  }
  if (!isLargeScreen.value) sidebarStore.setOpen(false)
}
</script>

<template>
  <div class="w-72 h-full shrink-0 flex flex-col bg-white/85 backdrop-blur-xl border-r border-white/70 shadow-[16px_0_48px_rgba(15,23,42,0.08)]">
    <!-- Logo -->
    <div class="px-4 pt-5 pb-4 border-b border-slate-200/70">
      <div class="flex items-center gap-3 rounded-2xl app-surface px-3 py-3">
        <img
            src="/logo.png"
            alt="Logo"
            class="w-10 h-10 rounded-2xl shadow-sm object-cover ring-1 ring-white"
        >
        <div class="min-w-0">
          <div class="text-[10px] uppercase tracking-[0.22em] text-slate-400 font-semibold">Health Diary</div>
          <span class="block text-lg font-bold text-slate-900 truncate">时康日记</span>
        </div>
      </div>
    </div>

    <!-- New Chat Button -->
    <div class="p-4">
      <button
        class="w-full flex items-center justify-center gap-2 rounded-2xl px-4 py-3 font-semibold app-primary-action transition-all active:scale-[0.99]"
        @click="handleNewConversation"
      >
        <MessageSquarePlus :size="20" />
        <span>新对话</span>
      </button>
    </div>

    <!-- Categories / Tags -->
    <div class="px-4 pb-2">
      <div class="text-xs font-semibold text-slate-400 mb-2 uppercase tracking-[0.24em]">最近对话</div>
    </div>

    <!-- Chat List -->
    <div class="flex-1 overflow-y-auto px-3 space-y-1 app-scrollbar-hidden">
      <button
        v-for="conv in conversations"
        :key="conv.id"
        class="w-full text-left px-3 py-3 rounded-2xl text-sm flex items-center gap-3 truncate transition-all group border"
        :class="isActiveConversation(conv)
          ? 'bg-primary/10 border-primary/20 text-slate-900 shadow-sm'
          : 'bg-transparent border-transparent text-slate-700 hover:bg-white/70 hover:border-slate-200/80 hover:shadow-sm'"
        @click="handleSelectConversation(conv)"
      >
        <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-xl" :class="isActiveConversation(conv) ? 'bg-white text-primary shadow-sm' : 'bg-slate-100 text-slate-400 group-hover:bg-white'">
          <MessageSquare :size="16" />
        </div>
        <div class="min-w-0 flex-1">
          <span class="block truncate font-medium">{{ conv.title }}</span>
          <span v-if="isActiveConversation(conv)" class="text-[11px] text-primary/80">当前会话</span>
        </div>
        <Trash2
          :size="14"
          class="shrink-0 text-red-400 opacity-100 md:text-slate-300 md:opacity-0 md:group-hover:opacity-100 transition-opacity"
          @click="handleDeleteConversation(conv, $event)"
        />
      </button>
    </div>

    <!-- Bottom Actions -->
    <div class="p-4 border-t border-slate-200/70 space-y-2">
        <button class="w-full flex items-center gap-3 px-3 py-3 text-sm text-slate-700 hover:bg-white/80 rounded-2xl transition-all border border-transparent hover:border-slate-200/80 hover:shadow-sm" @click="handleClick('Settings')">
            <User :size="18" class="text-slate-500" />
            <span class="font-medium">我的空间</span>
        </button>
        <button class="w-full flex items-center gap-3 px-3 py-3 text-sm text-slate-700 hover:bg-white/80 rounded-2xl transition-all border border-transparent hover:border-slate-200/80 hover:shadow-sm" @click="handleClick('MealCalendar')">
            <CalendarDays :size="18" class="text-slate-500" />
            <span class="font-medium">三餐日历</span>
        </button>
    </div>
  </div>
</template>
