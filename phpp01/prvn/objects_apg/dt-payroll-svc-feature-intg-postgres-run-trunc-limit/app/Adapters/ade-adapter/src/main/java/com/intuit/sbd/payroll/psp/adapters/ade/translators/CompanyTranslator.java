package com.intuit.sbd.payroll.psp.adapters.ade.translators;

import com.intuit.ems.cep.api.ResourceNameEnum;
import com.intuit.ems.cep.api.ServiceResult;
import com.intuit.ems.cep.company.v1.service.Expand;
import com.intuit.ems.cep.company.v1.service.params.CompanyServiceParams;
import com.intuit.ems.cep.company.v1.service.params.taxsetup.TaxSetupGetServiceParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.ade.cep.impl.ServiceFactory;
import com.intuit.sbd.payroll.psp.adapters.ade.mapping.*;
import com.intuit.sbd.payroll.psp.adapters.ade.tools.ServiceHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.schema.finance.v3.EmailAddress;
import com.intuit.schema.finance.v3.PhysicalAddress;
import com.intuit.schema.finance.v3.TelephoneNumber;
import com.intuit.schema.payroll.v3.common.ContactType;
import com.intuit.schema.payroll.v3.company.AdditionalFilingAmount;
import com.intuit.schema.payroll.v3.company.*;
import com.intuit.schema.payroll.v3.compliance.FilingTypeEnum;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: TimothyD698
 * Date: 8/2/13
 */
public class CompanyTranslator {
    private static final int MAX_RATE_SCALE = 6;

    public static final String[] IGNORE_AGENCIES = {"NOCALC", "SSA"};

    public static ServiceResult<com.intuit.schema.payroll.v3.company.Company> populateCompany(Company pspCompany, CompanyServiceParams pCompanyServiceParams) {
        ServiceResult<com.intuit.schema.payroll.v3.company.Company> serviceResult = new ServiceResult<com.intuit.schema.payroll.v3.company.Company>();
        com.intuit.schema.payroll.v3.company.Company companyCdm = new com.intuit.schema.payroll.v3.company.Company();

        companyCdm.setId(pspCompany.getSourceCompanyId());
        companyCdm.setCompanyLegalName(pspCompany.getLegalName());
        companyCdm.setAddress(createAddress(pspCompany.getMailingAddress()));
        companyCdm.setPhone(createPhoneNumber(pspCompany.getPhone()));
        companyCdm.setAccountStatus(getAccountStatus(pspCompany));

        if (pCompanyServiceParams.getExpand() != null) {
            @SuppressWarnings("unchecked")
            List<Expand> expandList = pCompanyServiceParams.getExpand();
            for (Expand expandItem : expandList) {
                switch (expandItem) {
                    case TAXSETUP: {
                        TaxSetupGetServiceParams taxSetupGetServiceParams = new TaxSetupGetServiceParams();
                        taxSetupGetServiceParams.setCompanyId(pCompanyServiceParams.getCompanyId());
                        ServiceResult<TaxSetup> taxSetupServiceResult = ServiceFactory.getInstance().<TaxSetup, TaxSetupGetServiceParams>constructGetServiceInstance(ResourceNameEnum.TAXSETUP).service(taxSetupGetServiceParams);
                        if (taxSetupServiceResult.isSuccess()) {
                            companyCdm.setTaxSetup(taxSetupServiceResult.getResult());
                        } else {
                            serviceResult.merge(taxSetupServiceResult);
                        }
                    }
                    break;
                    case CONTACTS: {
                        ServiceResult<List<com.intuit.schema.payroll.v3.common.Contact>> contactServiceResult = ServiceFactory.getInstance().<com.intuit.schema.payroll.v3.common.Contact, CompanyServiceParams>constructGetListServiceInstance(ResourceNameEnum.CONTACTS).service(pCompanyServiceParams);
                        if (contactServiceResult.isSuccess()) {
                            companyCdm.setContacts(contactServiceResult.getResult());
                        } else {
                            ServiceHelper.mergeResultIgnoreEntityDoesNotExist(contactServiceResult, serviceResult);
                        }
                        break;
                    }
                }
            }
        }

        serviceResult.setResult(companyCdm);
        return serviceResult;
    }

