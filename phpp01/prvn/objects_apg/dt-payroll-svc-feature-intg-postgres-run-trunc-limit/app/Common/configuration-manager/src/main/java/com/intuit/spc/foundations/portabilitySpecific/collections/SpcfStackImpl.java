/*
 * author		Manoj Garg
 * department	SPC Foundations
 * project	    Portability
 * 2005-11-03   Initial Implementation
 *
 * NOTE: All third-party runtime exceptions are caught and re-thrown as
 * portable exceptions
 */
package com.intuit.spc.foundations.portabilitySpecific.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import com.intuit.spc.foundations.portability.SpcfClassCastException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfIndexOutOfBoundsException;
import com.intuit.spc.foundations.portability.SpcfInvalidOperationException;
import com.intuit.spc.foundations.portability.SpcfNullPointerException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfRuntimeException;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfCollection;
import com.intuit.spc.foundations.portability.collections.SpcfConcurrentModificationException;
import com.intuit.spc.foundations.portability.collections.SpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfNoSuchElementException;
import com.intuit.spc.foundations.portability.collections.SpcfStack;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * A platform specific implementation of SpcfStack
 */
public class SpcfStackImpl<T> extends SpcfStack<T> implements Cloneable, List<T>
{

	/**
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = -2376814992426390160L;

	//	Member Variables

	/**
	 * The encapsulated third party runtime Stack implementation
	 */
	protected ArrayList<T> mThirdPartyStack;

	/**
     * The number of times this stack has been <i>structurally modified</i>.
     * Structural modifications are those that change the size of the
     * stack, or otherwise perturb it in such a fashion that iterations in
     * progress may yield incorrect results.<p>
     *
     * This field is used by the stack iterator implementation returned by the
     * <tt>getIterator</tt> method. If the value of this field changes unexpectedly,
     * the iterator will throw a <tt>SpcfConcurrentModificationException</tt> in
     * response to the <tt>next</tt> or <tt>hasNext</tt> operations. This provides
     * <i>fail-fast</i> behavior, rather than non-deterministic behavior in
     * the face of concurrent modification during iteration.<p>
     *
     * <b>Use of this field by subclasses is optional.</b> If a subclass
     * wishes to provide fail-fast iterators, then it merely has to increment this
     * field in its <tt>push(Object)</tt>, <tt>clear()</tt> and <tt>pop()</tt> methods
     * (and any other methods that it overrides that result in structural modifications
     * to the stack).  A single call to <tt>push(Object)</tt> or <tt>pop()</tt> must add
     * no more than one to this field. If an implementation does not wish to provide fail-fast
     * iterators, this field may be ignored.
     */
	protected int mVersion;

	//	Constructors
	/**
	 * Constructs an empty stack with an initial capacity of ten.
	 */
	public SpcfStackImpl()
	{
		mThirdPartyStack = new ArrayList<T>(10);
		mVersion = 0;
	}
	
	/**
	 * Constructs an empty stack with an initial capacity of ten.
	 * @param typeParam the type parameter for this generic type
	 */
	public SpcfStackImpl(SpcfClass typeParam)
	{
		mThirdPartyStack = new ArrayList<T>(10);
		mVersion = 0;
		this.mTypeParameters = new SpcfClass[] { typeParam };
	}

