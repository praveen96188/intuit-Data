package com.intuit.sbd.payroll.psp.tools;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ers.EntitlementDisable;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.EntitlementUnit;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: TimothyD698
 * Date: 11/27/12
 */
public class DisableEntitlementUnitList {

    private static SpcfLogger logger = Application.getLogger(DisableEntitlementUnitList.class);

    private static final String FILE_NAME_COMMAND = "-file";
    private static final String AUTH_USER_COMMAND = "-authUser";

    private static String mFileName = null;
    private static String mAuthUser = null;

    private static void parseArgs(String[] args) {

        final String usage = "DisableEntitlementUnitList -file=FullPathOfFile [-authUser=CorpId]";

        for (String arg : args) {
            String[] argParts = arg.split("=");
            if(argParts.length == 2) {
                if(argParts[0].equals(FILE_NAME_COMMAND)) {
                    mFileName = argParts[1];
                } else if(argParts[0].equals(AUTH_USER_COMMAND)) {
                    mAuthUser = argParts[1];
                } else {
                    logger.error("Invalid Command, Usage - " + usage);
                    throw new RuntimeException("Invalid command: " + argParts[0]);
                }
            } else {
                logger.error("Invalid Argument, Usage - " + usage);
                throw new RuntimeException("Invalid argument: " + arg);
            }
        }

        if (mFileName == null) {
            logger.error("Invalid parameters - Must provide filename. Usage - " + usage);
            System.exit(-1);
        }
    }

    public static void main(String[] args) {

        List<EntitlementUnit> entitlementUnits = new ArrayList<EntitlementUnit>(100);

        try {
            parseArgs(args);

            logger.info("Starting Disable Entitlement Unit List with filename - " + mFileName);

            // Set the Principal to the provided Corp ID if provided.
            AuthUser user = AuthUser.findUser(mAuthUser);
            if (user != null) {
                PayrollServices.setCurrentPrincipal(user.createPrincipal());
            } else {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.MassEntitlementDisableTool);
            }

            // Open the file
            FileReader fileReader = null;
            try {
                File f = new File(mFileName);
                fileReader = new FileReader(f);
                BufferedReader input =  new BufferedReader(fileReader);

                String line;

                PayrollServices.beginUnitOfWork();

                // For each license number in the file
                while (( line = input.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length != 2) {
                        logger.error("Invalid line in input file - " + line);
                    } else {
                        String licenseNumber = values[0];
                        String sourceCompanyId = values[1];
                        EntitlementUnit eu = getEntitlementUnit(licenseNumber.trim(), sourceCompanyId.trim());
                        if (eu != null) {
                            entitlementUnits.add(eu);
                        }
                     }
                }
            } catch (IOException e) {
                logger.error("I/O Exception while reading file", e);
                System.exit(-1);
            } finally {

                PayrollServices.rollbackUnitOfWork();

                if(fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error in main", e);
            System.exit(-1);
        }

        disableEntitlementUnits(entitlementUnits);

        logger.info("Process completed. " + entitlementUnits.size() + " Entitlements updated.");

        System.exit(0);
    }

    private static EntitlementUnit getEntitlementUnit(String licenseNumber, String sourceCompanyId) {

        logger.info("Finding License Number/PSID - " + licenseNumber + "/" + sourceCompanyId);

        // Find the entitlement units for this license number/company.
        DomainEntitySet<EntitlementUnit> entitlementUnits = Application.find(EntitlementUnit.class,
                                EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)
                                    .And(EntitlementUnit.Company().SourceSystemCd().equalTo(SourceSystemCode.QBDT))
                                    .And(EntitlementUnit.Company().SourceCompanyId().equalTo(sourceCompanyId)));

        if (entitlementUnits.size() == 0) {
            logger.warn("Unable to find an Entitlement Unit for License Number/PSID - " + licenseNumber + "/" + sourceCompanyId);
            return null;
        } else {
            // Find the first one that is active.
            for (EntitlementUnit eu : entitlementUnits) {
                if (eu.isActivated()) {
                    return eu;
                }
            }
        }

        return null;
    }

    private static void disableEntitlementUnits(List<EntitlementUnit> entitlementUnits) {

        try {
            EntitlementDisable entitlementDisable = new EntitlementDisable();
            entitlementDisable.disable(entitlementUnits);
        } catch (Throwable t) {
            logger.error("Error during call to disable entitlements.", t);
        }
    }

    /**
     * @param args
     * @return
     * @throws Exception
     */
    public String mainAuto(String[] args){

        List<EntitlementUnit> entitlementUnits = new ArrayList<EntitlementUnit>(100);

        try {
            parseArgs(args);

            logger.info("Starting Disable Entitlement Unit List with filename - " + mFileName);

            // Set the Principal to the provided Corp ID if provided.
            AuthUser user = AuthUser.findUser(mAuthUser);
            if (user != null) {
                PayrollServices.setCurrentPrincipal(user.createPrincipal());
            } else {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.MassEntitlementDisableTool);
            }

            // Open the file
            FileReader fileReader = null;
            try {
                File f = new File(mFileName);
                fileReader = new FileReader(f);

                BufferedReader input =  new BufferedReader(fileReader);

                String line;

                PayrollServices.beginUnitOfWork();

                // For each license number in the file
                while (( line = input.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length != 2) {
                        logger.error("Invalid line in input file - " + line);
                    } else {
                        String licenseNumber = values[0];
                        String sourceCompanyId = values[1];
                        EntitlementUnit eu = getEntitlementUnit(licenseNumber.trim(), sourceCompanyId.trim());
                        if (eu != null) {
                            entitlementUnits.add(eu);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error(" Exception while reading file ", e);
               throw new Exception("I/O Exception while reading file");
            } finally {

                PayrollServices.rollbackUnitOfWork();

                if(fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error in main", e);
        }

        String  jobResult = disableEntitlementUnitsAuto(entitlementUnits);

        logger.info("Process completed. " + entitlementUnits.size() + " Entitlements updated for Auto Mass Cancellation");

        return jobResult;
    }

    /**
     * @param entitlementUnits
     * @return
     */
    private static String disableEntitlementUnitsAuto(List<EntitlementUnit> entitlementUnits) {
        String jobResult = null;
        try {
            EntitlementDisable entitlementDisable = new EntitlementDisable();
            jobResult = entitlementDisable.disableAuto(entitlementUnits);
        } catch (Throwable t) {
            logger.error("Error during  disable entitlementsUnits", t);
        }
        return jobResult;
    }
}
