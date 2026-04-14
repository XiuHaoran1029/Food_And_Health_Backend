<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import { Check, X } from 'lucide-vue-next'

const props = defineProps({
  show: {
    type: Boolean,
    default: false
  },
  modelValue: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: '选择日期'
  },
  minDate: {
    type: Date,
    default: () => new Date()
  }
})

const emit = defineEmits(['update:show', 'update:modelValue', 'confirm', 'cancel'])

const today = new Date()
const minYear = props.minDate.getFullYear()
const maxYear = minYear + 10

const selectedYear = ref(minYear)
const selectedMonth = ref(props.minDate.getMonth() + 1)
const selectedDay = ref(props.minDate.getDate())

const yearList = computed(() => {
  const years = []
  for (let y = minYear; y <= maxYear; y++) {
    years.push(y)
  }
  return years
})

const monthList = computed(() => {
  const months = []
  const minMonth = selectedYear.value === minYear ? props.minDate.getMonth() + 1 : 1
  for (let m = minMonth; m <= 12; m++) {
    months.push(m)
  }
  return months
})

const dayList = computed(() => {
  const days = []
  const daysInMonth = new Date(selectedYear.value, selectedMonth.value, 0).getDate()
  let minDay = 1
  if (selectedYear.value === minYear && selectedMonth.value === props.minDate.getMonth() + 1) {
    minDay = props.minDate.getDate()
  }
  for (let d = minDay; d <= daysInMonth; d++) {
    days.push(d)
  }
  return days
})

const yearRef = ref(null)
const monthRef = ref(null)
const dayRef = ref(null)
const itemHeight = 44

function scrollToSelected(refEl, list, value) {
  if (!refEl || !list.length) return
  const index = list.indexOf(value)
  if (index >= 0) {
    refEl.scrollTop = index * itemHeight
  }
}

function getScrollIndex(refEl, list) {
  if (!refEl || !list.length) return list[0]
  const scrollTop = refEl.scrollTop
  const index = Math.round(scrollTop / itemHeight)
  return list[Math.max(0, Math.min(index, list.length - 1))]
}

function onYearScroll() {
  const year = getScrollIndex(yearRef.value, yearList.value)
  if (year !== selectedYear.value) {
    selectedYear.value = year
    adjustMonthAndDay()
  }
}

function onMonthScroll() {
  const month = getScrollIndex(monthRef.value, monthList.value)
  if (month !== selectedMonth.value) {
    selectedMonth.value = month
    adjustDay()
  }
}

function onDayScroll() {
  const day = getScrollIndex(dayRef.value, dayList.value)
  if (day !== selectedDay.value) {
    selectedDay.value = day
  }
}

function adjustMonthAndDay() {
  const minMonth = selectedYear.value === minYear ? props.minDate.getMonth() + 1 : 1
  if (selectedMonth.value < minMonth) {
    selectedMonth.value = minMonth
  }
  adjustDay()
}

function adjustDay() {
  const daysInMonth = new Date(selectedYear.value, selectedMonth.value, 0).getDate()
  let minDay = 1
  if (selectedYear.value === minYear && selectedMonth.value === props.minDate.getMonth() + 1) {
    minDay = props.minDate.getDate()
  }
  if (selectedDay.value < minDay) {
    selectedDay.value = minDay
  }
  if (selectedDay.value > daysInMonth) {
    selectedDay.value = daysInMonth
  }
}

function initFromModelValue() {
  if (props.modelValue) {
    const parts = props.modelValue.split('-').map(Number)
    if (parts.length === 3) {
      selectedYear.value = Math.max(parts[0], minYear)
      selectedMonth.value = parts[1]
      selectedDay.value = parts[2]
      adjustMonthAndDay()
    }
  } else {
    selectedYear.value = minYear
    selectedMonth.value = props.minDate.getMonth() + 1
    selectedDay.value = props.minDate.getDate()
  }
  nextTick(() => {
    scrollToSelected(yearRef.value, yearList.value, selectedYear.value)
    scrollToSelected(monthRef.value, monthList.value, selectedMonth.value)
    scrollToSelected(dayRef.value, dayList.value, selectedDay.value)
  })
}

