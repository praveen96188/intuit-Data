package com.intuit.spc.foundations.portability.threading;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;

/***
 * SpcfThread is used to create and manage a new platform specific runtime thread. 
 * Client applications and components can use multiple portable threads when
 * using this class.
 * <p>
 * SpcfThread provides attributes to set initial thread priority and whether a thread
 * is a background or foreground thread. The semantics of these are important to understand
 * since multi-thread applications will perform differently simply by changing this 
 * settings.
 * <p>
 * This class is considered low-level since it provides only the minimum API necessary to
 * create, name and manipulate a new thread of execution. The code that executes on 
 * the newly created thread must be provided an object implementing the portable 
 * ISpcfRunnable interface which provides a method called "run". Returning from this 
 * "stop" method will end the thread. The thread cannot be reused, reentered or 
 * restarted. Clients bind their ISpcfRunnable object to SpcfThread.
 * <p>
 * Adding threading to any application will increase its complexity. Great care must be
 * taken when using this class to ensure threading best practices are adhere to. See 
 * SpcfThreading components best practices for more information.
 * <p>
 * This type provides no mechanism for synchronization. Shared data must be manually 
 * synchronized by client code. Failure to do so could corrupt data. SpcfThreading 
 * synchronization provides types or using the language specific keyword synchronized.

 */
public abstract class SpcfThread 
{
	// No thread is creating using the default factory method for thread create.
	private static SpcfThread sInstanceForStaticMethods = SpcfFactory.getInstance().createThread();
	  
	/***
	 * Factory method to create a new SpcfThread instance. No platform specific thread is 
	 * created using this method. Use this call when you start the thread providing the 
	 * ISpcfRunnable object.
	 * @return new SpcfThread object.
	 */
	public static SpcfThread createInstance()
	{
		return SpcfFactory.getInstance().createThread();
	}
	
	/***
	 * Factory method to create a new SpcfThread instance bound to the ISpcfRunnable object.
	 * @param runnable A client implemented ISpcfRunnable object.
	 * @return New SpcfThread instance bound to the specified ISpcfRunnable object.
	 * @throws SpcfArgumentNullException if specified runnableObject is null.
	 */
	public static SpcfThread createInstance(ISpcfRunnable runnable)
	{
		return SpcfFactory.getInstance().createThread(runnable);	
	}  
	 
	/**
     * Set the instance of ISpcfThreadUncaughtExceptionHandler to
     * add custom notification behavior when an exception thrown within
     * a thread goes uncaught. For Java, this can provide useful notification
     * before the thread just dies.  For .NET, the event occurs for the initial
     * application domain, and even if the exception is thrown in the main thread.
     * It will also not prevent the application from terminating which is the new 
     * default behavior for 2.0    
     * @param handler An instance of ISpcfThreadUncaughtExceptionHandler that 
     * provides the method to invoke. To remove an existing handler, pass null.  
     * @throws SpcfSecurityException If required permissions are not met
     */
	public static void setUncaughtExceptionHandler(ISpcfThreadUncaughtExceptionHandler handler )
	{ 
		sInstanceForStaticMethods.doSetUncaughtExceptionHandler(handler, false); 
	} 
	
	/**
     * Sets the default handler for uncaught exceptions. It will only do this
     * if a handler has not already been set.  This method is used by SpcfLogManager
     * to register itself as the default if it is available. Others should 
     * use the UncaughtExceptionHandler property. 
     * @param handler An instance of ISpcfThreadUncaughtExceptionHandler that 
     * provides the method to invoke.   
     * @throws SpcfSecurityException If required permissions are not met
     */
	public static void initializeUncaughtExceptionHandler(ISpcfThreadUncaughtExceptionHandler handler )
	{ 
		sInstanceForStaticMethods.doSetUncaughtExceptionHandler(handler, true); 
	}
	   
	/**
     * Forwarding virtual method for setUncaughtExceptionHandler 
     */
    protected abstract void doSetUncaughtExceptionHandler(ISpcfThreadUncaughtExceptionHandler handler, boolean default_only); 
     
	/***
	 * 
	 * When a new instance of SpcfThread is created, it is not started. Call 
	 * this method to invoke the ISpcfRunnable.run code on the new thread. This 
	 * method can only be called once, otherwise an exception is thrown. 
	 * The thread is created as a foreground thread regardless of the current thread's 
	 * characteristics. You can change this by setting SpcfThread.setBackground(true), 
	 * prior to calling SpcfThread.start.
	 * @throws SpcfIllegalThreadStateException Thrown if the thread has already been started.
	 * @throws com.intuit.spc.foundations.portability.SpcfInvalidOperationException if the thread was not created.
	 */
	public abstract void start();
	
