package com.intuit.spc.foundations.portabilitySpecific.threading;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfInvalidOperationException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfSecurityException;
import com.intuit.spc.foundations.portability.threading.SpcfIllegalThreadStateException;
import com.intuit.spc.foundations.portability.threading.SpcfThread;
import com.intuit.spc.foundations.portability.threading.SpcfThreadInterruptedException;
import com.intuit.spc.foundations.portability.threading.SpcfThreadPriority;
import com.intuit.spc.foundations.portability.threading.SpcfThreadState;
import com.intuit.spc.foundations.portability.threading.ISpcfRunnable; 
import com.intuit.spc.foundations.portability.threading.ISpcfThreadUncaughtExceptionHandler;

public class SpcfThreadImpl extends SpcfThread
{
	private boolean mThreadNameSet;
	protected Thread mThread;
	protected static ISpcfThreadUncaughtExceptionHandler mUncaughtExceptionHandler;
		
	protected class SpcfRunnableWrapper implements Runnable
	{
		ISpcfRunnable mRunnable;
		SpcfRunnableWrapper(ISpcfRunnable runnable)
		{
			this.mRunnable = runnable;
		}
		
		public ISpcfRunnable getRunnable()
		{
			return mRunnable;
		}
		
		public void run()
		{
			// TODO Need to handle unhandled exceptions here
			this.mRunnable.run();
		}
	}
	 
	protected class SpcfUncaughtExceptionWrapper implements Thread.UncaughtExceptionHandler
	{
		private ISpcfThreadUncaughtExceptionHandler mClientHandler; 
		  
		SpcfUncaughtExceptionWrapper(ISpcfThreadUncaughtExceptionHandler handler )
		{
			this.mClientHandler = handler; 
		} 
		 
		public void uncaughtException(Thread t, Throwable e)
		{ 
			if (mClientHandler != null) 
			{
				mClientHandler.handleException(new SpcfThreadImpl(t), e);
			}
		} 
	}
	
	/**
	 * @see SpcfThread#doSetUncaughtExceptionHandler()
	 */ 
	protected void doSetUncaughtExceptionHandler(ISpcfThreadUncaughtExceptionHandler handler, boolean default_only)
	{   
		//if default_only is true, no change can be made if a handler has already been defined
		if (default_only && mUncaughtExceptionHandler != null)
		{ 
			return;  
		}
		
		try
		{
			mUncaughtExceptionHandler = handler;
			if (mUncaughtExceptionHandler == null)
			{
				//removes handler it was defined
				Thread.setDefaultUncaughtExceptionHandler(null);
			}
			else
			{
				// create a wrapper class with expected interface to pass on to handler
				SpcfUncaughtExceptionWrapper handlerWrapper = new SpcfUncaughtExceptionWrapper(handler);
				Thread.setDefaultUncaughtExceptionHandler(handlerWrapper);
				
			} 
		}
		catch(SecurityException se )
		{
			throw new SpcfSecurityException(se);
		}
	}

	/***
	 * Default constructor provided for two-phase construction. No runntime thread
	 * is create until the create is explicitly called. Once create has been called,
	 * a subsequent call to SpcfThread.start must be made before the thread 
	 * begins to run.
	 *
	 */
	public SpcfThreadImpl()
	{
	}

	/***
	 * Constructor overload used for single phase construction. Runtime thread is created
	 * and available. A subsequent call to SpcfThread.start must be made before thread
	 * begins to run.
	 * @param runnableObject A client implementation of ISpcfRunnable. 
	 * This is the code that will run on the new thread.
	 */
	public SpcfThreadImpl(ISpcfRunnable runnableObject)
	{
		create(runnableObject);
	}
	
	/***
	 * Wrapper method to provide a portable thread api for an already existing
	 * thread. This is intended for internal use only.
	 * @param t A previously existing thread reference.
	 * @throws SpcfArgumentNullException if specified thread is null.
	 */
	public SpcfThreadImpl(Thread t)
	{
		SpcfParamValidator.checkIsNotNull(t, "t");
		this.mThread = t;
	}

	/***
	 * Override to forward identity check to runtime thread if it has been created.
	 */
	@Override
	public boolean equals(Object o)
	{
		if (o == null) return false;
		
		if (o instanceof SpcfThreadImpl)
		{
			SpcfThreadImpl oThread = (SpcfThreadImpl)o;
			
			// Both Threads are null, use default equality
			if ((mThread == null) && (oThread.mThread == null))
			{
				return super.equals(o);
			}
			// Both Threads are not null, use Thread identity
			else if ((mThread != null) && (oThread.mThread != null))
			{
				return mThread.equals(oThread.mThread);
			}			
		}
		
		// otherwise, one or the other is null, or the specific object isn't an SpcfThreadImpl.
		// So, we can't be equal.
		return false;
	}
	
