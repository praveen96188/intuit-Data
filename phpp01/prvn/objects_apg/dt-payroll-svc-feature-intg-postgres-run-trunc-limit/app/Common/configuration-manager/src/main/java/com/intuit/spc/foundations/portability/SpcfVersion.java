package com.intuit.spc.foundations.portability;

/**
 * SpcfVersion class provides a formal representation for versions, including
 * SPCF portable language version and platform version.
 * @author gwang
 */
public abstract class SpcfVersion 
{   
    /**
     * Return the major string.
     * @return the major string.
     */
    public abstract String getMajor();
 
    /**
     * Return the minor string.
     * @return the minor string.
     */    
    public abstract String getMinor();
    
    /**
     * Return the major and minor string.
     * @return the major and minor string.
     */
    public abstract String getMajorAndMinor();
    
    /**
     * Return the release number.
     * @return the release number.
     */
    public abstract String getRelease();
    
    /**
     * Return the patch number.
     * @return the patch number.
     */
    public abstract String getPatch();
    
    /**
     * Return the whole string.
     * @return the whole string.
     */
    public abstract String toString();
}
