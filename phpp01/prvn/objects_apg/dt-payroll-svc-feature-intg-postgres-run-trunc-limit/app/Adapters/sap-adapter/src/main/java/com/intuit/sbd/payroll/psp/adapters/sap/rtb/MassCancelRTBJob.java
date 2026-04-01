package com.intuit.sbd.payroll.psp.adapters.sap.rtb;


import com.intuit.ems.payroll.psp.gateways.ebs.EBSGatewayFactory;
import com.intuit.ems.payroll.psp.gateways.ebs.IEBSGateway;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.AdapterExceptionFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TaxServiceInfoDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.common.utils.batchjobs3util.IDPSS3FileUtility;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.*;
import java.io.File;
import java.text.SimpleDateFormat;


/**
 * Mass Cancel RTB Job does the following things
 * Step 1: Disable entitlement
 * Step 2: Cancel Company Services (i.e TAX, DD services - for now we are cancelling TAX and DD service only )
 */

public class MassCancelRTBJob extends BaseRTBJob {


    private static final SpcfLogger logger = PayrollServices.getLogger(MassCancelRTBJob.class);

    private static final AdapterExceptionFactory aeFactory = new AdapterExceptionFactory(logger);

    private static final String LIC = "LIC";

    private static final String PSID = "PSID";

    private static final int RECORD_PROCESSING_THRESHOLD = 500;


    private List<ServiceCode> mServiceCodes;

    private SourceSystemCode mSourceSystemCode;

    private DomainEntitySet<ServiceSubStatus> mServiceSubStatuses;


    private IEBSGateway ebsGateway = null;

    private int successfulCount = 0;

    private Boolean failedCompanyFlag = false;

    public MassCancelRTBJob(byte[] fileBinary) throws Exception {

        super(fileBinary);

        mSourceSystemCode = SourceSystemCode.QBDT;

        mServiceCodes = new ArrayList<ServiceCode>();

        mServiceCodes.add(ServiceCode.Tax);

        mServiceCodes.add(ServiceCode.DirectDeposit);

        mServiceSubStatuses = new DomainEntitySet<ServiceSubStatus>();

        mServiceSubStatuses.add(getServiceSubStatusFromStatusCd(ServiceSubStatusCode.Cancelled));

    }


    public JobResult process() throws RTBJobException {

        JobResult jobResult = new JobResult();

        String licenseNumber = null;

        String sourceCompanyId = null;

        String isSingleFile     = "";

        StopWatch stopWatch = StopWatch.startTimer();

        logger.info("MassCancelTimer:started job " + stopWatch.getElapsedMillis());

        logger.info("Mass cancelling for " + recordSize + " records. RecordSize=" + recordSize);

        String outDir = BatchUtils.getConfigString("psp_rtb_mcn_decr_dir");

        String currentTimeStamp = new SimpleDateFormat("yyyyMMddHHmm").format(new java.util.Date());

        String fileName = outDir + File.separator + "MassCancel_"+currentTimeStamp+".txt";

        Map<String,String> mapRecord = new HashMap<String, String>();

        for (int i = 0; i < recordSize; i++) {

            try {

                PayrollServices.beginUnitOfWork();

                licenseNumber = ((List) excelKeyValueList.get(LIC)).get(i).toString().trim();

                sourceCompanyId = ((List) excelKeyValueList.get(PSID)).get(i).toString().trim();

                //Step 1: Cancel Company Services (i.e TAX, DD services - for now we are cancelling TAX and DD service only )
                try{
                    PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompanyFromPSID(sourceCompanyId);
                    cancelCompanyService(jobResult, sourceCompanyId.trim());
                }finally {
                    PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
                }


                //Step 2: Disable entitlement
                disableEntitlement(licenseNumber, sourceCompanyId);

                PayrollServices.commitUnitOfWork();

                successfulCount++;

                logger.info("Successfully cancelled source company with PSID=" + sourceCompanyId);

                jobResult.addInfoMessage("Disabling of Entitlement and cancelling of company services is successful for licenseNumber " + licenseNumber + " PSID " + sourceCompanyId);

            } catch (Throwable pThrowable) {
                PayrollServices.rollbackUnitOfWork();
                jobResult.addErrorMessage("Disabling of Entitlement and cancelling of company services is failed for licenseNumber=" + licenseNumber + " PSID=" + sourceCompanyId + " and can be process manually");
                logger.error("Disabling of Entitlement and cancelling of company services is failed for licenseNumber=" + licenseNumber + " PSID=" + sourceCompanyId + " and can be process manually");

                try {
                    mapRecord.put(sourceCompanyId,licenseNumber);
                    failedCompanyFlag = true;
                    if(!isSingleFile.equals(fileName)){
                        logger.info("File created for auto mass cancellation process : " +fileName);
                    }
                    isSingleFile = fileName;
                } catch (Exception ex) {
                    logger.error("Failed during writing failed company to a file for licenseNumber=" + licenseNumber + " PSID=" + sourceCompanyId);
                }
            }

        }// for each record
        logger.info("MasCancelTimer:done processing .elapsed time: " + stopWatch.getElapsedMillis());

        stopWatch.stop();

        jobResult.addInfoMessage("Finished processing.");

        jobResult.addInfoMessage("===========================REPORT============================");

        jobResult.addInfoMessage("Total number of companies to be cancelled " + recordSize);

        jobResult.addInfoMessage("Number of companies successfully cancelled " + successfulCount);

        jobResult.addInfoMessage("Number of companies failed to cancelled " + (recordSize - successfulCount));

        jobResult.setSuccess(true);

        jobResult.addInfoMessage("=============================================================");

        logger.info("Mass cancelling is completed. SuccessfulCancelled=" + successfulCount + " TotalRecords=" + recordSize);

        try {

            prepareEncryptedFileAndSendEmail(fileName,mapRecord);

        }catch (Exception exception){

            logger.error("Exception during process of failed flag : "+exception);
        }
        return jobResult;

    }