    public static List<com.intuit.schema.payroll.v3.common.Contact> getAllContactsRoleInfo(Company pspCompany, ContactRole contactRole) {
        List<com.intuit.schema.payroll.v3.common.Contact> cdmContacts = new ArrayList<com.intuit.schema.payroll.v3.common.Contact>();

        if (contactRole != null) {
            DomainEntitySet<Contact> pspContacts = pspCompany.getContactsByRoleCode(contactRole);
            for (Contact pspContact : pspContacts) {
                com.intuit.schema.payroll.v3.common.Contact contactCdm = new com.intuit.schema.payroll.v3.common.Contact();
                contactCdm.setId(pspContact.getId().toString());
                contactCdm.setTaxId(pspCompany.getFedTaxId());
                contactCdm.setSuffix(pspContact.getSuffix());
                contactCdm.setGivenName(pspContact.getFirstName());
                contactCdm.setMiddleName(pspContact.getMiddleName());
                contactCdm.setFamilyName(pspContact.getLastName());
                contactCdm.setFullName(pspContact.getFullName());
                contactCdm.setPrimaryPhone(createPhoneNumber(pspContact.getPhone()));
                contactCdm.setAlternatePhone(createPhoneNumber(pspContact.getSecondPhone()));
                contactCdm.setFax(createPhoneNumber(pspContact.getFax()));
                contactCdm.setCompanyName(pspCompany.getLegalName());

                String email = pspContact.getEmail();
                if (email != null) {
                    EmailAddress emailAddress = new EmailAddress();
                    emailAddress.setAddress(email);
                    contactCdm.setPrimaryEmailAddress(emailAddress);
                }

                cdmContacts.add(contactCdm);
            }
        }
        return cdmContacts;
    }

    public static com.intuit.schema.payroll.v3.common.Contact getContactRoleInfo(Company pspCompany, ContactRole contactRole) {
        if (contactRole == null) {
            return null;
        }

        com.intuit.schema.payroll.v3.common.Contact contactCdm = new com.intuit.schema.payroll.v3.common.Contact();

        Contact pspContact = pspCompany.getContactByRoleCode(contactRole);
        if (pspContact != null) {
            contactCdm.setId(pspContact.getId().toString());
            contactCdm.setTaxId(pspCompany.getFedTaxId());
            contactCdm.setSuffix(pspContact.getSuffix());
            contactCdm.setGivenName(pspContact.getFirstName());
            contactCdm.setMiddleName(pspContact.getMiddleName());
            contactCdm.setFamilyName(pspContact.getLastName());
            contactCdm.setFullName(pspContact.getFullName());
            contactCdm.setPrimaryPhone(createPhoneNumber(pspContact.getPhone()));
            contactCdm.setAlternatePhone(createPhoneNumber(pspContact.getSecondPhone()));
            contactCdm.setFax(createPhoneNumber(pspContact.getFax()));
            contactCdm.setCompanyName(pspCompany.getLegalName());

            if (contactRole == ContactRole.PayrollAdmin) {
                contactCdm.setContactType(ContactType.PAYROLL_ADMIN);
            } else if (contactRole == ContactRole.PrimaryPrincipal) {
                contactCdm.setContactType(ContactType.PRIMARY_PRINCIPAL);
            }

            String email = pspContact.getEmail();
            if (email != null) {
                EmailAddress emailAddress = new EmailAddress();
                emailAddress.setAddress(email);
                contactCdm.setPrimaryEmailAddress(emailAddress);
            }
        }
        return contactCdm;
    }

    private static TelephoneNumber createPhoneNumber(String pPhone) {
        if (pPhone == null) {
            return null;
        }

        TelephoneNumber phoneNumber = new TelephoneNumber();
        phoneNumber.setFreeFormNumber(pPhone);
        return phoneNumber;
    }

