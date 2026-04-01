package com.intuit.sbd.payroll.psp.mapper.guideline401k.company;

import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyAdditionalInfo;
import com.intuit.sbd.payroll.psp.domain.IndustryType;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.v4.company.definitions.Industry;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PspCompanyToV4IndustryMapper extends BeanMapper<Company, Industry> {

    @Override
    public Industry mapToTarget(Company pspCompany, Class<Industry> t) {

        CompanyAdditionalInfo companyAdditionalInfo = pspCompany.getCompanyAdditionalInfo();
        Industry v4Industry = new Industry();
        if(Objects.isNull(companyAdditionalInfo)) {
            return v4Industry;
        }
        IndustryType pspIndustryType = companyAdditionalInfo.getIndustryType();

        if(Objects.isNull(pspIndustryType)) {
            return v4Industry;
        }
        String pspIndustryName = pspIndustryType.getIndustry();

        if(!pspIndustryName.isEmpty()){
            v4Industry.setName(pspIndustryName);
        }

        String pspIndustryCode = pspIndustryType.getStandardIndustryCode();
        if(!pspIndustryCode.isEmpty()){
            v4Industry.setCode(pspIndustryCode);
        }
        return v4Industry;
    }
}
