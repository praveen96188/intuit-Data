package com.intuit.sbd.payroll.psp.adapters.ade.mapping;

import com.intuit.payroll.agency.dao.DataStore;

/**
 * Created with IntelliJ IDEA.
 * User: shivanandad069
 * Date: 9/29/13
 * Time: 9:04 PM
 */
public class JurisdictionIdMapper {
    private static final DataStore mStore = DataStore.getDataStore();
    private static final String IRS = "IRS";
    private static final String FEDERAL = "FEDERAL";
    private static final String FD = "FD";

    public static String getStateCode(String complianceJurisdictionId) {
        String[] splitJurisdiction = complianceJurisdictionId.split("_");
        if(splitJurisdiction.length == 2) {
            if (FEDERAL.equalsIgnoreCase(splitJurisdiction[1])) {
                return IRS;
            } else {
                return splitJurisdiction[1];
            }
        } else {
            return complianceJurisdictionId;
        }
    }

    public static String getComplianceJurisdictionId(String countryCode, String stateCode) {
        if (FD.equalsIgnoreCase(stateCode) || IRS.equalsIgnoreCase(stateCode)) {
            stateCode = FEDERAL;
        }
        return countryCode + "_" + stateCode;
    }

    public static boolean isValidJurisdictionId(String jurisdiction) {
        return jurisdiction != null && mStore != null && mStore.getJurisdiction(getStateCode(jurisdiction)) != null;
    }

}
