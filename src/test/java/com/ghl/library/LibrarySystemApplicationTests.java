package com.ghl.library;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring 上下文冒烟测试：验证所有 Bean（含 AOP、异步线程池）能够正常装配。
 *
 * @author ghl
 */
@SpringBootTest
class LibrarySystemApplicationTests {

    @Test
    void contextLoads() {
        // 上下文启动成功即通过
    }
}
