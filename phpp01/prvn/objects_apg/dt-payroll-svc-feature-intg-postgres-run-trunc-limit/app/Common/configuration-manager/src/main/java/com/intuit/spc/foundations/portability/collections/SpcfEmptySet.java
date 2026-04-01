package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 *	The immutable empty set.
 */

public abstract class SpcfEmptySet<T> extends SpcfSet<T> 
{
	/**
	 * Value used only on java side for serialization.
	 */
	private static final long serialVersionUID = -5148164565571813171L;
	


	/**
	 * Overrides the default implementation to check if 
	 * Object is a derivation of SpcfSet and of size == 0.
	 * If it is, then we assume we are equal.
	 */
    public boolean equals(Object o) 
    {
        return ((o instanceof ISpcfSet) && (((ISpcfSet)o).getSize()== 0));
    }

    /**
     * Override default. Our value semantics are constant, so we use a 0 hashcode.
     */
    public int hashCode()
    {
    	return 0;
    }
    
	/**
	 * The set is not modified.
	 * @param obj is ignored and not used.
	 * @return Always returns false.
	 */
	public boolean add(T obj) 
	{
		return false;
	}

	/**
	 * The set is not modified.
	 * @param collection is ignored and not used.
	 * @return Always returns false.
	 */
	public boolean addAll(SpcfCollection<T> collection) 
	{
		return false;
	}

	/**
	 * The set is not modified. This is a no-op method call.
	 */
	public void clear() 
	{
	}

	/**
	 * The set is always empty, so there is nothing to search. 
	 * @param obj is ignored and not used.
	 * @return Always returns false. 
	 */
	public boolean contains(T obj) 
	{
		return false;
	}

	/**
	 * The set is not modified. This is a no-op method call.
	 * @param obj is ignored and not used.
	 */
	public boolean remove(T obj) 
	{
		return false;
	}

	/**
	 * This empty set always is size of zero.
	 * @return Always returns 0.
	 */	
	public int getSize() 
	{
		return 0;
	}

	/**
	 * This set contains no elements and calling this method always returns a zero length array.
	 * @return N/A
	 */
	public abstract Object[] toArray();
	
	/**
	 * This set contains no elements and calling this method always returns the provided array.
	 * All cells of the provided array, if any, are set to null.
	 * @return N/A
	 * @throws SpcfArgumentNullException if a == null.
	 */
	public abstract <E> E[] toArray(E[] a);
	
	
	/**
	 * Constructs an immutable empty set.
	 * @return New instance of an immutable empty set.
	 */
	public static <S> SpcfSet<S> createInstance()
	{
		return SpcfFactory.getInstance().<S>createEmptySet();
	}
	
	/**
	 * Constructs an immutable empty set.
	 * @param typeParam the type parameter for this generic type
	 * @return New instance of an immutable empty set.
	 */
	public static <S> SpcfSet<S> createInstance(SpcfClass typeParam)
	{
		return SpcfFactory.getInstance().<S>createEmptySet(typeParam);
	}
}
