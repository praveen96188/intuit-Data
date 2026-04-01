package com.intuit.sbd.payroll.psp.domain;

import com.intuit.payroll.agency.api.IRulesInfo;
import com.intuit.payroll.agency.api.IRulesPaymentTemplate;
import com.intuit.payroll.agency.api.RulesObjectBroker;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang.ObjectUtils;

import java.util.*;

/**
 * Hand-written business logic
 */
public class CompanyAgencyPaymentTemplate extends BaseCompanyAgencyPaymentTemplate {
    public static String AgencyTaxPayerIdKeyName="CAPT_ATaxPayerId";

    public NaturalKey getNaturalKey(Company pCompany) {
        return new NaturalKey(CompanyAgencyPaymentTemplate.class, pCompany.getId(), getPaymentTemplate().getAgency().getAgencyId(), getPaymentTemplate().getPaymentTemplateCd());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static CompanyAgencyPaymentTemplate findCompanyAgencyPaymentTemplate(CompanyAgency pCompanyAgency, PaymentTemplate pPaymentTemplate) {
        return findCompanyAgencyPaymentTemplate(pCompanyAgency.getCompany(), pPaymentTemplate);
    }

    public static CompanyAgencyPaymentTemplate findCompanyAgencyPaymentTemplate(Company pCompany, PaymentTemplate paymentTemplate) {
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = null;

        NaturalKey naturalKey = new NaturalKey(CompanyAgencyPaymentTemplate.class, pCompany.getId(), paymentTemplate.getAgency().getAgencyId(), paymentTemplate.getPaymentTemplateCd());
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            companyAgencyPaymentTemplate = Application.findById(CompanyAgencyPaymentTemplate.class, primaryKey);
        } else {
            DomainEntitySet<CompanyAgencyPaymentTemplate> companyAgencyPaymentTemplates = Application.find(CompanyAgencyPaymentTemplate.class, new Query<CompanyAgencyPaymentTemplate>()
                    .Where(CompanyAgencyPaymentTemplate.CompanyAgency().Company().equalTo(pCompany)
                            .And(CompanyAgencyPaymentTemplate.CompanyAgency().Agency().equalTo(paymentTemplate.getAgency()))
                            .And(CompanyAgencyPaymentTemplate.PaymentTemplate().equalTo(paymentTemplate))));

            if (companyAgencyPaymentTemplates.size() > 1) {
                throw new RuntimeException(
                        "Query for company agency payment template for company " + pCompany.getSourceCompanyId() + " and payment template " + paymentTemplate.getPaymentTemplateCd() + " did not return 0 or 1 results as expected");
            }

            if (!companyAgencyPaymentTemplates.isEmpty()) {
                companyAgencyPaymentTemplate = companyAgencyPaymentTemplates.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, companyAgencyPaymentTemplate.getId());
            }
        }

