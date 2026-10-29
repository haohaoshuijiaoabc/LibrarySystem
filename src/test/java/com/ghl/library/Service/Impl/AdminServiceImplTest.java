package com.ghl.library.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghl.library.Constant.StockOperation;
import com.ghl.library.DTO.AdminLoginDTO;
import com.ghl.library.DTO.AppendBookDTO;
import com.ghl.library.DTO.TransactionBookNumbersDTO;
import com.ghl.library.Exception.LibraryException;
import com.ghl.library.Mapper.AdminMapper;
import com.ghl.library.Mapper.BookMapper;
import com.ghl.library.Mapper.UserMapper;
import com.ghl.library.Pojo.Admin;
import com.ghl.library.Pojo.Book;
import com.ghl.library.Pojo.PageResult;
import com.ghl.library.Pojo.Result;
import com.ghl.library.Pojo.User;
import com.ghl.library.Service.StockOperator;
import com.ghl.library.Util.Code;
import com.ghl.library.VO.UserVO;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AdminServiceImpl} 单元测试。
 *
 * @author ghl
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private AdminMapper adminMapper;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private StockOperatorFactory stockOperatorFactory;

    @Mock
    private StockOperator stockOperator;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    @DisplayName("管理员登录成功应写入会话")
    void loginSuccess() {
        Admin admin = new Admin(1, "admin", "123456", 0, 0);
        when(adminMapper.selectOne(any(QueryWrapper.class))).thenReturn(admin);

        Result<Void> result = adminService.login(new AdminLoginDTO("admin", "123456"), session);

        assertThat(result.getCode()).isEqualTo(Code.SUCCESS);
        verify(session).setAttribute("admin", 1);
    }

    @Test
    @DisplayName("账号密码为空应返回参数错误")
    void loginWithBlankInput() {
        Result<Void> result = adminService.login(new AdminLoginDTO(" ", ""), session);

        assertThat(result.getCode()).isEqualTo(Code.INFO_EMPTY_ERROR);
        verify(adminMapper, never()).selectOne(any());
    }

    @Test
    @DisplayName("密码错误应抛出业务异常")
    void loginWithWrongPassword() {
        when(adminMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new Admin(1, "admin", "123456", 0, 0));

        LibraryException exception = loginAndCatch(new AdminLoginDTO("admin", "bad"));

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.USER_OR_PASSWORD_ERROR);
        verify(session, never()).setAttribute(anyString(), any());
    }

    @Test
    @DisplayName("图书分页查询应返回通用分页组件")
    void getBookInfo() {
        Page<Book> page = new Page<>(1, 2);
        page.setRecords(List.of(new Book(1, "三体", "刘慈欣", 10)));
        page.setTotal(5);
        when(bookMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        Result<PageResult<Book>> result = adminService.getBookInfo(1, 2, "三体", "");

        assertThat(result.getCode()).isEqualTo(Code.SUCCESS);
        assertThat(result.getData().getRecords()).hasSize(1);
        assertThat(result.getData().getRecords().get(0).getName()).isEqualTo("三体");
        assertThat(result.getData().getTotal()).isEqualTo(5);
        assertThat(result.getData().getPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("用户分页查询应转换成脱敏 VO")
    void getUserInfo() {
        Page<User> page = new Page<>(1, 10);
        page.setRecords(List.of(new User(1, "alice", "123456", "13800000001", 0, 0)));
        page.setTotal(1);
        when(userMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

        Result<PageResult<UserVO>> result = adminService.getUserInfo(1, 10, "alice", "");

        UserVO vo = result.getData().getRecords().get(0);
        assertThat(vo.getUsername()).isEqualTo("alice");
        assertThat(vo.getPhone()).isEqualTo("13800000001");
        assertThat(result.getData().getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("增加库存应委托给对应策略")
    void addStock() {
        when(bookMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new Book(1, "三体", "刘慈欣", 10));
        when(stockOperatorFactory.get(StockOperation.ADD)).thenReturn(stockOperator);

        Result<Void> result = adminService.add(new TransactionBookNumbersDTO("三体", 5));

        assertThat(result.getCode()).isEqualTo(Code.SUCCESS);
        verify(stockOperator).operate("三体", 5);
    }

    @Test
    @DisplayName("库存变更数量非法时应拒绝")
    void addStockWithInvalidNumbers() {
        LibraryException exception = addAndCatch(new TransactionBookNumbersDTO("三体", 0));

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.ADD_NUMBER_ERROR);
        verify(stockOperatorFactory, never()).get(any());
    }

    @Test
    @DisplayName("扣减不存在的图书应报错")
    void reduceMissingBook() {
        when(bookMapper.selectOne(any(QueryWrapper.class))).thenReturn(null);

        LibraryException exception = reduceAndCatch(new TransactionBookNumbersDTO("不存在", 1));

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.BOOK_EXIST_ERROR);
    }

    @Test
    @DisplayName("扣减库存应委托给对应策略")
    void reduceStock() {
        when(bookMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new Book(1, "三体", "刘慈欣", 10));
        when(stockOperatorFactory.get(StockOperation.REDUCE)).thenReturn(stockOperator);

        Result<Void> result = adminService.reduce(new TransactionBookNumbersDTO("三体", 2));

        assertThat(result.getCode()).isEqualTo(Code.SUCCESS);
        verify(stockOperator).operate("三体", 2);
    }

    @Test
    @DisplayName("新增图书信息不完整应报错")
    void appendWithBlankInfo() {
        LibraryException exception = appendAndCatch(new AppendBookDTO("", "", 1));

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.APPEND_BOOK_INFO_ERROR);
    }

    @Test
    @DisplayName("新增已存在图书应报错")
    void appendDuplicateBook() {
        when(bookMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new Book(1, "三体", "刘慈欣", 10));

        LibraryException exception = appendAndCatch(new AppendBookDTO("三体", "刘慈欣", 1));

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.BOOK_EXIST);
    }

    @Test
    @DisplayName("删除仍有库存的图书应报错")
    void deleteBookWithStock() {
        when(bookMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new Book(1, "三体", "刘慈欣", 10));

        LibraryException exception = deleteAndCatch("三体");

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.BOOK_STOCK_EXIST);
        verify(bookMapper, never()).delete(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("删除零库存图书应成功")
    void deleteEmptyStockBook() {
        when(bookMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new Book(1, "三体", "刘慈欣", 0));
        when(bookMapper.delete(any(QueryWrapper.class))).thenReturn(1);

        Result<Void> result = adminService.delete("三体");

        assertThat(result.getCode()).isEqualTo(Code.SUCCESS);
        verify(bookMapper).delete(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("封禁用户应把状态置为 1")
    void lockUser() {
        when(userMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new User(1, "alice", "123456", "13800000001", 0, 0));
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        adminService.changeState("alice");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertThat(captor.getValue().getIsLocked()).isEqualTo(1);
    }

    @Test
    @DisplayName("解封用户应清除密码错误次数")
    void unlockUser() {
        when(userMapper.selectOne(any(QueryWrapper.class)))
                .thenReturn(new User(1, "alice", "123456", "13800000001", 1, 3));
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        adminService.changeState("alice");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).updateById(captor.capture());
        assertThat(captor.getValue().getIsLocked()).isZero();
        assertThat(captor.getValue().getPasswordErrorCount()).isZero();
    }

    private LibraryException loginAndCatch(AdminLoginDTO dto) {
        try {
            adminService.login(dto, session);
        } catch (LibraryException e) {
            return e;
        }
        return null;
    }

    private LibraryException addAndCatch(TransactionBookNumbersDTO dto) {
        try {
            adminService.add(dto);
        } catch (LibraryException e) {
            return e;
        }
        return null;
    }

    private LibraryException reduceAndCatch(TransactionBookNumbersDTO dto) {
        try {
            adminService.reduce(dto);
        } catch (LibraryException e) {
            return e;
        }
        return null;
    }

    private LibraryException appendAndCatch(AppendBookDTO dto) {
        try {
            adminService.append(dto);
        } catch (LibraryException e) {
            return e;
        }
        return null;
    }

    private LibraryException deleteAndCatch(String name) {
        try {
            adminService.delete(name);
        } catch (LibraryException e) {
            return e;
        }
        return null;
    }
}
