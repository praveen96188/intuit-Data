package com.intuit.sbd.payroll.psp.jss.processors;

import com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail.*;
import com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail.models.ConfigFileModel;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.jss.JSSAutoScheduleGenerator;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJob;
import com.intuit.sbd.payroll.psp.jss.JSSBatchJobStep;
import com.intuit.sbd.payroll.psp.jss.JSSScheduleGenerator;
import com.intuit.sbg.shared.batchjob.annotations.ScheduledJob;

import java.io.File;
import java.util.Set;

/**
 * @author vdammur1
 */

@ScheduledJob(name = "SendCustomEmailsProcessor", resourcePath = "/normal", autoScheduleGenerator = JSSAutoScheduleGenerator.class, scheduleGenerator = JSSScheduleGenerator.class)
public class SendCustomEmailsProcessor extends JSSBatchJob {
    private Set<String> workflowsToExecute;
    private ConfigFileModel[] configFileModelArray;

    public SendCustomEmailsProcessor(String[] pArguments) {
        super(pArguments);
    }

    public SendCustomEmailsProcessor(String[] pArguments, String pJobId) {
        super(pArguments, pJobId);
    }

    @Override
    protected void execute() throws Exception {
        getLogger().info("Started  Send Custom Email Processor Batch Job");
        try {
            StopWatch timer = StopWatch.startTimer();
            executeStep(DownloadConfigFile.class);
            executeStep(DeserializeConfigFile.class);
            executeStep(WorkFlowDecider.class);
            executeStep(DownloadInputFiles.class);
            executeStep(DeserializeAndProcessInputFiles.class);
            getLogger().info("Completed Send Custom Email Processor batch job. Elapsed time: " + timer.stop().getElapsedTimeString());
        } catch (Exception e) {
            throw e;
        } finally {
            executeStep(DeleteProcessedFilesAndDir.class);
        }
    }

    public static class DownloadConfigFile extends JSSBatchJobStep<SendCustomEmailsProcessor> {
        @Override
        protected void execute() throws Exception {
            try {
                getLogger().info("job=SendCustomEmailsProcessor, Action=DownloadConfigFile, Status=Start");
                String s3RootDir = BatchUtils.getConfigString("psp_custom_email_s3_root_dir");
                String localDir = BatchUtils.getConfigString("psp_custom_email_local_root_dir");
                String configFileName = BatchUtils.getConfigString("psp_custom_email_config_file_name");
                String s3FilePath = s3RootDir + File.separator + configFileName;
                String localFilePath = localDir + File.separator + configFileName;
                getLogger().info("job=SendCustomEmailsProcessor, Action=DownloadConfigFile, s3FilePath={}, localFilePath={}", s3FilePath, localFilePath);
                new S3FileDownloader().downloadFile(s3FilePath, localFilePath);
                getLogger().info("job=SendCustomEmailsProcessor, Action=DownloadConfigFile, Status=Complete");
            } catch (Exception e) {
                getLogger().error("job=SendCustomEmailsProcessor, Action=DownloadConfigFile, Status=Error", e);
                throw e;
            }
        }
    }

    public static class DeserializeConfigFile extends JSSBatchJobStep<SendCustomEmailsProcessor> {
        @Override
        protected void execute() {
            try {
                getLogger().info("job=SendCustomEmailsProcessor, Action=DeserializeConfigFile, Status=Start");
                getBatchJobProcessor().configFileModelArray =  new ConfigFileDeserializer().deserializeConfigFile();
                for(ConfigFileModel configFileModel: getBatchJobProcessor().configFileModelArray) {
                    getLogger().info("job=SendCustomEmailsProcessor, Action=DeserializeConfigFile, ConfigValues={}", configFileModel.toString());
                }
                getLogger().info("job=SendCustomEmailsProcessor, Action=DeserializeConfigFile, Status=Complete");
            } catch (Exception e) {
                getLogger().error("job=SendCustomEmailsProcessor, Action=DeserializeConfigFile, Status=Error", e);
                throw e;
            }
        }
    }

    public static class WorkFlowDecider extends JSSBatchJobStep<SendCustomEmailsProcessor> {
        @Override
        protected void execute() throws Exception {
            try {
                getLogger().info("job=SendCustomEmailsProcessor, Action=WorkFlowDecider, Status=Start");
                getBatchJobProcessor().workflowsToExecute = new CustomEmailWorkFlowManager().getWorkflowsToExecute(getBatchJobProcessor().configFileModelArray);
                for(String workflow: getBatchJobProcessor().workflowsToExecute) {
                    getLogger().info("job=SendCustomEmailsProcessor, Action=WorkFlowDecider, Msg=WorkflowToBeExecuted, workflow={}", workflow);
                }
                getLogger().info("job=SendCustomEmailsProcessor, Action=WorkFlowDecider, Status=Complete");
            } catch (Exception e) {
                getLogger().error("job=SendCustomEmailsProcessor, Action=WorkFlowDecider, Status=Error", e);
                throw e;
            }
        }
    }

    public static class DownloadInputFiles extends JSSBatchJobStep<SendCustomEmailsProcessor> {
        @Override
        protected void execute() throws Exception {
            try {
                getLogger().info("job=SendCustomEmailsProcessor, Action=DownloadInputFiles, Status=Start");
                new CustomEmailInputFileDownloader(getBatchJobProcessor().configFileModelArray, getBatchJobProcessor().workflowsToExecute).downloadAndArchiveInputFiles();
                getLogger().info("job=SendCustomEmailsProcessor, Action=DownloadInputFiles, Status=Complete");
            } catch (Exception e) {
                getLogger().info("job=SendCustomEmailsProcessor, Action=DownloadInputFiles, Status=Error", e);
                throw e;
            }
        }
    }

    public static class DeserializeAndProcessInputFiles extends JSSBatchJobStep<SendCustomEmailsProcessor> {
        @Override
        protected void execute() throws Exception {
            try {
                getLogger().info("job=SendCustomEmailsProcessor, Action=DeserializeAndProcessInputFiles, Status=Start");
                new CustomEmailWorkFlowProcessor(getBatchJobProcessor().configFileModelArray, getBatchJobProcessor().workflowsToExecute).process();
                getLogger().info("job=SendCustomEmailsProcessor, Action=DeserializeAndProcessInputFiles, Status=Complete");
            } catch (Exception e) {
                getLogger().error("job=SendCustomEmailsProcessor, Action=DeserializeAndProcessInputFiles, Status=Error", e);
                throw e;
            }
        }
    }


    public static class DeleteProcessedFilesAndDir extends JSSBatchJobStep<SendCustomEmailsProcessor> {
        @Override
        protected void execute() throws Exception {
            try {
                getLogger().info("job=SendCustomEmailsProcessor, Action=DeleteProcessedFiles, Status=Start");
                String localDir = BatchUtils.getConfigString("psp_custom_email_local_root_dir");
                new CustomEmailFileUtility(getBatchJobProcessor().configFileModelArray, getBatchJobProcessor().workflowsToExecute).deleteFilesAndFoldersFromDir(localDir);
                getLogger().info("job=SendCustomEmailsProcessor, Action=DeleteProcessedFiles, Status=Complete");
            } catch (Exception e) {
                getLogger().info("job=SendCustomEmailsProcessor, Action=DeleteProcessedFiles, Status=Error", e);
                throw e;
            }
        }
    }
}
