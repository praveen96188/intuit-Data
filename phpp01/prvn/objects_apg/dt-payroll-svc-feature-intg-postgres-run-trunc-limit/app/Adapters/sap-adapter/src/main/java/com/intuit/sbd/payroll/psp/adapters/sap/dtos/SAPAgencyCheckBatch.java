package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: mwaqarbaig
 * Date: Aug 2, 2011
 * Time: 10:40:09 AM
 */
public class SAPAgencyCheckBatch extends SAPCheckPrintingBatch {
    private String mTemplateNameLine1;
    private String mTemplateNameLine2;
    private Date mInitiationDate;
    private boolean isSuperCheck;

    public String getTemplateNameLine1() {
        return mTemplateNameLine1;
    }

    public void setTemplateNameLine1(String pTemplateNameLine1) {
        mTemplateNameLine1 = pTemplateNameLine1;
    }

    public String getTemplateNameLine2() {
        return mTemplateNameLine2;
    }

    public void setTemplateNameLine2(String pTemplateNameLine2) {
        mTemplateNameLine2 = pTemplateNameLine2;
    }

    public Date getInitiationDate() {
        return mInitiationDate;
    }

    public void setInitiationDate(Date pInitiationDate) {
        mInitiationDate = pInitiationDate;
    }

    public boolean getIsSuperCheck() {
        return isSuperCheck;
    }

    public void setIsSuperCheck(boolean superCheck) {
        isSuperCheck = superCheck;
    }
}
