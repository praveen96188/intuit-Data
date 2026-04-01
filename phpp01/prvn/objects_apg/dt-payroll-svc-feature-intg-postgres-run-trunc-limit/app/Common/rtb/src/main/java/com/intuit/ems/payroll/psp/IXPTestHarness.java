package com.intuit.ems.payroll.psp;

import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author cbhat
 *
 * This is a test harness for testing out IXP SDK which evaluates the IXP feature
 * flags concurrently similation the production scenario
 */
public class IXPTestHarness {
    private static final Logger logger = LoggerFactory.getLogger(IXPTestHarness.class);
    public static void main(String[] args) throws InterruptedException {

        final Runtime[] runtime = {Runtime.getRuntime()};
        final AtomicLong[] i = {new AtomicLong(0)};
        final long[] startTime = {0};
        int mb = 1024*1024;
        final long[] freeMemory = {0};
        final long[] maxMemory = { 0 };
        final long[] totalMemory = { 0 };
        final long[] usedMemory = { 0 };
        String pattern = "HH:mm:ss.SSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        ExecutorService executorService = Executors.newFixedThreadPool(24);
        logger.info("####### Starting Concurrent run");

        startTime[0] = System.currentTimeMillis();

        for(int ii=0; ii<1000000; ii++) {
            executorService.submit(new Runnable() {
                public void run() {
                        boolean flag = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_GUIDELINE_ONLY_ASSISTED, false);
                        freeMemory[0] = runtime[0].freeMemory()/mb;
                        maxMemory[0] = runtime[0].maxMemory()/mb;
                        totalMemory[0] = runtime[0].totalMemory()/mb;
                        usedMemory[0] = totalMemory[0] - freeMemory[0];

                        String date = simpleDateFormat.format(new Date());
                        if(i[0].incrementAndGet()%1000 == 0){
                            logger.info(date +" |  ENABLE_GUIDELINE_INTEGRATION_1=" + flag + ", i="+ i[0]
                                    + ", #####" + ", Free Memory="+freeMemory[0] +", MaxMemory="+maxMemory[0]+", TotalMemory="+totalMemory[0] + ", UsedMemory=" + usedMemory[0]);
                        }
                }
            });
        }
        logger.info("####### Entering shutdown() and awaitTermination()");
        executorService.shutdown();
        executorService.awaitTermination(2, TimeUnit.MINUTES);
        logger.info("####### Done awaitTermination()");
        Thread.sleep(600000);
        logger.info("####### Done Sleeping, exiting the Test Harness");
    }
}
