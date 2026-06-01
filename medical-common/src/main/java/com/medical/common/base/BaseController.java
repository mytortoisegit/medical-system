package com.medical.common.base;

/**
 * Controller基类 - 所有Controller继承此类
 */
public class BaseController {

    /**
     * 起始页默认值
     */
    protected static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 每页条数默认值
     */
    protected static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页条数
     */
    protected static final int MAX_PAGE_SIZE = 100;

    /**
     * 校验并修正分页参数
     */
    protected void validatePageParam(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) {
            throw new IllegalArgumentException("页码必须大于0");
        }
        if (pageSize == null || pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("每页条数必须在1~" + MAX_PAGE_SIZE + "之间");
        }
    }
}
