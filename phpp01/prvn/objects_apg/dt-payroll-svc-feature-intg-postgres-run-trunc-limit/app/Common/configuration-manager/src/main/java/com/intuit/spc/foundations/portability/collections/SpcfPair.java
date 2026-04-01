package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.SpcfClassCastException;

/**
 * Contains a key/value pairing for an SpcfMap iterator entry.
 */
public class SpcfPair<K,V> implements Comparable {

	K mKey = null;
	V mValue = null;
	
	/**
	 * Creates a new instance of an SpcfPair
	 * @param key The key of the pair
	 * @param val The value for the pair
	 */
	public SpcfPair(K key, V val) 
	{
		mKey = key;
		mValue = val;
	}
	
	/**
	 * A default constructor
	 */
	public SpcfPair() {}

	/**
	 * Retrieves the key item for the pairing.
	 * @return The key item.
	 */
	public K getKeyItem()
	{
		return mKey;
	}
	
	/**
	 * Sets the key item for the pairing.
	 * @param key The key item.
	 */
	public void setKeyItem(K key)
	{
	    mKey = key;
	}
	
	/**
	 * Retrieves the value item for the pairing.
	 * @return The value item.
	 */
	public V getValueItem()
	{
		return mValue;
	}
	
	/**
	 * Sets the value item for the pairing.
	 * @param val The value item.
	 */
	public void setValueItem(V val)
	{
		mValue = val;
	}

	/**
	 * Compares the current SpcfPair to the received object.
	 * @param o The object to compare against.
	 * @return 0 if the 2 items are equal, or an int indicating relative ordering.
	 * @throws SpcfClassCastException If the object is not an SpcfPair of the appropriate type.
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(Object o)
	{
		SpcfPair<K,V> c = null;
		
		try
		{
			c = (SpcfPair<K,V>)o;
		} 
		catch (ClassCastException e)
		{
			throw new SpcfClassCastException(e);
		}
		
		if (mKey.equals(c.getKeyItem()))
		{
			return 0;
		} 
		else
		{
			return (mKey.hashCode() - c.getKeyItem().hashCode());
		}
	}
	
	/**
	 * Compares the SpcfPair for equality with the received object.  Equality
	 * is established by having the key objects indicate equality.
	 * @param o The object to compare for equality.
	 * @return True if the objects are equal
	 */
	@SuppressWarnings("unchecked")
	public boolean equals(Object o)
	{
		try
		{
		    SpcfPair<K,V> c = (SpcfPair<K,V>)o;
		
		    return (mKey.equals(c.getKeyItem()));
		}
		catch (ClassCastException e)
		{
			e.toString();
			return false;
		}
	}
	
	/**
	 * Returns the hash code for the current SpcfPair class.  If objects are 
	 * determined to be equal according to the equals method, then the hash code
	 * should be identical for equal objects.  Objects having the same hash code
	 * are not guaranteed to be equal however.
	 * @return The int hash code for the current object.
	 */
	public int hashCode()
	{
		return mKey.hashCode();
	}
}
