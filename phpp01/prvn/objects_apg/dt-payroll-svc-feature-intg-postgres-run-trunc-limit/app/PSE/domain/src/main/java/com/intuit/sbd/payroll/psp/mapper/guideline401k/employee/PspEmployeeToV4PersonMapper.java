package com.intuit.sbd.payroll.psp.mapper.guideline401k.employee;

import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.v4.network.definitions.Person;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PspEmployeeToV4PersonMapper extends BeanMapper<Employee, Person> {
    @Override
    public Person mapToTarget(Employee employee, Class<Person> t) {

        Person person = new Person();
        person.setGivenName(employee.getFirstName());
        person.setFamilyName(employee.getLastName());
        person.setDateOfBirth(Objects.nonNull(employee.getBirthDate()) ?employee.getBirthDate().toLocalDate():null);
        person.setMiddleName(employee.getMiddleName());

        return person;
    }
}
