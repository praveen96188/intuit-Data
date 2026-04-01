/**
 * 
 */
package com.intuit.spc.foundations.portability.util;

/**
 * Portable representation of currency. 
 * 
 * @author rgroth
 *
 */
public abstract class SpcfCurrency
{
    public static SpcfCurrency createInstance()
    {
        return new com.intuit.spc.foundations.portabilitySpecific.util.SpcfCurrencyImpl();
    }
    
    /**
     * Returns the ISO 4217 Currency Code for this currency instance.
     * @return
     */
    public abstract String getCurrencyCode();
    
    /**
     * change the currency to that which is passed in. This will be recognized
     * instead of that represented by the locale.
     */
    public abstract void setCurrencyCode(String currencyCode);
    
    /**
     * Returns the symbol associated with the ISO 4217 Currency code for this currency instance.
     * @return
     */
    public abstract String getCurrencySymbol();

}
