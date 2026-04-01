package com.intuit.sbd.payroll.psp;

import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 */
public class NameOverrides {
    public static final String OVERRIDE_PROPERTIES_FILE = "manual.overrides.database.naming.properties";

    private static Properties overrideProperties;
    private static File nameOverrideFile;

    static public void load(String bedlFolderPath) {
        nameOverrideFile = new File(bedlFolderPath, OVERRIDE_PROPERTIES_FILE);
        if (!nameOverrideFile.exists())
            throw new IllegalArgumentException(nameOverrideFile.getAbsolutePath() + " : file does not exist");

        overrideProperties = new Properties();
        try {
            overrideProperties.load(new FileInputStream(nameOverrideFile));
        }
        catch (Exception e) {
            throw new RuntimeException(nameOverrideFile.getAbsolutePath() + " : failed to load", e);
        }
    }

    static public void store() {
        File outputFile = null;
        try {
            outputFile = new File(nameOverrideFile.getParent(), "psp.database.naming.properties");
            overrideProperties.store(new FileOutputStream(outputFile), null);
        }
        catch (IOException e) {
            throw new RuntimeException(outputFile.getAbsolutePath() + " : failed to store", e);
        }
    }

    static public String getOverride(String objectName) {
        return overrideProperties.getProperty(objectName);
    }

    static public void putOverride(String originalName, String overridenName) {
        if (!originalName.equals(overridenName) && !overrideProperties.containsKey(originalName)) {
            overrideProperties.put(originalName, overridenName);
        }
    }


}
