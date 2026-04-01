/*
 * author		LGM
 * department	SPC Foundations
 * project	    Portability
 * 2005-02-15   Initial Implementation
 *
 * NOTE: All third-party runtime exceptions are caught and re-thrown as
 * portable exceptions
 */
package com.intuit.spc.foundations.portabilitySpecific.collections;

import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfCollection;
import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.portability.collections.SpcfTreeMap;
import com.intuit.spc.foundations.portability.collections.ISpcfComparator;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfNullPointerException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.SpcfClassCastException;
import com.intuit.spc.foundations.portability.SpcfArgumentNullException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A platform specific implementation of SpcfTreeMap.
 */
public class SpcfTreeMapImpl<K,V> extends SpcfTreeMap<K,V> implements Cloneable, Map<K, V>
{

	/**
	 * constant used for serialization
	 */
	private static final long serialVersionUID = -1971341358045633178L;

	/**
	 * The encapsulated third party runtime object
	 */
	protected TreeMap<K,V> mThirdPartyTreeMap;

	//Constructors

	/**
	 * Constructs an empty treeMap.
	 */
	public SpcfTreeMapImpl()
	{
		mThirdPartyTreeMap = new TreeMap<K,V>();
	}
	
	/**
	 * Constructs an empty treeMap.
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 */
	public SpcfTreeMapImpl(SpcfClass param1, SpcfClass param2)
	{
		this.mTypeParameters = new SpcfClass[] { param1,param2};
		mThirdPartyTreeMap = new TreeMap<K,V>();
	}

	/**
	 * Constructs an empty TreeMap with the specified comparator.
	 *
	 * @param   c   the initial comparator. (A null value indicates that the keys' natural ordering should be used.)
	 */
	public SpcfTreeMapImpl(ISpcfComparator<K> c)
	{
		mThirdPartyTreeMap = new TreeMap<K,V>(c);
	}

	/**
	 * Constructs a TreeMap with the specified third party runtime object.
	 * This constructor allows the user to encapsulate any object that
	 * implements java.util.TreeMap.
	 * @param   thirdPartyTreeMap java.util.TreeMap implementation
	 * @throws SpcfArgumentNullException if thirdPartyTreeMap is null
	 */
	public SpcfTreeMapImpl(TreeMap<K,V> thirdPartyTreeMap)
	{
		SpcfParamValidator.checkIsNotNull(thirdPartyTreeMap, "thirdPartyTreeMap");
		mThirdPartyTreeMap = thirdPartyTreeMap;
	}

	/**
	 * Returns the encapsulated third party runtime object
	 * @return a java.util.TreeMap implementation
	 */
	public TreeMap<K,V> toSpecific()
	{
		return mThirdPartyTreeMap;
	}

