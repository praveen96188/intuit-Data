/**
 * 
 */
package com.intuit.spc.foundations.portability.util;

import com.intuit.spc.foundations.portability.text.SpcfLocaleInfo;


/**
 * SpcfLocalContext represents the parts of locale in a more granular fashion. The current locale dictates the timezone,
 * currency and language unless they are set separately. If they are set separately, they become the current settings
 * overriding what is represented in the current locale. The locale can also be set to override what the virtual machine 
 * recognizes as the current locale.
 *      - Locale
 *      - Timezone
 *      - Currency
 *      - Language
 *  These values of each are deteremined in this order:
 *  1. a value that has been set
 *  2. the value held in the local context's current locale
 *  3. the value held in the virtual machine's current locale
 *  SpcfLocalContext is representing the context for the current thread. It will be passed on to
 *  child threads.
 * 
 * @author rgroth
 *
 */
public class SpcfLocalContext
{
    private SpcfLocaleInfo mLocale = null;
    private SpcfLanguage mLanguage = null;
    private SpcfTimeZone mTimeZone = null;
    private SpcfCurrency mCurrency = null;
    
    /**
     * Only need a single static instance of the ability to get the per thread
     * instance of the local context. The initalValue() method will only be called
     * the first time tls.get() is called. This is an anonymous class.
     */
    private static InheritableThreadLocal tls = new InheritableThreadLocal()
    {
        protected synchronized Object initialValue()
        {
            return new SpcfLocalContext();
        }
    };
    
    /**
     * Gets the existing instance from thread local storage, or if
     * one doesn't exist, creates it and stores it there. Initially all
     * values are represented by the locale known to the virtual machine.
     * @return the local context for this thread
     */
    public static SpcfLocalContext getInstance()
    {
        return (SpcfLocalContext)tls.get();
    }
    
    /**
     * Gets the SpcfLocaleInfo instance held in this context. Can be
     * the one that was set or the default known to the VM if none was
     * set.
     * 
     * @return the set locale or the default if none set
     */
    public SpcfLocaleInfo getLocale()
    {
        // if we don't have one, get the default from the virtual machine
        if(mLocale == null)
        {
            mLocale = SpcfLocaleInfo.getDefault();
        }
        
        return mLocale;
    }
    
    /**
     * The current value can be changed to the default on the next call to
     * getLcoale() by passing in a null locale.
     * 
     * @param locale the desired locale
     */
    public void setLocale(SpcfLocaleInfo locale)
    {
        mLocale = locale;
    }
    
    /**
     * Get the language from the current locale or the one that has been
     * set manually by setLanguage().
     * @return returns the language that was set or the language
     * contained in the SpcfLocaleInfo.
     */
    public SpcfLanguage getLanguage()
    {
        if(mLanguage == null)
        {
            mLanguage = new SpcfLanguage(getLocale().getLanguage());
        }
        
        return mLanguage;
    }
    
    /**
     * Override the language known to the current locale. Can be set back to
     * the language know to the current locale by passing in null.
     * @param language The language to be used rather than that in the locale. 
     * Null resets to the language known to the locale.
     */
    public void setLanguage(SpcfLanguage language)
    {
        mLanguage = language;
    }
    
    /**
     * Gets the set timezone or the VM's local if not set otherwise.
     * @return returns the set timezone of the local if none has been set
     */
    public SpcfTimeZone getTimeZone()
    {
        if(mTimeZone == null)
        {
            mTimeZone = SpcfTimeZone.getLocalTimeZone();
        }
        
        return mTimeZone;
    }
    
    /**
     * Sets the timezone. Does not effect the local timezone known to the VM.
     * @param timezone setting to null will cause the local timezone to be returned on the
     * next get
     */
    public void setTimeZone(SpcfTimeZone timezone)
    {
        mTimeZone = timezone;
    }

    /**
     * Gets the currency that has been set or that known to the current locale.
     * @return The currency
     */
    public SpcfCurrency getCurrency()
    {
        if(mCurrency == null)
        {
            mCurrency = SpcfCurrency.createInstance();
        }
        
        return mCurrency;
    }
    
    /**
     * Overrides the currency known to the current locale. Set to null to get the VM's local currency 
     * on next getCurrency() call.
     * @param currency
     */
    public void setCurrency(SpcfCurrency currency)
    {
        mCurrency = currency;
    }
}
