/*
 * author		RGS
 * department	SPC Foundations
 * project	    Portability
 * 2004-05-03   Initial Implementation
 */
package com.intuit.spc.foundations.portability.collections;

import java.io.Serializable;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * An abstract class from which portable collections are derived.
 */
public abstract class SpcfCollection<T>  extends SpcfCollectionIterable<T> implements Serializable, ISpcfParameterizedType 
{
	private static final long serialVersionUID = 6250160947165134016L;
	
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
	 * Adds an object to the collection
	 *
	 * @param  obj the T object to be added to the collection
	 * @return true if the collection changed as a result of the call.
	 *         false if this collection does not permit duplicates and
	 *         already contains the specified element
     * @throws SpcfUnsupportedOperationException add is not supported by
     *         this collection.
     * @throws SpcfArgumentNullException if the specified element is null and this
     *         collection does not support null elements.
     * @throws SpcfIllegalArgumentException some aspect of this element prevents
     *         it from being added to this collection.
	 */
	public abstract boolean add(T obj);

	/**
	 * Adds all of the elements in the specified collection to this collection.
	 * The behavior of this operation is undefined if the specified collection
	 * is modified while the operation is in progress.
	 *
	 * This implementation iterates over the specified collection, and adds each
	 * T object returned by the iterator to this collection, in turn.
	 *
	 * Note that this implementation will throw an UnsupportedOperationException unless
	 * addAll is overridden (assuming the specified collection is non-empty).
	 *
	 * @param collection collection whose elements are to be added to this collection.
	 * @return true if this collection changed as a result of the call.
     * @throws SpcfUnsupportedOperationException if this collection does not
     *         support the addAll method.
     * @throws SpcfArgumentNullException if the specified collection contains one
     *         or more null elements and this collection does not support null
     *         elements or if the specified collection is null.
     * @throws SpcfIllegalArgumentException some aspect of an element of the
     *	       specified collection prevents it from being added to this
     *	       collection.
	 */
	public abstract boolean addAll(SpcfCollection<T> collection);

	/**
	 * Adds all of the elements in the specified array to this collection.
	 * The behavior of this operation is undefined if the specified collection
	 * is modified while the operation is in progress.
	 *
	 * This implementation iterates over the specified array, and adds each
	 * T object returned by the iterator to this collection, in turn.
	 *
	 * Note that this implementation will throw an UnsupportedOperationException unless
	 * addAll is overridden (assuming the specified collection is non-empty).
	 *
	 * @param array Array whose elements are to be added to this collection.
	 * @return true if this collection changed as a result of the call.
     * @throws SpcfUnsupportedOperationException if this collection does not
     *         support the addAll method.
     * @throws SpcfArgumentNullException if the specified array contains one
     *         or more null elements and this collection does not support null
     *         elements or if the specified collection is null.
     * @throws SpcfIllegalArgumentException some aspect of an element of the
     *	       specified collection prevents it from being added to this
     *	       collection.
	 */
	public boolean addArray(T[] array)
	{
		SpcfParamValidator.checkIsNotNull(array, "Array Argument");
		
		boolean retVal = false;
		
		for(int i = 0; i < array.length; i++)
		{
			if (this.add(array[i])) retVal = true;
		}
		
		return retVal;
	}
	
	/**
	 * Removes all of the elements from this collection.  The collection will
	 * be empty after this call returns.
     * @throws SpcfUnsupportedOperationException if this collection does not
     *         support the clear method.
	 */
	public abstract void clear();

	/**
	 * Returns true if this collection contains the specified element.
	 *
	 * @param obj element whose presence in this collection is to be tested.
	 * @return true if the specified element is present; false otherwise.
     * @throws SpcfIllegalArgumentException if the type of the specified element
     * 	       is incompatible with this collection (optional).
     * @throws SpcfArgumentNullException if the specified element is null and this
     *         collection does not support null elements (optional).
	 **/
	public abstract boolean contains(T obj);

	/**
	 * Removes a single instance of the specified element from this collection, if
	 * it is present.
	 *
	 * @param obj element to be removed from this collection.
     * @throws SpcfIllegalArgumentException if the type of the specified element
     * 	       is incompatible with this collection (optional).
     * @throws SpcfArgumentNullException if the specified element is null and this
     *         collection does not support null elements (optional).
     * @throws SpcfUnsupportedOperationException remove is not supported by this
     *         collection.
	 */
	public abstract boolean remove(T obj);

	/**
	 * Returns the number of elements in this collection.
	 *
	 * @return the number of elements in this collection.
	 */
	public abstract int getSize();

