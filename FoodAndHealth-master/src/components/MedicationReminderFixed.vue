<template>
  <div class="min-h-screen flex flex-col bg-[radial-gradient(circle_at_top_left,rgba(22,119,255,0.12),transparent_28%),radial-gradient(circle_at_bottom_right,rgba(16,185,129,0.08),transparent_24%),linear-gradient(180deg,#f8fafc_0%,#eef4ff_100%)]">
    <header class="app-surface-strong border-b border-white/70 px-4 pt-5 py-3 flex items-center sticky top-0 z-10">
      <button class="p-2 -ml-2 rounded-full app-icon-action transition-colors" @click="goBack">
        <ArrowLeft size="24" />
      </button>
      <h1 class="text-lg font-bold text-slate-900 flex-1 text-center pr-8 tracking-wide">添加用药计划</h1>
    </header>

    <main class="flex-1 p-4 max-w-lg mx-auto w-full space-y-6">
      <div class="app-card p-5 space-y-6">
        <h2 class="font-bold text-slate-900 flex items-center gap-2 border-b border-slate-200/80 pb-3">
          <Pill size="20" class="text-primary" />
          用药信息
        </h2>

        <div class="space-y-2">
          <label class="block text-sm font-medium text-slate-700">药品名称 <span class="text-red-500">*</span></label>
          <input
            v-model="form.medicineName"
            type="text"
            class="app-input w-full px-4 py-3 rounded-2xl"
            placeholder="请输入药品通用名 / 商品名"
            autofocus
          />
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <label class="block text-sm font-medium text-slate-700">每日次数 <span class="text-red-500">*</span></label>
            <input
              v-model="form.takeTimes"
              type="number"
              min="1"
              inputmode="numeric"
              class="app-input w-full px-4 py-3 rounded-2xl"
              placeholder="0"
            />
          </div>

          <div class="space-y-2">
            <label class="block text-sm font-medium text-slate-700">单次粒数 <span class="text-red-500">*</span></label>
            <input
              v-model="form.singleDosage"
              type="number"
              min="1"
              inputmode="numeric"
              class="app-input w-full px-4 py-3 rounded-2xl"
              placeholder="0"
            />
          </div>
        </div>

        <div class="space-y-2">
          <label class="block text-sm font-medium text-slate-700">用药截止时间 <span class="text-red-500">*</span></label>
          <button
            type="button"
            class="w-full flex items-center gap-3 px-4 py-3 rounded-2xl border border-slate-200/80 bg-slate-50 text-left text-slate-700 hover:bg-slate-100 transition-colors"
            @click="showDatePopup = true"
          >
            <CalendarIcon size="18" class="text-slate-400 shrink-0" />
            <span class="flex-1" :class="!form.stopTime ? 'text-slate-400' : ''">
              {{ form.stopTime || '请选择停药日期' }}
            </span>
          </button>
        </div>

        <div class="app-surface rounded-2xl p-3 text-xs leading-relaxed text-slate-600 flex items-start gap-2">
          <Clock size="14" class="mt-0.5 shrink-0" />
          请准确填写用药信息，避免用药错误；长期用药可选择最远日期。
        </div>
      </div>

      <button
        type="button"
        class="w-full py-3.5 rounded-2xl font-bold app-primary-action active:scale-[0.98] transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:shadow-none flex items-center justify-center gap-2"
        :disabled="!canSave || isSaving"
        @click="onSave"
      >
        <Check size="20" />
        {{ isSaving ? '保存中...' : '保存计划' }}
      </button>

      <div class="h-6"></div>
    </main>

    <WheelDatePicker
      v-model:show="showDatePopup"
      v-model="form.stopTime"
      title="选择停药日期"
      :min-date="new Date()"
      @confirm="onDateConfirm"
    />
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Pill, Calendar as CalendarIcon, Clock, Check } from 'lucide-vue-next'
import { Capacitor } from '@capacitor/core'
import { showToast } from 'vant'
import { CapacitorCalendar } from '@ebarooni/capacitor-calendar'
import WheelDatePicker from './WheelDatePicker.vue'
import { sendMedicineRecord } from '@/api/medicine'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  medicineName: '',
  takeTimes: '',
  singleDosage: '',
  stopTime: ''
})
const showDatePopup = ref(false)
const isSaving = ref(false)

const todayStr = computed(() => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
})

