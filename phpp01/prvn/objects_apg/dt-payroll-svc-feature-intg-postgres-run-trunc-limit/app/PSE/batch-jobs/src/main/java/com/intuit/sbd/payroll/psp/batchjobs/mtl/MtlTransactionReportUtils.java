package com.intuit.sbd.payroll.psp.batchjobs.mtl;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbg.shared.filestore.FileStore;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kmuthurangam
 * <p>
 * Utility class for MTL Transactional Report enrichment
 * </p>
 */
public class MtlTransactionReportUtils {

    private static final SpcfLogger logger = Application.getLogger(MtlTransactionReportUtils.class);

    public static final String[] MTL_TRANSACTION_REPORT_FILE_EXTENSIONS = new String[]{"csv", "CSV"};
    public static final String FILE_EXTENSION_PGP = "pgp";

    // PSP S3 configuration
    public static final String MTL_S3_BUCKET = "mtl_s3_bucket";
    public static final String MTL_OUTBOUND_FOLDER = "mtl_outbound_folder";
    public static final String MTL_ARCHIVE_FOLDER = "mtl_archive_folder";

    public static final String MTL_WORK_FOLDER = "mtl_work_folder";

    // Partner S3 configuration
    public static final String MTL_PARTNER_S3_BUCKET = "mtl_partner_s3_bucket";
    public static final String MTL_PARTNER_INBOUND_FOLDER = "mtl_partner_inbound_folder";

    public static final String MTL_BATCH_SIZE = "mtl_batch_size";

    public static final String MTL_TRANSACTION_REPORT_HEADERS = "mtl_transaction_report_headers";
    public static final String MTL_BEAN_COLUMN_MAPPING = "mtl_bean_column_mapping";

    public static final String ENRICHED_FILE = "enriched";

    public static void downloadAllReports() throws Exception {
        //String region = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_s3_bucket_region");
        //String s3BucketAssumeRole = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "mtl_partner_s3_iam_assume_role");
        FileStore fileStore = S3UploadUtils.getFileStore();
        S3UploadUtils.downloadAndArchiveAllFiles(fileStore, getMtlPartnerS3Bucket(), getMtlPartnerInboundFolder(), getMtlWorkFolder(), getMtlArchiveFolder());
        decryptAllRawReports(Paths.get(getMtlWorkFolder()));
    }

    public static void enrichAllReports() throws IOException {
        AtomicInteger noOfReportsProcessed = new AtomicInteger();
        Files.list(Paths.get(MtlTransactionReportUtils.getMtlWorkFolder())).forEach((filePath) -> {
            boolean enriched = enrichReport(filePath);
            if (enriched) {
                noOfReportsProcessed.getAndIncrement();
            }
        });

        if (noOfReportsProcessed.get() == 0) {
            logger.info("No files found to process");
        }
    }


