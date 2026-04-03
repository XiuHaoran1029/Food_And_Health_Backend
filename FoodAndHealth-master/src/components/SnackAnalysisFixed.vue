<template>
  <div class="min-h-screen flex flex-col bg-[radial-gradient(circle_at_top_left,rgba(22,119,255,0.12),transparent_28%),radial-gradient(circle_at_bottom_right,rgba(16,185,129,0.08),transparent_24%),linear-gradient(180deg,#f8fafc_0%,#eef4ff_100%)]">
    <header class="app-surface-strong border-b border-white/70 px-4 py-3 flex items-center sticky top-0 z-10">
      <button class="p-2 -ml-2 rounded-full app-icon-action transition-colors" @click="goBack">
        <ArrowLeft size="24" />
      </button>
      <h1 class="text-lg font-bold text-slate-900 flex-1 text-center pr-8 tracking-wide">添加零食</h1>
    </header>

    <main class="flex-1 p-4 max-w-lg mx-auto w-full">
      <form @submit.prevent="onSubmit" class="space-y-6">
        <div class="app-card p-5 sm:p-6 space-y-6">
          <div class="space-y-3">
            <label class="block text-sm font-medium text-slate-700">零食名称</label>
            <input v-model="form.name" type="text" placeholder="请输入零食名称（如可乐、薯片）" class="app-input w-full px-4 py-3 rounded-2xl" autofocus required />
          </div>

          <div class="space-y-3">
            <label class="block text-sm font-medium text-slate-700">零食种类</label>
            <div class="grid grid-cols-2 gap-3">
              <label class="relative cursor-pointer group">
                <input type="radio" name="snackType" value="drink" v-model="form.type" class="peer sr-only" required />
                <span class="flex flex-col items-center justify-center p-3 rounded-2xl border transition-all duration-200 app-surface text-slate-500 peer-checked:border-primary peer-checked:bg-primary/10 peer-checked:text-primary group-hover:border-slate-300 peer-checked:group-hover:border-primary">
                  <Coffee size="24" class="mb-1" />
                  <span class="text-sm font-medium">饮品</span>
                </span>
              </label>
              <label class="relative cursor-pointer group">
                <input type="radio" name="snackType" value="bag" v-model="form.type" class="peer sr-only" required />
                <span class="flex flex-col items-center justify-center p-3 rounded-2xl border transition-all duration-200 app-surface text-slate-500 peer-checked:border-primary peer-checked:bg-primary/10 peer-checked:text-primary group-hover:border-slate-300 peer-checked:group-hover:border-primary">
                  <Package size="24" class="mb-1" />
                  <span class="text-sm font-medium">袋装零食</span>
                </span>
              </label>
            </div>
          </div>

          <div class="space-y-3">
            <label class="block text-sm font-medium text-slate-700">数量</label>
            <div class="flex items-center gap-3">
              <input v-model.number="form.num" type="number" min="1" step="1" placeholder="请输入数量" class="app-input flex-1 px-4 py-3 rounded-2xl" required />
              <div class="px-4 py-3 rounded-2xl bg-slate-50 text-slate-500 border border-slate-200/80 w-20 text-center font-medium">
                {{ form.type === 'drink' ? 'ml' : form.type === 'bag' ? 'g' : '-' }}
              </div>
            </div>
          </div>

          <div class="space-y-3">
            <label class="block text-sm font-medium text-slate-700">备注</label>
            <input v-model="form.remark" type="text" placeholder="请输入备注，如：下午加餐薯片、追剧吃坚果" class="app-input w-full px-4 py-3 rounded-2xl" required />
          </div>

          <div class="pt-4">
            <button type="submit" class="w-full py-3.5 rounded-2xl font-bold app-primary-action active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:shadow-none" :disabled="!form.name || !form.type || !form.num || loading">
              {{ loading ? '分析中...' : '提交保存' }}
            </button>
          </div>

          <div v-if="analysisResult" class="mt-6 p-4 app-surface rounded-2xl space-y-2">
            <h2 class="text-base font-bold text-slate-900">AI 分析结果</h2>
            <p class="text-sm text-slate-700 whitespace-pre-wrap leading-7">{{ analysisResult }}</p>
          </div>
        </div>
      </form>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Coffee, Package } from 'lucide-vue-next'
import { showToast } from 'vant'
import { sendMessage } from '@/api/message'
import { createConversation } from '@/api/conversation'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  name: '',
  type: '',
  num: '',
  remark: ''
})

const loading = ref(false)
const analysisResult = ref('')

const goBack = () => {
  if (window.history.state && window.history.state.back) {
    router.back()
  } else {
    router.push('/')
  }
}

const getTypeLabel = (value) => (value === 'drink' ? '饮品' : value === 'bag' ? '袋装零食' : '')

async function getOrCreateSnackAnalysisConversation() {
  const typeLabel = getTypeLabel(form.value.type)
  const timestamp = new Date().toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
  const conversationTitle = `${typeLabel} - ${form.value.name} (${timestamp})`
  const createRes = await createConversation(userStore.userId, conversationTitle)
  return createRes.data.id
}

const onSubmit = async () => {
  if (!form.value.name || !form.value.type || !form.value.num || Number(form.value.num) < 1) {
    showToast('请填写完整的零食信息')
    return
  }

  loading.value = true
  analysisResult.value = ''

  try {
    const quantityStr = `${form.value.num}`
    const conversationId = await getOrCreateSnackAnalysisConversation()

    const apiPayload = {
      userId: userStore.userId,
      conversationId,
      content: form.value.name,
      role: getTypeLabel(form.value.type),
      function_type: 'snack_analysis',
      img: form.value.remark,
      mimeType: quantityStr.valueOf()
    }

    const res = await sendMessage(apiPayload)
    analysisResult.value = res?.data?.content || res?.data || ''

    showToast('分析完成')
    await router.push({ name: 'Home', query: { conversationId } })
  } catch (err) {
    showToast(err?.response?.data?.message || err?.message || '提交失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* Tailwind-only styling */
</style>

