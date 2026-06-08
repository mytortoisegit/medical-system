package com.medical.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends PageQueryDTO {

    /** 用户名（模糊查询） */
    private String username;

    /** 真实姓名（模糊查询） */
    private String realName;

    /** 角色ID */
    private Long roleId;

    /** 状态 */
    private Integer status;
}