    public static PhysicalAddress createAddress(Address address) {
        if (address == null) {
            return null;
        }

        PhysicalAddress physicalAddress = new PhysicalAddress();
        physicalAddress.setId(address.getId().toString());
        physicalAddress.setLine1(address.getAddressLine1());
        physicalAddress.setLine2(address.getAddressLine2());
        physicalAddress.setLine3(address.getAddressLine3());
        physicalAddress.setCity(address.getCity());
        physicalAddress.setCountrySubDivisionCode(address.getState());
        physicalAddress.setPostalCode(address.getZipCode());
        physicalAddress.setCountry(address.getCountry());

        return physicalAddress;
    }

    public static List<TaxPaymentGroup> buildTaxPaymentTemplates(boolean showAllFrequencies, Company pspCompany) {
        List<TaxPaymentGroup> taxPaymentTemplates = new ArrayList<TaxPaymentGroup>();

        DomainEntitySet<CompanyAgency> companyAgencies = pspCompany.getCompanyAgencyCollection()
                                                                   .find(CompanyAgency.Agency().AgencyId().notIn(com.intuit.sbd.payroll.psp.adapters.ade.translators.CompanyTranslator.IGNORE_AGENCIES));

        for (CompanyAgency companyAgency : companyAgencies) {
            for (CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate : companyAgency.getCompanyAgencyPaymentTemplateCollection()) {
                // Below if condition is added to handle bad data. Really companyAgencyPaymentTemplates are added only for the payment templates under CompanyAgency Agency.
                if(companyAgencyPaymentTemplate.getPaymentTemplate().getAgency().equals(companyAgency.getAgency())) {
                    taxPaymentTemplates.add(CompanyTranslator.buildTaxPaymentGroup(companyAgency, companyAgencyPaymentTemplate, showAllFrequencies));
                }
            }
        }

        return taxPaymentTemplates;
    }

    public static void addDepositFrequencies(boolean showAllFrequencies, CompanyAgency companyAgency, CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate, TaxPaymentGroup taxPaymentTemplateCDM) {
        // If payment template uses frequency of another payment template, get the deposit frequencies of that payment template
        if (showAllFrequencies) {
            String paymentTemplateId = PaymentTemplate.getUsesFrequencyOf(companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd());
            companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(companyAgency.getCompany(), PaymentTemplate.findPaymentTemplate(paymentTemplateId));

            DomainEntitySet<EffectiveDepositFrequency> effectiveDepositFrequencies = companyAgencyPaymentTemplate.getEffectiveDepositFrequencyCollection().sort(EffectiveDepositFrequency.EffectiveDate());

            for (com.intuit.sbd.payroll.psp.domain.EffectiveDepositFrequency effectiveDepositFrequency : effectiveDepositFrequencies) {
                taxPaymentTemplateCDM.getDepositFrequencies().add(buildTaxDepositFrequency(effectiveDepositFrequency));
            }
        } else {
            SpcfCalendar today = PSPDate.getPSPTime().copy();
            CalendarUtils.clearTime(today);
            taxPaymentTemplateCDM.getDepositFrequencies().add(buildTaxDepositFrequency(companyAgencyPaymentTemplate.getPaymentTemplate().getEffectiveDepositFreq(companyAgency.getCompany(), today)));
        }
    }

    public static TaxDepositFrequency buildTaxDepositFrequency(EffectiveDepositFrequency effectiveDepositFrequency) {

        TaxDepositFrequency taxDepositFrequencyCDM = new TaxDepositFrequency();

        Date endDate = null;

        Date startDate = new Date(effectiveDepositFrequency.getEffectiveDate().getTimeInMilliseconds());

        if (effectiveDepositFrequency.getInvalidDate() != null) {
            try {
                endDate = new Date(effectiveDepositFrequency.getInvalidDate().getTimeInMilliseconds());
            } catch (Exception ignore) {
            }
        }

        taxDepositFrequencyCDM.setStartDate(startDate);
        taxDepositFrequencyCDM.setEndDate(endDate);
        taxDepositFrequencyCDM.setFrequency(FrequencyMapper.getComplainceFrequencyByDepositFrequencyCode(effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId()));
        return taxDepositFrequencyCDM;
    }

