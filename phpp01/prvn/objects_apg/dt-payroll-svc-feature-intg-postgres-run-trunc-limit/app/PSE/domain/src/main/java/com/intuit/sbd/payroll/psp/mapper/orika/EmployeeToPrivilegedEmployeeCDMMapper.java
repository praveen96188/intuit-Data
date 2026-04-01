package com.intuit.sbd.payroll.psp.mapper.orika;

import com.intuit.payroll.api.employee.model.PrivilegedEmployeeCDM;
import com.intuit.payroll.api.shared.model.AddressCDM;
import com.intuit.payroll.api.shared.model.BankAccountSubCDM;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import ma.glasnost.orika.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EmployeeToPrivilegedEmployeeCDMMapper extends BeanMapper<Employee, PrivilegedEmployeeCDM> {

	public static Logger LOGGER = LoggerFactory.getLogger(EmployeeToPrivilegedEmployeeCDMMapper.class);

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("id", "id");
		addBidirectionalFieldMapping("firstName", "firstName");
		addBidirectionalFieldMapping("lastName", "lastName");
		addBidirectionalFieldMapping("genderCd", "gender");
		addBidirectionalFieldMapping("phone", "homePhone");
		addBidirectionalFieldMapping("taxId", "taxId");
		addBidirectionalFieldMapping("birthDate", "birthDate");
	}

	@Override
	public void mapAtoB(Employee employee, PrivilegedEmployeeCDM privilegedEmployeeCDM, MappingContext context) {
		privilegedEmployeeCDM
				.setHomeAddress(getEntityCDMMapper().mapToTarget(employee.getMailingAddress(), AddressCDM.class));
		privilegedEmployeeCDM.setWorkAddress(
				getEntityCDMMapper().mapToTarget(employee.getCompany().getLegalAddress(), AddressCDM.class));
		addBankAccounts(employee, privilegedEmployeeCDM);

	}

	private void addBankAccounts(Employee employee, PrivilegedEmployeeCDM privilegedEmployeeCDM) {
		// Map sensitized version of bank accounts
		DomainEntitySet<EmployeeBankAccount> bankAccount = employee.getEmployeeBankAccountCollection();
		if (bankAccount != null && bankAccount.size() >= 1) {
			List<BankAccountSubCDM> bankAccountSubCDMs = new ArrayList<BankAccountSubCDM>();
			for (int i = 0, n = bankAccount.size(); i < n; i++) {
				BankAccountSubCDM bankAccountSubCDM = getEntityCDMMapper().mapToTarget(bankAccount.get(i),
						BankAccountSubCDM.class);
				bankAccountSubCDMs.add(bankAccountSubCDM);
			}
			privilegedEmployeeCDM.setBankAccounts(bankAccountSubCDMs);
		} else {
			LOGGER.warn("BankAccount is null for employeeId={}", employee.getId());
		}
	}
}
