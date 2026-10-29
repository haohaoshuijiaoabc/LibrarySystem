package com.ghl.library.Exception;

import com.ghl.library.Pojo.Result;
import com.ghl.library.Util.Code;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>统一拦截 Controller 层抛出的异常，将其转换为标准的 {@link Result} 响应，
 * 避免异常堆栈直接暴露给前端，同时记录服务端日志便于排查。</p>
 *
 * @author ghl
 */
@Slf4j
@RestControllerAdvice
public class GlobalException {

    /**
     * 处理业务异常（可预期的错误，如库存不足、重复借阅）。
     *
     * @param e 业务异常
     * @return 携带业务错误码的响应
     */
    @ExceptionHandler(LibraryException.class)
    public Result<Void> handleLibraryException(LibraryException e) {
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理请求参数校验失败。
     *
     * @param e 参数校验异常
     * @return 参数错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleArgumentNotValid(MethodArgumentNotValidException e) {
        String message = Code.PARAM_ERROR_MSG;
        FieldError fieldError = e.getBindingResult().getFieldError();
        if (fieldError != null && fieldError.getDefaultMessage() != null) {
            message = fieldError.getField() + ": " + fieldError.getDefaultMessage();
        }
        log.warn("参数校验失败：{}", message);
        return Result.error(Code.PARAM_ERROR, message);
    }

    /**
     * 处理缺少必填请求参数。
     *
     * @param e 缺参异常
     * @return 参数错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数：{}", e.getParameterName());
        return Result.error(Code.PARAM_ERROR, "缺少请求参数：" + e.getParameterName());
    }

    /**
     * 处理请求体格式错误（如 JSON 无法解析）。
     *
     * @param e 请求体解析异常
     * @return 参数错误响应
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return Result.error(Code.PARAM_ERROR, Code.PARAM_ERROR_MSG);
    }

    /**
     * 兜底处理未预期异常，对外只返回通用提示，避免泄露内部实现。
     *
     * @param e 未预期异常
     * @return 系统错误响应
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(Code.UNKNOWN_CODE_ERROR, Code.UNKNOWN_CODE_ERROR_MSG);
    }
}
