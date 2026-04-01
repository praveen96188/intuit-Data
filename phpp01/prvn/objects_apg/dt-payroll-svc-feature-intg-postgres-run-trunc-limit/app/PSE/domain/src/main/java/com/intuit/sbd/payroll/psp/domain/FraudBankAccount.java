package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import java.util.List;

/**
 * Hand-written business logic
 */
public class FraudBankAccount extends BaseFraudBankAccount {
    public static String AccountNumberKeyName="FBank_Acount_AccNo";

	/**
	 * Default constructor.
	 */
	public FraudBankAccount()
	{
		super();
	}

    public FraudBankAccount(Company pCompany, CompanyBankAccount pCompanyBankAccount) {
        super();
        if (pCompany != null && pCompanyBankAccount != null) {
            this.setCompany(pCompany);
            this.setAccountNumber(pCompanyBankAccount.getBankAccount().getAccountNumber());
            this.setRoutingNumber(pCompanyBankAccount.getBankAccount().getRoutingNumber());
            this.setBankName(pCompanyBankAccount.getBankAccount().getBankName());
            this.setAccountTypeCd(pCompanyBankAccount.getBankAccount().getAccountTypeCd());
            this.setBankAccountOwnerName(pCompany.getLegalName());
            this.setFraudBankAccountReason(FraudBankAccountReasonType.EmployerBankAccountOfTerminatedCompany);
        }
    }

    public FraudBankAccount(Company pCompany, EmployeeBankAccount pEmployeeBankAccount) {
        super();
        if (pCompany != null && pEmployeeBankAccount != null) {
            this.setCompany(pCompany);
            this.setAccountNumber(pEmployeeBankAccount.getBankAccount().getAccountNumber());
            this.setRoutingNumber(pEmployeeBankAccount.getBankAccount().getRoutingNumber());
            this.setBankName(pEmployeeBankAccount.getBankAccount().getBankName());
            this.setAccountTypeCd(pEmployeeBankAccount.getBankAccount().getAccountTypeCd());
            this.setBankAccountOwnerName(pEmployeeBankAccount.getEmployee().getFullName());
            this.setFraudBankAccountReason(FraudBankAccountReasonType.EmployeeBankAccountOfTerminatedCompany);
        }
    }

    public static FraudBankAccount findFraudBankAccount(Company pCompany, EmployeeBankAccount pEmployeeBankAccount) {
        Criterion<FraudBankAccount> where = FraudBankAccount.Company().equalTo(pCompany);
        if(pEmployeeBankAccount.getBankAccount().getAccountNumber() == null){
            where = where.And(FraudBankAccount.AccountNumberEnc().isNull());
        }else{
            List<String> fraudBankAccountEncList = EncryptionUtils.deterministicEncryptWithAllKeys(FraudBankAccount.AccountNumberKeyName,pEmployeeBankAccount.getBankAccount().getAccountNumber());
            where = where.And(FraudBankAccount.AccountNumberEnc().in(fraudBankAccountEncList));
        }
            where = where.And(FraudBankAccount.RoutingNumber().equalTo(pEmployeeBankAccount.getBankAccount().getRoutingNumber()))
                    .And(FraudBankAccount.BankName().equalTo(pEmployeeBankAccount.getBankAccount().getBankName()))
                    .And(FraudBankAccount.AccountTypeCd().equalTo(pEmployeeBankAccount.getBankAccount().getAccountTypeCd()))
                    .And(FraudBankAccount.BankAccountOwnerName().equalTo(pEmployeeBankAccount.getEmployee().getFullName()))
                    .And(FraudBankAccount.FraudBankAccountReason().equalTo(FraudBankAccountReasonType.EmployeeBankAccountOfTerminatedCompany));
        DomainEntitySet<FraudBankAccount> fraudBankAccounts = Application.find(FraudBankAccount.class, where);

        return fraudBankAccounts.isEmpty() ? null : fraudBankAccounts.get(0);
    }

    public static FraudBankAccount findFraudBankAccount(Company pCompany, CompanyBankAccount pCompanyBankAccount) {
        Criterion<FraudBankAccount> where = FraudBankAccount.Company().equalTo(pCompany);
        if(pCompanyBankAccount.getBankAccount().getAccountNumber() == null){
            where = where.And(FraudBankAccount.AccountNumberEnc().isNull());
        } else {
            List<String> fraudBankAccountEncList = EncryptionUtils.deterministicEncryptWithAllKeys(FraudBankAccount.AccountNumberKeyName,pCompanyBankAccount.getBankAccount().getAccountNumber());
            where = where.And(FraudBankAccount.AccountNumberEnc().in(fraudBankAccountEncList));
        }
            where = where.And(FraudBankAccount.RoutingNumber().equalTo(pCompanyBankAccount.getBankAccount().getRoutingNumber()))
                .And(FraudBankAccount.BankName().equalTo(pCompanyBankAccount.getBankAccount().getBankName()))
                .And(FraudBankAccount.AccountTypeCd().equalTo(pCompanyBankAccount.getBankAccount().getAccountTypeCd()))
                .And(FraudBankAccount.BankAccountOwnerName().equalTo(pCompany.getLegalName()))
                .And(FraudBankAccount.FraudBankAccountReason().equalTo(FraudBankAccountReasonType.EmployerBankAccountOfTerminatedCompany));

        DomainEntitySet<FraudBankAccount> fraudBankAccounts = Application.find(FraudBankAccount.class, where);

        return fraudBankAccounts.isEmpty() ? null : fraudBankAccounts.get(0);
    }

    public void setAccountNumber(String pAccountNumber) {
        super.setAccountNumberEnc(EncryptionUtils.deterministicEncrypt(AccountNumberKeyName,pAccountNumber));
    }


    public String getAccountNumber() {
        return EncryptionUtils.deterministicDecrypt(AccountNumberKeyName,getAccountNumberEnc());
    }
}