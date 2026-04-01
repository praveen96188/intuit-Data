/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
    @author Jeff Jones
 */
public class JaxBManagerImpl implements IJaxBManager {
    private JAXBContext mJAXBContext;

    private static final SpcfLogger logger;

    static {
        logger = PayrollServices.getLogger(JaxBManagerImpl.class);
    }

    /**
     *
     * @param pJAXBContext
     */
    protected JaxBManagerImpl(JAXBContext pJAXBContext) {
        mJAXBContext = pJAXBContext;
    }

    /**
     *
     * @param pObject
     * @return
     * @throws Exception
     */
    public String marshall(Object pObject) throws Exception {
        Marshaller marshaller = mJAXBContext.createMarshaller();
        OutputStream outputStream = new ByteArrayOutputStream();
        try {
            marshaller.marshal(pObject, outputStream);
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            throw new EwsException(EwsMessages.xmlParsingError());
        }
        return outputStream.toString();
    }

    /**
     *
     * @param pXML
     * @return
     * @throws Exception
     */
    public Object Unmarshall(String pXML) throws Exception {
        Object object;
        Unmarshaller unmarshaller = mJAXBContext.createUnmarshaller();
        InputStream inputStream = new ByteArrayInputStream(pXML.getBytes());
        try {
            object = unmarshaller.unmarshal(inputStream);
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
            throw new EwsException(EwsMessages.xmlParsingError());
        }
        return object;
    }
}