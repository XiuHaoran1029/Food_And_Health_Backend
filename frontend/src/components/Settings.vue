<template>
  <div class="min-h-screen flex flex-col bg-[radial-gradient(circle_at_top_left,rgba(22,119,255,0.12),transparent_28%),radial-gradient(circle_at_bottom_right,rgba(16,185,129,0.08),transparent_24%),linear-gradient(180deg,#f8fafc_0%,#eef4ff_100%)]">
    <!-- Header -->
    <header class="app-surface-strong border-b border-white/70 px-4 pt-5 py-3 flex items-center sticky top-0 z-10">
      <button
        class="p-2 -ml-2 rounded-full app-icon-action transition-colors"
        @click="goBack"
      >
        <ArrowLeft size="24" />
      </button>
      <h1 class="text-lg font-bold text-slate-900 flex-1 text-center pr-8 tracking-wide">个人设置</h1>
    </header>

    <!-- Main Content -->
    <main class="flex-1 p-4 max-w-lg mx-auto w-full space-y-6">
      
      <!-- Avatar Section -->
      <div class="flex flex-col items-center justify-center pt-2">
        <div class="relative group cursor-pointer" @click="onPickAvatar">
          <div class="w-24 h-24 rounded-[28px] border border-white/80 shadow-[0_16px_40px_rgba(15,23,42,0.10)] overflow-hidden bg-white/90 relative">
            <img
              v-if="avatarPreviewUrl"
              :src="avatarPreviewUrl"
              alt="avatar"
              class="w-full h-full object-cover"
            />
            <div v-else class="w-full h-full flex items-center justify-center text-slate-400 bg-slate-50">
              <User size="40" />
            </div>
            
            <!-- Overlay -->
            <div class="absolute inset-0 bg-black/0 group-hover:bg-black/10 transition-colors flex items-center justify-center"></div>
          </div>
          
          <!-- Camera Icon Badge -->
          <div class="absolute bottom-0 right-0 app-primary-action p-2 rounded-full border-2 border-white shadow-sm transition-colors">
            <Camera size="16" />
          </div>
          
          <input id="avatar-input" type="file" accept="image/*" class="hidden" @change="onAvatarChange" />
        </div>
        <p class="mt-3 text-sm text-slate-500">点击头像更换</p>
      </div>

      <!-- Basic Profile Card -->
      <div class="app-card p-5 space-y-5">
        <h2 class="font-bold text-slate-900 flex items-center gap-2">
          <UserCircle size="20" class="text-primary" />
          基本信息
        </h2>
        
        <div class="space-y-4">
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700">用户名</label>
            <input
              v-model="profileForm.username"
              type="text"
              class="app-input w-full px-4 py-3 rounded-2xl"
              placeholder="请输入用户名"
            />
          </div>

          <div class="space-y-2">
            <label class="block text-sm font-medium text-slate-700">患病名称</label>

<!-- Diseases Tags List -->
<div class="flex flex-wrap gap-2 mb-2" v-if="profileForm.sick.length > 0">
  <span
    v-for="disease in profileForm.sick"
    :key="disease"
    class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-blue-50 text-blue-600 rounded-full border border-blue-100 text-sm font-medium animate-in fade-in zoom-in duration-200"
  >
    {{ disease }}
    <button
      type="button"
      class="p-0.5 hover:bg-blue-100 rounded-full transition-colors"
      @click="removeDisease(disease)"
    >
      <X size="14" />
    </button>
  </span>
</div>

            <!-- Add Disease Input -->
            <div class="flex gap-2">
              <input
                v-model="newDisease"
                type="text"
                class="app-input flex-1 px-4 py-3 rounded-2xl"
                placeholder="添加患病名称（点击加号）"
                @keydown.enter.prevent="addDisease"
              />
              <button
                type="button"
                class="px-4 app-icon-action rounded-2xl font-medium flex items-center justify-center"
                @click="addDisease"
              >
                <Plus size="20" />
              </button>
            </div>
          </div>

          <!-- Avoid Foods -->
          <div class="space-y-2">
            <label class="block text-sm font-medium text-slate-700">忌口食品</label>

<!-- Tags List -->
<div class="flex flex-wrap gap-2 mb-2" v-if="taboo.length > 0">
  <span
    v-for="food in taboo"
    :key="food"
    class="inline-flex items-center gap-1.5 px-3 py-1.5 bg-red-50 text-red-600 rounded-full border border-red-100 text-sm font-medium animate-in fade-in zoom-in duration-200"
  >
    {{ food }}
    <button
      type="button"
      class="p-0.5 hover:bg-red-100 rounded-full transition-colors"
      @click="removeTaboo(food)"
    >
      <X size="14" />
    </button>
  </span>
</div>
            
<!-- Add Input -->
<div class="flex gap-2">
  <input
    v-model="newTaboo"
    type="text"
    class="app-input flex-1 px-4 py-3 rounded-2xl"
    placeholder="添加忌口（点击加号）"
    @keydown.enter.prevent="addTaboo"
  />
  <button
    type="button"
    class="px-4 app-icon-action rounded-2xl font-medium flex items-center justify-center"
    @click="addTaboo"
  >
    <Plus size="20" />
  </button>
