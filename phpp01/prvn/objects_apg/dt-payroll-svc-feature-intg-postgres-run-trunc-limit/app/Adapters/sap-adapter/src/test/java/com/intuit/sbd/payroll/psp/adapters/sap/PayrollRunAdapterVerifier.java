package com.intuit.sbd.payroll.psp.adapters.sap;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.SAPTranslator;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPBillingTransaction;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollBillingTransactions;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.PayrollStatus;
import com.intuit.sbd.payroll.psp.domain.SettlementType;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Arrays;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author vdammur1
 */
public class PayrollRunAdapterVerifier {
    private PayrollRunAdapter mPayrollRunAdapter;

    public void sapRecordWireVerifier(String sourceCompanyId,  String sourcePayrollRunId,  double financialReturnAmount) throws Throwable {

        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollRunId);

        assertEquals("Payroll Run Status", PayrollStatus.DebitReturned, payrollRun.getPayrollRunStatus());

        SAPPayrollBillingTransactions sapPayrollBillingTransactions = assertOne(new PayrollRunAdapter().findPayrollUncollectedBalances(company.getSourceCompanyId(), company.getSourceSystemCd().toString(), payrollRun.getSourcePayRunId()));
        SAPBillingTransaction ddTransaction = assertOne(sapPayrollBillingTransactions.getDdTransactions());
        sapPayrollBillingTransactions.getDdTransactions().get(0).setFinancialReturnAmount(financialReturnAmount);

        new PayrollRunAdapter().redebitPayrollTransactions(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(),
                SettlementType.Wire.toString(),
                SAPTranslator.getDateFromSpcfCalendar(PSPDate.getPSPTime()),
                new ArrayList<SAPPayrollBillingTransactions>(Arrays.asList(sapPayrollBillingTransactions)));

        Application.beginUnitOfWork();
        Application.refresh(payrollRun);
        assertOne(payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(com.intuit.sbd.payroll.psp.domain.TransactionTypeCode.EmployerDdRedebit)
                .And(FinancialTransaction.FinancialTransactionAmount().equalTo(new SpcfMoney(Double.toString(financialReturnAmount))))));

        CompanyEvent companyEvent = assertOne(CompanyEvent.findCompanyEvents(company, EventTypeCode.NonAchPaymentReceived));
        assertOne(companyEvent.getCompanyEventEmailCollection());
        Application.rollbackUnitOfWork();
    }

    public void sapCancelVerifier(SourceSystemCode sourceSystemCode,
                                  String sourceCompanyId, String pSourcePayrollRunId, PayrollRunAdapter mmPayrollRunAdapter) throws Throwable {

        PayrollServices.beginUnitOfWork();

        Company company = Company.findCompany(sourceCompanyId, sourceSystemCode);
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, pSourcePayrollRunId);
        DomainEntitySet<FinancialTransaction> financialTransactions
                =  FinancialTransaction.findAllFinancialTransaction(company, TransactionTypeCode.EmployeeDdCredit);

        ArrayList<String> transIds = new ArrayList<String>();
        for (FinancialTransaction financialTransaction : financialTransactions) {
            transIds.add(financialTransaction.getPaycheckSplit().getSourceDdTxnId());
        }
        // Make sure no ErFeeTxn is created for Symphony
        assertTrue(payrollRun.getFinancialTransactionCollection()
                .find(FinancialTransaction.TransactionType().TransactionTypeCd()
                        .equalTo(TransactionTypeCode.EmployerFeeDebit)).isEmpty());

        PayrollServices.rollbackUnitOfWork();

        mmPayrollRunAdapter.cancelPayrollTransaction(sourceCompanyId, sourceSystemCode.name(), transIds, pSourcePayrollRunId);

        // Make sure payroll run gets cancelled
        PayrollServices.beginUnitOfWork();
        PayrollRun cancelledPayrollRun = PayrollRun.findPayrollRun(company, pSourcePayrollRunId);
        assertEquals("Payroll Run Status", PayrollStatus.Canceled, cancelledPayrollRun.getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();
    }

}
