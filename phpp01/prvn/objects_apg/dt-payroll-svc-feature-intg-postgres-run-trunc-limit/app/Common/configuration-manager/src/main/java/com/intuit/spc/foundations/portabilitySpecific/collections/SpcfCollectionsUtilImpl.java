package com.intuit.spc.foundations.portabilitySpecific.collections;

import java.util.Comparator;

import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.collections.ISpcfComparator;
import com.intuit.spc.foundations.portability.collections.SpcfArraysUtil;
import com.intuit.spc.foundations.portability.collections.SpcfCollectionsUtil;
import com.intuit.spc.foundations.portability.collections.SpcfList;
import com.intuit.spc.foundations.portability.collections.SpcfSet;
import com.intuit.spc.foundations.portability.collections.SpcfMap;

/**
 * 
 * SpcfCollectionsUtilImpl provides concrete implementation for SpcfCollectionsUtil. 
 * SpcfCollectionsUtil contains common utility methods for searching 
 * and sorting SpcfList collections.
 */
public class SpcfCollectionsUtilImpl extends SpcfCollectionsUtil 
{
	class SpcfDecoratorComparator<T> implements ISpcfComparator<Object>
	{
		Comparator<T> mComparator;
		
		public SpcfDecoratorComparator(Comparator<T> c)
		{
			this.mComparator = c;
		}
		
		@SuppressWarnings("unchecked")
		public int compare(Object arg0, Object arg1) 
		{
			return mComparator.compare((T)arg0, (T)arg1);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> SpcfSet<T> doEmptySet()
	{
		return SpcfEmptySetImpl.<T>getInstance();
	}
	
	@SuppressWarnings("unchecked")
	protected <T> SpcfList<T> doEmptyList()
	{
		return SpcfEmptyListImpl.<T>getInstance();
	}
	
	@SuppressWarnings("unchecked")
	protected <K,V> SpcfMap<K,V> doEmptyMap()
	{
		return SpcfEmptyMapImpl.<K,V>getInstance();
	}

	@SuppressWarnings("unchecked")
	protected <T> void doSort(SpcfList<T> list)
	{
		SpcfParamValidator.checkIsNotNull(list, "List Argument");
		if (list.getSize() > 0)
		{
			Object[] listArray = list.toArray();
			SpcfArraysUtil.sort(listArray);
			for(int i = 0; i < listArray.length; ++i)
			{
				list.setItem(i, (T)listArray[i]);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	protected <T> void doSort(SpcfList<T> list, ISpcfComparator<T> comparator)
	{
		SpcfParamValidator.checkIsNotNull(list, "List Argument");
		SpcfParamValidator.checkIsNotNull(comparator, "Comparator Argument");
		
		if (list.getSize() > 0)
		{
			Object[] listArray = list.toArray();
			SpcfArraysUtil.sort(listArray, new SpcfDecoratorComparator<T>(comparator));
			for(int i = 0; i < listArray.length; ++i)
			{
				list.setItem(i, (T)listArray[i]);
			}
		}
	}

	protected <T> int doSearch(SpcfList<T> list, T obj)
	{
		SpcfParamValidator.checkIsNotNull(list, "List Argument");
		//SpcfParamValidator.checkIsNotNull(obj, "Object Argument");
        if (obj == null) return list.indexOf(obj);
		
		int index = -1;
		if (list.getSize() > 0)
		{
			Object[] a = list.toArray(new Object[list.getSize()]);
			index = SpcfArraysUtil.search(a, obj);
		}
		
		return index;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> int doSearch(SpcfList<T> list, T obj, ISpcfComparator<T> c)
	{
		SpcfParamValidator.checkIsNotNull(list, "List Argument");
		SpcfParamValidator.checkIsNotNull(c, "Comparator Argument");

        if (obj == null) return list.indexOf(obj);
		
		int index = -1;
		if (list.getSize() > 0)
		{
			Object[] a = list.toArray(new Object[list.getSize()]);
			index = SpcfArraysUtil.search(a, (Object)obj, (ISpcfComparator)c);
		}
		
		return index;
	}
}
