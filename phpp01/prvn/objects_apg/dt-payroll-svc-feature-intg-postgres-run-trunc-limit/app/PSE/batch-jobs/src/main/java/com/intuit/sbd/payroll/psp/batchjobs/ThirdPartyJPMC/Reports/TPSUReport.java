package com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.Reports;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.JPMCEventMessage;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC.JPMCEventMessageBuilder;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.pgp.utils.PgpFileUtils;
import com.intuit.sbd.payroll.psp.common.utils.FileUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.Address;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by charithah418 on 8/22/2016.
 */
//JPMC ThirdParty screening file please refer Jira PSP-11247
public class TPSUReport extends JPMCReportBase {

    private static final String DELIMITER = ",";
    private static final String FILE_EOL = "\r\n";

    public static final String TPS_PREFIX = "Intuit_TPS_File_";

    public static final String DATA_FILE_EXT = ".csv";
    public static final String ENCRYPTED_DATA_FILE_EXT = ".pgp";

    private static List<String> mEncryptionKeyList;
    private static String mSignatureKey;
    private static String mSignatureKeyPassword;

    //Reading the batch size from database
    private static final int BATCH_SIZE = SystemParameter.findIntValue(SystemParameter.Code.TPSU_REPORT_BATCH_SIZE);
    int incremental_size_head, incremental_size_tail;

    //JPMCEventMessage is a Outline, The object is not a message it is a value read from database.
    public String mDataFile;


    int processors = Runtime.getRuntime().availableProcessors();
    int recommended = processors * 2;
    int threadCount = recommended;



    public TPSUReport() {
        logger = Application.getLogger(this.getClass());
    }

    @Override
    public void createJPMCReport(SpcfCalendar fromDate, SpcfCalendar toDate) throws Exception {
        logger.info("Getting Data for TPSU_Report");
        logger.info("creating thread pool with size: " + threadCount + "\t recommended: " + recommended + " for " + processors + " processors.");

        String formattedDate = StringFormatter.formatDate(PSPDate.getPSPTime(), "yyyyMMdd");
        this.mDataFile = OUTPUT_DIRECTORY + File.separator + TPS_PREFIX + formattedDate + DATA_FILE_EXT;

        List<JPMCEventMessage> jpmcEventMessages;
        List<String> companies = Company.findActiveCompaniesOnDDService();

        FileWriter fileWriter = new FileWriter(mDataFile);

        //Write the header record 
        StringBuilder recordData = new StringBuilder();
        createTPSUHeader(recordData);
        writeData(fileWriter, recordData.toString());

        //Write into the data file
        incremental_size_tail = companies.size();
        incremental_size_head = 0;

        while (incremental_size_tail > BATCH_SIZE) {
            jpmcEventMessages = getTPSUReportData(companies.subList(incremental_size_head, incremental_size_head + BATCH_SIZE));
            createTPSUFile(jpmcEventMessages, fileWriter);
            incremental_size_head += BATCH_SIZE;
            incremental_size_tail -= BATCH_SIZE;
        }

        jpmcEventMessages = getTPSUReportData(companies.subList(incremental_size_head, incremental_size_head + incremental_size_tail));
        createTPSUFile(jpmcEventMessages, fileWriter);

        fileWriter.close();

        boolean enableEncryption = SystemParameter.findBooleanValue(SystemParameter.Code.JPMC_ENABLE_ENCRYPTION, false);
        if (enableEncryption) {
            try {
                encryptTPSUFile(TPS_PREFIX + formattedDate);
            } catch (Exception exception) {
                FileUtils.deleteFile(OUTPUT_DIRECTORY + File.separator + TPS_PREFIX + formattedDate + ENCRYPTED_DATA_FILE_EXT);
                throw new RuntimeException("Exception = encrypting the TSU_Report " + exception);
            } finally {
                FileUtils.deleteFile(mDataFile);
            }
        }
    }

    public List<JPMCEventMessage> getTPSUReportData(List<String> companyIds) throws Exception {
        final List<JPMCEventMessage> jpmcEventMessages = new ArrayList<JPMCEventMessage>();
        StopWatch timer = StopWatch.startTimer();
        ExecutorService executor=null;
	    try {
	        executor = Executors.newFixedThreadPool(threadCount);
	        CompletionService<JPMCEventMessage> completionService = new ExecutorCompletionService<JPMCEventMessage>(executor);
	
	        for (final String companyId : companyIds) {
	                completionService.submit(new Callable<JPMCEventMessage>() {
	                Company company = null;
	                JPMCEventMessageBuilder jpmcEventMessageBuilder = null;
	
	                @Override
	                public JPMCEventMessage call() throws Exception {
	                    ProcessResult<String> result = new ProcessResult<String>();
	                    result.setResult(companyId);
	                    PayrollServices.beginUnitOfWork();
	                    company = Company.findCompany(companyId, SourceSystemCode.QBDT);
	                    jpmcEventMessageBuilder = getEventMessage(company);
	                    PayrollServices.commitUnitOfWork();
	                    return jpmcEventMessageBuilder.build();
	                }
	            });
	        }
	
	        for (String company : companyIds) {
	            try {
	               jpmcEventMessages.add(completionService.take().get());
	            } catch (InterruptedException ex) {
	                logger.error("Thread interrupted Exception", ex.getCause());
	                throw new Exception();
	            } catch (ExecutionException ex) {
	                logger.error("Thread Execution Exception", ex.getCause());
	                throw new Exception();
	            }
	        }
		} finally {
			if (executor != null) {
				ThreadingUtils.shutdownAndAwaitTermination(executor);
			}

		}
        logger.info("Completed writing companies: " + timer.stop().getElapsedTimeString());
        return jpmcEventMessages;
    }

