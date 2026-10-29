package com.ghl.library.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 作者维度库存统计。
 *
 * @author ghl
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorStockVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 作者 */
    private String author;

    /** 该作者图书种类数 */
    private int bookCount;

    /** 该作者图书总库存 */
    private long stock;
}
