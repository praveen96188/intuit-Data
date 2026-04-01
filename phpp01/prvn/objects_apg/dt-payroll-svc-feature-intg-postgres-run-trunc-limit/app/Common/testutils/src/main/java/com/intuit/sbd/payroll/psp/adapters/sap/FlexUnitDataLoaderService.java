/*
 * $Id: //psp/dev/Common/TestUtils/src/com/intuit/sbd/payroll/psp/adapters/sap/FlexUnitDataLoaderService.java#2 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.DatabaseConfigManager;
import com.intuit.sbd.payroll.psp.domain.OffloadGroup;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyBankAccountDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.TransactionReturnTestDataLoader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.coretests.*;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.kafka.common.protocol.types.Field;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import scala.tools.nsc.Global;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * FlexUnitDataLoaderService - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class FlexUnitDataLoaderService {

    private HashMap<String, Method> dataLoaderRegistry = new HashMap<String, Method>();
    private static final SpcfLogger logger = PayrollServices.getLogger(FlexUnitDataLoaderService.class);

    public FlexUnitDataLoaderService() {
        // set the psp principal

        try {
            //dataLoaderRegistry.put("Employee :: Update Employee tax id",
            //        EmployeeUpdateDataLoader.class.getMethod("changeEmployeeTaxId"));
            dataLoaderRegistry.put("Company :: Create Basic Data",
                    SAPCompanyDataLoader.class.getMethod("createCompanyData"));
            dataLoaderRegistry.put("Company :: Create Basic Data with Test Events",
                    SAPCompanyDataLoader.class.getMethod("createEventTestCompany"));
            dataLoaderRegistry.put("Company :: Create QBDT Company with Cancelled Random Debits",
                                CompanyBankAccountDataLoader.class.getMethod("createCompanyWithCancelledDebits"));
            //dataLoaderRegistry.put("Company :: Create Activations (Lots of companies)",
            //        ActivationsDataLoader.class.getMethod("loadManyActivationsCompanies"));
            //dataLoaderRegistry.put("Company :: Create 1000 Companies",
            //        SAPCompanyDataLoader.class.getMethod("create1000Companies"));
            dataLoaderRegistry.put("Company :: Create QBDT Company",
                    AddCompanyDataLoader.class.getMethod("loadAddQBDTCompanyCoreDiffAgreeFailsFraudControls"));
            dataLoaderRegistry.put("Payroll :: Issue Refund Test",
                    ERFinancialTxRefundCoreDataLoader.class.getMethod("addEmployerDDRejectRefundTransaction"));
            dataLoaderRegistry.put("Payroll :: Add Escalation Test",
                    AddEscalationProcessDataLoader.class.getMethod("loadPayrollRunForAddEscalationTest"));
            dataLoaderRegistry.put("Payroll :: Reverse Transaction Test",
                    TransactionReverseCoreDataLoader.class.getMethod("loadPayrollRunForTransactionReverseTest"));
            dataLoaderRegistry.put("Payroll :: Add Employer Fee Test",
                    ERFeeAddCoreDataLoader.class.getMethod("loadDataForAddFeeForACHSettlementWithSetup"));
            dataLoaderRegistry.put("Payroll :: Void Txn ACH Test",
                    VoidDDFinancialTransactionCoreDataLoader.class.getMethod("loadTestTxVoidDataACHWithSetup"));
            dataLoaderRegistry.put("Payroll :: Void Txn Non-ACH Test",
                    VoidDDFinancialTransactionCoreDataLoader.class.getMethod("loadTestTxVoidDataNonACHWithSetup"));
            dataLoaderRegistry.put("Payroll :: Cancel Employer Txn Test",
                    CancelERFinancialTxCoreDataLoader.class.getMethod("loadTestERCancelDataWithSetup"));
            dataLoaderRegistry.put("Payroll :: Add Redebit Test",
                    RedebitAddCoreDataLoader.class.getMethod("loadTestRedebitAddDataWithSetup"));
            dataLoaderRegistry.put("Payroll :: Add Repayment Txn Test",
                    AddRepaymentTransactionsDataLoader.class.getMethod("submitPayrollWithSetup"));
            dataLoaderRegistry.put("Bank Returns :: Find Bank Returns Test",
                    TransactionReturnTestDataLoader.class.getMethod("loadDataForMultipleTransactionReturns"));
            dataLoaderRegistry.put("Bank Returns :: Lots of Bank Returns Test",
                    TransactionReturnTestDataLoader.class.getMethod("createLotsaBankReturns"));
            dataLoaderRegistry.put("Company :: Load QBDT OFX Transmission",
                    AddQBDTOFX.class.getMethod("loadOFXTransmission"));
            dataLoaderRegistry.put("Payroll :: Add Recover Bad Debt Txn Test",
                    ACHReturnsDataLoader.class.getMethod("loadData2DayERGenericRetAgentWritesOff"));
            dataLoaderRegistry.put("Payroll :: Add Recover Bad Debt Txn With Fee Test",
                    AddRecoverBadDebtTransactionDataLoader.class.getMethod("loadAddRecoverBadDebtDataWithFee"));
            dataLoaderRegistry.put("Payroll :: Add Bad Debt Write-Off Test",
                    ACHReturnsDataLoader.class.getMethod("loadData2DayERGenericReturn"));
            dataLoaderRegistry.put("Payroll :: Add Employee Return Refund Txn Test",
                    ACHReturnsDataLoader.class.getMethod("loadData2DayCompanyPutOnHold1EEReturnCompanyOffHold"));
            dataLoaderRegistry.put("Payroll :: Add Employer Return Refund Txn Test",
                    ACHReturnsDataLoader.class.getMethod("loadData2DayERNSFsWireRepayment"));
            dataLoaderRegistry.put("Payroll :: Add Employee Return Transfer Test",
                    ACHReturnsDataLoader.class.getMethod("loadDataForEEReturnTransferReturn"));
            dataLoaderRegistry.put("Payroll :: Add Fee Transfer Test",
                    ACHReturnsDataLoader.class.getMethod("loadData2DayERNSFsOffloadRedebitAndReturnFee"));
            dataLoaderRegistry.put("Payroll :: Add Refund Txn Test",
                    ACHReturnsDataLoader.class.getMethod("loadData5AgentCancels1CheckCancelsRefund"));
            dataLoaderRegistry.put("Payroll :: Add Intuit 5 Day Return Transfer Test",
                    ACHReturnsDataLoader.class.getMethod("loadData2PayRunsERNSFsAgentCancels2nd"));
            dataLoaderRegistry.put("Payroll :: Add Redebit Test (QBDT)",
                    ACHReturnsDataLoader.class.getMethod("loadQBDTPayrollReturnedAddPayrollRedebitR02NonNSF"));
            dataLoaderRegistry.put("Fraud :: Load fraud company and payroll",
                    LoadFraudEvents.class.getMethod("loadFraudCompanyAndPayroll"));
            dataLoaderRegistry.put("Fraud :: Load payroll with same employee bank accounts",
                    LoadFraudEvents.class.getMethod("loadCompanyAddEmployeeWithSameBankAccount"));
            dataLoaderRegistry.put("Fraud :: Load 2 Payroll Fraud Companies",
                    LoadFraudEvents.class.getMethod("testFraudulentPayrollsProcess_MultipleBatches"));
            dataLoaderRegistry.put("Fraud :: Load 4 Payroll Fraud Events from 3 Companies",
                    LoadFraudEvents.class.getMethod("testFraudulentPayrollsProcess_3Companies"));
            dataLoaderRegistry.put("Fraud :: Load 6 Payroll/Signup Fraud Events from 4 Companies",
                    LoadFraudEvents.class.getMethod("load6PayrolLEventsFrom4Companies"));
            dataLoaderRegistry.put("Payroll :: Load refund/rebill data",
                    SAPPayrollDataLoader.class.getMethod("loadQBDTCompanyRequests1TxnReversed"));
            dataLoaderRegistry.put("Payroll :: Load Wire Expected Date Test",
                    AddWireExpectedDataLoader.class.getMethod("loadWireExpectedDate"));
            dataLoaderRegistry.put("Load User Data Only",
                    FlexUnitDataLoaderService.class.getMethod("nullDataLoader"));
			dataLoaderRegistry.put("Payroll :: ACH Offload Data",
                    ACHReturnsDataLoader.class.getMethod("loadQBDTPayrollOffloaded"));
            dataLoaderRegistry.put("Payroll :: Refund ER for Fraud or Escalation Data",
                    RefundERFraudOrEscalationDataLoader.class.getMethod("loadRefundData"));
            dataLoaderRegistry.put("Payroll :: Load prefund payroll with taxes",
                    PrefundPayrollDataLoader.class.getMethod("load"));
            dataLoaderRegistry.put("Payroll :: Load multiple NSF payrolls",
                    SAPPayrollDataLoader.class.getMethod("loadCompanyWith2NSFPayrolls"));
//          dataLoaderRegistry.put("Payroll :: Multiple payrolls exceed 100k Limit",
//                  PayrollSubmitTaxDataLoader.class.getMethod("loadMultiplePayrolls100kLimit"));
//            dataLoaderRegistry.put("Tax Payments :: Reject payment",
//                    TaxPaymentsSynchronizationDataLoader.class.getMethod("loadRejectTaxPayments"));
//            dataLoaderRegistry.put("Tax Payments :: Payroll with tax adjustments",
//                    PayrollSubmitTaxDataLoader.class.getMethod("payrollsWithTaxAdjustments"));
            dataLoaderRegistry.put("Util :: Run offload",
                    FlexUnitDataLoaderService.class.getMethod("runOffload"));
            dataLoaderRegistry.put("Util :: Run ach handler",
                    FlexUnitDataLoaderService.class.getMethod("runACHHandler"));
        } catch (Throwable ex) {
            logger.error("Error loading registry", ex);
        }
    }

    public static void nullDataLoader() {

    }

    @Test
    public void runActivations() throws Exception {
     runDataLoader("Company :: Create Activations Gemini Company (Unassigned)");
    }

    public void runDataLoader(String dataLoaderKey) throws Exception {
        Method m = dataLoaderRegistry.get(dataLoaderKey);
        boolean isUtilMethod = dataLoaderKey.indexOf("Util ::") > -1;

        try {
            if (m != null) {
                PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
                if(!isUtilMethod){
                    PayrollServicesTest.truncateTables();
                }
                m.invoke(null);
            } else {
                throw new Exception("DataLoader not registered for key " + dataLoaderKey);
            }

            if(!isUtilMethod){
            AddUsers();
        }
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public String[] getRegisteredDataLoaders() {
        return dataLoaderRegistry.keySet().toArray(new String[dataLoaderRegistry.keySet().size()]);
    }

    public void changePSPDate(Date pNewDate) throws Exception {
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(pNewDate.getTime()));
            PayrollServices.commitUnitOfWork();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        }
    }

    public Date getPSPDate() {
        try {
            PayrollServices.beginUnitOfWork();
            return CalendarUtils.convertToDate(PSPDate.getPSPTime());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public static void runOffload(){
        try{
            OffloadACHTransactions offloader = new OffloadACHTransactions();
            offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
        }
        finally{
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public static void runACHHandler(){
        try{
            PayrollServices.beginUnitOfWork();
            SpcfCalendar postDate = PSPDate.getPSPTime();
            CalendarUtils.addBusinessDays(postDate, 5);
            PSPDate.setPSPTime(postDate);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            ProcessACHTransactions post = new ProcessACHTransactions();
            post.process(PSPDate.getPSPTime());
            PayrollServices.commitUnitOfWork();
        }
        finally{
            PayrollServices.rollbackUnitOfWork();
        }
    }


    public static String getSqlPathForAddUsers() {
        String sqlFilePath;
        //it seems that property is not defined anywhere but leaving this as it is for now
        if (System.getProperty("insertTestUsers") != null) {
            if(Application.isOracleDB()) {
                sqlFilePath = new File(System.getProperty("insertTestUsers")).getAbsolutePath();
            } else {
                //see if this file is being user
                sqlFilePath = new File(System.getProperty("insertTestUsers")).getAbsolutePath();
            }

        } else {
            if(Application.isOracleDB()) {
                sqlFilePath = findFileOnClassPath("create_sap_users.sql");
            } else {
                sqlFilePath = findFileOnClassPath("postgres/create_sap_users_pg.sql");
            }
        }
        return sqlFilePath;
    }

    /**
     * Add users to the database
     */
    public static void AddUsers() {
        Process process = null;
        String monolithDBConnectionToken = DatabaseConfigManager.MonolithDbToken;
        String dbUserName= ConfigurationManager.getSettingValue(monolithDBConnectionToken, "dataAccess.connection.username");
        String dbPassword=ConfigurationManager.getSettingValue(monolithDBConnectionToken, "dataAccess.connection.password");

        String sqlFilePath = getSqlPathForAddUsers();
        String processCommandString;
        try {
            if (Application.isOracleDB()) {
                processCommandString = String.format("sqlplus %s/%s@XE @%s", dbUserName, dbPassword, sqlFilePath);
            } else {
                processCommandString = String.format("psql postgresql://%s:%s@127.0.0.1:5432/psp -f %s", dbUserName, dbPassword, sqlFilePath);
            }
            process = Runtime.getRuntime().exec(processCommandString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedWriter writer = null;
        try {
            if (Application.isOracleDB() && process != null) {
                writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                writer.write("exit\n");
                writer.flush();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while (reader.readLine() != null);
            }
            if(process != null) {
                process.waitFor();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(writer != null) {
                try{
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String[] readStrings(String fileName) {
        BufferedReader reader;
        String filePath = findFileOnClassPath(fileName);
        try {
            reader = new BufferedReader(new FileReader(filePath));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(fileName + " not found");  //TODO: revise when exception handling standard is defined
        }

        String line;
        ArrayList<String> lines = new ArrayList<String>();
        try {
            while ((line = reader.readLine()) != null) {
                if (!line.equals("/") && !line.trim().startsWith("--") && line.length() > 0) {
                    lines.add(line);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error while reading " + filePath);  //TODO: revise when exception handling standard is defined
        }

        return lines.toArray(new String[lines.size()]);
    }

     public static String findFileOnClassPath(String fileName) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        ResourceLoader resourceLoader = resolver.getResourceLoader();
        Resource resource = resourceLoader.getResource("classpath:/" + fileName);
        String path;
        if (resource.exists()) {
            try {
                path = resource.getFile().getAbsolutePath();
            }
            catch (IOException ex) {
                throw new RuntimeException("File not found: " + fileName);
            }
        }
        else {
            throw new RuntimeException("File not found: " + fileName);
        }
        return path;
    }

    public static void main(String args[]) {
        try {
            PayrollServices.beginUnitOfWork();

            //SAPCompanyDataLoader.createCompanyData();

            //Reset PSP Date
            PSPDate.resetPSPTime();

            PayrollServices.commitUnitOfWork();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