	/***
	 * Overridden since the equals operator is also overridden.
	 * @return Returns the hashcode for this instance.
	 */
	@Override
	public int hashCode()
	{
		if (mThread != null) return mThread.hashCode();
		
		return super.hashCode();
	}
	
	/***
	 * Returns platform specific thread instance. Maybe be null.
	 * @return The platform specific thread, null otherwise.
	 */
	public Thread toSpecific()
	{
		return mThread;
	}
	/**
	 * Initializes a new platform thread
	 */
	private void create(ISpcfRunnable runnableObject)
	{
		SpcfParamValidator.checkIsNotNull(runnableObject, "runnableObject");
		
		// Object semantic check
		if (mThread != null)
		{
			throw new SpcfInvalidOperationException("The thread has already been created.");			
		}
		
		mThreadNameSet = false;
		
		// Create the thread
		mThread = new Thread(new SpcfRunnableWrapper(runnableObject));
		
		// Set its default name to empty
		mThread.setName("");
		
		// to explicitly default to a user thread regardless
		// of the characteristics of the calling thread.
		mThread.setDaemon(false);			
	}

	/**
	 * @see SpcfThread#start()
	 */
	@Override
	public void start() 
	{
		try
		{
			objectSemanticCheck();
			mThread.start();
		}
		catch(IllegalThreadStateException  s)
		{
			throw new SpcfIllegalThreadStateException(s);
		}
	}

	/**
	 * @see SpcfThread#start(ISpcfRunnable)
	 */
	@Override
	public void start(ISpcfRunnable runnableObject)
	{
		create(runnableObject);
		start();
	}

	/**
	 * @see SpcfThread#setName(String)
	 */
	@Override
	public void setName(String name) 
	{
		SpcfParamValidator.checkIsNotNull(name, "name");

		try
		{
			objectSemanticCheck();

			if (!mThreadNameSet)
			{
				mThread.setName(name);
				mThreadNameSet = true;
			}
			else
			{
				throw new SpcfInvalidOperationException("The thread name has already been set.");
			}
		}
		catch(SecurityException s)
		{
			throw new SpcfSecurityException(s);
		}
	}

	/**
	 * @see SpcfThread#getName()
	 */
	@Override
	public String getName() 
	{
		objectSemanticCheck();
		return mThread.getName();
	}

	/**
	 * @see SpcfThread#getThreadState()
	 */
	@Override
	public SpcfThreadState getThreadState() 
	{
		objectSemanticCheck();
		if (mThread.getState() == Thread.State.NEW)
		{
			return SpcfThreadState.Initialized;
		}
		else if (mThread.getState() == Thread.State.RUNNABLE)
		{
			return SpcfThreadState.Running;
		}
		else if (mThread.getState() == Thread.State.TERMINATED)
		{
			return SpcfThreadState.Terminated;
		}
		else 
		{
			/*if ((t.getState() == Thread.State.BLOCKED) ||
			     (t.getState() == Thread.State.WAITING) ||
				 (t.getState() == Thread.State.TIMED_WAITING))

			*/
			return SpcfThreadState.Wait;
		}
	}

