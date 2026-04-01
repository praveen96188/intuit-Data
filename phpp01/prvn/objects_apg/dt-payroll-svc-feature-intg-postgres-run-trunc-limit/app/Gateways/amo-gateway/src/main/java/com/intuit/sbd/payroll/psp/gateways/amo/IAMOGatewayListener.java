package com.intuit.sbd.payroll.psp.gateways.amo;

/**
 * Created with IntelliJ IDEA.
 * User: jjones1
 * Date: 1/15/13
 * Time: 3:21 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IAMOGatewayListener {
    public void onRequest(String pTransmissionId, String pRequest);
    public void onResponse(String pTransmissionId, String pResponse);
}
