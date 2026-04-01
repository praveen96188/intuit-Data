package com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.factory;

import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto.AddressDTO;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto.CompanyDTO;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto.ContactDTO;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto.EntitlementDTO;
import com.intuit.sbd.payroll.psp.domain.*;

import java.util.Date;

/**
 * Author: Sriram Nutakki
 * Date created: 8/19/13
 */
public class WorkersCompFactory {

    public static CompanyDTO createCompany(Company pCompany, String fein) {

        // Company basic info
        CompanyDTO wcCompany = createCompanyAndContact(pCompany);

        // Entitlements
        if (pCompany != null) {
            for (EntitlementUnit entitlementUnit : pCompany.getEntitlementUnitCollection()) {
                if (entitlementUnit.getFedTaxId().equalsIgnoreCase(fein)) {
                    EntitlementDTO entitlement = createEntitlement(entitlementUnit);
                    if (entitlement != null && entitlementUnit.getFedTaxId().equalsIgnoreCase(fein)) {
                        wcCompany.addEntitlement(entitlement);
                    }
                }
            }
        }

        return wcCompany;
    }

    public static CompanyDTO createCompany(Company pCompany, String fein, String subsNum) {

        // Company basic info
        CompanyDTO wcCompany = createCompanyAndContact(pCompany);

        // Entitlements
        if (pCompany != null) {
            for (EntitlementUnit entitlementUnit : pCompany.getEntitlementUnitCollection()) {
                if (entitlementUnit.getFedTaxId().equalsIgnoreCase(fein) &&
                        entitlementUnit.getEntitlement().getSubscriptionNumber().equalsIgnoreCase(subsNum))
                {
                    EntitlementDTO entitlement = createEntitlement(entitlementUnit);
                    if (entitlement != null) {
                        wcCompany.addEntitlement(entitlement);
                    }
                }
            }
        }

        return wcCompany;
    }

    public static CompanyDTO createCompany(Company pCompany) {

        // Company basic info
        CompanyDTO wcCompany = createCompanyAndContact(pCompany);

        // Entitlements
        if (pCompany != null) {
            for (EntitlementUnit entitlementUnit : pCompany.getEntitlementUnitCollection()) {
                EntitlementDTO entitlement = createEntitlement(entitlementUnit);
                if (entitlement != null) {
                    wcCompany.addEntitlement(entitlement);
                }
            }
        }

        return wcCompany;
    }

    private static CompanyDTO createCompanyAndContact(Company pCompany) {
        CompanyDTO wcCompany = new CompanyDTO();
        if (pCompany != null) {
            // Company basic info
            wcCompany.setEin(pCompany.getFedTaxId());
            wcCompany.setCompanyLegalName(pCompany.getLegalName());
            wcCompany.setCompanyName(pCompany.getDbaName());
            wcCompany.setPsid(pCompany.getSourceCompanyId());

            // Contact
            for(com.intuit.sbd.payroll.psp.domain.Contact pContact : pCompany.getContactCollection()) {
                if (pContact.getContactRoleCd() == ContactRole.PayrollAdmin) {
                    ContactDTO contact = createContact(pContact);
                    wcCompany.setContact(contact);
                    break;
                }
            }
            AddressDTO address = createAddress(pCompany.getLegalAddress());
            if (address != null) {
                wcCompany.setAddress(address);
            }
        }
        addLastPayrollRun(pCompany,wcCompany);

        return wcCompany;
    }

    public static ContactDTO createContact(com.intuit.sbd.payroll.psp.domain.Contact contact) {
        ContactDTO wcContact = new ContactDTO();
        if (contact != null) {
            wcContact.setPhone(contact.getPhone());
            wcContact.setEmail(contact.getEmail());
            wcContact.setName(contact.getFullName());
            wcContact.setFirstName(contact.getFirstName()) ;
            wcContact.setLastName(contact.getLastName());
        }

        return wcContact;

    }

    public static AddressDTO createAddress(com.intuit.sbd.payroll.psp.domain.Address address) {
        AddressDTO wcAddress = new AddressDTO();
        if (address != null) {
            wcAddress.setAddressLine1(address.getAddressLine1());
            wcAddress.setAddressLine2(address.getAddressLine2());
            wcAddress.setAddressLine3(address.getAddressLine3());
            wcAddress.setCity(address.getCity());
            wcAddress.setState(address.getState());
            wcAddress.setZipCode(address.getZipCode());
            wcAddress.setZipExtension(address.getZipCodeExtension());
            wcAddress.setCountry(address.getCountry());
        }

        return wcAddress;
    }

    public static EntitlementDTO createEntitlement(EntitlementUnit entitlementUnit) {
        EntitlementDTO entitlement = null;
        if (entitlementUnit != null
                && entitlementUnit.getEntitlement() != null
                && entitlementUnit.getEntitlement().getEntitlementCode() != null) {
            EntitlementCode entitlementCode = entitlementUnit.getEntitlement().getEntitlementCode();
            EntitlementStateCode entitlementStateCode = entitlementUnit.getEntitlement().getEntitlementState();
            entitlement = new EntitlementDTO();
            if (entitlementStateCode.equals(EntitlementStateCode.Enabled)) {
                if (entitlementUnit.getEntitlementUnitStatus() != null) {
                    entitlement.setActive(
                            entitlementUnit.getEntitlementUnitStatus().equals(EntitlementUnitStatusCode.Activated));
                } else {
                    entitlement.setActive(entitlementStateCode.equals(EntitlementStateCode.Enabled));
                }
            } else {
                entitlement.setActive(entitlementStateCode.equals(EntitlementStateCode.Enabled));
            }

            entitlement.setPrimary(
                    entitlementCode.getIsPrimary());
            entitlement.setAssetItemCode(
                    entitlementCode.getAssetItemCd() != null ? entitlementCode.getAssetItemCd().name() : "");
            entitlement.setEditionType(
                    entitlementCode.getEditionType() != null ? entitlementCode.getEditionType().name() : "");
            entitlement.setSubscriptionNumber(entitlementUnit.getEntitlement().getSubscriptionNumber());
        }
        return entitlement;
    }

    /**
     * @param pCompany
     * @param pwcCompany
     */
    public static void addLastPayrollRun(Company pCompany, CompanyDTO pwcCompany) {
        PayrollRun payrollRun = PayrollRun.findLastPayrollRunWithActivePaychecks(pCompany);
        if (payrollRun != null) {
            pwcCompany.setLastPaycheckDate(new Date(payrollRun.getPaycheckDate().getTimeInMilliseconds()));
            pwcCompany.setLastPayrollRunDate(new Date(payrollRun.getPayrollRunDate().getTimeInMilliseconds()));
        }
    }

}
