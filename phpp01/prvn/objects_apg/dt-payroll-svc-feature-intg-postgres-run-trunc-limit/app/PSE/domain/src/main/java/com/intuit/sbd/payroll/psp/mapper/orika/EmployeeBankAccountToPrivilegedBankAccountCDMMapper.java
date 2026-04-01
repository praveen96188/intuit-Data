package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.payroll.api.shared.model.PrivilegedBankAccountCDM;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class EmployeeBankAccountToPrivilegedBankAccountCDMMapper
		extends BeanMapper<EmployeeBankAccount, PrivilegedBankAccountCDM> {

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("id", "id");
		addBidirectionalFieldMapping("bankAccount.accountTypeCd", "accountType");
		addBidirectionalFieldMapping("bankAccount.accountNumber", "accountNumber");
		addBidirectionalFieldMapping("bankAccount.routingNumber", "routingNumber");
		addBidirectionalFieldMapping("modifiedDate", "updated");
		addBidirectionalFieldMapping("effectiveDate", "created");
	}

	@Override
	public void mapAtoB(EmployeeBankAccount employeeBankAccount, PrivilegedBankAccountCDM bankAccountCDM,
			MappingContext context) {
		// ALWAYS SET AS FALSE
		bankAccountCDM.setVerified(false);
		if (employeeBankAccount.getEmployee() != null && employeeBankAccount.getEmployee().getCompany() != null
				&& employeeBankAccount.getEmployee().getCompany().getSourceCompanyId() != null) {
			bankAccountCDM
					.setCompanyId(Long.valueOf(employeeBankAccount.getEmployee().getCompany().getSourceCompanyId()));
		}
		if (employeeBankAccount.getEmployee().getCompany() != null) {
			bankAccountCDM.setPrincipal(getEntityCDMMapper().mapToTarget(employeeBankAccount.getEmployee().getCompany(),
					BankPrincipalCDM.class));
		}
	}

}
