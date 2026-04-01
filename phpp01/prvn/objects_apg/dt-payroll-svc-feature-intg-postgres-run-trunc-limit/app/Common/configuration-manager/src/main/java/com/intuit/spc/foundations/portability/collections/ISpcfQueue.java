package com.intuit.spc.foundations.portability.collections;

/**
 * A non-generic SpcfCollection interface intended to simulate type erasure in 
 * order to facilitate object-based examination of the generic SpcfQueue.
 */
public interface ISpcfQueue {

	/**
	 * Compares the specified object with this ISpcfQueue for equality. 
	 * Returns true if and only if the specified object is also an ISpcfQueue, 
	 * both ISpcfQueues have the same size, and all corresponding pairs of 
	 * elements in the two SpcfLists are equal. (Two elements e1 and e2 are 
	 * equal if <code>(e1==null ? e2==null : e1.equals(e2))</code>.) In other words, 
	 * two SpcfQueues are defined to be equal if they contain the same elements 
	 * in the same order. This definition ensures that the equals method 
	 * works properly across different implementations of SpcfQueue&lt;T&gt;.
	 * @param o Object to be compared for equality with this SpcfList.
	 * @return  true if the objects are the same; false otherwise.
	 */
	boolean equals(Object o);
	
	/**
	 * Returns the hash code for the current SpcfQueue class.  If objects are 
	 * determined to be equal according to the equals method, then the hash code
	 * should be identical for equal objects.  Objects having the same hash code
	 * are not guaranteed to be equal however.
	 * @return The int hash code for the current object.
	 */
	int hashCode();
	
	/**
	 * Returns the number of elements in this collection.
	 *
	 * @return the number of elements in this collection.
	 */
    int getSize();
    
    /**
     * Adds the given object to the queue.  The object is added into the last 
     * position in the queue, and the remainder of the queue is not altered.  
     * @param o
     * @return True if the object could be added to the collection, false otherwise.
     */
    boolean enqueueObject(Object o);
    
    /**
     * Retrieves and removes the head (first item) of this queue.  If the queue is
     * empty, then null will be returned.
     * @return The first item in the queue, or null if the queue is empty.
     */
    Object dequeueObject();
    
    /**
     * Retrieves the head (first item) from this queue without removing it.  This
     * method differs from dequeueObject in that the head element is not removed
     * from the first position in the queue.  If the queue is empty, then null 
     * will be returned.
     * @return The first item in the queue, or null if the queue is empty.
     */
    Object peekObject();
    
	/**
	 * Returns an array containing all of the elements in this collection. 
	 * If the collection makes any guarantees as to what order
	 * its elements are returned by its iterator, this method must return the 
	 * elements in the same order.<br/><br/>
	 *
	 * The returned array will be "safe" in that no references to it are maintained 
	 * by this collection.(In other words, this method
	 * must allocate a new array even if this collection is backed by an array). The caller is 
	 * thus free to modify the returned
	 * array.<br/><br/>
	 *
	 * This class provides default implementation of this method based on iterator. 
	 * Derived classes may consider to override
	 * the default implemenation of this method to provide efficient implementation or 
	 * to fill the array in different
	 * sequence.<br/><br/>
	 *
	 * If the collection contains no elements, a zero length array is returned.

	 * This method acts as bridge between array-based and collection-based APIs.
	 *
	 * @return an array of objects contained in collection or a zero length array 
	 * if the collection is empty.
	 */
    Object[] toArray();
}
