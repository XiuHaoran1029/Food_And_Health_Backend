<script setup>
import { ref, onMounted } from 'vue'
import { MessageSquarePlus, MessageSquare, User, Trash2 } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getConversationList, createConversation, deleteConversation } from '@/api/conversation'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const emit = defineEmits(['select-conversation'])

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

    const list = res.data?.content || res.data?.records || (Array.isArray(res.data) ? res.data : [])

    conversations.value = list

    console.log('Processed List:', conversations.value) // 确认这里确实是 []

    // 如果没有对话，自动创建一个新对话
    if (conversations.value.length === 0) {
      await handleNewConversation()
    } else {
      // 如果有对话，自动选择第一个对话
      emit('select-conversation', conversations.value[0].id)
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
    emit('select-conversation', newConv.id);
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
  emit('select-conversation', conv.id)
}

const handleClick = (buttonText) => {
  if (buttonText === 'Settings') {
    router.push({ name: 'Settings' })
  }
}
</script>

<template>
  <div class="w-64 h-full bg-white border-r border-gray-200 flex flex-col shrink-0 shadow-sm">
    <!-- Logo -->
    <div class="h-16 flex items-center px-4 border-b border-gray-100">
      <div class="w-8 h-8 bg-primary rounded-lg flex items-center justify-center text-white font-bold mr-2 shadow-sm">
        A
      </div>
      <span class="text-xl font-bold text-gray-800">AAA</span>
    </div>

    <!-- New Chat Button -->
    <div class="p-4">
      <button
        class="w-full flex items-center justify-center gap-2 bg-primary/10 text-primary hover:bg-primary/20 py-2.5 rounded-lg transition-colors font-medium"
        @click="handleNewConversation"
      >
        <MessageSquarePlus :size="20" />
        <span>新对话</span>
      </button>
    </div>

    <!-- Categories / Tags -->
    <div class="px-4 pb-2">
      <div class="text-xs font-semibold text-gray-400 mb-2 uppercase tracking-wider">最近对话</div>
    </div>

    <!-- Chat List -->
    <div class="flex-1 overflow-y-auto px-2 space-y-1">
      <button
        v-for="conv in conversations"
        :key="conv.id"
        class="w-full text-left px-3 py-2 rounded-lg text-sm text-gray-700 hover:bg-gray-100 flex items-center gap-2 truncate transition-colors group"
        @click="handleSelectConversation(conv)"
      >
        <MessageSquare :size="16" class="shrink-0 text-gray-400 group-hover:text-gray-600" />
        <span class="truncate flex-1">{{ conv.title }}</span>
        <Trash2
          :size="14"
          class="shrink-0 text-gray-300 group-hover:text-red-400 opacity-0 group-hover:opacity-100 transition-opacity"
          @click="handleDeleteConversation(conv, $event)"
        />
      </button>
    </div>

    <!-- Bottom Actions -->
    <div class="p-4 border-t border-gray-100 space-y-2">
        <button class="w-full flex items-center gap-2 px-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-lg transition-colors" @click="handleClick('Settings')">
            <User :size="18" class="text-gray-500" />
            <span>我的空间</span>
        </button>
    </div>
  </div>
</template>
