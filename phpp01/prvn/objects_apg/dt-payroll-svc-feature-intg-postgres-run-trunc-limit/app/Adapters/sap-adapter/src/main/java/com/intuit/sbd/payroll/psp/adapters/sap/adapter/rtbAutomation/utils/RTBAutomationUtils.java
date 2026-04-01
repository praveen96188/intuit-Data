package com.intuit.sbd.payroll.psp.adapters.sap.adapter.rtbAutomation.utils;

import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.rtbAutomation.RTBAutomationAdapter;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;

/**
 * Created by smodgil on 27/08/19.
 * To be used for common code and constants for RTB Automation
 */

public class RTBAutomationUtils {

    private static final SpcfLogger logger = PayrollServices.getLogger(RTBAutomationAdapter.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    /**
     * This method is created to in case exception is thrown during automation job execution
     * @param t
     * @return Custom Message to be displayed in UI
     */
    public static String handleRTBAutomationException(Throwable t){

        if(t.getCause()!=null && t.getCause().getMessage().toString().contains("ORA")) {

            int oraCount = StringUtils.countMatches(t.getCause().getMessage(), "ORA");
            if (oraCount > 1) {
                int beginIndex = t.getCause().getMessage().toString().indexOf(":") + 1;
                int endIndex = t.getCause().getMessage().toString().indexOf("ORA", beginIndex + 1);
                String errorMsg = t.getCause().getMessage().toString().substring(beginIndex, endIndex);
                if (errorMsg.equalsIgnoreCase("no data found")) {
                    return "No data found";
                } else {
                    return "DupEE Update Failed: Internal Server Error";
                }
            }else if(t.getCause().getMessage().equals("Incorrect Details")){
                return "No data found";
            }else{
                return "DupEE Update Failed: Internal Server Error";
            }
        }else{
            return "DupEE Update Failed: Internal Server Error";
        }
    }
}
