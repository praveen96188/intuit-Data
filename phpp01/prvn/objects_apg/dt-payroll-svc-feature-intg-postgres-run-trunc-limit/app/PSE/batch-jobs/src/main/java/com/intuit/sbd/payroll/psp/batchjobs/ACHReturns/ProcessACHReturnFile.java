/*
 * $Id: //psp/dev/PSE/BatchJobs/src/com/intuit/sbd/payroll/psp/batchjobs/ACHReturns/ProcessACHReturnFile.java#3 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;


/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 20, 2008
 * Time: 10:16:23 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessACHReturnFile {
    private static final SpcfLogger logger;

    static {
        Application.initialize();
        ApplicationSecondary.initialize();
        logger = Application.getLogger(ProcessACHReturnFile.class);
    }

    /**
     * Main method to process the ACH Return file.
     * @param args String
     */
    public static void main (String args[]) {
        try {
            if (args.length != 1) {
                throw new RuntimeException("Wrong number of parameters. Usage: ProcessACHReturnFile <full-file-name>");
            }

            new ReturnFileParser().processFile(new File(args[0]));

        } catch (Throwable t) {
            t.printStackTrace();
            logger.error("Error processing returns file. ", t);
            PayrollServices.rollbackUnitOfWork();
            System.exit(1);
        }
    }
}
