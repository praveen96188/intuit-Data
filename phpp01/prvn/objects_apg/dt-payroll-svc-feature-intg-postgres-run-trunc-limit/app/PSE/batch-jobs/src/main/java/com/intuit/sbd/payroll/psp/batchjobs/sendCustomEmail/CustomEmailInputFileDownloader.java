package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail;

import com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail.models.ConfigFileModel;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileDecryptionResult;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.shared.filestore.FileStore;
import com.paycycle.util.PgpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class CustomEmailInputFileDownloader {

    private ConfigFileModel[] configFileModelArray;
    private Set<String> workFlowsToExecute;

    public static final String FILE_EXTENSION_PGP = "pgp";

    public CustomEmailInputFileDownloader(ConfigFileModel[] configFileModelArray, Set<String> workFlowsToExecute) {
        this.configFileModelArray = configFileModelArray;
        this.workFlowsToExecute = workFlowsToExecute;
    }

    public void downloadAndArchiveInputFiles() throws Exception {
        String s3InputDir = BatchUtils.getConfigString("psp_custom_email_s3_input_dir");
        String s3ArchiveDir = BatchUtils.getConfigString("psp_custom_email_s3_arch_dir");
        String localInputDir = BatchUtils.getConfigString("psp_custom_email_local_input_dir");
        Boolean inputDecryptionEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.L2SM_INPUT_DECRYPTION_ENABLED, false);
        for (int i = 0; i < configFileModelArray.length; i++) {
            ConfigFileModel config = configFileModelArray[i];
            String workflow = config.getWorkflowName();
            if(this.workFlowsToExecute.contains(workflow)) {
                String s3FileDir = s3InputDir + File.separator + config.getRelativeDir();
                String localFileDir = localInputDir + File.separator + config.getRelativeDir();
                String archiveFileDir = s3ArchiveDir + File.separator + config.getRelativeDir();
                log.info("job=SendCustomEmailsProcessor, Action=CustomEmailInputFileDownloader, Method=downloadAllFiles, s3FileDir={}, localFileDir={}, archiveFileDir={}", s3FileDir, localFileDir, archiveFileDir);
                FileStore fileStore = S3UploadUtils.getFileStore();
                S3UploadUtils.downloadAndArchiveAllFiles(fileStore, getPspS3Bucket(), s3FileDir, localFileDir, archiveFileDir);
                log.info("job=SendCustomEmailsProcessor, Action=CustomEmailInputFileDownloader, Method=downloadAllFiles, L2SM_INPUT_DECRYPTION_ENABLED FF Value={}", inputDecryptionEnabled);
                if(inputDecryptionEnabled){
                    decryptAllRawReports(Paths.get(localFileDir));
                }
            }
        }
    }
    private void decryptAllRawReports(Path path) throws Exception {
        Files.list(path).forEach(filePath -> {
            try {
                decryptRawReport(filePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void decryptRawReport(Path path) throws Exception {
        if (!isPgp(path.toString())) {
            log.info(String.format("Skipped decrypting CustomEmail Input file with filename - %s as it is not a pgp file", path.getFileName()));
            return;
        }
        String workDirectory = FilenameUtils.getFullPath(path.toString());
        String pgpFileName = FilenameUtils.getName(path.toString());
        String csvFileName = getFileNameWithExtension(pgpFileName,"csv");
        String l2smPrivateKey = BatchUtils.getConfigString("psp_custom_email_l2sm_private_key");
        String l2smPrivateKeyPassword = BatchUtils.getConfigString("psp_custom_email_l2sm_private_key_password");
        log.info("Starting pgpDecrypt without SignFile...");
        StopWatch sw = StopWatch.create(false);
        sw.start();
        PgpFileUtils.pgpDecryptUnsingedFile(workDirectory,
                pgpFileName,
                csvFileName,
                l2smPrivateKey,
                l2smPrivateKeyPassword);
        sw.stop();
        log.info("Decrypted File in location :" + workDirectory + pgpFileName);
        log.info("Completed pgpDecrypt without sign" + sw.getElapsedTimeString());
    }
    private boolean isPgp(String fileName) {
        return FilenameUtils.isExtension(fileName, FILE_EXTENSION_PGP);
    }

    private String getFileNameWithExtension(String fileName, String extension) {
        return FilenameUtils.getBaseName(fileName) + FilenameUtils.EXTENSION_SEPARATOR_STR + extension;
    }

    private String getPspS3Bucket() {
        return BatchUtils.getConfigString(S3UploadUtils.PSP_BATCHJOBS_S3_BUCKET);
    }
}
