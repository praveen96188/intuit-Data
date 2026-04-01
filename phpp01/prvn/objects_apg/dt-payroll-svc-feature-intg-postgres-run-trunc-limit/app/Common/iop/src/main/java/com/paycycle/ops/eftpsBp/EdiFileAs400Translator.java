package com.paycycle.ops.eftpsBp;

import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.paycycle.eftpsBp.EDIRecordTemplate;
import com.paycycle.eftpsBp.FieldId;
import com.paycycle.eftpsBp.RecordId;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Mar 12, 2011
 * Time: 4:19:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class EdiFileAs400Translator {
    private static final String mIntuitEftpsId = EftpsUtil.getConfigString("psp_eftps_sender_id");
    private static final String mAs400EftpsId = "CRI TAX SERVICE";

    /**
     * This method reads an ASCII file from the TFA and translates it to EBCDIC in preparation for transmission to
     * the AS400. It also translates the GS03 field from the Intuit TFA receiver ID to "CRI TAX SERVICE".
     * @param pSourceFile The source file from the TFA (must be ASCII)
     * @param pDestDir The destination directory where the translated file should be created
     * @return A Java temp File representing the EBCDIC/GS03 converted file to be uploaded to the AS400
     */
    public static File translateToAs400(File pSourceFile, String pDestDir) {
        try {
            EdiEftpsRecordList recordList = new EdiEftpsRecordList(pSourceFile);
            List<EDIRecordTemplate> gsRecs = recordList.getRecordListForId(RecordId.EDI_SEG_GS);

            //
            // Ensure there is only one GS segment in the file (we only support one GS segment per EFTPS EDI file)
            //
            if (gsRecs.size() != 1) {
                throw new RuntimeException(String.format("Invalid EFTPS EDI file GS segment count (%d)", gsRecs.size()));
            }

            //
            // Set the GS03 field to the AS400 expected value
            //
            gsRecs.get(0).setFieldValue(FieldId.EDI_SEG_GS03, mAs400EftpsId);

            //
            // Write the EBCDIC file (Cp1047 = IBM System 390 EBCDIC)
            //
            String prefix = recordList.getEftpsFileType().toString();
            String suffix = recordList.getEdiFileType().toString();
            String destFileName = String.format("ebcdic-%s%d.%s", prefix, recordList.getFileId(), suffix);
            File destFile = new File(pDestDir, destFileName);

            if (destFile.exists()) {
                destFile.delete();
            }

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile), "Cp1047"));

            try {
                recordList.write(writer, false); // writer automatically converts content to EBCDIC
                writer.flush();
            } finally {
                writer.close();
            }

            return destFile;
        } catch (Throwable t) {
            String msg = String.format("Unable to create temp file for EBCDIC conversion of %s ", pSourceFile.getName());
            throw new RuntimeException(msg, t);
        }
    }

    /**
     * This method reads an ASCII file from the AS400 and translates it to 80-byte wrapped ASCII in preparation for
     * transmission to the TFA. It also translates the GS02 field from "CRI TAX SERVICE" to the Intuit TFA sender ID.
     * @param pSourceFile The source file from the AS400 (must be ASCII)
     * @param pDestDir The destination directory where the translated file should be created
     * @return A Java File representing the 80-byte/GS02 converted file to be uploaded to the TFA
     */
    public static File translateFromAs400(File pSourceFile, String pDestDir) {
        try {
            //
            // Start by moving the source file to the archive dir (so it won't automatically process again)
            // The translated file will become the file of reference for all PSP processing (i.e. EftpsFile record)
            //
            File sourceFile = EftpsUtil.moveFile(pSourceFile, EftpsUtil.getArchiveDir());
            EdiEftpsRecordList recordList = new EdiEftpsRecordList(sourceFile);
            List<EDIRecordTemplate> ediRecs;

            //
            // Process the ISA segment
            //
            ediRecs = recordList.getRecordListForId(RecordId.EDI_SEG_ISA);

            if (ediRecs.size() != 1) {
                throw new RuntimeException(String.format("Invalid EFTPS EDI file ISA segment count (%d)", ediRecs.size()));
            }

            // Remove the '>' symbol from the ISA16 field to normalize the file with PSP
            ediRecs.get(0).setFieldValue(FieldId.EDI_SEG_ISA16, "");

            //
            // Process the GS segment
            //
            ediRecs = recordList.getRecordListForId(RecordId.EDI_SEG_GS);

            if (ediRecs.size() != 1) {
                throw new RuntimeException(String.format("Invalid EFTPS EDI file GS segment count (%d)", ediRecs.size()));
            }

            // Set the GS02 field to the TFA expected value
            ediRecs.get(0).setFieldValue(FieldId.EDI_SEG_GS02, mIntuitEftpsId);

            //
            // Write the 80-byte wrapped ASCII file (use PSP EFTPS EDI file naming convention)
            //
            String prefix = recordList.getEftpsFileType().toString();
            String suffix = recordList.getEdiFileType().toString();
            String destFileName = String.format("%s%d.%s", prefix, recordList.getFileId(), suffix);
            File destFile = new File(pDestDir, destFileName);

            if (destFile.exists()) {
                destFile.delete();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(destFile));

            try {
                writer.write(recordList.toString(false)); // toString(false) automatically converts to 80-byte wrapped
                writer.flush();
            } finally {
                writer.close();
            }

            return destFile;
        } catch (Throwable t) {
            String msg = String.format("Unable to create temp file for ASCII conversion of %s ", pSourceFile.getName());
            throw new RuntimeException(msg, t);
        }
    }

    /**
     * This method reads an ASCII file from the VAN and translates it to EBCDIC in preparation for transmission to
     * the AS400.
     * @param pSourceFile The source file from the VAN (must be ASCII)
     * @param pDestDir The destination directory where the translated file should be created
     * @return A Java temp File representing the EBCDIC converted file to be uploaded to the AS400
     */
    public static File translateStateEdiFileToAs400(File pSourceFile, String pDestDir) {
        try {
            StateEDIFileReader stateEDIFile = new StateEDIFileReader(pSourceFile);

            // Write the EBCDIC file (Cp1047 = IBM System 390 EBCDIC)
            String prefix = stateEDIFile.getEftpsFileType().toString();
            String suffix = stateEDIFile.getEdiFileType().toString();
            String destFileName = String.format("ebcdic-%s%d.%s", prefix, stateEDIFile.getFileId(), suffix);
            File destFile = new File(pDestDir, destFileName);

            if (destFile.exists()) {
                destFile.delete();
            }

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile), "Cp1047"));

            try {
                stateEDIFile.write(writer, false); // writer automatically converts content to EBCDIC
                writer.flush();
            } finally {
                writer.close();
            }

            return destFile;
        } catch (Throwable t) {
            String msg = String.format("Unable to create temp file for EBCDIC conversion of %s ", pSourceFile.getName());
            throw new RuntimeException(msg, t);
        }
    }
}
