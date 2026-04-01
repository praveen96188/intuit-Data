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

package com.paycycle.util;

import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

/**
 * This class implements a resource bundle backed by XML files.
 */
public class XmlResourcePool implements ResourceListener {
    protected SimpleMemTable mItemCache = new SimpleMemTable();
    protected List<XmlResource> mResourceList = new Vector<XmlResource>();
    protected ResourceHandler mDefaultHandler = new XmlResourceHandler();
    protected SAXParser mParser;

    protected enum XmlResourceType {
        FILE, RESOURCE
    }

    protected class XmlResource {
        private String mResourceName;
        private XmlResourceType mResourceType;

        XmlResource(String pResourceName, XmlResourceType pResourceType) {
            mResourceName = pResourceName;
            mResourceType = pResourceType;
        }

        public InputStream getInputStream() {
            try {
                switch (mResourceType) {
                    case FILE:
                        return new FileInputStream(mResourceName);
                    default:
                        return XmlResourcePool.class.getResourceAsStream(mResourceName);
                }
            } catch (Throwable t) {
                throw new RuntimeException(String.format("Error loading xml resource %s ", mResourceName), t);
            }
        }
    }

    /**
     * Create resource pool
     */
    public XmlResourcePool() {
        try {
            mParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Throwable t) {
            throw new RuntimeException("Unable to create SAXParser. ", t);
        }

        mDefaultHandler.addResourceListener(this);
    }

    /**
     * Loads the specified resource via getResourceAsStream() and adds it to the resource pool.
     *
     * @param pResourcePath The path to the resource
     */
    public void addResource(String pResourcePath) {
        add(new XmlResource(pResourcePath, XmlResourceType.RESOURCE));
    }

    /**
     * Loads the specified file via FileInputStream() and adds it to the resource pool.
     *
     * @param pFileName The path to the file
     */
    public void addFile(String pFileName) {
        add(new XmlResource(pFileName, XmlResourceType.FILE));
    }

    /**
     * Add the specified resource into the resource's pool
     */
    protected synchronized void add(XmlResource pResource) {
        parseResource(pResource);
        mResourceList.add(pResource);
    }

    public Object get(int key, boolean createNew) {
        return get(String.valueOf(key), createNew);
    }

    /**
     * Get the object pointed by the key - get's the cached object if createNew is false.
     */
    public Object get(String key, boolean createNew) {
        if (createNew) {
            //
            // Parse the corresponding XML resource and build the components
            //

            mItemCache = new SimpleMemTable(); // restart

            for (XmlResource resource : mResourceList) {
                parseResource(resource);
            }
        }

        return mItemCache.get(key);
    }

    private void parseResource(XmlResource pResource) {
        try {
            InputStream iStream = pResource.getInputStream();

            try {
                mParser.parse(iStream, mDefaultHandler);
            } finally {
                iStream.close();
            }
        } catch (Throwable t) {
            throw new RuntimeException("SAXParser error. ", t);
        }
    }

    public Object put(String key, Object item) {
        return mItemCache.put(key, item);
    }

    /**
     * Implement ResourceListener
     */
    public void handlerChanged(ResourceHandler handler) {
        try {
            mParser.getXMLReader().setContentHandler(handler);
            handler.addResourceListener(this);
        } catch (SAXException e) {
            throw new RuntimeException("Error changing handlers. ", e);
        }
    }

    public void itemCreated(String id, Object c) {
        mItemCache.put(id, c);
    }

    public Object itemRequested(String id) {
        return mItemCache.get(id);
    }
}
