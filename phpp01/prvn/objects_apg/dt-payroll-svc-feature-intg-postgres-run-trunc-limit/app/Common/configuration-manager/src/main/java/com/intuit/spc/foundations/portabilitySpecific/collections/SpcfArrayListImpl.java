/*
 * author		RGS
 * department	SPC Foundations
 * project	    Portability
 * 2004-05-05   Initial Implementation
 *
 * NOTE: All third-party runtime exceptions are caught and re-thrown as
 * portable exceptions
 */
package com.intuit.spc.foundations.portabilitySpecific.collections;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.collections.*;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

import java.util.*;

/**
 * A platform specific implementation of SpcfArrayList
 */
public class SpcfArrayListImpl<T> extends SpcfArrayList<T> implements Cloneable, List<T>
{

	/**
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = 1991331944482701367L;

	//Member Variables
	/**
	 * The encapsulated third party runtime List implementation
	 */
	protected List<T> mThirdPartyList;

	//Constructors

	/**
	 * Constructs an empty list with the specified initial capacity.
	 * @param   initialCapacity   the initial capacity of the list.
	 * @throws SpcfIllegalArgumentException if the specified initial capacity
	 *            is negative
	 */
	public SpcfArrayListImpl(int initialCapacity)
	{
		try
		{
			mThirdPartyList = new ArrayList<T>(initialCapacity);
		}
		catch(IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}
	
	/**
	 * Constructs an empty list with the specified initial capacity.
	 * @param   initialCapacity   the initial capacity of the list.
	 * @param typeParam the type parameter for this generic type
	 * @throws SpcfIllegalArgumentException if the specified initial capacity
	 *            is negative
	 */
	public SpcfArrayListImpl(int initialCapacity, SpcfClass typeParam)
	{
		try
		{
			mThirdPartyList = new ArrayList<T>(initialCapacity);
			mTypeParameters = new SpcfClass[] { typeParam };
		}
		catch(IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}

	/**
	 * Constructs an empty list with an initial capacity of ten.
	 */
	public SpcfArrayListImpl()
	{
		mThirdPartyList = new ArrayList<T>(10);
	}
	
	/**
	 * Constructs an empty list with an initial capacity of ten.
	 * @param typeParam the type parameter for this generic type
	 */
	public SpcfArrayListImpl(SpcfClass typeParam)
	{
		mTypeParameters = new SpcfClass[] { typeParam };
		mThirdPartyList = new ArrayList<T>(10);
	}

	/**
	 * Constructs a list with the specified third party runtime object.
	 * This constructor allows the user to encapsulate any object that
	 * implements the java.util.List interface, such as ArrayList and
	 * LinkedList.
	 *
	 * @param   thirdPartyList java.util.List implementation
	 * @throws SpcfArgumentNullException if thirdPartyList is null
	 */
	public SpcfArrayListImpl(List<T> thirdPartyList)
	{
		SpcfParamValidator.checkIsNotNull(thirdPartyList, "thirdPartyList");

		mThirdPartyList = thirdPartyList;
	}


	/**
	 * Returns the encapsulated third party runtime object
	 * @return a java.util.List implementation
	 */
	public List<T> toSpecific()
	{
		return mThirdPartyList;
	}

	/**
	 * Returns the encapsulated third party runtime object
	 * @return a java.util.List implementation
	 */
	public List<T> getSpecific()
	{
		return mThirdPartyList;
	}
	
	//SpcfArrayList Overrides

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfList#add
	 */
	@Override
	public void add(int index, T obj)
	{
		try
		{
			mThirdPartyList.add(index, obj);
		}
		catch (IndexOutOfBoundsException e)
		{
			throw new SpcfIndexOutOfBoundsException(e);
		}
		catch (UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#addAll
	 * (com.intuit.spc.foundations.portability.collections.SpcfCollection)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean addAll(SpcfCollection<T> collection)
	{
		boolean isListChanged = false;

		SpcfParamValidator.checkIsNotNull(collection, "collection");

		try
		{
			// make a copy of the arrayList so we do not alter the
			// original one if we have an exception part of the way
			// through the adds
			SpcfArrayListImpl<T> tmpArrayList = (SpcfArrayListImpl<T>) clone();

			ISpcfIterator<T> colIt = collection.getIterator();
			SpcfParamValidator.checkIsNotNull(colIt, "list iterator");

			//Use third-party iterator to add all of the elements of one
			//collection into the other
			while (colIt.hasNext())
			{
				if (tmpArrayList.add(colIt.next()))
				{
					isListChanged = true;
				}
			}
			// now save the temporary arrayList where we added all of the
			// elements of the collection, to our internal arrayList value
			mThirdPartyList = tmpArrayList.toSpecific();

		}
		catch(UnsupportedOperationException e)
		{
			//This collection does not support the addAll method.
			throw new SpcfUnsupportedOperationException(e);
		}
		catch(NullPointerException e)
		{
	        //If the specified collection contains one or more null elements
			//and this collection does not support null elements, or if the
			//specified collection is null.
			throw new SpcfNullPointerException(e);
		}
		return isListChanged;
	}

	/**
 	 * @see com.intuit.spc.foundations.portability.collections.SpcfArrayList#clone
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SpcfArrayList<T> clone()
	{
		List<T> thirdPartyList;

		try
		{	//Create a new instance of the List
			//Throws InstantiationException if List is an instance of
			//  java.util.Collections$SynchronizedList
			//  java.util.Collections$UnmodifiableList
			// Cast to generic causes warning which is suppressed above.
			thirdPartyList = mThirdPartyList.getClass().newInstance();

			//Copy all elements to the new List (reference only)
			thirdPartyList.addAll(mThirdPartyList);
			
			//TODO: Determine if the below implementation succeeds, as it doesn't
			// experience unchecked errors.
			//SpcfArrayList<T> newList = SpcfFactory.getInstance().<T>createArrayList();
			//newList.addAll(this);
			//return newList;
		}
		catch(UnsupportedOperationException e)
		{
			//this collection does not support the addAll method.
			throw new SpcfUnsupportedOperationException(e);
		}
		catch(ClassCastException e)
		{
			//the class of an element of the specified collection prevents it
			//from being added to this collection.
			throw new SpcfClassCastException(e);
		}
		catch(NullPointerException e)
		{
			//if the specified collection contains one or more null elements
			//and this collection does not support null elements, or if the
			//specified collection is null.
			throw new SpcfNullPointerException(e);
		}
		catch(IllegalArgumentException e)
		{
			//some aspect of an element of the specified collection prevents
			//it from being added to this collection.
			throw new SpcfIllegalArgumentException(e);
		}
		catch(InstantiationException e)
		{
			//newInstance
			throw new SpcfUnsupportedOperationException(e);
		}
		catch(IllegalAccessException e)
		{
			//newInstance
			throw new SpcfUnsupportedOperationException(e);
		}

		//Encapsulate the new reference with a portable object implementation
		return new SpcfArrayListImpl<T>(thirdPartyList);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfArrayList#getItem(int)
	 */
	@Override
	public T getItem(int index)
	{
		try
		{
			return mThirdPartyList.get(index);
		}
		catch(IndexOutOfBoundsException e)
		{
			//invalid index
			throw new SpcfIndexOutOfBoundsException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#contains(Object)
	 */
	// need to change from T to Object so we can implement List<T>
//	@Override
//	public boolean contains(T obj)
//	{
//		return mThirdPartyList.contains(obj);
//	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#contains(Object)
	 */
	@Override
	public boolean contains(Object o)
	{
		return mThirdPartyList.contains(o);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfList#indexOf(Object)
	 */
	// need to change from T to Object so we can implement List<T>
//	@Override
//	public int indexOf(T obj)
//	{
//		return mThirdPartyList.indexOf(obj);
//	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfList#indexOf(Object)
	 */
	@Override
	public int indexOf(Object o)
	{
		return mThirdPartyList.indexOf(o);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfList#removeAt(int)
	 */
	@Override
	public void removeAt(int index)
	{
		try
		{
			mThirdPartyList.remove(index);
		}
		catch(IndexOutOfBoundsException e)
		{
			throw new SpcfIndexOutOfBoundsException(e);
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfList#setItem(int, Object)
	 */
	@Override
	public void setItem(int index, T obj)
	{
		try
		{
			mThirdPartyList.set(index, obj);
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
		catch(IndexOutOfBoundsException e)
		{
			throw new SpcfIndexOutOfBoundsException(e);
		}

	}

	//SpcfCollection overrides

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#add
	 */
	@Override
	public boolean add(T obj)
	{
		boolean returnValue = false;

		try
		{
			returnValue = mThirdPartyList.add(obj);
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
		//throws ClassCastException, NullPointerException, IllegalArgumentException
		//consistency - .NET simply returns false
		//do nothing; do not re-throw.
		catch(RuntimeException e)
		{
			// purposely left blank
		}

		return returnValue;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#clear()
	 */
	@Override
	public void clear()
	{
		try
		{
			mThirdPartyList.clear();
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#remove
	 */
	// need to change from T to Object so we can implement List<T>
//	@Override()
//	public boolean remove(T obj)
//	{
//		try
//		{
//			return mThirdPartyList.remove(obj); // Just in case an T is an int.
//		}
//		catch(UnsupportedOperationException e)
//		{
//			throw new SpcfUnsupportedOperationException(e);
//		}
//		//throws ClassCastException, NullPointerException
//		//consistency - .NET simply does nothing
//		//do nothing; do not re-throw.
//		catch(RuntimeException e)
//		{
//			return false;
//		}
//	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#remove
	 */
	@Override
	public boolean remove(Object obj)
	{
		try
		{
			return mThirdPartyList.remove(obj); // Just in case an T is an int.
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
		//throws ClassCastException, NullPointerException
		//consistency - .NET simply does nothing
		//do nothing; do not re-throw.
		catch(RuntimeException e)
		{
			return false;
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#getSize()
	 */
	@Override
	public int getSize()
	{
		return mThirdPartyList.size();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#getIterator()
	 */
	@Override
	public ISpcfIterator<T> getIterator()
	{
		//Create a platform specific implementation of ISpcfIterator (portable)
		return new SpcfIteratorImpl<T>( mThirdPartyList.iterator() );
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#toArray()
	 */
	@Override
	public Object[] toArray() {
		return mThirdPartyList.toArray();
		//return SpcfToArrayImpl.toArray(this);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#toArray()
	 */
	@Override
	public<E> E[] toArray(E[] a) {
		SpcfParamValidator.checkIsNotNull(a, "Array Argument");
		try
		{
			return mThirdPartyList.toArray(a);
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
	
	// the following are some abstract methods of List which need to be implemented
	
	/**
	 * override List.subList(int,int)
	 * @param fromIndex
	 * @param toIndex
	 * @return list of type "T"
	 */
	public List<T> subList(int fromIndex, int toIndex)
	{
		try
		{
			return mThirdPartyList.subList(fromIndex, toIndex);
		}
		catch(IndexOutOfBoundsException e)
		{
			//invalid index
			throw new SpcfIndexOutOfBoundsException(e);
		}
		catch(IllegalArgumentException e)
		{
			//invalid index
			throw new SpcfIllegalArgumentException(e);
		}
	}
	
	/**
	 * implement List.size()
	 * @return the length of the list
	 */
	public int size()
	{
		return getSize();
	}
	
	/**
	 * Replaces the element at the specified position in this list with the specified element.
	 * @return element of type T, the element previously at the specified position.
	 * @param index - index of element to replace
	 * @param element - element to be stored at the specified position
	 */
	public T set(int index, T element)
	{
		try
		{
			return mThirdPartyList.set(index, element);
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
		catch(IndexOutOfBoundsException e)
		{
			//invalid index
			throw new SpcfIndexOutOfBoundsException(e);
		}
		catch(RuntimeException e)
		{
			throw new SpcfRuntimeException(e);
		}
	}
	
	/**
	 * Retains only the elements in this collection that are contained in the specified collection.
	 * @param c - elements to be retained in this collection.
	 * @return boolean value, true if this collection changed as a result of the call.
	 */
	public boolean retainAll(Collection<?> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection");

		try
		{
			return mThirdPartyList.retainAll(c);
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
	}
	
	/**
	 * Removes from this collection all of its elements that are 
	 * contained in the specified collection
	 * @param c - elements to be removed from this collection
	 * @return boolean value, true if this collection changed as a result of the call.
	 */
	public boolean removeAll(Collection<?> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection");

		try
		{
			return mThirdPartyList.removeAll(c);
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
	}
	
	/**
	 * Removes the element at the specified position in this list. 
	 * Shifts any subsequent elements to the left (subtracts one from their indices).
	 * @param index - the index of the element to removed
	 * @return object of type T, the element that was removed from the list.
	 */
	public T remove(int index)
	{
		try
		{
			return mThirdPartyList.remove(index);
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
		catch(IndexOutOfBoundsException e)
		{
			//invalid index
			throw new SpcfIndexOutOfBoundsException(e);
		}
		catch(RuntimeException e)
		{
			throw new SpcfRuntimeException(e);
		}
	}
	
	/** 
	 * Returns a list iterator of the elements in this list (in proper sequence).
	 * @param index - index of first element to be returned from the list iterator (by a call to the next method).
	 * @return a list iterator of the elements in this list (in proper sequence), starting at the specified position in this list.
	 */
	public ListIterator<T> listIterator(int index)
	{
		try
		{
			return mThirdPartyList.listIterator(index);
		}
		catch(IndexOutOfBoundsException e)
		{
			//invalid index
			throw new SpcfIndexOutOfBoundsException(e);
		}
	}
	
	/** 
	 * Returns a list iterator of the elements in this list (in proper sequence).
	 * @return a list iterator of the elements in this list (in proper sequence).
	 */
	public ListIterator<T> listIterator()
	{
		return mThirdPartyList.listIterator();
	}
	
	/** 
	 * Returns the index of the last occurrence of the specified object in this list.
	 * @param o object to look for
	 * @return the index of the last occurrence of the specified 
	 * object in this list; returns -1 if the object is not found.
	 */
	@Override
	public int lastIndexOf(Object o)
	{
		return mThirdPartyList.lastIndexOf(o);
	}
	
	/** 
	 * @param index index of element to return
	 * @return element of type T
	 */
	public T get(int index)
	{
		try
		{
			return mThirdPartyList.get(index);
		}
		catch(IndexOutOfBoundsException e)
		{
			//invalid index
			throw new SpcfIndexOutOfBoundsException(e);
		}
	}
	
	/** 
	 * Returns true if this collection contains all of the elements 
	 * in the specified collection.
	 * @param c - collection to be checked for containment in this collection
	 * @return boolean value, true if this collection contains all of the elements 
	 * in the specified collection.
	 */
	public boolean containsAll(Collection<?> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection");

		return mThirdPartyList.containsAll(c);
	}
	
	/** 
	 * Appends all of the elements in the specified Collection to the end of this list, 
	 * in the order that they are returned by the specified Collection's Iterator. 
	 * @param c - collection whose elements are to be added to this collection
	 * @return boolean value, true if this collection changed as a result of the call.
	 */
	public boolean addAll(Collection<? extends T> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection");

		try
		{
			return mThirdPartyList.addAll(c);
		}
		catch(UnsupportedOperationException e)
		{
			//This collection does not support the addAll method.
			throw new SpcfUnsupportedOperationException(e);
		}

	}
	
	/** 
	 * Inserts all of the elements in the specified Collection into this list, 
	 * starting at the specified position.
	 * @param c collection to add
	 * @param index index where to add collection
	 * @return boolean value
	 */
	public boolean addAll(int index, Collection<? extends T> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection");

		try
		{
			return mThirdPartyList.addAll(index, c);
		}
		catch(IndexOutOfBoundsException e)
		{
			//invalid index
			throw new SpcfIndexOutOfBoundsException(e);
		}
	}

}
