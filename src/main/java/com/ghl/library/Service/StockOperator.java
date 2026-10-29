package com.ghl.library.Service;

/**
 * 库存操作策略。
 *
 * <p>策略模式：将“增加库存”和“扣减库存”两类差异化的库存变更逻辑
 * 封装为独立实现，由 {@code StockOperatorFactory} 按操作类型选择，
 * 消除 Service 中的 {@code if/else} 业务分支。</p>
 *
 * @author ghl
 */
public interface StockOperator {

    /**
     * 当前策略对应的操作类型。
     *
     * @return 操作类型
     */
    String type();

    /**
     * 执行库存变更。
     *
     * @param bookName 书名
     * @param numbers  变更数量（调用方保证大于 0）
     */
    void operate(String bookName, int numbers);
}
