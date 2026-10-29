package com.ghl.library.Pojo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ghl.library.Util.Code;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link Result} 与 {@link PageResult} 通用组件测试。
 *
 * @author ghl
 */
class ResultTest {

    @Test
    @DisplayName("成功响应应携带业务数据")
    void successWithData() {
        Result<String> result = Result.success("hello");

        assertThat(result.getCode()).isEqualTo(Code.SUCCESS);
        assertThat(result.getMessage()).isEqualTo(Code.SUCCESS_MSG);
        assertThat(result.getData()).isEqualTo("hello");
    }

    @Test
    @DisplayName("成功响应可以不携带数据")
    void successWithoutData() {
        Result<Void> result = Result.success();

        assertThat(result.getCode()).isEqualTo(Code.SUCCESS);
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("失败响应应携带错误码与提示")
    void error() {
        Result<Void> result = Result.error(Code.BOOK_STOCK_ERROR, Code.BOOK_STOCK_ERROR_MSG);

        assertThat(result.getCode()).isEqualTo(Code.BOOK_STOCK_ERROR);
        assertThat(result.getMessage()).isEqualTo(Code.BOOK_STOCK_ERROR_MSG);
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("分页结果应保留全部分页字段")
    void pageResultShouldKeepFields() {
        Page<Book> source = new Page<>(2, 2);
        source.setRecords(List.of(new Book(1, "三体", "刘慈欣", 10)));
        source.setTotal(6);

        PageResult<Book> result = PageResult.of(source);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getName()).isEqualTo("三体");
        assertThat(result.getTotal()).isEqualTo(6);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getCurrent()).isEqualTo(2);
        assertThat(result.getPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("分页结果应支持实体到 VO 的映射")
    void pageResultMapping() {
        Page<Book> source = new Page<>(1, 2);
        source.setRecords(List.of(
                new Book(1, "三体", "刘慈欣", 10),
                new Book(2, "活着", "余华", 20)));
        source.setTotal(2);

        PageResult<String> result = PageResult.of(source, Book::getName);

        assertThat(result.getRecords()).containsExactly("三体", "活着");
        assertThat(result.getTotal()).isEqualTo(2);
    }
}
