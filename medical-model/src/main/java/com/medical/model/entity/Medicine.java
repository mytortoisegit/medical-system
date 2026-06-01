package com.medical.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.medical.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 中药药品实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("med_medicine")
public class Medicine extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 药品编码 */
    private String medicineCode;

    /** 药品名称 */
    private String medicineName;

    /** 拼音码（用于快速检索） */
    private String pinyinCode;

    /** 药品分类ID */
    private Long categoryId;

    /** 药品分类名称 */
    private String categoryName;

    /** 别名 */
    private String alias;

    /** 性味（寒、热、温、凉、平） */
    private String property;

    /** 味（酸、苦、甘、辛、咸） */
    private String taste;

    /** 归经 */
    private String meridian;

    /** 功效 */
    private String efficacy;

    /** 主治 */
    private String indication;

    /** 用法用量 */
    private String usageDosage;

    /** 禁忌 */
    private String contraindication;

    /** 毒性（无毒、小毒、有毒、大毒） */
    private String toxicity;

    /** 产地 */
    private String origin;

    /** 规格 */
    private String specification;

    /** 单位 */
    private String unit;

    /** 参考价格 */
    private BigDecimal referencePrice;

    /** 当前库存数量 */
    private Integer stockQuantity;

    /** 库存预警阈值 */
    private Integer stockAlertThreshold;

    /** 药品图片URL */
    private String imageUrl;

    /** 状态（0-下架 1-上架） */
    private Integer status;
}
