package com.medical.model.dto;

import lombok.Data;

/**
 * 分页查询基类DTO
 */
@Data
public class PageQueryDTO {

    /** 当前页码 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 排序字段 */
    private String orderBy;

    /** 排序方式（asc/desc） */
    private String orderType = "desc";
}
