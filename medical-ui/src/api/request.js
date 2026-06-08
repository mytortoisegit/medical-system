import axios from 'axios'
import { getToken, removeToken, setToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
  timeout: 15000
})

// 请求拦截器 — 自动带 JWT Token
service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器 — 统一错误处理 + Token 自动续期
service.interceptors.response.use(
  response => {
    const res = response.data
    // 自动刷新 Token（后端在响应头中下发新 Token）
    const newToken = response.headers['authorization']
    if (newToken) {
      setToken(newToken.replace('Bearer ', ''))
    }
    // 业务状态码判断
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          removeToken()
          window.location.href = '/login'
          break
        case 403:
          ElMessage.error('无权限访问')
          break
        case 500:
          ElMessage.error('服务器异常，请稍后重试')
          break
        default:
          ElMessage.error(error.response.data?.message || '网络异常')
      }
    } else {
      ElMessage.error('网络连接失败，请检查网络')
    }
    return Promise.reject(error)
  }
)

export default service
