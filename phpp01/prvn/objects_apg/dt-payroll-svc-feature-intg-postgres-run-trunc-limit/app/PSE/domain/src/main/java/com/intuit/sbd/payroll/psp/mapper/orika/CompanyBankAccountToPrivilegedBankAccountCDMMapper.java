package com.intuit.sbd.payroll.psp.mapper.orika;

import com.intuit.payroll.api.shared.model.*;
import com.intuit.sbd.payroll.psp.domain.BankAccountStatus;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class CompanyBankAccountToPrivilegedBankAccountCDMMapper
		extends BeanMapper<CompanyBankAccount, PrivilegedBankAccountCDM> {

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("id", "id");
		addBidirectionalFieldMapping("bankAccount.accountTypeCd", "accountType");
		addBidirectionalFieldMapping("bankAccount.accountNumber", "accountNumber");
		addBidirectionalFieldMapping("bankAccount.routingNumber", "routingNumber");
		addBidirectionalFieldMapping("company.sourceCompanyId", "companyId");
		addBidirectionalFieldMapping("version", "entityVersion");
		addBidirectionalFieldMapping("modifiedDate", "updated");
		addBidirectionalFieldMapping("effectiveDate", "created");
	}

	@Override
	public void mapAtoB(CompanyBankAccount companyBankAccount, PrivilegedBankAccountCDM bankAccountCDM,
			MappingContext context) {
		BankAccountVerification accountVerification = new BankAccountVerification();
		if (companyBankAccount.getStatusCd() == BankAccountStatus.Active) {
			bankAccountCDM.setVerified(true);
			accountVerification.setStatus(BankAccountVerificationStatus.VERIFIED);
		} else {
			accountVerification.setStatus(BankAccountVerificationStatus.NEEDS_VERIFICATION);
		}
		accountVerification.setType(BankAccountVerificationType.TEST_TRANSACTION);

		bankAccountCDM.setVerifications(new BankAccountVerification[] { accountVerification });
//		BankVerification bankVerification = new BankVerification();
//		bankVerification.setType(BankVerificationType.VERIFIED_TEST_TRANSACTION);
//		bankAccountCDM.setBankVerifications(new BankVerification[] { bankVerification });

		bankAccountCDM.setOwnerType("employer.principal");

		bankAccountCDM.setName(companyBankAccount.getSourceBankAccountName());
		bankAccountCDM.setDefault(companyBankAccount.getStatusCd().equals(BankAccountStatus.Active));
		bankAccountCDM.setPrincipal(
				getEntityCDMMapper().mapToTarget(companyBankAccount.getCompany(), BankPrincipalCDM.class));

	}

}
