package com.intuit.sbd.payroll.psp.batchjobs.monitors;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.batchjobs.util.ParameterException;
import com.intuit.sbd.payroll.psp.batchjobs.util.TimeConstraint;
import com.intuit.sbd.payroll.psp.domain.BatchJobAuditLog;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;

public class TimeConstraintTests {
    private static final String JOB_TYPE = "testjobtype";
    private static final String JOB_STEP = "testjobstep";
    private static final String NAME_SEPARATOR = ".";
    private static final String FULL_NAME = JOB_TYPE + NAME_SEPARATOR + JOB_STEP;

    private static final String STEP_STATE_STARTED = "Started";
    private static final String STEP_STATE_FINISHED = "Finished";

    /** A basic test of an absolute time constraint */
    @Test
    public void testAbsoluteRun() {
        // Use local timezone because absolute times are local timezone but database is in UTC
        SpcfCalendar tenMinutesAgo = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        tenMinutesAgo.addMinutes(-10);

        // Use UTC time for "database" entries
        SpcfCalendar today = SpcfCalendar.createInstance();
        SpcfCalendar yesterday = today.copy();
        yesterday.addDays(-1);
        SpcfCalendar dayBeforeYesterday = yesterday.copy();
        dayBeforeYesterday.addDays(-1);

        HashMap<String, String> params = new HashMap<String, String>();

        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT, false), "absolute");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_BEGIN_TIME, false), tenMinutesAgo.format("HH:mm"));
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_BEGIN_DAY, false), "TODAY");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_END_TIME, false), "NOW");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_END_DAY, false), "TODAY");

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(params, JOB_TYPE);

        DomainEntitySet<BatchJobAuditLog> logList = Application.find(BatchJobAuditLog.class);

        Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().like(JOB_TYPE + "%")
                                                .And(BatchJobAuditLog.JobAction().equalTo(JOB_TYPE)
                                                .And(BatchJobAuditLog.Message().equalTo(STEP_STATE_FINISHED)
                                                .And(BatchJobAuditLog.IsVerified().equalTo(false))));

        //where = where.And(BatchJobAuditLog.CreatedDate().between(monitorTimeConstraint.getBegin(), monitorTimeConstraint.getEnd()));
        where = where.And(BatchJobAuditLog.CreatedDate().greaterOrEqualThan(timeConstraint.getBegin()))
                .And(BatchJobAuditLog.CreatedDate().lessOrEqualThan(timeConstraint.getEnd()));

        logList.add(createLog(0, today, true));
        logList.add(createLog(0, yesterday, true));
        logList.add(createLog(0, dayBeforeYesterday, true));

        DomainEntitySet<BatchJobAuditLog> existingReturns = logList.find(where);

        assertTrue("Incorrect number of logs found.  Found " + existingReturns.size(), existingReturns.size() == 1);
    }

    /** Tests sending in an invalid time constraint value */
    @Test(expected=ParameterException.class)
    public void testInvalidTimeConstraint() {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT, false), "shouldnotfind");
        params.put(createKeyName("time_constraint_seconds", false), "300");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_MIN_SUCCESSFUL_RUNS, false), "5");

        TimeConstraint.getTimeConstraint(params, JOB_TYPE);
    }

    /** Tests sending in an invalid begin time value */
    @Test(expected=ParameterException.class)
    public void testInvalidTimeParameter() {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT, false), "absolute");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_BEGIN_TIME, false), "invalidparameter");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_BEGIN_DAY, false), "TODAY");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_END_TIME, false), "NOW");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_END_DAY, false), "TODAY");

        TimeConstraint.getTimeConstraint(params, JOB_TYPE);
    }

    /** Tests sending in an invalid begin day value */
    @Test(expected=ParameterException.class)
    public void testInvalidDateParameter() {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT, false), "absolute");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_BEGIN_TIME, false), "NOW");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_BEGIN_DAY, false), "invalidparameter");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_END_TIME, false), "NOW");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_END_DAY, false), "TODAY");

        TimeConstraint.getTimeConstraint(params, JOB_TYPE);
    }

    /** Tests sending in a begin time that is after the end time */
    @Test(expected=ParameterException.class)
    public void testTimeInFutureParameter() {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT, false), "absolute");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_BEGIN_TIME, false), "NOW");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_BEGIN_DAY, false), "TODAY");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_END_TIME, false), "NOW");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_END_DAY, false), "YESTERDAY");

        TimeConstraint.getTimeConstraint(params, JOB_TYPE);
    }

    /** Tests min_successful_runs not getting enough results to pass the test */
    @Test
    public void testValidSuccessfulRunsFailure() {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT, false), "relative");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_SECONDS, false), "300");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_MIN_SUCCESSFUL_RUNS, false), "5");

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(params, JOB_TYPE);

        DomainEntitySet<BatchJobAuditLog> logList = Application.find(BatchJobAuditLog.class);

        Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().like(JOB_TYPE + "%")
                                                .And(BatchJobAuditLog.JobAction().equalTo(JOB_TYPE)
                                                .And(BatchJobAuditLog.Message().equalTo(STEP_STATE_FINISHED)
                                                .And(BatchJobAuditLog.IsVerified().equalTo(false))));

        where = where.And(BatchJobAuditLog.CreatedDate().greaterOrEqualThan(timeConstraint.getBegin()))
                .And(BatchJobAuditLog.CreatedDate().lessOrEqualThan(timeConstraint.getEnd()));

        logList.add(createLog(150, null, true));
        logList.add(createLog(140, null, true));
        logList.add(createLog(130, null, true));

        DomainEntitySet<BatchJobAuditLog> existingReturns = logList.find(where);

        boolean successful = timeConstraint.hasValidSuccessfulRuns(existingReturns);
        assertTrue("Successful runs is valid when it shouldn't be.  Found " + existingReturns.size(), !successful);
    }

    /** Tests min_successful_runs and getting enough results to pass the test */
    @Test
    public void testValidSuccessfulRuns() {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT, false), "relative");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_SECONDS, false), "300");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_MIN_SUCCESSFUL_RUNS, false), "5");

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(params, JOB_TYPE);

        DomainEntitySet<BatchJobAuditLog> logList = Application.find(BatchJobAuditLog.class);

        Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().like(JOB_TYPE + "%")
                                                .And(BatchJobAuditLog.JobAction().equalTo(JOB_TYPE)
                                                .And(BatchJobAuditLog.Message().equalTo(STEP_STATE_FINISHED)
                                                .And(BatchJobAuditLog.IsVerified().equalTo(false))));

        where = where.And(BatchJobAuditLog.CreatedDate().greaterOrEqualThan(timeConstraint.getBegin()))
                .And(BatchJobAuditLog.CreatedDate().lessOrEqualThan(timeConstraint.getEnd()));

        logList.add(createLog(150, null, true));
        logList.add(createLog(140, null, true));
        logList.add(createLog(130, null, true));
        logList.add(createLog(120, null, true));
        logList.add(createLog(110, null, true));

        DomainEntitySet<BatchJobAuditLog> existingReturns = logList.find(where);

        boolean successful = timeConstraint.hasValidSuccessfulRuns(existingReturns);
        assertTrue("Successful runs is invalid.  Found " + existingReturns.size(), successful);
    }

    /** Add extra runs that are out of the time constraint to verify it excludes properly */
    @Test
    public void testExtraLogsRun() {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT, false), "relative");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_SECONDS, false), "300");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_MIN_SUCCESSFUL_RUNS, false), "1");

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(params, JOB_TYPE);

        DomainEntitySet<BatchJobAuditLog> logList = Application.find(BatchJobAuditLog.class);

        Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().like(JOB_TYPE + "%")
                                                .And(BatchJobAuditLog.JobAction().equalTo(JOB_TYPE)
                                                .And(BatchJobAuditLog.Message().equalTo(STEP_STATE_FINISHED)
                                                .And(BatchJobAuditLog.IsVerified().equalTo(false))));

        where = where.And(BatchJobAuditLog.CreatedDate().greaterOrEqualThan(timeConstraint.getBegin()))
                .And(BatchJobAuditLog.CreatedDate().lessOrEqualThan(timeConstraint.getEnd()));

        logList.add(createLog(150, null, true));
        logList.add(createLog(400, null, true));
        logList.add(createLog(450, null, true));
        logList.add(createLog(500, null, true));
        logList.add(createLog(600, null, true));

        DomainEntitySet<BatchJobAuditLog> existingReturns = logList.find(where);

        assertTrue("Incorrect number of logs found.  Found " + existingReturns.size(), existingReturns.size() == 1);
    }

    /** Simple relative time constraint test */
    @Test
    public void testSuccessfulRun() {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT, false), "relative");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_SECONDS, false), "300");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_MIN_SUCCESSFUL_RUNS, false), "1");

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(params, JOB_TYPE);

        DomainEntitySet<BatchJobAuditLog> logList = Application.find(BatchJobAuditLog.class);
        
        Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().like(JOB_TYPE + "%")
                                                .And(BatchJobAuditLog.JobAction().equalTo(JOB_TYPE)
                                                .And(BatchJobAuditLog.Message().equalTo(STEP_STATE_FINISHED)
                                                .And(BatchJobAuditLog.IsVerified().equalTo(false))));

        where = where.And(BatchJobAuditLog.CreatedDate().greaterOrEqualThan(timeConstraint.getBegin()))
                .And(BatchJobAuditLog.CreatedDate().lessOrEqualThan(timeConstraint.getEnd()));

        logList.add(createLog(150, null, true));

        DomainEntitySet<BatchJobAuditLog> existingReturns = logList.find(where);

        assertTrue("Not enough logs found.  Found " + existingReturns.size(), existingReturns.size() == 1);
    }

    /** Test without any BatchJobAuditLogs to find */
    @Test
    public void testNoneFound() {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT, false), "relative");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_SECONDS, false), "300");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_MIN_SUCCESSFUL_RUNS, false), "1");

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(params, JOB_TYPE);

        DomainEntitySet<BatchJobAuditLog> logList = Application.find(BatchJobAuditLog.class);

        Criterion<BatchJobAuditLog> where = BatchJobAuditLog.JobNamespace().like(JOB_TYPE + "%")
                                                .And(BatchJobAuditLog.JobAction().equalTo(JOB_TYPE)
                                                .And(BatchJobAuditLog.Message().equalTo(STEP_STATE_FINISHED)
                                                .And(BatchJobAuditLog.IsVerified().equalTo(false))));

        where = where.And(BatchJobAuditLog.CreatedDate().greaterOrEqualThan(timeConstraint.getBegin()))
                .And(BatchJobAuditLog.CreatedDate().lessOrEqualThan(timeConstraint.getEnd()));

        DomainEntitySet<BatchJobAuditLog> existingReturns = logList.find(where);

        assertTrue("Logs found where there shouldn't be any.  Found " + existingReturns.size(), existingReturns.size() == 0);
    }

    /** Test that a job step will not use the batch job's parameter values */ 
    @Test
    public void testJobStepTimeConstraint() {
        HashMap<String, String> params = new HashMap<String, String>();

        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT, false), "relative");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_SECONDS, false), "300");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_MIN_SUCCESSFUL_RUNS, false), "1");

        TimeConstraint timeConstraint = TimeConstraint.getTimeConstraint(params, FULL_NAME);

        assertNull("Time constraint found when it should not have found one", timeConstraint);

        // Add actual job step values
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT, true), "relative");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_SECONDS, true), "300");
        params.put(createKeyName(TimeConstraint.TIME_CONSTRAINT_MIN_SUCCESSFUL_RUNS, true), "1");

        timeConstraint = TimeConstraint.getTimeConstraint(params, FULL_NAME);

        assertNotNull("Time constraint not found when it should have found one", timeConstraint);
    }

    /**
     * Creates the key name for the name value pair
     * @param key The key name
     * @param useJobStep Whether to add the job step into the key name
     * @return The dot separated key name
     */
    private String createKeyName(String key, boolean useJobStep) {
        return JOB_TYPE + (useJobStep ? NAME_SEPARATOR + JOB_STEP : "") + NAME_SEPARATOR + key;
    }

    /**
     * Creates a BatchJobAuditLog initialized with the specified data
     * @param offsetSeconds The number of seconds to subtract.  Used if calendar is null
     * @param calendar The calendar object to use as create date
     * @param isFinished Whether to use the finished message or start message
     * @return The BatchJobAuditLog initialized with the specified data
     */
    private BatchJobAuditLog createLog(int offsetSeconds, SpcfCalendar calendar, boolean isFinished) {
        BatchJobAuditLog auditLog = new BatchJobAuditLog();

        auditLog.setJobNamespace(JOB_TYPE);
        auditLog.setJobAction(JOB_TYPE);

        if (isFinished) {
            auditLog.setMessage(STEP_STATE_FINISHED);
        } else {
            auditLog.setMessage(STEP_STATE_STARTED);
        }

        auditLog.setIsVerified(false);

        if (calendar == null) {
            SpcfCalendar spcfCalendar = SpcfCalendar.createInstance();
            spcfCalendar.addSeconds(-offsetSeconds);
            auditLog.setCreatedDate(spcfCalendar);
        } else {
            auditLog.setCreatedDate(calendar);
        }

        return auditLog;
    }
}
