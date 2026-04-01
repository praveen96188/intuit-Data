package com.intuit.spc.foundations.portability.util;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.collections.ISpcfIterator;
import com.intuit.spc.foundations.portability.collections.SpcfArrayList;
import com.intuit.spc.foundations.portability.collections.SpcfCollection;

/**
 * SpcfItemizableDecimal abstracts the notion of itemization through portable
 * interface. Itemization decomposes a decimal value to a list of items. An
 * item is comprised of a description and an amount which is abstracted into
 * SpcfItem type.  The itemized decimal can contain items with same description.
 * There is a limitation on the length of description field. See SpcfItem for
 * details.  This class doesn't allow to itemized with null valued item.
 * <p>
 * SpcfItemizableDeicmal adopts the behaviors of SpcfCollection while adding
 * calculation behaviors and getter/setter of a decimal.  Depending on the 
 * number of description/amount pairs it holds, an itemizable decimal can 
 * either be in itemized state or not.
 * </p>
 * <p>
 * SpcfItemizableDecimal is a special type which is valid to use with 
 * SPC Data Access dynamic entity.  If an instance of SpcfItemizableDecimal is
 * not itemized, it is persisted as a decimal.  If it is itemized,
 * it is persisted as the collection.  This special logic is handled by
 * the dynamic entity persistence.  However, this persistence logic is not yet 
 * handled by static entity persistence.   
 * </p>
 * <p>
 * SpcfItemizableDecimal is intended to use in the situation where a single type
 * representation is required and the behaviors/persistence is determined at 
 * runtime. Consider using SpcfItemizableDecimal if you need behaviors/persistence
 * of itemization.  Otherwise, use either SpcfDecimal or SpcfCollection.
 * </p>
 * <p>
 * Note that SpcfItemizableDecimal does not derive from either SpcfDecimal or 
 * SpcfCollection.  It basically holds the instance of an SpcfDecimal and SpcfList.
 * Unlike other types in portablity package, SpcfItemizableDecimal is portable and 
 * created as a concrete class which can be instantiated with new operator.  
 * However, the factory methods are implemented to be consistence with existing
 * design pattern.   
 * </p>
 * <p>
 * SpcfItemizableDecimal is low level class which is not thread safe.  Application
 * should ensure the usage to avoid concurrency issue.  If multiple threads are 
 * accessing the item collection, users need to make sure it is thread-safe.
 * </p>
 * <pre>
 * To create a new SpcfItemizableDecimal instance,
 * 	SpcfItemizableDecimal iDec = new SpcfItemizableDecimal(myDec);
 * 
 * To use as a single decimal,
 * 	iDec.setValue(myDec);
 *  myDec = iDec.getValue();
 *  
 * To use as itemized decimal,
 *  iDec.addItem(new SpcfItem("my description", myDec));
 *  iDec.getIterator();
 * </pre>
 * 
 */
public class SpcfItemizableDecimal
{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 5551873882775623993L;
	/**
	 * Non itemized value
	 */
	private SpcfDecimal mValue;
	/**
	 * Collection of itemized description/amount pairs
	 */
	private SpcfCollection<SpcfItem> mItems;
	

	/**
	 * Create an instance of SpcfItemizableDeicmal with specified decimal.
	 * @param dec the specified decimal
	 * @throws SpcfArgumentNullException if the specified decimal is null
	 */
	public SpcfItemizableDecimal(SpcfDecimal dec) 
	{
		this.setValue(dec);
	}
	
	/**
	 * Set the specified decimal value to this itemizable decimal.  It can
	 * be called regardless of itemization status. 
	 * @param dec specified decimal value
	 * @throws SpcfArgumentNullException if the specified decimal is null
	 */
	public void setValue(SpcfDecimal dec) 
	{ 
		SpcfParamValidator.checkIsNotNull(dec,"dec");
		
		mValue = dec;
	}

	/**
	 * This is a special method with its returns value depending on whether 
	 * this decimal is itemized or not.  If this decimal is not itemized, 
	 * returns the stored single value.  If it is itemized, returns the sum of
	 * the amount of all items. This behavior is intentionally created in order
	 * to avoid the frequent conditional check by providing a method which can 
	 * retrieve a most access value depending on its itemization state.
	 * Beware that you cannot retrieve the value stored using setValue method
	 * if this decimal is itemized. Use getOriginalValue method for retrieving 
	 * the value set by either setValue method or the constructor.
	 * @return the SpcfDecimal instance either the total value or original value.
	 */
	public SpcfDecimal getValue()
	{ 
		if(!isItemized()) {
			return mValue;
		}
		else {
			return this.getTotal();
		}
	}
	
