package com.intuit.sbd.payroll.psp.jss.processors.workerscomp.service;

import com.intuit.iam.utilities.StringUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Payroll;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;
import java.util.List;

@Component
public class WorkersCompFileGeneratorSplitLimit extends WorkersCompFileGenerator<com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Payroll> {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(WorkersCompFileGeneratorNext.class);
    private static final String WC_FILE_PATH = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "wc_server_insurepay_send_dir");

    /**
     * @param payroll
     * @return
     */
    @Override
    protected String getXMLFileName(Payroll type) {
        String companyId = null;
        if (type.getBusinesses() != null
                && type.getBusinesses().getItem() != null
                && type.getBusinesses().getItem().size() > 0) {
            List<com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Business> businesses = type.getBusinesses().getItem();
            if (businesses.size() == 0) {
                throw new RuntimeException("No businesses were found in payroll");
            }
            for (com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Business business:
                    businesses) {
                List<JAXBElement<?>> content = business.getContent();
                for (JAXBElement ele : content
                ) {
                    String val = ele.getName().toString();
                    if (StringUtils.equals(val, "companyId")) {
                        companyId = ele.getValue().toString();
                        break;
                    }
                }
            }
        }
        String timeInMs = Long.toString(SpcfCalendar.getCurrentTimeInMilliseconds());
        return companyId + "-" + timeInMs;
    }

    /**
     * @return
     */
    @Override
    protected List<String> getWCPgpKeys() {
       return BatchUtils.getWCSplitLimitPgpKeys();
    }

    /**
     * @return
     */
    @Override
    protected String getFilePath() {
        return WC_FILE_PATH;
    }
    @Override
    protected String getFileName() {
        return "intuit-trupay.xsd";
    }

}
