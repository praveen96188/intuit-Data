package com.intuit.sbd.payroll.psp.agency.util.edi813;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.fixedlen.RecordTemplate;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2011
 * Time: 8:49:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class Edi813DtmSegment {
    private Date mFileSendDate;

    public Edi813DtmSegment(RecordTemplate pTemplate) {
        mFileSendDate = EftpsUtil.getDateFromShortDateString(pTemplate.getFieldValue(FieldId.EDI_813_SEG_DTM02));
    }

    public Date getFileSendDate() {
        return mFileSendDate;
    }

    public void setFileSendDate(final Date pFileSendDate) {
        mFileSendDate = pFileSendDate;
    }
}
