/*
 * author		RGS
 * department	SPC Foundations
 * project	    Portability
 * 2004-05-03   Initial Implementation
 */
package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * A resizable-array implementation of the SpcfCollection. The size of this list
 * is dynamically increased as required, positional access is allowed, and
 * duplicates and null values are generally permitted.
 * <p>Each SpcfArrayList&lt;T&gt; instance has a capacity. The capacity is the size
 * of the array used to store the elements in the list.
 * It is always at least as large as the list size.
 * As elements are added to an ArrayList, its capacity grows
 * automatically. The details of the growth policy are not
 * specified beyond the fact that adding an element has constant
 * amortized time cost.
 * </p>
 */
public abstract class SpcfArrayList<T> extends SpcfList<T> implements ISpcfCloneable<SpcfArrayList<T>>
{
	/**
	 * Value used only on java side for serialization.
	 */
	private static final long serialVersionUID = -9107299750687549153L;
	
	
	/**
	 * Returns a new instance of SpcfArrayList containing a shallow copy of the encapsulated
	 * 3rd party list.
	 * <p>A shallow copy of a collection copies only the elements of the collection,
	 * whether they are reference types or value types, but it does not copy the objects
	 * that the references refer to. The references in the new collection point to the
	 * same objects that the references in the original collection point to.</p>
	 *
	 * @return A new instance of SpcfArrayList containing a shallow copy of the encapsulated 3rd party list
	 * @throws SpcfUnsupportedOperationException - if the clone method is not supported by this list.
	 */
	public abstract SpcfArrayList<T> clone();

	/**
	 * Returns an empty portable list with an initial capacity of ten.
	 * @return an SpcfArrayList&lt;T&gt; object
	 */
	public static <S> SpcfArrayList<S> createInstance()
	{
		return SpcfFactory.getInstance().<S>createArrayList();
	}
	
	/**
	 * Returns an empty portable list with an initial capacity of ten.
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfArrayList&lt;T&gt; object
	 */
	public static <S> SpcfArrayList<S> createInstance(SpcfClass typeParam)
	{
		return SpcfFactory.getInstance().<S>createArrayList(typeParam);
	}

	/**
	 * Returns a portable list.
	 * @param initialCapacity the initial capacity of the list.
	 * @return an SpcfArrayList&lt;T&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public static <S> SpcfArrayList<S> createInstance(int initialCapacity)
	{
		return SpcfFactory.getInstance().<S>createArrayList(initialCapacity);
	}
	
	/**
	 * Returns a portable list.
	 * @param initialCapacity the initial capacity of the list.
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfArrayList&lt;T&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public static <S> SpcfArrayList<S> createInstance(int initialCapacity, SpcfClass typeParam)
	{
		return SpcfFactory.getInstance().<S>createArrayList(initialCapacity, typeParam);
	}
}
