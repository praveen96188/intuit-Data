package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * A linked hash map is a hash map whose enumerations preserve the element ordering 
 * based on insertion order.
 * Hash maps, including linked hash maps, map K keys to V values.  
 * A map cannot contain duplicate keys; and each key can map to at most one value.
 * A map can contain duplicate values.
 * The linked hash map is not sorted, but it does preserve the ordering of insertion.
 * The map can contain null values but not null keys. This differs from java's LinkedHashMap&lt;K,V&gt; 
 * implementation in that java's version allows null keys.
 * The map instance created within the SpcfLinkedHashMap&lt;K,V&gt; is the java LinkedHashMap&lt;K,V&gt; class,
 * but there is no analog in the C# .Net library.
 * @see com.intuit.spc.foundations.portability.collections.SpcfHashMap
 */

public abstract class SpcfLinkedHashMap<K,V> extends SpcfHashMap<K,V>
{
	private static final long serialVersionUID = -7250906152670679611L;

	/**
	 * The equality check for SpcfLinkedHashMap&lt;K,V&gt; is necessarilly different than that of the
	 * base SpcfMap implementation because the ordering of the elements in a linked hash map is 
	 * important. Therefore: (1) For two LinkedHashMaps to be equal, not only must they contain the 
	 * same key-value pairs, they must also be in the same exact order; and (2) We must restrict the
	 * object parameter to being an SpcfLinkedHashMap because the ordering of elements in regular 
	 * SpcfMaps are indeterminate and mutable, which would make repeated calls to this method return
	 * different boolean results depending on what order the underlying SpcfMap decided to return in
	 * the moment.
	 * @param o Object to be compared for equality with this SpcfMap.
	 * @return true if objects are the same; false otherwise. Returns false if o cast to SpcfLinkedHashMap fails.
	 */
	@Override
	public abstract boolean equals(Object o);
	
	/**
	 * Returns the hash code for the current SpcfList class.  If objects are 
	 * determined to be equal according to the equals method, then the hash code
	 * should be identical for equal objects.  Objects having the same hash code
	 * are not guaranteed to be equal however.
	 * @return The int hash code for the current object.
	 */
	@Override
	public abstract int hashCode();
	
	/**
	 * Constructs an empty linked hashMap with the default initial capacity (10)
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @return an SpcfLinkedHashMap&lt;K,V&gt; object
	 */
	public static <L,W> SpcfHashMap<L,W> createInstance()
	{
		return SpcfFactory.getInstance().<L,W>createLinkedHashMap();
	}
	
	/**
	 * Constructs an empty linked hashMap with the default initial capacity (10)
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @return an SpcfLinkedHashMap&lt;K,V&gt; object
	 */
	public static <L,W> SpcfHashMap<L,W> createInstance(SpcfClass param1, SpcfClass param2)
	{
		return SpcfFactory.getInstance().<L,W>createLinkedHashMap(param1, param2);
	}

	/**
	 * Returns an empty linked hashmap with the specified initial capacity
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param initialCapacity The initial capacity.
	 * @return an SpcfLinkedHashMap&lt;K,V&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public static <L,W> SpcfHashMap<L,W> createInstance(int initialCapacity)
	{
		return SpcfFactory.getInstance().<L,W>createLinkedHashMap(initialCapacity);
	}
	
	/**
	 * Returns an empty linked hashmap with the specified initial capacity
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param initialCapacity The initial capacity.
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @return an SpcfLinkedHashMap&lt;K,V&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public static <L,W> SpcfHashMap<L,W> createInstance(int initialCapacity, SpcfClass param1, SpcfClass param2)
	{
		return SpcfFactory.getInstance().<L,W>createLinkedHashMap(initialCapacity, param1, param2);
	}
	
	/**
	 * Constructs an empty linked hashMap with the default initial capacity (10)
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @return an SpcfLinkedHashMap&lt;K,V&gt; object
	 */
	public static <L,W> SpcfLinkedHashMap<L,W> createInstanceLinked()
	{
		return SpcfFactory.getInstance().<L,W>createLinkedHashMap();
	}
	
	/**
	 * Constructs an empty linked hashMap with the default initial capacity (10)
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @return an SpcfLinkedHashMap&lt;K,V&gt; object
	 */
	public static <L,W> SpcfLinkedHashMap<L,W> createInstanceLinked(SpcfClass param1, SpcfClass param2)
	{
		return SpcfFactory.getInstance().<L,W>createLinkedHashMap(param1, param2);
	}

	/**
	 * Returns an empty linked hashmap with the specified initial capacity
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param initialCapacity The initial capacity.
	 * @return an SpcfLinkedHashMap&lt;K,V&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public static <L,W> SpcfLinkedHashMap<L,W> createInstanceLinked(int initialCapacity)
	{
		return SpcfFactory.getInstance().<L,W>createLinkedHashMap(initialCapacity);
	}
	
	/**
	 * Returns an empty linked hashmap with the specified initial capacity
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param initialCapacity The initial capacity.
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @return an SpcfLinkedHashMap&lt;K,V&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public static <L,W> SpcfLinkedHashMap<L,W> createInstanceLinked(int initialCapacity, SpcfClass param1, SpcfClass param2)
	{
		return SpcfFactory.getInstance().<L,W>createLinkedHashMap(initialCapacity, param1, param2);
	}
}
