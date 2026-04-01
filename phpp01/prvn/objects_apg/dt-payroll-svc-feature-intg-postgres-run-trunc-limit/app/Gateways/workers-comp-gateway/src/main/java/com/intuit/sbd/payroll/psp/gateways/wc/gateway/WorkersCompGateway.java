package com.intuit.sbd.payroll.psp.gateways.wc.gateway;

/**
 * Author: Sriram Nutakki
 * Date created: 10/23/12
 */
public interface WorkersCompGateway {

    public void getSubscriptionChangesFromWC();

    public void pushPayrollDataToWC();

    public String getDisplayDataForHelpDesk(String sourceSystemCd, String sourceCompanyId);

    public void pushCompanyChanges() throws Exception;

}
