package com.intuit.sbd.payroll.psp.batchjobs.printedchecks.printedcheckfile;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.util.PrintConstants;
import com.intuit.sbd.payroll.psp.common.pgp.PgpWriter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.StringUtils;

import java.io.Writer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 5, 2011
 * Time: 4:45:24 PM
 */
public class PositivePayFile {
    private static final String EOL = "\n";
    private static final String HEADER = "HDR";
    private static final String TRAILER = "EOF";

    private static final String DATE_FORMAT = "yyMMdd";

    private static final String BANK_ACCOUNT_FORMAT = "0000000000";
    private static final String CHECK_NUMBER_FORMAT = "0000000000";
    private static final String CHECK_AMOUNT_FORMAT = "0000000000";
    private static final String TOTAL_AMOUNT_FORMAT = "000000000000";
    private static final String TOTAL_COUNT_FORMAT = "00000000";

    private static final int PAYEE_LENGTH = Math.min(PrintConstants.MAX_PAYEE_LENGTH, 40);

    private SimpleDateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);
    private DecimalFormat mCheckNumberFormat = new DecimalFormat(CHECK_NUMBER_FORMAT);
    private DecimalFormat mCheckAmountFormat = new DecimalFormat(CHECK_AMOUNT_FORMAT);
    private DecimalFormat mBankAccountFormat = new DecimalFormat(BANK_ACCOUNT_FORMAT);
    private DecimalFormat mTotalAmountFormat = new DecimalFormat(TOTAL_AMOUNT_FORMAT);
    private DecimalFormat mTotalCountFormat = new DecimalFormat(TOTAL_COUNT_FORMAT);

    private List<RecordHolder> mRecordHolders = new ArrayList<RecordHolder>();
    private SpcfDecimal mTotalAmount = SpcfMoney.ZERO;

    public int getCheckCount() {
        return mRecordHolders.size();
    }

    public void addHeader(PgpWriter pWriter, String pBankAccountNumber) throws Exception{
        pWriter.write(HEADER);
        pWriter.write(mBankAccountFormat.format(Integer.parseInt(pBankAccountNumber)));
        pWriter.write(mDateFormat.format(new Date(PSPDate.getPSPTime().getTimeInMilliseconds())));
        pWriter.write(EOL);
    }

    public void addTrailer(PgpWriter pWriter) throws Exception{
        pWriter.write(TRAILER);
        pWriter.write(mTotalAmountFormat.format(Long.parseLong(new SpcfMoney(mTotalAmount).toString().replace(".", ""))));
        pWriter.write(mTotalCountFormat.format(mRecordHolders.size()));
    }

    public void addDetailRecord(String pBankAccountNumber, String pCheckNumber, SpcfMoney pCheckAmount,
                                  SpcfCalendar pCheckDate, String pPayee1, String pPayee2, boolean pIsVoid) throws Exception {
        mTotalAmount = mTotalAmount.add(pCheckAmount);
        mRecordHolders.add(new RecordHolder(pBankAccountNumber, pCheckNumber, pCheckAmount, pCheckDate, pPayee1, pPayee2, pIsVoid));
    }

    public void writeDetailRecords(PgpWriter pWriter) throws Exception {
        Collections.sort(mRecordHolders);
        for (RecordHolder recordHolder : mRecordHolders) {
            writeDetailRecord(pWriter, recordHolder.getBankAccountNumber(), recordHolder.getCheckNumber(), recordHolder.getCheckAmount(),
                              recordHolder.getCheckDate(), recordHolder.getPayee1(), recordHolder.isVoid());
        }
    }

    private void writeDetailRecord(PgpWriter pWriter, String pBankAccountNumber, String pCheckNumber, SpcfMoney pCheckAmount,
                                  SpcfCalendar pCheckDate, String pPayee, boolean pIsVoid) throws Exception {
        if(pWriter == null || pCheckNumber == null || pCheckAmount == null ||
                pCheckDate == null || pPayee == null) {
            throw new RuntimeException("Error writing positive pay record. None of the parameters can be null. Check Number: " + pCheckNumber);
        }

        // account number  (Account number ......  pos:  1      length: 10)
        pWriter.write(mBankAccountFormat.format(Integer.parseInt(pBankAccountNumber)));

        // check number    (Serial number .......  pos: 11      length: 10)
        pWriter.write(mCheckNumberFormat.format(Long.parseLong(pCheckNumber)));

        // check amount (Amount ..............     pos: 21      length: 10)
        pWriter.write(mCheckAmountFormat.format(Long.parseLong(pCheckAmount.toString().replace(".", ""))));

        // issued date  (Effective date ......     pos: 31       length: 6     format: YYMMDD)
        pWriter.write(mDateFormat.format(new Date(pCheckDate.getTimeInMilliseconds())));

        // code         (Transaction code ....     pos: 37       length: 1)
        pWriter.write(pIsVoid ? "V" : " ");

        // additional   (Additional data ....      pos: 38       length: 15)
        pWriter.write(StringUtils.rightPad("", 15));

        // payee 1      (Payee name 1 ....         pos: 53       length: 40)
        pWriter.write(StringUtils.rightPad(truncateString(pPayee, PAYEE_LENGTH), 40));

        // payee 1.5    (Payee name 1.5 ....       pos: 93       length: 10)
        pWriter.write(StringUtils.rightPad("", 10));

        pWriter.write(EOL);
    }

    private String truncateString(String initalString, int maxCharacters) {
        if(initalString != null && initalString.length() > maxCharacters) {
            return initalString.substring(0, maxCharacters);
        }
        return initalString;
    }

    private class RecordHolder implements Comparable<RecordHolder> {
        String mBankAccountNumber;
        String mCheckNumber;
        SpcfMoney mCheckAmount;
        SpcfCalendar mCheckDate;
        String mPayee1;
        String mPayee2;
        boolean mIsVoid;

        private RecordHolder(String pBankAccountNumber, String pCheckNumber, SpcfMoney pCheckAmount, SpcfCalendar pCheckDate, String pPayee1, String pPayee2, boolean pIsVoid) {
            mBankAccountNumber = pBankAccountNumber;
            mCheckNumber = pCheckNumber;
            mCheckAmount = pCheckAmount;
            mCheckDate = pCheckDate;
            mPayee1 = pPayee1;
            mPayee2 = pPayee2;
            mIsVoid = pIsVoid;
        }

        public String getBankAccountNumber() {
            return mBankAccountNumber;
        }

        public String getCheckNumber() {
            return mCheckNumber;
        }

        public SpcfMoney getCheckAmount() {
            return mCheckAmount;
        }

        public SpcfCalendar getCheckDate() {
            return mCheckDate;
        }

        public String getPayee1() {
            return mPayee1;
        }

        public String getPayee2() {
            return mPayee2;
        }

        public boolean isVoid() {
            return mIsVoid;
        }

        public int compareTo(RecordHolder pRecordHolder) {
            if(getBankAccountNumber() == null && pRecordHolder.getBankAccountNumber() == null) {
                return  0;
            } else if(getBankAccountNumber() != null && pRecordHolder.getBankAccountNumber() == null) {
                return -1;
            } else if(getBankAccountNumber() == null && pRecordHolder.getBankAccountNumber() != null) {
                return 1;
            }

            if(getBankAccountNumber().equals(pRecordHolder.getBankAccountNumber())) {
                if(getCheckNumber() == null && pRecordHolder.getCheckNumber() == null) {
                    return  0;
                } else if(getCheckNumber() != null && pRecordHolder.getCheckNumber() == null) {
                    return -1;
                } else if(getCheckNumber() == null && pRecordHolder.getCheckNumber() != null) {
                    return 1;
                }

                return getCheckNumber().compareTo(pRecordHolder.getCheckNumber());
            }

            return getBankAccountNumber().compareTo(pRecordHolder.getBankAccountNumber());
        }
    }
}
