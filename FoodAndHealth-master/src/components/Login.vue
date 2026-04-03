<template>
  <div class="min-h-screen flex flex-col bg-[radial-gradient(circle_at_top_left,rgba(22,119,255,0.12),transparent_28%),radial-gradient(circle_at_bottom_right,rgba(16,185,129,0.08),transparent_24%),linear-gradient(180deg,#f8fafc_0%,#eef4ff_100%)]">
    <!-- Header -->
    <header class="app-surface-strong border-b border-white/70 px-4 py-3 flex items-center sticky top-0 z-10">
      <h1 class="text-lg font-bold text-slate-900 flex-1 text-center tracking-wide">
        {{ mode === 'login' ? '用户登录' : '注册账户' }}
      </h1>
    </header>

    <!-- Main Content -->
    <main class="flex-1 px-4 py-6 max-w-lg mx-auto w-full flex flex-col justify-center">
      <!-- Logo/Branding -->
      <div class="text-center mb-8">
        <div class="w-24 h-24 mx-auto mb-5 rounded-[28px] app-surface-strong flex items-center justify-center text-primary shadow-[0_16px_40px_rgba(22,119,255,0.16)]">
          <UserCircle size="52" />
        </div>
        <div class="inline-flex items-center gap-2 rounded-full border border-white/80 bg-white/70 px-3 py-1 text-[11px] font-semibold uppercase tracking-[0.24em] text-slate-400 shadow-sm">
          Health Diary
        </div>
        <h2 class="mt-4 text-2xl font-bold text-slate-900">时康日记</h2>
        <p class="text-slate-500 mt-2 leading-7">{{ mode === 'login' ? '欢迎回来，请登录您的账户' : '创建一个新账户开始使用' }}</p>
      </div>

      <!-- Form Area -->
      <div class="app-card p-6 sm:p-7">
        <!-- Login Form -->
        <form v-if="mode === 'login'" @submit.prevent="handleLogin" class="space-y-5">
          <!-- 邮箱输入框 -->
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">邮箱</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                <Mail size="20" />
              </div>
              <input
                  v-model="loginForm.email"
                  type="email"
                  required
                  class="app-input w-full pl-10 pr-4 py-3 rounded-2xl"
                  placeholder="请输入邮箱"
              />
            </div>
          </div>

          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">密码</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                <Lock size="20" />
              </div>
              <input
                  v-model="loginForm.password"
                  type="password"
                  required
                  class="app-input w-full pl-10 pr-4 py-3 rounded-2xl"
                  placeholder="请输入密码"
              />
            </div>
          </div>

          <div class="flex items-center justify-between text-sm">
            <label class="flex items-center cursor-pointer">
              <input type="checkbox" class="rounded border-slate-300 text-primary focus:ring-primary" />
              <span class="ml-2 text-slate-600">记住我</span>
            </label>
            <a href="#" class="text-primary hover:text-primary/80 font-medium">忘记密码?</a>
          </div>

          <button
              type="submit"
              class="w-full py-3.5 rounded-2xl font-bold app-primary-action active:scale-[0.98] transition-all"
          >
            登录
          </button>
        </form>

        <!-- Register Form -->
        <form v-else @submit.prevent="handleRegister" class="space-y-5">
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">用户名</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                <User size="20" />
              </div>
              <input
                  v-model="registerForm.username"
                  type="text"
                  required
                  class="app-input w-full pl-10 pr-4 py-3 rounded-2xl"
                  placeholder="请输入用户名"
              />
            </div>
          </div>

          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">邮箱</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                <Mail size="20" />
              </div>
              <input
                  v-model="registerForm.email"
                  type="email"
                  required
                  class="app-input w-full pl-10 pr-4 py-3 rounded-2xl"
                  placeholder="请输入邮箱"
              />
            </div>
          </div>

          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">密码</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                <Lock size="20" />
              </div>
              <input
                  v-model="registerForm.password"
                  type="password"
                  required
                  class="app-input w-full pl-10 pr-4 py-3 rounded-2xl"
                  placeholder="设置密码"
              />
            </div>
          </div>

          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">确认密码</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-gray-400">
                <Lock size="20" />
              </div>
              <input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  required
                  class="app-input w-full pl-10 pr-4 py-3 rounded-2xl"
                  placeholder="再次输入密码"
              />
            </div>
          </div>

          <button
              type="submit"
              class="w-full py-3.5 rounded-2xl font-bold app-primary-action active:scale-[0.98] transition-all"
          >
            注册
          </button>
        </form>

        <!-- Divider -->
        <div class="relative my-6">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-slate-200"></div>
          </div>
          <div class="relative flex justify-center text-sm">
            <span class="px-2 bg-transparent text-slate-400">或</span>
          </div>
        </div>

        <!-- Social Login -->
        <button
            @click="handleThirdPartyLogin('wechat')"
            class="w-full flex items-center justify-center gap-3 px-4 py-3 rounded-2xl app-surface text-slate-700 transition-all active:scale-[0.98]"
        >
          <svg class="w-5 h-5 text-[#07C160]" fill="currentColor" viewBox="0 0 24 24">
            <path d="M8.5 12c0 .83-.67 1.5-1.5 1.5S5.5 12.83 5.5 12s.67-1.5 1.5-1.5 1.5.67 1.5 1.5zm6 0c0 .83-.67 1.5-1.5 1.5s-1.5-.67-1.5-1.5.67-1.5 1.5-1.5 1.5.67 1.5 1.5z"/>
            <path d="M20.5 12c0-4.42-3.58-8-8-8s-8 3.58-8 8c0 2.21.9 4.21 2.34 5.66L6 21l3.34-1.66C10.19 19.78 11.08 20 12 20c4.42 0 8-3.58 8-8z" opacity="0.1"/>
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"/>
          </svg>
          <span class="font-medium">微信快捷登录</span>
        </button>

        <!-- Toggle Mode -->
        <div class="mt-6 text-center">
          <button
              @click="toggleMode"
              class="text-primary hover:text-primary/80 font-medium transition-colors text-sm"
          >
            {{ mode === 'login' ? '还没有账户？立即注册' : '已有账户？返回登录' }}
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, Mail, UserCircle } from 'lucide-vue-next'
import { showToast } from 'vant'
import { login, register } from '@/api/auth'
import { useUserStore } from '@/store/user'
import {encryptPassword,computeSM3} from '@/utils/encrypt.js'

