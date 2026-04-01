package com.intuit.sbd.payroll.psp.batchjobs;

import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class DefaultParameterConfig {
    protected final CommandLineParser commandLineParser = new PosixParser();

    public CommandLine getCommandLine(String[] args) throws ParseException {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }
        return commandLineParser.parse(getOptions(), args);
    }

    public List<String> getList(CommandLine commandLine, DefaultParameterConfig.Argument argument) {
        //if passed from command line, retrieve it from here
        if (Objects.nonNull(commandLine) && commandLine.hasOption(argument.getName())) {
            return new ArrayList<>(Arrays.asList(commandLine.getOptionValue(argument.getName()).split(",")));
        }
        //if defaultValue is empty string, return empty list
        if(argument.getDefaultValue().toString().isEmpty()) {
            return new ArrayList<>();
        }
        //split the string(in csv format) by comma, and return the list of strings
        return new ArrayList<>(Arrays.asList(argument.getDefaultValue().toString().split(",")));
    }

    public SpcfCalendar convertToSpcfCalendar(String dateTimeString) {
        DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTime().withZoneUTC();
        DateTime dateTime = DATE_FORMAT.parseDateTime(dateTimeString);
        return SpcfCalendar.createInstance(dateTime.getMillis());
    }

    public static Options getOptions() {
        Options options = new Options();
        for (DefaultParameterConfig.Argument argument : DefaultParameterConfig.Argument.values()) {
            options.addOption(argument.getName(), true, argument.getDescription());
        }
        return options;
    }

    public <T> T getValue(CommandLine commandLine, DefaultParameterConfig.Argument argument) {
        Object value = null;
        String className = argument.getDefaultValue().getClass().getSimpleName();
        String commandOptionName = argument.getName();
        if(Objects.nonNull(commandLine) && commandLine.hasOption(commandOptionName)) {
            value = getValue(commandLine.getOptionValue(commandOptionName), className);
        }
        if(Objects.nonNull(value)) {
            return (T) value;
        }
        return getSystemParameterValueOrDefault(argument.getCode(), className, argument.getDefaultValue());
    }

    public SpcfCalendar getDate(CommandLine commandLine, DefaultParameterConfig.Argument argument) {
        SpcfCalendar date = null;
        String commandOptionName = argument.getName();
        if(Objects.nonNull(commandLine) && commandLine.hasOption(commandOptionName)) {
            date = getValue(commandLine.getOptionValue(commandOptionName), "SpcfCalendarImpl");
        }
        if(Objects.nonNull(date)) {
            return date;
        }
        return getSystemParameterValueOrDefault(argument.getCode(), "SpcfCalendarImpl", argument.getDefaultValue());
    }

    public <T> T getValue(String commandValue, String className) {
        Object object = null;
        switch (className) {
            case "Integer":
                object = NumberUtils.toInt(commandValue);
                break;
            case "String":
                object = commandValue;
                break;
            case "Boolean":
                object = BooleanUtils.toBoolean(commandValue);
                break;
            case "SMSMigrationStatus":
                object = SMSMigrationStatus.valueOf(commandValue);
                break;
            case "SpcfCalendarImpl":
                object = convertToSpcfCalendar(commandValue);
                break;
        }
        return (T) object;
    }

    public <T> T getSystemParameterValueOrDefault(SystemParameter.Code code, String className, Object defaultValue) {
        Object object = null;
        switch(className) {
            case "Integer":
                object = SystemParameter.findIntValue(code, (Integer) defaultValue);
                break;
            case "String":
                object = SystemParameter.findStringValue(code,(String) defaultValue);
                break;
            case "Boolean":
                object = SystemParameter.findBooleanValue(code, (Boolean) defaultValue);
                break;
            default:
                object = defaultValue;
        }
        return (T) object;
    }

    public enum Argument {

        //Workforce
        MAX_COMPANIES_PER_RUN("maxCompaniesPerRun", SystemParameter.Code.MAX_COMPANIES_PER_RUN, "Maximum Companies per batch job run", 1000),
        MAX_WORKFORCE_INVITE_PER_DAY("maxWorkforceInvitePerDay", SystemParameter.Code.MAX_WORKFORCE_INVITE_PER_DAY, "Maximum Workforce Invites per day", 50000),
        INVITATION_MODE_EMAIL_TEMPLATES("invitationModeEmailTemplates", null, "Name of OINP Email template names",
                "FreshInvites:WFOfflineInviteEmailTemplate_Default;ReEngageAutoInvited:WFOfflineReAutoInviteEmailTemplate_Default,WFOfflineReAutoInviteEmailTemplate_Second;ReEngageERInvited:WFOfflineReERInviteEmailTemplate_Default"),
        WORKFORCE_BULK_INVITE_COMPANY_IDS("companyIds", null, "List of companies to send workforce bulk invites", ""),
        LAST_PAYROLL_RUN_DURATION_COMPANY("lastPayrollRunDurationCompany", SystemParameter.Code.LAST_PAYROLL_RUN_DURATION_COMPANY, "Duration of Last Payroll Run for Company", 15),
        LAST_PAID_DURATION_EMPLOYEE("lastPaidDurationEmployee", SystemParameter.Code.LAST_PAID_DURATION_EMPLOYEE, "Duration of Last Paid for Employee", 90),
        SETTLEMENT_DATE_DURATION("settlementDateDuration", null, "Settlement Date Duration for Employee", 2),
        WORKFORCE_INVITE_COVERED("workforceInviteCovered", SystemParameter.Code.WORKFORCE_INVITE_COVERED, "Updated Workforce Invite after each job run", 50000),
        PUBLISH_STATUS_WORKFORCE("publishStatusWorkforce", SystemParameter.Code.PUBLISH_STATUS_WORKFORCE, "Publish Status for company","0"),
        MAX_ROWS_TO_FETCH("maxRowsToFetch", null, "Retrieving these number of rows from paycheck table", 5000),
        LIMIT_FOR_PERIOD("limitForPeriod", null, "Rate Limiter Limit per second for Invite API", 10),

        // PSPtoSMSMigration
        COMPANY_COUNT("companyCount", null, "Maximum Companies per batch job run", 10),
        SOURCE_COMPANY_IDS("sourceCompanyIds", null, "Companies per batch job run", ""),
        SMS_MIGRATION_STATUS("smsMigrationStatus", null, "SMS status", SMSMigrationStatus.DataCollectionComplete),
        REVERT_SMS_MIGRATED_COMPANIES("revertSMSMigratedCompanies", null, "revert SMS flags for sms migrated companies if true", false),
        DEBUG("debug",null,"Extra debugging added for monitoring", false),
        RISK_LIMIT_MIGRATION("riskLimitMigration", null, "Flag to enable Risk Limits migration (from PSP to AMS)", false),

        //Real Time Entity Publish Retry Batch Job
        RETRY_REALTIME_ENTITY_BATCH_SIZE("batchSize", null, "Entities per batch job run", Integer.MAX_VALUE),
        RETRY_REALTIME_ENTITY_CHUNK_SIZE("chunkSize", null, "Entities partition chunk for commit", 10),
        RETRY_REALTIME_ENTITY_IDS("entityId", null, "Entity ids", ""),
        RETRY_REALTIME_ENTITY_START_TIME("startTime", null, "Start time", null),
        RETRY_REALTIME_ENTITY_END_TIME("endTime", null, "End time", null),
        RETRY_REALTIME_ENTITY_STATUS("entityStatus", null, "Entity Status to be processed", "Failed,InProgress"),
        RETRY_REALTIME_ENTITY_NAME("entityName", null, "Entity Names to process", ""),

        IS_DD_QUERY("isDDQuery", null,"Boolean value for DD / DD+Paper only", true);

        private String name;
        private SystemParameter.Code code;
        private String description;
        private Object defaultValue;

        Argument(String name, SystemParameter.Code code, String description, Object defaultValue) {
            this.name = name;
            this.code = code;
            this.description = description;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        public SystemParameter.Code getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }
}
