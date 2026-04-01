/**
 * AckFile.java
 *
 * Copyright (c) 2007 PayCycle, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * PayCycle, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with PayCycle.
 *
 * PAYCYCLE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. PAYCYCLE SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */

package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.agency.util.EdiFileRecord;
import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.domain.EdiFileStatus;
import com.intuit.sbd.payroll.psp.domain.EdiFileType;
import com.intuit.sbd.payroll.psp.domain.EftpsFile;
import com.intuit.sbd.payroll.psp.domain.SystemOwnerType;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.fixedlen.RecordListener;
import com.paycycle.fixedlen.RecordTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes and generates ACK files (EDI 997) from and to EFTPS.
 */
public class AckFile extends EftpsEDIFile implements RecordListener {
    protected static final SpcfLogger logger = SpcfLogManager.getLogger(AckFile.class);

    protected EDIRecordTemplate mAk1RecordTemplate;
    protected EDIRecordTemplate mAk2RecordTemplate;
    protected EDIRecordTemplate mAk5RecordTemplate;
    protected EDIRecordTemplate mAk9RecordTemplate;
    protected EDIRecordTemplate mAkURecordTemplate;
    protected int mAcceptCount = 0;
    protected int mRejectCount = 0;
    protected int mRecordCount;
    protected String mAckGsCtrlNum; // as string (no int conversion) to keep integrity of source GS ctrl num (i.e. 001234)
    protected EftpsEdiType mEdiAckType;
    protected List<AckInfo> mAcksToWriteList = new ArrayList<AckInfo>();

    static class AckInfo {
        int mTransSetIdCode;
        String mTransSetCtrlNum;

        AckInfo(EftpsEdiType pEdiType, String pStCtrlNum) {
            mTransSetIdCode = pEdiType.value();
            mTransSetCtrlNum = pStCtrlNum;
        }
    }

    public AckFile() {
        mAk1RecordTemplate = getRecordTemplate(RecordId.EDI_997_SEG_AK1);
        mAk2RecordTemplate = getRecordTemplate(RecordId.EDI_997_SEG_AK2);
        mAk5RecordTemplate = getRecordTemplate(RecordId.EDI_997_SEG_AK5);
        mAk9RecordTemplate = getRecordTemplate(RecordId.EDI_997_SEG_AK9);

        // This is used for flushing unwanted AK segments from the file
        mAkURecordTemplate = getRecordTemplate(RecordId.EDI_SEG_GENERIC);

        addReadRecordListener(this);
    }

    public AckFile(EftpsEdiType pEdiAckType, String pAckGsCtrlNum) {
        this();

        mEdiAckType = pEdiAckType;
        mAckGsCtrlNum = pAckGsCtrlNum;

        configureForTransmit();
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

    public void queueAckToWrite(EftpsEdiType pEdiType, String pTransSetCtrlNum) {
        mAcksToWriteList.add(new AckInfo(pEdiType, pTransSetCtrlNum));
    }

    public EftpsEdiType getEdiAckType() {
        return mEdiAckType;
    }

    public void setEdiAckType(EftpsEdiType pEdiAckType) {
        this.mEdiAckType = pEdiAckType;
    }

    public void setAckGsCtrlNum(String pAckGsCtrlNum) {
        this.mAckGsCtrlNum = pAckGsCtrlNum;
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
            case EDI151:
                fileType = EdiFileType.EftpsPaymentResponseAck;     // Ack for 151 file
                break;
            case EDI813:
                fileType = EdiFileType.EftpsPaymentAck;             // Ack for 813 file
                break;
            case EDI821:
                fileType = EdiFileType.EftpsForecastAck;            // Ack for 821 file
                break;
            case EDI824:
                fileType = EdiFileType.EftpsEnrollmentResponseAck;  // Ack for 824 file
                break;
            case EDI826:
                fileType = EdiFileType.EftpsPaymentConfirmationAck; // Ack for 826 file
                break;
            case EDI827:
                fileType = EdiFileType.EftpsPaymentReturnAck;       // Ack for 827 file
                break;
            case EDI838:
                fileType = EdiFileType.EftpsEnrollmentAck;          // Ack for 838 file
                break;
        }

        return fileType;
    }

