package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import java.util.Arrays;

import java.util.Collections;
import java.util.List;

/**
 * Hand-written business logic
 */
public class Law extends BaseLaw {
    public static final String FIT    = "1";
    public static final String EEFICA = "61";
    public static final String ERFICA = "62";
    public static final String EEMED  = "63";
    public static final String ERMED  = "64";
    public static final String FUTA   = "65";
    public static final String SUTA   = "66";
    public static final String AEIC = "143";
    public static final String COBRA  = "196";
    public static final String NY_METRO = "197";
    public static final String LAW_177 = "177";
    public static final String LAW_14 = "14";
    public static final String LAW_9 = "926";
    public static final String EEMED_ADDL  = "200";
    public static final String NATIONAL_PAID_LEAVE_CREDIT = "214";
    public static final String EMPLOYEE_RETENTION_CREDIT = "215";
    public static final String FICA_ER_DEFERRAL_CREDIT = "216";
    public static final String FICA_EE_DEFERRAL_CREDIT = "217";
    public static final String CT_PAID_LEAVE = "218";
    public static final String COVID_ADVANCE_CREDIT = "9178";

    // laws that create negative liability for the IRS-941-PAYMENT
    public static final List<String> IRS_CREDIT_LAWS = Arrays.asList(COBRA, NATIONAL_PAID_LEAVE_CREDIT, EMPLOYEE_RETENTION_CREDIT, FICA_ER_DEFERRAL_CREDIT, FICA_EE_DEFERRAL_CREDIT);
    // laws used to track non qbdt liability, these laws do not create a company law and will not be include in liability checks
    public static final List<String> NON_QBDT_LAWS = Collections.singletonList(COVID_ADVANCE_CREDIT);

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public Law()
	{
		super();
	}

    public boolean isFUTA() {
        return FUTA.equals(getLawId()) || SUTA.equals(getLawId());
    }

    public boolean isFIT() {
        return FIT.equals(getLawId());
    }

    public boolean isFICA() {
        return EEFICA.equals(getLawId()) || ERFICA.equals(getLawId()) || FICA_ER_DEFERRAL_CREDIT.equals(getLawId()) || FICA_EE_DEFERRAL_CREDIT.equals(getLawId());
    }

    public boolean isFICA_EE() {
        return EEFICA.equals(getLawId());
    }

    public boolean isFICADeferral() {
        return FICA_ER_DEFERRAL_CREDIT.equals(getLawId()) || FICA_EE_DEFERRAL_CREDIT.equals(getLawId());
    }

    public boolean isMED() {
        return EEMED.equals(getLawId()) || ERMED.equals(getLawId()) || EEMED_ADDL.equals(getLawId());
    }

    public boolean isMED_EE() {
        return EEMED.equals(getLawId()) || EEMED_ADDL.equals(getLawId());
    }
    public boolean isCOBRA() {
        return isCOBRA(getLawId());
    }

    public static boolean isCOBRA(String pLawId) {
        return COBRA.equals(pLawId);
    }

    public boolean isIRSCreditLaw() {
        return IRS_CREDIT_LAWS.contains(getLawId());
    }

	public boolean isAEIC() {
        return AEIC.equals(getLawId());
    }

    public boolean isNY_WH() {
        return "36".equals(getLawId());
    }

    public boolean isNYMetro() {
        return NY_METRO.equals(getLawId());
    }

    public boolean isLaw177() {
        return LAW_177.equals(getLawId());
    }

    public boolean isIA() {
        return LAW_14.equals(getLawId());
    }

    public boolean isAdditionalMedicare() {
        return "200".equals(getLawId());
    }

    public static Law getNYMetroLaw() {
        return Application.findById(Law.class, NY_METRO);
    }

    public static Law getFicaErLaw() {
        return Application.findById(Law.class, ERFICA);
    }

    //"extra" laws that nothing should map to or don't apply anymore
    public boolean shouldExcludeFromUI() {
        return getLawId().equals("65") || getLawId().equals("143");
    }

