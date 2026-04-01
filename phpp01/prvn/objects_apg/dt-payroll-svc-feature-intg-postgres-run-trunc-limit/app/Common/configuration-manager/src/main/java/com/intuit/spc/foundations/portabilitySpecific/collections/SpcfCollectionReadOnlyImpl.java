/*
 * author		LGM
 * department	SPC Foundations
 * project	    Portability
 * 2005-02-16   Initial Implementation
 */
package com.intuit.spc.foundations.portabilitySpecific.collections;

import java.util.Collection;
import java.util.Collections;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfClassCastException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.collections.SpcfCollection;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;

/**
 * An abstract class from which portable collections are derived.
 *  @param <T> the type stored in the set
 */
public class SpcfCollectionReadOnlyImpl<T> extends SpcfCollection<T> implements Collection<T>
{

	/**
	 * serial version UID used for serializing
	 */
	private static final long serialVersionUID = -7764202581571812829L;
	
	/**
	 * The encapsulated third party runtime object
	 */
	protected Collection<T> mThirdPartyCollection;

	//Constructors

	/**
	 * Constructs a collection with the specified third party runtime object.
	 * This constructor encapsulates any object that
	 * implements the java.util.Collection interface.
	 * @param   thirdPartyCollection java.util.Collection implementation
	 * @throws SpcfArgumentNullException if thirdPartyCollection is null
	 */
	public SpcfCollectionReadOnlyImpl(Collection<T> thirdPartyCollection)
	{
		SpcfParamValidator.checkIsNotNull(thirdPartyCollection, "thirdPartyCollection");

		mThirdPartyCollection = thirdPartyCollection;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#add
	 */
	@Override
	public boolean add(T obj)
	{
		throw new SpcfUnsupportedOperationException();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#addAll(SpcfCollection)
	 */
	@Override
	public boolean addAll(SpcfCollection<T> collection)
	{
		throw new SpcfUnsupportedOperationException();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#clear()
	 */
	@Override
	public void clear()
	{
		throw new SpcfUnsupportedOperationException();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#contains
	 */
	@Override
	public boolean contains(Object obj)
	{
		return mThirdPartyCollection.contains(obj);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#remove
	 */
	@Override
	public boolean remove(Object obj)
	{
		throw new SpcfUnsupportedOperationException();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#getSize()
	 */
	@Override
	public int getSize()
	{
		return mThirdPartyCollection.size();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#getIterator()
	 */
	@Override
	public ISpcfIterator<T> getIterator()
	{
		return new SpcfIteratorImpl<T>(mThirdPartyCollection.iterator());
	}

	/**
	 * Returns the readonly encapsulated third party runtime object
	 * @return a java.util.Collection implementation
	 */
	public Collection<T> toSpecific()
	{
		return Collections.unmodifiableCollection(mThirdPartyCollection);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#toArray()
	 */
	@Override
	public Object[] toArray() {
		return mThirdPartyCollection.toArray();
		//return SpcfToArrayImpl.toArray(this);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#toArray(E[])
	 */
	@Override
	public <E> E[] toArray(E[] a) {
		SpcfParamValidator.checkIsNotNull(a, "Array Argument");
		try
		{
			return mThirdPartyCollection.toArray(a);
		}
		catch(ClassCastException e)
		{
			throw new SpcfClassCastException(e);
		}
		catch(ArrayStoreException ase)
		{
			throw new SpcfClassCastException(ase);			
		}
		//return SpcfToArrayImpl.toArray(a, this);
	}

	/**
	 * implements java.util.Collection#addAll(Collection<? extends T>)
	 * @param collection this is ignored
	 * @return this always throws an exception
	 * @throws SpcfUnsupportedOperationException always throws this exception
	 */
	public boolean addAll(Collection<? extends T> collection)
	{
		throw new SpcfUnsupportedOperationException();
	}

	/**
	 * Returns true if this collection contains all of the elements of the specified collection. 
	 * If the specified collection is also a collection, this method returns true if it is a subset of this collection.
	 * @param c collection to be checked for containment in this set
	 * @return true if this set contains all of the elements of the specified collection
	 * @exception SpcfClassCastException if the types of one or more elements in the specified collection are incompatible with this collection (optional). 
     * @exception SpcfArgumentNullException if the specified collection contains one or more null elements and this collection does not support null elements (optional). 
     * @exception SpcfArgumentNullException if the specified collection is null.
	 */
	public boolean containsAll(Collection<?> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection is null");
		
		try
		{
			return mThirdPartyCollection.containsAll(c);
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
	 * throws SpcfUnsupportedOperationException since this is a readonly collection
	 * @param c collection that defines which elements will be removed from this collection
	 * @return true if this collection changed as a result of the call.
	 * @exception SpcfUnsupportedOperationException if the removeAll  method is not supported by this Collection. 
	 */
	public boolean removeAll(Collection<?> c)
	{
		throw new SpcfUnsupportedOperationException();
	}

	/**
	 * Returns the number of elements in this collection. 
	 * If this collection contains more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE
	 * @return the size of the collection
	 */
	public int size()
	{
		return mThirdPartyCollection.size();
	}

	/**
	 * throws SpcfUnsupportedOperationException since this is a readonly collection
	 * @param c collection that defines which elements will be retained from this collection
	 * @return true if this collection changed as a result of the call.
	 * @exception SpcfUnsupportedOperationException if the retainAll  method is not supported by this Collection. 
	 */
	public boolean retainAll(Collection<?> c)
	{
		throw new SpcfUnsupportedOperationException();
	}

}
