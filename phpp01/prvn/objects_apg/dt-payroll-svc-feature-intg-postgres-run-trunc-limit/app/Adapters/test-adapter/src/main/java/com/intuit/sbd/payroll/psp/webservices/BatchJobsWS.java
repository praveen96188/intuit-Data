package com.intuit.sbd.payroll.psp.webservices;

import com.intuit.ems.payroll.psp.gateways.ers.ERSGateway;
import com.intuit.ems.payroll.psp.gateways.ers.ERSGatewayFactory;
import com.intuit.ems.payroll.psp.gateways.ers.ERSMockGateway;
import com.intuit.ems.payroll.psp.gateways.tfs.TFSGateway;
import com.intuit.ems.payroll.psp.gateways.tfs.TFSGatewayFactory;
import com.intuit.ems.payroll.psp.gateways.tfs.TFSMockGateway;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.AssetChangeReasonType;
import com.intuit.iep.customerasset.intuitcustomerassetabo.v1.AssetStatusType;
import com.intuit.onlinepayroll.webservices.v1.ContractorPaymentCompanyModel;
import com.intuit.onlinepayroll.webservices.v1.PayrollCompanyModel;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.MockSimpleSftpFile;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.ReturnFileParser;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.ACHTransactions.ProcessMissedPayrolls;
import com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.ATFDataExtractProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract.CreateFilingsSpecificTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobController;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.GEMSUpload.DailyGemsUploadBatchProcess;
import com.intuit.sbd.payroll.psp.batchjobs.GEMSUpload.MonthlyGemsUploadBatchProcess;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.ThirdParty401kBatchProcess;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.ThirdParty401kSignUp;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.ThirdParty401kValidationProcess;
import com.intuit.sbd.payroll.psp.batchjobs.amo.AMOMessageProcessing;
import com.intuit.sbd.payroll.psp.batchjobs.eoqsuiadjustments.EoqSUITaxAdjustments;
import com.intuit.sbd.payroll.psp.batchjobs.fraudpayrolls.ProcessFraudulentPayrolls;
import com.intuit.sbd.payroll.psp.batchjobs.iop.SyncIOPData;
import com.intuit.sbd.payroll.psp.batchjobs.offload.CreateTransactionOffloadedEvents;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.processors.AMOMessageProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EntitlementProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.GemsGeneralLedgerProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.raf.RAFFileWriter;
import com.intuit.sbd.payroll.psp.batchjobs.salestax.SalesTaxExceptionProcess;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpAchReturnsFileDownload;
import com.intuit.sbd.payroll.psp.batchjobs.util.SftpTP401kFileUpload;
import com.intuit.sbd.payroll.psp.batchjobs.utils.ACHCompare;
import com.intuit.sbd.payroll.psp.batchjobs.utils.CompareResults;
import com.intuit.sbd.payroll.psp.batchjobs.utils.GemsCompare;
import com.intuit.sbd.payroll.psp.batchjobs.zeropayments.ProcessZeroPayments;
import com.intuit.sbd.payroll.psp.common.utils.FileUtils;
import com.intuit.sbd.payroll.psp.common.utils.SftpFactory;
import com.intuit.sbd.payroll.psp.common.utils.jsch.Transporter;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyBankAccount;
import com.intuit.sbd.payroll.psp.domain.DICRFileStatus;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.NACHAFileStatus;
import com.intuit.sbd.payroll.psp.domain.NACHAFileType;
import com.intuit.sbd.payroll.psp.domain.OffloadBatchStatus;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.gateways.amo.*;
import com.intuit.sbd.payroll.psp.gateways.email.factory.CompanyEventEmailManager;
import com.intuit.sbd.payroll.psp.gateways.email.factory.EventEmailTemplateFactory;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmailTemplate;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventEmail;
import com.intuit.sbd.payroll.psp.gateways.iop.IOPGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.iop.MockIOPGateway;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.sbd.payroll.psp.webservices.wsdto.*;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.text.SpcfDateFormat;
import com.intuit.spc.foundations.portability.text.SpcfFormatException;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.FlushMode;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Mar 5, 2008
 * Time: 8:57:57 AM
 * To change this template use File | Settings | File Templates.
 */
@WebService()
public class BatchJobsWS {
    private static final char ADDENDA_REC_TYPE = '7';
    private static final SpcfLogger logger = Application.getLogger(BatchJobsWS.class);