	/**
	 * Returns the value stored using setValue method or the constructor.  
	 * Unlike getValue method, the return value doesn't depend whether 
	 * the decimal is itemized or not.  It always returns the single value 
	 * representation of this itemizable decimal.
	 * @return the value stored using setValue method.
	 */
	public SpcfDecimal getOriginalValue() 
	{
		return mValue;
	}
	
	/**
	 * Determines whether this deicmal is itemized or not.
	 * @return true if this itemizable deicmal contains one or more items,
	 *         false otherwise
	 */
	public boolean isItemized() 
	{ 
		return !this.getNonNullItemCollection().isEmpty();
	}

	/**
	 * Adds specified item (description/amount pair) to this itemizable decimal.
	 * This itemizable decimal allows to add duplicates.
	 * see com.intuit.spc.foundations.portability.collections.SpcfCollection#add
	 * @param item the specified item to be added to this itemizable  decimal
	 * @return true if this itemizable decimal changed as a result of the call.
	 *         false otherwise
	 * @throws SpcfArgumentNullException if specified item is null 
	 */
	public boolean addItem(SpcfItem item) 
	{
		SpcfParamValidator.checkIsNotNull(item, "item");
		
		return this.getNonNullItemCollection().add(item);
	}
	
	
	/**
	 * Adds all of the items in specified collection to this itemizable decimal.
	 * see com.intuit.spc.foundations.portability.collections.SpcfCollection#addAll(SpcfCollection)
	 * @param items the collection of items to add
	 * @return true if the collecton
	 * @throws SpcfArgumentNullException if either specified item collection is null or 
	 * one or more item in specified item collection is null.
	 */
	public boolean addAllItems(SpcfCollection<SpcfItem> items) 
	{
		SpcfParamValidator.checkIsNotNull(items, "items");
		
		for (SpcfItem item : items) {
			SpcfParamValidator.checkIsNotNull(item, "item");
		}
		return this.getNonNullItemCollection().addAll(items);
	}
	
	/**
	 * Retrieves all items of this itemizable decimal.  If it is not 
	 * itemized, return empty collection.
	 * @return the collection containing all items in this itemizable decimal
	 */
	private SpcfCollection<SpcfItem> getAllItems() 
	{ 
		return this.getNonNullItemCollection();
	}
	
	/**
	 * Returns the iterator of the collection of items.  If it is not 
	 * itemized, return iterator of empty collection.
	 * see com.intuit.spc.foundations.portability.collections.SpcfCollection#getIterator()
	 * @return iterator the iterator for all items in this itemizable decimal
	 */
	public ISpcfIterator<SpcfItem> getIterator() 
	{
		return this.getNonNullItemCollection().getIterator();
	}

	/**
	 * Determines whether this itemizable decimal contains the specified item.
	 * Equality of SpcfItem is used to do the comparison. If it is not itemized
	 * returns false.
	 * see com.intuit.spc.foundations.portability.collections.SpcfCollection#contains(Object)
	 * @param item the specified item to check
	 * @return boolean true if the specified item contain in current itemized
	 *         decimal, false otherwise (including null)
	 */
	public boolean containsItem(SpcfItem item) 
	{
		if(!isItemized()) return false;

		return this.getNonNullItemCollection().contains(item);
	}
	
	/**
	 * Determines whether this itemizable decimal contains all specified items.
	 * Equality of SpcfItem is used to do the comparison. If it is not itemized
	 * returns false.
	 * see com.intuit.spc.foundations.portability.collections.SpcfCollection#containsAll(SpcfCollection)
	 * @param items the specified items to check
	 * @return true if all specified items exist in this decimal, 
	 *         false otherwise (including null)
	 */
	public boolean containsAllItems(SpcfCollection<SpcfItem> items) 
	{
		if(!isItemized()) return false;
		
		return this.getNonNullItemCollection().containsAll(items);
	}
	
	/**
	 * Returns the number of items in this itemizable decimal.
	 * If it is not itemized, return zero.
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#getSize()
	 * @return int the number of items
	 */
	public int getSize() 
	{ 
		return this.getNonNullItemCollection().getSize();
	}
	
	/**
	 * Removes all items which has the same description/amount values from
	 * this itemizable decimal.
	 * see com.intuit.spc.foundations.portability.collections.SpcfCollection#remove(Object)
	 * @param item the specified item to remove
	 * @return true if this itemizable decimal is changed, false otherwise (including null)
	 */
	public boolean removeItem(SpcfItem item) 
	{ 
		if(!isItemized()) return false;
		
		boolean isChanged = false;

		while(this.containsItem(item))
		{
			this.getNonNullItemCollection().remove(item);
			isChanged = true;
		}
		return isChanged;
	}
	
