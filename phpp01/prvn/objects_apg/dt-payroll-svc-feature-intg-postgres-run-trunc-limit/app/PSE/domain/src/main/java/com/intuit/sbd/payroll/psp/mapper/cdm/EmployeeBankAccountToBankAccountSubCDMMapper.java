package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.shared.model.BankAccountCDMImpl;
import com.intuit.payroll.api.shared.model.BankAccountSubCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalSubCDM;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.EmployeeBankAccount;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.EmployeeBankAccountToBankAccountSubCDMMapper")
public class EmployeeBankAccountToBankAccountSubCDMMapper extends BeanMapper<EmployeeBankAccount, BankAccountSubCDM> {
    @Override
    public BankAccountSubCDM mapToTarget(EmployeeBankAccount employeeBankAccount, Class<BankAccountSubCDM> target) {
        BankAccountSubCDM bankAccountSubCDM = getMapper().mapToTarget(employeeBankAccount.getBankAccount(), BankAccountSubCDM.class);
        if(Objects.isNull(bankAccountSubCDM)){
            bankAccountSubCDM = new BankAccountCDMImpl();
        }

        bankAccountSubCDM.setPrincipal(getMapper().mapToTarget(employeeBankAccount.getEmployee(), BankPrincipalCDM.class));
        //TODO need to know the conversion
        //getMapper().mapToTarget(employeeBankAccount.getBankAccount(), BankAccountSubCDM.class);

        bankAccountSubCDM.setOwnerType("employee");
        return bankAccountSubCDM;
    }
}