    @WebMethod
    public void rescheduleBatchJob(@WebParam(name = "jobtype") String pJobType,
                                   @WebParam(name = "jobtimer") String pJobTimer,
                                   @WebParam(name = "retrydelay") String pRetryDelay,
                                   @WebParam(name = "autoschedule") boolean pAutoSchedule,
                                   @WebParam(name = "maxretries") int pMaxRetries) throws Exception {
        if ((pJobType == null) || (pJobType.length() == 0)) {
            throw new RuntimeException("Job Name cannot be null or empty.");
        }

        if ((pJobTimer == null) || (pJobTimer.length() == 0)) {
            throw new RuntimeException("Job Timer cannot be null or empty.");
        }

        try {
            BatchJobType jobType = BatchJobType.valueOf(pJobType);

            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

            PayrollServices.beginUnitOfWork();

            BatchJobSetup bjSetup = PayrollServices.entityFinder.findById(BatchJobSetup.class, jobType);

            bjSetup.setDelayBetweenRetriesTimerExpression(pRetryDelay);
            bjSetup.setIsAutomaticallyScheduled(pAutoSchedule);
            bjSetup.setMaxRetries(pMaxRetries);
            bjSetup.setJobTimerExpression(pJobTimer);

            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            logger.error("Error rescheduling batch job.", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public List<String> generate401kFiles(@WebParam(name = "offloadDate") String offloadDate,
                                          @WebParam(name = "uploadFiles") boolean uploadFiles,
                                          @WebParam(name = "archiveFiles") boolean archiveFiles,
                                          @WebParam(name = "shouldNotReturnFiles") boolean shouldNotReturnFiles) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        // offload date validations
        if (offloadDate == null || offloadDate.length() == 0) {
            offloadDate = PSPDate.getPSPTime().format("yyyyMMdd");
        }
        if (offloadDate != null && offloadDate.length() != 8) {
            throw new RuntimeException(
                    "Invalid offload date format" + offloadDate + ".  Correct format: yyyyMMdd");
        }

        List<String> fileContents = new ArrayList<String>();

        try {
            ThirdParty401kBatchProcess tp401kBatchProcess = new ThirdParty401kBatchProcess();

            tp401kBatchProcess.createFiles();

            Application.beginUnitOfWork();
            ArrayList<Integer> batchIds = tp401kBatchProcess.getBatchIds();

            if (uploadFiles) {
                new SftpTP401kFileUpload().upload();
            }

            for (Integer batchId : batchIds) {
                ThirdParty401kBatch tp401kBatch = tp401kBatchProcess.getUploadBatch(batchId);
                if (!shouldNotReturnFiles) {
                    if (fileContents == null) {
                        fileContents = new ArrayList<String>();
                    }
                    fileContents.add(getFileContents(tp401kBatch));
                }
                if (archiveFiles) {
                    tp401kBatchProcess.archiveFiles();
                }

                tp401kBatch.setUploadStatusCd(ThirdParty401kBatchStatusCode.Archived);
                Application.save(tp401kBatch);
            }
            Application.commitUnitOfWork();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            Application.rollbackUnitOfWork();
        }

        return fileContents;
    }

    @WebMethod
    public List<TP401kSignupQueueWSDTO> query401kSignupQueue() throws Exception {
        List<TP401kSignupQueueWSDTO> returnList = new ArrayList<TP401kSignupQueueWSDTO>();

        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<ThirdParty401kSignUpQueue> tp401kSignUpQueue = Application.find(ThirdParty401kSignUpQueue.class);
            for (ThirdParty401kSignUpQueue tp401kSignUpItem : tp401kSignUpQueue) {
                TP401kSignupQueueWSDTO wsdto = new TP401kSignupQueueWSDTO();
                wsdto.FedTaxId = tp401kSignUpItem.getFedTaxId();
                wsdto.custodialId = tp401kSignUpItem.getCustodialId();
                wsdto.EffectiveDate = tp401kSignUpItem.getEffectiveDate().toString();
                wsdto.LegalName = tp401kSignUpItem.getLegalName();
                wsdto.HasSafeHarbor = tp401kSignUpItem.getHasSafeHarbor();
                wsdto.Status = tp401kSignUpItem.getStatus().toString();
                returnList.add(wsdto);
            }
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return returnList;
    }

    @WebMethod
    public List<EmailRecipientWSDTO> process401kValidation() throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            new ThirdParty401kValidationProcess().validate401kData();

            PayrollServices.beginUnitOfWork();
            DomainEntitySet<CompanyEventEmail> eventList = CompanyEventEmail.findEmailEventsByTemplateAndStatus(
                    EventEmailStatus.Pending, EventEmailTemplateTypeCode.Correct401kEmployeeInfo, EventEmailTemplateTypeCode.Correct401kEmployeeInfoAfterSend);

            Collection<CompanyEventEmailManager> companyEmailManagers =
                    EventEmailTemplateFactory.buildCompanyEmailManagers(eventList);

            List<EventEmailTemplate> masterTemplateList =
                    EventEmailTemplateFactory.buildMasterTemplateList(companyEmailManagers);

            List<EmailRecipientWSDTO> currEmailRecipients = new ArrayList<EmailRecipientWSDTO>();

            for (EventEmailTemplate currTemplate : masterTemplateList) {

                //Recipients
                List<List<IEventEmail>> recipients = currTemplate.getRecipientsToTransmit();
                for (List<IEventEmail> currList : recipients) {
                    for (IEventEmail currRecipient : currList) {
                        List<EmailPropertyWSDTO> currEmailProperties = new ArrayList<EmailPropertyWSDTO>();
                        EmailRecipientWSDTO currDTORecipient = new EmailRecipientWSDTO();
                        currDTORecipient.templateId = currTemplate.getTemplateId().name();
                        currDTORecipient.companyId = currRecipient.getCompanyId();
                        currRecipient.getProperties();
                        currDTORecipient.recipientEmail = currRecipient.getRecipientEmail();
                        currDTORecipient.recipientId = removeGUIDFromReceipientId(currRecipient.getRecipientId());
                        currDTORecipient.recipientName = currRecipient.getRecipientName();

                        //Properties
                        Properties recipientProperties = currRecipient.getProperties();
                        Enumeration enumPropertyNames = recipientProperties.propertyNames();
                        while (enumPropertyNames.hasMoreElements()) {
                            EmailPropertyWSDTO currProperty = new EmailPropertyWSDTO();
                            String key = (String) enumPropertyNames.nextElement();
                            String value = recipientProperties.getProperty(key);
                            currProperty.propertyName = key;
                            currProperty.propertyValue = value;
                            currEmailProperties.add(currProperty);
                        }
                        currDTORecipient.properties = currEmailProperties;
                        currEmailRecipients.add(currDTORecipient);
                    }
                }

            }
            PayrollServices.commitUnitOfWork();
            return currEmailRecipients;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void process401kSignupFiles(@WebParam(name = "fileData") ArrayList<String> pFileData) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        ThirdParty401kSignUp.signUp(pFileData);
    }

    @WebMethod
    public void initiateRAFTapeCreation(@WebParam(name = "rafActionCode") String rafActionCode) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

        try {
            Application.beginUnitOfWork();
            RAFActionCode code = RAFActionCode.valueOf(rafActionCode);
            if (code == null) {
                throw new RuntimeException("Code: " + rafActionCode + " is not valid");
            }
            RAFEnrollmentFile.createFile(code);
            Application.commitUnitOfWork();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void recreateRAFTape(@WebParam(name = "guid") String rafFileId) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

        try {
            Application.beginUnitOfWork();
            RAFEnrollmentFile file = Application.findById(RAFEnrollmentFile.class, SpcfUniqueId.createInstance(rafFileId));
            if (file == null) {
                throw new RuntimeException("Could not find RAFEnrollment file with guid: " + rafFileId);
            }

            RAFEnrollmentFile.initiateRecreation(file);
            Application.commitUnitOfWork();

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public List<RAFEnrollmentFileWSDTO> writeRAFTape(@WebParam(name = "returnFileContents") Boolean bReturnFileContents) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

        try {
            List<RAFEnrollmentFileWSDTO> returnFiles = new ArrayList<RAFEnrollmentFileWSDTO>();

            RAFFileWriter rafWriter = new RAFFileWriter();
            List<SpcfUniqueId> generatedFileGUIDs = rafWriter.execute();

            Application.beginUnitOfWork();
            if (bReturnFileContents && generatedFileGUIDs.size() > 0) {
                for (SpcfUniqueId currentId : generatedFileGUIDs) {
                    RAFEnrollmentFileWSDTO currentRAFEnrollmentFileWSDTO = new RAFEnrollmentFileWSDTO();
                    RAFEnrollmentFile file = Application.findById(RAFEnrollmentFile.class, currentId);

                    currentRAFEnrollmentFileWSDTO.fileContent = getFileContents(file);
                    currentRAFEnrollmentFileWSDTO.fileName = file.getFileName();
                    returnFiles.add(currentRAFEnrollmentFileWSDTO);
                }
            }
            PayrollServices.commitUnitOfWork();

            return returnFiles;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void recreateAllFilingSpecificTransactions(@WebParam(name = "year") int year,
                                                      @WebParam(name = "quarter") int quarter) throws Exception {
        if (quarter < 0 || quarter > 4) {
            throw new RuntimeException("Invalid Parameter: '" + quarter + "'. Please enter a valid Quarter ");
        }

        SpcfCalendar firstDayOfQtr = CalendarUtils.getFirstDayOfQuarter(year, quarter);
        SpcfCalendar lastDayOfQtr = CalendarUtils.getLastDayOfQuarter(year, quarter);

        try {
            PayrollServices.beginUnitOfWork();
            new CreateFilingsSpecificTransactions().processCompanyDailyLiabilities(CreateFilingsSpecificTransactions.ALL, firstDayOfQtr, lastDayOfQtr);
            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public String runATFExtractE2E(@WebParam(name = "extractType") String extractType) throws Exception {
        try {
            ATFDataExtractRunType extractTypeEnum = ATFDataExtractRunType.valueOf(extractType);

            String jobParameters = extractTypeEnum.toString();

            if (extractTypeEnum == ATFDataExtractRunType.QuarterlyData) {
                int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
                int year = PSPDate.getPSPTime().getYear();
                String timePeriodForAll = " " + year + " " + quarter;
                jobParameters = jobParameters + timePeriodForAll;
            }

            BatchJobManager batchJobManager = new BatchJobManager();
            String jobId = batchJobManager.scheduleJob(BatchJobType.ATFDataExtract, jobParameters);
            return jobId;
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public List<String> generateAndReturnATFDataExtractFiles(@WebParam(name = "runDate") String runDate,
                                                             @WebParam(name = "extractType") String extractType,
                                                             @WebParam(name = "timePeriod") String timePeriod,
                                                             @WebParam(name = "shouldNotReturnFiles") boolean shouldNotReturnFiles,
                                                             @WebParam(name = "includeWageBase") Boolean includeWageBase,
                                                             @WebParam(name = "year") String year) {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        // run date validations
        if (runDate == null || runDate.length() == 0) {
            runDate = PSPDate.getPSPTime().format("yyyyMMdd");
        }
        if (runDate != null && runDate.length() != 8) {
            throw new RuntimeException(
                    "Invalid offload date format" + runDate + ".  Correct format: yyyyMMdd");
        }

        List<String> fileContents = new ArrayList<String>();

        try {
            PayrollServices.beginUnitOfWork();

            ATFDataExtractRunType extractTypeEnum = ATFDataExtractRunType.valueOf(extractType);

            String jobParameters = extractTypeEnum.toString();

            if (extractTypeEnum == ATFDataExtractRunType.QuarterlyData) {
                String timePeriodForAll = timePeriod;
                if (timePeriod == null) {
                    int quarter = CalendarUtils.getQuarterAsInt(PSPDate.getPSPTime());
                    timePeriodForAll = " " + PSPDate.getPSPTime().getYear() + " " + quarter;
                }

                jobParameters = jobParameters + timePeriodForAll;
            } else if (extractTypeEnum == ATFDataExtractRunType.AnnualData) {
                if (year == null || year.length() != 4) {
                    throw new RuntimeException("extractType: AnnualData requires year in format [yyyy]");
                }
                jobParameters += " "+ year;
            }

            SpcfCalendar runForDate = PSPDate.getPSPTime().toLocal();
            if (runDate != null) {
                SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
                dateFormat.setPattern("yyyyMMdd");
                SpcfCalendar parsedRunDate = dateFormat.parse(runDate);
                //Set the date on the calendar that has the local time zone
                runForDate.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
            }
            Application.commitUnitOfWork();

            ATFDataExtractProcessor processor = new ATFDataExtractProcessor(BatchJobProcessor.RunMode.NotUsingFlux,
                                                                            BatchJobType.ATFDataExtract, SpcfUniqueId.createInstance(true).toString(),
                                                                            jobParameters);
            processor.validateRuntimeParameters();
            processor.execute();

            String extractBatchId = processor.getExtractBatchId();

            PayrollServices.beginUnitOfWork();
            ATFDataExtractBatch extractBatch = PayrollServices.entityFinder.findById(ATFDataExtractBatch.class, SpcfUniqueId.createInstance(extractBatchId));

            if (!shouldNotReturnFiles) {
                DomainEntitySet<ATFDataExtractFile> extractFiles;

                // Parameter is optional and considered false if not specified.
                if (includeWageBase == null || !includeWageBase.booleanValue()) {
                    // Everything but the Wage Limits extract.
                    extractFiles = Application.find(ATFDataExtractFile.class,
                                                    ATFDataExtractFile.ATFDataExtractBatch().equalTo(extractBatch)
                                                                      .And(ATFDataExtractFile.FileType().notEqualTo(ATFDataExtractFileType.WageLimitsInfo)));
                } else {
                    extractFiles = Application.find(ATFDataExtractFile.class,
                                                    ATFDataExtractFile.ATFDataExtractBatch().equalTo(extractBatch));
                }

                fileContents = getDataExtractFileContents(extractFiles);
            }
            PayrollServices.rollbackUnitOfWork();
        } catch (Throwable ex) {
            PayrollServices.rollbackUnitOfWork();
            throw new RuntimeException(ex);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return fileContents;

    }

    @WebMethod
    public List<String> generateNACHAFiles(@WebParam(name = "offloadGroup") String offloadGroup,
                                           @WebParam(name = "offloadDate") String offloadDate,
                                           @WebParam(name = "shouldNotReturnFiles") boolean shouldNotReturnFiles) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        // offload date validations
        if (offloadDate == null || offloadDate.length() == 0) {
            offloadDate = PSPDate.getPSPTime().format("yyyyMMdd");
        }
        if (offloadDate != null && offloadDate.length() != 8) {
            throw new RuntimeException(
                    "Invalid offload date format" + offloadDate + ".  Correct format: yyyyMMdd");
        }

        // offload group validations
        if (offloadGroup == null) {
            throw new RuntimeException("NULL Offload Group");
        }
        List<String> fileContents = new ArrayList<String>();

        try {
            PayrollServices.beginUnitOfWork();
            SpcfCalendar runForDate = PSPDate.getPSPTime().toLocal();
            if (offloadDate != null) {
                SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
                dateFormat.setPattern("yyyyMMdd");
                SpcfCalendar parsedRunDate = dateFormat.parse(offloadDate);
                //Set the date on the calendar that has the local time zone
                runForDate.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
            }

            OffloadGroup offloadGroupObj = OffloadGroup.findOffloadGroup(offloadGroup);
            if (offloadGroupObj == null) {
                throw new RuntimeException("Invalid offload group code: " + offloadGroup);
            }
            Application.commitUnitOfWork();

            OffloadACHTransactions offloadACHTxs = new OffloadACHTransactions();
            offloadACHTxs.offloadAndPostOffload(offloadGroup, runForDate);

            Application.beginUnitOfWork();
            if (offloadACHTxs.getOffloadBatch() != null) {
                OffloadBatch createdBatch = Application.findById(OffloadBatch.class, offloadACHTxs.getOffloadBatch().getId());
                DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
                if (!shouldNotReturnFiles) {
                    fileContents = getFileContents(nachaFiles);
                }
                for (NACHAFile nachaFile : nachaFiles) {
                    nachaFile.setStatus(NACHAFileStatus.Archived);
                    Application.save(nachaFile);
                }
            }
            PayrollServices.commitUnitOfWork();

            //Create fee offload events
            CreateTransactionOffloadedEvents eventCreator = new CreateTransactionOffloadedEvents();
            eventCreator.createTransactionOffloadedEvents();
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return fileContents;
    }

    @WebMethod
    public void ProcessACHReturnFile(@WebParam(name = "path") String pPath,
                                     @WebParam(name = "file") String pFile) {
        if (pPath == null || pPath.length() == 0) {
            throw new RuntimeException(
                    "Path cannot be null or empty.");
        }
        if (pFile == null || pFile.length() == 0) {
            throw new RuntimeException(
                    "File cannot be null or empty.");
        }

        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

            PayrollServices.beginUnitOfWork();
            ReturnFileParser returnFileParser = new ReturnFileParser();
            returnFileParser.processFile(new File(pPath, pFile));
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            logger.error("Error handling file file_path:" + pPath + " filename:" + pFile + ".", e);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public List<EmailRecipientWSDTO> runEmailBatchJob(@WebParam(name = "emailTemplate") String pEmailTemplate) throws Exception {
        try {
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<CompanyEventEmail> eventList = CompanyEventEmail.findEmailEventsByStatus(
                    EventEmailStatus.Pending, EventEmailStatus.GroupIncomplete, EventEmailStatus.Resend);

            if(pEmailTemplate != null) {
                EventEmailTemplateTypeCode eventEmailTemplateTypeCode = EventEmailTemplateTypeCode.valueOf(pEmailTemplate);
                eventList = eventList.find(CompanyEventEmail.EmailTemplateTypeCd().equalTo(eventEmailTemplateTypeCode));
            }

            Collection<CompanyEventEmailManager> companyEmailManagers =
                    EventEmailTemplateFactory.buildCompanyEmailManagers(eventList);

            List<EventEmailTemplate> masterTemplateList =
                    EventEmailTemplateFactory.buildMasterTemplateList(companyEmailManagers);

            List<EmailRecipientWSDTO> currEmailRecipients = new ArrayList<EmailRecipientWSDTO>();

            for (EventEmailTemplate currTemplate : masterTemplateList) {

                //Recipients
                List<List<IEventEmail>> recipients = currTemplate.getRecipientsToTransmit();
                for (List<IEventEmail> currList : recipients) {
                    for (IEventEmail currRecipient : currList) {
                        List<EmailPropertyWSDTO> currEmailProperties = new ArrayList<EmailPropertyWSDTO>();
                        EmailRecipientWSDTO currDTORecipient = new EmailRecipientWSDTO();
                        currDTORecipient.templateId = currTemplate.getTemplateId().name();
                        currDTORecipient.companyId = currRecipient.getCompanyId();
                        currRecipient.getProperties();
                        currDTORecipient.recipientEmail = currRecipient.getRecipientEmail();
                        currDTORecipient.recipientId = removeGUIDFromReceipientId(currRecipient.getRecipientId());
                        currDTORecipient.recipientName = currRecipient.getRecipientName();

                        //Properties
                        Properties recipientProperties = currRecipient.getProperties();
                        Enumeration enumPropertyNames = recipientProperties.propertyNames();
                        while (enumPropertyNames.hasMoreElements()) {
                            EmailPropertyWSDTO currProperty = new EmailPropertyWSDTO();
                            String key = (String) enumPropertyNames.nextElement();
                            String value = recipientProperties.getProperty(key);
                            currProperty.propertyName = key;
                            currProperty.propertyValue = value;
                            currEmailProperties.add(currProperty);
                        }
                        currDTORecipient.properties = currEmailProperties;
                        currEmailRecipients.add(currDTORecipient);
                    }
                }

            }
            PayrollServices.commitUnitOfWork();
            return currEmailRecipients;
        } catch (Throwable t) {
            PayrollServices.rollbackUnitOfWork();
            throw new Exception(t);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private String removeGUIDFromReceipientId(String pRecipientId) {
        String hyphen = ".*-";

        Pattern p = Pattern.compile("[" + hyphen + "]+");
        // Split input with the pattern
        String[] result = p.split(pRecipientId);

        //Re-assemble the pieces we want
        String recipientMinusGUID = null;
        if (result.length >= 3) {
            recipientMinusGUID = result[0] + "-" + result[1] + "-" + result[2];
        }

        return recipientMinusGUID;
    }

    @WebMethod
    public List<NACHAValidationWSDTO> generateAndValidateNACHAFiles(
            @WebParam(name = "offloadGroup") String offloadGroup,
            @WebParam(name = "offloadDate") String offloadDate,
            @WebParam(name = "expectedFileContents") List<InputFileContentDTO> expectedFileContents)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        // offload date validations
        if (offloadDate == null || offloadDate.length() == 0) {
            offloadDate = PSPDate.getPSPTime().format("yyyyMMdd");
        }

        if (offloadDate != null && offloadDate.length() != 8) {
            throw new RuntimeException(
                    "Invalid offload date format" + offloadDate + ".  Correct format: yyyyMMdd");
        }

        // offload group validations
        if (offloadGroup == null) {
            throw new RuntimeException("NULL Offload Group");
        }
        List<NACHAValidationWSDTO> nachaValidationDTOList = new ArrayList<NACHAValidationWSDTO>();

        try {
            PayrollServices.beginUnitOfWork();
            SpcfCalendar runForDate = PSPDate.getPSPTime().toLocal();
            if (offloadDate != null) {
                SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
                dateFormat.setPattern("yyyyMMdd");
                SpcfCalendar parsedRunDate = dateFormat.parse(offloadDate);
                //Set the date on the calendar that has the local time zone
                runForDate.setValues(parsedRunDate.getYear(), parsedRunDate.getMonth(), parsedRunDate.getDay());
            }

            OffloadGroup offloadGroupObj = OffloadGroup.findOffloadGroup(offloadGroup);
            if (offloadGroupObj == null) {
                throw new RuntimeException("Invalid offload group code: " + offloadGroup);
            }
            Application.commitUnitOfWork();

            ACHFileType achFileType = null;
            OffloadACHTransactions offloadACHTxs = new OffloadACHTransactions();
            if (expectedFileContents != null && expectedFileContents.size() > 0) {
                for (InputFileContentDTO expectedFileContent : expectedFileContents) {
                    if (expectedFileContent.fileType.equals(NACHAFileType.CCDPlus.toString())) {
                        achFileType = ACHFileType.Tax;
                        break;
                    }
                }
            }

            if (achFileType != null) {
                offloadACHTxs.offloadAndPostOffload(offloadGroup, runForDate, achFileType);
            } else {
                offloadACHTxs.offloadAndPostOffload(offloadGroup, runForDate);
            }

            Application.beginUnitOfWork();
            OffloadBatch createdBatch = Application.findById(OffloadBatch.class, offloadACHTxs.getOffloadBatch().getId());

            DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
            PayrollServices.commitUnitOfWork();
            // validate the content with the input expected file contents
            NACHAFile ccdFile = null;
            NACHAFile ppdFile = null;
            NACHAFile ccdPlus = null;
            for (NACHAFile nachaFile : nachaFiles) {
                if (nachaFile.getFileType().equals(NACHAFileType.CCD)) {
                    ccdFile = nachaFile;
                } else if (nachaFile.getFileType().equals(NACHAFileType.CCDPlus)) {
                    ccdPlus = nachaFile;
                } else {
                    ppdFile = nachaFile;
                }
            }

            NACHAValidationWSDTO validationWSDTO = null;
            for (InputFileContentDTO inputFileContentDTO : expectedFileContents) {
                if (inputFileContentDTO.fileType.equalsIgnoreCase(NACHAFileType.CCD.toString())) {
                    validationWSDTO = new NACHAValidationWSDTO();
                    if (null != ccdFile) {
                        validationWSDTO = validateFileAndTraceNumbers(inputFileContentDTO.expectedFileContent,
                                                                      ccdFile.getFileName(), NACHAFileType.CCD);
                    } else {
                        validationWSDTO.fileComparision = false;
                        validationWSDTO.traceNumberComparision = false;
                        validationWSDTO.overallValidation = false;
                        validationWSDTO.fileType = "CCD";
                        validationWSDTO.filevalidationErrors = Arrays.asList(new String[]{"No CCD file generated"});
                    }
                    nachaValidationDTOList.add(validationWSDTO);
                } else if (inputFileContentDTO.fileType.equalsIgnoreCase(NACHAFileType.CCDPlus.toString())) {
                    validationWSDTO = new NACHAValidationWSDTO();
                    if (null != ccdPlus) {
                        validationWSDTO = validateFileAndTraceNumbers(inputFileContentDTO.expectedFileContent,
                                                                      ccdPlus.getFileName(), NACHAFileType.CCDPlus);
                    } else {
                        validationWSDTO.fileComparision = false;
                        validationWSDTO.traceNumberComparision = false;
                        validationWSDTO.overallValidation = false;
                        validationWSDTO.fileType = "CCDPlus";
                        validationWSDTO.filevalidationErrors = Arrays.asList(new String[]{"No CCD Plus file generated"});
                    }
                    nachaValidationDTOList.add(validationWSDTO);
                } else if (inputFileContentDTO.fileType.equalsIgnoreCase(NACHAFileType.PPD.toString())) {
                    validationWSDTO = new NACHAValidationWSDTO();
                    if (null != ppdFile) {
                        validationWSDTO = validateFileAndTraceNumbers(inputFileContentDTO.expectedFileContent,
                                                                      ppdFile.getFileName(), NACHAFileType.PPD);
                    } else {
                        validationWSDTO.fileComparision = false;
                        validationWSDTO.traceNumberComparision = false;
                        validationWSDTO.overallValidation = false;
                        validationWSDTO.fileType = "PPD";
                        validationWSDTO.filevalidationErrors = Arrays.asList(new String[]{"No PPD file generated"});
                    }
                    nachaValidationDTOList.add(validationWSDTO);
                }

                //Create fee offload events
                CreateTransactionOffloadedEvents eventCreator = new CreateTransactionOffloadedEvents();
                eventCreator.createTransactionOffloadedEvents();

            }
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return nachaValidationDTOList;
    }

    @WebMethod
    public void createBankReturns(@WebParam(name = "bankReturnDTOs") List<BankReturnWSDTO> bankReturnDTOs)
            throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

        if (bankReturnDTOs == null || bankReturnDTOs.size() <= 0) {
            return;
        }

        try {
            // need auto-flush enabled to handle returns for different payrolls for same company (PSRV001219)
            Application.setDefaultHibernateFlushMode(FlushMode.AUTO);

            // create TransactionReturnBatch and TransactionReturns
            PayrollServices.beginUnitOfWork();

            TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
            transactionReturnBatch.setACHReturnFileName(null);
            transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

            transactionReturnBatch = Application.save(transactionReturnBatch);//TransactionReturnBE.addTransactionReturnBatch(transactionReturnBatch);

            Collection<TransactionReturn> transactionReturnList = buildTransactionReturnList(transactionReturnBatch, bankReturnDTOs);
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);

            PayrollServices.commitUnitOfWork();

            // Process TransactionReturns associated with the TransactionReturnBatch
            ReturnFileParser returnsProcessor = new ReturnFileParser();
            returnsProcessor.processTransactionReturns(transactionReturnBatch.getId());

        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();

            // clear default preference so threads from thread pool won't retain this for other work.
            Application.setDefaultHibernateFlushMode(null);
        }
    }

    @WebMethod
    public List<BankReturnWSDTO> createNonPayrollBankReturns(
            @WebParam(name = "sourcePayrollSystem") String sourcePayrollSystem,
            @WebParam(name = "sourceCompanyID") String sourceCompanyID,
            @WebParam(name = "transactionType") String transactionType,
            @WebParam(name = "bankReturnCd") String bankReturnCd,
            @WebParam(name = "bankReturnDescription") String bankReturnDescription)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourcePayrollSystem == null || sourcePayrollSystem.trim().length() == 0) {
            throw new RuntimeException("sourcePayrollSystem can not be null");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("sourceCompanyID can not be null");
        }

        if (bankReturnCd == null || bankReturnCd.trim().length() == 0) {
            throw new RuntimeException("bankReturnCd can not be null");
        }

        try {
            // need auto-flush enabled to handle returns for different payrolls for same company (PSRV001219)
            Application.setDefaultHibernateFlushMode(FlushMode.AUTO);

            // create TransactionReturnBatch and TransactionReturns
            PayrollServices.beginUnitOfWork();
            SourceSystemCode sourceSystemCode = SourceSystemCode.valueOf(sourcePayrollSystem);
            TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
            transactionReturnBatch.setACHReturnFileName(null);
            transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

            transactionReturnBatch = Application.save(transactionReturnBatch);//TransactionReturnBE.addTransactionReturnBatch(transactionReturnBatch);

            DomainEntitySet<TransactionReturn> txnReturns = null;

            txnReturns = createTransactionReturnsByTxnType(transactionReturnBatch, sourceSystemCode,
                                                           sourceCompanyID, null, null, transactionType,
                                                           bankReturnCd, bankReturnDescription);
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
            PayrollServices.commitUnitOfWork();

            // Process TransactionReturns associated with the TransactionReturnBatch
            ReturnFileParser returnsProcessor = new ReturnFileParser();
            returnsProcessor.processTransactionReturns(transactionReturnBatch.getId());

            // build output DTOs
            List<BankReturnWSDTO> bankReturnWSDTOs = new ArrayList<BankReturnWSDTO>();
            PayrollServices.beginUnitOfWork();
            for (TransactionReturn transactionReturn : txnReturns) {
                transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
                bankReturnWSDTOs.add(buildBankReturnWSDTO(transactionReturn));
            }
            PayrollServices.commitUnitOfWork();
            return bankReturnWSDTOs;

        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();

            // clear default preference so threads from thread pool won't retain this for other work.
            Application.setDefaultHibernateFlushMode(null);
        }

    }

    @WebMethod
    public List<BankReturnWSDTO> createPayrollBankReturns(
            @WebParam(name = "sourcePayrollSystem") String sourcePayrollSystem,
            @WebParam(name = "sourceCompanyID") String sourceCompanyID,
            @WebParam(name = "sourceBatchId") String sourceBatchId,
            @WebParam(name = "ddTransactionId") String ddTransactionId,
            @WebParam(name = "transactionType") String transactionType,
            @WebParam(name = "bankReturnCd") String bankReturnCd,
            @WebParam(name = "bankReturnDescription") String bankReturnDescription,
            @WebParam(name = "sourcePaycheckId") String sourcePaycheckId,
            @WebParam(name = "sourceBillPaymentId") String sourceBillPaymentId)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (sourcePayrollSystem == null || sourcePayrollSystem.trim().length() == 0) {
            throw new RuntimeException("sourcePayrollSystem can not be null");
        }

        if (sourceCompanyID == null || sourceCompanyID.trim().length() == 0) {
            throw new RuntimeException("sourceCompanyID can not be null");
        }

        if ((sourceBatchId == null || sourceBatchId.trim().length() == 0) &&
                (ddTransactionId == null || ddTransactionId.trim().length() == 0) &&
                (sourcePaycheckId == null || sourcePaycheckId.trim().length() == 0) &&
                (sourceBillPaymentId == null || sourceBillPaymentId.trim().length() == 0)) {
            throw new RuntimeException("Either sourceBatchId or ddTransactioId or sourcePaycheckId or sourceBillPaymentId must be specified");
        }

        if ((sourceBatchId == null || sourceBatchId.trim().length() > 0) &&
                (ddTransactionId == null || ddTransactionId.trim().length() == 0) &&
                (sourcePaycheckId == null || sourcePaycheckId.trim().length() == 0) &&
                (sourceBillPaymentId == null || sourceBillPaymentId.trim().length() == 0) &&
                (transactionType == null || transactionType.trim().length() == 0)) {
            throw new RuntimeException("If ddTransactioId and sourcePaycheckId and sourceBillPaymentId are not specified both " +
                                               "sourceBatchId and transactionType must be specified");
        }

        if (bankReturnCd == null || bankReturnCd.trim().length() == 0) {
            throw new RuntimeException("bankReturnCd can not be null");
        }

        try {
            // need auto-flush enabled to handle returns for different payrolls for same company (PSRV001219)
            Application.setDefaultHibernateFlushMode(FlushMode.AUTO);

            // create TransactionReturnBatch and TransactionReturns
            PayrollServices.beginUnitOfWork();
            SourceSystemCode sourceSystemCode = SourceSystemCode.valueOf(sourcePayrollSystem);
            TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
            transactionReturnBatch.setACHReturnFileName(null);
            transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

            transactionReturnBatch = Application.save(transactionReturnBatch);//TransactionReturnBE.addTransactionReturnBatch(transactionReturnBatch);

            DomainEntitySet<TransactionReturn> txnReturns = null;
            if (null != ddTransactionId && ddTransactionId.trim().length() != 0) {
                if (transactionType != null) {
                    txnReturns = createReversalTransactionReturns(transactionReturnBatch, sourceSystemCode,
                                                                  sourceCompanyID, ddTransactionId,
                                                                  bankReturnCd, bankReturnDescription);
                } else {
                    txnReturns = createPaycheckSplitTransactionReturns(transactionReturnBatch, sourceSystemCode,
                                                                       sourceCompanyID, ddTransactionId,
                                                                       bankReturnCd, bankReturnDescription);
                }
            } else if (null != sourcePaycheckId && sourcePaycheckId.trim().length() != 0) {
                txnReturns = createPaycheckTransactionReturns(transactionReturnBatch, sourceSystemCode,
                                                              sourceCompanyID, sourcePaycheckId,
                                                              bankReturnCd, bankReturnDescription);
            } else if (sourceBillPaymentId != null && sourceBillPaymentId.trim().length() != 0 &&
                    (transactionType == null || transactionType.trim().length() == 0)) {
                txnReturns = createBillPaymentTransactionReturns(transactionReturnBatch, sourceSystemCode,
                                                                 sourceCompanyID, sourceBillPaymentId,
                                                                 bankReturnCd, bankReturnDescription);

            } else if (sourceBillPaymentId != null && sourceBillPaymentId.trim().length() != 0 &&
                    (transactionType != null && transactionType.trim().length() != 0)) {
                txnReturns = createTransactionReturnsByTxnType(transactionReturnBatch, sourceSystemCode,
                                                               sourceCompanyID, sourceBatchId, sourceBillPaymentId, transactionType,
                                                               bankReturnCd, bankReturnDescription);

            } else {
                txnReturns = createTransactionReturnsByTxnType(transactionReturnBatch, sourceSystemCode,
                                                               sourceCompanyID, sourceBatchId, sourceBillPaymentId, transactionType,
                                                               bankReturnCd, bankReturnDescription);
            }
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
            PayrollServices.commitUnitOfWork();

            // Process TransactionReturns associated with the TransactionReturnBatch
            ReturnFileParser returnsProcessor = new ReturnFileParser();
            returnsProcessor.processTransactionReturns(transactionReturnBatch.getId());

            // build output DTOs
            List<BankReturnWSDTO> bankReturnWSDTOs = new ArrayList<BankReturnWSDTO>();
            PayrollServices.beginUnitOfWork();
            for (TransactionReturn transactionReturn : txnReturns) {
                transactionReturn = Application.findById(TransactionReturn.class, transactionReturn.getId());
                bankReturnWSDTOs.add(buildBankReturnWSDTO(transactionReturn));
            }
            PayrollServices.commitUnitOfWork();
            return bankReturnWSDTOs;

        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();

            // clear default preference so threads from thread pool won't retain this for other work.
            Application.setDefaultHibernateFlushMode(null);
        }
    }

    @WebMethod
    public void runAchTransactionProcessor(@WebParam(name = "processingDate") String processingDate) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (processingDate == null || processingDate.length() == 0) {
            processingDate = PSPDate.getPSPTime().format("yyyyMMdd");
        }
        SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
        dateFormat.setPattern("yyyyMMdd");
        try {
            dateFormat.parse(processingDate);
        } catch (SpcfFormatException ex) {
            throw new RuntimeException("Invalid Processing Date format '" + processingDate + "'.  Correct format: yyyyMMdd");
        }

        try {
            PayrollServices.beginUnitOfWork();
            ProcessACHTransactions transactionProcessor = new ProcessACHTransactions();
            transactionProcessor.process(processingDate);
            PayrollServices.commitUnitOfWork();

        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }


    @WebMethod
    public void runMissedTransactionProcessor(@WebParam(name = "processingDate") String processingDate) throws Exception {
        try {
            executeMissedTransactionProcessor(processingDate);
        } catch (Exception e) {
            throw e;
        }
    }

    @WebMethod
    public MissedTransactionProcessorWSDTO runMissedTransactionProcessorWithResults(@WebParam(name = "processingDate") String processingDate) throws Exception {
        try {
            MissedTransactionProcessorWSDTO returnDTO = executeMissedTransactionProcessor(processingDate);
            System.out.println("Return DTO error: " + returnDTO.errorMessage);
            System.out.println("Notification DTO message: " + returnDTO.notificationMessage);
            return returnDTO;
        } catch (Exception e) {
            throw e;
        }
    }

    private MissedTransactionProcessorWSDTO executeMissedTransactionProcessor(String pProcessingDate) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        MissedTransactionProcessorWSDTO returnDTO = new MissedTransactionProcessorWSDTO();
        if (pProcessingDate == null || pProcessingDate.length() == 0) {
            pProcessingDate = PSPDate.getPSPTime().format("yyyyMMdd");
        }
        SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
        dateFormat.setPattern("yyyyMMdd");
        try {
            dateFormat.parse(pProcessingDate);
        } catch (SpcfFormatException ex) {
            throw new RuntimeException("Invalid Processing Date format '" + pProcessingDate + "'.  Correct format: yyyyMMdd");
        }

        try {
            PayrollServices.beginUnitOfWork();
            ProcessMissedACHTransactions transactionProcessor = new ProcessMissedACHTransactions();
            transactionProcessor.process(pProcessingDate);
            returnDTO.errorMessage = transactionProcessor.getErrorMessage();
            returnDTO.notificationMessage = transactionProcessor.getNotificationMessage();
            PayrollServices.commitUnitOfWork();
            return returnDTO;
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void runMissedPayrollProcessor(@WebParam(name = "processingDate") String processingDate) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (processingDate == null || processingDate.length() == 0) {
            processingDate = PSPDate.getPSPTime().format("yyyyMMdd");
        }
        SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
        dateFormat.setPattern("yyyyMMdd");
        try {
            dateFormat.parse(processingDate);
        } catch (SpcfFormatException ex) {
            throw new RuntimeException("Invalid Processing Date format '" + processingDate + "'.  Correct format: yyyyMMdd");
        }

        try {
            PayrollServices.beginUnitOfWork();
            ProcessMissedPayrolls transactionProcessor = new ProcessMissedPayrolls();
            transactionProcessor.process(processingDate);
            PayrollServices.commitUnitOfWork();

        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void runSalesTaxExceptionProcess(@WebParam(name = "offloadDate") String offloadDate) throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        // offload date validations
        if (offloadDate == null || offloadDate.length() == 0) {
            offloadDate = PSPDate.getPSPTime().format("yyyyMMdd");
        }
        if (offloadDate != null && offloadDate.length() != 8) {
            throw new RuntimeException(
                    "Invalid offload date format" + offloadDate + ".  Correct format: yyyyMMdd");
        }

        try {

            SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
            dateFormat.setPattern("yyyyMMdd");
            SpcfCalendar vertexDate = dateFormat.parse(offloadDate);

            String vertexUpdateDate = vertexDate.format("yyyyMM01");

            PayrollServices.beginUnitOfWork();
            SalesTaxExceptionProcess step = new SalesTaxExceptionProcess();
            step.process(vertexUpdateDate);
            PayrollServices.commitUnitOfWork();

        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

    }

    private List<String> getDataExtractFileContents(DomainEntitySet<ATFDataExtractFile> pExtractFiles) throws Throwable {
        List<String> fileContents = new ArrayList<String>(pExtractFiles.size());



        for (ATFDataExtractFile file : pExtractFiles) {

            System.out.println("Attempting to unzip " + file.getFileName());
            FileUtils.gUnZip(file.getFileName());

            String fileNameWithoutGZ = file.getFileName().split(".gz")[0];

            System.out.println("File to read " + fileNameWithoutGZ);

            List<String> companyLines = new ArrayList<String>(); //no pun intended

            StringBuilder content = new StringBuilder();

            //sort adjacent lines that contain PSIDs.  Everything else passed through without sorting.
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(fileNameWithoutGZ));
            while ((line = reader.readLine()) != null) {
                if (line.matches(".*\\d{9}.*")) { //PSID
                    companyLines.add(line);
                } else {
                    if (!companyLines.isEmpty()) {
                        Collections.sort(companyLines);
                        for (String s : companyLines) {
                            content.append(s).append("\n");
                        }
                        companyLines = new ArrayList<String>();
                    }
                    content.append(line).append("\n");
                }
            }
            if (!companyLines.isEmpty()) {
                Collections.sort(companyLines);
                for (String s : companyLines) {
                    content.append(s).append("\n");
                }
            }

            fileContents.add(content.toString());
        }

        return fileContents;
    }

    private List<String> getFileContents(DomainEntitySet<NACHAFile> pNACHAFiles) {
        List<String> fileContents = new ArrayList<String>(pNACHAFiles.size());
        BufferedReader reader = null;
        String line = null;
        StringBuffer content = null;

        int i = 0;
        for (NACHAFile file : pNACHAFiles) {
            try {
                content = new StringBuffer();
                reader = new BufferedReader(new FileReader(file.getFileName()));
                while ((line = reader.readLine()) != null) {
                    content.append(line + "\n");
                }
            } catch (Exception e) {
                // continue with next file
            }
            fileContents.add(content.toString());
        }

        return fileContents;
    }

    private String getFileContents(RAFEnrollmentFile pRAFFile) throws Exception {
        String line;
        StringBuffer content = new StringBuffer();
        BufferedReader reader = new BufferedReader(new FileReader(pRAFFile.getFileName()));
        while ((line = reader.readLine()) != null) {
            content.append(line + "\n");
        }

        return content.toString();
    }

    private String getFileContents(ThirdParty401kBatch pTP401kBatchs) throws Exception {
        String line;
        StringBuffer content = new StringBuffer();

        if (!ThirdParty401kBatchStatusCode.Empty.equals(pTP401kBatchs.getUploadStatusCd())) {
            content = new StringBuffer();
            BufferedReader reader = new BufferedReader(new FileReader(pTP401kBatchs.getFileName()));
            while ((line = reader.readLine()) != null) {
                content.append(line + "\n");
            }

        }

        return content.toString();
    }

    private DomainEntitySet<TransactionReturn> buildTransactionReturnList(TransactionReturnBatch pTransactionReturnBatch,
                                                                          List<BankReturnWSDTO> bankReturnDTOs) {
        DomainEntitySet<TransactionReturn> transactionRetruns = new DomainEntitySet<TransactionReturn>();
        TransactionReturn transactionReturn = null;
        FinancialTransaction financialTx = null;
        String nachaCode = null;
        String achMsg = null;
        for (BankReturnWSDTO bankReturnWSDTO : bankReturnDTOs) {
            transactionReturn = new TransactionReturn();
            nachaCode = bankReturnWSDTO.bankReturnCd;
            transactionReturn.setBankReturnCd(nachaCode);
            achMsg = nachaCode + "00000000000000000000000000000DESCRIPTION";
            if (nachaCode.equals("C01")) {
                achMsg = bankReturnWSDTO.accountNumber;
            } else if (nachaCode.equals("C02")) {
                achMsg = bankReturnWSDTO.routingNumber;
            } else if (nachaCode.equals("C03")) {
                achMsg = bankReturnWSDTO.routingNumber + "   " + bankReturnWSDTO.accountNumber;
            } else if (nachaCode.equals("C05")) {
                achMsg = bankReturnWSDTO.accountType;
            } else if (nachaCode.equals("C06")) {
                achMsg = bankReturnWSDTO.accountNumber + "   " + bankReturnWSDTO.accountType;
            } else if (nachaCode.equals("C07")) {
                achMsg = bankReturnWSDTO.routingNumber + bankReturnWSDTO.accountNumber + bankReturnWSDTO.accountType;
            }
            transactionReturn.setBankReturnDescription(achMsg);

            transactionReturn.setReturnBatch(pTransactionReturnBatch);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());

            try {
                financialTx =
                        Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(bankReturnWSDTO.transactionId));
            } catch (Exception ex) {
                System.out.println("Error while getting Financial Transaction" + ex);
            }

            if (financialTx != null) {
                //function call to check whether the transaction return with "R" code is already created or not for the
                //same MMT.
                isTransactionReturnExists(nachaCode, financialTx);
                transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
                transactionReturn.setCompany(financialTx.getCompany());
            }


            transactionRetruns.add(Application.save(transactionReturn));
        }

        return transactionRetruns;
    }

    private DomainEntitySet<TransactionReturn> createPaycheckSplitTransactionReturns(TransactionReturnBatch pTransactionReturnBatch,
                                                                                     SourceSystemCode pSourceSystem, String pSourceCompanyID,
                                                                                     String pDDTransactionId,
                                                                                     String pBankReturnCd, String pBankReturnDescription) throws Exception {
        DomainEntitySet<TransactionReturn> transactionReturns = new DomainEntitySet<TransactionReturn>();
        Company pCompany = Company.findCompany(pSourceCompanyID, pSourceSystem);

        if (null == pCompany) {
            throw new RuntimeException("Invalid sourceCompanyID specified");
        }

        PaycheckSplit paycheckSplit = PaycheckSplit.findNonCanceledPaycheckSplit(pCompany,
                                                                                 pDDTransactionId);

        if (paycheckSplit == null) {
            throw new RuntimeException("There is no paycheck split with the id " + pDDTransactionId);
        }

        TransactionReturn transactionReturn = new TransactionReturn();
        FinancialTransaction financialTx = paycheckSplit.getFinancialTransaction();

        if (financialTx == null) {
            throw new RuntimeException("There is no corresponding financial transaction for the specified " +
                                               "Paycheck split");
        }

        if (financialTx.getCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Executed) {
            throw new RuntimeException("The corresponding financial transaction for the specified " +
                                               "Paycheck split is not in the executed state");
        }

        //function call to check whether the transaction return with "R" code is already created or not for the
        //same MMT.
        isTransactionReturnExists(pBankReturnCd, financialTx);

        transactionReturn.setBankReturnCd(pBankReturnCd);
        transactionReturn.setBankReturnDescription(pBankReturnDescription);
        transactionReturn.setReturnBatch(pTransactionReturnBatch);
        transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
        transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());
        transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
        transactionReturn.setCompany(financialTx.getCompany());

        transactionReturn = Application.save(transactionReturn);
        transactionReturns.add(transactionReturn);

        return transactionReturns;
    }

    private DomainEntitySet<TransactionReturn> createReversalTransactionReturns(TransactionReturnBatch pTransactionReturnBatch,
                                                                                SourceSystemCode pSourceSystem, String pSourceCompanyID,
                                                                                String pDDTransactionId,
                                                                                String pBankReturnCd, String pBankReturnDescription) throws Exception {
        DomainEntitySet<TransactionReturn> transactionReturns = new DomainEntitySet<TransactionReturn>();
        Company pCompany = Company.findCompany(pSourceCompanyID, pSourceSystem);

        if (null == pCompany) {
            throw new RuntimeException("Invalid sourceCompanyID specified");
        }

        PaycheckSplit paycheckSplit = PaycheckSplit.findNonCanceledPaycheckSplit(pCompany,
                                                                                 pDDTransactionId);

        TransactionReturn transactionReturn = new TransactionReturn();

        if (paycheckSplit == null) {
            Paycheck paycheck = Paycheck.findNonCanceledPaycheck(pCompany, pDDTransactionId);

            if (paycheck == null) {
                throw new RuntimeException("There is no paycheck split or paycheck with the id " + pDDTransactionId);
            }

            DomainEntitySet<PaycheckSplit> allSplits = paycheck.getPaycheckSplitCollection();
            if (allSplits == null || allSplits.size() != 0) {
                paycheckSplit = allSplits.get(0);
            }
        }

        if (paycheckSplit == null) {
            throw new RuntimeException("There is no paycheck split or paycheck with the id " + pDDTransactionId);
        }

        FinancialTransaction financialTx = paycheckSplit.getFinancialTransaction();

        DomainEntitySet<FinancialTransaction> finTxns = FinancialTransaction.findFinancialTransactions(pCompany, financialTx, TransactionTypeCode.EmployeeDdReversalDebit);

        if (finTxns.isEmpty()) {
            throw new RuntimeException("There is no corresponding reversal financial transaction for the specified " +
                                               "Paycheck split");
        }

        financialTx = finTxns.get(0);

        if (financialTx == null) {
            throw new RuntimeException("There is no corresponding reversal financial transaction for the specified " +
                                               "Paycheck split");
        }

        if (financialTx.getCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Executed) {
            throw new RuntimeException("The corresponding financial transaction for the specified " +
                                               "Paycheck split is not in the executed state");
        }

        //function call to check whether the transaction return with "R" code is already created or not for the
        //same MMT.
        isTransactionReturnExists(pBankReturnCd, financialTx);

        transactionReturn.setBankReturnCd(pBankReturnCd);
        transactionReturn.setBankReturnDescription(pBankReturnDescription);
        transactionReturn.setReturnBatch(pTransactionReturnBatch);
        transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
        transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());
        transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
        transactionReturn.setCompany(financialTx.getCompany());

        transactionReturn = Application.save(transactionReturn);
        transactionReturns.add(transactionReturn);

        return transactionReturns;
    }

    private DomainEntitySet<TransactionReturn> createPaycheckTransactionReturns(TransactionReturnBatch pTransactionReturnBatch,
                                                                                SourceSystemCode pSourceSystem, String pSourceCompanyID,
                                                                                String pSourcePaycheckId,
                                                                                String pBankReturnCd, String pBankReturnDescription) throws Exception {
        DomainEntitySet<TransactionReturn> transactionReturns = new DomainEntitySet<TransactionReturn>();
        Company pCompany = Company.findCompany(pSourceCompanyID, pSourceSystem);

        if (null == pCompany) {
            throw new RuntimeException("Invalid sourceCompanyID specified");
        }

        Paycheck paycheck = Paycheck.findNonCanceledPaycheck(pCompany, pSourcePaycheckId);
        if (paycheck == null) {
            throw new RuntimeException("There are no corresponding financial transaction for the specified " +
                                               "Paycheck");
        }
        boolean hasExecutedTxns = false;
        for (PaycheckSplit paycheckSplit : paycheck.getPaycheckSplitCollection()) {
            FinancialTransaction financialTx = paycheckSplit.getFinancialTransaction();

            if (financialTx.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Executed ||
                    financialTx.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Completed) {
                TransactionReturn transactionReturn = new TransactionReturn();

                transactionReturn.setBankReturnCd(pBankReturnCd);
                transactionReturn.setBankReturnDescription(pBankReturnDescription);
                transactionReturn.setReturnBatch(pTransactionReturnBatch);
                transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
                transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());

                if (financialTx != null) {

                    //function call to check whether the transaction return with "R" code is already created or not for the
                    //same MMT.
                    isTransactionReturnExists(pBankReturnCd, financialTx);

                    transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
                    transactionReturn.setCompany(financialTx.getCompany());
                }

                transactionReturn = Application.save(transactionReturn);
                transactionReturns.add(transactionReturn);
                hasExecutedTxns = true;
            }
        }

        if (!hasExecutedTxns) {
            throw new RuntimeException("There are no executed or completed transactions for the specified Paycheck Id "
                                               + pSourcePaycheckId);
        }

        return transactionReturns;
    }

    private DomainEntitySet<TransactionReturn> createBillPaymentTransactionReturns(TransactionReturnBatch pTransactionReturnBatch,
                                                                                   SourceSystemCode pSourceSystem,
                                                                                   String pSourceCompanyID,
                                                                                   String pSourceBillPaymentId,
                                                                                   String pBankReturnCd,
                                                                                   String pBankReturnDescription) throws Exception {
        DomainEntitySet<TransactionReturn> transactionReturns = new DomainEntitySet<TransactionReturn>();
        Company pCompany = Company.findCompany(pSourceCompanyID, pSourceSystem);

        if (null == pCompany) {
            throw new RuntimeException("Invalid sourceCompanyID specified");
        }

        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(pCompany, pSourceBillPaymentId);

        if (billPayment == null) {
            throw new RuntimeException("There are no corresponding financial transaction for the specified BillPayment");
        }
        boolean hasExecutedTxns = false;
        for (BillPaymentSplit billPaymentSplit : billPayment.getBillPaymentSplitCollection()) {
            FinancialTransaction financialTx = billPaymentSplit.getFinancialTransaction();
            if (financialTx.getCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Executed &&
                    financialTx.getCurrentTransactionState().getTransactionStateCd() != TransactionStateCode.Completed) {
                throw new RuntimeException("Transaction " + pSourceBillPaymentId + " cannot be returned");
            }

            if (financialTx.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Executed ||
                    financialTx.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Completed) {
                TransactionReturn transactionReturn = new TransactionReturn();

                transactionReturn.setBankReturnCd(pBankReturnCd);
                transactionReturn.setBankReturnDescription(pBankReturnDescription);
                transactionReturn.setReturnBatch(pTransactionReturnBatch);
                transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
                transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());

                if (financialTx != null) {

                    //function call to check whether the transaction return with "R" code is already created or not for the
                    //same MMT.
                    isTransactionReturnExists(pBankReturnCd, financialTx);

                    transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
                    transactionReturn.setCompany(financialTx.getCompany());
                }

                transactionReturn = Application.save(transactionReturn);
                transactionReturns.add(transactionReturn);
                hasExecutedTxns = true;
            }
        }

        if (!hasExecutedTxns) {
            throw new RuntimeException("There are no executed or completed transactions for the specified Bill Payment Id "
                                               + pSourceBillPaymentId);
        }

        return transactionReturns;
    }

    private DomainEntitySet<TransactionReturn> createTransactionReturnsByTxnType(TransactionReturnBatch pTransactionReturnBatch,
                                                                                 SourceSystemCode pSourceSystem, String pSourceCompanyID,
                                                                                 String pSourceBatchId, String pBillPaymentId, String pTransactionType,
                                                                                 String pBankReturnCd, String pBankReturnDescription) throws Exception {
        DomainEntitySet<TransactionReturn> transactionReturns = new DomainEntitySet<TransactionReturn>();
        Company pCompany = Company.findCompany(pSourceCompanyID, pSourceSystem);

        if (null == pCompany) {
            throw new RuntimeException("Invalid sourceCompanyID specified");
        }
        DomainEntitySet<FinancialTransaction> finTxs = null;
        if (null != pSourceBatchId) {
            PayrollRun payrollRun = TransactionsWS.findPayrollRunBySourceId(pCompany, pSourceBatchId);

            if (payrollRun == null) {
                throw new RuntimeException("There is no corresponding payroll run for the specified " +
                                                   "Source Batch Id " + pSourceBatchId);
            }
            finTxs = payrollRun.getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.valueOf(pTransactionType)}, null)
                               .find(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO));
        } else if (pBillPaymentId != null) {
            BillPayment billPayment = BillPayment.findBillPaymentBySourceId(pCompany, pBillPaymentId);

            if (billPayment == null || billPayment.getPayrollRun() == null) {
                throw new RuntimeException("There is no corresponding payroll run for the specified " +
                                                   "Bill Payment Id " + pBillPaymentId);
            }
            finTxs = billPayment.getPayrollRun().getFinancialTransactions(
                    new TransactionTypeCode[]{TransactionTypeCode.valueOf(pTransactionType)}, null);
        } else {
            CompanyBankAccount companyBankAccount = null;

            Criterion<CompanyBankAccount> where = CompanyBankAccount.Company().equalTo(pCompany)
                                                                    .And(CompanyBankAccount.ExpirationDate().isNull());

            DomainEntitySet<CompanyBankAccount> companyBankAccounts = Application.find(CompanyBankAccount.class, where);

            if (!companyBankAccounts.isEmpty()) {
                companyBankAccount = companyBankAccounts.get(0);
            }
            TransactionType transactionType = TransactionType.findTransactionType(TransactionTypeCode.valueOf(pTransactionType));

            Expression<FinancialTransaction> query =
                    new Query<FinancialTransaction>()
                            .Where(FinancialTransaction.Company().equalTo(pCompany)
                                                       .And(FinancialTransaction.DebitBankAccount().equalTo(companyBankAccount.getBankAccount())
                                                                                .Or(FinancialTransaction.CreditBankAccount().equalTo(companyBankAccount.getBankAccount())))
                                                       .And(FinancialTransaction.TransactionType().equalTo(transactionType))
                                                       .And(FinancialTransaction.FinancialTransactionAmount().greaterThan(SpcfMoney.ZERO)))
                            .OrderBy(FinancialTransaction.SettlementDate().Descending());

            finTxs = Application.find(FinancialTransaction.class, query);
        }
        boolean hasExecutedTxns = false;
        ArrayList<MoneyMovementTransaction> processedMMTs = new ArrayList<MoneyMovementTransaction>();
        for (FinancialTransaction financialTx : finTxs) {
            boolean alreadyProcessedMMT = processedMMTs.contains(financialTx.getMoneyMovementTransaction());
            if ((financialTx.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Executed ||
                    financialTx.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Completed) && !alreadyProcessedMMT) {
                TransactionReturn transactionReturn = new TransactionReturn();

                transactionReturn.setBankReturnCd(pBankReturnCd);
                transactionReturn.setBankReturnDescription(pBankReturnDescription);
                transactionReturn.setReturnBatch(pTransactionReturnBatch);
                transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
                transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());

                if (financialTx != null) {
                    //function call to check whether the transaction return with "R" code is already created or not for the
                    //same MMT.
                    isTransactionReturnExists(pBankReturnCd, financialTx);
                    transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
                    transactionReturn.setCompany(financialTx.getCompany());
                }

                transactionReturn = Application.save(transactionReturn);
                processedMMTs.add(financialTx.getMoneyMovementTransaction());
                transactionReturns.add(transactionReturn);
                hasExecutedTxns = true;
            }
        }

        if (!hasExecutedTxns) {
            throw new RuntimeException("There are no executed or completed transactions for the specified " +
                                               "transaction type " + pTransactionType + "for the payroll " + pSourceBatchId);
        }

        return transactionReturns;
    }

    private BankReturnWSDTO buildBankReturnWSDTO(TransactionReturn pTxnReturn)
            throws Exception {
        // Refresh the TransactionReturn
        pTxnReturn = Application.findById(TransactionReturn.class, pTxnReturn.getId());
        DomainEntitySet<FinancialTransaction> finTxnList = TransactionReturn.
                                                                                    findNonOverPaymentFinancialTransacttion(pTxnReturn);

        FinancialTransaction financialTransaction = finTxnList.get(0);

        BankAccount nonIntuitBankAccount = getNonIntuitBankAccountType(financialTransaction);
        BankReturnWSDTO bankReturnWSDTO = new BankReturnWSDTO();
        bankReturnWSDTO.id = pTxnReturn.getId().toString();
        bankReturnWSDTO.transactionId = financialTransaction.getId().toString();
        bankReturnWSDTO.bankReturnCd = pTxnReturn.getBankReturnCd();
        bankReturnWSDTO.description = pTxnReturn.getBankReturnDescription();
        bankReturnWSDTO.returnStatus = pTxnReturn.getReturnStatusCd().toString();
        bankReturnWSDTO.traceNumber = Long.toString(pTxnReturn.getBankReturnTraceNumber());
        bankReturnWSDTO.statusChangeDate = CalendarUtils.getDateWithoutSeconds(pTxnReturn.getReturnStatusEffectiveDate().toLocal());
        bankReturnWSDTO.accountNumber = nonIntuitBankAccount.getAccountNumber();
        bankReturnWSDTO.accountType = nonIntuitBankAccount.getAccountTypeCd().toString();
        bankReturnWSDTO.routingNumber = nonIntuitBankAccount.getRoutingNumber();

        TransactionTypeCode txTypeCode = financialTransaction.getTransactionType().getTransactionTypeCd();

        if (TransactionTypeCode.EmployeeDdCredit.equals(txTypeCode) ||
                TransactionTypeCode.EmployeeDdReversalDebit.equals(txTypeCode) ||
                TransactionTypeCode.EmployeeEscalationCredit.equals(txTypeCode)) {
            if (null != financialTransaction.getPaycheckSplit()) {
                Employee employee = financialTransaction.getPaycheckSplit().getPaycheck().getDDEmployee();
                bankReturnWSDTO.sourceEmployeeId = employee.getSourceEmployeeId();
                bankReturnWSDTO.employeeDisplayName = employee.getFirstName() + employee.getLastName();
            }

        }
        return bankReturnWSDTO;
    }

    private BankAccount getNonIntuitBankAccountType(FinancialTransaction pFinancialTransaction) {
        if (pFinancialTransaction.getCreditBankAccountType() != BankAccountOwnerType.Intuit) {
            return pFinancialTransaction.getCreditBankAccount();
        } else {
            return pFinancialTransaction.getDebitBankAccount();
        }
    }

    private NACHAValidationWSDTO validateFileAndTraceNumbers(String pExpectedFileContent, String pCreatedFileName,
                                                             NACHAFileType pFileType) throws Exception {
        NACHAValidationWSDTO nachaValidationWSDTO = new NACHAValidationWSDTO();
        BufferedReader compareReader = new BufferedReader(new FileReader(pCreatedFileName));

        ACHCompare achCompare = new ACHCompare();
        CompareResults results = achCompare.compareACH(pExpectedFileContent, compareReader, pFileType);

        nachaValidationWSDTO.fileComparision = results.getStatus();
        nachaValidationWSDTO.filevalidationErrors = Arrays.asList(results.getReasons());
        nachaValidationWSDTO.fileType = pFileType.toString();
        HashMap<String, String> recordsTraceNums = achCompare.getRecordTraceNumMap();
        HashMap<String, String> recordPsIds = achCompare.getPsIds();
        HashMap<String, String> einPsIds = achCompare.getPsIdsWithEINs();

        //todo:v2 determine how to test accumulated trace numbers and ensure trace numbers are ascending
        for (String currRecord : recordsTraceNums.keySet()) {
            String currTraceNum = recordsTraceNums.get(currRecord);
            EntryDetailRecord entryDetailRecord = EntryDetailRecord.findEntryDetailRecordsWithTraceNumber(Long.parseLong(currTraceNum));
            if (entryDetailRecord != null) {
                boolean foundAMatch = false;
                Long expectedTraceNumber = Long.parseLong(currTraceNum);
                Long actualTraceNumber = null;

                if (entryDetailRecord.getTraceNumber() != null) {
                    actualTraceNumber = Long.parseLong(entryDetailRecord.getTraceNumber());
                    if (actualTraceNumber.equals(expectedTraceNumber)) {
                        foundAMatch = true;
                    }
                }
                nachaValidationWSDTO.traceNumberComparision = foundAMatch;
            } else {  // for intuit txns, there is neither record data or a trace number
                nachaValidationWSDTO.traceNumberComparision = true;
            }
        }

        //ensure trace numbers are ascending and not duplicated in the file
        Long previousTraceNumber = 0L;
        ArrayList<String> foundTraceNumbers = new ArrayList<String>();

        for (String traceNumber : achCompare.getTraceNumbers()) {
            Long longCurrTraceNum = new Long(traceNumber);
            if (previousTraceNumber.compareTo(longCurrTraceNum) > 0) {
                nachaValidationWSDTO.traceNumberComparision = false;
            }
            previousTraceNumber = longCurrTraceNum;
            if (foundTraceNumbers.contains(traceNumber)) {
                nachaValidationWSDTO.traceNumberComparision = false;
            }
            foundTraceNumbers.add(traceNumber);
        }

        if ((nachaValidationWSDTO.fileComparision) && (nachaValidationWSDTO.traceNumberComparision)) {
            nachaValidationWSDTO.overallValidation = true;
        }
        nachaValidationWSDTO.psIdComparision = true;
        nachaValidationWSDTO.psIdEinComparision = true;

        if(pFileType.equals(NACHAFileType.CCDPlus)) {
            try {
                Application.beginUnitOfWork();
                //Validate Psids
                for (String currRecord : recordsTraceNums.keySet()) {
                    String traceNum = recordsTraceNums.get(currRecord);
                    EntryDetailRecord entryDetailRecord = EntryDetailRecord.findEntryDetailRecordsWithTraceNumber(Long.parseLong(traceNum));
                    if (entryDetailRecord != null) {
                        boolean foundPsidMatch = false;
                        String sourceCompanyId = entryDetailRecord.getCompany().getSourceCompanyId();
                        if(recordPsIds.containsValue(sourceCompanyId))
                            foundPsidMatch = true;

                        //If Psid match is not found for one record, total File compare is failed.
                        if (!foundPsidMatch) {
                            nachaValidationWSDTO.psIdComparision = foundPsidMatch;
                        }
                    }
                }
                for (String ein : einPsIds.keySet()) {
                    boolean foundPsidEinMatch = false;
                    String psid = einPsIds.get(ein);
                    ein = ein.substring(1);
                    Company company = Company.findActiveCompany(SourceSystemCode.QBDT, ein);
                    if(company != null && psid.length() == 20 && psid.trim().equals(company.getSourceCompanyId())) {
                        foundPsidEinMatch = true;
                    }
                    //If Psid Ein match is not found for one record, total File compare is failed.
                    if (!foundPsidEinMatch) {
                        nachaValidationWSDTO.psIdEinComparision = foundPsidEinMatch;
                    }
                }
                if (!nachaValidationWSDTO.psIdComparision || !nachaValidationWSDTO.psIdEinComparision) {
                    nachaValidationWSDTO.overallValidation = false;
                }
                Application.rollbackUnitOfWork();
            } finally {
                Application.rollbackUnitOfWork();
            }
        }

        return nachaValidationWSDTO;
    }

    @WebMethod
    public void createBankReturnsForMoneyMovementTransactions(@WebParam(name = "bankReturnDTOs") List<BankReturnWSDTO> bankReturnDTOs)
            throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (bankReturnDTOs == null || bankReturnDTOs.size() <= 0) {
            return;
        }

        try {
            // need auto-flush enabled to handle returns for different payrolls for same company (PSRV001219)
            Application.setDefaultHibernateFlushMode(FlushMode.AUTO);

            // create TransactionReturnBatch and TransactionReturns
            PayrollServices.beginUnitOfWork();

            TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
            transactionReturnBatch.setACHReturnFileName(null);
            transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
            transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturnBatch = Application.save(transactionReturnBatch);
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);

            Collection<TransactionReturn> transactionReturnList = buildTransactionReturnListForMMT(transactionReturnBatch, bankReturnDTOs);
            transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
            PayrollServices.commitUnitOfWork();

            // Process TransactionReturns associated with the TransactionReturnBatch
            ReturnFileParser returnsProcessor = new ReturnFileParser();
            returnsProcessor.processTransactionReturns(transactionReturnBatch.getId());

        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            throw e;
        } finally {
            PayrollServices.rollbackUnitOfWork();

            // clear default preference so threads from thread pool won't retain this for other work.
            Application.setDefaultHibernateFlushMode(null);
        }
    }

    private DomainEntitySet<TransactionReturn> buildTransactionReturnListForMMT(TransactionReturnBatch pTransactionReturnBatch,
                                                                                List<BankReturnWSDTO> bankReturnDTOs) {
        DomainEntitySet<TransactionReturn> transactionRetruns = new DomainEntitySet<TransactionReturn>();
        TransactionReturn transactionReturn = null;
        MoneyMovementTransaction mmTxn = null;
        String nachaCode = null;
        String achMsg = null;
        for (BankReturnWSDTO bankReturnWSDTO : bankReturnDTOs) {
            transactionReturn = new TransactionReturn();
            nachaCode = bankReturnWSDTO.bankReturnCd;
            transactionReturn.setBankReturnCd(nachaCode);
            achMsg = nachaCode + "00000000000000000000000000000DESCRIPTION";

            // pad account number
            if(bankReturnWSDTO.accountNumber != null) {
                bankReturnWSDTO.accountNumber = StringUtils.rightPad(bankReturnWSDTO.accountNumber, 17, " ");
            }

            // convert S, C, G, and L into transaction numbers
            if(bankReturnWSDTO.accountType != null) {
                if(bankReturnWSDTO.accountType.equals("C")) {
                    bankReturnWSDTO.accountType = "21";
                } else if(bankReturnWSDTO.accountType.equals("S")) {
                    bankReturnWSDTO.accountType = "31";
                } else if(bankReturnWSDTO.accountType.equals("G")) {
                    bankReturnWSDTO.accountType = "41";
                } else if(bankReturnWSDTO.accountType.equals("L")) {
                    bankReturnWSDTO.accountType = "51";
                }
            }

            if (nachaCode.equals("C01")) {
                achMsg = bankReturnWSDTO.accountNumber;
            } else if (nachaCode.equals("C02")) {
                achMsg = bankReturnWSDTO.routingNumber;
            } else if (nachaCode.equals("C03")) {
                achMsg = bankReturnWSDTO.routingNumber + "   " + bankReturnWSDTO.accountNumber;
            } else if (nachaCode.equals("C05")) {
                achMsg = bankReturnWSDTO.accountType;
            } else if (nachaCode.equals("C06")) {
                achMsg = bankReturnWSDTO.accountNumber + "   " + bankReturnWSDTO.accountType;
            } else if (nachaCode.equals("C07")) {
                achMsg = bankReturnWSDTO.routingNumber + bankReturnWSDTO.accountNumber + bankReturnWSDTO.accountType;
            }
            transactionReturn.setBankReturnDescription(achMsg);
            transactionReturn.setReturnBatch(pTransactionReturnBatch);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());

            try {
                mmTxn = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(bankReturnWSDTO.transactionId));
            } catch (Exception ex) {
                System.out.println("Error while getting MoneyMovement Transaction" + ex);
            }

            if (mmTxn != null) {
                transactionReturn.setMoneyMovementTransaction(mmTxn);
                transactionReturn.setCompany(mmTxn.getCompany());

                //function call to check whether the transaction return with "R" code is already created or not for the
                //same MMT.
                isTransactionReturnExists(nachaCode, mmTxn.getFinancialTransactionCollection().iterator().next());

                transactionRetruns.add(Application.save(transactionReturn));
            }
        }

        return transactionRetruns;
    }

    @WebMethod
    public void runFraudulentPayrollProcessor() throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        try {
            PayrollServices.beginUnitOfWork();
            ProcessFraudulentPayrolls fraudProcess = new ProcessFraudulentPayrolls();
            fraudProcess.processFraudulentPayrolls();
            PayrollServices.commitUnitOfWork();

        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public GEMSValidationWSDTO generateAndValidateGEMSFiles(
            @WebParam(name = "pCommandName") String pCommandName,
            @WebParam(name = "pProcessingDate") String pProcessingDate,
            @WebParam(name = "pGemsUploadBatchId") String pGemsUploadBatchId,
            @WebParam(name = "expectedFileContents") InputFileContentDTO expectedFileContent)
            throws Exception {

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

        GEMSValidationWSDTO gemsValidationDTO = null;

        try {
            PayrollServices.beginUnitOfWork();
            GemsUploadBatch batch = null;
            if (expectedFileContent.fileType.equals("Daily")) {
                DailyGemsUploadBatchProcess process = new DailyGemsUploadBatchProcess();
                process.createFile(pGemsUploadBatchId,PSPDate.getPSPTime() );
                batch = process.getUploadBatch();
            } else if (expectedFileContent.fileType.equals("Monthly")) {
                MonthlyGemsUploadBatchProcess process = new MonthlyGemsUploadBatchProcess();
                if (pCommandName.equals("gen") || pCommandName.equals("regen")) {
                    process.process(pCommandName, pProcessingDate, pGemsUploadBatchId);
                } else {
                    throw new RuntimeException("Command name must be either gen or regen");
                }
                batch = process.getUploadBatch();
                process.process("file", pProcessingDate, Integer.toString(batch.getBatchId()));

            }

            PayrollServices.commitUnitOfWork();

            gemsValidationDTO = validateGemsUploadFile(expectedFileContent.fileType, expectedFileContent.expectedFileContent, batch.getFileName());
            gemsValidationDTO.batchID = Integer.toString(batch.getBatchId());

        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            throw ex;
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return gemsValidationDTO;
    }

    /**
     * Function to validate the created file with the expected file
     *
     * @param pExpectedFileContent Expected File Content
     * @param pCreatedFileName     Created File Name
     */
    private GEMSValidationWSDTO validateGemsUploadFile(String fileType, String pExpectedFileContent,
                                                       String pCreatedFileName) {

        GEMSValidationWSDTO gemsValidationDTO = new GEMSValidationWSDTO();
        try {

            BufferedReader compareReader = new BufferedReader(new FileReader(pCreatedFileName));

            GemsCompare compare = new GemsCompare();
            CompareResults compareResults;

            if (ReportingFrequency.Daily.toString().equals(fileType)) {
                compareResults = compare.compareGemsUploadFile(pExpectedFileContent,
                                                               compareReader, ReportingFrequency.Daily);
            } else {

                compareResults = compare.compareGemsMonthlyFile(pExpectedFileContent,
                                                                compareReader, ReportingFrequency.Monthly);
            }

            gemsValidationDTO.fileComparision = compareResults.getStatus();

            if (!compareResults.getStatus()) {
                gemsValidationDTO.filevalidationErrors = Arrays.asList(compareResults.getReasons());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return gemsValidationDTO;
    }

    /**
     * Function to check whether the transaction return exists for the given MMT with Bank Retrun Code like 'R%'
     *
     * @param nachaCode
     * @param pFinancialTransaction
     */
    private void isTransactionReturnExists(String nachaCode, FinancialTransaction pFinancialTransaction) {
        if (nachaCode.substring(0, 1).equals("R")) {
            DomainEntitySet<TransactionReturn> txnReturnList = TransactionReturn.
                                                                                        findTransactionReturnsByReturnCodeAndMMT(pFinancialTransaction.getMoneyMovementTransaction(), nachaCode.substring(0, 1));

            if (txnReturnList.size() > 0) {
                throw new RuntimeException("Financial Transaction " + pFinancialTransaction.getId() + "is Returned Twice with " + nachaCode.substring(0, 1) + " Code");
            }
        }
    }

    @WebMethod
    public void runEftpsEnrollmentBatchProcessor() throws Exception {
        //        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        //        try {
        //            PayrollServices.beginUnitOfWork();
        //            EftpsEnrollmentBatchProcess process = new EftpsEnrollmentBatchProcess();
        //            process.execute();
        //            PayrollServices.commitUnitOfWork();
        //
        //        } catch (Exception ex) {
        //            PayrollServices.rollbackUnitOfWork();
        //            throw ex;
        //        } finally {
        //            PayrollServices.rollbackUnitOfWork();
        //        }
    }

    @WebMethod
    public void runTaxPaymentSubmissionProcessor() throws Exception {
        // KP: Removing EFE and old tax payment dependencies
        //        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        //        TaxPaymentSubmission process = new TaxPaymentSubmission(); // handles its own transactions
        //        process.execute(PSPDate.getPSPTime());
    }

    @WebMethod
    public void runTaxPaymentSynchronizationProcessor() throws Exception {
        // KP: Removing EFE and old tax payment dependencies
        //        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        //        TaxPaymentSynchronization process = new TaxPaymentSynchronization(); // handles its own transactions
        //        process.execute();
    }

    @WebMethod
    public void populateIopMockGateway(ArrayList<PayrollCompanyModel> pPayrollCompanyModelList) {
        if (pPayrollCompanyModelList != null) {
            logger.info("Input list size = " + pPayrollCompanyModelList.size());
            IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
            for (PayrollCompanyModel payrollCompanyModel : pPayrollCompanyModelList) {
                Long id = payrollCompanyModel.getCompany().getId();
                MockIOPGateway.addPayrollCompanyModel(id.intValue(), payrollCompanyModel);
                logger.info("Added company = " + id);
            }
        }
    }

    @WebMethod
    public void populateIopMockGatewayWithContractorPayments(ArrayList<ContractorPaymentCompanyModel> pContractorPaymentCompanyModelList) {
        if (pContractorPaymentCompanyModelList != null) {
            logger.info("Input list size = " + pContractorPaymentCompanyModelList.size());
            IOPGatewayFactory.setInstanceClass(MockIOPGateway.class);
            for (ContractorPaymentCompanyModel contractorPaymentCompanyModel : pContractorPaymentCompanyModelList) {
                Long id = contractorPaymentCompanyModel.getCompany().getId();
                MockIOPGateway.addContractorPaymentCompanyModel(id.intValue(), contractorPaymentCompanyModel);
                logger.info("Added company = " + id);
            }
        }
    }

    @WebMethod
    public void runIopSyncProcessor(String tokenDate) throws Exception {
        try {
            PayrollServices.beginUnitOfWork();
            Long token = CalendarUtils.createInstanceFromDateTime(tokenDate).toLocal().getTimeInMilliseconds();
            try {
                PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.IOP_SYNC_TOKEN, String.valueOf(token));
            } catch (Exception e) {
                SystemParameter systemParameter = new SystemParameter();
                systemParameter.setSystemParameterCd(SystemParameter.Code.IOP_SYNC_TOKEN.toString());
                systemParameter.setSystemParameterDescription("Token used to sync data from IOP to PSP");
                systemParameter.setSystemParameterOrg("PSP");
                systemParameter.setSystemParameterValue(token.toString());
                Application.save(systemParameter);
            }
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            SyncIOPData syncIOPData = new SyncIOPData();
            syncIOPData.process();
            PayrollServices.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public String fetchMostRecentPositivePayFile() throws Exception {
        return fetchMostRecentReconPlusFile(AccountingReportFileType.PositivePay.toString());
    }

    @WebMethod
    public String getCheckStatus() throws Exception {
        StringBuffer batchStatus = new StringBuffer();
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
            PayrollServices.beginUnitOfWork();
            /*  Get all the batches */
            DomainEntitySet<AgencyCheckBatch> batches = Application.find(AgencyCheckBatch.class);
            if (batches == null) {
                throw new RuntimeException("No batches found.");
            }
            String newLine = System.getProperty("line.separator");
            for (AgencyCheckBatch batch : batches) {
                DomainEntitySet<PaymentBatchAssoc> paymentBatchAssociations = batch.getPaymentBatchAssocCollection();
                String positivePayFilePath = (batch.getPositivePayFile() == null || batch.getPositivePayFile().getFileName() == null) ? "N/A" : batch.getPositivePayFile().getStatus().toString();
                String positivePayFileStatus = (batch.getPositivePayFile() == null || batch.getPositivePayFile().getFileName() == null) ? "N/A" : batch.getPositivePayFile().getFileName().toString();
                String reconPlusFilePath = (batch.getReconPlusFile() == null || batch.getReconPlusFile().getFileName() == null) ? "N/A" : batch.getReconPlusFile().getStatus().toString();
                String reconPlusFileStatus = (batch.getReconPlusFile() == null || batch.getReconPlusFile().getFileName() == null) ? "N/A" : batch.getReconPlusFile().getFileName().toString();
                batchStatus.append(String.format("%sBatch Sent to printer on: %s%sPositivePay File - Status: %s,\tFilename: %s", newLine, (batch.getSentToPrinter() == null ? "Not Yet" : batch.getSentToPrinter()), newLine, positivePayFileStatus, positivePayFilePath));
                batchStatus.append(String.format("%sReconPlus File - Status: %s,\tFilename: %s", newLine, positivePayFileStatus, positivePayFilePath));
                //                for (PaymentBatchAssoc paymentBatchAssociation : paymentBatchAssociations) {
                //                    if (paymentBatchAssociation.getMoneyMovementTransaction().getCompany().equals(company)) {
                //                        for (PayrollRun payrollRun : paymentBatchAssociation.getMoneyMovementTransaction().getPayrollRuns()) {
                //                            if (payrollRun.getPaycheckDate().toLocal().equals(CalendarUtils.convertToSpcfCalendar(pCheckDate))) {
                //                                /*  This batch is for the payroll for this particular company and this paycheck date    */
                //                                for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
                //                                    batchStatus.append(String.format("%sPaycheck Id: %s\tAmount: %s",newLine,paycheck.getSourcePaycheckId(),paycheck.getPaycheckTotalAmount().toString()));
                //                                }
                //                            }
                //                        }
                //                    }
                //                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting check batch status: " + e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return batchStatus.toString();
    }

    @WebMethod
    public String fetchMostRecentReconPlusFile(@WebParam(name = "FileType") String pFileType) throws Exception {
        try {
            PayrollServices.beginUnitOfWork();

            Criterion<AccountingReportFile> fileCriterion = AccountingReportFile.FileName().isNotNull();
            if (pFileType != null) {
                fileCriterion = fileCriterion.And(AccountingReportFile.Type().equalTo(AccountingReportFileType.valueOf(pFileType)));
            }

            DomainEntitySet<AccountingReportFile> accountingReportFiles = Application.find(AccountingReportFile.class, fileCriterion);
            if (accountingReportFiles.size() == 0) {
                throw new RuntimeException("No recon plus files of type " + pFileType + " found");
            }
            accountingReportFiles.sort(AccountingReportFile.<AccountingReportFile>ModifiedDate().Descending());
            String fileName = accountingReportFiles.get(0).getFileName();
            fileName = fileName.replace(BatchUtils.getConfigString("psp_batch_ftp_send_dir"), BatchUtils.getConfigString("psp_batch_ftp_arcv_dir"));
            File file = new File(fileName);

            StringBuilder fileContents = new StringBuilder();
            String newLine = System.getProperty("line.separator");
            FileInputStream iStreamFile = new FileInputStream(file);
            InputStreamReader iStreamReader = new InputStreamReader(iStreamFile);
            BufferedReader reader = new BufferedReader(iStreamReader);

            ArrayList<String> lines = new ArrayList<String>(40);
            try {
                while (reader.ready()) {
                    lines.add(reader.readLine());
                }
            } finally {
                reader.close();
            }

            if (isReconFile(pFileType)) {
                Collections.sort(lines);
            }

            for (String line : lines) {
                fileContents.append(line).append(newLine);
            }

            return fileContents.toString();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private boolean isReconFile(String pFileType) {
        AccountingReportFileType reportFileType = AccountingReportFileType.valueOf(pFileType);
        return reportFileType == AccountingReportFileType.PrintedCheckReconPlus
                || reportFileType == AccountingReportFileType.TaxAccountsReconPlus
                || reportFileType == AccountingReportFileType.ReturnsAccountsReconPlus;
    }

    @WebMethod
    public void createZeroPayments(@WebParam(name = "checkDate") Calendar pCheckDate) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (pCheckDate == null) {
            pCheckDate = new GregorianCalendar();
        }
        try {
            PayrollServices.beginUnitOfWork();
            new ProcessZeroPayments().process(CalendarUtils.convertToSpcfCalendar(pCheckDate));
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            throw new RuntimeException("Error creating Zero Payments." + e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void eoqSUITaxAdjustments(@WebParam(name = "processingDate") Calendar pProcessingDate,
                                     @WebParam(name = "processingMessage") String pProcessingMessage) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        if (pProcessingDate == null) {
            pProcessingDate = new GregorianCalendar();
        }
        try {

            new EoqSUITaxAdjustments().process(CalendarUtils.convertToSpcfCalendar(pProcessingDate), pProcessingMessage,true);

        } catch (Exception e) {
            throw new RuntimeException("Error running eoq SUI Tax Adjustments." + e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public String createReconFiles(@WebParam(name = "paymentTemplate") String pPaymentTemplateCd, /*  Could pass-in the report name instead of payment template   */
                                   @WebParam(name = "frequency") String pFrequency,
                                   @WebParam(name = "beginDate") Calendar pBeginDate,
                                   @WebParam(name = "endDate") Calendar pEndDate) throws Exception {
        if (pBeginDate == null) {
            throw new RuntimeException("No begin date specified");
        }

        if (pEndDate == null) {
            throw new RuntimeException("No end date specified");
        }

        SpcfCalendar beginDate = CalendarUtils.convertToSpcfCalendar(pBeginDate);
        SpcfCalendar endDate = CalendarUtils.convertToSpcfCalendar(pEndDate);
        endDate.addDays(1);
        if (pPaymentTemplateCd == null || pPaymentTemplateCd.trim().length() == 0) {
            throw new RuntimeException("No paymentTemplateCode specified");
        }
        String retVal = "";
        try {
            /*  Execute the Recon batch job */
            BatchJobManager.runJob(BatchJobType.StateReport);
            /*  Then retrieve the output from the table */
            PayrollServices.beginUnitOfWork();
            DomainEntitySet<StateReportOutput> reportOutputs = Application.find(StateReportOutput.class, new Query<StateReportOutput>()
                    .Where(StateReportOutput.BeginDate().greaterOrEqualThan(beginDate)
                                            .And(StateReportOutput.EndDate().lessOrEqualThan(endDate))));

            PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(pPaymentTemplateCd, DepositFrequencyCode.valueOf(pFrequency));
            for (StateReportOutput reportOutput : reportOutputs) {
                for (StateReportAssoc stateReportAssoc : reportOutput.getStateReportAssocCollection()) {
                    if (stateReportAssoc.getPaymentTemplateFrequency().equals(paymentTemplateFrequency)) {
                        BufferedReader reader = new BufferedReader(new StringReader(reportOutput.getReportOutput()));
                        StringBuffer sb = new StringBuffer();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                            sb.append(System.getProperty("line.separator"));
                        }
                        if (pPaymentTemplateCd.startsWith("NM")) {
                            // prepare XML for web service return
                            retVal = "<![CDATA[" +
                                    sb.toString() +
                                    "]]>";
                        } else {
                            retVal = sb.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            PayrollServices.rollbackUnitOfWork();
            throw new RuntimeException("Error creating ReconFiles." + e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return retVal;
    }

    @WebMethod
    public void runBatchJob(@WebParam(name = "JobName") String pJobName,
                            @WebParam(name = "Args") Collection<String> pArgs) throws Exception {
        try {
            BatchJobType batchJobType;
            try {
                batchJobType = BatchJobType.valueOf(pJobName);
            } catch (Exception e) {
                throw new RuntimeException("Batch job " + pJobName + " does not exits");
            }

            SftpFactory.setInstanceClass(MockSimpleSftpFile.class);
            if (pArgs != null && pArgs.size() > 0) {
                BatchJobManager.runJob(batchJobType, (String[]) pArgs.toArray(new String[pArgs.size()]));
            } else {
                BatchJobManager.runJob(batchJobType);
            }
        } finally {
            SftpFactory.setInstanceClass(Transporter.class);
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public void enableMockAMOGateway(@WebParam(name = "enable") boolean enable) throws Exception {
        File messageDir = new File(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_amo_message_dir"));
        File[] files = messageDir.listFiles();
        if(files != null) {
            for (File file : files) {
                file.delete();
            }
        }

        if (enable) {
            AMOGatewayFactory.setInstanceClass(AMOMockGateway.class);
        } else {
            AMOGatewayFactory.setInstanceClass(AMOGateway.class);
        }
    }

    @WebMethod
    public void addAMOMessagesToMockGatewayQueue(@WebParam(name = "Messages") List<EntitlementMessageWSDTO> pEntitlementMessages) throws Exception {

        for (EntitlementMessageWSDTO entitlementMessage : pEntitlementMessages) {
            Message message = new Message();
            message.transactionDate = SpcfUtils.convertXmlGregorianCalendarToSpcfCalendar(entitlementMessage.TransactionDate);

            DataLoadServices.AssetItemNumber assetItemNumber = null;
            try{
                assetItemNumber = DataLoadServices.AssetItemNumber.valueOf(entitlementMessage.AssetItemNumber);
            } catch (Throwable t) {
                // ignore
            }

            com.intuit.sbd.payroll.psp.gateways.amo.Entitlement entitlement;
            if(assetItemNumber == null){
                entitlement = new com.intuit.sbd.payroll.psp.gateways.amo.Entitlement(entitlementMessage.LicenseNumber,
                                                                                      entitlementMessage.EntitlementOfferingCode,
                                                                                      entitlementMessage.OrderNumber,
                                                                                      entitlementMessage.AssetItemNumber,
                                                                                      entitlementMessage.CustomerId,
                                                                                      entitlementMessage.BillingZipCode);
            } else {
                entitlement = new com.intuit.sbd.payroll.psp.gateways.amo.Entitlement(entitlementMessage.LicenseNumber,
                                                                                      entitlementMessage.EntitlementOfferingCode,
                                                                                      entitlementMessage.OrderNumber,
                                                                                      assetItemNumber,
                                                                                      entitlementMessage.CustomerId,
                                                                                      entitlementMessage.BillingZipCode);
            }


            if(entitlementMessage.AssetStatus != null) {
                entitlement.assetStatus = AssetStatusType.valueOf(entitlementMessage.AssetStatus);
            }
            entitlement.cancellationReason = entitlementMessage.CancellationReason;
            entitlement.addressToUse = entitlementMessage.BillingAddress;
            entitlement.nextChargeDate = SpcfUtils.convertXmlGregorianCalendarToSpcfCalendar(entitlementMessage.NextChargeDate);
            entitlement.subscriptionEndDate = SpcfUtils.convertXmlGregorianCalendarToSpcfCalendar(entitlementMessage.SubscriptionEndDate);

            if (entitlementMessage.ContactEmail != null ||
                    entitlementMessage.ContactFirstName != null ||
                    entitlementMessage.ContactMiddleName != null ||
                    entitlementMessage.ContactLastName != null) {
                entitlement.addContactUpdate(entitlementMessage.ContactFirstName,
                                             entitlementMessage.ContactMiddleName,
                                             entitlementMessage.ContactLastName,
                                             entitlementMessage.ContactEmail);
            }

            if (entitlementMessage.IncludeBillingInformation != null && entitlementMessage.IncludeBillingInformation) {
                entitlement.addBillingUpdate(entitlementMessage.CCExpirationMonth,
                                             entitlementMessage.CCExpirationYear,
                                             entitlementMessage.CCNumber,
                                             entitlementMessage.CCType);
            }

            if (entitlementMessage.Edition != null) {
                entitlement.addTransactionAttributes(new TransactionAttribute(AMOMessageProcessing.editionElementName(), entitlementMessage.Edition));
            }

            if (entitlementMessage.NumberOfEmployees != null) {
                entitlement.addTransactionAttributes(new TransactionAttribute(AMOMessageProcessing.numberOfEmployeesElementName(), entitlementMessage.NumberOfEmployees));
            }

            if (entitlementMessage.EntitlementState != null) {
                entitlement.entitlementState = entitlementMessage.EntitlementState;
            }

            if (entitlementMessage.SourceLicenseNumber != null && entitlementMessage.TargetLicenseNumber != null) {
                entitlement.entitlementTransfer = new EntitlementTransfer(entitlementMessage.SourceLicenseNumber,
                                                                          entitlementMessage.TargetLicenseNumber);
            }

            if(entitlementMessage.EntitlementUnits != null) {
                for (EntitlementUnitMessageWSDTO entitlementUnitMessage : entitlementMessage.EntitlementUnits) {
                    entitlement.addEntitlementUnitUpdates(new com.intuit.sbd.payroll.psp.gateways.amo.EntitlementUnit(entitlementUnitMessage.getEin(), entitlementUnitMessage.getStatus()));
                }
            }

            if(entitlementMessage.EventReason != null) {
                message.eventReason = AssetChangeReasonType.valueOf(entitlementMessage.EventReason);
            }

            message.entitlements.add(entitlement);

            AMOMockGateway.getMessages().add(message);
        }
    }

    @WebMethod
    public void addCompanyW2CountsToTFSMockGatewayQueue(@WebParam(name = "W2CountsByCompany") List<AnnualBillingItemWSDTO> pAnnualBillingItems) throws Exception {
        Map<String, Integer> w2CountsByCompany = new HashMap<String, Integer>();

        for (AnnualBillingItemWSDTO dto : pAnnualBillingItems) {
            w2CountsByCompany.put(dto.psid, dto.formCount);
        }

        TFSMockGateway.setW2PageCountsByCompany(w2CountsByCompany);
    }

    @WebMethod
    public void enableMockTFSGateway(@WebParam(name = "enable") boolean enable) throws Exception {
        if (enable) {
            TFSMockGateway.reset();
            TFSGatewayFactory.setInstanceClass(TFSMockGateway.class);
        } else {
            TFSGatewayFactory.setInstanceClass(TFSGateway.class);
        }
    }

    @WebMethod
    public void runAMOBatchJob() throws Exception {
        AMOMessageProcessor amoMessageProcessor =
                new AMOMessageProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.AMOMessageProcessor, UUID.randomUUID().toString(), "");
        amoMessageProcessor.execute();
    }

    @WebMethod
    public void enableMockERSGateway(@WebParam(name = "enable") boolean enable) throws Exception {
        if (enable) {
            ERSGatewayFactory.setInstanceClass(ERSMockGateway.class);
        } else {
            ERSGatewayFactory.setInstanceClass(ERSGateway.class);
        }
    }

    @WebMethod
    public void runEntitlementBatchJob() throws Exception {
        EntitlementProcessor entitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");
        entitlementProcessor.execute();
    }

    @WebMethod
    public List<String> getERSActivateEntitlementUnitRequests() throws Exception {
        return marshalRequests(ERSMockGateway.getActivateEntitlementRequests());
    }

    @WebMethod
    public List<String> getERSDeactivateEntitlementUnitRequests() throws Exception {
        return marshalRequests(ERSMockGateway.getDeactivateEntitlementUnitRequests());
    }

    @WebMethod
    public List<String> getERSDisableRequests() throws Exception {
        return marshalRequests(ERSMockGateway.getCancelEntitlementsRequests());
    }

    private List<String> marshalRequests(List pRequests) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance("com.intuit.ems.payroll.psp.gateways.ers");
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        List<String> requests = new ArrayList<String>();
        for (Object o : pRequests) {
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(o, stringWriter);
            requests.add(stringWriter.toString());
        }

        pRequests.clear();

        return requests;
    }

    @WebMethod
    public void runMonthlyFeeBatchJob(@WebParam(name = "BillingPeriod") Calendar pBillingPeriod) {
        SpcfCalendar billingPeriod = (pBillingPeriod == null) ? null : CalendarUtils.convertToSpcfCalendar(pBillingPeriod);
        String[] jobArgs = (billingPeriod == null) ? new String[0] : new String[] {billingPeriod.format("yyyyMMdd")};

        BatchJobManager.runJob(BatchJobType.MonthlyFee, jobArgs);
    }

    @WebMethod
    public String getGemsMonthlyFile(@WebParam(name = "Period") String pPeriod,
                                     @WebParam(name = "forceToRegenerate") boolean pForceToRegenerate) throws Exception{
        String fileContents = null;
        try {
            if (StringUtils.isEmpty(pPeriod)) {
                throw new RuntimeException("No Period specified to generate monthly gems file");
            }

            if (!pPeriod.matches(BatchUtils.VALIDYYYYMM)) {
                throw new RuntimeException("Invalid processing Period format:" + pPeriod + ", Valid format is yyyyMM");
            }

            PayrollServices.beginUnitOfWork();
            DomainEntitySet<GemsMonthlyBalance> gemsMonthlyBalances = Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ReportingPeriod().equalTo(pPeriod));
            GemsUploadBatch gemsUploadBatch = null;
            if (gemsMonthlyBalances.size() > 0) {
                gemsUploadBatch = gemsMonthlyBalances.getFirst().getGemsUploadBatch();
            }
            int batchId = 0;
            if(gemsUploadBatch != null) {
                batchId = gemsUploadBatch.getBatchId();
            }
            PayrollServices.rollbackUnitOfWork();

            if (gemsUploadBatch == null) {
                BatchJobManager.runJob(BatchJobType.GemsGeneralLedger, "gen", pPeriod);
            } else if (pForceToRegenerate) {
                BatchJobManager.runJob(BatchJobType.GemsGeneralLedger, "regen", String.valueOf(batchId));
            }

            PayrollServices.beginUnitOfWork();
            gemsMonthlyBalances = Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ReportingPeriod().equalTo(pPeriod));
            if (gemsMonthlyBalances.size() > 0) {
                gemsUploadBatch = gemsMonthlyBalances.getFirst().getGemsUploadBatch();
            } else {
                return "Error in running - GemsGeneralLedger batch job";
            }

            batchId = gemsUploadBatch.getBatchId();
            PayrollServices.rollbackUnitOfWork();

            BatchJobManager.runJobStep(BatchJobType.GemsGeneralLedger, GemsGeneralLedgerProcessor.CreateGemsGeneralLedgerFile.class, "file", String.valueOf(batchId));

            PayrollServices.beginUnitOfWork();
            gemsUploadBatch = Application.find(GemsUploadBatch.class, GemsUploadBatch.BatchId().equalTo(batchId)).getFirst();


            fileContents = getGemsFileContent(gemsUploadBatch);

            PayrollServices.rollbackUnitOfWork();

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return fileContents;

    }

    @WebMethod
    public String getGemsDailyFile(@WebParam(name = "BatchId") String pBatchId) throws Exception{
        String fileContents = null;
        try {

            //Generate Gems Daily Upload File
            PayrollServices.beginUnitOfWork();
            DailyGemsUploadBatchProcess process = new DailyGemsUploadBatchProcess();
            DomainEntitySet<GemsUploadBatch> gemsUploadBatches = Application.find(GemsUploadBatch.class);
            if(StringUtils.isBlank(pBatchId) && gemsUploadBatches.isEmpty()) {
                process.createFile();
            } else {
                if (StringUtils.isBlank(pBatchId) && gemsUploadBatches.size() == 1) {
                    pBatchId = String.valueOf(gemsUploadBatches.getFirst().getBatchId());
                }
                process.createFile(pBatchId,PSPDate.getPSPTime());
            }

            GemsUploadBatch batch = process.getUploadBatch();

            fileContents = getGemsFileContent(batch);
            PayrollServices.commitUnitOfWork();

        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return fileContents;

    }

    @WebMethod
    public void resetGemsBatchIdSequence() throws Exception {
        try {
            Application.beginUnitOfWork();

            Statement statement = Application.getConnection().createStatement();
            ResultSet resultSet = null;
            try {
                long batchIdSequenceValue = 0;

                String environmentId = ConfigurationManager.getEnvironmentIdentifier();
                String ownerName = "pspadm";
                if("local".equals(environmentId)) {
                    ownerName = "psp_local";
                }

                batchIdSequenceValue = Application.nextSequenceValue(SequenceId.SEQ_GEMS_UPLOAD_BATCH_ID, Long.class);
                batchIdSequenceValue = batchIdSequenceValue * -1 + 1;

                statement.execute("alter sequence "+ownerName+".SEQ_GEMS_UPLOAD_BATCH_ID increment by "+ batchIdSequenceValue +" minvalue 1");
                Application.nextSequenceValue(SequenceId.SEQ_GEMS_UPLOAD_BATCH_ID, Long.class);
                statement.execute("alter sequence "+ownerName+".SEQ_GEMS_UPLOAD_BATCH_ID increment by 1 minvalue 1");

            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
                statement.close();
            }

            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    private String getGemsFileContent(GemsUploadBatch gemsUploadBatch) throws IOException {
        StringBuilder fileContents = new StringBuilder();

        if(gemsUploadBatch == null || StringUtils.isEmpty(gemsUploadBatch.getFileName())) {
            return "Error in creating - gems file";
        }

        File file = new File(gemsUploadBatch.getFileName());
        FileInputStream iStreamFile = new FileInputStream(file);
        InputStreamReader iStreamReader = new InputStreamReader(iStreamFile);
        BufferedReader reader = new BufferedReader(iStreamReader);

        try {
            while (reader.ready()) {
                fileContents.append(reader.readLine());
                fileContents.append(System.getProperty("line.separator"));
            }
        } finally {
            reader.close();
        }
        return fileContents.toString();
    }

    @WebMethod
    public String submitFsetReturnsFiling(@WebParam(name = "sourceSystemCD") String pSourceSystemCd,
                                          @WebParam(name = "sourceCompanyID") String pSourceCompanyId,
                                          @WebParam(name = "dueDate") String pDueDate) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TestAdapter);


        PayrollServices.beginUnitOfWork();

        Company company = null;
        if(StringUtils.isNotBlank(pSourceSystemCd) && StringUtils.isNotBlank(pSourceCompanyId)) {
            company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

            if(company == null) {
                throw new RuntimeException("No company is found with PS Id:" + pSourceCompanyId +" Source system code:"+ pSourceSystemCd);
            }
        }

        SpcfCalendar dueDate = null;

        if(StringUtils.isNotBlank(pDueDate)){
            if(pDueDate.length() != 10) {
                throw new RuntimeException(
                        "Invalid from date format" + pDueDate + ".  Correct format: MM/dd/yyyy");
            } else {
                dueDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
                SpcfDateFormat dateFormat = SpcfDateFormat.createInstance();
                dateFormat.setPattern("MM/dd/yyyy");
                SpcfCalendar tempDueDate = dateFormat.parse(pDueDate);
                dueDate.setValues(tempDueDate.getYear(), tempDueDate.getMonth(), tempDueDate.getDay());
            }
        }

        //MS Payments based on Init Date, company
        PaymentMethod[] paymentMethods = {PaymentMethod.ACHCredit};
        MoneyMovementTransaction.TaxPaymentsFinder paymentsFinder = MoneyMovementTransaction.findTaxPayments().setPaymentMethods(paymentMethods).setTaxPaymentStatuses(TaxPaymentStatus.AcknowledgedByAgency)
                                                                                            .setPaymentTemplateCd("MS-M89-PAYMENT");

        if(company != null) {
            paymentsFinder = paymentsFinder.setCompany(company);
        }

        if(dueDate != null) {
            paymentsFinder = paymentsFinder.setDueDate(dueDate);
        }

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = paymentsFinder.find();

        SpcfCalendar today = PSPDate.getPSPTime();
        CalendarUtils.clearTime(today);
        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            moneyMovementTransaction.setInitiationDate(today); // Update Init date to today's date so that these will be picked by FsetFilingProcessor
        }
        MoneyMovementTransaction moneyMovementTransaction = moneyMovementTransactions.getFirst();
        PayrollServices.commitUnitOfWork();

        if(moneyMovementTransactions.isEmpty()) {
            return "No Executed Payments to process Return filings";
        }

        BatchJobManager.runJob(BatchJobType.FsetFilingProcessor);

        StringBuilder fileContents = new StringBuilder();
        try {
            PayrollServices.beginUnitOfWork();

            Application.refresh(moneyMovementTransaction);
            DomainEntitySet<FsetFilingDetail> fsetFilingDetails = Application.find(FsetFilingDetail.class, FsetFilingDetail.MoneyMovementTransaction().equalTo(moneyMovementTransaction));

            FsetFile fsetFile = fsetFilingDetails.getFirst().getParentFile();

            File file = new File(fsetFile.getFileName());
            FileInputStream iStreamFile = new FileInputStream(file);
            InputStreamReader iStreamReader = new InputStreamReader(iStreamFile);
            BufferedReader reader = new BufferedReader(iStreamReader);

            try {
                while (reader.ready()) {
                    fileContents.append(reader.readLine());
                    fileContents.append(System.getProperty("line.separator"));
                }
            } finally {
                reader.close();
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return fileContents.toString();
    }

    @WebMethod
    public String runEETotalsCalculationBatchJob(@WebParam(name = "sourceSystemCD") String pSourceSystemCd,
                                                 @WebParam(name = "sourceCompanyID") String pSourceCompanyId,
                                                 @WebParam(name = "yearQuarter") String pYearQuarter, @WebParam(name = "mode") String pMode) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TestAdapter);

        try {
            StringBuilder batchJobParams = new StringBuilder();
            PayrollServices.beginUnitOfWork();

            if(StringUtils.isNotBlank(pSourceSystemCd) && StringUtils.isNotBlank(pSourceCompanyId)) {
                Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

                if(company == null) {
                    throw new RuntimeException("No company is found with PS Id:" + pSourceCompanyId +" Source system code:"+ pSourceSystemCd);
                }
                batchJobParams.append("-compSeqPattern=").append(company.getId().toString()).append(" ");
            }
            PayrollServices.rollbackUnitOfWork();

            if(StringUtils.isNotBlank(pYearQuarter)) {
                batchJobParams.append("-yearQuarter=").append(pYearQuarter).append(" ");
            }

            if(StringUtils.isNotBlank(pMode)) {
                batchJobParams.append("-mode=").append(pMode);
            }

            BatchJobManager.runJob(BatchJobType.EmployeeTotalsCalculationProcess, batchJobParams.toString());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
        return "Successfully ran EE Totals calculation batch job!!";
    }

    @WebMethod
    public String runEETotalsAnnualCalculationBatchJob(@WebParam(name = "year") int pYear,
                                                       @WebParam(name = "sourceCompanyID") String pSourceCompanyId) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TestAdapter);

        BatchJobManager.runJob(BatchJobType.EmployeeW2TotalsCalculationProcessor, "-year:"+Integer.toString(pYear), "-companyId:"+pSourceCompanyId);

        return "Successfully completed EE Annual Totals calculation batch job!!";
    }

    @WebMethod
    public void runEnrollmentDeleteSelection() {
        BatchJobManager.runJob(BatchJobType.EnrollmentDeleteSelectionProcessor);
    }

    @WebMethod
    public String runEEPayrollItemsCalculationBatchJob(@WebParam(name = "sourceSystemCD") String pSourceSystemCd,
                                                       @WebParam(name = "sourceCompanyID") String pSourceCompanyId,
                                                       @WebParam(name = "yearQuarter") String pYearQuarter, @WebParam(name = "year") String pYear) throws Exception {
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TestAdapter);

        try {
            StringBuilder batchJobParams = new StringBuilder();
            PayrollServices.beginUnitOfWork();

            if(StringUtils.isNotBlank(pSourceSystemCd) && StringUtils.isNotBlank(pSourceCompanyId)) {
                Company company = Company.findCompany(pSourceCompanyId, SourceSystemCode.valueOf(pSourceSystemCd));

                if(company == null) {
                    throw new RuntimeException("No company is found with PS Id:" + pSourceCompanyId +" Source system code:"+ pSourceSystemCd);
                }
                batchJobParams.append("-compSeq:").append(company.getId().toString()).append(" ");
            }
            PayrollServices.rollbackUnitOfWork();

            if(StringUtils.isNotBlank(pYearQuarter)) {
                batchJobParams.append("-yearQuarter:").append(pYearQuarter).append(" ");
            }

            if(StringUtils.isNotBlank(pYear)) {
                batchJobParams.append("-year:").append(pYear);
            }

            BatchJobManager.runJob(BatchJobType.EmployeePayrollItemTotalsCalcProcess, batchJobParams.toString());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        return "Successfully ran EE Payroll Items calculation batch job!!";
    }

    @WebMethod
    public String runningStatusFromDbForBatchJob(@WebParam(name = "jobtype") String pJobType) throws Exception {
        if ((pJobType == null) || (pJobType.length() == 0)) {
            throw new RuntimeException("Job Name cannot be null or empty.");
        }

            BatchJobType jobType = BatchJobType.valueOf(pJobType);

            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

            return BatchJobController.canRunBatchJob(jobType) ? "Not Running" : "Running";
    }

    @WebMethod
    public void updatingStatusOfDbForBatchJob(@WebParam(name = "jobtype") String pJobType, @WebParam(name = "setStatus") boolean status) throws Exception {
        if ((pJobType == null) || (pJobType.length() == 0)) {
            throw new RuntimeException("Job Name cannot be null or empty.");
        }

        BatchJobType jobType = BatchJobType.valueOf(pJobType);

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));

        PayrollServices.beginUnitOfWork();

        Expression<BatchJobStatus> batchJobStatusQuery =
                new Query<BatchJobStatus>()
                        .Where(BatchJobStatus.JobType().equalTo(jobType));
        DomainEntitySet<BatchJobStatus> batchJobStatusSet = Application.find(BatchJobStatus.class, batchJobStatusQuery);
        if(CollectionUtils.isNotEmpty(batchJobStatusSet)){
            BatchJobStatus batchJobStatus = batchJobStatusSet.getFirst();
            if(status == true)
            batchJobStatus.setIsRunning(Boolean.TRUE);
            else
                batchJobStatus.setIsRunning(Boolean.FALSE);
            Application.save(batchJobStatus);
        }
        PayrollServices.commitUnitOfWork();
    }

    @WebMethod
    public String findNachAndOffloadBatchAndUpdateStatus(@WebParam(name = "offloadDate") String pOffloadDate) throws Exception {
        PayrollServices.beginUnitOfWork();
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        SpcfCalendar offloadDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT,pOffloadDate);
        SpcfCalendar offloadDate1 = SpcfCalendar.parse(BatchUtils.DATE_FORMAT,pOffloadDate);
        SpcfCalendar statusEffectiveDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT,pOffloadDate);
        offloadDate1.addDays(1);
        statusEffectiveDate.addDays(-2);
        List<NACHAFile> nachaList = new ArrayList<>();
        try {
            Expression<OffloadBatch> query = (new Query()).Where(OffloadBatch.OffloadDate().between(offloadDate, offloadDate1));

            DomainEntitySet<OffloadBatch> batchList = Application.find(OffloadBatch.class, query);

            if (batchList.isEmpty()) {
                return null;
            } else {
                Iterator offLoadBatchList = batchList.iterator();
                while (offLoadBatchList.hasNext()) {
                    OffloadBatch batch = (OffloadBatch) offLoadBatchList.next();
                    DomainEntitySet<NACHAFile> nachaFileList = batch.getNACHAFileCollection();
                    Iterator nachaFileIterator = nachaFileList.iterator();
                    while (nachaFileIterator.hasNext()) {
                        NACHAFile nachaFile = (NACHAFile) nachaFileIterator.next();
                        if ((nachaFile != null) && (nachaFile.getFileType().equals(NACHAFileType.PPD) || nachaFile.getFileType().equals(NACHAFileType.CCD))) {
                            nachaFile.setStatus(NACHAFileStatus.InProcess);
                            nachaFile.setStatusEffectiveDate(statusEffectiveDate);
                            nachaList.add(nachaFile);
                            Application.save(nachaFile);
                        }
                    }
                    batch.setStatusCd(OffloadBatchStatus.Completed);
                    batch.setStatusEffeciveDate(statusEffectiveDate);
                }
                batchList.get(0).setStatusCd(OffloadBatchStatus.InProcess);
            }
            PayrollServices.commitUnitOfWork();
        }
        catch(Exception e){
            logger.error("Error: ",e);
            throw e;
        } finally{
            PayrollServices.rollbackUnitOfWork();
        }
        return nachaList.isEmpty() ? "No NACHA Files are present." : "Successfully changed the status of NachaFile";
    }

    @WebMethod
    public String checkDICRFilesStatusForProcessed() throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        DomainEntitySet<DICRFile> dicrFiles = null;
        try {
            PayrollServices.beginUnitOfWork();
            Expression<DICRFile> query = new Query<DICRFile>().Where(DICRFile.Status().notIn(DICRFileStatus.Archived));
            dicrFiles = Application.find(DICRFile.class, query);
            Iterator dicrFilesList = dicrFiles.iterator();

            while (dicrFilesList.hasNext()) {
                DICRFile dicrFile = (DICRFile) dicrFilesList.next();
                dicrFile.setStatus(DICRFileStatus.Archived);
                Application.save(dicrFile);
            }
            PayrollServices.commitUnitOfWork();
        }catch(Exception e){
            logger.error("Error: ",e);
            throw e;
        }finally{
            PayrollServices.rollbackUnitOfWork();
        }
        return dicrFiles.isEmpty() ? "No DICR Files are present." : "Successfully changed the status of DICRFile.";
    }

    @WebMethod
    public void checkNACHAFilesStatusNullOrArchivedAndUpdate() throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        DomainEntitySet<NACHAFile> nachaFiles = null;
        try {
            PayrollServices.beginUnitOfWork();
            Expression<NACHAFile> query = new Query<NACHAFile>().Where(NACHAFile.Status().in(NACHAFileStatus.Finalized));
            nachaFiles = Application.find(NACHAFile.class, query);
            Iterator nachaFilesList = nachaFiles.iterator();

            while (nachaFilesList.hasNext()) {
                NACHAFile nachaFile = (NACHAFile) nachaFilesList.next();
                if(nachaFile.getFileName() == null) {
                    nachaFile.setStatus(NACHAFileStatus.Archived);
                    Application.save(nachaFile);
                }
            }
            PayrollServices.commitUnitOfWork();
        }catch(Exception e){
            logger.error("Error: ",e);
            throw e;
        }finally{
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @WebMethod
    public String uploadReturnFile(@WebParam(name = "traceNumber") String traceNumber) throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.TestAdapter));
        logger.info( "traceNumber: "+traceNumber);
        File file = updateTraceNumberInACHReturnFile(traceNumber, "achreturn/AchReturns.txt", "Intuit_AchReturns_20220801.txt");
        Transporter sftp = BatchUtils.getBankSftpConnection(new SftpAchReturnsFileDownload().getAchReturnsFileDownloadListener());
        try {
            sftp.setLogger(logger);

            sftp.connect();

            sftp.changeLocalDir(file.getParent());

            boolean enableEncryption = SystemParameter.findBooleanValue(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);
            if (enableEncryption) {
                sftp.changeRemoteDir(BatchUtils.getConfigString("psp_batch_bank_encrypted_recv_dir"));
            } else {
                sftp.changeRemoteDir(BatchUtils.getConfigString("psp_batch_bank_recv_dir"));
            }

            sftp.uploadFile(file.getName());

        } catch (Exception e) {
            logger.error("Error uploading Returns file to bank (aborting process) ");
            throw new RuntimeException("Error uploading Returns file to bank (aborting process) ", e);
        } finally {
            try {
                sftp.disconnect();
            } catch (Exception e) {
                logger.error("Error in disconnecting after uploading file(aborting process) ");
                throw new RuntimeException("Error in disconnecting after uploading file(aborting process) ", e);
            }
        }
        return "File uploaded";
    }
    private File updateTraceNumberInACHReturnFile(String pTraceNumber, String pFilePath, String pFileName){
        String achFileName =Application.findFileOnClassPath(pFilePath);
        File file = null;

        try{
            BufferedReader bufferedReader = new BufferedReader(new FileReader(achFileName));
            String currentLine = bufferedReader.readLine();

            String absolutePath = Application.findFileOnClassPath("achreturn");
            logger.info("absolute path : "+absolutePath);

            file = new File(absolutePath + "/"+pFileName);
            file.createNewFile();

            FileWriter f = new FileWriter(file);
            char entryType;
            while (currentLine != null) {
                if (currentLine.length() != 0) {
                    entryType = currentLine.charAt(0);
                    if (entryType == ADDENDA_REC_TYPE) {
                        String orgTraceNumber = currentLine.substring(6, 21);
                        currentLine = currentLine.replaceAll(orgTraceNumber,
                                StringFormatter.formatLong(Long.parseLong(pTraceNumber),15));
                        f.write(currentLine);
                    }else{
                        f.write(currentLine);
                    }
                    f.write("\n");
                }

                currentLine = bufferedReader.readLine();
            }
            f.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
}
