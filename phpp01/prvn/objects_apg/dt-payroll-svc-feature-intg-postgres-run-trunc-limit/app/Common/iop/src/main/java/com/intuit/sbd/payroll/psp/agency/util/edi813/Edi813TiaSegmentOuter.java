package com.intuit.sbd.payroll.psp.agency.util.edi813;

import com.paycycle.eftpsBp.FieldId;
import com.paycycle.fixedlen.RecordTemplate;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jan 19, 2011
 * Time: 8:50:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class Edi813TiaSegmentOuter {
    private BigDecimal mSegmentTaxAmount;

    public Edi813TiaSegmentOuter(RecordTemplate pTemplate) {
        mSegmentTaxAmount = new BigDecimal(pTemplate.getFieldValue(FieldId.EDI_813_SEG_OUTER_TIA02)).scaleByPowerOfTen(-2);
    }

    public BigDecimal getSegmentTaxAmount() {
        return mSegmentTaxAmount;
    }

    public void setSegmentTaxAmount(final BigDecimal pSegmentTaxAmount) {
        mSegmentTaxAmount = pSegmentTaxAmount;
    }
}
