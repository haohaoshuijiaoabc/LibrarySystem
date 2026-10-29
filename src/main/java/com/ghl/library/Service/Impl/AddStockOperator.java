package com.ghl.library.Service.Impl;

import com.ghl.library.Constant.StockOperation;
import com.ghl.library.Exception.LibraryException;
import com.ghl.library.Mapper.BookMapper;
import com.ghl.library.Service.StockOperator;
import com.ghl.library.Util.Code;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 增加库存策略。
 *
 * @author ghl
 */
@Component
public class AddStockOperator implements StockOperator {

    @Resource
    private BookMapper bookMapper;

    @Override
    public String type() {
        return StockOperation.ADD;
    }

    @Override
    public void operate(String bookName, int numbers) {
        int rows = bookMapper.increaseStock(bookName, numbers);
        if (rows == 0) {
            throw new LibraryException(Code.ADD_FAIL_ERROR, Code.ADD_FAIL_ERROR_MSG);
        }
    }
}
