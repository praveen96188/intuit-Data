package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.DataObject;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.agency.util.EdiFileRecord;
import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiFileRecord;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.util.StringUtil;

import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 21, 2011
 * Time: 10:10:09 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class EftpsEDIFile extends EdiFile {

    @Override
    public int writeISAHeader() {
        try {
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA05, "30");
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA06, StringUtil.leftPad(EftpsUtil.getConfigString("psp_eftps_sender_id"), "0", 15));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA07, "12");
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA08, StringUtil.rightPad(EftpsUtil.getConfigString("psp_eftps_receiver_id"), " ", 15));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA09, new SimpleDateFormat("yyMMdd").format(mCreateDate));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA10, new SimpleDateFormat("HHmm").format(mCreateDate));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA13, EftpsUtil.formatCtrlNum(getIsaControlNumber()));
            mISAHeaderTemplate.setFieldValue(FieldId.EDI_SEG_ISA15, EftpsUtil.getConfigString("psp_eftps_edi_mode"));
            return writeRecord(mISAHeaderTemplate);
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write ISA header. ", t);
        }
    }

    public EdiFileRecord getEdiFileRecord() {
        if (mEdiFileRecord == null) {
            mEdiFileRecord = new EftpsEdiFileRecord(this);
        }

        return mEdiFileRecord;
    }

    public EftpsEdiFileRecord getEftpsEdiFileRecord() {
        return (EftpsEdiFileRecord) getEdiFileRecord();
    }

    public int writeGSHeader() {
        try {
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS01, getEdiFileType().funcIdCode());
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS02, EftpsUtil.getConfigString("psp_eftps_sender_id"));
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS03, EftpsUtil.getConfigString("psp_eftps_receiver_id"));
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS04, new SimpleDateFormat("yyMMdd").format(mCreateDate));
            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS05, new SimpleDateFormat("HHmm").format(mCreateDate));

            mGSHeaderTemplate.setFieldValue(FieldId.EDI_SEG_GS06, EftpsUtil.formatCtrlNum(getGsControlNumber()));

            return writeRecord(mGSHeaderTemplate);
        } catch (Throwable t) {
            throw new EftpsBpRuntimeException("Can't write GS header. ", t);
        }
    }

    public <T extends DataObject> int write(DomainEntitySet<T> pData) {
        return write(pData, new EDIWrappedStringWriter());       
    }

    public int write() {
        return write(new EDIWrappedStringWriter());
    }    
    
}
