package com.intuit.sbd.payroll.psp.jss.processors.reencryption;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.sbg.recrypt.config.Param;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataReEncryptionUtils {

    private static final SpcfLogger LOGGER = SpcfLogManager.getLogger(DataReEncryptionUtils.class);

    public static void setSystemProperties(String paramFilePath) throws IOException {
        Param.DatabaseType databaseType = getDatabaseTypeFromParamFile(paramFilePath);
        String project;
        switch (databaseType) {
            case S3:
                project = "S3";
                break;
            default:
                project = "PSP";
        }
        LOGGER.info("Setting project.name=" + project);
        System.setProperty("project.name", project);
    }

    public static Param.DatabaseType getDatabaseTypeFromParamFile(String paramFilePath) throws IOException {
        Param param = getParamObject(paramFilePath);
        return param.getDatabaseType();
    }

    private static Param getParamObject(String path) throws IOException {
        return new ObjectMapper().readValue(loadConfigFile(path), Param.class);
    }

    private static String loadConfigFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}
