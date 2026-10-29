package com.ghl.library.Service;

import com.ghl.library.VO.BookStatisticsVO;

/**
 * 数据看板统计服务。
 *
 * @author ghl
 */
public interface StatisticsService {

    /**
     * 计算管理端数据看板指标。
     *
     * @return 统计数据
     */
    BookStatisticsVO getStatistics();
}
