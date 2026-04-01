package com.intuit.ems.payroll.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ERRefundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ankit on 1/27/14.
 */
public class RefundTransmissionFeesAndTaxes {


    private static final String FILE_NAME_COMMAND = "-file";

    private static String mFileName = null;

    private static int totalNumberOfTransactionsToBeProcessed = 0;
    private static int totalNumberOfTransactionsProcessed = 0;

    private static void parseArgs(String[] args) {

        final String usage = "RefundTransmissionFeesAndTaxes -file=FullPathOfFile";

        for (String arg : args) {
            String[] argParts = arg.split("=");
            if (argParts.length == 2) {
                if (argParts[0].equals(FILE_NAME_COMMAND)) {
                    mFileName = argParts[1];
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

        List<FinancialTransaction> financialTransactionList = new ArrayList<FinancialTransaction>();
        Application.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.BatchJob));
        try {
            parseArgs(args);

            System.out.println("INFO: Starting Refund Transmission Fees And Taxes with filename - " + mFileName);

            // Open the file
            FileReader fileReader = null;
            try {
                File f = new File(mFileName);
                fileReader = new FileReader(f);
                BufferedReader input = new BufferedReader(fileReader);

                String line;

                PayrollServices.beginUnitOfWork();

                // For each transaction in the file
                while ((line = input.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length != 1) {
                        System.out.println("ERROR: Invalid line in input file - " + line);
                    } else {
                        String financialTransactionId = values[0];
                        FinancialTransaction financialTransaction = getFinancialTransaction(financialTransactionId);
                        if (financialTransaction != null) {
                            financialTransactionList.add(financialTransaction);
                        }
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

        totalNumberOfTransactionsToBeProcessed = financialTransactionList.size();
        totalNumberOfTransactionsProcessed = refundFeesAndTaxes(financialTransactionList);

        System.out.println("INFO: Process completed. " + "Total number of transaction to be refunded - " + totalNumberOfTransactionsToBeProcessed +
                                   ". Total number of transaction refunded - " + totalNumberOfTransactionsProcessed + ".");

        System.exit(0);
    }

    private static FinancialTransaction getFinancialTransaction(String financialTransactionId) {

        System.out.println("INFO: Finding FT with id - " + financialTransactionId);

        FinancialTransaction financialTransaction = Application.findById(FinancialTransaction.class, new SpcfUniqueIdImpl(financialTransactionId));

        if (financialTransaction == null) {
            System.out.println("WARN: Unable to find FT - " + financialTransactionId);
        } else {
            System.out.println("INFO: Found FT - " + financialTransactionId);
        }

        return financialTransaction;
    }

    private static int refundFeesAndTaxes(List<FinancialTransaction> financialTransactionList) {
        int countOfTransactionsRefunded = 0;
        try {

            for (FinancialTransaction financialTransaction : financialTransactionList) {
                PayrollServices.beginUnitOfWork();
                Application.refresh(financialTransaction);
                try {
                    TransactionTypeCode transactionTypeCode = financialTransaction.getTransactionType().getTransactionTypeCd();
                    if (transactionTypeCode == TransactionTypeCode.EmployerFeeDebit ||
                            transactionTypeCode == TransactionTypeCode.ServiceSalesAndUseTax) {
                        System.out.println("INFO: Processing Financial Transaction " + financialTransaction.getId() + " - Transaction Type " + transactionTypeCode);

                        Company company = financialTransaction.getCompany();

                        ERRefundDTO erRefundDTO = new ERRefundDTO();
                        erRefundDTO.setSettlementType(SettlementTypeDTO.ACH);
                        erRefundDTO.setFinancialTxId(financialTransaction.getId().toString());
                        erRefundDTO.setFinancialTxAmt(financialTransaction.getFinancialTransactionAmount());
                        erRefundDTO.setTxDate(new DateDTO(PSPDate.getPSPTime()));

                        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.
                                refundEmployerTransaction(
                                        company.getSourceSystemCd(),
                                        company.getSourceCompanyId(),
                                        erRefundDTO);
                        if (processResult.isSuccess()) {
                            System.out.println("Created Refund Transaction - " + processResult.getResult().getId());
                            System.out.println("Successfully processed Financial Transaction " + financialTransaction.getId());
                            countOfTransactionsRefunded++;
                        } else {
                            System.out.println("Unable to process Financial Transaction " + financialTransaction.getId() + ". Error while creating refund - " + processResult.getErrorMessages());
                        }
                    } else {

                        throw new RuntimeException("Invalid Transaction Type - Transaction Id - " + financialTransaction.getId() +
                                                           "Transaction Type - " + financialTransaction.getTransactionType().getTransactionTypeCd() +
                                                           ". Transaction Types need to be either EmployerFeeDebit or ServiceSalesAndUseTax to process refeund");
                    }
                    PayrollServices.commitUnitOfWork();

                } catch (Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                    e.printStackTrace();
                    PayrollServices.rollbackUnitOfWork();
                }
            }

        } catch (Throwable t) {
            System.out.println("ERROR: Error during call to refundFeesAndTaxes.");
            t.printStackTrace();
        }
        return countOfTransactionsRefunded;

    }
}
