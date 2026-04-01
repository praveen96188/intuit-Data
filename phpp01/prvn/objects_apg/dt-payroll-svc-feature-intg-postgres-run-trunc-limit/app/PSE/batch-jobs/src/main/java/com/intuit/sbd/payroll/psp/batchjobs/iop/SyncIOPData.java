package com.intuit.sbd.payroll.psp.batchjobs.iop;


import com.intuit.onlinepayroll.webservices.v1.ContractorPaymentCompanyModel;
import com.intuit.onlinepayroll.webservices.v1.PayrollCompanyModel;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.iop.IIOPGateway;

import com.intuit.sbd.payroll.psp.gateways.iop.IOPGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.iop.exceptions.ServiceUnavailableException;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Jeff Jones
 */
public class SyncIOPData {

    private int mInterval;
    private int mMinPoolSize;
    private int mMaxPoolSize;
    private int mMaxWait;

    private static SpcfLogger logger = PayrollServices.getLogger(SyncIOPData.class);

    public SyncIOPData() {
        readConfigurationParameters();
    }

    public void process() throws Exception {
        ExecutorService threadPool = null;
        Boolean threadPoolShutDownInitiated = Boolean.FALSE;
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.IOPSyncBatchJob));

            Long token;
            boolean shouldSyncBillPayments = false;
            try {
                shouldSyncBillPayments = Boolean.parseBoolean(SourcePayrollParameter.findValue(SourceSystemCode.IOP,
                                                                                               SourcePayrollParameterCode.SyncBillPayments));
                token = SystemParameter.findLongValue(SystemParameter.Code.IOP_SYNC_TOKEN);
            } catch (Exception e) {
                token = createTokenSystemParameter(PSPDate.getPSPTime().toLocal());
            }

            final SpcfCalendar sCal = SpcfCalendar.createInstance(token).toLocal();
            final SpcfCalendar eCal = calculateEndTimeWithinTimeWindow(sCal, PSPDate.getPSPTime().toLocal());
            if (eCal == null || eCal.before(sCal) || eCal.equals(sCal)) {
                logger.info("IOP: Start time is after end time. Exiting...");
                return;
            }

            final IIOPGateway iopGateway = IOPGatewayFactory.createInstance();

            threadPool = new ThreadPoolExecutor(mMinPoolSize, mMaxPoolSize, mInterval,
                                                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            CompletionService<Boolean> completionService = new ExecutorCompletionService<Boolean>(threadPool);

            // Get list of companies being updated

            logger.info(String.format("IOP: Getting list of companies with payroll activity, StartDate: %s, EndDate %s",
                                      sCal.toString(), eCal.toString()));
            final List<Integer> payrollCompanyList = iopGateway.getCompaniesWithPayrollActivity(sCal, eCal);


            logger.info(String.format("IOP returned %s company/s with payrolls to process, StartDate: %s, EndDate %s, CompanyIds %s",
                                      String.valueOf(payrollCompanyList.size()), sCal.toString(), eCal.toString(), payrollCompanyList.toString()));

            final List<Integer> contractorCompanyList;

            if (shouldSyncBillPayments) {
                logger.info(String.format("IOP: Getting list of companies with contractor payment activity, StartDate: %s, EndDate %s",
                                          sCal.toString(), eCal.toString()));
                contractorCompanyList = iopGateway.getCompaniesWithContractorPaymentActivity(sCal, eCal);

                logger.info(String.format("IOP returned %s company/s with payments to process, StartDate: %s, EndDate %s, CompanyIds %s",
                                          String.valueOf(contractorCompanyList.size()), sCal.toString(), eCal.toString(), contractorCompanyList.toString()));
            } else {
                contractorCompanyList = new ArrayList<Integer>();

                logger.info("Skipping sync of bill payments");
            }

            // Create list of companies being updated so companies get created at the same time
            HashSet<Integer> companyIdsToUpdate = new HashSet<Integer>();

            for (Integer companyId : payrollCompanyList) {
                companyIdsToUpdate.add(companyId);
            }

            for (Integer companyId : contractorCompanyList) {
                companyIdsToUpdate.add(companyId);
            }

            saveCompanyList(companyIdsToUpdate, payrollCompanyList,contractorCompanyList,sCal,eCal);

            logger.info(String.format("IOP returned %s unique company/s with payrolls or payments to process, StartDate: %s, EndDate %s",
                                      String.valueOf(companyIdsToUpdate.size()), sCal.toString(), eCal.toString()));

            DomainEntitySet<IOPSyncCompany> iopSyncCompanyList = IOPSyncCompany.findPendingCompanyList();
            logger.info(String.format("IOPSync Job started processing %s company/s with payrolls or payments to process",
                                      String.valueOf(iopSyncCompanyList.size())));
            final AtomicInteger failureCount = new AtomicInteger();
            final AtomicInteger batchCount = new AtomicInteger();
            // Run Sync company payrolls
            for (final IOPSyncCompany iopSyncCompany : iopSyncCompanyList) {

                completionService.submit(new Callable<Boolean>() {

                    public Boolean call() {
                        Boolean result = Boolean.FALSE;
                        try {

                            PayrollCompanyModel payrollCompanyModel = null;
                            String companyId = String.valueOf(iopSyncCompany.getCompanyId());
                            SpcfCalendar startTime = iopSyncCompany.getStartTime();
                            SpcfCalendar endTime = iopSyncCompany.getEndTime();
                            if (startTime == null) {
                                startTime = sCal;
                            }
                            if (endTime == null) {
                                endTime = eCal;
                            }
                            if (iopSyncCompany.getHasEmployeePayroll()) {
                                int paycheckCount = 0;
                                int employeeCount = 0;
                                logger.info(String.format("IOP: Getting paycheck details for  %s company with payrolls to process, StartDate: %s, EndDate %s",
                                                          companyId, startTime.toString(), endTime.toString()));
                                // Check if this company has paychecks
                                payrollCompanyModel = iopGateway.getPaychecksEmployeesCompanyDetails(Long.valueOf(companyId), startTime, endTime);
                                paycheckCount = payrollCompanyModel == null ? 0 : payrollCompanyModel.getPaychecks().size();
                                employeeCount = payrollCompanyModel == null ? 0 : payrollCompanyModel.getEmployees().size();

                                logger.info(String.format("IOP: Received paycheck details for  %s company with payrolls to process, paycheck count: %s, Employee count %s",
                                                          companyId, String.valueOf(paycheckCount), String.valueOf(employeeCount)));
                            }

                            ContractorPaymentCompanyModel contractorPaymentCompanyModel = null;

                            if (iopSyncCompany.getHasContractorPayment()) {
                                int paymentCount = 0;
                                int contractorCount = 0;
                                logger.info(String.format("IOP: Getting contractor payment details for  %s company with payments to process, StartDate: %s, EndDate %s",
                                                          companyId, startTime.toString(), endTime.toString()));
                                // Check if this company has payments
                                contractorPaymentCompanyModel = iopGateway.getContractorPaymentCompanyModel(Long.valueOf(companyId), startTime, endTime);
                                paymentCount = (contractorPaymentCompanyModel == null) ? 0 : contractorPaymentCompanyModel.getContractorPayments().size();
                                contractorCount = (contractorPaymentCompanyModel == null) ? 0 : contractorPaymentCompanyModel.getContractors().size();
                                logger.info(String.format("IOP: Received contractor payment details for  %s company with payments to process, Payments count: %s, Contractor count %s",
                                                          companyId, String.valueOf(paymentCount), String.valueOf(contractorCount)));
                            }

                            final IOPProcessor iopProcessor = new IOPProcessor(Integer.valueOf(companyId), payrollCompanyModel, contractorPaymentCompanyModel, startTime, endTime);

                            try {
                                result = iopProcessor.processCompany();
                            } catch (Exception ex) {
                                result = Boolean.FALSE;
                                logger.info("IOPSyncError: Unable to process data for company " + companyId, ex);
                            }

                        } catch (Exception ex) {
                            result = Boolean.FALSE;
                            failureCount.incrementAndGet();
                            logger.info("IOPSyncError: Unable to get sync data for company " + iopSyncCompany.getCompanyId(), ex);
                        }

                        if (Boolean.FALSE.equals(result)) {
                            iopSyncCompany.setStatus(IOPSyncStatus.Failed);
                        } else {
                            iopSyncCompany.setStatus(IOPSyncStatus.Synced);
                        }
                        iopSyncCompany.setRetryCount(iopSyncCompany.getRetryCount() + 1);
                        Application.save(iopSyncCompany);

                        if (batchCount.incrementAndGet() % 100 == 0) {
                            Application.getHibernateSession().flush();
                        }
                        return result;
                    }
                });

            }

            if (threadPool != null) {
                if (mMaxWait <= 0) {
                    ThreadingUtils.shutdownAndAwaitTermination(threadPool, mInterval);
                } else {
                    ThreadingUtils.shutdownAndAwaitTermination(threadPool, mInterval, mMaxWait);
                }
                threadPoolShutDownInitiated = Boolean.TRUE;
            }

            PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.IOP_SYNC_TOKEN, String.valueOf(eCal.getTimeInMilliseconds()));
            if (failureCount.get() == 0) { // All companies payroll models retrieved successfully
                logger.info("Completed Syncing IOP data.");
                logger.info("IOP: Successfully retrieved all company models");
            } else {
                logger.warn("IOP: IOPSyncError-Critical: # Companies Failed - " + failureCount.get());
            }
        } catch (ServiceUnavailableException e) {
            logger.error("Error: IOP Service is not available. ", e);
        } catch (Exception e) {
            logger.warn("Error: IOPDataSync failed.", e);
        } finally {
            if (threadPoolShutDownInitiated.equals(Boolean.FALSE) && threadPool != null) {
                if (mMaxWait <= 0) {
                    ThreadingUtils.shutdownAndAwaitTermination(threadPool, mInterval);
                } else {
                    ThreadingUtils.shutdownAndAwaitTermination(threadPool, mInterval, mMaxWait);
                }
            }
        }
    }



    /**
     *
     * @return  int
     */
    public static int getIOPSynchEndTimeCalcluationValue() {
        int endTimeCalculationToken;
        try {
            endTimeCalculationToken = SystemParameter.findIntValue(SystemParameter.Code.IOP_SYNC_END_TIME_CALCULATION_TOKEN);
            if (endTimeCalculationToken < 0) {
                logger.info("IOP: IOP_SYNC_END_TIME_CALCULATION_TOKEN has -ve value of " + endTimeCalculationToken + ".At present -ve value is not supported. Default value 0 will be considered.");
                endTimeCalculationToken = 0;
            }
        } catch (Exception e) {
            logger.info("IOP: IOP_SYNC_END_TIME_CALCULATION_TOKEN has invalid value. Default value 0 will be considered.");
            endTimeCalculationToken = 0;
        }
        return endTimeCalculationToken;
    }
    public static long getIOPSynchMaxTimeWindowValue() {
        long maxEndTimeCalculationToken =0;
        try {
            maxEndTimeCalculationToken = SystemParameter.findIntValue(SystemParameter.Code.IOP_SYNC_MAX_TIME_WINDOW_TOKEN,0);
            if (maxEndTimeCalculationToken < 0) {
                maxEndTimeCalculationToken = 0;
            }
        } catch (Exception e) {
            maxEndTimeCalculationToken = 0;
        }
        return maxEndTimeCalculationToken;
    }

    private static Long createTokenSystemParameter(SpcfCalendar pSpcfCalendar) {
        Long token = pSpcfCalendar.getTimeInMilliseconds();

        SystemParameter systemParameter = new SystemParameter();
        systemParameter.setSystemParameterCd(SystemParameter.Code.IOP_SYNC_TOKEN.toString());
        systemParameter.setSystemParameterDescription("Token used to sync data from IOP to PSP");
        systemParameter.setSystemParameterOrg("PSP");
        systemParameter.setSystemParameterValue(token.toString());
        Application.save(systemParameter);

        return token;
    }

    private void readConfigurationParameters() {
        //mRequestTimeout = SystemParameter.findIntValue(SystemParameter.Code.IOP_REQUEST_TIMEOUT, 10000);
        mInterval = SystemParameter.findIntValue(SystemParameter.Code.IOP_SYNC_THREAD_POOL_INTERVAL, 60);
        mMaxWait = SystemParameter.findIntValue(SystemParameter.Code.IOP_SYNC_THREAD_POOL_MAX_WAIT, 5 * 60);
        mMinPoolSize = SystemParameter.findIntValue(SystemParameter.Code.IOP_SYNC_MIN_THREAD_POOL_SIZE, 10);
        mMaxPoolSize = SystemParameter.findIntValue(SystemParameter.Code.IOP_SYNC_MAX_THREAD_POOL_SIZE, 40);
    }

    /**
     *
     * @param sCal
     * @param eCal
     * @return
     */
    public static SpcfCalendar calculateEndTimeWithinTimeWindow(SpcfCalendar sCal, SpcfCalendar eCal) {
        if (eCal == null || sCal == null) {
            return null;
        }
        int endTimeCalculationToken = getIOPSynchEndTimeCalcluationValue();
        eCal = eCal.copy();
        // Substract 5(endTimeCalculationToken) minutes.  This is to give enough time for transactions to commit on IOP
        if (endTimeCalculationToken > 0) {
            eCal.addMinutes(-endTimeCalculationToken);
        }
        if (eCal.before(sCal) || eCal.equals(sCal)) {
            return null;
        }
        long timeWindowInMinute = eCal.subtract(sCal) / (1000 * 60);
        long maxEndTimeCalculationToken = getIOPSynchMaxTimeWindowValue();
        if (maxEndTimeCalculationToken > 0 && timeWindowInMinute > 0 && timeWindowInMinute >= maxEndTimeCalculationToken) {
            eCal = sCal.copy();
            eCal.addMinutes((int) maxEndTimeCalculationToken);
        }
        return eCal;
    }

    /**
     *
     * @param pCompanyListToSave
     * @param pEmployeePayrollCompanyList
     * @param pContractorPaymentCompanyList
     * @param sCal
     * @param eCal
     */
    private static void saveCompanyList(HashSet<Integer> pCompanyListToSave, List<Integer> pEmployeePayrollCompanyList, List<Integer> pContractorPaymentCompanyList,SpcfCalendar sCal,SpcfCalendar eCal) {
        int batchCount = 0;
        Boolean hasEmployeePayroll;
        Boolean hasContractorPayment;
        for (Integer companyId : pCompanyListToSave) {
            if (pEmployeePayrollCompanyList != null && pEmployeePayrollCompanyList.size() > 0 && pEmployeePayrollCompanyList.contains(companyId)) {
                hasEmployeePayroll = Boolean.TRUE;
            } else {
                hasEmployeePayroll = Boolean.FALSE;
            }
            if (pContractorPaymentCompanyList != null && pContractorPaymentCompanyList.size() > 0 && pContractorPaymentCompanyList.contains(companyId)) {
                hasContractorPayment = Boolean.TRUE;
            } else {
                hasContractorPayment = Boolean.FALSE;
            }
            IOPSyncCompany iopCompany = new IOPSyncCompany(companyId, hasEmployeePayroll, hasContractorPayment);
            if (sCal != null) {
                iopCompany.setStartTime(sCal);
            }
            if (eCal != null) {
                iopCompany.setEndTime(eCal);
            }
            Application.save(iopCompany);
            if (++batchCount % 100 == 0) {
                Application.getHibernateSession().flush();
            }
        }
        Application.getHibernateSession().flush();
    }
}
