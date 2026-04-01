package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.payslip.model.PrivilegedEmployerInfoCDM;
import com.intuit.payroll.api.shared.model.AddressSubCDM;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.FundingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.CompanyToPrivilegedEmployerInfoCDM")
@Slf4j
public class CompanyToPrivilegedEmployerInfoCDM extends BeanMapper<Company, PrivilegedEmployerInfoCDM> {

    /**
     * @param s          source object: Company
     * @param targetType target type object: PrivilegedEmployerInfoCDM
     * @return PrivilegedEmployerInfoCDM
     */
    @Override
    public PrivilegedEmployerInfoCDM mapToTarget(Company s, Class<PrivilegedEmployerInfoCDM> targetType) {

        if (isNull(s)) {
            log.error("Company is null");
            return null;
        }
        PrivilegedEmployerInfoCDM t = new PrivilegedEmployerInfoCDM();
        t.setCompanyId(s.getSourceCompanyId());
        t.setName(s.getLegalName());
        t.setBusinessName(s.getDbaName());
        t.setTaxIdentificationNumber(s.getFedTaxId());
        t.setAddress(getMapper().mapToTarget(s.getLegalAddress(), AddressSubCDM.class));
        t.setActiveEmployeeCount(s.getEmployees() != null ? s.getEmployees().size() : 0);
        if (nonNull(s.getFundingModel())) {
            FundingModel fundingModel = s.getFundingModel();
            t.setPreFundDays(isNull(fundingModel) ? null : fundingModel.getNumberOfFundingDays());
        }

        return t;
    }

}