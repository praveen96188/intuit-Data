/*
 * $Id: //psp/dev/PSE/Processes-Core/src/com/intuit/sbd/payroll/psp/processes/UpdateQBPayrollInfoCore.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.ObjectUtils;

/**
 * Core process for updating Quickbooks PayrollRun Information
 *
 * @author Marcela Villani
 */
public class UpdateQBPayrollInfoCore extends Process implements IProcess {
    private static SpcfLogger logger = Application.getLogger(UpdateQBPayrollInfoCore.class);

    private Company domainCompany;
    private PayrollRun domainPayrollRun;
    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private PayrollRunDTO payrollRunDTO;
    private boolean paycheckInfoOnly;

    public UpdateQBPayrollInfoCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                   PayrollRunDTO pPayrollRunDTO, boolean pPaycheckOnly) {
        sourceSystemCd = pSourceSystemCd;
        sourceCompanyId = pSourceCompanyId;
        payrollRunDTO = pPayrollRunDTO;
        paycheckInfoOnly = pPaycheckOnly;
    }

    public ProcessResult<PayrollRun> process() {
        ProcessResult<PayrollRun> processResult = new ProcessResult<PayrollRun>();

        String sessionid=payrollRunDTO.getSessionId();
        // Get the paycheck dto collection
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {

            Paycheck paycheck = Paycheck.findPaycheck(domainCompany, paycheckDTO.getPaycheckId());

            paycheck.setSessionId(sessionid);
            // the source id can be changed if the list id of a "new" paycheck matches that of a paycheck that has already been saved and the check is DIY
            if (paycheck.getOriginalSourceId() != null && !paycheck.getOriginalSourceId().equals(paycheckDTO.getPaycheckId()) &&
                    paycheck.getQbdtPaycheckInfo() != null && paycheck.getQbdtPaycheckInfo().getIsAssisted()) {
                processResult.getMessages().DuplicatePaycheckUniqueIdMatchesPreviousPaycheck(EntityName.Paycheck, paycheckDTO.getPaycheckId(), paycheckDTO.getPaycheckId(), sourceSystemCd.toString(), sourceCompanyId);
                return processResult;
            } else {
                paycheck.setSourcePaycheckId(paycheckDTO.getPaycheckId());
            }

            QbdtPaycheckInfo qbdtPaycheckInfo = paycheck.getQbdtPaycheckInfo();
            QBDTPaycheckInfoDTO qbdtPaycheckInfoDTO = paycheckDTO.getQBDTPaycheckInfoDTO();

            if (qbdtPaycheckInfoDTO != null && qbdtPaycheckInfo == null) {
                qbdtPaycheckInfo = new QbdtPaycheckInfo();
                qbdtPaycheckInfo.setCompany(domainCompany);
                qbdtPaycheckInfo.setPaycheck(paycheck);
                paycheck.setQbdtPaycheckInfo(qbdtPaycheckInfo);
                Application.save(qbdtPaycheckInfo);
            }
            // QBDTPaycheckInfo
            if (qbdtPaycheckInfo != null) {
                updateQBDTPaycheckInfoFromDTO(qbdtPaycheckInfoDTO, qbdtPaycheckInfo, paycheck);
            }

            if (employeeChanged(paycheckDTO, paycheck)) {
                Employee newEmployee = Employee.findEmployee(domainCompany, paycheckDTO.getEmployeeId());
                paycheck.setSourceEmployee(newEmployee);
                if (paycheck.getDDEmployee() != null) {
                    paycheck.setDDEmployee(newEmployee);
                }
            }

            if (paycheckInfoOnly) {
                continue;
            }

            // these loops are looking for an item that closely matches the line item. The QBDTInfo is completely overwritten so it
            // does not matter if multiple items match the search criteria, each one will be updated with new information
            String errorMessage = "Company " + domainCompany.getSourceSystemCd() + ":" + domainCompany.getSourceCompanyId() + " Mod for paycheck '" + paycheck.getSourcePaycheckId() + "' received with amounts or items that do not match saved paycheck. See item: ";
            boolean isProdFixEnable = FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_GUIDELINE_PROD_FIX);
            DomainEntitySet<Compensation> compensationsCopy;
            if(isProdFixEnable) {
                compensationsCopy = new DomainEntitySet<Compensation>();
                compensationsCopy.addAll(paycheck.getCompensationCollection());
            }
            else {
                compensationsCopy = new DomainEntitySet<Compensation>(paycheck.getCompensationCollection());
            }
            for (CompensationTransactionDTO compensationTransactionDTO : paycheckDTO.getCompensationTransactions()) {

                Criterion<Compensation> query = Compensation.CompanyPayrollItem().SourcePayrollItemId().equalTo(compensationTransactionDTO.getSourcePayrollItemId())
                                                            .And(Compensation.CompensationAmount().equalTo(compensationTransactionDTO.getCompensationAmount()));
                if (compensationTransactionDTO.getHoursWorked() != null) {
                    query = query.And(Compensation.HoursWorked().equalTo(Double.parseDouble(compensationTransactionDTO.getHoursWorked().toString())));
                } else {
                    query = query.And(Compensation.HoursWorked().equalTo(0.0));
                }

                DomainEntitySet<Compensation> compensations = compensationsCopy.find(query);
                if (compensations.size() > 0) {
                    Compensation compensation = compensations.get(0);

                    // remove it from the collection so we don't find it a second time
                    compensationsCopy.remove(compensation);

                    if (compensationTransactionDTO.getQBDTPaylineInfoDTO() != null) {
                        compensationTransactionDTO.getQBDTPaylineInfoDTO().copyToDomain(compensation.getQbdtPaylineInfo());
                    }

                    Application.save(compensation);
                } else {
                    logger.info(errorMessage + compensationTransactionDTO.getSourcePayrollItemId());
                }
            }
            DomainEntitySet<Deduction> deductionsCopy;
            if(isProdFixEnable) {
                deductionsCopy = new DomainEntitySet<Deduction>();
                deductionsCopy.addAll(paycheck.getDeductionCollection());
            }else{
                deductionsCopy = new DomainEntitySet<Deduction>(paycheck.getDeductionCollection());
            }
            for (DeductionTransactionDTO deductionTransactionDTO : paycheckDTO.getDeductionTransactions()) {
                DomainEntitySet<Deduction> deductions = deductionsCopy.find(
                        Deduction.CompanyPayrollItem().SourcePayrollItemId().equalTo(deductionTransactionDTO.getSourcePayrollItemId())
                                 .And(Deduction.DeductionAmount().equalTo(SpcfUtils.convertToSpcfMoney(deductionTransactionDTO.getDeductionAmount()))));
                if (deductions.size() > 0) {
                    Deduction deduction = deductions.get(0);

                    // remove it from the collection so we don't find it a second time
                    deductionsCopy.remove(deduction);

                    if (deductionTransactionDTO.getQBDTPaylineInfoDTO() != null) {
                        deductionTransactionDTO.getQBDTPaylineInfoDTO().copyToDomain(deduction.getQbdtPaylineInfo());
                    }

                    Application.save(deduction);
                } else {
                    logger.info(errorMessage + deductionTransactionDTO.getSourcePayrollItemId());
                }
            }
            DomainEntitySet<EmployerContribution> employerContributionsCopy;
            if(isProdFixEnable) {
                employerContributionsCopy = new DomainEntitySet<EmployerContribution>();
                employerContributionsCopy.addAll(paycheck.getEmployerContributionCollection());
            }else{
                employerContributionsCopy = new DomainEntitySet<EmployerContribution>(paycheck.getEmployerContributionCollection());
            }
            for (EmployerContributionTransactionDTO employerContributionTransactionDTO : paycheckDTO.getEmployerContributionTransactions()) {
                DomainEntitySet<EmployerContribution> employerContributions = employerContributionsCopy.find(
                        EmployerContribution.CompanyPayrollItem().SourcePayrollItemId().equalTo(employerContributionTransactionDTO.getSourcePayrollItemId())
                                            .And(EmployerContribution.ContributionAmount().equalTo(SpcfUtils.convertToSpcfMoney(employerContributionTransactionDTO.getContributionAmount()))));
                if (employerContributions.size() > 0) {
                    EmployerContribution employerContribution = employerContributions.get(0);

                    // remove it from the collection so we don't find it a second time
                    employerContributionsCopy.remove(employerContribution);

                    if (employerContributionTransactionDTO.getQBDTPaylineInfoDTO() != null) {
                        employerContributionTransactionDTO.getQBDTPaylineInfoDTO().copyToDomain(employerContribution.getQbdtPaylineInfo());
                    }

                    Application.save(employerContribution);
                } else {
                    logger.info(errorMessage + employerContributionTransactionDTO.getSourcePayrollItemId());
                }
            }

            Application.save(paycheck);
        }

        domainPayrollRun = Application.save(domainPayrollRun);

        processResult.setResult(domainPayrollRun);
        return processResult;
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (payrollRunDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.PayrollRun, "PayrollRun", "PayrollRunDTO");
            return validationResult;
        }

        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate company exists
        domainCompany = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (domainCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                                                               sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        // Validate PayrollRun Exists

        domainPayrollRun = PayrollRun.findPayrollRun(domainCompany, payrollRunDTO.getPayrollTXBatchId());
        if (domainPayrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(EntityName.PayrollRun, payrollRunDTO.getPayrollTXBatchId(),
                                                                  payrollRunDTO.getPayrollTXBatchId(), sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            Paycheck paycheck = Paycheck.findPaycheck(domainCompany, paycheckDTO.getPaycheckId());
            if (paycheck == null) {
                // validate paycheck exists
                validationResult.getMessages().PaycheckDoesNotExist(EntityName.PayCheck, paycheckDTO.getPaycheckId(),
                                                                    sourceSystemCd.toString(), sourceCompanyId, paycheckDTO.getPaycheckId());
            } else if (employeeChanged(paycheckDTO, paycheck)) {
                // validate if the employee id is different the employees match before updating the relationship
                Employee oldEmployee = paycheck.getSourceEmployee();
                Employee newEmployee = Employee.findEmployee(domainCompany, paycheckDTO.getEmployeeId());
                if (newEmployee == null || (domainCompany.isCompanyOnService(ServiceCode.Tax) && !ObjectUtils.equals(oldEmployee.getTaxId(), newEmployee.getTaxId()))) {
                    validationResult.getMessages().GenericError(EntityName.PayCheck, paycheckDTO.getEmployeeId(),
                                                                "Employee on paycheck " + paycheckDTO.getPaycheckId() + " does not match employee already saved for that paycheck. " +
                                                                        "New EE Id: " + paycheckDTO.getEmployeeId() + " Old EE Id: " + oldEmployee.getSourceEmployeeId());
                }
            }
        }

        return validationResult;
    }

    private boolean employeeChanged(PaycheckDTO paycheckDTO, Paycheck pPaycheck) {
        return paycheckDTO.getEmployeeId() != null && pPaycheck.getSourceEmployee() != null &&
                !pPaycheck.getSourceEmployee().getSourceEmployeeId().equals(paycheckDTO.getEmployeeId());
    }

    private void updateQBDTPaycheckInfoFromDTO(QBDTPaycheckInfoDTO pQBDTPaycheckInfoDTO, QbdtPaycheckInfo pQbdtPaycheckInfo, Paycheck pPaycheck) {
        if (pQBDTPaycheckInfoDTO.getListId() != null) {
            if (pQbdtPaycheckInfo.getListId() != null) {
                NaturalKey naturalKey = new NaturalKey(Paycheck.class, domainCompany.getId(), pQbdtPaycheckInfo.getListId());
                Application.getSessionCache().removePrimaryKey(naturalKey);
            }
            pQbdtPaycheckInfo.setListId(pQBDTPaycheckInfoDTO.getListId());
            NaturalKey naturalKey = new NaturalKey(Paycheck.class, domainCompany.getId(), pQbdtPaycheckInfo.getListId());
            Application.getSessionCache().addPrimaryKey(naturalKey, pPaycheck.getId());
        }

        pQbdtPaycheckInfo.setAccountName(pQBDTPaycheckInfoDTO.getAccountName());
        pQbdtPaycheckInfo.setCheckNumber(pQBDTPaycheckInfoDTO.getCheckNumber());
        pQbdtPaycheckInfo.setCleared(pQBDTPaycheckInfoDTO.getCleared());
        pQbdtPaycheckInfo.setMemo(pQBDTPaycheckInfoDTO.getMemo());
        pQbdtPaycheckInfo.setProrate(pQBDTPaycheckInfoDTO.isProrate());
        pQbdtPaycheckInfo.setTrackingClass(pQBDTPaycheckInfoDTO.getTrackingClass());
        pQbdtPaycheckInfo.setSickHoursAccrued(pQBDTPaycheckInfoDTO.getSickHoursAccrued());
        pQbdtPaycheckInfo.setVacationHoursAccrued(pQBDTPaycheckInfoDTO.getVacationHoursAccrued());
        if (pQBDTPaycheckInfoDTO.isOnService() != null) {
            pQbdtPaycheckInfo.setOnService(pQBDTPaycheckInfoDTO.isOnService());
        }
        Application.save(pQbdtPaycheckInfo);

    }

}