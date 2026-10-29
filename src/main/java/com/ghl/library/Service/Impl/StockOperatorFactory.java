package com.ghl.library.Service.Impl;

import com.ghl.library.Constant.StockOperation;
import com.ghl.library.Exception.LibraryException;
import com.ghl.library.Service.StockOperator;
import com.ghl.library.Util.Code;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存操作策略工厂。
 *
 * <p>工厂模式：应用启动时收集所有 {@link StockOperator} 实现并按操作类型注册，
 * 业务层通过 {@link #get(String)} 获取策略，新增操作类型时无需修改调用方。</p>
 *
 * @author ghl
 */
@Component
public class StockOperatorFactory {

    private final Map<String, StockOperator> operatorMap;

    /**
     * 由 Spring 注入全部策略实现并建立索引。
     *
     * @param operators 所有库存操作策略
     */
    public StockOperatorFactory(List<StockOperator> operators) {
        Map<String, StockOperator> map = new HashMap<>();
        for (StockOperator operator : operators) {
            map.put(operator.type(), operator);
        }
        this.operatorMap = Map.copyOf(map);
    }

    /**
     * 获取指定类型的库存操作策略。
     *
     * @param type 操作类型
     * @return 对应的策略实现
     * @throws LibraryException 当操作类型未注册时抛出
     */
    public StockOperator get(String type) {
        StockOperator operator = operatorMap.get(type);
        if (operator == null) {
            throw new LibraryException(Code.UNKNOWN_CODE_ERROR, "不支持的库存操作类型：" + type);
        }
        return operator;
    }
}
