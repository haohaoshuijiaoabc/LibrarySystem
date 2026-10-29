package com.ghl.library.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ghl.library.Mapper.BookMapper;
import com.ghl.library.Pojo.Book;
import com.ghl.library.Pojo.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link BookServiceImpl} 单元测试。
 *
 * @author ghl
 */
@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    @DisplayName("无条件查询应返回全部图书")
    void queryAll() {
        when(bookMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(List.of(new Book(1, "三体", "刘慈欣", 10)));

        Result<List<Book>> result = bookService.query(null, null);

        assertThat(result.getData()).hasSize(1);
        verify(bookMapper).selectList(any(QueryWrapper.class));
    }

    @Test
    @DisplayName("按书名与作者查询应返回匹配结果")
    void queryWithFilters() {
        when(bookMapper.selectList(any(QueryWrapper.class)))
                .thenReturn(List.of(new Book(1, "三体", "刘慈欣", 10)));

        Result<List<Book>> result = bookService.query("三体", "刘慈欣");

        assertThat(result.getData().get(0).getAuthor()).isEqualTo("刘慈欣");
    }
}
