package com.intuit.sbd.payroll.psp.agency.util.edi813;

import com.paycycle.eftpsBp.FieldId;
import com.paycycle.fixedlen.RecordTemplate;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2011
 * Time: 8:50:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class Edi813RefSegmentOuter {
    private String mRefNumQual;
    private String mRefNum;

    public Edi813RefSegmentOuter(RecordTemplate pTemplate) {
        mRefNumQual = pTemplate.getFieldValue(FieldId.EDI_813_SEG_OUTER_REF01);
        mRefNum = pTemplate.getFieldValue(FieldId.EDI_813_SEG_OUTER_REF02);
    }

    public String getRefNumQual() {
        return mRefNumQual;
    }

    public void setRefNumQual(final String pRefNumQual) {
        mRefNumQual = pRefNumQual;
    }

    public String getRefNum() {
        return mRefNum;
    }

    public void setRefNum(final String pRefNum) {
        mRefNum = pRefNum;
    }
}
