/**
 * Creating this or setting this does not impact the VM's notion of the current language as
 * contained in the native locale. Supports ISO 639-1 values (2 characters). Can also be empty string.
 */
package com.intuit.spc.foundations.portability.util;

import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;

/**
 * Representation of language separate from locale so that they can
 * vary independently. ISO 639-1 2 character language string. 
 * 
 * @author rgroth
 *
 */
public class SpcfLanguage
{
    private String mLanguage = "";

    /**
     * Set to ISO 639-1 values. Will throw an exception if set to null.
     * Can be the empty string.
     * @param language ISO 639 value or empty string. Null causes exception.
     */
    public SpcfLanguage(String language)
    {
        setLanguage(language);
    }
    
    /**
     * Returns ISO 639-1 2 character represenation of language. Can be empty string.
     */
    public String toString()
    {
        return mLanguage;
    }
    
    /**
     * Returns ISO 639-1 2 character represenation of language. Can be empty string.
     * 
     * @return ISO 639-1 2 character language representation.
     */
    public String getLanguage()
    {
        return mLanguage;
    }
    
    /**
     * Set to ISO 639-1 values. Will throw an exception if set to null.
     * Can be the empty string. Only lengths of 0 or 2 allowed.
     * @param language ISO 639 value or empty string. Null causes exception.
     */
    public void setLanguage(String language)
    {
        SpcfParamValidator.checkIsNotNull(language, "language");
        int length = language.length();
        if(!(length == 0 || length == 2))
        {
            throw new SpcfIllegalArgumentException("Must use ISO 639-1 (2 character) or empty string");
        }
        mLanguage = language;
    }
    
    /**
     * Compares this SpcfLanguage instance to the provided instance.
     * Equality is based on the contained language strings being equal.
     * 
     * @param language
     * @return true if they are equal language strings, false otherwise.
     */
    public boolean equals(SpcfLanguage language)
    {
        return mLanguage.equals(language.toString());
    }
    
    /**
     * Compares this SpcfLanguage instance to the provided instance.
     * Equality is based on the contained language strings being equal.
     * Will throw class cast exception if param o is not of type SpcfLanguage.
     */
    public boolean equals(Object o)
    {
        return this.equals((SpcfLanguage)o);
    }
    
    /**
     * Compares the given string to the internal string representation of the language.
     * @param s a language string
     * @return true if the provided string is equal to the internal language string.
     */
    public boolean equals(String s)
    {
        return mLanguage.equals(s);
    }
    
    /**
     * Hashcode based on the internal string's hashcode.
     */
    public int hashCode()
    {
        return mLanguage.hashCode();
    }

}
