<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo">
        <span class="logo-icon">🌿</span>
        <span v-show="!isCollapse" class="logo-text">中医药品管理</span>
      </div>
      <el-menu :default-active="activeMenu" :collapse="isCollapse" :collapse-transition="false" router
        background-color="transparent" text-color="#D4C5B0" active-text-color="#FFFAF5">
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/medicine">
          <el-icon><FirstAidKit /></el-icon>
          <span>药品管理</span>
        </el-menu-item>
        <el-menu-item index="/stock-alert">
          <el-icon><Warning /></el-icon>
          <span>库存预警</span>
        </el-menu-item>
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse" :size="20">
            <Fold v-if="!isCollapse" /><Expand v-else />
          </el-icon>
          <el-breadcrumb separator="›">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <span class="user-name">{{ userStore.userInfo.realName || userStore.userInfo.username }}</span>
          <el-dropdown @command="handleCommand">
            <el-avatar :size="34" :icon="UserFilled" style="background:var(--primary-light)" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <div class="page-enter">
          <router-view />
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')

const handleCommand = async (cmd) => {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    await userStore.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
  } else if (cmd === 'password') {
    ElMessageBox.prompt('请输入新密码', '修改密码', { confirmButtonText: '确定', inputType: 'password' })
      .then(() => { ElMessage.success('密码修改成功，请重新登录'); userStore.resetState(); router.push('/login') })
  }
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.layout-aside {
  background: linear-gradient(180deg, #3E2723 0%, #5C3D1E 100%);
  overflow: hidden;
  transition: width 0.3s;
}
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.logo-icon { font-size: 24px; }
.logo-text { font-size: 16px; font-weight: bold; color: #FFFAF5; white-space: nowrap; }
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #E8D5B7;
  box-shadow: 0 1px 8px rgba(94,53,29,0.05);
}
.header-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn { cursor: pointer; color: var(--text-secondary); }
.header-right { display: flex; align-items: center; gap: 12px; }
.user-name { font-size: 14px; color: var(--text-primary); }
.layout-main { background: var(--bg-base); padding: 20px; overflow-y: auto; }

:deep(.el-menu) { border-right: none; }
:deep(.el-menu-item) { margin: 4px 8px; border-radius: 8px; }
:deep(.el-menu-item:hover) { background: rgba(255,255,255,0.08) !important; }
:deep(.el-menu-item.is-active) { background: rgba(201,169,110,0.2) !important; }
:deep(.el-breadcrumb__inner) { color: var(--text-secondary) !important; }
</style>
