package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.*;

/**
 * SpcfArraysUtil contains common utility methods for manipulating single dimensional fixed arrays.
 */
public abstract strictfp class SpcfArraysUtil {
	
	/**
     * SpcfArraysUtil instance that is used for static methods
     */
    private static SpcfArraysUtil sArraysUtil;
    
    static
    {
        sArraysUtil = SpcfFactory.getInstance().createArraysUtil(); //instance for static methods
    }

	/***
	 * This is an in-order comparison of fixed arrays of T[]. Because of 
	 * type erasure, this signature is equivalent to parameters of Object[], Object[].
	 * The array element equivalence check is performed by the element's equals()
	 * override or its default implementation. 
	 * Arrays are considered equal if both are null.
	 * The following must be true for the arrays to be equal:
	 * 1. Both arrays must be the same runtime type. Parameter contavariance is not supported.
	 * 2. Each array index, both elements are null or the element's equal override returns true.
	 * 3. Both arrays have the same number of elements.
	 * @param thisArray of type T's or null. Null elements are also allowed.
	 * @param thatArray of type T's or null. Null elements are also allowed.
	 * @return true if equal by the above rules; false otherwise.
	 */
	public static <T> boolean equals(T[] thisArray, T[] thatArray)
	{
		return sArraysUtil.doEquals(thisArray, thatArray);
	}
	
	/***
	 * This is an in-order comparison of the fixed arrays. 
	 * The following must be true for the arrays to be equal:
	 * Both array references are null or Both arrays have the same number of elements and elements have the same value.
	 * @param a of type long[] or null.
	 * @param a2 of type long[] or null.
	 * @return true if equal by the above rules; false otherwise.
	 */
	public static boolean equals(long[] a, long[] a2)
	{
		return sArraysUtil.doEquals(a, a2);
	}
	
	/***
	 * This is an in-order comparison of the fixed arrays. 
	 * The following must be true for the arrays to be equal:
	 * Both array references are null or Both arrays have the same number of elements and elements have the same value.
	 * @param a of type int[] or null.
	 * @param a2 of type int[] or null.
	 * @return true if equal by the above rules; false otherwise.
	 */
	public static boolean equals(int[] a, int[] a2)
	{
		return sArraysUtil.doEquals(a, a2);
	}
	
	/***
	 * This is an in-order comparison of the fixed arrays. 
	 * The following must be true for the arrays to be equal:
	 * Both array references are null or Both arrays have the same number of elements and elements have the same value.
	 * @param a of type short[] or null.
	 * @param a2 of type short[] or null.
	 * @return true if equal by the above rules; false otherwise.
	 */
	public static boolean equals(short[] a, short[] a2)
	{
		return sArraysUtil.doEquals(a, a2);
	}
	
	/***
	 * This is an in-order comparison of the fixed arrays. 
	 * The following must be true for the arrays to be equal:
	 * Both array references are null or Both arrays have the same number of elements and elements have the same value.
	 * @param a of type char[] or null.
	 * @param a2 of type char[] or null.
	 * @return true if equal by the above rules; false otherwise.
	 */
	public static boolean equals(char[] a, char[] a2)
	{
		return sArraysUtil.doEquals(a, a2);
	}
	
	/***
	 * This is an in-order comparison of the fixed arrays. 
	 * The following must be true for the arrays to be equal:
	 * Both array references are null or Both arrays have the same number of elements and elements have the same value.
	 * @param a of type byte[] or null.
	 * @param a2 of type byte[] or null.
	 * @return true if equal by the above rules; false otherwise.
	 */
	public static boolean equals(byte[] a, byte[] a2)
	{
		return sArraysUtil.doEquals(a, a2);
	}
	
	/***
	 * This is an in-order comparison of the fixed arrays. 
	 * The following must be true for the arrays to be equal:
	 * Both array references are null or Both arrays have the same number of elements and elements have the same value.
	 * @param a of type boolean[] or null.
	 * @param a2 of type boolean[] or null.
	 * @return true if equal by the above rules; false otherwise.
	 */
	public static boolean equals(boolean[] a, boolean[] a2)
	{
		return sArraysUtil.doEquals(a, a2);
	}
	
	/***
	 * This is an in-order comparison of the fixed arrays. 
	 * The following must be true for the arrays to be equal:
	 * Both array references are null or Both arrays have the same number of elements and elements have the same value.
	 * @param a of type double[] or null.
	 * @param a2 of type double[] or null.
	 * @return true if equal by the above rules; false otherwise.
	 */
	public static boolean equals(double[] a, double[] a2)
	{
		return sArraysUtil.doEquals(a, a2);
	}
	
	/***
	 * This is an in-order comparison of the fixed arrays. 
	 * The following must be true for the arrays to be equal:
	 * Both array references are null or Both arrays have the same number of elements and elements have the same value.
	 * @param a of type float[] or null.
	 * @param a2 of type float[] or null.
	 * @return true if equal by the above rules; false otherwise.
	 */
	public static boolean equals(float[] a, float[] a2)
	{
		return sArraysUtil.doEquals(a, a2);
	}

    /* Array Fill utilities */
	
	/***
	 * Assigns the value to each element of the array.
	 * @param a A single dimensional array of T[].
	 * @param val A value of T to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 */	
	public static <T> void fill(T[] a, T val)
	{
		sArraysUtil.doFill(a, 0, a != null ? a.length : 0, val);
	}

	/***
	 * Assigns the value to each element of the array. 
	 * The range to be filled extends from index fromIndex, 
	 * inclusive, to index toIndex, exclusive.
	 * @param a A single dimensional array of T[].
	 * @param fromIndex the first array element to begin.
	 * @param toIndex the index of the last element (exclusive) to be modified.
	 * @param val A value of T to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfIllegalArgumentException if fromIndex &gt; toIndex
	 * @throws SpcfIndexOutOfBoundsException if fromIndex &lt; 0 or toIndex &gt; a.length
	 */	
	public static <T> void fill(T[] a, int fromIndex, int toIndex, T val)
	{
		sArraysUtil.doFill(a, fromIndex, toIndex, val);
	}

	/***
	 * Assigns the value to each element of the array.
	 * @param a A single dimensional array of long[].
	 * @param val A value of long to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 */	
	public static void fill(long[] a, long val)
	{
		sArraysUtil.doFill(a, 0, a != null ? a.length : 0, val);
	}

	/***
	 * Assigns the value to each element of the array. 
	 * The range to be filled extends from index fromIndex, 
	 * inclusive, to index toIndex, exclusive.
	 * @param a A single dimensional array of long[].
	 * @param fromIndex the first array element to begin.
	 * @param toIndex the index of the last element (exclusive) to be modified.
	 * @param val A value of long to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfIllegalArgumentException if fromIndex &gt; toIndex
	 * @throws SpcfIndexOutOfBoundsException if fromIndex &lt; 0 or toIndex &gt; a.length
	 */	
	public static void fill(long[] a, int fromIndex, int toIndex, long val)
	{
		sArraysUtil.doFill(a, fromIndex, toIndex, val);
	}

	/***
	 * Assigns the value to each element of the array.
	 * @param a A single dimensional array of int[].
	 * @param val A value of int to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 */	
	public static void fill(int[] a, int val)
	{
		sArraysUtil.doFill(a, 0, a != null ? a.length : 0, val);
	}

	/***
	 * Assigns the value to each element of the array. 
	 * The range to be filled extends from index fromIndex, 
	 * inclusive, to index toIndex, exclusive.
	 * @param a A single dimensional array of int[].
	 * @param fromIndex the first array element to begin.
	 * @param toIndex the index of the last element (exclusive) to be modified.
	 * @param val A value of int to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfIllegalArgumentException if fromIndex &gt; toIndex
	 * @throws SpcfIndexOutOfBoundsException if fromIndex &lt; 0 or toIndex &gt; a.length
	 */	
	public static void fill(int[] a, int fromIndex, int toIndex, int val)
	{
		sArraysUtil.doFill(a, fromIndex, toIndex, val);
	}
	
	/***
	 * Assigns the value to each element of the array.
	 * @param a A single dimensional array of short[].
	 * @param val A value of short to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 */	
	public static void fill(short[] a, short val)
	{
		sArraysUtil.doFill(a, 0, a != null ? a.length : 0, val);
	}

	/***
	 * Assigns the value to each element of the array. 
	 * The range to be filled extends from index fromIndex, 
	 * inclusive, to index toIndex, exclusive.
	 * @param a A single dimensional array of short[].
	 * @param fromIndex the first array element to begin.
	 * @param toIndex the index of the last element (exclusive) to be modified.
	 * @param val A value of short to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfIllegalArgumentException if fromIndex &gt; toIndex
	 * @throws SpcfIndexOutOfBoundsException if fromIndex &lt; 0 or toIndex &gt; a.length
	 */	
	public static void fill(short[] a, int fromIndex, int toIndex, short val)
	{
		sArraysUtil.doFill(a, fromIndex, toIndex, val);
	}
	
	/***
	 * Assigns the value to each element of the array.
	 * @param a A single dimensional array of char[].
	 * @param val A value of char to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 */	
	public static void fill(char[] a, char val)
	{
		sArraysUtil.doFill(a, 0, a != null ? a.length : 0, val);
	}

	/***
	 * Assigns the value to each element of the array. 
	 * The range to be filled extends from index fromIndex, 
	 * inclusive, to index toIndex, exclusive.
	 * @param a A single dimensional array of char[].
	 * @param fromIndex the first array element to begin.
	 * @param toIndex the index of the last element (exclusive) to be modified.
	 * @param val A value of char to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfIllegalArgumentException if fromIndex &gt; toIndex
	 * @throws SpcfIndexOutOfBoundsException if fromIndex &lt; 0 or toIndex &gt; a.length
	 */	
	public static void fill(char[] a, int fromIndex, int toIndex, char val)
	{
		sArraysUtil.doFill(a, fromIndex, toIndex, val);
	}
	
	/***
	 * Assigns the value to each element of the array.
	 * @param a A single dimensional array of byte[].
	 * @param val A value of byte to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 */	
	public static void fill(byte[] a, byte val)
	{
		sArraysUtil.doFill(a, 0, a != null ? a.length : 0, val);
	}

	/***
	 * Assigns the value to each element of the array. 
	 * The range to be filled extends from index fromIndex, 
	 * inclusive, to index toIndex, exclusive.
	 * @param a A single dimensional array of byte[].
	 * @param fromIndex the first array element to begin.
	 * @param toIndex the index of the last element (exclusive) to be modified.
	 * @param val A value of byte to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfIllegalArgumentException if fromIndex &gt; toIndex
	 * @throws SpcfIndexOutOfBoundsException if fromIndex &lt; 0 or toIndex &gt; a.length
	 */	
	public static void fill(byte[] a, int fromIndex, int toIndex, byte val)
	{
		sArraysUtil.doFill(a, fromIndex, toIndex, val);
	}
	
	/***
	 * Assigns the value to each element of the array.
	 * @param a A single dimensional array of boolean[].
	 * @param val A value of boolean to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 */	
	public static void fill(boolean[] a, boolean val)
	{
		sArraysUtil.doFill(a, 0, a != null ? a.length : 0, val);
	}

	/***
	 * Assigns the value to each element of the array. 
	 * The range to be filled extends from index fromIndex, 
	 * inclusive, to index toIndex, exclusive.
	 * @param a A single dimensional array of boolean[].
	 * @param fromIndex the first array element to begin.
	 * @param toIndex the index of the last element (exclusive) to be modified.
	 * @param val A value of boolean to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfIllegalArgumentException if fromIndex &gt; toIndex
	 * @throws SpcfIndexOutOfBoundsException if fromIndex &lt; 0 or toIndex &gt; a.length
	 */	
	public static void fill(boolean[] a, int fromIndex, int toIndex, boolean val)
	{
		sArraysUtil.doFill(a, fromIndex, toIndex, val);
	}
	
	/***
	 * Assigns the value to each element of the array.
	 * @param a A single dimensional array of double[].
	 * @param val A value of double to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 */	
	public static void fill(double[] a, double val)
	{
		sArraysUtil.doFill(a, 0, a != null ? a.length : 0, val);
	}

	/***
	 * Assigns the value to each element of the array. 
	 * The range to be filled extends from index fromIndex, 
	 * inclusive, to index toIndex, exclusive.
	 * @param a A single dimensional array of double[].
	 * @param fromIndex the first array element to begin.
	 * @param toIndex the index of the last element (exclusive) to be modified.
	 * @param val A value of double to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfIllegalArgumentException if fromIndex &gt; toIndex
	 * @throws SpcfIndexOutOfBoundsException if fromIndex &lt; 0 or toIndex &gt; a.length
	 */	
	public static void fill(double[] a, int fromIndex, int toIndex, double val)
	{
		sArraysUtil.doFill(a, fromIndex, toIndex, val);
	}
	
	/***
	 * Assigns the value to each element of the array.
	 * @param a A single dimensional array of float[].
	 * @param val A value of float to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 */	
	public static void fill(float[] a, float val)
	{
		sArraysUtil.doFill(a, 0, a != null ? a.length : 0, val);
	}

	/***
	 * Assigns the value to each element of the array. 
	 * The range to be filled extends from index fromIndex, 
	 * inclusive, to index toIndex, exclusive.
	 * @param a A single dimensional array of float[].
	 * @param fromIndex the first array element to begin.
	 * @param toIndex the index of the last element (exclusive) to be modified.
	 * @param val A value of float to store in each array element.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfIllegalArgumentException if fromIndex &gt; toIndex
	 * @throws SpcfIndexOutOfBoundsException if fromIndex &lt; 0 or toIndex &gt; a.length
	 */	
	public static void fill(float[] a, int fromIndex, int toIndex, float val)
	{
		sArraysUtil.doFill(a, fromIndex, toIndex, val);
	}

	/**
	 * Sorts the specified array of objects according to the order. 
	 * This sort is not guaranteed to be stable. Equal elements may be reordered as a result of 
	 * the sort.
	 * @param a fixed array to sort.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfUnsupportedOperationException The array is read-only or if the array 
	 * contains elements that are not <i>mutually comparable</i> 
	 * (for example if contains null elements or elements of different runtime type: e.g. strings and integers)
	 */
	public static <T> void sort(T[] a)
	{
		sArraysUtil.doSort(a);		
	}
	
	/**
	 * Sorts the specified array of objects according to the provided comparator. 
	 * This sort is not guaranteed to be stable. Equal elements may be reordered as a result of 
	 * the sort.
	 * @param a fixed array to sort.
	 * @param comparator the client specified comparer used to sort the elements of the array.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfUnsupportedOperationException The array is read-only or if the array 
	 * contains elements that are not <i>mutually comparable</i> 
	 * (for example if contains null elements or elements of different runtime type: e.g. strings and integers)
	 */
	public static <T> void sort(T[] a, ISpcfComparator<T> comparator)
	{
		sArraysUtil.doSort(a, comparator);
	}
	
	/**
     * Searches the specified array for the specified object using the binary
     * search algorithm.  The array must be sorted into ascending order
     * according to the natural ordering of its elements.
     * If it is not sorted, the results are undefined.  If the array contains multiple
     * elements equal to the specified object, there is no guarantee which one
     * will be found.
     * 
     * @param a the array to be searched.
     * @param obj the obj to be searched for.
     * @return index of the search key, if it is contained in the array, otherwise
     * a value &lt; 0.
 	 * @throws SpcfIllegalArgumentException if array == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements that 
	 * are not mutually comparable. null comparisons are not supported. 
    */
	public static <T> int search(T[] a, T obj)
	{
		return sArraysUtil.doSearch(a, obj);		
	}
	
	/**
     * Searches the specified array for the specified object using the binary search algorithm.  
     * The array must be sorted according to order specified by the comparator.
     * If it is not sorted using the specified comparator, the results are undefined.  
     * If the array contains multiple elements equal to the specified object, 
     * there is no guarantee which one will be found.
     * 
     * @param a the array to be searched.
     * @param obj the obj to be searched for.
	 * @param comparator Comparator used for element object comparison.
     * @return index of the search key, if it is contained in the list, otherwise
     * a value &lt; 0.
 	 * @throws SpcfIllegalArgumentException if array is null or the comparator is null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
    */
	public static <T> int search(T[] a, T obj, ISpcfComparator<T> comparator)
	{
		return sArraysUtil.doSearch(a, obj, comparator);
	}
	
	/**
     * Searches the specified array for the specified object using the binary
     * search algorithm.  The array must be sorted into ascending order.
     * If it is not sorted, the results are undefined.  If the array contains multiple
     * elements equal to the specified object, there is no guarantee which one
     * will be found.
     * 
     * @param a the array to be searched.
     * @param obj the obj to be searched for.
     * @return index of the search key, if it is contained in the array, otherwise
     * a value &lt; 0.
 	 * @throws SpcfIllegalArgumentException if array == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
    */
	public static int search(long[] a, long obj)
	{
		return sArraysUtil.doSearch(a, obj);		
	}

	/**
     * Searches the specified array for the specified object using the binary
     * search algorithm.  The array must be sorted into ascending order.
     * If it is not sorted, the results are undefined.  If the array contains multiple
     * elements equal to the specified object, there is no guarantee which one
     * will be found.
     * 
     * @param a the array to be searched.
     * @param obj the obj to be searched for.
     * @return index of the search key, if it is contained in the array, otherwise
     * a value &lt; 0.
 	 * @throws SpcfIllegalArgumentException if array == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
   */
	public static int search(int[] a, int obj)
	{
		return sArraysUtil.doSearch(a, obj);		
	}

	/**
     * Searches the specified array for the specified object using the binary
     * search algorithm.  The array must be sorted into ascending order.
     * If it is not sorted, the results are undefined.  If the array contains multiple
     * elements equal to the specified object, there is no guarantee which one
     * will be found.
     * 
     * @param a the array to be searched.
     * @param obj the obj to be searched for.
     * @return index of the search key, if it is contained in the array, otherwise
     * a value &lt; 0.
 	 * @throws SpcfIllegalArgumentException if array == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
    */
	public static int search(short[] a, short obj)
	{
		return sArraysUtil.doSearch(a, obj);		
	}

	/**
     * Searches the specified array for the specified object using the binary
     * search algorithm.  The array must be sorted into ascending order.
     * If it is not sorted, the results are undefined.  If the array contains multiple
     * elements equal to the specified object, there is no guarantee which one
     * will be found.
     * 
     * @param a the array to be searched.
     * @param obj the obj to be searched for.
     * @return index of the search key, if it is contained in the array, otherwise
     * a value &lt; 0.
 	 * @throws SpcfIllegalArgumentException if array == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
    */
	public static int search(char[] a, char obj)
	{
		return sArraysUtil.doSearch(a, obj);		
	}

	/**
     * Searches the specified array for the specified object using the binary
     * search algorithm.  The array must be sorted into ascending order.
     * If it is not sorted, the results are undefined.  If the array contains multiple
     * elements equal to the specified object, there is no guarantee which one
     * will be found.
     * 
     * @param a the array to be searched.
     * @param obj the obj to be searched for.
     * @return index of the search key, if it is contained in the array, otherwise
     * a value &lt; 0.
 	 * @throws SpcfIllegalArgumentException if array == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
    */
	public static int search(byte[] a, byte obj)
	{
		return sArraysUtil.doSearch(a, obj);		
	}

	/**
     * Searches the specified array for the specified object using the binary
     * search algorithm.  The array must be sorted into ascending order.
     * If it is not sorted, the results are undefined.  If the array contains multiple
     * elements equal to the specified object, there is no guarantee which one
     * will be found.
     * 
     * @param a the array to be searched.
     * @param obj the obj to be searched for.
     * @return index of the search key, if it is contained in the array, otherwise
     * a value &lt; 0.
 	 * @throws SpcfIllegalArgumentException if array == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
    */
	public static int search(float[] a, float obj)
	{
		return sArraysUtil.doSearch(a, obj);		
	}

	/**
     * Searches the specified array for the specified object using the binary
     * search algorithm.  The array must be sorted into ascending order.
     * If it is not sorted, the results are undefined.  If the array contains multiple
     * elements equal to the specified object, there is no guarantee which one
     * will be found.
     * 
     * @param a the array to be searched.
     * @param obj the obj to be searched for.
     * @return index of the search key, if it is contained in the array, otherwise
     * a value &lt; 0.
 	 * @throws SpcfIllegalArgumentException if array == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
    */
	public static int search(double[] a, double obj)
	{
		return sArraysUtil.doSearch(a, obj);		
	}
	
	/**
	 * Sorts the specified array of longs in ascending numerical order. 
	 * This sort is not guaranteed to be stable. Equal elements may be reordered as a result of 
	 * the sort.
	 * @param a fixed array to sort.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
	 */
	public static void sort(long[] a)
	{
		sArraysUtil.doSort(a);		
	}
	
	/**
	 * Sorts the specified array of ints in ascending numerical order.  
	 * This sort is not guaranteed to be stable. Equal elements may be reordered as a result of 
	 * the sort.
	 * @param a fixed array to sort.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
	 */
	public static void sort(int[] a)
	{
		sArraysUtil.doSort(a);		
	}
	
	/**
	 * Sorts the specified array of shorts in ascending numerical order.
	 * This sort is not guaranteed to be stable. Equal elements may be reordered as a result of 
	 * the sort.
	 * @param a fixed array to sort.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
	 */
	public static void sort(short[] a)
	{
		sArraysUtil.doSort(a);		
	}
	
	/**
	 * Sorts the specified array of chars in ascending numerical order. 
	 * This sort is not guaranteed to be stable. Equal elements may be reordered as a result of 
	 * the sort.
	 * @param a fixed array to sort.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
	 */
	public static void sort(char[] a)
	{
		sArraysUtil.doSort(a);	
	}
	
	/**
	 * Sorts the specified array of bytes in ascending numerical order. 
	 * This sort is not guaranteed to be stable. Equal elements may be reordered as a result of 
	 * the sort.
	 * @param a fixed array to sort.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
	 */
	public static void sort(byte[] a)
	{
		sArraysUtil.doSort(a);
	}
	
	/**
	 * Sorts the specified array of doubles in ascending numerical order. 
	 * This sort is not guaranteed to be stable. Equal elements may be reordered as a result of 
	 * the sort.
	 * @param a fixed array to sort.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
	 */
	public static void sort(double[] a)
	{
		sArraysUtil.doSort(a);
	}
	
	/**
	 * Sorts the specified array of floats in ascending numerical order. 
	 * This sort is not guaranteed to be stable. Equal elements may be reordered as a result of 
	 * the sort.
	 * @param a fixed array to sort.
	 * @throws SpcfIllegalArgumentException if a == null.
	 * @throws SpcfUnsupportedOperationException The array contains elements 
	 * that are not mutually comparable. null comparisons are not supported. 
	 */
	public static void sort(float[] a)
	{
		sArraysUtil.doSort(a);
	}
	
	
    /**
     * Copies the source array into the target array.
     * @param sourceArray the source array to be copied
     * @param sourceArrayStartPosition the starting position in the source array
     * @param destinationArray the destination array in which to put the copied elements
     * @param destinationArrayStartPosition the starting position in the destination array
     * @param count number of elements to copy from the source into the target
     * @throws SpcfArgumentNullException if either source or destination arrays are null
     * @throws SpcfIllegalArgumentException if the source or target are not arrays or are arrays of incompatible types 
	 * @throws SpcfArgumentOutOfRangeException if either starting position is less than 0 or if count is less than 0  
	 * @throws SpcfIndexOutOfBoundsException if starting positions plus count exceeds the respective array's length 
     */
	public static void copy(Object sourceArray, int sourceArrayStartPosition, 
			Object destinationArray, int destinationArrayStartPosition, int count)
	{
		// Perform the copy through the Impl:
		sArraysUtil.doCopy(sourceArray, sourceArrayStartPosition,
				destinationArray, destinationArrayStartPosition, count);
	}

	
	/**
	 * Reallocates an array with a new size, and copies the contents
	 * of the old array to the new array.
	 * @param oldArray the old array to be reallocated
	 * @param newSize the new array size
	 * @return a new array with the same contents as the old array
     * @throws SpcfArgumentNullException if the old array is null
	 * @throws SpcfArgumentOutOfRangeException if newSize is less than 0
	 */
    public static <T> T[] resize(T[] oldArray, int newSize)
    {
		// Perform the resize through the Impl:
		return sArraysUtil.doResize(oldArray, newSize);
    }

    
	/**
	 * Reallocates an array with a new size, and copies the contents
	 * of the old array to the new array.
	 * @param oldArray the old array to be reallocated
	 * @param newSize the new array size
	 * @return a new array with the same contents as the old array
     * @throws SpcfArgumentNullException if the old array is null
	 * @throws SpcfArgumentOutOfRangeException if newSize is less than 0
	 */
    public static long[] resize(long[] oldArray, int newSize)
    {
		// Perform the resize through the Impl:
		return sArraysUtil.doResize(oldArray, newSize);
    }
    
    
	/**
	 * Reallocates an array with a new size, and copies the contents
	 * of the old array to the new array.
	 * @param oldArray the old array to be reallocated
	 * @param newSize the new array size
	 * @return a new array with the same contents as the old array
     * @throws SpcfArgumentNullException if the old array is null
	 * @throws SpcfArgumentOutOfRangeException if newSize is less than 0
	 */
    public static int[] resize(int[] oldArray, int newSize)
    {
		// Perform the resize through the Impl:
		return sArraysUtil.doResize(oldArray, newSize);
    }
    
    
	/**
	 * Reallocates an array with a new size, and copies the contents
	 * of the old array to the new array.
	 * @param oldArray the old array to be reallocated
	 * @param newSize the new array size
	 * @return a new array with the same contents as the old array
     * @throws SpcfArgumentNullException if the old array is null
	 * @throws SpcfArgumentOutOfRangeException if newSize is less than 0
	 */
    public static short[] resize(short[] oldArray, int newSize)
    {
		// Perform the resize through the Impl:
		return sArraysUtil.doResize(oldArray, newSize);
    }

    
	/**
	 * Reallocates an array with a new size, and copies the contents
	 * of the old array to the new array.
	 * @param oldArray the old array to be reallocated
	 * @param newSize the new array size
	 * @return a new array with the same contents as the old array
     * @throws SpcfArgumentNullException if the old array is null
	 * @throws SpcfArgumentOutOfRangeException if newSize is less than 0
	 */
    public static char[] resize(char[] oldArray, int newSize)
    {
		// Perform the resize through the Impl:
		return sArraysUtil.doResize(oldArray, newSize);
    }
    
    
	/**
	 * Reallocates an array with a new size, and copies the contents
	 * of the old array to the new array.
	 * @param oldArray the old array to be reallocated
	 * @param newSize the new array size
	 * @return a new array with the same contents as the old array
     * @throws SpcfArgumentNullException if the old array is null
	 * @throws SpcfArgumentOutOfRangeException if newSize is less than 0
	 */
    public static byte[] resize(byte[] oldArray, int newSize)
    {
		// Perform the resize through the Impl:
		return sArraysUtil.doResize(oldArray, newSize);
    }
    
    
	/**
	 * Reallocates an array with a new size, and copies the contents
	 * of the old array to the new array.
	 * @param oldArray the old array to be reallocated
	 * @param newSize the new array size
	 * @return a new array with the same contents as the old array
     * @throws SpcfArgumentNullException if the old array is null
	 * @throws SpcfArgumentOutOfRangeException if newSize is less than 0
	 */
    public static double[] resize(double[] oldArray, int newSize)
    {
		// Perform the resize through the Impl:
		return sArraysUtil.doResize(oldArray, newSize);
    }
    

	/**
	 * Reallocates an array with a new size, and copies the contents
	 * of the old array to the new array.
	 * @param oldArray the old array to be reallocated
	 * @param newSize the new array size
	 * @return a new array with the same contents as the old array
     * @throws SpcfArgumentNullException if the old array is null
	 * @throws SpcfArgumentOutOfRangeException if newSize is less than 0
	 */
    public static float[] resize(float[] oldArray, int newSize)
    {
		// Perform the resize through the Impl:
		return sArraysUtil.doResize(oldArray, newSize);
    }
    
    /**
     * Constructs a SpcfArraysUtil object.
     * @return SpcfArraysUtil implementation object.
     */
    public static SpcfArraysUtil createInstance()
    {
        return SpcfFactory.getInstance().createArraysUtil();   
    }
    
	/**
	 * Forwarding virtual methods for copyImpl functionality.
	 * @param sourceArray The source array to be copied from.
	 * @param sourceArrayStartPosition The source array offset.
	 * @param destinationArray The destination array to be copied to.
	 * @param destinationArrayStartPosition The destination array offset.
	 * @param count The number of elements to be copied.
	 */
    protected abstract void doCopy(Object sourceArray, int sourceArrayStartPosition, 
    		Object destinationArray, int destinationArrayStartPosition, int count);
	
    /**
     * Forwarding virtual methods for resize functionality.
     * @param oldArray The original array to be resized.
     * @param newSize The new size to make the array.
     * @return The newly resized array.
     */
	protected abstract <T> T[] doResize(T[] oldArray, int newSize);
	
    /**
     * Forwarding virtual methods for resize functionality.
     * @param oldArray The original array to be resized.
     * @param newSize The new size to make the array.
     * @return The newly resized array.
     */
    protected abstract long[] doResize(long[] oldArray, int newSize);
	
    /**
     * Forwarding virtual methods for resize functionality.
     * @param oldArray The original array to be resized.
     * @param newSize The new size to make the array.
     * @return The newly resized array.
     */
    protected abstract int[] doResize(int[] oldArray, int newSize);
	
    /**
     * Forwarding virtual methods for resize functionality.
     * @param oldArray The original array to be resized.
     * @param newSize The new size to make the array.
     * @return The newly resized array.
     */
    protected abstract short[] doResize(short[] oldArray, int newSize);
	
    /**
     * Forwarding virtual methods for resize functionality.
     * @param oldArray The original array to be resized.
     * @param newSize The new size to make the array.
     * @return The newly resized array.
     */
    protected abstract char[] doResize(char[] oldArray, int newSize);
	
    /**
     * Forwarding virtual methods for resize functionality.
     * @param oldArray The original array to be resized.
     * @param newSize The new size to make the array.
     * @return The newly resized array.
     */
    protected abstract byte[] doResize(byte[] oldArray, int newSize);
	
    /**
     * Forwarding virtual methods for resize functionality.
     * @param oldArray The original array to be resized.
     * @param newSize The new size to make the array.
     * @return The newly resized array.
     */
    protected abstract double[] doResize(double[] oldArray, int newSize);
	
    /**
     * Forwarding virtual methods for resize functionality.
     * @param oldArray The original array to be resized.
     * @param newSize The new size to make the array.
     * @return The newly resized array.
     */
    protected abstract float[] doResize(float[] oldArray, int newSize);

    /**
     * Forwarding virtual methods for sort functionality.
     * @param a The array to be sorted.
     */
	protected abstract <T> void doSort(T[] a);
	
    /**
     * Forwarding virtual methods for sort functionality.
     * @param a The array to be sorted.
     * @param comparator The comparator to be used during sorting.
     */
	protected abstract <T> void doSort(T[] a, ISpcfComparator<T> comparator);
	
    /**
     * Forwarding virtual methods for sort functionality.
     * @param a The array to be sorted.
     */
    protected abstract void doSort(long[] a);
    
    /**
     * Forwarding virtual methods for sort functionality.
     * @param a The array to be sorted.
     */
    protected abstract void doSort(int[] a);
    
    /**
     * Forwarding virtual methods for sort functionality.
     * @param a The array to be sorted.
     */
    protected abstract void doSort(short[] a);
    
    /**
     * Forwarding virtual methods for sort functionality.
     * @param a The array to be sorted.
     */
    protected abstract void doSort(char[] a);
    
    /**
     * Forwarding virtual methods for sort functionality.
     * @param a The array to be sorted.
     */
    protected abstract void doSort(byte[] a);
    
    /**
     * Forwarding virtual methods for sort functionality.
     * @param a The array to be sorted.
     */
    protected abstract void doSort(double[] a);
    
    /**
     * Forwarding virtual methods for sort functionality.
     * @param a The array to be sorted.
     */
    protected abstract void doSort(float[] a);
    
	/**
	 * Forwarding virtual methods for search functionality.
	 * @param a The array to be searched.
	 * @param obj The object to search for.
	 * @return The index of the object, or -1 if it is not found.
	 */
	protected abstract <T> int doSearch(T[] a, T obj);
	
	/**
	 * Forwarding virtual methods for search functionality.
	 * @param a The array to be searched.
	 * @param obj The object to search for.
	 * @param comparator The comparator to use while searching.
	 * @return The index of the object, or -1 if it is not found.
	 */
	protected abstract <T> int doSearch(T[] a, T obj, ISpcfComparator<T> comparator);
	
	/**
	 * Forwarding virtual methods for search functionality.
	 * @param a The array to be searched.
	 * @param obj The object to search for.
	 * @return The index of the object, or -1 if it is not found.
	 */
 	protected abstract int doSearch(long[] a, long obj);
	
	/**
	 * Forwarding virtual methods for search functionality.
	 * @param a The array to be searched.
	 * @param obj The object to search for.
	 * @return The index of the object, or -1 if it is not found.
	 */
 	protected abstract int doSearch(int[] a, int obj);
	
	/**
	 * Forwarding virtual methods for search functionality.
	 * @param a The array to be searched.
	 * @param obj The object to search for.
	 * @return The index of the object, or -1 if it is not found.
	 */
 	protected abstract int doSearch(short[] a, short obj);
	
	/**
	 * Forwarding virtual methods for search functionality.
	 * @param a The array to be searched.
	 * @param obj The object to search for.
	 * @return The index of the object, or -1 if it is not found.
	 */
 	protected abstract int doSearch(char[] a, char obj);
	
	/**
	 * Forwarding virtual methods for search functionality.
	 * @param a The array to be searched.
	 * @param obj The object to search for.
	 * @return The index of the object, or -1 if it is not found.
	 */
 	protected abstract int doSearch(byte[] a, byte obj);
	
	/**
	 * Forwarding virtual methods for search functionality.
	 * @param a The array to be searched.
	 * @param obj The object to search for.
	 * @return The index of the object, or -1 if it is not found.
	 */
 	protected abstract int doSearch(float[] a, float obj);
	
	/**
	 * Forwarding virtual methods for search functionality.
	 * @param a The array to be searched.
	 * @param obj The object to search for.
	 * @return The index of the object, or -1 if it is not found.
	 */
 	protected abstract int doSearch(double[] a, double obj);

	/**
	 * Forwarding virtual methods for equals functionality.
	 * @param thisArray The first array to be compared for equality.
	 * @param thatArray The second array to be compared for equality.
	 * @return True if the arrays are equal, false otherwise.
	 */
	protected abstract <T> boolean doEquals(T[] thisArray, T[] thatArray);
	
	/**
	 * Forwarding virtual methods for equals functionality.
	 * @param a The first array to be compared for equality.
	 * @param a2 The second array to be compared for equality.
	 * @return True if the arrays are equal, false otherwise.
	 */
    protected abstract boolean doEquals(long[] a, long[] a2);
    
	/**
	 * Forwarding virtual methods for equals functionality.
	 * @param a The first array to be compared for equality.
	 * @param a2 The second array to be compared for equality.
	 * @return True if the arrays are equal, false otherwise.
	 */
    protected abstract boolean doEquals(int[] a, int[] a2);
    
	/**
	 * Forwarding virtual methods for equals functionality.
	 * @param a The first array to be compared for equality.
	 * @param a2 The second array to be compared for equality.
	 * @return True if the arrays are equal, false otherwise.
	 */
    protected abstract boolean doEquals(short[] a, short[] a2);
    
	/**
	 * Forwarding virtual methods for equals functionality.
	 * @param a The first array to be compared for equality.
	 * @param a2 The second array to be compared for equality.
	 * @return True if the arrays are equal, false otherwise.
	 */
    protected abstract boolean doEquals(char[] a, char[] a2);
    
	/**
	 * Forwarding virtual methods for equals functionality.
	 * @param a The first array to be compared for equality.
	 * @param a2 The second array to be compared for equality.
	 * @return True if the arrays are equal, false otherwise.
	 */
    protected abstract boolean doEquals(byte[] a, byte[] a2);
    
	/**
	 * Forwarding virtual methods for equals functionality.
	 * @param a The first array to be compared for equality.
	 * @param a2 The second array to be compared for equality.
	 * @return True if the arrays are equal, false otherwise.
	 */
    protected abstract boolean doEquals(boolean[] a, boolean[] a2);
    
	/**
	 * Forwarding virtual methods for equals functionality.
	 * @param a The first array to be compared for equality.
	 * @param a2 The second array to be compared for equality.
	 * @return True if the arrays are equal, false otherwise.
	 */
    protected abstract boolean doEquals(double[] a, double[] a2);
    
	/**
	 * Forwarding virtual methods for equals functionality.
	 * @param a The first array to be compared for equality.
	 * @param a2 The second array to be compared for equality.
	 * @return True if the arrays are equal, false otherwise.
	 */
    protected abstract boolean doEquals(float[] a, float[] a2);
    
    /**
     * Forwarding virtual methods for fill functionality.
     * @param a The array to be filled.
     * @param fromIndex The starting index to be filled in the array.
     * @param toIndex The final index to be filled in the array.
     * @param val The value to fill the array with.
     */
    protected abstract <T> void doFill(T[] a, int fromIndex, int toIndex, T val);
    
    /**
     * Forwarding virtual methods for fill functionality.
     * @param a The array to be filled.
     * @param fromIndex The starting index to be filled in the array.
     * @param toIndex The final idnex to be filled in the array.
     * @param val The value to fill the array with.
     */
    protected abstract void doFill(long[] a, int fromIndex, int toIndex, long val);
    
    /**
     * Forwarding virtual methods for fill functionality.
     * @param a The array to be filled.
     * @param fromIndex The starting index to be filled in the array.
     * @param toIndex The final idnex to be filled in the array.
     * @param val The value to fill the array with.
     */
    protected abstract void doFill(int[] a, int fromIndex, int toIndex, int val);
    
    /**
     * Forwarding virtual methods for fill functionality.
     * @param a The array to be filled.
     * @param fromIndex The starting index to be filled in the array.
     * @param toIndex The final idnex to be filled in the array.
     * @param val The value to fill the array with.
     */
    protected abstract void doFill(short[] a, int fromIndex, int toIndex, short val);
    
    /**
     * Forwarding virtual methods for fill functionality.
     * @param a The array to be filled.
     * @param fromIndex The starting index to be filled in the array.
     * @param toIndex The final idnex to be filled in the array.
     * @param val The value to fill the array with.
     */
    protected abstract void doFill(char[] a, int fromIndex, int toIndex, char val);
    
    /**
     * Forwarding virtual methods for fill functionality.
     * @param a The array to be filled.
     * @param fromIndex The starting index to be filled in the array.
     * @param toIndex The final idnex to be filled in the array.
     * @param val The value to fill the array with.
     */
    protected abstract void doFill(byte[] a, int fromIndex, int toIndex, byte val);
    
    /**
     * Forwarding virtual methods for fill functionality.
     * @param a The array to be filled.
     * @param fromIndex The starting index to be filled in the array.
     * @param toIndex The final idnex to be filled in the array.
     * @param val The value to fill the array with.
     */
    protected abstract void doFill(boolean[] a, int fromIndex, int toIndex, boolean val);
    
    /**
     * Forwarding virtual methods for fill functionality.
     * @param a The array to be filled.
     * @param fromIndex The starting index to be filled in the array.
     * @param toIndex The final idnex to be filled in the array.
     * @param val The value to fill the array with.
     */
    protected abstract void doFill(double[] a, int fromIndex, int toIndex, double val);
    
    /**
     * Forwarding virtual methods for fill functionality.
     * @param a The array to be filled.
     * @param fromIndex The starting index to be filled in the array.
     * @param toIndex The final idnex to be filled in the array.
     * @param val The value to fill the array with.
     */
    protected abstract void doFill(float[] a, int fromIndex, int toIndex, float val);
}
