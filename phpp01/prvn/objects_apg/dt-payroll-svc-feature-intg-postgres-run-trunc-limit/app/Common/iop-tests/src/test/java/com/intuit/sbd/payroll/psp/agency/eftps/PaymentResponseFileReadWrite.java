package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.ops.eftpsBp.PaymentResponseFile;
import com.paycycle.util.StringUtil;
import org.apache.commons.collections.ListUtils;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 15, 2010
 * Time: 1:18:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaymentResponseFileReadWrite extends PaymentResponseFile {
    private String outboundDir = null;
    private PaymentFileInfo paymentFileInfo;
    private List<RejectionInfo> rejectionInfos = new ArrayList<RejectionInfo>();
    private List<String> rejectionRefIds = new ArrayList<String>();
    protected PaymentFileMode mFileMode;

    public enum PaymentFileMode {
        PFM_NEXT_DAY, // Normal next-day payment file (Intuit debit)
        PFM_SAME_DAY, // Emergency same-day payment file (no money movement)
        PFM_100K      // 100k payment file (Client debit)
    }

    public PaymentResponseFileReadWrite(String pOutboundDir) {
        this(PaymentFileMode.PFM_NEXT_DAY.toString(), pOutboundDir);
    }

    public PaymentResponseFileReadWrite(String pfileMode, String pOutboundDir) {
        addReadRecordListener(this);
        //setFileMode(pfileMode);

        outboundDir = pOutboundDir;
        if (outboundDir == null) {
            outboundDir = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, EftpsUtil.WORK_DIR);
        }

        String fileExtn = "E" + new SimpleDateFormat("DDDHHmmssSSS").format(getCreateDate());
        createFileName("EFTPSTX", fileExtn, outboundDir);
    }

    protected void setFileMode(String pfileMode) {
        mFileMode = PaymentFileMode.valueOf(pfileMode);
    }

    protected void setSameDayFileMode() {
        mFileMode = PaymentFileMode.PFM_SAME_DAY;
    }

    public int writeISAHeader() {
        try {
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA05, "12");
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA06, StringUtil.rightPad(EftpsUtil.getConfigString("psp_eftps_receiver_id"), " ", 15));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA07, "30");
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA08, StringUtil.leftPad(EftpsUtil.getConfigString("psp_eftps_sender_id"), "0", 15));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA09, new SimpleDateFormat("yyMMdd").format(mCreateDate));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA10, new SimpleDateFormat("HHmm").format(mCreateDate));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA13, EftpsUtil.formatCtrlNum(getIsaControlNumber()));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA15, EftpsUtil.getConfigString("psp_eftps_edi_mode"));

            return writeRecord(mISAHeaderTemplate);
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write ISA header. ", t);
        }
    }

    public int writeGSHeader() {
        try {
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS01, getEdiFileType().funcIdCode());
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS02, EftpsUtil.getConfigString("psp_eftps_receiver_id"));
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS03, EftpsUtil.getConfigString("psp_eftps_sender_id"));
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS04, new SimpleDateFormat("yyMMdd").format(mCreateDate));
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS05, new SimpleDateFormat("HHmm").format(mCreateDate));
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS06, EftpsUtil.formatCtrlNum(getGsControlNumber()));

            return writeRecord(mGSHeaderTemplate);
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write GS header. ", t);
        }
    }

    @Override
    protected int writeContent() {
        int segCount = 0;

        startFile();

        for (PaymentFileSegmentInfo paymentFileSegmentInfo : paymentFileInfo.getPaymentFileSegmentInfos()) {
            segCount++;

            startTransactionSet();

            setBTARecordValues(paymentFileSegmentInfo);
            writeTransaction(mBtaRecordTemplate);

            String confNum = setBTIRecordValues(paymentFileSegmentInfo);
            paymentFileSegmentInfo.setAuthorizationNumber(confNum);
            writeTransaction(mBtiRecordTemplate);

            if (mFileMode.equals(PaymentFileMode.PFM_SAME_DAY)) {  // FOR SAME DAY, THERE WILL BE ONLY ONE TFS RECORD WILL BE WRITTEN IN 151 EVEN THOUGH 813 FILE CONTAINS MORE THAN ONE PAYMENT(i.e REFs).
                mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS01, "94");
                mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS02, mBtiRecordTemplate.getFieldValue(FieldId.EDI_151_SEG_BTI02));

                if (rejectionRefIds != null && rejectionRefIds.size() > 0)    // if any rejections induced.
                {
                    mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS03, "1Q");
                    mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS04, getErrorCode(rejectionRefIds.get(0)));
                } else {
                    mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS03, "4N");
                    mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS04, mBtiRecordTemplate.getFieldValue(FieldId.EDI_151_SEG_BTI08));
                }

                writeTransaction(mTfsRecordTemplate);
            } else {

                for (PaymentFileRefTransaction refTransaction : paymentFileSegmentInfo.getRefTransactions()) {
                    setTFSrecordValues(refTransaction, confNum);
                    writeTransaction(mTfsRecordTemplate);
                }
            }
            endTransactionSet();
        }

        endFile();

        return segCount;
    }

    private void setTFSrecordValues(PaymentFileRefTransaction pFileRefTransaction, String pConfNum) {
        mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS02, pFileRefTransaction.getRefNumber());

        mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS01, "F8");
        mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS03, "93");

        if (rejectionRefIds.contains(pFileRefTransaction.getRefNumber())) {
            mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS03, "1Q");
            mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS04, getErrorCode(pFileRefTransaction.getRefNumber()));
        } else {
            mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS04, pConfNum);
        }
    }

    private String setBTIRecordValues(PaymentFileSegmentInfo pPaymentFileSegmentInfo) {
        mBtiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTI01, "BT");
        mBtiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTI02, pPaymentFileSegmentInfo.getStSegmentId());
        mBtiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTI03, "SV");
        mBtiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTI04, pPaymentFileSegmentInfo.getBtiIdCode());
        mBtiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTI07, "93");

        String confNum;
        if (mFileMode.equals(PaymentFileMode.PFM_SAME_DAY)) {
            confNum = new SimpleDateFormat("ssSSS").format(new Date(SpcfCalendar.createInstance().getTimeInMilliseconds()));
        } else {
            confNum = new SimpleDateFormat("ddHHmmssddSSS").format(new Date(SpcfCalendar.createInstance().getTimeInMilliseconds()));
        }

        mBtiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTI08, confNum);

        return confNum;
    }

    private void setBTARecordValues(PaymentFileSegmentInfo pPaymentFileSegmentInfo) {
        mBtaRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTA02, new SimpleDateFormat("yyMMdd").format(getCreateDate()));
        int count = ListUtils.intersection(rejectionRefIds, pPaymentFileSegmentInfo.getRefNumbers()).size();

        if (count > 0) {
            if (mFileMode.equals(PaymentFileMode.PFM_SAME_DAY)) {
                mBtaRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTA01, "RJ");
            } else if (mFileMode.equals(PaymentFileMode.PFM_100K)) {
                mBtaRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTA01, "RD");
            } else {
                mBtaRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTA01, "AC");
                mBtaRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTA03, "AB");

                BigDecimal monetaryAmount = new BigDecimal(0.00);
                for (PaymentFileRefTransaction paymentFileRefTransaction : pPaymentFileSegmentInfo.getRefTransactions()) {
                    if (rejectionRefIds.contains(paymentFileRefTransaction.getRefNumber())) {
                        monetaryAmount = monetaryAmount.add(SpcfUtils.convertToBigDecimal(paymentFileRefTransaction.getTransactionAmount()));
                    }
                }
                monetaryAmount = SpcfUtils.convertToBigDecimal((SpcfMoney) pPaymentFileSegmentInfo.getSegmentTotalAmount().subtract(SpcfUtils.convertToSpcfMoney(monetaryAmount)));

                mBtaRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTA04, EftpsUtil.formatStripDecimal(monetaryAmount));
            }
        } else {
            if (mFileMode.equals(PaymentFileMode.PFM_SAME_DAY)) {
                mBtaRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTA01, "AH");
            } else {
                mBtaRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTA01, "AD");
            }
        }
    }

    public void configureForTransmit() {
    }

    public String getOutboundDir() {
        return outboundDir;
    }

    public void setOutboundDir(String outboundDir) {
        this.outboundDir = outboundDir;
    }

    private String getErrorCode(String pTransId) {
        for (RejectionInfo rejectionInfo : rejectionInfos) {
            if (rejectionInfo.getId().equals(pTransId)) {
                return rejectionInfo.getCode();
            }
        }

        return null;
    }

    public void setRejectionInfos(List<RejectionInfo> pRejectionInfos) {
        if (pRejectionInfos != null && pRejectionInfos.size() > 0) {
            this.rejectionInfos = pRejectionInfos;
            rejectionRefIds.clear();
            for (RejectionInfo pRejectionInfo : pRejectionInfos) {
                rejectionRefIds.add(pRejectionInfo.getId());
            }
        }
    }

    public String getAckFileName() {
        if (getAckFile() != null) {
            return getAckFile().getFileName();
        } else {
            return null;
        }
    }

    protected void createFileName(String pFilePrefix, String pFileExtension, String pDirectory) {
        String prefix = (pFilePrefix == null) ? "" : pFilePrefix;
        String ext = (pFileExtension == null) ? "" : pFileExtension;

        // remove leading '.' from file extension if present.
        if (ext.startsWith(".")) {
            ext = ext.substring(1);
        }

        String fileName = String.format("%s.%s.pgp", prefix, ext);
        boolean validDir = ((pDirectory != null) && (pDirectory.length() > 0));

        setFileName(validDir ? new File(new File(pDirectory), fileName).getPath() : fileName);
    }

    public void queuePaymentResponseToWrite(PaymentFileSegmentInfo pPaymentFileSegmentInfo) {
        paymentFileInfo.addPaymentFileSegmentInfo(pPaymentFileSegmentInfo);
    }

    public void setPaymentFileInfo(PaymentFileInfo paymentFileInfo) {
        this.paymentFileInfo = paymentFileInfo;
    }
}
