package com.intuit.spc.foundations.portability.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An Annotation(java)/Attribute(c#) that is used to specify that
 * a particular class (and the contents) of the entire file are non-portable.
 * If this annotation/attribute is applied to the publicly accessible outer class
 * then J2C/C2J will not translate the file that it has been applied within.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SpcfNonPortableClass 
{

}
