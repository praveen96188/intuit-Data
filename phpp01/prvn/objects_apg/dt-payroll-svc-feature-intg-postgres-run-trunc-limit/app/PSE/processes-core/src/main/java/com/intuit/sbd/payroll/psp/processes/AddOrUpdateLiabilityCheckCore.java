package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityCheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityCheckLineDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jan 13, 2011
 * Time: 3:17:35 PM
 */
public class AddOrUpdateLiabilityCheckCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private LiabilityCheckDTO mLiabilityCheckDTO;

    private Company mCompany;
    private PayrollRun mPayrollRun;
    private boolean mIsNew = false;
    
    private UpdateQBDTTransactionInfoCore mUpdateQBDTTransactionInfoCore;

    public AddOrUpdateLiabilityCheckCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, LiabilityCheckDTO pLiabilityCheckDTO) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mLiabilityCheckDTO = pLiabilityCheckDTO;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode,
                                                                                                                       mSourceCompanyId);

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (mLiabilityCheckDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.LiabilityCheck, "LiabilityCheckDTO", "LiabilityCheckDTO");
            return validationResult;
        }

        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        if(mLiabilityCheckDTO.getSourcePayrollRunId() != null) {
            mPayrollRun = PayrollRun.findPayrollRun(mCompany, mLiabilityCheckDTO.getSourcePayrollRunId());
            if(mPayrollRun == null) {
                validationResult.getMessages().InvalidValue(EntityName.PayrollRun, mLiabilityCheckDTO.getSourcePayrollRunId(), "SourcePayrollRunId");
                return validationResult;
            } else {
                // force auto update of token for liability checks associated with a payroll
                mLiabilityCheckDTO.getQBDTTransactionInfoDTO().setToken(0L);
            }
        }

        if(mLiabilityCheckDTO.getQBDTTransactionInfoDTO() != null && mLiabilityCheckDTO.getQBDTTransactionInfoDTO().getId() != null) {
            mUpdateQBDTTransactionInfoCore = new UpdateQBDTTransactionInfoCore(mLiabilityCheckDTO.getQBDTTransactionInfoDTO());
            validationResult = mUpdateQBDTTransactionInfoCore.validate();
            if (!validationResult.isSuccess()) {
                return validationResult;
            }
        }

        return validationResult;
    }

    @Override
    public ProcessResult<LiabilityCheck> process() {
        ProcessResult<LiabilityCheck> processResult = new ProcessResult<LiabilityCheck>();

        LiabilityCheck liabilityCheck = LiabilityCheck.findLiabilityCheckBySourceId(mCompany, mLiabilityCheckDTO.getSourceId());

        // hide old DIY DD liability checks for migration companies
        if(liabilityCheck != null && mLiabilityCheckDTO.isNewInBalanceFile()) {
            Application.getSessionCache().removePrimaryKey(liabilityCheck.getNaturalKey());
            liabilityCheck.setSourceId("-" + liabilityCheck.getSourceId());
            if(liabilityCheck.getQbdtTransactionInfo() != null) {
                liabilityCheck.getQbdtTransactionInfo().setToken(Company.EXCLUDE_TOKEN);
            }
            liabilityCheck = Application.save(liabilityCheck);
            Application.getSessionCache().addPrimaryKey(liabilityCheck.getNaturalKey(), liabilityCheck.getId());
            liabilityCheck = null;
        }

        if(liabilityCheck == null) {
            mIsNew = true;
            liabilityCheck = new LiabilityCheck();
            liabilityCheck.setCompany(mCompany);
            if(mPayrollRun != null) {
                liabilityCheck.setPayrollRun(mPayrollRun);
                mPayrollRun.addLiabilityCheck(liabilityCheck);
            }
            if(mLiabilityCheckDTO.getSourceId() == null) {
                long nextId = Long.parseLong(mCompany.getNextPayrollTransactionId());
                liabilityCheck.setSourceId(Long.toString(nextId));
            } else {
                liabilityCheck.setSourceId(mLiabilityCheckDTO.getSourceId());
            }
            liabilityCheck = Application.save(liabilityCheck);

            if(mLiabilityCheckDTO.getQBDTTransactionInfoDTO() != null) {
                QbdtTransactionInfo qbdtTransactionInfo = new QbdtTransactionInfo();
                qbdtTransactionInfo.setCompany(mCompany);
                qbdtTransactionInfo.setLiabilityCheck(liabilityCheck);
                liabilityCheck.setQbdtTransactionInfo(qbdtTransactionInfo);
                qbdtTransactionInfo = Application.save(qbdtTransactionInfo);
                mLiabilityCheckDTO.getQBDTTransactionInfoDTO().setId(qbdtTransactionInfo.getId());
                mUpdateQBDTTransactionInfoCore = new UpdateQBDTTransactionInfoCore(mLiabilityCheckDTO.getQBDTTransactionInfoDTO());
                mUpdateQBDTTransactionInfoCore.validate();
            }
        }

        copyInfoFromDTO(mLiabilityCheckDTO, liabilityCheck);
        copyAmountsAndDatesFromDTO(mLiabilityCheckDTO, liabilityCheck, !mLiabilityCheckDTO.isClientUpdate() || liabilityCheck.getType() == null);

        if(mUpdateQBDTTransactionInfoCore != null) {
            processResult.merge(mUpdateQBDTTransactionInfoCore.process());
        }

        processResult.setResult(liabilityCheck);
        if (processResult.isSuccess()) {
            Application.getSessionCache().addPrimaryKey(liabilityCheck.getNaturalKey(), liabilityCheck.getId());
        }

        return processResult;
    }

    private void copyInfoFromDTO(LiabilityCheckDTO pLiabilityCheckDTO, LiabilityCheck pLiabilityCheck) {
        pLiabilityCheck.setType(pLiabilityCheckDTO.getLiabilityCheckType());
        pLiabilityCheck.setIsVoid(pLiabilityCheckDTO.isVoid());
        if(pLiabilityCheckDTO.getSystemModifiedToken() != null) {
            pLiabilityCheck.setSystemModifiedToken(pLiabilityCheckDTO.getSystemModifiedToken());
        }

        for (LiabilityCheckBillingDetailAssoc liabilityCheckBillingDetailAssoc : pLiabilityCheck.getLiabilityCheckBillingDetailAssocCollection()) {
            BillingDetail foundBillingDetail = pLiabilityCheckDTO.getAssociatedBillingDetails().findEntity(BillingDetail.Id().equalTo(liabilityCheckBillingDetailAssoc.getBillingDetail().getId()));
            // remove associations from the dto that already exist, and delete ones that are not present in the dto
            if(foundBillingDetail != null) {
                pLiabilityCheckDTO.getAssociatedBillingDetails().remove(foundBillingDetail);
            } else {
                Application.delete(liabilityCheckBillingDetailAssoc);
            }
        }

        for (BillingDetail billingDetail : pLiabilityCheckDTO.getAssociatedBillingDetails()) {
            LiabilityCheckBillingDetailAssoc liabilityCheckBillingDetailAssoc = new LiabilityCheckBillingDetailAssoc();
            liabilityCheckBillingDetailAssoc.setBillingDetail(billingDetail);
            liabilityCheckBillingDetailAssoc.setLiabilityCheck(pLiabilityCheck);
            Application.save(liabilityCheckBillingDetailAssoc);
            pLiabilityCheck.addLiabilityCheckBillingDetailAssoc(liabilityCheckBillingDetailAssoc);
        }
    }

    private void copyAmountsAndDatesFromDTO(LiabilityCheckDTO pLiabilityCheckDTO, LiabilityCheck pLiabilityCheck, boolean pAllowDateAndAmountUpdates) {
        if(pAllowDateAndAmountUpdates) {
            pLiabilityCheck.setPeriodEndDate(pLiabilityCheckDTO.getPeriodEndDate());
            pLiabilityCheck.setTransactionDate(pLiabilityCheckDTO.getTransactionDate());
        }

        for (Iterator<LiabilityCheckLineDTO> liabilityCheckLineIterator = pLiabilityCheckDTO.getLiabilityCheckLineDTOs().iterator(); liabilityCheckLineIterator.hasNext();) {
            LiabilityCheckLineDTO liabilityCheckLineDTO = liabilityCheckLineIterator.next();
            Criterion<LiabilityCheckLine> where;
            // match all taxes
            if(liabilityCheckLineDTO.getCompanyPayrollItemId() != null) {
                where = LiabilityCheckLine.CompanyLaw().SourceId().equalTo(liabilityCheckLineDTO.getCompanyPayrollItemId())
                        .Or(LiabilityCheckLine.CompanyPayrollItem().SourcePayrollItemId().equalTo(liabilityCheckLineDTO.getCompanyPayrollItemId()));
            }
            // match all fees
            else if(!liabilityCheckLineDTO.getQBDTTransactionInfo().isDirectDeposit()) {
                where = LiabilityCheckLine.CompanyLaw().isNull()
                        .And(LiabilityCheckLine.CompanyPayrollItem().isNull())
                        .And(LiabilityCheckLine.QbdtTransactionInfo().Memo().equalTo(liabilityCheckLineDTO.getQBDTTransactionInfo().getMemo()));
            }
            // match dd
            else {
                where = LiabilityCheckLine.CompanyLaw().isNull()
                        .And(LiabilityCheckLine.CompanyPayrollItem().isNull())
                        .And(LiabilityCheckLine.QbdtTransactionInfo().IsDirectDeposit().equalTo(liabilityCheckLineDTO.getQBDTTransactionInfo().isDirectDeposit()));
            }

            // find all system generated lines
            DomainEntitySet<LiabilityCheckLine> liabilityCheckLines = pLiabilityCheck.getLiabilityCheckLineCollection().find(where);
            for (LiabilityCheckLine liabilityCheckLine : liabilityCheckLines) {
                QbdtTransactionInfo qbdtTransactionInfo = liabilityCheckLine.getQbdtTransactionInfo();
                if(qbdtTransactionInfo != null && liabilityCheckLineDTO.getQBDTTransactionInfo() != null) {
                    liabilityCheckLineDTO.getQBDTTransactionInfo().setSystemGenerated(qbdtTransactionInfo.getSystemGenerated());
                }
            }

            // remove zero lines from system updates
            if(!liabilityCheckLineDTO.isFeeLine() && liabilityCheckLineDTO.getQBDTTransactionInfo().isSystemGenerated() &&
                    (pAllowDateAndAmountUpdates && ((mIsNew && (liabilityCheckLineDTO.getAmount() == null || liabilityCheckLineDTO.getAmount().compareTo(SpcfMoney.ZERO) == 0)) ||
                    (liabilityCheckLines.size() == 0 && (liabilityCheckLineDTO.getAmount() == null || liabilityCheckLineDTO.getAmount().compareTo(SpcfMoney.ZERO) == 0))))) {
                // do not save zeros for new liability check lines
                liabilityCheckLineIterator.remove();
            }
            // remove system generated lines from client updates
            else if(!pAllowDateAndAmountUpdates && liabilityCheckLineDTO.getQBDTTransactionInfo() != null &&
                    liabilityCheckLineDTO.getQBDTTransactionInfo().isSystemGenerated()) {
                liabilityCheckLineIterator.remove();
            }
        }

        // delete all of the existing lines except system generated lines when processing client updates
        for (Iterator<LiabilityCheckLine> iterator = pLiabilityCheck.getLiabilityCheckLineCollection().iterator(); iterator.hasNext();) {
            LiabilityCheckLine liabilityCheckLine = iterator.next();
            QbdtTransactionInfo qbdtTransactionInfo = liabilityCheckLine.getQbdtTransactionInfo();
            if(pAllowDateAndAmountUpdates || (qbdtTransactionInfo != null && !qbdtTransactionInfo.getSystemGenerated())) {                
                Application.delete(liabilityCheckLine);
                if(qbdtTransactionInfo != null) {
                    Application.delete(qbdtTransactionInfo);
                }
                iterator.remove();
            }
        }

        // create new lines
        for (LiabilityCheckLineDTO liabilityCheckLineDTO : pLiabilityCheckDTO.getLiabilityCheckLineDTOs()) {
            LiabilityCheckLine liabilityCheckLine = new LiabilityCheckLine();
            liabilityCheckLine.setAmount(liabilityCheckLineDTO.getAmount());
            liabilityCheckLine.setLiabilityCheck(pLiabilityCheck);
            CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(mCompany, liabilityCheckLineDTO.getCompanyPayrollItemId());
            if(companyLaw != null) {
                liabilityCheckLine.setCompanyLaw(companyLaw);
            }
            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(mCompany, liabilityCheckLineDTO.getCompanyPayrollItemId());
            if(companyPayrollItem != null) {
                liabilityCheckLine.setCompanyPayrollItem(companyPayrollItem);
            }
            liabilityCheckLine = Application.save(liabilityCheckLine);
            pLiabilityCheck.getLiabilityCheckLineCollection().add(liabilityCheckLine);

            QbdtTransactionInfo qbdtTransactionInfo = new QbdtTransactionInfo();
            qbdtTransactionInfo.setCompany(mCompany);
            qbdtTransactionInfo.setLiabilityCheckLine(liabilityCheckLine);
            liabilityCheckLineDTO.getQBDTTransactionInfo().copyQBDTTransactionInfoFromDTO(qbdtTransactionInfo);
            qbdtTransactionInfo = Application.save(qbdtTransactionInfo);
            liabilityCheckLine.setQbdtTransactionInfo(qbdtTransactionInfo);
        }

        // if this is a client update recalc the total
        if(pLiabilityCheckDTO.isClientUpdate()) {
            SpcfDecimal totalAmount = SpcfDecimal.createInstance(0);
            for (LiabilityCheckLine liabilityCheckLine : pLiabilityCheck.getLiabilityCheckLineCollection()) {
                if(liabilityCheckLine.getAmount() != null) {
                    totalAmount = totalAmount.add(liabilityCheckLine.getAmount());
                }
            }
            // total should be opposite sign of sum
            pLiabilityCheck.setAmount(new SpcfMoney(totalAmount.negate()));
        } else {
            pLiabilityCheck.setAmount(pLiabilityCheckDTO.getAmount());
        }
    }


}
