package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail;

public interface FileDownloader {
    void downloadFile(String srcFilePath, String destFilePath);
}
