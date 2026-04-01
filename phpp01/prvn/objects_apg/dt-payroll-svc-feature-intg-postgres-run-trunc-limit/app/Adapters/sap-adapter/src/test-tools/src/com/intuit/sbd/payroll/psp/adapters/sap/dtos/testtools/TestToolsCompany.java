package com.intuit.sbd.payroll.psp.adapters.sap.dtos.testtools;

import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPCompany;

/**
 * Created by IntelliJ IDEA.
 * User: cyoder
 * Date: Mar 10, 2009
 * Time: 8:46:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestToolsCompany extends SAPCompany {
    private String gseq;
    private String offloadCd;

    public String getOffloadCd() {
        return offloadCd;
    }

    public void setOffloadCd(String offloadCd) {
        this.offloadCd = offloadCd;
    }

    public String getGseq() {
        return gseq;
    }

    public void setGseq(String gseq) {
        this.gseq = gseq;
    }
}
