package com.intuit.sbd.payroll.psp.adapters.sap.adapter;

import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPLedgerOperationJob;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPQuarter;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSUICreditsJob;
import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPSystemParameter;
import com.intuit.sbd.payroll.psp.domain.LedgerOperationJob;
import com.intuit.sbd.payroll.psp.domain.SUICreditsJob;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import org.apache.commons.lang.StringUtils;

/**
 * User: rnorian
 * Date: Apr 28, 2010
 * Time: 4:54:14 PM
 */
public class AdministrationTranslator {
    public static SAPSystemParameter getSAPSystemParameter(SystemParameter pSystemParameter) {
        SAPSystemParameter sapSystemParameter = new SAPSystemParameter();
        sapSystemParameter.setCode(pSystemParameter.getSystemParameterCd());
        sapSystemParameter.setValue(pSystemParameter.getSystemParameterValue());
        sapSystemParameter.setDescription(pSystemParameter.getSystemParameterDescription());
        sapSystemParameter.setOrg(pSystemParameter.getSystemParameterOrg());
        return sapSystemParameter;
    }

    public static SAPLedgerOperationJob getSAPLedgerOperationJob(LedgerOperationJob pLedgerOperationJob, int pTotalRecords, int pProcessedRecords, String type) {
        SAPLedgerOperationJob sapJob = new SAPLedgerOperationJob();
        sapJob.setId(pLedgerOperationJob.getId().toString());
        sapJob.setFinishTime(SAPTranslator.getDateFromSpcfCalendar(pLedgerOperationJob.getFinishTime()));
        sapJob.setStartTime(SAPTranslator.getDateFromSpcfCalendar(pLedgerOperationJob.getStartTime()));
        sapJob.setStatus(pLedgerOperationJob.getStatus().toString());
        sapJob.setUploadTime(SAPTranslator.getDateFromSpcfCalendar(pLedgerOperationJob.getCreatedDate()));
        if (StringUtils.isNotEmpty(type)) {
            sapJob.setType(type);
        } else {
            sapJob.setType(pLedgerOperationJob.getJobType().toString());
        }
        sapJob.setTotalRecords(pTotalRecords);
        sapJob.setProcessedRecords(pProcessedRecords);
        sapJob.setDescription(pLedgerOperationJob.getDescription());
        return sapJob;
    }

    public static SAPSUICreditsJob getSAPSUICreditsJob(SUICreditsJob job) {
        SAPSUICreditsJob sapJob = new SAPSUICreditsJob();
        sapJob.setId(job.getId().toString());
        sapJob.setCreatedDate(SAPTranslator.getDateFromSpcfCalendar(job.getCreatedDate()));
        sapJob.setModifiedDate(SAPTranslator.getDateFromSpcfCalendar(job.getModifiedDate()));
        if (job.getPaymentTemplate() != null) {
            sapJob.setPaymentTemplate(job.getPaymentTemplate().getPaymentTemplateCd());
        } else {
            sapJob.setPaymentTemplate("");
        }
        sapJob.setStatus(job.getStatus().toString());
        sapJob.setQuarter(new SAPQuarter(job.getYear(), job.getQuarter()));
        if(job.getProcessedFile()!=null)
        {
            sapJob.setProcessedFileExists(true);
        }
        return sapJob;
    }
}
