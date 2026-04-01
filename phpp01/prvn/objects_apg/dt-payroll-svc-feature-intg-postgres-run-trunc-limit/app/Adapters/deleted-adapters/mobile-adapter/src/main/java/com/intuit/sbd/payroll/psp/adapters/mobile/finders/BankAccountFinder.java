package com.intuit.sbd.payroll.psp.adapters.mobile.finders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.BankAccountStatus;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * @author Jeff Jones
 */
public class BankAccountFinder {

    public static CompanyBankAccount findCompanyBankAccount(Company pCompany) {
        DomainEntitySet<CompanyBankAccount> companyBankAccounts =
                Application.find(CompanyBankAccount.class,
                                                CompanyBankAccount.Company().equalTo(pCompany)
                                                .And(CompanyBankAccount.StatusCd().equalTo(BankAccountStatus.Active)));
        if (companyBankAccounts.isEmpty()) {
            companyBankAccounts =
                    Application.find(CompanyBankAccount.class, new Query<CompanyBankAccount>()
                            .Where(CompanyBankAccount.Company().equalTo(pCompany)
                            .And(CompanyBankAccount.StatusCd().equalTo(BankAccountStatus.PendingVerification)))
                            .OrderBy(CompanyBankAccount.StatusEffectiveDate().Descending())
                            .LimitResults(0,1));

            if (companyBankAccounts.isEmpty()) {
                companyBankAccounts =
                    Application.find(CompanyBankAccount.class, new Query<CompanyBankAccount>()
                            .Where(CompanyBankAccount.Company().equalTo(pCompany)
                            .And(CompanyBankAccount.StatusCd().equalTo(BankAccountStatus.Inactive)))
                            .OrderBy(CompanyBankAccount.StatusEffectiveDate().Descending())
                            .LimitResults(0,1));
            }
        } else {
            if (companyBankAccounts.size() > 1) {
                throw new RuntimeException("Company " + pCompany.getSourceSystemCd()
                        + ":" + pCompany.getSourceCompanyId() + " has more than one active account");
            }
        }

        if (companyBankAccounts.size() != 1) {
            return null;
        }

        return companyBankAccounts.get(0);
    }

}
