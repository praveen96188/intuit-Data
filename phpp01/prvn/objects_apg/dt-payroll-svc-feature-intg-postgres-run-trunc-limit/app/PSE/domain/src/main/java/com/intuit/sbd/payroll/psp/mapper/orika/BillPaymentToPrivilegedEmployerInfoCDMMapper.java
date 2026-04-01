package com.intuit.sbd.payroll.psp.mapper.orika;

import com.intuit.payroll.api.payslip.model.PrivilegedEmployerInfoCDM;
import com.intuit.payroll.api.shared.model.AddressCDM;
import com.intuit.payroll.api.shared.model.PrivilegedBankAccountCDM;
import com.intuit.sbd.payroll.psp.domain.BillPayment;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class BillPaymentToPrivilegedEmployerInfoCDMMapper extends BeanMapper<BillPayment, PrivilegedEmployerInfoCDM> {

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("payee.company.sourceCompanyId", "companyId");
		addBidirectionalFieldMapping("payee.company.legalName", "name");
		addBidirectionalFieldMapping("payee.company.dbaName", "businessName");
		addBidirectionalFieldMapping("payee.company.fundingModel.numberOfFundingDays", "preFundDays");
		addBidirectionalFieldMapping("payee.company.fedTaxId", "taxIdentificationNumber");
	}

	@Override
	public void mapAtoB(BillPayment billPayment, PrivilegedEmployerInfoCDM employerInfoCDM, MappingContext context) {
		Company company = billPayment.getPayee().getCompany();
		if (company != null) {
			CompanyBankAccount bankAccount = CompanyBankAccount.findCompanyBankAccountIncludingExpired(billPayment.getPayrollRun().getCompany(), billPayment.getPayrollRun().getPayrollDebitInfo().bankAccount);

			if (bankAccount != null) {
				employerInfoCDM.setBankAccountId(bankAccount.getId().toString());
				employerInfoCDM
						.setBankAccount(getEntityCDMMapper().mapToTarget(bankAccount, PrivilegedBankAccountCDM.class));
			} else {
				employerInfoCDM.setBankAccountId("1");
			}

			employerInfoCDM.setAddress(getEntityCDMMapper().mapToTarget(company.getLegalAddress(), AddressCDM.class));
		}
	}

}