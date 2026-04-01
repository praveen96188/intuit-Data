/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/UpdatePayrollCore.java#2 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

import java.util.Collection;

/**
 * Core process for updating an existing payroll.
 *
 * @author Dawn Martens
 */

public class UpdatePayrollCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private Collection<PaycheckDTO> mPaychecks;
    private Company mCompany;
    private PayrollRun mPayrollRun;
    private UpdatePayroll401k mUpdatePayroll401k;
    private String mTransmissionId;
    /**
     * The process parameters
     *
     * @param pSourceSystemCd - the source system
     * @param pSourceCompanyId - company id
     * @param pPayrollRun - the payroll run to update
     * @param pPaychecks - the paychecks to update
     */
    public UpdatePayrollCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                               PayrollRun pPayrollRun, Collection<PaycheckDTO> pPaychecks, String pTransmissionId) {
        this.mSourceSystemCd = pSourceSystemCd;
        this.mSourceCompanyId = pSourceCompanyId;
        this.mPayrollRun = pPayrollRun;
        this.mPaychecks = pPaychecks;
        mTransmissionId = pTransmissionId;
    }

    /**
     * process request
     * @return result - error messages and status if any
     */
    public ProcessResult<PayrollRun> process() {
        ProcessResult<PayrollRun> result = new ProcessResult<PayrollRun>();
        result.setResult(mPayrollRun);

        for (PaycheckDTO currentPaycheckDTO : mPaychecks) {
            String sourcePaycheckId = currentPaycheckDTO.getPaycheckId();
            Paycheck domainPaycheck = Paycheck.findPaycheckInStatus(mCompany, sourcePaycheckId, PaycheckStatusCode.Active, PaycheckStatusCode.Inactive);
            domainPaycheck.setGrossAmount(currentPaycheckDTO.getPaycheckGrossAmount());
            domainPaycheck.setYTDGrossAmount(currentPaycheckDTO.getPaycheckYTDGrossAmount());
            domainPaycheck.setYTDNetAmount(currentPaycheckDTO.getPaycheckYTDNetAmount());
            domainPaycheck.setPayPeriodBeginDate(DateDTO.convertToSpcfCalendar(currentPaycheckDTO.getPayPeriodBeginDate()));
            domainPaycheck.setPayPeriodEndDate(DateDTO.convertToSpcfCalendar(currentPaycheckDTO.getPayPeriodEndDate()));
            domainPaycheck.setNetAmount(currentPaycheckDTO.getPaycheckNetAmount());

            if (currentPaycheckDTO.getQBDTPaycheckInfoDTO() != null) {
                if (domainPaycheck.getQbdtPaycheckInfo() == null) {
                    QbdtPaycheckInfo qbdtPaycheckInfo = new QbdtPaycheckInfo();
                    qbdtPaycheckInfo.setCompany(mCompany);
                    qbdtPaycheckInfo.setPaycheck(domainPaycheck);
                    domainPaycheck.setQbdtPaycheckInfo(qbdtPaycheckInfo);
                    Application.save(qbdtPaycheckInfo);                    
                }

                if (currentPaycheckDTO.getQBDTPaycheckInfoDTO().getListId() != null) {
                    QbdtPaycheckInfo qbdtPaycheckInfo = domainPaycheck.getQbdtPaycheckInfo();
                    if (qbdtPaycheckInfo.getListId() != null) {
                        NaturalKey naturalKey = new NaturalKey(Paycheck.class, mCompany.getId(), qbdtPaycheckInfo.getListId());
                        Application.getSessionCache().removePrimaryKey(naturalKey);
                    }
                    qbdtPaycheckInfo.setListId(currentPaycheckDTO.getQBDTPaycheckInfoDTO().getListId());
                    NaturalKey naturalKey = new NaturalKey(Paycheck.class, mCompany.getId(), qbdtPaycheckInfo.getListId());
                    Application.getSessionCache().addPrimaryKey(naturalKey, domainPaycheck.getId());
                }
            }            

            Employee currentEmployee = Employee.findEmployee(mCompany, currentPaycheckDTO.getEmployeeId());
            domainPaycheck.setSourceEmployee(currentEmployee);

            if (domainPaycheck.getCompensationCollection()!=null && domainPaycheck.getCompensationCollection().size()>0) {
                for (Compensation currentDomainCompensation : domainPaycheck.getCompensationCollection()) {
                    if (currentDomainCompensation.getQbdtPaylineInfo() != null) {
                       Application.delete(currentDomainCompensation.getQbdtPaylineInfo());
                    }
                    Application.delete(currentDomainCompensation);
                }
            }

            if (domainPaycheck.getDeductionCollection()!=null && domainPaycheck.getDeductionCollection().size()>0) {
                for (Deduction currentDomainDeduction : domainPaycheck.getDeductionCollection()) {
                    if (currentDomainDeduction.getQbdtPaylineInfo() != null) {
                       Application.delete(currentDomainDeduction.getQbdtPaylineInfo());
                    }
                    Application.delete(currentDomainDeduction);
                }
            }

            if (domainPaycheck.getEmployerContributionCollection()!=null && domainPaycheck.getEmployerContributionCollection().size()>0) {
                for (EmployerContribution currentDomainContribution : domainPaycheck.getEmployerContributionCollection()) {
                    if (currentDomainContribution.getQbdtPaylineInfo() != null) {
                       Application.delete(currentDomainContribution.getQbdtPaylineInfo());
                    }
                    Application.delete(currentDomainContribution);
                }
            }

            if (domainPaycheck.getTaxCollection()!=null && domainPaycheck.getTaxCollection().size()>0) {
                for (Tax currentDomainTax : domainPaycheck.getTaxCollection()) {
                    Application.delete(currentDomainTax);
                }
            }

            Collection<CompensationTransactionDTO> compensationItems = currentPaycheckDTO.getCompensationTransactions();
            if (compensationItems!=null && compensationItems.size()>0) {            
                for (CompensationTransactionDTO currentCompensationDTO : currentPaycheckDTO.getCompensationTransactions()) {
                    PayrollSubmitHelper.createCompensation(currentCompensationDTO, mCompany, domainPaycheck);
                }
            }

            Collection<DeductionTransactionDTO> deductionItems = currentPaycheckDTO.getDeductionTransactions();
            if (deductionItems!=null && deductionItems.size()>0) {
                for (DeductionTransactionDTO currentDeductionDTO : currentPaycheckDTO.getDeductionTransactions()) {
                    PayrollSubmitHelper.createDeduction(currentDeductionDTO, mCompany, domainPaycheck);
                }
            }

            Collection<EmployerContributionTransactionDTO> employerContributionTransactionItems = currentPaycheckDTO.getEmployerContributionTransactions();
            if (employerContributionTransactionItems!=null && employerContributionTransactionItems.size()>0) {
                for (EmployerContributionTransactionDTO currentEmployerContributionDTo : currentPaycheckDTO.getEmployerContributionTransactions()) {
                    PayrollSubmitHelper.createEmployerContribution(currentEmployerContributionDTo, mCompany, domainPaycheck);
                }
            }

            Collection<LiabilityTransactionDTO> taxItems = currentPaycheckDTO.getLiabilityTransactions();
            if (taxItems!=null && taxItems.size()>0) {
                for (LiabilityTransactionDTO currentTaxDTO : currentPaycheckDTO.getLiabilityTransactions()) {
                    PayrollSubmitHelper.createTaxLiability(mCompany, currentTaxDTO, domainPaycheck);
                }
            }
        }

        if (mUpdatePayroll401k!=null) {
            result.merge(mUpdatePayroll401k.process());
        }

        return result;
    }

    /**
     * Validate process prarameters.
     *
     * @return ProcessResult - containing any validation errors
     */
    public ProcessResult validate() {
        ProcessResult validationResult = Validator.validCompanyParameters(mSourceSystemCd,
                mSourceCompanyId);

        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        if (mPayrollRun == null) {
            validationResult.getMessages().InvalidValue(EntityName.PayrollRun, "Payroll Run missing", null);
        } else if (!mPayrollRun.getCompany().equals(mCompany)) {
            validationResult.getMessages().InvalidValue(EntityName.PayrollRun, mPayrollRun.getId().toString(), "Payroll Run not associated with given company");
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        for (PaycheckDTO paycheck : mPaychecks) {
            validationResult.merge(paycheck.validatePaycheckDTO());

            Employee currentEmployee = Employee.findEmployee(mCompany, paycheck.getEmployeeId());
            if (currentEmployee==null) {
                validationResult.getMessages().EmployeeDoesNotExist(EntityName.Employee, paycheck.getEmployeeId(), mSourceSystemCd.toString(), mSourceCompanyId, paycheck.getEmployeeId());
            }

            Paycheck existingPaycheck = Paycheck.findPaycheckInStatus(mCompany, paycheck.getPaycheckId(), PaycheckStatusCode.Active, PaycheckStatusCode.Inactive);
            if (existingPaycheck == null) {
                validationResult.getMessages().PaycheckDoesNotExist(EntityName.PayCheck,
                        paycheck.getPaycheckId(), mSourceSystemCd.toString(), mSourceCompanyId, paycheck.getPaycheckId());
            }

            validationResult.merge(PayrollSubmitHelper.validatePaycheckDTOFor401k(mCompany, paycheck));
            validationResult.merge(PayrollSubmitHelper.validatePayrollItemsExistForLineItems(mCompany, paycheck));

            CompanyEvent.updateInvalidPaycheckInformationEvents(mPayrollRun.getCompany(), paycheck.getEmployeeId(), paycheck.getPaycheckId(), mTransmissionId, validationResult);
        }
        
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        boolean mustProcess401k = mCompany.isCompanyOnService(ServiceCode.ThirdParty401k) && CompanyService.wasCompanyOnServiceForDate(mCompany, ServiceCode.ThirdParty401k, mPayrollRun.getPaycheckDate());

        if (mustProcess401k) {
            if (!mCompany.isAllowedCapability(SystemCapabilityCode.SubmitPayroll, ServiceCode.ThirdParty401k)) {
                            validationResult.getMessages().CompanyOperationNotAllowed401k(EntityName.Company,
                                    mCompany.getSourceSystemCd().toString(),
                                    mCompany.getSourceCompanyId(), SystemCapabilityCode.SubmitPayroll.toString());
            }

            mUpdatePayroll401k = new UpdatePayroll401k(mPayrollRun, mPaychecks, mTransmissionId);
            validationResult.merge(mUpdatePayroll401k.validate());
        }

        if (!mustProcess401k) {
            if (!mCompany.isAllowedCapability(SystemCapabilityCode.SubmitPayroll)) {
                            validationResult.getMessages().CompanyOperationNotAllowed(
                                    mCompany.getSourceSystemCd().toString(),
                                    mCompany.getSourceCompanyId(), SystemCapabilityCode.SubmitPayroll.toString());
            }
        }

        return validationResult;
    }


}
