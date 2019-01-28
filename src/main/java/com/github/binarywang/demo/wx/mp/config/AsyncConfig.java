package com.github.binarywang.demo.wx.mp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {
    @Value("${async.threadNamePrefix}")
    private String threadNamePrefix;    // 配置线程池中的线程名称前缀
    @Value("${async.corePoolSize}")
    private Integer corePoolSize;       // 配置线程池中的核心线程数
    @Value("${async.maxPoolSize}")
    private Integer maxPoolSize;        // 配置最大线程数
    @Value("${async.queueCapacity}")
    private Integer queueCapacity;      // 配置队列大小

    /**
     * 线程池配置
     * @return
     */
    @Bean
    public AsyncTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            }
        });
        return executor;
    }

}
