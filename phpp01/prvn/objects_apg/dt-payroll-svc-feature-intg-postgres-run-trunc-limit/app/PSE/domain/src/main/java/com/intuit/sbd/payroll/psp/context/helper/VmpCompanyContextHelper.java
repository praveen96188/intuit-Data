package com.intuit.sbd.payroll.psp.context.helper;

import com.intuit.sbd.payroll.psp.context.exception.NoVmpCompanyFoundException;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Slf4j
public class VmpCompanyContextHelper extends BaseCompanyContextHelper {

    protected Collection<Company> filterCompanyList(Collection<Company> companyList){
        Collection<Company> filteredCompanyList = null;
        if (CollectionUtils.isNotEmpty(companyList)) {
            Company[] companyArray = new Company[companyList.size()];
            filteredCompanyList = CompanyService.filterVMPEnabledCompanies(companyList.toArray(companyArray));
        }

        if(CollectionUtils.isEmpty(filteredCompanyList)) {
            throw new NoVmpCompanyFoundException("No Vmp company found");
        }

        if(filteredCompanyList.size() > 1) {
            return null;
        }

        return filteredCompanyList;
    }
}
