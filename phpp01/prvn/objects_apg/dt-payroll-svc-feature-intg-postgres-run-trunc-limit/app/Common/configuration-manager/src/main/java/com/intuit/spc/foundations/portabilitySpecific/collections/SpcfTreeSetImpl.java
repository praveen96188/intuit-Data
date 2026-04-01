/*
 * author		LGM
 * department	SPC Foundations
 * project	    Portability
 * 2004-10-01   Initial Implementation
 *
 * NOTE: All third-party runtime exceptions are caught and re-thrown as
 * portable exceptions
 */
package com.intuit.spc.foundations.portabilitySpecific.collections;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.collections.*;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

import java.util.*;

/**
 * A platform specific implementation of SpcfTreeSet
 */
public class SpcfTreeSetImpl<T> extends SpcfTreeSet<T> implements Cloneable, Set<T>
{

	/**
	 * constant used for serialization
	 */
	private static final long serialVersionUID = -2438068197207943115L;
	
	//Member Variables

	/**
	 * The encapsulated third party runtime TreeSet implementation
	 */
	protected TreeSet<T> mThirdPartyTreeSet;

	//Constructors
	/**
	 * Constructs an empty TreeSet with the specified comparator.
	 *
	 * @param   c   the initial comparator.  (A null value indicates that the keys' natural ordering should be used.)
	 */
	public SpcfTreeSetImpl(ISpcfComparator<T> c)
	{
		mThirdPartyTreeSet = new TreeSet<T>(c);
	}

	/**
	 * Constructs an empty tree set with the default comparator.
	 */
	public SpcfTreeSetImpl()
	{
		mThirdPartyTreeSet = new TreeSet<T>();
	}
	
	/**
	 * Constructs an empty tree set with the default comparator.
	 * @param typeParam the type to store in the SpcfTreeSet
	 */
	public SpcfTreeSetImpl(SpcfClass typeParam)
	{
		this.mTypeParameters = new SpcfClass[] { typeParam };
		mThirdPartyTreeSet = new TreeSet<T>();
	}

	/**
	 * Constructs a TreeSet with the specified third party runtime object.
	 * This constructor allows the user to encapsulate any object that
	 * implements the java.util.TreeSet interface.
	 *
	 * @param   thirdPartyTreeSet java.util.TreeSet implementation
	 * @throws SpcfArgumentNullException if thirdPartyTreeSet is null
	 */
	public SpcfTreeSetImpl(TreeSet<T> thirdPartyTreeSet)
	{
		SpcfParamValidator.checkIsNotNull(thirdPartyTreeSet, "thirdPartyTreeSet");

		mThirdPartyTreeSet = thirdPartyTreeSet;
	}


	/**
	 * Returns the encapsulated third party runtime object
	 * @return a java.util.TreeSet implementation
	 */
	public TreeSet<T> toSpecific()
	{
		return mThirdPartyTreeSet;
	}

	//SpcfTreeSet Overrides

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#addAll
	 * (com.intuit.spc.foundations.portability.collections.SpcfCollection)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(SpcfCollection<T> collection)
	{
		boolean isSetChanged = false;

		SpcfParamValidator.checkIsNotNull(collection, "collection");
		try
		{
			// make a copy of the treeSet so we do not alter the
			// original one if we have an exception part of the way
			// through the adds.
			SpcfTreeSetImpl<T> tmpTreeSet = (SpcfTreeSetImpl<T>) clone();

			ISpcfIterator<? extends T> colIt = collection.getIterator();
			SpcfParamValidator.checkIsNotNull(colIt, "colIt");

			//Use third-party iterator to add all of the elements of one
			//collection into the other
			while (colIt.hasNext())
			{
				if (tmpTreeSet.add(colIt.next()))
				{
					isSetChanged = true;
				}
			}
			// now save the temporary treeSet where we added all of the
			// elements of the collection, to our internal treeSet value
			mThirdPartyTreeSet = tmpTreeSet.toSpecific();

		}
		catch(UnsupportedOperationException e)
		{
			//This collection does not support the addAll method.
			throw new SpcfUnsupportedOperationException(e);
		}
		catch(NullPointerException e)
		{
	        //If the specified collection contains one or more null elements
			//and this collection does not support null elements, or if the
			//specified collection is null.
			throw new SpcfNullPointerException(e);
		}
		return isSetChanged;

	}

