package com.intuit.spc.foundations.portability.collections;

import com.intuit.spc.foundations.portability.reflect.SpcfClass;

/**
 * An Spcf interface that should be applied to generic types that are expected to be serialzed.
 * 
 * This interface allows for type parameters to be queried at runtime from instances of generic types
 * at runtime.  This feature intiially will be used for serialization so that type information about
 * the type parameter of a generic type can be queried at runtime when the object type information is
 * queried via reflection.
 * 
 * @author jbrewer
 */
public interface ISpcfParameterizedType 
{
	/**
	 * Returns an arary containing SpcfClass types that represent the type parameter
	 * for the implementing generic type.  The type parameters should be in order.<br/> <br/>
	 * For example:  Map&lt;K,V&gt; <br/>
	 * [SpcfClass for first parameter,SpcfClass for second parameter]
	 * @return An array containing the type parameters in order.
	 */
	SpcfClass[] getTypeParams();
	
	/**
	 * Sets the runtime type for the type param(s) used as type parameters in the implementing object.
	 * Allows the type parameters to be queried at runtime by a generic type.
	 * @param typeParams An array containing the in order set of SpcfClass representations of the type
	 * parameters
	 */
	void setTypeParams(SpcfClass[] typeParams);

}
