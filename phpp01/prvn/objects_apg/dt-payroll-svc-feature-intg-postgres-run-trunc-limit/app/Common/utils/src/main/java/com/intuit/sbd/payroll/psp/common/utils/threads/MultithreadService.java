package com.intuit.sbd.payroll.psp.common.utils.threads;

import com.intuit.sbd.payroll.psp.Application;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

public class MultithreadService<S,T> {
    private static final Logger logger = LoggerFactory.getLogger(MultithreadService.class);
    private final int INTERVAL = 60;
    private final int MAX_WAIT = 300;

    public interface IExecutable<S, T> {
        T execute(S x);
    }

    private IExecutable<S,T> executable;
    private Collection<S> listToProcess;

    public MultithreadService(Collection<S> listToProcess, IExecutable<S,T> executable){
        this.listToProcess = listToProcess;
        this.executable = executable;
    }

    public List<Pair<S, T>> execute() {
        List<Pair<S, T>> resultList = new ArrayList();
        // Create threadPool with given parameters
        int cores = Runtime.getRuntime().availableProcessors();
        logger.info("No of cores: " + cores);
        int numberOfThreads = cores * 2;
        if (numberOfThreads <= 0) {
            numberOfThreads = 10;
        }

        ExecutorService executorService = new ThreadPoolExecutor(cores, // core size
                numberOfThreads, // max size
                60 * 5, // idle timeout
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());

        CompletionService<Pair<S,T>> completionService = new ExecutorCompletionService(executorService);

        for (S obj: listToProcess) {
            completionService.submit(() -> {
                T result = executable.execute(obj);
                Pair<S,T> resultPair= new Pair<S,T>(obj, result);
                return resultPair;
            });
        }

        try {
            for (int index = 0; index < listToProcess.size(); index++) {
                Future<Pair<S,T>> future = completionService.take();
                Pair<S,T> result = future.get();
                resultList.add(result);
            }
        } catch (InterruptedException e) {
            logger.error("Exception : ", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            logger.error("ExecutionException : ", e);
            throw launderThrowable(e.getCause());
        } finally {
            Application.rollbackUnitOfWork();
            if (executorService != null) {
                shutdownAndAwaitTermination(executorService, INTERVAL, MAX_WAIT);
            }
        }
        return resultList;
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

    public static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException)
            return (RuntimeException) t;
        else if (t instanceof Error)
            throw (Error) t;
        else
            throw new IllegalStateException("Not unchecked", t);
    }
}
