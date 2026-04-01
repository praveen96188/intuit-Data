package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 *	The immutable empty map.
 */

public abstract class SpcfEmptyMap<K,V> extends SpcfMap<K,V> 
{
	private static final long serialVersionUID = -5449860493411905690L;

	/**
	 * Overrides the default implementation to check if 
	 * Object is a derivation of SpcfSet and of size == 0.
	 * If it is, then we assume we are equal.
	 */
    public boolean equals(Object o) 
    {
        return ((o instanceof ISpcfMap) && (((ISpcfMap)o).getSize()== 0));
    }

    /**
     * Override default. Our value semantics are constant, so we use a 0 hashcode.
     */
    public int hashCode()
    {
    	return 0;
    }  
    
    /**
     * Does nothing.
     * @param key Ignored.
     * @param val Ignored.
     * @return Always null.
     */
	@Override
	public V add(K key, V val) 
	{
		return null;
	}

	/**
	 * Does nothing
	 * @param map Ignored
	 */
	@Override
	public void addAll(SpcfMap<K,V> map) 
	{
		// Do nothing
	}

	/**
	 * Does nothing.
	 */
	@Override
	public void clear() 
	{
		// Do nothing
	}

	/**
	 * Does nothing
	 * @param key Ignored.
	 * @return Always false
	 */
	@Override
	public boolean containsKey(K key) 
	{
		return false;
	}

	/**
	 * Does nothing
	 * @param val Ignored.
	 * @return Always false
	 */
	@Override
	public boolean containsValue(V val) 
	{
		return false;
	}

	/**
	 * Does nothing
	 * @param key Ignored
	 * @return Always null
	 */
	@Override
	public V getItem(K key) 
	{
		return null;
	}

	/**
	 * Returns an empty collection
	 * @return An empty collection
	 */
	@Override
	public SpcfCollection<K> getKeyList() 
	{
		return SpcfFactory.getInstance().<K>createArrayList();
	}

	/**
	 * Returns an empty collection
	 * @return An empty collection
	 */
	@Override
	public SpcfCollection<V> getValueList() 
	{
		return SpcfFactory.getInstance().<V>createArrayList();
	}

	/**
	 * Does nothing
	 * @param key Ignored
	 * @return Always null
	 */
	@Override
	public V remove(K key) 
	{
		return null;
	}

	/**
	 * This empty set always is size of zero.
	 * @return Always returns 0.
	 */	
	@Override
	public int getSize() 
	{
		return 0;
	}
	
	/**
	 * Constructs an immutable empty map.
	 * @return New instance of an immutable empty map.
	 */
	public static <L,W> SpcfMap<L,W> createInstance()
	{
		return SpcfFactory.getInstance().<L,W>createEmptyMap();
	}
	
	/**
	 * Constructs an immutable empty map.
	 * @param param1 the SpcfClass representation of the first type parameter
	 * @param param2 the SpcfClass representation of the second type parameter
	 * @return New instance of an immutable empty map.
	 */
	public static <L,W> SpcfMap<L,W> createInstance(SpcfClass param1, SpcfClass param2)
	{
		return SpcfFactory.getInstance().<L,W>createEmptyMap(param1,param2);
	}

}