    public boolean isWithholding() {
        return getPaymentTemplate().getCategory() == PaymentTemplateCategory.Withholding && getLawTypeCd() != null && getLawTypeCd().contains("SWT");
    }

    public boolean isSUIER() {
        return getPaymentTemplate().getCategory() == PaymentTemplateCategory.SUI && getLawTypeCd() != null && getLawTypeCd().contains("SUI-ER");
    }

    public boolean isNMWorkerComp() {
        return getLawId().equals("192");
    }

    public boolean isPrimaryLawForStateID() {
        return isWithholding() || isSUIER() || isSpecialPrimaryLawId();
    }

    public static Law getSuiLaw(String state, LawCategoryCode categoryCode) {

        // Find the SUI law for the given state/category code.
        DomainEntitySet<Law> laws = Application.find(Law.class, Law.LawCategoryCode().equalTo(categoryCode)
                                                                   .And(Law.PaymentTemplate().Category().equalTo(PaymentTemplateCategory.SUI))
                                                                   .And(Law.PaymentTemplate().PaymentTemplateCd().like(state + "-%")));

        // Should just be 1 for most states.  As of Jan. 2013, the following states have more than 1 SUI Supplemental Law:
        //      IA, OR, NJ, RI, ME
        if (laws.size() != 1) {
            return null;
        } else {
            return laws.getFirst();
        }
    }

    public static DomainEntitySet<Law> findWithholdingLawForTemplate(String paymentTemplateCd) {
        return Application.find(Law.class, Law.PaymentTemplate().PaymentTemplateCd().equalTo(paymentTemplateCd)
                                              .And(Law.LawCategoryCode().equalTo(LawCategoryCode.Withholding)));
    }

    public LawRateRange getLawRateRange() {
        return Application.find(LawRateRange.class, LawRateRange.Law().equalTo(this)).getFirst();
    }

    public DomainEntitySet<LawRateValue> getLawRateValues() {
        return Application.find(LawRateValue.class, LawRateValue.Law().equalTo(this)).sort(LawRateValue.Rate());
    }

    public boolean rateIsValidValue(SpcfDecimal rate) {
        if (rate == null) {
            return false;
        }

        DomainEntitySet<LawRateValue> lawRateValues = Application.find(LawRateValue.class,
                                                                       LawRateValue.Law().equalTo(this));
        for (LawRateValue value : lawRateValues) {
            // Make sure the scales are the same, since it looks like trailing zeroes matter.
            SpcfDecimal compareValue = value.getRate().setScale(rate.getScale());
            if (compareValue.equals(rate)) {
                return true;
            }
        }

        // If there were no values defined for this law, the rate is valid.
        return (lawRateValues.size() == 0);
    }

    public boolean rateFallsWithinRange(SpcfDecimal rate) {
        if (rate == null) {
            return false;
        }

        DomainEntitySet<LawRateRange> lawRateRanges = Application.find(LawRateRange.class,
                                                                       LawRateRange.Law().equalTo(this));
        // There should only be one valid range, but the database does not enforce it.
        for (LawRateRange range : lawRateRanges) {
            // If the min/max are defined, make sure the rate falls between them.
            if ((range.getMinRate() == null || range.getMinRate().isLessThanEqualTo(rate)) &&
                    (range.getMaxRate() == null || range.getMaxRate().isGreaterThanEqualTo(rate))) {
                return true;
            }
        }

        // If there were no ranges defined for this law, the rate is valid.
        return (lawRateRanges.size() == 0);
    }
    public boolean isSpecialPrimaryLawId() {

        switch (getLawId()) {
            // NW Workers Comp
            case "192":
            case NY_METRO:
            //MA LAWIDS
            case "206":
            case "207":
            case "209":
            case "210":
            //WA LAWIDS
            case "211":
            case "212":
            case "218":
            case "208":
            case "221":
            case "222":
            case "223":
            case "224":
            case "225":
            case "226":
                return true;
            default:
                return false;
        }
    }

}

