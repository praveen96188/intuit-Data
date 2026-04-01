package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.DataObject;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.agency.util.EdiFileRecord;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.agency.util.StateEdiFileRecord;
import com.intuit.sbd.payroll.psp.domain.EdiFileStatus;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.util.StringUtil;

import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 21, 2011
 * Time: 10:06:27 AM
 * State EDI files differ from EFTPS in that they use version 3050
 */
public abstract class StateEDIFile extends EdiFile {
    protected String mGroupTxnTime;

    public StateEDIFile(){
        mISAHeaderTemplate = getRecordTemplate(RecordId.EDI_SEG_ISA_4010);
    }

    protected String getGroupTxnTime() {
        return mGroupTxnTime;
    }
    
    @Override
    public void configureForTransmit() {
        createFileName(getEftpsFileType().toString(), getEdiFileType().toString(), EftpsUtil.getEdiWorkDir());
        getEdiFileRecord().setCompletionStatus(EdiFileStatus.PendingTransmission);
    }

    @Override
    public int writeISAHeader() {
        try {
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA05, "32");
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA06, StringUtil.rightPad(EftpsUtil.getConfigString("psp_edi_sender_id"), " ", 15));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA07, "01");
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA08, StringUtil.rightPad(EftpsUtil.getConfigString("psp_edi_receiver_id"), " ", 15));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA09, new SimpleDateFormat("yyMMdd").format(mCreateDate));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA10, new SimpleDateFormat("HHmm").format(mCreateDate));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA13, EftpsUtil.formatCtrlNum(getIsaControlNumber()));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA15, EftpsUtil.getConfigString("psp_edi_edi_mode"));

            return writeRecord(mISAHeaderTemplate);
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write ISA header. ", t);
        }
    }

    public EdiFileRecord getEdiFileRecord() {
        if (mEdiFileRecord == null) {
            mEdiFileRecord = new StateEdiFileRecord(this);
        }

        return mEdiFileRecord;
    }

    public StateEdiFileRecord getStateEdiFileRecord() {
        return (StateEdiFileRecord) getEdiFileRecord();
    }

    public int writeGSHeader() {
        try {
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS01, getEdiFileType().funcIdCode());
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS02, EftpsUtil.getConfigString("psp_edi_sender_id"));
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS03, EftpsUtil.getConfigString("psp_edi_receiver_code"));
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS04, new SimpleDateFormat("yyyyMMdd").format(mCreateDate));
            mGroupTxnTime = new SimpleDateFormat("HHmmss").format(mCreateDate);
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS05, mGroupTxnTime);
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS08, "004010");
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS06, EftpsUtil.formatCtrlNum(getGsControlNumber()));

            return writeRecord(mGSHeaderTemplate);
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write GS header. ", t);
        }
    }

    public <T extends DataObject> int write(DomainEntitySet<T> pData) {
        return write(pData, new EDIWrappedStringWriter(512));
    }

    public int write() {
        return write(new EDIWrappedStringWriter(512));
    }

    
}
