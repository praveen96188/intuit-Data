package com.intuit.sbd.payroll.psp.agency.ach.txp;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.paycycle.eft.FieldId;
import java.math.BigDecimal;
/**
 * MA withholding for TXP output
 */

// Instantiated by class name stored in payment template.
@SuppressWarnings("unused")
public class Txp_MA_PFML extends TxpRecordManager {
    public Txp_MA_PFML() {
        super("/pay-def-ma-pfml.xml");
    }

    public void createTxpRecord(EntryDetailRecord pEdr) {

        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, getWithoutHyphens(getAgencyId(pEdr))+" ");
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_FEIN,getWithoutSpecChars(pEdr.getCompany().getLegalName()));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(),8));
        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
