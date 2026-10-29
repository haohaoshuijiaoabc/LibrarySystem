package com.ghl.library.Pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作日志实体。
 *
 * <p>由 {@code AuditLogAspect} 在业务方法执行后异步写入，用于关键业务操作留痕。</p>
 *
 * @author ghl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("operation_log")
public class OperationLog {

    /** 日志 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人标识，如 {@code 管理员#1}、{@code 用户#3} */
    private String operator;

    /** 业务模块 */
    private String module;

    /** 操作动作 */
    private String action;

    /** 请求参数/异常摘要 */
    private String detail;

    /** 是否成功：1-成功，0-失败 */
    private Integer success;

    /** 方法执行耗时（毫秒） */
    @TableField("cost_ms")
    private Long costMs;

    /** 记录时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
