package com.paycycle.util;

import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PgpUtils {

    protected static final SpcfLogger logger = SpcfLogManager.getLogger(PgpUtils.class);

    public static List<String> getTfaPgpKeys() {
        List<String> keys = new ArrayList<String>();
        keys.add(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs,"psp_tfa_public_key"));
        keys.add(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs,"psp_tfa_intuit_public_key"));
        return keys;
    }

    public static File getUnencryptedFile(File file) throws Exception {
        return getUnencryptedFile(file
                ,ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs,"psp_tfa_intuit_private_key")
                ,ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs,"psp_tfa_intuit_key_password"));
    }

    public static File getUnencryptedFile(File file,String pDecryptionKey,
                                          String pDecryptionKeyPassword) throws Exception {
        String workingDir = FilenameUtils.getFullPath(file.getAbsolutePath());
        String encryptedFileName = FilenameUtils.getName(file.getAbsolutePath());
        String decryptedFileName = FilenameUtils.getBaseName(file.getAbsolutePath());
        PgpFileUtils.pgpDecryptUnsingedFile( workingDir
                , encryptedFileName
                , decryptedFileName
                , pDecryptionKey
                , pDecryptionKeyPassword
                , true);
        File result = new File(workingDir+decryptedFileName);
        return result;
    }

}
