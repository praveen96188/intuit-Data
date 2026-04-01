package com.intuit.spc.foundations.portabilitySpecific.collections;

import java.util.*;

import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfNullPointerException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.collections.*;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * A platform specific implementation of SpcfHashMap.
 */
public class SpcfLinkedHashMapImpl<K,V> extends SpcfLinkedHashMap<K,V> implements Cloneable, Map<K, V>
{

	/**
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 5386051542343773084L;
	
	/**
	 * The encapsulated runtime object
	 */
	protected SpcfHashMapImpl<K,V> mHashMap = null;
	
	/**
	 * Constructs an empty linked hash map with the default initial capacity (10)
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 */
	public SpcfLinkedHashMapImpl()
	{
		mHashMap = new SpcfHashMapImpl<K,V>(new LinkedHashMap<K,V>(10));
	}
	
	/**
	 * Constructs an empty linked hash map with the default initial capacity (10)
	 * and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 */
	public SpcfLinkedHashMapImpl(SpcfClass param1, SpcfClass param2)
	{
		this.mTypeParameters = new SpcfClass[] {param1,param2};
		mHashMap = new SpcfHashMapImpl<K,V>(new LinkedHashMap<K,V>(10));
	}

	
	/**
	 * Constructs an empty linked hash map with the specified initial
	 * capacity and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param  initialCapacity the initial capacity.
	 * @throws IllegalArgumentException if the initial capacity is negative.
	 */
	public SpcfLinkedHashMapImpl(int initialCapacity)
	{
		try
		{
			mHashMap = new SpcfHashMapImpl<K,V>(new LinkedHashMap<K,V>(initialCapacity));
		}
		catch(IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}
	
	/**
	 * Constructs an empty linked hash map with the specified initial
	 * capacity and the default load factor (1.0 for .NET and 0.75 for Java).
	 * @param  initialCapacity the initial capacity.
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @throws IllegalArgumentException if the initial capacity is negative.
	 */
	public SpcfLinkedHashMapImpl(int initialCapacity, SpcfClass param1, SpcfClass param2)
	{
		try
		{
			this.mTypeParameters = new SpcfClass[] {param1,param2};
			mHashMap = new SpcfHashMapImpl<K,V>(new LinkedHashMap<K,V>(initialCapacity));
		}
		catch(IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}

	
	/**
	 * Constructs a linked hash map with the specified third party runtime object.
	 * @param thirdPartyMap java.util.LinkedHashMap<K,V> implementation
	 * @throws NullPointerException if thirdPartyMap is null
	 */
	public SpcfLinkedHashMapImpl(LinkedHashMap<K,V> thirdPartyMap)
	{
		SpcfParamValidator.checkIsNotNull(thirdPartyMap, "thirdPartyMap");

		mHashMap = new SpcfHashMapImpl<K,V>(thirdPartyMap);
	}
	

	/**
	 * Returns the encapsulated third party runtime object.
	 * @return java.util.LinkedHashMap<K,V> implementation
	 */
	public LinkedHashMap<K,V> toSpecific()
	{
		return (LinkedHashMap<K,V>)mHashMap.toSpecific();
	}
	
	
	/**
	 * Adds an element to the map, preserving the ordering.
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#add
	 */
	@Override
	public V add(K key, V val)
	{
		return mHashMap.add(key, val); 
	}


	/**
	 * Adds all the elements from the given map to this one, preserving the ordering.
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#addAll
	 * (com.intuit.spc.foundations.portability.collections.SpcfMap)
	 */
	@Override
	public void addAll(SpcfMap<K, V> map)
	{
		mHashMap.addAll(map);
	}
	
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#clear()
	 */
	@Override
	public void clear()
	{
		mHashMap.clear();
	}
	
	
	/**
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public SpcfHashMap<K, V> clone()
	{
		try
		{
			return new SpcfLinkedHashMapImpl<K, V>((LinkedHashMap<K,V>)mHashMap.toSpecific().clone());
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
	}
	
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#containsKey
	 */
	@Override
	public boolean containsKey(Object key)
	{
		return mHashMap.containsKey(key);
	}
	
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#containsValue
	 */
	@Override
	public boolean containsValue(Object val)
	{
		return mHashMap.containsValue(val);
	}
	
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfLinkedHashMap#equals(java.lang.Object)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o)
	{
		// If o is null, then the answer is obviously false:
		if (o == null) return false;
		
		// o must be an SpcfLinkedHashMap:
		if (!(o instanceof SpcfLinkedHashMapImpl)) return false;
		
		// Cast object o to an SpcfLinkedHashMap: 
		SpcfLinkedHashMapImpl<K,V> slhm = (SpcfLinkedHashMapImpl<K,V>)o;

		// If we were handed a pointer to ourselves, then we are the same:
		if (this == slhm) return true;

		// The sizes must be the same:
		if (this.getSize() != slhm.getSize()) return false;
		
		// Make sure the keys are in the same order, are the same, and their associated values are the same:
		try
		{
			SpcfCollection<K> keyList1 = this.getKeyList();
			SpcfCollection<K> keyList2 = slhm.getKeyList();
			ISpcfIterator<K> iterator1 = keyList1.getIterator();
			ISpcfIterator<K> iterator2 = keyList2.getIterator();

			while(iterator1.hasNext())
			{
				// The keys must line up:
				K key1 = iterator1.next();
				K key2 = iterator2.next();
				if (!key1.equals(key2)) return false;
				
				// The values must match:
				V val1 = this.getItem(key1);
				V val2 = slhm.getItem(key2);
				if (val1 == null && val2 != null) return false;
				else if (!val1.equals(val2)) return false;
			}
		}
		catch (SpcfNoSuchElementException e)
		{
			return false;
		}
		catch (SpcfConcurrentModificationException e)
		{
			return false;
		}
		
		// If we made it here, they must be equal:
		return true;
	}
	
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#getItem
	 */
	@Override
	public V getItem(K key)
	{
		return mHashMap.getItem(key);
	}
	
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#getKeyList()
	 */
	@Override
	public SpcfCollection<K> getKeyList()
	{
		return mHashMap.getKeyList();
	}
	
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#getSize()
	 */
	@Override
	public int getSize()
	{
		return mHashMap.getSize();
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#getValueList()
	 */
	@Override
	public SpcfCollection<V> getValueList()
	{
		return mHashMap.getValueList();
	}
	
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return mHashMap.toSpecific().hashCode();
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#remove
	 */
	@Override
	public V remove(Object key)
	{
		return mHashMap.remove(key);
	}

	/**
	 * Returns a collection view of the mappings contained in this map. 
	 * Each element in the returned collection is a Map.Entry. 
	 * @return a set view of the mappings contained in this map.
	 */
	public Set<Map.Entry<K,V>> entrySet()
	{
		return mHashMap.entrySet();
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

		return mHashMap.get(key);
	}
	
	/**
	 * Returns a set view of the keys contained in this map. 
	 * @return a set view of the keys contained in this map.
	 */
	public Set<K> keySet()
	{
		return mHashMap.keySet();
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
			return mHashMap.put(key, value);
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
			mHashMap.putAll(t);
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
		return mHashMap.values();
	}}
