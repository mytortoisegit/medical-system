import request from './request'

/**
 * 分页查询药品列表
 */
export function getMedicinePage(params) {
  return request.get('/medicine/page', { params })
}

/**
 * 获取药品详情
 */
export function getMedicineById(id) {
  return request.get(`/medicine/${id}`)
}

/**
 * 新增药品
 */
export function addMedicine(data) {
  return request.post('/medicine', data)
}

/**
 * 更新药品
 */
export function updateMedicine(data) {
  return request.put('/medicine', data)
}

/**
 * 删除药品
 */
export function deleteMedicine(id) {
  return request.delete(`/medicine/${id}`)
}

/**
 * 更新药品库存
 */
export function updateStock(id, quantity) {
  return request.put(`/medicine/stock/${id}`, null, { params: { quantity } })
}

/**
 * 库存预警药品列表
 */
export function getStockAlert(params) {
  return request.get('/medicine/alert', { params })
}

/**
 * 分类统计（工作台饼图）
 */
export function countByCategory() {
  return request.get('/medicine/stats/category')
}
