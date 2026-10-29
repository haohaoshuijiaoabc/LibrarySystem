package com.ghl.library.Service;

import com.ghl.library.DTO.AdminLoginDTO;
import com.ghl.library.DTO.AppendBookDTO;
import com.ghl.library.DTO.TransactionBookNumbersDTO;
import com.ghl.library.Pojo.Book;
import com.ghl.library.Pojo.PageResult;
import com.ghl.library.Pojo.Result;
import com.ghl.library.VO.UserVO;
import jakarta.servlet.http.HttpSession;

/**
 * 管理员业务接口。
 *
 * @author ghl
 */
public interface AdminService {

    /**
     * 管理员登录。
     *
     * @param login   登录参数
     * @param session 会话
     * @return 登录结果
     */
    Result<Void> login(AdminLoginDTO login, HttpSession session);

    /**
     * 分页查询图书。
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param bookName 书名关键字
     * @param author   作者关键字
     * @return 图书分页数据
     */
    Result<PageResult<Book>> getBookInfo(Integer pageNum, Integer pageSize, String bookName, String author);

    /**
     * 管理员退出登录。
     *
     * @param session 会话
     * @return 退出结果
     */
    Result<Void> logout(HttpSession session);

    /**
     * 增加图书库存。
     *
     * @param add 库存变更参数
     * @return 操作结果
     */
    Result<Void> add(TransactionBookNumbersDTO add);

    /**
     * 扣减图书库存。
     *
     * @param reduce 库存变更参数
     * @return 操作结果
     */
    Result<Void> reduce(TransactionBookNumbersDTO reduce);

    /**
     * 新增图书。
     *
     * @param book 图书信息
     * @return 操作结果
     */
    Result<Void> append(AppendBookDTO book);

    /**
     * 删除图书。
     *
     * @param name 书名
     * @return 操作结果
     */
    Result<Void> delete(String name);

    /**
     * 切换用户封禁/解封状态。
     *
     * @param name 用户名
     * @return 操作结果
     */
    Result<Void> changeState(String name);

    /**
     * 分页查询用户（脱敏后的用户信息）。
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param userName 用户名关键字
     * @param phone    手机号关键字
     * @return 用户分页数据
     */
    Result<PageResult<UserVO>> getUserInfo(Integer pageNum, Integer pageSize, String userName, String phone);
}
