package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.FsetFile;
import com.intuit.sbd.payroll.psp.domain.FsetFileStatus;
import com.intuit.sbd.payroll.psp.domain.FsetFileType;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * User: ihannur
 * Date: 9/13/12
 * Time: 2:14 PM
 */
public class SftpFsetConnection {
    protected static final SpcfLogger logger = Application.getLogger(SftpFsetConnection.class);

    private Map<String, SpcfUniqueId> mListenerFileMap = new Hashtable<String, SpcfUniqueId>();

    private static final String FILE_PATTERN = ".*\\.xml";


    private class FileUploadListener extends JSchAdapter {
        public void upload(FileBean val) {
            SpcfUniqueId id = mListenerFileMap.get(val.getFilename());

            if (id == null) {
                throw new RuntimeException("Unable to correlate FSET file transmitted.");
            }

            try {
                PayrollServices.beginUnitOfWork();

                FsetFile fsetFile = Application.findById(FsetFile.class, id);
                if (fsetFile == null) {
                    throw new RuntimeException("Could not find FSET file: " + id);
                }

                fsetFile.setStatusCd(FsetFileStatus.SentToAgency);
                fsetFile.setStatusEffectiveDate(PSPDate.getPSPTime());
                fsetFile.setSubmitDate(PSPDate.getPSPTime());

                Application.save(fsetFile);
                PayrollServices.commitUnitOfWork();

                logger.info("Changing file " + val.getFilename() + " status to SentToAgency");
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public void upload(DomainEntitySet<FsetFile> pFsetFiles) {
        if (pFsetFiles.isEmpty()) {
            logger.info("No Pending FSET Files to upload. Skipping upload..");
            return;
        }
        int maxRetries = Integer.parseInt(BatchUtils.getTaxAgencyConfigString("psp_fset_sftp_retry_max", "10"));
        int retryCount = 0;
        Transporter sftp = BatchUtils.getFsetSftpConnection(getUploadListener());
        sftp.setLogger(logger);

        try {
            sftp.connect();
            sftp.changeLocalDir(BatchUtils.getTaxAgencyConfigString("psp_fset_send_dir"));
            sftp.changeRemoteDir(BatchUtils.getTaxAgencyConfigString("psp_fset_sftp_destination"));

            for (FsetFile fsetFile : pFsetFiles) {

                logger.info("Attempting to upload File:" + fsetFile.getFileName());
                boolean done = false;

                do {
                    try {

                        String remoteFileName = fsetFile.getFileName().substring(fsetFile.getFileName().lastIndexOf(File.separator) + 1);
                        mListenerFileMap.put(remoteFileName, fsetFile.getId());
                        
            			String localFileName = FilenameUtils.getName(fsetFile.getFileName());

            			sftp.uploadFile(localFileName, remoteFileName);

                        done = true;

                    } catch (Exception e) {
                        if (retryCount == 0) {
                            logger.error("Error sending FSET Return File:" + fsetFile.getFileName() + " files (attempting retry) ", e);
                        } else if (retryCount < maxRetries) {
                            logger.error("Error sending FSET Return File:" + fsetFile.getFileName() + " files (retry attempt " + retryCount + ") ", e);
                        } else {
                            throw new RuntimeException("Error sending FSET Return File:" + fsetFile.getFileName() + " files (aborting process) ", e);
                        }
                    }

                    if (!done) {
                        BatchUtils.delay(Long.parseLong(BatchUtils.getTaxAgencyConfigString("psp_fset_sftp_retry_delay", "1")));
                    }

                } while (!done && (++retryCount <= maxRetries));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in FSET upload files.", e);
        } finally {
            if (sftp != null) {
                try {
					sftp.disconnect();
				} catch (Exception e) {
					 throw new RuntimeException("Error in FSET upload files disconnecting.", e);
				}
            }
        }
    }

    public FileUploadListener getUploadListener() {
        return new FileUploadListener();
    }

    public FileDownloadListener getDownloadListener() {
        return new FileDownloadListener();
    }

    private class FileDownloadListener extends JSchAdapter {
        public void download(FileBean val) {
            File file = new File(BatchUtils.getTaxAgencyConfigString("psp_fset_recv_dir"), val.getFilename());

            try {
                PayrollServices.beginUnitOfWork();

                FsetFile fsetFile = new FsetFile();
                fsetFile.setFileName(file.getAbsolutePath());
                fsetFile.setStatusCd(FsetFileStatus.ReceivedByAgency);
                fsetFile.setStatusEffectiveDate(PSPDate.getPSPTime());
                fsetFile.setFileType(FsetFileType.FsetAck);

                Application.save(fsetFile);

                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public void downloadFiles() {
        logger.info("Attempting to download FSET file(s) from agency...");

        boolean done = false;
        int maxRetries = Integer.parseInt(BatchUtils.getTaxAgencyConfigString("psp_fset_sftp_retry_max", "10"));
        int retryCount = 0;

        do {
            Transporter sftp = BatchUtils.getFsetSftpConnection(new FileDownloadListener());
            try {
                sftp.setLogger(logger);

                sftp.connect();

                sftp.changeLocalDir(BatchUtils.getTaxAgencyConfigString("psp_fset_recv_dir"));
                sftp.changeRemoteDir(BatchUtils.getTaxAgencyConfigString("psp_fset_sftp_source"));

                List<String> fsetResponseFiles = sftp.getRemoteDirListing(FILE_PATTERN);

                // download the files and then delete them from the remote host
                // (if anything fails, an exception will be thrown)
                for (String fsetResponseFile : fsetResponseFiles) {
                	sftp.downloadFile(fsetResponseFile);
                    sftp.deleteRemoteFile(fsetResponseFile);
				}

                done = true;
            } catch (Exception e) {
                if (retryCount == 0) {
                    logger.error("Error receiving FSET Ack  file from provider (attempting retry) ", e);
                } else if (retryCount < maxRetries) {
                    logger.error("Error receiving FSET Ack file from provider (retry attempt " + retryCount + ") ", e);
                } else {
                    throw new RuntimeException("Error receiving FSET Ack file from provider (aborting process) ", e);
                }
            } finally {
                try {
					sftp.disconnect();
				} catch (Exception e) {
					throw new RuntimeException("Error receiving FSET Ack file from provider after disconnecting (aborting process) ", e);
				}
            }

            if (!done) {
                BatchUtils.delay(Long.parseLong(BatchUtils.getTaxAgencyConfigString("psp_fset_sftp_retry_delay", "2")));
            }
        } while (!done && (++retryCount <= maxRetries));
    }


}
