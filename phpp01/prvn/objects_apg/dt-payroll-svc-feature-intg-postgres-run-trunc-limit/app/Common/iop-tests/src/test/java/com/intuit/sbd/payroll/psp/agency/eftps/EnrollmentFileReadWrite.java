package com.intuit.sbd.payroll.psp.agency.eftps;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.fixedlen.RecordListener;
import com.paycycle.fixedlen.RecordTemplate;
import com.paycycle.ops.eftpsBp.AckFile;
import com.paycycle.ops.eftpsBp.EnrollmentFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Dec 10, 2010
 * Time: 2:43:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnrollmentFileReadWrite extends EnrollmentFile implements RecordListener {

    private String outboundDir = null;
    private AckFile mAckFile = null;
    private EnrollmentResponseFileReadWrite mEnrollmentResponseFile = null;
    private EnrollResponseSegInfo enrollResponseSegInfo = null;
    private List<RejectionInfo> rejectionInfos = new ArrayList<RejectionInfo>();
    protected EDIRecordTemplate mLxRecordTemplate;
    protected EDIRecordTemplate mN1RecordTemplate;
    protected EDIRecordTemplate mN4RecordTemplate;
    protected EDIRecordTemplate mN9RecordTemplate;

    public EnrollmentFileReadWrite(String pOutputFoldername) {
        mBtpRecordTemplate = getRecordTemplate(RecordId.EDI_838_SEG_BTP);
        mPerRecordTemplate = getRecordTemplate(RecordId.EDI_838_SEG_PER);
        mLxRecordTemplate = getRecordTemplate(RecordId.EDI_838_SEG_LX);
        mN1RecordTemplate = getRecordTemplate(RecordId.EDI_838_SEG_N1);
        mN4RecordTemplate = getRecordTemplate(RecordId.EDI_838_SEG_N4);
        mN9RecordTemplate = getRecordTemplate(RecordId.EDI_838_SEG_N9);

        outboundDir = pOutputFoldername;

        if(outboundDir == null){
            outboundDir = ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, EftpsUtil.WORK_DIR);
        }

        String fileExtn = "E" + new SimpleDateFormat("DDDHHmmssSSS").format(getCreateDate());
        createFileName("EFTPSTX", fileExtn, outboundDir);

        addReadRecordListener(this);
    }

    @Override
    protected int readContent() {
        int count = 0;

        // read the ISA header record
        readRecord(getISAHeaderTemplate());
        // read the GS header record
        readRecord(getGSHeaderTemplate());
        mEnrollmentResponseFile.setRejectionInfos(rejectionInfos);
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

            List<String> lxLoopIds = new ArrayList<String>();
            //read BTP
            readRecord(getBtpRecordTemplate());
            //read PER
            readRecord(getPerRecordTemplate());

            while (true) {
                String lxSeg = peekNextSegmentCode();

                if (lxSeg == null) {
                    // We hit EOF.  But where's the SE, GE, IEA???
                    // Throw an exception, since the file is incomplete.
                    throw new EftpsBpRuntimeException("Incomplete Enrollment Response file.  Expected OTI or SE segment.");
                }
                if (lxSeg.startsWith("SE")) {
                    // read the SE trailer record (also writes the ack info in notify handler)
                    readRecord(getSETrailerTemplate());
                    break;
                }
                if(lxSeg.startsWith("LX")){
                    // read the LX Loop
                    count += readRecord(mLxRecordTemplate);
                }
                if(lxSeg.startsWith("N1")){
                    // read the N1 Loop
                    count += readRecord(mN1RecordTemplate);
                }
                if(lxSeg.startsWith("N4")){
                    // read the N4 Loop
                    count += readRecord(mN4RecordTemplate);
                }
                if(lxSeg.startsWith("N9")){
                    // read the N9 Loop
                    count += readRecord(mN9RecordTemplate);
                }
            }
        }
        // read the GE trailer record
        readRecord(getGETrailerTemplate());
        // read the IEA trailer record
        readRecord(getIEATrailerTemplate());
        // finally, write the Ack file
        mAckFile.write();
        //finally, write 824(Enrollment response) file
        mEnrollmentResponseFile.write();

        return count;
    }

    public void recordCreated(RecordTemplate template) {
        if(template == getGSHeaderTemplate()){
            mAckFile = new AckFileReadWrite(getEdiFileType(), getGSHeaderTemplate().getFieldValue(FieldId.EDI_SEG_GS06), getOutboundDir());
            mEnrollmentResponseFile = new EnrollmentResponseFileReadWrite(getOutboundDir());
        } else if(template == getSTHeaderTemplate()){
            enrollResponseSegInfo = new EnrollResponseSegInfo(getSTHeaderTemplate().getFieldValue(FieldId.EDI_SEG_ST02));
        } else if (template == getSETrailerTemplate()) {
            mAckFile.queueAckToWrite(getEdiFileType(), getSTHeaderTemplate().getFieldValue(FieldId.EDI_SEG_ST02));
            mEnrollmentResponseFile.queueEnrollResponseToWrite(enrollResponseSegInfo);
        } else if(template == mLxRecordTemplate){
            enrollResponseSegInfo.addTransactioId(template.getFieldValue(FieldId.EDI_838_SEG_LX01));
        }
    }

    public void setRejectionInfos(List<RejectionInfo> rejectionInfos) {
        this.rejectionInfos = rejectionInfos;
    }

    public String getAckFileName(){
        if(mAckFile != null)
            return mAckFile.getFileName();
        else
            return null;
    }

    public String getEnrollmentResponseFileName(){
        if(mEnrollmentResponseFile != null)
            return mEnrollmentResponseFile.getFileName();
        else
            return null;
    }

    public void configureForTransmit() {
    }

    public String getOutboundDir() {
        return outboundDir;
    }

    public void setOutboundDir(String outboundDir) {
        this.outboundDir = outboundDir;
    }
}
