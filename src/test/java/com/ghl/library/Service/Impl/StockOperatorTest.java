package com.ghl.library.Service.Impl;

import com.ghl.library.Constant.StockOperation;
import com.ghl.library.Exception.LibraryException;
import com.ghl.library.Mapper.BookMapper;
import com.ghl.library.Service.StockOperator;
import com.ghl.library.Util.Code;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 库存操作策略与工厂测试。
 *
 * @author ghl
 */
@ExtendWith(MockitoExtension.class)
class StockOperatorTest {

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private AddStockOperator addStockOperator;

    @InjectMocks
    private ReduceStockOperator reduceStockOperator;

    @Test
    @DisplayName("增加库存成功")
    void addStockSuccess() {
        when(bookMapper.increaseStock("三体", 5)).thenReturn(1);

        addStockOperator.operate("三体", 5);

        assertThat(addStockOperator.type()).isEqualTo(StockOperation.ADD);
    }

    @Test
    @DisplayName("增加库存失败应抛出业务异常")
    void addStockFailure() {
        when(bookMapper.increaseStock("三体", 5)).thenReturn(0);

        LibraryException exception = null;
        try {
            addStockOperator.operate("三体", 5);
        } catch (LibraryException e) {
            exception = e;
        }

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.ADD_FAIL_ERROR);
    }

    @Test
    @DisplayName("扣减库存不足应抛出业务异常")
    void reduceStockFailure() {
        when(bookMapper.decreaseStock("三体", 5)).thenReturn(0);

        LibraryException exception = null;
        try {
            reduceStockOperator.operate("三体", 5);
        } catch (LibraryException e) {
            exception = e;
        }

        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.BOOK_STOCK_ERROR);
        assertThat(reduceStockOperator.type()).isEqualTo(StockOperation.REDUCE);
    }

    @Test
    @DisplayName("策略工厂应按类型返回对应实现")
    void factoryResolve() {
        StockOperator add = mock(StockOperator.class);
        when(add.type()).thenReturn(StockOperation.ADD);
        StockOperatorFactory factory = new StockOperatorFactory(List.of(add));

        assertThat(factory.get(StockOperation.ADD)).isSameAs(add);

        LibraryException exception = null;
        try {
            factory.get(StockOperation.REDUCE);
        } catch (LibraryException e) {
            exception = e;
        }
        assertThat(exception).isNotNull();
        assertThat(exception.getCode()).isEqualTo(Code.UNKNOWN_CODE_ERROR);
    }
}
