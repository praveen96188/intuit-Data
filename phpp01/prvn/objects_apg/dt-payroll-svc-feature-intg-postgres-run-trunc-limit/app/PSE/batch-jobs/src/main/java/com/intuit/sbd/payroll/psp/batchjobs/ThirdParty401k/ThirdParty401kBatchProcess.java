package com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k;

import com.intuit.idps.domain.item.Key;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.dtos.ThirdParty401kCensusDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ThirdParty401k.dtos.ThirdParty401kPayrollDTO;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileWriter;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.hibernate.SequenceId;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.StringFormatter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author Jeff Jones
 */
public class ThirdParty401kBatchProcess {
    private static final SpcfLogger logger;
    private static final String OUTPUT_DIRECTORY;

    private static final String CENSUS_FILE_PREFIX = "Census_Batch_Intuit_";
    private static final String PAYROLL_FILE_PREFIX = "Payroll_Batch_Intuit_";
    private static final String FILE_EXT = ".csv";
    private static final String DELIMITER = ",";
    private static final String FILE_EOL = "\n";

    private ArrayList<Integer> mNewBatchIdList;

    private HashMap<Company, Company401kInfo> company401kInfoMap = new HashMap<Company, Company401kInfo>();

    static {
        Application.initialize();
        ApplicationSecondary.initialize();
        logger = Application.getLogger(ThirdParty401kBatchProcess.class);
        OUTPUT_DIRECTORY = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs,
                                                                "psp_batch_ftp_send_dir");
    }

    public void createFiles() throws Exception {
        logger.info("Create 401k files started.");
        mNewBatchIdList = new ArrayList<Integer>();

        try {
            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);

            List<ThirdParty401kPayrollDTO> tp401kPayrollDTOs = getPaychecksForOffload();
            Collections.sort(tp401kPayrollDTOs);

            ThirdParty401kBatch payrollBatch = generateNewBatch();
            generatePayrollFileSecure(tp401kPayrollDTOs, payrollBatch);
            List<ThirdParty401kCensusDTO> tp401kEmployeeDTOs = getEmployeesForOffload(tp401kPayrollDTOs);
            Collections.sort(tp401kEmployeeDTOs);

            ThirdParty401kBatch censusBatch = generateNewBatch();
            generateCensusFileSecure(tp401kEmployeeDTOs, censusBatch);

            mNewBatchIdList.add(censusBatch.getBatchId());
            mNewBatchIdList.add(payrollBatch.getBatchId());

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        logger.info("Create 401k files completed.");
    }

    public List<ThirdParty401kPayrollDTO> getPaychecksForOffload() {
        List<ThirdParty401kPayrollDTO> paychecksReadyForOffload = new ArrayList<ThirdParty401kPayrollDTO>();

        // set the time to the last millisecond of the day
        SpcfCalendar initiationDate = PSPDate.getPSPTime().toLocal();
        initiationDate.addDays(1);
        CalendarUtils.clearTime(initiationDate);
        initiationDate.addMilliseconds(-1);

        DomainEntitySet<ThirdParty401kPaycheckPendingState> tpPaychecks = Paycheck.findTP401kOffloadablePaychecks(initiationDate);
        logger.info("processing " + tpPaychecks.size() + " potential paychecks for offload");
        for (ThirdParty401kPaycheckPendingState tpPaycheck : tpPaychecks) {
            Paycheck paycheck = tpPaycheck.getThirdParty401kPaycheck().getPaycheck();
            PayrollRun payrollRun = paycheck.getPayrollRun();

            if (!company401kInfoMap.containsKey(payrollRun.getCompany())) {
                company401kInfoMap.put(payrollRun.getCompany(), new Company401kInfo(payrollRun.getCompany()));
            }

            Company company = paycheck.getPayrollRun().getCompany();
            ThirdParty401kCompanyServiceInfo tp401kCompanyServiceInfo =
                    (ThirdParty401kCompanyServiceInfo) CompanyService.findCompanyService(company, ServiceCode.ThirdParty401k);
            ThirdParty401kPayrollDTO tp401kPayrollDTO = new ThirdParty401kPayrollDTO(tp401kCompanyServiceInfo, paycheck);

            if (tp401kPayrollDTO.getValidationErrors().isEmpty()) {
                paychecksReadyForOffload.add(tp401kPayrollDTO);

                // Mark ThirdParty401kPaycheckPendingState as being sent to allow a quick delete
                ThirdParty401kPaycheck tp401Paycheck = paycheck.getThirdParty401kPaycheck();
                tp401Paycheck.setCurrentStateCd(ThirdParty401kPaycheckStateCode.Sent);

                ThirdParty401kPaycheckPendingState thirdParty401kPaycheckPendingState = tp401Paycheck.getThirdParty401kPaycheckPendingState();
                thirdParty401kPaycheckPendingState.setStateCd(ThirdParty401kPaycheckStateCode.Sent);
                Application.save(thirdParty401kPaycheckPendingState);

                // Update ThirdParty401kPaycheckState to maintain history
                ThirdParty401kPaycheckState newPaycheckState = new ThirdParty401kPaycheckState();
                newPaycheckState.setStateEffectiveDate(PSPDate.getPSPTime());
                newPaycheckState.setThirdParty401kPaycheck(tp401Paycheck);
                newPaycheckState.setStateCd(ThirdParty401kPaycheckStateCode.Sent);

                Application.save(newPaycheckState);
                Application.save(tp401Paycheck);
            } else {
                logEligibleCensusRecordNotSent(tp401kPayrollDTO);
            }
        }

        return paychecksReadyForOffload;
    }
    public void generatePayrollFileSecure(List<ThirdParty401kPayrollDTO> pPaychecks, ThirdParty401kBatch pPayrollBatch) throws Exception {
        SpcfCalendar pspDate = PSPDate.getPSPTime();
        SpcfCalendar systemTime = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        String payrollFileName;
        if (shouldTruncateFilename()) {
            payrollFileName = OUTPUT_DIRECTORY + File.separator + PAYROLL_FILE_PREFIX +
                    StringFormatter.formatDate(pspDate, "yyyyMMdd") + FILE_EXT;
        } else {
            payrollFileName = OUTPUT_DIRECTORY + File.separator + PAYROLL_FILE_PREFIX +
                    StringFormatter.formatDate(pspDate, "yyyyMMdd") + "_" +
                    StringFormatter.formatDate(systemTime, "HHmmss") + FILE_EXT;
        }

        IDPSFileWriter payrollFileWriter = null;

        int recordCount = 0;
        try {

            Key key  = IDPSFileStreamManager.newKeyHandleLatest();
            payrollFileWriter = new IDPSFileWriter(payrollFileName,key);
            for (ThirdParty401kPayrollDTO tp401kPayrollDTO : pPaychecks) {

                // record paycheck being written to file for send in db
                ThirdParty401kBatchPaycheck tp401kBatchPaycheck = new ThirdParty401kBatchPaycheck();
                tp401kBatchPaycheck.setCompany(tp401kPayrollDTO.getPaycheck().getCompany());
                tp401kBatchPaycheck.setPaycheck(tp401kPayrollDTO.getPaycheck());
                tp401kBatchPaycheck.setThirdParty401kBatch(pPayrollBatch);
                Application.save(tp401kBatchPaycheck);

                // write to file
                ++recordCount;
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getBureauName());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getBureauCompanyId());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getFEIN());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getCustodialAccountId());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getLastName());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getFirstName());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getSalary());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getDeferral());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getRoth());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getLoan());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getMatching());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getProfitSharing());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getSafeHarbor());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getHours());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getTaxId());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getBeginPayPeriod());
                writeSecureData(payrollFileWriter, DELIMITER);
                writeSecureData(payrollFileWriter, tp401kPayrollDTO.getEndPayPeriod());
                writeSecureData(payrollFileWriter, FILE_EOL);
            }
            payrollFileWriter.flush();
        } finally {
            payrollFileWriter.close();
        }

        finalizeUploadBatch(recordCount > 0, payrollFileName, pPayrollBatch);
    }
    private boolean shouldTruncateFilename() {
        String value = BatchUtils.getConfigString("psp_batch_tp401k_truncate_file_name");
        boolean shouldTruncateFileName;
        try {
            shouldTruncateFileName = Boolean.valueOf(value);
        } catch (Throwable t) {
            logger.error(t);
            shouldTruncateFileName = false;
        }
        return shouldTruncateFileName;
    }

    public List<ThirdParty401kCensusDTO> getEmployeesForOffload(List<ThirdParty401kPayrollDTO> pPaychecks) {
        Map<SpcfUniqueId, ThirdParty401kCensusDTO> employeeCensusDTOMap = new HashMap<SpcfUniqueId, ThirdParty401kCensusDTO>();

        // include all employees that have a paycheck going (intentionally include EE once even if multiple
        // paychecks exist for EE
        for (ThirdParty401kPayrollDTO pPaycheck : pPaychecks) {
            employeeCensusDTOMap.put(pPaycheck.getPaycheck().getSourceEmployee().getId(), pPaycheck);
        }

        DomainEntitySet<CompanyService> companyServices = CompanyService.findActiveCompanyServiceByServiceCode(ServiceCode.ThirdParty401k);
        for (CompanyService companyService : companyServices) {
            Company company = companyService.getCompany();
            ThirdParty401kCompanyServiceInfo tp401kCompanyServiceInfo = (ThirdParty401kCompanyServiceInfo) companyService;

            if (!company401kInfoMap.containsKey(company)) {
                company401kInfoMap.put(company, new Company401kInfo(company));
            }
            Company401kInfo company401kInfo = company401kInfoMap.get(company);

            boolean new401kCompany = company401kInfo.isNew401kCompany();
            if (new401kCompany) {
                CompanyEvent.createCompanyEvent(company, EventTypeCode.Employee401kDataUploaded);
            }

            DomainEntitySet<Employee> employees = company.getCloudEmployees();
            for (Employee employee : employees) {
                if (employeeCensusDTOMap.containsKey(employee.getId())) {
                    continue;
                }

                EmployeeCensusInclusionTest eeInclusionTest = new EmployeeCensusInclusionTest(company401kInfo, company, employee);
                logger.info(eeInclusionTest);
                if (eeInclusionTest.isEligible()) {
                    ThirdParty401kCensusDTO employeeCensusDTO = new ThirdParty401kCensusDTO(tp401kCompanyServiceInfo,
                                                                                            employee);
                    if (employeeCensusDTO.getValidationErrors().isEmpty()) {
                        employeeCensusDTOMap.put(employee.getId(), employeeCensusDTO);
                    } else {
                        logEligibleCensusRecordNotSent(employeeCensusDTO);
                    }
                }
            }
        }

        return new ArrayList<ThirdParty401kCensusDTO>(employeeCensusDTOMap.values());
    }

    private void logEligibleCensusRecordNotSent(ThirdParty401kCensusDTO censusDTO) {
        String msg = "Employee " + censusDTO.getFirstName() + " " + censusDTO.getLastName() +
                " not included in the 401k file for the following reasons:\n";

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(msg);
        for (String err : censusDTO.getValidationErrors()) {
            stringBuilder.append("\t").append(err).append("\n");
        }

        logger.warn(stringBuilder.toString());
    }
    public void generateCensusFileSecure(List<ThirdParty401kCensusDTO> pCensusDTOs, ThirdParty401kBatch pEmployeeCensusBatch) throws Exception {
        SpcfCalendar pspDate = PSPDate.getPSPTime();
        SpcfCalendar systemTime = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        String censusFileName;
        if (shouldTruncateFilename()) {
            censusFileName = OUTPUT_DIRECTORY + File.separator + CENSUS_FILE_PREFIX +
                    StringFormatter.formatDate(pspDate, "yyyyMMdd") + FILE_EXT;
        } else {
            censusFileName = OUTPUT_DIRECTORY + File.separator + CENSUS_FILE_PREFIX +
                    StringFormatter.formatDate(pspDate, "yyyyMMdd") + "_" +
                    StringFormatter.formatDate(systemTime, "HHmmss") + FILE_EXT;
        }

        IDPSFileWriter censusFileWriter = null;

        int recordCount = 0;
        try {

            Key key  = IDPSFileStreamManager.newKeyHandleLatest();
            censusFileWriter = new IDPSFileWriter(censusFileName,key);
            for (ThirdParty401kCensusDTO tp401kOffloadDTO : pCensusDTOs) {

                // record employee being written to file for send in db
                ThirdParty401kBatchEmployee tp401kBatchEmployee = new ThirdParty401kBatchEmployee();
                tp401kBatchEmployee.setEmployee(tp401kOffloadDTO.getEmployee());
                tp401kBatchEmployee.setThirdParty401kBatch(pEmployeeCensusBatch);
                Application.save(tp401kBatchEmployee);

                ++recordCount;
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getBureauName());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getBureauCompanyId());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getAsOfDate());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getFEIN());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getCustodialAccountId());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getFirstName());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getMiddleName());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getTaxId());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getBirthDate());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getStreetAddress());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getCity());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getState());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getZip());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getPhone());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getEMail());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getDateOfHire());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getTerminationDate());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getTerminationStatus());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getIsHCE());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getIsFamilyMember());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getOwnershipPercentage().toString());
                writeSecureData(censusFileWriter, DELIMITER);
                writeSecureData(censusFileWriter, tp401kOffloadDTO.getLastName());
                writeSecureData(censusFileWriter, FILE_EOL);
            }

            censusFileWriter.flush();
        } finally {
            censusFileWriter.close();
        }

        finalizeUploadBatch(recordCount > 0, censusFileName, pEmployeeCensusBatch);
    }
    public void archiveFiles() throws Exception {
        try {
            PayrollServices.beginUnitOfWork();

            String archiveDir = BatchUtils.getConfigString("psp_batch_ftp_arcv_dir");

            DomainEntitySet<ThirdParty401kBatch> batchSet =
                    BatchUtils.getTP401kUploadFilesByStatus(ThirdParty401kBatchStatusCode.Transmitted);

            for (ThirdParty401kBatch batch : batchSet) {
                BatchUtils.moveFile(batch.getFileName(), archiveDir);

                batch.setUploadStatusCd(ThirdParty401kBatchStatusCode.Archived);
                batch.setStatusEffectiveDate(PSPDate.getPSPTime());

                Application.save(batch);
            }

            PayrollServices.commitUnitOfWork();
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private ThirdParty401kBatch generateNewBatch() {
        ThirdParty401kBatch uploadBatch = new ThirdParty401kBatch();

        int batchId = generateNewBatchId();
        uploadBatch.setBatchId(batchId);
        uploadBatch.setUploadStatusCd(ThirdParty401kBatchStatusCode.InProcess);
        uploadBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
        uploadBatch.setUploadDate(PSPDate.getPSPTime());
        Application.save(uploadBatch);

        return uploadBatch;
    }

    private int generateNewBatchId() {
        return Application.nextSequenceValue(SequenceId.SEQ_401K_UPLOAD_BATCH_ID, Long.class).intValue();
    }

    private void writeData(FileWriter pFileWriter, String pData) throws IOException {
        if (pData == null) {
            pData = "";
        }
        pFileWriter.write(pData);
    }
    private void writeSecureData(IDPSFileWriter pFileWriter, String pData) throws IOException {
        if (pData == null) {
            pData = "";
        }
        pFileWriter.write(pData);
    }

    private void finalizeUploadBatch(boolean pIsValidBatch, String pFileName, ThirdParty401kBatch pBatch) {
        if (pBatch == null) {
            throw new RuntimeException("TP401k upload batch could not be determined from result set.");
        } else {
            if (pIsValidBatch) {
                pBatch.setFileName(pFileName);
                pBatch.setUploadStatusCd(ThirdParty401kBatchStatusCode.Finalized);
            } else {
                boolean result = new File(pFileName).delete();
                if (!result) {
                    logger.warn("Unable to delete file " + pFileName);
                }
                pBatch.setUploadStatusCd(ThirdParty401kBatchStatusCode.Empty);
            }
            pBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
            Application.save(pBatch);
        }
    }

    public ThirdParty401kBatch getUploadBatch(int pBatchId) {
        Expression<ThirdParty401kBatch> query =
                new Query<ThirdParty401kBatch>()
                        .Where(ThirdParty401kBatch.BatchId().equalTo(pBatchId));

        DomainEntitySet<ThirdParty401kBatch> batchSet = Application.find(ThirdParty401kBatch.class, query);

        return batchSet.isEmpty() ? null : batchSet.get(0);
    }

    public ArrayList<Integer> getBatchIds() {
        return this.mNewBatchIdList;
    }
}

/**
 * Employees that must be included in census:
 * 1 -  if employee has a paycheck that will be uploaded as part of payroll batch, that employee's information must
 * be included in the census
 * <p/>
 * Employees that meet the following criteria will be included:
 * 2 -  all employees of companies that are 'new' 401K companies that have never uploaded an employee census
 * and the employee is not terminated or the employee has been paid this year
 * 3 -  all new employees of existing 401k companies
 * 4 -  existing employees of existing 401k companies that have had their info updated and are non-terminated
 * and have been paid this year
 */
class EmployeeCensusInclusionTest {
    private Company401kInfo company401kInfo;
    private Company company;
    private Employee employee;
    private Boolean isEligible;

    private HashMap<String, Boolean> inclusionTestResults = new HashMap<String, Boolean>();


    EmployeeCensusInclusionTest(Company401kInfo pCompany401kInfo, Company company, Employee employee) {
        this.company401kInfo = pCompany401kInfo;
        this.company = company;
        this.employee = employee;
        isEligible = isEligible();
    }

    private boolean isNew401kCompany() {
        DomainEntitySet<CompanyEvent> companyEvents =
                CompanyEvent.findCompanyEvents(company, EventTypeCode.Employee401kDataUploaded, CompanyEventStatus.Active, false);

        boolean isNew401kCompany = company401kInfo.isNew401kCompany(); 
        inclusionTestResults.put("isNew401kCompany", isNew401kCompany);
        return isNew401kCompany;
    }

    private boolean is401kEmployee() {
        boolean is401kEmployee = employee.getThirdParty401kInfo() != null;
        inclusionTestResults.put("is401kEmployee", is401kEmployee);
        return is401kEmployee;
        }

    private boolean hasEmployeeEverBeenSentToTOK() {
        boolean hasEmployeeEverBeenSentToTOK = company401kInfo.getLastSendDate(employee) != null;
        inclusionTestResults.put("hasEmployeeEverBeenSentToTOK", hasEmployeeEverBeenSentToTOK);
        return hasEmployeeEverBeenSentToTOK;
    }

    private boolean isEmployeeActiveAndNonTerminated() {
        boolean isEmployeeActiveAndNonTerminated = EmployeeStatus.Active.equals(employee.getStatusCd()) && employee
                .getTerminationDate() == null;
        inclusionTestResults.put("isEmployeeActiveAndNonTerminated", isEmployeeActiveAndNonTerminated);
        return isEmployeeActiveAndNonTerminated;
    }

    private boolean hasEmployeeBeenPaidThisYear() {
        SpcfCalendar toDate = PSPDate.getPSPTime();
        SpcfCalendar fromDate = SpcfCalendar.createInstance(toDate.getYear(), 1, 1, SpcfTimeZone.getLocalTimeZone());

        long count = Paycheck.findPaycheckCountByEmployee(company, employee, fromDate, toDate);
        boolean hasEmployeeBeenPaidThisYear = count > 0;
        inclusionTestResults.put("hasEmployeeBeenPaidThisYear", hasEmployeeBeenPaidThisYear);
        return hasEmployeeBeenPaidThisYear;
        }

    private boolean isEmployeeUpdated() {
        boolean isEmployeeUpdated = false;

        if (employee != null) {
            SpcfCalendar lastSendDate = company401kInfo.getLastSendDate(employee);
            if (lastSendDate != null) {
                isEmployeeUpdated = employeeModifiedAfterLastSendDate(lastSendDate)
                        || employeeAddressModifiedAfterLastSendDate(lastSendDate);
            } else {
                inclusionTestResults.put("employeeNeverSent", true);
                isEmployeeUpdated = true;
            }
        }

        if (!inclusionTestResults.containsKey("employeeNeverSent")) {
            inclusionTestResults.put("isEmployeeUpdated", isEmployeeUpdated);
        }
        return isEmployeeUpdated;
    }

    private boolean employeeModifiedAfterLastSendDate(SpcfCalendar lastSendDate) {
        SpcfCalendar lastModifiedDate = employee.getLastTOKModifiedDate();
        boolean employeeModifiedAfterLastSendDate = lastModifiedDate != null && lastModifiedDate.after(lastSendDate);
        inclusionTestResults.put("employeeModifiedAfterLastSendDate", employeeModifiedAfterLastSendDate);
        return employeeModifiedAfterLastSendDate;
    }

    private boolean employeeAddressModifiedAfterLastSendDate(SpcfCalendar lastSendDate) {
        boolean addressModifiedAfterLastSendDate =
                employee != null && employee.getMailingAddress() != null
                        && employee.getMailingAddress().getModifiedDate() != null
                        && employee.getMailingAddress().getModifiedDate().after(lastSendDate);
        inclusionTestResults.put("addressModifiedAfterLastSendDate", addressModifiedAfterLastSendDate);
        return addressModifiedAfterLastSendDate;
    }

    public boolean isEligible() {
        if (isEligible != null) {
            return isEligible;
        }

        isEligible = false;
        boolean new401kCompany = isNew401kCompany();
        if (is401kEmployee()) {
            if (new401kCompany && (isEmployeeActiveAndNonTerminated() || hasEmployeeBeenPaidThisYear()))
                isEligible = true;
            else if (!new401kCompany) {
                if (!hasEmployeeEverBeenSentToTOK() && (isEmployeeActiveAndNonTerminated() || hasEmployeeBeenPaidThisYear())) {
                    isEligible = true;
                } else if (isEmployeeUpdated()) {
                    if (isEmployeeActiveAndNonTerminated())
                        isEligible = true;
                    else if (hasEmployeeBeenPaidThisYear())
                        isEligible = true;
                    else if (hasEmployeeEverBeenSentToTOK())
                        isEligible = true;
                }
            }
        }

        inclusionTestResults.put("isEligible", isEligible);
        return isEligible;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(employee.toString()).append("  ");

        // specifically show eligibility first
        if (inclusionTestResults.containsKey("isEligible")) {
            builder.append("isEligible: ").append(inclusionTestResults.get("isEligible"));
        }

        for (String testName : inclusionTestResults.keySet()) {
            if (!testName.equals("isEligible")) {
                builder.append("  ").append(testName).append(": ").append(inclusionTestResults.get(testName));
            }
        }
        return builder.toString();
    }
}

