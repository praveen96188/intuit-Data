package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.shared.model.AddressCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.sbd.payroll.psp.domain.Employee;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class EmployeeToBankPrincipalCDMMapper extends BeanMapper<Employee, BankPrincipalCDM> {

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("firstName", "firstName");
		addBidirectionalFieldMapping("lastName", "lastName");
		addBidirectionalFieldMapping("birthDate", "birthDate");
		addBidirectionalFieldMapping("taxId", "taxIdentifier");
	}

	@Override
	public void mapAtoB(Employee employee, BankPrincipalCDM bankPrincipalCDM, MappingContext context) {
		bankPrincipalCDM.setAddress(getEntityCDMMapper().mapToTarget(employee.getMailingAddress(), AddressCDM.class));
	}

}
