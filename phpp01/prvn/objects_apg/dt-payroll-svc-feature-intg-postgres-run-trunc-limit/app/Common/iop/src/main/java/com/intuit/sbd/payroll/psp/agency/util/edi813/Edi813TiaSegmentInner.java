package com.intuit.sbd.payroll.psp.agency.util.edi813;

import com.paycycle.eftpsBp.FieldId;
import com.paycycle.fixedlen.RecordTemplate;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2011
 * Time: 8:52:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class Edi813TiaSegmentInner {
    private String mTaxInfoIdNum;
    private BigDecimal mTaxAmount;

    public Edi813TiaSegmentInner(RecordTemplate pTemplate) {
        mTaxInfoIdNum = pTemplate.getFieldValue(FieldId.EDI_813_SEG_INNER_TIA01);
        mTaxAmount = new BigDecimal(pTemplate.getFieldValue(FieldId.EDI_813_SEG_INNER_TIA02)).scaleByPowerOfTen(-2);
    }

    public String getTaxInfoIdNum() {
        return mTaxInfoIdNum;
    }

    public void setTaxInfoIdNum(final String pTaxInfoIdNum) {
        mTaxInfoIdNum = pTaxInfoIdNum;
    }

    public BigDecimal getTaxAmount() {
        return mTaxAmount;
    }

    public void setTaxAmount(final BigDecimal pTaxAmount) {
        mTaxAmount = pTaxAmount;
    }
}
