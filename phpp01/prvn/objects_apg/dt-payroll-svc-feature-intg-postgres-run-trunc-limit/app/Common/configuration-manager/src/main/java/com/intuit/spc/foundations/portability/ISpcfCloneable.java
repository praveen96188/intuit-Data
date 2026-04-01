package com.intuit.spc.foundations.portability;

/**
 * A portable interface for cloning objects. Cloning is used to 
 * create a new instance of a class with the same value as an 
 * existing instance.
 *   
 * <pre>
 * Things to do to make it work:
 * 1)	Implement ISpcfCloneable either from an abstract class or a concrete class 
 * 		depending on the requirement.
 * 2)	Object class has a clone method in Java. In order to override the Object.clone(), 
 * 		always follow the two rules:
 * 			a.	While implementing ISpcfCloneable from an abstract class, 
 * 				make sure to make the clone method abstract so that the 
 * 				concrete class would implement it. i.e.)
 * 				public abstract SpcfSample clone();
 * 			b.	While implementing ISpcfCloneable directly from a 
 * 				concrete class, make sure to give implementation 
 * 				for the clone.
 * </pre> 
 * 
 */
public interface ISpcfCloneable<T>
{
	/**
     *  Gets a cloned instance of the invoked object
     *  
     *  @return Returns a cloned instance
	 */
	T clone();
}
