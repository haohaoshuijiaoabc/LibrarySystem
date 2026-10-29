package com.ghl.library.Service;

import com.ghl.library.Pojo.OperationLog;
import com.ghl.library.Pojo.PageResult;

/**
 * 操作日志服务。
 *
 * @author ghl
 */
public interface OperationLogService {

    /**
     * 异步写入操作日志。
     *
     * @param operationLog 日志内容
     */
    void recordAsync(OperationLog operationLog);

    /**
     * 分页查询操作日志，按时间倒序。
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @return 分页日志
     */
    PageResult<OperationLog> page(int pageNum, int pageSize);
}
