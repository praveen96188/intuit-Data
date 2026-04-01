package com.intuit.sbd.payroll.psp.batchjobs.entity.retry;

import com.intuit.sbd.payroll.psp.batchjobs.DefaultParameterConfig;
import com.intuit.sbd.payroll.psp.domain.Status;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class RealTimeEntityEventRetryConfig extends DefaultParameterConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(RealTimeEntityEventRetryConfig.class);
    private int batchSize;
    private int chunkSize;
    private List<SpcfUniqueId> entityIds;
    private SpcfCalendar startTime;
    private SpcfCalendar endTime;
    private List<Status> statuses;
    private List<String> entityNamesToPublish;

    public RealTimeEntityEventRetryConfig(String[] args) {
        try {
            CommandLine commandLine = getCommandLine(args);
            for (Argument argument : Argument.values()) {
                switch (argument) {
                    case RETRY_REALTIME_ENTITY_BATCH_SIZE:
                        setBatchSize(getValue(commandLine, argument));
                        break;
                    case RETRY_REALTIME_ENTITY_CHUNK_SIZE:
                        setChunkSize(getValue(commandLine, argument));
                        break;
                    case RETRY_REALTIME_ENTITY_IDS:
                        setEntityIds(convertListOfStringToSpcfUniqueId(getList(commandLine, argument)));
                        break;
                    case RETRY_REALTIME_ENTITY_START_TIME:
                        setStartTime(getDate(commandLine, argument));
                        break;
                    case RETRY_REALTIME_ENTITY_END_TIME:
                        setEndTime(getDate(commandLine, argument));
                        break;
                    case RETRY_REALTIME_ENTITY_STATUS:
                        setStatuses(convertListOfStringToStatus(getList(commandLine, argument)));
                        break;
                    case RETRY_REALTIME_ENTITY_NAME:
                        setEntityNamesToPublish(getList(commandLine, argument));
                        break;
                }
            }
            LOGGER.info("RealTimeEntityEventRetryConfig={}", this.toString());
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse the command line options", e);
        } catch (Exception e) {
            LOGGER.error("Failed with error other than ParseException, Error={}", e.getMessage(), e);
            throw e;
        }
    }

    private List<Status> convertListOfStringToStatus(List<String> list) {
        return list.stream().map(Status::valueOf).collect(Collectors.toList());
    }

    public List<SpcfUniqueId> convertListOfStringToSpcfUniqueId(List<String> list) {
        return list.stream().map(SpcfUniqueIdImpl::new).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
