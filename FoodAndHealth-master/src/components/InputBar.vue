<script setup>
import { Plus, Mic, Send, X } from 'lucide-vue-next'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { sendMessage } from '@/api/message'
import { useUserStore } from '@/store/user'
import { fileToBase64, compressImage } from '@/utils/helper'
import { processAIResponse } from '@/utils/messageNormalization'
import { sendReport } from '@/utils/reportAnalysis'

const props = defineProps({
  conversationId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['message-sent'])

const router = useRouter()
const userStore = useUserStore()
const inputValue = ref('')
const isSending = ref(false)
const isReportAnalyzing = ref(false)
const attachedImage = ref(null)   // { file, base64, preview }
const fileInput = ref(null)

function handleClick(buttonText) {
  if (buttonText === 'FoodRecord') router.push({ name: 'FoodRecord' })
  if (buttonText === 'SnackAnalysis') router.push({ name: 'SnackAnalysis' })
  if (buttonText === 'MedicationReminder') router.push({ name: 'MedicationReminder' })
  if (buttonText === 'ReportAnalysis') {
    isReportAnalyzing.value = true
    sendReport().finally(() => {
      isReportAnalyzing.value = false
    })
  }
}

function triggerFileInput() {
  fileInput.value?.click()
}

async function handleFileChange(event) {
  const file = event.target.files?.[0]
  if (!file) return
  if (file.size > 10* 1024 * 1024) {
    showToast('图片大小不能超过10MB')
    return
  }
  try {
    // Get compressed version for upload
    const compressedResult = await compressImage(file)
    const compressedBase64 = typeof compressedResult === 'string'
      ? compressedResult
      : compressedResult.base64
    // Get original version for preview
    const previewBase64 = await fileToBase64(file)
    attachedImage.value = { file, base64: compressedBase64, preview: previewBase64 }
  } catch (e) {
    showToast('图片处理失败')
  }
  // reset input so same file can be re-selected
  event.target.value = ''
}

function removeImage() {
  attachedImage.value = null
}

async function handleSend() {
  const content = inputValue.value.trim()
  if (!content && !attachedImage.value) return
  if (!props.conversationId) {
    showToast('请先选择或创建一个对话')
    return
  }

  isSending.value = true
  const imgBase64 = attachedImage.value?.base64 || ''
  const imgPreview = attachedImage.value?.preview || ''

  // Optimistically show user message
  emit('message-sent', {
    role: 'user',
    content: content || '',
    img: imgPreview,
    sequence: Date.now()
  })

  inputValue.value = ''
  attachedImage.value = null

  try {
    const res = await sendMessage({
      userId: userStore.userId,
      conversationId: props.conversationId,
      content: content,
      role: 'user',
      function_type: 'normal',
      img: imgBase64,
      mimeType: ''
    })

    // Append AI reply
    const aiMsg = res.data
    if (aiMsg) {
      console.log('AI reply:', aiMsg)
      
      // ✅ 修复: 处理AI回复，通过role字段区分消息类型
      const processedAIReply = processAIResponse(aiMsg)
      
      if (processedAIReply) {
        // 直接发送AI消息，MainContent会根据role字段正确处理
        emit('message-sent', processedAIReply)
      } else {
        console.warn('Failed to process AI reply:', aiMsg)
      }
    } else {
      console.warn('No AI reply received:', res)
    }
  } catch (e) {
    showToast(e.message || '发送失败，请重试')
  } finally {
    isSending.value = false
  }
}

function handleKeydown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSend()
  }
}
</script>

<template>
  <div class="w-full max-w-4xl mx-auto px-4 pb-4">
    <!-- Overlay for ReportAnalysis -->
    <Teleport to="body">
      <div
        v-if="isReportAnalyzing"
        class="fixed inset-0 z-[2000] flex items-center justify-center bg-black/40 backdrop-blur-sm"
      >
        <div class="flex items-center gap-2 rounded-xl bg-white/95 px-4 py-3 text-gray-800 shadow-lg">
          <svg class="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 1 1-6.219-8.56" />
          </svg>
          <span class="text-sm font-medium">正在分析报告，请稍候...</span>
        </div>
      </div>
    </Teleport>
    <!-- Functional Buttons -->
    <div class="flex gap-2 mb-4 overflow-x-auto no-scrollbar pb-1">
      <button class="px-4 py-1.5 bg-white border border-gray-200 rounded-full text-sm text-gray-600 hover:bg-gray-50 hover:border-primary/50 transition-colors whitespace-nowrap shadow-sm" @click="handleClick('FoodRecord')">
        三餐分析
      </button>
      <button class="px-4 py-1.5 bg-white border border-gray-200 rounded-full text-sm text-gray-600 hover:bg-gray-50 hover:border-primary/50 transition-colors whitespace-nowrap shadow-sm" @click="handleClick('SnackAnalysis')">
        零食分析
      </button>
      <button class="px-4 py-1.5 bg-white border border-gray-200 rounded-full text-sm text-gray-600 hover:bg-gray-50 hover:border-primary/50 transition-colors whitespace-nowrap shadow-sm" @click="handleClick('MedicationReminder')">
        用药提醒
      </button>
      <button class="px-4 py-1.5 bg-white border border-gray-200 rounded-full text-sm text-gray-600 hover:bg-gray-50 hover:border-primary/50 transition-colors whitespace-nowrap shadow-sm" @click="handleClick('ReportAnalysis')">
        报告分析
      </button>
    </div>

    <!-- Image preview -->
    <div v-if="attachedImage" class="mb-2 flex items-center gap-2">
      <div class="relative inline-block">
        <img :src="attachedImage.preview" class="h-16 w-16 object-cover rounded-lg border border-gray-200" />
        <button
          @click="removeImage"
          class="absolute -top-1.5 -right-1.5 w-5 h-5 bg-gray-600 text-white rounded-full flex items-center justify-center"
        >
          <X :size="12" />
        </button>
      </div>
    </div>

    <!-- Input Area -->
    <div class="bg-white rounded-2xl shadow-sm border border-gray-200 p-2 flex items-end gap-2 focus-within:ring-2 focus-within:ring-primary/20 focus-within:border-primary transition-all">
      <!-- Hidden file input -->
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        class="hidden"
        @change="handleFileChange"
      />

      <!-- Add / Image Button -->
      <button
        @click="triggerFileInput"
        class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition-colors shrink-0"
        title="上传图片"
        :disabled="isSending"
      >
        <Plus :size="24" />
      </button>

      <!-- Text Input -->
      <textarea
        v-model="inputValue"
        placeholder="向AAA提问"
        rows="1"
        :disabled="isSending"
        class="flex-1 max-h-32 py-3 bg-transparent border-none outline-none text-gray-700 resize-none placeholder:text-gray-400 disabled:opacity-60"
        @input="$event.target.style.height = 'auto'; $event.target.style.height = $event.target.scrollHeight + 'px'"
        @keydown="handleKeydown"
      ></textarea>

      <!-- Right Actions -->
      <div class="flex items-center gap-1 pb-1">
        <button class="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-full transition-colors" title="语音输入" :disabled="isSending">
          <Mic :size="22" />
        </button>
        <button
          @click="handleSend"
          class="p-2 bg-primary text-white rounded-full hover:bg-primary/90 transition-colors shadow-sm disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="(!inputValue.trim() && !attachedImage) || isSending"
        >
          <!-- Loading spinner -->
          <svg v-if="isSending" class="animate-spin" :size="20" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 1 1-6.219-8.56" />
          </svg>
          <Send v-else :size="20" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }
</style>
