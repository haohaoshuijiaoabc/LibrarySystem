package com.ghl.library.Exception;

/**
 * 业务异常。
 *
 * <p>业务代码中主动抛出该异常，由 {@link GlobalException} 统一转换为
 * {@link com.ghl.library.Pojo.Result} 响应，避免在 Controller 中编写大量错误分支。</p>
 *
 * @author ghl
 */
public class LibraryException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务错误码 */
    private final Integer code;

    /**
     * 构造业务异常。
     *
     * @param code    业务错误码
     * @param message 错误提示
     */
    public LibraryException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取业务错误码。
     *
     * @return 业务错误码
     */
    public Integer getCode() {
        return code;
    }
}
