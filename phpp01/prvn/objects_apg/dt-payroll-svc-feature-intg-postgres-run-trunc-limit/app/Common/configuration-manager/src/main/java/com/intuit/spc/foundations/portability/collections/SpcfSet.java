/*
 * author		LGM
 * department	SPC Foundations
 * project	    Portability
 * 2005-03-01   Initial Implementation
 */
package com.intuit.spc.foundations.portability.collections;

/**
 * A resizable-array implementation of the SpcfCollection&lt;T&gt;.
 * Duplicates are not permitted.
 */

public abstract class SpcfSet<T> extends SpcfCollection<T> implements ISpcfSet
{
	private static final long serialVersionUID = -3367851758597430462L;

	/**
	 * Compares the specified object with this SpcfSet for equality. 
	 * Returns true if the specified object is also a SpcfSet, 
	 * the two SpcfSet have the same size, 
	 * and every member of the specified SpcfSet is contained in this SpcfSet 
	 * (or equivalently, every member of this SpcfSet is contained in the 
	 * specified SpcfSet). This definition ensures that the equals method 
	 * works properly across different implementations of the SpcfSet interface.
	 * @param o Object to be compared for equality with this SpcfSet.
	 * @return  true if the objects are the same; false otherwise.
	 */
	public boolean equals(Object o)
	{
		// not equal if object parameter is null
		if (o == null) return false;
		
		// not equal if object parameter is not SpcfSet
		if (!(o instanceof ISpcfSet)) return false;

		// Cast to SpcfSet:
		ISpcfSet inSet = (ISpcfSet) o;

		// If we were handed a pointer to ourselves, then we are the same:
		if (this == inSet) return true;

		// not equal if number of members are not the same
		if (getSize() != inSet.getSize()) return false;

		// iterate through the SpcfSet and make sure the parameter SpcfSet
		// contains the same elements
		ISpcfIterator<T> it = this.getIterator();
		try
		{
			while(it.hasNext())
			{
				if (!inSet.containsObject(it.next()))
				{
					return false;
				}
			}
		}
		catch (SpcfNoSuchElementException e)
		{
			e.toString();
			return false;
		}
		catch (SpcfConcurrentModificationException e)
		{
			e.toString();
			return false;
		}

		return true;
	}
	
	/**
	 * Returns the hash code for the current SpcfSet class.  If objects are 
	 * determined to be equal according to the equals method, then the hash code
	 * should be identical for equal objects.  Objects having the same hash code
	 * are not guaranteed to be equal however.
	 * @return The int hash code for the current object.
	 */
	public int hashCode()
	{
		int hash = 0;
		ISpcfIterator<T> it = this.getIterator();

		while(it.hasNext())
		{
			hash = hash + it.next().hashCode();
		}

		return hash;
	}
	
	/**
	  * Returns true if this set contains the specified object.
	  *
	  * @param obj Item of type object whose presence in this set is to be tested.
	  * @return true if this set contains the specified item.
	  */
	@SuppressWarnings("unchecked")
	public boolean containsObject(Object obj)
	{
		try
		{
		    return this.contains((T)obj);
		} catch (ClassCastException e) {
			e.toString();
			return false;
		}
	}
}
