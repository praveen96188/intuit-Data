package com.intuit.spc.foundations.portability.util;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfArgumentOutOfRangeException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfParamValidator;

/**
 * SpcfItem represents an immutable description and amount pair.  A 
 * description is represented as a string with 232 characters limit 
 * and an amount is represented as SpcfDecimal. It is used as an entry
 * for SpcfItemizableDecimal.  In SpcfPair, the key is used to identify 
 * the uniquenes of a pair.  Unlike SpcfPair, the description of SpcfItem 
 * is not a key.  The combination of both description and amount identify
 * an item. 
 * 
 * The following explains why the size of description is limited to 232.
 * SpcfItemizableDecimal has one-to-many relationship with SpcfItem.  As 
 * SpcfItemizableDecimal is first-class core data type which can be persist
 * with dynamic entity, there is a limitation on the size of each column which
 * is 255 (it is subject to change in the future).  SpcfItem is converted to 
 * String representation which should not exceed 255 in length.  The size of
 * description field is limited to 255-maximum number of character
 * a decimal can have (13 digit for the whole part + 8 for the fractional part).
 * When dynamic entity persistance is changed to allow more than 255 limit, 
 * these limit shall be updated as well.
 */
public class SpcfItem {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7932376499581255615L;
	/**
	 * Description of this item
	 */
	private String mDescription = null;
	/**
	 * Amount of this item
	 */
	private SpcfDecimal mAmount = null;
	/**
	 * Maximum size allowed for lenght of Description 
	 * 255-Sizeof(IntegralPart+FrationalPart+DecimalPoint+Delimiter) = 255-24 = 232
	 */
	public final static int MaxDescriptionLength = 255-(SpcfDecimal.MaxIntDigits+SpcfDecimal.MaxScale+1+1);
	
	/**
	 * Creates a new instance of an SpcfItem with specified 
	 * description and amount
	 * @param description the specified description
	 * @param amount the specified amount
	 * @throws SpcfArgumentNullException if either specified description
	 *         or amount is null.
	 * @throws SpcfArgumentOutOfRangeException if specified description is empty
	 *         or greater than 232 characters.
	 */
	public SpcfItem(String description, SpcfDecimal amount) 
	{
		init(description, amount);
	}
	
	/**
	 * Init
	 * @param description
	 * @param amount
	 */
	private void init(String description, SpcfDecimal amount) 
	{
		SpcfParamValidator.checkIsNotNullOrBlankString(description, "description");
		SpcfParamValidator.checkIsNotNull(amount, "amount");
		
		if(description.length() > MaxDescriptionLength) {
			throw new SpcfArgumentOutOfRangeException("description", description);
		}
		
		mDescription = description;
		mAmount = amount;
	}

	/**
	 * Retrieves the description of this item.
	 * @return The description of this item
	 */
	public String getDescription()
	{
		return mDescription;
	}
	
	/**
	 * Retrieves the amount of this SpcfItem.
	 * @return The amount of this item
	 */
	public SpcfDecimal getAmount()
	{
		return mAmount;
	}
	
	/**
	 * Determines whether the specified object is the same as this object. 
	 * Equality of two SpcfItem is determined by comparing their description
	 * and amount.  
	 * @param o The object to compare for equality.
	 * @return True if the description and amount are equal; Otherwise, false.
	 */
	@SuppressWarnings("unchecked")
	public boolean equals(Object o)
	{
		if(o == null || !(o instanceof SpcfItem))
		{
			return false;
		}
	    SpcfItem i = (SpcfItem)o;
		
	    // mDescription and mAmount cannot be null
	    return mDescription.equals(i.getDescription()) && mAmount.equals(i.getAmount());
	}
	
	/**
	 * Returns the hash code for the current SpcfItem class.  If objects are 
	 * determined to be equal according to the equals method, then the hash code
	 * should be identical for equal objects.  Objects having the same hash code
	 * are not guaranteed to be equal however.
	 * @return The int hash code for the current object.
	 */
	public int hashCode()
	{
		return mDescription.hashCode() + mAmount.hashCode();
	}
	
	/**
	 * This method is added just for the consistency with existing design pattern.
	 * It shall be removed when creation pattern is changed. Consider using 
	 * new operator to create SpcfItem instead of createInstance method.
	 * @param description the description of the item
	 * @param amount the amount of the item
	 * @return the new instance of SpcfItem
     * @throws SpcfArgumentNullException - if specified descripiton/amount is null
     * @throws SpcfArgumentOutOfRangeException - if specified description is empty
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createItem(String, SpcfDecimal)
	 */
	public static SpcfItem createInstance(String description, SpcfDecimal amount) 
	{
		return SpcfFactory.getInstance().createItem(description, amount);
	}
	
}