watch(() => props.show, (val) => {
  if (val) {
    initFromModelValue()
  }
})

function onConfirm() {
  const month = String(selectedMonth.value).padStart(2, '0')
  const day = String(selectedDay.value).padStart(2, '0')
  const dateStr = `${selectedYear.value}-${month}-${day}`
  emit('update:modelValue', dateStr)
  emit('confirm', dateStr)
  emit('update:show', false)
}

function onCancel() {
  emit('cancel')
  emit('update:show', false)
}

function onOverlayClick() {
  onCancel()
}
</script>

<template>
  <Teleport to="body">
    <Transition name="picker">
      <div v-if="show" class="fixed inset-0 z-50">
        <div 
          class="absolute inset-0 bg-black/50" 
          @click="onOverlayClick"
        ></div>
        
        <div class="absolute bottom-0 left-0 right-0 bg-white rounded-t-2xl shadow-xl">
          <div class="flex items-center justify-between px-4 py-3 border-b border-gray-100">
            <button 
              class="px-3 py-1 text-gray-500 text-sm active:bg-gray-100 rounded-lg transition-colors"
              @click="onCancel"
            >
              取消
            </button>
            <span class="text-base font-semibold text-gray-800">{{ title }}</span>
            <button 
              class="px-3 py-1 text-primary text-sm font-medium active:bg-primary/10 rounded-lg transition-colors"
              @click="onConfirm"
            >
              确定
            </button>
          </div>
          
          <div class="relative px-2 py-2">
            <div class="absolute left-0 right-0 top-1/2 -translate-y-1/2 h-11 bg-gray-50 rounded-lg pointer-events-none mx-3 border-y border-gray-100"></div>
            
            <div class="flex relative z-10">
              <div 
                ref="yearRef"
                class="flex-1 h-[220px] overflow-y-auto scrollbar-hide"
                style="scroll-snap-type: y mandatory;"
                @scroll="onYearScroll"
              >
                <div class="h-[88px]"></div>
                <div 
                  v-for="year in yearList" 
                  :key="year"
                  class="h-11 flex items-center justify-center text-lg text-gray-700"
                  :class="year === selectedYear ? 'text-gray-900 font-semibold' : ''"
                  style="scroll-snap-align: center;"
                >
                  {{ year }} 年
                </div>
                <div class="h-[88px]"></div>
              </div>
              
              <div 
                ref="monthRef"
                class="flex-1 h-[220px] overflow-y-auto scrollbar-hide"
                style="scroll-snap-type: y mandatory;"
                @scroll="onMonthScroll"
              >
                <div class="h-[88px]"></div>
                <div 
                  v-for="month in monthList" 
                  :key="month"
                  class="h-11 flex items-center justify-center text-lg text-gray-700"
                  :class="month === selectedMonth ? 'text-gray-900 font-semibold' : ''"
                  style="scroll-snap-align: center;"
                >
                  {{ month }} 月
                </div>
                <div class="h-[88px]"></div>
              </div>
              
              <div 
                ref="dayRef"
                class="flex-1 h-[220px] overflow-y-auto scrollbar-hide"
                style="scroll-snap-type: y mandatory;"
                @scroll="onDayScroll"
              >
                <div class="h-[88px]"></div>
                <div 
                  v-for="day in dayList" 
                  :key="day"
                  class="h-11 flex items-center justify-center text-lg text-gray-700"
                  :class="day === selectedDay ? 'text-gray-900 font-semibold' : ''"
                  style="scroll-snap-align: center;"
                >
                  {{ day }} 日
                </div>
                <div class="h-[88px]"></div>
              </div>
            </div>
          </div>
          
          <div class="h-[env(safe-area-inset-bottom,20px)]"></div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.picker-enter-active,
.picker-leave-active {
  transition: opacity 0.3s ease;
}

.picker-enter-active > div:last-child,
.picker-leave-active > div:last-child {
  transition: transform 0.3s ease;
}

.picker-enter-from,
.picker-leave-to {
  opacity: 0;
}

.picker-enter-from > div:last-child,
.picker-leave-to > div:last-child {
  transform: translateY(100%);
}
</style>
