package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.UpdateDataSyncTokensDTO;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

import java.util.Arrays;
import java.util.Collection;

/**
 * User: dweinberg
 * Date: 11/1/11
 * Time: 10:14 AM
 */
public class UpdateDataSyncTokens extends Process {

    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private UpdateDataSyncTokensDTO dto;

    private Company company;
    private DomainEntitySet<Employee> employees;
    private DomainEntitySet<QbdtPayrollItemInfo> payrollItems;
    private DomainEntitySet<Paycheck> paychecks;
    private DomainEntitySet<PriorPaymentSubmission> priorPayments;
    private DomainEntitySet<CompanyAdjustmentSubmission> liabilityAdjustments;
    private DomainEntitySet<LiabilityCheck> liabilityChecks;
    private DomainEntitySet<QbdtPayrollTransaction> qbdtPayrollTransactions;

    public UpdateDataSyncTokens(SourceSystemCode sourceSystemCd, String sourceCompanyId, UpdateDataSyncTokensDTO dto) {
        this.sourceSystemCd = sourceSystemCd;
        this.sourceCompanyId = sourceCompanyId;
        this.dto = dto;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (dto == null) {
            validationResult.getMessages().InvalidArgument(EntityName.DTO, null, "Update Data Sync Tokens DTO");
            return validationResult;
        }

        validationResult.merge(dto.validate());
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        validationResult.merge(Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        company = Company.findCompany(sourceCompanyId, sourceSystemCd);

        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        if (dto.getEmployees().isEmpty() &&
                dto.getPayrollItems().isEmpty() &&
                dto.getLiabilityAdjustments().isEmpty() &&
                dto.getLiabilityChecks().isEmpty() &&
                dto.getPriorPaymentsAndRefunds().isEmpty() &&
                dto.getQbdtOnlyPayrollTransactions().isEmpty() &&
                dto.getPaychecks().isEmpty()) {
            validationResult.getMessages().GenericError(EntityName.DTO, sourceCompanyId, "At least one item must be selected.");
        }

        if (dto.getEmployees().isEmpty()) {
            employees = new DomainEntitySet<Employee>();
        } else {
            employees = Application.find(Employee.class, Employee.Company().equalTo(company).And(Employee.Id().in(dto.getEmployees())));
            validateAllElementsFound(validationResult, employees, dto.getEmployees().size());
        }

        if (dto.getPayrollItems().isEmpty()) {
            payrollItems = new DomainEntitySet<QbdtPayrollItemInfo>();
        } else {
            payrollItems = Application.find(QbdtPayrollItemInfo.class, QbdtPayrollItemInfo.Company().equalTo(company).And(QbdtPayrollItemInfo.Id().in(dto.getPayrollItems())));
            validateAllElementsFound(validationResult, payrollItems, dto.getPayrollItems().size());
        }

        if (dto.getPaychecks().isEmpty()) {
            paychecks = new DomainEntitySet<Paycheck>();
        } else {
            paychecks = Application.find(Paycheck.class, Paycheck.Company().equalTo(company).And(Paycheck.Id().in(dto.getPaychecks())));
            validateAllElementsFound(validationResult, paychecks, dto.getPaychecks().size());
        }

        if (dto.getPriorPaymentsAndRefunds().isEmpty()) {
            priorPayments = new DomainEntitySet<PriorPaymentSubmission>();
        } else {
            priorPayments = Application.find(PriorPaymentSubmission.class, PriorPaymentSubmission.Company().equalTo(company).And(PriorPaymentSubmission.Id().in(dto.getPriorPaymentsAndRefunds())));
            validateAllElementsFound(validationResult, priorPayments, dto.getPriorPaymentsAndRefunds().size());
        }

        if (dto.getLiabilityAdjustments().isEmpty()) {
            liabilityAdjustments = new DomainEntitySet<CompanyAdjustmentSubmission>();
        } else {
            liabilityAdjustments = Application.find(CompanyAdjustmentSubmission.class, CompanyAdjustmentSubmission.Company().equalTo(company).And(CompanyAdjustmentSubmission.Id().in(dto.getLiabilityAdjustments())));
            validateAllElementsFound(validationResult, liabilityAdjustments, dto.getLiabilityAdjustments().size());
        }

        if (dto.getLiabilityChecks().isEmpty()) {
            liabilityChecks = new DomainEntitySet<LiabilityCheck>();
        } else {
            liabilityChecks = Application.find(LiabilityCheck.class, LiabilityCheck.Company().equalTo(company).And(LiabilityCheck.Id().in(dto.getLiabilityChecks())));
            validateAllElementsFound(validationResult, liabilityChecks, dto.getLiabilityChecks().size());
        }

        if (dto.getQbdtOnlyPayrollTransactions().isEmpty()) {
            qbdtPayrollTransactions = new DomainEntitySet<QbdtPayrollTransaction>();
        } else {
            qbdtPayrollTransactions = Application.find(QbdtPayrollTransaction.class, QbdtPayrollTransaction.Company().equalTo(company).And(QbdtPayrollTransaction.Id().in(dto.getQbdtOnlyPayrollTransactions())));
            validateAllElementsFound(validationResult, qbdtPayrollTransactions, dto.getQbdtOnlyPayrollTransactions().size());
        }

        return validationResult;
    }

    private void validateAllElementsFound(ProcessResult validationResult, Collection returnedItems, int expectedSize) {
        if (returnedItems.size() != expectedSize) {
            validationResult.getMessages().InvalidValue(EntityName.DTO, Arrays.toString(returnedItems.toArray()), "IDs");
        }
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        long toToken;
        if (dto.getAction() == UpdateDataSyncTokensDTO.Action.Push) {
            toToken = company.getNextToken();
        } else if (dto.getAction() == UpdateDataSyncTokensDTO.Action.Stop) {
            toToken = -1;
        } else {
            throw new RuntimeException();
        }

        StringBuilder noteBuilder = new StringBuilder();
        noteBuilder.append("Comment: ").append(dto.getComment());
        noteBuilder.append("\nAction: ").append(dto.getAction().toString());

        if (paychecks.size() > 0) {
            noteBuilder.append("\nPaychecks: ");
            for (Paycheck paycheck : paychecks) {
                noteBuilder.append(paycheck.getSourcePaycheckId()).append(" ");
                paycheck.getQbdtPaycheckInfo().setToken(toToken);
                Application.save(paycheck);
            }
        }

        if (priorPayments.size() > 0 || liabilityAdjustments.size() > 0 || liabilityChecks.size() > 0 || qbdtPayrollTransactions.size() > 0) {
            noteBuilder.append("\nPayroll Transactions: ");

            for (PriorPaymentSubmission priorPayment : priorPayments) {
                noteBuilder.append(priorPayment.getSourceId()).append(" ");
                for (QbdtTransactionInfo qbdtTransactionInfo : priorPayment.getQbdtTransactionInfoCollection()) {
                    qbdtTransactionInfo.setToken(toToken);
                    if(dto.getUndelete()) {
                        qbdtTransactionInfo.setIsDeleted(false);
                    }
                    Application.save(qbdtTransactionInfo);
                }
                if (priorPayment.getQbdtPayrollTransaction() != null) {
                    priorPayment.getQbdtPayrollTransaction().getQbdtTransactionInfo().setToken(toToken);
                    if(dto.getUndelete()) {
                        priorPayment.getQbdtPayrollTransaction().getQbdtTransactionInfo().setIsDeleted(false);
                    }
                    Application.save(priorPayment.getQbdtPayrollTransaction().getQbdtTransactionInfo());
                }
            }

            for (CompanyAdjustmentSubmission liabilityAdjustment : liabilityAdjustments) {
                noteBuilder.append(liabilityAdjustment.getSourceId()).append(" ");
                liabilityAdjustment.getQbdtTransactionInfo().setToken(toToken);
                if(dto.getUndelete()) {
                    liabilityAdjustment.getQbdtTransactionInfo().setIsDeleted(false);
                }
                if(liabilityAdjustment.getQbdtPayrollTransaction() != null) {
                    liabilityAdjustment.getQbdtPayrollTransaction().getQbdtTransactionInfo().setToken(toToken);
                    if(dto.getUndelete()) {
                        liabilityAdjustment.getQbdtPayrollTransaction().getQbdtTransactionInfo().setIsDeleted(false);
                    }
                }
                Application.save(liabilityAdjustment);
            }

            for (LiabilityCheck liabilityCheck : liabilityChecks) {
                noteBuilder.append(liabilityCheck.getSourceId()).append(" ");
                liabilityCheck.getQbdtTransactionInfo().setToken(toToken);
                if(dto.getUndelete()) {
                    liabilityCheck.getQbdtTransactionInfo().setIsDeleted(false);
                }
                Application.save(liabilityCheck);
            }

            for (QbdtPayrollTransaction qbdtPayrollTransaction : qbdtPayrollTransactions) {
                noteBuilder.append(qbdtPayrollTransaction.getSourceId()).append(" ");
                qbdtPayrollTransaction.getQbdtTransactionInfo().setToken(toToken);
                if(dto.getUndelete()) {
                    qbdtPayrollTransaction.getQbdtTransactionInfo().setIsDeleted(false);
                }

                if(qbdtPayrollTransaction.getPriorPaymentSubmission() != null){
                    for (QbdtTransactionInfo qbdtTransactionInfo : qbdtPayrollTransaction.getPriorPaymentSubmission().getQbdtTransactionInfoCollection()) {
                        qbdtTransactionInfo.setToken(toToken);
                        if(dto.getUndelete()) {
                            qbdtTransactionInfo.setIsDeleted(false);
                        }
                    }
                }
                if(qbdtPayrollTransaction.getCompanyAdjustmentSubmission() != null){
                    qbdtPayrollTransaction.getCompanyAdjustmentSubmission().getQbdtTransactionInfo().setToken(toToken);
                    if(dto.getUndelete()) {
                        qbdtPayrollTransaction.getCompanyAdjustmentSubmission().getQbdtTransactionInfo().setIsDeleted(false);
                    }
                }
                Application.save(qbdtPayrollTransaction);
            }
        }

        if (employees.size() > 0) {
            noteBuilder.append("\nEmployees: ");
            for (Employee employee : employees) {
                noteBuilder.append(employee.getSourceEmployeeId()).append(" ");
                employee.getQbdtEmployeeInfo().setToken(toToken);
                if(dto.getUndelete()) {
                    employee.getQbdtEmployeeInfo().setIsDeleted(false);
                }
                Application.save(employee);
            }
        }

        if (payrollItems.size() > 0) {
            noteBuilder.append("\nPayroll Items: ");
            for (QbdtPayrollItemInfo payrollItem : payrollItems) {
                if (payrollItem.getCompanyLaw() != null) {
                    noteBuilder.append(payrollItem.getCompanyLaw().getSourceId()).append(" ");
                } else if (payrollItem.getCompanyPayrollItem() != null) {
                    noteBuilder.append(payrollItem.getCompanyPayrollItem().getSourcePayrollItemId()).append(" ");
                }
                payrollItem.setToken(toToken);
                if(dto.getUndelete()) {
                    payrollItem.setIsDeleted(false);
                }
                Application.save(payrollItem);
            }
        }

        CompanyEvent.createManualDataSyncEvent(company, noteBuilder.toString());

        return processResult;
    }
}
