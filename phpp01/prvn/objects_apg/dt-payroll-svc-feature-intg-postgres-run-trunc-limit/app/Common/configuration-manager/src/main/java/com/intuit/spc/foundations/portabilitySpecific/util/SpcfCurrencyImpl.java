/**
 * 
 */
package com.intuit.spc.foundations.portabilitySpecific.util;

import com.intuit.spc.foundations.portability.util.SpcfCurrency;
import com.intuit.spc.foundations.portability.text.SpcfLocaleInfo;
import com.intuit.spc.foundations.portabilitySpecific.text.SpcfLocaleInfoUtility;
import java.util.Currency;
import java.util.Locale;

/**
 * Adapter to the native representation of Currency. Implements the
 * portable interface defined by SpcfCurrency.
 * 
 * @author rgroth
 *
 */
public class SpcfCurrencyImpl extends SpcfCurrency
{

    private Currency currency;
    
    public SpcfCurrencyImpl()
    {
        currency = Currency.getInstance(Locale.getDefault());
    }
    
    public SpcfCurrencyImpl(String currencyCode)
    {
        currency = Currency.getInstance(currencyCode);
    }
    
    public SpcfCurrencyImpl(SpcfLocaleInfo locale)
    {
        currency = Currency.getInstance(SpcfLocaleInfoUtility.getLocale(locale));
    }
    
    public SpcfCurrencyImpl(Locale locale)
    {
        currency = Currency.getInstance(locale);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfCurrency#getCurrencyCode()
     */ 
    @Override
    public String getCurrencyCode()
    {
        return currency.getCurrencyCode();
    }

    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfCurrency#setCurrencyCode(String currencyCode)
     */ 
    @Override
    public void setCurrencyCode(String currencyCode)
    {
        currency = Currency.getInstance(currencyCode);
    }

    /**
     * @see com.intuit.spc.foundations.portability.util.SpcfCurrency#getCurrencySymbol()
     */ 
    @Override
    public String getCurrencySymbol()
    {
        return currency.getSymbol();
    }

}
