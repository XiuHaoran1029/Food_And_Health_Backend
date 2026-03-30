<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- Header -->
    <header class="bg-white border-b border-gray-100 px-4 py-3 flex items-center shadow-sm sticky top-0 z-10">
      <button
          class="p-2 -ml-2 hover:bg-gray-100 rounded-full text-gray-600 transition-colors"
          @click="goBack"
      >
        <ArrowLeft size="24" />
      </button>
      <h1 class="text-lg font-bold text-gray-800 flex-1 text-center pr-8">添加零食</h1>
    </header>

    <!-- Main Content -->
    <main class="flex-1 p-4 max-w-lg mx-auto w-full">
      <form @submit.prevent="onSubmit" class="space-y-6">
        <!-- 零食名称输入框 -->
        <div class="space-y-3">
          <label class="block text-sm font-medium text-gray-700">零食名称</label>
          <input
              v-model="form.name"
              type="text"
              placeholder="请输入零食名称（如可乐、薯片）"
              class="w-full px-4 py-3 rounded-xl border border-gray-200 bg-white text-gray-800 placeholder:text-gray-400 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
              required
          />
        </div>

        <!-- 零食种类选择 -->
        <div class="space-y-3">
          <label class="block text-sm font-medium text-gray-700">零食种类</label>
          <div class="grid grid-cols-2 gap-3">
            <label
                class="relative cursor-pointer group"
            >
              <input
                  type="radio"
                  name="snackType"
                  value="drink"
                  v-model="form.type"
                  class="peer sr-only"
                  required
              />
              <div class="flex flex-col items-center justify-center p-3 rounded-xl border-2 transition-all duration-200
                border-gray-200 bg-white text-gray-600
                peer-checked:border-primary peer-checked:bg-primary/5 peer-checked:text-primary
                group-hover:border-gray-300 peer-checked:group-hover:border-primary">
                <Coffee size="24" class="mb-1" />
                <span class="text-sm font-medium">饮品</span>
              </div>
            </label>
            <label
                class="relative cursor-pointer group"
            >
              <input
                  type="radio"
                  name="snackType"
                  value="bag"
                  v-model="form.type"
                  class="peer sr-only"
                  required
              />
              <div class="flex flex-col items-center justify-center p-3 rounded-xl border-2 transition-all duration-200
                border-gray-200 bg-white text-gray-600
                peer-checked:border-primary peer-checked:bg-primary/5 peer-checked:text-primary
                group-hover:border-gray-300 peer-checked:group-hover:border-primary">
                <Package size="24" class="mb-1" />
                <span class="text-sm font-medium">袋装零食</span>
              </div>
            </label>
          </div>
        </div>

        <!-- 数量输入框 + 联动单位 -->
        <div class="space-y-3">
          <label class="block text-sm font-medium text-gray-700">数量</label>
          <div class="flex items-center gap-3">
            <input
                v-model.number="form.num"
                type="number"
                min="1"
                step="1"
                placeholder="请输入数量"
                class="flex-1 px-4 py-3 rounded-xl border border-gray-200 bg-white text-gray-800 placeholder:text-gray-400 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
                required
            />
            <!-- 单位展示：根据种类自动切换，禁用状态美化 -->
            <div class="px-4 py-3 rounded-xl bg-gray-50 text-gray-500 border border-gray-200 w-20 text-center font-medium">
              {{ form.type === 'drink' ? 'ml' : form.type === 'bag' ? 'g' : '-' }}
            </div>
          </div>
        </div>

        <div class="space-y-3">
          <label class="block text-sm font-medium text-gray-700">备注</label>
          <input
              v-model="form.remark"
              type="text"
              placeholder="请输入备注，如：如：下午加餐薯片、追剧吃坚果"
              class="w-full px-4 py-3 rounded-xl border border-gray-200 bg-white text-gray-800 placeholder:text-gray-400 focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
              required
          />
        </div>

        <!-- 提交按钮 -->
        <div class="pt-4">
          <button
              type="submit"
              class="w-full py-3.5 bg-primary text-white rounded-xl font-bold shadow-lg shadow-primary/30 
              active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:shadow-none"
              :disabled="!form.name || !form.type || !form.num || loading"
          >
            {{ loading ? '分析中...' : '提交保存' }}
          </button>
        </div>
      </form>

      <!-- AI 分析结果（需求 7.5） -->
      <div v-if="analysisResult" class="mt-6 p-4 bg-white rounded-xl border border-gray-200 shadow-sm space-y-2">
        <h2 class="text-base font-bold text-gray-800">AI 分析结果</h2>
        <p class="text-sm text-gray-700 whitespace-pre-wrap">{{ analysisResult }}</p>
      </div>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import { ArrowLeft, Coffee, Package } from 'lucide-vue-next'
