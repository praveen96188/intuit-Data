package com.intuit.sbd.payroll.psp.agency.util.edi813;

import com.paycycle.eftpsBp.FieldId;
import com.paycycle.fixedlen.RecordTemplate;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2011
 * Time: 8:52:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class Edi813RefSegmentInner {
    private int mTransactionId;

    public Edi813RefSegmentInner(RecordTemplate pTemplate) {
        mTransactionId = pTemplate.getFieldInt(FieldId.EDI_813_SEG_INNER_REF02);
    }

    public int getTransactionId() {
        return mTransactionId;
    }

    public void setTransactionId(final int pTransactionId) {
        mTransactionId = pTransactionId;
    }
}
