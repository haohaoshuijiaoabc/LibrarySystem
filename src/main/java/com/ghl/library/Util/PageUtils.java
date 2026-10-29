package com.ghl.library.Util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 通用分页参数构建工具。
 *
 * <p>统一处理页码、每页条数的默认值与上限，避免分页参数异常导致
 * {@code LIMIT} 语句报错或一次查询过多数据。</p>
 *
 * @author ghl
 */
public final class PageUtils {

    /** 默认每页条数 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 每页条数上限 */
    public static final int MAX_PAGE_SIZE = 100;

    private PageUtils() {
    }

    /**
     * 根据请求参数构建安全的分页对象。
     *
     * @param pageNum  页码，为空或小于 1 时使用第 1 页
     * @param pageSize 每页条数，为空或小于 1 时使用默认值，超过上限时截断
     * @param <T>      记录类型
     * @return 分页对象
     */
    public static <T> Page<T> of(Integer pageNum, Integer pageSize) {
        int current = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int size = pageSize == null || pageSize < 1
                ? DEFAULT_PAGE_SIZE
                : Math.min(pageSize, MAX_PAGE_SIZE);
        return new Page<>(current, size);
    }
}
