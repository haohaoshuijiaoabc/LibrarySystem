package com.ghl.library.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ghl.library.Pojo.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志数据访问层。
 *
 * @author ghl
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
