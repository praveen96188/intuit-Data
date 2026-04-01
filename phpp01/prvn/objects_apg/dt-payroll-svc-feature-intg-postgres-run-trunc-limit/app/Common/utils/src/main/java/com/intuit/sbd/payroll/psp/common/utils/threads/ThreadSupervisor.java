package com.intuit.sbd.payroll.psp.common.utils.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Apr 29, 2009
 * Time: 1:52:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class ThreadSupervisor implements IWorkQueue {


    private Queue<Object> mWorkQueue = new ConcurrentLinkedQueue<Object>();

    /**
     * Adds a work unit to the queue.  All work to be done by the threads must be added to the queue in advance.
     */
    public void addWork(Object pUnit) {
        mWorkQueue.add(pUnit);
    }


    /**
     * @return The number of work units in the queue.
     */
    public int getWorkQueueSize() {
        return mWorkQueue.size();
    }


    /**
     * @return Whether the queue has any work remaining.  Note that a true return does not guarantee that work will
     * still be there the next time you call getWorkUnit().
     */
    public boolean hasWork() {
        return (mWorkQueue.size() > 0);
    }


    /**
     * My implementation of IWorkQueue.  The ConcurrentLinkedQueue manages concurrent access from multiple threads,
     * so getWorkUnit() is simple.
     *
     * @return the next work unit, or null if no work remains
     */
    public Object getWorkUnit() {
        return mWorkQueue.poll();
    }



    ArrayList<WorkerThread> mThreads = new ArrayList<WorkerThread>();

    /**
     * Adds your WorkerThread-derived thread to the list of threads being "supervised."
     * It is this collection of threads that will be started by startThreads() and waited-for by waitForThreads(...).
     */
    public void addThread(WorkerThread pThread) {
        mThreads.add(pThread);
    }

    /**
     * Get the list of threads.
     */
    public List<WorkerThread> getThreads() {
        return mThreads;
    }


    /**
     * Starts all threads.
     */
    public void startThreads() {
        for (WorkerThread t : mThreads) {
            t.start();
        }
    }


    /**
     * Checks to see if any thread isRunning().  If so, it takes a short nap (pPollIntervalMillis) and checks again.
     * It returns when all threads report isRunning()==false.
     *
     * @param pPollIntervalMillis
     */
    public void waitForThreads(long pPollIntervalMillis) {
        if (pPollIntervalMillis < 0) {
            pPollIntervalMillis = 1;
        }

        boolean bKeepChecking = true;
        while (bKeepChecking) {
            bKeepChecking = false;
            for (WorkerThread t : mThreads) {
                if (t.isRunning()) {
                    try { Thread.sleep(pPollIntervalMillis); } catch (InterruptedException e) { /*discard*/ }
                    bKeepChecking = true;
                }
            }
        }
    }
}
