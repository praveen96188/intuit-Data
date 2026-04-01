package com.intuit.sbd.payroll.psp.batchjobs.processors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EffectiveDepositFrequencyDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.MailSender;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.*;
import java.util.concurrent.*;

/**
 * User: rnorian
 * Date: 11/17/11
 * Time: 8:53 PM
 */
public class IRSDepositFrequencyFileProcessor extends BatchJobProcessor {

    private SpcfCalendar processStartDate;
    private String inputFileName;

    public static SpcfLogger logger = PayrollServices.getLogger(IRSDepositFrequencyFileProcessor.class);

    public IRSDepositFrequencyFileProcessor(RunMode pRunMode, BatchJobType pBatchJobType, String pJobId, String pJobInstanceParameters) {
        super(pRunMode, pBatchJobType, pJobId, pJobInstanceParameters);
    }

    @Override
    protected void validateRuntimeParameters() {
        processStartDate = null;
        inputFileName = null;

        String commandLine = getJobInstanceParameters().trim();
        if (commandLine != null && commandLine.length() > 0) {
            String[] args = commandLine.split(" ");

            if (args.length > 0) {
                for (String arg : args) {
                    if (arg.matches(BatchUtils.VALIDYYYYMMDD)) {
                        SpcfCalendar clDate = SpcfCalendar.parse(BatchUtils.DATE_FORMAT, arg);
                        processStartDate = SpcfCalendar.createInstance(clDate.getYear(), clDate.getMonth(), clDate.getDay(),
                                0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
                    } else {
                        inputFileName = arg;
                    }
                }
            }
        }
    }

    @Override
    protected void execute() {
        logger.info("Starting " + getClass().getSimpleName() + " processor");
        StopWatch timer = StopWatch.startTimer();

        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.IRSDepositFrequencyBatchJob));

        //Default flux job runs without these parameters. These parameters are passed if we want re-process already persisted file, so first step is not required
        if (inputFileName == null && processStartDate == null) {
            executeStep(new PersistDepositFrequencyFile());
            executeStep(new EmailDepositFrequencyFile());
            //executeStep(new PersistDepositFrequencyFileData());
        }

        //executeStep(new ProcessDepositFrequencyFile());
        executeStep(new ArchiveDepositFrequencyFiles());

