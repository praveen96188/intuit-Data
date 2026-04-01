package com.intuit.spc.foundations.portabilitySpecific.collections;

import com.intuit.spc.foundations.portability.SpcfClassCastException;
import com.intuit.spc.foundations.portability.collections.SpcfCollection;
import com.intuit.spc.foundations.portability.SpcfParamValidator;

/**
 * Utility class to convert an SpcfCollection to a fixed array. This class is
 * not intended to be used directly by client code.
 */
public class SpcfToArrayImpl {

	@SuppressWarnings("unchecked")
	private static <T> T[] makeGenericArray(Class<T> tClass, int size)
	{
		SpcfParamValidator.checkIsNotNull(tClass, "Class Argument");
		return (T[])java.lang.reflect.Array.newInstance(tClass, size);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#toArray()
	 */
	public static <T> Object[] toArray(SpcfCollection<T> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "Collection Argument");

		Object[] retArray = null;

		int size = c.getSize();
		if (size == 0)
		{
			return new Object[0];
		}
		else
		{		
    		retArray = new Object[size];
	        int idx = 0;
			for(T obj : c)
			{
	        	retArray[idx++] = obj;
			}
		} 		
		
        return retArray;
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#toArray()
	 */
	@SuppressWarnings("unchecked")
	public static <E, T> E[] toArray(E[] a, SpcfCollection<T> c)
	{	
		SpcfParamValidator.checkIsNotNull(a, "Array Argument");
		SpcfParamValidator.checkIsNotNull(c, "Collection Argument");
		
		try
		{
			int size = c.getSize();
			if (a.length < size)
			{
				a = (E[]) makeGenericArray(a.getClass().getComponentType(), size);
			}

			int index = 0;
			for(T obj : c)
			{
	            a[index] = (E)obj;
	            ++index;
			}

			// Store nulls in the rest of the cells
			while(index < a.length)
			{
				a[index] = null;
	            ++index;
			}
		}
		catch(ClassCastException e)
		{
			throw new SpcfClassCastException(e);
		}
		catch(ArrayStoreException ase)
		{
			throw new SpcfClassCastException(ase);			
		}
	
        return a;
	}	
}
