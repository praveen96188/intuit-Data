package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.fixedlen.RecordListener;
import com.paycycle.fixedlen.RecordTemplate;
import com.paycycle.ops.eftpsBp.AckFile;
import com.paycycle.ops.eftpsBp.EdiPaymentFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Nov 4, 2011
 * Time: 5:20:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class EdiPaymentFileReadWrite extends EdiPaymentFile implements RecordListener {

    private String outboundDir = null;
    private AckFile mAckFile = null;
    private Integer paymentFileId;
    private String paymentFileGroupTime;
    private Integer stSegId;
    private EdiPaymentResponseFileReadWrite ediPaymentResponseFileReadWrite;

    private List<EdiResponseFileTxnDetails> txnDetails;

    public EdiPaymentFileReadWrite(String pOutFolderName) {
        super();
        outboundDir = pOutFolderName;
        addReadRecordListener(this);

        if (outboundDir == null) {
            outboundDir = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, EftpsUtil.EDI_WORK_DIR);
        }
    }

    @Override
    protected int readContent() {
        int count = 0;

        txnDetails = new ArrayList<EdiResponseFileTxnDetails>();

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
            // read the BTI header record
            readRecord(mBtiRecordTemplate);
            // read the DTM header record
            readRecord(mDtmRecordTemplate);
            // read the BPR header record
            readRecord(mBprRecordTemplate);
            // read the N1 header record
            readRecord(mN1RecordTemplate);

            String nextSegCd = peekNextSegmentCode();
            if (nextSegCd != null && nextSegCd.startsWith("N2")) {
                // read the N2 header record
                readRecord(mN2RecordTemplate);
            }

            // read the N3 header record
            readRecord(mN3RecordTemplate);
            // read the N4 header record
            readRecord(mN4RecordTemplate);

            while (true) {
                count++;
                String tfsSeg = peekNextSegmentCode();
                if (tfsSeg == null) {
                    // We hit EOF.  But where's the SE, GE, IEA???
                    // Throw an exception, since the file is incomplete.
                    throw new EftpsBpRuntimeException("Incomplete Payment file.  Expected TFS or SE segment.");
                }
                if (tfsSeg.startsWith("SE")) {
                    // read the SE trailer record (also writes the ack info in notify handler)
                    readRecord(getSETrailerTemplate());
                    break;
                }
                // read the TFS Loop
                readRecord(mTfsRecordTemplate);
                //read REF segment - 3
                readRecord(mRefRecordTemplate);
                readRecord(mRefRecordTemplate);
                readRecord(mRefRecordTemplate);

                //read DTM segments - 2
                readRecord(mDtmRecordTemplate);
                readRecord(mDtmRecordTemplate);

                // read the N1 header record
                readRecord(mN1RecordTemplate);
                nextSegCd = peekNextSegmentCode();
                if (nextSegCd != null && nextSegCd.startsWith("N2")) {
                    // read the N2 header record
                    readRecord(mN2RecordTemplate);
                }
                // read the N3 header record
                readRecord(mN3RecordTemplate);
                // read the N4 header record
                readRecord(mN4RecordTemplate);

                //read the TIA outer segments - 4
                readRecord(mTiaOuterRecordTemplate);
                readRecord(mTiaOuterRecordTemplate);
                readRecord(mTiaOuterRecordTemplate);
                readRecord(mTiaOuterRecordTemplate);

                //read the TIA inner segments - 6
                readRecord(mTiaInnerRecordTemplate);
                readRecord(mTiaInnerRecordTemplate);
                readRecord(mTiaInnerRecordTemplate);
                readRecord(mTiaInnerRecordTemplate);
                readRecord(mTiaInnerRecordTemplate);
                readRecord(mTiaInnerRecordTemplate);

            }
        }
        // read the GE trailer record
        readRecord(getGETrailerTemplate());
        // read the IEA trailer record
        readRecord(getIEATrailerTemplate());
        // finally, write the Ack file
        mAckFile.write();

        //finally, create payment response file and write (EDI 151)
        ediPaymentResponseFileReadWrite = new EdiPaymentResponseFileReadWrite(outboundDir, paymentFileId, paymentFileGroupTime, txnDetails);
        ediPaymentResponseFileReadWrite.write();

        return count;

    }

    public void recordCreated(RecordTemplate template) {
        if (template == getGSHeaderTemplate()) {
            paymentFileGroupTime = getGSHeaderTemplate().getFieldValue(FieldId.EDI_SEG_GS05);
            paymentFileId = Integer.parseInt(getGSHeaderTemplate().getFieldValue(FieldId.EDI_SEG_GS06));
            mAckFile = new EdiAckFileReadWrite(outboundDir, getEdiFileType(), String.valueOf(paymentFileId));
        } else if (template == getSTHeaderTemplate()) {
            stSegId = template.getFieldInt(FieldId.EDI_SEG_ST02);
        } else if (template == getSETrailerTemplate()) {
            mAckFile.queueAckToWrite(getEdiFileType(), getSTHeaderTemplate().getFieldValue(FieldId.EDI_SEG_ST02));
        } else if (template == mTfsRecordTemplate) {
            EdiResponseFileTxnDetails ediResponseFileTxnDetails = new EdiResponseFileTxnDetails();
            ediResponseFileTxnDetails.setTxnId(mTfsRecordTemplate.getFieldValue(FieldId.EDI_813_SEG_TFS04));
            ediResponseFileTxnDetails.setTxnSetId(stSegId);
            ediResponseFileTxnDetails.setActionCode("CF");
            ediResponseFileTxnDetails.setErrorCd("000000");
            ediResponseFileTxnDetails.setMessage("Confirmation");
            txnDetails.add(ediResponseFileTxnDetails);
        }
    }

    public String getAckFileName() {
        if (mAckFile != null) {
            return mAckFile.getFileName();
        }
        return null;
    }

    public String getPaymentResponseFileName() {
        if (ediPaymentResponseFileReadWrite != null) {
            return ediPaymentResponseFileReadWrite.getFileName();
        }
        return null;
    }

    @Override
    public void configureForTransmit() {
    }
}
