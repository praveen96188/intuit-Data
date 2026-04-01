package com.intuit.sbd.payroll.psp.api.dtos;

/**
 * Created by aagarwal14 on 11/10/2016.
 */
public class PstubStateTaxInfoDTO {
    private String mAgencyId;
    private String mAgencyName;


    public PstubStateTaxInfoDTO() {
    }

    public String getAgencyId() {
        return mAgencyId;
    }

    public void setAgencyId(String pAgencyId) {
        mAgencyId = pAgencyId;
    }

    public String getAgencyName() {
        return mAgencyName;
    }

    public void setAgencyName(String pAgencyName) {
        mAgencyName = pAgencyName;
    }

}
