package com.intuit.sbd.payroll.psp.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ShutdownHookManager {
    private static int maxTimeForShutdownHooksInMillis;

    //these values will change only once, once set to true, it will remain true
    private boolean ShutdownTriggered = false;
    private boolean ShutDownComplete = false;

    //Countdown latch for shutdown hooks, we will set the count to total number of hooks
    //Each hook upon completion will countdown()
    private CountDownLatch shutDownLatch;


    @Autowired
    public ShutdownHookManager(List<ManagedShutdownHook> shutdownHooksList, @Value("${maxTimeForShutdownHooksInMillis:30000}") int maxTimeForShutdownHooksInMillis) {
        int numberOfShutdownHooks = shutdownHooksList.size();
        log.info("Total shutdown hooks={}, list={}",numberOfShutdownHooks,shutdownHooksList);
        shutDownLatch = new CountDownLatch(numberOfShutdownHooks);
        ShutdownHookManager.maxTimeForShutdownHooksInMillis = maxTimeForShutdownHooksInMillis;

        for(ManagedShutdownHook shutdownHook: shutdownHooksList) {
            registerHook(shutdownHook);
        }
    }


    /***
     * This function registers a new shutdown hook.
     * Implementation - Creates a new thread which will call shutdown() method of the hook when the thread starts
     * @param shutdownHook - ShutdownHook class
     */
    public void registerHook(ManagedShutdownHook shutdownHook) {
        log.info("action=registerShutdownHook Registering shutdown hook class={}",shutdownHook.getClass());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdownHook.shutdown(shutDownLatch);
        }));
    }


    /***
     * This function makes the current thread to wait till one of the two things happen
     * 1. All the shutdown hooks have completed
     * 2. The maxShutdownTimeInMillis has elapsed
     * Also logs if all the hooks were completed before the timeout
     * Implementation - Calls the await() method of countdown latch with timeout set to maxTimeForShutdownHooksInMilli
     *
     * We also set the boolean isShutDownComplete to true so that the next time we return true immediately
     ***/
    public void waitForShutdownHooksToComplete() throws InterruptedException,ShutdownNotTriggeredException {
        //if shutdown has not been triggered then throw an exception
        if(!isShutdownInProgress()) {
            throw new ShutdownNotTriggeredException("Shutdown has not been triggered!!!");
        }
        //if latches have completed already, return immediately
        if(ShutDownComplete) {
            return;
        }

        /*Make the current thread wait till either of two conditions
        1. all registered shutdown hooks are complete
        2. maxTimeForShutdownHooks has elapsed
        If all hooks are completed in time await() will return true else false
         */
        boolean allHooksCompleted = shutDownLatch.await(maxTimeForShutdownHooksInMillis, TimeUnit.MILLISECONDS);
        log.info("AllShutdownHooksCompletedBeforeTimeout={}",allHooksCompleted);

        //setting this so that next time we can return directly
        ShutDownComplete = true;
    }



    /***
     This function tells if the shutdown has already been triggered or not
     We maintain a boolean which we set as soon as we know that shutdown has been triggered for the first time
     If this value is not set, then we try to register a empty shutdown hook, if shutdown is already in progress we
     will receive a IllegalStateException "Shutdown in progress"
     ***/
    public boolean isShutdownInProgress() {
        //we set this to true the first time we become aware that shutdown process has been triggered
        //after that we return directly from here
        if(ShutdownTriggered) {
            return true;
        }
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
            }));
            //if this empty shutdown hook is successfully added then machine not in shutdown phase
            return false;
        } catch (IllegalStateException ex) {
            // if shutdown is already in progress we should receive this exception
            if(ex.getMessage().equals("Shutdown in progress")) {
                ShutdownTriggered = true;
                return true;
            }
        }
        // this should not reach here, but defaulting to return false
        return false;
    }

    public static class ShutdownNotTriggeredException extends Exception {
        public ShutdownNotTriggeredException(String message) {
            super(message);
        }
    }

}
