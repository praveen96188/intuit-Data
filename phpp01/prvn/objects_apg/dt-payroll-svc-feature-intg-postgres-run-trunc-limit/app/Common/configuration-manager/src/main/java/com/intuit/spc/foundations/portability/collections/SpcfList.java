/*
 * author		RGS
 * department	SPC Foundations
 * project	    Portability
 * 2004-05-03   Initial Implementation
 */
package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.SpcfIndexOutOfBoundsException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.util.SpcfMath;

/**
 * An array abstract class of the SpcfCollection&lt;T&gt;. Positional access is allowed, and
 * null elements and duplicates are generally permitted.
 */
public abstract class SpcfList<T> extends SpcfCollection<T> implements ISpcfList
{
	/**
	 * Value used only on java side for serialization.
	 */
	private static final long serialVersionUID = -6398350187140122489L;
	
	/**
	 * Value used to calculate the hash code.
	 */
	private final int HashConst = 31;
	
	/**
	 * Inserts the specified element at the specified position in this list and
	 * shifts the element currently at that position (if any) and any subsequent
	 * elements to the right (adds one to their indices).
	 *
	 * @param index index at which the specified element is to be inserted.
	 * @param obj element to be inserted
	 * @throws SpcfUnsupportedOperationException - if the add method is not supported by this list.
	 * @throws SpcfIndexOutOfBoundsException - index is out of range (index &lt; 0 || index &gt; size()).
	 */
	public abstract void add(int index, T obj);

	/**
	 * Inserts the specified elements at the specified position in this list and
	 * shifts the elements currently at that position (if any) and any subsequent
	 * elements to the right (adds one to their indices).
	 *
	 * @param index index at which the specified elements are to be inserted.
	 * @param collection The collection of elements to be inserted
	 * @throws SpcfUnsupportedOperationException - if the add method is not supported by this list.
	 * @throws SpcfIndexOutOfBoundsException - index is out of range (index &lt; 0 || index &gt; size()).
	 */
	public void addRange(int index, SpcfCollection<T> collection)
	{
		SpcfParamValidator.checkIsNotNull(collection, "Collection Parameter");
		
		int count = index;
	    ISpcfIterator<T> it = collection.getIterator();
	    
	    while(it.hasNext())
	    {
	    	this.add(count, it.next());
	    	count++;
	    }
	}
	
	/**
	 * Returns the element at the specified position in this list.
	 *
	 * @param index index of element to return.
	 * @return the element at the specified position in this list.
	 * @throws SpcfIndexOutOfBoundsException - if the given index is out of range (index &lt; 0 || index &gt;= size()).
	 */
	public abstract T getItem(int index);

	/**
	 * Returns the element at the specified position in this list as an Object.
	 *
	 * @param index index of element to return.
	 * @return the object element at the specified position in this list.
	 * @throws SpcfIndexOutOfBoundsException - if the given index is out of range (index &lt; 0 || index &gt;= size()).
	 */
	public Object getItemAsObject(int index)
	{
		return getItem(index);
	}
	
	/**
	 * Searches for the first occurence of the given argument, testing
	 * for equality using the equals method.
	 *
	 * @param   obj an object of type T.
	 * @return  the index of the first occurrence of the argument in this
	 *          list; returns -1 if the object is not found.
	 */
	public abstract int indexOf(T obj);

