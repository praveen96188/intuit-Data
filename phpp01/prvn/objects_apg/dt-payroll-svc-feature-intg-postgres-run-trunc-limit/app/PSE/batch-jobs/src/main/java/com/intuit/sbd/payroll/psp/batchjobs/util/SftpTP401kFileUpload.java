package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.ThirdParty401kBatch;
import com.intuit.sbd.payroll.psp.domain.ThirdParty401kBatchStatusCode;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

public class SftpTP401kFileUpload {
    protected static final SpcfLogger sfLogger;

    private Map<String, SpcfUniqueId> mFileMap = new Hashtable<String, SpcfUniqueId>();

    static {
        sfLogger = Application.getLogger(SftpTP401kFileUpload.class);
    }

    private class SftpTP401kFileUploadListener extends JSchAdapter {
        public void upload(FileBean event) {
            // for each file successfully received, update it's state in the db
            SpcfUniqueId id = mFileMap.get(event.getFilename());

            if (id == null) {
                throw new RuntimeException("Unable to correlate 401k file transmitted to server with 401k upload batch prefetch data.");
            }

            try {
                PayrollServices.beginUnitOfWork();

                ThirdParty401kBatch batch = Application.findById(ThirdParty401kBatch.class, id);

                if (batch == null) {
                    throw new RuntimeException("Could not correlate 401k file transmitted to provider with 401k upload batch record in the database.");
                }

                batch.setUploadStatusCd(ThirdParty401kBatchStatusCode.Transmitted);
                batch.setStatusEffectiveDate(PSPDate.getPSPTime());
                batch.setUploadDate(PSPDate.getPSPTime());

                Application.save(batch);

                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public void upload() {
        sfLogger.info("Attempting to upload 401k files to provider ...");

        boolean done = false;

        //todo maybe move this to a system parm
        int maxRetries = Integer.parseInt(BatchUtils.getConfigString("psp_batch_tp401k_max_retry", "10"));
        int retryCount = 0;

        do {
            getFilesToUpload();

            if (mFileMap.isEmpty()) {
                sfLogger.warn("No 401k file records found awaiting upload to provider.");
                done = true;
            } else {
                Transporter sftp = BatchUtils.get401kSftpConnection(new SftpTP401kFileUploadListener());
                try {
                    sftp.setLogger(sfLogger);

                    sftp.connect();

                    sftp.changeLocalDir(BatchUtils.getConfigString("psp_batch_ftp_send_dir"));
                    for (String file : mFileMap.keySet()) {
                        if (file.indexOf("Census") > -1) {
                            sftp.changeRemoteDir(BatchUtils.getConfigString("psp_batch_tp401k_census_dir"));
                        } else
                        if (file.indexOf("Payroll") > -1) {
                            sftp.changeRemoteDir(BatchUtils.getConfigString("psp_batch_tp401k_payroll_dir"));
                        } else {
                            sfLogger.error("Unknown file encountered during 401k upload, File Name: " + file);
                            continue;
                        }

                        sftp.uploadFile(file);

                    }

                    done = true;
                } catch (Throwable t) {
                    if (retryCount == 0) {
                        sfLogger.error("Error sending 401k files to provider (attempting retry) ", t);
                    } else if (retryCount < maxRetries) {
                        sfLogger.error("Error sending 401k files to provider (retry attempt " + retryCount + ") ", t);
                    } else {
                        throw new RuntimeException("Error sending 401k files to provider (aborting process) ", t);
                    }
                } finally {
                    try {
                        sftp.disconnect();
                    } catch (Exception e) {
                        throw new RuntimeException("Error in disconnecting at TP401 upload step ", e);
                    }
                }

                if (!done) {
                    BatchUtils.delay("psp_batch_tp401k_retry_delay", "1");
                }
            }
        }  while (!done && (++retryCount <= maxRetries));
    }

    private void getFilesToUpload() {
        try {
            // clear the file -> id map in case this isn't the first time through
            mFileMap.clear();

            PayrollServices.beginUnitOfWork();

            // get list of files to send to provider
            DomainEntitySet<ThirdParty401kBatch> batchSet =
                    BatchUtils.getTP401kUploadFilesByStatus(ThirdParty401kBatchStatusCode.Finalized,
                                                            ThirdParty401kBatchStatusCode.PendingTransmission);

            for (ThirdParty401kBatch batch : batchSet) {
                sfLogger.info("Preparing to upload 401k file " + batch.getFileName() + " to provider...");

                mFileMap.put(new File(batch.getFileName()).getName(), batch.getId());

                batch.setUploadStatusCd(ThirdParty401kBatchStatusCode.PendingTransmission);
                batch.setStatusEffectiveDate(PSPDate.getPSPTime());

                Application.save(batch);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }
}
