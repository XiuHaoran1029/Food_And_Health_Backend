import http from './http'


// 修复后 ✅
export const login = (data) =>
    http.post('/api/auth/login', data)
/**
 * 用户注册
 * @param {string} username
 * @param {string} password
 * @param {string} email
 */
export const register = (username, password, email) =>
  http.post('/api/auth/register', { username, password, email })

/**
 * 获取用户信息（用户名、疾病信息、忌口信息、头像）
 */
export const getInfo = () =>
    http.get('/api/auth/info', {
        params: { token: localStorage.getItem('token') }
    })
/**
 * 更新用户设置（用户名、疾病信息、忌口信息、头像）
 * @param {object} data - { userid, username, sick, taboo, avatar }
 */
export const updateSetting = (data) =>
  http.post('/api/auth/setting', data)


export const changePassword = (data) =>
  http.post('/api/auth/setting/change', data)
