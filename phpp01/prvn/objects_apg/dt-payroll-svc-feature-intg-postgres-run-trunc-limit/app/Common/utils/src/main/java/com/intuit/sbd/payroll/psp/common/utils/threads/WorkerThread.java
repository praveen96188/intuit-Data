package com.intuit.sbd.payroll.psp.common.utils.threads;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Apr 27, 2009
 * Time: 10:31:08 AM
 *
 * HOW TO USE THIS STUFF
 *
 * 1. Your worker threads
 *    a. Write a class that extends WorkerThread.
 *    c. Implement a constructor that calls WorkerThread's constructor.
 *    b. Implement process(Object pUnit) to process a single work unit.  Return a ProcessResult to communicate success
 *       or errors with this work unit.  If you throw any exception, or allow any thrown exception to leave process(),
 *       WorkerThread will set its "fatal error" flag and the thread will terminate.
 *
 * 2. Your main class
 *    a. Implement the IWorkQueue interface.  This is how your worker threads will get work units.
 *    b. Implement getWorkUnit().  Return the next piece of work, or null of there is no more.  The worker threads will
 *       terminate when getWorkUnit() returns null.  This is not useful if work continues to arrive after you start.
 *       The worker threads synchronize their calls to getWorkUnit()... If they're the only ones touching the underlying
 *       queue, then you don't have to worry about it.  (This is a big "if".)
 *    c. Construct your WorkerThread-derived objects and call their start() methods.
 *    d. Wait for all your workers to finish (worker.isRunning() == false).
 *
 */

public abstract class WorkerThread extends Thread {

    protected int mThreadNum;
    long mStartTime;
    IWorkQueue mQueue;
    protected SpcfLogger mLogger;

    boolean bRunning = false;

    boolean bFatalError = false;


    private /* not allowed */ WorkerThread() {
    }


    public WorkerThread(int pThreadNum, IWorkQueue pQueue, SpcfLogger pLogger) {
        mThreadNum = pThreadNum;
        mQueue = pQueue;
        mLogger = pLogger;
        bRunning = true; // set this here, not in run(), to avoid a race condition
    }


    synchronized protected void setFatalErrorOccurred() {
        bFatalError = true;
    }


    synchronized public boolean getFatalErrorOccurred() {
        return bFatalError;
    }


    public boolean isRunning() {
        return bRunning; // no synchronization on bRunning
    }


    public void run() {
        long startTime = System.currentTimeMillis();
        if (mLogger.isDebugEnabled()) {
            mLogger.debug(getName() + ": starting");
        }

        Collection<ProcessResult> results = new ArrayList<ProcessResult>();
        try {
            long nProcessed = 0;
            Object unit;
            while ((unit=mQueue.getWorkUnit()) != null) {
                ProcessResult prUnit = process(unit);
                results.add(prUnit);

                // todo: count errors and abort if "too many"

                ++nProcessed;
                if (mLogger.isInfoEnabled() && nProcessed % 100 == 0) {
                    mLogger.info("Thread "+mThreadNum+" processed "+nProcessed+" work units");
                }
            }
        } catch (Throwable t) {
            // if a subclass lets an exception out, it means it can't process any other units of work
            setFatalErrorOccurred();
            mLogger.error(getName() + " - exiting immediately due to fatal error: " + t.getMessage());
        } finally {
            if (mLogger.isDebugEnabled()) {
                long elapsed = System.currentTimeMillis() - startTime;
                String message = String.format("%s completed - %,d units of work processed in %,5.2f seconds",
                                               getName(), results.size(), (double)elapsed / 1000.0);
                mLogger.debug(message);
            }
            bRunning = false;
        }
    }



    /**
     * Process a single unit of work.  Return a ProcessResult describing the outcome.
     *
     * If any Throwable comes out of this method, no more units of work will be processed.
     *
     * @param pUnit
     * @return a ProcessResult describing the outcome
     */
    abstract protected ProcessResult process(Object pUnit);
}
