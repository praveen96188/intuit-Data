package com.intuit.sbd.payroll.psp.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public interface ManagedShutdownHook {
    Logger log = LoggerFactory.getLogger(ManagedShutdownHook.class);

    /***
     * The function which will be run when the shutdown is triggered
     */
    void execute();

    /*
    Once the shutdown hook is complete, we will call latch.countDown() to notify that this hook as completed.
    To avoid deadlock, even if a hook runs into an exception, we just log it and move ahead
     */
    default boolean shutdown(CountDownLatch latch) {
        boolean hookSuccessful = false;
        try {
            execute();
            hookSuccessful = true;
        } catch (Exception e) {
            log.error("Error during shutdown hook, message={}",e.getMessage(),e);
        } finally {
            latch.countDown();
            log.info("ShutdownHookCompletionStatus class={} success={}",this.getClass(),hookSuccessful);
            return hookSuccessful;
        }

    }

}