        logger.info("Completed " + getClass().getSimpleName() + ". Elapsed time: " + timer.stop().getElapsedTimeString());

    }

    public class EmailDepositFrequencyFile extends BatchJobProcessorStep {
        @Override
        public void execute() {

            //todo this method will need to be refactored once the file is processed by PSP.
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.IRSDepositFrequencyBatchJob));

            String recipient = BatchUtils.getConfigString("psp_batch_irs_deposit_frequency_notification_list", "");
            if ((recipient == null) || (recipient.length() == 0)) {
                logger.warn("No recipient email address specified for RAF response file, skipping task.");
                return;
            }

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL, true);
            DepositFrequencyFile depositFrequencyFile = DepositFrequencyFile.findDepositFrequencyFile(DepositFrequencyFileStatus.Received, false);
            PayrollServices.rollbackUnitOfWork();

            if (depositFrequencyFile == null) {
                return;
            }

            SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
            String subject = String.format("RAF response file for date: %s", spcfCalendar.format("MM/dd/yyyy"));
            String message = "The attached RAF response file for Assisted (Tax) companies is ready for processing.";

            logger.info(message);
            MailSender.sendEmail(BatchUtils.getConfigString("psp_batch_mail_server"), // server
                                 recipient,                 // to
                                 recipient,                 // from
                                 subject,                   // subject
                                 message,                   // message body
                                 depositFrequencyFile.getFileName());   // attachments

        }
    }

    static File[] listIncomingFiles() {
        String incomingDirectoryPath = BatchUtils.getConfigString("psp_batch_irs_deposit_frequency_recv_dir");
        File incomingDirectory = new File(incomingDirectoryPath);
        if (incomingDirectory == null || !incomingDirectory.exists()) {
            throw new RuntimeException(incomingDirectoryPath + " does not exist.  Please create the directory on the BOS server.");
        }

        //File name pattern example - PDBXM.B16N26.F057.E201126.txt, File name ends with E[4 digit year][2 digit cycle number].txt
        return incomingDirectory.listFiles(
                new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.matches(".*.E20\\d{4}.txt");
                    }
                }
        );
    }

    class PersistDepositFrequencyFile extends BatchJobProcessorStep {
        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.IRSDepositFrequencyBatchJob));

            //File name ends with E[4 digit year][2 digit cycle number].txt, if more than one file is found parse only the latest file i.e. latest year and cycle number.
            File[] depositFrequencyFiles = listIncomingFiles();

            if (depositFrequencyFiles.length == 0) {
                logger.info("No IRS Deposit Frequency file to process.");
                return;
            }

            int fileCount = depositFrequencyFiles.length;
            if (fileCount > 1) {
                // Sorting file name in ascending order to pick the latest year and cycle file. (E[4 digit year][2 digit cycle number])
                Arrays.sort(depositFrequencyFiles, new Comparator<File>() {
                    public int compare(File file1, File file2) {
                        String name1;
                        String name2;

                        if(file1 == null || file2 == null) {
                            return 0;
                        }

                        name1 = file1.getName();
                        name2 = file2.getName();

                        name1 = name1.substring(name1.length() - 11);
                        name2 = name2.substring(name2.length() - 11);

                        return name1.compareTo(name2);
                    }
                });

                logger.info("Expected only 1 deposit frequency file.  Found " + String.valueOf(fileCount) +
                        " files.\n Creating File entries with Skipped status for " + String.valueOf(fileCount - 1) + " files, Processing only latest file: " + depositFrequencyFiles[fileCount - 1].getName());
            }

            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                for (int i = 0; i < depositFrequencyFiles.length; i++) {
                    if (depositFrequencyFiles.length == i + 1) {
                        createDepositFrequencyFile(depositFrequencyFiles[i], DepositFrequencyFileStatus.Received);
                    } else {
                        createDepositFrequencyFile(depositFrequencyFiles[i], DepositFrequencyFileStatus.Skipped);
                    }
                }
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                logger.error("Error when creating DepositFrequencyFile records with Skipped status for older files", t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }

        private DepositFrequencyFile createDepositFrequencyFile(File pFile, DepositFrequencyFileStatus pFileStatus) {
            DomainEntitySet<DepositFrequencyFile> frequencyFiles = Application.find(DepositFrequencyFile.class, DepositFrequencyFile.FileName().like("%"+pFile.getName()));
            if (frequencyFiles.size() > 0) {
                logger.info("Deposit Frequency File :" + pFile.getName() + " has already been persisted.");
                return null;
            }

            DepositFrequencyFile depositFrequencyFile = new DepositFrequencyFile();
            depositFrequencyFile.setFileName(pFile.getAbsolutePath());
            depositFrequencyFile.setStatus(pFileStatus);
            Application.save(depositFrequencyFile);
            return depositFrequencyFile;
        }
    }

    class PersistDepositFrequencyFileData extends BatchJobProcessorStep {
        @Override
        public void execute() {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            String line = null;
            int counter = 0;
            try {

                DepositFrequencyFile depositFrequencyFile = DepositFrequencyFile.findDepositFrequencyFile(DepositFrequencyFileStatus.Received, false);

                if (depositFrequencyFile == null) {
                    return;
                }

                //To process the records in next step that belongs to the latest parsed file.
                File file = new File(depositFrequencyFile.getFileName());
                inputFileName = file.getName();

                BufferedReader inputStream = new BufferedReader(new FileReader(file));

                boolean foundMatchingCompanies = false;
                HashMap<String, DepositFrequencyFileRec> depositFrequencyFileHashMap = new HashMap<String, DepositFrequencyFileRec>();
                while ((line = inputStream.readLine()) != null) {
                    String ein = FixedFieldDefinition.EIN.read(line);
                    counter++;
                    //If EIN is null, PSP can not process the record and File trailer record is ignored with this check.
                    if (ein != null) {
                        String legalName = FixedFieldDefinition.LEGAL_NAME.read(line);
                        String formFiled = FixedFieldDefinition.FORM_FILED.read(line);
                        String depositFrequencyIndicator = FixedFieldDefinition.DEPOSIT_FREQUENCY.read(line);
                        String currentYear = FixedFieldDefinition.CURRENT_YEAR.read(line);
                        String lastPeriodBaseCd = FixedFieldDefinition.LAST_BASE_CODE.read(line);

                        //File can have more than one record with same ein, to avoid over writing in hash map handle it separately for second ein
                        if (depositFrequencyFileHashMap.keySet().contains(ein)) {

                            DomainEntitySet<Company> companies = findCompanies(new HashSet<String>(Arrays.asList(ein)), ServiceCode.Tax);
                            DepositFrequencyFileRec record = createDepositFrequencyFileRec(depositFrequencyFile, ein, legalName, formFiled, depositFrequencyIndicator, currentYear, lastPeriodBaseCd);
                            foundMatchingCompanies = saveDepositFrequencyFileRecs(depositFrequencyFile, foundMatchingCompanies, companies, record);

                        } else {
                            depositFrequencyFileHashMap.put(ein, createDepositFrequencyFileRec(depositFrequencyFile, ein, legalName, formFiled, depositFrequencyIndicator,
                                    currentYear, lastPeriodBaseCd));
                        }

                    }
                    if (counter % 500 == 0) {
                        logger.info("Processed record count:" + counter);
                        foundMatchingCompanies = saveDepositFrequencyFileRecs(depositFrequencyFile, foundMatchingCompanies, depositFrequencyFileHashMap);
                    }
                }

                if (!depositFrequencyFileHashMap.isEmpty()) {
                    foundMatchingCompanies = saveDepositFrequencyFileRecs(depositFrequencyFile, foundMatchingCompanies, depositFrequencyFileHashMap);
                }

                //If no matching companies found for the records in whole file, all records are persisted with SkippedCompanyDoesNotExist so update the file status to Processed.
                if (!foundMatchingCompanies) {
                    depositFrequencyFile.setStatus(DepositFrequencyFileStatus.Processed);
                    Application.save(depositFrequencyFile);
                }

                logger.info(" =========== Before Committing ===========  ");
                //TODO_REVIEW - good point, this is going to commit ~60K inserts.  We may want to consider alternative batching.  Leave as it until we do performance testing.
                PayrollServices.commitUnitOfWork();
                logger.info(" =========== After Committing ============  ");
            } catch (Throwable t) {
                logger.error("Failed to parse IRS DepositFrequency File :" + inputFileName + "\n\tLine # " + counter + ": " + line, t);  // To continue processing remaining files if any
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }

        private boolean saveDepositFrequencyFileRecs(DepositFrequencyFile pDepositFrequencyFile, boolean pFoundMatchingCompanies, HashMap<String, DepositFrequencyFileRec> pDepositFrequencyFileHashMap) {
            DomainEntitySet<Company> companies = findCompanies(pDepositFrequencyFileHashMap.keySet(), ServiceCode.Tax);
            for (DepositFrequencyFileRec depositFrequencyFileRec : pDepositFrequencyFileHashMap.values()) {
                List<String> fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,depositFrequencyFileRec.getEIN());
                pFoundMatchingCompanies = saveDepositFrequencyFileRecs(pDepositFrequencyFile, pFoundMatchingCompanies, companies.find(Company.FedTaxIdEnc().in(fedTaxIdEncList)), depositFrequencyFileRec);
            }
            pDepositFrequencyFileHashMap.clear();
            return pFoundMatchingCompanies;
        }

        private boolean saveDepositFrequencyFileRecs(DepositFrequencyFile pDepositFrequencyFile, boolean pFoundMatchingCompanies, DomainEntitySet<Company> companies, DepositFrequencyFileRec depositFrequencyFileRec) {
            if (companies.isEmpty()) {
                //Persist file record with status = SkippedCompanyDoesNotExist
                depositFrequencyFileRec.setStatus(DepositFrequencyFileRecStatus.SkippedCompanyDoesNotExist);
                Application.save(depositFrequencyFileRec);
            } else {
                pFoundMatchingCompanies = true;
                //Persist file record with status = Received, for all companies found to process later
                for (Company company : companies) {
                    DepositFrequencyFileRec record = createDepositFrequencyFileRec(pDepositFrequencyFile, depositFrequencyFileRec.getEIN(),
                            depositFrequencyFileRec.getCompanyName(), depositFrequencyFileRec.getFormFiled(), depositFrequencyFileRec.getDepositFrequency(),
                            depositFrequencyFileRec.getCurrentYear(), depositFrequencyFileRec.getLastPeriodBaseCode());
                    record.setStatus(DepositFrequencyFileRecStatus.Received);
                    record.setCompany(company);
                    Application.save(record);
                }
            }
            return pFoundMatchingCompanies;
        }

        private DepositFrequencyFileRec createDepositFrequencyFileRec(DepositFrequencyFile pDepositFrequencyFile, String pEin, String pLegalName,
                                                                      String pFormFiled, String pDepositFrequencyIndicator, String pCurrentYear, String pLastPeriodBaseCd) {
            DepositFrequencyFileRec record = new DepositFrequencyFileRec();
            record.setCompanyName(pLegalName);
            record.setEIN(pEin);
            record.setFormFiled(pFormFiled);
            record.setDepositFrequency(pDepositFrequencyIndicator);
            record.setCurrentYear(pCurrentYear);
            record.setLastPeriodBaseCode(pLastPeriodBaseCd);
            record.setDepositFrequencyFile(pDepositFrequencyFile);
            return record;
        }

        public DomainEntitySet<Company> findCompanies(Set<String> pFEIN, ServiceCode pServiceCode) {
            Iterator<String> iterator = pFEIN.iterator();
            List<String> feinEncList = new ArrayList<String>();
            while(iterator.hasNext()){
                feinEncList.addAll(EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,iterator.next()));
            }
            Expression<CompanyService> query =
                    new Query<CompanyService>()
                            .Where(CompanyService.Company().FedTaxIdEnc().in(feinEncList)
                                    .And(CompanyService.Company().SourceSystemCd().equalTo(SourceSystemCode.QBDT))
                                    .And(CompanyService.Service().ServiceCd().equalTo(pServiceCode)))
                            .EagerLoad(CompanyService.Company());
            DomainEntitySet<CompanyService> companyServices = Application.find(CompanyService.class, query);

            DomainEntitySet<Company> companies = new DomainEntitySet<Company>();
            for (CompanyService companyService : companyServices) {
                companies.add(companyService.getCompany());
            }

            return companies;
        }
    }

    public class ProcessDepositFrequencyFile extends BatchJobProcessorStep {
        @Override
        public void execute() {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.IRSDepositFrequencyBatchJob));

            DomainEntitySet<DepositFrequencyFile> filesToProcess = new DomainEntitySet<DepositFrequencyFile>();

            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                Criterion<DepositFrequencyFile> criterion = DepositFrequencyFile.Status().equalTo(DepositFrequencyFileStatus.Received);

                if (processStartDate != null) {
                    criterion = criterion.And(DepositFrequencyFile.CreatedDate().greaterOrEqualThan(processStartDate));
                }

                if (inputFileName != null) {
                    criterion = criterion.And(DepositFrequencyFile.FileName().like("%"+inputFileName));
                }

                filesToProcess = Application.find(DepositFrequencyFile.class, criterion);
                if (filesToProcess.isEmpty()) {
                    logger.info("No IRS Deposit Frequency file to process.");
                    return;
                }

                if (filesToProcess.size() > 1) {
                    throw new RuntimeException("Expected only 1 deposit frequency file to Process.  Found " + filesToProcess.size() + "\n");
                }
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }

            for (DepositFrequencyFile frequencyFile : filesToProcess) {
                processUpdates(frequencyFile);
            }

        }

        public void processUpdates(DepositFrequencyFile pDepositFrequencyFile) {

            StopWatch sw = StopWatch.startTimer();

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            Expression<DepositFrequencyFileRec> query = new Query<DepositFrequencyFileRec>().Where(DepositFrequencyFileRec.DepositFrequencyFile().equalTo(pDepositFrequencyFile)
                    .And(DepositFrequencyFileRec.Company().isNotNull())
                    .And(DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.Received)
                            .Or(DepositFrequencyFileRec.Status().equalTo(DepositFrequencyFileRecStatus.Error))));
            DomainEntitySet<DepositFrequencyFileRec> frequencyUpdates = Application.find(DepositFrequencyFileRec.class, query);
            int numberRecordsToProcess = frequencyUpdates.size();
            logger.info("Found DepositFrequencyFileRec: " + numberRecordsToProcess + " to process from File: " + pDepositFrequencyFile.getFileName());
            PayrollServices.rollbackUnitOfWork();

            int processors = Runtime.getRuntime().availableProcessors();
            int threadCount = processors * 2;

            logger.info("creating Thread pool with size: " + threadCount + " to process: " + numberRecordsToProcess + " DepositFrequencyFileRec.");

            ExecutorService executor = null;
            try {
                executor = Executors.newFixedThreadPool(threadCount);

                CompletionService<ErrorInformation> completionService = new ExecutorCompletionService<ErrorInformation>(executor);

                for (final DepositFrequencyFileRec frequencyUpdate : frequencyUpdates) {
                    completionService.submit(new Callable<ErrorInformation>() {
                        public ErrorInformation call() throws Exception {
                            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.IRSDepositFrequencyBatchJob));
                            return processRecord(frequencyUpdate);
                        }
                    });
                }

                List<ErrorInformation> errors = new ArrayList<ErrorInformation>();
                for (int t = 0; t < numberRecordsToProcess; t++) {
                    try {
                        Future<ErrorInformation> f = completionService.take();
                        if (f.get() != null) {
                            errors.add(f.get());
                        }
                        if (t % 1000 == 0) {
                            logger.info("Completed " + t + " in " + sw.getElapsedTimeString());
                        }
                    } catch (Throwable throwable) {
                        logger.error("Unexpected error", throwable);
                    }
                }
                if (!errors.isEmpty()) {
                    sendAlertEmail(errors);
                }
                logger.info("Processed " + numberRecordsToProcess + " records in " + sw.getElapsedTimeString() + " -- " + errors.size() + " errors");

            } catch (Throwable t) {
                logger.error("Encountered unrecoverable error during processing", t);
            } finally {
                if (executor != null) {
                    ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);
                }
            }

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            //If no records in Error or Received status, then mark DepositFrequencyFile as Processed
            frequencyUpdates = Application.find(DepositFrequencyFileRec.class, DepositFrequencyFileRec.DepositFrequencyFile().equalTo(pDepositFrequencyFile)
                    .And(DepositFrequencyFileRec.Status().in(DepositFrequencyFileRecStatus.Error, DepositFrequencyFileRecStatus.Received)));
            if (frequencyUpdates.isEmpty()) {
                pDepositFrequencyFile = Application.findById(DepositFrequencyFile.class, pDepositFrequencyFile.getId());
                pDepositFrequencyFile.setStatus(DepositFrequencyFileStatus.Processed);
                Application.save(pDepositFrequencyFile);
            }
            PayrollServices.commitUnitOfWork();
        }

        private ErrorInformation processRecord(DepositFrequencyFileRec frequencyUpdate) {
            boolean errorInProcessing = false;
            boolean skippedUpdating = false;
            boolean invalidDataFound = false;
            ErrorInformation errorInformation = null;

            if (isValidData(frequencyUpdate)) {

                try {
                    PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                    frequencyUpdate = Application.findById(DepositFrequencyFileRec.class, frequencyUpdate.getId());
                    Company company = frequencyUpdate.getCompany();
                    try {

                        if (shouldApplyDepositFrequency(company)) {
                            EffectiveDepositFrequencyDTO depositFrequencyDTO = createDTO(frequencyUpdate);
                            logger.info("processing deposit frequency for company: " + company + " Frequency code: " + depositFrequencyDTO.getPaymentFrequencyId());
                            ProcessResult pr = PayrollServices.paymentManager.updateDepositFrequency(SourceSystemCode.QBDT, company.getSourceCompanyId(), depositFrequencyDTO);
                            if (pr.isSuccess()) {
                                frequencyUpdate.setStatus(DepositFrequencyFileRecStatus.Processed);
                                PayrollServices.commitUnitOfWork();
                            } else {
                                errorInProcessing = true;
                                errorInformation = new ErrorInformation(frequencyUpdate.getId().toString(), company.getSourceCompanyId(), pr);
                            }
                        } else {
                            skippedUpdating = true;
                            logger.info("Skipped updating Deposit Frequency for Company: " + company);
                        }
                    } catch (Throwable t) {
                        errorInProcessing = true;
                        logger.error("Failed to process deposit frequency update for company " + company, t);
                    }
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }

            } else {
                invalidDataFound = true;
            }

            if (skippedUpdating || errorInProcessing || invalidDataFound) {

                try {
                    PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
                    frequencyUpdate = Application.findById(DepositFrequencyFileRec.class, frequencyUpdate.getId());
                    if (errorInProcessing) {
                        frequencyUpdate.setStatus(DepositFrequencyFileRecStatus.Error);
                    } else if (invalidDataFound) {
                        frequencyUpdate.setStatus(DepositFrequencyFileRecStatus.InvalidData);
                    } else {
                        frequencyUpdate.setStatus(DepositFrequencyFileRecStatus.SkippedUpdating);
                    }
                    if (errorInformation != null && errorInformation.result.getMessages() != null) {
                        String message = errorInformation.result.getMessages().toString();
                        frequencyUpdate.setErrorMessage(message.length() > 4000 ? message.substring(0, 4000) : message);
                    }
                    Application.save(frequencyUpdate);
                    PayrollServices.commitUnitOfWork();
                } finally {
                    PayrollServices.rollbackUnitOfWork();
                }
            }
            return errorInformation;
        }

        private boolean isValidData(DepositFrequencyFileRec pDepositFrequencyFileRec) {
            //TODO_CODEREVIEW: I didn't notice any 941/944 handling anywhere.  Is there nothing to be done there?
            //Applying deposit frequency only if Form filed=941 with valid deposit frequency code
//            if (pDepositFrequencyFileRec.getFormFiled() != null && pDepositFrequencyFileRec.getFormFiled().equals("941")) {
                return isValidFrequencyCode(pDepositFrequencyFileRec.getLastPeriodBaseCode()) || isValidFrequencyCode(pDepositFrequencyFileRec.getDepositFrequency());
//            }

//            return false;
        }

        private boolean isValidFrequencyCode(String pFrequencyCode) {
            if (pFrequencyCode != null && (pFrequencyCode.equals("S") || pFrequencyCode.equals("M"))) {
                return true;
            }
            return false;
        }

        private boolean shouldApplyDepositFrequency(Company pCompany) {
            /* Logic for deciding whether to apply deposit frequency
            1. Psp sets deposit frequency to SemiWeekly as default with effective date as First day of the service start date quarter.
            2. Psp also sets Semiweekly as deposit frequency when 100K threshold is exceeded - in this case do not apply deposit frequency
            3. Apply deposit frequency - if present deposit frequency is not SemiWeekly
            4. If deposit frequency is Semiweekly - apply deposit frequency only if it is added by PSP as a default deposit frequency
            */

            CompanyService companyService = pCompany.getCompanyService(ServiceCode.Tax);
            SpcfCalendar today = PSPDate.getPSPTime().copy();
            CalendarUtils.clearTime(today);
            EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(pCompany, PaymentTemplate.getIRS_941(), today);
            //Effective deposit frequency will never be null.
            if (effectiveDepositFrequency == null) {
                logger.error("EffectiveDepositFrequency is not found for company: " + pCompany);
                return true;
            }
            DepositFrequencyCode presentDepositFrequency = effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId();
            SpcfCalendar effectiveDate = effectiveDepositFrequency.getEffectiveDate();
            SpcfCalendar taxServiceStartDate = companyService.getServiceStartDate();
            CalendarUtils.clearTime(effectiveDate);
            //If Tax service is in pending, service start date value can be null
            if (taxServiceStartDate != null) {
                CalendarUtils.clearTime(taxServiceStartDate);
            }

            if (hasBeenChangedFromDefaultFrequencyCd(presentDepositFrequency)) {
                return true; // Apply deposit frequency - if present deposit frequency is not SemiWeekly
            } else if (hasExceededThreshold(pCompany)) {
                //100k threshold exceeded, must stay SemiWeekly. Do not update deposit frequency.
                return false;
            } else if (hasInitialDefaultDepositFrequency(effectiveDate, taxServiceStartDate)) {
                // company has never had a deposit frequency applied (it has SEMIWEEKLY w/default effective date)
                return true;
            } else {
                // company is not 100k and has already had deposit frequency updated by system or agent
                return false;
            }

        }

        private boolean hasBeenChangedFromDefaultFrequencyCd(DepositFrequencyCode pPresentDepositFrequencyCd) {
            DepositFrequencyCode pspDefault = DepositFrequencyCode.SEMIWEEKLY;  // psp sets this as default deposit frequency when the agency is created
            if (pPresentDepositFrequencyCd != pspDefault) {
                return true;
            }
            return false;
        }

        private boolean hasInitialDefaultDepositFrequency(SpcfCalendar pEffectiveDate, SpcfCalendar pTaxServiceStartDate) {
            if (pTaxServiceStartDate == null || pEffectiveDate.equals(pTaxServiceStartDate)
                    || pEffectiveDate.equals(CalendarUtils.getFirstDayOfQuarter(pTaxServiceStartDate))
                    || pEffectiveDate.equals(CalendarUtils.getFirstDayOfTheYear(pTaxServiceStartDate))) {
                return true;
            }
            return false;
        }

        private boolean hasExceededThreshold(Company pCompany) {
            DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEventWithDetailsEagerLoaded(pCompany, EventTypeCode.ThresholdExceeded, EventDetailTypeCode.PaymentTemplate, PaymentTemplate.getIRS_941().getPaymentTemplateCd());
            if (companyEvents.isEmpty()) {
                return false;
            }
            CompanyEvent companyEvent = companyEvents.get(companyEvents.size() - 1); //Checking the latest threshold event only
            //If threshold exceeded event is found, check if threshold got reversed
            if (companyEvent != null && companyEvent.getCompanyEventDetailValue(EventDetailTypeCode.ThresholdReversed) == null) {
                return true;
            }
            return false;
        }

        private EffectiveDepositFrequencyDTO createDTO(DepositFrequencyFileRec pRecord) {
            EffectiveDepositFrequencyDTO depositFrequencyDTO = new EffectiveDepositFrequencyDTO();
            depositFrequencyDTO.setAgencyId(Agency.IRS);
            depositFrequencyDTO.setEffectiveDate(PSPDate.getPSPTime());
            DepositFrequencyCode frequencyCode = null;
            //Rule for identifying the deposit frequency code
            // If exists, last period base value, apply this as deposit frequency
            // else if exists, deposit frequency value, apply this
            if (pRecord.getLastPeriodBaseCode() != null) {
                if (pRecord.getLastPeriodBaseCode().equals("M")) {
                    frequencyCode = DepositFrequencyCode.MONTHLY;
                } else if (pRecord.getLastPeriodBaseCode().equals("S")) {
                    frequencyCode = DepositFrequencyCode.SEMIWEEKLY;
                }
            }
            if (frequencyCode == null && pRecord.getDepositFrequency() != null) {
                if (pRecord.getDepositFrequency().equals("M")) {
                    frequencyCode = DepositFrequencyCode.MONTHLY;
                } else if (pRecord.getDepositFrequency().equals("S")) {
                    frequencyCode = DepositFrequencyCode.SEMIWEEKLY;
                }
            }

            if (frequencyCode == null) {
                throw new RuntimeException("Deposit frequency code value from file is NULL, can not be updated");
            }
            depositFrequencyDTO.setPaymentFrequencyId(frequencyCode);
            depositFrequencyDTO.setPaymentTemplateCd("IRS-941-PAYMENT");
            return depositFrequencyDTO;
        }

        private void sendAlertEmail(List<ErrorInformation> pErrors) {
            StringBuilder sb = new StringBuilder();
            sb.append("The following companies did not have their deposit frequencies updated: ").append("\n")
                    .append("\n");

            for (ErrorInformation error : pErrors) {
                sb.append(error);
            }

            logger.error(sb);

            MailSender mailSender = new MailSender(BatchUtils.getConfigString("psp_batch_mail_server"),
                    BatchUtils.getConfigString("psp_batch_irs_deposit_frequency_notification_list"),
                    BatchUtils.getConfigString("psp_batch_irs_deposit_frequency_notification_list"),
                    "IRS Deposit Frequency File processing errors",
                    sb.toString());

            mailSender.sendEmail();
        }

        private class ErrorInformation {
            private ErrorInformation(String pUpdateRecordId, String pSourceCompanyId, ProcessResult pResult) {
                updateRecordId = pUpdateRecordId;
                sourceCompanyId = pSourceCompanyId;
                result = pResult;
            }

            public String updateRecordId;
            public String sourceCompanyId;
            public ProcessResult result;

            public String toString() {
                StringBuilder sb = new StringBuilder();
                sb.append("\nPSID: ").append(sourceCompanyId)
                        .append("\nDepositFrequencyFileRec Id: ").append(updateRecordId)
                        .append("\n").append(result.getMessages())
                        .append("\n");
                return sb.toString();
            }
        }
    }

    public class ArchiveDepositFrequencyFiles extends BatchJobProcessorStep {
        @Override
        public void execute() {
            String fileName = null;
            try {
                PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

                //todo Update this finder when we start to process the files in PSP. Query for Processed and Skipped.
                DomainEntitySet<DepositFrequencyFile> depositFrequencyFiles = DepositFrequencyFile.findDepositFrequencyFiles(false,
                                                                                                                             DepositFrequencyFileStatus.Received,
                                                                                                                             DepositFrequencyFileStatus.Skipped);
                if (depositFrequencyFiles.isEmpty()) {
                    return;
                }

                String archiveDirectoryPath = BatchUtils.getConfigString("psp_batch_irs_deposit_frequency_archive_dir");

                for (DepositFrequencyFile depositFrequencyFile : depositFrequencyFiles) {
                    File archivedFile = BatchUtils.moveFile(depositFrequencyFile.getFileName(), archiveDirectoryPath);
                    fileName = archivedFile.getName();
                    if (!DepositFrequencyFileStatus.Skipped.equals(depositFrequencyFile.getStatus())) {
                        depositFrequencyFile.setStatus(DepositFrequencyFileStatus.Processed);
                    }
                    depositFrequencyFile.setFileName(archivedFile.getAbsolutePath());
                    depositFrequencyFile.setIsArchived(true);
                    Application.save(depositFrequencyFile);
                }
                PayrollServices.commitUnitOfWork();
            } catch (Throwable t) {
                logger.error("Failure archiving file " + fileName, t);
            } finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
    }
}

