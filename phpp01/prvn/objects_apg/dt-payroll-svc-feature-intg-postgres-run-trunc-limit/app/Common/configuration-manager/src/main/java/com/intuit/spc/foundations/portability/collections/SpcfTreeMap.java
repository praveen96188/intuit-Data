/*
 * author		LGM
 * department	SPC Foundations
 * project	    Portability
 * 2005-02-15   Initial Implementation
 */
package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * SpcfTreeMap&lt;K,V&gt; is an object that maps keys of K to values of V.  
 * A map cannot contain duplicate keys;
 * each key can map to at most one value.  Duplicate values are supported.
 * Null keys are not allowed but null values are allowed.
 * The map will be in ascending key order, sorted according to the natural
 * order for the key's class, or by the comparator provided
 * at creation time, depending on which constructor is used.  The keys should implement 
 * Comparable interface on java or System.IComparable on .Net if the comparator is not
 * provided in the constructor, otherwise the SpcfIllegalArgumentException will be thrown
 * as detailed in the next paragraph.
 *
 * <p>A SpcfIllegalArgumentException is thrown from an add, addAll, containsKey,
 * getItem, or remove method call when some aspect of a key or value
 * parameter prevents the method from completing successfully.  One reason
 * for this exception to be thrown is if the class of a parameter passed into
 * the method is incompatible with an existing class in the TreeMap.  In this context,
 * classes are incompatible if an exception is thrown when a comparison between an instance
 * of the two classes is performed.</p>
 *
 * <p>A SpcfUnsupportedOperationException is thrown from an add, addAll, clear,
 * or remove method when the method is not supported by the encapsulated collection.
 * The encapsulated instances of TreeMap created internally within SpcfTreeMap support all of
 * the stated methods.  If a readonly instance of a TreeMap is passed into the
 * SpcfTreeMapImpl constructor, then the stated methods will throw
 * SpcfUnsupportedOperationException.
 * </p>
 */

public abstract class SpcfTreeMap<K,V> extends SpcfMap<K,V> implements ISpcfCloneable<SpcfTreeMap<K,V>>
{
	private static final long serialVersionUID = 3047154806865554040L;

	/**
	 * Associates the specified value of V with the specified key of K in this map.
	 * If the map previously contained a mapping for
	 * this key, the old value is replaced by the specified value.  A map
	 * m is said to contain a mapping for a key k if and only if
	 * {@link #containsKey(K)} would return true.
	 *
	 * @param key key of K with which the specified value is to be associated.
	 * @param val value of V to be associated with the specified key.
	 *
     * @throws SpcfUnsupportedOperationException if add is not supported by
     *         this collection.
	 * @throws SpcfIllegalArgumentException if some aspect of this key or value
	 * 		   prevents it from being stored in this map.
	 * @throws SpcfArgumentNullException if the key is null.
	 */
	public abstract V add(K key, V val);


	/**
	 * Adds all of the elements in the specified collection to this collection.
	 * The behavior of this operation is undefined if the specified collection
	 * is modified while the operation is in progress.
	 * These mappings replace any mappings that this map had for any of the keys
	 * currently in the specified map.
	 *
	 * This implementation iterates over the specified collection, and adds each
	 * object returned by the iterator to this collection, in turn.  All elements of
	 * the specified map will be added or if there is an exception, no elements
	 * from the specified map will be added.
	 *
	 * @param map map whose elements are to be added to this collection.
     * @throws SpcfUnsupportedOperationException if addAll is not supported by
     *         this collection.
     * @throws SpcfArgumentNullException the specified collection contains one
     *         or more null elements and this collection does not support null
     *         elements or if the specified collection is null.
     * @throws SpcfIllegalArgumentException if some aspect of a key or value in the
     * 		   specified map prevents it from being stored in this map.
	 */
	public abstract void addAll(SpcfMap<K,V> map);

	/**
	 * Removes all mappings from this map.
	 *
     * @throws SpcfUnsupportedOperationException if clear is not supported by
     *         this collection.
 	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#clear()
	 */
	public abstract void clear();

	/**
	 * Returns a new instance of SpcfTreeMap&lt;K,V&gt; containing a shallow copy of the encapsulated
	 * 3rd party map: the keys and values themselves are not cloned.
	 *
	 * @return a shallow copy of this map.
	 */
	public abstract SpcfTreeMap<K,V> clone();

