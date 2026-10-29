package com.ghl.library.Service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghl.library.Mapper.OperationLogMapper;
import com.ghl.library.Pojo.OperationLog;
import com.ghl.library.Pojo.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link OperationLogServiceImpl} 单元测试。
 *
 * @author ghl
 */
@ExtendWith(MockitoExtension.class)
class OperationLogServiceImplTest {

    @Mock
    private OperationLogMapper operationLogMapper;

    @InjectMocks
    private OperationLogServiceImpl operationLogService;

    @Test
    @DisplayName("写入日志应补充创建时间")
    void recordAsync() {
        OperationLog log = new OperationLog();
        log.setOperator("管理员#1");
        log.setModule("图书管理");
        log.setAction("增加库存");
        when(operationLogMapper.insert(any(OperationLog.class))).thenReturn(1);

        operationLogService.recordAsync(log);

        assertThat(log.getCreatedAt()).isNotNull();
        verify(operationLogMapper).insert(log);
    }

    @Test
    @DisplayName("日志写入失败不应影响主业务")
    void recordFailureShouldNotThrow() {
        OperationLog log = new OperationLog();
        when(operationLogMapper.insert(any(OperationLog.class)))
                .thenThrow(new RuntimeException("db down"));

        operationLogService.recordAsync(log);

        verify(operationLogMapper).insert(log);
    }

    @Test
    @DisplayName("日志分页查询应返回通用分页结果")
    void page() {
        Page<OperationLog> page = new Page<>(1, 10);
        page.setRecords(List.of(new OperationLog(1L, "管理员#1", "图书管理",
                "增加库存", "{}", 1, 5L, LocalDateTime.now())));
        page.setTotal(1);
        when(operationLogMapper.selectPage(any(Page.class), any())).thenReturn(page);

        PageResult<OperationLog> result = operationLogService.page(1, 10);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getAction()).isEqualTo("增加库存");
        assertThat(result.getTotal()).isEqualTo(1);
    }
}
