package com.ghl.library.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ghl.library.Pojo.Borrow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 借阅记录数据访问层。
 *
 * @author ghl
 */
@Mapper
public interface BorrowMapper extends BaseMapper<Borrow> {

    /**
     * 统计某用户对某本书当前未归还的借阅记录数量。
     *
     * @param userId 用户 ID
     * @param bookId 图书 ID
     * @return 未归还记录数
     */
    @Select("SELECT COUNT(*) FROM borrow_record WHERE user_id = #{userId} AND book_id = #{bookId} AND is_return = 0")
    long countActiveBorrow(@Param("userId") Integer userId, @Param("bookId") Integer bookId);
}
