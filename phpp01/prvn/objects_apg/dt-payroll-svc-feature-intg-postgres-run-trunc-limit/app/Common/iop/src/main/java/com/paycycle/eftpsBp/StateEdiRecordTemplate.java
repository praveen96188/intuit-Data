package com.paycycle.eftpsBp;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.paycycle.delimlen.DelimFieldTemplate;
import com.paycycle.fixedlen.FieldTemplate;
import com.paycycle.fixedlen.RecordManagerException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 26, 2011
 * Time: 4:18:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class StateEdiRecordTemplate extends EDIRecordTemplate {

    private final static ArrayList<String> segmentCodes = new ArrayList<String>(Arrays.asList("ISA~", "GS~", "ST~", "IEA~", "SE~", "GE~", "AK1~", "AK2~", "AK3~", "AK4~", "AK5~", "AK9~", "BTA~", "BTI~", "TFS~", "REF~", "PBI~", "DTM~", "FIR~",
                                                                                        "BTP~", "PER~", "LX~", "N1~", "N2~", "N3~", "N4~", "N9~", "BGN~", "OTI~", "BPR~", "FGS~", "RIC~", "B2A~", "TRN~", "ENT~", "ACT~", "TIA~"));

    public StateEdiRecordTemplate(final EDIRecordTemplate pSource) {
        super(pSource);

        // Fields will be cloned in base class, so just copy source record buffer (no need to parse into fields)
        mRecordBuffer = new StringBuilder(pSource.mRecordBuffer);
    }

    /**
     * Writes the field contents to the writer.
     */
    public int write(Writer pWriter) throws IOException, RecordManagerException {
        if (pWriter == null) {
            return 0;
        }

        int count = 0;

        for (FieldTemplate field : getFieldList()) {
            String buf = field.getFormattedValue();

            // If delimiter is required then it is appended to the field.
            if (((DelimFieldTemplate) field).getDelimiterReqd()) {
                buf += getDelimiter();
            }

            if(buf.equals(RecordId.EDI_SEGMENT_SEP)) {
                pWriter.write(FieldId.EDI_FIELD_SEP);
            } else {
                pWriter.write(buf);
            }

            count += buf.length();
        }

        // include carriage return for each record.
        if ((count > 0) && mIncludeCarriageReturn) {
            pWriter.write(EftpsUtil.NEWLINE);
        }

        return count;
    }

    public int read(Reader pReader, int pRecordReadLimit) throws IOException, RecordManagerException {
        char buf[] = new char[1];
        int readLimit = pRecordReadLimit;

        //Create a new record buffer for each read operation
        StringBuilder recordBuffer = new StringBuilder();

        for (int fieldIndex = 0; fieldIndex < mFields.size(); ++fieldIndex) {
            FieldTemplate field = mFields.elementAt(fieldIndex);

            // Don't want to read a field for segment separator field as segment separator are not present in State EDI files from VAN 
            if (field.getId() != FieldId.SEGMENT_SEPARATOR) {
                
                // Create a new field buffer for each read operation
                // (allows others to cache previously read records via getRecordBuffer, if desired)
                StringBuilder buffer = new StringBuilder();

                // Hopefully the Reader is a BufferedReader for efficiency.
                while (pReader.read(buf, 0, 1) > 0) {
                    //
                    // Skip newline chars and continue reading
                    //
                    if ("\r\n".indexOf(buf[0]) < 0) {
                        buffer.append(buf[0]);

                        // If we hit the end of the field, stop reading
                        if (FieldId.EDI_FIELD_SEP.indexOf(buf[0]) >= 0) {
                            break;
                        } else if (--readLimit < 0) {

                            // Assert a read limit as a defensive measure to stop junk files from overwhelming the system.
                            throw new RuntimeException(String.format("Record read limit exceeded (%d)", pRecordReadLimit));
                        }
                    }
                }
                recordBuffer.append(buffer);
                if(recordBuffer.toString().equals("AK3~") || recordBuffer.toString().equals("AK4~")) {
                    continue; // Skip to check for next field for segment code, second field of AK3/AK4 records is mandatory and will be segment code in which errors are found
                }
                String nextSegmentCd = peekNextSegCode(pReader);
                if(nextSegmentCd != null && nextSegmentCd.indexOf(FieldId.EDI_FIELD_SEP) >= 0
                        && segmentCodes.contains(nextSegmentCd.substring(0, nextSegmentCd.indexOf(FieldId.EDI_FIELD_SEP)+1))){
                    break;
                }
            }
        }

        //
        // Populate the fields with the new record data (trimming any whitespace)
        //
        setRecordBuffer(new StringBuilder(recordBuffer.toString().trim()));

        return mRecordBuffer.length();
    }

}
