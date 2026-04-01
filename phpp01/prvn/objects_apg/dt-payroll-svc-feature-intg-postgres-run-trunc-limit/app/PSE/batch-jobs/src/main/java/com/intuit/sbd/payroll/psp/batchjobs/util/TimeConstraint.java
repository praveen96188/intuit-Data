package com.intuit.sbd.payroll.psp.batchjobs.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.sbd.payroll.psp.domain.BatchJobAuditLog;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Copyright (c) 2011 Intuit, Inc. All Rights Reserved.
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

/**
 * Created by IntelliJ IDEA.
 * User: janderson
 * Date: Mar 25, 2011
 * Time: 10:17:33 AM
 */
public class TimeConstraint {
    protected SpcfLogger logger;
    
    /** The begin time for the time constraint */
    private SpcfCalendar begin;
    /** The end time for the time constraint */
    private SpcfCalendar end;

    /** The minimum successful runs for a batch job */
    private int minimumSuccessfulRuns = 1;

    /** If the time constraint's parameters are valid */
    private boolean isValid;

    /** The JOB_TYPE and optionally JOB_STEP together with a "." between */
    private String jobName;
    /** All parameters found for the JOB_TYPE */
    private Map<String, String> parameters;

    private static final String DAY_DEFAULT = "TODAY";
    private static final String TIME_DEFAULT = "NOW";
    private static final String SUCCESSFUL_RUNS_DEFAULT = "1";

    public static final String TIME_CONSTRAINT = "time_constraint";

    // Parameters for absolute time constraints
    public static final String TIME_CONSTRAINT_BEGIN_TIME = "time_constraint_begin_time";
    public static final String TIME_CONSTRAINT_BEGIN_DAY = "time_constraint_begin_day";
    public static final String TIME_CONSTRAINT_END_TIME = "time_constraint_end_time";
    public static final String TIME_CONSTRAINT_END_DAY = "time_constraint_end_day";

    // Parameters for relative time constraints
    public static final String TIME_CONSTRAINT_SECONDS = "time_constraint_seconds";
    public static final String TIME_CONSTRAINT_MIN_SUCCESSFUL_RUNS = "min_successful_runs";

    /**
     * Constructor
     * @param parameters All parameters found for the JOB_TYPE
     * @param jobName The JOB_TYPE and optionally JOB_STEP together with a "." between
     */
    public TimeConstraint(Map<String, String> parameters, String jobName) {
        this.parameters = parameters;
        this.jobName = jobName;

        logger = Application.getLogger(this.getClass());
        
        String value = getParameter(TIME_CONSTRAINT);

        if (value != null) {
            if (value.compareToIgnoreCase("absolute") == 0) {
                createAbsoluteTimeConstraint();
            } else if (value.compareToIgnoreCase("relative") == 0) {
                createRelativeTimeConstraint();
            } else {
                throw createInvalidParameterException(TIME_CONSTRAINT, value, "Value not absolute or relative");
            }

            isValid = true;
        } else {
            isValid = false;

            logger.debug(jobName + " is not configured to have a time constraint parameter." +
                    "  Number of parameters is " + parameters.size());
        }
    }

    /**
     * Gets the time constraint object initialized with the parameters
     * @param parameters All parameters found for the JOB_TYPE
     * @param jobName The JOB_TYPE and optionally JOB_STEP together with a "." between
     * @return The time constraint object initialized with the parameters or null if invalid
     */
    public static TimeConstraint getTimeConstraint(Map<String, String> parameters, String jobName) {
        TimeConstraint timeConstraint = new TimeConstraint(parameters, jobName);
        return timeConstraint.isValid() ? timeConstraint : null;
    }

    /**
     * Creates and initializes an absolute time constraint
     * @return An absolute time constraint configured with the params
     */
    private TimeConstraint createAbsoluteTimeConstraint() {
        TimeConstraint timeConstraint = null;

        // Get required parameters
        String beginTime = getParameter(TIME_CONSTRAINT_BEGIN_TIME);

        if (beginTime == null) {
            throw createMissingParameterException(TIME_CONSTRAINT_BEGIN_TIME);
        }

        // Get optional parameters
        String beginDay = getParameterWithDefault(TIME_CONSTRAINT_BEGIN_DAY, DAY_DEFAULT);
        String endTime = getParameterWithDefault(TIME_CONSTRAINT_END_TIME, TIME_DEFAULT);
        String endDay = getParameterWithDefault(TIME_CONSTRAINT_END_DAY, DAY_DEFAULT);

        begin = parseDateTime(beginDay, beginTime, TIME_CONSTRAINT_BEGIN_TIME, TIME_CONSTRAINT_BEGIN_DAY);
        end = parseDateTime(endDay, endTime, TIME_CONSTRAINT_END_TIME, TIME_CONSTRAINT_END_DAY);

        if (begin.after(end)) {
            throw createInvalidParameterException(TIME_CONSTRAINT_BEGIN_TIME, "Begin=" + begin.toISO8601() + " End=" + end.toISO8601(),
                    "Beginning date time is after ending date time");
        }

        initSharedParameters();

        logger.info("Created absolute time constraint with beginning " + begin.toISO8601() + " (" + beginTime + "/" + beginDay + ")" +
                " and ending " + end.toISO8601() + " (" + endTime + "/" + endDay + ")" + " and \"" + minimumSuccessfulRuns +
                "\" minimum successful runs.");

        return timeConstraint;
    }

