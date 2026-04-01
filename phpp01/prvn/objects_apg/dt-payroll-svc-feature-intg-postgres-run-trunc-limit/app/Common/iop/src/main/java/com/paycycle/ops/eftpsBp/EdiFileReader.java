package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.domain.EdiFileType;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.EftpsBpRuntimeException;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;
import com.paycycle.fixedlen.RecordListener;
import com.paycycle.fixedlen.RecordManager;
import com.paycycle.util.XmlResourcePool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Feb 4, 2011
 * Time: 5:25:05 AM
 *
 * This is a generic EDI file reader class.  It will read any EDI file.
 */
public abstract class EdiFileReader extends RecordManager implements RecordListener {
    private XmlResourcePool mXmlResourcePool = new XmlResourcePool();
    private EDIRecordTemplate mEdiRecordTemplate;
    protected int mRecordCount = 0;
    private File mEdiFile = null;
    protected String mFileVersion; // EDI file version, EFTPS files version is - 003050 and State EDI Payments file version is - 004010
    private int counter = 0;
    protected EftpsEdiType mEftpsEdiType = null;

    protected int mPrevSegmentId; // segment id from previously read record
    protected int mFileId; // file id retrieved from field ISA13 (same as GS06)
    protected EdiFileType mEdiFileType = null;


    public EdiFileReader(File pEdiFile) {
        mXmlResourcePool.addResource("/eftps-edi-field-def.xml");

        // we'll use the generic record template to read records from the file
        mEdiRecordTemplate = (EDIRecordTemplate) mXmlResourcePool.get(RecordId.EDI_SEG_GENERIC, false);

        mEdiFile = pEdiFile;
    }

    public EdiFileReader(String pFileName) {
        this(new File(pFileName));
    }

    public XmlResourcePool getXmlResourcePool() {
        return mXmlResourcePool;
    }

    public EDIRecordTemplate getEdiRecordTemplate() {
        return mEdiRecordTemplate;
    }

    public void reset() {
        mRecordCount = 0;
    }

    public int getRecordCount() {
        return mRecordCount;
    }

    public File getEdiFile() {
        return mEdiFile;
    }

    public EftpsEdiType getEdiFileType() {
        return mEftpsEdiType;
    }

    public int getFileId() {
        return mFileId;
    }

    public EdiFileType getEftpsFileType() {
        return mEdiFileType;
    }

    @Override
    protected void setDefaultListeners() {
        addReadRecordListener(this);
    }

    public int readFile() {
        reset();

        if (mEdiFile != null) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(mEdiFile));

