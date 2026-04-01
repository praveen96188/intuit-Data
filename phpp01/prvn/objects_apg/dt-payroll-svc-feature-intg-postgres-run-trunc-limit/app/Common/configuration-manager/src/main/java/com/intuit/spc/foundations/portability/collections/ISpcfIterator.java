/*
 * author		RGS
 * department	SPC Foundations
 * project	    Portability
 * 2004-05-03   Initial Implementation
 */
package com.intuit.spc.foundations.portability.collections;
import java.util.Iterator;
import com.intuit.spc.foundations.portability.annotations.SpcfNonPortableClass;

/**
 * A portable interface for enumerating the elements T of a portable collection.
 *
 * @see java.util.Iterator
 */
@SpcfNonPortableClass
public interface ISpcfIterator<T> extends Iterator<T>
{
	/**
	 * Returns <tt>true</tt> if the iteration has more elements. In other
	 * words, returns <tt>true</tt> if <tt>next</tt> would return an element
	 * rather than throwing an exception.
	 *
	 * @return <tt>true</tt> if the iterator has more elements.
	 */
	boolean hasNext();

	/**
	 * Returns the next element in the iteration.
	 *
	 * @return the next element in the iteration.
	 * @exception SpcfNoSuchElementException iteration has no more elements.
	 * @throws SpcfConcurrentModificationException if the collection has been modified since
	 * the iterator was created.
	 */
	T next();
	
	/**
	 * Returns the current element.
	 *
	 * @return the element of the iteration in the current position.
	 * @exception SpcfNoSuchElementException iteration has no more elements.
	 * @throws SpcfConcurrentModificationException if the collection has been modified since
	 * the iterator was created.
	 */ 
     T getCurrent();

 	/**
 	 * Returns <tt>true</tt> If the iteration can advance to the next element. 
 	 * Returns false otherwise. 
 	 *
 	 * @return <tt>true</tt> if the iterator successfully move to the next element, false otherwise.
 	 */    
     boolean moveNext();    
     
	 /**
	  * Optionally supported. If any resources allocated to support this iterator, they are release
	  * or cleared. It is safe to invoking this method mulitple times without causing expection. 
	  */     
	  void dispose();
     
     /**
   	 * Unsupported portable method. Invoking this method will throw an exception.
   	 *
 	 * @throws SpcfUnsupportedOperationException regardless of the current state.
   	 */  
 	 void remove();
 	 
     /**
  	 * Unsupported portable method. Invoking this method will throw an exception regardless of 
  	 * the current state.
  	 *
 	 * @throws SpcfUnsupportedOperationException regardless of the current state.
  	 */  
     void reset();
}
