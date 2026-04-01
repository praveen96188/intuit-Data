package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.shared.model.BankAccountCDMImpl;
import com.intuit.payroll.api.shared.model.BankAccountSubCDM;
import com.intuit.payroll.api.shared.model.BankAccountType;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.BankAccountToBankAccountSubCDMMapper")
public class BankAccountToBankAccountSubCDMMapper extends BeanMapper<BankAccount, BankAccountSubCDM> {

    @Override
    public BankAccountSubCDM mapToTarget(BankAccount bankAccount, Class<BankAccountSubCDM> target) {
        Validate.notNull(bankAccount, "Bank Account ");

        BankAccountSubCDM bankAccountSubCDM = new BankAccountCDMImpl();
        bankAccountSubCDM.setAccountNumber(bankAccount.getAccountNumber());
        bankAccountSubCDM.setRoutingNumber(bankAccount.getRoutingNumber());

        SpcfUniqueId id= bankAccount.getId();
        if(Objects.nonNull(id)) {
            bankAccountSubCDM.setId(id.toString());
        }
        //TODO need to know the conversion
        bankAccountSubCDM.setAccountType(getBankAccountType(bankAccount.getAccountTypeCd()));
        return bankAccountSubCDM;
    }

    private BankAccountType getBankAccountType(com.intuit.sbd.payroll.psp.domain.BankAccountType bankAccountType) {
        if(Objects.isNull(bankAccountType)) {
            return null;
        }
        return BankAccountType.valueOf("PERSONAL_"+ StringUtils.toRootUpperCase(bankAccountType.name()));
    }
}