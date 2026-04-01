package com.intuit.sbd.payroll.psp.agency.util.edi813;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.fixedlen.RecordTemplate;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2011
 * Time: 8:48:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class Edi813BtiSegment {
    private Date mFileCreateDate;
    private String mBulkProviderTin;

    public Edi813BtiSegment(RecordTemplate pTemplate) {
        mFileCreateDate = EftpsUtil.getDateFromShortDateString(pTemplate.getFieldValue(FieldId.EDI_813_SEG_BTI05));
        mBulkProviderTin = pTemplate.getFieldValue(FieldId.EDI_813_SEG_BTI08);
    }

    public Date getFileCreateDate() {
        return mFileCreateDate;
    }

    public void setFileCreateDate(final Date pFileCreateDate) {
        mFileCreateDate = pFileCreateDate;
    }

    public String getBulkProviderTin() {
        return mBulkProviderTin;
    }

    public void setBulkProviderTin(final String pBulkProviderTin) {
        mBulkProviderTin = pBulkProviderTin;
    }
}
