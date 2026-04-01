package com.intuit.ems.payroll.psp.gateways.ers;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 20, 2010
 * Time: 1:52:09 PM
 */
public class ERSConnectionExceptionGateway implements IERSGateway {

    private static EntitlementInfoDTO mEntitlementDTO;

    public static void setEntitlementDTO(EntitlementInfoDTO pEntitlementDTO) {
        mEntitlementDTO = pEntitlementDTO;
    }

    private int companyCount = 0;
    public void activateEntitlement(String pLicenseNumber, String pEntitlementOfferingCode, String pEIN, boolean pIsReactivation, IERSGatewayListener pListener) throws Throwable {
        companyCount++;
        if(companyCount == 5) {
            throw new ERSConnectionException("Simulate a connection timeout");
        }
    }

    public void deactivateEntitlementUnit(String pLicenseNumber, String pEntitlementOfferingCode, String pEIN, IERSGatewayListener pListener) throws Throwable {
        companyCount++;
        if(companyCount == 5) {
            throw new ERSConnectionException("Simulate a connection timeout");
        }
    }

    public void disableEntitlement(String pLicenseNumber, String pEOC, IERSGatewayListener pListener) throws Throwable {
        companyCount++;
        if(companyCount == 5) {
            throw new ERSConnectionException("Simulate a connection timeout");
        }
    }

    public EntitlementInfoDTO getEntitlementInfo(String pLicenseNumber, String pEOC, boolean pIncludeDisabled, IERSGatewayListener pListener) throws Throwable {
        return mEntitlementDTO;
    }
}
