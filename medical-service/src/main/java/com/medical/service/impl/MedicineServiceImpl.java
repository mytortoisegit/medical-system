package com.medical.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.common.enums.ResultCode;
import com.medical.common.exception.BusinessException;
import com.medical.mapper.MedicineMapper;
import com.medical.model.dto.MedicineQueryDTO;
import com.medical.model.entity.Medicine;
import com.medical.service.MedicineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 药品服务实现
 */
@Slf4j
@Service
public class MedicineServiceImpl extends ServiceImpl<MedicineMapper, Medicine> implements MedicineService {

    @Override
    public Page<Medicine> pageQuery(MedicineQueryDTO queryDTO) {
        LambdaQueryWrapper<Medicine> wrapper = new LambdaQueryWrapper<>();

        // 药品名称模糊查询
        wrapper.like(StrUtil.isNotBlank(queryDTO.getMedicineName()),
                Medicine::getMedicineName, queryDTO.getMedicineName());

        // 药品编码精确查询
        wrapper.eq(StrUtil.isNotBlank(queryDTO.getMedicineCode()),
                Medicine::getMedicineCode, queryDTO.getMedicineCode());

        // 分类筛选
        wrapper.eq(queryDTO.getCategoryId() != null,
                Medicine::getCategoryId, queryDTO.getCategoryId());

        // 性味筛选
        wrapper.eq(StrUtil.isNotBlank(queryDTO.getProperty()),
                Medicine::getProperty, queryDTO.getProperty());

        // 归经筛选
        wrapper.like(StrUtil.isNotBlank(queryDTO.getMeridian()),
                Medicine::getMeridian, queryDTO.getMeridian());

        // 产地筛选
        wrapper.eq(StrUtil.isNotBlank(queryDTO.getOrigin()),
                Medicine::getOrigin, queryDTO.getOrigin());

        // 状态筛选
        wrapper.eq(queryDTO.getStatus() != null,
                Medicine::getStatus, queryDTO.getStatus());

        // 库存预警筛选（库存数量 <= 预警阈值）
        if (queryDTO.getStockAlert() != null && queryDTO.getStockAlert()) {
            wrapper.apply("stock_quantity <= stock_alert_threshold");
        }

        // 排序
        if (StrUtil.isNotBlank(queryDTO.getOrderBy())) {
            boolean isAsc = "asc".equalsIgnoreCase(queryDTO.getOrderType());
            if ("createTime".equals(queryDTO.getOrderBy())) {
                wrapper.orderBy(true, isAsc, Medicine::getCreateTime);
            } else if ("stockQuantity".equals(queryDTO.getOrderBy())) {
                wrapper.orderBy(true, isAsc, Medicine::getStockQuantity);
            }
        } else {
            wrapper.orderByDesc(Medicine::getCreateTime);
        }

        return this.page(new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addMedicine(Medicine medicine) {
        // 检查药品名称唯一性
        long count = this.count(new LambdaQueryWrapper<Medicine>()
                .eq(Medicine::getMedicineName, medicine.getMedicineName()));
        if (count > 0) {
            throw new BusinessException(ResultCode.MEDICINE_NAME_DUPLICATE);
        }
        return this.save(medicine);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMedicine(Medicine medicine) {
        Medicine exist = this.getById(medicine.getId());
        if (exist == null) {
            throw new BusinessException(ResultCode.MEDICINE_NOT_FOUND);
        }

        // 如果修改了名称，检查唯一性
        if (!exist.getMedicineName().equals(medicine.getMedicineName())) {
            long count = this.count(new LambdaQueryWrapper<Medicine>()
                    .eq(Medicine::getMedicineName, medicine.getMedicineName())
                    .ne(Medicine::getId, medicine.getId()));
            if (count > 0) {
                throw new BusinessException(ResultCode.MEDICINE_NAME_DUPLICATE);
            }
        }

        return this.updateById(medicine);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStock(Long medicineId, int quantity) {
        Medicine medicine = this.getById(medicineId);
        if (medicine == null) {
            throw new BusinessException(ResultCode.MEDICINE_NOT_FOUND);
        }

        int newStock = medicine.getStockQuantity() + quantity;
        if (newStock < 0) {
            throw new BusinessException(ResultCode.MEDICINE_STOCK_INSUFFICIENT);
        }

        medicine.setStockQuantity(newStock);
        boolean result = this.updateById(medicine);

        if (result) {
            log.info("药品库存更新: medicineId={}, 变更={}, 当前库存={}", medicineId, quantity, newStock);
        }

        return result;
    }

    @Override
    public boolean checkStockAlert(Long medicineId) {
        Medicine medicine = this.getById(medicineId);
        if (medicine == null) {
            throw new BusinessException(ResultCode.MEDICINE_NOT_FOUND);
        }
        return medicine.getStockQuantity() <= medicine.getStockAlertThreshold();
    }

    @Override
    public List<Map<String, Object>> countByCategory() {
        return getBaseMapper().countByCategory();
    }
}
