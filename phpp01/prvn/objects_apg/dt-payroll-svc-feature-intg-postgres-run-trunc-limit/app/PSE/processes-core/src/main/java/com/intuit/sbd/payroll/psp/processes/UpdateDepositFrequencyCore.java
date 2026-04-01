package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EffectiveDepositFrequencyDTO;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

/**
 * User: rkrishna
 * Date: May 20, 2009
 * Time: 11:12:33 AM
 */
public class UpdateDepositFrequencyCore extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private EffectiveDepositFrequencyDTO mEffectiveDepositFrequencyDTO;
    private CompanyAgency mCompanyAgency;
    private Company mCompany;
    private EffectiveDepositFrequency mEffectiveDepositFrequency = null;
    private PaymentTemplateFrequency mPaymentTemplateFrequency;
    private CompanyAgencyPaymentTemplate mAgencyPaymentTemplate;
    private boolean mInvalidateIfEquivalent;

    public UpdateDepositFrequencyCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                      EffectiveDepositFrequencyDTO pEffectiveDepositFrequencyDTO, boolean pInvalidateIfEquivalent) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mEffectiveDepositFrequencyDTO = pEffectiveDepositFrequencyDTO;
        mInvalidateIfEquivalent = pInvalidateIfEquivalent;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company Exists

        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        if (mEffectiveDepositFrequencyDTO == null) {
            validationResult.getMessages().InvalidArgument(EntityName.PaymentTemplate, null, "Effective Deposit Frequency DTO");
            return validationResult;
        }

        //Validate DTO
        ProcessResult validateCompanyResult = mEffectiveDepositFrequencyDTO.validate();
        validationResult.merge(validateCompanyResult);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        String agencyId = mEffectiveDepositFrequencyDTO.getAgencyId();

        // Check if Company Agency Exists
        mCompanyAgency =
                CompanyAgency.findCompanyAgency(mCompany, agencyId);
        if (mCompanyAgency == null) {
            validationResult.getMessages().CompanyAgencyNotFound(
                    EntityName.CompanyAgency, agencyId, mSourceSystemCd.toString(), mSourceCompanyId, agencyId);
            return validationResult;
        }

        // Check if Payment Template Exists
        PaymentTemplate paymentTemplate = PayrollServices.entityFinder.findById(PaymentTemplate.class, mEffectiveDepositFrequencyDTO.getPaymentTemplateCd());

        if (paymentTemplate == null) {
            validationResult.getMessages().EntityDoesNotExist(EntityName.PaymentTemplate,
                    mEffectiveDepositFrequencyDTO.getPaymentTemplateCd(), EntityName.PaymentTemplate.toString(),
                    mEffectiveDepositFrequencyDTO.getPaymentTemplateCd());
            return validationResult;
        }

        // Check if Payment Template is assigned to the company
        mAgencyPaymentTemplate =
                CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(mCompanyAgency, paymentTemplate);

        if (mAgencyPaymentTemplate == null) {
            validationResult.getMessages().PaymentTemplateNotAssignedToCompany(EntityName.PaymentTemplate,
                    paymentTemplate.getPaymentTemplateCd(), mSourceSystemCd.toString(), mSourceCompanyId,
                    paymentTemplate.getPaymentTemplateCd(), mEffectiveDepositFrequencyDTO.getAgencyId());
            return validationResult;
        }

        //Check if PaymentFrequencyId is supported for the Payment Template and find PaymentTemplateFrequency
        mPaymentTemplateFrequency = paymentTemplate.findSupportedPaymentTemplateFrequency(mEffectiveDepositFrequencyDTO.getPaymentFrequencyId());

        if (mPaymentTemplateFrequency == null) {
            validationResult.getMessages().PaymentFrequencyNotSupportedForThePaymentTemplate(
                    EntityName.PaymentTemplate, paymentTemplate.getPaymentTemplateCd(),
                    mEffectiveDepositFrequencyDTO.getPaymentFrequencyId().toString(), paymentTemplate.getPaymentTemplateCd());
            return validationResult;
        }
        SpcfCalendar effectiveDate = mEffectiveDepositFrequencyDTO.getEffectiveDate();

        // Check if an  Effective Deposit Frequency  already exists based on the input oldEffective Date
        // if the effectiveDate is not null, find the existing one and invalidate it.
        // Create a new record for the new one
        if (effectiveDate != null) {
            CalendarUtils.clearTime(effectiveDate);
            SpcfCalendar nextEffectiveDate = effectiveDate.copy();
            nextEffectiveDate.addDays(1);

            Criterion<EffectiveDepositFrequency> edfCriteria = EffectiveDepositFrequency.CompanyAgencyPaymentTemplate().CompanyAgency().Company().equalTo(mCompany)
                    .And(EffectiveDepositFrequency.CompanyAgencyPaymentTemplate().CompanyAgency().Agency().equalTo(paymentTemplate.getAgency()))
                    .And(EffectiveDepositFrequency.CompanyAgencyPaymentTemplate().PaymentTemplate().equalTo(paymentTemplate))
                    .And(EffectiveDepositFrequency.InvalidDate().isNull())
                    .And(EffectiveDepositFrequency.EffectiveDate().greaterOrEqualThan(effectiveDate)).And(EffectiveDepositFrequency.EffectiveDate().lessThan(nextEffectiveDate));

            Expression<EffectiveDepositFrequency> edfQuery =
                    new Query<EffectiveDepositFrequency>()
                            .Where(edfCriteria)
                            .OrderBy(EffectiveDepositFrequency.EffectiveDate().Descending());

            DomainEntitySet<EffectiveDepositFrequency> effectiveFrequencies = Application.find(EffectiveDepositFrequency.class, edfQuery);

            if (effectiveFrequencies.size() > 0) {
                mEffectiveDepositFrequency = effectiveFrequencies.get(0);
            }
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        if (areEquivalent(mEffectiveDepositFrequencyDTO, mEffectiveDepositFrequency)) {
            if(mInvalidateIfEquivalent) {
                mEffectiveDepositFrequency.setInvalidDate(PSPDate.getPSPTime());
                Application.save(mEffectiveDepositFrequency);
            }
            return processResult;
        }

        // If we found an existing deposit frequency for the specified date, invalidate the existing one
        // and create a new one
        if (mEffectiveDepositFrequency != null) {
            mEffectiveDepositFrequency.setInvalidDate(PSPDate.getPSPTime());
            Application.save(mEffectiveDepositFrequency);
        }

        SpcfCalendar date = mEffectiveDepositFrequencyDTO.getEffectiveDate();
        CalendarUtils.clearTime(date);

        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(mEffectiveDepositFrequencyDTO.getPaymentTemplateCd());
        EffectiveDepositFrequency effectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(mCompany, paymentTemplate , date);
        if (effectiveDepositFrequency != null) {
            DepositFrequencyCode currentFrequency = effectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId();
            if (currentFrequency.equals(mEffectiveDepositFrequencyDTO.getPaymentFrequencyId())) {
                return processResult;
            }
        }

        effectiveDepositFrequency = new EffectiveDepositFrequency();
        effectiveDepositFrequency.setEffectiveDate(date);
        effectiveDepositFrequency.setCompanyAgencyPaymentTemplate(mAgencyPaymentTemplate);
        effectiveDepositFrequency.setPaymentTemplateFrequency(mPaymentTemplateFrequency);
        mAgencyPaymentTemplate.addEffectiveDepositFrequency(effectiveDepositFrequency);
        Application.save(effectiveDepositFrequency);

        // Create Event
        CompanyEvent.createDepositFrequencyChangedEvent(mCompany, date, mAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplateCd(), mPaymentTemplateFrequency.getPaymentFrequencyId());

        // Get all non-executed MMTs, collect all the financial transactions attached to each one, remove the financial transactions from these
        // MMTs, delete the existing MMTs and create new MMTs based on the new effective frequencies
        // If it is a zero payment, do not change it.

        Criterion<MoneyMovementTransaction> mmtCriteria = MoneyMovementTransaction.PaymentTemplate().equalTo(mAgencyPaymentTemplate.getPaymentTemplate())
                                                                                  .And(MoneyMovementTransaction.Status().in(PaymentStatus.Created, PaymentStatus.OnHold))
                                                                                  .And(MoneyMovementTransaction.MoneyMovementTransactionAmount().greaterThan(SpcfMoney.ZERO))
                                                                                  .And(MoneyMovementTransaction.Company().equalTo(mCompany));

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, mmtCriteria);
        DomainEntitySet<FinancialTransaction> pendingFinancialTransactions = new DomainEntitySet<FinancialTransaction>();
        for (MoneyMovementTransaction mmTxn : moneyMovementTransactions) {
            DomainEntitySet<FinancialTransaction> fTs = mmTxn.getFinancialTransactionCollection();
            for (FinancialTransaction fT : fTs) {
                pendingFinancialTransactions.add(fT);
            }
        }

        MoneyMovementTransaction.moveAgencyFinancialTransactions(pendingFinancialTransactions, null);

        SpcfCalendar today = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(today);
        PayrollTaxHelper.applyAgencyTaxOverpayments(mCompany, today);

        return processResult;
    }

    public boolean areEquivalent(EffectiveDepositFrequencyDTO pEffectiveDepositFrequencyDTO, EffectiveDepositFrequency pEffectiveDepositFrequency) {
        if (pEffectiveDepositFrequencyDTO == null || pEffectiveDepositFrequency == null)
            return false;

        if (!mPaymentTemplateFrequency.equals(pEffectiveDepositFrequency.getPaymentTemplateFrequency()))
            return false;

        return true;
    }
}
