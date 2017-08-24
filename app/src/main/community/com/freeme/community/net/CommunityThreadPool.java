package com.freeme.community.net;

import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池 、缓冲队列
 */
public class CommunityThreadPool {
    // 阻塞队列最大任务数量
    static final int BLOCKING_QUEUE_SIZE = 20;
    static final int THREAD_POOL_MAX_SIZE = 10;

    static final int THREAD_POOL_SIZE = 6;
    /**
     * 缓冲BaseRequest任务队列
     */
    static ArrayBlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(
            CommunityThreadPool.BLOCKING_QUEUE_SIZE);
    /**
     * 线程池，目前是十个线程，
     */
    static AbstractExecutorService pool = new ThreadPoolExecutor(
            THREAD_POOL_SIZE, THREAD_POOL_MAX_SIZE, 15L, TimeUnit.SECONDS, blockingQueue,
            new ThreadPoolExecutor.DiscardOldestPolicy());

    public static CommunityThreadPool getInstance() {
        return Singleton.instance;
    }

    public static void removeAllTask() {
        CommunityThreadPool.blockingQueue.clear();
    }

    public static void removeTaskFromQueue(final Object obj) {
        CommunityThreadPool.blockingQueue.remove(obj);
    }

    /**
     * 关闭，并等待任务执行完成，不接受新任务
     */
    public static void shutdown() {
        if (CommunityThreadPool.pool != null) {
            CommunityThreadPool.pool.shutdown();
        }
    }

    /**
     * 关闭，立即关闭，并挂起所有正在执行的线程，不接受新任务
     */
    public static void shutdownRightnow() {
        if (CommunityThreadPool.pool != null) {
            CommunityThreadPool.pool.shutdownNow();
            try {
                // 设置超时极短，强制关闭所有任务
                CommunityThreadPool.pool.awaitTermination(1,
                        TimeUnit.MICROSECONDS);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行任务
     *
     * @param r
     */
    public void execute(final Runnable r) {
        if (r != null) {
            try {
                pool.execute(r);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class Singleton {
        private static CommunityThreadPool instance = new CommunityThreadPool();
    }
}
