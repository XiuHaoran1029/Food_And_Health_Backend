<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- Header -->
    <header class="bg-white border-b border-gray-100 px-4 pt-5 py-3 flex items-center shadow-sm sticky top-0 z-10">
      <button
        class="p-2 -ml-2 hover:bg-gray-100 rounded-full text-gray-600 transition-colors"
        @click="goBack"
      >
        <ArrowLeft size="24" />
      </button>
      <h1 class="text-lg font-bold text-gray-800 flex-1 text-center pr-8">三餐分析</h1>
    </header>

    <!-- 表单区域 -->
    <main class="flex-1 p-4 max-w-lg mx-auto w-full">
      <form class="space-y-6" @submit.prevent="onSubmit">
        <!-- 1. Meal Type Selection -->
        <div class="space-y-3">
          <label class="block text-sm font-medium text-gray-700">餐型选择</label>
          <div class="grid grid-cols-3 gap-3">
            <label
              v-for="type in mealTypes"
              :key="type.value"
              class="relative cursor-pointer group"
            >
              <input
                type="radio"
                v-model="form.mealType"
                :value="type.value"
                class="peer sr-only"
              />
              <div class="flex flex-col items-center justify-center p-3 rounded-xl border-2 transition-all duration-200
                border-gray-200 bg-white text-gray-600
                peer-checked:border-primary peer-checked:bg-primary/5 peer-checked:text-primary
                group-hover:border-gray-300 peer-checked:group-hover:border-primary">
                <span class="text-2xl mb-1">{{ type.icon }}</span>
                <span class="text-sm font-medium">{{ type.label }}</span>
              </div>
            </label>
          </div>
        </div>

        <!-- 2. Food Name Input -->
        <div class="space-y-3">
          <label class="block text-sm font-medium text-gray-700">食物名称</label>
          <input
            type="text"
            v-model="form.foodName"
            class="w-full px-4 py-3 rounded-xl border border-gray-200 bg-white text-gray-800 placeholder:text-gray-400
            focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
            placeholder="例如：牛肉面、沙拉..."
            maxlength="50"
          />
        </div>

        <!-- 3. Image Upload -->
        <div class="space-y-3">
          <label class="block text-sm font-medium text-gray-700">食物图片</label>

          <!-- Image Preview -->
          <div v-if="form.imageUrl" class="relative w-full aspect-square sm:aspect-video rounded-xl overflow-hidden shadow-sm border border-gray-200 group">
            <img :src="form.imageUrl" alt="食物预览" class="w-full h-full object-cover" />
            <div class="absolute inset-0 bg-black/0 group-hover:bg-black/10 transition-colors"></div>
            <button
              type="button"
              class="absolute top-3 right-3 bg-black/50 text-white p-2 rounded-full hover:bg-black/70 backdrop-blur-sm transition-colors"
              @click="deleteImage"
            >
              <X size="18" />
            </button>
          </div>

          <!-- Upload Button -->
          <button
            v-else
            type="button"
            class="w-full aspect-video border-2 border-dashed border-gray-300 rounded-xl flex flex-col items-center justify-center text-gray-400
            hover:border-primary hover:text-primary hover:bg-primary/5 transition-all gap-3 bg-white"
            @click="selectImage"
          >
            <CameraIcon size="32" stroke-width="1.5" />
            <span class="text-sm font-medium">拍照或选择图片</span>
          </button>
        </div>

        <!-- Submit Button -->
        <div class="pt-4">
          <button
            type="submit"
            class="w-full py-3.5 bg-primary text-white rounded-xl font-bold shadow-lg shadow-primary/30
            active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:shadow-none"
            :disabled="!form.mealType || !form.foodName || !form.imageUrl || loading"
          >
            {{ loading ? '分析中...' : '提交记录' }}
          </button>
        </div>
      </form>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Camera, CameraResultType } from '@capacitor/camera'
import { X, ArrowLeft, Camera as CameraIcon } from 'lucide-vue-next'
import { showToast } from 'vant'
import { sendMessage } from '@/api/message'
import { createConversation } from '@/api/conversation'
import { useUserStore } from '@/store/user'
import { fileToBase64, compressImage } from '@/utils/helper'

const router = useRouter()
const userStore = useUserStore()

const mealTypes = [
  { value: 'breakfast', label: '早餐', icon: '🍳' },
  { value: 'lunch', label: '午餐', icon: '🍱' },
  { value: 'dinner', label: '晚餐', icon: '🥗' }
]

const form = ref({
  mealType: '',
  foodName: '',
  imageUrl: '',
  imageFile: null  // stores the blob/file for base64 conversion
})

const loading = ref(false)

const goBack = () => {
  if (window.history.state && window.history.state.back) {
    router.back()
  } else {
    router.push('/')
  }
}

function getMealTypeLabel(value) {
  const type = mealTypes.find(t => t.value === value)
  return type ? type.label : ''
}

