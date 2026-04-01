/*
 * author		LGM
 * department	SPC Foundations
 * project	    Portability
 * 2005-02-16   Initial Implementation
 */
package com.intuit.spc.foundations.portability.collections;

import java.io.Serializable;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfStringBuilder;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * An abstract class from which portable map collections are derived.
 */
public abstract class SpcfMap<K, V> extends SpcfMapIterable<K, V> implements Serializable, ISpcfParameterizedType
{
	private static final long serialVersionUID = 2238419012339179338L;
	
	/**
	 * Array of SpcfClass objects corresponding to the type parameters used for this type.
	 */
	protected SpcfClass[] mTypeParameters;

		
	/**
	 * Gets the array of SpcfClass objects representing the type parameter or parameters corresponding
	 * to the type parameter(s) for this generic type.  Clients should not call this method directly but
	 * should instead use an overload of the createinstance  method  or constructor that specifies
	 * parameters of SpcfClass type.
	 * @see com.intuit.spc.foundations.portability.collections.ISpcfParameterizedType
	 * @return array of SpcfClass with each element corresponding to a specific type parameter
	 */
	public SpcfClass[] getTypeParams()
	{
		return mTypeParameters;
		
	}
	
	/**
	 * Sets the array of SpcfClass objects representing the type parameter(s) corresponding to the type
	 * parameters(s) for this generic type.  Clients should not call this method.
	 * @param typeParams array of SpcfClass with each element corresponding to a specific type parameter
	 */
	public void setTypeParams(SpcfClass[] typeParams)
	{
		mTypeParameters = typeParams;
	}

	/**
	 * Associates the specified value with the specified key in this map.
	 * If the map previously contained a mapping for this key,
	 * the old value is replaced.
	 *
	 * @param key key of type K with which the specified value is to be associated.
	 * @param val of type V to be associated with the specified key
	 * @return previous value associated with specified key,
	 * or null if there was no mapping for key.
	 * A null return can also indicate that the map previously associated
	 * null with the specified key.
     * @throws SpcfUnsupportedOperationException add is not supported by
     *         this collection.
     * @throws SpcfArgumentNullException if this map does not permit null keys or values,
     * 		   and the specified key or value is null.
     * @throws SpcfIllegalArgumentException some aspect of this element prevents
     *         it from being added to this collection (optional).
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
	 * object returned by the iterator to this collection, in turn.
	 *
	 * Note that this implementation will throw an UnsupportedOperationException unless
	 * add is overridden (assuming the specified collection is non-empty).
	 *
	 * @param map map whose elements are to be added to this map.
     * @throws SpcfUnsupportedOperationException if this collection does not
     *         support the addAll method.
     * @throws SpcfArgumentNullException if the specified collection contains one
     *         or more null key or value and this collection does not support null
     *         key or value, or if the specified collection is null.
     * @throws SpcfIllegalArgumentException some aspect of a key or value in the
     * 		   specified map prevents it from being stored in this map (optional).
     * @throws SpcfConcurrentModificationException if the key or value list of the
     *         specified map has changed during our iteration
    *          to add all of the elements to the internal map.

	 */
	public abstract void addAll(SpcfMap<K,V> map);

	/**
	 * Removes all of the elements from this collection.  The collection will
	 * be empty after this call returns.
     * @throws SpcfUnsupportedOperationException if this collection does not
     *         support the clear method.
	 */

	public abstract void clear();

	/**
	  * Returns true if this map contains a mapping for the specified
	  * key.
	  *
	  * @param key key of type K whose presence in this map is to be tested.
	  * @return true if this map contains a mapping for the specified
	  *         key.
	  *
	  * @throws SpcfIllegalArgumentException if the key is of an inappropriate type for
	  * 		  this map (optional).
	  * @throws SpcfArgumentNullException if the key is null and this map
	  *            does not not permit null keys (optional).
	  */
	public abstract boolean containsKey(K key);

