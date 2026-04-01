package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 *	The immutable empty list.
 */

public abstract class SpcfEmptyList<T> extends SpcfList<T> 
{
	/**
	 * Value used only on java side for serialization.
	 */
	private static final long serialVersionUID = -4631815555624240262L;
	

	/**
	 * Overrides the default implementation to check if 
	 * Object is a derivation of SpcfSet and of size == 0.
	 * If it is, then we assume we are equal.
	 */
	@Override
    public boolean equals(Object o) 
    {
        return ((o instanceof ISpcfList) && (((ISpcfList)o).getSize()== 0));
    }

    /**
     * Override default. Our value semantics are constant, so we use a 0 hashcode.
     */
    @Override
    public int hashCode()
    {
    	return 0;
    }
	
    /**
     * Does nothing
     * @param index ignored
     * @param obj ignored
     */
	@Override
	public void add(int index, T obj) 
	{
        // Do nothing
	}
	
	/**
	 * Returns null
	 * @param index ignored
	 * @return null
	 */
	@Override
	public T getItem(int index) 
	{
		return null;
	}

	/**
	 * Returns -1 indicating that the item was not found
	 * @param obj Ignored
	 * @return -1
	 */
	@Override
	public int indexOf(T obj) 
	{
		return -1;
	}
	
	/**
	 * Returns -1 indicating that the item was not found
	 * @param obj Ignored
	 * @return -1
	 */
	@Override
	public int lastIndexOf(T obj) 
	{
		return -1;
	}

    /**
     * Does nothing
     * @param index ignored
     */
	@Override
	public void removeAt(int index) 
	{
		// Do nothing.
	}

    /**
     * Does nothing
     * @param index ignored
     * @param obj ignored
     */
	@Override
	public void setItem(int index, T obj) 
	{
		// Do nothing.
	}

    /**
     * Does nothing
     * @param obj ignored
     * @return false
     */
	@Override
	public boolean add(T obj) 
	{
		return false;
	}

	/**
	 * Does nothing
	 * @param collection Ignored
	 * @return false
	 */
	@Override
	public boolean addAll(SpcfCollection<T> collection) 
	{
		return false;
	}

	/**
	 * Does nothing
	 */
	@Override
	public void clear() 
	{
		// Do nothing.
	}

	/**
	 * Does nothing.  False returned
	 * @param obj Ignored
	 * @return false
	 */
	@Override
	public boolean contains(T obj) 
	{
		return false;
	}

	/**
	 * Does nothing.  False returned.
	 * @param obj Ignored
	 * @return false
	 */
	@Override
	public boolean remove(T obj) 
	{
		return false;
	}

	/**
	 * This empty set always is size of zero.
	 * @return Always returns 0.
	 */	
	@Override
	public int getSize() 
	{
		return 0;
	}

	/**
	 * This set contains no elements and calling this method always returns a zero length array.
	 * @return N/A
	 */
	@Override
	public abstract Object[] toArray();

	/**
	 * This set contains no elements and calling this method always returns the provided array.
	 * All cells of the provided array, if any, are set to null.
	 * @return N/A
	 * @throws SpcfArgumentNullException if a == null.
	 */
	@Override
	public abstract <E> E[] toArray(E[] a);
	
	/**
	 * Constructs an immutable empty list.
	 * @return New instance of an immutable empty list.
	 */
	public static <S> SpcfList<S> createInstance()
	{
		return SpcfFactory.getInstance().<S>createEmptyList();
	}
	
	/**
	 * Constructs an immutable empty list.
	 * @param typeParam the type parameter for this generic type
	 * @return New instance of an immutable empty list.
	 */
	public static <S> SpcfList<S> createInstance(SpcfClass typeParam)
	{
		return SpcfFactory.getInstance().<S>createEmptyList(typeParam);
	}
	
	
}