    public static void buildAdditionalFilingAmounts(boolean showAll, CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate, TaxPaymentGroup taxPaymentTemplateCDM) {
        Criterion<CompanyFilingAmount> criterion = CompanyFilingAmount.InvalidDate().isNull();

        if(!showAll) {
            criterion = criterion.And(CompanyFilingAmount.EffectiveDate()
                                                         .equalTo(CalendarUtils.getFirstDayOfQuarter(PSPDate.getPSPTime().getYear(), CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime()))));
        }

        for (CompanyFilingAmount companyFilingAmount : companyAgencyPaymentTemplate.getCompanyFilingAmountCollection().find(criterion)) {
            if(!companyFilingAmount.getAdditionalFilingAmount().getRate()) {
                taxPaymentTemplateCDM.getAdditionalFilingAmounts().add(buildAdditionalFilingAmount(companyFilingAmount));
            }
        }
    }

    private static AdditionalFilingAmount buildAdditionalFilingAmount(CompanyFilingAmount pCompanyFilingAmount) {
        AdditionalFilingAmount additionalFilingAmount = new AdditionalFilingAmount();
        additionalFilingAmount.setAmount(new BigDecimal(pCompanyFilingAmount.getAmount()).setScale(2, RoundingMode.HALF_UP));
        additionalFilingAmount.setEffectiveDate(new Date(pCompanyFilingAmount.getEffectiveDate().getTimeInMilliseconds()));
        additionalFilingAmount.setId(AdditionalFilingIdMapper.getComplianceAdditionalIdByPspAtfLawId(pCompanyFilingAmount.getAdditionalFilingAmount().getATFLawId()));
        additionalFilingAmount.setName(pCompanyFilingAmount.getName());
        return additionalFilingAmount;
    }

    public static TaxItem buildAdditionalFilingAmountTaxItem(CompanyAgencyPaymentTemplate agencyPaymentTemplate, CompanyFilingAmount companyFilingAmount, boolean pShowAllRates) {
        TaxItem taxItem = new TaxItem();
        taxItem.setId(AdditionalFilingIdMapper.getComplianceAdditionalIdByPspAtfLawId(companyFilingAmount.getAdditionalFilingAmount().getATFLawId()));
        taxItem.setName(companyFilingAmount.getAdditionalFilingAmount().getName());
        taxItem.setJurisdictionId(JurisdictionIdMapper.getComplianceJurisdictionId("US", companyFilingAmount.getAdditionalFilingAmount().getPaymentTemplate().getAgency().getJurisdiction().getJurisdictionID()));
        taxItem.setAgencyId(AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId(companyFilingAmount.getAdditionalFilingAmount().getPaymentTemplate().getAgency().getAgencyId()));
        taxItem.setTaxPaymentGroupId(TaxPaymentGroupIdMapper.getComplianceTaxPayGroupIdByPSPPaymentTemplateCd(companyFilingAmount.getAdditionalFilingAmount().getPaymentTemplate().getPaymentTemplateCd()));
        if (pShowAllRates) {
            DomainEntitySet<CompanyFilingAmount> companyFilingAmounts = Application.find(CompanyFilingAmount.class, new Query<CompanyFilingAmount>()
                                                                            .Where(CompanyFilingAmount.CompanyAgencyPaymentTemplate().equalTo(agencyPaymentTemplate)
                                                                                                      .And(CompanyFilingAmount.Name().equalTo(companyFilingAmount.getAdditionalFilingAmount().getName())))
                                                                            .OrderBy(CompanyFilingAmount.EffectiveDate().Descending()));
            for (CompanyFilingAmount filingAmount : companyFilingAmounts) {
                taxItem.getTaxRates().add(getTaxRate(filingAmount));
            }
        } else {
            taxItem.getTaxRates().add(getTaxRate(companyFilingAmount));
        }
        return taxItem;
    }

    public static TaxItem buildLawTaxItem(CompanyLaw pCompanyLaw, boolean pShowAllRates) {

        if (PayrollItemStatus.Inactive == pCompanyLaw.getFilingStatus()) {
            return null;
        }

        String taxItemCode;
        try {
            taxItemCode = SourceSystemLawAssoc.findSourceIdBySourceSystemAndLaw(SourceSystemCode.ADE, pCompanyLaw.getLaw());
        } catch (Exception e) {
            return null;
        }

        TaxItem taxItem = new TaxItem();
        taxItem.setJurisdictionId(JurisdictionIdMapper.getComplianceJurisdictionId("US", pCompanyLaw.getCompanyAgency().getAgency().getJurisdiction().getJurisdictionID()));
        taxItem.setAgencyId(AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId(pCompanyLaw.getCompanyAgency().getAgency().getAgencyId()));
        taxItem.setId(taxItemCode);
        taxItem.setIsReimbursable(pCompanyLaw.getReimbursableStatus() == ReimbursableStatus.Reimbursable || pCompanyLaw.getExemptionStatus() == LawStatus.Exempt);
        taxItem.setName(pCompanyLaw.getLaw().getDescription());
        taxItem.setTaxPaymentGroupId(TaxPaymentGroupIdMapper.getComplianceTaxPayGroupIdByPSPPaymentTemplateCd(pCompanyLaw.getLaw().getPaymentTemplate().getPaymentTemplateCd()));
        if (pShowAllRates) {
            for (CompanyLawRate companyTaxRate : pCompanyLaw.getCompanyLawRateCollection().sort(CompanyLawRate.EffectiveDate())) {
                taxItem.getTaxRates().add(getTaxRate(companyTaxRate));
            }
        } else {
            CompanyLawRate currentRate = pCompanyLaw.getCompanyLawRateCollection().find(CompanyLawRate.EffectiveDate().lessOrEqualThan(PSPDate.getPSPTime())
                                                                                                      .And(CompanyLawRate.InvalidDate().isNull()))
                                                    .sort(CompanyLawRate.EffectiveDate().Descending()).getFirst();
            if (currentRate != null) {
                taxItem.getTaxRates().add(getTaxRate(currentRate));
            }
        }
        return taxItem;
    }

    private static TaxRate getTaxRate(CompanyLawRate currentRate) {
        TaxRate rate = new TaxRate();
        BigDecimal roundedRate;
        if (currentRate.getRateType() == QbdtNumericType.Percentage) {
            roundedRate = BigDecimal.valueOf(currentRate.getRate() * 100);
        } else {
            roundedRate = BigDecimal.valueOf(currentRate.getRate());
        }
        if (roundedRate.scale() > MAX_RATE_SCALE) {
            roundedRate = roundedRate.setScale(MAX_RATE_SCALE, RoundingMode.HALF_UP).stripTrailingZeros();
        }
        rate.setRate(roundedRate);
        if (currentRate.getEffectiveDate() != null) {
            rate.setStartDate(new Date(currentRate.getEffectiveDate().getTimeInMilliseconds()));
        }
        if (currentRate.getInvalidDate() != null) {
            rate.setEndDate(new Date(currentRate.getInvalidDate().getTimeInMilliseconds()));
        }
        return rate;
    }

    private static TaxRate getTaxRate(CompanyFilingAmount currentRate) {
        TaxRate rate = new TaxRate();
        BigDecimal roundedRate;
        if (currentRate.getAdditionalFilingAmount().getRate()) {
            roundedRate = BigDecimal.valueOf(currentRate.getAmount() * 100);
        } else {
            roundedRate = BigDecimal.valueOf(currentRate.getAmount());
        }
        if (roundedRate.scale() > MAX_RATE_SCALE) {
            roundedRate = roundedRate.setScale(MAX_RATE_SCALE, RoundingMode.HALF_UP).stripTrailingZeros();
        }
        rate.setRate(roundedRate);
        if (currentRate.getEffectiveDate() != null) {
            rate.setStartDate(new Date(currentRate.getEffectiveDate().getTimeInMilliseconds()));
        }
        if (currentRate.getInvalidDate() != null) {
            rate.setEndDate(new Date(currentRate.getInvalidDate().getTimeInMilliseconds()));
        }
        return rate;
    }

    private static com.intuit.schema.payroll.v3.company.Company.AccountStatus getAccountStatus(Company company) {

        // Default to Terminated.
        com.intuit.schema.payroll.v3.company.Company.AccountStatus accountStatus = com.intuit.schema.payroll.v3.company.Company.AccountStatus.TERMINATED;

        TaxCompanyServiceInfo companyService = (TaxCompanyServiceInfo)company.getCompanyService(ServiceCode.Tax);
        if (companyService != null) {
            if (companyService.getStatusCd().equals(ServiceSubStatusCode.ActiveCurrent)) {
                accountStatus = com.intuit.schema.payroll.v3.company.Company.AccountStatus.ACTIVE;
            // Check to see if last quarter to file is in the future (format is YYYYQ)
            } else if (companyService.getStatusCd().equals(ServiceSubStatusCode.Cancelled) &&
                       companyService.getLastQuarterToFile() >= (PSPDate.getPSPTime().getYear() * 10) + CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime())) {
                accountStatus = com.intuit.schema.payroll.v3.company.Company.AccountStatus.TERMINATED_WITH_FORMS_NEEDED;
            } else if (company.getCurrentOnHoldReason(ServiceSubStatusCode.AS400Hold) != null) {
                accountStatus = com.intuit.schema.payroll.v3.company.Company.AccountStatus.ON_HOLD;
            }
        }

        return accountStatus;
    }

    public static TaxPaymentGroup buildTaxPaymentGroup(CompanyAgency pCompanyAgency, CompanyAgencyPaymentTemplate pCompanyAgencyPaymentTemplate, boolean showAllFrequenciesAndAmounts) {
        TaxPaymentGroup taxPaymentTemplateCDM = new TaxPaymentGroup();
        taxPaymentTemplateCDM.setId(TaxPaymentGroupIdMapper.getComplianceTaxPayGroupIdByPSPPaymentTemplateCd(pCompanyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd()));
        taxPaymentTemplateCDM.setName(pCompanyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateAbbrev());
        taxPaymentTemplateCDM.setJurisdictionId(JurisdictionIdMapper.getComplianceJurisdictionId("US", pCompanyAgency.getAgency().getJurisdiction().getJurisdictionID()));
        taxPaymentTemplateCDM.setAgencyId(AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId(pCompanyAgency.getAgency().getAgencyId()));
        taxPaymentTemplateCDM.setIsActive(pCompanyAgencyPaymentTemplate.hasActiveLaw(false));
        taxPaymentTemplateCDM.setTaxId(pCompanyAgencyPaymentTemplate.getAgencyTaxpayerId());
        CompanyTranslator.addDepositFrequencies(showAllFrequenciesAndAmounts, pCompanyAgencyPaymentTemplate.getCompanyAgency(), pCompanyAgencyPaymentTemplate, taxPaymentTemplateCDM);
        CompanyTranslator.buildAdditionalFilingAmounts(showAllFrequenciesAndAmounts, pCompanyAgencyPaymentTemplate, taxPaymentTemplateCDM);
        return taxPaymentTemplateCDM;
    }

    public static TaxFilingType buildTaxFilingType(CompanyAgencyFormTemplate pCompanyAgencyFormTemplate) {
        TaxFilingType filerType = new TaxFilingType();
        String formTemplateCd = pCompanyAgencyFormTemplate == null ? "" : pCompanyAgencyFormTemplate.getFormTemplate().getFormTemplateCd();
        if (FormTemplate.IRS_944.equals(formTemplateCd) || FormTemplate.IRS_941.equals(formTemplateCd)) {
            if (FormTemplate.IRS_944.equals(formTemplateCd)) {
                filerType.setFilingType(FilingTypeEnum.form944);
            } else if (FormTemplate.IRS_941.equals(formTemplateCd)) {
                filerType.setFilingType(FilingTypeEnum.form941);
            }
            //filerType.setAgencyName(AgencyIdMapper.getComplianceAgencyIdByPSPAgencyId(pCompanyAgencyFormTemplate.getCompanyAgency().getAgency().getAgencyId()));
            filerType.setAgencyName(pCompanyAgencyFormTemplate.getCompanyAgency().getAgency().getName());
            filerType.setStartDate(new Date(pCompanyAgencyFormTemplate.getEffectiveDate().getTimeInMilliseconds()));
            if (pCompanyAgencyFormTemplate.getInvalidDate() != null) {
                filerType.setEndDate(new Date(pCompanyAgencyFormTemplate.getInvalidDate().getTimeInMilliseconds()));
            }

        }
        return filerType;
    }

}

