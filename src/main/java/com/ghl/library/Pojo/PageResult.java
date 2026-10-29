package com.ghl.library.Pojo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

/**
 * 通用分页结果组件。
 *
 * <p>将 MyBatis-Plus 的 {@link IPage} 转换为与持久层解耦的分页模型，
 * 并提供“实体 - VO”的通用映射方法，避免业务层重复拷贝分页字段。</p>
 *
 * @param <T> 记录类型
 * @author ghl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 每页条数 */
    private long size;

    /** 当前页码 */
    private long current;

    /** 总页数 */
    private long pages;

    /**
     * 由 {@link IPage} 直接构造分页结果。
     *
     * @param page 持久层分页对象
     * @param <T>  记录类型
     * @return 通用分页结果
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(), page.getSize(),
                page.getCurrent(), page.getPages());
    }

    /**
     * 由 {@link IPage} 构造分页结果，并将每条记录映射为目标类型。
     *
     * <p>适用于分页查询返回实体、但接口需要返回 VO 的场景。</p>
     *
     * @param page   持久层分页对象
     * @param mapper 记录转换函数
     * @param <S>    源记录类型
     * @param <T>    目标记录类型
     * @return 通用分页结果
     */
    public static <S, T> PageResult<T> of(IPage<S> page, Function<? super S, T> mapper) {
        List<T> records = page.getRecords().stream()
                .map(mapper)
                .toList();
        return new PageResult<>(records, page.getTotal(), page.getSize(),
                page.getCurrent(), page.getPages());
    }
}
