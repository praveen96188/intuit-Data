package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAdjustmentSubmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityAdjustmentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTTransactionInfoDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyAdjustmentSubmission;
import com.intuit.sbd.payroll.psp.domain.LiabilityAdjustment;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Nov 24, 2010
 * Time: 11:03:38 AM
 */
public class VoidLiabilityAdjustments extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private Collection<String> mCompanyAdjustmentSubmissionIds;
    private boolean mRecall = false;

    private ArrayList<CompanyAdjustmentSubmission> mCompanyAdjustmentSubmissions = new ArrayList<CompanyAdjustmentSubmission>();
    private ArrayList<CompanyAdjustmentSubmission> mOffsetAdjustmentSubmissions = new ArrayList<CompanyAdjustmentSubmission>();

    public VoidLiabilityAdjustments(SourceSystemCode pSourceSystemCd, String pSourceCompanyId, Collection<String> pCompanyAdjustmentSubmissionIds, boolean pRecall) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mCompanyAdjustmentSubmissionIds = pCompanyAdjustmentSubmissionIds;
        mRecall = pRecall;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();


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

        for (String sourceId : mCompanyAdjustmentSubmissionIds) {
            CompanyAdjustmentSubmission companyAdjustmentSubmission = CompanyAdjustmentSubmission.findCompanyAdjustmentSubmission(company, sourceId);
            if (companyAdjustmentSubmission == null) {
                companyAdjustmentSubmission = PayrollServices.entityFinder.findById(CompanyAdjustmentSubmission.class, SpcfUniqueId.createInstance(sourceId));
            }

            if (companyAdjustmentSubmission == null) {
                validationResult.getMessages().LiabilityAdjustmentNotExist(EntityName.CompanyAdjustmentSubmission, sourceId,
                        mSourceSystemCd.toString(), mSourceCompanyId);

            } else {
                mCompanyAdjustmentSubmissions.add(companyAdjustmentSubmission);
                for (CompanyAdjustmentSubmission adjustmentSubmission : companyAdjustmentSubmission.getAssociatedSubmissionCollection()) {
                    mCompanyAdjustmentSubmissions.add(adjustmentSubmission);
                }
            }
        }
        return validationResult;

    }

    @Override
    public ProcessResult<Collection<CompanyAdjustmentSubmission>> process() {
        ProcessResult<Collection<CompanyAdjustmentSubmission>> processResult = new ProcessResult<Collection<CompanyAdjustmentSubmission>>();
        SpcfCalendar paycheckDate = PSPDate.getPSPTime();
        for (CompanyAdjustmentSubmission companyAdjustmentSubmission : mCompanyAdjustmentSubmissions) {
            CompanyAdjustmentSubmissionDTO offsetCompanyAdjustmentSubmissionDTO = createOffsetCompanyAdjustmentSubmission(companyAdjustmentSubmission);

            ArrayList<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
            for (LiabilityAdjustment liabilityAdjustment : companyAdjustmentSubmission.getLiabilityAdjustmentCollection()) {
                paycheckDate = liabilityAdjustment.getPayrollRun().getPaycheckDate();
                LiabilityAdjustmentDTO offSetAdjustmentDTO = createOffsetLiabilityAdjustment(liabilityAdjustment);
                liabilityAdjustmentDTOs.add(offSetAdjustmentDTO);
            }
            offsetCompanyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);
            LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
            liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
            liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
            liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);
            liabilityAdjustmentOptionsDTO.setRecall(mRecall);
            liabilityAdjustmentOptionsDTO.setVoid(!mRecall);

            ProcessResult<CompanyAdjustmentSubmission> adjustmentProcessResult = PayrollServices.payrollManager.addLiabilityAdjustments(mSourceSystemCd, mSourceCompanyId, null, offsetCompanyAdjustmentSubmissionDTO, new DateDTO(paycheckDate), liabilityAdjustmentOptionsDTO);
            processResult.merge(adjustmentProcessResult);
            if(!processResult.isSuccess()) {
                return processResult;
            }
            CompanyAdjustmentSubmission offsetCompanyAdjustmentSubmission = adjustmentProcessResult.getResult();

            // Update offset CompanyAdjustmentSubmission with original values
            offsetCompanyAdjustmentSubmission.setCompany(companyAdjustmentSubmission.getCompany());
            offsetCompanyAdjustmentSubmission.setQbdtTransactionInfo(companyAdjustmentSubmission.getQbdtTransactionInfo());
            offsetCompanyAdjustmentSubmission = Application.save(offsetCompanyAdjustmentSubmission);
            companyAdjustmentSubmission.setVoidSubmission(offsetCompanyAdjustmentSubmission);
            Application.save(companyAdjustmentSubmission);

            mOffsetAdjustmentSubmissions.add(offsetCompanyAdjustmentSubmission);
        }


        processResult.setResult(mOffsetAdjustmentSubmissions);

        return processResult;
    }

    private CompanyAdjustmentSubmissionDTO createOffsetCompanyAdjustmentSubmission(CompanyAdjustmentSubmission pCompanyAdjustmentSubmission) {
        CompanyAdjustmentSubmissionDTO offsetCompanyAdjustmentSubmissionDTO = new CompanyAdjustmentSubmissionDTO();
        if (pCompanyAdjustmentSubmission.getAmount() != null) {
            offsetCompanyAdjustmentSubmissionDTO.setTotalAmount(new SpcfMoney(pCompanyAdjustmentSubmission.getAmount().negate()));
        } else {
            offsetCompanyAdjustmentSubmissionDTO.setTotalAmount(null);
        }
        offsetCompanyAdjustmentSubmissionDTO.setOriginalSubmissionId(pCompanyAdjustmentSubmission.getId());
        offsetCompanyAdjustmentSubmissionDTO.setSubmissionDate(new DateDTO(PSPDate.getPSPTime()));
        return offsetCompanyAdjustmentSubmissionDTO;
    }

    private LiabilityAdjustmentDTO createOffsetLiabilityAdjustment(LiabilityAdjustment pLiabilityAdjustment) {
        LiabilityAdjustmentDTO offsetAdjustmentDTO = new LiabilityAdjustmentDTO();
        if (pLiabilityAdjustment.getQbdtTransactionInfo() != null) {
            QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
            qbdtTransactionInfoDTO.createDTOFromQBDTTransactionInfo(pLiabilityAdjustment.getQbdtTransactionInfo());
            offsetAdjustmentDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);
        }
        offsetAdjustmentDTO.setLawId(pLiabilityAdjustment.getLaw().getLawId());
        offsetAdjustmentDTO.setEffectiveDate(new DateDTO(PSPDate.getPSPTime()));
        offsetAdjustmentDTO.setSourceEmployeeId(pLiabilityAdjustment.getEmployee() != null ? pLiabilityAdjustment.getEmployee().getSourceEmployeeId() : null);
        offsetAdjustmentDTO.setLawId(pLiabilityAdjustment.getLaw().getLawId());
        offsetAdjustmentDTO.setReconcilingAdjustment(false);

        // negate amount and wages
        if (pLiabilityAdjustment.getAmount() != null) {
            offsetAdjustmentDTO.setAmount(new SpcfMoney(pLiabilityAdjustment.getAmount().negate()));
        } else {
            offsetAdjustmentDTO.setAmount(null);
        }
        if (pLiabilityAdjustment.getTaxableWages() != null) {
            offsetAdjustmentDTO.setTaxableWages(new SpcfMoney(pLiabilityAdjustment.getTaxableWages().negate()));
        } else {
            offsetAdjustmentDTO.setTaxableWages(null);
        }
        if (pLiabilityAdjustment.getTotalWages() != null) {
            offsetAdjustmentDTO.setTotalWages(new SpcfMoney(pLiabilityAdjustment.getTotalWages().negate()));
        } else {
            offsetAdjustmentDTO.setTotalWages(null);
        }
        return offsetAdjustmentDTO;
    }
}
