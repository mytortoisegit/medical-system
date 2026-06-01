package com.medical.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药品查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MedicineQueryDTO extends PageQueryDTO {

    /** 药品名称（模糊查询） */
    private String medicineName;

    /** 药品编码 */
    private String medicineCode;

    /** 分类ID */
    private Long categoryId;

    /** 性味 */
    private String property;

    /** 归经 */
    private String meridian;

    /** 产地 */
    private String origin;

    /** 状态（0-下架 1-上架） */
    private Integer status;

    /** 是否库存预警 */
    private Boolean stockAlert;
}
