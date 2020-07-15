package com.example.mymqtt.mode;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 文件名: Mode1
 * 创建者: 友
 * 创建日期: 2020/7/8 10:44
 * 邮箱: 1738743304.com !
 * 描述: TODO
 */
public class Mode1 {
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(6);

    public static ScheduledExecutorService getScheduledThreadPool() {

        return scheduledThreadPool;
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }
}
