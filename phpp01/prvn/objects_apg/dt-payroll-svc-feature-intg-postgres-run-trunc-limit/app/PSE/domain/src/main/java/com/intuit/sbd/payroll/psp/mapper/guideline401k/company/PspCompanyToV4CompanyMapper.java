package com.intuit.sbd.payroll.psp.mapper.guideline401k.company;

import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.v4.Company;
import com.intuit.v4.company.Settings;
import com.intuit.v4.company.definitions.CompanyInfoSettingsAppData;
import com.intuit.v4.company.definitions.SettingsAppData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PspCompanyToV4CompanyMapper extends BeanMapper<com.intuit.sbd.payroll.psp.domain.Company, Company> {

    private final PspCompanyToV4CompanySettingsAppDataMapper pspCompanyToV4CompanySettingsAppDataMapper;

    @Autowired
    public PspCompanyToV4CompanyMapper(PspCompanyToV4CompanySettingsAppDataMapper pspCompanyToV4CompanySettingsAppDataMapper) {
        this.pspCompanyToV4CompanySettingsAppDataMapper = pspCompanyToV4CompanySettingsAppDataMapper;
    }

    @Override
    public Company mapToTarget(com.intuit.sbd.payroll.psp.domain.Company pspCompany, Class<Company> t) {
        Company v4Company = new Company();

        Settings settings = new Settings();
        SettingsAppData settingsAppData = new SettingsAppData();

        CompanyInfoSettingsAppData companyInfoSettingsAppData =
                pspCompanyToV4CompanySettingsAppDataMapper.mapToTarget(pspCompany, CompanyInfoSettingsAppData.class);

        settingsAppData.companyInfoAppData(companyInfoSettingsAppData);

        settings.qboAppData(settingsAppData);
        v4Company.settings(settings);

        return v4Company;
    }
}
