package com.intuit.sbd.payroll.psp.migration;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: Feb 18, 2011
 * Time: 3:24:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class EftpsEnrollmentUpdater {

    private static SpcfLogger logger = Application.getLogger(EftpsEnrollmentUpdater.class);

    public static void main(String args[]) {
        try {
            StopWatch sw = new StopWatch().start();

            Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BatchJob));
            
            logger.info("beginning eftps enrollment update...");

            if (args.length == 2) {
                try {
                    EftpsEnrollmentStatus enrollmentEnum = EftpsEnrollmentStatus.valueOf(args[0]);
                    FileReader fileReader = new FileReader(new File(args[1]));
                    BufferedReader input =  new BufferedReader(fileReader);

                    try {
                        String line;
                        while (( line = input.readLine()) != null) {
                            boolean success = true;
                            String[] lineArray = line.split(";");
                            String psid = lineArray[0];
                            String irsData = lineArray[1];

                            PayrollServices.beginUnitOfWork();

                            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
                            if (company != null) {
                                EftpsEnrollment enrollment = company.getCurrentEnrollment();
                                if (enrollment != null) {
                                    ProcessResult<EftpsEnrollment> updatedEnrollmentProcessResult = PayrollServices.companyManager.updateEftpsEnrollment(SourceSystemCode.QBDT, company.getSourceCompanyId(), enrollmentEnum);
                                    EftpsEnrollment updatedEnrollment = updatedEnrollmentProcessResult.getResult();

                                    if (updatedEnrollmentProcessResult.isSuccess() && enrollmentEnum == EftpsEnrollmentStatus.Enrolled) {
                                        updatedEnrollment.setEftpsEnrollmentId(irsData);
                                        Application.save(updatedEnrollment);
                                    } else if (updatedEnrollmentProcessResult.isSuccess() && enrollmentEnum == EftpsEnrollmentStatus.Rejected) {
                                        EftpsEnrollmentDetail detail = updatedEnrollment.findEnrollmentDetail();
                                        if (detail!=null) {
                                            detail.setRejectCd(irsData);
                                            Application.save(detail);
                                        }
                                    }
                                    success = updatedEnrollmentProcessResult.isSuccess();
                                    if (!success) {
                                        logger.error("Error updating EFTPS enrollment for company with PSID: "+psid+" "+updatedEnrollmentProcessResult.toString());
                                    }
                                } else {
                                    success=false;
                                    logger.error("Could not find current EFTPS enrollment for company with PSID: "+psid);
                                }
                            } else {
                                success=false;
                                logger.error("Could not find company with PSID: "+psid);
                            }

                            if (success) {
                                PayrollServices.commitUnitOfWork();
                            } else {
                                PayrollServices.rollbackUnitOfWork();
                            }
                        }
                    } catch (Throwable t) {
                        logger.fatal("Fatal error during mini EFTPS enrollment update", t);
                    } finally {
                        input.close();
                    }
                } catch (Throwable t) {
                    logger.fatal("Fatal error during mini EFTPS enrollment update.  Invalid enrollment status: "+args[0]);
                }
            } else {
                logger.fatal("invalid number of arguments.  Usage: EftpsEnrollmentStatus (Enrolled | Rejected) InputFileName");
            }
        } catch (Throwable t) {
            logger.fatal("Fatal error during mini EFTPS enrollment update", t);
            t.printStackTrace();
        }
    }
}