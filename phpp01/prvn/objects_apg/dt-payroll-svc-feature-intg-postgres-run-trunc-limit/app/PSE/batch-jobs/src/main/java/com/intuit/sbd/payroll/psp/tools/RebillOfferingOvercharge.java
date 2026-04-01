package com.intuit.sbd.payroll.psp.tools;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.RebillFeeTransactionDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileReader;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: TimothyD698
 * Date: 10/19/12
 * Time: 9:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class RebillOfferingOvercharge {

    private static SpcfLogger logger = Application.getLogger(RebillOfferingOvercharge.class);

    private static final String FILE_NAME_COMMAND = "-file";
    private static final String COMMIT_COMMAND = "-commit";
    private static final String START_DATE_COMMAND = "-start";
    private static final String END_DATE_COMMAND = "-end";

    private static String mFileName = null;
    private static boolean mCommit = false;
    private static SpcfCalendar mStartDate = null;
    private static SpcfCalendar mEndDate = null;

    private static void parseArgs(String[] args) {

        SpcfCalendar tmp;

        for (String arg : args) {
            String[] argParts = arg.split("=");
            if(argParts.length == 2) {
                if(argParts[0].equals(FILE_NAME_COMMAND)) {
                    mFileName = argParts[1];
                } else if(argParts[0].equals(COMMIT_COMMAND)) {
                    mCommit = Boolean.parseBoolean(argParts[1]);
                } else if(argParts[0].equals(START_DATE_COMMAND)) {
                    tmp = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, argParts[1]);
                    mStartDate = SpcfCalendar.createInstance(tmp.getYear(), tmp.getMonth(), tmp.getDay(), 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
                } else if(argParts[0].equals(END_DATE_COMMAND)) {
                    tmp = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, argParts[1]);
                    mEndDate = SpcfCalendar.createInstance(tmp.getYear(), tmp.getMonth(), tmp.getDay(), 23, 59, 59, 0, SpcfTimeZone.getLocalTimeZone());
                } else {
                    throw new RuntimeException("Invalid command: " + argParts[0]);
                }
            } else {
                throw new RuntimeException("Invalid argument: " + arg);
            }
        }

        if (mFileName == null || mStartDate == null || mEndDate == null) {
            logger.error("Invalid parameters - Must provide filename, start date, and end date (dates are YYYY-MM-DD).");
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        try {
            parseArgs(args);

            logger.info("Starting Re-Debit of Offering Overcharges");

            // Set the Principal to Timothy Dry.
            AuthUser user = AuthUser.findUser("50000034034");
            if (user != null) {
                PayrollServices.setCurrentPrincipal(user.createPrincipal());
            } else {
                PayrollServices.setCurrentPrincipal(SystemPrincipal.AS400Migration);
            }

            // Open the file
            InputStreamReader fileReader = null;
            try {
                File f = new File(mFileName);

                //check if file is encrypted
                if(StreamUtil.isFileIDPSEncrypted(mFileName)){
                    Key key = IDPSFileStreamManager.newKeyHandleLatest();
                    fileReader = new IDPSFileReader( f, key);
                }else{    
                	fileReader = new FileReader(f);
                }
                
                BufferedReader input =  new BufferedReader(fileReader);

                String line;
                
                // For each PSID in the file
                while (( line = input.readLine()) != null){
                    redebitOfferingFinancialTransaction(line.trim());
                }
            } catch (IOException e) {
                logger.error("Error reading file", e);
                System.exit(-1);
            } finally {
                if(fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }

            logger.info("Completed Re-Debit of Offering Overcharges");

        } catch (Exception e) {
            logger.error("Error in main", e);
            System.exit(-1);
        }
        System.exit(0);
    }

    private static void redebitOfferingFinancialTransaction(String sourceCompanyId) {

        try {
            PayrollServices.beginUnitOfWork();

            logger.info("Processing - " + sourceCompanyId);

            // Find the offending financial transaction we need to re-debit.
            Company company = Company.findCompanyNoEagerLoad(sourceCompanyId, SourceSystemCode.QBDT);
            DomainEntitySet<FinancialTransaction> finTrans = Application.find(FinancialTransaction.class, FinancialTransaction.Company().equalTo(company)
                        .And(FinancialTransaction.BillingDetail().ItemName().equalTo("Monthly Fee"))
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Completed))
                        .And(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit))
                        .And(FinancialTransaction.SettlementDate().greaterOrEqualThan(mStartDate))
                        .And(FinancialTransaction.SettlementDate().lessOrEqualThan(mEndDate)));

            // Verify there is only one result.
            if(finTrans.size() != 1) {
                logger.error("Incorrect result set size (" + finTrans.size() + ") for PSID: " + sourceCompanyId);
                return;
            }

            // Execute the re-debit.
            RebillFeeTransactionDTO rebillDTO = new RebillFeeTransactionDTO(finTrans.getFirst().getId().toString(), null);
            ProcessResult<DomainEntitySet<BillingDetail>> processResult = PayrollServices.financialTransactionManager.rebillFeeTransaction(rebillDTO);

            if(processResult.isSuccess()) {
                DomainEntitySet<BillingDetail> billingDetails = processResult.getResult();
                for (BillingDetail billingDetail : billingDetails) {
                    // If the re-debit produced the same amount as previously billed, rollback the transaction.
                    if(finTrans.getFirst().getFinancialTransactionAmount().equals(billingDetail.getFeeTransaction().getFinancialTransactionAmount())) {
                        logger.info("Re-Bill produced same amount - " + billingDetail.getItemTotal());
                        PayrollServices.rollbackUnitOfWork();
                    } else if(mCommit) {
                        logger.info("OFFERREDEBIT" + "^" + company.getSourceCompanyId() + "^" + company.getLegalName() + "^" + finTrans.getFirst().getFinancialTransactionAmount() +
                                            "^" + billingDetail.getItemSku() + "^" + billingDetail.getFeeTransaction().getFinancialTransactionAmount());
                        PayrollServices.commitUnitOfWork();
                    }
                }
            }

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
