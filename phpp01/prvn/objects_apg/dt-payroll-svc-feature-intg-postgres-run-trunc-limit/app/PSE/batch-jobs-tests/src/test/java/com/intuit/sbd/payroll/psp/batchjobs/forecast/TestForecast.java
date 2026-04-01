package com.intuit.sbd.payroll.psp.batchjobs.forecast;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DDTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: Sep 10, 2009
 * Time: 10:23:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestForecast {


    private static Company1Dataloader c1dl;

    private static final String OFFLOAD_ACH_DATA = "OffloadAchData";
    private static final String CREATE_ACH_FILES = "CreateAchFiles";
    private static final String UPLOAD_ACH_FILES = "UploadAchFiles";
    private static final String DOWNLOAD_DICR_FILES = "DownloadDicrFiles";
    private static final String ARCHIVE_DAILY_FILES = "ArchiveDailyFiles";
    private static final String INSERT_FINANCIAL_TRANSACTION_STATE = "InsertFinancialTransactionState";
    private static final String CREATE_TRANSACTION_OFFLOADED_EVENTS = "CreateTransactionOffloadedEvents";

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        Application.updateTables();
        ApplicationSecondary.truncateTables();
        PayrollServices.beginUnitOfWork();
        c1dl = new Company1Dataloader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testForecast_OnePayrollOffloadedOnePayrollPending() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090831170500");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 5, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, OFFLOAD_ACH_DATA, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 6, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, OFFLOAD_ACH_DATA, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 6, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, UPLOAD_ACH_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 11, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, UPLOAD_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 12, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 25, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Finished");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090910000000");
        PayrollServices.commitUnitOfWork();

        SpcfCalendar date = PSPDate.getPSPTime();
        date.addDays(-10);

        //Create Company
        PayrollServices.beginUnitOfWork();
        c1dl.persistCompany1();
        c1dl.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        for (int i = 1; i <= 2; i++) {
            if (!CalendarUtils.isWeekendOrHoliday(date)) {
                createPayroll(i, date);
                if (i != 2) {
                    runAchOffload(date);
                }
            }
            date.addDays(1);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090901000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessBatchJobForecast().processForecast();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Forecast> forecasts = Forecast.findForecasts(ForecastStatus.Error, null);
        assertTrue(forecasts.isEmpty());

        SpcfCalendar findDate = SpcfCalendar.createInstance(2009, 8, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        forecasts = Forecast.findForecasts(ForecastStatus.Closed, findDate);
        assertEquals(1, forecasts.size());

        Forecast forecast = forecasts.iterator().next();
        assertEquals(6, forecast.getActualTransactionCount());

        DomainEntitySet<ForecastDetail> forecastDetails = forecast.getForecastDetailCollection();
        assertEquals("Forecast Details", 7,forecastDetails.size());

        for (ForecastDetail forecastDetail : forecastDetails) {
            if (OFFLOAD_ACH_DATA.equals(forecastDetail.getJobAction())) {
                assertEquals(60000,forecastDetail.getActualRunTime() );
            }

            if (CREATE_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(240000,forecastDetail.getActualRunTime());
            }

            if (UPLOAD_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (DOWNLOAD_DICR_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (ARCHIVE_DAILY_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (INSERT_FINANCIAL_TRANSACTION_STATE.equals(forecastDetail.getJobAction())) {
                assertEquals(360000, forecastDetail.getActualRunTime());
            }

            if (CREATE_TRANSACTION_OFFLOADED_EVENTS.equals(forecastDetail.getJobAction())) {
                assertEquals(300000, forecastDetail.getActualRunTime());
            }
        }

        forecasts = Forecast.findForecasts(ForecastStatus.Open, null);
        assertEquals(1, forecasts.size());

        forecast = forecasts.iterator().next();
        assertEquals(6, forecast.getEstimatedTransactionCount());

        forecastDetails = forecast.getForecastDetailCollection();
        assertEquals(7, forecastDetails.size());

        for (ForecastDetail forecastDetail : forecastDetails) {
            if (OFFLOAD_ACH_DATA.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (CREATE_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(264000, forecastDetail.getEstimatedRunTime());
            }

            if (UPLOAD_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (DOWNLOAD_DICR_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (ARCHIVE_DAILY_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (INSERT_FINANCIAL_TRANSACTION_STATE.equals(forecastDetail.getJobAction())) {
                assertEquals(396000, forecastDetail.getEstimatedRunTime());
            }

            if (CREATE_TRANSACTION_OFFLOADED_EVENTS.equals(forecastDetail.getJobAction())) {
                assertEquals(330000, forecastDetail.getEstimatedRunTime());
            }
        }

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testForecast_OneMilliSecJobAction() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090831170500");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 5, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 5, 0, 1, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, UPLOAD_ACH_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 11, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, UPLOAD_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 12, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 25, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Finished");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090910000000");
        PayrollServices.commitUnitOfWork();

        SpcfCalendar date = PSPDate.getPSPTime();
        date.addDays(-10);

        //Create Company
        PayrollServices.beginUnitOfWork();
        c1dl.persistCompany1();
        c1dl.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        for (int i = 1; i <= 2; i++) {
            if (!CalendarUtils.isWeekendOrHoliday(date)) {
                createPayroll(i, date);
                if (i != 2) {
                    runAchOffload(date);
                }
            }
            date.addDays(1);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090901000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessBatchJobForecast().processForecast();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Forecast> forecasts = Forecast.findForecasts(ForecastStatus.Error, null);
        assertTrue(forecasts.isEmpty());

        SpcfCalendar findDate = SpcfCalendar.createInstance(2009, 8, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        forecasts = Forecast.findForecasts(ForecastStatus.Closed, findDate);
        assertEquals(1, forecasts.size());

        Forecast forecast = forecasts.iterator().next();
        assertEquals(6, forecast.getActualTransactionCount());

        DomainEntitySet<ForecastDetail> forecastDetails = forecast.getForecastDetailCollection();
        assertEquals(6, forecastDetails.size());

        for (ForecastDetail forecastDetail : forecastDetails) {
            if (CREATE_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(1, forecastDetail.getActualRunTime());
            }

            if (UPLOAD_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (DOWNLOAD_DICR_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (ARCHIVE_DAILY_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (INSERT_FINANCIAL_TRANSACTION_STATE.equals(forecastDetail.getJobAction())) {
                assertEquals(360000, forecastDetail.getActualRunTime());
            }

            if (CREATE_TRANSACTION_OFFLOADED_EVENTS.equals(forecastDetail.getJobAction())) {
                assertEquals(300000, forecastDetail.getActualRunTime());
            }
        }

        forecasts = Forecast.findForecasts(ForecastStatus.Open, null);
        assertEquals(1, forecasts.size());

        forecast = forecasts.iterator().next();
        assertEquals(6, forecast.getEstimatedTransactionCount());

        forecastDetails = forecast.getForecastDetailCollection();
        assertEquals(6, forecastDetails.size());

        for (ForecastDetail forecastDetail : forecastDetails) {
            if (CREATE_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(1, forecastDetail.getEstimatedRunTime());
            }

            if (UPLOAD_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (DOWNLOAD_DICR_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (ARCHIVE_DAILY_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (INSERT_FINANCIAL_TRANSACTION_STATE.equals(forecastDetail.getJobAction())) {
                assertEquals(396000, forecastDetail.getEstimatedRunTime());
            }

            if (CREATE_TRANSACTION_OFFLOADED_EVENTS.equals(forecastDetail.getJobAction())) {
                assertEquals(330000, forecastDetail.getEstimatedRunTime());
            }
        }

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testForecast_MissingStartedRow() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090831170500");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 5, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 11, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, UPLOAD_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 12, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 25, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Finished");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090910000000");
        PayrollServices.commitUnitOfWork();

        SpcfCalendar date = PSPDate.getPSPTime();
        date.addDays(-10);

        //Create Company
        PayrollServices.beginUnitOfWork();
        c1dl.persistCompany1();
        c1dl.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        for (int i = 1; i <= 2; i++) {
            if (!CalendarUtils.isWeekendOrHoliday(date)) {
                createPayroll(i, date);
                if (i != 2) {
                    runAchOffload(date);
                }
            }
            date.addDays(1);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090901000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessBatchJobForecast().processForecast();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Forecast> forecasts = Forecast.findForecasts(ForecastStatus.Error, null);
        assertTrue(forecasts.isEmpty());

        SpcfCalendar findDate = SpcfCalendar.createInstance(2009, 8, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        forecasts = Forecast.findForecasts(ForecastStatus.Closed, findDate);
        assertEquals(1, forecasts.size());

        Forecast forecast = forecasts.iterator().next();
        assertEquals(6, forecast.getActualTransactionCount());

        DomainEntitySet<ForecastDetail> forecastDetails = forecast.getForecastDetailCollection();
        assertEquals(5, forecastDetails.size());

        for (ForecastDetail forecastDetail : forecastDetails) {
            if (CREATE_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(300000, forecastDetail.getActualRunTime());
            }

            if (DOWNLOAD_DICR_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (ARCHIVE_DAILY_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (INSERT_FINANCIAL_TRANSACTION_STATE.equals(forecastDetail.getJobAction())) {
                assertEquals(360000, forecastDetail.getActualRunTime());
            }

            if (CREATE_TRANSACTION_OFFLOADED_EVENTS.equals(forecastDetail.getJobAction())) {
                assertEquals(300000, forecastDetail.getActualRunTime());
            }
        }

        forecasts = Forecast.findForecasts(ForecastStatus.Open, null);
        assertEquals(1, forecasts.size());

        forecast = forecasts.iterator().next();
        assertEquals(6, forecast.getEstimatedTransactionCount());

        forecastDetails = forecast.getForecastDetailCollection();
        assertEquals(5, forecastDetails.size());

        for (ForecastDetail forecastDetail : forecastDetails) {
            if (CREATE_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(330000, forecastDetail.getEstimatedRunTime());
            }

            if (DOWNLOAD_DICR_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (ARCHIVE_DAILY_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (INSERT_FINANCIAL_TRANSACTION_STATE.equals(forecastDetail.getJobAction())) {
                assertEquals(396000, forecastDetail.getEstimatedRunTime());
            }

            if (CREATE_TRANSACTION_OFFLOADED_EVENTS.equals(forecastDetail.getJobAction())) {
                assertEquals(330000, forecastDetail.getEstimatedRunTime());
            }
        }

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testForecast_MissingFinishedRow() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090831170500");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 5, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, UPLOAD_ACH_FILES, "Started");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 12, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 25, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Finished");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090910000000");
        PayrollServices.commitUnitOfWork();

        SpcfCalendar date = PSPDate.getPSPTime();
        date.addDays(-10);

        //Create Company
        PayrollServices.beginUnitOfWork();
        c1dl.persistCompany1();
        c1dl.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        for (int i = 1; i <= 2; i++) {
            if (!CalendarUtils.isWeekendOrHoliday(date)) {
                createPayroll(i, date);
                if (i != 2) {
                    runAchOffload(date);
                }
            }
            date.addDays(1);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090901000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessBatchJobForecast().processForecast();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Forecast> forecasts = Forecast.findForecasts(ForecastStatus.Error, null);
        assertTrue(forecasts.isEmpty());

        SpcfCalendar findDate = SpcfCalendar.createInstance(2009, 8, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        forecasts = Forecast.findForecasts(ForecastStatus.Closed, findDate);
        assertEquals(1, forecasts.size());

        Forecast forecast = forecasts.iterator().next();
        assertEquals(6, forecast.getActualTransactionCount());

        DomainEntitySet<ForecastDetail> forecastDetails = forecast.getForecastDetailCollection();
        assertEquals(5, forecastDetails.size());

        for (ForecastDetail forecastDetail : forecastDetails) {
            if (CREATE_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(300000, forecastDetail.getActualRunTime());
            }

            if (DOWNLOAD_DICR_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (ARCHIVE_DAILY_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (INSERT_FINANCIAL_TRANSACTION_STATE.equals(forecastDetail.getJobAction())) {
                assertEquals(360000, forecastDetail.getActualRunTime());
            }

            if (CREATE_TRANSACTION_OFFLOADED_EVENTS.equals(forecastDetail.getJobAction())) {
                assertEquals(300000, forecastDetail.getActualRunTime());
            }
        }

        forecasts = Forecast.findForecasts(ForecastStatus.Open, null);
        assertEquals(1, forecasts.size());

        forecast = forecasts.iterator().next();
        assertEquals(6, forecast.getEstimatedTransactionCount());

        forecastDetails = forecast.getForecastDetailCollection();
        assertEquals(5, forecastDetails.size());

        for (ForecastDetail forecastDetail : forecastDetails) {
            if (CREATE_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(330000, forecastDetail.getEstimatedRunTime());
            }

            if (DOWNLOAD_DICR_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (ARCHIVE_DAILY_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (INSERT_FINANCIAL_TRANSACTION_STATE.equals(forecastDetail.getJobAction())) {
                assertEquals(396000, forecastDetail.getEstimatedRunTime());
            }

            if (CREATE_TRANSACTION_OFFLOADED_EVENTS.equals(forecastDetail.getJobAction())) {
                assertEquals(330000, forecastDetail.getEstimatedRunTime());
            }
        }

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testForecast_MultiRunsSameDaySamePayrollCount() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090831170500");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 5, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, UPLOAD_ACH_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 11, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, UPLOAD_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 12, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 25, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Finished");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090910000000");
        PayrollServices.commitUnitOfWork();

        SpcfCalendar date = PSPDate.getPSPTime();
        date.addDays(-10);

        //Create Company
        PayrollServices.beginUnitOfWork();
        c1dl.persistCompany1();
        c1dl.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        for (int i = 1; i <= 2; i++) {
            if (!CalendarUtils.isWeekendOrHoliday(date)) {
                createPayroll(i, date);
                if (i != 2) {
                    runAchOffload(date);
                }
            }
            date.addDays(1);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090901000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessBatchJobForecast().processForecast();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessBatchJobForecast().processForecast();
        PayrollServices.commitUnitOfWork();        

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Forecast> forecasts = Forecast.findForecasts(ForecastStatus.Error, null);
        assertTrue(forecasts.isEmpty());

        SpcfCalendar findDate = SpcfCalendar.createInstance(2009, 8, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        forecasts = Forecast.findForecasts(ForecastStatus.Closed, findDate);
        assertEquals(1, forecasts.size());

        Forecast forecast = forecasts.iterator().next();
        assertEquals(6, forecast.getActualTransactionCount());

        DomainEntitySet<ForecastDetail> forecastDetails = forecast.getForecastDetailCollection();
        assertEquals(6, forecastDetails.size());

        for (ForecastDetail forecastDetail : forecastDetails) {
            if (CREATE_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(300000, forecastDetail.getActualRunTime());
            }

            if (UPLOAD_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (DOWNLOAD_DICR_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (ARCHIVE_DAILY_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (INSERT_FINANCIAL_TRANSACTION_STATE.equals(forecastDetail.getJobAction())) {
                assertEquals(360000, forecastDetail.getActualRunTime());
            }

            if (CREATE_TRANSACTION_OFFLOADED_EVENTS.equals(forecastDetail.getJobAction())) {
                assertEquals(300000, forecastDetail.getActualRunTime());
            }
        }

        forecasts = Forecast.findForecasts(ForecastStatus.Open, null);
        assertEquals(1, forecasts.size());

        forecast = forecasts.iterator().next();
        assertEquals(6, forecast.getEstimatedTransactionCount());

        forecastDetails = forecast.getForecastDetailCollection();
        assertEquals(6, forecastDetails.size());

        for (ForecastDetail forecastDetail : forecastDetails) {
            if (CREATE_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(330000, forecastDetail.getEstimatedRunTime());
            }

            if (UPLOAD_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (DOWNLOAD_DICR_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (ARCHIVE_DAILY_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (INSERT_FINANCIAL_TRANSACTION_STATE.equals(forecastDetail.getJobAction())) {
                assertEquals(396000, forecastDetail.getEstimatedRunTime());
            }

            if (CREATE_TRANSACTION_OFFLOADED_EVENTS.equals(forecastDetail.getJobAction())) {
                assertEquals(330000, forecastDetail.getEstimatedRunTime());
            }
        }

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testForecast_MultiRunsSameDayDiffPayrollCount() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090831170500");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 5, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, UPLOAD_ACH_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 11, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, UPLOAD_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 12, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 25, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Finished");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090910000000");
        PayrollServices.commitUnitOfWork();

        SpcfCalendar date = PSPDate.getPSPTime();
        date.addDays(-10);

        //Create Company
        PayrollServices.beginUnitOfWork();
        c1dl.persistCompany1();
        c1dl.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        for (int i = 1; i <= 2; i++) {
            if (!CalendarUtils.isWeekendOrHoliday(date)) {
                createPayroll(i, date);
                if (i != 2) {
                    runAchOffload(date);
                }
            }
            date.addDays(1);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090901000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessBatchJobForecast().processForecast();
        PayrollServices.commitUnitOfWork();

        date.addDays(-1);
        createPayroll(3, date);

        PayrollServices.beginUnitOfWork();
        new ProcessBatchJobForecast().processForecast();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Forecast> forecasts = Forecast.findForecasts(ForecastStatus.Error, null);
        assertTrue(forecasts.isEmpty());

        SpcfCalendar findDate = SpcfCalendar.createInstance(2009, 8, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        forecasts = Forecast.findForecasts(ForecastStatus.Closed, findDate);
        assertEquals(1, forecasts.size());

        Forecast forecast = forecasts.iterator().next();
        assertEquals(6, forecast.getActualTransactionCount());

        DomainEntitySet<ForecastDetail> forecastDetails = forecast.getForecastDetailCollection();
        assertEquals(6, forecastDetails.size());

        for (ForecastDetail forecastDetail : forecastDetails) {
            if (CREATE_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(300000, forecastDetail.getActualRunTime());
            }

            if (UPLOAD_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (DOWNLOAD_DICR_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (ARCHIVE_DAILY_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (INSERT_FINANCIAL_TRANSACTION_STATE.equals(forecastDetail.getJobAction())) {
                assertEquals(360000, forecastDetail.getActualRunTime());
            }

            if (CREATE_TRANSACTION_OFFLOADED_EVENTS.equals(forecastDetail.getJobAction())) {
                assertEquals(300000, forecastDetail.getActualRunTime());
            }
        }

        forecasts = Forecast.findForecasts(ForecastStatus.Open, null);
        assertEquals(1, forecasts.size());

        forecast = forecasts.iterator().next();
        assertEquals(12, forecast.getEstimatedTransactionCount());

        forecastDetails = forecast.getForecastDetailCollection();
        assertEquals(6, forecastDetails.size());

        for (ForecastDetail forecastDetail : forecastDetails) {
            if (CREATE_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(330000, forecastDetail.getEstimatedRunTime());
            }

            if (UPLOAD_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (DOWNLOAD_DICR_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (ARCHIVE_DAILY_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(66000, forecastDetail.getEstimatedRunTime());
            }

            if (INSERT_FINANCIAL_TRANSACTION_STATE.equals(forecastDetail.getJobAction())) {
                assertEquals(396000, forecastDetail.getEstimatedRunTime());
            }

            if (CREATE_TRANSACTION_OFFLOADED_EVENTS.equals(forecastDetail.getJobAction())) {
                assertEquals(330000, forecastDetail.getEstimatedRunTime());
            }
        }

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testForecast_MultiRunsDiffDayDiffPayrollCount() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090831170500");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        SpcfCalendar createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 5, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, UPLOAD_ACH_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 11, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, UPLOAD_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 12, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 8, 31, 17, 25, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Finished");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090910000000");
        PayrollServices.commitUnitOfWork();

        SpcfCalendar date = PSPDate.getPSPTime();
        date.addDays(-10);

        //Create Company
        PayrollServices.beginUnitOfWork();
        c1dl.persistCompany1();
        c1dl.updateTo2DayFundingModel();
        PayrollServices.commitUnitOfWork();

        for (int i = 1; i <= 2; i++) {
            if (!CalendarUtils.isWeekendOrHoliday(date)) {
                createPayroll(i, date);
                if (i != 2) {
                    runAchOffload(date);
                }
            }
            date.addDays(1);
        }

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090901000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessBatchJobForecast().processForecast();
        PayrollServices.commitUnitOfWork();

        date.addDays(-1);
        createPayroll(3, date);
        runAchOffload(date);

        PayrollServices.beginUnitOfWork();
        createdDate = SpcfCalendar.createInstance(2009, 9, 1, 17, 5, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 9, 1, 17, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 9, 1, 17, 10, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, UPLOAD_ACH_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 9, 1, 17, 11, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, UPLOAD_ACH_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 9, 1, 17, 12, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 9, 1, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, DOWNLOAD_DICR_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 9, 1, 17, 13, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 9, 1, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, ARCHIVE_DAILY_FILES, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 9, 1, 17, 14, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 9, 1, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, INSERT_FINANCIAL_TRANSACTION_STATE, "Finished");

        createdDate = SpcfCalendar.createInstance(2009, 9, 1, 17, 20, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Started");
        createdDate = SpcfCalendar.createInstance(2009, 9, 1, 17, 25, 0, 0, SpcfTimeZone.getLocalTimeZone());
        createBatchJobAuditLog(createdDate, CREATE_TRANSACTION_OFFLOADED_EVENTS, "Finished");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20090902000000");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        new ProcessBatchJobForecast().processForecast();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Forecast> forecasts = Forecast.findForecasts(ForecastStatus.Error, null);
        assertTrue(forecasts.isEmpty());

        SpcfCalendar findDate = SpcfCalendar.createInstance(2009, 8, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        forecasts = Forecast.findForecasts(ForecastStatus.Closed, findDate);
        assertEquals(1, forecasts.size());

        Forecast forecast = forecasts.iterator().next();
        assertEquals("Transaction Count", 6, forecast.getActualTransactionCount());

        DomainEntitySet<ForecastDetail> forecastDetails = forecast.getForecastDetailCollection();
        assertEquals("Forecast Details", 6, forecastDetails.size());

        for (ForecastDetail forecastDetail : forecastDetails) {
            if (CREATE_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(300000,forecastDetail.getActualRunTime());
            }

            if (UPLOAD_ACH_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000,forecastDetail.getActualRunTime() );
            }

            if (DOWNLOAD_DICR_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000,forecastDetail.getActualRunTime() );
            }

            if (ARCHIVE_DAILY_FILES.equals(forecastDetail.getJobAction())) {
                assertEquals(60000, forecastDetail.getActualRunTime());
            }

            if (INSERT_FINANCIAL_TRANSACTION_STATE.equals(forecastDetail.getJobAction())) {
                assertEquals(360000, forecastDetail.getActualRunTime());
            }

            if (CREATE_TRANSACTION_OFFLOADED_EVENTS.equals(forecastDetail.getJobAction())) {
                assertEquals(300000, forecastDetail.getActualRunTime());
            }
        }

        forecasts = Forecast.findForecasts(ForecastStatus.Open, null);
        assertEquals(0, forecasts.size());

        PayrollServices.commitUnitOfWork();
    }

    private static String batchJobAuditToString(BatchJobAuditLog auditLog) {
        StringBuffer sb = new StringBuffer();
        sb.append("\tBatch Job Audit Log:\n");
        sb.append("\t\tJob Action: ").append(auditLog.getJobAction()).append("\n");
        sb.append("\t\tJob Namespace: ").append(auditLog.getJobNamespace()).append("\n");
        sb.append("\t\tMessage: ").append(auditLog.getMessage()).append("\n");
        sb.append("\t\tCreated Date: ").append(auditLog.getCreatedDate().toString()).append("\n");
        return sb.toString();
    }

    private static String allForecastToString() {
        StringBuffer sb = new StringBuffer();
        for (DomainEntity de : Application.find(Forecast.class).sort(Forecast.<Forecast>CreatedDate())) {
            Forecast forecast = (Forecast) de;
            sb.append(forecastToString(forecast));
        }
        return sb.toString();
    }

    private static String forecastToString(Forecast forecast) {
        StringBuffer sb = new StringBuffer();
        sb.append("\tForecast:\n");
        sb.append("\t\tStatus: ").append(forecast.getStatus()).append("\n");
        sb.append("\t\tEstimated Transaction Count: ").append(forecast.getEstimatedTransactionCount()).append("\n");
        sb.append("\t\tActual Transaction Count: ").append(forecast.getActualTransactionCount()).append("\n");
        sb.append("\t\tRun Date: ").append(forecast.getRunDate().toString()).append("\n");
        sb.append("\t\tDetails: ").append(forecast.getRunDate().toString()).append("\n");

        for (ForecastDetail detail : forecast.getForecastDetailCollection().sort(ForecastDetail.JobAction())) {
            sb.append("\t\t\tJob Action: ").append(detail.getJobAction()).append("\n");
            sb.append("\t\t\tEstimated Run Time: ").append(detail.getEstimatedRunTime()).append("\n");
            sb.append("\t\t\tActual Run Time: ").append(detail.getActualRunTime()).append("\n");
        }
        return sb.toString();
    }


    private void createPayroll(int pIndex, SpcfCalendar pCheckDate) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(convertSpcfCalendarToString("yyyyMMdd000000", pCheckDate));
        PayrollRunDTO currentPayrollRunDTO = c1dl.getCompany1PR_DoesNotExceedLimits(new DateDTO(convertSpcfCalendarToString("yyyy-MM-dd", pCheckDate)));
        currentPayrollRunDTO.setPayrollTXBatchId("BatchIDX" + pIndex);
        Collection<PaycheckDTO> paychecks = currentPayrollRunDTO.getPaychecks();

        for (PaycheckDTO currPaycheck : paychecks) {
            currPaycheck.setPaycheckId(SpcfUniqueId.generateRandomUniqueIdString());
            Collection<DDTransactionDTO> ddTxns = currPaycheck.getDdTransactions();
            for (DDTransactionDTO currDDTxn : ddTxns) {
                currDDTxn.setDDTransactionId(SpcfUniqueId.generateRandomUniqueIdString());
            }
        }

        Company1Dataloader.persistPayrollRun(currentPayrollRunDTO);
        PayrollServices.commitUnitOfWork();
    }

    private void runAchOffload(SpcfCalendar pCheckDate) {
        OffloadACHTransactions offloader = new OffloadACHTransactions();

        // offload QBOE ER DB
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(convertSpcfCalendarToString("yyyyMMdd180500", pCheckDate));
        Application.commitUnitOfWork();

        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);
    }


    private String convertSpcfCalendarToString(String pFormat,SpcfCalendar pDate) {
        return new SimpleDateFormat(pFormat).format(CalendarUtils.convertToDate(pDate));
    }

    private void createBatchJobAuditLog(SpcfCalendar pDate, String pJobAction, String pMessage) {
        BatchJobAuditLog bjal = new BatchJobAuditLog();

        bjal.setCreatedDate(pDate);
        bjal.setJobAction(pJobAction);
        bjal.setMessage(pMessage);

        Application.save(bjal);
    }
    

}
