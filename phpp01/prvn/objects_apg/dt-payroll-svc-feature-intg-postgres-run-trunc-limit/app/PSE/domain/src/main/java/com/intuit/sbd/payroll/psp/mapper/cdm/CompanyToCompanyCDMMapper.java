package com.intuit.sbd.payroll.psp.mapper.cdm;

import com.intuit.payroll.api.company.model.*;
import com.intuit.payroll.api.shared.model.AddressSubCDM;
import com.intuit.payroll.api.shared.model.BankAccountSubCDM;
import com.intuit.payroll.api.shared.model.EmployerFlavor;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import com.intuit.sbd.payroll.psp.domain.EventDetailTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("com.intuit.sbd.payroll.psp.mapper.cdm.CompanyToCompanyCDMMapper")
public class CompanyToCompanyCDMMapper extends BeanMapper<Company, CompanyCDM>{

    @Override
    public CompanyCDM mapToTarget(Company company, Class<CompanyCDM> companyCDMClass) {
        CompanyCDM companyCDM = new CompanyCDM();
        companyCDM.setFlavor(EmployerFlavor.QBDT);
        companyCDM.setLegalName(company.getLegalName());
        companyCDM.setTaxIdentificationNumber(company.getFedTaxId());
        companyCDM.setBusinessName(company.getDbaName());
        companyCDM.setSignupDate(SpcfCalendar.toDateTime(company.getSignUpDate()));
        companyCDM.setBackOfficeId(Long.valueOf(company.getSourceCompanyId()));
//        companyCDM.setPreFundDays(company.getFundingModel());
        companyCDM.setEntityVersion(String.valueOf(company.getVersion()));
        companyCDM.setAccountantWholesale(false);
        companyCDM.setLegalAddress(getMapper().mapToTarget(company.getLegalAddress(), AddressSubCDM.class));
        companyCDM.setBusinessAddress(getMapper().mapToTarget(company.getLegalAddress(),AddressSubCDM.class));
        companyCDM.setMainAddress(getMapper().mapToTarget(company.getMailingAddress(),AddressSubCDM.class));
        companyCDM.setPayrollCompanyId(company.getSourceCompanyId());

        addIndustryCode(company, companyCDM);
        addContact(company, companyCDM);
        addRealmId(company, companyCDM);
        addBankAccounts(company,companyCDM);
        addHoldStatuses(company,companyCDM);
        addStatus(company,companyCDM);
        addPaymentLiabilityType(companyCDM);
        return companyCDM;
    }

    private void addIndustryCode(Company company, CompanyCDM companyCDM) {
        if (Objects.nonNull(company.getCompanyAdditionalInfo())
                && Objects.nonNull(company.getCompanyAdditionalInfo().getIndustryType())){
            companyCDM.setIndustryCode(company.getCompanyAdditionalInfo().getIndustryType().getStandardIndustryCode());
        }
    }

    private void addContact(Company company, CompanyCDM companyCDM) {
        if (CollectionUtils.isEmpty(company.getContactCollection())) {
            return;
        }
        Contact primaryContact = this.getContactByRoleCheck(company, ContactRole.PrimaryPrincipal);
        Contact contactAdmin = this.getContactByRoleCheck(company, ContactRole.PayrollAdmin);
        ContactCDM contactcdm = null;
        if (primaryContact != null) {
            contactcdm = getMapper().mapToTarget(primaryContact,ContactCDM.class);
        }
        if (contactcdm != null && contactAdmin != null) {
            contactcdm.setWorkPhone(contactAdmin.getPhone());
        }
        if (contactcdm != null) {
            companyCDM.setContact(contactcdm);
        } else {
            companyCDM.setContact(getMapper().mapToTarget(company.getContactCollection().get(0),ContactCDM.class));
        }
    }

    private void addRealmId(Company company, CompanyCDM companyCDM) {
        if (Objects.isNull(company.getIAMRealmId()))
            return;

        companyCDM.setId(company.getIAMRealmId());
        SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
        spcfCalendar.addHours(-1);
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.RealmIdUpdated,null,spcfCalendar,null,true);
        if (CollectionUtils.isEmpty(companyEvents))
            return;

        for (CompanyEvent companyEvent: companyEvents) {
            Map<EventDetailTypeCode, String> eventDetailInfoMap = companyEvent.getEventDetailInfo();
            if (Objects.nonNull(eventDetailInfoMap.get(EventDetailTypeCode.NewStringValue))
                && eventDetailInfoMap.get(EventDetailTypeCode.NewStringValue).contains(company.getIAMRealmId())){
                companyCDM.setPreviousRealmId(eventDetailInfoMap.get(EventDetailTypeCode.OldStringValue));
                return;
            }
        }
    }

    private void addPaymentLiabilityType(CompanyCDM companyCDM) {
        companyCDM.setPaymentLiabilityType(PaymentLiabilityType.CLIENT);
    }

    private void addHoldStatuses(Company company, CompanyCDM companyCDM) {
        DomainEntitySet<OnHoldReason> onHoldReasons = company.getCurrentOnHoldReasonsDomainEntitySet();
        if (CollectionUtils.isEmpty(onHoldReasons))
            return;

        List<HoldStatusCDM> holdStatusCDMs = new ArrayList<>();
        for (OnHoldReason onHoldReason : onHoldReasons) {
            ServiceSubStatusCode serviceSubStatusCode = onHoldReason.getOnHoldReasonCd();
            if (Objects.isNull(serviceSubStatusCode))
                continue;
            HoldStatusCDM holdStatusCDM = getMapper().mapToTarget(serviceSubStatusCode,HoldStatusCDM.class);
            if (holdStatusCDM.getHoldName() != null) {
                holdStatusCDMs.add(holdStatusCDM);
            }
        }
        companyCDM.setHoldStatuses(holdStatusCDMs);

    }

    private void addBankAccounts(Company company, CompanyCDM companyCDM) {
        // Map sensitized version of bank accounts
        DomainEntitySet<CompanyBankAccount> companyBankAccounts = company.getCompanyBankAccountCollection();
        if (CollectionUtils.isEmpty(companyBankAccounts))
            return;

        List<BankAccountSubCDM> bankAccountSubCDMs = new ArrayList<BankAccountSubCDM>();
        for (CompanyBankAccount companyBankAccount :companyBankAccounts) {
            // TODO: filter out inactive bank account
            bankAccountSubCDMs.add(getMapper().mapToTarget(companyBankAccount,BankAccountSubCDM.class));
        }
        companyCDM.setBankAccounts(bankAccountSubCDMs);
    }

    private void addStatus(Company company, CompanyCDM companyCDM) {
        EntitlementUnit entitlementUnit = company.getActivePrimaryEntitlementUnit();
        if(Objects.nonNull(entitlementUnit)
                && EntitlementStateCode.Enabled.equals(entitlementUnit.getEntitlement().getEntitlementState())){
            companyCDM.setStatus(CompanyStatusType.ACTIVE);
            return;
        }
        companyCDM.setStatus(CompanyStatusType.INACTIVE);
    }

    private Contact getContactByRoleCheck(Company company, ContactRole role) {
        if (CollectionUtils.isEmpty(company.getContactCollection()))
            return null;

        DomainEntitySet<Contact> contacts = company.getContactCollection();
        Optional<Contact> contactO = contacts.stream().filter(contact -> Objects.nonNull(contact.getContactRoleCd())
                && contact.getContactRoleCd().equals(role)).findFirst();
        if (contactO.isPresent()){
            return contactO.get();
        }
        return null;
    }

}