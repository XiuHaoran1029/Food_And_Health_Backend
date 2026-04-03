<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, ChevronLeft, ChevronRight, X, CalendarDays, UtensilsCrossed, Cookie, Soup, Salad } from 'lucide-vue-next'
import { showToast } from 'vant'
import { getMealDay, getMealMonth } from '@/api/meal.js'
import {useUserStore} from "@/store/user.js";
import MarkdownIt from 'markdown-it'

// 新增：初始化 markdown-it 实例
const md = new MarkdownIt({
  html: false, // 禁用 HTML 防 XSS
  breaks: true, // 换行转 <br>
  linkify: true, // 自动识别链接
  typographer: true, // 排版优化
  quotes: '“”‘’' // 中文引号
})

// 新增：封装解析函数，添加多层空值保护
const parseMarkdown = (content) => {
  if (!content) return '<p>继续保持健康饮食习惯。</p>'
  return md.render(content)
}

const router = useRouter()

const today = new Date()
const currentDate = ref(new Date(today.getFullYear(), today.getMonth(), 1))
const selectedDate = ref(formatDate(today))
const recordDays = ref([])
const detailLoading = ref(false)
const monthLoading = ref(false)
const modalVisible = ref(false)
const dayDetail = ref(null)
const dayError = ref('')
const userStore = useUserStore()

const weekLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const currentYear = computed(() => currentDate.value.getFullYear())
const currentMonth = computed(() => currentDate.value.getMonth() + 1)
const currentMonthTitle = computed(() => `${currentYear.value}年${currentMonth.value}月`)
const recordSet = computed(() => new Set(recordDays.value))

const calendarCells = computed(() => buildCalendarCells(currentDate.value))

function pad(value) {
  return String(value).padStart(2, '0')
}

