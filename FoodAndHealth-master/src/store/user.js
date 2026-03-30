import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(null)
  const username = ref('')
  const avatar= ref('')// 头像已移除
  const sick = ref('')
  const taboo = ref('')


  function _parseToken(rawToken) {
    if (!rawToken) {
      logout();
      return;
    }

    try {
      // 分隔符保持不变
      const SEPARATOR = '|#|';
      const parts = rawToken.split(SEPARATOR);

      // ======================
      // 关键修改：现在只有 2 部分
      // ======================
      if (parts.length !== 2) {
        throw new Error(`Token 格式错误：期望 2 个部分，实际得到 ${parts.length}`);
      }

      // 解析 userId
      const userIdStr = urlSafeBase64Decode(parts[0]);
      // 解析 username
      const usernameStr = urlSafeBase64Decode(parts[1]);

      const parsedId = Number(userIdStr);
      if (isNaN(parsedId)) {
        throw new Error('用户 ID 不是有效数字');
      }

      // 赋值
      userId.value = parsedId;
      username.value = usernameStr;
      // 头像清空/移除
      avatar.value = '';

      console.log('✅ Token 解析成功:', { id: userId.value, user: username.value });

    } catch (e) {
      console.error('❌ Token 解析失败:', e.message);
      logout();
    }
  }

  function setToken(rawToken) {
    if (!rawToken) {
      logout();
      console.log('❌ Token 不能为空')
      return;
    }
    token.value = rawToken;
    localStorage.setItem('token', rawToken);
    _parseToken(rawToken);
  }

  function logout() {
    token.value = ''
    userId.value = null
    username.value = ''
    // 头像清空
    avatar.value = ''
    sick.value = ''
    taboo.value = ''
    localStorage.removeItem('token')
  }

  // URL Safe Base64 解码
  function urlSafeBase64Decode(base64Url) {
    let base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const pad = base64.length % 4;
    if (pad) {
      base64 += '='.repeat(4 - pad);
    }
    try {
      return atob(base64);
    } catch (e) {
      console.error('Base64 解码失败:', e);
      throw new Error('Invalid Base64');
    }
  }

  // 初始化时解析已存储的 token
  if (token.value) {
    _parseToken(token.value)
  }

  const isLoggedIn = computed(() => !!token.value)

  return {
    token,
    userId,
    username,
    avatar, // 保留变量但不使用，防止页面报错
    sick,
    taboo,
    setToken,
    logout,
    isLoggedIn
  }
})
