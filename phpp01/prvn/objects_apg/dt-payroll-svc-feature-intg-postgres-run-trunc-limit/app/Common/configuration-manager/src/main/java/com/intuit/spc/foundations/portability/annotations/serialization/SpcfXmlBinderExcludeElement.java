package com.intuit.spc.foundations.portability.annotations.serialization;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation (attribute in C#) used to indicate that the getter/setter pair to which this is
 * attached should be ignored at serialization and de-serialization time by the SPCF XML Binder.
 * This should be attached to both getter and setter pair in java.
 * @author jbrewer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SpcfXmlBinderExcludeElement 
{

}
