package com.intuit.sbd.payroll.psp.gateways.amo;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 1/15/13
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IAMOWSGateway {

    public GetCustomerAssetResponseTypeDTO getCustomerAsset(String pLicenseNumber, String pEntitlementOfferingCode, IAMOGatewayListener pListener) throws Exception;

}
