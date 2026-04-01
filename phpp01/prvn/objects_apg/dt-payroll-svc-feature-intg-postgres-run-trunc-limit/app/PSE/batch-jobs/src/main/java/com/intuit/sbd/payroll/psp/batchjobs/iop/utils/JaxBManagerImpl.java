/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.batchjobs.iop.utils;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
    @author Jeff Jones
 */
public class JaxBManagerImpl implements IJaxBManager {
    private JAXBContext mJAXBContext;

    /**
     *
     * @param pJAXBContext
     */
    protected JaxBManagerImpl(JAXBContext pJAXBContext) {
        mJAXBContext = pJAXBContext;
    }

    public String marshall(Object pObject) throws Exception {
        OutputStream outputStream = null;
        try {
            Marshaller marshaller = mJAXBContext.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
            QName qName = new QName("http://webservices.onlinepayroll.intuit.com", "EMSPManagerSOAP");

            outputStream = new ByteArrayOutputStream();

            marshaller.marshal(new JAXBElement(qName, pObject.getClass(), pObject), outputStream);
        } catch (Exception e) {
            throw new Exception("Unable to marshall string.", e);
        }
        return outputStream.toString();
    }
}
