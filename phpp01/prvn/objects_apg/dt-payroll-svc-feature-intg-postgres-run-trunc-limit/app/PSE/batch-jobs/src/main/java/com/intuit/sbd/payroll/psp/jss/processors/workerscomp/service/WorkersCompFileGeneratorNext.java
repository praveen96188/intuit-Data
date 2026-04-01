package com.intuit.sbd.payroll.psp.jss.processors.workerscomp.service;

import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.schema.Payroll;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.schema.*;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.Writer;
import java.util.List;

@Component
public class WorkersCompFileGeneratorNext extends WorkersCompFileGenerator<com.intuit.sbd.payroll.psp.jss.processors.workerscomp.schema.Payroll> {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(WorkersCompFileGeneratorNext.class);
    private static final String WC_FILE_PATH = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "wc_server_send_dir");
    /**
     * @param payroll
     * @return
     */
    @Override
    protected String getXMLFileName(Payroll type) {
        String companyId = null;
        if (type.getBusinesses() != null
                && type.getBusinesses().getBusiness() != null
                && type.getBusinesses().getBusiness().size() > 0)
        {
            List<Payroll.Businesses.Business> businesses = type.getBusinesses().getBusiness();
            if(businesses.size() == 0) {
                throw new RuntimeException("No businesses were found in payroll");
            }
            companyId = businesses.get(0).getCompanyId();
        }
        String timeInMs = Long.toString(SpcfCalendar.getCurrentTimeInMilliseconds());
        return companyId + "-" + timeInMs;
    }

    /**
     * @return
     */
    @Override
    protected List<String> getWCPgpKeys() {
       return BatchUtils.getWCPgpKeys();
    }

    /**
     *
     */
    @Override
    protected String getFileName() {
        return "intuit.xsd";
    }

    /**
     * @return
     */
    @Override
    protected String getFilePath() {
        return WC_FILE_PATH;
    }
}
