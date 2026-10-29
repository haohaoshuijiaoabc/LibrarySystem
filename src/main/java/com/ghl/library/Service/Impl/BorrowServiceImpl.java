package com.ghl.library.Service.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghl.library.Exception.LibraryException;
import com.ghl.library.Mapper.BookMapper;
import com.ghl.library.Mapper.BorrowMapper;
import com.ghl.library.Mapper.UserMapper;
import com.ghl.library.Pojo.Book;
import com.ghl.library.Pojo.Borrow;
import com.ghl.library.Pojo.PageResult;
import com.ghl.library.Pojo.Result;
import com.ghl.library.Pojo.User;
import com.ghl.library.Service.BorrowService;
import com.ghl.library.Util.Code;
import com.ghl.library.Util.PageUtils;
import com.ghl.library.VO.BorrowedVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 借阅业务实现。
 *
 * <p>并发控制分两层：应用内使用 synchronized 串行化借阅/归还操作，避免同一用户
 * 并发重复借阅；数据库层使用条件更新保证库存不会被扣成负数。</p>
 *
 * <p>事务由 {@link PlatformTransactionManager} 显式开启和提交，保证锁的持有范围
 * 覆盖事务提交，避免并发线程在提交前读到旧数据。</p>
 *
 * @author ghl
 */
@Slf4j
@Service
public class BorrowServiceImpl extends ServiceImpl<BorrowMapper, Borrow> implements BorrowService {

    /** 未归还状态 */
    private static final int STATUS_NOT_RETURNED = 0;

    /** 已归还状态 */
    private static final int STATUS_RETURNED = 1;

    /** 借阅列表默认展示的未知图书名称 */
    private static final String UNKNOWN_BOOK_NAME = "未知图书";

    @Resource
    private BookMapper bookMapper;

    @Resource
    private BorrowMapper borrowMapper;

    @Resource
    private UserMapper userMapper;

    /**
     * 借阅/归还互斥锁。
     * 使用最简单的 synchronized 即可满足课程项目的并发场景。
     */
    private final Object borrowReturnLock = new Object();

    @Resource
    private PlatformTransactionManager transactionManager;

    @Override
    public Result<BorrowedVo> borrow(Integer userId, Integer bookId) {
        User user = requireUser(userId);
        if (bookId == null) {
            throw new LibraryException(Code.BOOK_EXIST_ERROR, Code.BOOK_EXIST_ERROR_MSG);
        }
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new LibraryException(Code.BOOK_EXIST_ERROR, Code.BOOK_EXIST_ERROR_MSG);
        }

        synchronized (borrowReturnLock) {
            return borrowInTransaction(user, book);
        }
    }

    @Override
    public Result<PageResult<BorrowedVo>> borrowed(Integer pageNum, Integer pageSize, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("login");
        User user = requireUser(userId);

        Page<Borrow> page = PageUtils.of(pageNum, pageSize);
        IPage<Borrow> resultPage = borrowMapper.selectPage(page, new QueryWrapper<Borrow>()
                .eq("user_id", userId)
                .eq("is_return", STATUS_NOT_RETURNED)
                .orderByDesc("borrow_time"));

        List<Borrow> records = resultPage.getRecords();
        Set<Integer> bookIds = records.stream()
                .map(Borrow::getBookId)
                .collect(Collectors.toSet());
        Map<Integer, Book> bookMap = bookIds.isEmpty()
                ? Map.of()
                : bookMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        PageResult<BorrowedVo> result = PageResult.of(resultPage,
                borrow -> toBorrowedVo(user, bookMap.get(borrow.getBookId()), borrow));
        return Result.success(result);
    }

    @Override
    public Result<Void> returnBook(String bookName, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("login");
        requireUser(userId);
        if (StrUtil.isBlank(bookName)) {
            throw new LibraryException(Code.INFO_EMPTY_ERROR, Code.INFO_EMPTY_ERROR_MSG);
        }

        Book book = bookMapper.selectOne(new QueryWrapper<Book>().eq("name", bookName));
        if (book == null) {
            throw new LibraryException(Code.BOOK_EXIST_ERROR, Code.BOOK_EXIST_ERROR_MSG);
        }

        synchronized (borrowReturnLock) {
            return returnInTransaction(userId, book);
        }
    }

    private Result<BorrowedVo> borrowInTransaction(User user, Book book) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Result<BorrowedVo> result = doBorrow(user, book);
            transactionManager.commit(status);
            return result;
        } catch (RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        } catch (Error e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

    private Result<Void> returnInTransaction(Integer userId, Book book) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Result<Void> result = doReturn(userId, book);
            transactionManager.commit(status);
            return result;
        } catch (RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        } catch (Error e) {
            transactionManager.rollback(status);
            throw e;
        }
    }

    private Result<BorrowedVo> doBorrow(User user, Book book) {
        long activeCount = borrowMapper.countActiveBorrow(user.getId(), book.getId());
        if (activeCount > 0) {
            throw new LibraryException(Code.BOOK_BORROWED_ERROR, Code.BOOK_BORROW_ERROR_MSG);
        }

        if (bookMapper.borrowOne(book.getId()) == 0) {
            throw new LibraryException(Code.BOOK_STOCK_ERROR, Code.BOOK_STOCK_ERROR_MSG);
        }

        Borrow record = new Borrow();
        record.setUserId(user.getId());
        record.setBookId(book.getId());
        record.setBorrowTime(LocalDateTime.now());
        record.setIsReturn(STATUS_NOT_RETURNED);
        if (borrowMapper.insert(record) == 0) {
            throw new LibraryException(Code.BOOK_BORROWED_ERROR, Code.BOOK_BORROW_ERROR_MSG);
        }

        log.info("借阅成功：userId={}, bookId={}, bookName={}", user.getId(), book.getId(), book.getName());
        return Result.success(new BorrowedVo(user.getUsername(), book.getName(), record.getBorrowTime()));
    }

    private Result<Void> doReturn(Integer userId, Book book) {
        Borrow borrow = borrowMapper.selectOne(new QueryWrapper<Borrow>()
                .eq("user_id", userId)
                .eq("book_id", book.getId())
                .eq("is_return", STATUS_NOT_RETURNED));
        if (borrow == null) {
            throw new LibraryException(Code.BOOK_RETURNING, Code.BOOK_RETURNING_MSG);
        }

        borrow.setIsReturn(STATUS_RETURNED);
        if (borrowMapper.updateById(borrow) == 0) {
            throw new LibraryException(Code.BOOK_RETURN_ERROR, Code.BOOK_RETURNING_ERROR_MSG);
        }
        if (bookMapper.returnOne(book.getId()) == 0) {
            throw new LibraryException(Code.BOOK_RETURN_ERROR, Code.BOOK_RETURNING_ERROR_MSG);
        }

        log.info("归还成功：userId={}, bookId={}, bookName={}", userId, book.getId(), book.getName());
        return Result.success();
    }

    private User requireUser(Integer userId) {
        if (userId == null) {
            throw new LibraryException(Code.USER_LOGIN_SESSION_EMPTY_ERROR,
                    Code.USER_LOGIN_SESSION_EMPTY_ERROR_MSG);
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new LibraryException(Code.USER_ERROR, Code.USER_ERROR_MSG);
        }
        return user;
    }

    private BorrowedVo toBorrowedVo(User user, Book book, Borrow borrow) {
        String bookName = UNKNOWN_BOOK_NAME;
        if (book != null && book.getName() != null) {
            bookName = book.getName();
        }
        return new BorrowedVo(user.getUsername(), bookName, borrow.getBorrowTime());
    }
}
