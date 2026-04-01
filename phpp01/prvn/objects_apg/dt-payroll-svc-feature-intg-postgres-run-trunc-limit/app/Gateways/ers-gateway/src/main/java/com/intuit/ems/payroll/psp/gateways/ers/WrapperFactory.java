package com.intuit.ems.payroll.psp.gateways.ers;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;

import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 19, 2010
 * Time: 1:27:26 PM
 */
public class WrapperFactory {
    private static final String SALES_ORGANIZATION = "EMS";
    private static final String SCHEME_NAME = "ERS";

    public static ActivateEntitlementRequest generateActivateEntitlementRequest(String pLicenseNumber,
                                                                                String pEOC,
                                                                                String pEIN,
                                                                                boolean isReactivation) throws Exception {
        ActivateEntitlementRequest activateEntitlementRequest = new ActivateEntitlementRequest();
        activateEntitlementRequest.setTransactionInfo(generateTransactionInfoType());
        activateEntitlementRequest.setEntitlementId(generateEntitlementIdType(pEOC));
        activateEntitlementRequest.setLicenseId(generateLicenseIdType(pLicenseNumber));
        activateEntitlementRequest.setResourceValueId(pEIN);

        if(isReactivation) {
            // No, this is not a typo... the ERS guys told me to do this... doesn't make sense, but whatever
            activateEntitlementRequest.setReactivationOnly(false);
        }

        return activateEntitlementRequest;
    }

    public static DeactivateEntitlementUnitRequest generateDeactivateEntitlementUnitRequest(String pLicenseNumber,
                                                                                            String pEOC,
                                                                                            String pEIN) throws Exception {
        DeactivateEntitlementUnitRequest deactivateEntitlementUnitRequest = new DeactivateEntitlementUnitRequest();
        deactivateEntitlementUnitRequest.setEntitlementId(generateEntitlementIdType(pEOC));
        deactivateEntitlementUnitRequest.setLicenseId(generateLicenseIdType(pLicenseNumber));
        deactivateEntitlementUnitRequest.setTransactionInfo(generateTransactionInfoType());

        DeactivateEntitlementUnitRequest.EntitlementUnit entitlementUnit = new DeactivateEntitlementUnitRequest.EntitlementUnit();
        entitlementUnit.setResourceValueId(pEIN);
        deactivateEntitlementUnitRequest.setEntitlementUnit(entitlementUnit);

        DeactivateEntitlementUnitRequest.Validation validation = new DeactivateEntitlementUnitRequest.Validation();
        validation.setGenerateValidationCode(false);
        deactivateEntitlementUnitRequest.setValidation(validation);

        return deactivateEntitlementUnitRequest;
    }

    public static CancelEntitlementsRequest generateCancelEntitlementRequest(String pLicenseNumber, String pEOC) throws Exception {
        CancelEntitlementsRequest cancelEntitlementsRequest = new CancelEntitlementsRequest();
        cancelEntitlementsRequest.setTransactionInfo(generateTransactionInfoType());
        cancelEntitlementsRequest.setLicenseId(generateLicenseIdType(pLicenseNumber));
        cancelEntitlementsRequest.getEntitlementId().add(generateEntitlementIdType(pEOC));
        return cancelEntitlementsRequest;
    }

    public static GetEntitlementInformationAndPropertyDetailsRequest generateGetEntitlementInformationAndPropertyDetailsRequest(String pLicenseNumber, String pEOC, boolean pIncludeDisabled) throws Exception {
        GetEntitlementInformationAndPropertyDetailsRequest getEntitlementInformationAndPropertyDetailsRequest = new GetEntitlementInformationAndPropertyDetailsRequest();
        getEntitlementInformationAndPropertyDetailsRequest.setTransactionInfo(generateTransactionInfoType());
        getEntitlementInformationAndPropertyDetailsRequest.setLicenseId(generateLicenseIdType(pLicenseNumber));
        GetEntitlementInformationAndPropertyDetailsRequest.EntitlementOffering entitlementOffering = new GetEntitlementInformationAndPropertyDetailsRequest.EntitlementOffering();
        entitlementOffering.setEntitlementOfferingCode(pEOC);
        entitlementOffering.setIncludeDisabled(pIncludeDisabled);
        getEntitlementInformationAndPropertyDetailsRequest.setEntitlementOffering(entitlementOffering);
        getEntitlementInformationAndPropertyDetailsRequest.setEntitlementSynchronization(false);
        return getEntitlementInformationAndPropertyDetailsRequest;
    }

    public static EntitlementIdType generateEntitlementIdType(String pEOC) {
        EntitlementIdType entitlementIdType = new EntitlementIdType();
        entitlementIdType.setSchemeName(SCHEME_NAME);
        entitlementIdType.setValue(pEOC);
        return entitlementIdType;
    }

    public static LicenseIdType generateLicenseIdType(String pLicenseNumber) {
        LicenseIdType licenseIdType = new LicenseIdType();
        licenseIdType.setSchemeName(SCHEME_NAME);
        licenseIdType.setValue(pLicenseNumber);
        return licenseIdType;
    }

    public static TransactionInfoType generateTransactionInfoType() throws Exception {
        TransactionInfoType transactionInfoType = new TransactionInfoType();
        transactionInfoType.setSalesOrganization(SALES_ORGANIZATION);
        transactionInfoType.setTransactionDatetime(SpcfUtils.convertSpcfCalendarToXmlGregorianCalendar(PSPDate.getPSPTime()));
        transactionInfoType.setTransactionId(UUID.randomUUID().toString());
        return transactionInfoType;
    }
}
