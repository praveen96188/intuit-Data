package com.intuit.sbd.payroll.psp.entity.processor;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.entity.EntityContext;
import org.hibernate.event.spi.AbstractEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CompanyEntityProcessor extends BaseEntityProcessor<Company> {

    private final List<Class> interestedEntityClasses;
    private final Map<String, String> attributesCdmMap;

    @Autowired
    public CompanyEntityProcessor(EntityProcessorUtility entityProcessorUtility) {
        super(entityProcessorUtility);
        interestedEntityClasses = Arrays.asList(Company.class,Address.class,
                Contact.class,EntitlementUnit.class,
                CompanyBankAccount.class,OnHoldReason.class,
                CompanyService.class, BPCompanyServiceInfo.class, CDCompanyServiceInfo.class,
                DDCompanyServiceInfo.class, TaxCompanyServiceInfo.class,
                RACompanyServiceInfo.class, ThirdParty401kCompanyServiceInfo.class
                );
        attributesCdmMap = new HashMap(){{
            put("IAMRealmId","Id");
            put("LegalName","LegalName");
            put("FedTaxId","TaxIdentificationNumber");
            put("DbaName", "BusinessName");
            put("SignUpDate", "SignupDate");
            put("SourceCompanyId", "BackOfficeId");

            put("Address", "Address");
            put("Address.City", "Address");
            put("Address.Country", "Address");
            put("Address.State", "Address");
            put("Address.ZipCode", "Address");
            put("Address.AddressLine1", "Address");
            put("Address.AddressLine2", "Address");
            put("Address.AddressLine3", "Address");

            put("Contact", "Contact");
            put("Contact.FirstName", "Contact");
            put("Contact.MiddleName", "Contact");
            put("Contact.LastName", "Contact");
            put("Contact.Title", "Contact");
            put("Contact.Phone", "Contact");
            put("Contact.SecondPhone", "Contact");
            put("Contact.Email", "Contact");

            put("LegalAddress", "LegalAddress");
            put("MailingAddress", "MailingAddress");

            put("CompanyBankAccount", "BackAccount");
            put("CompanyBankAccount.AccountTypeCd", "BankAccount");
            put("CompanyBankAccount.AccountNumberEnc", "BankAccount");
            put("CompanyBankAccount.Status", "BankAccount");

            put("PayrollFrequency", "PreFundDays");
            put("OnHoldReason", "HoldStatuses");

            put("EntitlementUnit", "Status");
            put("EntitlementUnit.EntitlementUnitStatus", "Status");

            /*
            There is no corresponding field in CompanyCDM but we would like to listen to the
            CompanyService Status change and publish the CompanyCDM entity
             */
            put("CompanyService", "Status");
            put("CompanyService.StatusCd", "Status");
            put("BPCompanyServiceInfo", "Status");
            put("BPCompanyServiceInfo.StatusCd", "Status");
            put("CDCompanyServiceInfo", "Status");
            put("CDCompanyServiceInfo.StatusCd", "Status");
            put("DDCompanyServiceInfo", "Status");
            put("DDCompanyServiceInfo.StatusCd", "Status");
            put("TaxCompanyServiceInfo", "Status");
            put("TaxCompanyServiceInfo.StatusCd", "Status");
            put("RACompanyServiceInfo", "Status");
            put("RACompanyServiceInfo.StatusCd", "Status");
            put("ThirdParty401kCompanyServiceInfo", "Status");
            put("ThirdParty401kCompanyServiceInfo.StatusCd", "Status");

        }};
    }

    @Override
    public List<Class> getInterestedEntities() {
        return interestedEntityClasses;
    }

    @Override
    public Set<String> getAttributeFilters() {
        return attributesCdmMap.keySet();
    }

    @Override
    protected String getCdmAttributeName(String pspAttribute) {
        return attributesCdmMap.get(pspAttribute);
    }

    @Override
    public EntityContext<Company> process(AbstractEvent abstractEvent) {
        return createEntityContext(abstractEvent);
    }

    @Override
    public Class<?> getEntityType() {
        return Company.class;
    }

    @Override
    protected Company getCompany(Company entity) {
        return entity;
    }

    @Override
    public Company getEntity(Object entity) {
        if (entity instanceof Company) {
            return (Company) entity;
        } else if (entity instanceof EntitlementUnit) {
            return ((EntitlementUnit) entity).getCompany();
        } else if (entity instanceof CompanyBankAccount) {
            return ((CompanyBankAccount) entity).getCompany();
        } else if (entity instanceof Contact) {
            return ((Contact) entity).getCompany();
        } else if (entity instanceof OnHoldReason) {
            return ((OnHoldReason) entity).getCompany();
        } else if (entity instanceof Address) {
            Address address = (Address) entity;
            Company company = null;
            try {
                DomainEntitySet<Company> companyDomainEntitySet = address.findCompanyForAddress();
                if (Objects.isNull(companyDomainEntitySet) || Objects.isNull(companyDomainEntitySet.get(0)))
                    return null;
                company = companyDomainEntitySet.get(0);
            } catch (NoSuchElementException e){
                return null;
            }
            if(Objects.nonNull(company.getLegalAddress())) {
                company.getLegalAddress().getId().equals(address.getId());
                return company;
            }
            if(Objects.nonNull(company.getMailingAddress())) {
                company.getMailingAddress().getId().equals(address.getId());
                return company;
            }
        } else if (entity instanceof CompanyService) {
            return ((CompanyService) entity).getCompany();
        }
        return null;
    }

}
