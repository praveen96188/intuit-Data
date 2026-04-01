package com.paycycle.ops.eftpsBp;

import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.FieldId;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Feb 4, 2011
 * Time: 5:40:13 AM
 *
 * This is a generic EFTPS EDI file reader class.  It will read any EFTPS EDI file, translating the generic EDI record
 * template of the base class to the appropriate EFTPS EDI record template for the segment that was read.
 */
public abstract class EdiEftpsFileReader extends EdiFileReader {  

    public EdiEftpsFileReader(File pEdiFile) {
        super(pEdiFile);
    }

    public EdiEftpsFileReader(String pFileName) {
        super(pFileName);
    }

    @Override
    public void reset() {
        super.reset();
        mFileId = 0;
        mPrevSegmentId = 0;
        mEdiFileType = null;
        mEdiFileType = null;
    }

    /**
     * Overrides the base class translateRecord to translate the generic EDI record into the correct record type for
     * the EFTPS file being read (i.e. ISA, GS, ST, as well as the file specific segment types based on the type of
     * EFTPS file being read - 838, 813, 151, etc.)
     *
     * @param pRecordTemplate The generic record as read from the file
     * @return The translated EDI record based on the type of EFTPS file being read
     */
    @Override
    protected EDIRecordTemplate translateRecord(EDIRecordTemplate pRecordTemplate) {
        String segmentId = pRecordTemplate.getField(FieldId.SEGMENT_HEADER).getValue();
        EDIRecordTemplate template = null;

        try {
            template = getEDIRecordTemplate(segmentId);
        } catch (Throwable t) {
            template = pRecordTemplate;
        }

        //
        // Transfer the data buffer from the generic record to the type-specific record.
        //
        template.setRecordBuffer(pRecordTemplate);

        setFileTypes(template);

        //
        // Save the 'prev segment id' so we can resolve similar record types (i.e. inner/outer TIA/REF in 813)
        //
        mPrevSegmentId = template.getId();

        return template;
    }
}
