package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail;

import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3DownloadException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;

public class S3FileDownloader implements FileDownloader {

    public void downloadFile(String srcFilePath, String destFilePath) {
        String bucketName = BatchUtils.getConfigString(S3UploadUtils.PSP_BATCHJOBS_S3_BUCKET);
        try {
            S3UploadUtils.downloadFromS3FileStore(bucketName, srcFilePath, destFilePath);
        } catch (S3DownloadException e) {
            throw new RuntimeException(e);
        } catch (S3ConnectionException e) {
            throw new RuntimeException(e);
        }
    }
}
