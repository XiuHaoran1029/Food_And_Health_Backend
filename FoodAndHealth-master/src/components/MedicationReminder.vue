<script setup>
import { computed, nextTick, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, X, Pill, Calendar, Clock, Check } from 'lucide-vue-next'
import { showToast } from 'vant'

import WheelDatePicker from './WheelDatePicker.vue'
import { sendMedicineRecord } from '@/api/medicine'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  medicineName: '',
  takeTimes: '',
  singleDosage: '',
  stopTime: '' // 存储格式: 'YYYY-MM-DD'
})

const touched = ref({
  medicineName: false,
  takeTimes: false,
  singleDosage: false,
  stopTime: false
})

const commonMedicines = [
  '阿莫西林', '头孢克肟', '布洛芬', '对乙酰氨基酚', '奥美拉唑',
  '氯雷他定', '西替利嗪', '甲硝唑', '蒙脱石散', '维生素C',
  '复方甘草片', '氨溴索', '阿昔洛韦', '二甲双胍', '阿托伐他汀'
]

const suggestionsOpen = ref(false)
const suggestionsLoading = ref(false)
const medicineFocused = ref(false)

const showDatePopup = ref(false)

const todayStr = computed(() => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
})

const filteredSuggestions = computed(() => {
  const q = form.value.medicineName.trim().toLowerCase()
  if (!q) return []
  return commonMedicines
      .filter((name) => name.toLowerCase().includes(q))
      .slice(0, 8)
})

function goBack() {
  if (window.history.state && window.history.state.back) {
    router.back()
  } else {
    // 返回主页面时，确保状态正确重置
    router.push('/')
    // 可以在这里添加状态重置逻辑，如果需要的话
  }
}

function markTouched(key) {
  touched.value[key] = true
}

function sanitizePositiveInt(input) {
  const raw = String(input ?? '').replace(/[^\d]/g, '')
  if (!raw) return ''
  const n = Number(raw)
  if (!Number.isFinite(n) || n <= 0) return ''
  return String(Math.floor(n))
}

function onTakeTimesInput(e) {
  form.value.takeTimes = sanitizePositiveInt(e?.target?.value)
  touched.value.takeTimes = true
}

function onSingleDosageInput(e) {
  form.value.singleDosage = sanitizePositiveInt(e?.target?.value)
  touched.value.singleDosage = true
}

const errors = computed(() => {
  const e = {
    medicineName: '',
    takeTimes: '',
    singleDosage: '',
    stopTime: ''
  }

  const name = form.value.medicineName.trim()
  if (!name) e.medicineName = '请输入药品名称'

  const daily = Number(form.value.takeTimes)
  if (!form.value.takeTimes) e.takeTimes = '请输入每日次数'
  else if (!Number.isInteger(daily) || daily <= 0) e.takeTimes = '需为正整数'

  const dose = Number(form.value.singleDosage)
  if (!form.value.singleDosage) e.singleDosage = '请输入单次粒数'
  else if (!Number.isInteger(dose) || dose <= 0) e.singleDosage = '需为正整数'

  const end = form.value.stopTime
  if (!end) e.stopTime = '请选择停药日期'
  else if (end < todayStr.value) e.stopTime = '停药日期不能早于今天'

  return e
})

const canSave = computed(() => {
  return !errors.value.medicineName && !errors.value.takeTimes && !errors.value.singleDosage && !errors.value.stopTime
})

function onPickSuggestion(name) {
  form.value.medicineName = name
  suggestionsOpen.value = false
  medicineFocused.value = false
  nextTick(() => {
    const el = document.getElementById('daily-times-input')
    if (el) el.focus()
  })
}

function onMedicineFocus() {
  medicineFocused.value = true
  suggestionsOpen.value = true
}

function onMedicineBlur() {
  medicineFocused.value = false
  window.setTimeout(() => {
    suggestionsOpen.value = false
  }, 120)
}

function onClearMedicine() {
  form.value.medicineName = ''
  suggestionsOpen.value = false
  markTouched('medicineName')
  nextTick(() => {
    const el = document.getElementById('medicine-name-input')
    if (el) el.focus()
  })
}