import { showToast } from 'vant'
import { sendMessage } from '@/api/message'
// 引入创建会话的 API (参考代码1)
import { createConversation } from '@/api/conversation'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const goBack = () => {
  if (window.history.state && window.history.state.back) {
    router.back()
  } else {
    router.push('/')
  }
}

// 表单核心数据
const form = ref({
  name: '', // 零食名称
  type: '', // 种类：drink(饮品)/bag(袋装零食)
  num: '' , // 数量
  remark:''
})

const loading = ref(false)
const analysisResult = ref('')

// type value → 中文 label 映射
const typeConfig = {
  drink: { label: '饮品', icon: Coffee },
  bag: { label: '袋装零食', icon: Package }
}

const getTypeLabel = (value) => {
  return typeConfig[value]?.label || ''
}

// 创建零食分析会话 (参考代码1的逻辑)
async function getOrCreateSnackAnalysisConversation() {
  try {
    const typeLabel = getTypeLabel(form.value.type)
    const timestamp = new Date().toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
    // 生成标题：类型 - 名称 (时间)
    const conversationTitle = `${typeLabel} - ${form.value.name} (${timestamp})`

    console.log('[SnackRecord] 创建新的对话:', conversationTitle)
    const createRes = await createConversation(userStore.userId, conversationTitle)
    console.log('[SnackRecord] 新会话创建成功:', createRes.data.id)
    return createRes.data.id
  } catch (e) {
    console.error('[SnackRecord] 创建会话失败:', e)
    throw new Error('无法获取会话')
  }
}

// 表单提交处理函数
const onSubmit = async () => {
  // 基础校验
  if (!form.value.name || !form.value.type || !form.value.num || Number(form.value.num) < 1) {
    showToast('请填写完整的零食信息')
    return
  }

  loading.value = true
  analysisResult.value = ''

  try {
    // 1. 准备数量单位字符串 (原逻辑保留)
    const unit = form.value.type === 'drink' ? 'ml' : 'g'
    const quantityStr = `${form.value.num}`

    // 2. 创建新会话 (参考代码1的核心逻辑)
    console.log('[SnackRecord] 准备创建新对话')
    const conversationId = await getOrCreateSnackAnalysisConversation()
    console.log('[SnackRecord] 对话 ID:', conversationId)

    // 3. 构建 API 请求载荷
    // 【重要】严格保留原目标代码的字段赋值逻辑，未做语义修正
    const apiPayload = {
      userId: userStore.userId,
      conversationId: conversationId,       // 使用新生成的会话ID
      content: form.value.name,             // 原逻辑：内容填名称
      role: getTypeLabel(form.value.type),  // 原逻辑：角色填中文类型
      function_type: 'snack_analysis',
      img: form.value.remark,                 // 【保留原逻辑】将备注放入 img 字段
      mimeType: quantityStr.valueOf()                 // 【保留原逻辑】将数量+单位放入 mimeType 字段
    }

    console.log('[SnackRecord] 发送 API 请求:', apiPayload)

    const res = await sendMessage(apiPayload)
    console.log('[SnackRecord] API 响应:', res)

    // 4. 处理结果
    analysisResult.value = res?.data?.content || res?.data || ''

    showToast('分析完成')

    // 5. 跳转到主页面并传递 conversationId (参考代码1)
    await router.push({
      name: 'Home',
      query: { conversationId }
    })

  } catch (err) {
    // 参考代码1的详细错误日志
    console.error('[SnackRecord] onSubmit 错误:', err)
    console.error('[SnackRecord] 错误详情:', {
      message: err?.message,
      response: err?.response?.data,
      status: err?.response?.status
    })

    const errorMsg = err?.response?.data?.message || err?.message || '提交失败，请重试'
    showToast(errorMsg)
  } finally {
    loading.value = false
    console.log('[SnackRecord] onSubmit 结束')
  }
}
</script>

<style scoped>
/* Scoped styles can be removed or minimal if using Tailwind completely */
</style>
