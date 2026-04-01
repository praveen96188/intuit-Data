package com.intuit.sbd.payroll.psp.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.net.URL;

public class FileUtils {

    private static final SpcfLogger logger = Application.getLogger(FileUtils.class);

    public static String readClasspathFileContent(String path) {
        String resourcePath = StringUtils.join(ResourceUtils.CLASSPATH_URL_PREFIX, path);
        String fileContents = null;
        try {
            URL resourceURL = ResourceUtils.getURL(resourcePath);
            fileContents = IOUtils.toString(resourceURL);
        } catch (IOException e) {
            logger.error("Error reading path "+path, e);
        }
        return fileContents;
    }

}
