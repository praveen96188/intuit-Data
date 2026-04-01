package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTRequestInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SourceSystemTransmissionDTO;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.QbdtRequestInfo;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.interceptor.manager.DomainEntityChangeManager;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: May 14, 2008
 * Time: 1:49:45 PM
 */
public class FinalizeSourceSystemTransmissionSecondaryCore extends Process implements IProcess {
    /**
     * Core process for finalizing a transmission to PSP
     */

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private SourceSystemTransmissionDTO mSourceSystemTransmissionDTO;
    private String mTransmissionId;
    private Company mCompany;
    private SourceSystemTransmission mSourceSystemTransmission;
    private SpcfCalendar mFinalizeDateTime;

    public FinalizeSourceSystemTransmissionSecondaryCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pTransmissionId, SourceSystemTransmissionDTO pSourceSystemTransmissionDTO) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mTransmissionId = pTransmissionId;
        mSourceSystemTransmissionDTO = pSourceSystemTransmissionDTO;
        mFinalizeDateTime = PSPDate.getPSPTime();
    }

    public FinalizeSourceSystemTransmissionSecondaryCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, String pTransmissionId, SourceSystemTransmissionDTO pSourceSystemTransmissionDTO, SpcfCalendar pFinalizeDateTime) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mTransmissionId = pTransmissionId;
        mSourceSystemTransmissionDTO = pSourceSystemTransmissionDTO;
        mFinalizeDateTime = pFinalizeDateTime;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        // SourceSystem and Company are optional parameters so only perform company validations
        // if at least one of the two is passed to the process
        if (mSourceSystemCd != null || mSourceCompanyId != null) {
            // Check if Company parameters are valid
            validationResult.merge(Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
            if (!validationResult.isSuccess()) {
                return validationResult;
            }

            // Check if Company exists

            mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

            if (mCompany == null) {
                validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, mSourceCompanyId,
                        mSourceSystemCd.toString(), mSourceCompanyId);
                return validationResult;
            }
        }
        // Check if Transmission ID is null
        if (mTransmissionId == null) {
            validationResult.getMessages()
                    .InvalidSourceSystemTransmission(EntityName.SourceSystemTransmission, "Source System Transmission");
            return validationResult;
        }
        SourceSystemTransmission sourceSystemTransmission = null;
        if (mCompany != null) {
            sourceSystemTransmission = SourceSystemTransmission.findSourceSystemTransmissionByCompanyAndTransmissionIdentifier(mCompany, mTransmissionId);
        } else {
            sourceSystemTransmission = SourceSystemTransmission.findSourceSystemTransmissionByIdentifier(mTransmissionId);
        }
        if (Objects.isNull(sourceSystemTransmission)) {
            validationResult.getMessages()
                    .SourceSystemTransmissionDoesNotExist(EntityName.SourceSystemTransmission, mTransmissionId, mSourceSystemCd.toString(), mSourceCompanyId, mTransmissionId);
            return validationResult;
        }
        mSourceSystemTransmission = sourceSystemTransmission;

        if ((mCompany != null) && (!mCompany.equals(mSourceSystemTransmission.getCompany()))) {
            validationResult.getMessages()
                    .SourceSystemTransmissionDoesNotBelongToCompany(EntityName.SourceSystemTransmission, mTransmissionId, mSourceSystemCd.toString(), mSourceCompanyId, mTransmissionId);
            return validationResult;
        }
        // Check if Transmission belongs to the Company


        return validationResult;
    }

    public ProcessResult<SourceSystemTransmission> process() {
        try {
            ProcessResult<SourceSystemTransmission> processResult = new ProcessResult<SourceSystemTransmission>();
            if (mSourceSystemTransmissionDTO.getResponseToken() != null) {
                mSourceSystemTransmission.setResponseToken(mSourceSystemTransmissionDTO.getResponseToken());
            }

            if (mSourceSystemTransmissionDTO.getDescription() != null) {
                mSourceSystemTransmission.setDescription(mSourceSystemTransmissionDTO.getDescription());
            }
            if (mSourceSystemTransmissionDTO.getFromSourceSystem() != null) {
                mSourceSystemTransmission.setFromSourceSystem(mSourceSystemTransmissionDTO.getFromSourceSystem());
            }
            if (mSourceSystemTransmissionDTO.getToSourceSystem() != null) {
                mSourceSystemTransmission.setToSourceSystem(mSourceSystemTransmissionDTO.getToSourceSystem());
            }
            if (mSourceSystemTransmissionDTO.getIPAddress() != null) {
                mSourceSystemTransmission.setIPAddress(mSourceSystemTransmissionDTO.getIPAddress());
            }

            mSourceSystemTransmission.setFinalizeDateTime(mFinalizeDateTime);
            mSourceSystemTransmission = ApplicationSecondary.save(mSourceSystemTransmission);

            DomainEntityChangeManager.setDomainEntityChangeModelContext(mSourceSystemTransmission.getClass(), mSourceSystemTransmission);

            if (mSourceSystemTransmissionDTO.getResponseDocument() != null && mSourceSystemTransmissionDTO.getResponseDocument().length() > 0) {
                mSourceSystemTransmission.setResponseDocument(mSourceSystemTransmissionDTO.getResponseDocument());
            }
            mSourceSystemTransmission = ApplicationSecondary.save(mSourceSystemTransmission);
            if (mSourceSystemTransmissionDTO.getQBDTRequestInfoDTO() != null &&
                    SystemParameter.findBooleanValue(SystemParameter.Code.RECORD_REQUEST_INFO, false)) {
                QBDTRequestInfoDTO qbdtRequestInfoDTO = mSourceSystemTransmissionDTO.getQBDTRequestInfoDTO();
                QbdtRequestInfo qbdtRequestInfo = new QbdtRequestInfo();
                qbdtRequestInfo.setEmployeeAddCount(qbdtRequestInfoDTO.getEmployeeAddCount());
                qbdtRequestInfo.setEmployeeAddStart(qbdtRequestInfoDTO.getEmployeeAddStart());
                qbdtRequestInfo.setEmployeeAddEnd(qbdtRequestInfoDTO.getEmployeeAddEnd());
                qbdtRequestInfo.setEmployeeUpdateCount(qbdtRequestInfoDTO.getEmployeeUpdateCount());
                qbdtRequestInfo.setEmployeeUpdateStart(qbdtRequestInfoDTO.getEmployeeUpdateStart());
                qbdtRequestInfo.setEmployeeUpdateEnd(qbdtRequestInfoDTO.getEmployeeUpdateEnd());

                qbdtRequestInfo.setPayrollItemAddCount(qbdtRequestInfoDTO.getPayrollItemAddCount());
                qbdtRequestInfo.setPayrollItemAddStart(qbdtRequestInfoDTO.getPayrollItemAddStart());
                qbdtRequestInfo.setPayrollItemAddEnd(qbdtRequestInfoDTO.getPayrollItemAddEnd());
                qbdtRequestInfo.setPayrollItemUpdateCount(qbdtRequestInfoDTO.getPayrollItemUpdateCount());
                qbdtRequestInfo.setPayrollItemUpdateStart(qbdtRequestInfoDTO.getPayrollItemUpdateStart());
                qbdtRequestInfo.setPayrollItemUpdateEnd(qbdtRequestInfoDTO.getPayrollItemUpdateEnd());

                qbdtRequestInfo.setPaycheckAddCount(qbdtRequestInfoDTO.getPaycheckAddCount());
                qbdtRequestInfo.setPaycheckUpdateCount(qbdtRequestInfoDTO.getPaycheckUpdateCount());
                qbdtRequestInfo.setPayrollProcessingStart(qbdtRequestInfoDTO.getPayrollProcessingStart());
                qbdtRequestInfo.setPayrollProcessingEnd(qbdtRequestInfoDTO.getPayrollProcessingEnd());

                qbdtRequestInfo.setPayrollTransactionAddCount(qbdtRequestInfoDTO.getPayrollTransactionAddCount());
                qbdtRequestInfo.setPayrollTransactionAddStart(qbdtRequestInfoDTO.getPayrollTransactionAddStart());
                qbdtRequestInfo.setPayrollTransactionAddEnd(qbdtRequestInfoDTO.getPayrollTransactionAddEnd());
                qbdtRequestInfo.setPayrollTransactionUpdateCount(qbdtRequestInfoDTO.getPayrollTransactionUpdateCount());
                qbdtRequestInfo.setPayrollTransactionUpdateStart(qbdtRequestInfoDTO.getPayrollTransactionUpdateStart());
                qbdtRequestInfo.setPayrollTransactionUpdateEnd(qbdtRequestInfoDTO.getPayrollTransactionUpdateEnd());

                qbdtRequestInfo.setEmployeeDeleteCount(qbdtRequestInfoDTO.getEmployeeDeleteCount());
                qbdtRequestInfo.setPayrollItemDeleteCount(qbdtRequestInfoDTO.getPayrollItemDeleteCount());
                qbdtRequestInfo.setPaycheckDeleteCount(qbdtRequestInfoDTO.getPaycheckDeleteCount());
                qbdtRequestInfo.setPayrollTransactionDeleteCount(qbdtRequestInfoDTO.getPayrollTransactionDeleteCount());
                qbdtRequestInfo.setDeleteProcessingStart(qbdtRequestInfoDTO.getDeleteProcessingStart());
                qbdtRequestInfo.setDeleteProcessingEnd(qbdtRequestInfoDTO.getDeleteProcessingEnd());

                qbdtRequestInfo.setSourceSystemTransmission(mSourceSystemTransmission);
                ApplicationSecondary.save(qbdtRequestInfo);
            }
            processResult.setResult(mSourceSystemTransmission);
            return processResult;
        } finally {
            DomainEntityChangeManager.removeDomainEntityChangeModel();
        }
    }
}