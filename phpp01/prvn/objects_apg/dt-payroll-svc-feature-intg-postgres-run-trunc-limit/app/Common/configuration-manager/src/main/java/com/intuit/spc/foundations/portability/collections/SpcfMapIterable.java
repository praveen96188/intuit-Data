/*
 * author		barunachalam
 * 
 */
package com.intuit.spc.foundations.portability.collections;

import java.util.Iterator;
import com.intuit.spc.foundations.portability.annotations.SpcfNonPortableClass;

/**
 * An abstract class from which implements the Iterable interface for portable collections.
 */
@SpcfNonPortableClass
public abstract class SpcfMapIterable<K, V>  implements Iterable<SpcfPair<K, V>>, ISpcfMap
{

	/**
	 * Returns a portable iterator over the elements contained in this map.
	 *
	 * @return an ISpcfIterator over the elements contained in this map.
	 * @see ISpcfIterator
	 */
	public abstract ISpcfIterator<SpcfPair<K, V>> getIterator();
	
	/**
	 * Returns a platform specific Iterator for iterating over the elements in this map.
	 * The order of element return is map specific.
	 * 
	 * Using this iterator is portable.

	 * @return an Iterator<T> over the elements contained in this map.
	 */
	public Iterator<SpcfPair<K, V>> iterator()
	{
		return getIterator();
	}

}
