package com.intuit.sbd.payroll.psp.adapters.ews.v1_9;

/**
 * @author Jeff Jones
 */
public class EwsAdapterConst {

    //xPath Constants
    public static final String PSIMESSAGE_PATH = "//PSIMessage";
    public static final String ACTION_PATH = "//Action";
    public static final String PSID_PATH = "//Company/PSID";
    public static final String EIN_PATH = "//Company/EIN";
    public static final String PIN_PATH = "//Company/PIN";
    public static final String OLD_PIN_PATH = "//Company/OldPIN";
    public static final String FEATURE_NAME_PATH = "//Feature/FeatureName";
    public static final String ERROR_CODE = "//RespStatus/ErrorCode";

    //String for masking the PIN
    public static final String MASK_STRING = "************";

    public static final String NAMESPACE = "http://www.w3.org/2000/xmlns/";
    public static final String EWS_NAMESPACE = "http://ews.adapters.psp.payroll.sbd.intuit.com/";

    //Configuration
    public static final String RUN_DEBUG_CODE = "runDebugCode";

    //SubTypes
    public static final String ASSISTED = "QB Payroll Assisted";
    public static final String ASSISTED_ADV = "QB Payroll Assisted Adv";
    public static final String BASIC_LIMITED = "QB Payroll Basic Limited";
    public static final String BASIC_UNLIMITED = "QB Payroll Basic Unlimited";
    public static final String ENHANCED = "QB Payroll Enhanced";
    public static final String ENHANCED_ACCOUNTANT = "QB Payroll Enhanced Accountant";
    public static final String ENHANCED_UNLIMITED = "QB Payroll Enhanced Unlimited";
    public static final String NEW_BASIC_UNLIMITED = "QB Payroll New Basic Unlimited";
    public static final String STANDARD = "QB Payroll Standard";
    public static final String BASIC_0_TO_3_EMP = "QB Payroll Basic 0-3 Emp";
    public static final String ENHANCED_0_TO_3_EMP = "QB Payroll Enhanced 0-3 Emp";
    public static final String PAP_ENH_ACCT = "QB Payroll PAP Enh Acct";
    public static final String FREE_BASIC_1 = "QB Payroll Free Basic 1";

    //Status Pages
    public static final String STATUS_APP = "STATUSPAGE.PGM";    

}