	/**
	  * Returns true if this map contains a mapping for the specified
	  * key.
	  *
	  * @param key key of type object whose presence in this map is to be tested.
	  * @return true if this map contains a mapping for the specified
	  *         key.
	  *
	  * @throws SpcfIllegalArgumentException if the key is of an inappropriate type for
	  * 		  this map (optional).
	  * @throws SpcfArgumentNullException if the key is null and this map
	  *            does not not permit null keys (optional).
	  */
	@SuppressWarnings("unchecked")
	public boolean containsKeyObject(Object key)
	{
		try
		{
		    return containsKey((K)key);
		} catch (ClassCastException e) {
			e.toString();
			return false;
		}
	}
	
	/**
	 * Returns true if this map maps one or more keys to the
	 * specified value.
	 *
	 * @param val value of type V whose presence in this map is to be tested.
	 * @return true if this map maps one or more keys to the
	 *         specified value.
	 * @throws SpcfIllegalArgumentException if the value is of an inappropriate type for
	 * 		  this map (optional).
	 * @throws SpcfArgumentNullException if the value is null and this map
	 *            does not not permit null values (optional).
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
	 * @param key key whose associated value is to be returned.
	 * @return the value to which this map maps the specified key, or
	 *	       null if the map contains no mapping for this key.
	 *
	 * @throws SpcfIllegalArgumentException if the key is of an inappropriate type for
	 * 		  this map (optional).
	 * @throws SpcfArgumentNullException if key is null and if this map does not
	 *		  not permit null keys (optional).
	 *
	 */
	public abstract V getItem(K key);

	/**
	 * Returns the value to which this map maps the specified key.  Returns
	 * null if the map contains no mapping for this key.  A return
	 * value of null does not <i>necessarily</i> indicate that the
	 * map contains no mapping for the key; it's also possible that the map
	 * explicitly maps the key to null.  The containsKey
	 * operation may be used to distinguish these two cases.
	 *
	 * @param key key whose associated value is to be returned.
	 * @return the value to which this map maps the specified key, or
	 *	       null if the map contains no mapping for this key.
	 *
	 * @throws SpcfIllegalArgumentException if the key is of an inappropriate type for
	 * 		  this map (optional).
	 * @throws SpcfArgumentNullException if key is null and if this map does not
	 *		  not permit null keys (optional).
	 *
	 */
	@SuppressWarnings("unchecked")
	public Object getItemAsObject(Object key)
	{
		try
		{
		    return getItem((K)key);
		} catch (ClassCastException e) {
			e.toString();
			return null;
		}
	}
	
	/**
	 * Returns a read-only collection view of the keys contained in this map.
	 * This collection is updated when the map changes.
	 *
	 * @return a collection view of the keys contained in this map.
	 */
	public abstract SpcfCollection<K> getKeyList();

	/**
	 * Returns a read-only collection view of the values contained in this map.
	 * This collection is updated when the map changes.
	 *
	 * @return a collection view of the values contained in this map.
	 */
	public abstract SpcfCollection<V> getValueList();

	/**
	 * Removes a single instance of the specified key from this collection, if
	 * it is present.
	 *
	 * @param key key of type K whose mapping is to be removed from the map.
	 * @return previous value associated with specified key, or null if there was no mapping for key.
     * @throws SpcfIllegalArgumentException if the type of the specified key
     * 	       is incompatible with this collection (optional).
     * @throws SpcfArgumentNullException if the specified key is null and this
     *         collection does not support null keys (optional).
     * @throws SpcfUnsupportedOperationException remove is not supported by this
     *         collection.
	 */
	public abstract V remove(K key);

	/**
	 * Returns the number of key-value mappings in this map.
	 *
	 * @return the number of key-value mappings in this map.
	 */
	public abstract int getSize();
	
	/**
	 * Returns a portable iterator over the elements contained in this collection.
	 *
	 * @return an ISpcfIterator over the elements contained in this collection.
	 * @see ISpcfIterator
	 */
	public ISpcfIterator<SpcfPair<K,V>> getIterator()
	{
		SpcfCollection<SpcfPair<K,V>> col = SpcfFactory.getInstance().<SpcfPair<K,V>>createArrayList();
		
		SpcfCollection<K> keyList = this.getKeyList();
		ISpcfIterator<K> it = keyList.getIterator();
		
		while (it.hasNext()) 
		{
			K key = it.next();
			V val = this.getItem(key);
		
			SpcfPair<K,V> pair = new SpcfPair<K,V>(key, val);
			col.add(pair);
		}
		
		return col.getIterator();
	}
	
