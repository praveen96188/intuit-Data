package com.intuit.spc.foundations.portability.threading;
 
/***
 * A portable interface implemented by clients and used by SpcfThread.
 * Implement ISpcfUnhandledExceptionHandler to provide code to override the 
 * add custom notification behavior when an exception is thrown and goes 
 * unhandled. 
 * @see com.intuit.spc.foundations.portability.threading.SpcfThread  
 */
public interface ISpcfThreadUncaughtExceptionHandler 
{
	/***
	 * The entry point invoked by SpcfThread for handling uncaught exceptions
	 * @param thread SpcfThread that threw the exception. 
	 * @param exception Throwable/Exception that was thrown. 
	 */
	public void handleException(SpcfThread thread, Throwable exception);

}
