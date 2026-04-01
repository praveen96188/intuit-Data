package com.intuit.sbd.payroll.psp.batchjobs.forecast;

/*
 * Copyright (c) 2009 Intuit, Inc. All Rights Reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.Date;

/**
 * User: Jeff Jones
 */
public class ProcessBatchJobForecast {
    private static SpcfLogger logger = Application.getLogger(ProcessBatchJobForecast.class);

    enum JobActions {
        StartPrimaryAchOffloadMonitor,
        NotifyAchOffloadStarted,
        OffloadAchData,
        CreateAchFiles,
        UploadAchFiles,
        DownloadDicrFiles,
        ArchiveDailyFiles,
        InsertFinancialTransactionState,
        CreateTransactionOffloadedEvents
    }

    int mForecastEstimatePadding;
    int mForecastSearchWindow;
    int mForecastLookBackWindow;

    public ProcessBatchJobForecast() {
        mForecastEstimatePadding =
                SystemParameter.findIntValue(SystemParameter.Code.FORECAST_ESTIMATE_PADDING);
        mForecastSearchWindow =
                SystemParameter.findIntValue(SystemParameter.Code.FORECAST_SEARCH_WINDOW);
        mForecastLookBackWindow =
                SystemParameter.findIntValue(SystemParameter.Code.FORECAST_LOOK_BACK_WINDOW);
    }

    public static void main(String args[]) {
        try {
            PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.ForecastBatchJob));

