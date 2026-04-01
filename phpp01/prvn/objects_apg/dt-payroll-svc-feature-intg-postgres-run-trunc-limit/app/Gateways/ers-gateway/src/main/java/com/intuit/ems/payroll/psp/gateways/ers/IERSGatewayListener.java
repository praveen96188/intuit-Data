package com.intuit.ems.payroll.psp.gateways.ers;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Aug 29, 2011
 * Time: 2:56:17 PM
 */
public interface IERSGatewayListener {
    public void onRequest(String pTransmissionId, String pRequest);
    public void onResponse(String pTransmissionId, String pResponse);
}
