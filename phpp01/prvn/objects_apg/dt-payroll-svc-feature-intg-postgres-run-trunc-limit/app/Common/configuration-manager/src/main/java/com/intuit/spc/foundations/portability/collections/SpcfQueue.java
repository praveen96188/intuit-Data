package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * A queue implementation of the SpcfCollection.
 * The queue does not allow positional access, but instead allows items
 * to be added to the tail of the queue and retrieved from the head of the
 * queue.  Duplicated and null values are generally permitted.
 */

public abstract class SpcfQueue<T> extends SpcfCollection<T> implements ISpcfQueue 
{
	private static final long serialVersionUID = -6873565150509268255L;
	private final int HashConst = 31;
	
	
	/**
	 * Adds an object to the queue
	 *
	 * @param  obj The T object to be added to the collection
	 * @return True if the collection changed as a result of the call.
	 *         False if the item could not be inserted into the collection.
     * @throws SpcfIllegalArgumentException some aspect of this element prevents
     *         it from being added to this collection.
	 */
	@Override
	public boolean add(T obj) 
	{
		return enqueue(obj);
	}

	/**
	 * Adds all of the elements in the specified collection to this collection.
	 * The behavior of this operation is undefined if the specified collection
	 * is modified while the operation is in progress.
	 *
	 * This implementation iterates over the specified collection, and adds each
	 * T object returned by the iterator to this collection, in turn.
	 *
	 * @param collection A collection whose elements are to be added to this collection.
	 * @return true If this collection changed as a result of the call.
     * @throws SpcfIllegalArgumentException some aspect of an element of the
     *	       specified collection prevents it from being added to this
     *	       collection.
	 */
	@Override
	public boolean addAll(SpcfCollection<T> collection) 
	{
		ISpcfIterator<T> it = collection.getIterator();
		while(it.hasNext())
		{
			enqueue(it.next());
		}
		return true;
	}

	/**
	 * Removes all of the elements from this collection.  The collection will
	 * be empty after this call returns.
	 */
	@Override
	public void clear() 
	{
		for(int i = 0; i < this.getSize(); i++)
	    {
	    	this.dequeue();
	    }
	}
	
    /**
     * Adds the given object to the queue.  The object is added into the last 
     * position in the queue, and the remainder of the queue is not altered.  
     * @param o
     * @return True if the object could be added to the collection, false otherwise.
     */
	@SuppressWarnings("unchecked")
	public boolean enqueueObject(Object o)
	{
		try
		{
			T item = (T)o;
			return enqueue(item);
		} 
		catch (Exception e)
		{
			e.toString();
			return false;
		}
	}
	
    /**
     * Retrieves and removes the head (first item) of this queue.  If the queue is
     * empty, then null will be returned.
     * @return The first item in the queue, or null if the queue is empty.
     */
	public Object dequeueObject() 
	{
		return dequeue();
	}

    /**
     * Retrieves the head (first item) from this queue without removing it.  This
     * method differs from dequeueObject in that the head element is not removed
     * from the first position in the queue.  If the queue is empty, then null 
     * will be returned.
     * @return The first item in the queue, or null if the queue is empty.
     */
	public Object peekObject() 
	{
		return peek();
	}
	
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
	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object o)
	{
		if (this == o) return true;
		
		if (!(o instanceof ISpcfQueue)) return false;
		
		ISpcfQueue queue = null;
		
		try
		{
			queue = (ISpcfQueue)o;
		} catch (ClassCastException e) {
			e.toString();
			return false;
		}
		
		if (this.getSize() != queue.getSize()) return false;

		Object[] arr1 = this.toArray();
		Object[] arr2 = queue.toArray();

		for(int i = 0; i < this.getSize(); i++)
		{
			try
			{
			    T item1 = (T)arr1[i];
			    T item2 = (T)arr2[i];
		        if (!item1.equals(item2)) return false;
			}
			catch (Exception e)
			{
				e.toString();
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Returns the hash code for the current SpcfQueue class.  If objects are 
	 * determined to be equal according to the equals method, then the hash code
	 * should be identical for equal objects.  Objects having the same hash code
	 * are not guaranteed to be equal however.
	 * @return The int hash code for the current object.
	 */
	public int hashCode()
	{
	    int hash = 0;
	    
		Object[] arr1 = this.toArray();
		for (int i = 0; i < arr1.length; i++)
		{
		    hash = (hash * HashConst) + arr1[i].hashCode();
		}
		
	    return hash;
	}
	
	/**
	 * Returns an empty portable queue.
	 * @return an SpcfQueue&lt;T&gt; object
	 */
	public static <S> SpcfQueue<S> createInstance()
	{
		return SpcfFactory.getInstance().<S>createQueue();
	}
	
	/**
	 * Returns an empty portable queue.
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfQueue&lt;T&gt; object
	 */
	public static <S> SpcfQueue<S> createInstance(SpcfClass typeParam)
	{
		return SpcfFactory.getInstance().<S>createQueue(typeParam);
	}
	
	/**
	 * Adds an object to the queue
	 *
	 * @param  o The T object to be added to the collection
	 * @return True if the collection changed as a result of the call.
	 *         False if the item could not be inserted into the collection.
     * @throws SpcfIllegalArgumentException some aspect of this element prevents
     *         it from being added to this collection.
	 */
	public abstract boolean enqueue(T o);
	
	/**
	 * Removes the head object from the queue and returns it.
	 *
	 * @return The object that is removed from the queue.
	 */
	public abstract T dequeue();
	
	/**
	 * Returns the head object from the queue.
	 *
	 * @return The head object in the queue.
	 */
	public abstract T peek();
}
