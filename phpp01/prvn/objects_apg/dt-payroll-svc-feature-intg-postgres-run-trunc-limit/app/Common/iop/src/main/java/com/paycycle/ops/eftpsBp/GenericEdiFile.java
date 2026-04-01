package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.domain.EdiFileType;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;

/**
 * Created by IntelliJ IDEA.
 * User: svenkata
 * Date: Dec 10, 2010
 * Time: 11:41:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class GenericEdiFile extends EftpsEDIFile {
    protected EDIRecordTemplate mAk1Record;
    protected EDIRecordTemplate mAk2Record;
    protected EftpsEdiType mEdiType;
    protected EftpsEdiType mResponseForFileType;
    protected EdiFileType mEdiFileType;

    public GenericEdiFile() {
        mAk1Record = getRecordTemplate(RecordId.EDI_997_SEG_AK1);
        mAk2Record = getRecordTemplate(RecordId.EDI_997_SEG_AK2);
    }

    @Override
    public String getDetailedFileName() {
        return getFileName();
    }

    @Override
    public EftpsEdiType getEdiFileType() {
        return mEdiType;
    }

    @Override
    public EdiFileType getEftpsFileType() {
        if (mEdiType == null) {
            throw new RuntimeException("Unable to determine EDI file type.");
        }

        switch (mEdiType) {
            case EDI151:
                mEdiFileType = EdiFileType.EftpsPaymentResponse;
                break;
            case EDI813:
                mEdiFileType = EdiFileType.EftpsPayment;
                break;
            case EDI821:
                mEdiFileType = EdiFileType.EftpsForecast;
                break;
            case EDI824:
                mEdiFileType = EdiFileType.EftpsEnrollmentResponse;
                break;
            case EDI826:
                mEdiFileType = EdiFileType.EftpsPaymentConfirmation;
                break;
            case EDI827:
                mEdiFileType = EdiFileType.EftpsPaymentReturn;
                break;
            case EDI838:
                mEdiFileType = EdiFileType.EftpsEnrollment;
                break;
            case EDI997:
                switch (mResponseForFileType) {
                    case EDI151:
                        mEdiFileType = EdiFileType.EftpsPaymentResponseAck;     // Ack for 151 file
                        break;
                    case EDI813:
                        mEdiFileType = EdiFileType.EftpsPaymentAck;             // Ack for 813 file
                        break;
                    case EDI821:
                        mEdiFileType = EdiFileType.EftpsForecastAck;            // Ack for 821 file
                        break;
                    case EDI824:
                        mEdiFileType = EdiFileType.EftpsEnrollmentResponseAck;  // Ack for 824 file
                        break;
                    case EDI826:
                        mEdiFileType = EdiFileType.EftpsPaymentConfirmationAck; // Ack for 826 file
                        break;
                    case EDI827:
                        mEdiFileType = EdiFileType.EftpsPaymentReturnAck;       // Ack for 827 file
                        break;
                    case EDI838:
                        mEdiFileType = EdiFileType.EftpsEnrollmentAck;
                        break;
                }
        }

        return mEdiFileType;
    }

    @Override
    protected int readContent() {
        try {
            // read the ISA header record
            readRecord(getISAHeaderTemplate());

            // read the GS header record
            readRecord(getGSHeaderTemplate());

            String stSeg = peekNextSegmentCode();

            if (stSeg == null || !stSeg.startsWith("ST")) {
                throw new EftpsBpRuntimeException("Expecting at least one ST segment, found: " + stSeg);
            }

            // read the ST header record
            readRecord(getSTHeaderTemplate());

            mEdiType = EftpsEdiType.getValueByEdiType(getSTHeaderTemplate().getFieldInt(FieldId.EDI_SEG_ST01));

            if (EftpsEdiType.EDI997.equals(mEdiType)) {
                // read the AK1 record
                readRecord(mAk1Record);

                // read the AK2 record
                readRecord(mAk2Record);

                mResponseForFileType = EftpsEdiType.getValueByEdiType(mAk2Record.getFieldInt(FieldId.EDI_997_SEG_AK201));
            }
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException(String.format("Problem reading EDI file: %s ", getFileName()), t);
        }

        return 1;
    }
}
