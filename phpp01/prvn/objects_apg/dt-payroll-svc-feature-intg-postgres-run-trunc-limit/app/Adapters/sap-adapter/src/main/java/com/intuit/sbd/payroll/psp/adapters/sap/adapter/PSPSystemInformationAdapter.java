package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.FlexMethod;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPSPSystemInformation;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import flex.messaging.FlexContext;
import org.hibernate.FlushMode;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Reports system configuration information -- build number, model version, schema version, etc.
 */
public class PSPSystemInformationAdapter {

    @FlexMethod
    public SAPPSPSystemInformation getSystemInformation() {
        SAPPSPSystemInformation systemInformation = new SAPPSPSystemInformation();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);

            String pspTimeString = PSPDate.getPSPTime().toLocal().toString();
            systemInformation.setPspDate( pspTimeString );

            String pspBuildNumber = WARManifestReader.getBuildNumber();
            systemInformation.setBuildNumber( pspBuildNumber );

            String schemaVersion =
                    (String) Application.executeNamedQuery("maxDatabasePatch",new String[0], new Object[0]).get(0);

            systemInformation.setSchemaVersion(schemaVersion);

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return systemInformation;
    }
}

/**
 * Reads the SAP application's WAR file manifest and
 * extracts custom build number property
 */
class WARManifestReader {

    /**
     * NOTE: WEIRD BEHAVIOR -- to enable unit testing, errors reading build number are
     * returned as the build number.
     * @return
     */
    static public String getBuildNumber() {
        String buildNumber = "";
        try {
            Manifest manifest = readWARManifest();
            Attributes mainAttributes = manifest.getMainAttributes();
            buildNumber = mainAttributes.getValue("PSP-Build-Number");
        }
        catch (Throwable t) {
            buildNumber = t.getMessage();
        }

        return buildNumber;
    }

    static private Manifest readWARManifest() throws IOException {
        InputStream is = openManifestStream();
        return new Manifest(is);
    }

    static private InputStream openManifestStream() {
        InputStream is = FlexContext.getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF");
        if (is == null) {
            throw new RuntimeException("Could not open resource stream to WAR file manifest");
        }
        return is;
    }
}


