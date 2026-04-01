package com.intuit.sbd.payroll.psp.common.utils;

public class S3DownloadException extends Exception {
    public S3DownloadException(String message,Throwable cause){
        super(message,cause);
    }
}
