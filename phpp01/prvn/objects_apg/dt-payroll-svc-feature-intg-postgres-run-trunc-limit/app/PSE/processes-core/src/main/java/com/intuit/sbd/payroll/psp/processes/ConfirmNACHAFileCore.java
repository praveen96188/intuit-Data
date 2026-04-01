package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * Created by IntelliJ IDEA.
 * User: dmartens
 * Date: July 25, 2008
 * Time: 2:35:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfirmNACHAFileCore extends Process implements IProcess {

    private String mNACHAFileId;
    private String mConfirmationCode;
    private NACHAFile mNACHAFile;

    public ConfirmNACHAFileCore(String pNACHAFileId, String pConfirmationCode) {
        mNACHAFileId = pNACHAFileId;
        mConfirmationCode = pConfirmationCode;
    }

    public NACHAFile getNACHAFile() {
        return mNACHAFile;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mNACHAFileId == null) {
            validationResult.getMessages().InvalidArgument(EntityName.NACHAFile, null, "NACHA File Id");
            return validationResult;
        }

        if (mConfirmationCode == null || mConfirmationCode.length()==0) {
            validationResult.getMessages().InvalidArgument(EntityName.NACHAFile, mConfirmationCode, "Confirmation Code");
            return validationResult;
        }

        mNACHAFile = PayrollServices.entityFinder.findById(NACHAFile.class, SpcfUniqueId.createInstance(mNACHAFileId));

        if (mNACHAFile==null) {
            validationResult.getMessages().NACHAFileDoesNotExist(EntityName.NACHAFile, mNACHAFileId);
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        mNACHAFile.setConfirmationCode(mConfirmationCode);
        mNACHAFile.setConfirmationDate(PSPDate.getPSPTime());

        mNACHAFile=Application.save(mNACHAFile);

        return processResult;
    }
}