    /**
     *
     * @param fileName
     */
    private void prepareEncryptedFileAndSendEmail(String fileName, Map<String,String> mapRecord) throws Exception {

        if (failedCompanyFlag) {

            new ProcessMassCancellation().writeFailedCompanyToFile(fileName,mapRecord);

            String outDirDecr = BatchUtils.getConfigString("psp_rtb_mcn_send_dir");

            String filePathDecry = outDirDecr + File.separator + fileName.substring(fileName.indexOf("MassCancel_"));

            IDPSS3FileUtility.encryptFile(fileName,filePathDecry);

            if (new File(fileName).exists())
                new File(fileName).delete();

            new ProcessMassCancellation().sendEmail("psp_rtb_mcn_notify_tolist","psp_rtb_mcn_notify_fromlist", "psp_rtb_mcn_notify_subject", fileName);

            logger.info("Email send successfully to notify Mass cancellation failure");
        }
    }


    public JobResult validate() throws RTBJobException {

        JobResult jobResult = new JobResult();


        if ((!excelKeyValueList.containsKey(LIC)) || (!excelKeyValueList.containsKey(PSID))) {

            logger.error("Mass cancelling validation failure - license number or PSID missing");

            jobResult.addErrorMessage("License number or PSID missing !!");

            jobResult.setSuccess(false);

            return jobResult;

        }


        if (recordSize > RECORD_PROCESSING_THRESHOLD) {

            logger.error("Record size is more than excepted.");

            jobResult.addErrorMessage("Record size is more than excepted. Max Supported number of records are " + RECORD_PROCESSING_THRESHOLD);

            jobResult.setSuccess(false);

            return jobResult;

        }

        ebsGateway = EBSGatewayFactory.createInstance();

        if (ebsGateway == null) {
            jobResult.addErrorMessage("Not able to initiate EBSGateway.");
            jobResult.setSuccess(false);
            return jobResult;
        }

        jobResult.setSuccess(true);

        return jobResult;

    }


    /**
     * cancelCompanyService is for Cancelling the services of that company
     * Step 1 : Remove on Holds for company if any
     * Step 2: Set Last Quarter To File with latest year & quarter (if required) and cancel the company services
     *
     * @param pJobResult
     * @param pCompanyId
     * @throws Throwable
     */