// zero based start position
enum FixedFieldDefinition {
    LEGAL_NAME(0, 35),
    EIN(35, 9),
    FORM_FILED(44, 3),
    Q1_LOOKBACK(47, 13),
    Q2_LOOKBACK(60, 13),
    Q3_LOOKBACK(73, 13),
    Q4_LOOKBACK(86, 13),
    CURRENT_YEAR(99, 4),
    DEPOSIT_FREQUENCY(103, 1),
    FLAG_100K(104, 1),
    NEXT_YEAR_FREQ(105, 6),
    LAST_BASE(111, 6),
    LAST_BASE_CODE(117, 1),
    EFTPS_YEAR(118, 4),
    FUTA_EXEMPT(122, 1),
    SEASONAL(123, 1),
    NAME_CONTROL(124, 4),
    NAME_LINE(128, 35),
    IRC_INDICATOR(163, 1);

    private int startPosition;
    private int length;

    FixedFieldDefinition(int pStartPosition, int pLength) {
        startPosition = pStartPosition;
        length = pLength;
    }

    public String read(String pInputLine) {
        if (pInputLine == null || startPosition > pInputLine.length()) return null;
        String value = pInputLine.substring(startPosition, Math.min(startPosition + length, pInputLine.length()));
        if (value != null) {
            value = value.trim().isEmpty() ? null : value.trim();
        }
        return value;
    }
}

