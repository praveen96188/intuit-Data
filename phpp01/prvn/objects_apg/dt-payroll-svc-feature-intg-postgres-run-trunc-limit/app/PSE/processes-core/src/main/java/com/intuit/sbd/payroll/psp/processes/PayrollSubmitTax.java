package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.joda.time.LocalDate;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 10, 2009
 * Time: 9:06:47 AM
 */
public class PayrollSubmitTax extends Process implements IProcess {
    public static final SpcfCalendar FICA_DEFERRAL_BEGIN = SpcfCalendar.createInstance(2020, 3, 27, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
    public static final SpcfCalendar FICA_DEFERRAL_END = SpcfCalendar.createInstance(2020, 12, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
    public static final SpcfCalendar COBRA_CREDIT_END_DATE = SpcfCalendar.createInstance(2021, 9, 30, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());

    private SourceSystemCode sourceSystem;
    private String sourceCompanyId;
    private PayrollRunDTO payrollRunDTO;
    private PayrollRun payrollRun;
    private TaxServiceInfoDTO taxServiceInfoDTO;
    private String eventDetail = "Normal";

    public PayrollSubmitTax(SourceSystemCode pSourceSystemCd, String pCompanyId, PayrollRunDTO pPayrollRunDTO) {
        sourceSystem = pSourceSystemCd;
        sourceCompanyId = pCompanyId;
        payrollRunDTO = pPayrollRunDTO;

    }

    public void setPayrollRun(PayrollRun pPayrollRun) {
        payrollRun = pPayrollRun;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult<>();

        Company company = Company.findCompany(sourceCompanyId, sourceSystem);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                                                               sourceSystem.toString(), sourceCompanyId);
            return validationResult;
        }

        validationResult.merge(PayrollSubmitHelper.getInstance().validatePayrollRunDTO(company, payrollRunDTO));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }


        Set<String> lawIds = getLawIds(payrollRunDTO);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        List<Law> lawsToEagerLoad = new ArrayList<>();
        for (String currentLaw : lawIds) {
            Law law = PayrollServices.entityFinder.findById(Law.class, currentLaw);
            if (law == null) {
                validationResult.getMessages().LawDoesNotExist(EntityName.Law, currentLaw);
            } else {
                CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(
                        sourceSystem, sourceCompanyId, law.getPaymentTemplate().getAgency().getAgencyId());

                if (companyAgency == null) {
                    validationResult.getMessages().CompanyAgencyNotFound(
                            EntityName.CompanyAgency, sourceSystem.toString(), sourceSystem.toString(), sourceCompanyId, law.getPaymentTemplate().getAgency().getAgencyId());
                }

                lawsToEagerLoad.add(law);
            }
        }

        CompanyLaw.eagerLoadCompanyLaws(company, lawsToEagerLoad);

        TaxCompanyServiceInfo taxCompanyServiceInfo = (TaxCompanyServiceInfo) company.getCompanyService(ServiceCode.Tax);
        if (taxCompanyServiceInfo.getLastTaxYear() < payrollRunDTO.getTargetPayrollTXDate().getYear()) {
            taxServiceInfoDTO = (TaxServiceInfoDTO) PayrollServices.dtoFactory.create(taxCompanyServiceInfo);
            taxServiceInfoDTO.setLastTaxYear(payrollRunDTO.getTargetPayrollTXDate().getYear());
        }

