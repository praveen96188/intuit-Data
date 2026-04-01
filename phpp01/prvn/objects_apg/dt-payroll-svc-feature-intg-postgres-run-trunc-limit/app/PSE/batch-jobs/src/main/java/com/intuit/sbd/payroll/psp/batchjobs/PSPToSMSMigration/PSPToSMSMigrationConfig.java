package com.intuit.sbd.payroll.psp.batchjobs.PSPToSMSMigration;

import com.intuit.sbd.payroll.psp.batchjobs.DefaultParameterConfig;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class PSPToSMSMigrationConfig extends DefaultParameterConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(PSPToSMSMigrationConfig.class);

    private EnumSet<SMSMigrationStatus> smsMigrationStatusSupportedList = EnumSet.of(SMSMigrationStatus.DataCollectionComplete,
                                                                    SMSMigrationStatus.MigrationError,
                                                                    SMSMigrationStatus.MigrationOnHold,
                                                                    SMSMigrationStatus.MigrationInProgress,
                                                                    SMSMigrationStatus.MigrationComplete,
                                                                    SMSMigrationStatus.MigrationReverted);

    private int companyCount;
    private List<String> sourceCompanyIds;
    private SMSMigrationStatus smsMigrationStatus;
    private boolean revertSMSMigratedCompanies;
    private boolean isDebugLogEnabled;
    private boolean riskLmtMigrationEnabled;


    public PSPToSMSMigrationConfig(String[] args) {
        try {
            CommandLine commandLine = getCommandLine(args);
            for (Argument argument : Argument.values()) {
                switch (argument) {
                    case COMPANY_COUNT:
                        setCompanyCount(getValue(commandLine, argument));
                        break;
                    case SOURCE_COMPANY_IDS:
                        setSourceCompanyIds(getList(commandLine, argument));
                        break;
                    case SMS_MIGRATION_STATUS:
                        setSmsMigrationStatus(getValue(commandLine, argument));
                        break;
                    case  REVERT_SMS_MIGRATED_COMPANIES:
                        setRevertSMSMigratedCompanies(getValue(commandLine,argument));
                        break;
                    case DEBUG:
                        setDebugLogEnabled(getValue(commandLine,argument));
                        break;
                    case RISK_LIMIT_MIGRATION:
                        setRiskLmtMigrationEnabled(getValue(commandLine, argument));
                        break;
                }
            }
            LOGGER.info("PSPToSMSMigrationConfig : " +  this.toString());
            validateParamCombination(commandLine);
        } catch (ParseException e) {
            LOGGER.error("Failed to parse the command line options", e);
            throw new IllegalArgumentException("PSPtoSMSMigration Failed to parse the command line options", e);
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    private void validateParamCombination(CommandLine commandLine) {

        //check company count value from command line
        Object value = null;
        Argument argument= Argument.COMPANY_COUNT;
        String className = argument.getDefaultValue().getClass().getSimpleName();
        String commandOptionName = argument.getName();
        if(Objects.nonNull(commandLine) && commandLine.hasOption(commandOptionName)) {
            value = getValue(commandLine.getOptionValue(commandOptionName), className);
        }

        // both company count and source company id should not be present in command line
        if(value!=null && getSourceCompanyIds().size() > 0)
        {
            throw new IllegalArgumentException("PSPtoSMSMigration : Invalid Arguments - both company count and source company id should not be present in command line");
        }

        // REVERT SMS MIGRATED COMPANIES SHOULD PROVIDE PSIDS AS WELL
        if (revertSMSMigratedCompanies && (getSourceCompanyIds() == null || getSourceCompanyIds().size() == 0)) {
            throw new IllegalArgumentException("PSPtoSMSMigration : Invalid Arguments - revertSMSMigratedCompanies need smsMigrationCompanyIds to be passed");
        }
        // REVERT SMS MIGRATED COMPANIES SHOULD BE RUN ONLY ON MIGRATION COMPLETE STATUS
        else if (revertSMSMigratedCompanies && getSmsMigrationStatus() != SMSMigrationStatus.MigrationComplete) {
            throw new IllegalArgumentException("PSPtoSMSMigration : Invalid Arguments - revert only Migration Complete Companies");
        }
        // SMSMIGRATION STATUS SHOULD BE ONLY WHAT IS
        else if (!smsMigrationStatusSupportedList.contains(getSmsMigrationStatus())) {
            throw new IllegalArgumentException("PSPtoSMSMigration : Invalid Arguments - smsMigrationStatus not supported");
        }
    }
}