	/**
	 * Initializes a new instance of the Stack class that is empty and has the specified
	 * initial capacity.
	 * @param  initialCapacity the initial capacity of the stack.
	 * @throws SpcfIllegalArgumentException if the specified initial capacity
	 *         is negative.
	 */
	public SpcfStackImpl(int initialCapacity)
	{
		try
		{
			mThirdPartyStack = new ArrayList<T>(initialCapacity);
		}
		catch(IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}
	
	/**
	 * Initializes a new instance of the Stack class that is empty and has the specified
	 * initial capacity.
	 * @param  initialCapacity the initial capacity of the stack.
	 * @param typeParam the type parameter for this generic type
	 * @throws SpcfIllegalArgumentException if the specified initial capacity
	 *         is negative.
	 */
	public SpcfStackImpl(int initialCapacity, SpcfClass typeParam)
	{
		try
		{
			this.mTypeParameters = new SpcfClass[] { typeParam };
			mThirdPartyStack = new ArrayList<T>(initialCapacity);
		}
		catch(IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}

	/**
	 * Constructs a Stack with the specified third party runtime object.
	 * This constructor allows the user to encapsulate any object that
	 * is of type the java.util.ArrayList class.
	 *
	 * @param  thirdPartyStack java.util.ArrayList
	 * @throws SpcfNullPointerException if thirdPartyStack is null
	 */
	public SpcfStackImpl(ArrayList<T> thirdPartyStack)
	{
		SpcfParamValidator.checkIsNotNull(thirdPartyStack, "thirdPartyStack");
		mThirdPartyStack = thirdPartyStack;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfStack#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public SpcfStack<T> clone()
	{
		// Causes type-check warning
		ArrayList<T> thirdPartyStack = (ArrayList<T>) mThirdPartyStack.clone();
		SpcfStackImpl<T> spcfStackImpl = new SpcfStackImpl<T>(thirdPartyStack);
		spcfStackImpl.mVersion = mVersion;
		return spcfStackImpl;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfStack#empty()
	 */
	@Override
	public boolean empty()
	{
		return mThirdPartyStack.isEmpty();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfStack#peek()
	 */
	@Override
	public T peek()
	{
		if(mThirdPartyStack.isEmpty())
		{
			throw new SpcfInvalidOperationException("Stack is empty!");
		}
		return mThirdPartyStack.get(mThirdPartyStack.size() - 1);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfStack#pop()
	 */
	@Override
	public T pop()
	{
		if(mThirdPartyStack.isEmpty())
		{
			throw new SpcfInvalidOperationException("Stack is empty!");
		}

		try
		{
			T objRet = mThirdPartyStack.remove(mThirdPartyStack.size() - 1);
			mVersion++;
			return objRet;
		}
		catch (UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfStack#push
	 */
	@Override
	public void push(T item)
	{
		try
		{
			mThirdPartyStack.add(item);
			mVersion++;
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#clear()
	 */
	@Override
	public void clear()
	{
		try
		{
			mThirdPartyStack.clear();
			mVersion++;
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#contains
	 */
	@Override
	public boolean contains(Object obj)
	{
		return mThirdPartyStack.contains(obj);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#getSize()
	 */
	@Override
	public int getSize()
	{
		return mThirdPartyStack.size();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#getIterator()
	 */
	@Override
	@SuppressWarnings("all")
	public ISpcfIterator<T> getIterator()
	{
		return new SpcfStackIterator();
	}

	/**
	 * @see com.intuit.spc.foundations.portabilitySpecific.collections.SpcfEqualsImpl#inOrderEquals
	 * (SpcfCollection, SpcfCollection)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o)
	{
		// Got to check raw type here since inOrderEquals needs collections.
		if (!(o instanceof SpcfStackImpl)) return false;

		// Cast to SpcfStackImpl:
		SpcfStackImpl inStack = (SpcfStackImpl)o;
		
		// If we were handed a pointer to ourselves, then we are the same:
		if (this == inStack) return true;

		// Use inOrderEquals to check:
		return SpcfEqualsImpl.inOrderEquals(this, inStack);
	}

	/**
	 * Returns the encapsulated third party runtime object
	 * @return a java.util.ArrayList implementation
	 */
	public ArrayList<T> toSpecific()
	{
		return mThirdPartyStack;
	}

    /**
     * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#toArray()
     */
	@Override
    public Object[] toArray() {
		// the following does not give the same order as the .net did
		// so we are going back to the SpcfToArrayImpl
		//return mThirdPartyStack.toArray();
        return SpcfToArrayImpl.toArray(this);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#toArray()
     */
	@Override
    public <E> E[] toArray(E[] a) {
		SpcfParamValidator.checkIsNotNull(a, "Array Argument");
		try
		{
			// the following does not give the same order as the .net did
			// so we are going back to the SpcfToArrayImpl
			//return mThirdPartyStack.toArray(a);
	        return SpcfToArrayImpl.toArray(a, this);
		}
		catch(ClassCastException e)
		{
			throw new SpcfClassCastException(e);
		}
		catch(ArrayStoreException ase)
		{
			throw new SpcfClassCastException(ase);			
		}
    }
    
	/**
	 * implements java.util.List&lt;T&gt;.add(int, T)
	 * @param index index at which the specified element is to be inserted.
	 * @param obj element to be inserted.
	 */
	public void add(int index, T obj)
	{
		try
		{
			mThirdPartyStack.add(index, obj);
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
			return mThirdPartyStack.addAll(c);
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
			return mThirdPartyStack.addAll(index, c);
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

		return mThirdPartyStack.containsAll(c);
	}
	
	/** 
	 * @param index index of element to return
	 * @return element of type T
	 */
	public T get(int index)
	{
		try
		{
			return mThirdPartyStack.get(index);
		}
		catch(IndexOutOfBoundsException e)
		{
			//invalid index
			throw new SpcfIndexOutOfBoundsException(e);
		}
	}
	
	/**
	 * implements java.util.List&lt;T&gt;.indexOf(Object)
	 * @param o an object.
	 * @return the index of the first occurrence of the argument in this vector, that is, 
	 * the smallest value k such that elem.equals(elementData[k]) is true; returns -1 if 
	 * the object is not found.
	 */
	public int indexOf(Object o)
	{
		return mThirdPartyStack.indexOf(o);
	}
	

	/** 
	 * Returns the index of the last occurrence of the specified object in this list.
	 * @param o object to look for
	 * @return the index of the last occurrence of the specified 
	 * object in this list; returns -1 if the object is not found.
	 */
	public int lastIndexOf(Object o)
	{
		return mThirdPartyStack.lastIndexOf(o);
	}
	
	/** 
	 * Returns a list iterator of the elements in this list (in proper sequence).
	 * @return a list iterator of the elements in this list (in proper sequence).
	 */
	public ListIterator<T> listIterator()
	{
		return mThirdPartyStack.listIterator();
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
			return mThirdPartyStack.listIterator(index);
		}
		catch(IndexOutOfBoundsException e)
		{
			//invalid index
			throw new SpcfIndexOutOfBoundsException(e);
		}
	}
	
	/**
     * The method is not supported.
	 * @param index - the index of the element to removed
	 * @return object of type T, the element that was removed from the list.
     * @throws SpcfUnsupportedOperationException if the method is called.
	 */
	public T remove(int index)
	{
        throw new SpcfUnsupportedOperationException();
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
     * The method is not supported.
	 * @param c - elements to be removed from this collection
	 * @return boolean value, true if this collection changed as a result of the call.
     * @throws SpcfUnsupportedOperationException if the method is called.
	 */
	public boolean removeAll(Collection<?> c)
	{
        throw new SpcfUnsupportedOperationException();
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
			return mThirdPartyStack.retainAll(c);
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
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
			return mThirdPartyStack.set(index, element);
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
	 * implement List.size()
	 * @return the length of the list
	 */
	public int size()
	{
		return getSize();
	}
	
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
			return mThirdPartyStack.subList(fromIndex, toIndex);
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
	 * This is a private implementation of the SpcfIterator to provide the functionality
	 * for the stack class iteration
	 */
    private class SpcfStackIterator extends SpcfIterator<T>
    {
        /**
         * Index of element to be returned by subsequent call to next.
         */
        private int mCursor = 0;
        
        /**
         * The mExpectedVersion value that the iterator believes that the backing
         * SpcfStackImpl should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.
         */
        private int mExpectedVersion; 
        
        /**
         * this is the default constructor made private
         */
        private SpcfStackIterator()
        {  
            mExpectedVersion = mVersion;            
        }
        
        /**
         * @see com.intuit.spc.foundations.portability.collections.SpcfIterator#hasNext()
         */ 
    	@Override
        public boolean hasNext() 
        {
            return mCursor != getSize();
        }

        /**
         * @see com.intuit.spc.foundations.portability.collections.SpcfIterator#next()
         */ 
    	@Override
        public T next() 
        {
            checkForComodification();            
            try 
            {
                int idx = getSize() - mCursor - 1; 
                mHasCurrentItem = false;
    			mCurrentItem = mThirdPartyStack.get(idx);
    			mHasCurrentItem = true;
                mCursor++;
                return mCurrentItem;
            } 
            catch(IndexOutOfBoundsException ex) 
            {
                checkForComodification();
                throw new SpcfNoSuchElementException(ex);
            }
        }

        /**
         * @see com.intuit.spc.foundations.portability.collections.SpcfIterator#moveNext()
         */ 
    	@Override
        public boolean moveNext() 
        {
            checkForComodification();
            boolean hasNext = hasNext(); 
            if (hasNext) 
            {
                next();                
            }
            return hasNext;            
        }  
        
        /**
         * @see com.intuit.spc.foundations.portability.collections.SpcfIterator#getCurrent()
         */
    	@Override
        public T getCurrent()
        {
            checkForComodification();            
            try 
            {
                int idx = getSize() - mCursor;            
                T current = mThirdPartyStack.get(idx);               
                return current;
            } 
            catch(IndexOutOfBoundsException ex) 
            {
                checkForComodification();
                throw new SpcfNoSuchElementException(ex);
            }
        } 
        
    	/**
    	 * method to check to see if the stack has been modified
    	 */
        private void checkForComodification() 
        {
            if(mExpectedVersion != mVersion)
            {
                throw new SpcfConcurrentModificationException();
            }
        } 
    }
}