	/***
	 * SpcfThread supports two-phased construction by providing this method. To use this 
	 * method first allocate a new SpcThread object using default construction then call
	 * this method providing an ISpcfRunnable object. Its code will be invoked on the 
	 * new thread. The platform specific thread will not be created until this method is 
	 * called when creating a SpcfThread using the default constructor.
	 * @throws com.intuit.spc.foundations.portability.SpcfInvalidOperationException 
	 * Thrown if the thread was initially constructed 
	 * specifying an ISpcfRunnable object.
	 * @throws com.intuit.spc.foundations.portability.SpcfArgumentNullException if specified runnableObject is null.
	 * @param runnableObject A client implemented ISpcfRunnable object.
	 */
	public abstract void start(ISpcfRunnable runnableObject);
	
	/***
	 * SpcfThread can be named. Use this method to set a new thread name. If permission is
	 * allowed, a thread name can only be set once.
	 * @throws com.intuit.spc.foundations.portability.SpcfArgumentNullException The specified argument "name" 
	 * is null.
	 * @throws com.intuit.spc.foundations.portability.SpcfSecurityException No permission to 
	 * change the thread state.
	 * @throws com.intuit.spc.foundations.portability.SpcfInvalidOperationException The thread name 
	 * was already set.
	 */
	public abstract void setName(String name);

	/***
	 * SpcfThread can be named. Returns the name of the thread. If no name was set, the 
	 * empty string is returned.
	 * @return name of the SpcfThread.
	 * @throws com.intuit.spc.foundations.portability.SpcfInvalidOperationException if the thread was 
	 * not created and started.
	 */
	public abstract String getName();

	/***
	 * SpcfThread is always in some state - initialized, running, wait, etc. 
	 * Use this accessor to identify the current state of the runtime thread. 
	 * 
	 * Depending on the current state of the thread, certain SpcfThread methods may or 
	 * may not be called. e.g. The thread can only set background state in thread state
	 * SpcfThreadState.Initialized. 
	 * States are identified using the SpcfThreadState enum.
	 * @return The current state of the thread.
	 * @throws com.intuit.spc.foundations.portability.SpcfInvalidOperationException if 
	 * the thread was not created and started.
	 */
	public abstract SpcfThreadState getThreadState();

	/***
	 * SpcfThread is given a default priority of SpcfThreadPriority.Normal. 
	 * The only supported priorities are specifically identified by SpcfThreadPriority.
	 * The runtime may favor threads set with higher priority. Changing thread priority 
	 * from the default should be used for special cases only. Changing priority does 
	 * not necessarily have to be honored by the runtime.
	 * @param priority The new priority to assign this thread.
	 * @throws SpcfSecurityException Thrown if the thread does not have permission to change 
	 * the thread state.
	 * @throws com.intuit.spc.foundations.portability.SpcfIllegalArgumentException 
	 * Thrown if the specified priority is null.
	 * @throws com.intuit.spc.foundations.portability.SpcfInvalidOperationException 
	 * if the thread was not created and started.
	 * @throws SpcfIllegalThreadStateException if the thread is in the terminated state. 
	 * @see com.intuit.spc.foundations.portability.threading.SpcfThreadPriority
	 */
	public abstract void setPriority(SpcfThreadPriority priority);

	/***
	 * SpcfThread's current priority. The default is SpcfThreadPriority.Normal.
	 * @return The current thread priority value in terms of SpcfThreadPriority.
	 * @throws com.intuit.spc.foundations.portability.SpcfInvalidOperationException if the thread was not 
	 * created and started.
	 */
	public abstract SpcfThreadPriority getPriority();
	
	/***
	 * Allows any thread to sleep. Only a thread can put itself to sleep.
	 * @param timeOut The time in milliseconds for the thread to wait before returning. The
	 * time out could occur sooner than specified if this thread interrupted status is set
	 * or it gets interrupted while sleeping. For this case, an exception will be thrown. 
	 * A timeOut of 0ms is modified to 1ms to ensure interrupt status is checked.
	 * @throws SpcfThreadInterruptedException if this thread was interrupted.
	 * @throws com.intuit.spc.foundations.portability.SpcfArgumentOutOfRangeException if the specified time 
	 * out is negative.
	 */
	public static void sleep(int timeOut)
	{
		sInstanceForStaticMethods.doSleep(timeOut);
	}

