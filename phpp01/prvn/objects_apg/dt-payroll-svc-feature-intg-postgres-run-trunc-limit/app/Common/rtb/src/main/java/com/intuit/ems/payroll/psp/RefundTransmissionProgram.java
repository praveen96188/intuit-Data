package com.intuit.ems.payroll.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shivanandad069 on 8/12/14.
 */
public class RefundTransmissionProgram {


    private static final String FILE_NAME_COMMAND = "-file";
    private static final String REFUND_TYPE_COMMAND = "-refundtype";
    private static final int LENGTH_OF_ER_COURTESY_REFUND_FILE = 4;
    private static final String RECORD_SEPARATOR_OF_ER_COURTESY_REFUND_FILE = ",";

    private static String mFileName = null;
    private static TransactionTypeCode mTransactionTypeCode = TransactionTypeCode.ERCourtesyRefundCredit;

    private static int totalNumberOfTransactionsToBeProcessed = 0;
    private static int totalNumberOfTransactionsProcessed = 0;
    private static SpcfMoney totalAmountRefunded=null;
    private static List<FileRecord> fileRecords = null;

    private static void parseArgs(String[] args) {

        final String usage = "RefundTransmissionProgram -file=FullPathOfFile";

        for (String arg : args) {
            String[] argParts = arg.split("=");
            if (argParts.length == 2) {
                if (argParts[0].equals(FILE_NAME_COMMAND)) {
                    mFileName = argParts[1];
                } else if (argParts[0].equalsIgnoreCase(REFUND_TYPE_COMMAND)) {
                    mTransactionTypeCode = TransactionTypeCode.valueOf(argParts[1]);
                } else {
                    System.out.println("ERROR: Invalid Command, Usage - " + usage);
                    throw new RuntimeException("Invalid command: " + argParts[0]);
                }
            } else {
                System.out.println("ERROR: Invalid Argument, Usage - " + usage);
                throw new RuntimeException("Invalid argument: " + arg);
            }
        }

        if (mFileName == null) {
            System.out.println("ERROR: Invalid parameters - Must provide filename. Usage - " + usage);
            System.exit(-1);
        }

    }

