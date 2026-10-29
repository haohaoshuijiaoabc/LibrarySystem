package com.ghl.library.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ghl.library.Mapper.OperationLogMapper;
import com.ghl.library.Pojo.OperationLog;
import com.ghl.library.Pojo.PageResult;
import com.ghl.library.Service.OperationLogService;
import com.ghl.library.Util.PageUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现。
 *
 * <p>写日志使用 {@code @Async} 提交到自定义业务线程池，避免日志落库耗时
 * 拖慢主业务请求；查询仍在主线程执行。</p>
 *
 * @author ghl
 */
@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    /**
     * 异步写入操作日志。
     *
     * <p>日志属于旁路功能，写入失败不能影响主业务，因此这里捕获异常并记录错误日志，
     * 而不是让异常在异步线程中静默丢失。</p>
     *
     * @param operationLog 日志内容
     */
    @Async("libraryTaskExecutor")
    @Override
    public void recordAsync(OperationLog operationLog) {
        try {
            operationLog.setId(null);
            operationLog.setCreatedAt(LocalDateTime.now());
            operationLogMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("操作日志写入失败：operator={}, action={}",
                    operationLog.getOperator(), operationLog.getAction(), e);
        }
    }

    @Override
    public PageResult<OperationLog> page(int pageNum, int pageSize) {
        IPage<OperationLog> page = PageUtils.of(pageNum, pageSize);
        IPage<OperationLog> resultPage = operationLogMapper.selectPage(page, new LambdaQueryWrapper<OperationLog>()
                .orderByDesc(OperationLog::getCreatedAt)
                .orderByDesc(OperationLog::getId));
        return PageResult.of(resultPage);
    }
}
