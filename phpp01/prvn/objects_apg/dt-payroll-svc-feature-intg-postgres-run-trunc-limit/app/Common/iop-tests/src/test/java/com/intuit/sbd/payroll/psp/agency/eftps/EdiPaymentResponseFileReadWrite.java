package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.DataObject;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.StateEdiRecordTemplate;
import com.paycycle.ops.eftpsBp.EDIWrappedStringWriter;
import com.paycycle.ops.eftpsBp.EdiPaymentResponseFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 28, 2011
 * Time: 2:36:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class EdiPaymentResponseFileReadWrite extends EdiPaymentResponseFile {
    private String outboundDir = null;
    private Integer paymentFileId;
    private String paymentFileGroupTime;
    private String ctrlNumber;
    int counter = 0;

    private List<EdiResponseFileTxnDetails> txnDetails = new ArrayList<EdiResponseFileTxnDetails>();

    public EdiPaymentResponseFileReadWrite(String pOutboundDir, Integer pPaymentFileId, String pPaymentFileGroupTime, List<EdiResponseFileTxnDetails> pTxnDetails) {
        addReadRecordListener(this);

        paymentFileId = pPaymentFileId;
        paymentFileGroupTime = pPaymentFileGroupTime;
        txnDetails = pTxnDetails; 

        outboundDir = pOutboundDir;
        if (outboundDir == null) {
            outboundDir = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, EftpsUtil.EDI_WORK_DIR);
        }

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

    public int writeBtaSegment() {
        try {
            mBtaRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTA01, "AE"); // “AE” Acknowledge 
            mBtaRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTA02, new SimpleDateFormat("yyyyMMdd").format(mCreateDate));

            return writeRecord(new StateEdiRecordTemplate(mBtaRecordTemplate));
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write BTA header. ", t);
        }
    }

    public int writeBtiSegment() {
        try {
            mBtiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTI01, "T2");
            mBtiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTI02, "01101");
            mBtiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTI03, "47");
            mBtiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTI04, "MS");
            mBtiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_BTI05, new SimpleDateFormat("yyyyMMdd").format(mCreateDate));

            return writeRecord(new StateEdiRecordTemplate(mBtiRecordTemplate));
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write BTI header. ", t);
        }
    }

    public int writeTfsSegment(String pTaxId) {
        try {
            mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS01, "T2");
            mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS02, "89105");
            mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS03, "11");
            mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS04, pTaxId);
            mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS05, "49");
            mTfsRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_TFS06, "11"+pTaxId);

            return writeRecord(new StateEdiRecordTemplate(mTfsRecordTemplate));
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write TFS header. ", t);
        }
    }

    public int writeOuterRefSegment(String pRefId, String pRefNumber) {
        try {
            mRefOuterRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_OUTER_REF01, pRefId);
            mRefOuterRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_OUTER_REF02, pRefNumber);

            return writeRecord(new StateEdiRecordTemplate(mRefOuterRecordTemplate));
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write Outer REF header. ", t);
        }
    }

    public int writeInnerRefSegment(String pRefId, String pRefDescription) {
        try {
            mRefInnerRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_INNER_REF01, pRefId);
            mRefInnerRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_INNER_REF03, pRefDescription);

            return writeRecord(new StateEdiRecordTemplate(mRefInnerRecordTemplate));
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write Inner REF header. ", t);
        }
    }

    public int writePbiSegment(String pErrCd, String pActionCd, String pErrMessage) {
        try {
            mPbiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_PBI01, pErrCd);
            mPbiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_PBI02, pActionCd);
            mPbiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_PBI03, pErrMessage);
            if(pActionCd != null && (pActionCd.equals("CF") || pActionCd.equals("WQ"))) {
                mPbiRecordTemplate.setFieldValue(FieldId.EDI_151_SEG_PBI04, new SimpleDateFormat("DDDHHmmssSSS").format(getCreateDate()));
            }

            return writeRecord(new StateEdiRecordTemplate(mPbiRecordTemplate));
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write PBI header. ", t);
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
   
    @Override
    protected int writeContent() {        
        
        counter = 4;
        startFile();
        writeSTHeader();
        writeBtaSegment();
        writeBtiSegment();

        for (EdiResponseFileTxnDetails txnDetail : txnDetails) {
            counter += 6;
            writeTfsSegment(txnDetail.getTxnId());
            writeOuterRefSegment("FI", paymentFileId + paymentFileGroupTime);
            writeOuterRefSegment("TN", String.valueOf(txnDetail.getTxnSetId()));
            writeInnerRefSegment("T6", "Employers Withholding");
            writeInnerRefSegment("ZZ", "VAN Simulator");
            writePbiSegment(txnDetail.getErrorCd(), txnDetail.getActionCode(), txnDetail.getMessage());
        }
        writeSETrailer();

        endFile();

        return counter;
    }

    public void configureForTransmit() {
    }

    public String getOutboundDir() {
        return outboundDir;
    }

    public void setOutboundDir(String outboundDir) {
        this.outboundDir = outboundDir;
    }

    public <T extends DataObject> int write(DomainEntitySet<T> pData) {
        return write(pData, new EDIWrappedStringWriter(80));
    }

    public int write() {
        return write(new EDIWrappedStringWriter(80));
    }
}
