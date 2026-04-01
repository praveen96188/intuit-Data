package com.intuit.spc.foundations.portability.collections;

/**
 * A non-generic SpcfSet interface intended to simulate type erasure in 
 * order to facilitate object-based examination of the generic SpcfSet.
 */
public interface ISpcfSet {
	
	/**
	 * Returns the number of elements in this collection.
	 *
	 * @return the number of elements in this collection.
	 */
	int getSize();
	
	/**
	  * Returns true if this set contains the specified object.
	  *
	  * @param obj Item of type object whose presence in this set is to be tested.
	  * @return true if this set contains the specified item.
	  */
	boolean containsObject(Object obj);
	
	/**
	 * Compares the specified object with this SpcfSet for equality. 
	 * Returns true if the specified object is also a SpcfSet, 
	 * the two SpcfSet have the same size, 
	 * and every member of the specified SpcfSet is contained in this SpcfSet 
	 * (or equivalently, every member of this SpcfSet is contained in the 
	 * specified SpcfSet). This definition ensures that the equals method 
	 * works properly across different implementations of the SpcfSet interface.
	 * @param o Object to be compared for equality with this SpcfSet.
	 * @return  true if the objects are the same; false otherwise.
	 */
	boolean equals(Object o);
	
	/**
	 * Returns the hash code for the current SpcfSet class.  If objects are 
	 * determined to be equal according to the equals method, then the hash code
	 * should be identical for equal objects.  Objects having the same hash code
	 * are not guaranteed to be equal however.
	 * @return The int hash code for the current object.
	 */
	int hashCode();
}
