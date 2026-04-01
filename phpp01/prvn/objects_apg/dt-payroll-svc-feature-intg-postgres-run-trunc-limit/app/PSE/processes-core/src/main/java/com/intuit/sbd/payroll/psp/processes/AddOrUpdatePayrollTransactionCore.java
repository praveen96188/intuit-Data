package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTPayrollTransactionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTPayrollTransactionLineDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 11/11/11
 * Time: 11:11 AM
 */
public class AddOrUpdatePayrollTransactionCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private QBDTPayrollTransactionDTO mQBDTPayrollTransactionDTO;

    private Company mCompany;
    private Employee mEmployee;
    private CompanyAdjustmentSubmission mCompanyAdjustmentSubmission;
    private PriorPaymentSubmission mPriorPaymentSubmission;

    private UpdateQBDTTransactionInfoCore mUpdateQBDTTransactionInfoCore;

    public AddOrUpdatePayrollTransactionCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, QBDTPayrollTransactionDTO pQBDTPayrollTransactionDTO) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mQBDTPayrollTransactionDTO = pQBDTPayrollTransactionDTO;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode,
                                                                                                                       mSourceCompanyId);

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (mQBDTPayrollTransactionDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.PayrollTransaction, "QBDTPayrollTransactionDTO", "QBDTPayrollTransactionDTO");
            return validationResult;
        }

        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        if(mQBDTPayrollTransactionDTO.getRelatedAdjustmentSourceId() != null) {
            mCompanyAdjustmentSubmission = CompanyAdjustmentSubmission.findCompanyAdjustmentSubmission(mCompany, mQBDTPayrollTransactionDTO.getRelatedAdjustmentSourceId());
            if(mCompanyAdjustmentSubmission == null) {
                validationResult.getMessages().InvalidValue(EntityName.CompanyAdjustmentSubmission, mQBDTPayrollTransactionDTO.getRelatedAdjustmentSourceId(), "AdjustmentSourceId");
                return validationResult;
            }
        }

        if(mQBDTPayrollTransactionDTO.getRelatedPriorPaymentSourceId() != null) {
            mPriorPaymentSubmission = PriorPaymentSubmission.findPriorPaymentSubmissionByCompanyAndSourceId(mCompany, mQBDTPayrollTransactionDTO.getRelatedPriorPaymentSourceId());
            if(mPriorPaymentSubmission == null) {
                validationResult.getMessages().InvalidValue(EntityName.MoneyMovementTransaction, mQBDTPayrollTransactionDTO.getRelatedPriorPaymentSourceId(), "PriorPaymentSourceId");
                return validationResult;
            }
        }

        if(mQBDTPayrollTransactionDTO.getQBDTTransactionInfoDTO() != null && mQBDTPayrollTransactionDTO.getQBDTTransactionInfoDTO().getId() != null) {
            mUpdateQBDTTransactionInfoCore = new UpdateQBDTTransactionInfoCore(mQBDTPayrollTransactionDTO.getQBDTTransactionInfoDTO());
            validationResult = mUpdateQBDTTransactionInfoCore.validate();
            if (!validationResult.isSuccess()) {
                return validationResult;
            }
        }

        if(mQBDTPayrollTransactionDTO.getEmployeeSourceId() != null) {
            mEmployee = Employee.findEmployee(mCompany, mQBDTPayrollTransactionDTO.getEmployeeSourceId());
            if(mEmployee == null) {
                validationResult.getMessages().InvalidValue(EntityName.Employee, mQBDTPayrollTransactionDTO.getEmployeeSourceId(), "EmployeeSourceId");
                return validationResult;
            }
        }

        return validationResult;
    }

    @Override
    public ProcessResult<QbdtPayrollTransaction> process() {
        ProcessResult<QbdtPayrollTransaction> processResult = new ProcessResult<QbdtPayrollTransaction>();

        QbdtPayrollTransaction qbdtPayrollTransaction = QbdtPayrollTransaction.findQbdtPayrollTransactionBySourceId(mCompany, mQBDTPayrollTransactionDTO.getSourceId());
        if(qbdtPayrollTransaction == null) {
            qbdtPayrollTransaction = new QbdtPayrollTransaction();
            qbdtPayrollTransaction.setCompany(mCompany);

            if(mCompanyAdjustmentSubmission != null) {
                qbdtPayrollTransaction.setCompanyAdjustmentSubmission(mCompanyAdjustmentSubmission);
            }

            if(mPriorPaymentSubmission != null) {
                qbdtPayrollTransaction.setPriorPaymentSubmission(mPriorPaymentSubmission);
            }

            if(mEmployee != null) {
                qbdtPayrollTransaction.setEmployee(mEmployee);
            }

            if(mQBDTPayrollTransactionDTO.getSourceId() == null) {
                long nextId = Long.parseLong(mCompany.getNextPayrollTransactionId());
                qbdtPayrollTransaction.setSourceId(Long.toString(nextId));
            } else {
                qbdtPayrollTransaction.setSourceId(mQBDTPayrollTransactionDTO.getSourceId());
            }

            qbdtPayrollTransaction = Application.save(qbdtPayrollTransaction);

            if(mQBDTPayrollTransactionDTO.getQBDTTransactionInfoDTO() != null) {
                QbdtTransactionInfo qbdtTransactionInfo = new QbdtTransactionInfo();
                qbdtTransactionInfo.setCompany(mCompany);
                qbdtTransactionInfo.setQbdtPayrollTransaction(qbdtPayrollTransaction);
                qbdtTransactionInfo = Application.save(qbdtTransactionInfo);
                mQBDTPayrollTransactionDTO.getQBDTTransactionInfoDTO().setId(qbdtTransactionInfo.getId());
                mUpdateQBDTTransactionInfoCore = new UpdateQBDTTransactionInfoCore(mQBDTPayrollTransactionDTO.getQBDTTransactionInfoDTO());
                mUpdateQBDTTransactionInfoCore.validate();
            }
        }

        copyInfoFromDTO(mQBDTPayrollTransactionDTO, qbdtPayrollTransaction);
        if(mUpdateQBDTTransactionInfoCore != null) {
            processResult.merge(mUpdateQBDTTransactionInfoCore.process());
        }

        processResult.setResult(qbdtPayrollTransaction);

        return processResult;
    }

    private void copyInfoFromDTO(QBDTPayrollTransactionDTO pQBDTPayrollTransactionDTO, QbdtPayrollTransaction pQbdtPayrollTransaction) {
        pQbdtPayrollTransaction.setAmount(pQBDTPayrollTransactionDTO.getAmount());
        pQbdtPayrollTransaction.setIsVoided(pQBDTPayrollTransactionDTO.getIsVoided());
        pQbdtPayrollTransaction.setPeriodEndDate(pQBDTPayrollTransactionDTO.getPeriodEndDate());
        pQbdtPayrollTransaction.setTransactionDate(pQBDTPayrollTransactionDTO.getTransactionDate());
        pQbdtPayrollTransaction.setTransactionType(pQBDTPayrollTransactionDTO.getTransactionType());
        pQbdtPayrollTransaction.setEmployeeName(pQBDTPayrollTransactionDTO.getEmployeeName());

        // delete current lines
        for (Iterator<QbdtPayrollTransactionLine> iterator = pQbdtPayrollTransaction.getQbdtPayrollTransactionLineCollection().iterator(); iterator.hasNext();) {
            QbdtPayrollTransactionLine qbdtPayrollTransactionLine = iterator.next();
            QbdtTransactionInfo qbdtTransactionInfo = qbdtPayrollTransactionLine.getQbdtTransactionInfo();
            if(qbdtTransactionInfo != null) {
                qbdtPayrollTransactionLine.setQbdtTransactionInfo(null);
                Application.delete(qbdtTransactionInfo);
            }
            Application.delete(qbdtPayrollTransactionLine);
            iterator.remove();
        }

        // create new lines
        for (QBDTPayrollTransactionLineDTO qbdtPayrollTransactionLineDTO : pQBDTPayrollTransactionDTO.getQBDTPayrollTransactionLineDTOs()) {
            QbdtPayrollTransactionLine qbdtPayrollTransactionLine = new QbdtPayrollTransactionLine();
            qbdtPayrollTransactionLine.setAmount(qbdtPayrollTransactionLineDTO.getAmount());
            qbdtPayrollTransactionLine.setTaxableWageAmount(qbdtPayrollTransactionLineDTO.getTaxableWageAmount());
            qbdtPayrollTransactionLine.setWageBaseAmount(qbdtPayrollTransactionLineDTO.getWageBaseAmount());

            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(mCompany, qbdtPayrollTransactionLineDTO.getPayrollItemId());
            if(companyPayrollItem != null) {
                qbdtPayrollTransactionLine.setCompanyPayrollItem(companyPayrollItem);
            }

            if(qbdtPayrollTransactionLineDTO.getQBDTTransactionInfoDTO() != null) {
                QbdtTransactionInfo qbdtTransactionInfo = new QbdtTransactionInfo();
                qbdtTransactionInfo.setCompany(mCompany);
                qbdtTransactionInfo.setQbdtPayrollTransactionLine(qbdtPayrollTransactionLine);
                qbdtPayrollTransactionLineDTO.getQBDTTransactionInfoDTO().copyQBDTTransactionInfoFromDTO(qbdtTransactionInfo);
                qbdtTransactionInfo = Application.save(qbdtTransactionInfo);
                qbdtPayrollTransactionLine.setQbdtTransactionInfo(qbdtTransactionInfo);
            }

            qbdtPayrollTransactionLine.setQbdtPayrollTransaction(pQbdtPayrollTransaction);
            qbdtPayrollTransactionLine = Application.save(qbdtPayrollTransactionLine);
            pQbdtPayrollTransaction.addQbdtPayrollTransactionLine(qbdtPayrollTransactionLine);
        }
    }
}

