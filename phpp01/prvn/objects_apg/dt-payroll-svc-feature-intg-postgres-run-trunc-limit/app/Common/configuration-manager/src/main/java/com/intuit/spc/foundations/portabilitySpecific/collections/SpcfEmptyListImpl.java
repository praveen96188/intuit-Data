package com.intuit.spc.foundations.portabilitySpecific.collections;

import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfEmptyList;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;
import com.intuit.spc.foundations.portability.SpcfClassCastException;
import com.intuit.spc.foundations.portability.SpcfIndexOutOfBoundsException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 *  A platform specific implementation of SpcfEmptyList
 *  @param <T> the type stored in the list
 */
public class SpcfEmptyListImpl<T> extends SpcfEmptyList<T> implements List<T>
{

	/**
	 * serial version UID used for serializing
	 */
	private static final long serialVersionUID = 2914741289458834178L;
	
	/**
	 * static single instance
	 */
    @SuppressWarnings("unchecked")
	static SpcfEmptyListImpl sEmptyList;
    
    static 
    {
    	sEmptyList = new SpcfEmptyListImpl();
    }
	
    /**
     * default constructor
     */
	public SpcfEmptyListImpl() 
	{
		// This is intentially left blank
	}
	
	/**
	 * constructor taking an SpcfClass instance
	 * @param typeParam
	 */
	public SpcfEmptyListImpl(SpcfClass typeParam)
	{
		mTypeParameters = new SpcfClass[] { typeParam };
	}

	/**
	 * returns the instance of the empty list
	 * @param <T>
	 * @return an instance of the empty list
	 */
    @SuppressWarnings("unchecked")
	public static <T> SpcfEmptyList getInstance()
    {
    	return /*(SpcfEmptyList<T>)*/sEmptyList;
    }
	
    /**
     * @return array of objects
     */
	@Override
	public Object[] toArray() 
	{
		// cannot call ourselves to do "toArray" - infinite loop
		return new Object[0];
		//return SpcfToArrayImpl.toArray(this);
	}

	/**
	 * @param a an array to put the empty array list elements in
	 * @return an array of elements of type E
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <E> E[] toArray(E[] a)
	{
		SpcfParamValidator.checkIsNotNull(a, "Array Argument");
		try
		{
			// cannot call ourselves to do "toArray" - infinite loop
			for (int i = 0; i < a.length; i++)
			{
				a[i] = null;
			}
			return a;
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
	 * @return an iterator for the empty list
	 */
	@Override
	public ISpcfIterator<T> getIterator() 
	{
		return new SpcfEmptyIteratorImpl<T>();
	}

	// methods to implement List of T
	/**
	 * override List.subList(int,int)
	 * @param fromIndex
	 * @param toIndex
	 * @return list of type "T"
	 */
    @SuppressWarnings("unchecked")
	public List<T> subList(int fromIndex, int toIndex)
	{
		try
		{
			return sEmptyList.subList(fromIndex, toIndex);
		}
		catch (IndexOutOfBoundsException ex)
		{
			throw new SpcfIndexOutOfBoundsException(ex);
		}
	}
	
	/**
	 * implement List.size()
	 * @return the length of the list
	 */
	public int size()
	{
		return 0;
	}
	
	/**
	 * @return element of type T always returns null
	 * @param index location to set element
	 * @param element element to set into array
	 */
    @SuppressWarnings("unchecked")
	public T set(int index, T element)
	{
		// Do nothing.
		return null;
	}
	
	/**
	 * do nothing and return false
	 * @param c ignore parameter
	 * @return boolean value will always be false
	 */
    @SuppressWarnings("unchecked")
	public boolean retainAll(Collection<?> c)
	{
    	// do nothing and return false
		return false;
	}
	
	/**
	 * Do nothing and return false.
	 * @param c ignore the parameter
	 * @return boolean value
	 */
    @SuppressWarnings("unchecked")
	public boolean removeAll(Collection<?> c)
	{
		return false;
	}
	
	/**
	 * Do nothing and return null
	 * @param index ignore this parameter
	 * @return object of type T
	 */
    @SuppressWarnings("unchecked")
	public T remove(int index)
	{
		return null;
	}
	
	/** 
	 * returns null
	 * @param index
	 * @return a ListIterator for type T
	 */
    @SuppressWarnings("unchecked")
	public ListIterator<T> listIterator(int index)
	{
       	// TODO should I implement ListIterator<T> for SpcfEmptyIteratorImpl
    	// and return an instance of that?
		// TODO catch java exceptions
		return null;
	}
	
	/** 
	 * returns null
	 * @return a ListIterator for type T
	 */
    @SuppressWarnings("unchecked")
	public ListIterator<T> listIterator()
	{
    	// TODO should I implement ListIterator<T> for SpcfEmptyIteratorImpl
    	// and return an instance of that?
		// TODO catch java exceptions
		return null;
	}
	
	/** 
	 * Returns -1 indicating that the item was not found
	 * @param o Ignored
	 * @return index always -1 since the item is never found
	 */
	@Override
	public int lastIndexOf(Object o)
	{
		return -1;
	}
	
	/** 
	 * returns null
	 * @param index index of element to return
	 * @return always returns null
	 */
    @SuppressWarnings("unchecked")
	public T get(int index)
	{
		return null;
	}
	
	/** 
	 * does nothing 
	 * @param c collection to match against
	 * @return boolean value is always false
	 */
    @SuppressWarnings("unchecked")
	public boolean containsAll(Collection<?> c)
	{
		return false;
	}
	
	/** 
	 * does nothing
	 * @param c collection to add
	 * @return boolean value is always false
	 */
    @SuppressWarnings("unchecked")
	public boolean addAll(Collection<? extends T> c)
	{
		return false;
	}
	
	/** 
	 * does nothing
	 * @param c collection to add
	 * @param index index where to add collection
	 * @return boolean value is always false
	 */
    @SuppressWarnings("unchecked")
	public boolean addAll(int index, Collection<? extends T> c)
	{
		return false;
	}

	/** 
	 * Returns -1 indicating that the item was not found
	 * @param o object to look for
	 * @return index return value is always -1
	 */
	@Override
	public int indexOf(Object o)
	{
		return -1;
	}
	
	/**
	 * Does nothing.  False returned.
	 * @param o Ignored
	 * @return false
	 */
	@Override
	public boolean remove(Object o)
	{
		return false;
	}

	/**
	 * Does nothing.  False returned
	 * @param o Ignored
	 * @return false
	 */
	@Override
	public boolean contains(Object o)
	{
		return false;
	}
	
	
}
