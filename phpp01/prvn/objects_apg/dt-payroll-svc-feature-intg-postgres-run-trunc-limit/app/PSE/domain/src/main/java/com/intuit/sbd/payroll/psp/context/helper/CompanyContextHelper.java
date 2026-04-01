package com.intuit.sbd.payroll.psp.context.helper;

import com.intuit.sbd.payroll.psp.domain.Company;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class CompanyContextHelper extends BaseCompanyContextHelper {
    protected Collection<Company> filterCompanyList(Collection<Company> companies){
        return companies;
    }
}
