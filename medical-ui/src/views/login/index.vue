<template>
  <div class="login-page">
    <div class="login-left">
      <div class="brand-area">
        <div class="brand-icon">🌿</div>
        <h1>中医药品管理系统</h1>
        <p>传承千年智慧，服务当代健康</p>
      </div>
    </div>
    <div class="login-right">
      <div class="login-card">
        <h2>欢迎回来</h2>
        <p class="sub">请登录您的账户</p>
        <el-form ref="formRef" :model="form" :rules="rules" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" />
          </el-form-item>
          <el-form-item prop="captcha">
            <div class="captcha-row">
              <el-input v-model="form.captcha" placeholder="验证码" />
              <img :src="captchaImage" class="captcha-img" @click="refreshCaptcha" title="点击刷新" />
            </div>
          </el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">登 录</el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCaptcha } from '@/api/auth'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const captchaImage = ref('')

const form = reactive({ username: '', password: '', captcha: '', captchaKey: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const refreshCaptcha = async () => {
  try {
    const res = await getCaptcha()
    form.captchaKey = res.data.captchaKey
    captchaImage.value = res.data.captchaImage
    form.captcha = ''
  } catch { ElMessage.warning('验证码加载失败') }
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login({ username: form.username, password: form.password, captchaKey: form.captchaKey, captcha: form.captcha })
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/dashboard')
  } catch { refreshCaptcha() }
  finally { loading.value = false }
}

onMounted(() => refreshCaptcha())
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
}
.login-left {
  flex: 1;
  background: linear-gradient(135deg, #3E2723 0%, #5C3D1E 40%, #8B5E3C 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}
.login-left::before {
  content: '';
  position: absolute;
  width: 600px; height: 600px;
  background: radial-gradient(circle, rgba(201,169,110,0.15) 0%, transparent 70%);
  top: -100px; right: -100px;
  border-radius: 50%;
}
.brand-area {
  text-align: center;
  color: #E8D5B7;
  z-index: 1;
}
.brand-icon { font-size: 72px; margin-bottom: 16px; }
.brand-area h1 { font-size: 32px; color: #FFFAF5; margin: 0 0 12px; letter-spacing: 4px; }
.brand-area p { font-size: 16px; color: #C9A96E; margin: 0; letter-spacing: 2px; }
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FDF8F0;
}
.login-card {
  width: 400px;
  padding: 48px 40px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 8px 40px rgba(94,53,29,0.1);
}
.login-card h2 { font-size: 26px; color: #3E2723; margin: 0 0 4px; }
.sub { color: #8B5E3C; font-size: 14px; margin: 0 0 32px; }
.captcha-row { display: flex; gap: 12px; }
.captcha-img { height: 40px; border-radius: 8px; cursor: pointer; border: 1px solid #E8D5B7; }
.login-btn { width: 100%; height: 46px; font-size: 16px; letter-spacing: 12px; border-radius: 10px !important; }
</style>
