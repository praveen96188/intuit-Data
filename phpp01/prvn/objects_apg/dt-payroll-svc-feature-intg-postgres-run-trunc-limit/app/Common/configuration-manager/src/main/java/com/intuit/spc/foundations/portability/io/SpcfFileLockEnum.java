package com.intuit.spc.foundations.portability.io;

/**
 * Specifies how the file should be locked.<br/><br/>
 * 
 * Because file locking is platform-dependent, we can only enforce locking 
 * within our own process. That means we cannot make any guarantees that 
 * other processes won't access the file simultaneously.
 */
public enum SpcfFileLockEnum
{
	/**
	 * Exclusive Locking.
	 * An exclusive lock prevents other programs from acquiring an overlapping exclusive or shared lock.
	 */
	Exclusive,
	
	/**
	 * Shared Locking.
	 * A shared lock prevents other concurrently-running programs from acquiring an overlapping 
	 * exclusive lock, but does allow them to acquire overlapping shared locks.
	 * Note: Some operating systems do not support shared locks, in which case a request for a 
	 *       a shared lock is automatically converted into a request for an exclusive lock.
	 */
	Shared,
	
	/**
	 * No Locking.
	 * Because locking is controlled by the operating system, if another process already holds 
	 * an exclusive lock, then the request for "no lock" access might still be blocked.
	 */
	None
}
