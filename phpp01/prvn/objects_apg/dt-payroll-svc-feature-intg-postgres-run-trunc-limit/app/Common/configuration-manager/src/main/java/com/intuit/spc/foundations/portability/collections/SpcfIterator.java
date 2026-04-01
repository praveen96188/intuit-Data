package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.annotations.SpcfNonPortableClass;

/***
 * Portable iterator base class suitable for iterator derivation implementations.
 */
@SpcfNonPortableClass
public abstract class SpcfIterator<T> implements ISpcfIterator<T> {

	/***
	 * The current item.
	 */
	protected T mCurrentItem;
	
	/***
	 * Represents if mCurrentItem exists (value may be null).
	 */
	protected boolean mHasCurrentItem; 
    
	
	/**
	 * @see ISpcfIterator#hasNext()
	 */
	public abstract boolean hasNext();

	/**
	 * @see ISpcfIterator#next()
	 */
	public abstract T next();

	/**
	 * @see ISpcfIterator#moveNext()
	 */
    public abstract boolean moveNext();
	
	/**
	 * @see ISpcfIterator#getCurrent()
	 */
    public T getCurrent()
    {
    	if (!mHasCurrentItem)
    	{
    		throw new SpcfNoSuchElementException("No element is available.");
    	} 
    	return mCurrentItem; 
    }  

	/**
	 * @see ISpcfIterator#dispose()
	 */
    public void dispose()
    {
    	// Quietly ignored. No implementation.
    } 

	/**
	 * This method is not supported.
	 * @see ISpcfIterator#remove()
	 */
	public void remove()
	{
		throw new SpcfUnsupportedOperationException();
	}

	/**
	 * This method is not supported.
	 * @see ISpcfIterator#reset()
	 */
	public void reset() 
	{
		throw new SpcfUnsupportedOperationException("Not supported for portable code.");
	}
	
	//placeholder - required property for c# IEnumerator
	//object System.Collections.IEnumerator.Current
    //{
    //    get { return this.Current; }
    //}

}
