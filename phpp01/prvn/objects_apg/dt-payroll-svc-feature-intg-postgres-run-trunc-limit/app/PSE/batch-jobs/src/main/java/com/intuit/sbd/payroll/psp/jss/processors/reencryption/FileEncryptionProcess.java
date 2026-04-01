package com.intuit.sbd.payroll.psp.jss.processors.reencryption;


import com.intuit.sbg.recrypt.files.ReEncryptionApplication;

public class FileEncryptionProcess implements EncryptionProcess {
    public void startEncryption(String paramFilePath) throws Exception {
        DataReEncryptionUtils.setSystemProperties(paramFilePath);
        ReEncryptionApplication.main(new String[0]);
    }
}
