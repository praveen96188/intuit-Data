package com.intuit.spc.foundations.portabilitySpecific.collections;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.NoSuchElementException;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfClassCastException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfQueue;
import com.intuit.spc.foundations.portability.collections.SpcfNoSuchElementException;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * A collection designed for holding elements prior to processing. 
 * Besides basic Collection operations, queues provide additional insertion, extraction, 
 * and inspection operations.
 */
public class SpcfQueueImpl<T> extends SpcfQueue<T> implements Queue<T>
{

	/**
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 7006639831481335835L;
	
	/**
	 * The encapsulated third party runtime object
	 */
	protected Queue<T> mThirdPartyQueue;
	
	/**
	 * construtor to create an empty Queue
	 */
	public SpcfQueueImpl() 
	{
		mThirdPartyQueue = new LinkedList<T>();
	}
	
	/**
	 * construtor which defines the type to store in the Queue
	 * @param typeParam the type to store in the Queue
	 */
	public SpcfQueueImpl(SpcfClass typeParam)
	{
		this.mTypeParameters = new SpcfClass[] { typeParam };
		mThirdPartyQueue = new LinkedList<T>();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfQueue#enqueue
	 */
	@Override
	public boolean enqueue(T o) 
	{
		return mThirdPartyQueue.add(o);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfQueue#dequeue()
	 */
	@Override
	public T dequeue() 
	{
		return mThirdPartyQueue.remove();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfQueue#peek()
	 */
	@Override
	public T peek() 
	{
		return mThirdPartyQueue.peek();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#contains
	 */
	@Override
	public boolean contains(Object obj) 
	{
		return mThirdPartyQueue.contains(obj);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#remove
	 */
	@Override
	public boolean remove(Object obj) 
	{
		return mThirdPartyQueue.remove(obj);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#getSize()
	 */
	@Override
	public int getSize() 
	{
		return mThirdPartyQueue.size();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#toArray()
	 */
	@Override
	public Object[] toArray() 
	{
		return mThirdPartyQueue.toArray();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#toArray()
	 */
	@Override
	public <E> E[] toArray(E[] a) 
	{
		return mThirdPartyQueue.toArray(a);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#getIterator()
	 */
	@Override
	public ISpcfIterator<T> getIterator() 
	{
		//Create a platform specific implementation of ISpcfIterator (portable)
		return new SpcfIteratorImpl<T>( mThirdPartyQueue.iterator() );
	}
	/**
	 * Returns the hash code value for this collection. 
	 * @return the hash code value for this collection
	 */
	@Override
	public int hashCode()
	{
		return mThirdPartyQueue.hashCode();
	}

	/**
	 * Returns the encapsulated third party runtime object
	 * @return a Queue<T> implementation
	 */
	public Queue<T> toSpecific()
	{
		return mThirdPartyQueue;
	}
	
	/**
	 * Removes all this collection's elements that are also contained in the specified 
	 * collection (optional operation). After this call returns, this collection will contain 
	 * no elements in common with the specified collection.
	 * @param c collection that defines which elements will be removed from this collection
	 * @return true if this collection changed as a result of the call.
	 * @exception SpcfUnsupportedOperationException if the removeAll  method is not supported by this Collection. 
	 * @exception SpcfClassCastException if the types of one or more elements in this collection are incompatible with the specified collection (optional). 
	 * @exception SpcfArgumentNullException if this set contains a null element and the specified collection does not support null elements (optional). 
	 * @exception SpcfArgumentNullException if the specified collection is null. 
	 */
	public boolean removeAll(Collection<?> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection is null");
		
		try
		{
			return mThirdPartyQueue.removeAll(c);
		}
		catch (UnsupportedOperationException e1)
		{
			throw new SpcfUnsupportedOperationException(e1);
		}
		catch (ClassCastException e2)
		{
			throw new SpcfClassCastException(e2);
		}
		catch (NullPointerException e3)
		{
			throw new SpcfArgumentNullException(e3);
		}
	}
	
	/**
	 * Retains only the elements in this collection that are contained in the specified 
	 * collection (optional operation). In other words, removes from this collection all of 
	 * its elements that are not contained in the specified collection.
	 * @param c collection that defines which elements this collection will retain.
	 * @return true if this collection changed as a result of the call.
	 * @exception SpcfUnsupportedOperationException if the retainAll method is not supported by this Collection.
	 * @exception SpcfClassCastException if the types of one or more elements in this collection are incompatible with the specified collection
	 * @exception SpcfArgumentNullException if this set contains a null element and the specified collection does not support null elements
	 * @exception SpcfArgumentNullException if the specified collection is null
	 */
	public boolean retainAll(Collection<?> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection is null");
		
		try
		{
			return mThirdPartyQueue.retainAll(c);
		}
		catch (UnsupportedOperationException e1)
		{
			throw new SpcfUnsupportedOperationException(e1);
		}
		catch (ClassCastException e2)
		{
			throw new SpcfClassCastException(e2);
		}
		catch (NullPointerException e3)
		{
			throw new SpcfArgumentNullException(e3);
		}
	}
	
	/**
	 * Adds all of the elements in the specified collection to this collection 
	 * (optional operation). The behavior of this operation is undefined if the specified 
	 * collection is modified while the operation is in progress. (This implies that the 
	 * behavior of this call is undefined if the specified collection is this collection, 
	 * and this collection is nonempty.)
	 * @param c collection whose elements are to be added to this collection.
	 * @return true if this collection changed as a result of the call.
	 * @exception UnsupportedOperationException if the addAll method is not supported by this collection. 
	 * @exception ClassCastException if the class of some element of the specified collection prevents it from being added to this collection. 
	 * @exception NullPointerException if the specified collection contains one or more null elements and this set does not support null elements, or if the specified collection is null. 
	 * @exception IllegalArgumentException if some aspect of some element of the specified collection prevents it from being added to this collection.
	 */
	public boolean addAll(Collection<? extends T> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection is null");
		
		try
		{
			return mThirdPartyQueue.addAll(c);
		}
		catch (UnsupportedOperationException e1)
		{
			throw new SpcfUnsupportedOperationException(e1);
		}
		catch (ClassCastException e2)
		{
			throw new SpcfClassCastException(e2);
		}
		catch (NullPointerException e3)
		{
			throw new SpcfArgumentNullException(e3);
		}
		catch (IllegalArgumentException e3)
		{
			throw new SpcfIllegalArgumentException(e3);
		}
	}
	
	/**
	 * Returns the number of elements in this collection. If this collection contains more 
	 * than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
	 * @return the number of elements in this collection
	 */
	public int size()
	{
		return mThirdPartyQueue.size();
	}
	
	/**
	 * Returns true if this collection contains all of the elements in the specified collection.
	 * @param c collection to be checked for containment in this collection
	 * @return true if this collection contains all of the elements of the specified collection
	 * @exception SpcfClassCastException if the types of one or more elements in the specified collection are incompatible with this collection (optional). 
     * @exception SpcfArgumentNullException if the specified collection contains one or more null elements and this collection does not support null elements (optional). 
     * @exception SpcfArgumentNullException if the specified collection is null.
	 */
	public boolean containsAll(Collection<?> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection is null");
		
		try
		{
			return mThirdPartyQueue.containsAll(c);
		}
		catch (ClassCastException e2)
		{
			throw new SpcfClassCastException(e2);
		}
		catch (NullPointerException e3)
		{
			throw new SpcfArgumentNullException(e3);
		}
	}
	
	/**
	 * Retrieves and removes the head of this queue. 
	 * This method differs from the poll method in that it throws an exception if this 
	 * queue is empty.
	 * @return the head of this queue.
	 * @throws SpcfNoSuchElementException if this queue is empty.
	 */
	public T remove()
	{
		try
		{
			return mThirdPartyQueue.remove();
		}
		catch (NoSuchElementException e2)
		{
			throw new SpcfNoSuchElementException(e2);
		}
	}

	/**
	 * Retrieves, but does not remove, the head of this queue. 
	 * This method differs from the peek method only in that it throws an 
	 * exception if this queue is empty.
	 * @return the head of this queue.
	 * @throws SpcfNoSuchElementException if this queue is empty.
	 */
	public T element()
	{
		try
		{
			return mThirdPartyQueue.element();
		}
		catch (NoSuchElementException e2)
		{
			throw new SpcfNoSuchElementException(e2);
		}
	}
	
	/**
	 * Inserts the specified element into this queue, if possible. 
	 * When using queues that may impose insertion restrictions (
	 * for example capacity bounds), method offer is generally preferable to method 
	 * Collection.add(E), which can fail to insert an element only by throwing an exception.
	 * @param obj the element to insert.
	 * @return true if it was possible to add the element to this queue, else false
	 */
	public boolean offer(T obj)
	{
		return mThirdPartyQueue.offer(obj);
	}
	
	/**
	 * Retrieves and removes the head of this queue, or null  if this queue is empty.
	 * @return the head of this queue, or null if this queue is empty.
	 */
	public T poll()
	{
		return mThirdPartyQueue.poll();
	}
}
