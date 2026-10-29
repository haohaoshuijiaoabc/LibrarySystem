package com.ghl.library.Service;

import com.ghl.library.Pojo.Book;
import com.ghl.library.Pojo.Result;

import java.util.List;

/**
 * 图书查询业务接口。
 *
 * @author ghl
 */
public interface BookService {

    /**
     * 按书名/作者模糊查询图书。
     *
     * @param name   书名关键字，可为空
     * @param author 作者关键字，可为空
     * @return 图书列表
     */
    Result<List<Book>> query(String name, String author);
}
