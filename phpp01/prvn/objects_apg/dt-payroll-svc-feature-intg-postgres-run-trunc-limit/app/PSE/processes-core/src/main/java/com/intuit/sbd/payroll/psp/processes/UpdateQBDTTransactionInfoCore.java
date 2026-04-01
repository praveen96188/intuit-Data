package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTTransactionInfoDTO;
import com.intuit.sbd.payroll.psp.domain.QbdtTransactionInfo;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 27, 2010
 * Time: 10:55:06 AM
 */
public class UpdateQBDTTransactionInfoCore extends Process implements IProcess {
    private QBDTTransactionInfoDTO mQBDTTransactionInfoDTO;

    private QbdtTransactionInfo mQBDTTransactionInfo;

    public UpdateQBDTTransactionInfoCore(QBDTTransactionInfoDTO pQBDTTransactionInfoDTO) {
        mQBDTTransactionInfoDTO = pQBDTTransactionInfoDTO;
    }

    public ProcessResult<QbdtTransactionInfo> process() {
        ProcessResult<QbdtTransactionInfo> processResult = new ProcessResult<QbdtTransactionInfo>();

        mQBDTTransactionInfoDTO.copyQBDTTransactionInfoFromDTO(mQBDTTransactionInfo);
        Application.save(mQBDTTransactionInfo);
        processResult.setResult(mQBDTTransactionInfo);

        return processResult;
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mQBDTTransactionInfoDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.QBDTTransactionInfo, "QBDTTransactionInfo", "QBDTTransactionInfoDTO");
            return validationResult;
        }

        if(mQBDTTransactionInfoDTO.getId() == null) {
            validationResult.getMessages().InvalidValue(EntityName.QBDTTransactionInfo, "Id", "QBDTTransactionInfoDTO");
            return validationResult;
        }

        mQBDTTransactionInfo = Application.findById(QbdtTransactionInfo.class, mQBDTTransactionInfoDTO.getId());
        if(mQBDTTransactionInfo == null) {
            validationResult.getMessages().InvalidValue(EntityName.QBDTTransactionInfo, mQBDTTransactionInfoDTO.getId().toString(), "QBDTTransactionInfoDTO");
            return validationResult;
        }

        return validationResult;
    }
}
