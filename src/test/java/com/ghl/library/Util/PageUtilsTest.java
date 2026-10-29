package com.ghl.library.Util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link PageUtils} 单元测试。
 *
 * @author ghl
 */
class PageUtilsTest {

    @Test
    @DisplayName("正常分页参数应原样使用")
    void normalPage() {
        Page<Object> page = PageUtils.of(2, 20);

        assertThat(page.getCurrent()).isEqualTo(2);
        assertThat(page.getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("非法页码与每页条数应使用默认值")
    void invalidPage() {
        Page<Object> page = PageUtils.of(0, 0);

        assertThat(page.getCurrent()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(PageUtils.DEFAULT_PAGE_SIZE);
    }

    @Test
    @DisplayName("每页条数超过上限应截断")
    void pageSizeShouldBeCapped() {
        Page<Object> page = PageUtils.of(null, 10_000);

        assertThat(page.getCurrent()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(PageUtils.MAX_PAGE_SIZE);
    }
}
