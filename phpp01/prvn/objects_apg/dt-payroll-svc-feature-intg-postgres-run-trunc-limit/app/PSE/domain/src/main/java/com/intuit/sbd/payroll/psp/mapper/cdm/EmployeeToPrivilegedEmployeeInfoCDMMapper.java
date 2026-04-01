package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.payslip.model.PrivilegedEmployeeInfoCDM;
import com.intuit.payroll.api.shared.model.AddressSubCDM;
import com.intuit.sbd.payroll.psp.domain.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.EmployeeToPrivilegedEmployeeInfoCDMMapper")
@Slf4j
public class EmployeeToPrivilegedEmployeeInfoCDMMapper extends BeanMapper<Employee, PrivilegedEmployeeInfoCDM> {

    /**
     * @param s          source object: Employee
     * @param targetType target type object: PrivilegedEmployeeInfoCDM
     * @return PrivilegedEmployeeInfoCDM
     */
    @Override
    public PrivilegedEmployeeInfoCDM mapToTarget(Employee s, Class<PrivilegedEmployeeInfoCDM> targetType) {
        String employeeId = isNull(s.getId()) ? null : s.getId().toString();
        PrivilegedEmployeeInfoCDM t = new PrivilegedEmployeeInfoCDM();
        t.setEmployeeId(employeeId);
        t.setFirstName(s.getFirstName());
        t.setLastName(s.getLastName());
        t.setTaxId(s.getTaxId());
        t.setHomeAddress(getMapper().mapToTarget(s.getMailingAddress(), AddressSubCDM.class));
        return t;
    }
}