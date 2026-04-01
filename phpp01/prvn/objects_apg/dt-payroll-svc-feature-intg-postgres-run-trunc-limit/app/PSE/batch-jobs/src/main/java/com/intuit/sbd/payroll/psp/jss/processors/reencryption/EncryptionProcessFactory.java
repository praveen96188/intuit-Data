package com.intuit.sbd.payroll.psp.jss.processors.reencryption;

import com.intuit.sbg.recrypt.config.Param;

public class EncryptionProcessFactory {
    public EncryptionProcess getEncryptionProcess(Param.DatabaseType databaseType) {
        switch (databaseType) {
            case S3:
                return new FileEncryptionProcess();
            default:
                return new PspEncryptionProcess();
        }
    }
}
