package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.payslip.model.PrivilegedEmployeeInfoCDM;
import com.intuit.payroll.api.payslip.model.TaxFilingInformationCDM;
import com.intuit.payroll.api.shared.model.AddressCDM;
import com.intuit.sbd.payroll.psp.domain.Employee;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class EmployeeToPrivilegedEmployeeInfoCDMMapper extends BeanMapper<Employee, PrivilegedEmployeeInfoCDM> {
	
	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("id", "employeeId");
		addBidirectionalFieldMapping("firstName", "firstName");
		addBidirectionalFieldMapping("lastName", "lastName");
		addBidirectionalFieldMapping("taxId", "taxId");
		addBidirectionalFieldMapping("email", "federalTaxFilingInfo.jurisdiction");
		addBidirectionalFieldMapping("phone", "federalTaxFilingInfo.filingStatus");
	}

	@Override
	public void mapAtoB(Employee employee, PrivilegedEmployeeInfoCDM privilegedEmployeeInfoCDM,
			MappingContext context) {
		if(employee.getBirthDate()!=null) {
			TaxFilingInformationCDM homeStateTaxFilingInfo = new TaxFilingInformationCDM();
			homeStateTaxFilingInfo.setJurisdiction(employee.getBirthDate().format("yyyy-MM-dd"));
			privilegedEmployeeInfoCDM.setHomeStateTaxFilingInfo(homeStateTaxFilingInfo);
		}
		privilegedEmployeeInfoCDM
				.setHomeAddress(getEntityCDMMapper().mapToTarget(employee.getMailingAddress(), AddressCDM.class));

	}

}
