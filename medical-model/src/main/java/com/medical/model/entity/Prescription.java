package com.medical.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.medical.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 处方实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("med_prescription")
public class Prescription extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 处方编号 */
    private String prescriptionNo;

    /** 患者姓名 */
    private String patientName;

    /** 患者性别（0-未知 1-男 2-女） */
    private Integer patientGender;

    /** 患者年龄 */
    private Integer patientAge;

    /** 患者联系方式 */
    private String patientPhone;

    /** 诊断结果 */
    private String diagnosis;

    /** 治法 */
    private String treatment;

    /** 处方类型（内服/外用等） */
    private String prescriptionType;

    /** 剂数 */
    private Integer doseCount;

    /** 开方日期 */
    private LocalDate prescriptionDate;

    /** 开方医生ID */
    private Long doctorId;

    /** 开方医生姓名 */
    private String doctorName;

    /** 总金额 */
    private java.math.BigDecimal totalAmount;

    /** 审核状态（0-待审核 1-已审核 2-已驳回） */
    private Integer auditStatus;

    /** 审核人ID */
    private Long auditorId;

    /** 审核意见 */
    private String auditOpinion;

    /** 状态（0-作废 1-有效） */
    private Integer status;
}
