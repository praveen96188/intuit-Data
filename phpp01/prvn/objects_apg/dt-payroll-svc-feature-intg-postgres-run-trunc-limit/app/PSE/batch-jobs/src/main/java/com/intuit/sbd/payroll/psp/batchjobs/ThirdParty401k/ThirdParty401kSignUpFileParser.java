package com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k;

import au.com.bytecode.opencsv.CSVReader;
import com.intuit.idps.domain.item.Key;
import com.intuit.idps.service.IdpsException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.ThirdParty401kServiceInfoDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.dtos.ThirdParty401kSignUpDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileReader;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThirdParty401kSignUpFileParser {

    private ArrayList<ThirdParty401kSignUpDTO> mSignupArrayList = null;
    private ArrayList<String> mErrorArrayList = null;
    private static final SpcfLogger logger = Application.getLogger(ThirdParty401kSignUpFileParser.class);
    private static final String NEW_LINE = "\n";

    public void archiveTP401kSignUpBatch() {
        try {
            PayrollServices.beginUnitOfWork();

            String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");

            DomainEntitySet<ThirdParty401kSignUpBatch> batchList =
                    BatchUtils.getThirdParty401kSignUpBatchByStatus(ThirdParty401kBatchStatusCode.Finalized);
            if (batchList.isEmpty()) {
                logger.warn("There are no 401k signup batches in a Finalized state to be archived.");
            } else {
                for (ThirdParty401kSignUpBatch batch : batchList) {
                    BatchUtils.moveFile(batch.getFileName(), archiveDir);
                    
                    batch.setDownloadStatusCd(ThirdParty401kBatchStatusCode.Archived);
                    batch.setStatusEffectiveDate(PSPDate.getPSPTime());
                    Application.save(batch);
                }
            }
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    public void processSignupBatchs() {
        try {
            PayrollServices.beginUnitOfWork();

            DomainEntitySet<ThirdParty401kSignUpBatch> batchList =
                    BatchUtils.getThirdParty401kSignUpBatchByStatus(ThirdParty401kBatchStatusCode.Pending);
            if (batchList.isEmpty()) {
                logger.warn("There are no 401k signup batches in a Pending state to be processed.");
            } else {
                for (ThirdParty401kSignUpBatch batch : batchList) {
                    processSignupBatch(batch);
                }
            }
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    public void processSignupBatch(ThirdParty401kSignUpBatch pBatch) {
        mErrorArrayList = new ArrayList<String>();
        File file = new File(pBatch.getFileName());

        if (!file.exists()) {
            throw new RuntimeException("Unable to process 401k signup file. File does not exist: " + file.toString());
        }

        try {
            StopWatch timer = StopWatch.startTimer();
            logger.info("Beginning Process 401k Signup batch job.");

            loadSignupData(file);

            processSignupData();

            pBatch.setDownloadStatusCd(ThirdParty401kBatchStatusCode.Finalized);
            pBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
            Application.save(pBatch);

            sendMail(file.getName());

            logger.info("Completed Process 401k Signup batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void loadSignupData(File pFile) throws IOException {
        CSVReader reader = null;
        try {
            int rowCount = 1;
            ThirdParty401kSignUpDTO signupDTO;
            mSignupArrayList = new ArrayList<ThirdParty401kSignUpDTO>();
            if(StreamUtil.isFileIDPSEncrypted(pFile))
            {
                try {

                    Key key = IDPSFileStreamManager.newKeyHandleLatest();
                    reader = new CSVReader(new IDPSFileReader( pFile, key));
                }catch(IdpsException e)
                {
                    throw new RuntimeException("Can not proceed. IDPS error of Thirdparty401ksignup Error file", e);
                }
            }
            else{
                reader = new CSVReader(new FileReader(pFile));
            }

            //reader = new CSVReader(new FileReader(pFile));
            List<String[]> signupList = reader.readAll();

            for (String[] signupData : signupList) {
                if (signupData.length == 6) {
                    if (validate(signupData, rowCount)) {
                        signupDTO = new ThirdParty401kSignUpDTO();
                        signupDTO.setRecordType(ThirdParty401kSignUpDTO.RecordType.valueOf(signupData[0].trim()));
                        signupDTO.setFEIN(signupData[1].trim());
                        signupDTO.setCustodialAccountId(signupData[2].trim());
                        signupDTO.setEffectiveDate(signupData[3].trim());
                        signupDTO.setLegalName(signupData[4].trim());
                        signupDTO.setHasSafeHarbor(Boolean.valueOf(signupData[5].toLowerCase().trim()));
                        mSignupArrayList.add(signupDTO);
                    }
                } else {
                    String error = "Error with row " + rowCount + ": Unable to process record, incorrect number of fields found.";
                    logger.warn(error);
                    mErrorArrayList.add(error);
                }
                rowCount++;
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void processSignupData() {
        for (ThirdParty401kSignUpDTO tp401kSignUpDTO : mSignupArrayList) {
            DomainEntitySet<Company> companies = Company.findActiveCompanies(SourceSystemCode.QBDT, tp401kSignUpDTO.getFEIN());
            if (companies == null || companies.isEmpty()) {
                ThirdParty401kSignUpQueue tp401kSignupQueue =
                        ThirdParty401kSignUpQueue.findThirdParty401kSignUpQueue(tp401kSignUpDTO.getFEIN());

                switch (tp401kSignUpDTO.getRecordType()) {
                    case ADD:
                        if (tp401kSignupQueue != null) {
                            String error = "Error with company EIN " + tp401kSignUpDTO.getFEIN() +
                                    ": We received an ADD record but a sign-up record already exists in DB for company with " + tp401kSignUpDTO.getFEIN() + ".";
                            logger.warn(error);
                            mErrorArrayList.add(error);
                            continue;
                        }

                        tp401kSignupQueue = new ThirdParty401kSignUpQueue();
                        tp401kSignupQueue.setFedTaxId(tp401kSignUpDTO.getFEIN());
                        tp401kSignupQueue.setCustodialId(tp401kSignUpDTO.getCustodialAccountId());
                        tp401kSignupQueue.setLegalName(tp401kSignUpDTO.getLegalName());
                        tp401kSignupQueue.setEffectiveDate(tp401kSignUpDTO.getEffectiveDate());
                        tp401kSignupQueue.setHasSafeHarbor(tp401kSignUpDTO.hasSafeHarbor());
                        tp401kSignupQueue.setStatus(ThirdParty401kSignUpQueueStatusCode.Pending);

                        break;
                    case UPDATE:
                        if (tp401kSignupQueue == null) {
                            String error = "Error with company EIN " + tp401kSignUpDTO.getFEIN() +
                                    ": We received an UPDATE record but the company does not exist. Please update the record type to ADD and resubmit.";
                            logger.warn(error);
                            mErrorArrayList.add(error);
                            continue;
                        }
                        
                        tp401kSignupQueue.setFedTaxId(tp401kSignUpDTO.getFEIN());
                        tp401kSignupQueue.setCustodialId(tp401kSignUpDTO.getCustodialAccountId());
                        tp401kSignupQueue.setLegalName(tp401kSignUpDTO.getLegalName());
                        tp401kSignupQueue.setEffectiveDate(tp401kSignUpDTO.getEffectiveDate());
                        tp401kSignupQueue.setHasSafeHarbor(tp401kSignUpDTO.hasSafeHarbor());

                        break;
                    case CANCEL:
                        if (tp401kSignupQueue == null) {
                            String error = "Error with company EIN " + tp401kSignUpDTO.getFEIN() + ": We received a CANCEL record but the company does not exist.";
                            logger.warn(error);
                            mErrorArrayList.add(error);
                            continue;
                        }

                        tp401kSignupQueue.setStatus(ThirdParty401kSignUpQueueStatusCode.Cancelled);

                        break;
                }

                Application.save(tp401kSignupQueue);
            } else {
                ThirdParty401kServiceInfoDTO tp401kServiceInfoDTO;

                Company company = null;
                ThirdParty401kCompanyServiceInfo tp401kCompanyServiceInfo = null;
                switch (tp401kSignUpDTO.getRecordType()) {
                    case ADD:
                        if (companies.size() > 1) {
                            logger.error(String.format("Unable to process the TOK add request for EIN %s because more then one company with this EIN is active.", tp401kSignUpDTO.getFEIN()));
                            continue;
                        }

                        company = companies.get(0);
                        tp401kCompanyServiceInfo = (ThirdParty401kCompanyServiceInfo)
                                CompanyService.findCompanyService(company, ServiceCode.ThirdParty401k);

                        if (tp401kCompanyServiceInfo != null) {
                            if (isCompanyServiceActive(tp401kCompanyServiceInfo)) {
                                String error = "Error with company EIN " + tp401kSignUpDTO.getFEIN() +
                                        ": We received an ADD record but the company already exists. Please update the record type to UPDATE and resubmit.";
                                logger.warn(error);
                                mErrorArrayList.add(error);
                                continue;
                            } else {
                                tp401kServiceInfoDTO = new ThirdParty401kServiceInfoDTO();
                                tp401kServiceInfoDTO.setCustodialId(tp401kSignUpDTO.getCustodialAccountId());
                                tp401kServiceInfoDTO.setServiceStartDate(tp401kSignUpDTO.getEffectiveDate());
                                tp401kServiceInfoDTO.setHasSafeHarbor(tp401kSignUpDTO.hasSafeHarbor());

                                ProcessResult reactivatePR = PayrollServices.companyManager.reactivateService(
                                    company.getSourceSystemCd(), company.getSourceCompanyId(), tp401kServiceInfoDTO.getServiceCode());
                                if (!reactivatePR.isSuccess()) {
                                    logger.warn("FEIN: " + tp401kSignUpDTO.getFEIN() + ": " + reactivatePR.getMessages().get(0).getMessage());
                                    mErrorArrayList.add("Error with company EIN " + tp401kSignUpDTO.getFEIN() + ": Unable to reactivate 401k service");
                                    continue;
                                }

                                ProcessResult updatePR = PayrollServices.companyManager.updateService(company.getSourceSystemCd(), company.getSourceCompanyId(), tp401kServiceInfoDTO);
                                if (!updatePR.isSuccess()) {
                                    logger.warn("FEIN: " + tp401kSignUpDTO.getFEIN() + ": " + updatePR.getMessages().get(0).getMessage());
                                    mErrorArrayList.add("Error with company EIN " + tp401kSignUpDTO.getFEIN() + ": Unable to update 401k service");
                                    continue;
                                }
                            }
                        } else {
                            tp401kServiceInfoDTO = new ThirdParty401kServiceInfoDTO();
                            tp401kServiceInfoDTO.setCustodialId(tp401kSignUpDTO.getCustodialAccountId());
                            tp401kServiceInfoDTO.setServiceStartDate(tp401kSignUpDTO.getEffectiveDate());
                            tp401kServiceInfoDTO.setHasSafeHarbor(tp401kSignUpDTO.hasSafeHarbor());

                            ProcessResult addPR = PayrollServices.companyManager.addService(company.getSourceSystemCd(), company.getSourceCompanyId(), tp401kServiceInfoDTO);
                            if (!addPR.isSuccess()) {
                                logger.warn("FEIN: " + tp401kSignUpDTO.getFEIN() + ": " + addPR.getMessages().get(0).getMessage());
                                mErrorArrayList.add("Error with company EIN " + tp401kSignUpDTO.getFEIN() + ": Unable to add 401k service");
                                continue;
                            }
                        }

                        break;
                    case UPDATE:
                        if (companies.size() > 1) {
                            CompanyService companyService = null;
                            try {
                                companyService = CompanyService.findActiveCompanyServiceByFedTaxId(
                                        SourceSystemCode.QBDT, ServiceCode.ThirdParty401k, tp401kSignUpDTO.getFEIN());
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                                continue;
                            }
                            company = companyService.getCompany();
                        } else {
                            company = companies.get(0);
                        }

                        tp401kCompanyServiceInfo = (ThirdParty401kCompanyServiceInfo)
                                CompanyService.findCompanyService(company, ServiceCode.ThirdParty401k);

                        if (tp401kCompanyServiceInfo == null || isCompanyServiceNotActive(tp401kCompanyServiceInfo))  {
                            logger.warn("Error with company EIN " + tp401kSignUpDTO.getFEIN() +
                                    ": We received an UPDATE record but the company does not exist. Please update the record type to ADD and resubmit.");
                            continue;
                        }

                        tp401kServiceInfoDTO = new ThirdParty401kServiceInfoDTO();
                        tp401kServiceInfoDTO.setCustodialId(tp401kSignUpDTO.getCustodialAccountId());
                        tp401kServiceInfoDTO.setServiceStartDate(tp401kSignUpDTO.getEffectiveDate());
                        tp401kServiceInfoDTO.setHasSafeHarbor(tp401kSignUpDTO.hasSafeHarbor());

                        ProcessResult updatePR = PayrollServices.companyManager.updateService(company.getSourceSystemCd(), company.getSourceCompanyId(), tp401kServiceInfoDTO);
                        if (!updatePR.isSuccess()) {
                            logger.warn("FEIN: " + tp401kSignUpDTO.getFEIN() + ": " + updatePR.getMessages().get(0).getMessage());
                            mErrorArrayList.add("Error with company EIN " + tp401kSignUpDTO.getFEIN() + ": Unable to update 401k service");
                            continue;
                        }

                        break;
                    case CANCEL:
                        if (companies.size() > 1) {
                            CompanyService companyService = null;
                            try {
                                companyService = CompanyService.findActiveCompanyServiceByFedTaxId(
                                        SourceSystemCode.QBDT, ServiceCode.ThirdParty401k, tp401kSignUpDTO.getFEIN());
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                                continue;
                            }
                            company = companyService.getCompany();
                        } else {
                            company = companies.get(0);
                        }

                        tp401kCompanyServiceInfo = (ThirdParty401kCompanyServiceInfo)
                                CompanyService.findCompanyService(company, ServiceCode.ThirdParty401k);

                        if (tp401kCompanyServiceInfo == null || isCompanyServiceNotActive(tp401kCompanyServiceInfo))  {
                            String error = "Error with company EIN " + tp401kSignUpDTO.getFEIN() + ": We received a CANCEL record but the company does not exist.";
                            logger.warn(error);
                            mErrorArrayList.add(error);
                            continue;
                        }

                        ProcessResult cancelPR = PayrollServices.companyManager.deactivateService(
                            company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.ThirdParty401k);
                        if (!cancelPR.isSuccess()) {
                            logger.warn("FEIN: " + tp401kSignUpDTO.getFEIN() + ": " + cancelPR.getMessages().get(0).getMessage());
                            mErrorArrayList.add("Error with company EIN " + tp401kSignUpDTO.getFEIN() + ": Unable to cancel 401k service");
                            continue;
                        }
                        break;
                }
            }
        }
    }

    private boolean isCompanyServiceActive(ThirdParty401kCompanyServiceInfo pTP401kCompanyServiceInfo) {
        return ServiceSubStatusCode.ActiveCurrent.equals(pTP401kCompanyServiceInfo.getStatusCd()) ||
                ServiceSubStatusCode.PendingFirstPayroll.equals(pTP401kCompanyServiceInfo.getStatusCd());
    }

    private boolean isCompanyServiceNotActive(ThirdParty401kCompanyServiceInfo pTP401kCompanyServiceInfo) {
        return ServiceSubStatusCode.Cancelled.equals(pTP401kCompanyServiceInfo.getStatusCd()) ||
                ServiceSubStatusCode.Terminated.equals(pTP401kCompanyServiceInfo.getStatusCd());
    }

    private boolean validate(String [] pFieldData, int pRowCount) {
        boolean isValid = true;

        if (!validateValue(pFieldData[0], false, "ADD|UPDATE|CANCEL")) {
            String error = "Error with row " + pRowCount + ": Unable to process record, field 'Record Type' contains invalid data of '"+ pFieldData[0] + "'.";
            logger.warn(error);
            mErrorArrayList.add(error);
            isValid = false;
        }

        if (!validateValue(pFieldData[1], false, "\\p{Digit}{9,9}")) {
            String error = "Error with row " + pRowCount + ": Unable to process record, field 'Company EIN' contains invalid data of '"+ pFieldData[1] + "'.";
            logger.warn(error);
            mErrorArrayList.add(error);
            isValid = false;
        }

        if (!validateValue(pFieldData[2], false, "^(\\P{M}\\p{M}*){0,80}$")) {
            String error = "Error with row " + pRowCount + ": Unable to process record, field 'Custodial ID' contains invalid data of '"+ pFieldData[2] + "'.";
            logger.warn(error);
            mErrorArrayList.add(error);
            isValid = false;
        }

        if (!validateValue(pFieldData[3], false, "(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\\d\\d")) {
            String error = "Error with row " + pRowCount + ": Unable to process record, field 'Effective Date' contains invalid data of '"+ pFieldData[3] + "'.";
            logger.warn(error);
            mErrorArrayList.add(error);
            isValid = false;
        }

        if (!validateValue(pFieldData[4], false, "^(\\P{M}\\p{M}*){0,100}$")) {
            String error = "Error with row " + pRowCount + ": Unable to process record, field 'Legal Name' contains invalid data of '"+ pFieldData[4] + "'.";
            logger.warn(error);
            mErrorArrayList.add(error);
            isValid = false;
        }

        if (!validateValue(pFieldData[5], false, "true|false|TRUE|FALSE")) {
            String error = "Error with row " + pRowCount + ": Unable to process record, field 'Safe Harbor' contains invalid data of '"+ pFieldData[5] + "'.";
            logger.warn(error);
            mErrorArrayList.add(error);
            isValid = false;
        }

        return isValid;
    }

    private boolean validateValue(String pValue, boolean pNullable, String pPattern) {
        Pattern pattern = Pattern.compile(pPattern);

        if ((!pNullable) && (pValue == null)) {
            return false;
        }

        if (pValue == null) {
            return true;
        }

        Matcher matcher = pattern.matcher(pValue.trim());
        return matcher.matches();
    }

    private void sendMail(String pFilename) {
        if (mErrorArrayList != null && !mErrorArrayList.isEmpty()) {
            StringBuilder errors = new StringBuilder();
            for (String error : mErrorArrayList) {
                errors.append(error)
                        .append(NEW_LINE);
            }

            String subject = "Action Required: Failure to process 401k signup record(s)";

            StringBuilder bodyMsg = new StringBuilder();
            bodyMsg.append("This message is in reference to the following file:")
                    .append(NEW_LINE)
                    .append(NEW_LINE)
                    .append(pFilename)
                    .append(NEW_LINE)
                    .append(NEW_LINE)
                    .append("Some data failed to be processed for the following reason(s):")
                    .append(NEW_LINE)
                    .append(NEW_LINE)
                    .append(errors.toString())
                    .append(NEW_LINE)
                    .append("Please correct the issues and resend. If you have any questions or would like to talk to someone in our engineering department please email rnpspalertprod@intuit.com.")
                    .append(NEW_LINE)
                    .append(NEW_LINE)
                    .append("Sincerely,")
                    .append(NEW_LINE)
                    .append("Intuit Payroll Services");

            MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"),
                    BatchUtils.getConfigString("psp_batch_tp401k_notify_to_address"),
                    BatchUtils.getConfigString("psp_batch_tp401k_notify_from_address"),
                    subject,
                    bodyMsg.toString());
        }
    }
}
