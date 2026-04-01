/*
 * author		RGS
 * department	SPC Foundations
 * project	    Portability
 * 2004-05-04   Initial Implementation
 */
package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * An object that maps K keys to V values.  A map cannot contain duplicate keys;
 * and each key can map to at most one value.
 * A map can contain duplicate values.
 * The map is not sorted.
 * The map can contain null values but not null keys.
 * The map instance created within the SpcfHashMap&lt;K,V&gt; is the java HashMap&lt;K,V&gt; class.
 * If the SpcfHashMapImpl&lt;K,V&gt;(Map) constructor is called with a different
 * implementation than the standard java HashMap&lt;K,V&gt;, the aspects of this SpcfHashMap&lt;K,V&gt;
 * will be governed by the type of Map encapsulated when using the
 * SpcfHashMapImpl&lt;K,V&gt;(Map) constructor. For instance, if a readonly HashMap&lt;K,V&gt; is
 * created and passed into the SpcfHashMapImpl&lt;K,V&gt;(Map) constructor, any methods which
 * would alter the contents of the Map would throw SpcfUnsupportedOperationException.
 * <p>A SpcfIllegalArgumentException is not thrown from the SpcfHashMap&lt;K,V&gt; class methods.
 * <p>A SpcfUnsupportedOperationException is thrown from an add, addAll, clear,
 * or remove method when the method is not supported by the encapsulated collection.
 * The encapsulated instances of the map created internally within SpcfHashMap&lt;K,V&gt; support all of
 * the stated methods.  If a readonly or fixed size instance of a map is passed into the
 * SpcfHashMapImpl&lt;K,V&gt; constructor, then the stated methods will throw
 * SpcfUnsupportedOperationException.
 * <p>
 * An instance of SpcfHashMap&lt;K,V&gt; has two parameters that affect its
 * performance: initial capacity and load factor. The capacity is the
 * number of buckets in the hash table, and the initial capacity is
 * simply the capacity at the time the hash table is created. The load
 * factor is a measure of how full the hash table is allowed to get
 * before its capacity is automatically increased. When the number of
 * entries in the hash table exceeds the product of the load factor
 * and the current capacity, the capacity is roughly doubled by
 * calling the rehash method.
 */

public abstract class SpcfHashMap<K,V> extends SpcfMap<K,V> implements ISpcfCloneable<SpcfHashMap<K,V>>
{
	private static final long serialVersionUID = 3058468422619424942L;

	/**
	 * Returns a new instance of SpcfHashMap&lt;K,V&gt; containing a shallow copy of the encapsulated
	 * 3rd party hashMap: the keys and values themselves are not cloned.
	 *
	 * @return a shallow copy of this hashMap.
	 */
	public abstract SpcfHashMap<K,V> clone();

	/**
	 * Constructs an empty hashMap with the default initial capacity (10)
	 * and the default load factor  (1.0 for .NET and 0.75 for Java).
	 * @return an SpcfHashMap&lt;K,V&gt; object
	 */
	public static <L,W> SpcfHashMap<L,W> createInstance()
	{
		return SpcfFactory.getInstance().<L,W>createHashMap();
	}
	
	/**
	 * Constructs an empty hashMap with the default initial capacity (10)
	 * and the default load factor  (1.0 for .NET and 0.75 for Java).
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @return an SpcfHashMap&lt;K,V&gt; object
	 */
	public static <L,W> SpcfHashMap<L,W> createInstance(SpcfClass param1, SpcfClass param2)
	{
		return SpcfFactory.getInstance().<L,W>createHashMap(param1,param2);
	}

	/**
	 * Returns an empty portable hashMap with the specified initial capacity
	 * and the default load factor  (1.0 for .NET and 0.75 for Java).
	 * @param initialCapacity The initial capacity.
	 * @return an SpcfHashMap&lt;K,V&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public static <L,W> SpcfHashMap<L,W> createInstance(int initialCapacity)
	{
		return SpcfFactory.getInstance().<L,W>createHashMap(initialCapacity);
	}
	
	/**
	 * Returns an empty portable hashMap with the specified initial capacity
	 * and the default load factor  (1.0 for .NET and 0.75 for Java).
	 * @param initialCapacity The initial capacity.
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @return an SpcfHashMap&lt;K,V&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public static <L,W> SpcfHashMap<L,W> createInstance(int initialCapacity, SpcfClass param1, SpcfClass param2)
	{
		return SpcfFactory.getInstance().<L,W>createHashMap(initialCapacity,param1,param2);
	}
}