    /**
     * @param pJPMCEventMessageList
     * @throws Exception
     */
    public void createTPSUFile(List<JPMCEventMessage> pJPMCEventMessageList, FileWriter fileWriter) throws Exception {
        logger.info("Creating Data file for TPSU Report");
        /* ID, Company Name, Address Line 1, Address Line 2, City, State
        * Zipcode, Country Code
         */
        StringBuilder recordData = new StringBuilder();
        for (JPMCEventMessage jpmcEventMessage : pJPMCEventMessageList) {
            createTPSURecord(jpmcEventMessage, recordData);
        }

        try {
            writeData(fileWriter, recordData.toString());
            fileWriter.flush();
        } catch (IOException e) {
            logger.error("Unable to write data to the file");
            e.printStackTrace();
            throw new Exception();
        }
        logger.info("Finished creating Data file for TPSU report. Number of Records=" + pJPMCEventMessageList.size());
    }

    public void createTPSUHeader(StringBuilder recordData) {
        recordData.append("ID");
        recordData.append(DELIMITER);
        recordData.append("COMPANY NAME");
        recordData.append(DELIMITER);
        recordData.append("ADDRESS LINE 1");
        recordData.append(DELIMITER);
        recordData.append("ADDRESS LINE 2");
        recordData.append(DELIMITER);
        recordData.append("CITY");
        recordData.append(DELIMITER);
        recordData.append("STATE");
        recordData.append(DELIMITER);
        recordData.append("ZIPCODE");
        recordData.append(DELIMITER);
        recordData.append("COUNTRY CODE");
        recordData.append(FILE_EOL);
    }

    /**
     * @param jpmcEventMessage
     * @param recordData
     * @return
     */
    public void createTPSURecord(JPMCEventMessage jpmcEventMessage, StringBuilder recordData) {
        recordData.append(jpmcEventMessage.getFedTaxId());
        recordData.append(DELIMITER);

        recordData.append(jpmcEventMessage.getLegalName());
        recordData.append(DELIMITER);

        recordData.append(jpmcEventMessage.getAddressLine1());
        recordData.append(DELIMITER);

        recordData.append(jpmcEventMessage.getAddressLine2());
        recordData.append(DELIMITER);

        recordData.append(jpmcEventMessage.getCity());
        recordData.append(DELIMITER);

        recordData.append(jpmcEventMessage.getState());
        recordData.append(DELIMITER);

        recordData.append(jpmcEventMessage.getZipCode());
        recordData.append(DELIMITER);

        recordData.append(jpmcEventMessage.getCountry() == null ? "USA" : jpmcEventMessage.getCountry());
        recordData.append(FILE_EOL);
    }

    @Override
    protected JPMCEventMessageBuilder getEventMessage(Company company) {
        Address legalAddress = company.getLegalAddress();
        JPMCEventMessageBuilder jpmcEventMessageBuilder = JPMCEventMessageBuilder.JPMCEventMessage();

        if (legalAddress != null) {
            jpmcEventMessageBuilder.withAddressLine1(legalAddress.getAddressLine1())
                    .withAddressLine2(legalAddress.getAddressLine2())
                    .withCity(legalAddress.getCity())
                    .withState(legalAddress.getState())
                    .withCountry(legalAddress.getCountry())
                    .withSourceCompanyId(company.getSourceCompanyId())
                    .withZipCode(legalAddress.getZipCode())
                    .withFedTaxId(company.getFedTaxId())
                    .withLegalName(company.getLegalName());
        }
        return jpmcEventMessageBuilder;
    }

    private void encryptTPSUFile(String fileNameWithoutExtension) throws Exception {
        String csvFileName = File.separator + fileNameWithoutExtension + DATA_FILE_EXT;
        String pgpFileName = File.separator + fileNameWithoutExtension + ENCRYPTED_DATA_FILE_EXT;

        logger.info("Starting pgpEncrypt SignFile...");

        StopWatch sw = StopWatch.create(false);
        sw.start();

        readParameters();
        PgpFileUtils.pgpEncryptAndSignFile(OUTPUT_DIRECTORY,
                    csvFileName,
                    pgpFileName,
                    mEncryptionKeyList,
                    mSignatureKey,
                    mSignatureKeyPassword,
                    true,
                    true);

        sw.stop();
        logger.info("Encrypted File in location :" + OUTPUT_DIRECTORY + pgpFileName);
        logger.info("Completed pgpEncrypt with sign" + sw.getElapsedTimeString());
    }

    private void readParameters() {
        mEncryptionKeyList = new ArrayList<String>();
        String key;

        //Check if TPSU file QA environment is available else go with production file.
        String tpsuPublicKey = BatchUtils.getConfigString("psp_jpmc_tpsu_public_key");

        if(tpsuPublicKey != null && tpsuPublicKey.length() > 0) {
            mEncryptionKeyList.add(tpsuPublicKey);
        } else {
            //Add the banks public key so they can decrypt the file
            key = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_jpmc_public_key");
            if (key != null && key.length() > 0) {
                mEncryptionKeyList.add(key);
            }

            //Add our public key so we can decrypt the file
            key = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_public_key");
            if (key != null && key.length() > 0) {
                mEncryptionKeyList.add(key);
            }
        }
        //Sign the file with our private key
        mSignatureKey = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_private_key");
        mSignatureKeyPassword = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_batch_bank_intuit_private_key_password");
    }
}
