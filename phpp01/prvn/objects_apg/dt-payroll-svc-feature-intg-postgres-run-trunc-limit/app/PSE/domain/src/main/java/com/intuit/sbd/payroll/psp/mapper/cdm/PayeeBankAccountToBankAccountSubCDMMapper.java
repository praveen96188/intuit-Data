package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.shared.model.BankAccountCDMImpl;
import com.intuit.payroll.api.shared.model.BankAccountSubCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import com.intuit.sbd.payroll.psp.domain.PayeeBankAccount;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.PayeeBankAccountToBankAccountSubCDMMapper")
public class PayeeBankAccountToBankAccountSubCDMMapper extends BeanMapper<PayeeBankAccount, BankAccountSubCDM> {
    @Override
    public BankAccountSubCDM mapToTarget(PayeeBankAccount payeeBankAccount, Class<BankAccountSubCDM> target) {
        BankAccountSubCDM result = getMapper().mapToTarget(payeeBankAccount.getBankAccount(), BankAccountSubCDM.class);
        if(Objects.isNull(result)){
            result = new BankAccountCDMImpl();
        }
        result.setPrincipal(getMapper().mapToTarget(payeeBankAccount.getPayee(), BankPrincipalCDM.class));
        result.setEntityVersion(String.valueOf(SpcfCalendar.getNow().getTimeInMilliseconds()));
        return result;
    }
}