</div>
          </div>
        </div>

        <button
          type="button"
          class="w-full py-3.5 rounded-2xl font-bold app-primary-action active:scale-[0.98] transition-all"
          @click="saveProfile"
        >
          保存个人信息
        </button>
      </div>

      <!-- Security Card -->
      <div class="app-card p-5 space-y-5">
        <h2 class="font-bold text-slate-900 flex items-center gap-2">
          <Lock size="20" class="text-primary" />
          安全设置
        </h2>

        <div class="space-y-2">
          <p class="text-sm text-slate-500">定期修改密码可以提高账号安全性</p>
          <button
            type="button"
            class="w-full py-3.5 rounded-2xl font-bold app-surface text-slate-700 active:scale-[0.98] transition-all flex items-center justify-center gap-2"
            @click="openPasswordModal"
          >
            <Lock size="18" />
            修改密码
          </button>
        </div>
      </div>
      
      <!-- Logout Card -->
      <div class="app-card p-5">
        <button
          type="button"
          class="w-full py-3.5 bg-red-50 text-red-500 border border-red-100 rounded-2xl font-bold hover:bg-red-100 active:scale-[0.98] transition-all flex items-center justify-center gap-2"
          @click="handleLogout"
        >
          <LogOut size="18" />
          退出登录
        </button>
      </div>

      <!-- Safe Area for bottom spacing -->
      <div class="h-6"></div>
    </main>

    <!-- Password Modal -->
    <Teleport to="body">
      <div v-if="showPasswordModal" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-slate-950/45 backdrop-blur-sm transition-opacity" @click="showPasswordModal = false"></div>

        <!-- Modal Content -->
        <div class="app-card w-full max-w-md p-6 relative z-10 animate-in fade-in zoom-in-95 duration-200 max-h-[calc(100vh-2rem)] overflow-y-auto">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-bold text-slate-900">修改密码</h3>
            <button class="text-slate-400 hover:text-slate-600 transition-colors" @click="showPasswordModal = false">
              <X size="24" />
            </button>
          </div>
          
          <form class="space-y-4" @submit.prevent="changePassword">
            <div class="space-y-2">
              <label class="block text-sm font-medium text-slate-700">当前密码</label>
              <input
                v-model="passwordForm.currentPassword"
                type="password"
                class="app-input w-full px-4 py-3 rounded-2xl"
                placeholder="请输入当前密码"
              />
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-medium text-slate-700">新密码</label>
              <input
                v-model="passwordForm.newPassword"
                type="password"
                class="app-input w-full px-4 py-3 rounded-2xl"
                placeholder="请输入新密码"
              />
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-medium text-slate-700">确认新密码</label>
              <input
                v-model="passwordForm.confirmPassword"
                type="password"
                class="app-input w-full px-4 py-3 rounded-2xl"
                placeholder="请再次输入新密码"
              />
            </div>

            <div class="pt-2">
              <button
                type="submit"
                class="w-full py-3.5 rounded-2xl font-bold app-primary-action active:scale-[0.98] transition-all"
              >
                确认修改
              </button>
            </div>
          </form>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Camera, User, UserCircle, Lock, Plus, X, LogOut } from 'lucide-vue-next'
import { showToast } from 'vant'
import { useUserStore } from '@/store/user'
import { getInfo, updateSetting, changePassword as apiChangePassword } from '@/api/auth'
import { fileToBase64 } from '@/utils/helper'
import {encryptPassword,computeSM3} from '@/utils/encrypt.js'

const router = useRouter()
const userStore = useUserStore()

const profileForm = ref({
  username: '',
  sick: []
})
const newDisease = ref('')

const taboo = ref([])
const newTaboo = ref('')

const showPasswordModal = ref(false)
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const lockedScrollY = ref(0)

const lockPageScroll = () => {
  if (typeof window === 'undefined') return

  lockedScrollY.value = window.scrollY || window.pageYOffset || 0
  const body = document.body
  const html = document.documentElement

  body.style.position = 'fixed'
  body.style.top = `-${lockedScrollY.value}px`
  body.style.left = '0'
  body.style.right = '0'
  body.style.width = '100%'
  body.style.overflow = 'hidden'
  html.style.overflow = 'hidden'
}

const unlockPageScroll = () => {
  if (typeof window === 'undefined') return

  const body = document.body
  const html = document.documentElement

  body.style.position = ''
  body.style.top = ''
  body.style.left = ''
  body.style.right = ''
  body.style.width = ''
  body.style.overflow = ''
  html.style.overflow = ''

  window.scrollTo(0, lockedScrollY.value)
}

const avatarFile = ref(null)
const avatarPreviewUrl = ref('')

const openPasswordModal = () => {
  if (typeof window !== 'undefined') {
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
  }

  showPasswordModal.value = true
}

watch(showPasswordModal, (visible) => {
  if (visible) {
    if (typeof window !== 'undefined') {
      window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
    }
    lockPageScroll()
  } else {
    unlockPageScroll()
  }
})

