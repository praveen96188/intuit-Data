/*
 * $Id: //psp/dev/Adapters/TestAdapter/src/com/intuit/sbd/payroll/psp/webservices/PSPDateWS.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.api.PayrollServices;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Wiktor Kozlik
 */
@WebService()
public class PSPDateWS {

    static {
        System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace", "true");
    }

    @WebMethod
    public Date get() throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            Date date = new Date(PSPDate.getPSPTime().getTimeInMilliseconds());
            PayrollServices.commitUnitOfWork();
            return date;
        }
        catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            e.printStackTrace();
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void set(@WebParam(name = "date")String date) throws Exception {
        System.out.println(date);
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(date);
            PayrollServices.commitUnitOfWork();
        } catch(Exception e) {
            PayrollServices.rollbackUnitOfWork();
            e.printStackTrace();
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void reset() {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.resetPSPTime();
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
