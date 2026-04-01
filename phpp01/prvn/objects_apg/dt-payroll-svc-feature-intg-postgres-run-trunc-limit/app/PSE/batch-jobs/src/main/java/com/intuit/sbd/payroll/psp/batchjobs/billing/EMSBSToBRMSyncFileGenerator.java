package com.intuit.sbd.payroll.psp.batchjobs.billing;

import com.intuit.ems.payroll.psp.gateway.brm.BRMFileUploader;
import com.intuit.idps.service.IdpsException;
import com.intuit.idps.service.StreamingCryptoService;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.idps.domain.item.Key;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

/**
 * User: YifengS302
 * Date: 9/28/12
 * Time: 2:31 PM
 */
public class EMSBSToBRMSyncFileGenerator {

    private static SpcfLogger mLogger = Application.getLogger(EMSBSToBRMSyncFileGenerator.class);
    public String inputFileName=null;

    public void generate(SpcfCalendar mProcessingDate) {

        try {
            StopWatch sw = new StopWatch().start();
            SpcfCalendar startDate = CalendarUtils.dayOfMonthAfter(mProcessingDate, 1);
            SpcfCalendar endDate = CalendarUtils.getLastDayOfMonth(startDate);
            mLogger.info(String.format("EMSBSToBRMDataSync Job. Start Date=%s, End Date=%s", startDate, endDate));
            Application.beginUnitOfWork();
            Map<Bill.CompanyKey, Set<SpcfUniqueId>> companyBillMap = Bill.findOpenBillsByCompanyDuring(startDate, endDate);
            Application.rollbackUnitOfWork();
            genFile(companyBillMap);
            sw.stop();
            mLogger.info("completed EMSBS to BRM sync for bills from " + startDate.toString() + " to " + endDate.toString()
                                 + ", number of companies " + companyBillMap.size() + "     "
                                 + "duration: " + sw.getElapsedTimeString());
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    protected void genFile(Map<Bill.CompanyKey, Set<SpcfUniqueId>> companyBillMap) {

        if (companyBillMap == null || companyBillMap.isEmpty()) {
            return;
        }
        boolean fileGenSuccess = true;
        int processors = Runtime.getRuntime().availableProcessors();
        int threadCount = processors * 2;
        ExecutorService threadPool = null;       
	    try {
	    	threadPool = Executors.newFixedThreadPool(threadCount);
	    	CompletionService<String> completionService = new ExecutorCompletionService<String>(threadPool);
	        for (Map.Entry<Bill.CompanyKey, Set<SpcfUniqueId>> entry : companyBillMap.entrySet()) {
	            completionService.submit(new TransactionGens(entry.getKey().sourceCompanyId, entry.getKey().sourceSystemCode, entry.getValue()));
	        }
	        String filename = BRMFileUploader.LOCAL_WORK_DIR + BRMFileUploader.FILENAME_PATTERN;
	        filename = filename.replace("[timestamp]", PSPDate.getPSPTime().toLocal().format("yyyyMMddHHmmss"));
	        inputFileName=filename;
            OutputStreamWriter fileWriter = null;
	        try {
	            fileWriter = new FileWriter(filename);
	            fileWriter.write(BRMFileUploader.BRM_FILE_HEADER);
	            for (int i = 0; i < companyBillMap.size(); i++) {
	                String result = "";
	                try {
	
	                    Future<String> f = completionService.take();
	                    result = f.get(); 
	                } catch (InterruptedException e) {
	                    Thread.currentThread().interrupt();
	                } catch (Throwable t) {
	                    // should not get runtime error here, but in case of the unexpected errors, swallow it and let other threads go
	                    mLogger.error("failed to sync EMSBS to BRM", t);
	                }
	                if (!"".equals(result)) {
	                    fileWriter.write(result);
	                    fileWriter.flush();
	                }
	            }
	        } catch (IOException e) {
	            fileGenSuccess = false;
	            mLogger.error("Can not proceed. IO error of BRM file", e);
	        } finally {
	            if (fileWriter != null) {
	                try {
	                    fileWriter.close();
	                } catch (IOException e) {
	                    fileGenSuccess = false;
	                    mLogger.error("failed to close BRM file", e);
	                }
	            }
	        }
	        if (!fileGenSuccess) {
	            File outputFile = new File(filename);
	
	            try {
	                outputFile.delete();
	            } catch (Throwable e) {
	            }
	            throw new RuntimeException("BRM file is not generated due to IOException. Please check file system.");
	        }else {
	            File outputFile = new File(filename);
	            try {
                    String outputFileContent = new String(Files.readAllBytes(Paths.get(outputFile.getPath())));
                    mLogger.info("Generated file content: "+outputFileContent);
                }catch(IOException e){
	                mLogger.error("Error occured while getting the generated file content {}",e);
                }
	            updateSystemParameter(outputFile.getName());
	        }
		} finally {
			if (threadPool != null) {
				ThreadingUtils.shutdownAndAwaitTermination(threadPool);
			}
		}

    }

    private void updateSystemParameter(String pFileName) {
        try {
            Application.beginUnitOfWork();
            ProcessResult pr = PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.BRM_SYMPHONY_FILE_NAME, pFileName.substring(0, pFileName.length() -4));
            if (!pr.isSuccess()) {
                mLogger.error("failed to add value for " + SystemParameter.Code.BRM_SYMPHONY_FILE_NAME);
            }
            Application.commitUnitOfWork();
        } catch (Throwable t) {
            mLogger.error("failed to add value for " + SystemParameter.Code.BRM_SYMPHONY_FILE_NAME);
            throw new RuntimeException(t);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    protected class TransactionGens implements Callable<String> {

        private String sourceCompanyId;
        private SourceSystemCode sourceSystemCd;
        private Set<SpcfUniqueId> mBillIds;

        private int mEmployeesFoundInOtherBills;
        private Bill mPrimaryBill;

        public TransactionGens(String pSourceCompanyId, SourceSystemCode pSourceSystemCd, Set<SpcfUniqueId> pBillIds) {

            sourceCompanyId = pSourceCompanyId;
            sourceSystemCd = pSourceSystemCd;
            mBillIds = pBillIds;
        }

        public String call() throws Exception {

            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.EMSBSToBRMDataSyncBatchJob));
            StringBuffer transaction = new StringBuffer();
            try {
                Application.beginUnitOfWork();
                consolidateBillsAcrossCompanies();
                int delta = mPrimaryBill.getUsageCount() - mPrimaryBill.getSynchedCount() + mEmployeesFoundInOtherBills;
                if (delta > 0) {
                    String licenseId = mPrimaryBill.getCompanyUsage().getLicenseId();
                    String eoc = mPrimaryBill.getCompanyUsage().getEntitlementId();
                    Entitlement entitlement = Application.find(Entitlement.class, Entitlement.LicenseNumber().equalTo(licenseId).And(Entitlement.EntitlementOfferingCode().equalTo(eoc))).getFirst();
                    if (entitlement != null && entitlement.getEntitlementCode().getIsFirstUsageFree()) {
                        delta--;
                    }
                    if (delta > 0) {
                        transaction.append(licenseId).append(',');
                        transaction.append(eoc).append(',');
                        transaction.append("/PayrollEE,");
                        transaction.append(delta).append(',');
                        SpcfCalendar transactionDate = CalendarUtils.getFirstDayOfPrevMonth(mPrimaryBill.getBillDate());
                        if (entitlement != null) {
                            if (!entitlement.getRetail() && entitlement.getSubscriptionStartDate() != null &&
                                    transactionDate.before(entitlement.getSubscriptionStartDate())) {
                                transactionDate = entitlement.getSubscriptionStartDate();
                            } else if (transactionDate.before(entitlement.getCreatedDate())) {
                                transactionDate = entitlement.getCreatedDate();
                            }
                        }
                        transactionDate.setValues(transactionDate.getYear(), transactionDate.getMonth(), transactionDate.getDay(), 23, 59, 59, 999);
                        transaction.append(CalendarUtils.convertCalendarToXmlStringNoMilliSeconds(transactionDate)).append("\n");

                    }
                    mPrimaryBill.setSynchedCount(mPrimaryBill.getSynchedCount() + delta);
                    mPrimaryBill.setClosed(true);
                    Application.save(mPrimaryBill);
                    Application.commitUnitOfWork();
                    mLogger.info("completed EMSBS to BRM sync on bills " + Arrays.toString(mBillIds.toArray()) + " sent usage " + String.valueOf(delta));
                }
            } catch (Throwable e) {
                transaction = new StringBuffer();
                mLogger.error("failed to the synch process with BRM. Bill ids: " + Arrays.toString(mBillIds.toArray()), e);
            } finally {
                Application.rollbackUnitOfWork();
            }
            return transaction.toString();
        }

        /**
         * This method handles two sets of scenarios.  The first is when there are multiple bills for a company,
         * and only one of them is the current primary (i.e. customer migrated and sent payrolls under each
         * entitlement).  In this case we will:
         * For all of the included bills for a company:
         * identify the bill that is on the primary entitlement => mPrimaryBill
         * count the employees in all _other_ bills that are not in the primary bill => mEmployeesFoundInOtherBills
         * close the non-primary bills without updating sync count (file will not include them)
         * <p/>
         * The other scenario is when there is only one bill, but it's not on the current primary entitlement.
         * This would happen if the customer changes entitlements but does not send a new payroll before billing.
         * In this case we will manually move the bill/usage to the new entitlement.
         */

        private void consolidateBillsAcrossCompanies() {

            mEmployeesFoundInOtherBills = 0;
            mPrimaryBill = null;
            Company company = Company.findCompany(sourceCompanyId, sourceSystemCd);
            Entitlement primaryEntitlement = company.getActivePrimaryEntitlementUnit().getEntitlement();
            if (mBillIds.size() > 1) {
                List<Bill> otherBills = new ArrayList<Bill>();
                Set<String> primaryBillEmployeeIds = new HashSet<String>();
                for (SpcfUniqueId billId : mBillIds) {
                    Bill bill = Application.findById(Bill.class, billId);
                    otherBills.add(bill);
                    if (bill.isForEntitlement(primaryEntitlement)) {
                        for (PaycheckUsage paycheckUsage : bill.getPaycheckUsageCollection()) {

                            if(!isPaycheckUsageChargableInCurrentBill(paycheckUsage)){
                                continue;
                            }

                            primaryBillEmployeeIds.add(paycheckUsage.getEmployeeUsage().getSourceEmployeeId());
                        }
                        if (mPrimaryBill != null) {
                            throw new RuntimeException("Company has multiple bills for primary entitlement unit(s)");
                        }
                        mPrimaryBill = bill;
                    }
                }
                if (mPrimaryBill == null) {
                    throw new RuntimeException("Company has multiple bills but none of which are for the primary entitlement");
                }
                Set<String> otherBillsEmployeeIds = new HashSet<>();
                for (Bill otherBill : otherBills) {
                    otherBill.setClosed(true);
                    Application.save(otherBill);
                    for (PaycheckUsage paycheckUsage : otherBill.getPaycheckUsageCollection()) {

                        if (primaryBillEmployeeIds.contains(paycheckUsage.getEmployeeUsage().getSourceEmployeeId())) {
                            continue;
                        }

                        if(!isPaycheckUsageChargableInCurrentBill(paycheckUsage)){
                            continue;
                        }

                        otherBillsEmployeeIds.add(paycheckUsage.getEmployeeUsage().getSourceEmployeeId());
                    }
                }

                mEmployeesFoundInOtherBills = otherBillsEmployeeIds.size();
            } else {
                mPrimaryBill = Application.findById(Bill.class, mBillIds.iterator().next());
                if (!mPrimaryBill.isForEntitlement(primaryEntitlement)) {
                    mPrimaryBill.getCompanyUsage().migrateCompanyUsageToNewEntitlement(mPrimaryBill, primaryEntitlement);
                }
            }
        }

        private boolean isPaycheckUsageChargableInCurrentBill(PaycheckUsage paycheckUsage){
            if(paycheckUsage.isFree()){
                return false;
            }

            if(paycheckUsage.isCancelled()){
                return false;
            }

            EmployeeUsage employeeUsage = paycheckUsage.getEmployeeUsage();
            if(employeeUsage.getUsageCount() == 0){
                return false;
            }

            SpcfCalendar billDate = paycheckUsage.getBill().getBillDate();
            SpcfCalendar firstDayOfCurrentMonth = CalendarUtils.getFirstDayOfMonth(billDate);

            for(PaycheckUsage allPaycheckUsage:employeeUsage.getPaycheckUsageCollection()){
                if(allPaycheckUsage.getBill().getBillDate().before(firstDayOfCurrentMonth)){
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * This method is meant for encrypting the Symphony Usage files based on BRM IDPS key,input and output file
     */
    public static int streamEncryptFileSingleThread(Key key,File input,File output) throws IOException, IdpsException {

        mLogger.info("EMSBSToBRMSyncFileGenerator:streamEncryptFileSingleThread - Starting to encrypt the input file with key "+key.getName());
        mLogger.info("EMSBSToBRMSyncFileGenerator:streamEncryptFile-" + input.getName() + "outputFile: " + output.getName());
        mLogger.info("EMSBSToBRMSyncFileGenerator:streamEncryptFile- input file content:  " + input.toString() +" and size is: " +input.length());
        // for encryption the output buffer needs to be slightly larger than the input buffer
        byte[] outbuffer = new byte[8*1024];
        FileOutputStream fos = new FileOutputStream(output);
        FileInputStream in=null;

        StreamingCryptoService stream = null;

        int totalBytes = 0;
        try {
            in = new FileInputStream(input);
            mLogger.info("Input buffer size:" + in.toString().length());
            int chunkSize = 4096;
            // initialize the streaming encryption context
            stream = StreamingCryptoService.Factory.streamEncryptInit(key,chunkSize, in.getChannel());

            // get the output channel
            ReadableByteChannel streamOut = stream.getOutputChannel();
            mLogger.info("EMSBSToBRMSyncFileGenerator:streamEncryptFileSingleThread - "+streamOut.toString() + " length: "+streamOut.toString().length());
            int nBytes;
            // loop until we've encrypted all the bytes we intend to
            while ((nBytes = stream.streamEncryptNext()) != -1) {


                // read the encrypted bytes from the stream's output channel
                int readBytes = streamOut.read(ByteBuffer.wrap(outbuffer, 0, nBytes));
                mLogger.info("EMSBSToBRMSyncFileGenerator:streamEncryptFileSingleThread - readBytes" + readBytes);
                // make sure the number of bytes encrypted are the number of bytes read
                assert nBytes == readBytes;

                // do whatever you want with outbuffer, in this example we write to a file
                fos.write(outbuffer, 0, readBytes);

                // book-keeping
                totalBytes += nBytes;


            }
            fos.flush();
            mLogger.info("EMSBSToBRMSyncFileGenerator: streamEncryptFileSingleThread - Completed encrypting the input file");
        }catch (IOException e){
            mLogger.error("EMSBSToBRMSyncFileGenerator: streamEncryptFileSingleThread - IO Exception occured while encryption {}",e);
        }catch (Exception e1){
            mLogger.error("EMSBSToBRMSyncFileGenerator: streamEncryptFileSingleThread - Exception occured while encryption {}",e1);
        }
        finally {
            // close out everything
            if (in != null)
                in.close();
            if (fos != null)
                fos.close();

            // close the stream
            stream.streamClose();
        }

        return totalBytes;
    }
}
