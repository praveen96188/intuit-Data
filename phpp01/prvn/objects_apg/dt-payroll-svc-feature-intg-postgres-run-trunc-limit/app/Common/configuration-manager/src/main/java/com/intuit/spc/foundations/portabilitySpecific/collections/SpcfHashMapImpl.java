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

import com.intuit.spc.foundations.portability.collections.*;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;
import com.intuit.spc.foundations.portability.*;

import java.util.*;


/**
 * A platform specific implementation of SpcfHashMap.
 *
 * <p>An instance of SpcfHashMapImpl has two parameters that affect its
 * performance: <i>initial capacity</i> and <i>load factor</i>.  The
 * <i>capacity</i> is the number of buckets in the hash table, and the initial
 * capacity is simply the capacity at the time the hash table is created.  The
 * <i>load factor</i> is a measure of how full the hash table is allowed to
 * get before its capacity is automatically increased.  When the number of
 * entries in the hash table exceeds the product of the load factor and the
 * current capacity, the capacity is roughly doubled by calling the
 * rehash method.
 *
 * <p>As a general rule, the default load factor (.75) offers a good tradeoff
 * between time and space costs.  Higher values decrease the space overhead
 * but increase the lookup cost (reflected in most of the operations of the
 * HashMap class, including get and put).  The
 * expected number of entries in the map and its load factor should be taken
 * into account when setting its initial capacity, so as to minimize the
 * number of rehash operations.  If the initial capacity is greater
 * than the maximum number of entries divided by the load factor, no
 * rehash operations will ever occur.
 *
 */
public class SpcfHashMapImpl<K,V> extends SpcfHashMap<K,V> implements Cloneable, Map<K, V>
{

	/**
	 * constant used for serialization
	 */
	private static final long serialVersionUID = -6528154904346138074L;
	
	//Member Variables

	/**
	 * The encapsulated third party runtime object
	 */
	protected HashMap<K,V> mThirdPartyMap;

	//Constructors

	/**
	 * Constructs an empty hashMap with the default initial capacity (10)
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 */
	public SpcfHashMapImpl()
	{
		mThirdPartyMap = new HashMap<K,V>(10);
	}
	
	/**
	 * Constructs an empty hashMap with the default initial capacity (10)
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 */
	public SpcfHashMapImpl(SpcfClass param1, SpcfClass param2)
	{
		this.mTypeParameters = new SpcfClass[] {param1, param2};
		mThirdPartyMap = new HashMap<K,V>(10);
	}

	/**
	 * Constructs an empty hashMap with the specified initial
	 * capacity and the default load factor (1.0 for .NET and 0.75 for Java).
	 *
	 * @param  initialCapacity the initial capacity.
	 * @throws IllegalArgumentException if the initial capacity is negative.
	 */
	public SpcfHashMapImpl(int initialCapacity)
	{
		try
		{
			mThirdPartyMap = new HashMap<K,V>(initialCapacity);
		}
		catch(IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}

	}
	
	/**
	 * Constructs an empty hashMap with the specified initial
	 * capacity and the default load factor (1.0 for .NET and 0.75 for Java).
	 *
	 * @param  initialCapacity the initial capacity.
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @throws IllegalArgumentException if the initial capacity is negative.
	 */
	public SpcfHashMapImpl(int initialCapacity, SpcfClass param1, SpcfClass param2)
	{
		try
		{
			this.mTypeParameters = new SpcfClass[] {param1,param2};
			mThirdPartyMap = new HashMap<K,V>(initialCapacity);
		}
		catch(IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}

	}

	/**
	 * Constructs a hashMap with the specified third party runtime object.
	 * This constructor allows the user to encapsulate any object that
	 * implements the java.util.Map interface, such as HashMap, TreeMap, and
	 * WeakHashMap.
	 * @param   thirdPartyMap java.util.Map implementation
	 * @throws NullPointerException if thirdPartyMap is null
	 */
	public SpcfHashMapImpl(HashMap<K,V> thirdPartyMap)
	{
		SpcfParamValidator.checkIsNotNull(thirdPartyMap, "thirdPartyMap");

		mThirdPartyMap = thirdPartyMap;
	}

