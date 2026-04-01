package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail;

import com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail.models.ConfigFileModel;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author vdammur1
 */
@Slf4j
public class CustomEmailWorkFlowProcessor<T> {
    private Set<String> workflowsToExecute;
    private ConfigFileModel[] configFileModelArray;

    public CustomEmailWorkFlowProcessor(ConfigFileModel[] configFileModelArray, Set<String> workflowsToExecute) {
        this.workflowsToExecute = workflowsToExecute;
        this.configFileModelArray = configFileModelArray;
    }

    public void process() {
        String localInputDir = BatchUtils.getConfigString("psp_custom_email_local_input_dir");
        // Deserialize the input files and process the file one by one
        for(int i = 0; i < configFileModelArray.length; i++) {
            ConfigFileModel config = configFileModelArray[i];
            String workflow = config.getWorkflowName();
            if(workflowsToExecute.contains(workflow)) {
                String inputFilesDir = localInputDir + File.separator + config.getRelativeDir();
                log.info("job=SendCustomEmailsProcessor, Action=CustomEmailWorkFlowProcessor, Method=processAll, inputFilesDir={}", inputFilesDir);
                InputFileDeserializer inputFileDeserializer = CustomEmailInputFileDeserializerFactory.getInstance(workflow);
                File[] inputFiles = new File(inputFilesDir).listFiles();
                if(Objects.isNull(inputFiles) || inputFiles.length == 0) {
                    log.error("job=SendCustomEmailsProcessor, Action=CustomEmailWorkFlowProcessor, Method=processAll, Status=Error, Msg=NoInputFilesFound");
                    return;
                }
                for (File inputFile : inputFiles) {
                    if (inputFile.isFile()) {
                        String inputFilePath = inputFile.getAbsolutePath();
                        if(inputFilePath.endsWith("csv")) {
                            log.info("job=SendCustomEmailsProcessor, Action=CustomEmailWorkFlowProcessor, Method=processAll, inputFilePath={}", inputFilePath);
                            List<T> input = inputFileDeserializer.deserialize(inputFilePath);
                            ICustomEmailWorkFlowProcessor workFlowInstance = new CustomEmailWorkFlowFactory().getInstance(workflow);
                            workFlowInstance.process(input, inputFile.getName());
                        }
                    } else {
                        log.error("job=SendCustomEmailsProcessor, Action=CustomEmailWorkFlowProcessor, Method=processAll, Status=Error, Msg=NotAFile, FilePath={}", inputFile.getAbsolutePath());
                    }
                }

            }
        }
    }
}