    public static void main(String[] args) {

        Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BatchJob));
        try {
            parseArgs(args);

            System.out.println("INFO: Starting Refund Transmission Program with filename - " + mFileName);

            // Open the file
            FileReader fileReader = null;
            try {
                File f = new File(mFileName);
                fileReader = new FileReader(f);
                BufferedReader input = new BufferedReader(fileReader);

                String line;

                PayrollServices.beginUnitOfWork();
                fileRecords = new ArrayList<FileRecord>();
                // For each transaction in the file
                while ((line = input.readLine()) != null) {
                    String[] values = line.split(RECORD_SEPARATOR_OF_ER_COURTESY_REFUND_FILE);
                    if (values.length != LENGTH_OF_ER_COURTESY_REFUND_FILE) {
                        System.out.println("ERROR: Invalid line in input file - " + line);
                    } else {
                        totalNumberOfTransactionsToBeProcessed++;
                        validateAndAddRecords(values);

                    }
                }
            } catch (IOException e) {
                System.out.println("ERROR: I/O Exception while reading file");
                e.printStackTrace();
                System.exit(-1);
            } finally {

                PayrollServices.rollbackUnitOfWork();

                if (fileReader != null) {
                    try {
                        fileReader.close();
                    } catch (IOException e) {
                        // ignore
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("ERROR: Error in main");
            e.printStackTrace();
            System.exit(-1);
        }

        createRefundTransaction();
        System.out.println("INFO: Process completed. " + "Total number of transaction to be refunded - " + totalNumberOfTransactionsToBeProcessed +
                                   ". Total number of transaction refunded - " + totalNumberOfTransactionsProcessed + ".Total amount refunded is $"+totalAmountRefunded.toString() );

        System.exit(0);
    }

    static void createRefundTransaction() {
        totalAmountRefunded= new SpcfMoney("0.00");
        for (FileRecord fileRecord : fileRecords) {
            createCourtesyRefund(fileRecord.getSourceCompanyId(), fileRecord.getRefundAmount(), fileRecord.getNotes(), fileRecord.getSettlementTypeDTO().name());
        }
    }

    private static boolean validateAndAddRecords(String[] pValues) {
        SettlementTypeDTO settlementTypeDTO = null;
        if (StringUtils.isEmpty(pValues[0])) {
            System.out.println("ERROR: Invalid record for PSID at line :" + (totalNumberOfTransactionsToBeProcessed + 1));
            throw new RuntimeException("ERROR: Invalid record for PSID at line :" + (totalNumberOfTransactionsToBeProcessed + 1));
        }
        if (StringUtils.isEmpty(pValues[1])) {
            System.out.println("ERROR: Invalid record for refund amount  - " + pValues[1] + " for PSID:" + pValues[0]);
            throw new RuntimeException("ERROR: Invalid record for refund amount - " + pValues[1] + " for PSID:" + pValues[0]);
        }
        if (StringUtils.isEmpty(pValues[2])) {
            System.out.println("ERROR: Invalid record for settlement type - " + pValues[2] + " for PSID:" + pValues[0]);
            throw new RuntimeException("ERROR: Invalid record for settlement type - " + pValues[2] + " for PSID:" + pValues[0]);
        }
        try {
            settlementTypeDTO = SettlementTypeDTO.valueOf(pValues[2].trim());
        } catch (Throwable t) {
            System.out.println("ERROR: Invalid record for settlement type - " + pValues[2] + " for PSID:" + pValues[0]);
            throw new RuntimeException("ERROR: Invalid record for settlement type - " + pValues[2] + " for PSID:" + pValues[0]);
        }
        if (settlementTypeDTO == null) {
            System.out.println("ERROR: Invalid record for settlement type - " + pValues[2] + " for PSID:" + pValues[0]);
            throw new RuntimeException("ERROR: Invalid record for settlement type - " + pValues[2] + " for PSID:" + pValues[0]);
        }
        SpcfMoney refundAmount = getSpcfMoneyFromString(pValues[1].trim(),SpcfMoney.ZERO);
        FileRecord fileRecord = new FileRecord();
        fileRecord.setNotes(pValues[3]);
        fileRecord.setRefundAmount(refundAmount);
        fileRecord.setSourceCompanyId(pValues[0].trim());
        fileRecord.setSettlementTypeDTO(settlementTypeDTO);
        fileRecords.add(fileRecord);
        return false;
    }

    public static void createCourtesyRefund(String pSourceSystemId, SpcfMoney pRefundAmount, String pNote, String pSettlementTypeCd) {

        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<Company> companies = Company.searchCompaniesBySourceCompanyId(pSourceSystemId);
            if (companies.size() > 1) {
                System.out.println("PSID :" + pSourceSystemId + " has multiple records for company object.So cannot create refund transaction for this.");
                return;
            }
            if (companies.size() == 0) {
                System.out.println("PSID :" + pSourceSystemId + " has no records for company object.So cannot create refund transaction for this.");
                return;
            }
            Company company = companies.getFirst();
            SettlementTypeDTO settlementType = SettlementTypeDTO.valueOf(pSettlementTypeCd);
            ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(company.getSourceSystemCd(), pSourceSystemId,
                                                                                                                                 pRefundAmount, pNote, settlementType);
            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
                System.out.println("Created Courtesy Refund Transaction - " + processResult.getResult().getId() + " for PSID: " + pSourceSystemId + " refundAmount: " + pRefundAmount.toString());
                totalNumberOfTransactionsProcessed++;
                totalAmountRefunded=(SpcfMoney)totalAmountRefunded.add(processResult.getResult().getFinancialTransactionAmount());
            } else {
                System.out.println("Error creating Courtesy Refund transaction for :PSID :" + pSourceSystemId + " refundAmount :" + pRefundAmount + " SettlementTypeCd :" + pSettlementTypeCd +".Reason is: ");
                System.out.println( processResult.getErrorMessages());
            }
        } catch (Throwable t) {
            System.out.println("Error creating Courtesy Refund transaction");
            t.printStackTrace();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public static SpcfMoney getSpcfMoneyFromString(String pAmount, SpcfMoney defaultValue) {
        if(StringUtils.isEmpty(pAmount)){
            return defaultValue;
        }
        return new SpcfMoney(pAmount);
    }

        private static class FileRecord {
        private String sourceCompanyId;
        private String notes;
        private SettlementTypeDTO settlementTypeDTO;
        private SpcfMoney refundAmount;

        public String getSourceCompanyId() {
            return sourceCompanyId;
        }

        public void setSourceCompanyId(String pSourceCompanyId) {
            sourceCompanyId = pSourceCompanyId;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String pNotes) {
            notes = pNotes;
        }

        public SettlementTypeDTO getSettlementTypeDTO() {
            return settlementTypeDTO;
        }

        public void setSettlementTypeDTO(SettlementTypeDTO pSettlementTypeDTO) {
            settlementTypeDTO = pSettlementTypeDTO;
        }

        public SpcfMoney getRefundAmount() {
            return refundAmount;
        }

        public void setRefundAmount(SpcfMoney pRefundAmount) {
            refundAmount = pRefundAmount;
        }
    }
}
