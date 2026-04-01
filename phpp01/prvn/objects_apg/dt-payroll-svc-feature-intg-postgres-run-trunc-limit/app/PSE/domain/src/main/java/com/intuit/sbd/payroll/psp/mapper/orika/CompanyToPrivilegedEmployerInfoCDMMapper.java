package com.intuit.sbd.payroll.psp.mapper.orika;

import com.intuit.payroll.api.payslip.model.PrivilegedEmployerInfoCDM;
import com.intuit.payroll.api.shared.model.AddressCDM;
import com.intuit.payroll.api.shared.model.PrivilegedBankAccountCDM;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.BankAccountStatus;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class CompanyToPrivilegedEmployerInfoCDMMapper extends BeanMapper<Company, PrivilegedEmployerInfoCDM> {

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("sourceCompanyId", "companyId");
		addBidirectionalFieldMapping("legalName", "name");
		addBidirectionalFieldMapping("dbaName", "businessName");
		addBidirectionalFieldMapping("fundingModel.numberOfFundingDays", "preFundDays");
		addBidirectionalFieldMapping("fedTaxId", "taxIdentificationNumber");
	}

	@Override
	public void mapAtoB(Company company, PrivilegedEmployerInfoCDM employerInfoCDM, MappingContext context) {
		DomainEntitySet<CompanyBankAccount> bankAccount = company.getCompanyBankAccountCollection().find(CompanyBankAccount.StatusCd().equalTo(BankAccountStatus.Active));

		// SELECTING FIRST OF ACTIVE BANK ACCOUNTS
		if (bankAccount.size() > 0) {
			employerInfoCDM.setBankAccount(
					getEntityCDMMapper().mapToTarget(bankAccount.get(0), PrivilegedBankAccountCDM.class));
			employerInfoCDM.setBankAccountId(bankAccount.get(0).getId().toString());

		} else {
			employerInfoCDM.setBankAccountId("1");
		}

		employerInfoCDM.setAddress(getEntityCDMMapper().mapToTarget(company.getMailingAddress(), AddressCDM.class));
	}

}
