package com.intuit.sbd.payroll.psp.mapper.guideline401k.employee;

import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeStatus;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.v4.network.Contact;
import com.intuit.v4.network.definitions.EmployeeHireDetails;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PspEmployeeToV4ContactProfilesMapper extends BeanMapper<Employee, Contact.Profiles> {

    @Override
    public Contact.Profiles mapToTarget(Employee employee, Class<Contact.Profiles> t) {
        Contact.Profiles profiles = new Contact.Profiles();
        profiles.setEmployee(getV4EmployeeFromPspEmployee(employee));
        return profiles;
    }

    private com.intuit.v4.network.relationships.Employee getV4EmployeeFromPspEmployee(Employee employee) {
        com.intuit.v4.network.relationships.Employee v4Employee = new com.intuit.v4.network.relationships.Employee();
        //Job title mapping not available
        v4Employee.setJobTitle(null);
        //If employment status is not active, then terminated is set.
        v4Employee.setEmploymentStatus(Objects.nonNull(employee.getStatusCd()) ?
                employee.getStatusCd().equals(EmployeeStatus.Active) ?
                        com.intuit.v4.network.relationships.Employee.EmploymentStatusEnum.ACTIVE :
                        com.intuit.v4.network.relationships.Employee.EmploymentStatusEnum.TERMINATED :
                null);

        v4Employee.setActive(Objects.nonNull(employee.getStatusCd()) ?
                employee.getStatusCd().equals(EmployeeStatus.Active) ? true : false : null);

        v4Employee.setEmployeeHireDetails(getV4HireDetailsForEmployee(employee));

        return v4Employee;
    }

    private EmployeeHireDetails getV4HireDetailsForEmployee(Employee employee) {
        EmployeeHireDetails employeeHireDetails = new EmployeeHireDetails();

        employeeHireDetails.setHireDate(Objects.nonNull(employee.getHireDate()) ?
                employee.getHireDate().toLocalDate() : null);
        employeeHireDetails.setReleaseDate(Objects.nonNull(employee.getTerminationDate()) ?
                employee.getTerminationDate().toLocalDate() : null);

        return employeeHireDetails;
    }

}
