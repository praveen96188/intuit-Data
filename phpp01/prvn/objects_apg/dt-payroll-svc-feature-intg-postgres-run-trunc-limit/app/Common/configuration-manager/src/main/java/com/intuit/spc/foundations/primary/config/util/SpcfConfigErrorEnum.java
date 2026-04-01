package com.intuit.spc.foundations.primary.config.util;

import com.intuit.spc.foundations.portability.SpcfIdentifierEnum;

/**
 * A class that defines error codes for the CMS.
 */
public class SpcfConfigErrorEnum extends SpcfIdentifierEnum
{
    /**
     * Single Argument Constructor
     * 
     * @param id
     */
    private SpcfConfigErrorEnum(int id)
    {
        super(id);
    }

    /**
     * Configuration Error Enum - InvalidConversion
     */
    public final static SpcfConfigErrorEnum InvalidConversion = new SpcfConfigErrorEnum(1);
    /**
     * Configuration Error Enum - MissingConfig
     */
    public final static SpcfConfigErrorEnum MissingConfig = new SpcfConfigErrorEnum(2);
    /**
     * Configuration Error Enum - MissingMetaConfig
     */
    public final static SpcfConfigErrorEnum MissingMetaConfig = new SpcfConfigErrorEnum(3);
    /**
     * Configuration Error Enum - Initialization
     */
    public final static SpcfConfigErrorEnum Initialization = new SpcfConfigErrorEnum(4);
    /**
     * Configuration Error Enum - InvalidConfig
     */
    public final static SpcfConfigErrorEnum InvalidConfig = new SpcfConfigErrorEnum(5);
    /**
     * Configuration Error Enum - InvalidMetaConfig
     */
    public final static SpcfConfigErrorEnum InvalidMetaConfig = new SpcfConfigErrorEnum(6);
    /**
     * Configuration Error Enum - LoadConfigError
     */
    public final static SpcfConfigErrorEnum LoadConfigError = new SpcfConfigErrorEnum(7);
    /**
     * Configuration Error Enum - UpdateConfigError
     */
    public final static SpcfConfigErrorEnum UpdateConfigError = new SpcfConfigErrorEnum(8);
    /**
     * Configuration Error Enum - ProviderInitError
     */
    public final static SpcfConfigErrorEnum ProviderInitError = new SpcfConfigErrorEnum(9);
    /**
     * Configuration Error Enum - CannotLoadConfigError
     */
    public final static SpcfConfigErrorEnum CannotLoadConfigError = new SpcfConfigErrorEnum(10);
    /**
     * Configuration Error Enum - UpdateUnsupportedError
     */
    public final static SpcfConfigErrorEnum UpdateUnsupportedError = new SpcfConfigErrorEnum(11);
    /**
     * Configuration Error Enum - ListenerFailedError
     */
    public final static SpcfConfigErrorEnum ListenerFailedError = new SpcfConfigErrorEnum(12);
    /**
     * Configuration Error Enum - FileTooBigError
     */
    public final static SpcfConfigErrorEnum FileTooBigError = new SpcfConfigErrorEnum(13);
}
