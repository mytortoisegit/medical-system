package com.medical.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.medical.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统角色实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 角色名称 */
    private String roleName;

    /** 角色编码（ROLE_前缀） */
    private String roleCode;

    /** 角色描述 */
    private String description;

    /** 状态 */
    private Integer status;
}