            try {
                PayrollServices.beginUnitOfWork();

                new ProcessBatchJobForecast().processForecast();

                PayrollServices.commitUnitOfWork();
            }
            finally {
                PayrollServices.rollbackUnitOfWork();
            }
        } catch (Throwable ex) {
            logger.fatal("Exception in ProcessBatchJobForecast.main() ", ex);
            System.exit(1);
        }
    }

    public void processForecast() {
        logger.info("Forecast Process Started");

        SpcfCalendar today = PSPDate.getPSPTime();
        CalendarUtils.clearTime(today);

        SpcfCalendar date = PSPDate.getPSPTime();
        CalendarUtils.clearTime(date);
        date.addDays(-10);
        
        //Close previous with the status of Open and is not equal to today
        while (date.before(today)) {
            if (!BatchUtils.isWeekendOrHoliday(date)){
                closeForecasts(Forecast.findForecast(date), date);
            }
            date.addDays(1);
        }

        //Create a forecast if today is not a weekend or holiday.
        if (!BatchUtils.isWeekendOrHoliday(today)) {
            DomainEntitySet<Forecast> forecasts = null;

            forecasts = Forecast.findForecasts(ForecastStatus.Open, today);
            if (forecasts != null && forecasts.isEmpty()) {
                addForecast(today);
            } else {
                updateForecast(forecasts, today);
            }
        }
    }

    private void addForecast(SpcfCalendar pDate) {
        Integer edrCount = getEDRCount(pDate);
        logger.info("EDRCount = " + edrCount);

        Integer mmtCount = getMMTCount(pDate);
        logger.info("MMTCount = " + mmtCount);

        if (edrCount > 0) {
            Forecast forecast = new Forecast();

            forecast.setStatus(ForecastStatus.Open);
            forecast.setRunDate(pDate);
            forecast.setEstimatedTransactionCount(edrCount);
            Application.save(forecast);

            long estimatedTime = 0l;
            ForecastDetail forecastDetail = null;

            for (JobActions jobAction : JobActions.values()) {
                estimatedTime = getEstimatedBatchJobRunTime(edrCount, jobAction.toString(), pDate);
                if (estimatedTime > 0) {
                    forecastDetail = createForecastDetail(jobAction.toString(), estimatedTime, null);
                    forecast.getForecastDetailCollection().add(forecastDetail);
                    forecastDetail.setForecast(forecast);
                    Application.save(forecastDetail);
                }
            }
        }
    }

    private void updateForecast(DomainEntitySet<Forecast> pForecasts, SpcfCalendar pDate) {
        if (pForecasts != null && !pForecasts.isEmpty()) {
            for (Forecast forecast : pForecasts) {
                Integer edrCount = getEDRCount(pDate);
                logger.info("EDRCount = " + edrCount);

                Integer mmtCount = getMMTCount(pDate);
                logger.info("MMTCount = " + mmtCount);

                if (edrCount > 0) {
                    forecast.setRunDate(pDate);
                    forecast.setEstimatedTransactionCount(edrCount);
                    Application.save(forecast);

                    for (ForecastDetail forecastDetail : forecast.getForecastDetailCollection()) {
                        long estimatedTime = getEstimatedBatchJobRunTime(edrCount, forecastDetail.getJobAction(), pDate);
                        if (estimatedTime > 0) {
                            updateForecastDetail(forecastDetail, estimatedTime, null);
                            Application.save(forecastDetail);
                        }
                    }
                }
            }
        }
    }

    private void closeForecasts(Forecast pForecast, SpcfCalendar pDate) {
        if (pForecast != null) {
            if (ForecastStatus.Open.equals(pForecast.getStatus())) {
                OffloadBatch offloadBatch = getOffloadBatch(pDate);
                if (offloadBatch != null) {
                    pForecast.setActualTransactionCount(getEntryDetailRecordTotalCount(offloadBatch.getId()));
                    pForecast.setStatus(ForecastStatus.Closed);
                } else {
                    pForecast.setStatus(ForecastStatus.Error);
                }

                DomainEntitySet<ForecastDetail> forecastDetails = pForecast.getForecastDetailCollection();
                for (ForecastDetail forecastDetail : forecastDetails) {
                    forecastDetail.setActualRunTime(getActualBatchJobRunTime(pDate, forecastDetail.getJobAction()));
                }

                Application.save(pForecast);
            }
        } else {
            //No forecast found, try and add the actuals
            OffloadBatch offloadBatch = getOffloadBatch(pDate);
            if (offloadBatch != null) {
                int entryDetailRecordTotalCount = getEntryDetailRecordTotalCount(offloadBatch.getId());
                if (entryDetailRecordTotalCount > 0) {
                    Forecast forecast = new Forecast();
                    forecast.setActualTransactionCount(entryDetailRecordTotalCount);
                    forecast.setRunDate(getSpcfCalendarWithLocalTimezone(pDate));
                    forecast.setStatus(ForecastStatus.Closed);
                    Application.save(forecast);

                    long actualTime = 0l;
                    ForecastDetail forecastDetail = null;

                    for (JobActions jobAction : JobActions.values()) {
                        actualTime = getActualBatchJobRunTime(pDate, jobAction.toString());
                        if (actualTime > 0) {
                            forecastDetail = createForecastDetail(jobAction.toString(), null, actualTime);
                            forecastDetail.setForecast(forecast);
                            Application.save(forecastDetail);
                        }
                    }
                }
            }
        }
    }

    private DomainEntitySet<BatchJobAuditLog> getBatchJobAuditLog(SpcfCalendar pFromDate, SpcfCalendar pToDate, String JobAction) {
        Expression<BatchJobAuditLog> query =
                new Query<BatchJobAuditLog>()
                        .Where(BatchJobAuditLog.JobAction().equalTo(JobAction)
                                .And(BatchJobAuditLog.CreatedDate().greaterOrEqualThan(pFromDate)
                                .And(BatchJobAuditLog.CreatedDate().lessOrEqualThan(pToDate))))
                        .OrderBy(BatchJobAuditLog.CreatedDate());

        return Application.find(BatchJobAuditLog.class, query);
    }

    private OffloadBatch getOffloadBatch(SpcfCalendar pDate) {
        Expression<OffloadBatch> query =
                new Query<OffloadBatch>()
                       .Where(OffloadBatch.OffloadDate().equalTo(pDate)
                              .And(OffloadBatch.StatusCd().equalTo(OffloadBatchStatus.Completed)
                              .And(OffloadBatch.OffloadGroup().OffloadGroupCd().equalTo("STD"))))
                        .OrderBy(OffloadBatch.OffloadDate());
        DomainEntitySet<OffloadBatch> offloadBatchs = Application.find(OffloadBatch.class, query);

        if (!offloadBatchs.isEmpty()) {
            return offloadBatchs.iterator().next();
        }

        return null;
    }

    private long getElapsedMillis(SpcfCalendar pStart, SpcfCalendar pStop) {
        return pStop.getTimeInMilliseconds() - pStart.getTimeInMilliseconds();
    }

    private int getEntryDetailRecordTotalCount(SpcfUniqueId pOffloadBatchSeq) {
        int totalRecordCount = 0;

        String[] paramNames = new String[1];
        paramNames[0] = "offloadBatchSeq";

        Object[] paramValues = new Object[1];
        paramValues[0] = pOffloadBatchSeq;

        ArrayList returnList = Application.executeNamedQuery("findEntryDetailRecordTotalCount", paramNames, paramValues);

        if (returnList != null && !returnList.isEmpty()) {
            Long totalRecordCountLong = (Long) returnList.iterator().next();
            totalRecordCount = totalRecordCountLong.intValue();
        }

        return totalRecordCount;
    }

    private int calculateAverageTime(DomainEntitySet<ForecastDetail> pForcastDetails) {
        int actualRunTimeAvg = 0;

        if (pForcastDetails != null && !pForcastDetails.isEmpty()) {
            int actualRunTimeSum = 0;
            for (ForecastDetail forecastDetail : pForcastDetails) {
                actualRunTimeSum += forecastDetail.getActualRunTime();
            }
                                                                                                  
            actualRunTimeAvg = actualRunTimeSum / pForcastDetails.size();

            if (mForecastEstimatePadding > 0) {
                actualRunTimeAvg = ((mForecastEstimatePadding * actualRunTimeAvg) / 100) + actualRunTimeAvg;
            }
        }
        return actualRunTimeAvg;
    }

    private int getEstimatedBatchJobRunTime(int pTotalEDRCount, String pJobAction, SpcfCalendar pDate) {
        int txnThreshold = (mForecastSearchWindow * pTotalEDRCount) / 100;

        int fromTxnCount = pTotalEDRCount - txnThreshold;
        int toTxnCount = pTotalEDRCount + txnThreshold; 

        SpcfCalendar fromDate = getSpcfCalendarWithLocalTimezone(pDate);
        fromDate.addDays(mForecastLookBackWindow);

        SpcfCalendar toDate = getSpcfCalendarWithLocalTimezone(pDate);

        DomainEntitySet<ForecastDetail> forecastDetails = ForecastDetail.findForecastDetails(ForecastStatus.Closed, fromTxnCount, toTxnCount, fromDate, toDate, pJobAction);

        return calculateAverageTime(forecastDetails);
    }


    private Long getActualBatchJobRunTime(SpcfCalendar pDate, String pJobAction) {
        Long returnValue = 0l;
        SpcfCalendar startDate = null;
        SpcfCalendar endDate = null;

        SpcfCalendar fromDate = getSpcfCalendarWithLocalTimezone(pDate);

        SpcfCalendar toDate = getSpcfCalendarWithLocalTimezone(pDate);
        toDate.addDays(1);
        toDate.addMilliseconds(-1);

        DomainEntitySet<BatchJobAuditLog> batchJobAuditLogs = getBatchJobAuditLog(fromDate, toDate, pJobAction);

        for (BatchJobAuditLog batchJobAuditLog : batchJobAuditLogs) {
            if ("Started".equals(batchJobAuditLog.getMessage())) {
                startDate = batchJobAuditLog.getCreatedDate();
            } else if ("Finished".equals(batchJobAuditLog.getMessage())) {
                endDate = batchJobAuditLog.getCreatedDate();
            }
            if (startDate != null && endDate != null) {
                break;
            }
        }
        if ((startDate != null && endDate != null) &&
                (startDate.before(endDate))) {
            returnValue =  getElapsedMillis(startDate, endDate);
        }
        return returnValue;
    }

    private ForecastDetail createForecastDetail(String pJobAction,
                                                Long pEstimatedTime,
                                                Long pActualTime) {
        ForecastDetail forecastDetail = new ForecastDetail();

        forecastDetail.setJobAction(pJobAction);
        if (pEstimatedTime != null) {
            forecastDetail.setEstimatedRunTime(pEstimatedTime);
        }
        if (pActualTime != null) {
            forecastDetail.setActualRunTime(pActualTime);
        }

        return forecastDetail;
    }

    private void updateForecastDetail(ForecastDetail pForecastDetail,
                                                Long pEstimatedTime,
                                                Long pActualTime) {
        if (pEstimatedTime != null) {
            pForecastDetail.setEstimatedRunTime(pEstimatedTime);
        }
        if (pActualTime != null) {
            pForecastDetail.setActualRunTime(pActualTime);
        }
    }

    private int getEDRCount(SpcfCalendar pDate) {
        Integer returnValue = 0;

        pDate = pDate.toUtc();

        String[] paramNames = new String[1];
        paramNames[0] = "inputDate";

        String[] paramValues = new String[1];
        paramValues[0] = pDate.toISO8601(); //("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        ArrayList arrayList = Application.executeNamedQuery("findEntryDetailRecordTotalCountByDate", paramNames, paramValues);
        if (arrayList != null && !arrayList.isEmpty()) {
            returnValue = (Integer) arrayList.iterator().next();
        }

        return returnValue;
    }

    private int getMMTCount(SpcfCalendar pDate) {
        Integer returnValue = 0;
        Date date = CalendarUtils.convertToDate(pDate);

        String[] paramNames = new String[1];
        paramNames[0] = "inputDate";

        Object[] paramValues = new Object[1];
        paramValues[0] = date;

        ArrayList arrayList = Application.executeNamedQuery("findMMTTotalCountByDate", paramNames, paramValues);
        if (arrayList != null && !arrayList.isEmpty()) {
            returnValue = (Integer) arrayList.iterator().next();
        }

        return returnValue;
    }

    public static SpcfCalendar getSpcfCalendarWithLocalTimezone(SpcfCalendar pDate) {
        return SpcfCalendar.createInstance(pDate.getYear(),
                pDate.getMonth(), pDate.getDay(), pDate.getHour(), pDate.getMinute(),
                pDate.getSecond(), pDate.getMillisecond(), SpcfTimeZone.getLocalTimeZone());
    }
}
