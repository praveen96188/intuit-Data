/**
 * ResourceHandler.java
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

import com.paycycle.util.AppHelper;
import com.paycycle.util.Helper;
import com.paycycle.util.ResourceListener;
import com.paycycle.util.XmlResourceHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Stack;


/**
 * BindResource.  This resource bundle creates data models
 * associated XML configuration files that are locale-specific.
 */
public class ResourceHandler extends XmlResourceHandler {
    Stack m_stack = new Stack();
    String m_lastElement;

    /**
     * Override default implementation
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        if (XML_COMPONENT.equals(qName)) {
            Object c = createInstance(atts.getValue(XML_CLASS));
            String id = atts.getValue(XML_ID);
            String constantPath = atts.getValue(XML_CONSTANT);
            if (m_listeners != null) {
                String tag = id;
                if (constantPath != null) {
                    tag = AppHelper.getConstant(constantPath).toString();
                }

                // Notify listeners that a component with an id has been encountered
                for (int i = 0; i < m_listeners.size(); i++) {
                    ((ResourceListener) m_listeners.get(i)).itemCreated(tag, c);
                }
            }

            m_stack.push(c);

        } else if (XML_FIELD.equals(qName) || XML_RECORD.equals(qName)) {
            m_stack.push(createInstance(atts.getValue(XML_CLASS)));
        } else if (XML_PROPERTY.equals(qName)) {
            invokeSetMethod(m_stack.peek(), atts.getValue(XML_NAME), new Object[]{createArg(atts)});
        } else if (XML_ENTRY.equals(qName)) {
            try {
                Helper.invoke(m_stack.peek(), "put", new Object[]{atts.getValue(XML_KEY), atts.getValue(XML_VALUE)});
            } catch (Exception ex) {
            }
        }
        m_lastElement = qName;
    }

    /**
     * Override default implementation
     */
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        // Manage the current scope
        if (XML_FIELD.equals(qName) || XML_RECORD.equals(qName)) {
            Object c = m_stack.pop();
            if (!m_stack.empty()) {
                invokeAddMethod(m_stack.peek(), "", new Object[]{c});
            }
        }
        m_lastElement = "";
    }

    /**
     * Override default implementation
     */
    public void characters(char[] ch, int start, int length) {
        if (XML_DESCRIPTION.equals(m_lastElement)) {
            // Store the field description
            invokeSetMethod(m_stack.peek(), "Description", new Object[]{new String(ch, start, length)});
        }
    }
}
