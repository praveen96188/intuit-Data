package com.intuit.spc.foundations.portability.annotations.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 *
 * An annotation to specified the collection's item type. The XML binder serializer 
 * requires specifing the concrete type for the collection being serialized. For 
 * class properties (Java get/set methods) that are of type collection, its element 
 * type must be specified. Use this annotation for those property types. Fixed arrays 
 * property types do not use this annotation.
 * Refer to serializer documentation: com.intuit.spc.foundations.primary.serialization.SpcfXmlBinder
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SpcfXmlBinderInclude 
{
	/***
	 * Returns the collections's creatable item type.
	 * @return The item type.
	 */
	Class itemType();
}
