package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.agency.util.EftpsEdiType;
import com.intuit.sbd.payroll.psp.agency.util.EftpsUtil;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import com.intuit.sbd.payroll.psp.domain.EdiFileStatus;
import com.intuit.sbd.payroll.psp.domain.EdiTaxFile;
import com.intuit.sbd.payroll.psp.domain.EftpsFile;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbg.shared.filestore.FileStore;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.jscape.inet.ftp.FtpAdapter;
import com.jscape.inet.ftp.FtpUploadEvent;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: svenkata, kpaul
 * Date: Dec 10, 2010
 * Time: 5:21:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class FtpsEftpsTfaFileUpload {
    protected static final SpcfLogger mLogger = Application.getLogger(FtpsEftpsTfaFileUpload.class);

    private final long mUploadDelay = Long.parseLong(EftpsUtil.getConfigString("psp_eftps_ftp_upload_delay", "0"));

    private List<Integer> mUploadedFileIdList = new Vector<Integer>();
    private EftpsFileInfo mCurrentFile;

    private class EftpsFileInfo {
        SpcfUniqueId mSpcfUniqueId;
        EftpsEdiType mEftpsEdiType;
        int mFileId;
        File mFile;

        EftpsFileInfo(EftpsFile pEftpsFile) {
            mSpcfUniqueId = pEftpsFile.getId();
            mEftpsEdiType = EftpsEdiType.getValueByEdiType(pEftpsFile.getFileCode());
            mFileId = pEftpsFile.getFileId();
            mFile = new File(pEftpsFile.getFileName());
        }

        String getRemoteFileName() {
            String remoteFileName = null;

            switch (mEftpsEdiType) {
                case EDI813:
                    remoteFileName = EftpsUtil.getConfigString("psp_eftps_ftp_813_filename");
                    break;
                case EDI821:
                    remoteFileName = EftpsUtil.getConfigString("psp_eftps_ftp_821_filename");
                    break;
                case EDI838:
                    remoteFileName = EftpsUtil.getConfigString("psp_eftps_ftp_838_filename");
                    break;
                case EDI997:
                    remoteFileName = EftpsUtil.getConfigString("psp_eftps_ftp_997_filename");
                    break;
            }

            if (remoteFileName != null) {
                remoteFileName = remoteFileName.trim();
            }

            return ((remoteFileName == null) || (remoteFileName.length() == 0)) ? mFile.getName() : remoteFileName;
        }

        /**
         * The TFA wants us to delay uploads of files that target the same dataset on their side. This method checks
         * the last time we uploaded a file to the dataset we are currently targeting. In this case, the following
         * file types target the same dataset on the TFA mainframe and thus require a delay between uploads.
         * <br>813/838
         * <br>821
         * <br>997
         *
         * @return True if the delay time has expired and we can upload the file.
         */
        boolean isEligibleForUpload() {
            boolean okToUpload = true;
            Criterion<EdiTaxFile> where = null;

            //
            // These file types require a delay between uploads since they're destined for the same remote data set
            //
            switch (mEftpsEdiType) {
                case EDI813:
                case EDI838:
                    where = EftpsFile.FileCode().in(EftpsEdiType.EDI813.value(), EftpsEdiType.EDI838.value());
                    break;

                case EDI821:
                case EDI997:
                    where = EftpsFile.FileCode().equalTo(mEftpsEdiType.value());
                    break;
            }

            if (where != null) {
                try {
                    PayrollServices.beginUnitOfWork();

                    //
                    // Find files of the appropriate type(s) that have already been uploaded to the TFA
                    //

                    where = where.And(EftpsFile.SubmitDate().isNotNull());

                    Expression<EftpsFile> query = new Query<EftpsFile>().Select(EftpsFile.SubmitDate().Max()).Where(where);
                    List lastSubmitted = Application.executeQuery(EftpsFile.class, query);

                    //
                    // If there are any files of the appropriate type(s), confirm the wait time has elapsed.
                    //

                    if (!lastSubmitted.isEmpty() && (lastSubmitted.get(0) != null)) {
                        SpcfCalendar now = PSPDate.getPSPTime();
                        SpcfCalendar submitDate = (SpcfCalendar) lastSubmitted.get(0);
                        long elapsedTime = now.getTimeInMilliseconds() - submitDate.getTimeInMilliseconds();
                        okToUpload = (elapsedTime >= mUploadDelay);
                    }

                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            }

            return okToUpload;
        }
    }

    private class FtpsEftpsFileUploadListener extends FtpAdapter {
        public void upload(FtpUploadEvent event) {
            try {
                mLogger.info(String.format("Successfully uploaded file %s to TFA.", mCurrentFile.mFile.getName()));
                boolean deleteFile = false;
                PayrollServices.beginUnitOfWork();

                EftpsFile eftpsFile = Application.findById(EftpsFile.class, mCurrentFile.mSpcfUniqueId);

                eftpsFile.setStatusCd(EdiFileStatus.Completed);
                eftpsFile.setStatusEffectiveDate(PSPDate.getPSPTime());
                eftpsFile.setSubmitDate(PSPDate.getPSPTime());
                if (mCurrentFile.mFile.getName().endsWith(".pgp")) {
                    eftpsFile.setFileName(FilenameUtils.getPath(mCurrentFile.mFile.getAbsolutePath())
                            + FilenameUtils.getBaseName(mCurrentFile.mFile.getAbsolutePath()));
                    deleteFile = true;
                }
                Application.save(eftpsFile);

                PayrollServices.commitUnitOfWork();
                if (deleteFile){
                    mCurrentFile.mFile.delete();
                }
                mUploadedFileIdList.add(mCurrentFile.mFileId);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }

    public List<Integer> getUploadedFileIdList() {
        return mUploadedFileIdList;
    }

    public void uploadOnS3() {
        mLogger.info("Attempting to upload EFTPS file(s) to TFA...");

        //
        // We do not implement a retry loop here (as we do in other FTP/SFTP tasks) since this upload task executes
        // repeatedly throughout the day within a recurring batch job (which effectively acts as our retry mechanism).
        //

        List<EftpsFileInfo> fileList = getFilesToUpload();

        if (fileList.isEmpty()) {
            mLogger.info("No EFTPS file records found awaiting upload to TFA.");
        } else {
            try {
                FileStore fileStore = BatchUtils.getFileStore();
                for (EftpsFileInfo eftpsFileInfo : fileList) {
                    if (eftpsFileInfo.isEligibleForUpload()) {
                        mLogger.info(String.format("Uploading file %s to S3...", eftpsFileInfo.mFile.getName()));

                        try {
                            // need to set current file this way since resulting file name will be generic GDS name.
                            // (upload listener would not be able to differentiate which specific file was uploaded)
                            mCurrentFile = eftpsFileInfo;
                            File uploadFile = null;
                            if (mCurrentFile.mFile.getName().endsWith(".pgp")) {
                                uploadFile = mCurrentFile.mFile;
                            } else {
                                String workingDir = FilenameUtils.getFullPath(mCurrentFile.mFile.getAbsolutePath());
                                String encryptedFileName = FilenameUtils.getName(mCurrentFile.mFile.getName())+".pgp";
                                String unencryptedFileName = FilenameUtils.getName(mCurrentFile.mFile.getName());
                                PgpFileUtils.pgpEncryptWithoutSign(workingDir
                                        , unencryptedFileName
                                        , encryptedFileName
                                        , BatchUtils.getTfaPgpKeys()
                                        , Boolean.TRUE
                                        , Boolean.TRUE);
                                uploadFile = new File(workingDir+encryptedFileName);
                            }
                            fileStore.writeFile(EftpsUtil.getS3Bucket()
                                    , EftpsUtil.getS3OutboundFolder() + mCurrentFile.mFile.getName()
                                    , uploadFile);
                            updateEftpsFileStatusCompleted();
                        } catch (Throwable t) {
                            handleFileUploadError(eftpsFileInfo, t);
                        }
                    } else {
                        mLogger.info(String.format("EFTPS file %s upload to TFA delayed due to wait time window.",
                                eftpsFileInfo.mFile.getPath()));
                    }
                }
            } catch (Throwable t) {
                throw new RuntimeException("Error sending EFTPS file(s) to TFA (aborting process). ", t);
            }
        }
    }

    private void updateEftpsFileStatusCompleted() {
        try {
            mLogger.info(String.format("Successfully uploaded file %s to TFA.", mCurrentFile.mFile.getName()));
            boolean deleteFile = false;
            PayrollServices.beginUnitOfWork();

            EftpsFile eftpsFile = Application.findById(EftpsFile.class, mCurrentFile.mSpcfUniqueId);

            eftpsFile.setStatusCd(EdiFileStatus.Completed);
            eftpsFile.setStatusEffectiveDate(PSPDate.getPSPTime());
            eftpsFile.setSubmitDate(PSPDate.getPSPTime());
            if (!mCurrentFile.mFile.getName().endsWith(".pgp")) {
                eftpsFile.setFileName(mCurrentFile.mFile.getAbsoluteFile() + ".pgp");
                deleteFile = true;
            }
            Application.save(eftpsFile);

            PayrollServices.commitUnitOfWork();
            if (deleteFile) {
                mCurrentFile.mFile.delete();
            }
            mUploadedFileIdList.add(mCurrentFile.mFileId);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private List<EftpsFileInfo> getFilesToUpload() {
        List<EftpsFileInfo> fileList = new Vector<EftpsFileInfo>();

        try {
            PayrollServices.beginUnitOfWork();

            // get list of files to send to bank
            for (EftpsFile eftpsFile : EftpsFile.getPendingTransmissionEftpsFiles()) {
                mLogger.info("Preparing to upload EFTPS file " + eftpsFile.getFileName() + " to TFA...");
                fileList.add(new EftpsFileInfo(eftpsFile));
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return fileList;
    }

    private void handleFileUploadError(EftpsFileInfo pEftpsFileInfo, Throwable pThrowable) {
        String msg = String.format("Error sending EFTPS file %s to TFA.", pEftpsFileInfo.mFile.getName());
        mLogger.error(String.format("%s File skipped and moved to error directory. ", msg), pThrowable);
        EftpsUtil.updateEftpsFileErrorStatus(pEftpsFileInfo.mSpcfUniqueId, pEftpsFileInfo.mFile);
    }
}
