package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.shared.model.AddressSubCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDMImpl;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Payee;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.PayeeToBankPrincipalCDMMapper")
public class PayeeToBankPrincipalCDMMapper extends BeanMapper<Payee,BankPrincipalCDM> {

    @Override
    public BankPrincipalCDM mapToTarget(Payee payee, Class<BankPrincipalCDM> target) {
        BankPrincipalCDM result = new BankPrincipalCDMImpl();
        if (Objects.nonNull(payee.getMailingAddress()))
            result.setAddress(getMapper().mapToTarget(payee.getMailingAddress(), AddressSubCDM.class));
        result.setFirstName(payee.getName());
        return result;
    }
}
