package com.intuit.spc.foundations.portabilitySpecific.collections;

import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfClassCastException;
import com.intuit.spc.foundations.portability.SpcfUnsupportedOperationException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.collections.*;
import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * This class implements a resizable set backed by a hash table. It makes no
 * guarantees as to the iteration order of the set; This class permits
 * the null element.
 */
public class SpcfHashSetImpl<T> extends SpcfHashSet<T> implements Set<T>
{

	/**
	 * serial version UID for serialization
	 */
	private static final long serialVersionUID = -8762189880560561963L;
	
	/**
	 * The encapsulated third party runtime object
	 */
	private HashSet<T> mThirdPartySet;
	
	/**
	 * construtor to create an empty HashSet with the initial capacity of 10
	 */
	public SpcfHashSetImpl()
	{
		mThirdPartySet = new HashSet<T>(10);
	}
	
	/**
	 * construtor which defines the type to store in the HashSet
	 * @param typeParam the type to store in the HashSet
	 */
	public SpcfHashSetImpl(SpcfClass typeParam)
	{
		mTypeParameters = new SpcfClass[] { typeParam };
		mThirdPartySet = new HashSet<T>(10);
	}
	
	/**
	 * constructor which will populate the HashSet with the given collection
	 * @param c the collection to populate the HashSet with
	 */
	public SpcfHashSetImpl(SpcfCollection<T> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection is null");
		
		mThirdPartySet = new HashSet<T>();
		addAll(c);
	}

	/**
	 * construtor which take the initial capacity for the HashSet
	 * @param initialCapacity the initial capacity of the HashSet
	 */
	public SpcfHashSetImpl(int initialCapacity)
	{
		try
		{
			mThirdPartySet = new HashSet<T>(initialCapacity);
		}
		catch(IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}
	
	/**
	 * constructor which takes the intitial capacity and the type to store in the HashSet
	 * @param initialCapacity the initial capacity of the HashSet
	 * @param typeParam the type to store in the HashSet
	 */
	public SpcfHashSetImpl(int initialCapacity, SpcfClass typeParam)
	{
		try
		{
			mTypeParameters = new SpcfClass[] { typeParam };
			mThirdPartySet = new HashSet<T>(initialCapacity);
		}
		catch(IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);
		}
	}

	/**
	 * constructor which takes a java HashSet
	 * @param hashSet the hashset to encapsulate
	 */
	protected SpcfHashSetImpl(HashSet<T> hashSet)
	{
		SpcfParamValidator.checkIsNotNull(hashSet, "hashSet is null");
		mThirdPartySet = hashSet;		
	}

	/**
	 * Adds the specified element to this set if it is not already present.
	 */
	@Override
	public boolean add(T o)
	{
		return mThirdPartySet.add(o);
	}

	/**
	 * Adds all of the elements in the specified collection to this set if they're not already present.
	 */
	@Override
	public boolean addAll(SpcfCollection<T> c)
	{
		SpcfParamValidator.checkIsNotNull(c, "collection is null");
		boolean changed = false;
		
		for(T t : c) 
		{
			if ( mThirdPartySet.add(t))
			{
				changed = true;
			}
		}
		
		return changed;
	}

	/**
	 * Returns a shallow copy of this HashSet instance: the elements themselves are not cloned.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public SpcfHashSet<T> clone()
	{
		HashSet<T> thirdPartyHashSet;

		//Create a new instance of the TreeSet
		// Cast causes type-check warning
		thirdPartyHashSet = (HashSet<T>) mThirdPartySet.clone();

		//Encapsulate the new reference with a portable object implementation
		return new SpcfHashSetImpl<T>(thirdPartyHashSet);
	}

	/**
	 * Returns true if this set contains the specified element.
	 */
	@Override
	public boolean contains(Object obj)
	{
		return mThirdPartySet.contains(obj);
	}

	/**
	 * Removes the specified element from this set if it is present.
	 */
	@Override
	public boolean remove(Object obj)
	{
		return mThirdPartySet.remove(obj);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#clear()
	 */
	@Override
	public void clear() 
	{
		mThirdPartySet.clear();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#getSize()
	 */
	@Override
	public int getSize() 
	{
		return mThirdPartySet.size();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollectionIterable#getIterator()
	 */
	@Override
	public ISpcfIterator<T> getIterator() 
	{
		//Create a platform specific implementation of ISpcfIterator (portable)
		return new SpcfIteratorImpl<T>( mThirdPartySet.iterator() );
	}

	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#getSize()
	 */
	@Override
	public Object[] toArray() {
		return mThirdPartySet.toArray();
		//return SpcfToArrayImpl.toArray(this);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#toArray()
	 */
	@Override
	public <E> E[] toArray(E[] a) {
		SpcfParamValidator.checkIsNotNull(a, "Array Argument");
		try
		{
			return mThirdPartySet.toArray(a);
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
	 * Returns the encapsulated third party runtime object
	 * @return a HashSet<T> implementation
	 */
	public HashSet<T> toSpecific()
	{
		return mThirdPartySet;
	}
	
	/**
	 * Returns the number of elements in this set (its cardinality). 
	 * If this set contains more than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
	 * @return the number of elements in this set (its cardinality).
	 */
	public int size()
	{
		return mThirdPartySet.size();
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
			return mThirdPartySet.retainAll(c);
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
			return mThirdPartySet.containsAll(c);
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
			return mThirdPartySet.removeAll(c);
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
			return mThirdPartySet.addAll(c);
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
