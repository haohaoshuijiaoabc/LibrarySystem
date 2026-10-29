package com.ghl.library.Service.Impl;

import com.ghl.library.Constant.StockOperation;
import com.ghl.library.Exception.LibraryException;
import com.ghl.library.Mapper.BookMapper;
import com.ghl.library.Service.StockOperator;
import com.ghl.library.Util.Code;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 扣减库存策略。
 *
 * <p>借助 {@code WHERE stock >= #{numbers}} 的条件更新，即使并发扣减也不会出现负库存。</p>
 *
 * @author ghl
 */
@Component
public class ReduceStockOperator implements StockOperator {

    @Resource
    private BookMapper bookMapper;

    @Override
    public String type() {
        return StockOperation.REDUCE;
    }

    @Override
    public void operate(String bookName, int numbers) {
        int rows = bookMapper.decreaseStock(bookName, numbers);
        if (rows == 0) {
            throw new LibraryException(Code.BOOK_STOCK_ERROR, Code.BOOK_STOCK_ERROR_MSG);
        }
    }
}
