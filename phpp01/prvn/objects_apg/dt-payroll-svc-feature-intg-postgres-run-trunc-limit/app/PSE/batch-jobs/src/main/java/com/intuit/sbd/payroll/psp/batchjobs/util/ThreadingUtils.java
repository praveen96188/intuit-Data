package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: May 24, 2010
 * Time: 10:35:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class ThreadingUtils {
    private static SpcfLogger logger = Application.getLogger(ThreadingUtils.class);

    /**
     * Coerce an unchecked Throwable to a RuntimeException
     * <p/>
     * If the Throwable is an Error, throw it; if it is a
     * RuntimeException return it, otherwise throw IllegalStateException
     */
    public static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException)
            return (RuntimeException) t;
        else if (t instanceof Error)
            throw (Error) t;
        else
            throw new IllegalStateException("Not unchecked", t);
    }

    public static void shutdownAndAwaitTermination(ExecutorService pool, int interval, int maxWait) {
        logger.info("Shutting down thread pool");
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            int timeout = 0;
            while (timeout < maxWait && !pool.awaitTermination(interval, TimeUnit.SECONDS)) {
                logger.warn("Awaiting pool termination");
                timeout += interval;
            }
            if (timeout > maxWait) {
                pool.shutdownNow();
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public static ExecutorService createNewFixedThreadPool () {
        int processors = Runtime.getRuntime().availableProcessors();
        int threadCount = processors * (2);
        logger.info("Creating thread pool with " + threadCount + " threads");
        return Executors.newFixedThreadPool(threadCount);
    }

    public static void shutdownAndAwaitTermination(ExecutorService pool) {
       shutdownAndAwaitTermination(pool, 10, 300);
    }

    /**
     *
     * @param pool
     * @param interval
     */
    public static void shutdownAndAwaitTermination(ExecutorService pool, int interval) {
        logger.info("Shutting down thread pool");
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            while (!pool.awaitTermination(interval, TimeUnit.SECONDS)) {
                logger.warn("Awaiting pool termination");
            }
            pool.shutdownNow();
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }


}
