package com.intuit.ems.payroll.psp.gateways.ers;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 21, 2010
 * Time: 2:16:06 PM
 */
public class ERSExceptionGateway implements IERSGateway {

    private static EntitlementInfoDTO mEntitlementDTO;

    public static void setEntitlementDTO(EntitlementInfoDTO pEntitlementDTO) {
        mEntitlementDTO = pEntitlementDTO;
    }

    public void activateEntitlement(String pLicenseNumber, String pEntitlementOfferingCode, String pEIN, boolean pIsReactivation, IERSGatewayListener pListener) throws Throwable {
        if(Integer.parseInt(pEIN) == 5) {
            throw new RuntimeException("Simulate an exception thrown by an unexpected error");
        }
    }

    public void deactivateEntitlementUnit(String pLicenseNumber, String pEntitlementOfferingCode, String pEIN, IERSGatewayListener pListener) throws Throwable {
        if(Integer.parseInt(pEIN) == 6) {
            throw new RuntimeException("Simulate an exception thrown by an unexpected error");
        }
    }

    private int companyCount = 0;
    public void disableEntitlement(String pLicenseNumber, String pEOC, IERSGatewayListener pListener) throws Throwable {
        companyCount++;
        if(companyCount == 5) {
            throw new RuntimeException("Simulate an exception thrown by an unexpected error");
        }
    }

    public EntitlementInfoDTO getEntitlementInfo(String pLicenseNumber, String pEOC, boolean pIncludeDisabled, IERSGatewayListener pListener) throws Throwable {
        companyCount++;
        if(companyCount == 5) {
            throw new RuntimeException("Simulate an exception thrown by an unexpected error");
        }
        return mEntitlementDTO;
    }
}