	/**
	 * Compares the specified object with this SpcfMap for equality. 
	 * Returns true if the given object is also a SpcfMap and the 
	 * two SpcfMaps represent the same mappings. More formally, 
	 * two SpcfMaps t1 and t2 represent the same mappings 
	 * if t1.entrySet().equals(t2.entrySet()). 
	 * This ensures that the equals method works properly across 
	 * different implementations of SpcfMap.
	 * @param o Object to be compared for equality with this SpcfMap.
	 * @return  true if the objects are the same; false otherwise.
	 */
	public boolean equals(Object o)
	{
		// not equal if object parameter is null
		if (o == null) return false;
		
		// not equal if object parameter is not SpcfMap
		if (!(o instanceof ISpcfMap)) return false;
		
		// Cast to SpcfMap
		ISpcfMap inMap = (ISpcfMap) o;

		// If we were handed a pointer to ourselves, then we are the same:
		if (this == inMap) return true;

		// not equal if number of members are not the same
		if (getSize() != inMap.getSize()) return false;

		// iterate through the SpcfMap and make sure the parameter SpcfMap
		// contains the same elements
		SpcfCollection<K> keyList = this.getKeyList();
		ISpcfIterator<K> it = keyList.getIterator();
		try
		{
			while(it.hasNext())
			{
				K keyObj = it.next();

				// Both maps must contain the same key
				if (!inMap.containsKeyObject(keyObj))
				{
					return false;
				}

				// If the item with the given key is null, then the matching item must also be null.
				if ((getItem(keyObj) == null) && (inMap.getItemAsObject(keyObj) != null)) return false;
				
				// Each map must have the same value for the same keys
				if (!getItem(keyObj).equals(inMap.getItemAsObject(keyObj)))
				{
					return false;
				}
			}
		}
		catch (SpcfNoSuchElementException e)
		{
			e.toString();
			return false;
		}
		catch (SpcfConcurrentModificationException e)
		{
			e.toString();
			return false;
		}
		catch (SpcfArgumentNullException e)
		{
			e.toString();
			return false;
		}
		catch (SpcfIllegalArgumentException e)
		{
			e.toString();
			return false;
		}

		return true;
	}

	
	/**
	 * Creates a string representation of this map and its contents.
	 * 
	 * @return a string representing the contents of this map.
	 */
	@Override
	public String toString()
	{
		SpcfStringBuilder buf = SpcfFactory.getInstance().createStringBuilder();
		buf.append("{");

		SpcfCollection<K> keyList = this.getKeyList();
		ISpcfIterator<K> it = keyList.getIterator();
		
		boolean hasNext = it.hasNext();
		while (hasNext) 
		{
			K key = it.next();
			V val = this.getItem(key);
			buf.append((Object)key == this ? "(this Map)" : (key == null ? "null" : key.toString()));
			buf.append("=");
			buf.append((Object)val == this ? "(this Map)" : (val == null ? "null" : val.toString()));
			hasNext = it.hasNext();
			if (hasNext) buf.append(", ");
		}
		
		buf.append("}");
		return buf.toString();
	}	
	
	/**
	 * Returns the hash code for the current SpcfMap class.  If objects are 
	 * determined to be equal according to the equals method, then the hash code
	 * should be identical for equal objects.  Objects having the same hash code
	 * are not guaranteed to be equal however.
	 * @return The int hash code for the current object.
	 */
	public int hashCode()
	{
		int hash = 0;
		SpcfCollection<K> keyList = this.getKeyList();
		ISpcfIterator<K> it = keyList.getIterator();

	    while(it.hasNext())
		{
			K keyObj = it.next();
			hash = hash + getItem(keyObj).hashCode();
		}

		return hash;
	}
	
	/**
	 * Indicates whether or not the given SpcfCollection is empty (contains 0 elements).
	 * @return True if the collection contains 0 elements, false otherwise.
	 */
	public boolean isEmpty()
	{
		return (this.getSize() == 0);
	}
}