	/**
	  * Returns true if this map contains a mapping for the specified
	  * key.
	  *
	  * @param key key whose presence in this map is to be tested.
	  * @return true if this map contains a mapping for the specified
	  *         key.
	  *
	  * @throws SpcfIllegalArgumentException if the key is of an inappropriate type for
	  * 		  this map.
	  * @throws SpcfArgumentNullException if the key is null.
	  */
	public abstract boolean containsKey(K key);

	/**
	 * Returns true if this map maps one or more keys to the
	 * specified value.
	 *
	 * @param val value of V whose presence in this map is to be tested.
	 * @return true if this map maps one or more keys to the
	 *         specified value.
	 * @throws SpcfIllegalArgumentException not thrown by SpcfTreeMap&lt;K,V&gt;.containsValue.
	 * @throws SpcfArgumentNullException not thrown by SpcfTreeMap&lt;K,V&gt;.containsValue.
	 * @throws SpcfConcurrentModificationException if the map is changed while looking for the value
	 */
	public abstract boolean containsValue(V val);

	/**
	 * Returns the value to which this map maps the specified key.  Returns
	 * null if the map contains no mapping for this key.  A return
	 * value of null does not <i>necessarily</i> indicate that the
	 * map contains no mapping for the key; it's also possible that the map
	 * explicitly maps the key to null.  The containsKey
	 * operation may be used to distinguish these two cases.
	 *
	 * @param key key of K whose associated value is to be returned.
	 * @return the value of V to which this map maps the specified key, or
	 *	       null if the map contains no mapping for this key.
	 *
	 * @throws SpcfIllegalArgumentException if the key is of an inappropriate type for
	 * 		  this map.
	 * @throws SpcfArgumentNullException if key is null.
	 *
	 */
	public abstract V getItem(K key);

	/**
	 * Returns a read-only collection view of the keys contained in this map.
	 * This collection is updated when the map changes.
	 *
	 * @return a collection view of the keys contained in this map.
 	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#getKeyList()
	 */
	public abstract SpcfCollection<K> getKeyList();

	/**
	 * Returns a read-only collection view of the values contained in this map.
	 * This collection is updated when the map changes.
	 *
	 * @return a collection view of the values contained in this map.
 	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#getValueList()
	 */
	public abstract SpcfCollection<V> getValueList();

	/**
	 * Removes the mapping for this key from this map if it is present.
	 *
	 * @param key key whose mapping is to be removed from the map.
	 *
	 * @return previous value of V associated with specified key, or null if there was no mapping for key.
	 * @throws SpcfIllegalArgumentException if the key is of an inappropriate type for this map.
	 * @throws SpcfArgumentNullException if the key is null.
     * @throws SpcfUnsupportedOperationException if remove is not supported by
     *         this collection.
	 */
	public abstract V remove(K key);
	
	/**
	 * Returns the first key contained within the map, according to the map's
	 * comparator object.
	 * @return The first key in the map.
	 */
	public abstract K firstKey();
	
	/**
	 * Returns the last key contained within the map, according to the map's
	 * comparator object.
	 * @return The last key in the map.
	 */
	public abstract K lastKey();

	/**
	 * Returns the ISpcfComparator used by the current map for evaluating keys
	 * contained within the map.
	 * @return The comparator object for the current map, or null if the object's
	 * natural ordering is used.
	 */
	public abstract ISpcfComparator<K> comparator();
	
	/**
	 * Returns the number of key-value mappings in this map.
	 *
	 * @return the number of key-value mappings in this map.
 	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#getSize()
	 */
	public abstract int getSize();
	
	/**
	 * Returns an empty portable tree map.  The default comparator will be used.
	 * @return an empty SpcfTreeMap&lt;T&gt; object
	 */
	public static <L,W> SpcfTreeMap<L,W> createInstance()
	{
		return SpcfFactory.getInstance().<L,W>createTreeMap();
	}
	
	/**
	 * Returns an empty portable tree map.  The default comparator will be used.
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @return an empty SpcfTreeMap&lt;T&gt; object
	 */
	public static <L,W> SpcfTreeMap<L,W> createInstance(SpcfClass param1, SpcfClass param2)
	{
		return SpcfFactory.getInstance().<L,W>createTreeMap(param1,param2);
	}

	/**
	 * Returns an empty portable tree map which will use the specified comparator.
	 * @param c an object of ISpcfComparator&lt;K&gt; (A null value indicates keys' natural ordering should be used.)
	 * @return an empty SpcfTreeMap&lt;K,V&gt; object
	 */
	public static <L,W> SpcfTreeMap<L,W> createInstance(ISpcfComparator<L> c)
	{
		return SpcfFactory.getInstance().<L,W>createTreeMap(c);
	}
}
