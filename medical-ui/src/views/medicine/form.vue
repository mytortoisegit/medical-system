<template>
  <div class="medicine-form">
    <el-card shadow="never">
      <template #header>
        <span>{{ isEdit ? '编辑药品' : '新增药品' }}</span>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width:800px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="药品名称" prop="medicineName">
              <el-input v-model="form.medicineName" placeholder="请输入药品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="药品编码" prop="medicineCode">
              <el-input v-model="form.medicineCode" placeholder="自动生成或手动输入" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性味" prop="property">
              <el-select v-model="form.property" placeholder="请选择">
                <el-option v-for="p in ['寒','热','温','凉','平']" :key="p" :label="p" :value="p" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="归经">
              <el-input v-model="form.meridian" placeholder="如：肺、胃、大肠" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="产地">
              <el-input v-model="form.origin" placeholder="请输入产地" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="毒性">
              <el-select v-model="form.toxicity" placeholder="请选择">
                <el-option v-for="t in ['无毒','小毒','有毒','大毒']" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="规格">
              <el-input v-model="form.specification" placeholder="如：500g/袋" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单位">
              <el-input v-model="form.unit" placeholder="如：克" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="参考价格(元)">
              <el-input-number v-model="form.referencePrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存数量">
              <el-input-number v-model="form.stockQuantity" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预警阈值">
              <el-input-number v-model="form.stockAlertThreshold" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="功效">
          <el-input v-model="form.efficacy" type="textarea" :rows="2" placeholder="请输入功效" />
        </el-form-item>
        <el-form-item label="主治">
          <el-input v-model="form.indication" type="textarea" :rows="2" placeholder="请输入主治" />
        </el-form-item>
        <el-form-item label="用法用量">
          <el-input v-model="form.usageDosage" type="textarea" :rows="2" placeholder="请输入用法用量" />
        </el-form-item>
        <el-form-item label="禁忌">
          <el-input v-model="form.contraindication" type="textarea" :rows="2" placeholder="请输入禁忌" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          <el-button @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { addMedicine, updateMedicine, getMedicineById } from '@/api/medicine'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const saving = ref(false)
const isEdit = ref(false)

const form = reactive({
  medicineName: '', medicineCode: '', property: '', meridian: '',
  origin: '', toxicity: '无毒', specification: '', unit: '克',
  referencePrice: null, stockQuantity: 0, stockAlertThreshold: 100,
  efficacy: '', indication: '', usageDosage: '', contraindication: '',
  status: 1
})

const rules = {
  medicineName: [{ required: true, message: '请输入药品名称', trigger: 'blur' }]
}

const loadData = async () => {
  const id = route.query.id
  if (!id) return
  isEdit.value = true
  try {
    const res = await getMedicineById(id)
    Object.assign(form, res.data)
  } catch {
    ElMessage.error('加载药品信息失败')
  }
}

const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEdit.value) {
      await updateMedicine({ ...form, id: route.query.id })
      ElMessage.success('更新成功')
    } else {
      await addMedicine(form)
      ElMessage.success('新增成功')
    }
    router.push('/medicine')
  } finally {
    saving.value = false
  }
}

onMounted(() => loadData())
</script>
