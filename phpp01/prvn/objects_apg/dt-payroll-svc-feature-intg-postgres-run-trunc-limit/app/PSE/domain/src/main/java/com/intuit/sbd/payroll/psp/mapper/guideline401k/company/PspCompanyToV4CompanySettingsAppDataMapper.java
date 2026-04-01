package com.intuit.sbd.payroll.psp.mapper.guideline401k.company;

import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.mapper.cdm.BeanMapper;
import com.intuit.sbd.payroll.psp.mapper.guideline401k.address.PspAddressToV4AddressMapper;
import com.intuit.v4.common.Address;
import com.intuit.v4.company.definitions.CompanyInfoSettingsAppData;
import com.intuit.v4.company.definitions.Industry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PspCompanyToV4CompanySettingsAppDataMapper extends BeanMapper<Company, CompanyInfoSettingsAppData> {

    private final PspCompanyToV4IndustryMapper pspCompanyToV4IndustryMapper;
    private final PspCompanyToV4TaxFormMapper pspCompanyToV4TaxFormMapper;
    private final PspAddressToV4AddressMapper pspAddressToV4AddressMapper;

    @Autowired
    public PspCompanyToV4CompanySettingsAppDataMapper(PspCompanyToV4IndustryMapper pspCompanyToV4IndustryMapper,
                                                      PspCompanyToV4TaxFormMapper pspCompanyToV4TaxFormMapper,
                                                      PspAddressToV4AddressMapper pspAddressToV4AddressMapper) {
        this.pspCompanyToV4IndustryMapper = pspCompanyToV4IndustryMapper;
        this.pspCompanyToV4TaxFormMapper = pspCompanyToV4TaxFormMapper;
        this.pspAddressToV4AddressMapper = pspAddressToV4AddressMapper;
    }

    @Override
    public CompanyInfoSettingsAppData mapToTarget(Company pspCompany, Class<CompanyInfoSettingsAppData> t) {
        CompanyInfoSettingsAppData v4CompanyInfoSettingsAppData = new CompanyInfoSettingsAppData();

        v4CompanyInfoSettingsAppData.setLegalName(StringUtils.defaultString(pspCompany.getLegalName()));

        v4CompanyInfoSettingsAppData.setCompanyName(StringUtils.defaultString(pspCompany.getDbaName()));

        v4CompanyInfoSettingsAppData.setEin(StringUtils.defaultString(pspCompany.getFedTaxId()));

        Industry v4Industry = pspCompanyToV4IndustryMapper.mapToTarget(pspCompany, Industry.class);
        v4CompanyInfoSettingsAppData.setIndustry(v4Industry);


        TaxForm taxForm = pspCompanyToV4TaxFormMapper.mapToTarget(pspCompany, TaxForm.class);
        v4CompanyInfoSettingsAppData.setTaxForm(taxForm.id);

        v4CompanyInfoSettingsAppData.setCompanyEmail(StringUtils.defaultString(pspCompany.getNotificationEmail()));

        v4CompanyInfoSettingsAppData.setPublicEmail(StringUtils.defaultString(pspCompany.getNotificationEmail()));

        v4CompanyInfoSettingsAppData.setCompanyPhone(StringUtils.defaultString(pspCompany.getPhone()));

        v4CompanyInfoSettingsAppData.setLegalAddress(
                pspAddressToV4AddressMapper.mapToTarget(pspCompany.getLegalAddress(), Address.class));

        v4CompanyInfoSettingsAppData.setPublicAddress(
                pspAddressToV4AddressMapper.mapToTarget(pspCompany.getMailingAddress(),Address.class));

        v4CompanyInfoSettingsAppData.setCompanyAddress(
                pspAddressToV4AddressMapper.mapToTarget(pspCompany.getMailingAddress(),Address.class));

        return v4CompanyInfoSettingsAppData;
    }
}
