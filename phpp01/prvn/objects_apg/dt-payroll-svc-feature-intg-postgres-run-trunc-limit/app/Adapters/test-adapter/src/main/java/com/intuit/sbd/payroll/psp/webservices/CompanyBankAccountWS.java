package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: mamin
 * Date: Jun 8, 2009
 * Time: 9:19:49 AM
 * To change this template use File | Settings | File Templates.
 */
@WebService()
public class CompanyBankAccountWS {
    @WebMethod
    public void makeBankAccountActive(@WebParam (name = "sourceSystem") String sourceSystem,
                                      @WebParam (name = "sourceCompanyId") String sourceCompanyId,
                                      @WebParam (name = "sourceBankAccountId") String sourceBankAccountId) {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        PayrollServices.beginUnitOfWork();
        try {
            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystem));
            // Load CompanyBankAccount
            CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company, sourceBankAccountId);
            ArrayList<SpcfMoney> amountsToVerify = new ArrayList<SpcfMoney>();
            DomainEntitySet<FinancialTransaction> verificationTransactions = companyBankAccount.getVerificationTransactions();
            for (FinancialTransaction financialTransaction : verificationTransactions) {
                // Add the FinancialTransactionState object for the current State
                financialTransaction.updateFinancialTransactionState(TransactionStateCode.Executed);
                amountsToVerify.add(financialTransaction.getFinancialTransactionAmount());
                Application.save(financialTransaction);
            }
            PayrollServices.commitUnitOfWork();
            //
            PayrollServices.beginUnitOfWork();
            PSPDate.addDaysToPSPTime(10);
            ProcessResult<CompanyBankAccount> processResult = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(), amountsToVerify.get(0), amountsToVerify.get(1), false);
            PSPDate.resetPSPTime();
            PayrollServices.commitUnitOfWork();
        } catch (Throwable e) {
            PayrollServices.rollbackUnitOfWork();
            throw new RuntimeException(e);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void makeBankAccountActiveLikeAgent(@WebParam (name = "sourceSystem") String sourceSystem,
                                      @WebParam (name = "sourceCompanyId") String sourceCompanyId) {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        PayrollServices.beginUnitOfWork();
        try {
            Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.valueOf(sourceSystem));
            CompanyBankAccount companyBankAccount = CompanyBankAccount.findCompanyBankAccount(company);
            ProcessResult<CompanyBankAccount> result = PayrollServices.companyManager.verifyCompanyBankAccount(company.getSourceSystemCd(), company.getSourceCompanyId(), companyBankAccount.getSourceBankAccountId(),
                    null, null, true);
            if(!result.isSuccess()){
                throw new RuntimeException(result.getErrorMessages().toString());
            }
            PayrollServices.commitUnitOfWork();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