onMounted(async () => {
  try {
    const userInfo = await getInfo();
    profileForm.value.username = userInfo.data.username || ''
    profileForm.value.sick = userInfo.data.sick || []
    taboo.value = userInfo.data.taboo || []
    if (userInfo.data.img) {
      avatarPreviewUrl.value = userInfo.data.img
    }
  } catch (error) {
    console.error('获取用户信息失败:', error);
  }
})

const goBack = () => {
  if (window.history.state && window.history.state.back) {
    router.back()
  } else {
    router.push('/')
  }
}

const onPickAvatar = () => {
  const input = document.getElementById('avatar-input')
  if (input) input.click()
}

const onAvatarChange = (event) => {
  const file = event?.target?.files?.[0]
  if (!file) return

  if (avatarPreviewUrl.value && avatarPreviewUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(avatarPreviewUrl.value)
  }

  avatarFile.value = file
  avatarPreviewUrl.value = URL.createObjectURL(file)
}

const addDisease = () => {
  const value = newDisease.value.trim()
  if (!value) return
  if (profileForm.value.sick.includes(value)) {
    newDisease.value = ''
    return
  }
  profileForm.value.sick = [...profileForm.value.sick, value].slice(0, 20)
  newDisease.value = ''
}

const removeDisease = (value) => {
  profileForm.value.sick = profileForm.value.sick.filter((item) => item !== value)
}

const addTaboo = () => {
  const value = newTaboo.value.trim()
  if (!value) return
  if (taboo.value.includes(value)) {
    newTaboo.value = ''
    return
  }
  taboo.value = [...taboo.value, value].slice(0, 20)
  newTaboo.value = ''
}

const removeTaboo = (value) => {
  taboo.value = taboo.value.filter((item) => item !== value)
}

const saveProfile = async () => {
  try {
    let finalAvatar = userStore.avatar || avatarPreviewUrl.value || '' // 默认使用当前存储的头像

    // 只有当用户选择了新文件时，才进行转换和更新
    if (avatarFile.value) {
      try {
        finalAvatar = await fileToBase64(avatarFile.value);
        // 可选：调试打印，确认转换后的字符串长度是否正常
        // console.log('New Avatar Length:', finalAvatar.length);
      } catch (e) {
        console.error('图片转换失败', e);
        showToast('图片处理失败，请重试');
        return;
      }
    } else {
      // 如果没有选择新图片，finalAvatar 保持为 userStore.avatar
      // 如果 userStore.avatar 也是空的，说明用户本来就没有头像，这通常是合法的
      console.log('未更换头像，使用现有头像或空值');
    }

    // 构造提交数据 - 使用数组格式
    const payload = {
      userid: userStore.userId,
      username: profileForm.value.username,
      sick: profileForm.value.sick,
      taboo: taboo.value,
      // 后端字段是 img
      img: finalAvatar || ''
    };

    // 调试：在发送前打印查看
    console.log('Submitting payload:', payload);

    // 如果后端严格要求不能传空字符串表示“不修改”，你可能需要在这里删除该字段
    // if (!avatarFile.value) { delete payload.avatar; }

    await updateSetting(payload);

    // 更新本地 Store
    userStore.username = profileForm.value.username;
    userStore.sick = profileForm.value.sick;
    userStore.taboo = taboo.value;
    // 只有当确实有新值（或者是原本就有值）时才更新，避免意外清空
    userStore.avatar = finalAvatar;

    const oldToken =userStore.token;
    localStorage.setItem("token" ,updateToken(oldToken,finalAvatar));

    showToast('设置已保存');
  } catch (error) {
    console.error(error);
    showToast(error?.message || '保存失败');
  }
}

const changePassword = async () => {
  if (!passwordForm.value.currentPassword || !passwordForm.value.newPassword) {
    showToast('请填写完整密码信息')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    showToast('两次输入的新密码不一致')
    return
  }

  const payload = {
    userid: userStore.userId,
    old_password: encryptPassword(computeSM3(passwordForm.value.currentPassword)),
    new_password: encryptPassword(computeSM3(passwordForm.value.newPassword))

  }

  try {
    await apiChangePassword(payload);
    showToast('密码修改成功')
    passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
    showPasswordModal.value = false
  } catch (error) {
    showToast(error?.message || '修改密码失败')
  }
}


function updateToken(rawToken) {

  const decodedOuter = rawToken;

  const SEPARATOR = '|#|';

  const parts = decodedOuter.split(SEPARATOR);

  if (parts.length !== 2) {
    throw new Error(`Token 格式错误：期望 2 个部分，实际得到 ${parts.length}`);
  }

  return parts[0]+"|#|"+parts[1];
}


const handleLogout = () => {
  userStore.logout()
  router.replace('/login')
}

onUnmounted(() => {
  unlockPageScroll()
  if (avatarPreviewUrl.value && avatarPreviewUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(avatarPreviewUrl.value)
  }
})
</script>

<style scoped>
/* Scoped styles kept minimal */
</style>
