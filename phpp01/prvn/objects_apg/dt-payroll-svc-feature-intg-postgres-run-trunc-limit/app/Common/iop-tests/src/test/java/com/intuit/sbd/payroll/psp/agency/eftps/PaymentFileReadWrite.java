package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.fixedlen.RecordTemplate;
import com.paycycle.ops.eftpsBp.AckFile;
import com.paycycle.ops.eftpsBp.PaymentFileReader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 15, 2010
 * Time: 9:20:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class PaymentFileReadWrite extends PaymentFileReader {
    private String outboundDir = null;
    private AckFile mAckFile = null;
    protected PaymentFileInfo paymentFileInfo;
    private PaymentFileSegmentInfo paymentFileSegmentInfo = null;
    private PaymentResponseFileReadWrite mPaymentResponseFile = null;
    private PaymentReturnFileReadWrite mPaymentReturnFile = null;
    private SpcfDecimal refTransactionAmount = new SpcfMoney("0.0");
    private PaymentFileRefTransaction paymentFileRefTransaction = null;
    private List<RejectionInfo> rejectionInfos = new ArrayList<RejectionInfo>();
    private List<ReturnSegInfo> returnInfos = new ArrayList<ReturnSegInfo>();

    public PaymentFileReadWrite(String pOutFolderName) {
        super();
        outboundDir = pOutFolderName;
        addReadRecordListener(this);
        createFile();
    }

    @Override
    protected int readContent() {
        int count = 0;

        paymentFileInfo = new PaymentFileInfo();
        mPaymentResponseFile = new PaymentResponseFileReadWrite(getOutboundDir());
        mPaymentResponseFile.setPaymentFileInfo(paymentFileInfo);
        mPaymentResponseFile.setRejectionInfos(rejectionInfos);
        if (returnInfos != null && returnInfos.size() > 0) {
            mPaymentReturnFile = new PaymentReturnFileReadWrite(getOutboundDir());
            mPaymentReturnFile.setPaymentFileInfo(paymentFileInfo);
        }
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
            // read the TIA header record
            readRecord(mTiaOuterRecordTemplate);

            stSeg = peekNextSegmentCode();
            if (stSeg != null && stSeg.startsWith("REF")) {
                readRecord(mRefOuterRecordTemplate);
            }
            stSeg = peekNextSegmentCode();
            if(stSeg != null && stSeg.startsWith("REF")) // AS same day will have two REF'S
            {
               readRecord(mRefOuterRecordTemplate);
               stSeg = peekNextSegmentCode();
            }
            if (stSeg != null && stSeg.startsWith("BPR")) {
                readRecord(mBprRecordTemplate);
            }
            while (true) {
                String tfsSeg = peekNextSegmentCode();
                if (tfsSeg == null) {
                    // We hit EOF.  But where's the SE, GE, IEA???
                    // Throw an exception, since the file is incomplete.
                    throw new EftpsBpRuntimeException("Incomplete Enrollment Response file.  Expected OTI or SE segment.");
                }
                if (tfsSeg.startsWith("SE")) {
                    // read the SE trailer record (also writes the ack info in notify handler)
                    readRecord(getSETrailerTemplate());
                    break;
                }
                // read the TFS Loop
                count += readRecord(mTfsRecordTemplate);
                //read REF segment
                count += readRecord(mRefInnerRecordTemplate);
                //read FGS segment
                count += readRecord(mFgsRecordTemplate);
                String tiaSeg = peekNextSegmentCode();
                while (tiaSeg.startsWith("TIA")) {
                    count += readRecord(mTiaInnerRecordTemplate);
                    tiaSeg = peekNextSegmentCode();
                }
            }
        }
        // read the GE trailer record
        readRecord(getGETrailerTemplate());
        // read the IEA trailer record
        readRecord(getIEATrailerTemplate());
        // finally, write the Ack file
        mAckFile.write();
        // finally, write Payment response file (EDI 151)
        mPaymentResponseFile.write();
        //If Return file is requested, generate Payment return file(EDI 827)
        if (mPaymentReturnFile != null) {
            mPaymentReturnFile.write();
        }

        return count;

    }

    public void recordCreated(RecordTemplate template) {
        if (template == getGSHeaderTemplate()) {
            paymentFileInfo.setGroupId(getGSHeaderTemplate().getFieldValue(FieldId.EDI_SEG_GS06));
            mAckFile = new AckFileReadWrite(getEdiFileType(), paymentFileInfo.getGroupId(), getOutboundDir());
        } else if (template == getSTHeaderTemplate()) {
            String segId = template.getFieldValue(FieldId.EDI_SEG_ST02);
            paymentFileSegmentInfo = new PaymentFileSegmentInfo(segId);
            setReturnSegment(segId);
        } else if (template == getSETrailerTemplate()) {
            mAckFile.queueAckToWrite(getEdiFileType(), getSTHeaderTemplate().getFieldValue(FieldId.EDI_SEG_ST02));
            mPaymentResponseFile.queuePaymentResponseToWrite(paymentFileSegmentInfo);
        } else if (template == mBprRecordTemplate) {
            paymentFileSegmentInfo.setSegmentTotalAmount((SpcfMoney) new SpcfMoney(template.getFieldValue(FieldId.EDI_813_SEG_BPR02)).divide(new SpcfMoney("100")));
            paymentFileSegmentInfo.setTransactionTypeFlag(template.getFieldValue(FieldId.EDI_813_SEG_BPR03));
            paymentFileSegmentInfo.setTransactionHandlingCode(template.getFieldValue(FieldId.EDI_813_SEG_BPR01));
            if(paymentFileSegmentInfo.getTransactionHandlingCode().equals("I"))
            {
               mPaymentResponseFile.setFileMode(PaymentResponseFileReadWrite.PaymentFileMode.PFM_SAME_DAY.toString());
            }else
             if(paymentFileSegmentInfo.getTransactionHandlingCode().equals("C"))
             {
                mPaymentResponseFile.setFileMode(PaymentResponseFileReadWrite.PaymentFileMode.PFM_100K.toString());
             }else
             {
                 mPaymentResponseFile.setFileMode(PaymentResponseFileReadWrite.PaymentFileMode.PFM_NEXT_DAY.toString());
             }

            if (paymentFileSegmentInfo.getReturnErrorCode() != null) {
                if (paymentFileSegmentInfo.getReturnErrorCode().equals("C01")) {
                    paymentFileSegmentInfo.setReturnRefNumber(template.getFieldValue(FieldId.EDI_813_SEG_BPR15));
                } else if (paymentFileSegmentInfo.getReturnErrorCode().equals("C02")) {
                    paymentFileSegmentInfo.setReturnRefNumber(template.getFieldValue(FieldId.EDI_813_SEG_BPR13));
                }
            }
        } else if (template == mRefOuterRecordTemplate) {
            if (mRefOuterRecordTemplate.getFieldValue(FieldId.EDI_813_SEG_OUTER_REF01).equals("CK")) {
                mPaymentResponseFile.setSameDayFileMode();
            }
        } else if (template == mRefInnerRecordTemplate) {
            paymentFileRefTransaction = new PaymentFileRefTransaction();
            paymentFileRefTransaction.setRefNumber(mRefInnerRecordTemplate.getFieldValue(FieldId.EDI_813_SEG_INNER_REF02));
            paymentFileSegmentInfo.addRefNumber(paymentFileRefTransaction);
            refTransactionAmount = new SpcfMoney("0.0");
        } else if (template == mTiaInnerRecordTemplate) {
            refTransactionAmount = refTransactionAmount.add(new SpcfMoney(template.getFieldValue(FieldId.EDI_813_SEG_INNER_TIA02)).divide(new SpcfMoney("100")));
            paymentFileRefTransaction.setTransactionAmount((SpcfMoney) refTransactionAmount);
        } else if (template == mBtiRecordTemplate) {
            paymentFileSegmentInfo.setBtiIdCode(mBtiRecordTemplate.getFieldValue(FieldId.EDI_813_SEG_BTI08));
        }
    }

    private boolean setReturnSegment(String segId) {
        for (ReturnSegInfo returnInfo : returnInfos) {
            if (segId.equals(returnInfo.getReturnSegId())) {
                paymentFileSegmentInfo.setReturnErrorCode(returnInfo.getReturnSegErrorCode());
                paymentFileSegmentInfo.setRicErrorCode(returnInfo.getErrorCode());
                return true;
            }
        }
        return false;
    }

    public void setRejectionInfos(List<RejectionInfo> pRejectionInfos) {
        this.rejectionInfos = pRejectionInfos;
    }

    public void setReturnInfos(List<ReturnSegInfo> pReturnInfos) {
        this.returnInfos = pReturnInfos;
    }

    public PaymentFileInfo getPaymentFileInfoWithAmounts() {
        return paymentFileInfo;
    }

    public String getAckFileName() {
        if (mAckFile != null) {
            return mAckFile.getFileName();
        }
        return null;
    }

    public String getPaymentResponseFileName() {
        if (mPaymentResponseFile != null) {
            return mPaymentResponseFile.getFileName();
        }
        return null;
    }

    public String getPaymentReturnFileName() {
        if (mPaymentReturnFile != null) {
            return mPaymentReturnFile.getFileName();
        }
        return null;
    }

    public void configureForTransmit() {
    }

    public void createFile() {
        if (outboundDir == null) {
            outboundDir = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, EftpsUtil.WORK_DIR);
        }
        String fileExtn = "E" + new SimpleDateFormat("DDDHHmmssSSS").format(getCreateDate());
        createFileName("EFTPSTX", fileExtn, outboundDir);
    }

    public String getOutboundDir() {
        return outboundDir;
    }

    public void setOutboundDir(String outboundDir) {
        this.outboundDir = outboundDir;
    }
}
