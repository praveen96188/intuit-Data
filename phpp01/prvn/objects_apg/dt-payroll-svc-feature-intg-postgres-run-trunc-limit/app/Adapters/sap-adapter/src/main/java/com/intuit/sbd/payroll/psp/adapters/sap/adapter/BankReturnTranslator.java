/*
 * : $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBankReturn;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Date;

/**
 * BankReturnTranslator - SAP Translator class for Bank Returns
 *
 * @author Joe Warmelink
 */
public class BankReturnTranslator {

    /*
     * The fields, their positions and their types in the returned rows are as follows:
     * [0] TransactionReturn ID (SpcfUniqueId)
     * [1] PayrollRun status (PayrollStatus)
     * [2] FT transaction type code (TransactionTypeCode)
     * [3] company legal name (String) only when FT is a DD credit
     * [4] employee first name (String) only when FT is a DD credit
     * [5] employee middle name (String) only when FT is a DD credit
     * [6] employee last name (String) only when FT is a DD credit
     * [7] company EIN
     * [8] non-Intuit BA account number (String)
     * [9] non-Intuit BA routing number (String)
     * [10] TransactionReturn return date (SpcfCalendar)
     * [11] PayrollRun paycheck date (SpcfCalendar)
     * [12] Financial transaction ACH amount returned (SpcfMoney)
     * [13] bank return code (String)
     * [14] PayrollRun source payroll run id (String)
     * [15] TransactionReturn.ReturnStatusCd
     * [16] Company.SourceSystemCd
     * [17] Company.SourceCompanyId
     * [18] FinancialTransaction.id (SpcfUniqueId)
     * [19] Debit Bank Account Type
     */

    public static SAPBankReturn getSAPBankReturnFromDomainEntity(SpcfMoney achAmount,
                                                                 PayrollStatus payrollStatus,
                                                                 String bankReturnCd,
                                                                 SpcfCalendar transactionReturnDate,
                                                                 TransactionTypeCode transactionTypeCode,
                                                                 SpcfCalendar paycheckDate,
                                                                 String bankAccountNumber,
                                                                 String bankAccountRoutingNumber,
                                                                 String companyLegalName,
                                                                 String employeeFirstName,
                                                                 String employeeLastName,
                                                                 String companyEIN,
                                                                 String sourcePayrollId,
                                                                 TransactionReturnStatusCode txnReturnCd,
                                                                 SourceSystemCode sourceSystemCd,
                                                                 String sourceCompanyId,
                                                                 SpcfUniqueIdImpl finTxnId,
                                                                 BankAccountOwnerType debitBankAccountType,
                                                                 String payeeName) {
        SAPBankReturn sapBankReturn = new SAPBankReturn();

        // if the debit account is intuit then the return is a credit
        // meaning we owe the employer money thus it will show up in the UI as a negative
        if(debitBankAccountType == BankAccountOwnerType.Intuit){
            achAmount = (SpcfMoney)achAmount.negate();
        }                                                                                         

        sapBankReturn.setAmount(SAPTranslator.getDoubleFromSpcfMoney(achAmount));
        sapBankReturn.setReturnCd(bankReturnCd);
        sapBankReturn.setReturnDate(SAPTranslator.getDateFromSpcfCalendar(transactionReturnDate));
        sapBankReturn.setStatusCd(txnReturnCd.toString());
        sapBankReturn.setTxnType(transactionTypeCode.toString());
        sapBankReturn.setCheckDate(SAPTranslator.getDateFromSpcfCalendar(paycheckDate));
        sapBankReturn.setSourcePayRunId(sourcePayrollId);
        sapBankReturn.setTxnId(finTxnId.toString());
        sapBankReturn.setBankAccountNumber(bankAccountNumber);
        sapBankReturn.setBankRoutingNumber(bankAccountRoutingNumber);
        sapBankReturn.setCompanyId(sourceCompanyId);
        sapBankReturn.setCompanyName(companyLegalName);
        sapBankReturn.setCompanySourceSystemCd(sourceSystemCd.toString());
        sapBankReturn.setFein(companyEIN);

        if (employeeLastName != null && !(employeeLastName.equals(""))) {
            sapBankReturn.setEmployeeName(employeeFirstName + " " + employeeLastName);
        } else if(payeeName != null) {
            sapBankReturn.setEmployeeName(payeeName);
        } else {
            sapBankReturn.setEmployeeName("");
        }

        if(payrollStatus != null){
            sapBankReturn.setPayrollStatus(payrollStatus.toString());
        }
        else{
            sapBankReturn.setPayrollStatus("N/A");
        }

        return sapBankReturn;
    }
}
