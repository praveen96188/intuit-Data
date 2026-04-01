package com.intuit.spc.foundations.portability.annotations.serialization; 

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;  
/***
 *
 * An Annotation used to specify that the XmlBinder should allow a client
 * specified handler implementing ISpcfResolver to resolve the object
 * identity during serialization and return the resolved object during
 * deserialization with that identity. 
 */  
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)  
public @interface SpcfXmlBinderResolve 
{
	/***
	 * Returns the description of the object to be resolved.
	 * @return The string description of the object to be resolved.
	 */
	String objectDescription(); 
} 