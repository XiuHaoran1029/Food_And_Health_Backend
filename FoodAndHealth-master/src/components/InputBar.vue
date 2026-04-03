<script setup>
import { Mic, Send, X, Camera } from 'lucide-vue-next'
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
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

const emit = defineEmits(['message-sent', 'message-retracted'])

const router = useRouter()
const userStore = useUserStore()
const inputValue = ref('')
const isSending = ref(false)
const isReportAnalyzing = ref(false)
const attachedImage = ref(null)   // { file, base64, preview }
const fileInput = ref(null)
const textareaRef = ref(null)
let textareaKeydownHandler = null

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
  const draftSnapshot = {
    content,
    img: attachedImage.value ? { ...attachedImage.value } : null
  }
  const tempId = `user_${Date.now()}_${Math.random().toString(16).slice(2)}`

  // Optimistically show user message
  emit('message-sent', {
    id: tempId,
    role: 'user',
    content: content || '',
    img: imgPreview,
    sequence: Date.now(),
    pending: true
  })

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

    inputValue.value = ''
    attachedImage.value = null
    await nextTick(() => {
      textareaRef.value?.focus()
    })
  } catch (e) {
    emit('message-retracted', tempId)
    inputValue.value = draftSnapshot.content
    attachedImage.value = draftSnapshot.img
    await nextTick(() => {
      const el = textareaRef.value
      if (el) {
        el.style.height = 'auto'
        el.style.height = `${el.scrollHeight}px`
        el.focus()
      }
    })
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

onMounted(() => {
  textareaKeydownHandler = (event) => handleKeydown(event)
  textareaRef.value?.addEventListener('keydown', textareaKeydownHandler)
  nextTick(() => {
    textareaRef.value?.focus()
  })
})

onUnmounted(() => {
  if (textareaRef.value && textareaKeydownHandler) {
    textareaRef.value.removeEventListener('keydown', textareaKeydownHandler)
  }
})

watch(inputValue, () => {
  const el = textareaRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${el.scrollHeight}px`
}, { flush: 'post' })
</script>

<template>
  <div class="w-full max-w-4xl mx-auto px-4 pb-4">
    <!-- Overlay for ReportAnalysis -->
    <Teleport to="body">
      <div
        v-if="isReportAnalyzing"
        class="fixed inset-0 z-[2000] flex items-center justify-center bg-slate-950/45 backdrop-blur-sm"
      >
        <div class="flex items-center gap-3 rounded-2xl app-surface-strong px-5 py-4 text-slate-800">
          <svg class="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 1 1-6.219-8.56" />
          </svg>
          <span class="text-sm font-medium">正在分析报告，请稍候...</span>
        </div>
      </div>
    </Teleport>
    <!-- Functional Buttons -->
    <div class="mb-4 rounded-3xl app-surface-strong px-3 py-3">
      <div class="flex items-center justify-between gap-3 mb-3 px-1">
        <div>
          <div class="text-[10px] uppercase tracking-[0.24em] text-slate-400 font-semibold">Quick Actions</div>
          <div class="text-sm font-semibold text-slate-900">快捷入口</div>
        </div>
        <div class="text-xs text-slate-400">支持三餐、零食、用药和报告分析</div>
      </div>
      <div class="flex gap-2 overflow-x-auto no-scrollbar pb-1">
      <button class="px-4 py-2 bg-white border border-slate-200/80 rounded-full text-sm text-slate-600 hover:bg-slate-50 hover:border-primary/50 transition-all whitespace-nowrap shadow-sm active:scale-[0.99]" @click="handleClick('FoodRecord')">
        三餐分析
      </button>
      <button class="px-4 py-2 bg-white border border-slate-200/80 rounded-full text-sm text-slate-600 hover:bg-slate-50 hover:border-primary/50 transition-all whitespace-nowrap shadow-sm active:scale-[0.99]" @click="handleClick('SnackAnalysis')">
        零食分析
      </button>
      <button class="px-4 py-2 bg-white border border-slate-200/80 rounded-full text-sm text-slate-600 hover:bg-slate-50 hover:border-primary/50 transition-all whitespace-nowrap shadow-sm active:scale-[0.99]" @click="handleClick('MedicationReminder')">
        用药提醒
      </button>
      <button class="px-4 py-2 bg-white border border-slate-200/80 rounded-full text-sm text-slate-600 hover:bg-slate-50 hover:border-primary/50 transition-all whitespace-nowrap shadow-sm active:scale-[0.99]" @click="handleClick('ReportAnalysis')">
        报告分析
      </button>
      </div>
    </div>

    <!-- Image preview -->
    <div v-if="attachedImage" class="mb-3 flex items-center gap-3 rounded-2xl app-surface px-3 py-3">
      <div class="relative inline-block shrink-0">
        <img :src="attachedImage.preview" alt="待发送图片预览" class="h-16 w-16 object-cover rounded-2xl ring-1 ring-slate-200" />
        <button
          @click="removeImage"
          class="absolute -top-1.5 -right-1.5 w-6 h-6 bg-slate-700 text-white rounded-full flex items-center justify-center shadow-md"
        >
          <X :size="12" />
        </button>
      </div>
      <div class="min-w-0">
        <div class="text-sm font-semibold text-slate-900">已添加图片</div>
        <div class="text-xs text-slate-500">发送后会自动压缩上传</div>
      </div>
    </div>

    <!-- Input Area -->
    <div class="app-surface-strong rounded-[28px] p-2.5 flex items-end gap-2 transition-all focus-within:border-primary/60">
      <!-- Hidden file input -->
      <input
        id="composer-file-input"
        ref="fileInput"
        type="file"
        accept="image/*"
        class="hidden"
        @change="handleFileChange"
      />

      <!-- Add / Image Button -->
      <label class="composer-icon-btn" @click="triggerFileInput">
        <Camera size="24" />
      </label>

      <!-- Text Input -->
      <textarea
        ref="textareaRef"
        v-model="inputValue"
        placeholder="向康康提问"
        rows="1"
        enterkeyhint="send"
        autocapitalize="off"
        autocomplete="off"
        spellcheck="false"
        :disabled="isSending"
        class="composer-textarea"
      ></textarea>

      <!-- Right Actions -->
      <div class="flex items-center gap-1 pb-1">
        <button type="button" class="composer-icon-btn" title="语音输入">
          <Mic size="22" />
        </button>
        <button
          @click="handleSend"
          type="button"
          class="composer-send-btn"
          :disabled="(!inputValue.trim() && !attachedImage) || isSending"
        >
          <!-- Loading spinner -->
          <svg v-if="isSending" class="animate-spin" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12a9 9 0 1 1-6.219-8.56" />
          </svg>
          <Send v-else size="20" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.no-scrollbar::-webkit-scrollbar { display: none; }
.no-scrollbar { -ms-overflow-style: none; scrollbar-width: none; }

.composer-textarea::placeholder {
  color: #94a3b8;
}

.composer-textarea:disabled {
  opacity: 0.6;
}

.composer-textarea {
  flex: 1;
  min-height: 52px;
  max-height: 128px;
  padding: 12px 0;
  background: transparent;
  border: none;
  outline: none;
  color: #334155;
  resize: none;
  line-height: 1.5;
}

.composer-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  padding: 0;
  border: 1px solid rgba(226, 232, 240, 0.9);
  border-radius: 9999px;
  background: #f8fafc;
  color: #64748b;
  transition: all 180ms ease;
}

.composer-icon-btn:hover {
  background: #eff6ff;
  color: #1d4ed8;
  border-color: rgba(191, 219, 254, 0.95);
}

.composer-send-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  padding: 0;
  border: none;
  border-radius: 9999px;
  background: linear-gradient(135deg, #1677ff 0%, #3b82f6 100%);
  color: #fff;
  box-shadow: 0 14px 30px rgba(22, 119, 255, 0.24);
  transition: all 180ms ease;
}

.composer-send-btn:hover {
  filter: brightness(1.02);
}

.composer-send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  box-shadow: none;
}
</style>
