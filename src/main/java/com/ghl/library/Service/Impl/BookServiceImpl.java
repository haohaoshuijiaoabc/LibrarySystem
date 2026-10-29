package com.ghl.library.Service.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ghl.library.Mapper.BookMapper;
import com.ghl.library.Pojo.Book;
import com.ghl.library.Pojo.Result;
import com.ghl.library.Service.BookService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 图书查询业务实现。
 *
 * @author ghl
 */
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Resource
    private BookMapper bookMapper;

    @Override
    public Result<List<Book>> query(String name, String author) {
        QueryWrapper<Book> wrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(name)) {
            wrapper.like("name", name);
        }
        if (StrUtil.isNotBlank(author)) {
            wrapper.like("author", author);
        }
        wrapper.orderByAsc("id");
        List<Book> books = bookMapper.selectList(wrapper);
        return Result.success(books);
    }
}
