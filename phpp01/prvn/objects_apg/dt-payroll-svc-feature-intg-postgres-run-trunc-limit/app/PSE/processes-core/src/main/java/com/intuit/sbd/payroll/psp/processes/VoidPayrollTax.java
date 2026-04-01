package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityAdjustmentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 20, 2009
 * Time: 2:30:17 PM
 */
public class VoidPayrollTax extends Process implements IProcess {

    private VoidPayrollDTO voidPayrollDTO;
    private Company company;
    private CompanyAdjustmentSubmission companyVoid;
    private PayrollRun voidedPayrollRun;
    private List<Paycheck> payChecksToVoid = new ArrayList<Paycheck>();


    public VoidPayrollTax(Company pCompany, VoidPayrollDTO pVoidPayrollDTO) {
        company = pCompany;
        voidPayrollDTO = pVoidPayrollDTO;
    }

    public void setCompanyVoid(CompanyAdjustmentSubmission pCompanyVoid) {
        companyVoid = pCompanyVoid;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (company == null) {
            validationResult.getMessages().InvalidValue(EntityName.Company, null, "Company missing for VoidPayrollTax");
            return validationResult;
        }

        if (voidPayrollDTO == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.PayrollRun,
                    "VoidPayrollTax",
                    "voidPayrollDTO");
            return validationResult;
        }

        // validate DTO
        validationResult.merge(voidPayrollDTO.validate());
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        voidedPayrollRun = PayrollRun.findPayrollRun(company, voidPayrollDTO.getSourcePayrollRunId());
        if (voidedPayrollRun == null) {
            validationResult.getMessages().PayrollRunDoesNotExist(
                    EntityName.PayrollRun,
                    voidPayrollDTO.getSourcePayrollRunId(),
                    voidPayrollDTO.getSourcePayrollRunId(),
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId());
            return validationResult;
        }

