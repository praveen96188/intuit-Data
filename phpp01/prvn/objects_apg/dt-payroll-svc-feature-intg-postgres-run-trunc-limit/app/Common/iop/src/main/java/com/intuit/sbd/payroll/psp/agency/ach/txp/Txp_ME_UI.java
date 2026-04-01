package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.eft.FieldId;
import com.paycycle.util.StringUtil;


@SuppressWarnings("unused")
public class Txp_ME_UI extends TxpRecordManager {
    private static final SpcfLogger logger = Application.getLogger(Txp_ME_UI.class);
    public Txp_ME_UI() {
        super("/pay-def-me-ui.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        StringBuilder msg = new StringBuilder();
        String taxId = getAgencyId(pEdr);
        if (taxId == null || taxId.isEmpty()) {
            msg.append("ME state UI tax id is empty for PSID : ").append(pEdr.getCompany().getSourceCompanyId());
            logger.warn(msg.toString());
        } else {
            taxId = taxId.trim();
        }
        getTxpTemplate().setFieldValue(FieldId.TAXPAYER_EIN, taxId);
        getTxpTemplate().setFieldValue(FieldId.TAX_TYPE_CODE, pEdr.getMoneyMovementTransaction().getPaymentFrequency().getTaxCodeId());
        getTxpTemplate().setFieldValue(FieldId.TAX_PERIOD_END_DATE, getEndDate(pEdr));
        getTxpTemplate().setFieldValue(FieldId.TAX_AMOUNT, formatAmount(pEdr.getAmount(), 8));

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