async function getOrCreateFoodAnalysisConversation() {
  try {
    // 生成唯一的对话标题（包含餐型、食物名称和时间）
    const mealTypeLabel = getMealTypeLabel(form.value.mealType)
    const timestamp = new Date().toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
    const conversationTitle = `${mealTypeLabel} - ${form.value.foodName} (${timestamp})`

    // 每次都创建新会话
    console.log('[FoodRecord] 创建新的对话:', conversationTitle)
    const createRes = await createConversation(userStore.userId, conversationTitle)
    console.log('[FoodRecord] 新会话创建成功:', createRes.data.id)
    return createRes.data.id
  } catch (e) {
    console.error('[FoodRecord] 创建会话失败:', e)
    throw new Error('无法获取会话')
  }
}

const selectImage = async () => {
  try {
    showToast('正在处理图片...')
    
    // 1. 获取图片
    const image = await Camera.getPhoto({
      resultType: CameraResultType.Uri,
      quality: 80,
      allowEditing: true,
      width: 800,
      height: 800
    })
    
    console.log('[FoodRecord] 图片选择成功:', image.webPath)
    
    // 2. 将图片转为 Blob
    const response = await fetch(image.webPath)
    const originalBlob = await response.blob()
    console.log('[FoodRecord] 原始图片大小:', (originalBlob.size / 1024).toFixed(2), 'KB')
    
    // 3. 压缩图片
    console.log('[FoodRecord] 开始压缩图片...')
    const compressedResult = await compressImage(originalBlob, {
      maxWidth: 640,
      maxHeight: 640,
      minSize: 50,
      maxSize: 100
    })

    let finalBlob
    let compressedInfo = null

    // 判断返回类型：如果是字符串 (Base64)，则转换为 Blob
    if (typeof compressedResult === 'string') {
      console.log('[FoodRecord] 检测到返回的是 Base64 字符串，正在转换...')

      // 辅助函数：Base64 转 Blob
      const base64ToBlob = (base64Data, contentType = 'image/jpeg') => {
        const byteString = atob(base64Data.split(',')[1]) // 去掉 "data:image/...;base64," 前缀
        const ab = new ArrayBuffer(byteString.length)
        const ia = new Uint8Array(ab)
        for (let i = 0; i < byteString.length; i++) {
          ia[i] = byteString.charCodeAt(i)
        }
        return new Blob([ab], { type: contentType })
      }

      finalBlob = base64ToBlob(compressedResult)
    } else if (compressedResult instanceof Blob) {
      console.log('[FoodRecord] 检测到返回的是 Blob 对象')
      finalBlob = compressedResult
    } else if (compressedResult && compressedResult.blob) {
      console.log('[FoodRecord] 检测到返回的是 { blob: ... } 对象')
      finalBlob = compressedResult.blob
      compressedInfo = compressedResult
    } else {
      console.error('[FoodRecord] 无法识别的返回格式:', compressedResult)
      showToast('图片处理失败：格式错误')
      return
    }

    // 现在 finalBlob 一定是有效的 Blob 对象了
    form.value.imageFile = finalBlob

// 5. 将压缩后的 Blob 转为预览 URL
    form.value.imageUrl = URL.createObjectURL(finalBlob);

    console.log('[FoodRecord] 图片压缩完成')
    
    // 6. 显示压缩提示
    if (compressedInfo && typeof compressedInfo.compressedSize === 'number') {
      showToast(`图片处理完成`)
    } else {
      showToast('图片已压缩')
    }
  } catch (error) {
    console.error('[FoodRecord] 图片选择失败：', error)
    showToast('图片处理失败')
  }
}

const deleteImage = () => {
  form.value.imageUrl = ''
  form.value.imageFile = null
}

const onSubmit = async () => {
  console.log('[FoodRecord] onSubmit 开始', { form: form.value })

  loading.value = true

  try {
    // 1. 获取图片 Base64
    let imgBase64 = ''
    if (form.value.imageFile) {
      console.log('[FoodRecord] 开始转换图片为 Base64')
      imgBase64 = await fileToBase64(form.value.imageFile)
      console.log('[FoodRecord] 图片转换完成，长度:', imgBase64?.length)
    }

    // 2. 创建新对话
    console.log('[FoodRecord] 准备创建新对话')
    const conversationId = await getOrCreateFoodAnalysisConversation()
    console.log('[FoodRecord] 对话 ID:', conversationId)

    // 3. 调用 AI 分析 API
    const apiPayload = {
      userId: userStore.userId,
      conversationId: conversationId,
      content: form.value.foodName,
      role: form.value.mealType,
      function_type: 'food_analysis',
      img: imgBase64,
      mimeType: ''
    }
    console.log('[FoodRecord] 发送 API 请求')

    const res = await sendMessage(apiPayload)
    console.log('[FoodRecord] API 响应:', res)

    // 4. 跳转到主页面并传递 conversationId
    console.log('[FoodRecord] 跳转到主页面，conversationId:', conversationId)
    await router.push({
      name: 'Home',
      query: {conversationId}
    })

    showToast('分析完成')
  } catch (err) {
    console.error('[FoodRecord] onSubmit 错误:', err)
    console.error('[FoodRecord] 错误详情:', {
      message: err?.message,
      response: err?.response?.data,
      status: err?.response?.status
    })

    showToast(`提交失败：${err?.response?.data?.message || err?.message || '请重试'}`)
  } finally {
    loading.value = false
    console.log('[FoodRecord] onSubmit 结束，loading 设为 false')
  }
}
</script>

<style scoped>
/* Scoped styles can be removed or minimal if using Tailwind completely */
</style>
