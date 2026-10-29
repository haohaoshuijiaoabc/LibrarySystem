package com.ghl.library.Controller;

import com.ghl.library.DTO.ReturnBookDTO;
import com.ghl.library.DTO.UserLoginDTO;
import com.ghl.library.Pojo.Book;
import com.ghl.library.Pojo.PageResult;
import com.ghl.library.Pojo.Result;
import com.ghl.library.Service.BookService;
import com.ghl.library.Service.BorrowService;
import com.ghl.library.Service.UserService;
import com.ghl.library.VO.BorrowedVo;
import com.ghl.library.VO.UserLoginVO;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UserController} 单元测试。
 *
 * @author ghl
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private BookService bookService;

    @Mock
    private BorrowService borrowService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("登录接口应委托 UserService")
    void login() {
        when(userService.login(any(UserLoginDTO.class), eq(session))).thenReturn(Result.success());

        assertThat(userController.login(new UserLoginDTO("alice", "123456"), session).getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("用户信息接口应委托 UserService")
    void getInfo() {
        UserLoginVO vo = new UserLoginVO(1, "alice", "13800000001");
        when(userService.getInfo(session)).thenReturn(Result.success(vo));

        assertThat(userController.getInfo(session).getData()).isSameAs(vo);
    }

    @Test
    @DisplayName("图书查询接口应委托 BookService")
    void getAllBooks() {
        List<Book> books = List.of(new Book(1, "三体", "刘慈欣", 10));
        when(bookService.query("三体", "刘慈欣")).thenReturn(Result.success(books));

        assertThat(userController.getAllBooks("三体", "刘慈欣").getData()).hasSize(1);
    }

    @Test
    @DisplayName("借阅接口应将会话用户 ID 传给 BorrowService")
    void borrowBook() {
        BorrowedVo vo = new BorrowedVo("alice", "三体", null);
        when(session.getAttribute("login")).thenReturn(1);
        when(borrowService.borrow(1, 10)).thenReturn(Result.success(vo));

        Result<BorrowedVo> result = userController.borrowBook(10, session);

        assertThat(result.getData()).isSameAs(vo);
        verify(borrowService).borrow(1, 10);
    }

    @Test
    @DisplayName("借阅记录接口应委托 BorrowService")
    void borrowed() {
        PageResult<BorrowedVo> page = new PageResult<>(List.of(), 0, 10, 1, 0);
        when(borrowService.borrowed(anyInt(), anyInt(), eq(session))).thenReturn(Result.success(page));

        assertThat(userController.getBorrower(1, 10, session).getData()).isSameAs(page);
    }

    @Test
    @DisplayName("归还接口应委托 BorrowService")
    void returnBook() {
        when(borrowService.returnBook("三体", session)).thenReturn(Result.success());

        assertThat(userController.returnBook(new ReturnBookDTO("三体"), session).getCode()).isEqualTo(200);
        verify(borrowService).returnBook("三体", session);
    }

    @Test
    @DisplayName("登出接口应委托 UserService")
    void logout() {
        when(userService.logout(session)).thenReturn(Result.success());

        assertThat(userController.logout(session).getCode()).isEqualTo(200);
    }
}
