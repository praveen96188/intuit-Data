package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAdjustmentSubmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityAdjustmentDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyAdjustmentSubmission;
import com.intuit.sbd.payroll.psp.domain.LiabilityAdjustment;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 30, 2010
 * Time: 11:03:38 AM
 */
public class UpdateQBLiabilityAdjustmentInfo extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private CompanyAdjustmentSubmissionDTO mCompanyAdjustmentSubmissionDTO;

    private CompanyAdjustmentSubmission mCompanyAdjustmentSubmission;

    public UpdateQBLiabilityAdjustmentInfo(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, CompanyAdjustmentSubmissionDTO pCompanyAdjustmentSubmissionDTO) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mCompanyAdjustmentSubmissionDTO = pCompanyAdjustmentSubmissionDTO;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (mCompanyAdjustmentSubmissionDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.CompanyAdjustmentSubmission, "CompanyAdjustmentSubmission", "CompanyAdjustmentSubmissionDTO");
            return validationResult;
        }

        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        Company company = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                                                               mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        mCompanyAdjustmentSubmission = CompanyAdjustmentSubmission.findCompanyAdjustmentSubmission(company, mCompanyAdjustmentSubmissionDTO.getSourceId());
        if (mCompanyAdjustmentSubmission == null) {
            validationResult.getMessages().LiabilityAdjustmentNotExist(EntityName.CompanyAdjustmentSubmission, mCompanyAdjustmentSubmissionDTO.getSourceId(),
                                                                       mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        return validationResult;

    }

    @Override
    public ProcessResult<CompanyAdjustmentSubmission> process() {
        ProcessResult<CompanyAdjustmentSubmission> processResult = new ProcessResult<CompanyAdjustmentSubmission>();
        for (LiabilityAdjustmentDTO liabilityAdjustmentDTO : mCompanyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs()) {
            DomainEntitySet<LiabilityAdjustment> matchingAdjustments = mCompanyAdjustmentSubmission.getLiabilityAdjustmentCollection()
                    .find(LiabilityAdjustment.CompanyLaw().SourceId().equalTo(liabilityAdjustmentDTO.getPayrollItemId()));
            for (LiabilityAdjustment liabilityAdjustment : matchingAdjustments) {
                liabilityAdjustmentDTO.getQBDTTransactionInfoDTO().copyQBDTTransactionInfoFromDTO(liabilityAdjustment.getQbdtTransactionInfo());
                Application.save(liabilityAdjustment);
            }

            // update associated transactions
            for (CompanyAdjustmentSubmission associatedAdjustment : mCompanyAdjustmentSubmission.getAssociatedSubmissionCollection()) {
                DomainEntitySet<LiabilityAdjustment> matchingAssociatedAdjustments = associatedAdjustment.getLiabilityAdjustmentCollection()
                        .find(LiabilityAdjustment.CompanyLaw().SourceId().equalTo(liabilityAdjustmentDTO.getPayrollItemId()));
                for (LiabilityAdjustment liabilityAdjustment : matchingAssociatedAdjustments) {
                    liabilityAdjustmentDTO.getQBDTTransactionInfoDTO().copyQBDTTransactionInfoFromDTO(liabilityAdjustment.getQbdtTransactionInfo());
                    Application.save(liabilityAdjustment);
                }
            }
        }

        mCompanyAdjustmentSubmissionDTO.getQBDTTransactionInfoDTO().copyQBDTTransactionInfoFromDTO(mCompanyAdjustmentSubmission.getQbdtTransactionInfo());

        // todo this field is not the total of the adjustments as the name would lead you to assume
        // todo we should make it the sum and add a new field to QBDTTransaction info for the bogus amount coming from QB
        mCompanyAdjustmentSubmission.setAmount(mCompanyAdjustmentSubmissionDTO.getTotalAmount());

        processResult.setResult(Application.save(mCompanyAdjustmentSubmission));                

        return processResult;
    }
}
