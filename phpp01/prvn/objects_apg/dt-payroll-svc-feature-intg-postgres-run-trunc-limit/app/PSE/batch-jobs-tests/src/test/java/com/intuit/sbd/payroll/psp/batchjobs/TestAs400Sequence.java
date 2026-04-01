package com.intuit.sbd.payroll.psp.batchjobs;

import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class TestAs400Sequence {

    @Test
    public void test() {
        ExecutorService threadPool = null;
        try {
            // Create threadPool with given parameters
            threadPool = new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(threadPool);

            List<Integer> psids = getPsids();

            // Process each company in a separate thread (the signature must be there for the company to be processed)
            int numberOfProcessedCompanies = 0;
            for (Integer psid : psids) {
                numberOfProcessedCompanies++;
                final Integer finalPsid = psid;
                completionService.submit(new Callable<Integer>() {
                    public Integer call() {
                        return processPsid(finalPsid);
                    }

                });
            }

            // Get the results of each thread execution
            try {
                for (int t = 0; t < numberOfProcessedCompanies; t++) {
                    Future<Integer> f = completionService.take();
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            catch (Throwable t) {
                ThreadingUtils.launderThrowable(t.getCause());
            }
        }
        catch (Throwable t) {
            int i = 0;
        }
        finally {
            if (threadPool != null) {
                ThreadingUtils.shutdownAndAwaitTermination(threadPool, 60, 10);
            }

        }

    }

    private List<Integer> getPsids() {

        List<Integer> psids = new ArrayList<Integer>();

        for (int i = 0; i < 10; i++) {
            psids.add(100000341);
            psids.add(100000391);
            psids.add(100000647);
            psids.add(100000988);
            psids.add(100001039);

        }
        return psids;  //To change body of created methods use File | Settings | File Templates.
    }

    private Integer processPsid(Integer finalPsid) {
        return 1;
    }
}
