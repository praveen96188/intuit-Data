package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.EmployeeTax;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TaxTableMiscData;
import com.intuit.sbd.payroll.psp.util.HqlBuilder;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rgarg
 * Date: 1/19/16
 * Time: 3:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class CorrectOtherTax extends BaseRTBJob {

    private static final SpcfLogger logger = PayrollServices.getLogger(CorrectOtherTax.class);
    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);
    private static final String PSID = "PSID";
    private static final int RECORD_PROCESSING_THRESHOLD = 100;
    private SourceSystemCode mSourceSystemCode;
    private int successfulCount;

    {
        successfulCount = 0;
    }

    public CorrectOtherTax(){

    }

    public CorrectOtherTax(byte[] fileBinary) throws Exception {
        super(fileBinary);
        mSourceSystemCode = SourceSystemCode.QBDT;
    }

    @Override
    public JobResult process() throws RTBJobException {

        JobResult jobResult = new JobResult();
        String sourceCompanyId = null;
        boolean doneProcessing = false;
        HashMap<String,ArrayList<String>> sourceIdMap = new HashMap<String,ArrayList<String>>();

        ArrayList<String> nonNullSourceId = new ArrayList<String>();
        ArrayList<String> nullSourceId = new ArrayList<String>();

        logger.info("Initialized process to identify and delete null records for the PSIDs in the list ");


        for (int i = 0; i < recordSize; i++) {
            try {
                Application.beginUnitOfWork();

                sourceCompanyId = ((List) excelKeyValueList.get(PSID)).get(i).toString().trim();
                logger.info("Checking for PSID : " + sourceCompanyId + "  ........");
                if (null != sourceCompanyId) {
                    Company company = Company.findCompany(sourceCompanyId, mSourceSystemCode);
                    DomainEntitySet<EmployeeTax> empTax = EmployeeTax.findNullOtherTax(company);
                    if (null != empTax) {
                        doneProcessing =      doProcessing(empTax, sourceCompanyId, jobResult);
                        if(doneProcessing)    {
                           nullSourceId.add(sourceCompanyId);
                        }else if(!doneProcessing){
                            nonNullSourceId.add(sourceCompanyId);
                        }
                        if(doneProcessing){
                        getOtherTaxDetail(company, jobResult);
                        }
                        successfulCount++;
                    }


                }
                Application.commitUnitOfWork();
            } catch (Throwable pThrowable) {
                PayrollServices.rollbackUnitOfWork();
                jobResult.addErrorMessage("Error Deleting Null Other tax records failed for PSID " + sourceCompanyId);
                logger.error("Deleting null records  is unsuccessful for PSID=" + sourceCompanyId);
                pThrowable.printStackTrace();
            }
        }
             addInfoToJobResult(jobResult,nonNullSourceId,nullSourceId);
        return jobResult;  //To change body of implemented methods use File | Settings | File Templates.
    }



    /**
     * @param pEmpTax
     * @param psourceCompanyId
     * @param jobResult
     * @throws Exception
     * @See Method to process deletion for records having null company law for other tax in EmployeeTax and Tax_table_misc.
     */
    public boolean doProcessing(DomainEntitySet<EmployeeTax> pEmpTax, String psourceCompanyId, JobResult jobResult) throws Exception {
        boolean isDeleted = false;

        if (pEmpTax.size() <= 0) {
            jobResult.addInfoMessage("There are 0 NULL OTHER TAX records for the PSID :" + psourceCompanyId);
        } else {
            jobResult.addInfoMessage("There are " + pEmpTax.size() + " NULL OTHER TAX records for the PSID :" + psourceCompanyId);
            for (EmployeeTax employeeTax : pEmpTax) {
                isDeleted = deleteMiscRecord(employeeTax, psourceCompanyId);

                if (isDeleted) {
                    isDeleted = deleteRecord(employeeTax, psourceCompanyId);
                    if (isDeleted) {
                        logger.info("Deletion of employee tax record and tax table misc record done successfully for the PSID :" + psourceCompanyId);
                    }
                }
            }
        }
       return isDeleted;
    }

    /**
     * @param pEmployeeTax
     * @param pSourceCompanyId
     * @return
     * @See Method to delete corresponding records from taxtablemiscdata table.
     */
    private boolean deleteMiscRecord(EmployeeTax pEmployeeTax, String pSourceCompanyId) {
        logger.info("Deleting Tax table misc data records for PSID :" + pSourceCompanyId + "Now. ");
        boolean isDeleted = false;
        isDeleted = TaxTableMiscData.deleteForNullRecords(pEmployeeTax);
        if (isDeleted) {
            logger.info("Deleted Tax Table misc  records for PSID :" + pSourceCompanyId);
        }
        return isDeleted;
    }

    /**
     * @param empTax
     * @param sourceCompanyId
     * @return
     * @See Method to delete record having other tax type and null company law.
     */
    private boolean deleteRecord(EmployeeTax empTax, String sourceCompanyId) {
        logger.info("Deleting Employee tax record for PSID :" + sourceCompanyId + "Now. ");
        boolean isDeleted = false;
        SpcfUniqueId empTaxId = empTax.getId();
        isDeleted = EmployeeTax.deleteEmpTaxRecord(empTaxId);
        if (isDeleted) {
            logger.info("Deleted null Employee Tax records for PSID :" + sourceCompanyId);

        }

        return isDeleted;
    }

    /**
     * @param pCompany
     * @param pJobResult
     * @See Method to get list of laws under 'Other' Tax for the company.
     */
    public void getOtherTaxDetail(Company pCompany, JobResult pJobResult) {

        HqlBuilder hql = new HqlBuilder(true, "select distinct( cl.SourceId), cl.SourceDescription, et.TaxType from com.intuit.sbd.payroll.psp.domain.EmployeeTax et join  et.CompanyLaw cl join " +
                " cl.CompanyAgency ca where ca.Company = :company and et.TaxType = 'Other'");

        hql.setParameter("company", pCompany);

        List<Object[]> otherTaxList = hql.list();

        pJobResult.addInfoMessage("For company : Name :" + pCompany.getLegalName() + " : PSID : " + pCompany.getSourceCompanyId() +"  Following are the valid Other tax laws and source id:");

        for (Object[] data : otherTaxList) {
            pJobResult.addInfoMessage("Source id : "+ data[0] + " -->  Company Law : " + data[1]);
        }


    }

    @Override
    public JobResult validate() throws RTBJobException {
        JobResult jobResult = new JobResult();

        if (!excelKeyValueList.containsKey(PSID)) {
            logger.error("NULL OTHER TAX  validation failure - PSID missing");
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

    private void addInfoToJobResult(JobResult jobResult, ArrayList<String> pNonNullSourceId, ArrayList<String> pNullSourceId) {

        jobResult.addInfoMessage("Finished processing.");

        jobResult.addInfoMessage("===========================REPORT============================");

        jobResult.addInfoMessage("Total number of companies to be checked " + recordSize);

        jobResult.addInfoMessage("Number of companies successfully checked " + successfulCount);

        jobResult.addInfoMessage("Number of companies failed to check " + (recordSize - successfulCount));

        jobResult.setSuccess(true);

        jobResult.addInfoMessage("=============================================================");


        jobResult.addInfoMessage("Companies which have been successfully corrected for 9000 error due to NULL company law for OTHER TAX are :"+pNullSourceId.size());

        for(String sourceId : pNullSourceId)   {
            jobResult.addInfoMessage("PSID :"+ sourceId);
        }
        jobResult.addInfoMessage("=============================================================");
        jobResult.addInfoMessage("Companies which do not have NULL company laws for OTHER TAX and 9000 error is may be due to other reasons :"+pNonNullSourceId.size());

        for(String sourceId : pNonNullSourceId)   {
            jobResult.addInfoMessage("PSID :"+ sourceId);
        }
        jobResult.addInfoMessage("=============================================================");
        logger.info("Incorrect OtherTax Correction job completed.");



    }
}
