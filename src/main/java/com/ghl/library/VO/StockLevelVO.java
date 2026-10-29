package com.ghl.library.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 库存区间分布。
 *
 * @author ghl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockLevelVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 库存区间名称 */
    private String level;

    /** 该区间图书种类数 */
    private long count;
}
