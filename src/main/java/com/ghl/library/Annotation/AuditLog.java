package com.ghl.library.Annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务操作日志注解。
 *
 * <p>标注在 Controller 方法上，由 {@code AuditLogAspect} 通过 Spring AOP 拦截，
 * 记录操作人、模块、动作、耗时与执行结果，并交由自定义线程池异步落库。</p>
 *
 * @author ghl
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * 业务模块名称，如“图书管理”。
     *
     * @return 模块名称
     */
    String module();

    /**
     * 操作动作，如“新增库存”。
     *
     * @return 操作动作
     */
    String action();
}
