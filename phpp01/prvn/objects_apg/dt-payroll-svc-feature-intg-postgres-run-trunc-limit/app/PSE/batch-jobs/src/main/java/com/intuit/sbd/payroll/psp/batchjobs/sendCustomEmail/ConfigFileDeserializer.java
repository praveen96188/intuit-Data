package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail;

import com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail.models.ConfigFileModel;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Objects;

/**
 * @author vdammur1
 */
@Slf4j
public class ConfigFileDeserializer {

    private ConfigFileParser configFileParser;

    public ConfigFileDeserializer() {
        configFileParser = PayrollApplicationBeanFactory.getBean(ConfigFileParser.class);
    }

    public ConfigFileModel[] deserializeConfigFile() {
        String localRootDir = BatchUtils.getConfigString("psp_custom_email_local_root_dir");
        String configFileName = BatchUtils.getConfigString("psp_custom_email_config_file_name");
        String absoluteFilePath = localRootDir + File.separator + configFileName;
        log.info("job=SendCustomEmailsProcessor, Action=ConfigFileDeserializer, FilePath={}", absoluteFilePath);
        ConfigFileModel[] configFileModels =  configFileParser.getJsonConfig(absoluteFilePath);
        checkForNullValues(configFileModels);
        return configFileModels;
    }

    private void checkForNullValues(ConfigFileModel[] configFileModels) {
        for(ConfigFileModel configFileModel: configFileModels) {
            String workflowName = configFileModel.getWorkflowName();
            String inputFileRelativeDir = configFileModel.getRelativeDir();
            String scheduledTime = configFileModel.getScheduledTime();
            Boolean overrideScheduleTime = configFileModel.isOverrideScheduleTime();

            Objects.requireNonNull(workflowName, "workflowName cannot be null");
            Objects.requireNonNull(inputFileRelativeDir, "inputFileRelativeDir cannot be null");
            Objects.requireNonNull(scheduledTime, "scheduledTime cannot be null");
            Objects.requireNonNull(overrideScheduleTime, "overrideScheduleTime cannot be null");
        }
    }
}