const router = useRouter()
const userStore = useUserStore()
const mode = ref('login')

// 登录表单数据（修改为邮箱）
const loginForm = ref({
  email: '',
  password: ''
})

// 注册表单数据
const registerForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})


// 切换模式
const toggleMode = () => {
  mode.value = mode.value === 'login' ? 'register' : 'login'
  // 重置表单
  loginForm.value = { email: '', password: '' }
  registerForm.value = { username: '', email: '', password: '', confirmPassword: '' }
}

// 登录请求（修改为传递邮箱）
const handleLogin = async () => {
  try {
    // 1. 加密密码
    const sm3Pwd = computeSM3(loginForm.value.password);
    console.log('SM3 结果：', sm3Pwd); // 看是否有值

    const encryptedPassword = encryptPassword(sm3Pwd);
    console.log('加密后密码：', encryptedPassword); // 看这里是不是 null！！！

    // 如果加密失败，直接抛出
    if (!encryptedPassword) {
      showToast('系统安全组件异常，请联系管理员')
      return
    }
    // 2. 发送请求 (传递邮箱)
    const res = await login({
      email: loginForm.value.email,
      password: encryptedPassword
    })

    console.log(res)
    userStore.setToken(res.data.token)

    showToast('登录成功')
    router.push('/')
  } catch (error) {
    const msg = error?.message || error?.msg || '登录失败，请检查邮箱和密码'
    showToast(msg)
  }
}

// 注册请求
const handleRegister = async () => {
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    showToast('两次输入的密码不一致')
    return
  }

  try {
    // 1. 加密密码
    const encryptedPassword = encryptPassword(computeSM3(registerForm.value.password))

    // 2. 发送请求
    const res = await register(
        registerForm.value.username,
        encryptedPassword,
        registerForm.value.email
    )

    userStore.setToken(res.data.token)
    showToast('注册成功')
    await router.push('/')
  } catch (error) {
    if (error.message === '加密失败') {
      showToast('系统安全组件异常，请联系管理员')
    } else {
      const msg = error?.message || error?.msg || '注册失败，请稍后重试'
      showToast(msg)
    }
  }
}

// 第三方登录
const handleThirdPartyLogin = (provider) => {
  showToast(`${provider} 登录功能待实现`)
}
</script>

<style scoped>
/* Scoped styles kept minimal as we use Tailwind */
</style>