// 打开日期选择器
function openDatePicker() {
  markTouched('stopTime')
  showDatePopup.value = true
}

// 确认选择日期
function onDateConfirm(dateStr) {
  form.value.stopTime = dateStr
  showDatePopup.value = false
}

async function onSave() {
  if (!userStore.userId) {
    showToast('请先登录')
    return
  }

  touched.value = {
    medicineName: true,
    takeTimes: true,
    singleDosage: true,
    stopTime: true
  }

  if (!canSave.value) return

  const payload = {
    ...form.value,
    takeTimes: Number(form.value.takeTimes),
    singleDosage: Number(form.value.singleDosage),
    createdAt: new Date().toISOString()
  }

  try {
    const key = 'medicationPlans'
    const current = JSON.parse(localStorage.getItem(key) || '[]')
    const next = Array.isArray(current) ? [...current, payload] : [payload]
    localStorage.setItem(key, JSON.stringify(next))
  } catch {
    // ignore storage errors
  }

  try {

    await sendMedicineRecord({
      userId: userStore.userId,
      medicineName: form.value.medicineName,
      takeTimes: Number(form.value.takeTimes),
      singleDosage: Number(form.value.singleDosage),
      stopTime: form.value.stopTime
    })
    console.log("发送消息")
  } catch (err) {
    console.error('Save failed', err)
  }

  showToast('添加成功')
  goBack()
}

