package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.ops.eftpsBp.EnrollmentResponseFile;
import com.paycycle.util.StringUtil;
import org.apache.commons.collections.ListUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 13, 2010
 * Time: 2:37:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnrollmentResponseFileReadWrite extends EnrollmentResponseFile {
    private static final String BGN02_REF_NUM_VALUE = "123456";

    private List<EnrollResponseSegInfo> enrollResponseSegInfos = new ArrayList<EnrollResponseSegInfo>();
    private List<RejectionInfo> rejectionInfos = new ArrayList<RejectionInfo>();
    private List<String> rejectionTransIds = new ArrayList<String>();
    private String mOutboundDir = null;

    public EnrollmentResponseFileReadWrite(String pOutboundDir) {
        mOutboundDir = (pOutboundDir != null) ? pOutboundDir : EftpsUtil.getWorkDir();
        String fileExtn = "E" + new SimpleDateFormat("DDDHHmmssSSS").format(getCreateDate());
        createFileName("EFTPSTX", fileExtn, mOutboundDir);
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

        for (EnrollResponseSegInfo enrollResponseSegInfo : enrollResponseSegInfos) {
            segCount++;

            startTransactionSet();

            setValuesForBGNTransactions(enrollResponseSegInfo);
            writeTransaction(mBgnRecordTemplate);

            for (String transId : enrollResponseSegInfo.getTransactionIds()) {
                setOTIRecordvalues(transId);
                writeTransaction(mOtiRecordTemplate);
                writeTransaction(mRefRecordTemplate);
            }

            endTransactionSet();
        }

        endFile();

        return segCount;
    }

    private String getErrorCode(String pTransId) {
        for (RejectionInfo rejectionInfo : rejectionInfos) {
            if (rejectionInfo.getId().equals(pTransId)) {
                return rejectionInfo.getCode();
            }
        }
        return null;
    }

    private void setOTIRecordvalues(String pTransId) {
        if (rejectionTransIds.contains(pTransId)) {
            mOtiRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_OTI01, "IR");
            mRefRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_REF01, "1Q");
            mRefRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_REF02, getErrorCode(pTransId));
        } else {
            mOtiRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_OTI01, "IA");
            mRefRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_REF02, getRandomNumberForRef());
            mRefRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_REF01, "BB");
        }

        mOtiRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_OTI02, "2I");
        mOtiRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_OTI03, pTransId);

    }

    private void setValuesForBGNTransactions(EnrollResponseSegInfo pEnrollResponseSegInfo) {
        mBgnRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_BGN01, "00");
        mBgnRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_BGN02, BGN02_REF_NUM_VALUE);
        mBgnRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_BGN03, new SimpleDateFormat("yyMMdd").format(getCreateDate()));
        mBgnRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_BGN04, new SimpleDateFormat("HHmm").format(getCreateDate()));
        mBgnRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_BGN06, pEnrollResponseSegInfo.getSegmentId());

        if (ListUtils.intersection(pEnrollResponseSegInfo.getTransactionIds(), rejectionTransIds).size() > 0) {
            mBgnRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_BGN08, "U");
        } else {
            mBgnRecordTemplate.setFieldValue(FieldId.EDI_824_SEG_BGN08, "CF");
        }
    }

    public void queueEnrollResponseToWrite(EnrollResponseSegInfo pEnrollResponseSegInfo) {
        enrollResponseSegInfos.add(pEnrollResponseSegInfo);
    }

    public void setRejectionInfos(List<RejectionInfo> pRejectionInfos) {
        if (pRejectionInfos != null && pRejectionInfos.size() > 0) {
            rejectionInfos = pRejectionInfos;
            rejectionTransIds.clear();

            for (RejectionInfo pRejectionInfo : pRejectionInfos) {
                rejectionTransIds.add(pRejectionInfo.getId());
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

    public void configureForTransmit() {
    }

    public String getOutboundDir() {
        return mOutboundDir;
    }

    public void setOutboundDir(String outboundDir) {
        this.mOutboundDir = outboundDir;
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

    public String getRandomNumberForRef() {
        return new SimpleDateFormat("DDDddHHyyyymmssSSS").format(new Date(SpcfCalendar.createInstance().getTimeInMilliseconds()));
    }
}
