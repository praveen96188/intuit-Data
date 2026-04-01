/*
 * $Id: //psp/dev/Adapters/SAP/test-tools/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/testtools/TestToolsAdapter.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter.testtools;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.offload.CreateTransactionOffloadedEvents;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.ReturnFileParser;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.*;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollEmployeeTransaction;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompany;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPDDCompanyServiceInfo;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPServiceSubStatus;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.testtools.*;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.text.SpcfDateFormat;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

import java.util.*;

/**
 * CompanyAdapter -- Provides all of the SAP methods for creating, reading, updating, and deleting Company
 * objects using SAP DTOs as the interface.  This class is responsible for translating CRUD operations on
 * Company objects in the SAP DTO format into PSP core actions.
 *
 * @author Joe Warmelink
 */
public class TestToolsAdapter {
    private static final SpcfLogger logger = PayrollServices.getLogger(TestToolsAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    private static final TestToolsTranslator translator = new TestToolsTranslator();

    public static final String SERVICE_NAME = "TestToolsAdapter";
    public static final int MAX_RESULTS = 100;

    public TestToolsAdapter() {
    }


    /* For company search */
    public ArrayList<TestToolsCompany> findTestToolsCompanyByLegalNamePattern(String namePattern) {
        ArrayList<TestToolsCompany> retList = null;

        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<Company> spcfRetList = Company.searchCompaniesByLegalName(namePattern);
            if (spcfRetList == null)
                return null;

            retList = new ArrayList<TestToolsCompany>();
            for (Company company : spcfRetList) {
                TestToolsCompany result = getTestToolsCompanySummarySearchResult(company);
                retList.add(result);
            }
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return retList;
    }

    public ArrayList<TestToolsCompany> findTestToolsCompanyByFEIN(String fein) {

        ArrayList<TestToolsCompany> retList = new ArrayList<TestToolsCompany>();
        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<Company> spcfCompanies = Company.findActiveCompanies(fein);
            for(Company company : spcfCompanies){
                TestToolsCompany companyInfo = getTestToolsCompanySummarySearchResult(company);
                retList.add(companyInfo);
            }
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return retList;
    }

    public ArrayList<TestToolsCompany> findTestToolsCompanyBySourceId(String sourceSystemId) {
        ArrayList<TestToolsCompany> retList = new ArrayList<TestToolsCompany>();
        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<Company> spcfCompanies = Company.searchCompaniesBySourceCompanyId(sourceSystemId);
            for(Company company : spcfCompanies){
                TestToolsCompany companyInfo = getTestToolsCompanySummarySearchResult(company);
                retList.add(companyInfo);
            }
        }
        finally {
            PayrollServices.commitUnitOfWork();
        }

        return retList;
    }

    protected TestToolsCompany getTestToolsCompanySummarySearchResult(Company company) {
        TestToolsCompany testToolsCompany = new TestToolsCompany();
        testToolsCompany.setOffloadCd(company.getOffloadGroup().getOffloadGroupCd());
        testToolsCompany.setGseq(company.getId().toString());
        testToolsCompany.setSourceSystemCd(company.getSourceSystemCd().toString());
        testToolsCompany.setCompanyId(company.getSourceCompanyId());
        testToolsCompany.setFein(company.getFedTaxId());
        testToolsCompany.setLegalName(company.getLegalName());
        testToolsCompany.setDirectDepositService(new SAPDDCompanyServiceInfo());

        MigrationStatusCode migrationStatus = company.getMigrationStatus();

        if(migrationStatus != null)
            testToolsCompany.setMigrationStatus(migrationStatus.toString());

        DDCompanyServiceInfo directDepositServiceInfo =
                (DDCompanyServiceInfo) CompanyService.findService(ServiceCode.DirectDeposit, company);

        ServiceSubStatus serviceSubStatus =
                Application.findById(ServiceSubStatus.class, directDepositServiceInfo.getStatusCd());

        SAPDDCompanyServiceInfo sapDirectDepositServiceInfo = testToolsCompany.getDirectDepositService();
        sapDirectDepositServiceInfo.setServiceStatusCd(serviceSubStatus.getServiceStatus().getServiceStatusCd());
        sapDirectDepositServiceInfo.setServiceSubStatusCd(directDepositServiceInfo.getStatusCd());

        Collection<ServiceSubStatusCode> currentOnHoldReasons =
                PayrollServices.onHoldReasonFinder.findCurrentOnHoldReasonCodes(company);

        ArrayList<SAPServiceSubStatus> onHoldReasons = new ArrayList<SAPServiceSubStatus>();
        for (ServiceSubStatusCode serviceSubStatusCode : currentOnHoldReasons) {
            ServiceSubStatus subStatus = Application.findById(ServiceSubStatus.class, serviceSubStatusCode);
            onHoldReasons.add(CompanyTranslator.getSAPServiceSubStatusFromDomainEntity(subStatus));
        }
        testToolsCompany.setOnHoldReasons(onHoldReasons);
        return testToolsCompany;
    }


