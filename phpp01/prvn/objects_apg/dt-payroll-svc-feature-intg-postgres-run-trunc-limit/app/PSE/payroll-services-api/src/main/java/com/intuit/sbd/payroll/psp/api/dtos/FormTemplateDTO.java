package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.FormTemplate;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: svenkata
 * Date: Jan 21, 2011
 * Time: 10:51:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class FormTemplateDTO {

    private SpcfCalendar iEffectiveDate;
    private String iFilerType;

    public SpcfCalendar getEffectiveDate() {
        return iEffectiveDate;
    }

    public void setEffectiveDate(SpcfCalendar iEffectiveDate) {
        this.iEffectiveDate = iEffectiveDate;
    }

    public String getFilerType() {
        return iFilerType;
    }

    public void setFilerType(String iFilerType) {
        this.iFilerType = iFilerType;
    }

    public boolean is941944() {
        return getFilerType().equals(FormTemplate.IRS_941) || getFilerType().equals(FormTemplate.IRS_944);
    }
}
