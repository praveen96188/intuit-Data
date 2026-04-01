package com.intuit.sbd.payroll.psp.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.SourcePayrollParameter;
import com.intuit.sbd.payroll.psp.domain.SourcePayrollParameterCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Jun 29, 2008
 * Time: 9:46:56 PM
 */
public class OFXAPPVERObject {
    public static String VENTI_MAJOR = "20";
    public static String EMERALD_MAJOR = "21";
    public static String BLAZE_MAJOR = "22";
    public static String NIRVANA_MAJOR = "23";
    public static String RUBY_MAJOR = "24";
    public static String MANGO_MAJOR = "25";

    private Integer minAppQBVersionSupported = null;

    public static final String appVersionRegExPatten = "((\\d+)\\.\\d+\\.[ABRP]\\.(\\d+))([/](\\d+)(\\#)?(\\w+)?\\z)?";
    public static final Pattern appVersionRegEx = Pattern.compile(appVersionRegExPatten);

    /*
     *
     * @param appVersion
     */
    public OFXAPPVERObject(String appVersion) {
        parseAndStoreVersion(appVersion, true);
    }

    public OFXAPPVERObject(String appVersion, boolean requireExtendedInfo) {
        parseAndStoreVersion(appVersion, requireExtendedInfo);
    }

    private void parseAndStoreVersion(String appVersion, boolean requireExtendedInfo) {
        properlyFormattedAppID = Boolean.FALSE;
        fullAPPIDStr = appVersion;
        if (appVersion==null) {
            return;
        }
        String appVersionTrimmed = appVersion.trim();
        Matcher m = appVersionRegEx.matcher(appVersionTrimmed);
        if (!m.matches()) {
            return;
        }

        // missing taxTableId info
        if (requireExtendedInfo && m.groupCount() < 4) {
            return;
        }

        qbVersionStr = m.group(1);
        try {
            intQBVersion = new Integer(m.group(2));
            intRNumber = new Integer(m.group(3));
        } catch (NumberFormatException e) {
            return;
        }

        if (m.groupCount() > 4) {
            taxTableId = m.group(5);
        }

        if (m.groupCount() > 6) {
            flavorId = m.group(7);
        }

        properlyFormattedAppID = Boolean.TRUE;
    }


    Boolean properlyFormattedAppID = null;
    private String fullAPPIDStr = null;     // e.g. 17.01.n.10/14586#pro
    private Integer intQBVersion = null;    // e.g. 17
    private Integer intRNumber = null;      // e.g. 10
    private String qbVersionStr = null;     // e.g. 17.01.R.10
    private String taxTableId = null;       // e.g. 14586
    private String flavorId = null;         // e.g. pro

    public Integer getIntQBVersion() {
        return intQBVersion;
    }

    public Integer getIntRNumber() {
        return intRNumber;
    }

    public String getQBVersionStr() {
        return qbVersionStr;
    }

    public String getFullAPPIDStr() {
        return fullAPPIDStr;
    }

    public String getTaxTableId() {
        return taxTableId;
    }

    public String getFlavorId() {
        return flavorId;
    }

    /*
     *
     * @return
     */
    public boolean isProperlyFormatted() {
        return properlyFormattedAppID;
    }

    /**
     * Verify that the app version from the OFX has not been sunsetted.
     *
     * @return - true if valid, false if not.
     */
    public boolean isQBVersionActive() {
        // MinQBVersionSupported is cached on the domain level.
        String minAppQBVersionSupportedStr =
                SourcePayrollParameter.findSourcePayrollParameter(
                        SourceSystemCode.QBDT, SourcePayrollParameterCode.MinQBVersionSupported).getParameterValue();
        minAppQBVersionSupported = new Integer(minAppQBVersionSupportedStr);
        return (intQBVersion >= minAppQBVersionSupported);
    }

