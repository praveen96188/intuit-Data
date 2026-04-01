package com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers;

import com.intuit.sbd.payroll.psp.common.ofx.response.ICOINFOMOD;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 20, 2011
 * Time: 8:08:52 AM
 */
public class CompanyResponse {
    private ICOINFOMOD mICOINFOMOD;

    public ICOINFOMOD getICOINFOMOD() {
        return mICOINFOMOD;
    }

    public CompanyResponse(Company pCompany) {
        mICOINFOMOD = new ICOINFOMOD();

        mICOINFOMOD.setILEGALNAME(QBOFX.convertNULLToEmptyString(pCompany.getLegalName()));
        String fein = pCompany.getFedTaxId();
        if(fein != null && fein.length() > 2) {
            fein = fein.substring(0, 2) + "-" + fein.substring(2, fein.length());
        }
        mICOINFOMOD.setIFEIN(QBOFX.convertNULLToEmptyString(fein));

        if(pCompany.getLegalAddress() != null) {
            Address address = pCompany.getLegalAddress();
            mICOINFOMOD.setIADDR1(QBOFX.convertNULLToEmptyString(address.getAddressLine1()));
            mICOINFOMOD.setIADDR2(QBOFX.convertNULLToEmptyString(address.getAddressLine2()));
            mICOINFOMOD.setICITY(QBOFX.convertNULLToEmptyString(address.getCity()));
            mICOINFOMOD.setISTATE(QBOFX.convertNULLToEmptyString(address.getState()));
            String zipCode = address.getZipCode();
            if (address.getZipCodeExtension() != null) {
                zipCode += address.getZipCodeExtension();
            }
            mICOINFOMOD.setIPOSTALCODE(QBOFX.convertNULLToEmptyString(zipCode));

        }

        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(pCompany);
        if(companyBankAccount != null) {
            mICOINFOMOD.setIACCTNAME(companyBankAccount.getSourceBankAccountName());
        }

        if(pCompany.getActivePrimaryEntitlementUnit() != null) {
            EntitlementUnit entitlementUnit = pCompany.getActivePrimaryEntitlementUnit();
            mICOINFOMOD.setISERVICEKEY(QBOFX.convertNULLToEmptyString(entitlementUnit.getServiceKey()));
            mICOINFOMOD.setISUBTYPE(entitlementUnit.getEntitlement().getEntitlementCode().getQuickBooksSubtype() + "");
        }

        // QTR to start is not set until the balance file is received
        CompanyService companyService = pCompany.getCompanyService(ServiceCode.Tax);
        if(companyService != null && companyService.isActive()) {
            mICOINFOMOD.setIDTFILEQTRSTART(QBOFX.convertToOFXDate(companyService.getServiceStartDate()));
            mICOINFOMOD.setITAXREADY(QBOFX.Y_N(true));
        } else {
            mICOINFOMOD.setITAXREADY(QBOFX.Y_N(false));
        }
    }
}
