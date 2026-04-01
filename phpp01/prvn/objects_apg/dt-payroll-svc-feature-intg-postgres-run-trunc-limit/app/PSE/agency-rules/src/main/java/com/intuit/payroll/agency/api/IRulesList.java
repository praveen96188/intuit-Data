//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------

	/// <summary>
	/// Interface for a collection object used to pass an array as a method parameter.  This interface is exposed through COM.
	/// </summary>

package com.intuit.payroll.agency.api;

public interface IRulesList
{
    /// <summary>
    /// Number of elements in the collection.
    /// </summary>
    public int getCount ();

    /// <summary>
    /// gets an item from the collection at a specific position.
    /// </summary>
    /// <param name="index">Position of the item in the collection.</param>
    /// <returns>object reference to the item. Client code needs to cast it to the desired object.
    /// Agency agency = (Agency) agencyList.Item(2);
    /// if (agency.IsValid) {
    ///     // Do something
    /// }
    /// </returns>
    public Object getItem(int index);

    /// <summary>
    /// Add an object at the end of the collection.
    /// </summary>
    /// <param name="value">The object to be added.</param>
    /// <returns>true if the collection changed as a result of the call. false if this collection does not permit duplicates and already contains the specified element </returns>
    public boolean add(Object that);

    /// <summary>
    /// Delete an object of the collection.
    /// </summary>
    /// <param name="index">Position of the object to be deleted at the collection.</param>
    public void delete(int index);

    /// <summary>
    /// Removes all the elements from the collection.
    /// </summary>
    public void clear();
}