        return companyAgencyPaymentTemplate;
    }

    public static DomainEntitySet<CompanyAgencyPaymentTemplate> findCompanyAgencyPaymentTemplates(Company pCompany) {
        return Application.find(CompanyAgencyPaymentTemplate.class, CompanyAgencyPaymentTemplate.CompanyAgency().Company().equalTo(pCompany));
    }

    public static DomainEntitySet<CompanyAgencyPaymentTemplate> findSupportedCompanyAgencyPaymentTemplates(Company pCompany) {
        return Application.find(CompanyAgencyPaymentTemplate.class, CompanyAgencyPaymentTemplate.CompanyAgency().Company().equalTo(pCompany)
                .And(CompanyAgencyPaymentTemplate.PaymentTemplate().SupportStartDate().isNotNull()));
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public CompanyAgencyPaymentTemplate() {
        super();
    }

    public void createEffectiveDepositFrequency() {
        IRulesInfo rulesInfo = RulesObjectBroker.getInstance().getRulesInfo();
        IRulesPaymentTemplate paymentTemplateRules = rulesInfo.getPaymentTemplate(getPaymentTemplate().getPaymentTemplateCd());
        if (paymentTemplateRules == null) {
            //will be null for no calc templates
            return;
        }

        String defaultPaymentFrequencyId = paymentTemplateRules.getDefaultPaymentFrequencyID();
        if (defaultPaymentFrequencyId == null || defaultPaymentFrequencyId.length() == 0) {
            defaultPaymentFrequencyId = getPaymentTemplate().getDefaultDepositFrequency();
        }
        DomainEntitySet<PaymentTemplateFrequency> paymentTemplateFrequencies = getPaymentTemplate().getPaymentTemplateFrequencyCollection();
        if (paymentTemplateFrequencies != null && paymentTemplateFrequencies.size() > 0 && defaultPaymentFrequencyId != null) {
            for (PaymentTemplateFrequency paymentTemplateFrequency : paymentTemplateFrequencies) {
                if (paymentTemplateFrequency.getPaymentFrequencyId().toString().equals(defaultPaymentFrequencyId)) {
                    EffectiveDepositFrequency effectiveDepositFrequency = new EffectiveDepositFrequency();
                    effectiveDepositFrequency.setEffectiveDate(CalendarUtils.getFirstDayOfTheYearLocal(PSPDate.getPSPTime()));
                    effectiveDepositFrequency.setCompanyAgencyPaymentTemplate(this);
                    effectiveDepositFrequency.setPaymentTemplateFrequency(paymentTemplateFrequency);
                    addEffectiveDepositFrequency(effectiveDepositFrequency);
                    Application.save(effectiveDepositFrequency);
                }
            }
        }
    }

    public void createDepositFrequencyChangedEvent(PayrollRun pPayrollRun, SpcfCalendar pNewEffectiveDate, SpcfCalendar pOldEffectiveDate, DepositFrequencyCode pNewPaymentFrequencyId, DepositFrequencyCode pOldPaymentFrequencyId, boolean pUpperLimit) {
        PspPrincipal principal = (PspPrincipal) Application.getCurrentPrincipal();
        AuthUser foundUser = AuthUser.findUser(principal.getId());

        CompanyEvent event = CompanyEvent.createCompanyEvent(pPayrollRun.getCompany(), EventTypeCode.DepositFrequencyChanged);
        event.addCompanyEventDetail(EventDetailTypeCode.NewEffectiveDate, pNewEffectiveDate == null ? "" : pNewEffectiveDate.toString());
        event.addCompanyEventDetail(EventDetailTypeCode.OldEffectiveDate, pOldEffectiveDate == null ? "" : pOldEffectiveDate.toString());
        if (pUpperLimit) {
            event.addCompanyEventDetail(EventDetailTypeCode.ReasonDescription, "UpperLimit");
        }
        if (pNewPaymentFrequencyId != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.NewDepositFrequency, pNewPaymentFrequencyId.toString());
        } else {
            event.addCompanyEventDetail(EventDetailTypeCode.NewDepositFrequency, null);
        }
        if (pOldPaymentFrequencyId != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.OldDepositFrequency, pOldPaymentFrequencyId.toString());
        } else {
            event.addCompanyEventDetail(EventDetailTypeCode.OldDepositFrequency, null);
        }
        if (foundUser != null) {
            event.addCompanyEventDetail(EventDetailTypeCode.UserId, foundUser.getCorpId());
        }
        event.addCompanyEventDetail(EventDetailTypeCode.PaymentTemplate, getPaymentTemplate().getPaymentTemplateCd());
    }

    public static Map<CompanyAgencyPaymentTemplate, SpcfCalendarRange> getCompanyTemplateValidDates(Company company) {
        Map<CompanyAgencyPaymentTemplate, SpcfCalendarRange> companyTemplateValidDates = new HashMap<CompanyAgencyPaymentTemplate, SpcfCalendarRange>();

        List<Object[]> rows = Application.executeNamedQuery("findCompanyPaymentTemplateDates",
                new String[]{"company"},
                new Object[]{company});

        for (Object[] row : rows) {
            companyTemplateValidDates.put(
                    (CompanyAgencyPaymentTemplate) row[0],
                    new SpcfCalendarRange(
                            (SpcfCalendar) ObjectUtils.max((SpcfCalendar) row[1], (SpcfCalendar) row[2]),
                            (SpcfCalendar) row[3]));

        }

        return companyTemplateValidDates;
    }

    public static class SpcfCalendarRange {
        public SpcfCalendar begin;
        public SpcfCalendar end;

        public SpcfCalendarRange(SpcfCalendar begin, SpcfCalendar end) {
            this.begin = begin;
            this.end = end;
        }
    }



    /**
     * Recalculates whether each payment method is valid and sets the enabled status
     * This is called whenever a potential input has changed.
     * If the methods have changed, payment methods for existing payments are changed.
     * Enrollment holds are added or removed
     *
     * @return if the status on any payment method changed
     */
    public boolean recalculatePaymentMethods() {
        boolean statusChanged = false;
        for (CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod : getCompanyPaymentTemplatePaymentMethodCollection()) {
            if (companyPaymentTemplatePaymentMethod.recalculatePaymentEnabled()) {
                statusChanged = true;
            }
        }

        if (statusChanged) {
            for (MoneyMovementTransaction pendingPayment : MoneyMovementTransaction.findTaxPayments().setCompany(getCompanyAgency().getCompany()).setPaymentTemplate(getPaymentTemplate()).setPending().find()) {
                recalculatePaymentMethods(pendingPayment);
            }
        }

        return statusChanged;
    }

  /**
     * Determines the highest priority payment method enabled for this payment and updates to it.
     * Updates to null if no method is enabled.
     * Enrollment hold is added/removed if setting to/from null payment method.
     * This must be called whenever a potential input for payment-specific requirements are changed.
     * Precondition: company's payment method must have already been recalculated.
     *
     * @param payment payment to check payment-centric requirements against
     * @return if the payment method on the payment changes
     */
    public boolean recalculatePaymentMethods(MoneyMovementTransaction payment) {
        boolean statusChanged = false;

        boolean changeMethodToNull = true;
        PaymentMethod changeMethodTo = null;
        for (PaymentTemplatePaymentMethod paymentTemplatePaymentMethod : getPaymentTemplate().getPaymentTemplatePaymentMethods()) {
            if (paymentTemplatePaymentMethod.getPaymentMethodOrder() == -1) {
                //-1 indicates that this template does not have its payment methods automatically set
                changeMethodTo = null;
                changeMethodToNull = false;
                break;
            }

            CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod = getCompanyPaymentTemplatePaymentMethod(paymentTemplatePaymentMethod.getPaymentMethod());
            if (companyPaymentTemplatePaymentMethod == null) {
                //scenario: new payment method is added, but not added to company yet
                continue;
            }
            if (companyPaymentTemplatePaymentMethod.getEnabledForPayment(payment)) {
                changeMethodTo = paymentTemplatePaymentMethod.getPaymentMethod();
                changeMethodToNull = false;
                break;
            }
        }

        if (changeMethodTo != null || changeMethodToNull) {
            if (payment.getMoneyMovementPaymentMethod() != changeMethodTo) {
                //PSP-3025 if the agent changed the payment method manually, do not change it here (unless we have to)
                boolean currentPaymentMethodEnabled = payment.getMoneyMovementPaymentMethod() == null || getCompanyPaymentTemplatePaymentMethod(payment.getMoneyMovementPaymentMethod()).getEnabledForPayment(payment);
                boolean paymentWasManuallyChanged = CompanyEvent.findCompanyEventDetailForEventDetailValue(payment.getCompany(), EventTypeCode.PaymentMethodChanged, EventDetailTypeCode.UserId, EventDetailTypeCode.UniqueIdentifier, payment.getId().toString()).isNotEmpty();
                if (!currentPaymentMethodEnabled || !paymentWasManuallyChanged) {
                    statusChanged = true;
                    payment.updateTaxPaymentMethod(changeMethodTo);
                }
            }
        }
        payment.addOrRemoveEnrollmentHold();

        return statusChanged;

    }

    public PaymentMethod getCurrentPaymentMethod() {
        //todo unify EFTPSDirectDebit handling (settlement types, money methods, etc.)
        //special case for federal--always start at EFTPS and other processing will set to DirectDebit if needed
        if (getPaymentTemplate().getAgency().isIRS()) {
            return PaymentMethod.EFTPS;
        }

        for (PaymentTemplatePaymentMethod paymentTemplatePaymentMethod : getPaymentTemplate().getPaymentTemplatePaymentMethods()) {
            CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod = getCompanyPaymentTemplatePaymentMethod(paymentTemplatePaymentMethod.getPaymentMethod());
            if (companyPaymentTemplatePaymentMethod.getEnabled()) {
                return paymentTemplatePaymentMethod.getPaymentMethod();
            }
        }

        //if none enabled, null
        return null;
    }

    public boolean hasActiveLaw() {
        return hasActiveLaw(true);
    }

    public boolean hasActiveLaw(boolean includeDeleted) {
        DomainEntitySet<Law> laws = this.getPaymentTemplate().getLawCollection();
        for (Law law : laws) {
            CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(this.getCompanyAgency(), law);

            if (companyLaw != null) {
                // Only check the QBDT Payroll Item Info for the possible deleted flag if requested.
                if (!includeDeleted) {
                    QbdtPayrollItemInfo pItemInfo = companyLaw.getQbdtPayrollItemInfo();
                    if (pItemInfo != null && pItemInfo.getIsDeleted()) {
                        continue;
                    }
                }

                // FilingStatus == null is considered Active
                if (companyLaw.getFilingStatus() == null || companyLaw.getFilingStatus() == PayrollItemStatus.Active) {
                    return true;
                }
            }
        }
        return false;
    }

    public CompanyPaymentTemplatePaymentMethod getCompanyPaymentTemplatePaymentMethod(PaymentMethod paymentMethod) {
        return getCompanyPaymentTemplatePaymentMethodCollection()
                .find(CompanyPaymentTemplatePaymentMethod.PaymentMethod().equalTo(paymentMethod))
                .getFirst();
    }


    /*
    Creates new CAPT and everything associated except for Deposit Frequencies (client responsibility)
     */

    public static CompanyAgencyPaymentTemplate createNewCompanyAgencyPaymentTemplate(PaymentTemplate paymentTemplate, CompanyAgency companyAgency, String pAgencyId) {
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = new CompanyAgencyPaymentTemplate();

        companyAgencyPaymentTemplate.setCompanyAgency(companyAgency);
        companyAgencyPaymentTemplate.setPaymentTemplate(paymentTemplate);
        companyAgencyPaymentTemplate = Application.save(companyAgencyPaymentTemplate);
        //todo (see above) I guess I will leave this alone for now so if an agency has no WH we may get the right ID so less cleanup later
        if (pAgencyId == null && companyAgency.getAgency().isIRS()) {
            pAgencyId = companyAgency.getCompany().getFedTaxId();
        }
        companyAgencyPaymentTemplate.updateAgencyTaxpayerId(pAgencyId);
        paymentTemplate.createCompanyAgencyFormTemplate(companyAgency);

        //add payment methods
        for (PaymentTemplatePaymentMethod paymentMethod : paymentTemplate.getPaymentTemplatePaymentMethods()) {
            CompanyPaymentTemplatePaymentMethod.createNewCompanyPaymentTemplatePaymentMethod(paymentMethod, companyAgencyPaymentTemplate);
        }

        companyAgencyPaymentTemplate.recalculatePaymentMethods();

        companyAgency.addCompanyAgencyPaymentTemplate(companyAgencyPaymentTemplate);
        Application.save(companyAgencyPaymentTemplate);

          // add the payment template to the cache
        Application.getSessionCache().addPrimaryKey(companyAgencyPaymentTemplate.getNaturalKey(companyAgency.getCompany()), companyAgencyPaymentTemplate.getId());

        return companyAgencyPaymentTemplate;
    }




    public void updateAgencyTaxpayerId(String agencyTaxPayerId) {
        if (!ObjectUtils.equals(getAgencyTaxpayerId(), agencyTaxPayerId)) {
            setAgencyTaxpayerId(agencyTaxPayerId);
            PaymentTemplate paymentTemplate = this.getPaymentTemplate();
            MoneyMovementTransaction.TaxPaymentsFinder pendingPaymentsFinder = MoneyMovementTransaction.findTaxPayments().setCompany(getCompanyAgency().getCompany()).setPaymentTemplate(paymentTemplate).setPendingOrIgnore();
            for (MoneyMovementTransaction moneyMovementTransaction : pendingPaymentsFinder.find()) {
                moneyMovementTransaction.setAgencyTaxpayerId(agencyTaxPayerId);
            }
            this.recalculatePaymentMethods();
            for (MoneyMovementTransaction moneyMovementTransaction : pendingPaymentsFinder.find()) {
                this.recalculatePaymentMethods(moneyMovementTransaction);
                MoneyMovementTransaction.recreateEntryDetailRecords(moneyMovementTransaction);
            }

            if(Agency.FL_AGENT_ID.equals(getCompanyAgency().getAgency().getAgencyId())) {
                // Send in ADD file if all enrollment conditions are met. If it is already enrolled send again in ADD file
                ACHEnrollment.createACHEnrollment(getCompanyAgency().getCompany(), true);
            }
        }
    }

    /*
    Returns the active records (as of PSPDate) for each available type on the payment template.
    If any are missing, will include an empty CompanyFilingAmount with an NaN value.
     */
    public DomainEntitySet<CompanyFilingAmount> getActiveAndMissingCompanyFilingAmounts() {
        Map<String, CompanyFilingAmount> activeFilingAmounts = new HashMap<String, CompanyFilingAmount>();
        for (CompanyFilingAmount companyFilingAmount :
                getCompanyFilingAmountCollection()
                        .find(CompanyFilingAmount.InvalidDate().isNull()
                                                 .And(CompanyFilingAmount.EffectiveDate().lessOrEqualThan(PSPDate.getPSPTime())))
                        .sort(CompanyFilingAmount.EffectiveDate().Descending())) {
            if (!activeFilingAmounts.containsKey(companyFilingAmount.getName())) {
                activeFilingAmounts.put(companyFilingAmount.getName(), companyFilingAmount);
            }
        }
        for (AdditionalFilingAmount additionalFilingAmount : getPaymentTemplate().getAdditionalFilingAmountCollection()) {
            if (activeFilingAmounts.get(additionalFilingAmount.getName()) == null) {
                CompanyFilingAmount companyFilingAmount = new CompanyFilingAmount();
                companyFilingAmount.setCompanyAgencyPaymentTemplate(this);
                companyFilingAmount.setName(additionalFilingAmount.getName());
                companyFilingAmount.setAmount(Double.NaN);
                activeFilingAmounts.put(additionalFilingAmount.getName(), companyFilingAmount);
            }
        }

        return new DomainEntitySet<CompanyFilingAmount>(new HashSet<CompanyFilingAmount>(activeFilingAmounts.values()));
    }

    public CompanyFilingAmount getCompanyFilingAmount(AdditionalFilingAmount filingAmount, SpcfCalendar effectiveDate) {
        return getCompanyFilingAmountCollection()
                .find(CompanyFilingAmount.InvalidDate().isNull()
                                         .And(CompanyFilingAmount.EffectiveDate().lessOrEqualThan(effectiveDate))
                                         .And(CompanyFilingAmount.Name().equalTo(filingAmount.getName())))
                .sort(CompanyFilingAmount.EffectiveDate().Descending())
                .getFirst();
    }

    public CompanyAgencyFormTemplate getFormTemplate() {
        return getCompanyAgency().getFilingForm(getPaymentTemplate());
    }

    public static String getSuiAgencyTaxpayerId(String sourceCompanyId, String state) {
        String agencyTaxpayerId = null;

        DomainEntitySet<CompanyAgencyPaymentTemplate> capts = Application.find(CompanyAgencyPaymentTemplate.class,
                           CompanyAgencyPaymentTemplate.CompanyAgency().Company().SourceCompanyId().equalTo(sourceCompanyId)
                                                       .And(CompanyAgencyPaymentTemplate.PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI))
                                                       .And(CompanyAgencyPaymentTemplate.PaymentTemplate().PaymentTemplateCd().like(state + "-%")));

        if (capts.isNotEmpty()) {
            agencyTaxpayerId = capts.getFirst().getAgencyTaxpayerId();
        }

        return agencyTaxpayerId;
    }

    public void setAgencyTaxpayerId(String pAgencyTaxpayerId) {
        super.setAgencyTaxpayerIdEnc(EncryptionUtils.deterministicEncrypt(AgencyTaxPayerIdKeyName,pAgencyTaxpayerId));
    }


    public String getAgencyTaxpayerId() {
        return EncryptionUtils.deterministicDecrypt(AgencyTaxPayerIdKeyName,getAgencyTaxpayerIdEnc());
    }
}