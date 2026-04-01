package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.ops.eftpsBp.PaymentReturnFile;
import com.paycycle.util.StringUtil;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Jan 3, 2011
 * Time: 4:59:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class PaymentReturnFileReadWrite extends PaymentReturnFile {
    private String outboundDir = null;
    private PaymentFileInfo paymentFileInfo;

    PaymentReturnFileReadWrite(String pOutboundDir) {
        addReadRecordListener(this);

        outboundDir = pOutboundDir;
        if (outboundDir == null) {
            outboundDir = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, EftpsUtil.WORK_DIR);
        }

        String fileExtn = "E" + new SimpleDateFormat("DDDHHmmssSSS").format(getCreateDate());
        createFileName("EFTPSTX", fileExtn, outboundDir);
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
        int segCount = 1;

        startFile();

        for (PaymentFileSegmentInfo paymentFileSegmentInfo : paymentFileInfo.getPaymentFileSegmentInfos()) {
            if (paymentFileSegmentInfo.getReturnErrorCode() != null) {
                segCount++;

                startTransactionSet();

                setValuesForRICRecord(paymentFileSegmentInfo);
                writeTransaction(mRicRecordTemplate);

                setValuesForREFRecord1(paymentFileSegmentInfo);
                writeTransaction(mRefRecordTemplate);

                setValuesForREFRecord2(paymentFileSegmentInfo);
                writeTransaction(mRefRecordTemplate);

                if (paymentFileSegmentInfo.getReturnRefNumber() != null) {
                    setValuesForREFRecord3(paymentFileSegmentInfo);
                    writeTransaction(mRefRecordTemplate);
                }

                endTransactionSet();
            }
        }

        endFile();

        return segCount;
    }

    private void setValuesForREFRecord1(PaymentFileSegmentInfo pPaymentFileSegmentInfo) {
        mRefRecordTemplate.setFieldValue(FieldId.EDI_827_SEG_REF01, "93");
        mRefRecordTemplate.setFieldValue(FieldId.EDI_827_SEG_REF02, pPaymentFileSegmentInfo.getAuthorizationNumber());
    }

    private void setValuesForREFRecord2(PaymentFileSegmentInfo pPaymentFileSegmentInfo) {
        mRefRecordTemplate.setFieldValue(FieldId.EDI_827_SEG_REF01, "1Q");
        mRefRecordTemplate.setFieldValue(FieldId.EDI_827_SEG_REF02, pPaymentFileSegmentInfo.getReturnErrorCode());
    }

    private void setValuesForREFRecord3(PaymentFileSegmentInfo pPaymentFileSegmentInfo) {
        mRefRecordTemplate.setFieldValue(FieldId.EDI_827_SEG_REF01, "ZZ");
        mRefRecordTemplate.setFieldValue(FieldId.EDI_827_SEG_REF02, pPaymentFileSegmentInfo.getReturnRefNumber());
    }

    private void setValuesForRICRecord(PaymentFileSegmentInfo pPaymentFileSegmentInfo) {
        mRicRecordTemplate.setFieldValue(FieldId.EDI_827_SEG_RIC01, pPaymentFileSegmentInfo.getRicErrorCode());
        mRicRecordTemplate.setFieldValue(FieldId.EDI_827_SEG_RIC02, EftpsUtil.formatStripDecimal(SpcfUtils.convertToBigDecimal(pPaymentFileSegmentInfo.getSegmentTotalAmount())));
        mRicRecordTemplate.setFieldValue(FieldId.EDI_827_SEG_RIC03, pPaymentFileSegmentInfo.getTransactionTypeFlag());
    }

    public void configureForTransmit() {
    }

    public String getOutboundDir() {
        return outboundDir;
    }

    public void setOutboundDir(String outboundDir) {
        this.outboundDir = outboundDir;
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

    public void setPaymentFileInfo(PaymentFileInfo paymentFileInfo) {
        this.paymentFileInfo = paymentFileInfo;
    }
}
