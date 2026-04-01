package com.intuit.sbd.payroll.psp.batchjobs.ATFDataExtract;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.ThreadingUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.context.model.RequestType;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jul 18, 2008
 * Time: 4:16:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateFilingsSpecificTransactions {
    private static SpcfLogger logger = Application.getLogger(CreateFilingsSpecificTransactions.class);

    public static final String UPDATE = "UPDATE";
    public static final String ALL = "ALL";

    private PSPRequestContextManager pspRequestContextManager;

    public CreateFilingsSpecificTransactions() {
        pspRequestContextManager = PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    public static void main(String args[]) {
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AtfDataExtractBatchJob));

            // CreateFilingsSpecificTransactions process
            try {
                PayrollServices.beginUnitOfWork();

                if (args.length == 1 && args[0].equals(UPDATE)) {
                    new CreateFilingsSpecificTransactions().processCompanyDailyLiabilities(UPDATE, null, null);
                } else if (args.length == 3 && args[0].equals(ALL)) {
                    int yearVal = Integer.parseInt(args[1]);

                    int quarterVal =  Integer.parseInt(args[2]);
                    if (quarterVal < 0 || quarterVal > 4) {
                        throw new RuntimeException("Invalid Parameter: '"+args[2]+"'. Please enter a valid Quarter ");
                    }

                    SpcfCalendar firstDayOfQtr = CalendarUtils.getFirstDayOfQuarter(yearVal, quarterVal);
                    SpcfCalendar lastDayOfQtr = CalendarUtils.getLastDayOfQuarter(yearVal, quarterVal);

                    new CreateFilingsSpecificTransactions().processCompanyDailyLiabilities(ALL, firstDayOfQtr, lastDayOfQtr);
                } else {
                    new CreateFilingsSpecificTransactions().processCompanyDailyLiabilities(UPDATE, null, null);
                }


                PayrollServices.commitUnitOfWork();
                logger.info("After commit in CreateFilingsSpecificTransactions");
            }
            finally {
                PayrollServices.rollbackUnitOfWork();
            }
        }
        catch (Throwable ex) {
            logger.fatal("Exception in CreateFilingsSpecificTransactions.main() ", ex);
            System.exit(1);
        }
    }

    public void updateCompanyDailyLiabilities() {
        processCompanyDailyLiabilities("UPDATE", null, null);
    }

    private void multiThreadCDLCreation(final HashMap<SpcfUniqueId, List<SpcfCalendar>> pCompaniesWithQuartersToRecalculate, final SpcfCalendar startTime) {
        StopWatch sw = new StopWatch().start();
        ExecutorService executor = null;

        final ConcurrentHashMap<SpcfUniqueId, List<SpcfCalendar>> companiesWithQuartersToRecalculate = new ConcurrentHashMap<SpcfUniqueId, List<SpcfCalendar>>(pCompaniesWithQuartersToRecalculate);

        try {
            int threadCount = SystemParameter.findIntValue(SystemParameter.Code.FILING_SPECIFIC_NUM_THREADS, 8);
            executor = Executors.newFixedThreadPool(threadCount);

            logger.info("beginning multi-threading for company daily liabilities...");
            CompletionService<Boolean> completionService = new ExecutorCompletionService<Boolean>(executor);
            for (final SpcfUniqueId currCompanyId : companiesWithQuartersToRecalculate.keySet()) {
                completionService.submit(new Callable<Boolean>() {
                    public Boolean call() throws Exception {
                        try {
                            recalculateDailyLiabilitiesForCompanyAndQuarters(currCompanyId, companiesWithQuartersToRecalculate.get(currCompanyId), startTime);
                        } catch (Throwable t) {
                            return false;
                        }
                        return true;
                    }
                });
            }

            int totalCompaniesWithQuartersToProcess = companiesWithQuartersToRecalculate.size();
            int totalExceptionsCaught=0;
            int totalSuccess = 0;

            //todo: system parameter
            int exceptionsThreshold = 10;

            for (int t = 0, n = totalCompaniesWithQuartersToProcess; t < n; t++) {
                Future<Boolean> f = completionService.take();
                Boolean success = f.get();

                if (!success) {
                    logger.info("Total exceptions so far: "+ ++totalExceptionsCaught);
                    if (totalExceptionsCaught > exceptionsThreshold) {
                        throw new Throwable("Caught too many exceptions in CreateFilingsSpecificTransactions; assuming unrecoverable error is occurring and quitting now");
                    }
                } else {
                    totalSuccess++;
                }

                if (totalSuccess % 500 == 0) {
                    System.out.println("completed " + totalSuccess + " in " + sw.getElapsedTimeString());
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            logger.fatal("Encountered unrecoverable error during processing");
        } finally {
            ThreadingUtils.shutdownAndAwaitTermination(executor, 10, 300);
        }
    }

    private void recalculateDailyLiabilitiesForCompanyAndQuarters(SpcfUniqueId pCompanyId, List<SpcfCalendar> pQuartersToProcess, SpcfCalendar startTime) throws Throwable {
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AtfDataExtractBatchJob));

            PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
            Company currCompany = Application.findById(Company.class, pCompanyId);
            pspRequestContextManager.setRequestContext(currCompany, RequestType.OLAP, "ATFDataExtract");
            for (SpcfCalendar currQuarterStartDate : pQuartersToProcess) {
                currCompany.recalculateDailyLiabilities(currQuarterStartDate, startTime);
            }

            PayrollServices.commitUnitOfWork();
        } catch (Throwable t) {
            logger.error(t);
            throw t;
        } finally {
            PayrollServices.rollbackUnitOfWork();
            pspRequestContextManager.clearRequestContext();
        }
    }

    public void processCompanyDailyLiabilities(String processType, SpcfCalendar quarterStartDate, SpcfCalendar quarterEndDate) {
        logger.info("Company Daily Liability Process Started with parameters: "+processType+" quarterStartDate: "+ quarterStartDate+" quarterEndDate: "+quarterEndDate);

        SpcfCalendar startTime = PSPDate.getPSPTime().copy();
        String lastEventProcessedTimestamp = SystemParameter.findStringValue(SystemParameter.Code.FILING_SPECIFIC_TRANSACTIONS_TOKEN);
        SpcfCalendar lastRunDate = SpcfCalendar.createInstance(Long.parseLong(lastEventProcessedTimestamp), SpcfTimeZone.getLocalTimeZone());
        logger.info("Last run date/time: "+lastRunDate);

        HashMap<SpcfUniqueId, List<SpcfCalendar>> companiesAndQuartersToProcess = null;

        if (processType.equals(ALL)) {
            logger.info("Finding ALL fileable companies");
            ArrayList<SpcfUniqueId> companiesToProcess = Tax.getAllTaxCompanies();
            companiesAndQuartersToProcess = new HashMap<SpcfUniqueId, List<SpcfCalendar>>();
            for (SpcfUniqueId currCompanyId : companiesToProcess) {
                ArrayList<SpcfCalendar> quarterList = new ArrayList<SpcfCalendar>();
                quarterList.add(quarterStartDate);
                companiesAndQuartersToProcess.put(currCompanyId, quarterList);
            }

        } else {
            logger.info("Finding UPDATED fileable companies");
            companiesAndQuartersToProcess = getUpdatedFileableCompanies(lastRunDate);
        }

        logger.info("Recalculating daily liabilities for "+companiesAndQuartersToProcess.size()+" companies");
        multiThreadCDLCreation(companiesAndQuartersToProcess, startTime);
        logger.info("Done recalculating daily liabilities for "+companiesAndQuartersToProcess.size()+" companies");

        ProcessResult pr = PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.FILING_SPECIFIC_TRANSACTIONS_TOKEN, Long.toString(startTime.getTimeInMilliseconds()));

        logger.info("Completed business logic for Company Daily Liability Update");
    }

    private HashMap<SpcfUniqueId, List<SpcfCalendar>> getUpdatedFileableCompanies(SpcfCalendar pLastRunDate) {
        HashMap<SpcfUniqueId, List<SpcfCalendar>>  companyMap = new HashMap<SpcfUniqueId, List<SpcfCalendar>>();

        List<Object[]> updatedCompanies = Tax.getFileableCompanyLiabilities(pLastRunDate);
        updatedCompanies.addAll(Tax.getFileableCompanyAdjustments(pLastRunDate));

        for (Object[] currentObject : updatedCompanies) {
            SpcfUniqueId currentCompanyId = (SpcfUniqueId) currentObject[0];
            SpcfCalendar liabilityDate = (SpcfCalendar) currentObject[1];

            SpcfCalendar quarterStartDate = CalendarUtils.getFirstDayOfQuarter(liabilityDate);

            List<SpcfCalendar> quarterList = companyMap.get(currentCompanyId);
            if (quarterList == null) {
                quarterList = new ArrayList<SpcfCalendar>();
                quarterList.add(quarterStartDate);
                companyMap.put(currentCompanyId, quarterList);
            } else if (!quarterList.contains(quarterStartDate)) {
                quarterList.add(quarterStartDate);
                companyMap.put(currentCompanyId, quarterList);
            }
        }

        return companyMap;
    }
}