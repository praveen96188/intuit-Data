//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------

package com.intuit.payroll.agency.impl;

import com.intuit.payroll.agency.api.IRulesList;
import java.util.ArrayList;

/// <summary>
/// Implementation class for the IRulesList interface. a collection object used to pass an array as a method parameter.
/// </summary>
public class RulesList implements IRulesList {
    ArrayList<Object> m_arrayList = new ArrayList();
//    SpcfFactory m_spcfFactory = SpcfFactory.getInstance();
//    SpcfArrayList <Object> m_arrayList = m_spcfFactory.createArrayList();

    public RulesList() {
    }

    /// <summary>
    /// Number of elements in the collection.
    /// </summary>
    public int getCount()
    {
        return m_arrayList.size();
//        return m_arrayList.getSize();
    }

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
    public Object getItem(int index)
    {
        return m_arrayList.get(index);
//        return m_arrayList.getItemAsObject(index);
    }

    /// <summary>
    /// Add an object at the end of the collection.
    /// </summary>
    /// <param name="value">The object to be added.</param>
    /// <returns>Position of the added object in the collection.</returns>
    public boolean add(Object value)
    {
        return m_arrayList.add(value);
    }

    /// <summary>
    /// Delete an object of the collection.
    /// </summary>
    /// <param name="index">Position of the object to be deleted at the collection.</param>
    public void delete(int index)
    {
        m_arrayList.remove(index);
//        m_arrayList.removeAt(index);
    }

    /// <summary>
    /// Removes all the elements from the collection.
    /// </summary>
    public void clear()
    {
        m_arrayList.clear();
    }
}
