package com.ghl.library.Service.Impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ghl.library.Mapper.BookMapper;
import com.ghl.library.Mapper.BorrowMapper;
import com.ghl.library.Mapper.UserMapper;
import com.ghl.library.Pojo.Book;
import com.ghl.library.Pojo.Borrow;
import com.ghl.library.Pojo.User;
import com.ghl.library.Service.StatisticsService;
import com.ghl.library.VO.AuthorStockVO;
import com.ghl.library.VO.BookStatisticsVO;
import com.ghl.library.VO.StockLevelVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据看板统计服务实现。
 *
 * <p>统计类逻辑集中使用 Stream 完成：{@code groupingBy} 按作者分组、
 * {@code summarizingInt} 聚合库存、{@code sorted/limit} 生成排行，
 * 既减少手写循环，也便于扩展统计维度。</p>
 *
 * @author ghl
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    /** 作者排行取前 N 名 */
    private static final int TOP_AUTHOR_LIMIT = 5;

    /** 库存充足阈值 */
    private static final int SUFFICIENT_STOCK_THRESHOLD = 50;

    /** 库存区间名称 */
    private static final String LEVEL_SUFFICIENT = "库存充足";
    private static final String LEVEL_LOW = "库存偏低";
    private static final String LEVEL_EMPTY = "已售罄";

    /** 库存区间固定展示顺序 */
    private static final List<String> STOCK_LEVELS = List.of(LEVEL_SUFFICIENT, LEVEL_LOW, LEVEL_EMPTY);

    /** 平均库存保留两位小数时使用的精度系数 */
    private static final double PERCENT_SCALE = 100.0;

    @Resource
    private BookMapper bookMapper;

    @Resource
    private BorrowMapper borrowMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public BookStatisticsVO getStatistics() {
        List<Book> books = bookMapper.selectList(null);

        long totalStock = books.stream()
                .mapToLong(this::stockOf)
                .sum();

        double averageStock = books.isEmpty() ? 0.0
                : Math.round((double) totalStock / books.size() * PERCENT_SCALE) / PERCENT_SCALE;

        Map<String, IntSummaryStatistics> authorStatistics = books.stream()
                .filter(book -> StrUtil.isNotBlank(book.getAuthor()))
                .collect(Collectors.groupingBy(Book::getAuthor,
                        Collectors.summarizingInt(this::stockOf)));

        List<AuthorStockVO> topAuthors = authorStatistics.entrySet().stream()
                .map(entry -> new AuthorStockVO(entry.getKey(),
                        (int) entry.getValue().getCount(), entry.getValue().getSum()))
                .sorted(Comparator.comparingLong(AuthorStockVO::getStock).reversed()
                        .thenComparing(AuthorStockVO::getAuthor))
                .limit(TOP_AUTHOR_LIMIT)
                .toList();

        Map<String, Long> levelCounts = books.stream()
                .collect(Collectors.groupingBy(
                        book -> resolveStockLevel(stockOf(book)), Collectors.counting()));

        List<StockLevelVO> stockLevels = STOCK_LEVELS.stream()
                .map(level -> new StockLevelVO(level, levelCounts.getOrDefault(level, 0L)))
                .toList();

        long activeBorrowCount = borrowMapper.selectCount(
                new QueryWrapper<Borrow>().eq("is_return", 0));
        long totalUsers = userMapper.selectCount(new QueryWrapper<User>());

        return new BookStatisticsVO(books.size(), totalStock, averageStock,
                activeBorrowCount, totalUsers, topAuthors, stockLevels);
    }

    private int stockOf(Book book) {
        Integer stock = book.getStock();
        return stock == null ? 0 : stock;
    }

    private String resolveStockLevel(int stock) {
        if (stock <= 0) {
            return LEVEL_EMPTY;
        }
        return stock < SUFFICIENT_STOCK_THRESHOLD ? LEVEL_LOW : LEVEL_SUFFICIENT;
    }
}
