package com.ghl.library.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ghl.library.Pojo.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 图书数据访问层。
 *
 * <p>库存变更全部使用数据库原子更新语句（{@code stock = stock ± n}），
 * 在 SQL 层完成“读取 - 计算 - 写回”，避免并发场景下的丢失更新问题。</p>
 *
 * @author ghl
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {

    /**
     * 原子增加库存。
     *
     * @param name    书名
     * @param numbers 增加数量
     * @return 受影响行数
     */
    @Update("UPDATE book SET stock = stock + #{numbers} WHERE name = #{name}")
    int increaseStock(@Param("name") String name, @Param("numbers") int numbers);

    /**
     * 原子扣减库存，库存不足时不会更新。
     *
     * @param name    书名
     * @param numbers 扣减数量
     * @return 受影响行数，0 表示库存不足或图书不存在
     */
    @Update("UPDATE book SET stock = stock - #{numbers} WHERE name = #{name} AND stock >= #{numbers}")
    int decreaseStock(@Param("name") String name, @Param("numbers") int numbers);

    /**
     * 借阅时原子扣减 1 本库存。
     *
     * @param bookId 图书 ID
     * @return 受影响行数，0 表示库存不足
     */
    @Update("UPDATE book SET stock = stock - 1 WHERE id = #{bookId} AND stock >= 1")
    int borrowOne(@Param("bookId") Integer bookId);

    /**
     * 归还时原子增加 1 本库存。
     *
     * @param bookId 图书 ID
     * @return 受影响行数
     */
    @Update("UPDATE book SET stock = stock + 1 WHERE id = #{bookId}")
    int returnOne(@Param("bookId") Integer bookId);
}