    @Override
    protected int writeContent() {
        int ackCount = 0;

        startFile();
        startTransactionSet();

        mAk1RecordTemplate.setFieldValue(FieldId.EDI_997_SEG_AK101, mEdiAckType.funcIdCode());
        mAk1RecordTemplate.setFieldValue(FieldId.EDI_997_SEG_AK102, mAckGsCtrlNum);
        writeTransaction(mAk1RecordTemplate);

        for (AckInfo ackInfo : mAcksToWriteList) {
            mAk2RecordTemplate.setFieldValue(FieldId.EDI_997_SEG_AK201, ackInfo.mTransSetIdCode);
            mAk2RecordTemplate.setFieldValue(FieldId.EDI_997_SEG_AK202, ackInfo.mTransSetCtrlNum);
            writeTransaction(mAk2RecordTemplate);

            mAk5RecordTemplate.setFieldValue(FieldId.EDI_997_SEG_AK501, "A");
            writeTransaction(mAk5RecordTemplate);

            ++ackCount;
        }

        mAk9RecordTemplate.setFieldValue(FieldId.EDI_997_SEG_AK901, "A");
        mAk9RecordTemplate.setFieldValue(FieldId.EDI_997_SEG_AK902, ackCount);
        mAk9RecordTemplate.setFieldValue(FieldId.EDI_997_SEG_AK903, ackCount);
        mAk9RecordTemplate.setFieldValue(FieldId.EDI_997_SEG_AK904, ackCount);
        writeTransaction(mAk9RecordTemplate);

        endTransactionSet();
        endFile();

        return ackCount;
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

                EftpsFile eftpsFile = EftpsFile.getEftpsFileByFileId(fileId);

                eftpsFile.setAcknowledgementFile(getEdiFileRecord().getEftpsFile());

                // Copy parent record System Owner
                getEdiFileRecord().setSystemOwner(eftpsFile.getSystemOwner());

                //
                // If we're processing an ack for a file belonging the the AS400:
                //   - If it's a 151/826/827 ack, we need to forward the ack file to the TFA, so set the completion
                //     status to PendingTransmission
                //   - If it's an 813 ack, we need to forward the ack file to the AS400, so set the completion
                //     status to SendToAS400
                //

                if (SystemOwnerType.AS400.equals(eftpsFile.getSystemOwner())) {
                    switch (mEdiAckType) {
                        case EDI151: // Direction: from AS400 -to- PSP -to- TFA
                        case EDI826:
                        case EDI827:
                            getEdiFileRecord().setCompletionStatus(EdiFileStatus.PendingTransmission);
                            break;
                        case EDI813: // Direction: from TFA -to- PSP -to- AS400
                            getEdiFileRecord().setCompletionStatus(EdiFileStatus.SendToAS400);
                            break;
                    }
                }

                Application.save(eftpsFile);
            } else if (template == mAk5RecordTemplate) {
                ++mRecordCount;

                String transSetAckCode = mAk5RecordTemplate.getFieldValue(FieldId.EDI_997_SEG_AK501);

                if ("A".equals(transSetAckCode)) {
                    ++mAcceptCount;
                } else {
                    ++mRejectCount;

                    //
                    // This is bad, it means we have a logic problem in our code that generates
                    // files and the file with this ST set was either generated wrong, or
                    // somehow got corrupted in transmission.  There isn't anything we can do to
                    // fix it, so just log the error and move on.  Support should have a look.
                    //

                    int gsCtrlNum = mAk1RecordTemplate.getFieldInt(FieldId.EDI_997_SEG_AK102);
                    int stCtrlNum = mAk2RecordTemplate.getFieldInt(FieldId.EDI_997_SEG_AK202);

                    String err = String.format("In the EFTPS %s file with GS06 Ctrl Num %d, the Transaction Set " +
                            "with ST02 Ctrl Num %d was rejected (ack file: %s, record: %s).",
                            mEdiAckType, gsCtrlNum, stCtrlNum, getDetailedFileName(), template.getName());

                    logger.error("EFTPS ACK FILE REJECT: " + err);
                }
            }
        } catch (Throwable t) {
            String err = String.format("Error while reading ACK record (ack file: %s, record: %s) ",
                    getDetailedFileName(), template.getName());
            throw new RuntimeException(err, t);
        }
    }
}
