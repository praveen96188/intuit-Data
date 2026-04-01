package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * This class implements a resizable set backed by a hash table. It makes no 
 * guarantees as to the iteration order of the set; This class permits 
 * the null element.
 */

public abstract class SpcfHashSet<T> extends SpcfSet<T> implements ISpcfCloneable<SpcfHashSet<T>>
{
	/**
	 * Value used only on java side for serialization.
	 */
	private static final long serialVersionUID = 7120980317568350288L;



	/**
	 * Adds the specified element to this set if it is not already present. null objects are allowed. 
	 * @param o the object of T to be added to the set.
	 * @return true if the set changed as a result of the call, false otherwise.
	 * @throws SpcfIllegalArgumentException some aspect of an element of the 
	 * specified collection prevents it from being added to this collection.  
	 * One reason the class would prevent the specified object from being added is if
	 * an object already exists in the HashSet which cannot be compared to the
	 * specified object.
 	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#add(T)
	 */
	public abstract boolean add(T o);
	
	/**
	 * Adds all of the elements in the specified collection to this set if they're 
	 * not already present. null collection elements are allowed.
	 * @return true if the set changed as a result of the call, false otherwise.
	 * @throws SpcfArgumentNullException If collection is null.
	 */
	public abstract boolean addAll(SpcfCollection<T> collection);
	
	/**
	 * Returns a shallow copy of this HashSet instance: the elements themselves are not cloned.
	 */
	public abstract SpcfHashSet<T> clone();
	
	/**
	 * Returns true if this set contains the specified element. null argument is allowed.
	 */
	public abstract boolean contains(T obj);
	
	/**
	 * Removes the specified element from this set if it is present. null argument is allowed.
	 */
	public abstract boolean remove(T obj);
	
	/**
	 * Constructs an empty SpcfHashSet with the default initial capacity (10)
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @return an SpcfHashSet&lt;T&gt; object
	 */
	public static <S> SpcfHashSet<S> createInstance()
	{
		return SpcfFactory.getInstance().<S>createHashSet();
	}
	
	/**
	 * Constructs an empty SpcfHashSet with the default initial capacity (10)
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfHashSet&lt;T&gt; object
	 */
	public static <S> SpcfHashSet<S> createInstance(SpcfClass typeParam)
	{
		return SpcfFactory.getInstance().<S>createHashSet(typeParam);
	}
	
	/**
	 * Returns an empty SpcfHashSet with the specified initial capacity
	 * and the default load factor  (1.0 for .NET and 0.75 for Java).
	 * @param initialCapacity The initial capacity.
	 * @return an SpcfHashSet&lt;T&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public static <S> SpcfHashSet<S> createInstance(int initialCapacity)
	{
		return SpcfFactory.getInstance().<S>createHashSet(initialCapacity);
	}
	
	/**
	 * Returns an empty SpcfHashSet with the specified initial capacity
	 * and the default load factor  (1.0 for .NET and 0.75 for Java).
	 * @param initialCapacity The initial capacity.
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfHashSet&lt;T&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public static <S> SpcfHashSet<S> createInstance(int initialCapacity, SpcfClass typeParam)
	{
		return SpcfFactory.getInstance().<S>createHashSet(initialCapacity, typeParam);
	}


	/**
	 * Constructs an SpcfHashSet with default load factor 
	 * (1.0 for .NET and 0.75 for Java) and adds collection items into the set.
	 * @param c collection of T which is added into the set.
	 * @return an SpcfHashSet&lt;T&gt; object
	 * @throws SpcfArgumentNullException if collection is null.
	 */
	public static <S> SpcfHashSet<S> createInstance(SpcfCollection<S> c)
	{
		return SpcfFactory.getInstance().<S>createHashSet(c);
	}
}
