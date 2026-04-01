package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.shared.model.*;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.BankAccountToPrivilegedBankAccountCDMMapper")
@Slf4j
public class BankAccountToPrivilegedBankAccountCDMMapper extends BeanMapper<BankAccount, PrivilegedBankAccountCDM> {

    @Override
    public PrivilegedBankAccountCDM mapToTarget(BankAccount s, Class<PrivilegedBankAccountCDM> target) {

        if (isNull(s)) {
            log.error("BankAccount not found");
            return null;
        }

        PrivilegedBankAccountCDM t = new PrivilegedBankAccountCDMImpl();
        t.setAccountNumber(s.getAccountNumber());
        t.setRoutingNumber(s.getRoutingNumber());

        // set Account Type
        com.intuit.sbd.payroll.psp.domain.BankAccountType bankAccountType = s.getAccountTypeCd();
        if (!isNull(bankAccountType)) {
            if (bankAccountType == com.intuit.sbd.payroll.psp.domain.BankAccountType.Checking) {
                t.setAccountType(BankAccountType.BUSINESS_CHECKING);
            } else if (bankAccountType == com.intuit.sbd.payroll.psp.domain.BankAccountType.Savings) {
                t.setAccountType(BankAccountType.BUSINESS_SAVINGS);
            }
        }
        return t;
    }
}