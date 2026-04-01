package com.intuit.spc.foundations.portabilitySpecific.collections;

import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfIndexOutOfBoundsException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.collections.*;

import java.util.Arrays;

/**
 * SpcfArraysUtilImpl provides concrete implementation for SpcfArraysUtil. 
 * Contains common utility methods for manipulating single dimensional fixed arrays.
 */
public strictfp class SpcfArraysUtilImpl extends SpcfArraysUtil {
	
	protected <T> boolean doEquals(T[] thisArray, T[] thatArray)
	{
		// Both null, then equal
		if ((thisArray == null) && (thatArray == null)) return true;
			
		// Here one of them may be null, if true then not equal 
		if ((thisArray == null) || (thatArray == null)) return false;
			
		// Must check this, since we cannot allow parameter covariance
		// for portability reasons.
		// Ensure these arrays are the same runtime type.
		if (!thisArray.getClass().equals(thatArray.getClass())) return false;
		
		// Forward the 
		return Arrays.equals(thisArray, thatArray);		
	}
	
	protected boolean doEquals(long[] a, long[] a2)
	{
		return Arrays.equals(a, a2);
	}
	
	protected boolean doEquals(int[] a, int[] a2)
	{
		return Arrays.equals(a, a2);
	}
	
	protected boolean doEquals(short[] a, short[] a2)
	{
		return Arrays.equals(a, a2);
	}
	
	protected boolean doEquals(char[] a, char[] a2)
	{
		return Arrays.equals(a, a2);
	}
	
	protected boolean doEquals(byte[] a, byte[] a2)
	{
		return Arrays.equals(a, a2);
	}
	
	protected boolean doEquals(boolean[] a, boolean[] a2)
	{
		return Arrays.equals(a, a2);
	}
	
	protected boolean doEquals(double[] a, double[] a2)
	{
		return Arrays.equals(a, a2);
	}
	
	protected boolean doEquals(float[] a, float[] a2)
	{
		return Arrays.equals(a, a2);
	}
	
    protected <T> void doFill(T[] a, int fromIndex, int toIndex, T val)
	{
		fillArgumentSemantics(fromIndex, toIndex, a, a != null ? a.length : 0);
		Arrays.fill(a, fromIndex, toIndex, val);
	}
	
    protected void doFill(long[] a, int fromIndex, int toIndex, long val)
    {
		fillArgumentSemantics(fromIndex, toIndex, a, a != null ? a.length : 0);
		Arrays.fill(a, fromIndex, toIndex, val);   	
   }

    protected void doFill(int[] a, int fromIndex, int toIndex, int val)
    {
		fillArgumentSemantics(fromIndex, toIndex, a, a != null ? a.length : 0);
		Arrays.fill(a, fromIndex, toIndex, val);
   }
    
    protected void doFill(short[] a, int fromIndex, int toIndex, short val)
    {
		fillArgumentSemantics(fromIndex, toIndex, a, a != null ? a.length : 0);
		Arrays.fill(a, fromIndex, toIndex, val);
   }

    protected void doFill(char[] a, int fromIndex, int toIndex, char val)
    {
		fillArgumentSemantics(fromIndex, toIndex, a, a != null ? a.length : 0);
		Arrays.fill(a, fromIndex, toIndex, val);
    }

    protected void doFill(byte[] a, int fromIndex, int toIndex, byte val)
    {
		fillArgumentSemantics(fromIndex, toIndex, a, a != null ? a.length : 0);
		Arrays.fill(a, fromIndex, toIndex, val);
   }

    protected void doFill(boolean[] a, int fromIndex, int toIndex, boolean val)
    {
		fillArgumentSemantics(fromIndex, toIndex, a, a != null ? a.length : 0);
		Arrays.fill(a, fromIndex, toIndex, val);
    }

    protected void doFill(double[] a, int fromIndex, int toIndex, double val)
    {
		fillArgumentSemantics(fromIndex, toIndex, a, a != null ? a.length : 0);
		Arrays.fill(a, fromIndex, toIndex, val);
   }

    protected void doFill(float[] a, int fromIndex, int toIndex, float val)
    {
		fillArgumentSemantics(fromIndex, toIndex, a, a != null ? a.length : 0);
		Arrays.fill(a, fromIndex, toIndex, val);
   }
    
	protected void fillArgumentSemantics(int fromIndex, int toIndex, Object a, int length)
	{
	    // Semantic check
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");
	    if (fromIndex > toIndex) throw new SpcfIllegalArgumentException("fromIndex > toIndex");
	    if (fromIndex < 0) throw new SpcfIndexOutOfBoundsException("fromIndex < 0");
	    if (toIndex > length) throw new SpcfIndexOutOfBoundsException("toIndex > array.Length");
	}
	
	protected <T> void doSort(T[] a)
	{
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		try
		{
		    Arrays.sort(a);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}
	}
	
	protected <T> void doSort(T[] a, ISpcfComparator<T> c)
	{
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");
	    SpcfParamValidator.checkIsNotNull(c, "Comparator Argument");

		try
		{
		    Arrays.sort(a, c);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}
	}
	
    protected void doSort(long[] a)
    {
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		try
		{
		    Arrays.sort(a);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}
    }
    
    protected void doSort(int[] a)
    {
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		try
		{
		    Arrays.sort(a);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}
    }
    
    protected void doSort(short[] a)
    {
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		try
		{
		    Arrays.sort(a);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}
    }
    
    protected void doSort(char[] a)
    {
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		try
		{
		    Arrays.sort(a);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}
    }
    
    protected void doSort(byte[] a)
    {
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		try
		{
		    Arrays.sort(a);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}
    }
       
    protected void doSort(double[] a)
    {
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		try
		{
		    Arrays.sort(a);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}
    }
    
    protected void doSort(float[] a)
    {
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		try
		{
		    Arrays.sort(a);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}
    }
    
	protected <T> int doSearch(T[] a, T obj)
	{
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");
	    
	    // If the object is null, perform a linear search
	    if (obj == null) return linearNullSearch(a);
	    
		int index = -1;
		
		try
		{
		    index = Arrays.binarySearch(a, obj);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}	
		
		return index;
	}
	
	protected <T> int doSearch(T[] a, T obj, ISpcfComparator<T> c)
	{
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");
	    SpcfParamValidator.checkIsNotNull(c, "Comparator Argument");

	    // If the object is null, perform a linear search
	    if (obj == null) return linearNullSearch(a);
	    
		int index = -1;
		
		try
		{
		    index = Arrays.binarySearch(a, obj, c);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}
		
		return index;
	}
	
	protected int doSearch(long[] a, long obj)
	{
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		int index = -1;
		
		try
		{
		    index = Arrays.binarySearch(a, obj);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}	
		
		return index;
	}
	
	protected int doSearch(int[] a, int obj)
	{
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		int index = -1;
		
		try
		{
		    index = Arrays.binarySearch(a, obj);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}	
		
		return index;
	}
	
	protected int doSearch(short[] a, short obj)
	{
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		int index = -1;
		
		try
		{
		    index = Arrays.binarySearch(a, obj);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}	
		
		return index;
	}
	
	protected int doSearch(char[] a, char obj)
	{
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		int index = -1;
		
		try
		{
		    index = Arrays.binarySearch(a, obj);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}	
		
		return index;
	}
	
	protected int doSearch(byte[] a, byte obj)
	{
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		int index = -1;
		
		try
		{
		    index = Arrays.binarySearch(a, obj);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}	
		
		return index;
	}
	
	protected int doSearch(float[] a, float obj)
	{
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		int index = -1;
		
		try
		{
		    index = Arrays.binarySearch(a, obj);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}	
		
		return index;
	}
	
	protected int doSearch(double[] a, double obj)
	{
	    SpcfParamValidator.checkIsNotNull(a, "Array Argument");

		int index = -1;
		
		try
		{
		    index = Arrays.binarySearch(a, obj);
		}
		catch(Exception e)
		{
			throw new SpcfUnsupportedOperationException("exception occurred in sorting.", e);
		}	
		
		return index;
	}

	
	@Override
	protected void doCopy(Object sourceArray, int sourceArrayStartPosition, 
    		Object destinationArray, int destinationArrayStartPosition, int count)
	{
		// Check params:
		SpcfParamValidator.checkIsNotNull(sourceArray, "Source Array");
		SpcfParamValidator.checkIsNotNull(destinationArray, "Destination Array");
		SpcfParamValidator.checkIsNonNegative(sourceArrayStartPosition, "Source Array Start Position");
		SpcfParamValidator.checkIsNonNegative(destinationArrayStartPosition, "Destination Array Start Position");
		SpcfParamValidator.checkIsNonNegative(count, "Count");
		
		try
		{
			System.arraycopy(sourceArray, sourceArrayStartPosition, 
					destinationArray, destinationArrayStartPosition, count);
		}
		catch (ArrayStoreException ase)
		{
			// Either source or destination objects are not arrays.
			throw new SpcfIllegalArgumentException(ase);
		}
		catch (IndexOutOfBoundsException ioobe)
		{
			// Happens if the start position + count exceeds the length of the array
			throw new SpcfIndexOutOfBoundsException(ioobe);
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected <T> T[] doResize(T[] oldArray, int newSize)
	{
		// Check params:
		SpcfParamValidator.checkIsNotNull(oldArray, "Old Array");
		SpcfParamValidator.checkIsNonNegative(newSize, "New Array Size");

        // If the old and new array sizes are the same, just return the old array:
        if (oldArray.length == newSize) return oldArray;

		// Do the resize:
		try
		{
			int oldSize = oldArray.length;
			Class elementType = oldArray.getClass().getComponentType();
			T[] newArray = (T[])(java.lang.reflect.Array.newInstance(elementType, newSize));
			int preserveLength = Math.min(oldSize, newSize);
			if (preserveLength > 0) System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
	   		return newArray;
		}
		catch (ClassCastException cce)
		{
			// If for some reason the cast to T[] fails.
			throw new SpcfIllegalArgumentException(cce);
		}
	}
	
	@Override
	protected long[] doResize(long[] oldArray, int newSize)
	{
		// Check params:
		SpcfParamValidator.checkIsNotNull(oldArray, "Old Array");
		SpcfParamValidator.checkIsNonNegative(newSize, "New Array Size");

        // If the old and new array sizes are the same, just return the old array:
        if (oldArray.length == newSize) return oldArray;

		// Do the resize:
		long[] newArray = new long[newSize];
		int oldSize = oldArray.length;
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0) System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		return newArray;
	}

	@Override
	protected int[] doResize(int[] oldArray, int newSize)
	{
		// Check params:
		SpcfParamValidator.checkIsNotNull(oldArray, "Old Array");
		SpcfParamValidator.checkIsNonNegative(newSize, "New Array Size");

        // If the old and new array sizes are the same, just return the old array:
        if (oldArray.length == newSize) return oldArray;

		// Do the resize:
		int[] newArray = new int[newSize];
		int oldSize = oldArray.length;
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0) System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		return newArray;
	}

	@Override
	protected short[] doResize(short[] oldArray, int newSize)
	{
		// Check params:
		SpcfParamValidator.checkIsNotNull(oldArray, "Old Array");
		SpcfParamValidator.checkIsNonNegative(newSize, "New Array Size");

        // If the old and new array sizes are the same, just return the old array:
        if (oldArray.length == newSize) return oldArray;

		// Do the resize:
		short[] newArray = new short[newSize];
		int oldSize = oldArray.length;
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0) System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		return newArray;
	}

	@Override
	protected char[] doResize(char[] oldArray, int newSize)
	{
		// Check params:
		SpcfParamValidator.checkIsNotNull(oldArray, "Old Array");
		SpcfParamValidator.checkIsNonNegative(newSize, "New Array Size");

        // If the old and new array sizes are the same, just return the old array:
        if (oldArray.length == newSize) return oldArray;

		// Do the resize:
		char[] newArray = new char[newSize];
		int oldSize = oldArray.length;
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0) System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		return newArray;
	}

	@Override
	protected byte[] doResize(byte[] oldArray, int newSize)
	{
		// Check params:
		SpcfParamValidator.checkIsNotNull(oldArray, "Old Array");
		SpcfParamValidator.checkIsNonNegative(newSize, "New Array Size");

        // If the old and new array sizes are the same, just return the old array:
        if (oldArray.length == newSize) return oldArray;

		// Do the resize:
		byte[] newArray = new byte[newSize];
		int oldSize = oldArray.length;
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0) System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		return newArray;
	}

	@Override
	protected double[] doResize(double[] oldArray, int newSize)
	{
		// Check params:
		SpcfParamValidator.checkIsNotNull(oldArray, "Old Array");
		SpcfParamValidator.checkIsNonNegative(newSize, "New Array Size");

        // If the old and new array sizes are the same, just return the old array:
        if (oldArray.length == newSize) return oldArray;

		// Do the resize:
		double[] newArray = new double[newSize];
		int oldSize = oldArray.length;
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0) System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		return newArray;
	}

	@Override
	protected float[] doResize(float[] oldArray, int newSize)
	{
		// Check params:
		SpcfParamValidator.checkIsNotNull(oldArray, "Old Array");
		SpcfParamValidator.checkIsNonNegative(newSize, "New Array Size");

        // If the old and new array sizes are the same, just return the old array:
        if (oldArray.length == newSize) return oldArray;

		// Do the resize:
		float[] newArray = new float[newSize];
		int oldSize = oldArray.length;
		int preserveLength = Math.min(oldSize, newSize);
		if (preserveLength > 0) System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
		return newArray;
	}
	
	protected <T> int linearNullSearch(T[] a)
	{
    	for (int i = 0; i < a.length; i++)
    	{
    		if (a[i] == null) return i;
    	}
    	
    	return -1;
	}
}
