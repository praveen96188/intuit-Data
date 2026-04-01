package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.EdiFileStatus;
import com.intuit.sbd.payroll.psp.domain.StateEdiTaxFile;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Nov 2, 2011
 * Time: 11:20:50 AM
 */
public class ScpEdiVanFileUpload {
    protected static final SpcfLogger mLogger = Application.getLogger(FtpsEftpsTfaFileUpload.class);

    private final String mDestination = EftpsUtil.getConfigString("psp_edi_scp_destination");
    private final String mDestinationBackup = EftpsUtil.getConfigString("psp_edi_scp_backup_destination");

    private List<Integer> mUploadedFileIdList = new Vector<Integer>();
    private StateEdiTaxFile mCurrentFile;

    private class ScpFileUploadListener extends JSchAdapter {
        public void connected(String scpConnectedEvent) {
            mLogger.info("Successfully connected");
        }

        public void disconnected(String scpDisconnectedEvent) {
            mLogger.info("Successfully Disconnected");
        }

        public void download(FileBean scpFileDownloadedEvent) {
            mLogger.info("File downloaded:"+ scpFileDownloadedEvent.getFilename());
        }

        public void upload(FileBean event) {
            try {
                mLogger.info(String.format("Successfully uploaded file %s to VAN.", mCurrentFile.getFileName()));

                PayrollServices.beginUnitOfWork();

                Application.refresh(mCurrentFile);

                mCurrentFile.setStatusCd(EdiFileStatus.Completed);
                mCurrentFile.setStatusEffectiveDate(PSPDate.getPSPTime());
                mCurrentFile.setSubmitDate(PSPDate.getPSPTime());

                Application.save(mCurrentFile);

                PayrollServices.commitUnitOfWork();

                mUploadedFileIdList.add(mCurrentFile.getFileId());
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }

    }

    public List<Integer> getUploadedFileIdList() {
        return mUploadedFileIdList;
    }

    public void upload() {
        mLogger.info("Attempting to upload State EDI file(s) to VAN...");

        // We do not implement a retry loop here (as we do in other FTP/SFTP tasks)

        List<StateEdiTaxFile> fileList = getFilesToUpload();

        if (fileList.isEmpty()) {
            mLogger.info("No State EDI file records found awaiting upload to VAN.");
        } else {
            Transporter sftp = BatchUtils.getEdiVanScpConnection(new ScpFileUploadListener());

            try {
                sftp.connect();

                for (StateEdiTaxFile stateEdiTaxFile : fileList) {
                    mCurrentFile = stateEdiTaxFile;

                    mLogger.info(String.format("Uploading file %s to VAN...", mCurrentFile.getFileName()));

                    try {

                        sftp.uploadFile(mCurrentFile.getFileName(), mDestination);

                        if (mDestinationBackup != null) {
                            sftp.uploadFile(mCurrentFile.getFileName(), mDestinationBackup);
                        }
                    } catch (Throwable t) {
                        handleFileUploadError(t);
                    }
                }
            } catch (Throwable t) {
                throw new RuntimeException("Error sending EDI file(s) to VAN (aborting process). ", t);
            } finally {
                try {
                    sftp.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private List<StateEdiTaxFile> getFilesToUpload() {
        List<StateEdiTaxFile> fileList = new Vector<StateEdiTaxFile>();

        try {
            PayrollServices.beginUnitOfWork();

            // get list of files to send to bank
            for (StateEdiTaxFile stateEdiTaxFile : StateEdiTaxFile.getPendingTransmissionStateEdiFiles()) {
                mLogger.info("Preparing to upload State EDI file " + stateEdiTaxFile.getFileName() + " to VAN...");
                fileList.add(stateEdiTaxFile);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return fileList;
    }

    private void handleFileUploadError(Throwable pThrowable) {
        String msg = String.format("Error sending State Edi file %s to VAN.", mCurrentFile.getFileName());
        mLogger.error(String.format("%s File skipped and moved to error directory. ", msg), pThrowable);
        EftpsUtil.updateEftpsFileErrorStatus(mCurrentFile.getFileId(), new File(mCurrentFile.getFileName()), EftpsUtil.getEdiErrDir());
    }
}
