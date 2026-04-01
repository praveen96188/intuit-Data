package com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.OnHoldReason;
import com.intuit.sbd.payroll.psp.domain.SourcePayrollParameter;
import com.intuit.sbd.payroll.psp.domain.SourcePayrollParameterCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;


/**
 * @author kmuthurangam
 * <p>
 * Helper class for Transaction Returns
 */
public class TransactionReturnHelper {

    public void updateSourcePayrollParameterFundingModel(String fundingModel) {
        PayrollServices.beginUnitOfWork();
        SourcePayrollParameter defaultFundingModel = SourcePayrollParameter.findSourcePayrollParameter(SourceSystemCode.QBDT, SourcePayrollParameterCode.DefaultFundingModel);
        defaultFundingModel.setParameterValue(fundingModel);
        PayrollServices.commitUnitOfWork();
    }

    public DomainEntitySet<FinancialTransaction> findReturnedFinancialTransactions(Company company, String sourcePayrollRunId) {
        DomainEntitySet<FinancialTransaction> returnedFinancialTransactions = FinancialTransaction
                .findFinancialTransactions(company, sourcePayrollRunId, null, null, null, null, null, null, TransactionStateCode.Returned);
        return returnedFinancialTransactions;
    }

    public DomainEntitySet<FinancialTransaction> findEmployerVerificationReturnTransfers(Company company) {
        DomainEntitySet<FinancialTransaction> xferFTsCreated = FinancialTransaction.
                findFinancialTransactions(company.getSourceSystemCd(),
                        company.getSourceCompanyId(),
                        TransactionTypeCode.IntuitEmployerVerificationReturnTransfer,
                        TransactionStateCode.Created);
        return xferFTsCreated;
    }

    public DomainEntitySet<OnHoldReason> getNonExpiredOnHoldReasonList(Company company) {
        Expression<OnHoldReason> query =
                new Query<OnHoldReason>()
                        .Where(OnHoldReason.Company().equalTo(company)
                                .And(OnHoldReason.ExpirationDate().isNull()))
                        .OrderBy(OnHoldReason.OnHoldReasonCd());

        return Application.find(OnHoldReason.class, query);
    }

}
