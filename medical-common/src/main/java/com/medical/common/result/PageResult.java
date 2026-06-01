package com.medical.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询结果封装
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private long pageNum;

    /** 每页条数 */
    private long pageSize;

    /** 总页数 */
    private long pages;

    /** 数据列表 */
    private List<T> records;

    /**
     * 从MyBatis-Plus分页对象构建
     */
    public static <T> PageResult<T> of(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        return new PageResult<>(
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages(),
                page.getRecords()
        );
    }

    /**
     * 手动构建分页结果
     */
    public static <T> PageResult<T> of(long total, long pageNum, long pageSize, List<T> records) {
        long pages = (total + pageSize - 1) / pageSize;
        return new PageResult<>(total, pageNum, pageSize, pages, records);
    }
}
