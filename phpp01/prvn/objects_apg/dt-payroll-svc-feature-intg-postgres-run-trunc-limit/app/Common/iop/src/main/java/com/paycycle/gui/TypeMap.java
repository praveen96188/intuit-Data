/**
 * TypeMap.java
 *
 * Copyright (c) 1999-2000 PayCycle, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * PayCycle, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with PayCycle.
 *
 * PAYCYCLE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. PAYCYCLE SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */

package com.paycycle.gui;

import com.paycycle.util.SimpleMemTable;

import java.util.Enumeration;

public class TypeMap {
    SimpleMemTable m_items = new SimpleMemTable();
    Class m_keyClass;

    public void setKeyClass(Class clazz) {
        m_keyClass = clazz;
    }

    /**
     * Return object value that has been mapped to the supplied key.
     */
    public Object get(Object key) {
        return m_items.get(key);
    }

    /**
     * Return a string value that has been mapped to the supplied key.
     */
    public String getString(Object key) {
        return m_items.get(key).toString();
    }

    /**
     * Adds an entry to the map. The key is first treated as a field
     * name in this class.  If the corresponding field name is not
     * found, then the key is treated as a string literal.
     */
    public void put(String key, String value) {
        try {
            m_items.put(m_keyClass == null ? key : m_keyClass.getField(key).get(null), value);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * Returns an enumeration of all the keys in the map
     */
    public Enumeration keys() {
        return m_items.keys();
    }

}
