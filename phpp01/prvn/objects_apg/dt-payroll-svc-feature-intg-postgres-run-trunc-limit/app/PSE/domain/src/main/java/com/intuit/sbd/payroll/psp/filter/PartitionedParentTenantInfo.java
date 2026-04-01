package com.intuit.sbd.payroll.psp.filter;

import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

import java.util.Objects;

/* This interface is used for Parent Classes of Partitioned Tables which have COMPANY_FK
* */
public interface PartitionedParentTenantInfo {
    default SpcfUniqueId getTenantId(){
        Company company = getCompany();
        if(Objects.isNull(company)){
            return null;
        }
        return company.getId();
    }

    Company getCompany();
}
