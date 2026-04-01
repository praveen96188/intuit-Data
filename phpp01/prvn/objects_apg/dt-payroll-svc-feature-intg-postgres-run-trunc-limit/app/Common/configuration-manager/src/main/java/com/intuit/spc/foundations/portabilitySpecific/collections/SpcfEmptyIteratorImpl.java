package com.intuit.spc.foundations.portabilitySpecific.collections;

import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
/**
 * The empty iterator. Used by empty collection implementations.
 * Provides a full usable implementation of SpcfIterator that cannot iterate.  
 */
public class SpcfEmptyIteratorImpl<T> implements ISpcfIterator<T>
{	
	/**
	 * There is never a next element available
	 * @return Always returns false.
	 */
	public boolean hasNext() 
	{
		return false;
	}

	/**
	 * There is never a next element available. Calling this method always throws an exception.
	 * @return N/A
	 * @throws SpcfUnsupportedOperationException
	 */
	public T next() 
	{
		throw new SpcfUnsupportedOperationException();
	}

	/**
	 * There is never a current element. Calling this method always throws an exception.
	 * @return N/A
	 * @throws SpcfUnsupportedOperationException
	 */
	public T getCurrent() 
	{
		throw new SpcfUnsupportedOperationException();
	}

	/**
	 * There is never a next element available. Calling this method always returns false.
	 * @return Always returns false.
	 */
	public boolean moveNext() 
	{
		return false;
	}

	/**
	 * There is nothing to dispose. Calling this method is a no-op operation.
	 */
	public void dispose() 
	{
	}

	/**
	 * There is nothing to remove. Calling this method always throws an exception.
	 * @throws SpcfUnsupportedOperationException
	 */
	public void remove() 
	{
		throw new SpcfUnsupportedOperationException();
	}

	/**
	 * Cannot reset an iteration. Calling this method always throws an exception.
	 * @throws SpcfUnsupportedOperationException
	 */
	public void reset() 
	{
		throw new SpcfUnsupportedOperationException();
	}
}