	/**
	 * Returns the encapsulated third party runtime object
	 * @return a java.util.HashMap implementation
	 */
	public HashMap<K,V> toSpecific()
	{
		return /*(HashMap<K,V>)*/mThirdPartyMap;
	}

	
	//SpcfHashMap Overrides

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#add
	 */
	@Override
	public V add(K key, V val)
	{
		// must check for null key because java allows null keys but .NET does not.
		SpcfParamValidator.checkIsNotNull(key, "key");

		try
		{
			return mThirdPartyMap.put(key, val);
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
		catch(NullPointerException e)
		{
			throw new SpcfNullPointerException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#addAll
	 * (com.intuit.spc.foundations.portability.collections.SpcfMap)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void addAll(SpcfMap<K,V> map)
	{
		SpcfParamValidator.checkIsNotNull(map, "map");

		try
		{
			// make a copy of the map so we do not alter the
			// original one if we have an exception part of the way
			// through the addAll
			SpcfHashMapImpl<K,V> tmpHashMap = (SpcfHashMapImpl<K,V>) clone();

			SpcfCollection<K> keyList = map.getKeyList();
			SpcfCollection<V> valueList = map.getValueList();
			SpcfParamValidator.checkIsNotNull(keyList, "keyList");
			SpcfParamValidator.checkIsNotNull(valueList, "valueList");

			ISpcfIterator<K> keyIt = keyList.getIterator();
			ISpcfIterator<V> valueIt = valueList.getIterator();
			SpcfParamValidator.checkIsNotNull(keyIt, "keyIt");
			SpcfParamValidator.checkIsNotNull(valueIt, "valueIt");

			//Use third-party iterator to add all of the elements of one
			//collection into the other
			while (keyIt.hasNext() && valueIt.hasNext())
			{
				tmpHashMap.add(keyIt.next(), valueIt.next());
			}
			// now save the temporary hashMap where we added all of the
			// elements of the collection, to our internal hashMap value
			mThirdPartyMap = tmpHashMap.toSpecific();

		}
		catch(UnsupportedOperationException e)
		{
			//This collection does not support the addAll method.
			throw new SpcfUnsupportedOperationException(e);
		}
		catch(NullPointerException e)
		{
	        //If the specified collection contains one or more null elements
			//and this collection does not support null elements, or if the
			//specified collection is null.
			throw new SpcfNullPointerException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#clear()
	 */
	@Override
	public void clear()
	{
		try
		{
			mThirdPartyMap.clear();
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfHashMap#clone
	 */
	@SuppressWarnings("unchecked")
	@Override
	public SpcfHashMap<K,V> clone()
	{

		try
		{
			return new SpcfHashMapImpl<K,V>((HashMap<K,V>) mThirdPartyMap.clone());
		}
		// this is the exception that the documentation says is thrown
		// but the compiler says that it is not thrown
		//catch(CloneNotSupportedException e)
		//{
			// HashMap instance we are encapsulating is not clonable
		//	throw new SpcfUnsupportedOperationException(e);
		//}
		// this appears to be the exception that is really thrown
		catch(UnsupportedOperationException e)
		{
			//putAll not supported
			throw new SpcfUnsupportedOperationException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#containsKey
	 */
//	@Override
//	public boolean containsKey(K key)
//	{
//		// must check for null key because java allows null keys but .NET does not.
//		SpcfParamValidator.checkIsNotNull(key, "key");
//
//		try
//		{
//			return mThirdPartyMap.containsKey(key);
//		}
//		catch(ClassCastException e)
//		{
//			throw new SpcfIllegalArgumentException(e);
//		}
//	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#containsKey
	 */
	@Override
	public boolean containsKey(Object key)
	{
		// must check for null key because java allows null keys but .NET does not.
		SpcfParamValidator.checkIsNotNull(key, "key");

		try
		{
			return mThirdPartyMap.containsKey(key);
		}
		catch(ClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#containsValue
	 */
//	@Override
//	public boolean containsValue(V val)
//	{
//		try
//		{
//			return mThirdPartyMap.containsValue(val);
//		}
//		catch(ClassCastException e)
//		{
//			throw new SpcfIllegalArgumentException(e);
//		}
//	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#containsValue
	 */
	@Override
	public boolean containsValue(Object val)
	{
		try
		{
			return mThirdPartyMap.containsValue(val);
		}
		catch(ClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#getItem
	 */
	@Override
	public V getItem(K key)
	{
		// must check for null key because java allows null keys but .NET does not.
		SpcfParamValidator.checkIsNotNull(key, "key");

		return mThirdPartyMap.get(key);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#remove
	 */
//	@Override
//	public V remove(K key)
//	{
//		// must check for null key because java allows null keys but .NET does not.
//		SpcfParamValidator.checkIsNotNull(key, "key");
//
//		try
//		{
//			return mThirdPartyMap.remove(key);
//		}
//		catch(UnsupportedOperationException e)
//		{
//			//Map is read-only
//			throw new SpcfUnsupportedOperationException(e);
//		}
//	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#remove
	 */
	@Override
	public V remove(Object key)
	{
		// must check for null key because java allows null keys but .NET does not.
		SpcfParamValidator.checkIsNotNull(key, "key");

		try
		{
			return mThirdPartyMap.remove(key);
		}
		catch(UnsupportedOperationException e)
		{
			//Map is read-only
			throw new SpcfUnsupportedOperationException(e);
		}
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#getSize()
	 */
	@Override
	public int getSize()
	{
		return mThirdPartyMap.size();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#getKeyList()
	 */
	@Override
	public SpcfCollection<K> getKeyList()
	{
		return new SpcfCollectionReadOnlyImpl<K>(/*(Collection<K>)*/mThirdPartyMap.keySet());

	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#getValueList()
	 */
	@Override
	public SpcfCollection<V> getValueList()
	{
		return new SpcfCollectionReadOnlyImpl<V>(/*(Collection<V>)*/mThirdPartyMap.values());
	}

	/**
	 * Returns a collection view of the mappings contained in this map. 
	 * Each element in the returned collection is a Map.Entry. 
	 * @return a set view of the mappings contained in this map.
	 */
	public Set<Map.Entry<K,V>> entrySet()
	{
		return mThirdPartyMap.entrySet();
	}
	
	/**
	 * Returns the value to which the specified key is mapped in this identity hash map, 
	 * or null if the map contains no mapping for this key. 
	 * @param key - key whose associated value is to be returned.
	 * @return the value to which this map maps the specified key, 
	 * or null if the map contains no mapping for this key.
	 */
	public V get(Object key)
	{
		// must check for null key because java allows null keys but .NET does not.
		SpcfParamValidator.checkIsNotNull(key, "key");

		return mThirdPartyMap.get(key);
	}
	
	/**
	 * Returns a set view of the keys contained in this map. 
	 * @return a set view of the keys contained in this map.
	 */
	public Set<K> keySet()
	{
		return mThirdPartyMap.keySet();
	}
	
	/**
	 * Associates the specified value with the specified key in this map. 
	 * If the map previously contained a mapping for this key, the old value is replaced.
	 * @param key - key with which the specified value is to be associated.
	 * @param value - value to be associated with the specified key.
	 * @return previous value associated with specified key, 
	 * or null  if there was no mapping for key
	 */
	public V put(K key, V value)
	{
		// must check for null key because java allows null keys but .NET does not.
		SpcfParamValidator.checkIsNotNull(key, "key");

		try
		{
			return mThirdPartyMap.put(key, value);
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
		catch(NullPointerException e)
		{
			throw new SpcfNullPointerException(e);
		}
	}
	
	/**
	 * Copies all of the mappings from the specified map to this map. 
	 * These mappings will replace any mappings that this map had for 
	 * any of the keys currently in the specified map.
	 * @param t a map to add to this map
	 */
	public void putAll(Map<? extends K, ? extends V> t)
	{
		SpcfParamValidator.checkIsNotNull(t, "t");

		try
		{
			mThirdPartyMap.putAll(t);
		}
		catch(UnsupportedOperationException e)
		{
			//This collection does not support the addAll method.
			throw new SpcfUnsupportedOperationException(e);
		}
		catch(NullPointerException e)
		{
	        //If the specified collection contains one or more null elements
			//and this collection does not support null elements, or if the
			//specified collection is null.
			throw new SpcfNullPointerException(e);
		}

	}
	
	/**
	 * @return integer size of this map
	 */
	public int size()
	{
		return getSize();
	}
	
	/**
	 * Returns a collection view of the values contained in this map. 
	 * @return java collection of values of the map
	 */
	public Collection<V> values()
	{
		return mThirdPartyMap.values();
	}
}
