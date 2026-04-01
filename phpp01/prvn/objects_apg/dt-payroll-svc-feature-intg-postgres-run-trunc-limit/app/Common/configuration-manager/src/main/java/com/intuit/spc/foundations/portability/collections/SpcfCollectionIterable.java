/*
 * author		LGM
 * department	SPC Foundations
 * project	    Portability
 * 2006-05-10   Initial Implementation
 */
package com.intuit.spc.foundations.portability.collections;

import java.util.Iterator;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.annotations.SpcfNonPortableClass;

/**
 * An abstract class from which implements the Iterable interface for portable collections.
 */
@SpcfNonPortableClass
public abstract class SpcfCollectionIterable<T>  implements Iterable<T>
{

	/**
	 * Returns a portable iterator over the elements contained in this collection.
	 *
	 * @return an ISpcfIterator over the elements contained in this collection.
	 * @see ISpcfIterator
	 */
	public abstract ISpcfIterator<T> getIterator();
	
	/**
	 * Returns a platform specific Iterator for iterating over the elements in this collection.
	 * The order of element return is collection specific.
	 * 
	 * Using this iterator is portable.

	 * @return an Iterator<T> over the elements contained in this collection.
	 */
	public Iterator<T> iterator()
	{
		return getIterator();
	}

	
	/**
	 * Creates a string representation of this collection and its contents.
	 * 
	 * @return a string representing the contents of this collection.
	 */
	@Override
	public String toString()
	{
		SpcfStringBuilder buf = SpcfFactory.getInstance().createStringBuilder();
		buf.append("[");
		Iterator<T> i = iterator();
		boolean hasNext = i.hasNext();
		while (hasNext) 
		{
			T o = i.next();
			buf.append(o == this ? "(this Collection)" : (o == null ? "null" : o.toString()));
			hasNext = i.hasNext();
			if (hasNext) buf.append(", ");
		}
		
		buf.append("]");
		return buf.toString();
	}
}
