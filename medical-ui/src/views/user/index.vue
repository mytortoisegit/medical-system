<template>
  <div class="user-page">
    <div class="toolbar">
      <el-button type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon> 新增大夫
      </el-button>
    </div>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small">
              {{ row.status === 0 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后登录" width="160">
          <template #default="{ row }">
            {{ row.lastLoginTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @change="fetchData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑大夫' : '新增大夫'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="大夫姓名" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" style="width:100%" placeholder="请选择">
            <el-option label="管理员" :value="1" />
            <el-option label="大夫" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { getUserPage, addUser, updateUser, deleteUser, resetPassword } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10 })
const form = reactive({
  username: '', realName: '', phone: '', email: '',
  roleId: 2, status: 0
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserPage(query)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally { loading.value = false }
}

const openDialog = (row) => {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, {
      id: row.id, username: row.username, realName: row.realName,
      phone: row.phone || '', email: row.email || '',
      roleId: row.roleId || 2, status: row.status || 0
    })
  } else {
    formRef.value?.resetFields()
    Object.assign(form, { username: '', realName: '', phone: '', email: '', roleId: 2, status: 0 })
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    isEdit.value ? await updateUser({ id: form.id, ...form }) : await addUser(form)
    ElMessage.success(isEdit.value ? '更新成功' : '新增成功，默认密码 123456')
    dialogVisible.value = false
    fetchData()
  } finally { saving.value = false }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除「${row.realName}」吗？`, '警告', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

const handleResetPwd = async (row) => {
  await ElMessageBox.confirm(`确定将「${row.realName}」密码重置为 123456？`, '提示', { type: 'warning' })
  await resetPassword(row.id)
  ElMessage.success('密码已重置为 123456')
}

fetchData()
</script>

<style scoped>
.toolbar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
