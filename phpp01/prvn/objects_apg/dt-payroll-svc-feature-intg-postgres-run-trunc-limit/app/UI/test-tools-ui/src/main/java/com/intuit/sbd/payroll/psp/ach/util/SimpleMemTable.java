/**
* SimpleMemTable.java
* 
* Copyright (c) 1999-2000 PayCycle, Inc. All Rights Reserved.
* 
* This software is the confidential and proprietary information of
* PayCycle, Inc. ("Confidential Information").  You shall not
* disclose such Confidential Information and shall use it only in
* accordance with the terms of the license agreement you entered into
* with PayCycle.
* 
* PAYCYCLE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
* SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
* IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
* PURPOSE, OR NON-INFRINGEMENT. PAYCYCLE SHALL NOT BE LIABLE FOR ANY DAMAGES
* SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
* THIS SOFTWARE OR ITS DERIVATIVES.
* 
* CopyrightVersion 1.0
*/

package com.intuit.sbd.payroll.psp.ach.util;

import java.util.Enumeration;
import java.io.Serializable;


// can't be replaced using a Java 2 "Collections" API
// since this package must also run on JDK 1.1


/**
 * This class implements a special purpose hashtable.  It works like a
 * normal <code>java.util.Hashtable</code> except that: <OL>
 *
 *	<LI> Keys to "get" are strings which are known to be interned,
 *	so that "==" is used instead of "String.equals".  (Interning
 *	could be document-relative instead of global.)
 *
 *	<LI> It's not synchronized, since it's to be used only by
 *	one thread at a time.
 *
 *	<LI> The keys () enumerator allocates no memory, with live
 *	updates to the data disallowed.
 *
 *	<LI> It's got fewer bells and whistles:  fixed threshold and
 *	load factor, no JDK 1.2 collection support, only keys can be
 *	enumerated, things can't be removed, simpler inheritance; more.
 *
 *	</OL>
 *
 * <P> The overall result is that it's less expensive to use these in
 * performance-critical locations, in terms both of CPU and memory,
 * than <code>java.util.Hashtable</code> instances.  In this package
 * it makes a significant difference when normalizing attributes,
 * which is done for each start-element construct.
 *
 * @version $Revision: 1.3 $
 */
public final class SimpleMemTable implements Enumeration, Serializable
{
	// entries ...
	private Entry		table[];

	// currently enumerated key
	private Entry current = null;
	private int currentBucket = 0;

	private int count;
	private int threshold;

	private static final float loadFactor = 0.75f;

	/**
	* Constructs a new, empty hashtable with the specified initial 
	* capacity.
	* @param      initialCapacity   the initial capacity of the hashtable.
	*/
	public SimpleMemTable (int initialCapacity) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException ("Illegal Capacity: "
								+ initialCapacity);

		if (initialCapacity == 0)
			initialCapacity = 1;
	
		table = new Entry [initialCapacity];
		threshold = (int)(initialCapacity * loadFactor);
	}

	/**
	* Constructs a new, empty hashtable with a default capacity.
	*/
	public SimpleMemTable() {
		this (11);
	}

	public void clear () {
		count = 0;
		currentBucket = 0;
		current = null;
		for (int i = 0; i < table.length; i++)
			table [i] = null;
	}

	/**
	* Returns the number of keys in this hashtable.
	* @return  the number of keys in this hashtable.
	*/
	public int size()	{
		return count;
	}

	/**
	* Returns an array of keys in this hashtable.
	* @return	 an array of keys in this hashtable.
	*/
	public Object[] getKeyArray () {
		Object[]		res = new Object[count];
		int			i = 0;
		Enumeration	keys = keys ();
		while (keys.hasMoreElements ()) {
			res[i++] = keys.nextElement ();
		}
		
		return res;
	}

	/**
	* Returns an enumeration of the keys in this hashtable.
	* @return  an enumeration of the keys in this hashtable.
	* @see     java.util.Enumeration
	*/
	public Enumeration keys() {
		currentBucket = 0;
		current = null;
		return this;
	}

	/**
	* Used to view this as an enumeration; returns true if there
	* are more keys to be enumerated.
	*/
	public boolean hasMoreElements () {
		if (current != null)
			return true;

		while (currentBucket < table.length) {
			current = table [currentBucket++];
			if (current != null)
				return true;
		}
		return false;
	}

	/**
	* Used to view this as an enumeration; returns the next key
	* in the enumeration.
	*/
	public Object nextElement () {
		Object retval;

		if (current == null)
			throw new IllegalStateException ();

		retval = current.key;
		current = current.next;
		return retval;
	}

	/**
	* Returns the value to which the specified key is mapped in this
	* hashtable ... the key is interned.
	*/
	/**
	* Returns the value to which the specified key is mapped in this hashtable.
	*/
	public Object getInterned (Object key) {
		Entry tab[] = table;
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index] ; e != null ; e = e.next) {
			if ((e.hash == hash) && (e.key == key))
			return e.value;
		}
		return null;
	}

	/**
	* Returns the value to which the specified key is mapped in this
	* hashtable.
	*/
	public Object get (Object key) {
		Entry tab[] = table;
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index] ; e != null ; e = e.next) {
			if ((e.hash == hash) && e.key.equals(key))
			return e.value;
		}
		return null;
	}

	/**
	* Increases the capacity of and internally reorganizes this 
	* hashtable, in order to accommodate and access its entries more 
	* efficiently.  This method is called automatically when the 
	* number of keys in the hashtable exceeds this hashtable's capacity 
	* and load factor. 
	*/
	private void rehash() {
		int oldCapacity = table.length;
		Entry oldMap[] = table;

		int newCapacity = oldCapacity * 2 + 1;
		Entry newMap[] = new Entry[newCapacity];

		threshold = (int)(newCapacity * loadFactor);
		table = newMap;

		for (int i = oldCapacity ; i-- > 0 ;) {
			for (Entry old = oldMap[i] ; old != null ; ) {
				Entry e = old;
				old = old.next;

				int index = (e.hash & 0x7FFFFFFF) % newCapacity;
				e.next = newMap[index];
				newMap[index] = e;
			}
		}
	}

	/**
	* Maps the specified <code>key</code> to the specified 
	* <code>value</code> in this hashtable. 
	*
	* <P>The value can be retrieved by calling the <code>get</code> method 
	* with a key that is equal to the original key. 
	*/
	public Object put (Object key, Object value) {
		// Makes sure the key is not already in the hashtable.
		Entry tab[] = table;
		int hash = key.hashCode();
		int index = (hash & 0x7FFFFFFF) % tab.length;
		for (Entry e = tab[index] ; e != null ; e = e.next) {
			if ((e.hash == hash) && (e.key == key)) {
				Object old = e.value;
				e.value = value;
				return old;
			}
		}

		if (count >= threshold) {
			// Rehash the table if the threshold is exceeded
			rehash();

			tab = table;
			index = (hash & 0x7FFFFFFF) % tab.length;
		} 

		// Creates the new entry.
		Entry e = new Entry(hash, key, value, tab[index]);
		tab[index] = e;
		count++;
		return null;
	}

	/**
	* Hashtable collision list.
	*/
	private static class Entry {
		int		hash;
		Object	key;
		Object	value;
		Entry	next;

		protected Entry(int hash, Object key, Object value, Entry next) {
			this.hash = hash;
			this.key = key;
			this.value = value;
			this.next = next;
		}
	}
}
