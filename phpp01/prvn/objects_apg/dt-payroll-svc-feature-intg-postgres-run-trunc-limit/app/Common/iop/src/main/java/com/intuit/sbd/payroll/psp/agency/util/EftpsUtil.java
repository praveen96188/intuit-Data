package com.intuit.sbd.payroll.psp.agency.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.processes.TransactionThread;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.ops.eftpsBp.EDIWrappedStringWriter;
import com.paycycle.ops.eftpsBp.PaymentFile;
import com.paycycle.util.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Nov 5, 2010
 * Time: 5:19:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class EftpsUtil {
    protected static SpcfLogger logger = Application.getLogger(EftpsUtil.class);

    public static final String NEWLINE = System.getProperty("line.separator");
    public static final String WORK_DIR = "psp_eftps_ftp_work_dir";
    public static final String AS400_DIR = "psp_eftps_ftp_as400_dir";
    public static final String TFA_DIR = "psp_eftps_ftp_tfa_dir";
    public static final String ARCHIVE_DIR = "psp_eftps_ftp_arcv_dir";
    public static final String ERROR_DIR = "psp_eftps_ftp_err_dir";
    public static final String S3_BUCKET = "psp_eftps_s3_bucket";
    public static final String S3_INBOUND_FOLDER = "psp_eftps_s3_folder_inbound";
    public static final String S3_OUTBOUND_FOLDER = "psp_eftps_s3_folder_outbound";
    public static final String S3_ARCHIVE_FOLDER = "psp_eftps_s3_folder_archive";

    public static final String EDI_WORK_DIR = "psp_edi_scp_work_dir";
    public static final String EDI_AS400_DIR = "psp_edi_scp_as400_dir";
    public static final String EDI_VAN_DIR = "psp_edi_scp_van_dir";
    public static final String EDI_ARCHIVE_DIR = "psp_edi_scp_arcv_dir";
    public static final String EDI_ERROR_DIR = "psp_edi_scp_err_dir";
    public static final String EDI_FTP_AS400_DIR = "psp_edi_ftp_as400_dir";

    synchronized public static int getNextEftpsFileSequence() {
        return Application.executeTransactionThread(new TransactionThread<Integer>() {
            public Integer transaction() {
                return Application.nextSequenceValue(SequenceId.SEQ_EFTPS_FILE_SEQUENCE, Long.class).intValue();
            }
        });
    }

    synchronized public static int getNextEftpsSegmentSequence() {
        return Application.executeTransactionThread(new TransactionThread<Integer>() {
            public Integer transaction() {
                return Application.nextSequenceValue(SequenceId.SEQ_EFTPS_SEGMENT_SEQUENCE, Long.class).intValue();
            }
        });
    }

    synchronized public static int getNextEftpsPaymentSequence() {
        return Application.executeTransactionThread(new TransactionThread<Integer>() {
            public Integer transaction() {
                return Application.nextSequenceValue(SequenceId.SEQ_EFTPS_PAYMENT_SEQUENCE, Long.class).intValue();
            }
        });
    }

    public static int getEdi838MaxSegmentsPerFile() {
        return SystemParameter.findIntValue(SystemParameter.Code.EFTPS_838_MAX_SEGMENT_COUNT, 20);
    }

    public static int getEdi838MaxTransactionsPerSegment() {
        return SystemParameter.findIntValue(SystemParameter.Code.EFTPS_838_MAX_TRANSACTION_COUNT, 1000);
    }

    public static int getMaxAllowedEnrollmentsPerFile() {
        return getEdi838MaxSegmentsPerFile() * getEdi838MaxTransactionsPerSegment();
    }

    public static int getEdi813MaxSegmentsPerFile() {
        return SystemParameter.findIntValue(SystemParameter.Code.EFTPS_813_MAX_SEGMENTS_PER_FILE, 50);
    }

    public static int getEdi813MaxTransactionsPerSegment() {
        return SystemParameter.findIntValue(SystemParameter.Code.EFTPS_813_MAX_PAYMENTS_PER_SEGMENT, 1000);
    }

    public static int getEdi813MaxTransactionsPerSegment4010() {
        return SystemParameter.findIntValue(SystemParameter.Code.EDI_813_MAX_PAYMENTS_PER_SEGMENT_4010, 100000);
    }

    public static BigDecimal getEdi813MaxAchAmountPerSegment() {
        return new BigDecimal(SystemParameter.findStringValue(
                SystemParameter.Code.EFTPS_813_MAX_ACH_AMOUNT_PER_SEGMENT, "99999999.99"));
    }

    public static int getEftpsSettlementDateOffset() {
        return SystemParameter.findIntValue(SystemParameter.Code.EFTPS_813_SETTLEMENT_DATE_OFFSET, 1);
    }

    public static int getMaxAllowedPaymentsPerFile() {
        return getEdi813MaxSegmentsPerFile() * getEdi813MaxTransactionsPerSegment();
    }

    public static int getMaxPaymentsToProcessPerBatchRun() {
        return SystemParameter.findIntValue(SystemParameter.Code.EFTPS_813_MAX_PAYMENTS_TO_PROCESS_PER_BATCH_RUN, 100000);
    }

    public static String formatStripDecimal(BigDecimal pDecimalNumber) {
        return pDecimalNumber.setScale(2).toString().replaceAll("[.]", "");
    }

    public static Date getDateFromShortDateString(String pShortDateString) {
        Date date = null;

        try {
            date = new SimpleDateFormat("yyMMdd").parse(pShortDateString);
        } catch (ParseException e) {
            logger.error(String.format("Error parsing short date string %s ", pShortDateString), e);
        }

        return date;
    }

    public static Date getDateFromLongDateString(String pLongDateString) {
        Date date = null;

        try {
            date = new SimpleDateFormat("yyyyMMdd").parse(pLongDateString);
        } catch (ParseException e) {
            logger.error(String.format("Error parsing short date string %s ", pLongDateString), e);
        }

        return date;
    }

    /**
     * Formats the given date as YYMMDD
     *
     * @param pDate : The date to format
     * @return : The formatted date string as YYMMDD
     */
    public static String formatShortDate(Date pDate) {
        return new SimpleDateFormat("yyMMdd").format(pDate);
    }

    /**
     * Formats the given date as CCYYMMDD
     *
     * @param pDate : The date to format
     * @return : The formatted date string as CCYYMMDD
     */
    public static String formatLongDate(Date pDate) {
        return new SimpleDateFormat("yyyyMMdd").format(pDate);
    }

    /**
     * Control numbers are formatted to a fixed-length of 9 digits (left padded with zeros)
     *
     * @param pNumber : The given control number to format
     * @return : The formatted control number as a string
     */
    public static String formatCtrlNum(int pNumber) {
        return StringUtil.numberFormat(pNumber, "000000000");
    }

    /**
     * Reference numbers are formatted to a fixed-length of 8 digits (left padded with zeros)
     *
     * @param pNumber : The given reference number to format
     * @return : The formatted reference number as a string
     */
    public static String formatRefNum(int pNumber) {
        return StringUtil.numberFormat(pNumber, "00000000");
    }

    /**
     * A File Id Modifier is built from a given date and the next EFTPS_FILE_SEQUENCE.
     * <br>
     * If has the format: YYMMDD###
     * where: YYMMDD is the formatted form of the given date (converted to an integer)
     * and: ### is the next EFTPS_FILE_SEQUENCE modulo 1000
     * <br>
     * A File Id Modifier is always 9 digits and is constructed as: (YYMMDD * 1000) + (EFTPS_FILE_SEQUENCE % 1000)
     *
     * @param pDate : The date to use to construct the file id modifier.
     * @return : A unique file id modifier string for the given date (formatted as: YYMMDD###)
     */
    public static int getNewFileIdModifier(Date pDate) {
        return (Integer.parseInt(formatShortDate(pDate)) * 1000) + (getNextEftpsFileSequence() % 1000);
    }

    /**
     * The next Segment Control Number from EFTPS_SEGMENT_SEQUENCE.
     * A Segment Control Number must be a minimum of 4 and a maximum of 9 digits.
     *
     * @return : The next unique Segment Control Number
     */
    public static int getNewSegmentControlNumber() {
        return getNextEftpsSegmentSequence() % 1000000000; // max len is 9, so mask off > 9 significant digits
    }

    /**
     * The next Payment Reference Number from EFTPS_PAYMENT_SEQUENCE.
     *
     * @return : The next unique Payment Reference Number
     */
    public static int getNewPaymentReferenceNumber() {
        return getNextEftpsPaymentSequence() % 100000000; // max len is 8, so mask off > 8 significant digits
    }

    public static String getConfigString(String pKey) {
        return ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, pKey);
    }

    public static String getConfigString(String pKey, String pDefaultValue) {
        return ConfigurationManager.getSettingValue(ConfigurationModule.TaxAgency, pKey, pDefaultValue);
    }

    public static String getWorkDir() {
        return getConfigString(WORK_DIR);
    }

    public static String getAS400Dir() {
        return getConfigString(AS400_DIR);
    }

    public static String getTfaDir() {
        return getConfigString(TFA_DIR);
    }

    public static String getErrDir() {
        return getConfigString(ERROR_DIR);
    }

    public static String getS3Bucket() { return getConfigString(S3_BUCKET); }

    public static String getS3InboundFolder() { return getConfigString(S3_INBOUND_FOLDER); }

    public static String getS3OutboundFolder() { return getConfigString(S3_OUTBOUND_FOLDER); }

    public static String getS3ArchiveFolder() { return getConfigString(S3_ARCHIVE_FOLDER); }

    public static String getArchiveDir() {
        return getConfigString(ARCHIVE_DIR);
    }

    public static String getEdiWorkDir() {
        return getConfigString(EDI_WORK_DIR);
    }

    public static String getEdiAS400Dir() {
        return getConfigString(EDI_AS400_DIR);
    }

    public static String getEdiVanDir() {
        return getConfigString(EDI_VAN_DIR);
    }

    public static String getEdiErrDir() {
        return getConfigString(EDI_ERROR_DIR);
    }

    public static String getEdiArchiveDir() {
        return getConfigString(EDI_ARCHIVE_DIR);
    }

    public static String getEdiFtpAs400Dir() {
        return getConfigString(EDI_FTP_AS400_DIR);
    }

    public static File moveFile(String pSourceFile, String pDestDir) {
        return moveFile(pSourceFile, pDestDir, null);
    }

    public static File moveFile(File pSourceFile, String pDestDir) {
        return moveFile(pSourceFile, pDestDir, null);
    }

    public static File moveFile(String pSourceFile, String pDestDir, String pCharsetName) {
        return moveFile(new File(pSourceFile), pDestDir, pCharsetName);
    }

    public static File moveFile(File pSourceFile, String pDestDir, String pCharsetName) {
        File newFile = copyFile(pSourceFile, pDestDir, pCharsetName);
        pSourceFile.delete(); // delete the source file
        return newFile;
    }

    public static File copyFile(String pSourceFile, String pDestDir) {
        return copyFile(new File(pSourceFile), pDestDir);
    }

    public static File copyFile(File pSourceFile, String pDestDir) {
        return copyFile(pSourceFile, pDestDir, null);
    }

    public static File copyFile(File pSourceFile, String pDestDir, String pCharsetName) {
        try {
            File destFile = new File(pDestDir, pSourceFile.getName());
            BufferedReader reader;
            BufferedWriter writer;

            if (pCharsetName != null) {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(pSourceFile), pCharsetName));
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile), pCharsetName));
            } else {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(pSourceFile)));
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile)));
            }

            try {
                char buf[] = new char[1024];
                int bytesRead;

                while ((bytesRead = reader.read(buf)) > 0) {
                    writer.write(buf, 0, bytesRead);
                }

                writer.flush();
            } finally {
                writer.close();
                reader.close();
            }

            return destFile;
        } catch (Throwable t) {
            String msg = String.format("Error copying file (source: %s, dest: %s)", pSourceFile.getPath(), pDestDir);
            throw new RuntimeException(msg, t);
        }
    }

    public static List<File> getFilesFromDir(String pFileRecvDir) {
        logger.info(String.format("Begin getFilesFromDir for %s", pFileRecvDir));

        File mRecvDir = new File(pFileRecvDir);

        if (!mRecvDir.exists()) {
            throw new RuntimeException(String.format("Specified path does not exist: %s", mRecvDir.getPath()));
        } else if (!mRecvDir.isDirectory()) {
            throw new RuntimeException(String.format("Specified path is not a directory: %s", mRecvDir.getPath()));
        }

        FileFilter ff = new FileFilter() {
            public boolean accept(File file) {
                return !file.isDirectory() && !file.isHidden();
            }
        };

        File mFiles[] = mRecvDir.listFiles(ff);

        logger.info(String.format("Return getFilesFromDir for %s with file count %d", pFileRecvDir, mFiles.length));

        return Arrays.asList(mFiles);
    }

    public static DomainEntitySet<MoneyMovementTransaction> getPendingPayments(PaymentFile.PaymentFileMode pPaymentMode,
                                                                               int pMaxRows) {
        PaymentMethod paymentMethod;

        switch (pPaymentMode) {
            case PFM_100K: {
                paymentMethod = PaymentMethod.EFTPSDirectDebit;
                break;
            }
            default: {
                paymentMethod = PaymentMethod.EFTPS;
                break;
            }
        }
        return MoneyMovementTransaction.getPendingTaxPaymentsForDate(paymentMethod, PSPDate.getPSPTime(), pMaxRows);
    }

    public static void updateEftpsFileErrorStatus(SpcfUniqueId pUniqueId, File pFile) {
        boolean manageTransaction = !Application.hasActiveTransaction();

        try {
            if (pFile.exists()) {
                pFile = EftpsUtil.moveFile(pFile, EftpsUtil.getErrDir());
            }

            if (manageTransaction) {
                Application.beginUnitOfWork();
            }

            EftpsFile.updateErrorStatus(pUniqueId, pFile.getPath());

            if (manageTransaction) {
                Application.commitUnitOfWork();
            }
        } catch (Throwable t) {
            logger.error(String.format("Error updating EftpsFile record to 'Error' status (file: %s)", pFile.getPath()), t);
        } finally {
            if (manageTransaction) {
                Application.rollbackUnitOfWork();
            }
        }
    }

    public static void updateEftpsFileErrorStatus(int pFileId, File pFile, String pErrDirectory) {
        boolean manageTransaction = !Application.hasActiveTransaction();

        try {
            if (pFile.exists()) {
                pFile = EftpsUtil.moveFile(pFile, pErrDirectory);
            }

            if (manageTransaction) {
                Application.beginUnitOfWork();
            }

            EdiTaxFile ediTaxFile = EdiTaxFile.getEdiFileByFileId(pFileId);
            if(ediTaxFile != null) {
                ediTaxFile.updateErrorStatus(pFile.getPath());
            }

            if (manageTransaction) {
                Application.commitUnitOfWork();
            }
        } catch (Throwable t) {
            logger.error(String.format("Error updating EdiTaxFile record to 'Error' status (file: %s)", pFile.getPath()), t);
        } finally {
            if (manageTransaction) {
                Application.rollbackUnitOfWork();
            }
        }
    }

    public static File convertAsciiToEbcdic(File pSourceFile) {
        try {
            File destFile = File.createTempFile("ebcdic-", null, new File(getWorkDir()));
            BufferedReader reader = new BufferedReader(new FileReader(pSourceFile));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile), "Cp1047"));

            //
            // Cp1047 = IBM System 390 EBCDIC
            //

            try {
                while (reader.ready()) {
                    writer.write(reader.readLine()); // automatically converts the buffer to EBCDIC
                }

                writer.flush();
            } finally {
                writer.close();
                reader.close();
            }

            return destFile;
        } catch (Throwable t) {
            String msg = String.format("Unable to create temp file for EBCDIC conversion of %s ", pSourceFile.getName());
            throw new RuntimeException(msg, t);
        }
    }

    public static File convertEbcdicToAscii(File pSourceFile) {
        try {
            File destFile = File.createTempFile("ascii-", null, new File(getWorkDir()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(pSourceFile), "Cp1047"));
            BufferedWriter writer = new BufferedWriter(new FileWriter(destFile));
            EDIWrappedStringWriter wrappedWriter = new EDIWrappedStringWriter(); // so output will be 80-byte wrapped

            //
            // Cp1047 = IBM System 390 EBCDIC
            //

            try {
                char buf[] = new char[1024]; // no crlf in EBCDIC, so read source file in chunks
                int bytesRead;

                while ((bytesRead = reader.read(buf)) > 0) {
                    wrappedWriter.write(new String(buf, 0, bytesRead)); // automatically converts the buffer to ASCII
                }

                writer.write(wrappedWriter.toString());
                writer.flush();
            } finally {
                writer.close();
                reader.close();
            }

            return destFile;
        } catch (Throwable t) {
            String msg = String.format("Unable to create temp file for ASCII conversion of %s ", pSourceFile.getName());
            throw new RuntimeException(msg, t);
        }
    }

    public static void cleanDirectory(String folderPath) {
        File folder = new File(folderPath);
        // Get all files
        File[] files = folder.listFiles();
        // Delete all files
        for (File file : files) {
            file.delete();
        }
    }
}
