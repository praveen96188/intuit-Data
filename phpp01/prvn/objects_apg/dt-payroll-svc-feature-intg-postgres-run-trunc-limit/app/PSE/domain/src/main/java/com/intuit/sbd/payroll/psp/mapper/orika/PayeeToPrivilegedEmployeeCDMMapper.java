package com.intuit.sbd.payroll.psp.mapper.orika;
import com.intuit.payroll.api.employee.model.PrivilegedEmployeeCDM;
import com.intuit.payroll.api.shared.model.AddressCDM;
import com.intuit.payroll.api.shared.model.BankAccountSubCDM;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.Payee;
import com.intuit.sbd.payroll.psp.domain.PayeeBankAccount;
import ma.glasnost.orika.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PayeeToPrivilegedEmployeeCDMMapper extends BeanMapper<Payee, PrivilegedEmployeeCDM> {

    public static Logger LOGGER = LoggerFactory.getLogger(PayeeToPrivilegedEmployeeCDMMapper.class);

    @Override
    public void directFieldToFieldMapping() {
        addBidirectionalFieldMapping("id", "id");
        addBidirectionalFieldMapping("name", "firstName");
        addBidirectionalFieldMapping("phone", "homePhone");
        addBidirectionalFieldMapping("taxId", "taxId");
    }

    @Override
    public void mapAtoB(Payee payee, PrivilegedEmployeeCDM privilegedEmployeeCDM, MappingContext context) {
        privilegedEmployeeCDM
                .setHomeAddress(getEntityCDMMapper().mapToTarget(payee.getMailingAddress(), AddressCDM.class));
        addBankAccounts(payee, privilegedEmployeeCDM);

    }

    private void addBankAccounts(Payee payee, PrivilegedEmployeeCDM privilegedEmployeeCDM) {
        // Map sensitized version of bank accounts
        DomainEntitySet<PayeeBankAccount> bankAccount = payee.getPayeeBankAccountCollection();
        if (bankAccount != null && bankAccount.size() >= 1) {
            List<BankAccountSubCDM> bankAccountSubCDMs = new ArrayList<BankAccountSubCDM>();
            for (int i = 0, n = bankAccount.size(); i < n; i++) {
                BankAccountSubCDM bankAccountSubCDM = getEntityCDMMapper().mapToTarget(bankAccount.get(i),
                        BankAccountSubCDM.class);
                bankAccountSubCDMs.add(bankAccountSubCDM);
            }
            privilegedEmployeeCDM.setBankAccounts(bankAccountSubCDMs);
        } else {
            LOGGER.warn("BankAccount is null for payeeId={}", payee.getId());
        }
    }
}