watch(
    () => form.value.medicineName,
    async (val) => {
      if (!medicineFocused.value) return
      if (!val.trim()) {
        suggestionsOpen.value = false
        return
      }
      suggestionsLoading.value = true
      await new Promise((r) => window.setTimeout(r, 80))
      suggestionsLoading.value = false
      suggestionsOpen.value = true
    }
)
</script>

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
      <h1 class="text-lg font-bold text-gray-800 flex-1 text-center pr-8">添加用药计划</h1>
    </header>

    <!-- Main Content -->
    <main class="flex-1 p-4 max-w-lg mx-auto w-full space-y-6">

      <!-- Form Card -->
      <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-5 space-y-6">
        <h2 class="font-bold text-gray-800 flex items-center gap-2 border-b border-gray-100 pb-3">
          <Pill size="20" class="text-primary" />
          用药信息
        </h2>

        <!-- Medicine Name -->
        <div class="space-y-2 relative">
          <label class="block text-sm font-medium text-gray-700">
            药品名称 <span class="text-red-500">*</span>
          </label>

          <div class="relative">
            <input
                id="medicine-name-input"
                v-model="form.medicineName"
                type="text"
                autocomplete="off"
                class="w-full px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-gray-800 placeholder:text-gray-400 focus:bg-white focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all"
                :class="touched.medicineName && errors.medicineName ? 'border-red-400 focus:border-red-400 focus:ring-red-100' : ''"
                placeholder="请输入药品通用名 / 商品名"
                @focus="onMedicineFocus"
                @blur="onMedicineBlur"
                @input="markTouched('medicineName')"
            />

            <button
                v-if="form.medicineName"
                type="button"
                class="absolute right-3 top-1/2 -translate-y-1/2 p-1 rounded-full text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors"
                @click="onClearMedicine"
            >
              <X size="16" />
            </button>
          </div>

          <!-- Suggestions Dropdown -->
          <div
              v-if="suggestionsOpen && (filteredSuggestions.length || suggestionsLoading)"
              class="absolute left-0 right-0 top-full mt-1 bg-white border border-gray-100 rounded-xl shadow-xl overflow-hidden z-20"
          >
            <div v-if="suggestionsLoading" class="px-4 py-3 text-sm text-gray-500 flex items-center gap-2">
              <div class="w-4 h-4 border-2 border-primary border-t-transparent rounded-full animate-spin"></div>
              正在联想...
            </div>
            <button
                v-for="name in filteredSuggestions"
                :key="name"
                type="button"
                class="w-full text-left px-4 py-3 text-sm text-gray-700 hover:bg-gray-50 active:bg-gray-100 transition-colors flex items-center justify-between group"
                @pointerdown.prevent="onPickSuggestion(name)"
            >
              {{ name }}
              <Check size="14" class="opacity-0 group-hover:opacity-100 text-primary transition-opacity" />
            </button>
          </div>

          <p v-if="touched.medicineName && errors.medicineName" class="text-xs text-red-500 flex items-center gap-1">
            {{ errors.medicineName }}
          </p>
        </div>

        <!-- Frequency and Dose Row -->
        <div class="grid grid-cols-2 gap-4">
          <!-- Daily Times -->
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">
              每日次数 <span class="text-red-500">*</span>
            </label>
            <div class="relative">
              <input
                  id="daily-times-input"
                  :value="form.takeTimes"
                  type="number"
                  inputmode="numeric"
                  class="w-full px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-gray-800 placeholder:text-gray-400 focus:bg-white focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all pr-10"
                  :class="touched.takeTimes && errors.takeTimes ? 'border-red-400 focus:border-red-400 focus:ring-red-100' : ''"
                  placeholder="0"
                  @input="onTakeTimesInput"
                  @blur="markTouched('takeTimes')"
              />
              <span class="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 text-sm">次</span>
            </div>
          </div>

          <!-- Dose Pills -->
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">
              单次粒数 <span class="text-red-500">*</span>
            </label>
            <div class="relative">
              <input
                  :value="form.singleDosage"
                  type="number"
                  inputmode="numeric"
                  class="w-full px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-gray-800 placeholder:text-gray-400 focus:bg-white focus:border-primary focus:ring-2 focus:ring-primary/20 outline-none transition-all pr-10"
                  :class="touched.singleDosage && errors.singleDosage ? 'border-red-400 focus:border-red-400 focus:ring-red-100' : ''"
                  placeholder="0"
                  @input="onSingleDosageInput"
                  @blur="markTouched('singleDosage')"
              />
              <span class="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 text-sm">粒</span>
            </div>
          </div>
        </div>

        <div v-if="(touched.takeTimes && errors.takeTimes) || (touched.singleDosage && errors.singleDosage)" class="text-xs text-red-500">
          {{ errors.takeTimes || errors.singleDosage }}
        </div>

        <!-- End Date (Roller Style) -->
        <div class="space-y-2">
          <label class="block text-sm font-medium text-gray-700">
            用药截止时间 <span class="text-red-500">*</span>
          </label>
          <div
              class="relative flex items-center w-full px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 cursor-pointer transition-all"
              :class="[
              touched.stopTime && errors.stopTime ? 'border-red-400 bg-red-50' : '',
              !touched.stopTime || !errors.stopTime ? 'hover:bg-gray-100 active:bg-gray-200' : ''
            ]"
              @click="openDatePicker"
          >
            <Calendar size="18" class="text-gray-400 mr-3 shrink-0" />
            <span
                class="flex-1 text-gray-800"
                :class="!form.stopTime ? 'text-gray-400' : ''"
            >
              {{ form.stopTime || '请选择停药日期' }}
            </span>
          </div>
          <p v-if="touched.stopTime && errors.stopTime" class="text-xs text-red-500">
            {{ errors.stopTime }}
          </p>
        </div>

        <div class="bg-blue-50 text-blue-600 text-xs p-3 rounded-xl leading-relaxed flex items-start gap-2">
          <Clock size="14" class="mt-0.5 shrink-0" />
          请准确填写用药信息，避免用药错误；长期用药可选择最远日期。
        </div>

      </div>

      <!-- Action Button -->
      <button
          type="button"
          class="w-full py-3.5 bg-primary text-white rounded-xl font-bold shadow-lg shadow-primary/30 active:scale-[0.98] transition-all disabled:opacity-50 disabled:shadow-none disabled:active:scale-100 flex items-center justify-center gap-2"
          :disabled="!canSave"
          @click="onSave"
      >
        <Check size="20" />
        保存计划
      </button>

      <!-- Safe Area -->
      <div class="h-6"></div>
    </main>

    <!-- Date Picker Popup (Wheel Style) -->
    <WheelDatePicker
      v-model:show="showDatePopup"
      v-model="form.stopTime"
      title="选择停药日期"
      :min-date="new Date()"
      @confirm="onDateConfirm"
    />
  </div>
</template>

<style scoped>
/* Tailwind CSS + 自定义组件样式 */
</style>
