package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.payslip.model.PrivilegedEmployeeInfoCDM;
import com.intuit.payroll.api.shared.model.AddressCDM;
import com.intuit.sbd.payroll.psp.domain.Payee;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class PayeeToPrivilegedEmployeeInfoCDMMapper extends BeanMapper<Payee, PrivilegedEmployeeInfoCDM> {

	@Override
	public void directFieldToFieldMapping() {
		addBidirectionalFieldMapping("id", "employeeId");
		addBidirectionalFieldMapping("name", "firstName");
		addBidirectionalFieldMapping("taxId", "taxId");
		addBidirectionalFieldMapping("email", "federalTaxFilingInfo.jurisdiction");
		addBidirectionalFieldMapping("phone", "federalTaxFilingInfo.filingStatus");
	}

	@Override
	public void mapAtoB(Payee payee, PrivilegedEmployeeInfoCDM privilegedEmployeeInfoCDM, MappingContext context) {
		privilegedEmployeeInfoCDM
				.setHomeAddress(getEntityCDMMapper().mapToTarget(payee.getMailingAddress(), AddressCDM.class));

	}

}
