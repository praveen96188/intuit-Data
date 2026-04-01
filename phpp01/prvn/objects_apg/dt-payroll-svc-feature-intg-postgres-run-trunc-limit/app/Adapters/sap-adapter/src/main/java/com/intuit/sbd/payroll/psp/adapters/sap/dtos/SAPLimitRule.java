package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 5/3/12
 * Time: 7:53 AM 
 */
public class SAPLimitRule {
    private String mId;
    private String mDescription;
    private String mSourceSystem;

    public String getId() {
        return mId;
    }

    public void setId(String pId) {
        mId = pId;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String pDescription) {
        mDescription = pDescription;
    }

    public String getSourceSystem() {
        return mSourceSystem;
    }

    public void setSourceSystem(String pSourceSystem) {
        mSourceSystem = pSourceSystem;
    }
}
