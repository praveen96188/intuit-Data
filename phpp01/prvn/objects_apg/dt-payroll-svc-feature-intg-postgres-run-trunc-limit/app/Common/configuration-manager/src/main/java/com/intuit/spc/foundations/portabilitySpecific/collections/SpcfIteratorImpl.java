/*
 * author		RGS
 * department	SPC Foundations
 * project	    Portability
 * 2004-05-06   Initial Implementation
 *
 * NOTE: All third-party runtime exceptions are caught and re-thrown as
 * portable exceptions
 */
package com.intuit.spc.foundations.portabilitySpecific.collections;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.collections.*;
import java.util.*;

/**
 * A platform specific implementation of ISpcfIterator
 */
public class SpcfIteratorImpl<T> extends SpcfIterator<T>
{
	private Iterator<T> mThirdPartyIterator;

	/**
	 * Constructs a portable Iterator with the specified third party runtime implementation
	 * of java.util.Iterator.
	 *
	 * @param   thirdPartyIterator  java.util.Iterator.
	 */
	public SpcfIteratorImpl(Iterator<T> thirdPartyIterator)
	{
		SpcfParamValidator.checkIsNotNull(thirdPartyIterator, "thirdPartyIterator");
		mThirdPartyIterator = thirdPartyIterator;
	}

	/**
	 * Returns the encapsulated third party runtime object.
	 * @return a java.util.Iterator implementation
	 */
	public Iterator<T> toSpecific()
	{
		return mThirdPartyIterator;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfIterator#hasNext()
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		return mThirdPartyIterator.hasNext();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfIterator#next()
	 * @see java.util.Iterator#next()
	 */
	public T next()
	{
		try
		{
			// Cache currentItem
			mHasCurrentItem = false;
			mCurrentItem = mThirdPartyIterator.next();
			mHasCurrentItem = true;
		}
		catch(NoSuchElementException e)
		{
			throw new SpcfNoSuchElementException(e);
		}
		catch(ConcurrentModificationException e)
		{
			throw new SpcfConcurrentModificationException(e);
		}

		return mCurrentItem;
	}  

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfIterator#moveNext()
	 */
    public boolean moveNext()
    {
    	// For the simple case, move to next and return.
     	if (hasNext()) 
    	{
    		mCurrentItem = next();
    		return true;
    	}

     	// Below here is a moveNext false case.
     	// We add behavior to enforce moveNext semantics.
     	try
    	{
     		// hasNext doesn't detect a SpcfConcurrentModificationException
     		// but the expected semantics of moveNext are required to detect it.
     		// If we move forward we know we will fail.
     		// But this call will force a SpcfConcurrentModificationException
     		// if the collection was changed.
     		// If instead the exception is simply a SpcfNoSuchElementException,
     		// we eat the exception and just return false.
    		next();
    	}
		catch(SpcfNoSuchElementException e)
		{
			// For this exception only, we eat the exception and return false.
 		}

    	return false;
    }
    
	/**
	 * This method is not supported.
	 * @see com.intuit.spc.foundations.portability.collections.SpcfIterator#remove()
	 */
	public void remove()
	{
		throw new SpcfUnsupportedOperationException();
	}
}
