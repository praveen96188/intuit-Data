package com.intuit.ems.payroll.psp.gateways.ers;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 13, 2010
 * Time: 1:13:42 PM
 */
public interface IERSGateway {
    public void activateEntitlement(String pLicenseNumber, String pEntitlementOfferingCode, String pEIN, boolean pIsReactivation, IERSGatewayListener pListener) throws Throwable;
    public void deactivateEntitlementUnit(String pLicenseNumber, String pEntitlementOfferingCode, String pEIN, IERSGatewayListener pListener) throws Throwable;    
    public void disableEntitlement(String pLicenseNumber, String pEOC, IERSGatewayListener pListener) throws Throwable;
    public EntitlementInfoDTO getEntitlementInfo(String pLicenseNumber, String pEOC, boolean pIncludeDisabled, IERSGatewayListener pListener) throws Throwable;
}
