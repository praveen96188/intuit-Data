package com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail;

import com.intuit.sbd.payroll.psp.batchjobs.sendCustomEmail.models.ConfigFileModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@Slf4j
public class CustomEmailFileUtility {

    private ConfigFileModel[] configFileModelArray;
    private Set<String> workflowsToExecute;

    public CustomEmailFileUtility(ConfigFileModel[] configFileModelArray, Set<String> workflowsToExecute) {
        this.configFileModelArray = configFileModelArray;
        this.workflowsToExecute = workflowsToExecute;
    }

    public void deleteFilesAndFoldersFromDir(String dirPath) throws IOException {
        log.info("job=SendCustomEmailsProcessor, Action=DeleteProcessedFiles, Method=deleteLocalFilesFromDir, Status=Start, Path={}", dirPath);
        FileUtils.cleanDirectory(new File(dirPath));
        log.info("job=SendCustomEmailsProcessor, Action=DeleteProcessedFiles, Method=deleteLocalFilesFromDir, Status=Complete, Path={}", dirPath);
    }
}
