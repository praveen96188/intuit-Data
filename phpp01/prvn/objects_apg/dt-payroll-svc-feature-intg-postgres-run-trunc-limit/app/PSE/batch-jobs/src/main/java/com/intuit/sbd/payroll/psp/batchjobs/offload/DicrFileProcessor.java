package com.intuit.sbd.payroll.psp.batchjobs.offload;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReader;
import com.intuit.sbd.payroll.psp.common.pgp.PgpReaderFactory;
import com.intuit.sbd.payroll.psp.domain.DICRFile;
import com.intuit.sbd.payroll.psp.domain.DICRFileStatus;
import com.intuit.sbd.payroll.psp.domain.NACHAFile;
import com.intuit.sbd.payroll.psp.domain.NACHAFileStatus;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.jss.processors.DailyBatchJobsProcessor;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Mar 9, 2008
 * Time: 11:55:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class DicrFileProcessor {
    private static final SpcfLogger logger = Application.getLogger(DicrFileProcessor.class);

    static {
        Application.initialize();
        ApplicationSecondary.initialize();
    }

    // app/system exit enumeration
    private enum ExitCode {
        Nominal {public int getExitValue() {
            return 0;
        }},
        IllegalArgument {public int getExitValue() {
            return 1;
        }},
        RuntimeError {public int getExitValue() {
            return 2;
        }},
        ApplicationError {public int getExitValue() {
            return 3;
        }};

        public abstract int getExitValue();
    }

    public static void processFile(String pDicrFileName) throws RuntimeException {
        processFile(new File(pDicrFileName));
    }

    /**
     * This method processes the given DICR file. If the file is valid, a new DICRFile record will be added to the
     * database and it will be associated with the appropriate NACHAFile record. A DICR file is validated as follows:
     * <br>
     *    <bold>* The file exists and can be read</bold>
     *    <bold>* The file contains a '1' record that is exactly 94 bytes long (ACH rule)</bold>
     *    <bold>* The file contains a '9' record that is exactly 94 bytes long (ACH rule)</bold>
     * <br>
     * Only NACHAFile records with a status of Transmitted or PendingAcknowledgement will be selected and eligible
     * for association. If the File ID Modifier field within the DICR file's '1' record cannot be associated with
     * an eligible NACHAFile record in the database, then no action is taken (other than logging the error).
     * <br>
     * @param pDicrFileName The DICR file to be processed
     * @throws RuntimeException
     */
    public static void processFile(File pDicrFileName) throws RuntimeException {
        try {
            String ack1Record = null;
            String ack9Record = null;
            PgpReader reader = PgpReaderFactory.createInstance();
            reader.open(pDicrFileName);

            logger.info("Processing DICR file: " + pDicrFileName);

            // retrieve the '1' and '9' records from the dicr file
            try {
                String record;

                while (reader.ready()) {
                    record = reader.readLine();

                    if (record.startsWith("1")) {
                        ack1Record = record;
                    } else if (record.startsWith("9")) {
                        ack9Record = record;
                    }

                    if ((ack1Record != null) && (ack9Record != null)) {
                        break; // we have what we need, so break
                    }
                }
            } finally {
                reader.close();
            }

            // verify the '1' and '9' records are valid
            if (ack1Record == null) {
                throw new RuntimeException("DICR file parsing error. Unable to locate the '1' record in file " +
                                           pDicrFileName);
            } else if (ack9Record == null) {
                throw new RuntimeException("DICR file parsing error. Unable to locate the '9' record in file " +
                                           pDicrFileName);
            } else if (ack1Record.length() != 94) {
                throw new RuntimeException("DICR file parsing error. The '1' record has an invalid length of " +
                                           ack1Record.length() + " (should be 94) in file " + pDicrFileName);
            } else if (ack9Record.length() != 94) {
                throw new RuntimeException("DICR file parsing error. The '9' record has an invalid length of " +
                                           ack9Record.length() + " (should be 94) in file " + pDicrFileName);
            }

            DomainEntitySet<NACHAFile> nachaFileList =
                    BatchUtils.getNachaFilesByStatus(NACHAFileStatus.Transmitted,
                            NACHAFileStatus.PendingAcknowledgement);

            // locate the NACHAFile record that matches this DICR file
            NACHAFile nachaFile = null;
            for (NACHAFile nFile : nachaFileList) {
                // record 1, index 33 contains the file id modifier
                if (nFile.getFileIDModifier().equalsIgnoreCase(ack1Record.substring(33, 34))) {
                    nachaFile = nFile;
                    break; // we have what we need, so break
                }
            }

            if (nachaFile == null) {
                throw new RuntimeException("No NACHA files currently eligible for DICR file associations match the " +
                                           "DICR file being processed (based on the File ID Modifier field within " +
                                           "the '1' record of the DICR file " + pDicrFileName + ").");
            }

            // record 9, index 31-42 contains the total debit entry dollar amount
            SpcfMoney debitAmt = new SpcfMoney(SpcfDecimal.createInstance(ack9Record.substring(31, 41) +
                                                                          "." +
                                                                          ack9Record.substring(41, 43)));

            // record 9, index 43-54 contains the total credit entry dollar amount
            SpcfMoney creditAmt = new SpcfMoney(SpcfDecimal.createInstance(ack9Record.substring(43, 53) +
                                                                           "." +
                                                                           ack9Record.substring(53, 55)));

            // if the debit amounts between the NACHA and DICR files don't match, report as fatal, but keep processing
            if (!nachaFile.getDebitTxnTotalAmount().equals(debitAmt)) {
                String dAmt = String.format("$%(,.2f", new BigDecimal(debitAmt.toString()));
                String nAmt = String.format("$%(,.2f", new BigDecimal(nachaFile.getDebitTxnTotalAmount().toString()));

                logger.fatal("The debit amount within the DICR file (" + pDicrFileName + ") does not match the " +
                             "debit amount within its associated NACHA file (" + nachaFile.getFileName() + ") " +
                             "[NACHA debit amount (as transmitted to the bank): " + nAmt +
                             ", DICR debit amount (as received from the bank): " + dAmt + "]");
            }

            // if the credit amounts between the NACHA and DICR files don't match, report as fatal, but keep processing
            if (!nachaFile.getCreditTxnTotalAmount().equals(creditAmt)) {
                String dAmt = String.format("$%(,.2f", new BigDecimal(creditAmt.toString()));
                String nAmt = String.format("$%(,.2f", new BigDecimal(nachaFile.getCreditTxnTotalAmount().toString()));

                logger.fatal("The credit amount within the DICR file (" + pDicrFileName + ") does not match the " +
                             "credit amount within its associated NACHA file (" + nachaFile.getFileName() + ") " +
                             "[NACHA credit amount (as transmitted to the bank): " + nAmt +
                             ", DICR credit amount (as received from the bank): " + dAmt + "]");
            }

            // create the new dicr file record and associate it with the nacha file record
            DICRFile dicrFile = new DICRFile();
            dicrFile.setNACHAFile(nachaFile);
            dicrFile.setFileName(pDicrFileName.getAbsolutePath());
            dicrFile.setCreditTxnTotalAmount(creditAmt);
            dicrFile.setDebitTxnTotalAmount(debitAmt);
            dicrFile.setStatus(DICRFileStatus.Processed);
            nachaFile.setStatus(NACHAFileStatus.Acknowledged);
            nachaFile.setStatusEffectiveDate(PSPDate.getPSPTime());
            Application.save(dicrFile);

            if(DailyBatchJobsProcessor.doesPSPONACHAFileExists(nachaFile)){
                DailyBatchJobsProcessor.updatePSPONachaFile(nachaFile, NACHAFileStatus.PendingAcknowledgement, NACHAFileStatus.Acknowledged,false);
            }

            logger.info("ACH file " +
                        new File(nachaFile.getFileName()).getName() +
                        " successfully acknowledged via DICR file " +
                        pDicrFileName.getName());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void logExitError(ExitCode pExitCode, Throwable pException) {
        logger.fatal("DICR File Processor failed with exit code " + pExitCode.toString() + ".", pException);
    }

    /**
     * A simple method to show utility usage.
     */
    private static void showUsage() {
        System.out.println("Usage: DicrFileProcessor <dicrfile>");
        System.out.println("Where: <dicrfile> is the name of the DICR file to process");
        System.out.println(" Note: Only NACHAFile records in a Transmitted or PendingAcknowledgement state will be " +
                           "eligible for selection/association with the specified DICR file.");
    }

    /**
     * A main method is provided to allow execution from the command line.
     * @param args Only one argument is allowed (see showUsage method for details.)
     */
    public static void main(String[] args) {
        ExitCode exitCode = ExitCode.Nominal;

        try {
            if (args.length != 1) {
                throw new IllegalArgumentException("No command arguments specified.");
            }

            try {
                PayrollServices.beginUnitOfWork();
                DicrFileProcessor.processFile(args[0]);
                PayrollServices.commitUnitOfWork();
            }
            finally {
                PayrollServices.rollbackUnitOfWork();
            }
        } catch (IllegalArgumentException e) {
            exitCode = ExitCode.IllegalArgument;
            logExitError(exitCode, e);
            showUsage();
        } catch (RuntimeException e) {
            exitCode = ExitCode.RuntimeError;
            logExitError(exitCode, e);
        }

        System.exit(exitCode.getExitValue());
    }
}
