package com.medical.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.medical.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药品分类实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("med_category")
public class MedicineCategory extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 分类名称 */
    private String categoryName;

    /** 分类编码 */
    private String categoryCode;

    /** 父分类ID（0-顶级分类） */
    private Long parentId;

    /** 层级 */
    private Integer level;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态（0-禁用 1-启用） */
    private Integer status;
}
