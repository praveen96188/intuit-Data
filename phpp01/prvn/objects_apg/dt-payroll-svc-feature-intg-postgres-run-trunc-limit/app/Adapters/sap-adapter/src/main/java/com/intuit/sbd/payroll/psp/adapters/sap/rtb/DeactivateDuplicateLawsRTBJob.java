

package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.PayrollItemStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.List;

/**
 * Created by nloharuka on 3/27/18.
 */
public class DeactivateDuplicateLawsRTBJob extends BaseRTBJob {
    private static final SpcfLogger logger = PayrollServices.getLogger(DeactivateDuplicateLawsRTBJob.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    private static final String PSID = "PSID";
    private static final String AGENCY_ID = "AGENCY_ID";
    private static final String LAW_TYPE = "LAW_TYPE";
    private static final int RECORD_PROCESSING_THRESHOLD = 10;

    private SourceSystemCode mSourceSystemCode;
    private int successfulCount = 0;


    public DeactivateDuplicateLawsRTBJob(byte[] fileBinary) throws Exception {
        super(fileBinary);
        mSourceSystemCode = SourceSystemCode.QBDT;
    }

    @Override
    public JobResult process() throws RTBJobException {
        JobResult jobResult = new JobResult();
        String sourceCompanyId = null;
        String agencyId=null;
        String lawType=null;
        logger.info("Deactivating Duplicate Laws for " + recordSize + " records. RecordSize=" + recordSize);

        for (int i = 0; i < recordSize; i++) {

            try {
                PayrollServices.beginUnitOfWork();
                sourceCompanyId = ((List) excelKeyValueList.get(PSID)).get(i).toString().trim();
                agencyId = ((List) excelKeyValueList.get(AGENCY_ID)).get(i).toString().trim();
                lawType = ((List) excelKeyValueList.get(LAW_TYPE)).get(i).toString().trim();
                //Step 1: Deactivate duplicate laws
                deactivateDuplicateLaws(sourceCompanyId.trim(), agencyId.trim(),lawType.trim(),jobResult);
                PayrollServices.commitUnitOfWork();
                successfulCount++;
                logger.info("Successfully deactivated duplicate laws for PSID='"+ sourceCompanyId+"' Agency='"+agencyId+"' Law Type='"+lawType+"'");
                jobResult.addInfoMessage("Deactivation of duplicate Laws is successful for PSID='" + sourceCompanyId+"' Agency='"+agencyId+"' Law Type='"+lawType+"'");
            } catch (Throwable pThrowable) {
                PayrollServices.rollbackUnitOfWork();
                jobResult.addErrorMessage("Deactivation of duplicate laws failed for PSID='" + sourceCompanyId+"' Agency='"+agencyId+"' Law Type='"+lawType+"'");
                jobResult.addErrorMessage("--------------------------------------------------------------------");
                logger.error("Deactivation of duplicate laws is unsuccessful for PSID='" + sourceCompanyId+"' Agency='"+agencyId+"' Law Type='"+lawType+"'");
                pThrowable.printStackTrace();
            }
        }// for each record
        jobResult.addInfoMessage("Finished processing.");
        jobResult.addInfoMessage("===========================REPORT============================");
        jobResult.addInfoMessage("Total number of agency and law type for which duplicate laws needs to be deactivated " + recordSize);
        jobResult.addInfoMessage("Successfully deactivated " + successfulCount);
        jobResult.addInfoMessage("Failed to be deactivated " + (recordSize - successfulCount));
        jobResult.setSuccess(true);
        jobResult.addInfoMessage("=============================================================");
        logger.info("Deactivation of duplicate laws is completed. Successful Deactivation Count=" + successfulCount + " TotalRecords=" + recordSize);
        return jobResult;

    }

    @Override
    public JobResult validate() throws RTBJobException {
        JobResult jobResult = new JobResult();

        if (!excelKeyValueList.containsKey(PSID)) {
            logger.error("Validation failure for Duplicate Laws Deactivation  - PSID missing");
            jobResult.addErrorMessage("PSID missing !!");
            jobResult.setSuccess(false);
            return jobResult;

        }

        if (recordSize > RECORD_PROCESSING_THRESHOLD) {
            logger.error("Record size is more than excepted.");
            jobResult.addErrorMessage("Record size is more than excepted. Max Supported number of records are " + RECORD_PROCESSING_THRESHOLD);
            jobResult.setSuccess(false);
            return jobResult;
        }

        jobResult.setSuccess(true);

        return jobResult;

    }

    /**
     * Deactivate Duplicate Laws
     * @param pCompanyId
     * @throws Throwable
     */
    private void deactivateDuplicateLaws(String pCompanyId, String pAgencyId, String pLawType, JobResult pJobResult) throws Throwable {

        //find company
        Company company = Company.findCompany(pCompanyId, mSourceSystemCode);
        if (company == null) {
            pJobResult.addErrorMessage("Company not found for PSID='" + pCompanyId + "'.");
            aeFactory.throwGenericException("Company not found for PSID='" + pCompanyId + "'.");
        }

        Expression<CompanyLaw> where=new Query<CompanyLaw>()
                .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)
                        .And(CompanyLaw.CompanyAgency().Agency().AgencyId().equalTo(pAgencyId))
                        .And(CompanyLaw.Law().LawAbbrev().equalTo(pLawType))
                        .And((CompanyLaw.FilingStatus().equalTo(PayrollItemStatus.Active))
                                .Or(CompanyLaw.FilingStatus().equalTo(PayrollItemStatus.Active))));

        DomainEntitySet<CompanyLaw> companyLaws= Application.find(CompanyLaw.class, where);

        if (companyLaws == null || companyLaws.size()==0) {
            pJobResult.addErrorMessage("Duplicate Company Laws not found for PSID='" + pCompanyId + "' Agency ID='"+pAgencyId+"' Law Type='"+pLawType+"'.");
            aeFactory.throwGenericException("Duplicate Company Laws not found for PSID='" + pCompanyId + "' Agency ID='"+pAgencyId+"' Law Type='"+pLawType+"'.");
        }

        for(CompanyLaw cl: companyLaws){
            cl.setFilingStatus(PayrollItemStatus.Inactive);
            cl.setStatus(PayrollItemStatus.Inactive);
        }


    }

}
