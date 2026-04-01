package com.intuit.sbd.payroll.psp.jss.processors.reencryption;

import com.intuit.sbg.recrypt.psp.ReEncryptionApplication;

public class PspEncryptionProcess implements EncryptionProcess {
    public void startEncryption(String paramFilePath) throws Exception {
        DataReEncryptionUtils.setSystemProperties(paramFilePath);
        ReEncryptionApplication.main(new String[0]);
    }
}