	//SpcfTreeMap Overrides

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeMap#add
	 */
	@Override
	public V add(K key, V val)
	{
		SpcfParamValidator.checkIsNotNull(key, "key");

		try
		{
			return mThirdPartyTreeMap.put(key, val);
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
		// .NET throws SpcfIllegalArgumentException so java will also
		catch(ClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
		catch(IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
		catch(NullPointerException e)
		{
			throw new SpcfNullPointerException(e);
		}
		// we must throw the IllegalArgument instead of the ClassCast Exception
		// because if one adds string and then SpcfStringBuilder, one gets the ClassCast
		// but if one adds SpcfStringBuilder and then string, one gets ArgumentException
		// we want to be consistant on the exceptions thrown.
		catch (SpcfClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeMap#addAll
	 * (com.intuit.spc.foundations.portability.collections.SpcfMap)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void addAll(SpcfMap<K,V> map)
	{
		SpcfParamValidator.checkIsNotNull(map, "map");

		try
		{
			// make a copy of the map so we do not alter the
			// original one if we have an exception part of the way
			// through the addAll.
			SpcfTreeMapImpl<K,V> tmpTreeMap = (SpcfTreeMapImpl<K,V>) clone();

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
				tmpTreeMap.add(keyIt.next(), valueIt.next());
			}
			// now save the temporary hashMap where we added all of the
			// elements of the collection, to our internal hashMap value
			mThirdPartyTreeMap = tmpTreeMap.toSpecific();

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
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeMap#clear()
	 */
	@Override
	public void clear()
	{
		try
		{
			mThirdPartyTreeMap.clear();
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeMap#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public SpcfTreeMap<K,V> clone()
	{
		TreeMap<K,V> thirdPartyTreeMap;

		thirdPartyTreeMap = (TreeMap<K,V>) mThirdPartyTreeMap.clone();

		//Encapsulate the copy with a portable object implementation
		return new SpcfTreeMapImpl<K,V>(thirdPartyTreeMap);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeMap#containsKey
	 */
	@Override
	public boolean containsKey(Object key)
	{
		try
		{
			SpcfParamValidator.checkIsNotNull(key, "key");
			return mThirdPartyTreeMap.containsKey(key);
		}
		catch(ClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
		catch(SpcfClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
		catch(NullPointerException e)
		{
			throw new SpcfNullPointerException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeMap#containsValue
	 */
	@Override
	public boolean containsValue(Object val)
	{
		try
		{
			return mThirdPartyTreeMap.containsValue(val);
		}
		catch(ClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
		catch(NullPointerException e)
		{
			throw new SpcfNullPointerException(e);
		}
		catch(SpcfClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeMap#getItem
	 */
	@Override
	public V getItem(K key)
	{
		try
		{
			SpcfParamValidator.checkIsNotNull(key, "key");
			return mThirdPartyTreeMap.get(key);
		}
		catch(ClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
		catch(NullPointerException e)
		{
			throw new SpcfNullPointerException(e);
		}
		catch(SpcfClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeMap#remove
	 */
	@Override
	public V remove(Object key)
	{
		try
		{
			SpcfParamValidator.checkIsNotNull(key, "key");
			return mThirdPartyTreeMap.remove(key);
		}
		catch(UnsupportedOperationException e)
		{
			//Map is read-only
			throw new SpcfUnsupportedOperationException(e);
		}
		catch(ClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
		catch(NullPointerException e)
		{
			throw new SpcfNullPointerException(e);
		}
		catch(SpcfClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeMap#getSize()
	 */
	@Override
	public int getSize()
	{
		return mThirdPartyTreeMap.size();
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeMap#firstKey()
	 */
	@Override
	public K firstKey()
	{
		return mThirdPartyTreeMap.firstKey();
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeMap#lastKey()
	 */
	@Override
	public K lastKey()
	{
		return mThirdPartyTreeMap.lastKey();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeMap#comparator()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ISpcfComparator<K> comparator()
	{
		return (ISpcfComparator<K>)mThirdPartyTreeMap.comparator();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeMap#getKeyList()
	 */
	@Override
	public SpcfCollection<K> getKeyList()
	{
		return new SpcfCollectionReadOnlyImpl<K>(mThirdPartyTreeMap.keySet());
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeMap#getValueList()
	 */
	@Override
	public SpcfCollection<V> getValueList()
	{
		return new SpcfCollectionReadOnlyImpl<V>(mThirdPartyTreeMap.values());
	}
	
	/**
	 * Returns a collection view of the mappings contained in this map. 
	 * Each element in the returned collection is a Map.Entry. 
	 * @return a set view of the mappings contained in this map.
	 */
	public Set<Map.Entry<K,V>> entrySet()
	{
		return mThirdPartyTreeMap.entrySet();
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

		return mThirdPartyTreeMap.get(key);
	}
	
	/**
	 * Returns a set view of the keys contained in this map. 
	 * @return a set view of the keys contained in this map.
	 */
	public Set<K> keySet()
	{
		return mThirdPartyTreeMap.keySet();
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
			return mThirdPartyTreeMap.put(key, value);
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
			mThirdPartyTreeMap.putAll(t);
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
		return mThirdPartyTreeMap.values();
	}
}
