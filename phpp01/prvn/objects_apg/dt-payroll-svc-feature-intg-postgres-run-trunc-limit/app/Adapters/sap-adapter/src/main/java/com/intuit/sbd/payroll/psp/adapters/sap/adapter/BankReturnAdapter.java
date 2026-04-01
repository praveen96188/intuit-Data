/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/adapter/BankReturnAdapter.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.Operation;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBankReturn;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBankReturnExtendedInfo;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSearchResults;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.aspect.CompanyIdentifierType;
import com.intuit.sbd.payroll.psp.context.aspect.TenantId;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.TransactionCategory;
import com.intuit.sbd.payroll.psp.domain.TransactionReturnStatusCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeGroupCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

/**
 * BankReturnAdapter - SAP Adapter for Bank Returns
 *
 * @author Joe Warmelink
 */
public class BankReturnAdapter {

    private static final SpcfLogger logger = PayrollServices.getLogger(CompanyAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    public BankReturnAdapter() {
    }

    @FlexMethod
    @Operation(operationIds = OperationId.BankReturnView)
    public SAPSearchResults<SAPBankReturn> findCompanyBankReturnsByComplexSearch(
            String pFein,
            Date pFromDate,
            Date pToDate,
            double pAmount,
            String searchType,
            String pOrderBy,
            boolean pOrderDesc,
            int pFirstResult,
            int pMaxResults,
            boolean isForPrinting) throws Throwable {

        PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
        try {

            ArrayList<SAPBankReturn> sapBankReturnList = new ArrayList<SAPBankReturn>();

            SpcfMoney pAchAmount = SAPTranslator.getSpcfMoneyFromDouble(pAmount);

            ArrayList<Object[]> transactions = new ArrayList<Object[]>();
            int totalResulstsCount = 0;

            if (searchType.equals("RiskCollections")) {
                totalResulstsCount = TransactionReturn.findTransactionReturnsBySAPCriteriaCount(
                        TransactionReturnStatusCode.Open,
                        SAPTranslator.getSpcfCalendarFromDate_BeginDay(pFromDate),
                        SAPTranslator.getSpcfCalendarFromDate_EndDay(pToDate),
                        false,
                        "R",
                        pFein,
                        pAchAmount,
                        ServiceSubStatusCode.RiskCollections,
                        null,
                        null,
                        false);
                transactions = TransactionReturn.findTransactionReturnsBySAPCriteria(
                        TransactionReturnStatusCode.Open,
                        SAPTranslator.getSpcfCalendarFromDate_BeginDay(pFromDate),
                        SAPTranslator.getSpcfCalendarFromDate_EndDay(pToDate),
                        false,
                        "R",
                        pFein,
                        pAchAmount,
                        ServiceSubStatusCode.RiskCollections,
                        null,
                        null,
                        false,
                        pOrderBy,
                        pOrderDesc,
                        pFirstResult,
                        pMaxResults);
            }
            else if (searchType.equals("FRG")) {
                totalResulstsCount = TransactionReturn.findTransactionReturnsBySAPCriteriaCount(
                        TransactionReturnStatusCode.Open,
                        SAPTranslator.getSpcfCalendarFromDate_BeginDay(pFromDate),
                        SAPTranslator.getSpcfCalendarFromDate_EndDay(pToDate),
                        true,
                        "R",
                        pFein,
                        pAchAmount,
                        ServiceSubStatusCode.RiskCollections,
                        null,
                        TransactionTypeGroupCode.Debit,
                        true);
                transactions = TransactionReturn.findTransactionReturnsBySAPCriteria(
                        TransactionReturnStatusCode.Open,
                        SAPTranslator.getSpcfCalendarFromDate_BeginDay(pFromDate),
                        SAPTranslator.getSpcfCalendarFromDate_EndDay(pToDate),
                        true,
                        "R",
                        pFein,
                        pAchAmount,
                        ServiceSubStatusCode.RiskCollections,
                        null,
                        TransactionTypeGroupCode.Debit,
                        true,
                        pOrderBy,
                        pOrderDesc,
                        pFirstResult,
                        pMaxResults);
            }


            for (Object[] object : transactions) {
                sapBankReturnList.add(getBankReturn(object));
            }

            if (isForPrinting) {
                coreGetBankReturnExtendedInfo(sapBankReturnList);
            }

            if(pOrderBy!=null){
                if(pOrderBy.equalsIgnoreCase("bankAccountNumber")){
                    sortListByBankAccountNumber(sapBankReturnList,pOrderDesc);
                }else if(pOrderBy.equalsIgnoreCase("fein")){
                    sortListByFein(sapBankReturnList,pOrderDesc);
                }
            }

            SAPSearchResults<SAPBankReturn> sapSearchResults = new SAPSearchResults<SAPBankReturn>();
            sapSearchResults.setTotalRecords(totalResulstsCount);
            sapSearchResults.setReturnsList(sapBankReturnList);

            return sapSearchResults;

        } catch(Throwable t) {
            aeFactory.throwGenericException("Error searching for bank returns.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    @FlexMethod
    @Operation(operationIds = OperationId.BankReturnView)
    public SAPSearchResults<SAPBankReturn> findCompanyBankReturns(
            String pFein,
            Date pFromDate,
            Date pToDate,
            boolean pShowOpen,
            boolean pShowResolved,
            String pTransactionType,
            String pTransactionCategory,
            boolean pExclude5DayFunding,
            String includeCode,
            double pAmount,
            String pOrderBy,
            boolean pOrderDesc,
            int pFirstResult,
            int pMaxResults,
            boolean isForPrinting) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            TransactionTypeGroupCode transactionType = pTransactionType != null ? TransactionTypeGroupCode.valueOf(pTransactionType) : null;
            TransactionCategory transactionCategory = pTransactionCategory != null ? TransactionCategory.valueOf(pTransactionCategory) : null;

            ArrayList<SAPBankReturn> sapBankReturnList = new ArrayList<SAPBankReturn>();

            TransactionReturnStatusCode txnReturnCode = null;

            if(pShowOpen && !pShowResolved) {
                txnReturnCode = TransactionReturnStatusCode.Open;
            }

            if(pShowResolved && !pShowOpen) {
                txnReturnCode = TransactionReturnStatusCode.Resolved;
            }

            SpcfMoney pAchAmount = SAPTranslator.getSpcfMoneyFromDouble(pAmount);

            int totalResultsCount = TransactionReturn.findTransactionReturnsBySAPCriteriaCount(
                    txnReturnCode,
                    SAPTranslator.getSpcfCalendarFromDate_BeginDay(pFromDate),
                    SAPTranslator.getSpcfCalendarFromDate_EndDay(pToDate),
                    pExclude5DayFunding,
                    includeCode,
                    pFein,
                    pAchAmount,
                    null,
                    transactionCategory,
                    transactionType,
                    false);
            ArrayList<Object[]> transactions = TransactionReturn.findTransactionReturnsBySAPCriteria(
                    txnReturnCode,
                    SAPTranslator.getSpcfCalendarFromDate_BeginDay(pFromDate),
                    SAPTranslator.getSpcfCalendarFromDate_EndDay(pToDate),
                    pExclude5DayFunding,
                    includeCode,
                    pFein,
                    pAchAmount,
                    null,
                    transactionCategory,
                    transactionType,
                    false,
                    pOrderBy,
                    pOrderDesc,
                    pFirstResult,
                    pMaxResults);

            for(Object[] object : transactions) {
                sapBankReturnList.add(getBankReturn(object));
            }

            if(isForPrinting){
                coreGetBankReturnExtendedInfo(sapBankReturnList);
            }

            if(pOrderBy != null){
                if(pOrderBy.equalsIgnoreCase("bankAccountNumber")){
                    sortListByBankAccountNumber(sapBankReturnList, pOrderDesc);
                }else if(pOrderBy.equalsIgnoreCase("fein")){
                    sortListByFein(sapBankReturnList, pOrderDesc);
                }
            }

            SAPSearchResults<SAPBankReturn> sapSearchResults = new SAPSearchResults<SAPBankReturn>();
            sapSearchResults.setTotalRecords(totalResultsCount);
            sapSearchResults.setReturnsList(sapBankReturnList);

            return sapSearchResults;

        } catch(Throwable t) {
            aeFactory.throwGenericException("Error searching for bank returns.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }


    private void sortListByFein(ArrayList<SAPBankReturn> sapBankReturnList, boolean pOrderDesc){
        Collections.sort(sapBankReturnList, (SAPBankReturn a, SAPBankReturn b) -> b.getFein().compareTo(a.getFein()));
        if(!pOrderDesc) {
            Collections.reverse(sapBankReturnList);
        }
    }

    private void sortListByBankAccountNumber(ArrayList<SAPBankReturn> sapBankReturnList, boolean pOrderDesc){
        Collections.sort(sapBankReturnList, (SAPBankReturn a, SAPBankReturn b) -> b.getBankAccountNumber().compareTo(a.getBankAccountNumber()));
        if(!pOrderDesc) {
            Collections.reverse(sapBankReturnList);
        }
    }

    @FlexMethod
    @Operation(operationIds = {OperationId.BankReturnUpdate})
    public void saveBankReturnNote( String pSourceSystemCode,
                                    @TenantId(IdType = CompanyIdentifierType.PSID) String pSourceCompanyId,
                                    String transactionId,
                                    String pBankReturnStatus,
                                    String pNote) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork();

            SourceSystemCode sourceSystemCode = SourceSystemCode.valueOf(pSourceSystemCode);
            TransactionReturnStatusCode bankReturnStatus = TransactionReturnStatusCode.valueOf(pBankReturnStatus);

            ProcessResult  processResult = PayrollServices.financialTransactionManager.updateBankReturnStatus(
                    sourceSystemCode,
                    pSourceCompanyId,
                    transactionId,
                    bankReturnStatus,
                    pNote);

            if (processResult.isSuccess()) {
                PayrollServices.commitUnitOfWork();
            } else {
                aeFactory.throwGenericException("Error saving bank return note.", processResult);
            }
        } catch(Throwable t) {
            aeFactory.throwGenericException("Error saving bank return note.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @FlexMethod
    @Operation(operationIds = OperationId.BankReturnView)
    public ArrayList<SAPBankReturn> getBankReturnExtendedInfo( ArrayList<SAPBankReturn> bankReturns) throws Throwable {
        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            return coreGetBankReturnExtendedInfo(bankReturns);
        } catch(Throwable t) {
            aeFactory.throwGenericException("Error finding bank return extended information.", t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return null;
    }

    private ArrayList<SAPBankReturn> coreGetBankReturnExtendedInfo(ArrayList<SAPBankReturn> bankReturns) throws Exception {
        HashMap<String, SAPBankReturnExtendedInfo> hashMap = new HashMap<String, SAPBankReturnExtendedInfo>();
        for(SAPBankReturn bankReturn : bankReturns){

            SAPBankReturnExtendedInfo bankReturnExtendedInfo;

            if(hashMap.containsKey(bankReturn.getSourcePayRunId())){
                bankReturnExtendedInfo = hashMap.get(bankReturn.getSourcePayRunId());
            }
            else{
                bankReturnExtendedInfo = new SAPBankReturnExtendedInfo();

                Company company =
                        Company.findCompany(bankReturn.getCompanyId(), SourceSystemCode.valueOf(bankReturn.getCompanySourceSystemCd()));

                PayrollRun payrollRun = PayrollRun.findPayrollRun(company, bankReturn.getSourcePayRunId());

                // get balance due
                if(payrollRun != null){
                    bankReturnExtendedInfo.setPayrollBalanceDue(
                            SAPTranslator.getDoubleFromSpcfMoney(new SpcfMoney(payrollRun.getUncollectedAmountForPayroll())));


                    // get expected resolution date
                    bankReturnExtendedInfo.setExpectedResolutionDate(
                            SAPTranslator.getDateFromSpcfCalendar(payrollRun.getExpectedResolutionDate()));
                }
                else {
                    bankReturnExtendedInfo.setPayrollBalanceDue(0.00);
                    bankReturnExtendedInfo.setExpectedResolutionDate(null);
                }

                hashMap.put(bankReturn.getSourcePayRunId(), bankReturnExtendedInfo);
            }

            bankReturn.setBankReturnExtendedInfo(bankReturnExtendedInfo);
        }
        return bankReturns;

    }



    private SAPBankReturn getBankReturn(Object[] object) throws Exception{
        return BankReturnTranslator.getSAPBankReturnFromDomainEntity((SpcfMoney) object[12],
                (PayrollStatus) object[1],
                (String) object[13],
                (SpcfCalendar) object[10],
                (TransactionTypeCode) object[2],
                (SpcfCalendar) object[11],
                (String) object[8],
                (String) object[9],
                (String) object[3],
                (String) object[4],
                (String) object[6],
                (String) object[7],
                (String) object[14],
                (TransactionReturnStatusCode) object[15],
                (SourceSystemCode) object[16],
                (String) object[17],
                (SpcfUniqueIdImpl) object[18],
                (BankAccountOwnerType)object[19],
                (String)object[20]);
    }

}
