package com.intuit.sbd.payroll.psp.mapper.orika;

import com.intuit.payroll.api.shared.model.BankAccountSubCDM;
import com.intuit.payroll.api.shared.model.BankPrincipalCDM;
import com.intuit.sbd.payroll.psp.domain.BankAccount;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import ma.glasnost.orika.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author kmuthurangam
 */
@Component
public class CompanyBankAccountToBankAccountSubCDMMapper extends BeanMapper<CompanyBankAccount, BankAccountSubCDM> {

    public static Logger LOGGER = LoggerFactory.getLogger(CompanyBankAccountToBankAccountSubCDMMapper.class);

    @Override
    public void directFieldToFieldMapping() {
        addBidirectionalFieldMapping("id", "id");
    }

    @Override
    public void mapAtoB(CompanyBankAccount companyBankAccount, BankAccountSubCDM bankAccountCDM,
                        MappingContext context) {
        bankAccountCDM.setOwnerType("employer.principal");
        bankAccountCDM.setPrincipal(getEntityCDMMapper().mapToTarget(companyBankAccount.getCompany(), BankPrincipalCDM.class));
        BankAccount bankAccount = companyBankAccount.getBankAccount();
        if (bankAccount != null) {
            bankAccountCDM.setAccountNumber(bankAccount.getAccountNumber());
            bankAccountCDM.setRoutingNumber(bankAccount.getRoutingNumber());
        } else {
            LOGGER.error("Missing bank account for company {}", companyBankAccount.getCompany().getSourceCompanyId());
        }
    }

}
