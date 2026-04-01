package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBookTransferTransaction;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.IntuitBankAccount;

/**
 * User: ihannur
 * Date: 2/21/12
 * Time: 10:34 AM
 */
public class AccountingTranslator {
    public static SAPBookTransferTransaction getSAPBookTransferTransaction(FinancialTransaction pFinancialTransaction) {
        SAPBookTransferTransaction sapBookTransferTransaction = new SAPBookTransferTransaction();
        sapBookTransferTransaction.setAmount(SAPTranslator.getDoubleFromSpcfMoney(pFinancialTransaction.getFinancialTransactionAmount()));
        sapBookTransferTransaction.setFromAccount(IntuitBankAccount.findIntuitBankAccount(pFinancialTransaction.getDebitBankAccount()).getDescription());
        sapBookTransferTransaction.setToAccount(IntuitBankAccount.findIntuitBankAccount(pFinancialTransaction.getCreditBankAccount()).getDescription());
        sapBookTransferTransaction.setSettlementDate(SAPTranslator.getDateFromSpcfCalendar(pFinancialTransaction.getSettlementDate()));
        sapBookTransferTransaction.setTransactionId(pFinancialTransaction.getId().toString());
        sapBookTransferTransaction.setStatus(pFinancialTransaction.getCurrentTransactionState().getName());
        sapBookTransferTransaction.setCreatedBy(SAPTranslator.getUserNameFromUserID(pFinancialTransaction.getCreatorId()));
        sapBookTransferTransaction.setTransactionType(pFinancialTransaction.getTransactionType().toString());
        sapBookTransferTransaction.setCreatedDate(SAPTranslator.getDateFromSpcfCalendar(pFinancialTransaction.getCreatedDate()));
        sapBookTransferTransaction.setActionCollection(PayrollRunTranslator.getSAPActionEventsFromDomainEntities(pFinancialTransaction.getActionCollection()));

        return sapBookTransferTransaction;
    }
}
