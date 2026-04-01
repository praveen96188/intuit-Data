/**
* XmlResourcePool.java
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

package com.intuit.sbd.payroll.psp.ach.util;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import org.xml.sax.*;

/**
 * This class implements a resource bundle backed by XML files.
 */
public class XmlResourcePool implements ResourceListener
{
	protected SimpleMemTable m_items = new SimpleMemTable ();
	protected Vector m_resources = new Vector ();
	protected ResourceHandler m_defaultHandler;
	protected SAXParser m_parser;
	
	/**
	 * Create resource pool
	 */
	public XmlResourcePool()
	{
	}

	/**
	 * Create resource pool and add the specified file into the resource's pool.
	 * @param path full path of the XML resource file.
	 */
	public XmlResourcePool(String filePath)
	{
		add(filePath);
	}

	/**
	 * Add the specified file into the resource's pool.
	 * @param path full path of the XML resource file.
	 */
	public void add (String path) {
		add (new File (path));
	}

	/**
	 * Creates and returns singleton SAX parser
	 */
	SAXParser getParser() {
		if (m_parser != null)
			return m_parser;
		try {
			m_parser = SAXParserFactory.newInstance().newSAXParser();
		} catch (Exception ex) {
			//AppMgr.getLogger().error("Unable to create parser: " + ex);
		}
		return m_parser;
	}

	/**
	 * Add the specified file into the resource's pool
	 */
	public synchronized void add (File f)
	{
		// If not found in pool, or the entry has been modified since
		// the last load, then add and (re)parse the resource
		Entry	oldEntry, newEntry = new Entry (f);
		for (int i = 0; i < m_resources.size (); i++) {
			oldEntry = (Entry)m_resources.elementAt (i);
			if (f.equals (oldEntry.m_file)) {
				if (newEntry.m_lastModified == oldEntry.m_lastModified)
					return;
				else {
					m_resources.remove (oldEntry);
					break;
				}
			}
		}
		
		if (m_defaultHandler == null) {
			m_defaultHandler = new XmlResourceHandler ();
			m_defaultHandler.addResourceListener (this);
		}
		
		try {
			getParser().parse(new InputSource(new FileInputStream(f)), m_defaultHandler);
		} catch (Exception ex) {
			throw new RuntimeException("Unable to parse: " + ex);
		}

		m_resources.add (newEntry);
	}
	
	public Object get (int key, boolean createNew) {
		return get (String.valueOf (key), createNew);
	}

	/**
	 * Get the object pointed by the key - get's the cached object is createNew
	 * is false.
	 */
	public Object get (String key, boolean createNew) {
	 	// Parse the corresponding XML resource file and build the components
		if (createNew) {
			m_items = new SimpleMemTable ();		// restart
			try {
				for (int i = 0; i < m_resources.size (); i++) {
					getParser().parse (new InputSource(new FileInputStream(
						((Entry)m_resources.elementAt (i)).m_file)), m_defaultHandler);

				}
			} catch (Exception ex) {
				throw new RuntimeException("Unable to parse: " + ex);
			}
		}
		return m_items.get (key);
	}

	public Object put (String key, Object item) {
		return m_items.put (key, item);
	}

	/**
	 * A resource entry consists of the XML file and the last modified date of
	 * the XML file.
	 */
	class Entry
	{
		File m_file;
		long m_lastModified;

		Entry (File f) {
			m_file = f;
			m_lastModified = f.lastModified ();
		}
	}
	
	/** Implement ResourceListener */
	public void handlerChanged (ResourceHandler handler)
	{
		try {
			getParser().getXMLReader().setContentHandler(handler);
			handler.addResourceListener (this);
		} catch (SAXException e) {
			throw new RuntimeException("Unable to parse: " + e);
		}
	}

	public void itemCreated (String id, Object c) {
		m_items.put (id, c);
	}
	
	public Object itemRequested (String id) {
		return m_items.get (id);
	}
}
