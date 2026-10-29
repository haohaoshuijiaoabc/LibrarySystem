package com.ghl.library.Controller;

import com.ghl.library.DTO.AdminLoginDTO;
import com.ghl.library.DTO.AppendBookDTO;
import com.ghl.library.DTO.TransactionBookNumbersDTO;
import com.ghl.library.Pojo.Book;
import com.ghl.library.Pojo.OperationLog;
import com.ghl.library.Pojo.PageResult;
import com.ghl.library.Pojo.Result;
import com.ghl.library.Service.AdminService;
import com.ghl.library.Service.OperationLogService;
import com.ghl.library.Service.StatisticsService;
import com.ghl.library.VO.BookStatisticsVO;
import com.ghl.library.VO.UserVO;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AdminController} 单元测试：验证接口层正确委托 Service 并返回统一响应。
 *
 * @author ghl
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @Mock
    private StatisticsService statisticsService;

    @Mock
    private OperationLogService operationLogService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AdminController adminController;

    @Test
    @DisplayName("登录接口应委托 AdminService")
    void login() {
        when(adminService.login(any(AdminLoginDTO.class), eq(session))).thenReturn(Result.success());

        Result<Void> result = adminController.login(new AdminLoginDTO("admin", "123456"), session);

        assertThat(result.getCode()).isEqualTo(200);
        verify(adminService).login(any(AdminLoginDTO.class), eq(session));
    }

    @Test
    @DisplayName("图书分页接口应委托 AdminService")
    void getBookInfo() {
        PageResult<Book> page = new PageResult<>(List.of(), 0, 10, 1, 0);
        when(adminService.getBookInfo(1, 10, "", "")).thenReturn(Result.success(page));

        Result<PageResult<Book>> result = adminController.getBookInfo(1, 10, "", "");

        assertThat(result.getData()).isSameAs(page);
    }

    @Test
    @DisplayName("用户分页接口应委托 AdminService")
    void getUserInfo() {
        PageResult<UserVO> page = new PageResult<>(List.of(), 0, 10, 1, 0);
        when(adminService.getUserInfo(1, 10, "alice", "138")).thenReturn(Result.success(page));

        Result<PageResult<UserVO>> result = adminController.getUserInfo(1, 10, "alice", "138");

        assertThat(result.getData()).isSameAs(page);
    }

    @Test
    @DisplayName("库存与图书维护接口应委托 AdminService")
    void stockOperations() {
        when(adminService.add(any(TransactionBookNumbersDTO.class))).thenReturn(Result.success());
        when(adminService.reduce(any(TransactionBookNumbersDTO.class))).thenReturn(Result.success());
        when(adminService.append(any(AppendBookDTO.class))).thenReturn(Result.success());
        when(adminService.delete(anyString())).thenReturn(Result.success());

        assertThat(adminController.addNumbers(new TransactionBookNumbersDTO("三体", 1)).getCode()).isEqualTo(200);
        assertThat(adminController.reduceNumbers(new TransactionBookNumbersDTO("三体", 1)).getCode()).isEqualTo(200);
        assertThat(adminController.append(new AppendBookDTO("三体", "刘慈欣", 1)).getCode()).isEqualTo(200);
        assertThat(adminController.deleteBooks("三体").getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("用户状态管理接口应委托 AdminService")
    void changeState() {
        when(adminService.changeState("alice")).thenReturn(Result.success());

        assertThat(adminController.changeState("alice").getCode()).isEqualTo(200);
        verify(adminService).changeState("alice");
    }

    @Test
    @DisplayName("登出接口应委托 AdminService")
    void logout() {
        when(adminService.logout(session)).thenReturn(Result.success());

        assertThat(adminController.logout(session).getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("统计接口应返回看板数据")
    void statistics() {
        BookStatisticsVO statistics = new BookStatisticsVO(1, 1, 1.0, 0, 1, List.of(), List.of());
        when(statisticsService.getStatistics()).thenReturn(statistics);

        Result<BookStatisticsVO> result = adminController.statistics();

        assertThat(result.getData()).isSameAs(statistics);
    }

    @Test
    @DisplayName("操作日志接口应返回分页日志")
    void operationLogs() {
        PageResult<OperationLog> page = new PageResult<>(List.of(), 0, 10, 1, 0);
        when(operationLogService.page(anyInt(), anyInt())).thenReturn(page);

        Result<PageResult<OperationLog>> result = adminController.operationLogs(1, 10);

        assertThat(result.getData()).isSameAs(page);
    }
}