                try {
                    while (mEdiRecordTemplate.read(reader) > 0) {
                        ++mRecordCount;
                        notifyReadRecordCreated(translateRecord(mEdiRecordTemplate));
                    }
                } finally {
                    reader.close();
                }
            } catch (Throwable t) {
                throw new RuntimeException(String.format("Error reading EDI file %s ", mEdiFile.getPath()), t);
            }
        }

        return mRecordCount;
    }

    /**
     * Override this translation method if you need to convert the generic record prior to notifyReadRecordCreated.
     * Default is no translation (notifyReadRecordCreated will notify using generic template).
     * @param pRecordTemplate The generic record as read from the file
     * @return The translated record (typically converting from generic to specific EDIRecordTemplate)
     */
    protected EDIRecordTemplate translateRecord(EDIRecordTemplate pRecordTemplate) {
        return pRecordTemplate;
    }

    protected EDIRecordTemplate getEDIRecordTemplate(String segmentId) {
        EDIRecordTemplate template = null;
        //
        // Resolve the generic EDI record to the correct EDIRecordTemplate based on the segment id and file type.
        // If no match is found for this segment id (technically an error,) just return the generic record.
        //
        if ("ISA".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_SEG_ISA, false);
        } else if ("IEA".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_SEG_IEA, false);
        } else if ("GS".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_SEG_GS, false);
        } else if ("GE".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_SEG_GE, false);
        } else if ("ST".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_SEG_ST, false);
        } else if ("SE".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_SEG_SE, false);
        } else if ("AK1".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_997_SEG_AK1, false);
        } else if ("AK2".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_997_SEG_AK2, false);
        } else if ("AK3".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_997_SEG_AK3_4010, false);
        } else if ("AK4".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_997_SEG_AK4_4010, false);
        } else if ("AK5".equals(segmentId)) {
            if (mFileVersion != null && mFileVersion.equals("004010")) {
                template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_997_SEG_AK5_4010, false);
            } else {
                template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_997_SEG_AK5, false);
            }
        } else if ("AK9".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_997_SEG_AK9, false);
        } else if ("BTP".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_838_SEG_BTP, false);
        } else if ("PER".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_838_SEG_PER, false);
        } else if ("LX".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_838_SEG_LX, false);
        } else if ("N1".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_838_SEG_N1, false);
        } else if ("N3".equals(segmentId)) { // not used in PSP, but maybe on AS400
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_838_SEG_N3, false);
        } else if ("N4".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_838_SEG_N4, false);
        } else if ("N9".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_838_SEG_N9, false);
        } else if ("BGN".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_824_SEG_BGN, false);
        } else if ("OTI".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_824_SEG_OTI, false);
        } else if ("BPR".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_813_SEG_BPR, false);
        } else if ("FGS".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_813_SEG_FGS, false);
        } else if ("BTA".equals(segmentId)) {
            if (mFileVersion != null && mFileVersion.equals("004010")) {
                template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_151_SEG_BTA_4010, false);
            } else {
                template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_151_SEG_BTA, false);
            }
        } else if ("RIC".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_827_SEG_RIC, false);
        } else if ("B2A".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_821_SEG_B2A, false);
        } else if ("TRN".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_821_SEG_TRN, false);
        } else if ("ENT".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_821_SEG_ENT, false);
        } else if ("ACT".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_821_SEG_ACT, false);
        } else if ("FIR".equals(segmentId)) {
            template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_821_SEG_FIR, false);
        } else if ("TIA".equals(segmentId)) {
            if (mPrevSegmentId == RecordId.EDI_813_SEG_DTM) {
                template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_813_SEG_OUTER_TIA, false);
            } else { // there can be multiple inner TIA segments within a file
                template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_813_SEG_INNER_TIA, false);
            }
        } else if ("DTM".equals(segmentId)) {
            switch (mEftpsEdiType) {
                case EDI813:
                    template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_813_SEG_DTM, false);
                    break;
                case EDI821:
                    template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_821_SEG_DTM, false);
                    break;
            }
        } else if ("REF".equals(segmentId)) {
            switch (mEftpsEdiType) {
                case EDI813:
                    if (mPrevSegmentId == RecordId.EDI_813_SEG_TFS) {
                        template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_813_SEG_INNER_REF, false);
                    } else { // there can be multiple outer REF segments within a BEPS 813 file
                        template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_813_SEG_OUTER_REF, false);
                    }
                    break;
                case EDI151:
                    if (mPrevSegmentId == RecordId.EDI_151_SEG_TFS_4010) {
                        template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_151_SEG_OUTER_REF_4010, false);
                        counter = 1;
                    } else if (counter < 2 && mPrevSegmentId == RecordId.EDI_151_SEG_OUTER_REF_4010) {
                        template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_151_SEG_OUTER_REF_4010, false);
                        counter++;
                    } else if (mPrevSegmentId == RecordId.EDI_151_SEG_OUTER_REF_4010) {
                        template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_151_SEG_INNER_REF_4010, false);
                        counter = 1;
                    } else if (counter < 2 && mPrevSegmentId == RecordId.EDI_151_SEG_INNER_REF_4010) {
                        template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_151_SEG_INNER_REF_4010, false);
                        counter++;
                    } else {
                        template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_151_SEG_PBI_4010, false);
                    }
                    break;
                case EDI824:
                    template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_824_SEG_REF, false);
                    break;
                case EDI827:
                    template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_827_SEG_REF, false);
                    break;
            }
        } else if ("TFS".equals(segmentId)) {
            switch (mEftpsEdiType) {
                case EDI151:
                    if (mFileVersion != null && mFileVersion.equals("004010")) {
                        template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_151_SEG_TFS_4010, false);
                    } else {
                        template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_151_SEG_TFS, false);
                    }
                    break;
                case EDI813:
                    template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_813_SEG_TFS, false);
                    break;
                case EDI826:
                    template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_826_SEG_TFS, false);
                    break;
            }
        } else if ("BTI".equals(segmentId)) {
            switch (mEftpsEdiType) {
                case EDI151:
                    if (mFileVersion != null && mFileVersion.equals("004010")) {
                        template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_151_SEG_BTI_4010, false);
                    } else {
                        template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_151_SEG_BTI, false);
                    }
                    break;
                case EDI813:
                    template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_813_SEG_BTI, false);
                    break;
                case EDI826:
                    template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_826_SEG_BTI, false);
                    break;
            }
        } else if ("PBI".equals(segmentId)) {
            switch (mEftpsEdiType) {
                case EDI151:
                    template = (EDIRecordTemplate) getXmlResourcePool().get(RecordId.EDI_151_SEG_PBI_4010, false);
                    break;
            }
        } else {
            throw new EftpsBpRuntimeException("No EDI Record match found for segment Id =" + segmentId);
        }

        return template;
    }

    protected void setFileTypes(EDIRecordTemplate pTemplate){
        //
        // Set the EDI and EFTPS file types (driven by ST segment (and AK2 segment if processing 997 file))
        //
        switch (pTemplate.getId()) {
            //
            // If we just read the ISA segment, set the file id using field ISA13.
            //
            case RecordId.EDI_SEG_ISA:
                mFileId = pTemplate.getFieldInt(FieldId.EDI_SEG_ISA13);
                break;

            //
            // If we just read the GS segment, set the file id using field GS06.
            // (note: the AS400 has different ISA13 and GS06 field values, whereas in PSP they are the same)
            // (note: because of this, we need to use GS06 for the file id to keep everything compatible)
            // (note: once the AS400 is retired, we can go back to using ISA13 as the file id)
            //
            case RecordId.EDI_SEG_GS:
                mFileId = pTemplate.getFieldInt(FieldId.EDI_SEG_GS06);
                mFileVersion = pTemplate.getFieldValue(FieldId.EDI_SEG_GS08);
                break;

            //
            // If we just read the ST segment, set the file types so we can accurately translate later records.
            //
            case RecordId.EDI_SEG_ST:
                mEftpsEdiType = EftpsEdiType.getValueByEdiType(pTemplate.getFieldInt(FieldId.EDI_SEG_ST01));

                switch (mEftpsEdiType) {
                    case EDI151:
                        if(mFileVersion != null && mFileVersion.equals("004010")){
                            mEdiFileType = EdiFileType.StateEdiPaymentResponse;
                        } else {
                            mEdiFileType = EdiFileType.EftpsPaymentResponse;
                        }
                        break;
                    case EDI813:
                        mEdiFileType = EdiFileType.EftpsPayment;
                        break;
                    case EDI821:
                        mEdiFileType = EdiFileType.EftpsForecast;
                        break;
                    case EDI824:
                        mEdiFileType = EdiFileType.EftpsEnrollmentResponse;
                        break;
                    case EDI826:
                        mEdiFileType = EdiFileType.EftpsPaymentConfirmation;
                        break;
                    case EDI827:
                        mEdiFileType = EdiFileType.EftpsPaymentReturn;
                        break;
                    case EDI838:
                        mEdiFileType = EdiFileType.EftpsEnrollment;
                        break;
                    case EDI997:
                        // resolved when AK2 record is read
                        break;
                }
                break;

            //
            // If we just read an AK2 segment, set the eftps ack file type using field AK201 (file type being ACKed).
            //
            case RecordId.EDI_997_SEG_AK2:
                switch (EftpsEdiType.getValueByEdiType(pTemplate.getFieldInt(FieldId.EDI_997_SEG_AK201))) {
                    case EDI151: // Ack for 151 file
                        mEdiFileType = EdiFileType.EftpsPaymentResponseAck;
                        break;
                    case EDI813: // Ack for 813 file
                        if(mFileVersion != null && mFileVersion.equals("004010")){
                            mEdiFileType = EdiFileType.StateEdiPaymentAck;
                        } else {
                            mEdiFileType = EdiFileType.EftpsPaymentAck;
                        }
                        break;
                    case EDI821: // Ack for 821 file
                        mEdiFileType = EdiFileType.EftpsForecastAck;
                        break;
                    case EDI824: // Ack for 824 file
                        mEdiFileType = EdiFileType.EftpsEnrollmentResponseAck;
                        break;
                    case EDI826: // Ack for 826 file
                        mEdiFileType = EdiFileType.EftpsPaymentConfirmationAck;
                        break;
                    case EDI827: // Ack for 827 file
                        mEdiFileType = EdiFileType.EftpsPaymentReturnAck;
                        break;
                    case EDI838: // Ack for 838 file
                        mEdiFileType = EdiFileType.EftpsEnrollmentAck;
                        break;
                }
                break;
        }
    }

}