    public ArrayList<TTOffloadGroup> findOffloadGroups() throws Exception {
       PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<OffloadGroup> offloadGroups = Application.find(OffloadGroup.class);
            PayrollServices.commitUnitOfWork();
            return translator.getTTOffloadGroupsFromDomainEntities(offloadGroups);
        } catch (Exception ex) {
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

     public ArrayList<SAPPayrollEmployeeTransaction> findTransactionsToOffload(
            String offloadGroupCode) throws Exception {

        ArrayList<SAPPayrollEmployeeTransaction> returnTransactionList = new ArrayList<SAPPayrollEmployeeTransaction>();

        try {
            PayrollServices.beginUnitOfWork();
        }
        catch (Throwable ex) {
            aeFactory.throwFindFinancialTransactionsException(ex);
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnTransactionList;
    }



     public void setPSPDate(Date newDate) {

        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SAPTranslator.getSpcfCalendarFromDate(newDate));
            PayrollServices.commitUnitOfWork();
        }
        finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return;
    }

    public void saveOffloadGroup(String groupCode,
                                 String groupName,
                                 String groupDescription,
                                 String cutoffTime) throws Exception {
        try {
            PayrollServices.beginUnitOfWork();
            OffloadGroup grp = OffloadGroup.findOffloadGroup(groupCode);
            if (grp != null) {
                grp.setName(groupName);
                grp.setDescription(groupDescription);
                grp.setCutoffTime(cutoffTime);
                grp = Application.save(grp);
            } else {
                throw new Exception("Offload group '" + groupCode + "' does not exist.");
            }
            PayrollServices.commitUnitOfWork();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;                    
        }
    }

    public void addOffloadGroup(String groupCode,
                                String groupName,
                                String groupDescription,
                                String cutoffTime) throws Exception {
        try {
            PayrollServices.beginUnitOfWork();
            OffloadGroup grp = OffloadGroup.findOffloadGroup(groupCode);
            if (grp == null) {
                grp = new OffloadGroup();
                grp.setOffloadGroupCd(groupCode);
                grp.setName(groupName);
                grp.setDescription(groupDescription);
                grp.setCutoffTime(cutoffTime);
                grp = Application.save(grp);
            } else {
                throw new Exception("Offload group '" + groupCode + "' already exists.");
            }
            PayrollServices.commitUnitOfWork();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        }
    }

