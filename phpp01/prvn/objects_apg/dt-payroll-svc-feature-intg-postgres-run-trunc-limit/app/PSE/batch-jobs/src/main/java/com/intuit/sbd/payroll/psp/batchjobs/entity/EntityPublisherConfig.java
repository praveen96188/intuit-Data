package com.intuit.sbd.payroll.psp.batchjobs.entity;

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.PublishStatusWorkflowState;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.company.CompanyPublishStatusWorkflows;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import static com.intuit.sbd.payroll.psp.domain.SystemParameter.Code;

@Getter
@Setter
public class EntityPublisherConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityPublisherConfig.class);
    private final CommandLineParser commandLineParser = new PosixParser();
    private int chunkSize;
    private int batchSize;
    private SpcfCalendar batchLastProcessed = null;
    private SpcfCalendar batchStartTime = SpcfCalendar.getNow();
    private CompanyPublishStatusWorkflows companyPublishStatusWorkflows;
    private PublishStatusWorkflowState publishStatusWorkflowState;
    private String topicName = "";
    private List<String> sourceCompanyIds;
    private boolean republishMode;
    private NamedQueryEnum namedQuery;

    public EntityPublisherConfig() {
        this(null);
    }

    public SpcfCalendar convertToSpcfCalendar(String dateTimeString) {
        DateTime lastProcessedTime = null;
        DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.dateTime().withZoneUTC();
        lastProcessedTime = DATE_FORMAT.parseDateTime(dateTimeString);
        return SpcfCalendar.createInstance(lastProcessedTime.getMillis());
    }

    public String getCustomTopicName(CommandLine commandLine, Argument argument) {
        String value = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_jss_internal_queue_topic_name");
        if (Objects.nonNull(commandLine) && commandLine.hasOption(argument.getName())) {
            value = commandLine.getOptionValue(argument.getName());
        }
        return value;
    }

    public NamedQueryEnum getCustomNamedQuery(CommandLine commandLine, Argument argument) {
        String value = "";
        if (Objects.nonNull(commandLine) && commandLine.hasOption(argument.getName())) {
            value = commandLine.getOptionValue(argument.getName());
        }
        return StringUtils.isEmpty(value) ? null : NamedQueryEnum.valueOf(value);
    }

    public List<String> getCustomSourceCompanyIds(CommandLine commandLine, Argument argument) {
        List<String> list = new ArrayList<>();
        if (Objects.nonNull(commandLine) && commandLine.hasOption(argument.getName())) {
            list = new ArrayList<>(Arrays.asList(commandLine.getOptionValue(argument.getName()).split(",")));
        }
        return list;
    }

    public Boolean getCustomRepublishStatus(CommandLine commandLine, Argument argument) {
        Boolean value = false;
        if (Objects.nonNull(commandLine) && commandLine.hasOption(argument.getName())) {
            value = Boolean.parseBoolean(commandLine.getOptionValue(argument.getName()));
        }
        return value;
    }

    public SpcfCalendar getCustomBatchStartTime(CommandLine commandLine, Argument argument) {
        SpcfCalendar batchStartTime = null;
        if (Objects.nonNull(commandLine) && commandLine.hasOption(argument.getName())) {
            batchStartTime = convertToSpcfCalendar(commandLine.getOptionValue(argument.getName()));
        } else {
            batchStartTime = SpcfCalendar.getNow();
        }
        return batchStartTime;
    }

    public EntityPublisherConfig(String[] args) {
        try {
            CommandLine commandLine = getCommandLine(args);
            for (Argument argument : Argument.values()) {
                switch (argument) {
                    case ENTITY_BATCH_SIZE:
                        setBatchSize(getValue(commandLine, argument));
                        break;
                    case ENTITY_CHUNK_SIZE:
                        setChunkSize(getValue(commandLine, argument));
                        break;
                    case BATCH_TARGETED_FOR_SERVICE:
                        setCompanyPublishStatusWorkflows(getValue(commandLine, argument));
                        break;
                    case PUBLISH_STATUS_MODE:
                        setPublishStatusWorkflowState(getValue(commandLine, argument));
                        break;
                    case BATCH_START_TIME:
                        setBatchStartTime(getCustomBatchStartTime(commandLine, argument));
                        break;
                    case BATCH_LAST_PROCESSED_TIME:
                        setBatchLastProcessed(getValue(commandLine, argument));
                        break;
                    case BATCH_TOPIC_NAME:
                        setTopicName(getCustomTopicName(commandLine, argument));
                        break;
                    case BATCH_SOURCE_COMPANY_IDS:
                        setSourceCompanyIds(getCustomSourceCompanyIds(commandLine, argument));
                        break;
                    case BATCH_REPUBLISH:
                        setRepublishMode(getCustomRepublishStatus(commandLine, argument));
                        break;
                    case NAMED_QUERY:
                        setNamedQuery(getCustomNamedQuery(commandLine, argument));
                        break;
                }
            }
            LOGGER.info(toString());
        } catch (ParseException e) {
            LOGGER.error("Failed to parse the command line options", e);
            throw new RuntimeException("Failed to parse the command line options", e);
        }
    }

    public static Options getOptions() {
        Options options = new Options();
        for (Argument argument : Argument.values()) {
            options.addOption(argument.getName(), true, argument.getDescription());
        }
        return options;
    }

    private <T> T getValue(CommandLine commandLine, Argument argument) {
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

    private <T> T getValue(String commandValue, String className) {
        Object object = null;
        switch (className) {
            case "Integer":
                object = NumberUtils.toInt(commandValue);
                break;
            case "String":
                object = commandValue;
                break;
            case "SpcfCalendarImpl":
                object = convertToSpcfCalendar(commandValue);
                break;
            case "CompanyPublishStatusWorkflows":
                object = CompanyPublishStatusWorkflows.valueOf(commandValue);
                break;
            case "PublishStatusWorkflowState":
                object = PublishStatusWorkflowState.workflowState(NumberUtils.toInt(commandValue));
                break;
        }
        return (T) object;
    }

    private <T> T getSystemParameterValueOrDefault(SystemParameter.Code code, String className, Object defaultValue) {
        Object object = null;
        switch(className) {
            case "Integer":
                object = SystemParameter.findIntValue(code, (Integer) defaultValue);
                break;
            case "String":
                object = SystemParameter.findStringValue(code,(String) defaultValue);
                break;
            case "SpcfCalendarImpl":
                object =  convertToSpcfCalendar(SystemParameter.findStringValue(code));
                break;
            case "CompanyPublishStatusWorkflows":
                object = CompanyPublishStatusWorkflows.valueOf(SystemParameter.findStringValue(code));
                break;
            case "PublishStatusWorkflowState":
                object = PublishStatusWorkflowState.workflowState(NumberUtils.toInt(SystemParameter.findStringValue(code)));
                break;
        }
        return (T) object;
    }

    private CommandLine getCommandLine(String[] args) throws ParseException {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }
        return commandLineParser.parse(getOptions(), args);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("job=initial_load,EntityPublisherConfig{");
        sb.append("batchSize=").append(batchSize);
        sb.append(", chunkSize=").append(chunkSize);
        sb.append(", targetedService=").append(companyPublishStatusWorkflows);
        sb.append(", republish=").append(republishMode);
        sb.append(", publishStatusMode=").append(publishStatusWorkflowState);
        sb.append(", topicName=").append(topicName);
        sb.append(", lastProcessedTime=").append(batchLastProcessed);
        sb.append(", batchStartTime=").append(batchStartTime);
        sb.append(", psIds=").append(sourceCompanyIds);
        sb.append(", namedQuery=").append(namedQuery);
        sb.append('}');
        return sb.toString();
    }

    public enum Argument {
        ENTITY_BATCH_SIZE("batchSize", Code.ENTITY_PUBLISH_BATCH_SIZE, "Batch size", 5000),
        ENTITY_CHUNK_SIZE("chunkSize", Code.ENTITY_CHUNK_SIZE, "Chunk size", 10),
        BATCH_TARGETED_FOR_SERVICE("targetedService", Code.BATCH_TARGETED_FOR_SERVICE, "Targeted Service", CompanyPublishStatusWorkflows.EVS),
        PUBLISH_STATUS_MODE("publishStatusMode", Code.ENTITY_PUBLISH_STATUS, "Publish Status Mode-publish/retry/republish", PublishStatusWorkflowState.INITIAL),
        BATCH_START_TIME("startTime", Code.ENTITY_PUBLISH_BATCH_SIZE, "Start date", SpcfCalendar.getNow()),
        BATCH_LAST_PROCESSED_TIME("lastProcessedTime", Code.EVS_LAST_PROCESSED_TIME, "Last successful publish time for EVS", SpcfCalendar.getNow()),
        BATCH_TOPIC_NAME("topicName", null, "Topic name ", ""),
        BATCH_SOURCE_COMPANY_IDS("psIds", null, "List of PSID to be published", ""),
        BATCH_REPUBLISH("republish", null, "Republish all the entities", false),
        NAMED_QUERY("namedQuery", null, "Named query to fetch Entities", "");

        private String name;
        private SystemParameter.Code code;
        private String description;
        private Object defaultValue;

        Argument(String name, Code code, String description, Object defaultValue) {
            this.name = name;
            this.code = code;
            this.description = description;
            this.defaultValue = defaultValue;
        }

        public String getName() {
            return name;
        }

        public Code getCode() {
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
