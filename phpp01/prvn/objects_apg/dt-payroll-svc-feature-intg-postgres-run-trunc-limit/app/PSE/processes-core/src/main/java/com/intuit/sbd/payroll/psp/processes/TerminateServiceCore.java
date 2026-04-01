package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyAdjustmentSubmissionDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityAdjustmentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionCancelEEDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.*;

/**
 * @author Jeff Jones
 */
public class TerminateServiceCore  extends Process implements IProcess {

    private SourceSystemCode sourceSystemCode;
    private String sourceCompanyId;
    private Company company;

    private ServiceCode serviceCode;
    @SuppressWarnings({"FieldCanBeLocal"})
    private CompanyService companyService;

    private List<CancelEETransactionsCore> recallProcesses;
    private List<AddLiabilityAdjustmentsCore> liabilityAdjustmentOffsetProcesses;
    private List<CancelTransactionsBillPayment> cancelBillPaymentProcesses;

    private DeactivateServiceCore cancelServiceCoreProcess;

    public TerminateServiceCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, ServiceCode pServiceCode) {
        sourceSystemCode = pSourceSystemCode;
        sourceCompanyId = pSourceCompanyId;
        serviceCode = pServiceCode;
    }

    public Company getCompany() {
        return company;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate company parameters
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCode, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (serviceCode == null) {
            validationResult.getMessages().ServiceCodeNotSpecified(EntityName.Company, sourceCompanyId);
            return validationResult;
        }

        //Validate company exists
        company = Company.findCompany(sourceCompanyId, sourceSystemCode);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    this.sourceSystemCode.toString(), sourceCompanyId);
            return validationResult;
        }

        //Validate company service exists for company
        companyService = company.getService(serviceCode);
        if (companyService == null) {
            validationResult.getMessages().CompanyDoesNotExistOnService(EntityName.Company, sourceCompanyId,
                    this.sourceSystemCode.toString(), sourceCompanyId, serviceCode.toString());
            return validationResult;
        }

        //find pending tax payrolls
        recallProcesses = new ArrayList<CancelEETransactionsCore>();
        liabilityAdjustmentOffsetProcesses = new ArrayList<AddLiabilityAdjustmentsCore>();
        cancelBillPaymentProcesses = new ArrayList<CancelTransactionsBillPayment>();

        Set<PayrollRun> payrollRunsToRecall = new HashSet<PayrollRun>();
        DomainEntitySet<FinancialTransaction> pendingFinancialTransactions = Application.find(FinancialTransaction.class,
                FinancialTransaction.TransactionType().TransactionTypeCd().in(
                         /*  For Tax (Assisted) */
                        TransactionTypeCode.EmployerTaxDebit,
                        TransactionTypeCode.EmployerTaxDirectDebit,
                        /*  For DD */
                        TransactionTypeCode.EmployerDdDebit,
                        TransactionTypeCode.EmployerFeeDebit,
                        TransactionTypeCode.ServiceSalesAndUseTax)
                        .And(FinancialTransaction.Company().equalTo(company))
                        .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));

        for (FinancialTransaction pendingFinancialTransaction : pendingFinancialTransactions) {
            PayrollRun payrollRun = pendingFinancialTransaction.getPayrollRun();
            if (! payrollRunsToRecall.contains(payrollRun)) {
                payrollRunsToRecall.add(payrollRun);

                //recall paychecks
                if (payrollRun.getPaycheckCollection().find(Paycheck.Status().equalTo(PaycheckStatusCode.Active)).size() != 0) {
                    TransactionCancelEEDTO dto = new TransactionCancelEEDTO();
                    dto.setAgentCancel(false);
                    dto.setSourcePayrollRunId(payrollRun.getSourcePayRunId());

                    List<String> sourcePaycheckList = new ArrayList<String>();
                    for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
                        if (!paycheck.isVoidedOrRecalled()) {
                            sourcePaycheckList.add(paycheck.getSourcePaycheckId());
                        }
                    }
                    dto.setSourcePaycheckIdList(sourcePaycheckList);

                    CancelEETransactionsCore cancelProcess = new CancelEETransactionsCore(company.getSourceSystemCd(), company.getSourceCompanyId(), dto);
                    recallProcesses.add(cancelProcess);
                    validationResult.merge(cancelProcess.validate()); //will fail if payment executed
                }

                //"recall" LAs
                SpcfUniqueId originalSubmissionId = null;
                if (payrollRun.getLiabilityAdjustmentCollection().size() > 0) {
                    Map<String, SpcfDecimal> adjustmentAmounts = new HashMap<String, SpcfDecimal>();

                    for (LiabilityAdjustment liabilityAdjustment : payrollRun.getLiabilityAdjustmentCollection()) {
                        String lawId = liabilityAdjustment.getLaw().getLawId();
                        if (! adjustmentAmounts.containsKey(lawId)) {
                            adjustmentAmounts.put(lawId, SpcfMoney.ZERO);
                            originalSubmissionId = liabilityAdjustment.getCompanyAdjustmentSubmission().getId();
                        }

                        adjustmentAmounts.put(lawId, adjustmentAmounts.get(lawId).add(liabilityAdjustment.getAmount()));
                    }

                    List<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
                    for (Map.Entry<String, SpcfDecimal> adjustmentAmount : adjustmentAmounts.entrySet()) {
                        if (! adjustmentAmount.getValue().equals(SpcfMoney.ZERO)) {
                            LiabilityAdjustmentDTO liabilityAdjustmentDTO = new LiabilityAdjustmentDTO();
                            liabilityAdjustmentDTO.setAmount(new SpcfMoney(adjustmentAmount.getValue().negate()));
                            liabilityAdjustmentDTO.setEffectiveDate(new DateDTO(payrollRun.getPaycheckDate()));
                            liabilityAdjustmentDTO.setLawId(adjustmentAmount.getKey());
                            liabilityAdjustmentDTO.setTaxableWages(SpcfMoney.ZERO);
                            liabilityAdjustmentDTO.setTotalWages(SpcfMoney.ZERO);
                            liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
                        }
                    }

                    if (liabilityAdjustmentDTOs.size() > 0) {
                        CompanyAdjustmentSubmissionDTO adjustmentSubmissionDTO = new CompanyAdjustmentSubmissionDTO();
                        adjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);
                        adjustmentSubmissionDTO.setIsVoid(false);
                        adjustmentSubmissionDTO.setSubmissionDate(new DateDTO(PSPDate.getPSPTime()));
                        adjustmentSubmissionDTO.setOriginalSubmissionId(originalSubmissionId);
                        LiabilityAdjustmentOptionsDTO optionsDTO = new LiabilityAdjustmentOptionsDTO(true, true, true, null, true, false);

                        AddLiabilityAdjustmentsCore liabilityAdjustmentOffsetProcess = new AddLiabilityAdjustmentsCore(
                                company.getSourceSystemCd(),
                                company.getSourceCompanyId(),
                                payrollRun.getSourcePayRunId(),
                                adjustmentSubmissionDTO,
                                new DateDTO(payrollRun.getPaycheckDate()),
                                optionsDTO,
                                null);

                        liabilityAdjustmentOffsetProcesses.add(liabilityAdjustmentOffsetProcess);

                        validationResult.merge(liabilityAdjustmentOffsetProcess.validate());
                    }
                }

                // Recall Bill Payments
                if (payrollRun.getBillPaymentCollection().size() > 0) {
                    List<String> billPaymentSourceIds = new ArrayList<String>();
                    for ( BillPayment payment : payrollRun.getBillPaymentCollection() ) {
                        billPaymentSourceIds.add(payment.getSourceId());
                    }
                    CancelTransactionsBillPayment cancelProcess = new CancelTransactionsBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentSourceIds, null);
                    cancelBillPaymentProcesses.add(cancelProcess);
                    validationResult.merge(cancelProcess.validate());
                }

            }

            // todo confirm with risk this is ok
            if((pendingFinancialTransaction.isFeeTransaction() || pendingFinancialTransaction.isSalesTaxTransaction())&& pendingFinancialTransaction.getCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Created) {
                pendingFinancialTransaction.cancelFinancialTransaction();
            }
        }

        //Validate PSP moves money for this service
        if (!companyService.getService().doesPSPMoveMoneyForService()) {
            validationResult.getMessages().CannotTerminateNonMoneyMovementService(EntityName.Company, sourceCompanyId);
            return validationResult;
        }

        // verify the service can be deactivated
        cancelServiceCoreProcess = new DeactivateServiceCore(this.sourceSystemCode, sourceCompanyId, serviceCode);
        validationResult.merge(cancelServiceCoreProcess.validate());

        return validationResult;
    }


    public ProcessResult process() {
        ProcessResult<Company> processResult = new ProcessResult<Company>();

        // recall tax
        for (CancelEETransactionsCore recallProcess : recallProcesses) {
            processResult.merge(recallProcess.process());
        }
        // recall adjustments
        for (AddLiabilityAdjustmentsCore liabilityAdjustmentOffsetProcess : liabilityAdjustmentOffsetProcesses) {
            processResult.merge(liabilityAdjustmentOffsetProcess.process());
        }
        // Recall Bill Payments
        for (CancelTransactionsBillPayment cancelProcess : cancelBillPaymentProcesses) {
            processResult.merge(cancelProcess.process());
        }

        // deactivate company service
        processResult.merge(cancelServiceCoreProcess.process());

        // terminate all services with liability
        for (CompanyService cs : company.getCompanyServiceCollection()) {
            if (cs.getService().doesPSPMoveMoneyForService()) {
                cs.updateCompanyServiceStatus(ServiceSubStatusCode.Terminated);
            }
        }

        processResult.setResult(company);
        return processResult;
    }
}
