import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/auth'
import { setToken, removeToken, getToken, setUser, getUser } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: getUser() || {}
  }),

  actions: {
    /** 登录 */
    async login(loginData) {
      const res = await loginApi(loginData)
      this.token = res.data.token
      this.userInfo = res.data.user
      setToken(res.data.token)
      setUser(res.data.user)
    },

    /** 退出 */
    async logout() {
      try { await logoutApi() } catch {}
      this.resetState()
    },

    /** 获取用户信息 */
    async fetchUserInfo() {
      const res = await getUserInfo()
      this.userInfo = res.data
      setUser(res.data)
    },

    /** 重置状态 */
    resetState() {
      this.token = ''
      this.userInfo = {}
      removeToken()
    }
  }
})
