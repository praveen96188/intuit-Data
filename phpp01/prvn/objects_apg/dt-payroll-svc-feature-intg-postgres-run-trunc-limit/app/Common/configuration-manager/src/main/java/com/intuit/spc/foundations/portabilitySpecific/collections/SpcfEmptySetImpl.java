package com.intuit.spc.foundations.portabilitySpecific.collections;

import java.util.Collection;
import java.util.Set;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfClassCastException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfEmptySet;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 *	The concrete implementation of the immutable empty set.
 *  @param <T> the type stored in the set
 */
public class SpcfEmptySetImpl<T> extends SpcfEmptySet<T> implements Set<T>
{

	/**
	 * serial version UID used for serializing
	 */
	private static final long serialVersionUID = -8963745494013578635L;
	
	/**
	 * static single instance
	 */
    @SuppressWarnings("unchecked")
	static SpcfEmptySetImpl sEmptySet;
    
    static 
    {
    	sEmptySet = new SpcfEmptySetImpl();
    }

    /**
     * default constructor
     */
    public SpcfEmptySetImpl() 
    {
		// This is intentially left blank
    }
    
	/**
	 * constructor taking an SpcfClass instance
	 * @param typeParam
	 */
    public SpcfEmptySetImpl(SpcfClass typeParam)
    {
    	mTypeParameters = new SpcfClass[] { typeParam };
    }
    
	/**
	 * returns the instance of the empty set
	 * @param <T> the type to store in the set
	 * @return an instance of the empty set
	 */
    @SuppressWarnings("unchecked")
	public static <T> SpcfEmptySet getInstance()
    {
    	return sEmptySet;
    }

	/**
	 * There are no elements to iterate, but supports the iteration api.
	 * @return Always returns a useable but limited derivation of ISpcfIterator.
	 */
    @Override
	public ISpcfIterator<T> getIterator() 
	{
		return new SpcfEmptyIteratorImpl<T>();
	}
	
    /**
     * @return array of objects
     */
    @Override
	public Object[] toArray()
	{
		return new Object[0];
		//return SpcfToArrayImpl.toArray(this);
	}
	
	/**
	 * returns an array of objects from the set
	 * @param a an array to put the empty array list elements in
	 * @return an array of elements of type E
	 */
    @Override
	@SuppressWarnings("unchecked")
	public <E> E[] toArray(E[] a)
	{
		SpcfParamValidator.checkIsNotNull(a, "Array Argument");
		try
		{
			// cannot call ourselves to do "toArray" - infinite loop
			for (int i = 0; i < a.length; i++)
			{
				a[i] = null;
			}
			return a;
		}
		catch(ClassCastException e)
		{
			throw new SpcfClassCastException(e);
		}
		catch(ArrayStoreException ase)
		{
			throw new SpcfClassCastException(ase);			
		}
		//return SpcfToArrayImpl.toArray(a, this);		
	}


	/**
	 * implement List.size()
	 * @return the length of the list which is always '0'
	 */
	public int size()
	{
		return 0;
	}
	
	/**
	 * do nothing and return false
	 * @param c ignore parameter
	 * @return boolean value will always be false
	 */
    @SuppressWarnings("unchecked")
	public boolean retainAll(Collection<?> c)
	{
    	// do nothing and return false
		return false;
	}
	
	/** 
	 * does nothing 
	 * @param c collection to match against
	 * @return boolean value is always false
	 */
    @SuppressWarnings("unchecked")
	public boolean containsAll(Collection<?> c)
	{
		return false;
	}
	
	/**
	 * Do nothing and return false.
	 * @param c ignore the parameter
	 * @return boolean value
	 */
    @SuppressWarnings("unchecked")
	public boolean removeAll(Collection<?> c)
	{
		return false;
	}
	
	/** 
	 * does nothing
	 * @param c collection to add
	 * @return boolean value is always false
	 */
    @SuppressWarnings("unchecked")
	public boolean addAll(Collection<? extends T> c)
	{
		return false;
	}

	/**
	 * Does nothing.  False returned.
	 * @param o Ignored
	 * @return false
	 */
	@Override
	public boolean remove(Object o)
	{
		return false;
	}

	/**
	 * Does nothing.  False returned
	 * @param o Ignored
	 * @return false
	 */
	@Override
	public boolean contains(Object o)
	{
		return false;
	}
}
