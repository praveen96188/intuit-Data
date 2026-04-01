package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;

/**
 * SpcfCollectionsUtil contains common utility methods for searching and sorting 
 * SpcfList collections.
 */
public abstract class SpcfCollectionsUtil 
{
	/**
     * SpcfCollectionsUtil instance that is used for static methods
     */
    private static SpcfCollectionsUtil sCollectionsUtil;
    static
    {
    	sCollectionsUtil = SpcfFactory.getInstance().createCollectionsUtil(); //instance for static methods
    }
   
    /**
     * Provides access to an instance of an empty set.
     * @return Returns the cached instance of the empty set.
     */
    public static <T> SpcfSet<T> emptySet() 
    {
    	return sCollectionsUtil.<T>doEmptySet();
    }
    
    /**
     * Provides access to an instance of an empty list.
     * @return Returns the cached instance of the empty list.
     */
    public static <T> SpcfList<T> emptyList() 
    {
    	return sCollectionsUtil.<T>doEmptyList();
    }
    
    /**
     * Provides access to an instance of an empty map.
     * @return Returns the cached instance of the empty map.
     */
    public static <K,V> SpcfMap<K,V> emptyMap() 
    {
    	return sCollectionsUtil.<K,V>doEmptyMap();
    }
    
	/**
	 * Sorts the SpcfList array of objects according to the natural ordering of its elements. 
	 * This sort is not guaranteed to be stable. Equal elements may be reordered as a result of 
	 * the sort.
	 * @param list a SpcfList to sort.
	 * @throws SpcfIllegalArgumentException if list == null.
	 * @throws SpcfUnsupportedOperationException The list is read-only or if the list 
	 * contains elements that are not mutually comparable. null comparisons are not supported. 
	 */
    public static <T> void sort(SpcfList<T> list)
    {
    	sCollectionsUtil.doSort(list);
    }

	/**
	 * Sorts the SpcfList array of objects according to the ISpcfComparator comparison implementation.
	 * This sort is not guaranteed to be stable. Equal elements may be reordered as a result of 
	 * the sort.
	 * @param list a SpcfList to sort.
	 * @param c Comparator used for element object comparison.
 	 * @throws SpcfIllegalArgumentException if list is null or the comparator is null.
	 * @throws SpcfUnsupportedOperationException The list is read-only or if the list 
	 * contains elements that are not mutually comparable. null comparisons are not supported. 
	 */
    public static <T> void sort(SpcfList<T> list, ISpcfComparator<T> c)
    {
    	sCollectionsUtil.doSort(list, c);
    }

    /**
     * Searches the specified list for the specified object using the binary
     * search algorithm.  The list must be sorted into ascending order
     * according to the natural ordering of its elements.
     * If it is not sorted, the results are undefined.  If the list contains multiple
     * elements equal to the specified object, there is no guarantee which one
     * will be found.  If a null key value is passed in, the algorithm will simply 
     * return list.indexOf(null) in the list, resulting in reduced search efficiency 
     * on par with O(n).
     * 
     * @param list the list to be searched.
     * @param key the key to be searched for.
     * @return index of the search key, if it is contained in the list, otherwise
     * a value &lt; 0.
 	 * @throws SpcfIllegalArgumentException if list == null.
	 * @throws SpcfUnsupportedOperationException The list is read-only or if the list 
	 * contains elements that are not mutually comparable. null comparisons are not supported. 
    */
    public static <T> int search(SpcfList<T> list, T key)
    {
       	return sCollectionsUtil.doSearch(list, key);    	
    }

    /**
     * Searches the specified list for the specified object using the binary search algorithm.  
     * The list must be sorted according to order specified by the comparator.
     * If it is not sorted using the specified comparator, the results are undefined.  
     * If the list contains multiple elements equal to the specified object, 
     * there is no guarantee which one will be found.  If a null key value is passed in,
     * the algorithm will simply return list.indexOf(null) in the list, resulting in
     * reduced search efficiency on par with O(n).
     * 
     * @param list the list to be searched.
     * @param key the key to be searched for.
	 * @param c Comparator used for element object comparison.
     * @return index of the search key, if it is contained in the list, otherwise
     * a value &lt; 0.
 	 * @throws SpcfIllegalArgumentException if list is null or the comparator is null.
	 * @throws SpcfUnsupportedOperationException The list is read-only or if the list 
	 * contains elements that are not mutually comparable. null comparisons are not supported. 
    */
    public static <T> int search(SpcfList<T> list, T key, ISpcfComparator<T> c)
    {
      	return sCollectionsUtil.doSearch(list, key, c);    	   	
    }
    
    /**
     * Constructs a SpcfCollectionsUtil object.
     * @return SpcfCollectionsUtil implementation object.
     */
    public static SpcfCollectionsUtil createInstance()
    {
    	return SpcfFactory.getInstance().createCollectionsUtil();
    }
    
    /**
     * Forwarding impl methods
     * @param list The list to be sorted.
     */
    protected abstract <T> void doSort(SpcfList<T> list);
    
    /**
     * Forwarding impl methods
     * @param list The list to be sorted.
     * @param c The comparator to use for sorting.
     */
    protected abstract <T> void doSort(SpcfList<T> list, ISpcfComparator<T> c);
    
    /**
     * Forwarding impl methods
     * @param list The list to be searched.
     * @param obj The object to search for.
     * @return The index of the object if it is found, or -1 if it is not.
     */
    protected abstract <T> int doSearch(SpcfList<T> list, T obj);
    
    /**
     * Forwarding impl methods
     * @param list The list to be searched.
     * @param obj The object to search for.
     * @param c The comparator to use to compare objects during searching.
     * @return The index of the object if it is found, or -1 if it is not.
     */
    protected abstract <T> int doSearch(SpcfList<T> list, T obj, ISpcfComparator<T> c);
    
    /**
     * Forwarding impl methods
     * @return An empty SpcfSet.
     */
    protected abstract <T> SpcfSet<T> doEmptySet();
    
    /**
     * Forwarding impl methods
     * @return An empty SpcfList.
     */
    protected abstract <T> SpcfList<T> doEmptyList();
    
    /**
     * Forwarding impl methods
     * @return An empty SpcfMap.
     */
    protected abstract <K,V> SpcfMap<K,V> doEmptyMap();
}
