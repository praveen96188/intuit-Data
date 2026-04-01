package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.ACHEnrollmentFile;
import com.intuit.sbd.payroll.psp.domain.ACHEnrollmentFileStatus;
import com.intuit.sbd.payroll.psp.domain.ACHEnrollmentFileType;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import org.apache.commons.lang.StringUtils;

/**
 * User: ihannur
 * Date: 2/13/13
 * Time: 11:02 AM
 */
public class UploadACHResponseFileCore extends Process implements IProcess {

    String mFileName;
    String mFileContent;

    public UploadACHResponseFileCore(String pFileName, String pFileContent) {
        mFileName = pFileName;
        mFileContent = pFileContent;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (StringUtils.isEmpty(mFileName)) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.ACHEnrollment, mFileName, mFileName);
        }

        if (StringUtils.isEmpty(mFileContent)) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.ACHEnrollment, mFileContent, mFileContent);
        }

        return validationResult;
    }

    @Override
    public ProcessResult<ACHEnrollmentFile> process() {
        ProcessResult<ACHEnrollmentFile> result = new ProcessResult<ACHEnrollmentFile>();

        ACHEnrollmentFile enrollmentResponseFile = new ACHEnrollmentFile();
        enrollmentResponseFile.setType(ACHEnrollmentFileType.Response);
        enrollmentResponseFile.updateStatus(ACHEnrollmentFileStatus.UploadedByAgent);
        enrollmentResponseFile.setFileName(mFileName);

        Application.save(enrollmentResponseFile);

        enrollmentResponseFile.setFileContent(mFileContent);

        result.setResult(enrollmentResponseFile);

        return result;
    }
}
