package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.agency.util.EdiFileRecord;
import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.domain.EdiFileStatus;
import com.intuit.sbd.payroll.psp.domain.EdiFileType;
import com.intuit.sbd.payroll.psp.domain.StateEdiTaxFile;
import com.intuit.sbd.payroll.psp.domain.SystemOwnerType;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.fixedlen.RecordListener;
import com.paycycle.fixedlen.RecordTemplate;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 24, 2011
 * Time: 10:47:20 PM
 * Processes  ACK files (EDI 997) from VAN.
 */

public class StateAckFile extends StateEDIFile implements RecordListener {

    protected static final SpcfLogger logger = SpcfLogManager.getLogger(StateAckFile.class);

    protected EDIRecordTemplate mAk1RecordTemplate;
    protected EDIRecordTemplate mAk2RecordTemplate;
    protected EDIRecordTemplate mAk5RecordTemplate;
    protected EDIRecordTemplate mAk9RecordTemplate;
    protected EDIRecordTemplate mAkURecordTemplate;
    protected int mAcceptCount = 0;
    protected int mRejectCount = 0;
    protected int mRecordCount;
    protected EftpsEdiType mEdiAckType;

    public StateAckFile() {
        mAk1RecordTemplate = getRecordTemplate(RecordId.EDI_997_SEG_AK1);
        mAk2RecordTemplate = getRecordTemplate(RecordId.EDI_997_SEG_AK2);
        mAk5RecordTemplate = getRecordTemplate(RecordId.EDI_997_SEG_AK5_4010);
        mAk9RecordTemplate = getRecordTemplate(RecordId.EDI_997_SEG_AK9);

        // This is used for flushing unwanted AK segments from the file. Ex - AK3, AK4 - details are not used
        mAkURecordTemplate = getRecordTemplate(RecordId.EDI_SEG_GENERIC);

        addReadRecordListener(this);

    }

    public int getAcceptCount() {
        return mAcceptCount;
    }

    public int getRejectCount() {
        return mRejectCount;
    }

    public int getRecordCount() {
        return mRecordCount;
    }

    @Override
    public EftpsEdiType getEdiFileType() {
        return EftpsEdiType.EDI997;
    }

    /**
     * This method will determine the type of this acknowledgement file based upon the file it is acknowledging.
     * (i.e. if we're ack-ing an 824 file (EftpsPaymentResponse), then this method will return EftpsPaymentResponseAck)
     * @return The type of this ack file.
     */
    @Override
    public EdiFileType getEftpsFileType() {
        EdiFileType fileType = null;

        switch (mEdiAckType) {
            case EDI813:
                fileType = EdiFileType.StateEdiPaymentAck;     // Ack for 813 file
                break;
        }

        return fileType;
    }

    @Override
    protected int readContent() {
        int count = 0;

        mAcceptCount = 0;
        mRejectCount = 0;
        mRecordCount = 0;

        // read the ISA header record
        readRecord(getISAHeaderTemplate());

        // read the GS header record
        readRecord(getGSHeaderTemplate());

        while (true) {
            String stSeg = peekNextSegmentCode();

            if ((stSeg == null) || (stSeg.startsWith("GE"))) {
                // There are no more transaction sets in the file.
                break;
            }

            if (!stSeg.startsWith("ST")) {
                throw new EftpsBpRuntimeException("Expecting ST segment, found: " + stSeg);
            }

            // read the ST header record
            readRecord(getSTHeaderTemplate());

            // read the AK1 record
            readRecord(mAk1RecordTemplate);

            while (true) {
                String akSeg = peekNextSegmentCode();

                if ((akSeg == null) || (akSeg.startsWith("AK9"))) {
                    // No more acks in the file
                    break;
                }

                if (akSeg.startsWith("AK2")) {
                    // read the AK2 record
                    readRecord(mAk2RecordTemplate);
                } else if (akSeg.startsWith("AK5")) {
                    // read the AK5 record
                    readRecord(mAk5RecordTemplate);
                } else {
                    // read the unwanted record
                    readRecord(mAkURecordTemplate);
                }

                ++count;
            }

            // read the AK9 record
            readRecord(mAk9RecordTemplate);

            // read the SE trailer record
            readRecord(getSETrailerTemplate());
        }

        // read the GE trailer record
        readRecord(getGETrailerTemplate());

        // read the IEA trailer record
        readRecord(getIEATrailerTemplate());

        // Changing the File Name to encrypted file because the unencrypted file will be deleted after processing.
        EdiFileRecord ediFileRecord = getEdiFileRecord();
        ediFileRecord.getEftpsFile().setFileName(getEdiFileRecord().getEdiFile().getFileName()+".pgp");

        // complete the EftpsFile record
        ediFileRecord.completeRecord();

        return count;
    }

    public void recordCreated(RecordTemplate template) {
        try {
            if (template == mAk2RecordTemplate) {
                mEdiAckType = EftpsEdiType.getValueByEdiType(mAk2RecordTemplate.getFieldInt(FieldId.EDI_997_SEG_AK201));

                // get the file id of the file we're acknowledging (AK102 contains GS06 of file being acked)
                int fileId = mAk1RecordTemplate.getFieldInt(FieldId.EDI_997_SEG_AK102);

                StateEdiTaxFile stateEdiTaxFile = StateEdiTaxFile.getStateEdiTaxFileByFileId(fileId);

                if(stateEdiTaxFile != null) {
                    stateEdiTaxFile.setAcknowledgementFile(getEdiFileRecord().getEftpsFile());

                    // Copy parent record System Owner
                    getEdiFileRecord().setSystemOwner(stateEdiTaxFile.getSystemOwner());
                    Application.save(stateEdiTaxFile);
                } else {

                    // Copy parent record System Owner
                    getEdiFileRecord().setSystemOwner(SystemOwnerType.AS400); // AS400 Files - parent file is not present in PSP

                    switch (mEdiAckType) {
                        case EDI813: // Direction: from VAN -to- PSP -to- AS400
                            getEdiFileRecord().setCompletionStatus(EdiFileStatus.SendToAS400);
                            break;
                    }
                }


            } else if (template == mAk5RecordTemplate) {
                ++mRecordCount;

                String transSetAckCode = mAk5RecordTemplate.getFieldValue(FieldId.EDI_997_SEG_AK501);

                if ("A".equals(transSetAckCode)) {
                    ++mAcceptCount;
                } else {
                    ++mRejectCount;

                    // This is bad, it means we have a logic problem in our code that generates
                    // files and the file with this ST set was either generated wrong, or
                    // somehow got corrupted in transmission.  There isn't anything we can do to
                    // fix it, so just log the error and move on.  Support should have a look.

                    int gsCtrlNum = mAk1RecordTemplate.getFieldInt(FieldId.EDI_997_SEG_AK102);
                    int stCtrlNum = mAk2RecordTemplate.getFieldInt(FieldId.EDI_997_SEG_AK202);

                    String err = String.format("In the State EDI %s file with GS06 Ctrl Num %d, the Transaction Set " +
                            "with ST02 Ctrl Num %d was rejected (ack file: %s, record: %s).",
                            mEdiAckType, gsCtrlNum, stCtrlNum, getDetailedFileName(), template.getName());

                    logger.error("STATE EDI ACK FILE REJECT: " + err);
                }
            }
        } catch (Throwable t) {
            String err = String.format("Error while reading ACK record (ack file: %s, record: %s) ",
                    getDetailedFileName(), template.getName());
            throw new RuntimeException(err, t);
        }
    }    
}
