package com.intuit.spc.foundations.portability.threading;

/***
 * A portable interface implemented by clients and used by SpcfThread.
 * Implement ISpcfRunnable to provide code that will execute on a new thread.
 * @see com.intuit.spc.foundations.portability.threading.SpcfThread
 */
public interface ISpcfRunnable 
{
	/***
	 * The entry point invoked by SpcfThread on the new thread.
	 */
	public void run();
}
