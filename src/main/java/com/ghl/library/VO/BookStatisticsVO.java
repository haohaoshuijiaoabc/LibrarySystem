package com.ghl.library.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 管理端数据看板统计结果。
 *
 * <p>由统计服务遍历图书数据汇总得到，供管理首页的数据看板展示。</p>
 *
 * @param totalBooks         图书种类数
 * @param totalStock         库存总册数
 * @param averageStock       平均每种图书库存
 * @param activeBorrowCount  当前在借册数
 * @param totalUsers         注册用户数
 * @param topAuthors         库存最高的作者排行
 * @param stockLevels        库存区间分布
 * @author ghl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalBooks;

    private long totalStock;

    private double averageStock;

    private long activeBorrowCount;

    private long totalUsers;

    private List<AuthorStockVO> topAuthors;

    private List<StockLevelVO> stockLevels;
}
