package com.intuit.sbd.payroll.psp.util;

import com.intuit.sbd.payroll.psp.domain.CreditDebitCode;
import com.intuit.sbd.payroll.psp.domain.EntryDetailRecord;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: May 18, 2011
 * Time: 1:00:45 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ITxpRecordManager {
    public void createTxpRecord(EntryDetailRecord pEdr);
    public void createTxpRecord(EntryDetailRecord pEdr, FinancialTransaction pFinancialTransaction,
                                CreditDebitCode pCreateRecordDataForCredit);
}
