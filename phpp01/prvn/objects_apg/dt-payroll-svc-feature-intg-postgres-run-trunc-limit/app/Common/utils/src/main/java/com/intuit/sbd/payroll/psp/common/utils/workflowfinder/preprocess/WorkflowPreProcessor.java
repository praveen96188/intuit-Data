package com.intuit.sbd.payroll.psp.common.utils.workflowfinder.preprocess;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.S3ConnectionException;
import com.intuit.sbd.payroll.psp.common.utils.S3DownloadException;
import com.intuit.sbd.payroll.psp.common.utils.S3UploadUtils;
import com.intuit.sbd.payroll.psp.common.utils.workflowfinder.WorkflowFinderConstants;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Component
public class WorkflowPreProcessor {

    private WorkflowDigestHelper workflowDigestHelper;
    private FileReader fileReader;
    private Map<Integer, String> workflowDigestMap;

    private static final SpcfLogger LOGGER = Application.getLogger(WorkflowPreProcessor.class);

    @Autowired
    public WorkflowPreProcessor(WorkflowDigestHelper workflowDigestHelper,
                                FileReader fileReader) {
        this.workflowDigestHelper = workflowDigestHelper;
        this.fileReader = fileReader;
    }

    @PostConstruct
    public void preProcess() {
        try {
            LOGGER.info("Event=WorkflowPreprocess, Status=Start");
            if(!Application.isParallelEnv() && !Application.isIntegrationTestEnvironment()) {
                downloadS3File();
            }
            List<String> lines = readLocalFile();
            workflowDigestMap = preProcessWorkflows(lines);
            LOGGER.info("Event=WorkflowPreprocess, Status=Done");
        } catch (S3DownloadException | S3ConnectionException e) {
            LOGGER.warn("Event=WorkflowPreprocess, Status=Failed " + e);
        } catch (Exception e) {
            LOGGER.warn("Event=WorkflowPreprocess, Status=Failed " + e);
        }
    }

    public Map<Integer, String> getWorkflowDigestMap() {
        return workflowDigestMap;
    }

    private Map<Integer, String> preProcessWorkflows(List<String> lines) {
        LOGGER.info("Event=WorkflowPreprocess, SubEvent=PrepareDigest, Status=Start");
        Map<Integer, String> workflowDigestMap = workflowDigestHelper.preProcess(lines);
        LOGGER.info("Event=WorkflowPreprocess, SubEvent=PrepareDigest, Status=Done");
        return workflowDigestMap;
    }

    private List<String> readLocalFile() {
        List<String> lines = null;
        LOGGER.info("Event=WorkflowPreprocess, SubEvent=ReadLocalFile, Status=Start, FilePath=" + WorkflowFinderConstants.WORKFLOW_LOCAL_DIR + WorkflowFinderConstants.SLASH_SEPARATOR + WorkflowFinderConstants.WORKFLOW_S3FILE_NAME);
        if(Application.isParallelEnv() || Application.isIntegrationTestEnvironment()) {
            lines = fileReader.read(Application.findFileObjectOnClassPath("workflow-report.csv").getAbsolutePath());
        } else {
            lines = fileReader.read(WorkflowFinderConstants.WORKFLOW_LOCAL_DIR + WorkflowFinderConstants.SLASH_SEPARATOR + WorkflowFinderConstants.WORKFLOW_S3FILE_NAME);
        }
        LOGGER.info("Event=WorkflowPreprocess, SubEvent=ReadLocalFile, Status=Done, FilePath=" + WorkflowFinderConstants.WORKFLOW_LOCAL_DIR + WorkflowFinderConstants.SLASH_SEPARATOR + WorkflowFinderConstants.WORKFLOW_S3FILE_NAME);
        return lines;
    }

    private void downloadS3File() throws S3ConnectionException, S3DownloadException {
        LOGGER.info("Event=WorkflowPreprocess, SubEvent=DownloadS3File, Status=Start, FilePath=" + WorkflowFinderConstants.WORKFLOW_BUCKET_NAME + WorkflowFinderConstants.WORKFLOW_S3FILE_NAME);
        String bucketName = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, S3UploadUtils.PSP_BATCHJOBS_S3_BUCKET);
        S3UploadUtils.downloadFromS3FileStore(bucketName, WorkflowFinderConstants.WORKFLOW_S3FILE_NAME, WorkflowFinderConstants.WORKFLOW_LOCAL_DIR + WorkflowFinderConstants.SLASH_SEPARATOR + WorkflowFinderConstants.WORKFLOW_S3FILE_NAME);
        LOGGER.info("Event=WorkflowPreprocess, SubEvent=DownloadS3File, Status=Done, FilePath=" + WorkflowFinderConstants.WORKFLOW_BUCKET_NAME + WorkflowFinderConstants.WORKFLOW_S3FILE_NAME);
    }

}
