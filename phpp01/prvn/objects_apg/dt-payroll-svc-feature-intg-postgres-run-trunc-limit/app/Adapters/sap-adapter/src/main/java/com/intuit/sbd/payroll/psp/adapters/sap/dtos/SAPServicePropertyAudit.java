package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Feb 24, 2010
 * Time: 12:42:22 PM
 */
public class SAPServicePropertyAudit extends SAPPropertyAudit {
    private String serviceCd;

    public String getServiceCd() {
        return serviceCd;
    }

    public void setServiceCd(String pServiceCd) {
        serviceCd = pServiceCd;
    }
}
