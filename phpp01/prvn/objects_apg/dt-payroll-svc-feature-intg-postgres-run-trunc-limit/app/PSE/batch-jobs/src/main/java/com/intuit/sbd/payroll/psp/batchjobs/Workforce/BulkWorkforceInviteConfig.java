package com.intuit.sbd.payroll.psp.batchjobs.Workforce;

import com.intuit.sbd.payroll.psp.batchjobs.DefaultParameterConfig;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class BulkWorkforceInviteConfig extends DefaultParameterConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkWorkforceInviteConfig.class);

    private int maxCompaniesPerRun;
    private int maxWorkforceInvitePerDay;
    private List<SpcfUniqueId> companyIds;
    private int lastPayrollRunDurationCompany;
    private int lastPaidDurationEmployee;
    private int settlementDateDuration;
    private int workforceInviteCovered;
    private String publishStatusWorkforce;
    private int maxRowsToFetch;
    private int limitForPeriod;
    private boolean isDDQuery;
    private Map<InvitationMode,List<String>> invitationModeEmailTemplatesListMap;

    public BulkWorkforceInviteConfig(String[] args) {
        try {
            CommandLine commandLine = getCommandLine(args);
            for (DefaultParameterConfig.Argument argument : DefaultParameterConfig.Argument.values()) {
                switch (argument) {
                    case MAX_COMPANIES_PER_RUN:
                        setMaxCompaniesPerRun(getValue(commandLine, argument));
                        break;
                    case MAX_WORKFORCE_INVITE_PER_DAY:
                        setMaxWorkforceInvitePerDay(getValue(commandLine, argument));
                        break;
                    case INVITATION_MODE_EMAIL_TEMPLATES:
                        setInvitationModeEmailTemplatesListMap(getInvModeEmailTemplatesMap(commandLine));
                        break;
                    case WORKFORCE_BULK_INVITE_COMPANY_IDS:
                        setCompanyIds(convertListOfStringToSpcfUniqueId(getList(commandLine, argument)));
                        break;
                    case LAST_PAYROLL_RUN_DURATION_COMPANY:
                        setLastPayrollRunDurationCompany(getValue(commandLine, argument));
                        break;
                    case LAST_PAID_DURATION_EMPLOYEE:
                        setLastPaidDurationEmployee(getValue(commandLine, argument));
                        break;
                    case SETTLEMENT_DATE_DURATION:
                        setSettlementDateDuration(getValue(commandLine, argument));
                        break;
                    case WORKFORCE_INVITE_COVERED:
                        setWorkforceInviteCovered(getValue(commandLine, argument));
                        break;
                    case PUBLISH_STATUS_WORKFORCE:
                        setPublishStatusWorkforce(getValue(commandLine, argument));
                        break;
                    case MAX_ROWS_TO_FETCH:
                        setMaxRowsToFetch(getValue(commandLine, argument));
                        break;
                    case LIMIT_FOR_PERIOD:
                        setLimitForPeriod(getValue(commandLine, argument));
                        break;
                    case IS_DD_QUERY:
                        setDDQuery(getValue(commandLine, argument));
                        break;
                }
            }
            LOGGER.info("BulkWorkforceInviteConfig={}", this.toString());
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse the command line options", e);
        } catch (Exception e) {
            LOGGER.error("Failed with error other than ParseException, Error={}", e.getMessage(), e);
            throw e;
        }
    }

    public List<SpcfUniqueId> convertListOfStringToSpcfUniqueId(List<String> list) {
        return list.stream().map(SpcfUniqueIdImpl::new).collect(Collectors.toList());
    }

    public Map<InvitationMode,List<String>> getInvModeEmailTemplatesMap(CommandLine commandLine) {
        if (Objects.nonNull(commandLine) && commandLine.hasOption(Argument.INVITATION_MODE_EMAIL_TEMPLATES.getName())) {
            return getInvModeEmailTemplatesMapFromString(commandLine.getOptionValue(Argument.INVITATION_MODE_EMAIL_TEMPLATES.getName()));
        } else {
            return getInvModeEmailTemplatesMapFromString((String) Argument.INVITATION_MODE_EMAIL_TEMPLATES.getDefaultValue());
        }
    }

    public Map<InvitationMode,List<String>> getInvModeEmailTemplatesMapFromString(String mapAsString) {
        //get K:V pairs by splitting on ;
        List<String> keyValuePairs = Arrays.asList(mapAsString.split(";"));

        //loop over key-value pair strings ("K:V1")
        Map<InvitationMode,List<String>> map = new HashMap<>();
        for(String keyValuePair: keyValuePairs) {
            //split each keyValue string on :, K will be first element
            String[] keyValue = keyValuePair.split(":");
            String keyStr = keyValue[0];
            InvitationMode key = InvitationMode.valueOf(keyStr);
            //since V is a list of strings,
            List<String> value = Arrays.asList(keyValue[1].split(","));

            map.put(key,value);
        }
        return map;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
