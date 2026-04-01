package com.intuit.sbd.payroll.psp.mapper.orika;

import com.intuit.payroll.api.company.model.PrivilegedCompanyCDM;
import com.intuit.payroll.api.shared.model.BankAccountCDM;
import com.intuit.payroll.api.shared.model.PrivilegedBankAccountCDMImpl;
import com.intuit.sbd.payroll.psp.domain.Company;
import ma.glasnost.orika.MappingContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompanyToPrivilegedCompanyCDMMapper extends BeanMapper<Company, PrivilegedCompanyCDM> {

    @Override
    public void directFieldToFieldMapping() {
    }

    @Override
    public void mapAtoB(Company company, PrivilegedCompanyCDM privilegedCompanyCDM, MappingContext context) {
        BankAccountCDM bankAccountCDM = getEntityCDMMapper().mapToTarget(company, PrivilegedBankAccountCDMImpl.class);
        if (bankAccountCDM != null) {
            List<BankAccountCDM> bankAccountCDMs = new ArrayList<>();
            bankAccountCDMs.add(bankAccountCDM);
            privilegedCompanyCDM.setPrivilegedBankAccounts(bankAccountCDMs);

            // clear out the non privileged bankAccount field
            privilegedCompanyCDM.setBankAccounts(null);
        }
    }

}
