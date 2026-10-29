package com.ghl.library.Service;

import com.ghl.library.Pojo.PageResult;
import com.ghl.library.Pojo.Result;
import com.ghl.library.VO.BorrowedVo;
import jakarta.servlet.http.HttpSession;

/**
 * 借阅业务接口。
 *
 * @author ghl
 */
public interface BorrowService {

    /**
     * 借阅图书。
     *
     * @param userId 用户 ID
     * @param bookId 图书 ID
     * @return 借阅结果
     */
    Result<BorrowedVo> borrow(Integer userId, Integer bookId);

    /**
     * 分页查询当前用户未归还的借阅记录。
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param session  会话
     * @return 借阅记录分页数据
     */
    Result<PageResult<BorrowedVo>> borrowed(Integer pageNum, Integer pageSize, HttpSession session);

    /**
     * 归还图书。
     *
     * @param bookName 书名
     * @param session  会话
     * @return 归还结果
     */
    Result<Void> returnBook(String bookName, HttpSession session);
}