	/**
	 * @see SpcfThread#setPriority(SpcfThreadPriority)
	 */
	@Override
	public void setPriority(SpcfThreadPriority priority) 
	{
		// We can't use SpcfParamValidator.checkIsNotNull here since the C#
		// implementation cannot be null (Enums are value types.) Instead
		// C# can hold illegal values. Use SpcfIllegalArgumentException
		// for maintain platform semantics for these two different cases.
		if (priority == null)
		{
			throw new SpcfIllegalArgumentException("priority cannot be null.");
		}
		
		objectSemanticCheck();
		
		try
		{		
			// Can't set priority if the thread has terminated.
			if (mThread.getState() == Thread.State.TERMINATED)
			{
                throw new SpcfIllegalThreadStateException("Thread has already terminated.");
			}
							
			if (SpcfThreadPriority.Minimum == priority)
			{
				mThread.setPriority(Thread.MIN_PRIORITY);
			}
			else if (SpcfThreadPriority.Normal == priority)
			{
				mThread.setPriority(Thread.NORM_PRIORITY);
			}
			else if (SpcfThreadPriority.Maximum == priority)
			{
				mThread.setPriority(Thread.MAX_PRIORITY);
			}
			else
			{
				throw new SpcfIllegalArgumentException("Unknown SpcfThreadPriority: " + priority.toString());
			}
		}
		catch(IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
		catch(SecurityException e)
		{
			throw new SpcfSecurityException(e);		
		}	
	}

	/**
	 * @see SpcfThread#getPriority()
	 */
	@Override
	public SpcfThreadPriority getPriority() 
	{
		objectSemanticCheck();
		
		int priority = mThread.getPriority();
		if (priority < Thread.NORM_PRIORITY)
		{
			return SpcfThreadPriority.Minimum;
		}
		else if (priority > Thread.NORM_PRIORITY)
		{
			return SpcfThreadPriority.Maximum;			
		}

		return SpcfThreadPriority.Normal;
	}

	/**
	 * @see SpcfThread#sleep()
	 * No object state is accessed so no object semantic check is necessary only parameter check.
	 */
	@Override
	protected void doSleep(int timeOut) 
	{
		SpcfParamValidator.checkIsNonNegative(timeOut, "timeOut");
		
		try 
		{
			// force at least 1ms timeOut - Java may not throw interrupt for sleep(0).
			if (timeOut == 0) timeOut = 1;
			
			Thread.sleep(timeOut);
		} 
		catch (InterruptedException e) 
		{
			throw new SpcfThreadInterruptedException(e);
		}
	}

	/**
	 * @see SpcfThread#interrupt()
	 */
	@Override
	public void interrupt() 
	{
		objectSemanticCheck();
		try
		{
			mThread.interrupt();
		}
		catch(SecurityException e)
		{
			throw new SpcfSecurityException(e);
		}
	}

	/**
	 * @see SpcfThread#join()
	 */
	@Override
	public boolean join() 
	{
		objectSemanticCheck();
		
		// Zero timeOut in Java implies wait forever.
		return joinImpl(0);
	}

	/**
	 * @see SpcfThread#join(int)
	 */
	@Override
	public boolean join(int timeOut) 
	{
		SpcfParamValidator.checkIsNonNegative(timeOut, "timeOut");
		objectSemanticCheck();

		// Zero timeOut implies status check only
		if (timeOut == 0)
		{
			// timeOut == 0 means wait forever in Java. Here we want to simply check.
			timeOut = 1;
		}
		
		return joinImpl(timeOut);
	}

	/**
	 * Private implementation. Assumes object semantics have been checked.
	 */
	private boolean joinImpl(int timeOut) 
	{
		if (timeOut < 0)
		{
			throw new SpcfIllegalArgumentException("The specified timeOut was negative.");
		}
		
		try 
		{
			if (mThread.getState() == Thread.State.NEW)
			{
				throw new SpcfIllegalThreadStateException("Thread was not started.");				
			}
			
			mThread.join(timeOut);
			
			return mThread.getState() == Thread.State.TERMINATED;			
		} 
		catch (InterruptedException e) 
		{
			throw new SpcfThreadInterruptedException(e);
		}
	}

	/**
	 * @see SpcfThread#setBackground(boolean)
	 */
	@Override
	public void setBackground(boolean isBackground) 
	{
		objectSemanticCheck();

		try
		{
			mThread.setDaemon(isBackground);
		}
		catch(IllegalThreadStateException e)
		{
			throw new SpcfIllegalThreadStateException(e);
		}
		catch(SecurityException e)
		{
			throw new SpcfSecurityException(e);		
		}
	}

	/**
	 * @see SpcfThread#getBackground()
	 */
	@Override
	public boolean getBackground() 
	{
		objectSemanticCheck();
		return mThread.isDaemon();
	}
	
	/**
	 * @see SpcfThread#getId()
	 */
	@Override
	public long getId()
	{
		objectSemanticCheck();
		return mThread.getId();	
	}
	
	/**
	 * @see SpcfThread#getCurrentThreadImpl()
	 */
	@Override
	protected SpcfThread getCurrentThreadImpl()
	{
		return new SpcfThreadImpl(Thread.currentThread());
	}

	// Simple semantic check since SpcfThread can be created without creating a thread.
	private void objectSemanticCheck()
	{
		// Object semantic check
		if (mThread == null)
		{
			throw new SpcfInvalidOperationException("The thread must first be created.");			
		}
	}
}
