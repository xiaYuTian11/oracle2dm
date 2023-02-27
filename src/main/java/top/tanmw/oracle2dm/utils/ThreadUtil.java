package top.tanmw.oracle2dm.utils;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.alibaba.ttl.threadpool.TtlExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程工具类
 *
 * @author TMW
 * @since 2022/4/26 14:27
 */
public class ThreadUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUtil.class);
    public static final ExecutorService EXECUTOR_SERVICE = TtlExecutors.getTtlExecutorService(new ThreadPoolExecutor(
            12, 24, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setDaemon(true).setNamePrefix("ThreadUtil-")
                    .setUncaughtExceptionHandler((t, e) -> LOGGER.error("任务处理异常!", e))
                    .setThreadFactory(Thread::new).build()
    ));

    public static void sleep(Integer second) {
        cn.hutool.core.thread.ThreadUtil.sleep(second, TimeUnit.SECONDS);
    }

}
