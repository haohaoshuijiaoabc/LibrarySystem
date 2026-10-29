package com.ghl.library.Constant;

/**
 * 库存操作类型常量。
 *
 * <p>使用字符串常量代替枚举，既保持策略工厂的键值语义，又避免项目中出现枚举类型。</p>
 *
 * @author ghl
 */
public final class StockOperation {

    /** 增加库存 */
    public static final String ADD = "ADD";

    /** 扣减库存 */
    public static final String REDUCE = "REDUCE";

    private StockOperation() {
    }
}
