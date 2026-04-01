/*
 * author		LGM
 * department	SPC Foundations
 * project	    Portability
 * 2004-10-01   Initial Implementation
 */
package com.intuit.spc.foundations.portability.collections;

import java.util.Comparator;

/**
 * A portable interface for a comparison function, which imposes a
 * total ordering on some collection of objects.
 *
 * @see java.util.Comparator
 */
public interface ISpcfComparator<T> extends Comparator<T>
{
	/**
	 * Compares its two arguments for order.
	 *
	 * @return a negative integer, zero or a positive integer as the
	 * first argument is less than, equal to, or greater than the second.
	 */
	int compare(T o1, T o2);

}
