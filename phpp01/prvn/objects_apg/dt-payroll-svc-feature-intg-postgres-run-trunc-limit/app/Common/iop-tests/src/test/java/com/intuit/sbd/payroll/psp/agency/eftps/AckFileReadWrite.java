package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.ops.eftpsBp.AckFile;
import com.paycycle.util.StringUtil;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 16, 2010
 * Time: 11:23:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class AckFileReadWrite extends AckFile {
    private String outboundDir = null;

    public AckFileReadWrite(String pOutboundDir) {
        super();
        outboundDir = pOutboundDir;
    }

    public AckFileReadWrite(EftpsEdiType pEdiAckType, String pAckGsCtrlNum,String pOutboundDir) {
        this(pOutboundDir);

        setEdiAckType(pEdiAckType);
        setAckGsCtrlNum(pAckGsCtrlNum);

        outboundDir = pOutboundDir;
        if(outboundDir == null){
            outboundDir = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency,  EftpsUtil.WORK_DIR);
        }
        String fileExtn = "E"+new SimpleDateFormat("DDDHHmmssSSS").format(getCreateDate());
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

    public void configureForTransmit() {
    }

    public String getOutboundDir() {
        return outboundDir;
    }

    public void setOutboundDir(String outboundDir) {
        this.outboundDir = outboundDir;
    }
    
    protected void createFileName(String pFilePrefix, String pFileExtension, String pDirectory) {

        String ext = "E"+new SimpleDateFormat("DDDHHmmssSSS").format(getCreateDate());

        String fileName = String.format("%s.%s.pgp", pFilePrefix, ext);
        boolean validDir = ((pDirectory != null) && (pDirectory.length() > 0));

        setFileName(validDir ? new File(new File(pDirectory), fileName).getPath() : fileName);
    }

}
