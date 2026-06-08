package com.medical.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.model.dto.MedicineQueryDTO;
import com.medical.model.entity.Medicine;

import java.util.List;
import java.util.Map;

/**
 * 药品服务接口
 */
public interface MedicineService extends IService<Medicine> {

    /** 分页查询 */
    Page<Medicine> pageQuery(MedicineQueryDTO queryDTO);

    /** 新增 */
    boolean addMedicine(Medicine medicine);

    /** 更新 */
    boolean updateMedicine(Medicine medicine);

    /** 更新库存 */
    boolean updateStock(Long medicineId, int quantity);

    /** 检查库存预警 */
    boolean checkStockAlert(Long medicineId);

    /** 统计各分类药品数量（工作台饼图数据） */
    List<Map<String, Object>> countByCategory();
}
