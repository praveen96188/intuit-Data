package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;

/**
 * A non-generic SpcfMap interface intended to simulate type erasure in 
 * order to facilitate object-based examination of the generic SpcfMap.
 */
public interface ISpcfMap {
	
	/**
	 * Returns the number of elements in this collection.
	 *
	 * @return the number of elements in this collection.
	 */
	int getSize();
	
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
	boolean containsKeyObject(Object key);
	
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
	 */
	Object getItemAsObject(Object key);
	
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
	boolean equals(Object o);
	
	/**
	 * Returns the hash code for the current SpcfMap class.  If objects are 
	 * determined to be equal according to the equals method, then the hash code
	 * should be identical for equal objects.  Objects having the same hash code
	 * are not guaranteed to be equal however.
	 * @return The int hash code for the current object.
	 */
	int hashCode();
}
