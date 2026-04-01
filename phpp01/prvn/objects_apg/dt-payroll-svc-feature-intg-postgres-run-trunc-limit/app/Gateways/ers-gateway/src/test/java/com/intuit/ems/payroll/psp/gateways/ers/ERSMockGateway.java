package com.intuit.ems.payroll.psp.gateways.ers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 20, 2010
 * Time: 2:42:52 PM
 */
public class ERSMockGateway implements IERSGateway {
    private static List<ActivateEntitlementRequest> mActivateEntitlementRequests = new ArrayList<ActivateEntitlementRequest>();
    private static List<DeactivateEntitlementUnitRequest> mDeactivateEntitlementUnitRequests = new ArrayList<DeactivateEntitlementUnitRequest>();
    private static List<CancelEntitlementsRequest> mCancelEntitlementsRequests = new ArrayList<CancelEntitlementsRequest>();

    private static EntitlementInfoDTO mEntitlementDTO;

    public static void setEntitlementDTO(EntitlementInfoDTO pEntitlementDTO) {
        mEntitlementDTO = pEntitlementDTO;
    }

    public static List<ActivateEntitlementRequest> getActivateEntitlementRequests() {
        return mActivateEntitlementRequests;
    }

    public static List<DeactivateEntitlementUnitRequest> getDeactivateEntitlementUnitRequests() {
        return mDeactivateEntitlementUnitRequests;
    }

    public static List<CancelEntitlementsRequest> getCancelEntitlementsRequests() {
        return mCancelEntitlementsRequests;
    }

    public void activateEntitlement(String pLicenseNumber, String pEntitlementOfferingCode, String pEIN, boolean pIsReactivation, IERSGatewayListener pListener) throws Throwable {
        mActivateEntitlementRequests.add(WrapperFactory.generateActivateEntitlementRequest(pLicenseNumber, pEntitlementOfferingCode, pEIN, pIsReactivation));
    }

    public void deactivateEntitlementUnit(String pLicenseNumber, String pEntitlementOfferingCode, String pEIN, IERSGatewayListener pListener) throws Throwable {
        mDeactivateEntitlementUnitRequests.add(WrapperFactory.generateDeactivateEntitlementUnitRequest(pLicenseNumber, pEntitlementOfferingCode, pEIN));
    }

    public void disableEntitlement(String pLicenseNumber, String pEOC, IERSGatewayListener pListener) throws Throwable {
        mCancelEntitlementsRequests.add(WrapperFactory.generateCancelEntitlementRequest(pLicenseNumber, pEOC));
    }

    public EntitlementInfoDTO getEntitlementInfo(String pLicenseNumber, String pEOC, boolean pIncludeDisabled, IERSGatewayListener pListener) throws Throwable {
        return mEntitlementDTO;
    }
}
