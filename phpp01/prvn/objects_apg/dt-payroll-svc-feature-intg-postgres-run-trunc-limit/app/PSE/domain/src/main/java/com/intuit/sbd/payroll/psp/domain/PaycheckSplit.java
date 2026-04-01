package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class PaycheckSplit extends BasePaycheckSplit {

    public static PaycheckSplit findNonCanceledPaycheckSplit(Company pCompany, String pSourcePaycheckSplitId) {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //
        // Finders/Counters
        //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        String[] paramNames = new String[3];
        paramNames[0] = "company";
        paramNames[1] = "sourcePaycheckSplitId";
        paramNames[2] = "txnState";

        Object[] paramValues = new Object[3];
        paramValues[0] = pCompany;
        paramValues[1] = pSourcePaycheckSplitId;
        paramValues[2] = TransactionStateCode.Cancelled;


        DomainEntitySet<PaycheckSplit> retList = Application.findByNamedQueryUsingCache(PaycheckSplit.class, "findPaycheckSplitByCompanyExcludeTxnState", paramNames, paramValues);
        if (retList.size() > 0) {
            return retList.get(0);
        }
        return null;
    }

    public static PaycheckSplit findFraudCheckPaycheckSplits(Company pCompany, String pSourcePaycheckSplitId) {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //
        // Finders/Counters
        //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        String[] paramNames = new String[3];
        paramNames[0] = "company";
        paramNames[1] = "sourcePaycheckSplitId";
        paramNames[2] = "paycheckState";

        Object[] paramValues = new Object[3];
        paramValues[0] = pCompany;
        paramValues[1] = pSourcePaycheckSplitId;
        paramValues[2] = PaycheckStatusCode.Active;

        DomainEntitySet<PaycheckSplit> retList = Application.findByNamedQueryUsingCache(PaycheckSplit.class, "findFraudCheckPaycheckSplitsById", paramNames, paramValues);
        if (retList.size() > 0) {
            return retList.get(0);
        }
        return null;        
    }

    public static DomainEntitySet<PaycheckSplit> findFraudCheckPaycheckSplits(Company pCompany, SpcfCalendar pCheckDate) {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //
        // Finders/Counters
        //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        String[] paramNames = new String[3];
        paramNames[0] = "company";
        paramNames[1] = "depositDate";
        paramNames[2] = "paycheckState";

        Object[] paramValues = new Object[3];
        paramValues[0] = pCompany;
        paramValues[1] = pCheckDate;
        paramValues[2] = PaycheckStatusCode.Active;

        return Application.findByNamedQueryUsingCache(PaycheckSplit.class, "findFraudCheckPaycheckSplits", paramNames, paramValues);
    }

    public static PaycheckSplit findPaycheckSplit(PayrollRun pPayrollRun, String pSourcePaycheckSplitId) {
        return pPayrollRun.getPaycheckSplit(pSourcePaycheckSplitId);
    }


    public static DomainEntitySet<PaycheckSplit> findPaycheckSplitsByEmployeeBankAccountForIOP(Company pCompany, EmployeeBankAccount pEmployeeBankAccount, PayrollRun pPayrollRun) {
        return Application.findByNamedQueryUsingCache(
                PaycheckSplit.class,
                "findPaycheckSplitByEmployeeBankAccountAndCheckStatus",
                new String[]{"company", "payrollRunDate", "employeeBankAccount", "paycheckState"},
                new Object[]{pCompany, pPayrollRun.getPayrollRunDate(), pEmployeeBankAccount, PaycheckStatusCode.Active});
    }

    public static DomainEntitySet<PaycheckSplit> findPaycheckSplitsByEmployeeBankAccount(Company pCompany, EmployeeBankAccount pEmployeeBankAccount, PayrollRun pPayrollRun) {
        return Application.findByNamedQueryUsingCache(
                PaycheckSplit.class,
                "findPaycheckSplitByEmployeeBankAccountExcludeTxnState",
                new String[]{"company", "payrollRunDate", "sourceBankAccountId", "txnState", "employeeBankAccount"},
                new Object[]{pCompany, pPayrollRun.getPayrollRunDate(), pEmployeeBankAccount.getSourceBankAccountId(), TransactionStateCode.Cancelled, pEmployeeBankAccount.getBankAccount()});
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public PaycheckSplit() {
        super();
    }

    public DomainEntitySet<FinancialTransaction> getFinancialTransactions() {
        DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class, FinancialTransaction.PaycheckSplit().equalTo(this));
        return financialTransactions;
    }
}