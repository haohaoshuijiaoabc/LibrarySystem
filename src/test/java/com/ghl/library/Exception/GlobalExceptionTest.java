package com.ghl.library.Exception;

import com.ghl.library.Pojo.Result;
import com.ghl.library.Util.Code;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link GlobalException} 单元测试。
 *
 * @author ghl
 */
class GlobalExceptionTest {

    private final GlobalException globalException = new GlobalException();

    @Test
    @DisplayName("业务异常应返回原始错误码与提示")
    void handleLibraryException() {
        Result<Void> result = globalException.handleLibraryException(
                new LibraryException(Code.BOOK_STOCK_ERROR, Code.BOOK_STOCK_ERROR_MSG));

        assertThat(result.getCode()).isEqualTo(Code.BOOK_STOCK_ERROR);
        assertThat(result.getMessage()).isEqualTo(Code.BOOK_STOCK_ERROR_MSG);
    }

    @Test
    @DisplayName("未预期异常不应泄露内部细节")
    void handleUnexpectedException() {
        Result<Void> result = globalException.handleException(
                new IllegalStateException("database password is root"));

        assertThat(result.getCode()).isEqualTo(Code.UNKNOWN_CODE_ERROR);
        assertThat(result.getMessage()).isEqualTo(Code.UNKNOWN_CODE_ERROR_MSG);
        assertThat(result.getMessage()).doesNotContain("password");
    }

    @Test
    @DisplayName("静态资源不存在应返回 404 而不是系统异常")
    void handleNoResourceFound() {
        NoResourceFoundException exception =
                new NoResourceFoundException(HttpMethod.GET, "favicon.ico");

        Result<Void> result = globalException.handleNoResourceFound(exception);

        assertThat(result.getCode()).isEqualTo(Code.NOT_FOUND_ERROR);
        assertThat(result.getMessage()).isEqualTo(Code.NOT_FOUND_ERROR_MSG);
    }
}