	/***
	 * Interrupts the thread. If the thread is currently blocked in a wait, join or sleep then
	 * its interrupt status is cleared and a SpcfThreadInterruptException is thrown on the 
	 * thread. If the thread is not blocked on one of these calls, then its interrupt status is set. 
	 * A subsequent call to one of these blocking calls will cause the SpcfThreadInterruptedException
	 * to be thrown and the interrupt status cleared.
	 * 
	 * @throws com.intuit.spc.foundations.portability.SpcfSecurityException No permission to change the thread state.
	 * @throws com.intuit.spc.foundations.portability.SpcfInvalidOperationException if the thread was not 
	 * created and started.
	 */
	public abstract void interrupt();
	

	/***
	 * Used to synchronize one thread to a different thread. The calling thread is blocked until
	 * the thread being joined has terminated.
	 * @return boolean: true if the joined thread has terminated, false otherwise.
	 * @throws SpcfThreadInterruptedException if this thread was interrupted.
	 * @throws com.intuit.spc.foundations.portability.SpcfInvalidOperationException if the thread was not 
	 * created and started.
	 */
	public abstract boolean join();
	
	/***
	 * Used to synchronize one thread to a different thread. The calling thread is blocked until
	 * the thread being joined has terminated or the timeout has expired.
	 * @param timeOut must be an integer value greater than or equal to zero. Time is specified in milliseconds. 
	 * A timeOut of 0ms is modified to 1ms to ensure interrupt status is checked.
	 * @return boolean: true if the joined thread has terminated, false otherwise.
	 * @throws com.intuit.spc.foundations.portability.SpcfArgumentOutOfRangeException 
	 * if the specified time out is negative.
	 * @throws SpcfThreadInterruptedException if this thread was interrupted.
	 * @throws com.intuit.spc.foundations.portability.SpcfInvalidOperationException 
	 * if the thread was not created and started.
	 */
	public abstract boolean join(int timeOut);
	
	/***
	 * Mark the thread as a background thread. By default all SpcfThreads are created as
	 * a foreground thread. You must call this method to change this default prior to calling
	 * SpcfThread.start.
	 * 
	 * A background thread is no different than a foreground thread except a background thread
	 * does not prevent the runtime from exiting. Once all foreground threads have terminated,
	 * running background threads are stopped (without notification) and the runtime exits.
	 * 
	 * @param isBackground - true to set the thread as a background thread, false otherwise.
	 * @throws SpcfIllegalThreadStateException Thrown if SpcfThread.start was previously called.
	 * @throws com.intuit.spc.foundations.portability.SpcfSecurityException No permission to 
	 * change the thread state.
	 * @throws com.intuit.spc.foundations.portability.SpcfInvalidOperationException 
	 * if the thread was not created.
	 */
	public abstract void setBackground(boolean isBackground);
	
	/***
	 * Identifies if the thread is currently typed as a background or not. If not, it is considered
	 * a foreground thread.
	 * @return true if the thread is a background thread, false otherwise.
	 * @throws com.intuit.spc.foundations.portability.SpcfInvalidOperationException - if the thread 
	 * was not created and started.
	 */
	public abstract boolean getBackground();
	
	/***
	 * Retrieves a platform specific identifier for this thread. The id is an integral value assigned by
	 * the underlying platform and remains constant duing the life of a thread. An id may be reused 
	 * once a thread has been terminated. 
	 * @return runtime assigned value.
	 * @throws com.intuit.spc.foundations.portability.SpcfInvalidOperationException if the thread 
	 * was not created and started.
	 */
	public abstract long getId();
	
	/***
	 * Convenience method to obtain a portable SpcfThread reference to the current thread.
	 * Calling this method on the same thread multiple times will always return a new instance
	 * of SpcfThread but wrap the same current platform specific thread instance. 
	 * 
	 * @return a new SpcfThread reference to the current thread.
	 */
	public static SpcfThread getCurrentThread()
	{
		return sInstanceForStaticMethods.getCurrentThreadImpl();
	}
	
	/***
	 * Family method for static implementation.
	 */
	protected abstract void doSleep(int timeOut);
	/***
	 * 
	 * Family method for static implementation.
	 */
	protected abstract SpcfThread getCurrentThreadImpl();
  

}
