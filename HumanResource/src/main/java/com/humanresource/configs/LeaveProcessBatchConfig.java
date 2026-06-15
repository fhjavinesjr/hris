package com.humanresource.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class LeaveProcessBatchConfig {

    @Value("${hris.leave.batch.compute-threads:0}")
    private int configuredThreads;

    @Bean(name = "leaveProcessComputeExecutor", destroyMethod = "shutdown")
    public ExecutorService leaveProcessComputeExecutor() {
        int threads = configuredThreads > 0
                ? configuredThreads
                : Math.min(Runtime.getRuntime().availableProcessors() * 2, 32);
        return Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r);
            t.setName("leave-compute-" + t.getId());
            t.setDaemon(true);
            return t;
        });
    }

    @Bean(name = "leaveProcessBatchAsyncExecutor")
    public ThreadPoolTaskExecutor leaveProcessBatchAsyncExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(2);
        exec.setMaxPoolSize(4);
        exec.setQueueCapacity(20);
        exec.setThreadNamePrefix("leave-batch-async-");
        exec.setWaitForTasksToCompleteOnShutdown(true);
        exec.setAwaitTerminationSeconds(30);
        exec.initialize();
        return exec;
    }
}