	/**
	 * Removes items from this itemizable decimal that are contain in
	 * specified item collection.  If this decimal is not itemized, 
	 * return false.
	 * see com.intuit.spc.foundations.portability.collections.SpcfCollection#removeAll(SpcfCollection)
	 * @param items the specified item collection
	 * @return true if this itemizable decimal is changed, false otherwise (including null)
	 */
	public boolean removeAllItems(SpcfCollection<SpcfItem> items) 
	{ 
		if(!isItemized()) return false;

		if ((items == null) || (items.getSize() == 0)) return false;
		
		boolean isChanged = false;
		
		ISpcfIterator<SpcfItem> iter = items.getIterator();
		while(iter.hasNext())
		{
	        SpcfItem item = iter.next();
		    if (this.containsItem(item)) 
		    {
		        this.removeItem(item);
		        isChanged = true;
		    }
		}
		    
	    return isChanged;	
	}

	/**
	 * Removes all items from this itemizable decimal.  If removal is 
	 * successful, it will change from itemized mode to single valu 
	 * mode. This call is no-op when this itemizable decimal is not 
	 * itemized.
	 * @see com.intuit.spc.foundations.portability.collections.SpcfCollection#clear()
	 */
	public void clearItems() 
	{ 
		if(isItemized()) {
			this.getNonNullItemCollection().clear();
		}
	}

	/**
	 * Returns the hashcode value of this itemizable decimal.
	 * @return int hashcode value of this object
	 */
	public int hashCode() 
	{
		int hash = 0;
		
		if(mValue!=null) 
		{
			hash = mValue.hashCode();
		}

		if(mItems!=null) {
			hash = hash + mItems.hashCode();
		}
		
		return hash;
	}

	/**
	 * Determines whether this itemizable decimal and specified object is 
	 * the same or not. The two itemizable deicimals are equal iff its
	 * single value, its colletion and itemization state are the same.
	 * @param o the specified object to compare
	 * @return true if equal, false otherwise
	 */
	public boolean equals(Object o) 
	{
		if(o == null || !(o instanceof SpcfItemizableDecimal))  {
			return false;
		}

		SpcfItemizableDecimal iDec = (SpcfItemizableDecimal) o;
		
		boolean stateCheck = this.isItemized() == iDec.isItemized();
		boolean singleValueCheck = this.getValue().equals(iDec.getValue());
		boolean multiValueCheck = this.getAllItems().equals(iDec.getAllItems());

		return stateCheck && singleValueCheck && multiValueCheck; 
	}

	/**
	 * Returns the string representation of this itemizable decimal.
	 * @return String the string representation of the single decimal
	 *                if this itemizable decimal is not itemized. 
	 *                the string representation of the item collection 
	 *                if this itemizable decimal is itemized.
	 */
	public String toString() 
	{ 
		if(isItemized()) {
			return this.getNonNullItemCollection().toString();
		}
		else {
			return mValue!=null ? mValue.toString() : "null";
		}
	}

	/**
	 * The private util method to get non-null item collection.
	 * @return item list
	 */
	private SpcfCollection<SpcfItem> getNonNullItemCollection() 
	{
		if(mItems==null) {
			mItems = SpcfArrayList.<SpcfItem>createInstance();
		}
		return mItems;
	}
	
	/**
	 * The util method to get the total amount of all items in this 
	 * itemizable decimal. If not itemized, it will returns decimal 
	 * with value zero.
	 * @return total amount of all items
	 */
	private SpcfDecimal getTotal() 
	{
		SpcfDecimal total = SpcfDecimal.createInstance(0);
		
		for (SpcfItem item : this.getNonNullItemCollection()) {
			total = total.add(item.getAmount());
		}
		
		return total;
	}

	
	/**
	 * This method is added just for the consistency with existing design pattern.
	 * It shall be removed when creation pattern is changed. Consider using 
	 * new operator to create SpcfItemizableDecimal instead of createInstance
	 * method.
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createItemizableDecimal(SpcfDecimal)
	 * @param dec the specified decimal
	 * @return new instance of SpcfItemizableDecimal
	 */
	public static SpcfItemizableDecimal createInstance(SpcfDecimal dec) 
	{
		return SpcfFactory.getInstance().createItemizableDecimal(dec);
	}
}
