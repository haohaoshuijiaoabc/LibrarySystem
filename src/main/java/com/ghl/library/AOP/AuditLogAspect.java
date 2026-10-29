package com.ghl.library.AOP;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghl.library.Annotation.AuditLog;
import com.ghl.library.Pojo.OperationLog;
import com.ghl.library.Service.OperationLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.StringJoiner;

/**
 * 操作日志切面。
 *
 * <p>拦截标注了 {@link AuditLog} 的方法，统计执行耗时与成功状态，
 * 解析当前会话的操作用户，并将日志提交到异步线程池落库。</p>
 *
 * @author ghl
 */
@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    /** 日志详情最大长度，防止超长请求参数撑爆字段 */
    private static final int DETAIL_MAX_LENGTH = 400;

    /** 毫秒换算基数 */
    private static final long NANOS_PER_MILLIS = 1_000_000L;

    /** 参数序列化器，仅用于日志详情，配置在静态常量中线程安全 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Resource
    private OperationLogService operationLogService;

    /**
     * 环绕通知：执行目标方法并记录操作日志。
     *
     * @param joinPoint 连接点
     * @param auditLog  方法上的日志注解
     * @return 目标方法返回值
     * @throws Throwable 目标方法抛出的异常
     */
    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startNanos = System.nanoTime();
        String detail = buildDetail(joinPoint);
        int success = 1;
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            success = 0;
            detail = truncate(detail + " | 异常：" + throwable.getMessage());
            throw throwable;
        } finally {
            OperationLog operationLog = new OperationLog();
            operationLog.setOperator(resolveOperator());
            operationLog.setModule(auditLog.module());
            operationLog.setAction(auditLog.action());
            operationLog.setDetail(detail);
            operationLog.setSuccess(success);
            operationLog.setCostMs((System.nanoTime() - startNanos) / NANOS_PER_MILLIS);
            recordQuietly(operationLog);
        }
    }

    private void recordQuietly(OperationLog operationLog) {
        try {
            operationLogService.recordAsync(operationLog);
        } catch (Exception e) {
            log.warn("提交操作日志失败：{}", operationLog, e);
        }
    }

    private String buildDetail(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = joinPoint.getSignature() instanceof MethodSignature signature
                ? signature.getParameterNames()
                : new String[0];
        StringJoiner detail = new StringJoiner(", ");
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof HttpSession || arg instanceof ServletRequest || arg instanceof ServletResponse) {
                continue;
            }
            String name = i < parameterNames.length ? parameterNames[i] : "arg" + i;
            detail.add(name + "=" + toJsonQuietly(arg));
        }
        return truncate(detail.toString());
    }

    private String toJsonQuietly(Object arg) {
        try {
            return OBJECT_MAPPER.writeValueAsString(arg);
        } catch (JsonProcessingException e) {
            log.debug("操作日志参数序列化失败，退化为 toString", e);
            return String.valueOf(arg);
        }
    }

    private String resolveOperator() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return "系统";
        }
        HttpSession session = servletAttributes.getRequest().getSession(false);
        if (session == null) {
            return "匿名";
        }
        Object adminId = session.getAttribute("admin");
        if (adminId != null) {
            return "管理员#" + adminId;
        }
        Object userId = session.getAttribute("login");
        if (userId != null) {
            return "用户#" + userId;
        }
        return "匿名";
    }

    private String truncate(String detail) {
        if (detail == null) {
            return "";
        }
        return detail.length() <= DETAIL_MAX_LENGTH ? detail : detail.substring(0, DETAIL_MAX_LENGTH);
    }
}
