package com.ghl.library.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ghl.library.Mapper.BookMapper;
import com.ghl.library.Mapper.BorrowMapper;
import com.ghl.library.Mapper.UserMapper;
import com.ghl.library.Pojo.Book;
import com.ghl.library.VO.AuthorStockVO;
import com.ghl.library.VO.BookStatisticsVO;
import com.ghl.library.VO.StockLevelVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * {@link StatisticsServiceImpl} 单元测试。
 *
 * @author ghl
 */
@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private BookMapper bookMapper;

    @Mock
    private BorrowMapper borrowMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    @Test
    @DisplayName("统计结果应正确完成分组、聚合与排序")
    void getStatistics() {
        when(bookMapper.selectList(any())).thenReturn(List.of(
                new Book(1, "三体", "刘慈欣", 100),
                new Book(2, "球状闪电", "刘慈欣", 0),
                new Book(3, "活着", "余华", 30),
                new Book(4, "百年孤独", "马尔克斯", 200),
                new Book(5, "无名之书", null, 5)));
        when(borrowMapper.selectCount(any(QueryWrapper.class))).thenReturn(3L);
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(6L);

        BookStatisticsVO statistics = statisticsService.getStatistics();

        assertThat(statistics.getTotalBooks()).isEqualTo(5);
        assertThat(statistics.getTotalStock()).isEqualTo(335);
        assertThat(statistics.getAverageStock()).isEqualTo(67.0);
        assertThat(statistics.getActiveBorrowCount()).isEqualTo(3);
        assertThat(statistics.getTotalUsers()).isEqualTo(6);

        List<AuthorStockVO> topAuthors = statistics.getTopAuthors();
        assertThat(topAuthors).hasSize(3);
        assertThat(topAuthors.get(0).getAuthor()).isEqualTo("马尔克斯");
        assertThat(topAuthors.get(0).getStock()).isEqualTo(200);
        assertThat(topAuthors.get(1).getAuthor()).isEqualTo("刘慈欣");
        assertThat(topAuthors.get(1).getBookCount()).isEqualTo(2);
        assertThat(topAuthors.get(1).getStock()).isEqualTo(100);
        assertThat(topAuthors.get(2).getAuthor()).isEqualTo("余华");
        assertThat(topAuthors.get(2).getStock()).isEqualTo(30);

        List<StockLevelVO> stockLevels = statistics.getStockLevels();
        assertThat(stockLevels).hasSize(3);
        assertThat(stockLevels.get(0).getLevel()).isEqualTo("库存充足");
        assertThat(stockLevels.get(0).getCount()).isEqualTo(2);
        assertThat(stockLevels.get(1).getLevel()).isEqualTo("库存偏低");
        assertThat(stockLevels.get(1).getCount()).isEqualTo(2);
        assertThat(stockLevels.get(2).getLevel()).isEqualTo("已售罄");
        assertThat(stockLevels.get(2).getCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("无图书时统计结果应全为零且不抛异常")
    void getStatisticsWithEmptyLibrary() {
        when(bookMapper.selectList(any())).thenReturn(new ArrayList<>());
        when(borrowMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(userMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

        BookStatisticsVO statistics = statisticsService.getStatistics();

        assertThat(statistics.getTotalBooks()).isZero();
        assertThat(statistics.getTotalStock()).isZero();
        assertThat(statistics.getAverageStock()).isZero();
        assertThat(statistics.getTopAuthors()).isEmpty();
        assertThat(statistics.getStockLevels()).hasSize(3);
        for (StockLevelVO stockLevel : statistics.getStockLevels()) {
            assertThat(stockLevel.getCount()).isZero();
        }
    }
}
