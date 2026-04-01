package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.shared.model.AddressSubCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDMImpl;
import com.intuit.sbd.payroll.psp.domain.Address;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.EmployeeToBankPrincipalCDMMapper")
public class EmployeeToBankPrincipalCDMMapper extends BeanMapper<Employee,BankPrincipalCDM> {

    @Override
    public BankPrincipalCDM mapToTarget(Employee employee, Class<BankPrincipalCDM> target) {
        BankPrincipalCDM bankPrincipalCDM = new BankPrincipalCDMImpl();
        if (Objects.nonNull(employee.getMailingAddress()))
            bankPrincipalCDM.setAddress(getMapper().mapToTarget(employee.getMailingAddress(), AddressSubCDM.class));
        bankPrincipalCDM.setFirstName(employee.getFirstName());
        bankPrincipalCDM.setTaxIdentifier(employee.getTaxId());
        bankPrincipalCDM.setLastName(employee.getLastName());
        if (Objects.nonNull(employee.getBirthDate()))
            bankPrincipalCDM.setBirthDate(SpcfCalendar.createInstance(
                    employee.getBirthDate().getYear(),
                    employee.getBirthDate().getMonth(),
                    employee.getBirthDate().getDay(),
                    SpcfTimeZone.getLocalTimeZone()).toLocalDate());
        return bankPrincipalCDM;
    }
}
