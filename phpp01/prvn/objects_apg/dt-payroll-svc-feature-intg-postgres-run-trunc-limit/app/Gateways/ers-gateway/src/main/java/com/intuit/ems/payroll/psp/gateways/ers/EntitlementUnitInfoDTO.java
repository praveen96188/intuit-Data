package com.intuit.ems.payroll.psp.gateways.ers;

import com.intuit.sbd.payroll.psp.domain.EntitlementUnitStatusCode;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 8/2/12
 * Time: 1:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class EntitlementUnitInfoDTO {

    private String mFedTaxId;
    private EntitlementUnitStatusCode mEntitlementUnitStatusCode;

    public String getFedTaxId() {
        return mFedTaxId;
    }

    public void setFedTaxId(String pFedTaxId) {
        this.mFedTaxId = pFedTaxId;
    }

    public EntitlementUnitStatusCode getEntitlementUnitStatusCode() {
        return mEntitlementUnitStatusCode;
    }

    public void setEntitlementUnitStatusCode(EntitlementUnitStatusCode pEntitlementUnitStatusCode) {
        this.mEntitlementUnitStatusCode = pEntitlementUnitStatusCode;
    }
}
