package com.intuit.ems.payroll.psp;

import com.intuit.ems.payroll.psp.vmpRealmUpdate.CancelVMPServiceProcessor;
import com.intuit.ems.payroll.psp.vmpRealmUpdate.DeleteCompanyRealmProcessor;
import com.intuit.ems.payroll.psp.vmpRealmUpdate.RunningMode;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;

public class CancelVMPAndRealmUpdate {

    private static SpcfLogger logger = SpcfLogManager.getLogger(CancelVMPAndRealmUpdate.class);

    private static RunningMode mode;

    private static String jiraId;

    private static String filePath;

    public static void main(String[] args) throws Exception {
        try {
            Application.initialize();
            parseArgs(args);
            logger.info(String.format("Jira:%s mode:%s filePath:%s", jiraId, mode.name(), filePath));
            if (mode == RunningMode.CANCEL_VMP) {
                CancelVMPServiceProcessor cancelVMPServiceProcessor= PayrollApplicationBeanFactory.getBean(CancelVMPServiceProcessor.class);
                cancelVMPServiceProcessor.process(jiraId, filePath);
            } else if (mode == RunningMode.DELETE_REALM) {
                DeleteCompanyRealmProcessor deleteCompanyRealmProcessor = PayrollApplicationBeanFactory.getBean(DeleteCompanyRealmProcessor.class);
                deleteCompanyRealmProcessor.process(jiraId, filePath);
            }
        }catch (Exception e){
            Application.uninitialize();
        }
    }

    private static void parseArgs(String[] args) throws Exception {
        for (String arg : args) {
            String argParts[]= arg.split("=");
            switch(argParts[0].trim()){
                case "Jira":
                    jiraId=argParts[1];
                    break;
                case "Mode":
                    mode=RunningMode.valueOf(argParts[1].trim());
                    break;
                case "FilePath":
                    filePath=argParts[1];
                    break;
            }
        }
        if(StringUtils.isEmpty(jiraId) || mode==null || StringUtils.isEmpty(filePath)){
            logger.error(String.format("Incorrect input jiraId=%s, mode=%s, psidList size=%s ",jiraId,mode,filePath));
            throw new Exception("Incorrect input");
        }
    }

}
