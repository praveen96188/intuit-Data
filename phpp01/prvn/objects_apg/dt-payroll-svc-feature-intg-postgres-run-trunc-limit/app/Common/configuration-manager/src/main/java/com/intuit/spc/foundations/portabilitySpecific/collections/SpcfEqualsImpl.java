package com.intuit.spc.foundations.portabilitySpecific.collections;

import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfCollection;
import com.intuit.spc.foundations.portability.collections.SpcfConcurrentModificationException;
import com.intuit.spc.foundations.portability.collections.SpcfNoSuchElementException;

/**
 * Internal utility class for comparing SpcfCollections for inorder equality. 
 * This class is not intended to be directly used by client code.
 */
public class SpcfEqualsImpl {
	/**
	 * Compares the two SpcfCollection<T> for equality. 
	 * Returns true if and only both SpcfCollection<T> have the runtime raw type, same size, 
	 * and all corresponding pairs of elements in the two SpcfCollection<T> are equal. 
	 * (Two elements e1 and e2 are equal if (e1==null ? e2==null : e1.equals(e2)).) 
	 * In other words, two SpcfCollection<T> are defined to be equal if they contain 
	 * the same elements in the same order. This definition ensures that the equals method 
	 * works properly across different implementations of SpcfCollection<T>.
	 * @param lhsColl SpcfCollection<T> to be compared for equality with SpcfCollection<T> rhsColl.
	 * @param rhsColl SpcfCollection<T> to be compared for equality with SpcfCollection<T> lhsColl.
	 * @return  true if the objects are the same; false otherwise.
	 */
	public static <T> boolean inOrderEquals(SpcfCollection<T> lhsColl, SpcfCollection<T> rhsColl)
	{
		// both null, they are equal
		if ((lhsColl == null) && (rhsColl == null))
		{
			return true;
		}
		
		// One or other is null, not equal
		if ((lhsColl == null) || (rhsColl == null))
		{
			return false;
		}
		
		// Must be the same runtime type
		if (!lhsColl.getClass().equals(rhsColl.getClass()))
		{
			return false;
		}

		// Not equal if number of members are not the same
		if (lhsColl.getSize() != rhsColl.getSize())
		{
			return false;
		}

		try
		{
			// iterate through the SpcfCollection and make sure the parameter SpcfCollection
			// contains the same elements
			ISpcfIterator<T> lhsIt = lhsColl.getIterator();
			ISpcfIterator<T> rhsIt = rhsColl.getIterator();
			
			// Equality includes order
			while(lhsIt.hasNext())
			{
				if (!rhsIt.hasNext()) 
				{
					return false;
				}
				
				T lhs = lhsIt.next();
				T rhs = rhsIt.next();
				
				if (!lhs.equals(rhs))
				{
					return false;
				}
			}
		}
		catch (SpcfNoSuchElementException e)
		{
			return false;
		}
		catch (SpcfConcurrentModificationException e)
		{
			return false;
		}

		return true;	
	}
}
