package com.ghl.library.AOP;

import com.ghl.library.Annotation.AuditLog;
import com.ghl.library.DTO.TransactionBookNumbersDTO;
import com.ghl.library.Exception.LibraryException;
import com.ghl.library.Pojo.OperationLog;
import com.ghl.library.Service.OperationLogService;
import com.ghl.library.Util.Code;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link AuditLogAspect} 单元测试。
 *
 * @author ghl
 */
@ExtendWith(MockitoExtension.class)
class AuditLogAspectTest {

    @Mock
    private OperationLogService operationLogService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private AuditLog auditLog;

    @InjectMocks
    private AuditLogAspect auditLogAspect;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("方法执行成功应记录成功日志与耗时")
    void aroundSuccess() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{new TransactionBookNumbersDTO("三体", 2)});
        when(joinPoint.proceed()).thenReturn("ok");
        when(auditLog.module()).thenReturn("图书管理");
        when(auditLog.action()).thenReturn("增加库存");

        Object result = auditLogAspect.around(joinPoint, auditLog);

        ArgumentCaptor<OperationLog> captor = ArgumentCaptor.forClass(OperationLog.class);
        verify(operationLogService).recordAsync(captor.capture());
        OperationLog log = captor.getValue();
        assertThat(result).isEqualTo("ok");
        assertThat(log.getModule()).isEqualTo("图书管理");
        assertThat(log.getAction()).isEqualTo("增加库存");
        assertThat(log.getSuccess()).isEqualTo(1);
        assertThat(log.getCostMs()).isNotNull();
        assertThat(log.getCostMs()).isGreaterThanOrEqualTo(0L);
        assertThat(log.getDetail()).contains("三体");
        assertThat(log.getOperator()).isEqualTo("系统");
    }

    @Test
    @DisplayName("方法执行失败应记录失败日志并继续抛出原异常")
    void aroundFailure() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{new TransactionBookNumbersDTO("三体", 2)});
        when(joinPoint.proceed()).thenThrow(new LibraryException(Code.BOOK_STOCK_ERROR, Code.BOOK_STOCK_ERROR_MSG));
        when(auditLog.module()).thenReturn("图书管理");
        when(auditLog.action()).thenReturn("增加库存");

        LibraryException exception = null;
        try {
            auditLogAspect.around(joinPoint, auditLog);
        } catch (LibraryException e) {
            exception = e;
        }

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.BOOK_STOCK_ERROR);

        ArgumentCaptor<OperationLog> captor = ArgumentCaptor.forClass(OperationLog.class);
        verify(operationLogService).recordAsync(captor.capture());
        assertThat(captor.getValue().getSuccess()).isZero();
        assertThat(captor.getValue().getDetail()).contains("异常");
    }

    @Test
    @DisplayName("管理员会话中的操作应记录管理员标识")
    void resolveAdminOperator() throws Throwable {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("admin", 2);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(joinPoint.getArgs()).thenReturn(new Object[]{new TransactionBookNumbersDTO("三体", 2)});
        when(joinPoint.proceed()).thenReturn("ok");
        when(auditLog.module()).thenReturn("图书管理");
        when(auditLog.action()).thenReturn("增加库存");

        auditLogAspect.around(joinPoint, auditLog);

        ArgumentCaptor<OperationLog> captor = ArgumentCaptor.forClass(OperationLog.class);
        verify(operationLogService).recordAsync(captor.capture());
        assertThat(captor.getValue().getOperator()).isEqualTo("管理员#2");
    }

    @Test
    @DisplayName("无会话时应记录匿名操作")
    void resolveAnonymousOperator() throws Throwable {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(joinPoint.getArgs()).thenReturn(new Object[]{new TransactionBookNumbersDTO("三体", 2)});
        when(joinPoint.proceed()).thenReturn("ok");
        when(auditLog.module()).thenReturn("图书管理");
        when(auditLog.action()).thenReturn("增加库存");

        auditLogAspect.around(joinPoint, auditLog);

        ArgumentCaptor<OperationLog> captor = ArgumentCaptor.forClass(OperationLog.class);
        verify(operationLogService).recordAsync(captor.capture());
        assertThat(captor.getValue().getOperator()).isEqualTo("匿名");
    }

    @Test
    @DisplayName("异步日志提交失败不应影响主业务返回")
    void recordFailureShouldNotBreakBusiness() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{new TransactionBookNumbersDTO("三体", 2)});
        when(joinPoint.proceed()).thenReturn("ok");
        when(auditLog.module()).thenReturn("图书管理");
        when(auditLog.action()).thenReturn("增加库存");
        doThrow(new RuntimeException("executor shutdown")).when(operationLogService).recordAsync(any());

        assertThat(auditLogAspect.around(joinPoint, auditLog)).isEqualTo("ok");
    }
}
