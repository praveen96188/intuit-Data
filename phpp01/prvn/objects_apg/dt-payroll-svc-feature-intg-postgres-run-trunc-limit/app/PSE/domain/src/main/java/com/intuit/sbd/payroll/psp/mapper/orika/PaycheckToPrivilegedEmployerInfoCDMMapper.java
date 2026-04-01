package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.payslip.model.PrivilegedEmployerInfoCDM;
import com.intuit.payroll.api.shared.model.AddressCDM;
import com.intuit.payroll.api.shared.model.PrivilegedBankAccountCDM;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class PaycheckToPrivilegedEmployerInfoCDMMapper extends BeanMapper<Paycheck, PrivilegedEmployerInfoCDM> {

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("company.sourceCompanyId", "companyId");
		addBidirectionalFieldMapping("company.legalName", "name");
		addBidirectionalFieldMapping("company.dbaName", "businessName");
		addBidirectionalFieldMapping("company.fundingModel.numberOfFundingDays", "preFundDays");
		addBidirectionalFieldMapping("company.fedTaxId", "taxIdentificationNumber");
	}

	@Override
	public void mapAtoB(Paycheck paycheck, PrivilegedEmployerInfoCDM employerInfoCDM, MappingContext context) {
		Company company = paycheck.getCompany();
		if (company != null) {
			/*
			 * DomainEntitySet<FinancialTransaction> financialTransactions =
			 * paycheck.getPayrollRun()
			 * .getFinancialTransactions(TransactionTypeCode.EmployerDdDebit);
			 *
			 * if (financialTransactions.size() >= 1) { FinancialTransaction
			 * financialTransaction = financialTransactions.getFirst(); CompanyBankAccount
			 * companyBankAccount = financialTransaction.getCompanyBankAccount();
			 * System.out.println(financialTransaction.getCreditBankAccount());
			 * System.out.println(financialTransaction.getDebitBankAccount());
			 *
			 * }
			 */
			CompanyBankAccount bankAccount = CompanyBankAccount.findCompanyBankAccountIncludingExpired(paycheck.getCompany(), paycheck.getPayrollRun().getPayrollDebitInfo().bankAccount);
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
