package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.shared.model.BankAccountSubCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import ma.glasnost.orika.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmployeeBankAccountToBankAccountSubCDMMapper extends BeanMapper<EmployeeBankAccount, BankAccountSubCDM> {

	public static Logger LOGGER = LoggerFactory.getLogger(EmployeeBankAccountToBankAccountSubCDMMapper.class);

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("id", "id");
	}

	@Override
	public void mapAtoB(EmployeeBankAccount employeeBankAccount, BankAccountSubCDM bankAccountCDM,
			MappingContext context) {
		bankAccountCDM.setPrincipal(
				getEntityCDMMapper().mapToTarget(employeeBankAccount.getEmployee(), BankPrincipalCDM.class));
		BankAccount bankAccount = employeeBankAccount.getBankAccount();
		if (bankAccount != null) {
			bankAccountCDM.setAccountNumber(bankAccount.getAccountNumber());
			bankAccountCDM.setRoutingNumber(bankAccount.getRoutingNumber());
		} else {
			LOGGER.error("Missing bank account for employee {}", employeeBankAccount.getEmployee().getId());
		}
	}

}
