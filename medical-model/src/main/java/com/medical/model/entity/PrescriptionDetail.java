package com.medical.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.medical.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 处方明细实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("med_prescription_detail")
public class PrescriptionDetail extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 处方ID */
    private Long prescriptionId;

    /** 药品ID */
    private Long medicineId;

    /** 药品名称 */
    private String medicineName;

    /** 药品规格 */
    private String specification;

    /** 数量 */
    private Integer quantity;

    /** 单位 */
    private String unit;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 金额 */
    private BigDecimal amount;

    /** 用法 */
    private String usageMethod;

    /** 备注 */
    private String note;

    /** 排序号 */
    private Integer sortOrder;
}
