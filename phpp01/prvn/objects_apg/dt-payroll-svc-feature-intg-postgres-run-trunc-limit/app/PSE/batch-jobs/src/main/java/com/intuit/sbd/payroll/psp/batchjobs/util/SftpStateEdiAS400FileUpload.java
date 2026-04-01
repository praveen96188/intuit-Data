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
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Oct 27, 2011
 * Time: 5:54:48 PM
 */
public class SftpStateEdiAS400FileUpload {
    protected static final SpcfLogger mLogger = Application.getLogger(SftpStateEdiAS400FileUpload.class);

    private EdiFileInfo mCurrentFile;

    private class EdiFileInfo {
        private SpcfUniqueId mSpcfUniqueId;
        private int mFileId;
        private File mFile;

        EdiFileInfo(StateEdiTaxFile pStateEdiTaxFile) {
            mSpcfUniqueId = pStateEdiTaxFile.getId();
            mFileId = pStateEdiTaxFile.getFileId();
            File tempFormattedFile = new File(pStateEdiTaxFile.getFileName());
            mFile = new File(EftpsUtil.getEdiAS400Dir(), tempFormattedFile.getName());
        }

        public String getRemoteFileName() {
            //As400 doesn't accept if the file name length is more than 10, So restricting random number from 10000000 to 99999999
            String fileName = String.valueOf(Math.round(Math.random()*90000000) + 10000000);
            fileName = "RI"+fileName + ".MBR";

            // Remote AS400 file name must be the original file name: #VANFileName#
            return String.format("%s", fileName);

        }

        public void uploadSuccessful() {

            // Update the StateEdiTaxFile record to Completed status
            try {
                PayrollServices.beginUnitOfWork();

                StateEdiTaxFile stateEdiTaxFile = Application.findById(StateEdiTaxFile.class, mSpcfUniqueId);

                stateEdiTaxFile.setStatusCd(EdiFileStatus.Completed);
                stateEdiTaxFile.setFileName(mFile.getPath());
                stateEdiTaxFile.setStatusEffectiveDate(PSPDate.getPSPTime());

                Application.save(stateEdiTaxFile);

                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }

            mLogger.info(String.format("Successfully uploaded file %s to AS400.", mFile.getName()));
        }

    }

    private class SftpEftpsFileUploadListener extends JSchAdapter {
        public void upload(FileBean event) {
            mCurrentFile.uploadSuccessful();
        }
    }

    public void upload() {
        mLogger.info("Attempting to upload State Edi Tax file(s) to AS400...");

        // We do not implement a retry loop here (as we do in other FTP/SFTP tasks) since this upload task executes
        // repeatedly throughout the day within a recurring batch job (which effectively acts as our retry mechanism).

        List<EdiFileInfo> fileList = getFilesToUpload();

        if (fileList.isEmpty()) {
            mLogger.info("No State EDI file(s) found to upload to AS400.");
        } else {
            Transporter sftp = BatchUtils.getEftpsAs400SftpConnection(new SftpEftpsFileUploadListener());

            try {
                sftp.setLogger(mLogger);

                sftp.connect();

                sftp.changeRemoteDir(EftpsUtil.getEdiFtpAs400Dir());

                for (EdiFileInfo eftpsFileInfo : fileList) {
                    try {
                        mCurrentFile = eftpsFileInfo;
                        String remoteFileName = mCurrentFile.getRemoteFileName();
                        mLogger.info(String.format("Uploading file %s to AS400, Remote File name: %s...", mCurrentFile.mFile.getName(), remoteFileName));
                        sftp.uploadFile(mCurrentFile.mFile.getName(), remoteFileName);

                    } catch (Throwable t) {
                        handleFileUploadError(eftpsFileInfo, t);
                    }
                }
            } catch (Throwable e) {
                throw new RuntimeException("Error sending State EDI file(s) to AS400 (aborting process) ", e);
            } finally {
                try {
                    sftp.disconnect();
                } catch (Exception e) {
                    throw new RuntimeException("Error in disconnecting at EDI 400 upload step ", e);
                }
            }
        }
    }

    private List<EdiFileInfo> getFilesToUpload() {
        List<EdiFileInfo> fileList = new Vector<EdiFileInfo>();

        try {
            PayrollServices.beginUnitOfWork();

            // get list of files to send to AS400
            for (StateEdiTaxFile stateEdiTaxFile : StateEdiTaxFile.getSendToAS400StateEdiFiles()) {
                mLogger.info("Preparing to upload State Edi file " + stateEdiTaxFile.getFileName() + " to AS400...");
                fileList.add(new EdiFileInfo(stateEdiTaxFile));
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return fileList;
    }

    private void handleFileUploadError(EdiFileInfo pEftpsFileInfo, Throwable pThrowable) {
        String msg = String.format("Error sending State EDI file %s to AS400.", pEftpsFileInfo.mFile.getName());
        mLogger.error(String.format("%s File skipped and moved to error directory. ", msg), pThrowable);
        EftpsUtil.updateEftpsFileErrorStatus(pEftpsFileInfo.mFileId, pEftpsFileInfo.mFile, EftpsUtil.getEdiErrDir());
    }
}
