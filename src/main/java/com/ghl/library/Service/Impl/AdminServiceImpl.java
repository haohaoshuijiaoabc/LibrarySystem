package com.ghl.library.Service.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghl.library.DTO.AdminLoginDTO;
import com.ghl.library.DTO.AppendBookDTO;
import com.ghl.library.DTO.TransactionBookNumbersDTO;
import com.ghl.library.Constant.StockOperation;
import com.ghl.library.Exception.LibraryException;
import com.ghl.library.Mapper.AdminMapper;
import com.ghl.library.Mapper.BookMapper;
import com.ghl.library.Mapper.UserMapper;
import com.ghl.library.Pojo.Admin;
import com.ghl.library.Pojo.Book;
import com.ghl.library.Pojo.PageResult;
import com.ghl.library.Pojo.Result;
import com.ghl.library.Pojo.User;
import com.ghl.library.Service.AdminService;
import com.ghl.library.Util.Code;
import com.ghl.library.Util.PageUtils;
import com.ghl.library.VO.UserVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 管理员业务实现。
 *
 * @author ghl
 */
@Slf4j
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    /** 用户状态：正常 */
    private static final int USER_STATE_NORMAL = 0;

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private BookMapper bookMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private StockOperatorFactory stockOperatorFactory;

    @Override
    public Result<Void> login(AdminLoginDTO login, HttpSession session) {
        String adminName = login.getAdminName();
        String password = login.getPassword();

        if (StrUtil.hasBlank(adminName, password)) {
            return Result.error(Code.INFO_EMPTY_ERROR, Code.INFO_EMPTY_ERROR_MSG);
        }

        Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("name", adminName));
        if (admin == null || !Objects.equals(admin.getPassword(), password)) {
            throw new LibraryException(Code.USER_OR_PASSWORD_ERROR, Code.USER_OR_PASSWORD_ERROR_MSG);
        }

        session.setAttribute("admin", admin.getId());
        log.info("管理员登录成功：adminId={}", admin.getId());
        return Result.success();
    }

    @Override
    public Result<PageResult<Book>> getBookInfo(Integer pageNum, Integer pageSize,
                                                String bookName, String author) {
        Page<Book> page = PageUtils.of(pageNum, pageSize);
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(bookName)) {
            queryWrapper.like("name", bookName);
        }
        if (StrUtil.isNotBlank(author)) {
            queryWrapper.like("author", author);
        }
        queryWrapper.orderByAsc("id");
        IPage<Book> resultPage = bookMapper.selectPage(page, queryWrapper);
        return Result.success(PageResult.of(resultPage));
    }

    @Override
    public Result<Void> logout(HttpSession session) {
        session.removeAttribute("admin");
        return Result.success();
    }

    @Override
    public Result<Void> add(TransactionBookNumbersDTO add) {
        requirePositive(add.getNumbers());
        requireBookExists(add.getBook());
        stockOperatorFactory.get(StockOperation.ADD)
                .operate(add.getBook(), add.getNumbers());
        return Result.success();
    }

    @Override
    public Result<Void> reduce(TransactionBookNumbersDTO reduce) {
        requirePositive(reduce.getNumbers());
        requireBookExists(reduce.getBook());
        stockOperatorFactory.get(StockOperation.REDUCE)
                .operate(reduce.getBook(), reduce.getNumbers());
        return Result.success();
    }

    @Override
    public Result<Void> append(AppendBookDTO book) {
        if (StrUtil.hasBlank(book.getBookName(), book.getAuthor()) || book.getStock() == null) {
            throw new LibraryException(Code.APPEND_BOOK_INFO_ERROR, Code.APPEND_BOOK_INFO_ERROR_MSG);
        }
        requirePositive(book.getStock());

        Book existing = bookMapper.selectOne(
                new QueryWrapper<Book>().eq("name", book.getBookName()));
        if (existing != null) {
            throw new LibraryException(Code.BOOK_EXIST, Code.BOOK_EXIST_MSG);
        }

        Book newBook = new Book();
        newBook.setName(book.getBookName());
        newBook.setAuthor(book.getAuthor());
        newBook.setStock(book.getStock());
        if (bookMapper.insert(newBook) == 0) {
            throw new LibraryException(Code.APPEND_BOOK_ERROR, Code.APPEND_BOOK_ERROR_MSG);
        }
        return Result.success();
    }

    @Override
    public Result<Void> delete(String name) {
        if (StrUtil.isBlank(name)) {
            throw new LibraryException(Code.INFO_EMPTY_ERROR, Code.INFO_EMPTY_ERROR_MSG);
        }
        Book book = requireBookExists(name);
        if (book.getStock() > 0) {
            throw new LibraryException(Code.BOOK_STOCK_EXIST, Code.BOOK_STOCK_EXIST_MSG);
        }
        if (bookMapper.delete(new QueryWrapper<Book>().eq("name", name)) == 0) {
            throw new LibraryException(Code.DELETE_BOOK_ERROR, Code.DELETE_BOOK_ERROR_MSG);
        }
        return Result.success();
    }

    @Override
    public Result<Void> changeState(String name) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", name));
        if (user == null) {
            throw new LibraryException(Code.USER_ERROR, Code.USER_ERROR_MSG);
        }

        if (Objects.equals(user.getIsLocked(), USER_STATE_NORMAL)) {
            user.setIsLocked(1);
        } else {
            user.setIsLocked(USER_STATE_NORMAL);
            user.setPasswordErrorCount(0);
        }
        if (userMapper.updateById(user) == 0) {
            throw new LibraryException(Code.CHANGE_STATE_ERROR, Code.CHANGE_STATE_ERROR_MSG);
        }
        return Result.success();
    }

    @Override
    public Result<PageResult<UserVO>> getUserInfo(Integer pageNum, Integer pageSize,
                                                  String userName, String phone) {
        Page<User> page = PageUtils.of(pageNum, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(userName)) {
            queryWrapper.like("username", userName);
        }
        if (StrUtil.isNotBlank(phone)) {
            queryWrapper.like("phone", phone);
        }
        queryWrapper.orderByAsc("id");
        IPage<User> resultPage = userMapper.selectPage(page, queryWrapper);

        PageResult<UserVO> pageResult = PageResult.of(resultPage, user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            return vo;
        });
        return Result.success(pageResult);
    }

    /**
     * 校验库存变更数量必须为正数。
     *
     * @param numbers 变更数量
     */
    private void requirePositive(Integer numbers) {
        if (numbers == null || numbers <= 0) {
            throw new LibraryException(Code.ADD_NUMBER_ERROR, Code.ADD_NUMBER_ERROR_MSG);
        }
    }

    /**
     * 校验图书存在并返回。
     *
     * @param name 书名
     * @return 图书
     */
    private Book requireBookExists(String name) {
        Book book = bookMapper.selectOne(new QueryWrapper<Book>().eq("name", name));
        if (book == null) {
            throw new LibraryException(Code.BOOK_EXIST_ERROR, Code.BOOK_EXIST_ERROR_MSG);
        }
        return book;
    }
}
