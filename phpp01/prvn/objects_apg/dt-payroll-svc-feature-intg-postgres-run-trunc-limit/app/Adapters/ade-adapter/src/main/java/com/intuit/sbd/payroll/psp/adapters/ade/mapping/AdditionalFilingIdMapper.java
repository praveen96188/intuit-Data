package com.intuit.sbd.payroll.psp.adapters.ade.mapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: znorcross
 * Date: 5/1/14
 * Time: 12:37 PM
 */
public class AdditionalFilingIdMapper {
    //Mapping if agencyids in PSP to agencyids mapped in CEP/AED
    private static final Map<String, String> PSP_ATF_LAW_ID_TO_COMPLIANCE_ADDITIONAL_ID_MAP = new HashMap<String, String>();
    private static final Map<String, String> COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP = new HashMap<String, String>();


    static {
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_MA_SC_ER_EMAC", "9018");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_MA_SC_ER_UHI", "193");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_NV_SC_ER_SBC", "9019");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_MO_SC_ER_FIA", "9008");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_MO_SC_ER_UAS", "195");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_AZ_SUI_Credit", "9014");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_CO_SUI_Credit", "9005");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_IL_WH_Credit", "9004");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_KY_SUI_Credit", "9015");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_MA_SUI_Credit", "9000");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_MA_UHI_Credit", "9001");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_MI_SUI_Credit", "9011");
        // this atf law is not included in the additional filing static table yet COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_NE_SUI_Credit", "9007");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_NH_SUI_Credit", "9009");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_NV_SUI_Credit", "9006");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_PA_SUI_Credit", "9012");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_SC_SUI_Credit", "9010");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_TX_SUI_Credit", "9016");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_VT_SUI_Credit", "9003");
        COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.put("US_WI_SUI_Credit", "9013");


        // reverse map
        for (String pspAgencyId : COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.keySet()) {
            PSP_ATF_LAW_ID_TO_COMPLIANCE_ADDITIONAL_ID_MAP.put(COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.get(pspAgencyId), pspAgencyId);
        }
    }

    public static String getPspAtfLawIdByComplianceAdditionalId(String complianceAgencyId) {
        return COMPLIANCE_ADDITIONAL_ID_TO_PSP_ATF_LAW_ID_MAP.get(complianceAgencyId);
    }

    public static String getComplianceAdditionalIdByPspAtfLawId(String pspAgencyId) {
        return PSP_ATF_LAW_ID_TO_COMPLIANCE_ADDITIONAL_ID_MAP.get(pspAgencyId);
    }
}

