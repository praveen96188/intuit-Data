package com.intuit.sbd.payroll.psp.agency.ach.txp;

import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: May 23, 2011
 * Time: 12:40:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class Txp_OR_LOCAL extends TxpRecordManager {
    public Txp_OR_LOCAL() {
        super("/pay-def-or.xml");
    }

    /**
     * Build the EDI TXP record and save it to the given EDR.
     * @param pEdr The EDR that will own the TXP record.
     */
    public void createTxpRecord(EntryDetailRecord pEdr) {
        //
        // TODO: Build the TXP record
        //

        pEdr.setTxpRecordData(getTxpTemplate().toString());
    }
}
