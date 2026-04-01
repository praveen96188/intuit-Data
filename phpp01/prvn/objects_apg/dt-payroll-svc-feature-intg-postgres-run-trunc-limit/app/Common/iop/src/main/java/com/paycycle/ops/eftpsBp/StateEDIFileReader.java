package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.domain.EdiFileType;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.StateEdiRecordTemplate;
import com.paycycle.fixedlen.RecordTemplate;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 26, 2011
 * Time: 5:34:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class StateEDIFileReader extends EdiFileReader {
    private List<EDIRecordTemplate> mRecordList = new ArrayList<EDIRecordTemplate>();

    public StateEDIFileReader(File pEdiFile) {
        super(pEdiFile);
        readFile();
    }

    public StateEDIFileReader(String pFileName) {
        super(pFileName);
        readFile();
    }

    @Override
    public void reset() {
        super.reset();
        mFileId = 0;
        mPrevSegmentId = 0;
        mEdiFileType = null;
        mEdiFileType = null;
    }

    public EftpsEdiType getEdiFileType() {
        return mEftpsEdiType;
    }

    public EdiFileType getEftpsFileType() {
        return mEdiFileType;
    }

    public int getFileId() {
        return mFileId;
    }

    public List<EDIRecordTemplate> getRecordList() {
        return mRecordList;
    }

    public int readFile() {
        reset();

        if (getEdiFile() != null) {
            try {

                BufferedReader reader = new BufferedReader(new FileReader(getEdiFile()));

                String nextSegmentCode = StateEdiRecordTemplate.peekNextSegCode(reader);
                try {
                    while (nextSegmentCode != null) {
                        readRecord(nextSegmentCode, reader);
                        ++mRecordCount;
                        nextSegmentCode = StateEdiRecordTemplate.peekNextSegCode(reader);
                    }
                } finally {
                    reader.close();
                }

                
            } catch (Throwable t) {
                throw new RuntimeException(String.format("Error reading EDI file %s ", getEdiFile().getPath()), t);
            }
        }

        return mRecordCount;
    }

    /**
     * Overrides the base class translateRecord to translate the generic EDI record into the correct record type for
     * the EFTPS file being read (i.e. ISA, GS, ST, as well as the file specific segment types based on the type of
     * EFTPS file being read - 838, 813, 151, etc.)
     *
     * @param nextSegmentHeader The generic record as read from the file
     * @param pReader The generic record as read from the file
     * @return The translated EDI record based on the type of EFTPS file being read
     */

    protected EDIRecordTemplate readRecord(String nextSegmentHeader, BufferedReader pReader) throws Exception {

        String segmentId = null;
        if(nextSegmentHeader != null && nextSegmentHeader.indexOf(FieldId.EDI_FIELD_SEP) >= 0) {
            segmentId = nextSegmentHeader.substring(0, nextSegmentHeader.indexOf(FieldId.EDI_FIELD_SEP)); 
        }
        StateEdiRecordTemplate template;

         try {
            template = new StateEdiRecordTemplate(getEDIRecordTemplate(segmentId));
        } catch (Throwable t) {
            throw new RuntimeException("Error reading State EDI file", t);
        }

        template.read(pReader);
        notifyReadRecordCreated(template);
                
        setFileTypes(template);

        return template;
    }

    public void recordCreated(RecordTemplate template) {
        // Want a copy since xml resource pool reuses templates (and we don't want to keep reloading the resource)
        mRecordList.add((EDIRecordTemplate)template.clone());
    }

    public void write(Writer pWriter, boolean pWantCrlf) {
        try {
            for (EDIRecordTemplate template : mRecordList) {
                StateEdiRecordTemplate sRecordTemplate = new StateEdiRecordTemplate(template);
                sRecordTemplate.setIncludeCarriageReturn(pWantCrlf);
                sRecordTemplate.write(pWriter);
                sRecordTemplate.setIncludeCarriageReturn(false); // for neatness and consistency
            }
        } catch (Throwable t) {
            throw new RuntimeException("Error writing EdiEftpsRecordList to writer. ", t);
        }
    }    

    public String toString(boolean pFormatted) {
        Writer writer = pFormatted ? new StringWriter() : new EDIWrappedStringWriter();

        write(writer, pFormatted);

        return writer.toString();
    }
}
