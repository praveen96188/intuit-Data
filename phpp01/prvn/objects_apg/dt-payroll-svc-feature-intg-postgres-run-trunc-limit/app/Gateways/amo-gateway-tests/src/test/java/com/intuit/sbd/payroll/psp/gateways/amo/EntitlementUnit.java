package com.intuit.sbd.payroll.psp.gateways.amo;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 15, 2010
 * Time: 8:39:18 AM
 */
public class EntitlementUnit {
    // entitlement unit statuses
    public static String ACTIVATED = "Activated";
    public static String DEACTIVATED = "Deactivated";

    public EntitlementUnit(String pEin, String pStatus) {
        ein = pEin;
        status = pStatus;
    }

    public String ein;
    public String status;
}
