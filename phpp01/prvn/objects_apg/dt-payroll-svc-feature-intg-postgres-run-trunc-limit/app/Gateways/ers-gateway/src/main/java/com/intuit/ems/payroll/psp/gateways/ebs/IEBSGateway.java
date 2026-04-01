package com.intuit.ems.payroll.psp.gateways.ebs;

/**
 * Created with IntelliJ IDEA.
 * User: ssaxena2
 * Date: 3/16/17
 * Time: 8:24 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IEBSGateway {
    void disableEntitlement(String pLicenseNumber, String pEOC)throws Exception;
}
