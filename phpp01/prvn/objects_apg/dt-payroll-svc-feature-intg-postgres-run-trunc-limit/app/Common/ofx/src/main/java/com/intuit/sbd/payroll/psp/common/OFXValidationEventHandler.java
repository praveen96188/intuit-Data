package com.intuit.sbd.payroll.psp.common;
/*
 * $Id: //psp/dev/Common/OFX/src/com/intuit/sbd/payroll/psp/common/OFXValidationEventHandler.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.sbd.payroll.psp.Application;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

/**
 * This event handler allows the OFX Manager to continue parsing messages even though an illegal tag is
 * found inside the parser
 *
 * @author Sean Barenz
 */
public class OFXValidationEventHandler implements
        ValidationEventHandler {
    private static SpcfLogger logger = SpcfLogManager.getLogger(OFXValidationEventHandler.class);
    public boolean handleEvent(ValidationEvent ve) {
        boolean success = false;
        if (ve.getSeverity() == ValidationEvent.ERROR || ve.getSeverity() == ValidationEvent.FATAL_ERROR) {
            ValidationEventLocator locator = ve.getLocator();
            //print message from valdation event
            logger.warn("Message is " + ve.getMessage() + "\nColumn is " +
                    locator.getColumnNumber() +
                    " at line number " + locator.getLineNumber());
            success = true;
        } else {
            success = false;
        }
        return success;
    }
}