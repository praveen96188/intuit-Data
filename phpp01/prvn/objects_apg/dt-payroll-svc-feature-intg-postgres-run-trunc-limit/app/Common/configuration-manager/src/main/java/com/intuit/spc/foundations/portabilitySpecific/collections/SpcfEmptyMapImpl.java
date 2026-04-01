package com.intuit.spc.foundations.portabilitySpecific.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.collections.SpcfEmptyMap;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * A platform specific implementation of SpcfEmptyMap
 * @param <K> the type for the Key
 * @param <V> the type for the Value
 */
public class SpcfEmptyMapImpl<K,V> extends SpcfEmptyMap<K,V> implements Map<K, V>
{

	/**
	 * constant used for serialization
	 */
	private static final long serialVersionUID = 3604505835817375L;
	
	/**
	 * The encapsulated third party runtime object
	 */
	@SuppressWarnings("unchecked")
	static SpcfEmptyMapImpl sEmptyMap;
    
    static 
    {
    	sEmptyMap = new SpcfEmptyMapImpl();
    }
	
    /**
     * Constructs the empty map
     *
     */
	public SpcfEmptyMapImpl()
	{
		// This is intentially left blank
	}
	
	/**
	 * Constructs the empty map
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 */
	public SpcfEmptyMapImpl(SpcfClass param1, SpcfClass param2)
	{
		this.mTypeParameters = new SpcfClass[] {param1,param2};
	}
	
	/**
	 * returns the instance of the empty map
	 * @param <K> the type for the Key
	 * @param <V> the type for the Value
	 * @return an instance of the empty map
	 */
    @SuppressWarnings("unchecked")
	public static <K,V> SpcfEmptyMap getInstance()
    {
    	return sEmptyMap;
    }

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#containsKey(Object)
	 */
	@Override
	public boolean containsKey(Object key) 
	{
		// must check for null key because java allows null keys but .NET does not.
		SpcfParamValidator.checkIsNotNull(key, "key");

		return false;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfMap#containsValue(Object)
	 */
	@Override
	public boolean containsValue(Object val)
	{
		return false;
	}

	/**
	 * since this map is empty, will always return null
	 * @returns null
	 */
	@Override
	public V remove(Object key) 
	{
		return null;	
	}

	/**
	 * Returns a unmodifiable collection view of the mappings contained in this map. 
	 * Each element in the returned collection is a Map.Entry. 
	 * @return a set view of the mappings contained in this map.
	 */
	@SuppressWarnings("unchecked")
	public Set<Map.Entry<K,V>> entrySet()
	{
		return Collections.unmodifiableSet((Set<Map.Entry<K,V>>)sEmptyMap.entrySet());
	}
	
	/**
	 * Returns the value to which the specified key is mapped in this identity hash map, 
	 * or null if the map contains no mapping for this key. 
	 * @param key - key whose associated value is to be returned.
	 * @return null because an empty map contains no mapping for this key.
	 */
	public V get(Object key)
	{
		// must check for null key because java allows null keys but .NET does not.
		SpcfParamValidator.checkIsNotNull(key, "key");

		return null;
	}
	
	/**
	 * Returns a unmodifiable set view of the keys contained in this map. 
	 * @return a set view of the keys contained in this map.
	 */
	@SuppressWarnings("unchecked")
	public Set<K> keySet()
	{
		return Collections.unmodifiableSet((Set<K>)sEmptyMap.keySet());
	}
	
	/**
	 * put does nothing to keep the map empty
	 * @param key key to use to add to the map
	 * @param value value to add to the map
	 * @return null since there was no mapping for key
	 */
	public V put(K key, V value)
	{
		return null; 
	}
	
	/**
	 * does nothing
	 * @param t a map to add to this map
	 */
	public void putAll(Map<? extends K, ? extends V> t)
	{
		// does nothing 
	}
	
	/**
	 * @return integer size of this map
	 */
	public int size()
	{
		return 0;
	}
	
	/**
	 * Returns an unmodifiable collection view of the values contained in this map. 
	 * @return java collection of values of the map
	 */
	@SuppressWarnings("unchecked")
	public Collection<V> values()
	{
		return Collections.unmodifiableCollection((Collection<V>)sEmptyMap.values());
	}
}
