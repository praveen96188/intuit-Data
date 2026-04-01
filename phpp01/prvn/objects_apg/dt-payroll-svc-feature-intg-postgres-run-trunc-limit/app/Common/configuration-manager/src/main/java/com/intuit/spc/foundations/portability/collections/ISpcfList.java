package com.intuit.spc.foundations.portability.collections;

/**
 * A non-generic SpcfList interface intended to simulate type erasure in 
 * order to facilitate object-based examination of the generic SpcfList.
 */
public interface ISpcfList {
	
	/**
	 * Compares the specified object with this SpcfList for equality. 
	 * Returns true if and only if the specified object is also a SpcfList, 
	 * both SpcfLists have the same size, and all corresponding pairs of 
	 * elements in the two SpcfLists are equal. (Two elements e1 and e2 are 
	 * equal if <code>(e1==null ? e2==null : e1.equals(e2))</code>.) In other words, 
	 * two SpcfLists are defined to be equal if they contain the same elements 
	 * in the same order. This definition ensures that the equals method 
	 * works properly across different implementations of SpcfList&lt;T&gt;.
	 * @param o Object to be compared for equality with this SpcfList.
	 * @return  true if the objects are the same; false otherwise.
	 */
	boolean equals(Object o);
	
	/**
	 * Returns the hash code for the current SpcfList class.  If objects are 
	 * determined to be equal according to the equals method, then the hash code
	 * should be identical for equal objects.  Objects having the same hash code
	 * are not guaranteed to be equal however.
	 * @return The int hash code for the current object.
	 */
	int hashCode();
	
	/**
	 * Returns the element at the specified position in this list as an Object.
	 *
	 * @param index index of element to return.
	 * @return the object element at the specified position in this list.
	 */
    Object getItemAsObject(int index); 
    
	/**
	 * Returns the number of elements in this collection.
	 *
	 * @return the number of elements in this collection.
	 */
    int getSize();
}
