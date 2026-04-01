package com.intuit.spc.foundations.portabilitySpecific.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.collections.SpcfCollection;
import com.intuit.spc.foundations.portability.collections.SpcfMap;
import com.intuit.spc.foundations.portabilitySpecific.collections.SpcfCollectionReadOnlyImpl;

/**
 * An implementation of ReadOnly Map.
 */
public class SpcfReadOnlyMapWrapper<K,V> extends SpcfMap<K,V> implements Map<K, V>
{

	/**
	 * constant used for serialization
	 */
	private static final long serialVersionUID = -508491462525322121L;
	
	/**
	 * The encapsulated third party runtime object
	 */
	protected Map<K,V> mThirdPartyMap;
	
	/**
	 * Constructs a map with the specified third party runtime object.
	 * This constructor allows the user to encapsulate any object that
	 * implements the java.util.Map<K,V> interface, such as HashMap<K,V>, TreeMap<K,V>, and
	 * WeakHashMap<K,V>.
	 * @param   thirdPartyMap java.util.Map<K,V> implementation
	 * @throws SpcfArgumentNullException if thirdPartyHash is null
	 */
	public SpcfReadOnlyMapWrapper(Map<K,V> thirdPartyMap)
	{
		SpcfParamValidator.checkIsNotNull(thirdPartyMap, "thirdPartyMap");
		mThirdPartyMap = thirdPartyMap;
	}	

	/**
	 * add not supported
	 * @throws SpcfUnsupportedOperationException because cannot change a ReadOnly map
	 */
	@Override
	public V add(K key, V val) 
	{
		throw new SpcfUnsupportedOperationException(); 
	}

	/**
	 * addAll not supported
	 * @throws SpcfUnsupportedOperationException because cannot change a ReadOnly map
	 */
	@Override
	public void addAll(SpcfMap<K, V> map) 
	{
		throw new SpcfUnsupportedOperationException();		
	}

	/**
	 * clear not supported
	 * @throws SpcfUnsupportedOperationException because cannot change a ReadOnly map
	 */
	@Override
	public void clear()
	{
		throw new SpcfUnsupportedOperationException();		
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#containsKey(Object)
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
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#containsValue(Object)
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
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#getItem(Object)
	 */
	@Override
	public V getItem(K key) 
	{
		//must check for null key because java allows null keys but .NET does not.
		SpcfParamValidator.checkIsNotNull(key, "key");

		return mThirdPartyMap.get(key);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#getKeyList()
	 */
	@Override
	public SpcfCollection<K> getKeyList()
	{
		return new SpcfCollectionReadOnlyImpl<K>(mThirdPartyMap.keySet());
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#getValueList()
	 */
	@Override
	public SpcfCollection<V> getValueList() 
	{
		return new SpcfCollectionReadOnlyImpl<V>(mThirdPartyMap.values());
	}

	/**
	 * remove not supported
	 * @throws SpcfUnsupportedOperationException because cannot change a ReadOnly map
	 */
	@Override
	public V remove(Object key) 
	{
		throw new SpcfUnsupportedOperationException();	
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
	 * Returns the encapsulated third party runtime object
	 * @return a java.util.Map<K,V> implementation
	 */
	public Map<K,V> toSpecific()
	{
		return mThirdPartyMap;
	}

	/**
	 * Returns a collection view of the mappings contained in this map. 
	 * Each element in the returned collection is a Map.Entry. 
	 * @return a set view of the mappings contained in this map.
	 */
	public Set<Map.Entry<K,V>> entrySet()
	{
		return Collections.unmodifiableSet(mThirdPartyMap.entrySet());
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
		return Collections.unmodifiableSet(mThirdPartyMap.keySet());
	}
	
	/**
	 * put not supported
	 * @param key key to use to add to the map
	 * @param value value to add to the map
	 * @return always throws exception
	 * @throws SpcfUnsupportedOperationException because cannot change a ReadOnly map
	 */
	public V put(K key, V value)
	{
		throw new SpcfUnsupportedOperationException(); 
	}
	
	/**
	 * addAll not supported
	 * @param t a map to add to this map
	 * @throws SpcfUnsupportedOperationException because cannot change a ReadOnly map
	 */
	public void putAll(Map<? extends K, ? extends V> t)
	{
		throw new SpcfUnsupportedOperationException(); 
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
		return Collections.unmodifiableCollection(mThirdPartyMap.values());
	}
}
