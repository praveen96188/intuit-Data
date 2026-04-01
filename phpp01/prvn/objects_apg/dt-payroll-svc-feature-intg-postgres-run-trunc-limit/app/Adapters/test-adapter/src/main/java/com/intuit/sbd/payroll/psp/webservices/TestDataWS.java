package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.*;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.SubmitPayrollRequest;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.test.QBDTWSRequestCreator;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.eoqsuiadjustments.LiabilityAdjustmentsCleanUp;
import com.intuit.sbd.payroll.psp.batchjobs.eoqsuiadjustments.SUIRatePaymentsCleanUp;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EoqSUIAdjustmentsProcessor;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.CompanyNote;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.sbd.payroll.psp.webservices.wsdto.PropertyValue;
import com.intuit.sbd.payroll.psp.webservices.wsdto.TableWSDTO;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 8, 2008
 * Time: 4:45:30 PM
 * To change this template use File | Settings | File Templates.
 */
@WebService()
public class TestDataWS {
    @WebMethod
    public void deleteAll(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                          @WebParam(name = "sourceCompanyID") String sourceCompanyID) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        SpcfLogger logger = Application.getLogger(TestDataWS.class);
        String envId = ConfigurationManager.getEnvironmentIdentifier();
        boolean defaultEnvironments = "local".equals(envId)
                || "dev".equals(envId) || "dev1".equals(envId) || "dev2".equals(envId)
                || "ds2".equals(envId) || "ds3".equals(envId) || "ltq2".equals(envId)
                || "ds1".equals(envId);
        SystemParameter allowDeleteAll = SystemParameter.findSystemParameter(SystemParameter.Code.PSP_TEST_DELETE_ALL);
        boolean allowedEnvironment = defaultEnvironments || Boolean.parseBoolean(allowDeleteAll.getSystemParameterValue());
        try {
            PayrollServices.beginUnitOfWork();
            if (allowedEnvironment) {
                logger.info("Calling truncate tables...");
                Application.truncateTables();
                ApplicationSecondary.truncateTables();
                PSPDate.resetPSPTime();
                logger.info("Truncate tables complete.");
            } else {
                logger.info("Deleting all companies is not allowed from Environement:" + envId);
                throw new RuntimeException("Deleting all companies is not allowed from Environement:" + envId);
            }
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void deleteAllCompanies(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                   @WebParam(name = "sourceCompanyID") String sourceCompanyID) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        SpcfLogger logger = Application.getLogger(TestDataWS.class);
        String envId = ConfigurationManager.getEnvironmentIdentifier();
        SystemParameter allowDeleteAll = SystemParameter.findSystemParameter(SystemParameter.Code.PSP_TEST_DELETE_ALL);
        boolean allowedEnvironment = Boolean.parseBoolean(allowDeleteAll.getSystemParameterValue());
        if (!allowedEnvironment) {
            String errorMessage = "Deleting company(s) is not allowed for Environement:" + envId;
            logger.info(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        try {
            PayrollServices.beginUnitOfWork();
            if (allowedEnvironment) {
                logger.info("Calling truncate tables...");
                Application.truncateTables();
                ApplicationSecondary.truncateTables();
                PSPDate.resetPSPTime();
                logger.info("Truncate tables complete.");
            } else {
                logger.info("Deleting all companies is not allowed from Environement:" + envId);
                throw new RuntimeException("Deleting all companies is not allowed from Environement:" + envId);
            }
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void deleteFEIN(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                           @WebParam(name = "fein") String fein) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            SourceSystem sourceSystem = Application.findById(SourceSystem.class, SourceSystemCode.valueOf(sourceSystemCD));
            if (sourceSystem == null) {
                throw new RuntimeException("Invalid SourceSystemCode");
            }
            Company company = Company.findActiveCompany(SourceSystemCode.valueOf(sourceSystemCD), fein);
            if (company != null) {
                Application.deleteCompany(company.getId().toString());
                System.out.println("Deleting company finished");
                PayrollServices.commitUnitOfWork();
            } else {
                PayrollServices.commitUnitOfWork();
            }
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void setSystemParameter(@WebParam(name = "systemParameterName") String pSystemParameterName,
                                   @WebParam(name = "systemParameterValue") String pSystemParameterValue) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (pSystemParameterName == null || pSystemParameterName.trim().length() == 0) {
            throw new RuntimeException("No systemParameterName is specified");
        }
        if (pSystemParameterValue == null || pSystemParameterValue.trim().length() == 0) {
            throw new RuntimeException("No systemParameterValue is specified");
        }
        if (!(Validator.isValidLength(pSystemParameterValue, 1, 400))) {
            throw new RuntimeException("systemParameterValue length must be between 1 and 400 characters");
        }
        try {
            PayrollServices.beginUnitOfWork();
            PayrollServices.systemParameterManager.updateSystemParameterValue(pSystemParameterName, pSystemParameterValue);
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public String getSystemParameter(@WebParam(name = "systemParameterName") String pSystemParameterName) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (pSystemParameterName == null || pSystemParameterName.trim().length() == 0) {
            throw new RuntimeException("No systemParameterName is specified");
        }
        try {
            PayrollServices.beginUnitOfWork();
            SystemParameter systemParameter = SystemParameter.findSystemParameter(pSystemParameterName);
            PayrollServices.commitUnitOfWork();
            return systemParameter.getSystemParameterValue();
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void deleteCompany(Company company) {
        System.out.println("############## Getting Company Events ##################");
        DomainEntitySet<CompanyEvent> companyEvents = Application.find(CompanyEvent.class, CompanyEvent.Company().equalTo(company));
        for (CompanyEvent companyEvent : companyEvents) {
            delete(companyEvent);
        }
        DomainEntitySet<CompanyNote> companyNotes = company.getCompanyNoteCollection();
        Iterator<CompanyNote> iterator = companyNotes.iterator();
        while (iterator.hasNext()) {
            deleteCompanyNote(iterator.next());
        }
        DomainEntitySet<CompanyOffer> companyOffers =
                Application.find(CompanyOffer.class,
                                 CompanyOffer.Company().equalTo(company));
        for (CompanyOffer companyOffer : companyOffers) {
            deleteCompanyOffer(companyOffer);
        }
        DomainEntitySet<CompanyService> companyServices =
                Application.find(CompanyService.class,
                                 CompanyService.Company().equalTo(company));
        for (CompanyService companyService : companyServices) {
            deleteCompanyService(companyService);
        }
        DomainEntitySet<EntryDetailRecord> entryDetailRecords =
                Application.find(EntryDetailRecord.class,
                                 EntryDetailRecord.Company().equalTo(company));
        for (EntryDetailRecord entryDetailRecord : entryDetailRecords) {
            delete(entryDetailRecord);
        }
        DomainEntitySet<FinancialTransaction> financialTransactions =
                Application.find(FinancialTransaction.class,
                                 FinancialTransaction.Company().equalTo(company));
        for (FinancialTransaction financialTransaction : financialTransactions) {
            deleteFinancialTransaction(financialTransaction);
        }
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions =
                Application.find(MoneyMovementTransaction.class,
                                 MoneyMovementTransaction.Company().equalTo(company));
        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            deleteMoneyMovementTransaction(moneyMovementTransaction);
        }
        DomainEntitySet<PayrollRun> payrollRuns =
                Application.find(PayrollRun.class,
                                 PayrollRun.Company().equalTo(company));
        for (PayrollRun payrollRun : payrollRuns) {
            deletePayrollRun(payrollRun);
        }
        DomainEntitySet<Employee> employees =
                Application.find(Employee.class,
                                 Employee.Company().equalTo(company));
        for (Employee employee : employees) {
            deleteEmployee(employee);
        }
        DomainEntitySet<CompanyBankAccount> companyBankAccounts =
                Application.find(CompanyBankAccount.class,
                                 CompanyBankAccount.Company().equalTo(company));
        for (CompanyBankAccount companyBankAccount : companyBankAccounts) {
            deleteCompanyBankAccount(companyBankAccount);
        }
        DomainEntitySet<PropertyAudit> propertyAudits =
                Application.find(PropertyAudit.class,
                                 PropertyAudit.Company().equalTo(company));
        for (PropertyAudit propertyAudit : propertyAudits) {
            Application.delete(propertyAudit);
        }
        DomainEntitySet<TransactionResponse> transactionResponses =
                Application.find(TransactionResponse.class,
                                 TransactionResponse.Company().equalTo(company));
        for (TransactionResponse transactionResponse : transactionResponses) {
            deleteTransactionResponse(transactionResponse);
        }
        Address mailingAddress = company.getMailingAddress();
        Address legalAddress = company.getLegalAddress();
        DomainEntitySet<CompanyOffering> offerings = company.getCompanyOfferingCollection();
        delete(company);
        delete(mailingAddress);
        delete(legalAddress);
        for (CompanyOffering currentOffering : offerings) {
            delete(currentOffering);
        }

    }

    private void deleteCompanyBankAccount(CompanyBankAccount cba) {
        BankAccount bankAccount = cba.getBankAccount();
        delete(cba);
        delete(bankAccount);
    }

    private void deleteCompanyNote(CompanyNote companyNote) {
        delete(companyNote);
    }

    private void deleteCompanyOffer(CompanyOffer companyOffer) {
        Offer offer = companyOffer.getOffer();
        delete(companyOffer);
    }

    private void deleteCompanyService(CompanyService companyService) {
        DomainEntitySet<CompanyServiceBankAccount> companyServiceBankAccounts = Application.find(CompanyServiceBankAccount.class, CompanyServiceBankAccount.CompanyService().equalTo(companyService));
        for (CompanyServiceBankAccount companyServiceBankAccount : companyServiceBankAccounts) {
            delete(companyServiceBankAccount);
        }
        DDCompanyServiceInfo ddCompanyServiceInfo =
                Application.findById(DDCompanyServiceInfo.class, companyService.getId());
        delete(ddCompanyServiceInfo);
        delete(companyService);

    }

    private void deleteEmployee(Employee employee) {
        DomainEntitySet<EmployeeBankAccount> eeBankAccounts = Application.find(EmployeeBankAccount.class, EmployeeBankAccount.Employee().equalTo(employee));
        for (EmployeeBankAccount employeeBankAccount : eeBankAccounts) {
            delete(employeeBankAccount);
        }
        delete(employee);
    }

    private void deleteFinancialTransaction(FinancialTransaction financialTransaction) {
        DomainEntitySet<FinancialTransaction> associatedFinTxs =
                financialTransaction.getAssociatedTransactionsCollection();
        Iterator<FinancialTransaction> iterator = associatedFinTxs.iterator();
        while (iterator.hasNext()) {
            deleteFinancialTransaction(iterator.next());
        }
        DomainEntitySet<FinancialTransactionState> financialTransactionStates =
                financialTransaction.getFinancialTransactionStates();
        Iterator<FinancialTransactionState> stateIterator = financialTransactionStates.iterator();
        FinancialTransactionState finTxState = null;
        //Code to delete the ledger entries is removed because of the Ledger class removal
        while (stateIterator.hasNext()) {
            finTxState = stateIterator.next();
            delete(finTxState);
        }
        DomainEntitySet<TransactionOffloadBatch> transactionOffloadBatchs = Application.find(TransactionOffloadBatch.class, TransactionOffloadBatch.FinancialTransaction().equalTo(financialTransaction));
        Object[] txOffloadBatchs = transactionOffloadBatchs.toArray();
        for (Object transactionOffloadBatch : txOffloadBatchs) {
            delete((TransactionOffloadBatch) transactionOffloadBatch);
        }
        DomainEntitySet<TransactionReturn> transactionReturns =
                Application.find(TransactionReturn.class,
                                 TransactionReturn.MoneyMovementTransaction().equalTo(financialTransaction.getMoneyMovementTransaction()));
        Object[] txReurns = transactionReturns.toArray();
        for (Object transactionReturn : txReurns) {
            delete((TransactionReturn) transactionReturn);
        }
        MoneyMovementTransaction mmTx = financialTransaction.getMoneyMovementTransaction();
        delete(financialTransaction);
        if (mmTx != null) {
            deleteMoneyMovementTransaction(mmTx);
        }
    }

    private void deleteMoneyMovementTransaction(MoneyMovementTransaction moneyMovementTransaction) {
        delete(moneyMovementTransaction);
    }

    private void deletePayrollRun(PayrollRun payrollRun) {
        DomainEntitySet<Paycheck> payChecks = payrollRun.getPaycheckCollection();
        Iterator<Paycheck> payCheckIterator = payChecks.iterator();
        Paycheck paycheck = null;
        DomainEntitySet<PaycheckSplit> paycheckSplits = null;
        PaycheckSplit paycheckSplit = null;
        Iterator<PaycheckSplit> paycheckSplitIterator = null;
        while (payCheckIterator.hasNext()) {
            paycheck = payCheckIterator.next();
            paycheckSplits = paycheck.getPaycheckSplitCollection();
            paycheckSplitIterator = paycheckSplits.iterator();
            while (paycheckSplitIterator.hasNext()) {
                paycheckSplit = paycheckSplitIterator.next();
                delete(paycheckSplit.getEmployeeBankAccount());
                delete(paycheckSplit);
            }
            //Todo MV - delete compensations, deductions, taxes
            delete(paycheck);
        }
        delete(payrollRun);
    }

    private void deleteTransactionResponse(TransactionResponse transactionResponse) {
        delete(transactionResponse);
    }

    private void delete401kData() {
        DomainEntitySet<ThirdParty401kBatch> batch = Application.find(ThirdParty401kBatch.class);
        for (ThirdParty401kBatch item : batch) {
            Application.delete(item);
        }
        DomainEntitySet<ThirdParty401kSignUpQueue> signUpQueue = Application.find(ThirdParty401kSignUpQueue.class);
        for (ThirdParty401kSignUpQueue item : signUpQueue) {
            Application.delete(item);
        }
        DomainEntitySet<ThirdParty401kSignUpBatch> SignUpBatch = Application.find(ThirdParty401kSignUpBatch.class);
        for (ThirdParty401kSignUpBatch item : SignUpBatch) {
            Application.delete(item);
        }
    }

    private <T> void delete(T obj) {
        if (obj != null) {
            Application.delete((DomainEntity) obj);
        }
    }

    @WebMethod
    public void enableFakeSalesTaxGateway() throws Exception {
        SystemParameter.update(SystemParameter.Code.SALES_TAX_GATEWAY_IMPLEMENTATION_CLASS, FakeSalesTaxGateway.class.getName());
    }

    @WebMethod
    public void disableFakeSalesTaxGateway() throws Exception {
        SystemParameter.update(SystemParameter.Code.SALES_TAX_GATEWAY_IMPLEMENTATION_CLASS, "");
    }

    @WebMethod
    public List<TableWSDTO> getTableValues(@WebParam(name = "tableName") String tableName) {
        try {
            PayrollServices.beginUnitOfWork();
            List<TableWSDTO> rows = new ArrayList<TableWSDTO>();
            String query = "select * from ? ";
            PreparedStatement stmt = Application.getConnection().prepareStatement(query);
            stmt.setString( 1, tableName);
                    stmt.execute();
            ResultSet rs = stmt.getResultSet();
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                TableWSDTO row = new TableWSDTO();
                row.values = new ArrayList<PropertyValue>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    PropertyValue pv = new PropertyValue();
                    pv.property = metaData.getColumnName(i);
                    pv.value = rs.getString(i);
                    row.values.add(pv);
                }
                rows.add(row);
            }
            return rows;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public SubmitPayrollRequest createSubmitPayrollRequestFromCompany(@WebParam(name = "sourceSystemCD") String sourceSystemCD,
                                                                      @WebParam(name = "sourceCompanyID") String sourceCompanyID) throws Exception {
        if (sourceSystemCD == null || sourceSystemCD.trim().length() == 0) {
            throw new RuntimeException("No sourceSystemCD is specified");
        }
        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("No sourceCompanyID is specified");
        }
        SourceSystem sourceSystem = Application.findById(SourceSystem.class, SourceSystemCode.valueOf(sourceSystemCD));
        if (sourceSystem == null) {
            throw new RuntimeException("Invalid SourceSystemCode");
        }
        SubmitPayrollRequest submitPayrollRequest;
        try {
            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(sourceCompanyID, SourceSystemCode.valueOf(sourceSystemCD));
            if (company != null) {
                submitPayrollRequest = QBDTWSRequestCreator.createSubmitPayrollRequestFromCompany(company);
            } else {
                throw new RuntimeException("No Company for sourceCompanyID and sourceSystemCD is specified");
            }
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return submitPayrollRequest;
    }

    @WebMethod
    public void updatePaymentTemplateSupportStartDate(@WebParam(name = "paymentTemplateCd") String pPaymentTemplateCd,
                                                      @WebParam(name = "supportDate") String pSupportedDate) throws Exception {
        if (pPaymentTemplateCd == null || pPaymentTemplateCd.trim().length() == 0) {
            throw new RuntimeException("Payment Template Cd cannot be null or empty");
        }
        try {
            PayrollServices.beginUnitOfWork();
            PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, pPaymentTemplateCd);
            if (paymentTemplate == null) {
                throw new RuntimeException("Payment Template " + pPaymentTemplateCd + " does not exist");
            }
            if (pSupportedDate != null) {
                paymentTemplate.setSupportStartDate(CalendarUtils.convertToSpcfCalendar(new Date(pSupportedDate)));
            } else {
                paymentTemplate.setSupportStartDate(null);
            }
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void runSUICleanUp(@WebParam(name = "startPaycheckDate") String pStartPaycheckDate, @WebParam(name = "endPaycheckDate") String pEndPaycheckDate, @WebParam(name = "payrollRunDate") String pPayrollRunDate) {
        ArrayList<String> parameterList = new ArrayList<String>();
        parameterList.add("-commit=true");
        if (pStartPaycheckDate != null) {
            parameterList.add("-startPaycheckDate=" + pStartPaycheckDate);
        }
        if (pEndPaycheckDate != null) {
            parameterList.add("-endPaycheckDate=" + pEndPaycheckDate);
        }
        if (pPayrollRunDate != null) {
            parameterList.add("-startDate=" + pPayrollRunDate);
        }
        String[] parameters = new String[parameterList.size()];
        parameters = parameterList.toArray(parameters);
        SUIRatePaymentsCleanUp.main(parameters);
    }

    @WebMethod
    public void runLiabilityAdjustmentsCleanUp(@WebParam(name = "quarter") String pQuarter) {
        LiabilityAdjustmentsCleanUp.main(new String[]{"-commit=true", "-quarter=" + pQuarter});
    }

    @WebMethod
    public void setAllATFExtractSystemParameters(@WebParam(name = "systemParameterValue") String pSystemParameterValue) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (pSystemParameterValue == null || pSystemParameterValue.trim().length() == 0) {
            throw new RuntimeException("No systemParameterValue is specified");
        }
        if (!(Validator.isValidLength(pSystemParameterValue, 1, 400))) {
            throw new RuntimeException("systemParameterValue length must be between 1 and 400 characters");
        }
        try {
            PayrollServices.beginUnitOfWork();
            PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PERFORM_ATF_DEPOSIT_FREQUENCY_EXTRACT, pSystemParameterValue);
            PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PERFORM_ATF_WAGE_LIMITS_EXTRACT, pSystemParameterValue);
            PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PERFORM_ATF_COMPANY_INFO_EXTRACT, pSystemParameterValue);
            PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PERFORM_ATF_EMPLOYEE_INFO_EXTRACT, pSystemParameterValue);
            PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_EXTRACT, pSystemParameterValue);
            PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PERFORM_ATF_COMPANY_TAX_RATE_EXTRACT, pSystemParameterValue);
            PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PERFORM_ATF_EMPLOYEE_QUARTERLY_TOTALS_EXTRACT, pSystemParameterValue);
            PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PERFORM_ATF_W2_COUNT_INFO_EXTRACT, pSystemParameterValue);
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void runEoqAdjustmentsCombined(@WebParam(name = "startPaycheckDate") String pStartPaycheckDate,
                                          @WebParam(name = "endPaycheckDate") String pEndPaycheckDate,
                                          @WebParam(name = "payrollRunDate") String pPayrollRunDate,
                                          @WebParam(name = "processingDate") String pProcessingDate,
                                          @WebParam(name = "quarter") String pQuarter) {
        ArrayList<String> parameterList = new ArrayList<String>();
        parameterList.add("-commit=true");
        if (pStartPaycheckDate != null) {
            parameterList.add("-startPaycheckDate=" + pStartPaycheckDate);
        }
        if (pEndPaycheckDate != null) {
            parameterList.add("-endPaycheckDate=" + pEndPaycheckDate);
        }
        if (pPayrollRunDate != null) {
            parameterList.add("-startDate=" + pPayrollRunDate);
        }
        if (pProcessingDate != null) {
            parameterList.add("-processingDate=" + pProcessingDate);
        }
        if (pQuarter != null) {
            parameterList.add("-quarter=" + pQuarter);
        }
        String[] parameters = new String[parameterList.size()];
        parameters = parameterList.toArray(parameters);
        EoqSUIAdjustmentsProcessor.main(parameters);
    }
}