function formatDate(date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function buildCalendarCells(monthDate) {
  const year = monthDate.getFullYear()
  const month = monthDate.getMonth()
  const firstDay = new Date(year, month, 1)
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const startOffset = (firstDay.getDay() + 6) % 7
  const cells = []

  for (let i = 0; i < startOffset; i += 1) {
    cells.push(null)
  }

  for (let day = 1; day <= daysInMonth; day += 1) {
    const date = new Date(year, month, day)
    const dateStr = formatDate(date)
    cells.push({
      day,
      dateStr,
      isToday: dateStr === formatDate(today),
      hasRecord: recordSet.value.has(dateStr),
      isSelected: dateStr === selectedDate.value
    })
  }

  return cells
}

async function loadMonthRecords() {
  monthLoading.value = true
  recordDays.value = [] // 清空旧数据
  console.log(currentYear.value, currentMonth.value,userStore.userId)
  try {
    const res = await getMealMonth(currentYear.value, currentMonth.value,userStore.userId)

    const days = res?.data?.recordDays || []
    recordDays.value = Array.from(new Set(days)).filter(Boolean)
  } catch (error) {
    recordDays.value = []
    showToast(error?.message || '加载月份记录失败')
  } finally {
    monthLoading.value = false
  }
}

async function openDate(dateStr) {
  selectedDate.value = dateStr
  modalVisible.value = true
  detailLoading.value = true
  dayError.value = ''
  dayDetail.value = null
  resetSuggestState()

  try {
    const res = await getMealDay(dateStr,userStore.userId)
    dayDetail.value = res?.data || null
    console.log(dayDetail.value)
  } catch (error) {
    dayError.value = error?.message || '加载当日记录失败'
    showToast(dayError.value)
  } finally {
    detailLoading.value = false
  }
}

function handleDayClick(cell) {
  if (!cell || !cell.dateStr) return
  openDate(cell.dateStr)
}

function changeMonth(delta) {
  const next = new Date(currentDate.value)
  next.setMonth(next.getMonth() + delta)
  currentDate.value = new Date(next.getFullYear(), next.getMonth(), 1)
}

function closeModal() {
  modalVisible.value = false
}

function goBack() {
  if (window.history.state && window.history.state.back) {
    router.back()
  } else {
    router.push('/')
  }
}

function mealIcon(key) {
  if (key === 'breakfast') return Cookie
  if (key === 'lunch') return Soup
  return Salad
}

function mealLabel(key) {
  if (key === 'breakfast') return '早餐'
  if (key === 'lunch') return '午餐'
  return '晚餐'
}

function snackRoleLabel(role) {
  return Number(role) === 0 ? '饮品' : '零食'
}

// 饮食详情弹窗 - 健康建议按餐次独立折叠，默认全部折叠
const showSuggest = ref({
  breakfast: false,
  lunch: false,
  dinner: false
})

function resetSuggestState() {
  showSuggest.value = {
    breakfast: false,
    lunch: false,
    dinner: false
  }
}

function toggleSuggest(mealKey) {
  showSuggest.value[mealKey] = !showSuggest.value[mealKey]
}

watch(currentDate, () => {
  loadMonthRecords()
})

onMounted(async () => {
  await loadMonthRecords()
})
</script>

<template>
  <div class="min-h-screen text-slate-800 bg-[radial-gradient(circle_at_top_left,rgba(22,119,255,0.10),transparent_26%),radial-gradient(circle_at_bottom_right,rgba(16,185,129,0.10),transparent_24%),linear-gradient(180deg,#f8fafc_0%,#eef4ff_100%)]">
    <div class="mx-auto flex min-h-screen max-w-6xl flex-col px-3 py-4 sm:px-4 sm:py-6 lg:px-6">
      <header class="mb-4 sm:mb-6">
        <div class="app-surface-strong rounded-3xl p-4">
          <div class="flex items-center gap-3">
            <button
                class="flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-2xl app-icon-action transition"
                @click="goBack"
                aria-label="返回"
            >
              <ArrowLeft :size="20" />
            </button>
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2 text-emerald-500 mb-1">
                <CalendarDays :size="18" />
                <span class="text-xs font-semibold uppercase tracking-[0.2em]">Meal Calendar</span>
              </div>
              <h1 class="truncate text-xl font-bold text-slate-900 sm:text-2xl">三餐饮食记录日历</h1>
              <p class="mt-1 text-sm text-slate-500">点击日期查看当日饮食记录，绿色日期表示已记录</p>
            </div>
          </div>
        </div>
      </header>

      <main class="flex-1">
        <section class="app-card p-3 sm:p-4 lg:p-5">
          <div class="mb-4 flex items-center justify-between gap-3">
            <button
                class="inline-flex h-11 w-11 items-center justify-center rounded-2xl app-icon-action shadow-sm transition active:scale-95"
                @click="changeMonth(-1)"
                aria-label="上一个月"
            >
              <ChevronLeft :size="20" />
            </button>

            <div class="min-w-0 text-center">
              <div class="text-sm font-medium text-emerald-600">当前月份</div>
              <div class="text-xl font-bold text-slate-900 sm:text-2xl">{{ currentMonthTitle }}</div>
              <div v-if="monthLoading" class="mt-1 text-xs text-slate-400">正在同步月记录...</div>
            </div>

            <button
                class="inline-flex h-11 w-11 items-center justify-center rounded-2xl app-icon-action shadow-sm transition active:scale-95"
                @click="changeMonth(1)"
                aria-label="下一个月"
            >
              <ChevronRight :size="20" />
            </button>
          </div>

          <div class="grid grid-cols-7 gap-1.5 text-center text-xs font-semibold text-slate-400 sm:gap-2 sm:text-sm">
            <div v-for="label in weekLabels" :key="label" class="py-2">{{ label }}</div>
          </div>

          <div class="mt-1 grid grid-cols-7 gap-1.5 sm:gap-2">
            <button
                v-for="(cell, index) in calendarCells"
                :key="cell ? cell.dateStr : `empty-${index}`"
                type="button"
                class="calendar-day group relative flex h-12 items-start justify-start rounded-2xl p-2 text-left transition-all duration-200 sm:h-16 sm:p-3"
                :class="cell ? [
                cell.hasRecord ? 'bg-emerald-500 text-white shadow-lg shadow-emerald-500/20' : 'bg-white text-slate-800 hover:bg-slate-50',
                cell.isToday ? 'ring-2 ring-sky-500 ring-offset-2 ring-offset-white' : '',
                cell.isSelected ? 'scale-[0.98]' : ''
              ] : 'pointer-events-none bg-transparent'"
                :disabled="!cell"
                @click="handleDayClick(cell)"
            >
              <span v-if="cell" class="text-sm font-semibold sm:text-base">{{ cell.day }}</span>
              <span
                  v-if="cell && cell.hasRecord"
                  class="absolute bottom-2 right-2 h-2 w-2 rounded-full bg-white/90 sm:h-2.5 sm:w-2.5"
              ></span>
              <span
                  v-if="cell && cell.isToday"
                  class="absolute left-2 top-2 rounded-full border border-sky-500 px-1.5 py-0.5 text-[10px] font-semibold text-sky-600 bg-white/90"
              >
                今
              </span>
            </button>
          </div>
        </section>
      </main>
    </div>

    <Transition name="meal-modal">
        <div v-if="modalVisible" class="fixed inset-0 z-50 flex items-center justify-center px-3 py-4 sm:px-4">
        <div class="absolute inset-0 bg-slate-900/55 backdrop-blur-sm" @click="closeModal"></div>

        <section class="relative z-10 w-full max-w-4xl overflow-hidden rounded-[28px] app-surface-strong">
          <div class="flex items-start justify-between gap-3 border-b border-slate-200/80 px-4 py-4 sm:px-6">
            <div class="min-w-0">
              <p class="text-xs font-semibold uppercase tracking-[0.25em] text-emerald-500">当日详情</p>
              <h3 class="mt-1 truncate text-xl font-bold text-slate-900 sm:text-2xl">{{ selectedDate }} 饮食记录</h3>
            </div>
            <button
                class="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-full app-icon-action transition"
                @click="closeModal"
                aria-label="关闭弹窗"
            >
              <X :size="20" />
            </button>
          </div>

          <div class="max-h-[75vh] overflow-y-auto px-4 py-4 sm:px-6 sm:py-5">
            <div v-if="detailLoading" class="flex min-h-[280px] items-center justify-center text-slate-500">
              <div class="flex flex-col items-center gap-3">
                <div class="h-10 w-10 animate-spin rounded-full border-4 border-emerald-200 border-t-emerald-500"></div>
                <p>正在加载饮食记录...</p>
              </div>
            </div>

            <div v-else-if="dayError" class="rounded-2xl border border-red-100 bg-red-50 p-4 text-red-600">
              {{ dayError }}
            </div>

            <!-- 核心修改：强化餐食数据的存在性判断，仅渲染有有效数据的餐食 -->
            <div v-else-if="dayDetail" class="space-y-5">
              <!-- 早餐模块：仅当 breakfast 存在且包含核心字段（name/imageUrl 至少一个）时显示 -->
              <div v-if="dayDetail.breakfast && (dayDetail.breakfast.name || dayDetail.breakfast.imageUrl)" class="overflow-hidden rounded-3xl border border-slate-200/80 bg-white/95 shadow-sm">
                <div class="flex items-center gap-3 border-b border-slate-200/80 px-4 py-3 sm:px-5">
                  <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-emerald-100 text-emerald-600">
                    <component :is="mealIcon('breakfast')" :size="18" />
                  </div>
                  <div>
                    <div class="text-sm font-semibold text-slate-500">{{ mealLabel('breakfast') }}</div>
                    <div class="text-base font-bold text-slate-900">{{ dayDetail.breakfast.name || dayDetail.breakfast.comment || '早餐记录' }}</div>
                  </div>
                </div>

                <div class="grid gap-4 p-4 sm:grid-cols-[180px_1fr] sm:p-5">
                  <div class="overflow-hidden rounded-2xl bg-slate-100 shadow-sm">
                    <img
                        :src="`http://123.56.144.72:8080/image/${dayDetail.breakfast.imageUrl}`"
                        :alt="`${mealLabel('breakfast')}图片`"
                        class="h-40 w-full object-cover sm:h-full sm:min-h-[180px]"
                    />
                  </div>

                  <div class="space-y-3">
                    <div>
                      <p class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">名称 / 备注</p>
                      <p class="mt-1 text-sm leading-6 text-slate-700">{{ dayDetail.breakfast.comment || '暂无备注' }}</p>
                    </div>
                    <!-- 健康建议：默认折叠，使用 Markdown 渲染 -->
                    <button
                        @click="toggleSuggest('breakfast')"
                        class="flex items-center justify-between w-full text-left"
                    >
                        <span class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">健康建议</span>
                        <span class="text-xs text-slate-400">{{ showSuggest.breakfast ? '收起 ▲' : '展开 ▼' }}</span>
                    </button>
                    <div
                        v-show="showSuggest.breakfast"
                        class="mt-1 rounded-2xl bg-emerald-50 px-3 py-3 text-sm leading-6 text-emerald-700 markdown-content"
                        v-html="parseMarkdown(dayDetail.breakfast.suggest)"
                    ></div>
                  </div>
                </div>
              </div>

              <!-- 午餐模块：仅当 lunch 存在且包含核心字段（name/imageUrl 至少一个）时显示 -->
              <div v-if="dayDetail.lunch && (dayDetail.lunch.name || dayDetail.lunch.imageUrl)" class="overflow-hidden rounded-3xl border border-slate-200/80 bg-white/95 shadow-sm">
                <div class="flex items-center gap-3 border-b border-slate-200/80 px-4 py-3 sm:px-5">
                  <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-emerald-100 text-emerald-600">
                    <component :is="mealIcon('lunch')" :size="18" />
                  </div>
                  <div>
                    <div class="text-sm font-semibold text-slate-500">{{ mealLabel('lunch') }}</div>
                    <div class="text-base font-bold text-slate-900">{{ dayDetail.lunch.name || dayDetail.lunch.comment || '未命名餐品' }}</div>
                  </div>
                </div>

                <div class="grid gap-4 p-4 sm:grid-cols-[180px_1fr] sm:p-5">
                  <div class="overflow-hidden rounded-2xl bg-slate-100 shadow-sm">
                    <img
                        :src="`http://123.56.144.72:8080/image/${dayDetail.lunch.imageUrl}`"
                        :alt="`${mealLabel('lunch')}图片`"
                        class="h-40 w-full object-cover sm:h-full sm:min-h-[180px]"
                    />
                  </div>

                  <div class="space-y-3">
                    <div>
                      <p class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">名称 / 备注</p>
                      <p class="mt-1 text-sm leading-6 text-slate-700">{{ dayDetail.lunch.comment || '暂无备注' }}</p>
                    </div>
                    <div>
                      <button
                          @click="toggleSuggest('lunch')"
                          class="flex items-center justify-between w-full text-left"
                      >
                        <span class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">健康建议</span>
                        <span class="text-xs text-slate-400">{{ showSuggest.lunch ? '收起 ▲' : '展开 ▼' }}</span>
                      </button>
                      <div
                        v-show="showSuggest.lunch"
                        class="mt-1 rounded-2xl bg-emerald-50 px-3 py-3 text-sm leading-6 text-emerald-700 markdown-content"
                        v-html="parseMarkdown(dayDetail.lunch.suggest)"
                      >
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 晚餐模块：仅当 dinner 存在且包含核心字段（name/imageUrl 至少一个）时显示 -->
              <div v-if="dayDetail.dinner && (dayDetail.dinner.name || dayDetail.dinner.imageUrl)" class="overflow-hidden rounded-3xl border border-slate-200/80 bg-white/95 shadow-sm">
                <div class="flex items-center gap-3 border-b border-slate-200/80 px-4 py-3 sm:px-5">
                  <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-emerald-100 text-emerald-600">
                    <component :is="mealIcon('dinner')" :size="18" />
                  </div>
                  <div>
                    <div class="text-sm font-semibold text-slate-500">{{ mealLabel('dinner') }}</div>
                    <div class="text-base font-bold text-slate-900">{{ dayDetail.dinner.name || dayDetail.dinner.comment || '未命名餐品' }}</div>
                  </div>
                </div>

                <div class="grid gap-4 p-4 sm:grid-cols-[180px_1fr] sm:p-5">
                  <div class="overflow-hidden rounded-2xl bg-slate-100 shadow-sm">
                    <img
                        :src="`http://123.56.144.72:8080/image/${dayDetail.dinner.imageUrl}`"
                        :alt="`${mealLabel('dinner')}图片`"
                        class="h-40 w-full object-cover sm:h-full sm:min-h-[180px]"
                    />
                  </div>

                  <div class="space-y-3">
                    <div>
                      <p class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">名称 / 备注</p>
                      <p class="mt-1 text-sm leading-6 text-slate-700">{{ dayDetail.dinner.comment || '暂无备注' }}</p>
                    </div>
                    <div>
                      <button
                          @click="toggleSuggest('dinner')"
                          class="flex items-center justify-between w-full text-left"
                      >
                        <span class="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">健康建议</span>
                        <span class="text-xs text-slate-400">{{ showSuggest.dinner ? '收起 ▲' : '展开 ▼' }}</span>
                      </button>
                      <div
                        v-show="showSuggest.dinner"
                        class="mt-1 rounded-2xl bg-emerald-50 px-3 py-3 text-sm leading-6 text-emerald-700 markdown-content"
                        v-html="parseMarkdown(dayDetail.dinner.suggest)"
                      >
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 零食模块：仅当 snack 数组有数据时显示 -->
              <section v-if="dayDetail.snack && dayDetail.snack.length > 0" class="rounded-3xl border border-slate-200/80 bg-white/95 p-4 shadow-sm sm:p-5">
                <div class="mb-3 flex items-center gap-2">
                  <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-sky-100 text-sky-600">
                    <UtensilsCrossed :size="18" />
                  </div>
                  <div>
                    <h4 class="text-base font-bold text-slate-900">零食记录</h4>
                    <p class="text-sm text-slate-500">记录餐间补给与加餐情况</p>
                  </div>
                </div>

                <div class="space-y-3">
                  <div v-for="(snack, index) in dayDetail.snack" :key="`${snack.snack_name}-${index}`" class="rounded-2xl border border-slate-200/80 bg-slate-50 p-4">
                    <div class="flex flex-wrap items-start justify-between gap-3">
                      <div class="min-w-0 flex-1">
                        <div class="flex flex-wrap items-center gap-2">
                          <h5 class="truncate text-sm font-bold text-slate-900">{{ snack.snack_name }}</h5>
                          <span class="rounded-full bg-sky-100 px-2.5 py-1 text-[11px] font-semibold text-sky-700">{{ snackRoleLabel(snack.role) }}</span>
                        </div>
                        <p class="mt-1 text-sm text-slate-600">{{ snack.remark || '暂无备注' }}</p>
                      </div>
                      <div class="rounded-2xl bg-white px-3 py-2 text-right shadow-sm border border-slate-200/80">
                        <p class="text-[11px] text-slate-400">数量</p>
                        <p class="text-sm font-semibold text-slate-900">{{ snack.count }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </section>

              <!-- 无任何有效记录时显示空状态 -->
              <div v-if="!dayDetail.breakfast && !dayDetail.lunch && !dayDetail.dinner && (!dayDetail.snack || dayDetail.snack.length === 0)" class="flex min-h-[280px] flex-col items-center justify-center rounded-3xl border border-dashed border-emerald-200 bg-emerald-50/60 px-6 py-10 text-center app-surface">
                <div class="mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-white text-emerald-500 shadow-sm">
                  <CalendarDays :size="28" />
                </div>
                <h4 class="text-lg font-bold text-slate-900">今日无饮食记录</h4>
                <p class="mt-2 max-w-md text-sm leading-6 text-slate-500">
                  当前日期暂无早餐、午餐、晚餐或零食记录。你可以点击其他绿色日期查看已记录内容。
                </p>
              </div>
            </div>
          </div>
        </section>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.calendar-day {
  min-height: 3rem;
}

/* Markdown 解析内容样式 */
:deep(.markdown-content) {
  line-height: 1.7;
}

:deep(.markdown-content p) {
  margin: 0.6em 0;
}
:deep(.markdown-content strong) {
  font-weight: 700;
  color: #059669;
}
:deep(.markdown-content em) {
  font-style: italic;
  color: #047857;
}
:deep(.markdown-content ul),
:deep(.markdown-content ol) {
  padding-left: 1.5em;
  margin: 0.6em 0;
}
:deep(.markdown-content li) {
  margin: 0.4em 0;
}
:deep(.markdown-content a) {
  color: #0ea5e9;
  text-decoration: underline;
  text-underline-offset: 2px;
}
:deep(.markdown-content a:hover) {
  color: #0284c7;
}
:deep(.markdown-content blockquote) {
  border-left: 3px solid #34d399;
  padding-left: 1em;
  margin: 0.8em 0;
  color: #065f46;
  font-style: italic;
}
</style>