    /**
     * Verify that the app version string has not been marked as unsupported.
     *
     * @return - true if valid, false if not.
     */
    public boolean isQBBuildSupported() {
        // UnsupportedQBVersionList is cached on the domain level.
        String listOfInvalidBuildIds = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT, SourcePayrollParameterCode.UnsupportedQBVersionList).getParameterValue();
        String[] invalidQBBuildStrStrArray = listOfInvalidBuildIds.split("\\|");
        List<String> invalidQBBuildStrList = new ArrayList<String>();
        Collections.addAll(invalidQBBuildStrList, invalidQBBuildStrStrArray);
        return !invalidQBBuildStrList.contains(qbVersionStr);
    }

    public boolean isTaxTableSupported() {

        String listOfInvalidTaxTables = SourcePayrollParameter.findSourcePayrollParameter(
                SourceSystemCode.QBDT, SourcePayrollParameterCode.UnsupportedTaxTableList).getParameterValue();
        List<String> invalidTaxTableList = Arrays.asList(listOfInvalidTaxTables.split("\\|"));
        if(invalidTaxTableList.contains(taxTableId)) {
            return false;
        }

        try {
            int minSupportedTaxTable = SourcePayrollParameter.findIntValue(SourceSystemCode.QBDT, SourcePayrollParameterCode.MinSupportedTaxTableVersion);
            int taxTableVersion = Integer.parseInt(taxTableId);
            return taxTableVersion >= minSupportedTaxTable;
        } catch (NumberFormatException e) {
            Application.getLogger(OFXAPPVERObject.class).error("Tax table version: " + taxTableId + " is invalid.");
            return false;
        }
    }

    /**
     * Determines if the Quickbooks Version is about to be sunset based on the PSPDate
     * @return true if its about to be sunset, false otherwise
     */
    public boolean isMinQBVersionSupported() {
        boolean isMinQBVersionSupported = false;
        String lastSupportedVersionString =
                SourcePayrollParameter.findSourcePayrollParameter(
                        SourceSystemCode.QBDT, SourcePayrollParameterCode.MinQBVersionSupported).getParameterValue();
        minAppQBVersionSupported = new Integer (lastSupportedVersionString);
        if ((this.intQBVersion.intValue() == minAppQBVersionSupported)) {
            isMinQBVersionSupported = true;
        }
        return isMinQBVersionSupported;
    }

    /**
     * Determines if the Quickbooks Version supports usage billing
     * @return true if it does, false otherwise
     */
    public boolean usageBillingSupported() {
        return getIntQBVersion() != null && getIntRNumber() != null && (getIntQBVersion() >= 24 || (getIntQBVersion() == 23 && getIntRNumber() >= 3) || (getIntQBVersion() == 22 && getIntRNumber() >= 10));
    }

    /**
     * Determines if need to send back listIds
     * @return true if it does, false otherwise
     */
    public boolean listIdLoopBackSupported() {
        if (getIntQBVersion() == null || getIntRNumber() == null) {
            return false;
        } else {
            try {
                String supportedQBDTVerNumber = SystemParameter.findStringValue(SystemParameter.Code.LISTID_SUPPORTED_QBDT_VER, "");
                int majorVer = 9999;
                for (String ver : supportedQBDTVerNumber.split(",")) {
                    int index = ver.indexOf('R');
                    majorVer = Integer.parseInt(ver.substring(0, index));
                    int minorVer = Integer.parseInt(ver.substring(index+1));

                    if (getIntQBVersion() == majorVer && getIntRNumber() >= minorVer) {
                        return true;
                    }
                }

                if (getIntQBVersion() > majorVer) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
    }

    public boolean warningSupported() {
        return getIntQBVersion() != null && getIntRNumber() != null && ((getIntQBVersion() >= 24 && getIntRNumber() >= 3) || (getIntQBVersion() == 23 && getIntRNumber() >= 8) || (getIntQBVersion() == 22 && getIntRNumber() >= 14));
    }

    public boolean isExplicitWatermarkRequired() {

        // Greater than Mango all version built on symphony model with list ids in ofx request.

        return getIntQBVersion() != null && getIntRNumber() != null &&
                ((getIntQBVersion()>= Integer.parseInt(MANGO_MAJOR))|| (getIntQBVersion() >= Integer.parseInt(RUBY_MAJOR) && getIntRNumber() >= 3) ||
                        (getIntQBVersion() == Integer.parseInt(NIRVANA_MAJOR) && getIntRNumber() >= 9) ||
                        (getIntQBVersion() == Integer.parseInt(BLAZE_MAJOR) && getIntRNumber() >= 14));
    }
}
