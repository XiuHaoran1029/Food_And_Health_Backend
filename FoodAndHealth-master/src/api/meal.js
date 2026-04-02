import http from './http'

function pad(value) {
  return String(value).padStart(2, '0')
}

function formatDateKey(year, month, day) {
  return `${year}-${pad(month)}-${pad(day)}`
}

function parseDateKey(dateStr) {
  const [year, month, day] = dateStr.split('-').map(Number)
  return new Date(year, month - 1, day)
}

function escapeXml(text = '') {
  return String(text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&apos;')
}

function createIllustration(title, subtitle, fromColor, toColor) {
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 600 360" role="img" aria-label="${escapeXml(title)}">
      <defs>
        <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="${fromColor}" />
          <stop offset="100%" stop-color="${toColor}" />
        </linearGradient>
      </defs>
      <rect width="600" height="360" rx="36" fill="url(#g)" />
      <circle cx="510" cy="92" r="66" fill="rgba(255,255,255,0.16)" />
      <circle cx="110" cy="276" r="88" fill="rgba(255,255,255,0.11)" />
      <circle cx="410" cy="240" r="124" fill="rgba(255,255,255,0.08)" />
      <text x="46" y="118" fill="#fff" font-size="44" font-weight="700" font-family="Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif">${escapeXml(title)}</text>
      <text x="46" y="176" fill="rgba(255,255,255,0.92)" font-size="26" font-family="Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif">${escapeXml(subtitle)}</text>
      <text x="46" y="238" fill="rgba(255,255,255,0.88)" font-size="18" font-family="Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif">健康饮食 · 清新记录</text>
    </svg>
  `

  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}

const mealThemes = {
  breakfast: {
    label: '早餐',
    colors: ['#10b981', '#34d399'],
    names: ['番茄鸡蛋全麦三明治', '牛油果酸奶燕麦碗', '虾仁蔬菜蛋卷', '玉米南瓜鸡胸粥', '香蕉坚果吐司'],
    comments: ['清爽低负担，适合开启一天', '高纤维搭配优质蛋白', '热量友好，营养更均衡', '暖胃又饱腹，元气满满', '简单轻食，工作日也能坚持'],
    suggestions: ['早餐搭配一份水果更完整', '补充蛋白质和膳食纤维', '少油少盐，更适合早晨肠胃', '建议搭配温水和一份蔬果', '稳定血糖，上午状态更好']
  },
  lunch: {
    label: '午餐',
    colors: ['#0ea5e9', '#38bdf8'],
    names: ['糙米鸡胸沙拉碗', '番茄牛腩荞麦面', '清蒸鳕鱼时蔬套餐', '照烧鸡腿藜麦饭', '菌菇豆腐盖饭'],
    comments: ['蛋白质与蔬菜比例均衡', '能量补给充足，午后不容易困', '清淡不油腻，饱腹感更稳', '搭配粗粮更友好', '维持午后专注力的轻盈选择'],
    suggestions: ['午餐建议控制主食份量', '餐后可散步10分钟帮助消化', '注意蔬菜和优质蛋白的搭配', '尽量减少高油高糖酱汁', '均衡饮食，下午更有精神']
  },
  dinner: {
    label: '晚餐',
    colors: ['#8b5cf6', '#a78bfa'],
    names: ['南瓜鸡蛋豆腐汤', '低脂三文鱼蔬菜盘', '菌菇青菜荞麦面', '番茄牛肉炖菜', '清炒时蔬配紫薯'],
    comments: ['晚餐轻一点，身体更轻松', '适合夜间恢复与放松', '清爽少油，睡前负担更小', '温和养胃，比较适合晚上', '控制总量，睡眠质量更友好'],
    suggestions: ['晚餐尽量七分饱', '睡前2小时避免重口味加餐', '适量减少精制碳水', '晚餐搭配汤品更舒适', '轻负担饮食有助于休息']
  }
}

const snackCatalog = [
  { snack_name: '无糖酸奶', remark: '补充优质蛋白', role: 0, count: 250 },
  { snack_name: '柠檬气泡水', remark: '清爽解腻', role: 0, count: 330 },
  { snack_name: '原味坚果', remark: '少量即可', role: 1, count: 1 },
  { snack_name: '海苔脆片', remark: '办公室轻零食', role: 1, count: 1 },
  { snack_name: '苹果', remark: '天然果糖与纤维', role: 1, count: 1 },
  { snack_name: '无糖豆浆', remark: '植物蛋白补给', role: 0, count: 300 },
  { snack_name: '全麦饼干', remark: '控制份量更合适', role: 1, count: 2 },
  { snack_name: '淡茶饮', remark: '餐间提神', role: 0, count: 350 }
]

function pick(list, seed) {
  return list[Math.abs(seed) % list.length]
}

function isToday(dateStr) {
  const today = new Date()
  return formatDateKey(today.getFullYear(), today.getMonth() + 1, today.getDate()) === dateStr
}

function normalizeMonthResponse(payload, year, month) {
  const data = payload?.data ?? payload
  const recordDays = data?.recordDays

  if ((payload?.code === 200 || payload?.code === 0) && Array.isArray(recordDays)) {
    return {
      code: payload.code,
      msg: payload.message || payload.msg || 'success',
      data: {
        year: data.year ?? year,
        month: data.month ?? month,
        recordDays: recordDays.filter(Boolean)
      }
    }
  }

  throw new Error('Invalid month response')
}

function normalizeDayResponse(payload, dateStr) {
  const data = payload?.data ?? payload
  if (payload?.code === 200 || payload?.code === 0) {
    return {
      code: payload.code,
      message: payload.message || payload.msg || 'success',
      data: {
        date: data?.date ?? dateStr,
        breakfast: data?.breakfast ?? null,
        lunch: data?.lunch ?? null,
        dinner: data?.dinner ?? null,
        snack: Array.isArray(data?.snack) ? data.snack : []
      }
    }
  }

  throw new Error('Invalid day response')
}

// 仅保留真实API调用，移除mock数据逻辑
export async function getMealMonth(year, month,userId) {
  try {
    const response = await http.get('/api/meal/month', {
      params: { year, month,userId}
    })
    return normalizeMonthResponse(response, year, month)
  } catch (error) {
    // 真实API调用失败时抛出错误（前端统一处理）
    throw new Error(error?.response?.data?.message || '加载月份记录失败')
  }
}

export async function getMealDay(dateStr,userId) {
  try {
    const response = await http.get('/api/meal/day', {
      params: { dateStr,userId }
    })
    return normalizeDayResponse(response, dateStr)
  } catch (error) {
    throw new Error(error?.response?.data?.message || '加载当日记录失败')
  }
}