        // verify VoidPayroll is allowed for the company status
        if (!company.isAllowedCapability(SystemCapabilityCode.VoidPayroll)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId(), SystemCapabilityCode.VoidPayroll.toString());
            return validationResult;
        }

        boolean isHistorical = voidedPayrollRun.isHistoricalPayroll();
        if (isHistorical && voidedPayrollRun.getFinancialTransactionCollection().size() == 0) {
            // we have not accepted the tax service yet
            validationResult.getMessages().RecallPayrollRequired(
                    EntityName.PayrollRun,
                    voidPayrollDTO.getSourcePayrollRunId());
            return validationResult;
        }

        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(company);
        if (!isHistorical && companyBankAccount == null) {
            validationResult.getMessages().CompanyDoesNotHaveActiveBankAccount(
                    EntityName.Company,
                    company.getSourceCompanyId(),
                    company.getSourceSystemCd().toString(),
                    company.getSourceCompanyId());
            return validationResult;
        }

        // Verify Tax impound or fees have already offloaded or are being offloaded
        if (!voidedPayrollRun.havePayrollDebitTransactionsOffloaded()) {
            validationResult.getMessages().RecallPayrollRequired(
                    EntityName.PayrollRun,
                    voidPayrollDTO.getSourcePayrollRunId());
            return validationResult;
        }

        //Validate paychecks to be voided
        if ((voidPayrollDTO.getPaycheckIdList() != null) && !voidPayrollDTO.getPaycheckIdList().isEmpty()) {
            // paychecks are eager loaded by calling class
            for (String paycheckId : voidPayrollDTO.getPaycheckIdList()) {
                Paycheck paycheck = Paycheck.findPaycheck(company, paycheckId);

                if (paycheck == null) {
                    validationResult.getMessages().PaycheckDoesNotExist(
                            EntityName.PayCheck, paycheckId, company.getSourceSystemCd().toString(), company.getSourceCompanyId(), paycheckId);
                    break;
                } else if (paycheck.isVoidedOrRecalled()) {
                    validationResult.getMessages().PaycheckAlreadyCanceled(
                            EntityName.Paycheck,
                            voidPayrollDTO.getSourcePayrollRunId(),
                            paycheckId);
                } else {
                    payChecksToVoid.add(paycheck);
                }
            }
        } else {
            for (Paycheck paycheck : voidedPayrollRun.getPaycheckCollection()) {
                if (!paycheck.isVoidedOrRecalled()) {
                    payChecksToVoid.add(paycheck);
                }
            }

            if (payChecksToVoid.size() == 0) {
                validationResult.getMessages().PayrollRunAlreadyCanceled(
                        EntityName.PayrollRun,
                        voidPayrollDTO.getSourcePayrollRunId(),
                        voidPayrollDTO.getSourcePayrollRunId(),
                        company.getSourceSystemCd().toString(),
                        company.getSourceCompanyId());
            }
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult<>();
        Map<Law, SpcfDecimal> lawAmountsMap = new HashMap<>();
        Map<Law, SpcfDecimal> positiveLiabilitiesToMoveToNewPayroll = new HashMap<Law, SpcfDecimal> ();

        CompanyLaw nationalPaidLeaveLaw = CompanyLaw.findCompanyLaw(company, Law.NATIONAL_PAID_LEAVE_CREDIT);
        if (nationalPaidLeaveLaw != null && voidedPayrollRun.getFinancialTransactionCollection().find(FinancialTransaction.Law().equalTo(nationalPaidLeaveLaw.getLaw())).isNotEmpty()) {
            // positive amounts will create a LA below
            SpcfDecimal cancelledCreditAmount = PayrollSubmitTax.calculateNationalPaidLeaveCreditAmount(payChecksToVoid).negate();
            positiveLiabilitiesToMoveToNewPayroll.put(nationalPaidLeaveLaw.getLaw(), cancelledCreditAmount.add(positiveLiabilitiesToMoveToNewPayroll.computeIfAbsent(nationalPaidLeaveLaw.getLaw(), key -> SpcfMoney.ZERO)));
        }

        CompanyLaw employeeRetentionLeaveLaw = CompanyLaw.findCompanyLaw(company, Law.EMPLOYEE_RETENTION_CREDIT);
        if (employeeRetentionLeaveLaw != null && voidedPayrollRun.getFinancialTransactionCollection().find(FinancialTransaction.Law().equalTo(employeeRetentionLeaveLaw.getLaw())).isNotEmpty()) {
            // positive amounts will create a LA below
            SpcfDecimal cancelledCreditAmount = PayrollSubmitTax.calculateEmployeeRetentionCreditAmount(voidedPayrollRun.getPaycheckDate(), payChecksToVoid).negate();
            positiveLiabilitiesToMoveToNewPayroll.put(employeeRetentionLeaveLaw.getLaw(), cancelledCreditAmount.add(positiveLiabilitiesToMoveToNewPayroll.computeIfAbsent(employeeRetentionLeaveLaw.getLaw(), key -> SpcfMoney.ZERO)));
        }

        CompanyLaw eeFicaDeferralLaw = CompanyLaw.findCompanyLaw(company, Law.FICA_EE_DEFERRAL_CREDIT);
        if (eeFicaDeferralLaw != null && voidedPayrollRun.getFinancialTransactionCollection().find(FinancialTransaction.Law().equalTo(eeFicaDeferralLaw.getLaw())).isNotEmpty()) {
            // positive amounts will create a LA below
            SpcfDecimal cancelledCreditAmount = PayrollSubmitTax.calculateEeFicaDeferralCreditAmount(payChecksToVoid).negate();
            positiveLiabilitiesToMoveToNewPayroll.put(eeFicaDeferralLaw.getLaw(), cancelledCreditAmount.add(positiveLiabilitiesToMoveToNewPayroll.computeIfAbsent(eeFicaDeferralLaw.getLaw(), key -> SpcfMoney.ZERO)));
        }

        for (Paycheck paycheck : payChecksToVoid) {
            for (Tax tax : paycheck.getTaxCollection()) {
                Law law = tax.getLaw();

                SpcfDecimal lawAmount = lawAmountsMap.get(law);
                if (lawAmount == null) {
                    lawAmount = SpcfMoney.ZERO;
                }
                lawAmountsMap.put(tax.getLaw(), lawAmount.add(tax.getTaxLiabilityAmount().negate()));
            }
        }

        CompanyLaw ficaErDeferralCreditLaw = CompanyLaw.findCompanyLaw(company, Law.FICA_ER_DEFERRAL_CREDIT);
        if (voidedPayrollRun.getPaycheckDate().between(PayrollSubmitTax.FICA_DEFERRAL_BEGIN, PayrollSubmitTax.FICA_DEFERRAL_END) && ficaErDeferralCreditLaw != null) {
            Map<Law, SpcfDecimal> financialTransactionAmounts = voidedPayrollRun.getFinancialTransactionLiabilityBalancesByLaw();
            if (financialTransactionAmounts.containsKey(ficaErDeferralCreditLaw.getLaw())) {
                // only void the amount of credit available
                positiveLiabilitiesToMoveToNewPayroll.put(ficaErDeferralCreditLaw.getLaw(),
                                                          // using max because both amounts are negative, so we want the smaller absolute value
                                                          financialTransactionAmounts.get(ficaErDeferralCreditLaw.getLaw()).max(lawAmountsMap.get(Law.getFicaErLaw())).negate());
            }
        }

        // todo remove this it is used by Gemini only
        addLiabilityAdjustmentAmountsToMap(lawAmountsMap);

        // todo remove this it is used by Gemini only
        reduceLiabilityAmountsByHPDEAppliedAmounts(lawAmountsMap);

        if(!voidedPayrollRun.isHistoricalPayroll()) {
            Map<Law, SpcfDecimal> negativeLiabilityOffsets = new HashMap<>();

            // Move positive liabilities to a new payroll so we can debit for them. Create negative liability adjustments as an offset.
            for (Law law : lawAmountsMap.keySet()) {
                if (!law.getLawId().equals(Law.COBRA) && lawAmountsMap.get(law).isGreaterThan(SpcfMoney.ZERO)) {
                    SpcfDecimal positiveLiability = lawAmountsMap.get(law);
                    negativeLiabilityOffsets.put(law, positiveLiability);
                    positiveLiabilitiesToMoveToNewPayroll.put(law, positiveLiability);
                    lawAmountsMap.put(law, SpcfMoney.ZERO);
                }
            }

            // record negative liability adjustments for positive liabilities being moved to a new payroll, no effect on FTs
            processResult.merge(PayrollTaxHelper.createNegativeLiabilityAdjustments(voidedPayrollRun, negativeLiabilityOffsets));

            // update/create FTs
            processResult.merge(PayrollTaxHelper.updateTaxTransactions(voidedPayrollRun, lawAmountsMap));

            // create a new payroll for the positive liability, will create FTs
            processResult.merge(PayrollTaxHelper.createPayrollForPositiveLiability(voidedPayrollRun, positiveLiabilitiesToMoveToNewPayroll));

            for (Paycheck paycheck : payChecksToVoid) {
                if(paycheck.getQbdtPaycheckInfo() != null) {
                    String memo = paycheck.getQbdtPaycheckInfo().getMemo();
                    if(memo == null) {
                        memo = Paycheck.VOID_FUNDS_NOT_RECOVERED;
                    } else {
                        memo += " " + Paycheck.VOID_FUNDS_NOT_RECOVERED;
                    }
                    paycheck.getQbdtPaycheckInfo().setMemo(memo);
                    Application.save(paycheck);
                }
            }
        }

        // todo look at what is/was updating the "company void"
        if(companyVoid != null) {
            companyVoid = Application.save(companyVoid);
        }

        //ATF needs to process this void, so put it into the table of payrolls to process
        ATFPayrollsToProcess newPayrollToProcess = new ATFPayrollsToProcess();
        newPayrollToProcess.setPayrollRun(voidedPayrollRun);
        Application.save(newPayrollToProcess);

        return processResult;
    }

    private void reduceLiabilityAmountsByHPDEAppliedAmounts(Map<Law, SpcfDecimal> pLawAmountsMap) {
        for (Law law : pLawAmountsMap.keySet()) {
            DomainEntitySet<FinancialTransaction> hpdeAppliedAmounts =
                    voidedPayrollRun.getFinancialTransactionCollection()
                            .find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyHPDEPriorPaymentApplied)
                                    .And(FinancialTransaction.Law().equalTo(law))
                                    .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled)));
            for (FinancialTransaction hpdeAppliedAmount : hpdeAppliedAmounts) {
                if(hpdeAppliedAmount.getFinancialTransactionAmount() != null) {
                    pLawAmountsMap.put(law, pLawAmountsMap.get(law).subtract(hpdeAppliedAmount.getFinancialTransactionAmount()));
                }
            }
        }
    }

    /**
     * Add PayrollTax Amount to the map and add Payroll Taxes to the DB/Model.
     *
     * @param pLawAmountsMap
     */
    private void addLiabilityAdjustmentAmountsToMap(Map<Law, SpcfDecimal> pLawAmountsMap) {
        if (voidPayrollDTO.getPayrollTaxes() != null && voidPayrollDTO.getPayrollTaxes().size() > 0) {
            for (LiabilityAdjustmentDTO liabilityAdjustmentDTO : voidPayrollDTO.getPayrollTaxes()) {
                Law law = Application.findById(com.intuit.sbd.payroll.psp.domain.Law.class, liabilityAdjustmentDTO.getLawId());
                SpcfDecimal adjustmentAmount = liabilityAdjustmentDTO.getAmount();
                if (adjustmentAmount != null) {
                    adjustmentAmount = adjustmentAmount.negate();
                    if (pLawAmountsMap.get(law) != null) {
                        adjustmentAmount = adjustmentAmount.add(pLawAmountsMap.get(law));
                    }
                    pLawAmountsMap.put(law, adjustmentAmount);
                }

                LiabilityAdjustment liabilityAdjustment = new LiabilityAdjustment();
                liabilityAdjustment.setCompany(company);
                liabilityAdjustment.setAmount(liabilityAdjustmentDTO.getAmount());
                liabilityAdjustment.setEffectiveDate(DateDTO.convertToSpcfCalendar(liabilityAdjustmentDTO.getEffectiveDate()));
                liabilityAdjustment.setLaw(law);
                liabilityAdjustment.setPayrollRun(voidedPayrollRun);
                liabilityAdjustment.setCompanyAdjustmentSubmission(companyVoid);
                voidedPayrollRun.addLiabilityAdjustment(liabilityAdjustment);
            }
        }
    }

}