	/**
	 * Returns an array containing all of the elements in this collection. 
	 * If the collection makes any guarantees as to what order
	 * its elements are returned by its iterator, this method must return the 
	 * elements in the same order.<br/><br/>
	 *
	 * The returned array will be "safe" in that no references to it are maintained 
	 * by this collection.(In other words, this method
	 * must allocate a new array even if this collection is backed by an array). 
	 * The caller is thus free to modify the returned
	 * array.<br/><br/>
	 *
	 * This class provides default implementation of this method based on iterator. 
	 * Derived classes may consider to override
	 * the default implemenation of this method to provide efficient implementation or to 
	 * fill the array in different
	 * sequence.<br/><br/>
	 *
	 * If the collection contains no elements, a zero length array is returned.

	 * This method acts as bridge between array-based and collection-based APIs.
	 *
	 * @return an array of objects contained in collection or a zero length array if the collection is empty.
	 */
	public abstract Object[] toArray();
	
	/**
	 * Returns an array containing all of the elements in this collection. 
	 * If the collection makes any guarantees as to what order
	 * its elements are returned by its iterator, this method must return the elements 
	 * in the same order.<br/><br/>
	 *
	 * The returned array will be "safe" in that no references to it are maintained by this 
	 * collection.(In other words, this method
	 * must allocate a new array even if this collection is backed by an array). 
	 * The caller is thus free to modify the returned
	 * array.<br/><br/>
	 *
	 * This class provides default implementation of this method based on iterator. 
	 * Derived classes may consider to override
	 * the default implemenation of this method to provide efficient implementation or to 
	 * fill the array in different
	 * sequence.<br/><br/>
	 *
	 * If the specified array has sufficient storage it is used to store the contained 
	 * elements cast to the specified
	 * element type. The type specified must be the same type or derivation of. 
	 * 
	 * If the specified array is larger than the number of elements in this collection, 
	 * the additional array cells are assigned null. If the specified array is 
	 * smaller than required, a new array of sufficient size is created, used and returned instead.
	 *
	 * This method acts as bridge between array-based and collection-based APIs.
	 *
	 * @param a array whose elements must be or derived from element T type of this collection type.
     * @throws SpcfClassCastException if the type of the specified element
     * 	       is not convertible to the element type contained in this collection.
     * @throws SpcfArgumentNullException if the specified array is null.
	 * @return an array containing all of the elements in this collection. See note above.
	 */
	public abstract <E> E[] toArray(E[] a);
	
	/**
	 * Removes from this colection all of its elements that are contained in the 
	 * specified collection. This operation effectively modifies this collection 
	 * so that its value is the asymmetric collection difference of the two collections.
	 * @param c Collection that defines which elements will be removed from the current collection.
	 * @return true if this collection changed as a result of the call.
	 */
	public boolean removeAll(SpcfCollection<T> c)
	{
		if ((c == null) || (c.getSize() == 0)) return false;
		
		boolean isChanged = false;
		
		ISpcfIterator<T> iter = c.getIterator();
		while(iter.hasNext())
		{
		    try
		    {
		        T obj = iter.next();
			    if (this.contains(obj)) 
			    {
			        this.remove(obj);
			        isChanged = true;
			    }
			} 
			catch (Exception e)
			{
			    /* 
			     * The object is not castable to type <T>, so can not be
			     * present in the current collecton.  Take no action.
			     */  
			}
		}
		    
	    return isChanged;
	}
	
	/**
	 * Retains only the elements in this collection that are contained in the specified 
	 * collection. This operation effectively modifies this collection so that its value 
	 * is the intersection of the two collections.
	 * @param c Collection that defines which elements this collection will retain. 
	 * @return true if this collection changed as a result of the call. 
	 */
	public boolean retainAll(SpcfCollection<T> c)
	{
		if (c == null) return false;
		
		boolean isChanged = false;
		SpcfCollection<T> deleteList = SpcfFactory.getInstance().<T>createArrayList();
		
		ISpcfIterator<T> iter = this.getIterator();
		while(iter.hasNext())
		{
			T obj = iter.next();
			if (!c.contains(obj)) 
			{
			    deleteList.add(obj);
			    isChanged = true;
			}
		}
		
		this.removeAll(deleteList);
		
	    return isChanged;
	}
    
	/**
	 * Returns true if this collection contains all of the elements of the specified 
	 * collection. This method returns true if the specified collection is a subset 
	 * of the current collection. 
	 * @param c Collection to be checked for containment in the current collection.
	 * @return true if this collection contains all of the elements of the specified collection. 
	 */
	public boolean containsAll(SpcfCollection<T> c)
	{		
		if (c == null) return false;
		if (c.getSize() == 0) return true;
		
		ISpcfIterator<T> iter = c.getIterator();
		
		while(iter.hasNext())
		{
			T obj = null;
			
			try
			{
			    obj = iter.next();
			}
			catch (Exception ex)
			{
				/*
				 * The element is not castable to the type of the current
				 * object, so the collection can not contain it.
				 */
				return false;
			}
			
			if (!this.contains(obj)) 
			{
			    return false;
			}
		}
		
	    return true;
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
