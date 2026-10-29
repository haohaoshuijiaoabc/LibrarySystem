package com.ghl.library.Pojo;

import com.ghl.library.Util.Code;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应实体。
 *
 * <p>所有 Controller 统一返回该结构，前端只需判断 {@code code} 字段：
 * {@code 200} 表示业务成功，其余为业务错误码。</p>
 *
 * @param <T> 业务数据类型，无数据时使用 {@link Void}
 * @author ghl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务状态码，见 {@link Code} */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 业务数据 */
    private T data;

    /**
     * 构造无数据的成功响应。
     *
     * @param <T> 业务数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(Code.SUCCESS, Code.SUCCESS_MSG, null);
    }

    /**
     * 构造携带数据的成功响应。
     *
     * @param data 业务数据
     * @param <T>  业务数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(Code.SUCCESS, Code.SUCCESS_MSG, data);
    }

    /**
     * 构造失败响应。
     *
     * @param code    业务错误码
     * @param message 错误提示
     * @param <T>     业务数据类型
     * @return 失败响应
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