    /**
     * Creates and initializes a relative time constraint
     * @return A relative time constraint configured with the params
     */
    private TimeConstraint createRelativeTimeConstraint() {
        TimeConstraint timeConstraint = null;

        // Get required parameters
        String seconds = getParameter(TIME_CONSTRAINT_SECONDS);

        if (seconds == null) {
            throw createMissingParameterException(TIME_CONSTRAINT_SECONDS);
        }

        end = SpcfCalendar.createInstance();
        begin = end.copy();

        // Parse and change to negative to subtract seconds from calendar
        int secondsParsed = -Integer.parseInt(seconds);
        begin.addSeconds(secondsParsed);

        initSharedParameters();

        logger.info("Created relative time constraint with beginning " + begin.toISO8601() + " and ending " + end.toISO8601()
                + " " + seconds + "(seconds) and " + minimumSuccessfulRuns + " minimum successful runs.");

        return timeConstraint;
    }

    /**
     * Initializes parameters shared by all time constraints
     */
    private void initSharedParameters() {
        String successfulRuns = getParameterWithDefault(TIME_CONSTRAINT_MIN_SUCCESSFUL_RUNS, SUCCESSFUL_RUNS_DEFAULT);
        minimumSuccessfulRuns = Integer.parseInt(successfulRuns);
    }

    /**
     * Parses the date and time and creates a calendar set to the date and time.
     * @param date The relative date.  Can be "TODAY" or "YESTERDAY".
     * @param time The time in HH:mm
     * @param dayConstraint The constraint used to find the day
     * @param timeConstraint The constraint used to find the time
     * @return A calendar object set to the date and time
     */
    private SpcfCalendar parseDateTime(String date, String time, String dayConstraint, String timeConstraint) {
        // Start out with local timezone and convert to UTC at the end because all database times are UTC
        SpcfCalendar calendar = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        // Parse the date
        if (date.compareToIgnoreCase("today") == 0) {
            // Do nothing, already initialized to today 
        } else if (date.compareToIgnoreCase("yesterday") == 0) {
            calendar.addDays(-1);
        } else {
            throw createInvalidParameterException(dayConstraint, date, "Value not today or yesterday");
        }

        // Parse the time
        if (time.compareToIgnoreCase("now") == 0) {
            // Do nothing, already initialized to now
        } else {
            Pattern p = Pattern.compile("([0][0-9]|[1][0-9]|[2][0-3]):([0-5][0-9])");
            Matcher m = p.matcher(time);

            if (m.matches()) {
                int hour = Integer.parseInt(m.group(1));
                int minute = Integer.parseInt(m.group(2));

                // Set the calendar to the specified hour and minute while zeroing out seconds and milliseconds
                calendar.setValues(calendar.getYear(), calendar.getMonth(), calendar.getDay(), hour, minute, 0, 0);
            } else {
                throw createInvalidParameterException(timeConstraint, time, "Value not in HH:mm");
            }
        }

        SpcfCalendar calendarInUTC = calendar.toUtc();
        return calendarInUTC;
    }

    /**
     * Creates an exception when a supplied value is incorrect
     * @param parameterName The name of the parameter with the incorrect value
     * @param value The value that was found
     * @param reason The reason the value was invalid
     * @return The exception object
     */
    private ParameterException createInvalidParameterException(String parameterName, String value, String reason) {
        return new ParameterException("Time Constraint parameter found with an invalid value.  The parameter is \""
                        + jobName + "." + parameterName + "\" and the value is \"" + value + "\".  " + reason);
    }

    /**
     * Creates an exception when a required parameter is missing
     * @param parameterName The name of the parameter with the missing value
     * @return The exception object
     */
    private ParameterException createMissingParameterException(String parameterName) {
        return new ParameterException("Time Constraint parameter is missing.  The parameter is \""
                        + jobName + "." + parameterName + "\".");
    }

    /**
     * Gets the parameter or the default if missing
     * @param parameterName The name of the parameter
     * @param defaultValue The default value if missing
     * @return The parameter or the default if missing
     */
    private String getParameterWithDefault(String parameterName, String defaultValue) {
        String value = getParameter(parameterName);

        if (value == null) {
            value = defaultValue;
        }

        return value;
    }

    /**
     * Gets the parameter
     * @param parameterName The name of the parameter
     * @return The parameter or null if not found
     */
    private String getParameter(String parameterName) {
        return parameters.get(jobName + "." + parameterName);
    }

    /**
     * Validates that a minimum number of successful runs have occurred in the time frame
     * @param logEntries The logs for the batch
     * @return True if enough successful runs have occurred and false if not
     */
    public boolean hasValidSuccessfulRuns(DomainEntitySet<BatchJobAuditLog> logEntries) {
        boolean valid = logEntries.size() >= minimumSuccessfulRuns;

        if (!valid) {
            logger.error("BatchJob was invalid because it only ran " + logEntries.size() + " times and the minimum is " +
                    minimumSuccessfulRuns + " times.");
        }

        return valid;
    }

    /**
     * Get the begin time for the constraint
     * @return The beginning time for the time constraint
     */
    public SpcfCalendar getBegin() {
        return begin;
    }

    /**
     * Get the end time for the constraint
     * @return The ending time for the time constraint
     */
    public SpcfCalendar getEnd() {
        return end;
    }

    /**
     * Get if the time constraint's parameters are valid and properly filled out
     * @return True if valid false if not
     */
    public boolean isValid() {
        return isValid;
    }
}
