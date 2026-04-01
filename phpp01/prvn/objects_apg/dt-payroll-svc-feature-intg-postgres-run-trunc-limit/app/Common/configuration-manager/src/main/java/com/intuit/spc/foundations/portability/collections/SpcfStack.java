/*
 * author		Manoj Garg
 * department	SPC Foundations
 * project	    Portability
 * 2005-11-03   Initial Implementation
 */

package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.ISpcfCloneable;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * The Stack class represents a last-in-first-out (LIFO) stack of objects.
 * <p>The usual push and pop operations are provided, as well as a method to peek at the top item on the stack, a
 * method to test for whether the stack is empty. When a stack is first created, it contains no items.</p>
 */
abstract public class SpcfStack<T> extends SpcfCollection<T> implements ISpcfCloneable<SpcfStack<T>>
{
	private static final long serialVersionUID = -7829290061334242L;
	
	/**
	 * Returns a new instance of SpcfStack&lt;T&gt; containing a shallow copy of the encapsulated
	 * 3rd party stack.
	 * <p>A shallow copy of a collection copies only the elements of the collection,
	 * whether they are reference types or value types, but it does not copy the objects
	 * that the references refer to. The references in the new collection point to the
	 * same objects that the references in the original collection point to.</p>
	 * @return A new instance of SpcfStack&lt;T&gt; containing a shallow copy of the encapsulated 3rd party stack.
	 * @throws SpcfUnsupportedOperationException if the clone method is not supported 
     * by this stack.
	 */
	public abstract SpcfStack<T> clone();

	/**
	 * Tests if this stack is empty.
	 * @return true if and only if this stack contains no items; false otherwise.
	 */
	public abstract boolean empty();

	/**
	 * Looks at the object at the top of this stack without removing it from the stack.
	 * <p>This method is similar to the Pop method, but peek does not modify the stack.</p>
	 * @return the object of type T at the top of this stack.
	 * @throws SpcfInvalidOperationException if this stack is empty.
	 */
	public abstract T peek();

	/**
	 * Removes the object at the top of this stack and returns that object as the value of this function.
	 * <p>This method is similar to the Peek method, but unlike Peek this method will modify the Stack.</p>
	 * @return The object of type T at the top of this stack.
	 * @throws SpcfInvalidOperationException if this stack is empty.
	 * @throws SpcfUnsupportedOperationException pop is not supported by
     *         this collection.
	 */
	public abstract T pop();

	/**
	 * Pushes an item of type T onto the top of this stack.
	 * @param item the item of type T to be pushed onto this stack.
	 * @throws SpcfUnsupportedOperationException push is not supported by
     *         this collection.
	 */
	public abstract void push(T item);

    /**
     * This method is equivalenet to push the input object. <p>
     * 
     * This method always returns true.
     * @see com.intuit.spc.foundations.portability.collections.SpcfStack#push(T)
     */
	public boolean add(T obj)
	{
        push(obj);
        return true;
	}

    /**
     * This method is equivalenet to pushing each element in the collection. <p>
     * 
     * This method always returns true.
     * @see com.intuit.spc.foundations.portability.collections.SpcfStack#push(T)
     */
	public boolean addAll(SpcfCollection<T> collection)
	{
        for(T obj : collection)
        {
            push(obj);
        }
        return true;
	}

    /**
     * The method is not supported.
     * @throws SpcfUnsupportedOperationException if the method is called.
     */
    public boolean remove(T obj)
    {
        throw new SpcfUnsupportedOperationException();
    }	
	
	/**
	 * Returns an empty portable stack with an initial capacity of ten.
	 * @return an SpcfStack&lt;T&gt; object
	 */
	public static <S> SpcfStack<S> createInstance()
	{
		return SpcfFactory.getInstance().<S>createStack();
	}
	
	/**
	 * Returns an empty portable stack with an initial capacity of ten.
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfStack&lt;T&gt; object
	 */
	public static <S> SpcfStack<S> createInstance(SpcfClass typeParam)
	{
		return SpcfFactory.getInstance().<S>createStack(typeParam);
	}

	/**
	 * Returns a portable stack.
	 * @param initialCapacity the initial capacity of the stack.
	 * @return an SpcfStack&lt;T&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public static <S> SpcfStack<S> createInstance(int initialCapacity)
	{
		return SpcfFactory.getInstance().<S>createStack(initialCapacity);
	}
	
	/**
	 * Returns a portable stack.
	 * @param initialCapacity the initial capacity of the stack.
	 * @param typeParam the type parameter for this generic type
	 * @return an SpcfStack&lt;T&gt; object
	 * @throws SpcfIllegalArgumentException If the specified initial capacity is negative
	 */
	public static <S> SpcfStack<S> createInstance(int initialCapacity, SpcfClass typeParam)
	{
		return SpcfFactory.getInstance().<S>createStack(initialCapacity, typeParam);
	}
}