    public static boolean enrichReport(Path sourcePath) {
        if (!isValidRawReport(sourcePath.toString())) {
            logger.info(String.format("Skipped enriching MtlTransaction Report with filename - %s as it is not a csv file", sourcePath.getFileName()));
            return false;
        }

        if (isEnrichedReport(sourcePath.toString())) {
            logger.info(String.format("Skipped enriching MtlTransaction Report with filename - %s as it is not a csv file", sourcePath.getFileName()));
            return false;
        }

        MtlTransactionReportEnricher mtlTransactionReportEnricher = null;
        try {
            String enrichedFileName = MtlTransactionReportUtils.getEnrichedReportFileName(sourcePath.toString());
            Path targetPath = Paths.get(sourcePath.getParent().toString(), enrichedFileName);

            logger.info(String.format("Started enriching MTL Transaction Report - %s, Enriched MTL Transaction Report Name - %s", sourcePath.getFileName(), targetPath.getFileName()));
            mtlTransactionReportEnricher = new MtlTransactionReportEnricher(sourcePath, targetPath);
            mtlTransactionReportEnricher.enrichReport();
            logger.info(String.format("Completed enriching MTL Transaction Report - %s, Enriched MTL Transaction Report Name - %s", sourcePath.getFileName(), targetPath.getFileName()));

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (Objects.nonNull(mtlTransactionReportEnricher)) {
                try {
                    mtlTransactionReportEnricher.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return true;
    }

    public static void uploadAllEnrichedReports() throws IOException {
        logger.info("Started uploading all enriched MTL Transaction Reports");
        S3UploadUtils.uploadAllFiles(getMtlWorkFolder(), FILE_EXTENSION_PGP, getMtlS3Bucket(), getMtlOutboundFolder());
        logger.info("Completed uploading all enriched MTL Transaction Reports");
    }

    public static void encryptAndArchiveAllReports() throws Exception {
        logger.info("Started archiving all enriched MTL Transaction Reports");
        Path workPath = Paths.get(getMtlWorkFolder());
        encryptAllEnrichedReports(workPath);
        S3UploadUtils.uploadAllFiles(getMtlWorkFolder(), "enriched.pgp", getMtlS3Bucket(), getMtlArchiveFolder());
        logger.info("Completed archiving all enriched MTL Transaction Reports");
    }

    private static void encryptAllEnrichedReports(Path path) throws Exception {
        Files.list(path).forEach(filePath -> {
            try {
                encryptEnrichedReport(filePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void encryptEnrichedReport(Path path) throws Exception {
        if (!isEnrichedReport(path.toString())) {
            logger.info(String.format("Skipped encrypting MtlTransaction Report with filename - %s as it is not a csv file", path.getFileName()));
            return;
        }
        String workDirectory = FilenameUtils.getFullPath(path.toString());
        String txtFileName = FilenameUtils.getName(path.toString());
        String pgpFileName = getFileNameWithExtension(txtFileName,FILE_EXTENSION_PGP);
        String intuitPublicKey = BatchUtils.getConfigString("psp_batch_bank_intuit_public_key");
        logger.info("Starting pgpEncrypt without SignFile...");
        StopWatch sw = StopWatch.create(false);
        sw.start();
        PgpFileUtils.pgpEncryptWithoutSign(workDirectory,
                txtFileName,
                pgpFileName,
                Arrays.asList(intuitPublicKey),
                false,           // Create ASCII Armor file (Base64 encode the resulting binary stream)
                true);          // Include Integrity Packet(s) in the encrypted stream
        sw.stop();
        logger.info("Encrypted File in location :" + workDirectory + pgpFileName);
        logger.info("Completed pgpEncrypt without sign" + sw.getElapsedTimeString());
    }

    private static void decryptAllRawReports(Path path) throws Exception {
        Files.list(path).forEach(filePath -> {
            try {
                decryptRawReport(filePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void decryptRawReport(Path path) throws Exception {
        if (!isPgp(path.toString())) {
            logger.info(String.format("Skipped decrypting MtlTransaction Report with filename - %s as it is not a pgp file", path.getFileName()));
            return;
        }
        String workDirectory = FilenameUtils.getFullPath(path.toString());
        String pgpFileName = FilenameUtils.getName(path.toString());
        String txtFileName = getFileNameWithExtension(pgpFileName,"csv");
        String intuitPrivateKey = BatchUtils.getConfigString("psp_batch_bank_intuit_private_key");
        String intuitPrivateKeyPassword = BatchUtils.getConfigString("psp_batch_bank_intuit_private_key_password");
        logger.info("Starting pgpDecrypt without SignFile...");
        StopWatch sw = StopWatch.create(false);
        sw.start();
        PgpFileUtils.pgpDecryptUnsingedFile(workDirectory,
                pgpFileName,
                txtFileName,
                intuitPrivateKey,
                intuitPrivateKeyPassword);
        sw.stop();
        logger.info("Decrypted File in location :" + workDirectory + pgpFileName);
        logger.info("Completed pgpDecrypt without sign" + sw.getElapsedTimeString());
    }

    public static int getMtlBatchSize() {
        return ConfigurationManager.getConfiguration(ConfigurationModule.BatchJobs).getInteger(MTL_BATCH_SIZE, 100);
    }

    public static String getMtlS3Bucket() {
        return getConfigString(MTL_S3_BUCKET);
    }

    public static String getMtlPartnerS3Bucket() {
        return getConfigString(MTL_PARTNER_S3_BUCKET);
    }

    public static String getMtlWorkFolder() {
        return getConfigString(MTL_WORK_FOLDER);
    }

    public static String getMtlPartnerInboundFolder() {
        return getConfigString(MTL_PARTNER_INBOUND_FOLDER);
    }

    public static String getMtlOutboundFolder() {
        return getConfigString(MTL_OUTBOUND_FOLDER);
    }

    public static String getMtlArchiveFolder() {
        return getConfigString(MTL_ARCHIVE_FOLDER);
    }

    public static String[] getMtlTransactionReportHeaders() {
        return getConfigStringAsArray(MTL_TRANSACTION_REPORT_HEADERS);
    }

    public static String[] getMtlBeanColumMapping() {
        return getConfigStringAsArray(MTL_BEAN_COLUMN_MAPPING);
    }

    public static String getConfigString(String pKey) {
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, pKey);
    }

    public static String[] getConfigStringAsArray(String pKey) {
        String mtlTransactionReportHeader = getConfigString(pKey);
        String[] mtlTransactionReportHeaders = mtlTransactionReportHeader.split(",");
        Validate.isTrue((mtlTransactionReportHeaders.length > 0), pKey + " cannot be empty");
        return mtlTransactionReportHeaders;
    }

    public static boolean isValidRawReport(String fileName) {
        return FilenameUtils.isExtension(fileName, MTL_TRANSACTION_REPORT_FILE_EXTENSIONS);
    }

    public static boolean isEnrichedReport(String fileName) {
        return FilenameUtils.getBaseName(fileName).contains(ENRICHED_FILE);
    }

    public static boolean isPgp(String fileName) {
        return FilenameUtils.isExtension(fileName, FILE_EXTENSION_PGP);
    }

    public static String getFileNameWithExtension(String fileName, String extension) {
        return FilenameUtils.getBaseName(fileName) + FilenameUtils.EXTENSION_SEPARATOR_STR + extension;
    }

    public static String getEnrichedReportFileName(String mtlTransactionReportFileName) {
        Validate.notBlank(FilenameUtils.getName(mtlTransactionReportFileName), "Invalid filename provided", mtlTransactionReportFileName);
        return FilenameUtils.getBaseName(mtlTransactionReportFileName) + "_" + ENRICHED_FILE + FilenameUtils.EXTENSION_SEPARATOR_STR + FilenameUtils.getExtension(mtlTransactionReportFileName);
    }
}
