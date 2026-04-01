package com.intuit.sbd.payroll.psp.gateways.amo;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 27, 2010
 * Time: 2:04:33 PM
 */
public class EntitlementTransfer {
    public EntitlementTransfer(String pSourceLicenseNumber, String pTargetLicenseNumber) {
        sourceLicenseNumber = pSourceLicenseNumber;
        targetLicenseNumber = pTargetLicenseNumber;
    }

    public String sourceLicenseNumber;
    public String targetLicenseNumber;
}