const canSave = computed(() => {
  const daily = Number(form.value.takeTimes)
  const dose = Number(form.value.singleDosage)
  return !!form.value.medicineName.trim() && Number.isInteger(daily) && daily > 0 && Number.isInteger(dose) && dose > 0 && !!form.value.stopTime && form.value.stopTime >= todayStr.value
})

function goBack() {
  if (window.history.state && window.history.state.back) {
    router.back()
  } else {
    router.push('/')
  }
}

function onDateConfirm(dateStr) {
  form.value.stopTime = dateStr
  showDatePopup.value = false
}

const sleep = (ms) => new Promise((resolve) => window.setTimeout(resolve, ms))

function toTimestamp(dateLike) {
  const date = dateLike instanceof Date ? dateLike : new Date(dateLike)
  return date.getTime()
}

async function ensureCalendarPermission() {
  if (!Capacitor.isNativePlatform()) {
    showToast('请在手机端使用日历提醒功能')
    return false
  }

  try {
    const current = await CapacitorCalendar.checkPermission({ scope: 'writeCalendar' })
    if (current.result === 'granted') return true
    const requested = await CapacitorCalendar.requestFullCalendarAccess()
    return requested.result === 'granted'
  } catch (error) {
    showToast('日历权限申请失败，请检查系统权限设置')
    return false
  }
}

async function syncMedicationReminderToCalendar() {
  const hasPermission = await ensureCalendarPermission()
  if (!hasPermission) return false

  const times = Number(form.value.takeTimes)
  let scheduleTimes = []
  if (times === 1) {
    scheduleTimes = [{ h: 8, m: 0 }]
  } else if (times === 2) {
    scheduleTimes = [{ h: 8, m: 0 }, { h: 17, m: 30 }]
  } else {
    scheduleTimes = [{ h: 8, m: 0 }, { h: 11, m: 30 }, { h: 17, m: 30 }]
  }

  const reminderEnd = new Date(`${form.value.stopTime}T23:59:59`)

  for (const time of scheduleTimes) {
    const startDate = new Date()
    startDate.setHours(time.h, time.m, 0, 0)
    if (startDate.getTime() < Date.now()) {
      startDate.setDate(startDate.getDate() + 1)
    }
    const endDate = new Date(startDate.getTime() + 5 * 60 * 1000)

    try {
      await CapacitorCalendar.createEvent({
        title: `💊 服用 ${form.value.medicineName}`,
        description: `每日 ${form.value.takeTimes} 次，每次 ${form.value.singleDosage} 粒，截止 ${form.value.stopTime}`,
        startDate: toTimestamp(startDate),
        endDate: toTimestamp(endDate),
        alerts: [-5, 0],
        recurrence: {
          frequency: 'daily',
          interval: 1,
          end: toTimestamp(reminderEnd)
        }
      })
    } catch (err) {
      console.error('Calendar add error', err)
      return false
    }
  }

  return true
}

async function onSave() {
  if (isSaving.value) return
  if (!userStore.userId) {
    showToast('请先登录')
    return
  }

  if (!canSave.value) return

  isSaving.value = true
  try {
    const payload = {
      medicineName: form.value.medicineName.trim(),
      takeTimes: Number(form.value.takeTimes),
      singleDosage: Number(form.value.singleDosage),
      stopTime: form.value.stopTime,
      createdAt: new Date().toISOString()
    }

    try {
      const key = 'medicationPlans'
      const current = JSON.parse(localStorage.getItem(key) || '[]')
      const next = Array.isArray(current) ? [...current, payload] : [payload]
      localStorage.setItem(key, JSON.stringify(next))
    } catch (e) {
      console.warn('LocalStorage save failed', e)
    }

    try {
      await sendMedicineRecord({
        userId: userStore.userId,
        medicineName: payload.medicineName,
        takeTimes: payload.takeTimes,
        singleDosage: payload.singleDosage,
        stopTime: payload.stopTime
      })
    } catch (err) {
      console.error('API Save failed', err)
    }

    const synced = await syncMedicationReminderToCalendar()
    showToast(synced ? '添加成功，已同步日历提醒' : '添加成功，但未同步日历提醒')
    await sleep(700)
    goBack()
  } catch (error) {
    showToast('添加成功，但未写入系统日历')
    await sleep(700)
    goBack()
  } finally {
    isSaving.value = false
  }
}
</script>

<style scoped>
/* Tailwind-only styling */
</style>