	/**
	 * Searches for the last occurence of the given argument, testing
	 * for equality using the equals method.
	 *
	 * @param   obj an object of type T.
	 * @return  the index of the last occurrence of the argument in this
	 *          list; returns -1 if the object is not found.
	 */
	public int lastIndexOf(T obj)
	{
		Object[] arr = this.toArray();
		
		for(int i = this.getSize() - 1; i >= 0; i--)
		{
			if (obj == null)
			{
			    if (arr[i] == null) return i;	
			} 
			else 
			{
				if (obj.equals(arr[i])) return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Removes the element at the specified position in this list.
	 * Shifts any subsequent elements to the left (subtracts one from their
	 * indices).
	 *
	 * @param index the index of the element to removed.
	 * @throws SpcfIndexOutOfBoundsException if index out of range.
	 * @throws SpcfUnsupportedOperationException if this list is read-only
	 */
	public abstract void removeAt(int index);
	
	/**
	 * Removes the elements in the specified range from this list.
	 * Shifts any subsequent elements to the left (subtracts one from their
	 * indices).
	 *
	 * @param startIndex The index of the first element to removed.
	 * @param endIndex The index of the last element to be removed.
	 * @throws SpcfIndexOutOfBoundsException if index out of range.
	 * @throws SpcfUnsupportedOperationException if this list is read-only
	 */
	public void removeRange(int startIndex, int endIndex)
	{
		if ((endIndex >= this.getSize()) || (endIndex < 0))
		{
			throw new SpcfIndexOutOfBoundsException("SpcfList.removeRange(): endIndex");
		}
		
		if ((startIndex >= this.getSize()) || (startIndex < 0))
		{
			throw new SpcfIndexOutOfBoundsException("SpcfList.removeRange(): startIndex");
		}
		
		if (startIndex > endIndex) 		
		{
			throw new SpcfIndexOutOfBoundsException(
					"SpcfList.removeRange(): startIndex greater than endIndex");
		}
		
		for(int i = 0; i <= endIndex - startIndex; i++)
		{
			this.removeAt(startIndex);
		}
	}

	/**
	 * Replaces the element at the specified position in this list with
	 * the specified element.
	 *
	 * @param index index of element to replace.
	 * @param obj element to be stored at the specified position.
	 * @throws SpcfIndexOutOfBoundsException if index out of range
	 * @throws SpcfUnsupportedOperationException if this list is read-only
	 */
	public abstract void setItem(int index, T obj);
	
	/**
	 * Replaces the elements at the specified position in this list with
	 * the specified elements.
	 *
	 * @param index index of element to replace.
	 * @param collection Collection of elements to be stored at the specified position.
	 * @throws SpcfIndexOutOfBoundsException if index out of range
	 * @throws SpcfUnsupportedOperationException if this list is read-only
	 */
	public void setRange(int index, SpcfCollection<T> collection)
	{
		SpcfParamValidator.checkIsNotNull(collection, "Collection Parameter");
		
		if ((index + collection.getSize() > this.getSize()) || (index < 0)) 
		{
			throw new SpcfIndexOutOfBoundsException("SpcfList.setRange(): index");
		}
		
		int count = index;
	    ISpcfIterator<T> it = collection.getIterator();
	    
	    while(it.hasNext())
	    {
	    	this.setItem(count, it.next());
	    	count++;
	    }
	}
	
	/**
	 * Updates the current list, reversing the elements that it contains.  As a result
	 * the first element prior to the execution of this operation will become the
	 * last element in the list, and vice versa.
	 *
	 */
	public void reverse()
	{
		SpcfCollection<T> col = SpcfFactory.getInstance().<T>createArrayList();
		
		for(int i = this.getSize() - 1; i >= 0; i--)
		{
		    col.add(this.getItem(i));
		    this.removeAt(i);
		}
		
		ISpcfIterator<T> it = col.getIterator();
		while (it.hasNext())
		{
			this.add(it.next());
		}
	}

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
	@Override
	public boolean equals(Object o)
	{
		// If we were handed a pointer to ourselves, then we are the same:
		if (this == o) return true;
		
		// not equal if object parameter is null
		if (o == null)
		{
			return false;
		}
		
		// not equal if object parameter is not SpcfList
		if (!(o instanceof ISpcfList))
		{
			return false;
		}
		
		ISpcfList inList = (ISpcfList) o;

		// not equal if number of members are not the same
		if (getSize() != inList.getSize())
		{
			return false;
		}

		// iterate through the SpcfList and make sure the parameter SpcfList
		// contains the same elements in the same order
		try
		{
			for(int i = 0; i < getSize(); i++)
			{
				T item1 = getItem(i);
				Object item2 = inList.getItemAsObject(i);
				if (item1 == null && item2 != null) return false;
				if (item1 != null && !item1.equals(item2)) return false;
			}
		}
		catch (SpcfIndexOutOfBoundsException e)
		{
			e.toString();
			return false;
		}

		return true;
	}
	
	/**
	 * Returns the hash code for the current SpcfList class.  If objects are 
	 * determined to be equal according to the equals method, then the hash code
	 * should be identical for equal objects.  Objects having the same hash code
	 * are not guaranteed to be equal however.
	 * @return The int hash code for the current object.
	 */
	@Override
	public int hashCode()
	{
		int hash = 0;
		for(int i = 0; i < getSize(); i++)
		{
			hash = (hash * HashConst) + getItem(i).hashCode();
		}
		return hash;
	}
	
	/**
	 * Shuffles the list so that the contents are placed in a pseudo-random order.
	 * The algorithm iterates through the list of elements, and swaps each selected 
	 * element with either itself, or another element contained later in the list. 
	 */
	public void shuffle()
	{
		for (int i = 0; i < this.getSize(); i++)
		{
			this.swap(i, SpcfMath.randomInt(i, this.getSize()));
		}
	}
	
	/**
	 * Swaps the two elements contained at the given indices.
	 * @param index1 The index of the first element to be swapped.
	 * @param index2 The index of the second element to be swapped.
	 * @throws SpcfIndexOutOfBoundsException if an index is less than 0 or greater than array length.
	 * @throws SpcfIllegalArgumentException if index1 is greater than index2.  
	 */
	public void swap(int index1, int index2)
	{
		// Ensure the indices are within the appropriate range.
		if ((index1 < 0) || (index1 >= this.getSize())) 
			throw new SpcfIndexOutOfBoundsException("SpcfList swap index out of bounds.");
		if ((index2 < 0) || (index2 >= this.getSize())) 
			throw new SpcfIndexOutOfBoundsException("SpcfList swap index out of bounds.");
		
		if (index1 > index2) throw new SpcfIllegalArgumentException("SpcfList swap index1 > index2.");
		
		// If we are asked to swap an item with itself, just return.
		if (index1 == index2) return;
		
		T temp = null;
	    temp = this.getItem(index1);
	    this.removeAt(index1);
	    this.add(index2 - 1, temp);
	    
	    temp = this.getItem(index2);
	    this.removeAt(index2);
	    this.add(index1, temp);
	}
}
