package com.medical.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.model.dto.MedicineQueryDTO;
import com.medical.model.entity.Medicine;

/**
 * 药品服务接口
 */
public interface MedicineService extends IService<Medicine> {

    /**
     * 分页查询药品列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<Medicine> pageQuery(MedicineQueryDTO queryDTO);

    /**
     * 新增药品
     *
     * @param medicine 药品信息
     * @return 是否成功
     */
    boolean addMedicine(Medicine medicine);

    /**
     * 更新药品
     *
     * @param medicine 药品信息
     * @return 是否成功
     */
    boolean updateMedicine(Medicine medicine);

    /**
     * 更新药品库存
     *
     * @param medicineId 药品ID
     * @param quantity   变更数量（正数入仓，负数出仓）
     * @return 是否成功
     */
    boolean updateStock(Long medicineId, int quantity);

    /**
     * 检查库存预警
     *
     * @param medicineId 药品ID
     * @return 是否预警
     */
    boolean checkStockAlert(Long medicineId);
}
