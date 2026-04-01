package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.jsch.FileBean;
import com.intuit.sbd.payroll.psp.common.utils.jsch.JSchAdapter;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.domain.EdiFileStatus;
import com.intuit.sbd.payroll.psp.domain.EftpsFile;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.ops.eftpsBp.EdiFileAs400Translator;

import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: svenkata, kpaul
 * Date: Dec 20, 2010
 * Time: 9:42:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class SftpEftpsAs400FileUpload {
    protected static final SpcfLogger mLogger = Application.getLogger(SftpEftpsAs400FileUpload.class);

    private EftpsFileInfo mCurrentFile;

    private class EftpsFileInfo {
        private SpcfUniqueId mSpcfUniqueId;
        private int mFileId;
        private File mFile;
        private File mEbcdicFile;

        EftpsFileInfo(EftpsFile pEftpsFile) {
            mSpcfUniqueId = pEftpsFile.getId();
            mFileId = pEftpsFile.getFileId();
            mFile = new File(pEftpsFile.getFileName());

            //
            // Files being sent to the AS400 need to be translated to EBCDIC and have their GS03 field modified
            //
            mEbcdicFile = EdiFileAs400Translator.translateToAs400(mFile, EftpsUtil.getWorkDir());
        }

        public String getRemoteFileName() {
            String remoteFileName;
            String fileName = mFile.getName();

            //
            // Remote AS400 file name must be of the form: [A-Z][0-9]{9}.MBR
            // The standard EFTPS file name delivered to PSP from TFA should be of the form: EFTPSTX.E#########
            // If we see a file name of this form, use the E#########.MBR as the remote file name.
            // If not, use the file id to construct a file name to meet the AS400 member name requirement.
            //

            if (fileName.matches("EFTPSTX\\.[A-Z][0-9]{9}")) {
                remoteFileName = String.format("%s.MBR", fileName.split("\\.")[1]);
            } else {
                remoteFileName = String.format("E%09d.MBR", mFileId);
            }

            return remoteFileName;
        }

        public void uploadSuccessful() {
            //
            // Update the EftpsFile record to Completed status
            //
            try {
                PayrollServices.beginUnitOfWork();

                EftpsFile eftpsFile = Application.findById(EftpsFile.class, mSpcfUniqueId);

                eftpsFile.setStatusCd(EdiFileStatus.Completed);
                eftpsFile.setStatusEffectiveDate(PSPDate.getPSPTime());

                Application.save(eftpsFile);

                PayrollServices.commitUnitOfWork();
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }

            //
            // Archive the successfully uploaded EBCDIC file
            // (this file is only known locally, so it must be archived here)
            // (charset Cp1047 = IBM System 390 EBCDIC)
            //
            try {
                EftpsUtil.moveFile(mEbcdicFile, EftpsUtil.getArchiveDir(), "Cp1047");
            } catch (Throwable t) {
                //
                // Log the archive failure as an error, but don't propagate the exception since the file upload
                // was ultimately successful.
                //
                mLogger.error(String.format("Error archiving successfully uploaded EBCDIC file %s ",
                                            mEbcdicFile.getPath()), t);
            }

            mLogger.info(String.format("Successfully uploaded file %s (as %s) to AS400.",
                                       mFile.getName(), mEbcdicFile.getName()));
        }

        public void cleanup() {
            //
            // Cleanup orphaned EBCDIC file if appropriate
            // (only deletes file if it wasn't successfully archived in uploadSuccessful method)
            //
            try {
                if (mEbcdicFile.exists()) {
                    mEbcdicFile.delete();
                }
            } catch (Throwable t) {
                //
                // Log the delete failure as an error, but don't propagate the exception since we don't want to
                // interrupt any other pending uploads.
                //
                mLogger.error(String.format("Error deleting orphaned EBCDIC file %s ", mEbcdicFile.getPath()), t);
            }
        }
    }

    private class SftpEftpsFileUploadListener extends JSchAdapter {
        public void upload(FileBean event) {
            mCurrentFile.uploadSuccessful();
        }
    }

    public void upload() {
        mLogger.info("Attempting to upload EFTPS file(s) to AS400...");

        //
        // We do not implement a retry loop here (as we do in other FTP/SFTP tasks) since this upload task executes
        // repeatedly throughout the day within a recurring batch job (which effectively acts as our retry mechanism).
        //

        List<EftpsFileInfo> fileList = getFilesToUpload();

        if (fileList.isEmpty()) {
            mLogger.info("No EFTPS file(s) found to upload to AS400.");
        } else {
            Transporter sftp = BatchUtils.getEftpsAs400SftpConnection(new SftpEftpsFileUploadListener());

            try {
                sftp.setLogger(mLogger);

                sftp.connect();

                sftp.changeRemoteDir("/QSYS.LIB/ZEFTPSLIB.LIB/EFTPSTX.FILE");

                for (EftpsFileInfo eftpsFileInfo : fileList) {
                    try {
                        mCurrentFile = eftpsFileInfo;

                        try {
                            mLogger.info(String.format("Uploading file %s (as %s) to AS400...",
                                                       mCurrentFile.mFile.getName(),
                                                       mCurrentFile.mEbcdicFile.getName()));

                            sftp.uploadFile(mCurrentFile.mEbcdicFile.getName(), mCurrentFile.getRemoteFileName());
                        } finally {
                            mCurrentFile.cleanup();
                        }
                    } catch (Throwable t) {
                        handleFileUploadError(eftpsFileInfo, t);
                    }
                }
            } catch (Throwable e) {
                throw new RuntimeException("Error sending EFTPS file(s) to AS400 (aborting process) ", e);
            } finally {
                try {
                    sftp.disconnect();
                } catch (Exception e) {
                    throw new RuntimeException("Error in disconnecting at EFTPS 400 upload step ", e);
                }
            }
        }
    }

    private List<EftpsFileInfo> getFilesToUpload() {
        List<EftpsFileInfo> fileList = new Vector<EftpsFileInfo>();

        try {
            PayrollServices.beginUnitOfWork();

            // get list of files to send to AS400
            for (EftpsFile eftpsFile : EftpsFile.getSendToAS400EftpsFiles()) {
                mLogger.info("Preparing to upload EFTPS file " + eftpsFile.getFileName() + " to AS400...");
                fileList.add(new EftpsFileInfo(eftpsFile));
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return fileList;
    }

    private void handleFileUploadError(EftpsFileInfo pEftpsFileInfo, Throwable pThrowable) {
        String msg = String.format("Error sending EFTPS file %s to AS400.", pEftpsFileInfo.mFile.getName());
        mLogger.error(String.format("%s File skipped and moved to error directory. ", msg), pThrowable);
        EftpsUtil.updateEftpsFileErrorStatus(pEftpsFileInfo.mSpcfUniqueId, pEftpsFileInfo.mFile);
    }
}