    public ArrayList<TTOffloadBatch> findOffloadBatches() throws Exception {
        try {
            TestToolsTranslator translator = new TestToolsTranslator();
            PayrollServices.beginUnitOfWork();
            SpcfCalendar threeDaysAgo = SpcfCalendar.createInstance();
            threeDaysAgo.addDays(-3);
            logger.debug("Three days ago: " + threeDaysAgo.toString());
            Expression<OffloadBatch> query = new Query<OffloadBatch>().Where(OffloadBatch.CreatedDate().greaterOrEqualThan(threeDaysAgo)).OrderBy(OffloadBatch.StatusEffeciveDate().Descending());
            DomainEntitySet<OffloadBatch> offloadBatches = Application.find(OffloadBatch.class, query);
            return translator.getTTOffloadBatchesFromDomainEntities(offloadBatches);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public void generateNACHAFiles(String offloadGroup) throws Exception {
        String offloadDate = null;
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        offloadDate = PSPDate.getPSPTime().format("yyyyMMdd");

        // offload group validations
        if (offloadGroup == null) {
            throw new Exception("NULL Offload Group");
        }
        List<String> fileContents = new ArrayList<String>();

        try {
            PayrollServices.beginUnitOfWork();
            SpcfCalendar runForDate = PSPDate.getPSPTime().toLocal();
            if (offloadDate != null) {
                SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
                dateFormat.setPattern("yyyyMMdd");
                SpcfCalendar parsedRunDate = dateFormat.parse(offloadDate);
                //Set the date on the calendar that has the local time zone
                runForDate.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
            }

            OffloadGroup offloadGroupObj = OffloadGroup.findOffloadGroup(offloadGroup);
            if (offloadGroupObj == null) {
                throw new Exception("Invalid offload group code: " + offloadGroup);
            }
            Application.commitUnitOfWork();

            OffloadACHTransactions offloadACHTxs = new OffloadACHTransactions();
            offloadACHTxs.offloadAndPostOffload(offloadGroup, runForDate);

            //Create fee offload events
            CreateTransactionOffloadedEvents eventCreator = new CreateTransactionOffloadedEvents();
            eventCreator.createTransactionOffloadedEvents();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public void changeCompanyOffloadGroup(String sourceSystemCd, String companyId, String offloadGrpCd) throws Exception {
        try {
            PayrollServices.beginUnitOfWork();

            Company company = Company.findCompany(companyId, SourceSystemCode.valueOf(sourceSystemCd));
            if (company == null) {
                throw new Exception("Company " + companyId + ":" + sourceSystemCd + " not found.");
            }

            OffloadGroup offloadGroup  = OffloadGroup.findOffloadGroup(offloadGrpCd);

            company.setOffloadGroup(offloadGroup);
            company = Application.save(company);

            PayrollServices.commitUnitOfWork();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        }
    }

    public ArrayList<TTEntryDetailRecord> getEntryDetailRecords(Date fromDate,
                                                                Date toDate,
                                                                String offloadGroupCd,
                                                                int firstResult,
                                                                int maxResults) throws Exception {
        ArrayList<TTEntryDetailRecord> detailRecords = null;
        TestToolsTranslator ttTranslator = new TestToolsTranslator();
        try {
            PayrollServices.beginUnitOfWork();

            SpcfCalendar fromDateSpcf = SAPTranslator.getSpcfCalendarFromDate(fromDate);
            SpcfCalendar toDateSpcf = SAPTranslator.getSpcfCalendarFromDate(toDate);
            toDateSpcf.addDays(1);

            Criterion<EntryDetailRecord> where =
                                EntryDetailRecord.MoneyMovementTransaction().OffloadBatch().OffloadGroup().OffloadGroupCd().equalTo(offloadGroupCd)
                                  .And(EntryDetailRecord.MoneyMovementTransaction().CreatedDate().greaterOrEqualThan(fromDateSpcf))
                                  .And(EntryDetailRecord.MoneyMovementTransaction().CreatedDate().lessThan(toDateSpcf));
            Expression query = null;
            if (maxResults > 0) {
                query = new Query<EntryDetailRecord>().Where(where).LimitResults( firstResult, maxResults);
            } else {
                query = new Query<EntryDetailRecord>().Where(where);
            }
            DomainEntitySet<EntryDetailRecord> entryDetailRecords = Application.find(EntryDetailRecord.class, query);

            detailRecords = ttTranslator.getTTEntryDetailRecordsFromDomainEntities(entryDetailRecords);

            PayrollServices.rollbackUnitOfWork();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        }
        return detailRecords;
    }

    public void createBankReturnsForMoneyMovementTransactions(ArrayList<TTBankReturn> bankReturns) throws Exception {
        try {
            PayrollServices.beginUnitOfWork();

            TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
            transactionReturnBatch.setACHReturnFileName(null);
            transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Pending);

            transactionReturnBatch = Application.save(transactionReturnBatch);

            Collection<TransactionReturn> transactionReturnList = buildTransactionReturnListForMMT(transactionReturnBatch, bankReturns);
            PayrollServices.commitUnitOfWork();

            // Process TransactionReturns associated with the TransactionReturnBatch
            PayrollServices.beginUnitOfWork();
            ReturnFileParser returnsProcessor = new ReturnFileParser();
            transactionReturnBatch = Application.findById(TransactionReturnBatch.class, transactionReturnBatch.getId());
            returnsProcessor.processTransactionReturns(transactionReturnBatch);
            PayrollServices.commitUnitOfWork();

        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        }
    }

    private DomainEntitySet<TransactionReturn> buildTransactionReturnListForMMT(TransactionReturnBatch pTransactionReturnBatch,
                                                                     ArrayList<TTBankReturn> bankReturnDTOs) {
        DomainEntitySet<TransactionReturn> transactionRetruns = new DomainEntitySet<TransactionReturn>();
        TransactionReturn transactionReturn = null;
        MoneyMovementTransaction mmTxn = null;
        String nachaCode = null;
        String achMsg = null;
        for (TTBankReturn ttBankReturn : bankReturnDTOs) {
            transactionReturn = new TransactionReturn();
            nachaCode = ttBankReturn.getBankReturnCd();
            transactionReturn.setBankReturnCd(nachaCode);
            achMsg = nachaCode + "00000000000000000000000000000DESCRIPTION";
            if (nachaCode.equals("C01")) {
                achMsg = ttBankReturn.getAccountNumber();
            }
            else if (nachaCode.equals("C02")) {
                achMsg = ttBankReturn.getRoutingNumber();
            }
            else if (nachaCode.equals("C03")) {
                achMsg = ttBankReturn.getRoutingNumber() + "   " + ttBankReturn.getAccountNumber();
            }
            else if (nachaCode.equals("C05")) {
                if ("C".equals(ttBankReturn.getAccountType())) {
                    achMsg = "27"; // 22 or 27 mean change "S" to "C"
                }
                else if ("S".equals(ttBankReturn.getAccountType())) {
                    achMsg = "37"; // 32 or 37 mean change "C" to "S"
                }
                else {
                    achMsg = ttBankReturn.getAccountType();
                }
            }
            else if (nachaCode.equals("C06")) {
                achMsg = ttBankReturn.getAccountNumber() + "   " + ttBankReturn.getAccountType();
            }
            else if (nachaCode.equals("C07")) {
                achMsg = ttBankReturn.getRoutingNumber() + ttBankReturn.getAccountNumber() + ttBankReturn.getAccountType();
            }
            transactionReturn.setBankReturnDescription(achMsg);
            transactionReturn.setReturnBatch(pTransactionReturnBatch);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());

            try {
                mmTxn = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(ttBankReturn.getTransactionId()));
            }
            catch (Exception ex) {
                System.out.println("Error while getting MoneyMovement Transaction" + ex);
            }

            if (mmTxn != null) {
                transactionReturn.setMoneyMovementTransaction(mmTxn);
                transactionReturn.setCompany(mmTxn.getCompany());

                //function call to check whether the transaction return with "R" code is already created or not for the
                //same MMT.
                isTransactionReturnExists(nachaCode, mmTxn.getFinancialTransactionCollection().iterator().next());

                transactionRetruns.add(Application.save(transactionReturn));
            }
        }

        return transactionRetruns;
    }

    /**
     * Function to check whether the transaction return exists for the given MMT with Bank Retrun Code like 'R%'
     * @param nachaCode
     * @param pFinancialTransaction
     */
    private void isTransactionReturnExists(String nachaCode, FinancialTransaction pFinancialTransaction) {
        if (nachaCode.substring(0, 1).equals("R")) {
            DomainEntitySet<TransactionReturn> txnReturnList = TransactionReturn.
                    findTransactionReturnsByReturnCodeAndMMT(pFinancialTransaction.getMoneyMovementTransaction(), nachaCode.substring(0, 1));

            if (txnReturnList.size() > 0) {
                throw new RuntimeException("Financial Transaction " + pFinancialTransaction.getId() + "is Returned Twice with " + nachaCode.substring(0, 1) + " Code");
            }
        }
    }

}