	/**
	 * Returns a new instance of SpcfTreeSet containing a shallow copy of the
	 * encapsulated 3rd party TreeSet.
	 *
	 * <p>The clone will fail if the SpcfTreeSet was created using a
	 * decorated TreeSet implementation, such as a thread-safe or read only,
	 * if the scope of the decorator is not public.
	 *
 	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeSet#clone
	 */
	@SuppressWarnings("unchecked")
	@Override
	public SpcfTreeSet<T> clone()
	{
		TreeSet<T> thirdPartyTreeSet;

		//Create a new instance of the TreeSet
		// Cast causes type-check warning
		thirdPartyTreeSet = (TreeSet<T>) mThirdPartyTreeSet.clone();

		//Encapsulate the new reference with a portable object implementation
		return new SpcfTreeSetImpl<T>(thirdPartyTreeSet);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeSet#contains
	 */
	@Override
	public boolean contains(Object obj)
	{
		try
		{
			SpcfParamValidator.checkIsNotNull(obj, "obj");

			return mThirdPartyTreeSet.contains(obj);
		}
		catch (IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
		catch (SpcfClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
		catch (ClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}

	//SpcfCollection overrides

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#add
	 */
	@Override
	public boolean add(T obj)
	{
		boolean returnValue = false;

		// conform with .NET to not allow null objects
		SpcfParamValidator.checkIsNotNull(obj, "obj");
		try
		{
			returnValue = mThirdPartyTreeSet.add(obj);
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
		// .NET throws SpcfIllegalArgumentException so java will also
		catch (ClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
		// we must throw the IllegalArgument instead of the ClassCast Exception
		// because if one adds string and then SpcfStringBuilder, one gets the ClassCast
		// but if one adds SpcfStringBuilder and then string, one gets ArgumentException
		// we want to be consistant on the exceptions thrown.
		catch (SpcfClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}

		return returnValue;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#clear()
	 */
	@Override
	public void clear()
	{
		try
		{
			mThirdPartyTreeSet.clear();
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#remove
	 */
	@Override
	public boolean remove(Object obj)
	{
		SpcfParamValidator.checkIsNotNull(obj, "obj");

		try
		{
			return mThirdPartyTreeSet.remove(obj);
		}
		catch(UnsupportedOperationException e)
		{
			throw new SpcfUnsupportedOperationException(e);
		}
		catch (IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
		catch (ClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
		// we must throw the IllegalArgument instead of the ClassCast Exception
		// because if one adds string and then SpcfStringBuilder, one gets the ClassCast
		// but if one adds SpcfStringBuilder and then string, one gets ArgumentException
		// we want to be consistant on the exceptions thrown.
		catch (SpcfClassCastException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#getSize()
	 */
	@Override
	public int getSize()
	{
		return mThirdPartyTreeSet.size();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#getIterator()
	 */
	@Override
	public ISpcfIterator<T> getIterator()
	{
		//Create a platform specific implementation of ISpcfIterator (portable)
		return new SpcfIteratorImpl<T>( mThirdPartyTreeSet.iterator() );
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeSet#first()
	 */
	@Override
	public T first()
	{
		return mThirdPartyTreeSet.first();	
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeSet#last()
	 */
	@Override
	public T last()
	{
		return mThirdPartyTreeSet.last();		
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfTreeSet#comparator()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public ISpcfComparator<T> comparator()
	{
		return (ISpcfComparator<T>)mThirdPartyTreeSet.comparator();
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfArrayList#toArray()
	 */
	@Override
	public Object[] toArray() 
	{
		return mThirdPartyTreeSet.toArray();
		//return SpcfToArrayImpl.toArray(this);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfArrayList#toArray()
	 */
	@Override
	public <E> E[] toArray(E[] a) 
	{
		SpcfParamValidator.checkIsNotNull(a, "Array Argument");
		try
		{
			return mThirdPartyTreeSet.toArray(a);
		}
		catch(ClassCastException e)
		{
			throw new SpcfClassCastException(e);
		}
		catch(ArrayStoreException ase)
		{
			throw new SpcfClassCastException(ase);			
		}
		//return SpcfToArrayImpl.toArray(a, this);
	}

	/**
	 * Returns the number of elements in this set (its cardinality). 
	 * If this set contains more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
	 * @return the number of elements in this set (its cardinality).
	 */
	public int size()
	{
		return mThirdPartyTreeSet.size();
	}
	
	/**
	 * Retains only the elements in this set that are contained in the specified collection
	 * In other words, removes from this set all of its elements that are not contained 
	 * in the specified collection. If the specified collection is also a set, 
	 * this operation effectively modifies this set so that its value is the 
	 * intersection of the two sets.
	 * @param c collection that defines which elements this set will retain.
	 * @return true if this collection changed as a result of the call.
	 * @exception SpcfUnsupportedOperationException if the retainAll method is not supported by this Collection.
	 * @exception SpcfClassCastException if the types of one or more elements in this set are incompatible with the specified collection
	 * @exception SpcfArgumentNullException if this set contains a null element and the specified collection does not support null elements
	 * @exception SpcfArgumentNullException if the specified collection is null
	 */
	public boolean retainAll(Collection<?> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection is null");
		
		try
		{
			return mThirdPartyTreeSet.retainAll(c);
		}
		catch (UnsupportedOperationException e1)
		{
			throw new SpcfUnsupportedOperationException(e1);
		}
		catch (ClassCastException e2)
		{
			throw new SpcfClassCastException(e2);
		}
		catch (NullPointerException e3)
		{
			throw new SpcfArgumentNullException(e3);
		}
	}
	
	/**
	 * Returns true if this set contains all of the elements of the specified collection. 
	 * If the specified collection is also a set, this method returns true if it is a subset of this set.
	 * @param c collection to be checked for containment in this set
	 * @return true if this set contains all of the elements of the specified collection
	 * @exception SpcfClassCastException if the types of one or more elements in the specified collection are incompatible with this set (optional). 
     * @exception SpcfArgumentNullException if the specified collection contains one or more null elements and this set does not support null elements (optional). 
     * @exception SpcfArgumentNullException if the specified collection is null.
	 */
	public boolean containsAll(Collection<?> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection is null");
		
		try
		{
			return mThirdPartyTreeSet.containsAll(c);
		}
		catch (ClassCastException e2)
		{
			throw new SpcfClassCastException(e2);
		}
		catch (NullPointerException e3)
		{
			throw new SpcfArgumentNullException(e3);
		}
	}
	
	/**
	 * Removes from this set all of its elements that are contained in the specified collection (optional operation). 
	 * If the specified collection is also a set, this operation effectively modifies this set so that its value is the asymmetric set difference of the two sets.
	 * @param c collection that defines which elements will be removed from this set
	 * @return true if this set changed as a result of the call.
	 * @exception SpcfUnsupportedOperationException if the removeAll  method is not supported by this Collection. 
	 * @exception SpcfClassCastException if the types of one or more elements in this set are incompatible with the specified collection (optional). 
	 * @exception SpcfArgumentNullException if this set contains a null element and the specified collection does not support null elements (optional). 
	 * @exception SpcfArgumentNullException if the specified collection is null. 
	 */
	public boolean removeAll(Collection<?> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection is null");
		
		try
		{
			return mThirdPartyTreeSet.removeAll(c);
		}
		catch (UnsupportedOperationException e1)
		{
			throw new SpcfUnsupportedOperationException(e1);
		}
		catch (ClassCastException e2)
		{
			throw new SpcfClassCastException(e2);
		}
		catch (NullPointerException e3)
		{
			throw new SpcfArgumentNullException(e3);
		}
	}
	
	/**
	 * Adds all of the elements in the specified collection to this set if they're 
	 * not already present (optional operation). If the specified collection is 
	 * also a set, the addAll operation effectively modifies this set so that its 
	 * value is the union of the two sets. The behavior of this operation is unspecified 
	 * if the specified collection is modified while the operation is in progress.
	 * @param c collection whose elements are to be added to this set.
	 * @return true if this set changed as a result of the call.
	 * @exception SpcfUnsupportedOperationException if the addAll method is not supported by this set. 
	 * @exception SpcfClassCastException if the class of some element of the specified collection prevents it from being added to this set. 
	 * @exception SpcfArgumentNullException if the specified collection contains one or more null elements and this set does not support null elements, or if the specified collection is null. 
	 * @exception SpcfIllegalArgumentException if some aspect of some element of the specified collection prevents it from being added to this set.
	 */
	public boolean addAll(Collection<? extends T> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection is null");
		
		try
		{
			return mThirdPartyTreeSet.addAll(c);
		}
		catch (UnsupportedOperationException e1)
		{
			throw new SpcfUnsupportedOperationException(e1);
		}
		catch (ClassCastException e2)
		{
			throw new SpcfClassCastException(e2);
		}
		catch (NullPointerException e3)
		{
			throw new SpcfArgumentNullException(e3);
		}
		catch (IllegalArgumentException e3)
		{
			throw new SpcfIllegalArgumentException(e3);
		}
	}
}
