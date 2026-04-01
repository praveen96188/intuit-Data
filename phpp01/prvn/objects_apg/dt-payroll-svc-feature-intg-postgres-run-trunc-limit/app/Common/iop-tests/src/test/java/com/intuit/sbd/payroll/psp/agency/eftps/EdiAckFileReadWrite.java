package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.paycycle.eftpsBp.*;
import com.paycycle.fixedlen.RecordTemplate;
import com.paycycle.ops.eftpsBp.AckFile;

import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 29, 2011
 * Time: 10:33:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class EdiAckFileReadWrite extends AckFile {

    private String outboundDir = null;
    private String ctrlNumber;
    int counter = 0;
    
    public EdiAckFileReadWrite(String pOutboundDir, EftpsEdiType pEftpsEdiType, String pAckGsCtrlNum) {
        addReadRecordListener(this);
        setEdiAckType(pEftpsEdiType);
        outboundDir = pOutboundDir;
        setAckGsCtrlNum(pAckGsCtrlNum);

        if (outboundDir == null) {
            outboundDir = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, EftpsUtil.EDI_WORK_DIR);
        }
        mISAHeaderTemplate = getRecordTemplate(RecordId.EDI_SEG_ISA_4010);

        String fileExtn = getEdiFileType().toString();
        createFileName("EDITX", fileExtn, outboundDir);
    }

    public int writeISAHeader() {
        try {
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA05, "01");
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA06, EftpsUtil.getConfigString("psp_edi_receiver_id"));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA07, "32");
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA08, EftpsUtil.getConfigString("psp_edi_sender_id"));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA09, new SimpleDateFormat("yyMMdd").format(mCreateDate));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA10, new SimpleDateFormat("HHmm").format(mCreateDate));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA13, EftpsUtil.formatCtrlNum(getIsaControlNumber()));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA15, EftpsUtil.getConfigString("psp_edi_edi_mode"));

            return writeRecord(new StateEdiRecordTemplate(mISAHeaderTemplate));
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write ISA header. ", t);
        }
    }

    public int writeGSHeader() {
        try {

            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS01, getEdiFileType().funcIdCode());
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS02, EftpsUtil.getConfigString("psp_edi_receiver_code"));
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS03, EftpsUtil.getConfigString("psp_edi_sender_id"));
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS04, new SimpleDateFormat("yyyyMMdd").format(mCreateDate));
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS05, new SimpleDateFormat("HHmmss").format(mCreateDate));
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS08, "004010");
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS06, EftpsUtil.formatCtrlNum(getGsControlNumber()));

            return writeRecord(new StateEdiRecordTemplate(mGSHeaderTemplate));
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write GS header. ", t);
        }
    }

    public int writeSTHeader() {
        try {
            ctrlNumber = EftpsUtil.formatCtrlNum(EftpsUtil.getNewSegmentControlNumber());
            mSTHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ST01, getEdiFileType().toString());
            mSTHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ST02, ctrlNumber);

            return writeTransaction(new StateEdiRecordTemplate(mSTHeaderTemplate)); // The ST header record counts as a transaction within the ST segment
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write ST header. ", t);
        }
    }

    public int writeSETrailer() {
        try {
            // Increment mTransactionCount since the SE trailer is included in the transaction count for the ST segment
            mSETrailerTemplate.setFieldValue(FieldId.EDI_SEG_SE01, counter);
            mSETrailerTemplate.setFieldValue(FieldId.EDI_SEG_SE02, ctrlNumber);

            return writeRecord(new StateEdiRecordTemplate(mSETrailerTemplate)); // no need to call writeTransaction since we're ending the ST segment
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write SE trailer. ", t);
        }
    }

    public int writeGETrailer() {
        try {
            mGETrailerTemplate.setFieldValue(FieldId.EDI_SEG_GE01, 1);
            mGETrailerTemplate.setFieldValue(FieldId.EDI_SEG_GE02, EftpsUtil.formatCtrlNum(getGsControlNumber()));

            return writeRecord(new StateEdiRecordTemplate(mGETrailerTemplate));
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write GE trailer. ", t);
        }
    }

    public int writeIEATrailer() {
        try {
            mIEATrailerTemplate.setFieldValue(FieldId.EDI_SEG_IEA01, 1);
            mIEATrailerTemplate.setFieldValue(FieldId.EDI_SEG_IEA02, EftpsUtil.formatCtrlNum(getIsaControlNumber()));

            return writeRecord(new StateEdiRecordTemplate(mIEATrailerTemplate));
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write IEA trailer. ", t);
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

    protected int writeTransaction(RecordTemplate pRecord) {
        StateEdiRecordTemplate stateEdiRecordTemplate = new StateEdiRecordTemplate((EDIRecordTemplate)pRecord);
        int bytesWritten = writeRecord(stateEdiRecordTemplate);

        if (bytesWritten > 0) {
            ++mTransactionCount;
        }

        return bytesWritten;
    }
     
}