    private void cancelCompanyService(JobResult pJobResult, String pCompanyId) throws Throwable {

        ProcessResult pr = null;

        //find company

        Company company = Company.findCompany(pCompanyId, mSourceSystemCode);

        if (company == null) {

            aeFactory.throwGenericException("Company not found for PSID='" + pCompanyId + "'.", pr);

        }


        //Step 1 : Remove on Holds for company if any

        Collection<OnHoldReason> onHoldReasons = company.getCurrentOnHoldReasons();

        if (onHoldReasons.size() > 0) {

            pr = PayrollServices.companyManager.updateSubStatuses(mSourceSystemCode, pCompanyId, mServiceCodes.get(0), null);

            if (!pr.isSuccess()) {

                aeFactory.throwGenericException("Remove hold reasons is failed for PSID='" + pCompanyId + "'.", pr);
            }
        }

        //Step 2: Set Last Quarter To File with latest year & quarter (if required) and deactivate the company services

        for (ServiceCode serviceCode : mServiceCodes) {// for each service code

            if (ServiceCode.Tax.equals(serviceCode)) {

                if (company == null) {

                    throw aeFactory.companyNotFoundException();

                }

                //Step 2.1: Set Last Quarter To File with previous Quarter

                CompanyService taxService = CompanyService.findCompanyService(company, ServiceCode.Tax);

                TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) taxService;

                TaxServiceInfoDTO taxServiceInfoDTO = (TaxServiceInfoDTO) PayrollServices.dtoFactory.create(taxCompanyServiceInfo);

                int previousQuarter = CalendarUtils.getPreviousQuarter(PSPDate.getPSPTime());

                int year = (previousQuarter == 4) ? (PSPDate.getPSPTime().getYear() - 1) : PSPDate.getPSPTime().getYear();

                taxServiceInfoDTO.setLastQuarterToFile(Integer.parseInt(Integer.toString(year) + Integer.toString(previousQuarter)));

                pr = PayrollServices.companyManager.updateService(mSourceSystemCode, pCompanyId, taxServiceInfoDTO);

                if (!pr.isSuccess()) {

                    aeFactory.throwGenericException("Cancelling of Company service was failed for PSID='" + pCompanyId + "' .", pr);

                }

            }

            //Step 2.2: Deactivate the company services

            pr = PayrollServices.companyManager.updateSubStatuses(mSourceSystemCode, pCompanyId, serviceCode, mServiceSubStatuses);

            if (!pr.isSuccess()) {
                //PSP-16065 Ignore Pending ACH Returns failure
                //Throw exception if exception is other than ACH Returns failure
                if (!pr.getErrorMessages().containsMessage("227")) {
                    aeFactory.throwGenericException("Cancelling of Company service failed for PSID='" + pCompanyId + "' .", pr);
                }

                //Mark Pending ACH returns as resolved
                DomainEntitySet<TransactionReturn> trnReturnCollection = PayrollServices.entityFinder.find(TransactionReturn.class, new Query<TransactionReturn>()
                        .Where(TransactionReturn.Company().equalTo(company)
                                .And(TransactionReturn.ReturnStatusCd().notEqualTo(TransactionReturnStatusCode.Resolved))));

                pJobResult.addInfoMessage("####Marking Pending ACH Returns as resolved for PSID:" + company.getSourceCompanyId() + " to unblock ACH Returns failure.####");

                for (TransactionReturn tr : trnReturnCollection) {
                    tr.updateTransactionReturnStatus(TransactionReturnStatusCode.Resolved);
                }

                pJobResult.addInfoMessage("####Finished marking Pending ACH Returns as resolved for PSID:" + company.getSourceCompanyId() + ". Please review the ledger for corrections.####");

                DomainEntitySet<TransactionReturn> trnReturnCollection1 = PayrollServices.entityFinder.find(TransactionReturn.class, new Query<TransactionReturn>()
                        .Where(TransactionReturn.Company().equalTo(company)
                                .And(TransactionReturn.ReturnStatusCd().notEqualTo(TransactionReturnStatusCode.Resolved))));

                if (trnReturnCollection1.size() == 0) {
                    //Retry Deactivating the company service and if still failure is there, throw it.
                    pr = PayrollServices.companyManager.updateSubStatuses(mSourceSystemCode, pCompanyId, serviceCode, mServiceSubStatuses);

                    if (!pr.isSuccess()) {
                        aeFactory.throwGenericException("Cancelling of Company service was failed for PSID='" + pCompanyId + "' .", pr);
                    }
                }
            }

        }

    }


    /**
     * disableEntitlement for disabling the Entitlement from ERS (remote system) and PSP (our local system)
     * Step 1: get the EntitlementInfo from ERS for given licenseNumber and pCompanyId (PSID)
     * Step 2: disable the Entitlement to ERS system if it is enabled
     * Step 3: disable the Entitlement (& deactivated the Entitlement Unit) to PSP (local) system
     *
     * @param licenseNumber
     * @param pCompanyId
     * @throws Throwable
     */

    private void disableEntitlement(String licenseNumber, String pCompanyId) throws Throwable {

        EntitlementUnit eu;

        Company company;

        EntitlementUnit entitlementUnit = getEntitlementUnit(licenseNumber, pCompanyId);

        if (entitlementUnit == null) {

            logger.error("Exception: No active Entitlement found for given licenseNumber=" + licenseNumber + " PSID=" + pCompanyId);

            throw new Exception("No active Entitlement found for given licenseNumber=" + licenseNumber + " PSID=" + pCompanyId);

        }

        eu = Application.findById(EntitlementUnit.class, entitlementUnit.getId());

        company = eu.getCompany();

        Entitlement entitlement = eu.getEntitlement();

        //Step 1: get the EntitlementInfo from ERS for given licenseNumber and pCompanyId (PSID)

        ebsGateway.disableEntitlement(entitlement.getLicenseNumber(),
                entitlement.getEntitlementOfferingCode());

        //Step 2: update company entitlement & entitlement unit status

        EntitlementUnitDTO entitlementUnitDTO = PayrollServices.dtoFactory.create(eu);

        entitlementUnitDTO.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);

        entitlementUnitDTO.setEntitlementState(EntitlementStateCode.Disabled);

        //Step 3: disable the Entitlement (& deactivated the Entitlement Unit) to PSP (local) system
        ProcessResult processResult = PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(company.getSourceSystemCd(), company.getSourceCompanyId(), entitlementUnitDTO);

        if (!processResult.isSuccess()) {
            aeFactory.throwGenericException("Entitlement disabled is failed for licenseNumber='" + licenseNumber + "' and PSID='" + pCompanyId + "'.", processResult);
        }

    }


    private ServiceSubStatus getServiceSubStatusFromStatusCd(ServiceSubStatusCode serviceSubStatusCd) throws Exception {
        ServiceSubStatus retSubStatus = null;

        DomainEntitySet<ServiceSubStatus> serviceSubStatusList =
                PayrollServices.entityFinder.findObjects(ServiceSubStatus.class);

        for (ServiceSubStatus subStatus : serviceSubStatusList) {
            if (serviceSubStatusCd.equals(subStatus.getServiceSubStatusCd())) {
                retSubStatus = subStatus;
                break;
            }
        }

        return retSubStatus;

    }


    private EntitlementUnit getEntitlementUnit(String licenseNumber, String sourceCompanyId) {

        logger.info("Finding License Number/PSID - " + licenseNumber + "/" + sourceCompanyId);

        // Find the entitlement units for this license number/company.

        DomainEntitySet<EntitlementUnit> entitlementUnits = Application.find(EntitlementUnit.class,
                EntitlementUnit.Entitlement().LicenseNumber().equalTo(licenseNumber)
                        .And(EntitlementUnit.Company().SourceSystemCd().equalTo(SourceSystemCode.QBDT))
                        .And(EntitlementUnit.Company().SourceCompanyId().equalTo(sourceCompanyId)));

        if (entitlementUnits.size() == 0) {
            logger.warn("Unable to find an Entitlement Unit for License Number/PSID - " + licenseNumber + "/" + sourceCompanyId);
            return null;
        } else {
            // Find the first one that is active.
            for (EntitlementUnit eu : entitlementUnits) {
                if (eu.isActivated()) {
                    return eu;
                }
            }
        }

        return null;
    }

}
