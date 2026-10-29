package com.ghl.library.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghl.library.Exception.LibraryException;
import com.ghl.library.Mapper.BookMapper;
import com.ghl.library.Mapper.BorrowMapper;
import com.ghl.library.Mapper.UserMapper;
import com.ghl.library.Pojo.Book;
import com.ghl.library.Pojo.Borrow;
import com.ghl.library.Pojo.PageResult;
import com.ghl.library.Pojo.Result;
import com.ghl.library.Pojo.User;
import com.ghl.library.Util.Code;
import com.ghl.library.VO.BorrowedVo;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link BorrowServiceImpl} 单元测试。
 *
 * @author ghl
 */
@ExtendWith(MockitoExtension.class)
class BorrowServiceImplTest {

    @Mock
    private BookMapper bookMapper;

    @Mock
    private BorrowMapper borrowMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private HttpSession session;

    @InjectMocks
    private BorrowServiceImpl borrowService;

    /**
     * 单元测试用空事务管理器：直接执行回调，不开启真实数据库事务。
     */
    private static final PlatformTransactionManager NOOP_TRANSACTION_MANAGER = new PlatformTransactionManager() {
        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
            // 测试环境无真实事务，无需处理
        }

        @Override
        public void rollback(TransactionStatus status) {
            // 测试环境无真实事务，无需处理
        }
    };

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(borrowService, "transactionManager", NOOP_TRANSACTION_MANAGER);
    }

    @Test
    @DisplayName("借阅成功应扣减库存并写入记录")
    void borrowSuccess() {
        User user = new User(1, "alice", "123456", "13800000001", 0, 0);
        Book book = new Book(10, "三体", "刘慈欣", 5);
        when(userMapper.selectById(1)).thenReturn(user);
        when(bookMapper.selectById(10)).thenReturn(book);
        when(borrowMapper.countActiveBorrow(1, 10)).thenReturn(0L);
        when(bookMapper.borrowOne(10)).thenReturn(1);
        when(borrowMapper.insert(any(Borrow.class))).thenReturn(1);

        Result<BorrowedVo> result = borrowService.borrow(1, 10);

        assertThat(result.getCode()).isEqualTo(Code.SUCCESS);
        assertThat(result.getData().getUsername()).isEqualTo("alice");
        assertThat(result.getData().getBookname()).isEqualTo("三体");
        verify(bookMapper).borrowOne(10);
        verify(borrowMapper).insert(any(Borrow.class));
    }

    @Test
    @DisplayName("重复借阅同一本书应被拒绝")
    void borrowDuplicate() {
        when(userMapper.selectById(1))
                .thenReturn(new User(1, "alice", "123456", null, 0, 0));
        when(bookMapper.selectById(10)).thenReturn(new Book(10, "三体", "刘慈欣", 5));
        when(borrowMapper.countActiveBorrow(1, 10)).thenReturn(1L);

        LibraryException exception = borrowAndCatch(1, 10);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.BOOK_BORROWED_ERROR);
        verify(bookMapper, never()).borrowOne(anyInt());
    }

    @Test
    @DisplayName("库存不足时应抛出库存异常")
    void borrowWithoutStock() {
        when(userMapper.selectById(1))
                .thenReturn(new User(1, "alice", "123456", null, 0, 0));
        when(bookMapper.selectById(10)).thenReturn(new Book(10, "三体", "刘慈欣", 0));
        when(borrowMapper.countActiveBorrow(1, 10)).thenReturn(0L);
        when(bookMapper.borrowOne(10)).thenReturn(0);

        LibraryException exception = borrowAndCatch(1, 10);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.BOOK_STOCK_ERROR);
        verify(borrowMapper, never()).insert(any(Borrow.class));
    }

    @Test
    @DisplayName("未登录用户借阅应被拒绝")
    void borrowWithoutLogin() {
        LibraryException exception = borrowAndCatch(null, 10);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.USER_LOGIN_SESSION_EMPTY_ERROR);
        verify(userMapper, never()).selectById(any());
    }

    @Test
    @DisplayName("图书不存在时应报错")
    void borrowMissingBook() {
        when(userMapper.selectById(1))
                .thenReturn(new User(1, "alice", "123456", null, 0, 0));
        when(bookMapper.selectById(10)).thenReturn(null);

        LibraryException exception = borrowAndCatch(1, 10);

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.BOOK_EXIST_ERROR);
    }

    @Test
    @DisplayName("借阅记录分页应批量转换书名")
    void borrowedPage() {
        when(session.getAttribute("login")).thenReturn(1);
        when(userMapper.selectById(1))
                .thenReturn(new User(1, "alice", "123456", null, 0, 0));

        Page<Borrow> page = new Page<>(1, 10);
        page.setRecords(List.of(
                new Borrow(1, 1, 10, LocalDateTime.of(2026, 1, 1, 10, 0), 0),
                new Borrow(2, 1, 11, LocalDateTime.of(2026, 1, 2, 10, 0), 0)));
        page.setTotal(2);
        when(borrowMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);
        when(bookMapper.selectBatchIds(any())).thenReturn(List.of(
                new Book(10, "三体", "刘慈欣", 5),
                new Book(11, "活着", "余华", 3)));

        Result<PageResult<BorrowedVo>> result = borrowService.borrowed(1, 10, session);

        assertThat(result.getCode()).isEqualTo(Code.SUCCESS);
        assertThat(result.getData().getRecords()).hasSize(2);
        assertThat(result.getData().getRecords().get(0).getBookname()).isEqualTo("三体");
        assertThat(result.getData().getRecords().get(1).getBookname()).isEqualTo("活着");
        assertThat(result.getData().getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("归还成功应更新借阅记录并增加库存")
    void returnSuccess() {
        when(session.getAttribute("login")).thenReturn(1);
        when(userMapper.selectById(1))
                .thenReturn(new User(1, "alice", "123456", null, 0, 0));
        when(bookMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new Book(10, "三体", "刘慈欣", 5));
        when(borrowMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new Borrow(1, 1, 10, LocalDateTime.now(), 0));
        when(borrowMapper.updateById(any(Borrow.class))).thenReturn(1);
        when(bookMapper.returnOne(10)).thenReturn(1);

        Result<Void> result = borrowService.returnBook("三体", session);

        assertThat(result.getCode()).isEqualTo(Code.SUCCESS);
        verify(borrowMapper).updateById(any(Borrow.class));
        verify(bookMapper).returnOne(10);
    }

    @Test
    @DisplayName("重复归还应报错")
    void returnTwice() {
        when(session.getAttribute("login")).thenReturn(1);
        when(userMapper.selectById(1))
                .thenReturn(new User(1, "alice", "123456", null, 0, 0));
        when(bookMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new Book(10, "三体", "刘慈欣", 5));
        when(borrowMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        LibraryException exception = returnAndCatch("三体");

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.BOOK_RETURNING);
        verify(bookMapper, never()).returnOne(anyInt());
    }

    @Test
    @DisplayName("归还不存在的图书应报错")
    void returnMissingBook() {
        when(session.getAttribute("login")).thenReturn(1);
        when(userMapper.selectById(1))
                .thenReturn(new User(1, "alice", "123456", null, 0, 0));
        when(bookMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        LibraryException exception = returnAndCatch("不存在");

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.BOOK_EXIST_ERROR);
        verify(borrowMapper, never()).selectOne(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("未登录归还应被拒绝")
    void returnWithoutLogin() {
        when(session.getAttribute("login")).thenReturn(null);

        LibraryException exception = returnAndCatch("三体");

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.USER_LOGIN_SESSION_EMPTY_ERROR);
        verify(bookMapper, never()).selectOne(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("归还空书名应报参数错误")
    void returnWithBlankName() {
        when(session.getAttribute("login")).thenReturn(1);
        when(userMapper.selectById(1))
                .thenReturn(new User(1, "alice", "123456", null, 0, 0));

        LibraryException exception = returnAndCatch(" ");

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.INFO_EMPTY_ERROR);
    }

    private LibraryException borrowAndCatch(Integer userId, Integer bookId) {
        try {
            borrowService.borrow(userId, bookId);
        } catch (LibraryException e) {
            return e;
        }
        return null;
    }

    private LibraryException returnAndCatch(String bookName) {
        try {
            borrowService.returnBook(bookName, session);
        } catch (LibraryException e) {
            return e;
        }
        return null;
    }
}
