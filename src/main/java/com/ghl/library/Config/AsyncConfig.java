package com.ghl.library.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置。
 *
 * <p>为业务侧 {@code @Async} 任务（操作日志落库等）提供隔离的自定义线程池，
 * 避免占用 Tomcat 请求线程；队列满时采用 {@link ThreadPoolExecutor.CallerRunsPolicy}
 * 由调用线程兜底执行，保证任务不丢失。</p>
 *
 * @author ghl
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Value("${library.async.core-pool-size:4}")
    private int corePoolSize;

    @Value("${library.async.max-pool-size:8}")
    private int maxPoolSize;

    @Value("${library.async.queue-capacity:200}")
    private int queueCapacity;

    @Value("${library.async.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    /**
     * 业务异步线程池。
     *
     * @return 线程池执行器
     */
    @Bean("libraryTaskExecutor")
    public ThreadPoolTaskExecutor libraryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("library-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        // 由 Spring 生命周期回调执行 initialize()，此处不要重复初始化
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return libraryTaskExecutor();
    }

}
