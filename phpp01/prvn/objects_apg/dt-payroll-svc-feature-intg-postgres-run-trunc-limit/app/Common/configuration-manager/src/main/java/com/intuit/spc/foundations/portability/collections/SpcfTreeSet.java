/*
 * author		LGM
 * department	SPC Foundations
 * project	    Portability
 * 2004-10-01   Initial Implementation
 */

package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * A resizable-tree implementation of the SpcfCollection&lt;T&gt;. The size of this tree
 * is dynamically increased as required, and no duplicates are permitted.
 * <p>
 * This class guarantees that the sorted set will be in ascending
 * element order, sorted according to the natural order of the
 * elements, or by the comparator provided at
 * set creation time, depending on which constructor is used.
 * </p>
 * <p>
 * The objects added to the SpcfTreeSet should implement 
 * Comparable interface on java or System.IComparable on .Net if the comparator is not
 * provided in the method to create an instance of SpcfTreeSet, otherwise the 
 * SpcfIllegalArgumentException will be thrown
 * as detailed in the next paragraph.
 * </p>
 * <p>A SpcfIllegalArgumentException is thrown from an add, addAll, contains,
 * getItem, or remove method call when some aspect of a object prevents the method 
 * from completing successfully.  One reason
 * for this exception to be thrown is if the class of a parameter passed into
 * the method is incompatible with an existing class in the TreeSet.  In this context,
 * classes are incompatible if an exception is thrown when a comparison between an instance
 * of the two classes is performed.
 * </p>
 * 
 */

public abstract class SpcfTreeSet<T> extends SpcfSet<T> implements ISpcfCloneable<SpcfTreeSet<T>>
{
	private static final long serialVersionUID = -519309489124953725L;
	
	
	/**
	 * Adds an object to the TreeSet&lt;T&gt;.
	 * @param obj the object of T to be added to the collection
	 * @return true if the collection changed as a result of the call,
	 * false if the object was not added to the TreeSet&lt;T&gt; because the object 
	 * already exists in the TreeSet.
	 * @throws SpcfArgumentNullException null objects cannot be added.
	 * @throws SpcfIllegalArgumentException some aspect of an element of the specified 
	 * collection prevents it from being added to this collection.  One reason the 
	 * class would prevent the specified object from being added is if
	 * an object already exists in the TreeSet which cannot be compared to the
	 * specified object.
	 * @throws SpcfClassCastException Not thrown by SpcfTreeSet&lt;T&gt;.Add,
	 * instead SpcfIllegalArgumentException will be thrown if the type of
	 * the specified element is incompatible with the elements already in this collection.
	 * @throws SpcfUnsupportedOperationException not thrown by SpcfTreeSet&lt;T&gt;.Add
	 */
	public abstract boolean add(T obj);

	/**
	 * Adds all of the elements in the specified collection to this TreeSet&lt;T&gt;.
	 * The behavior of this operation is undefined if the specified collection
	 * is modified while the operation is in progress.
	 * This implementation iterates over the specified collection, and adds each
	 * object returned by the iterator to this TreeSet&lt;T&gt;, in turn.
	 * @param collection collection whose elements are to be added to this TreeSet&lt;T&gt;.
	 * @return true if this collection changed as a result of the call.
	 * @throws SpcfUnsupportedOperationException not thrown by SpcfTreeSet&lt;T&gt;.AddAll
	 * @throws SpcfClassCastException Not thrown by SpcfTreeSet&lt;T&gt;.AddAll,
	 * instead SpcfIllegalArgumentException will be thrown if the type of an element in
	 * the specified collection is incompatible with the elements already in this TreeSet&lt;T&gt;.
	 * @throws SpcfArgumentNullException the specified object is
	 * null or an element in the specified collection is null.
	 * @throws SpcfIllegalArgumentException some aspect of an
	 * element of the specified collection prevents it from being added
	 * to this TreeSet.  One reason the class would prevent the specified
	 * object from being added is if an object already exists in the TreeSet&lt;T&gt;
	 * which cannot be compared to the specified object.
	 */
	public abstract boolean addAll(SpcfCollection<T> collection);

	/**
	 * Returns a new instance of SpcfTreeSet&lt;T&gt; containing a shallow copy of the
	 * encapsulated 3rd party tree set.
	 *
	 * <p>A shallow copy of a collection copies only the elements of the
	 * collection, whether they are reference types or value types, but
	 * it does not copy the objects that the references refer to. The
	 * references in the new collection point to the same objects that the
	 * references in the original collection point to.
	 *
	 * @return A new instance of SpcfTreeSet&lt;T&gt; containing a shallow copy of the
	 * encapsulated 3rd party set
	 */
	public abstract SpcfTreeSet<T> clone();

	/**
	 * Returns true if this set contains the specified element.
	 *
	 * @param obj element of T whose presence in this set is to be tested.
	 * @return  true if the specified element is present; false otherwise.
     * @throws SpcfIllegalArgumentException if the type of the specified element
     * 	       is incompatible with this collection.
     * @throws SpcfArgumentNullException if the specified element is null.
	 **/
	public abstract boolean contains(T obj);

	/**
	 * Removes a single instance of the specified element from this collection if it is present.
	 * @param obj element of T to be removed from this collection.
	 * @throws SpcfIllegalArgumentException some aspect of an
	 * element of the specified collection prevents it from being removed
	 * from this collection.  One reason the class would prevent the specified
	 * object from being removed is if an object already exists in the TreeSet
	 * which cannot be compared to the specified object.
	 * @throws SpcfArgumentNullException the specified object is null.
	 * @throws SpcfUnsupportedOperationException not thrown by SpcfTreeSet.remove
	 * @throws SpcfClassCastException Not thrown by SpcfTreeSet.remove,
	 * instead SpcfIllegalArgumentException will be thrown if the type of
	 * the specified element is incompatible with the elements already in this collection.
	 **/
	public abstract boolean remove(T obj);
	
	/**
	 * Returns an empty portable tree set.  The default comparator will be used.
	 * @return an SpcfTreeSet&lt;T&gt; object
	 */
	public static <S> SpcfTreeSet<S> createInstance()
	{
		return SpcfFactory.getInstance().<S>createTreeSet();
	}
	
	/**
	 * Returns an empty portable tree set.  The default comparator will be used.
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfTreeSet&lt;T&gt; object
	 */
	public static <S> SpcfTreeSet<S> createInstance(SpcfClass typeParam)
	{
		return SpcfFactory.getInstance().<S>createTreeSet(typeParam);
	}
	
	/**
	 * Returns the first object contained within the set, according to the set's
	 * comparator object.
	 * @return The first set in the map.
	 */
	public abstract T first();
	
	/**
	 * Returns the last object contained within the set, according to the set's
	 * comparator object.
	 * @return The last set in the map.
	 */
	public abstract T last();

	/**
	 * Returns the ISpcfComparator used by the current set for evaluating objects
	 * contained within the set.
	 * @return The comparator object for the current set, or null if the object's
	 * natural ordering is used.
	 */
	public abstract ISpcfComparator<T> comparator();

	/**
	 * Returns an empty portable tree set with the specified comparator.
	 * @param c an object of ISpcfComparator (A null value indicates 
	 * that the keys' natural ordering should be used.)
	 * @return an SpcfTreeSet&lt;T&gt; object
	 */
	public static <S> SpcfTreeSet<S> createInstance(ISpcfComparator<S> c)
	{
		return SpcfFactory.getInstance().<S>createTreeSet(c);
	}
}