        return validationResult;
    }

    private Set<String> getLawIds(PayrollRunDTO payrollRunDTO) {
        Set<String> lawIds = new HashSet<>();
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            for (LiabilityTransactionDTO liabilityTransactionDTO : paycheckDTO.getLiabilityTransactions()) {
                lawIds.add(liabilityTransactionDTO.getLawId());
            }
        }

        return lawIds;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult<>();
        // these two system parameters aren't in the populate script. Making them configurable just in case congress decides to change the dates... again...
        SpcfCalendar ffcraActEndDate = SpcfCalendar.createInstance(LocalDate.parse(SystemParameter.findStringValue(SystemParameter.Code.FFCRA_END_DATE, "2021-03-31")).toDateTimeAtStartOfDay().getMillis());
        SpcfCalendar caresActEndDate = SpcfCalendar.createInstance(LocalDate.parse(SystemParameter.findStringValue(SystemParameter.Code.CARES_END_DATE, "2021-06-20")).toDateTimeAtStartOfDay().getMillis());

        SpcfDecimal ffcraCreditAmount = calculateNationalPaidLeaveCreditAmount(payrollRun.getPaycheckCollection());

        if (!ffcraCreditAmount.isZero()
                && payrollRun.getPaycheckCollection().get(0).getPayPeriodEndDate().compareTo(ffcraActEndDate) > 0) {
            processResult.getMessages().PayrollRejectFFCRAOutdatedItems(EntityName.PayrollRun,
                    payrollRun.getSourcePayRunId());
            return processResult;
        }

        CompanyLaw nationalPaidLeaveLaw = null;
        if (!ffcraCreditAmount.isZero()) {
            nationalPaidLeaveLaw = CompanyLaw.findCompanyLaw(payrollRun.getCompany(), Law.NATIONAL_PAID_LEAVE_CREDIT);
            if (nationalPaidLeaveLaw == null) {
                // this validation can't be included in the validate method because the payroll run hasn't been created yet
                processResult.getMessages().GenericError(EntityName.CompanyLaw, Law.NATIONAL_PAID_LEAVE_CREDIT, "Paychecks contain FFCRA credit amounts, but credit law does not exist");
                return processResult;
            }
            // create FFCRA payroll liability adjustment
            processResult.merge(createCreditAdjustment(payrollRun, nationalPaidLeaveLaw, new SpcfMoney(ffcraCreditAmount)));
        }

        if (payrollRun.getPaycheckDate().compareTo(caresActEndDate) < 1) {
            SpcfDecimal caresCreditAmount = calculateEmployeeRetentionCreditAmount(payrollRun.getPaycheckDate(), payrollRun.getPaycheckCollection());
            CompanyLaw employeeRetentionCredit = null;
            if (!caresCreditAmount.isZero()) {
                employeeRetentionCredit = CompanyLaw.findCompanyLaw(payrollRun.getCompany(), Law.EMPLOYEE_RETENTION_CREDIT);
                if (employeeRetentionCredit == null) {
                    // this validation can't be included in the validate method because the payroll run hasn't been set yet
                    processResult.getMessages().GenericError(EntityName.CompanyLaw, Law.EMPLOYEE_RETENTION_CREDIT, "Paychecks contain CARES credit amounts, but credit law does not exist");
                    return processResult;
                }
            }

            // create CARES liability adjustment
            processResult.merge(createCreditAdjustment(payrollRun, employeeRetentionCredit, new SpcfMoney(caresCreditAmount)));
        }

        // EE deferral ends 12/31/2020
        if (payrollRun.getPaycheckDate().compareTo(FICA_DEFERRAL_END) < 1) {
            SpcfDecimal eeDeferralCreditAmount = calculateEeFicaDeferralCreditAmount(payrollRun.getPaycheckCollection());
            CompanyLaw eeDeferralCredit = null;
            if (!eeDeferralCreditAmount.isZero()) {
                eeDeferralCredit = CompanyLaw.findCompanyLaw(payrollRun.getCompany(), Law.FICA_EE_DEFERRAL_CREDIT);
                if (eeDeferralCredit == null) {
                    // this validation can't be included in the validate method because the payroll run hasn't been set yet
                    processResult.getMessages().GenericError(EntityName.CompanyLaw, Law.FICA_EE_DEFERRAL_CREDIT, "Paychecks contain EE FICA DEFERRAL credit amounts, but credit law does not exist");
                    return processResult;
                }
            }
            // create EE deferral liability adjustment
            processResult.merge(createCreditAdjustment(payrollRun, eeDeferralCredit, new SpcfMoney(eeDeferralCreditAmount)));
        }

        HashMap<Law, SpcfDecimal> liabilityBalances = LiabilityBalances.getLiabilityBalances(payrollRun, payrollRunDTO.getBalanceFilePayroll());

        // if the paycheck date is within the fica er deferral window and the company has an active fica er deferral item offset the fica er amount
        CompanyLaw ficaErDeferralCreditLaw = CompanyLaw.findCompanyLaw(payrollRun.getCompany(), Law.FICA_ER_DEFERRAL_CREDIT);
        if (payrollRun.getPaycheckDate().between(FICA_DEFERRAL_BEGIN, FICA_DEFERRAL_END) && ficaErDeferralCreditLaw != null && ficaErDeferralCreditLaw.getCompanyAgency().getErFicaDeferralEnabled()) {
            SpcfDecimal ficaErAmount = liabilityBalances.computeIfAbsent(Law.getFicaErLaw(), key -> SpcfMoney.ZERO);
            SpcfMoney creditAmount = new SpcfMoney(ficaErAmount.negate());

            if (creditAmount.isGreaterThan(SpcfMoney.ZERO)) {
                SpcfDecimal totalCreditAdjustmentForQuarter = LiabilityAdjustment.calculateAdjustmentsAmountForQuarter(payrollRun.getCompany(),
                                                                                                                       payrollRun.getPaycheckDate())
                                                                                 .computeIfAbsent(ficaErDeferralCreditLaw.getLaw(), key -> SpcfMoney.ZERO);
                // only apply the amount that has been calculated for the quarter
                creditAmount = new SpcfMoney(creditAmount.min(totalCreditAdjustmentForQuarter.negate()));
            }

            if (!creditAmount.isZero()) {
                createCreditAdjustment(payrollRun, ficaErDeferralCreditLaw, creditAmount);
                liabilityBalances.put(ficaErDeferralCreditLaw.getLaw(), creditAmount);
            }
        }

        if ((payrollRun.getPaycheckDate().compareTo(COBRA_CREDIT_END_DATE) > 0)) {
            liabilityBalances.computeIfPresent(Application.findById(Law.class, Law.COBRA), (law, spcfDecimal) -> SpcfMoney.ZERO);
            CompanyEvent.createLiabilityAdjustmentCreatedEvent(payrollRun.getCompany(), Law.COBRA, payrollRun.getId().toString(), "Not creating financial transactions for COBRA credits starting 1st October 2021", eventDetail);
        }

        processResult.merge(PayrollTaxHelper.createTaxTransactions(payrollRun, new PayrollTaxHelper.CreateTaxTransactionsOptions(true, liabilityBalances)));

        if (payrollRunDTO.chargeFees()) {
            //
            // Create MonthlyFee fee transaction (if appropriate)
            //
            CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(payrollRun.getCompany());
            CompanyOffering companyOffering = payrollRun.getCompany().getOffering(ServiceCode.DirectDeposit);
            OfferingCode offeringCode = companyOffering.getOffering().getOfferingCode();
            FinancialTransaction erDebitFT = payrollRun.getEmployerTaxDebit(TransactionStateCode.Created);
            SpcfCalendar feeSettlementDate = null;

            //
            // Try to use the same settlement date as the ER debit (if possible)
            //
            if (erDebitFT != null) {
                feeSettlementDate = erDebitFT.getSettlementDate().toLocal();
            }

            // if feeSettlementDate is null, BD will use the next available settlement date
            BillingDetail.createExtraStateFeesForPayrollRunIfMeetsCriteria(payrollRun, companyBankAccount, feeSettlementDate, offeringCode);
        }

        if (taxServiceInfoDTO != null) {
            UpdateServiceTax updateServiceTaxCore = new UpdateServiceTax(payrollRun.getCompany(), taxServiceInfoDTO.getServiceStartDate(), taxServiceInfoDTO);
            processResult.merge(updateServiceTaxCore.execute());
        }

        return processResult;
    }

    public static SpcfDecimal calculateNationalPaidLeaveCreditAmount(Collection<Paycheck> paycheckCollection) {
        return sumPayrollItemAmountsForTaxTrackingTypes(paycheckCollection, CompanyPayrollItem.FFCRA_TAX_FORM_LINE_CODES);
    }

    public static SpcfDecimal calculateEmployeeRetentionCreditAmount(SpcfCalendar paycheckDate, Collection<Paycheck> paycheckCollection) {
        SpcfDecimal multiplier;
        if (paycheckDate.compareTo(FICA_DEFERRAL_END) < 1) {
            // calculated credit amount is 50% of wage amount
            multiplier = SpcfDecimal.createInstance(0.5);
        } else {
            // calculated credit amount is 70% of wage amount
            multiplier = SpcfDecimal.createInstance(0.7);
        }
        return sumPayrollItemAmountsForTaxTrackingTypes(paycheckCollection, CompanyPayrollItem.CARES_TAX_FORM_LINE_CODES).multiply(multiplier, 2, SpcfDecimal.SpcfRoundingType.HalfUp);
    }

    public static SpcfDecimal calculateEeFicaDeferralCreditAmount(Collection<Paycheck> paycheckCollection) {
        return sumPayrollItemAmountsForTaxTrackingTypes(paycheckCollection, CompanyPayrollItem.EE_FICA_DEFERRAL_TAX_FORM_LINE_CODES);
    }

    public static SpcfDecimal sumPayrollItemAmountsForTaxTrackingTypes(Collection<Paycheck> paycheckCollection, List<String> taxTrackingTypes) {
        if (taxTrackingTypes == null || taxTrackingTypes.isEmpty()) {
            return SpcfMoney.ZERO;
        }

        // find compensations amount
        SpcfDecimal creditAmount = paycheckCollection.stream()
                                                     .map(paycheck -> paycheck.getCompensationCollection()
                                                                              .stream()
                                                                              .filter(compensation -> taxTrackingTypes.contains(compensation.getCompanyPayrollItem().getTaxFormLine()))
                                                                              .map(Compensation::getCompensationAmount)
                                                                              .reduce(SpcfMoney.ZERO, (spcfMoney, spcfMoney2) -> new SpcfMoney(spcfMoney.add(spcfMoney2))))
                                                     .reduce(SpcfMoney.ZERO, (spcfMoney, spcfMoney2) -> new SpcfMoney(spcfMoney.add(spcfMoney2)))
                                                     .negate();

        // find employer contributions amount
        creditAmount = creditAmount.add(paycheckCollection.stream()
                                                          .map(paycheck -> paycheck.getEmployerContributionCollection()
                                                                                   .stream()
                                                                                   .filter(contribution -> taxTrackingTypes.contains(contribution.getCompanyPayrollItem().getTaxFormLine()))
                                                                                   .map(EmployerContribution::getContributionAmount)
                                                                                   .reduce(SpcfMoney.ZERO, (spcfMoney, spcfMoney2) -> new SpcfMoney(spcfMoney.add(spcfMoney2))))
                                                          .reduce(SpcfMoney.ZERO, (spcfMoney, spcfMoney2) -> new SpcfMoney(spcfMoney.add(spcfMoney2)))
                                                          .negate());

        // find deduction amount
        creditAmount = creditAmount.add(paycheckCollection.stream()
                                                          .map(paycheck -> paycheck.getDeductionCollection()
                                                                                   .stream()
                                                                                   .filter(deduction -> taxTrackingTypes.contains(deduction.getCompanyPayrollItem().getTaxFormLine()))
                                                                                   // since this is a positive deduction the we need to negate
                                                                                   .map(deduction -> deduction.getDeductionAmount().negate())
                                                                                   .reduce(SpcfMoney.ZERO, (spcfMoney, spcfMoney2) -> new SpcfMoney(spcfMoney.add(spcfMoney2))))
                                                          .reduce(SpcfMoney.ZERO, (spcfMoney, spcfMoney2) -> new SpcfMoney(spcfMoney.add(spcfMoney2)))
                                                          .negate());

        return creditAmount;
    }

    private ProcessResult<CompanyAdjustmentSubmission> createCreditAdjustment(PayrollRun payrollRun, CompanyLaw creditLaw, SpcfMoney creditAmount) {
        if (creditLaw == null || creditAmount == null || creditAmount.isZero()) {
            return new ProcessResult<>();
        }

        Company company = payrollRun.getCompany();
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = new CompanyAdjustmentSubmissionDTO();
        companyAdjustmentSubmissionDTO.setTotalAmount(creditAmount);
        companyAdjustmentSubmissionDTO.setSubmissionDate(new DateDTO(PSPDate.getPSPTime()));

        LiabilityAdjustmentDTO liabilityAdjustmentDTO = new LiabilityAdjustmentDTO();
        liabilityAdjustmentDTO.setAmount(creditAmount);
        liabilityAdjustmentDTO.setTaxableWages(SpcfMoney.ZERO);
        liabilityAdjustmentDTO.setEffectiveDate(new DateDTO(payrollRun.getPaycheckDate()));
        liabilityAdjustmentDTO.setLawId(creditLaw.getLaw().getLawId());

        QBDTTransactionInfoDTO liabilityAdjustmentLineTransactionInfoDTO = new QBDTTransactionInfoDTO();
        liabilityAdjustmentLineTransactionInfoDTO.setAccountName(creditLaw.getExpenseAccount());
        liabilityAdjustmentLineTransactionInfoDTO.setMemo("Credit for " + creditLaw.getSourceDescription());
        liabilityAdjustmentDTO.setQBDTTransactionInfoDTO(liabilityAdjustmentLineTransactionInfoDTO);
        companyAdjustmentSubmissionDTO.getLiabilityAdjustmentDTOs().add(liabilityAdjustmentDTO);

        QBDTTransactionInfoDTO qbdtTransactionInfoDTO = new QBDTTransactionInfoDTO();
        qbdtTransactionInfoDTO.setOnService(true);
        qbdtTransactionInfoDTO.setToken(-2L);
        qbdtTransactionInfoDTO.setMemo("Credit for " + creditLaw.getSourceDescription());
        qbdtTransactionInfoDTO.setCleared(QBOFX.DEFAULT_CLEARED_RESPONSE_STR);
        companyAdjustmentSubmissionDTO.setQBDTTransactionInfoDTO(qbdtTransactionInfoDTO);

        //noinspection
        AddLiabilityAdjustmentsCore addLiabilityAdjustmentsCore = new AddLiabilityAdjustmentsCore(company.getSourceSystemCd(), company.getSourceCompanyId(),
                                                                                                  null, companyAdjustmentSubmissionDTO, new DateDTO(payrollRun.getPaycheckDate()),
                                                                                                  new LiabilityAdjustmentOptionsDTO(true, true,
                                                                                                                                    false, new DateDTO(payrollRun.getPaycheckSettlementDate()),
                                                                                                                                    false, payrollRunDTO.getBalanceFilePayroll()),
                                                                                                  null, false);
        addLiabilityAdjustmentsCore.setPayrollRun(payrollRun);
        //noinspection unchecked
        return addLiabilityAdjustmentsCore.execute();
    }
}
