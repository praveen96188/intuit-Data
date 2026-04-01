package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.gateways.salestax.SalesTaxGSTAndEOSComparisonImpl;
import com.intuit.sbd.payroll.psp.gateways.salestax.ISalesTaxGateway;
import com.intuit.sbd.payroll.psp.gateways.salestax.SalesTaxGSTImpl;
import com.intuit.sbd.payroll.psp.gateways.salestax.SalesTaxGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.salestax.dto.*;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hand-written business logic
 */
public class BillingDetail extends BaseBillingDetail {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(BillingDetail.class);
    private static final SpcfDecimal SCALE = SpcfDecimal.createInstance("10000.0");
    private static final String BILLING_DETAILS_IN_MEMORY_CACHE_KEY = "BillingDetailsInMemory";

    //
    // Regex pattern to match Memo field values for ExtraStateFee
    // (used to determine which states have already been billed in a given billing period)
    //
    // Group 1 = State (two-letter state postal code - see BILLABLE_STATES)
    // Group 2 = Month (three-letter month abbreviation - see MONTHS)
    // Group 3 = Year  (four-digit year)
    //
    private static final Pattern STATE_FEE_MEMO_PATTERN = Pattern.compile("^(?:No state|State processing) fee for ([A-Za-z]{2}) for ([A-Za-z]{3}) ([0-9]{4})$");

    //
    // Month values that can appear in Memo fields
    //
    public static final String[] MONTHS = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};

    //
    // Valid state postal codes that are 'billable' in terms of ExtraStateFee (also can appear in Memo fields)
    // (all 50 states plus DC - notably this list does not include US territories)
    //
    private static final List<String> BILLABLE_STATES = Arrays.asList("AK","AL","AR","AZ","CA","CO","CT","DE","FL","GA",
                                                                      "HI","IA","ID","IL","IN","KS","KY","LA","MA","MD",
                                                                      "ME","MI","MN","MO","MS","MT","NC","ND","NE","NH",
                                                                      "NJ","NM","NV","NY","OH","OK","OR","PA","RI","SC",
                                                                      "SD","TN","TX","UT","VA","VT","WA","WI","WV","WY","DC");

    //
    // These states do not require sales tax for the Employee Organizer fee
    //
    private static final List<String> EO_TAX_EXEMPT_STATES = Arrays.asList("AK","DE","MT","NH","OR");

    public static class MEMOS {
        public static final String LEGAL_ENTITY_CHANGE_FEE = "Legal entity change fee";
        public static final String NO_FEE_FOR_DIRECT_DEPOSIT = "No fee for %d direct deposit(s)";
        public static final String INCLUDES_DISCOUNT = "(includes $%s discount)";
        public static final String FEE_FOR_DIRECT_DEPOSIT_AT_EACH = "Fee for %d direct deposit(s) at $%s each";
        public static final String FEE_FOR_DIRECT_DEPOSIT_AT_EACH_PLUS_PER_PAY_PERIOD = FEE_FOR_DIRECT_DEPOSIT_AT_EACH + " plus $%s per pay period";
        public static final String FEE_FOR_DIRECT_DEPOSIT_AT_PER_PAY_PERIOD = "Fee for %d direct deposit(s) at $%s per pay period";
        public static final String NO_FEE_FOR_EMPLOYEE_PAID = "No fee for %d employee(s) paid";
        public static final String FEE_FOR_EMPLOYEE_PAID = "Fee for %d employee(s) paid";
        public static final String NO_STATE_FEE = "No state fee for %s for %s %d";
        public static final String STATE_PROCESSING_FEE = "State processing fee for %s for %s %d";
        public static final String NO_MONTHLY_FEE = "No monthly fee for %s %d";
        public static final String MONTHLY_PROCESSING_FEE = "Monthly processing fee for %s %d";
        public static final String PAYROLL_FEE = "Payroll fee";
        public static final String DIRECT_DEPOSIT_TRANSMISSION_FEE = "Direct Deposit Transmission Fee";
        public static final String BACK_DATE_FEE = "Payroll not sent 2 days in advance";
        public static final String ENROLLMENT_FEE = "Enrollment Fee";
        public static final String ENROLLMENT_FEE_REFUND = "Enrollment fee refund";
        public static final String NSF_FEE_FOR_RETURNED_PAYROLL = "NSF fee for returned payroll";
        public static final String FEE_PER_W2 = "Fee per W2";
        public static final String BASE_W2_FEE = "W2 Processing Setup";
        public static final String AMENDED_RETURN_SS_CORRECTION_FEE = "Amended return - SS# correction fee";
        public static final String AMENDED_RETURN_FEE = "Amended return fee";
        public static final String DIRECT_DEPOSIT_REVERSAL_FEE = "Direct Deposit reversal fee";
        public static final String ADDITIONAL_COPY_OF_RETURN_FEE = "Additional copy of return fee";
        public static final String PRIOR_PAYROLL_ADJUSTMENT_FEE = "Prior payroll adjustment fee";
        public static final String CORRECT_W_2_FEE = "Correct W-2 fee";
        public static final String PAYMENT_ARRANGEMENT_FEE = "Payment Arrangement Fee";
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders and counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public DomainEntitySet<BillingDetail> findBillingDetailsAssociatedWithTheSameMMT() {
        DomainEntitySet<BillingDetail> billingDetails = new DomainEntitySet<BillingDetail>();
        if(getFeeTransaction() != null && getFeeTransaction().getMoneyMovementTransaction() != null) {
            for (FinancialTransaction financialTransaction : getFeeTransaction().getMoneyMovementTransaction().getFinancialTransactionCollection()) {
                if(financialTransaction.getBillingDetail() != null) {
                    billingDetails.add(financialTransaction.getBillingDetail());
                }
            }
        }

        return billingDetails;
    }

    /**
     * Searches historical BillingDetails for the given company and SKU looking for the most recent one that has
     * a non-zero sales tax amount.  It returns the most recent one, or null if none exist.
     *
     * @param pCompany
     * @param pSKU
     * @return a BillingDetail whose TaxAmount is > 0.0, or null
     */
    public static BillingDetail findLastNonZeroSalesTaxRate(final Company pCompany, String pSKU) {
        String[] names = new String[2];
        names[0] = "company";
        names[1] = "sku";

        Object[] values = new Object[2];
        values[0] = pCompany;
        values[1] = pSKU;

        DomainEntitySet<BillingDetail> found = Application.findByNamedQuery("findLastNonZeroSalesTaxRate", names, values, 0, 1);
        if (found == null || found.size() == 0) {
            return null;
        } else {
            return found.get(0);
        }
    }

    public static DomainEntitySet<BillingDetail> getBillingDetailsInCache(Company pCompany) {
        DomainEntitySet<BillingDetail> billingDetailsInMemory = Application.getSessionCache().getNonHibernateObject(BILLING_DETAILS_IN_MEMORY_CACHE_KEY + ":" + pCompany.getId());

        if (billingDetailsInMemory == null) {
            billingDetailsInMemory = new DomainEntitySet<BillingDetail>();
            Application.getSessionCache().addNonHibernateObject(BILLING_DETAILS_IN_MEMORY_CACHE_KEY + ":" + pCompany.getId(), billingDetailsInMemory);
        }

        return billingDetailsInMemory;

    }

    /**
     * Finds BillingDetails for the given OfferingServiceChargeTypes, related to the instance's PayrollRun.
     *
     * @param pPayrollRun The payroll run that owns the BillingDetails to search for
     * @param pChargeTypes The OfferingServiceChargeType(s) to look for
     * @return A list of matching BillingDetail entities
     */
    public static DomainEntitySet<BillingDetail> findBillingDetails(PayrollRun pPayrollRun, OfferingServiceChargeType... pChargeTypes) {
        Criterion<BillingDetail> where = PayrollRun().equalTo(pPayrollRun);

        if (pChargeTypes.length > 0) {
            where = where.And(BillingDetail.OfferingServiceChargeType().in(pChargeTypes));
        }

        DomainEntitySet<BillingDetail> bdSet = Application.find(BillingDetail.class, where);

        if (bdSet.isEmpty()) {
            bdSet = getBillingDetailsInCache(pPayrollRun.getCompany()).find(where);
        }

        return bdSet;
    }

    @SuppressWarnings({"unchecked", "JpaQlInspection"})
    public static DomainEntitySet<BillingDetail> findBillingDetailsInDateRange(Company pCompany, SpcfCalendar pFromDate, SpcfCalendar pToDate, OfferingServiceChargeType... pChargeTypes) {
        Criterion<BillingDetail> where = BillingDetail.BillingPeriod().between(pFromDate, pToDate).And(BillingDetail.PayrollRun().Company().equalTo(pCompany));

        if (pChargeTypes.length > 0) {
            where = where.And(BillingDetail.OfferingServiceChargeType().in(pChargeTypes));
        }

        Query<BillingDetail> query = new Query<BillingDetail>();

        query.Where(where)
             .OrderBy(BillingDetail.BillingPeriod(),
                      BillingDetail.OfferingServiceChargeType())
             .EagerLoad(BillingDetail.PayrollRun());

        DomainEntitySet<BillingDetail> bdSet = Application.find(BillingDetail.class, query);
        bdSet.addAll(getBillingDetailsInCache(pCompany).find(query));

        return bdSet;
    }

    public static DomainEntitySet<BillingDetail> findBillingDetailsInBillingPeriod(Company pCompany, SpcfCalendar pBillingPeriod, OfferingServiceChargeType... pChargeTypes) {
        return findBillingDetailsInDateRange(pCompany, CalendarUtils.getFirstDayOfMonth(pBillingPeriod), CalendarUtils.getLastDayOfMonth(pBillingPeriod), pChargeTypes);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Static create/update
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static DomainEntitySet<BillingDetail> createBillingDetail(PayrollRun pPayrollRun,
                                                     OfferingServiceChargeType pChargeType,
                                                     int pQuantity,
                                                     BigDecimal pUnitPriceOverride,
                                                     OfferingCode pOfferingCode,
                                                     boolean pApplyOffers,
                                                     boolean pCalcSalesTax) {
        DomainEntitySet<BillingDetail> billingDetails = new DomainEntitySet<BillingDetail>();

        //
        // Quantity cannot be negative
        //
        if (pQuantity < 0) {
            throw new IllegalArgumentException("The quantity must be a non-zero, positive number.");
        }

        //
        // Unit price override, when present, cannot be negative
        //
        if ((pUnitPriceOverride != null) && (pUnitPriceOverride.compareTo(BigDecimal.ZERO) < 0)) {
            throw new IllegalArgumentException("The overridden unit-price cannot be less than zero.");
        }

        //
        // Get the OfferingServiceChargeGroup for company offering
        //
        OfferingServiceChargeGroup group = OfferingServiceChargeGroup.getOfferingServiceChargeGroup(pPayrollRun.getCompany(), pOfferingCode, pChargeType);

        //
        // Charge type not defined for this offering, so just return
        //
        if (group == null) {
            return billingDetails;
        }

        DomainEntitySet<OfferingServiceCharge> charges = group.selectTiers();

        int billedQuantity = 0;
        for (OfferingServiceCharge charge : charges) {
            int adjustedQuantity = 0;
            if (charge.getIsTier()) {
                int adjustedTierUnits = charge.getTierUnits() > 0 ? charge.getTierUnits() - 1 : charge.getTierUnits();
                adjustedQuantity = pQuantity - billedQuantity - adjustedTierUnits;
            } else {
                adjustedQuantity = pQuantity;
            }

            if (adjustedQuantity <= 0) {
                continue;
            }

            // create a new domain entity
            BillingDetail detail = new BillingDetail();
            detail.setPayrollRun(pPayrollRun);
            detail.setServiceDate(PSPDate.getPSPTime());
            detail.setOfferingServiceChargeType(pChargeType);
            detail.setQuantity(adjustedQuantity);
            detail.setItemName(group.getName());
            detail.setItemSku(charge.getSKU());
            detail.setBillingPeriod(pPayrollRun.getPaycheckDate());
            detail.setOfferCd(null);
            detail.setOfferName(null);
            detail.setDiscountAmount(SpcfMoney.ZERO);
            detail.setTaxComputedDate(null);
            detail.setTaxJurisdiction(null);
            detail.setTaxAmount(SpcfMoney.ZERO);

            // if we got a specific unit price, use it... else look it up
            if (pUnitPriceOverride != null) {
                detail.setOfferingServiceChargePrice(null);
                detail.setBasePrice(SpcfMoney.ZERO);
                detail.setUnitPrice(SpcfUtils.convertToSpcfMoney(pUnitPriceOverride));
            } else {
                OfferingServiceChargePrice price = charge.getCurrentPrice();

                if (price != null) {
                    detail.setOfferingServiceChargePrice(price);
                    detail.setBasePrice(price.getBasePrice());
                    detail.setUnitPrice(price.getUnitPrice());
                } else {
                    logger.error("No current price configured for Charge " + charge.getId());
                    detail.setOfferingServiceChargePrice(null);
                    detail.setBasePrice(SpcfMoney.ZERO);
                    detail.setUnitPrice(SpcfMoney.ZERO);
                }
            }

            //
            // Calculate the total fees
            //
            SpcfMoney totalFees = detail.calcTierAdjustedFees();

            detail.setItemTotal(totalFees);

            //
            // Apply relevant Offer(s) if requested
            // (any discounts must be applied before sales tax is calculated)
            //
            if (pApplyOffers) {
                detail.applyOffers();
            }

            //
            // Calc sales tax if requested
            //
            if (pCalcSalesTax) {
                detail.computeSalesTax();
            }

            // in some cases billing detail is unwanted
            if (pOfferingCode == OfferingCode.UsageBilling && (pChargeType == OfferingServiceChargeType.DirectDepositFee || pChargeType == OfferingServiceChargeType.PerTransmission) && totalFees.isZero()) {
                //Do nothing
            } else {
                detail = Application.save(detail);
                pPayrollRun.addBillingDetail(detail);
                getBillingDetailsInCache(pPayrollRun.getCompany()).add(detail);
                billingDetails.add(detail);
            }

            billedQuantity += detail.getQuantity();
        }

        return billingDetails;
    }

    /**
     * Adds a fee and applies any applicable offers.
     * <p/>
     * Clients should call save() to compute sales tax (when necessary) and create financial transactions.
     * Settlement will be by ACH.
     * <p/>
     * NOTE: the returned BillingDetail instance is fresh when returned, but may become stale at any time after.
     * If there is any doubt, requery it by ID before using it.
     *
     * @param pChargeType OfferingServiceChargeType
     * @param pQuantity   Quantity
     * @return the BillingDetail instance representing the fee
     */
    public static DomainEntitySet<BillingDetail> createBillingDetail(PayrollRun pPayrollRun,
                                                    CompanyBankAccount pCompanyBankAccount,
                                                    OfferingServiceChargeType pChargeType,
                                                    int pQuantity,
                                                    OfferingCode pOfferingCode) {
        return createBillingDetail(pPayrollRun, pCompanyBankAccount, pChargeType, pQuantity, null, pOfferingCode); // no settlement date
    }

    public static DomainEntitySet<BillingDetail> createBillingDetail(PayrollRun pPayrollRun,
                                                    CompanyBankAccount pCompanyBankAccount,
                                                    OfferingServiceChargeType pChargeType,
                                                    int pQuantity,
                                                    SpcfCalendar pSettlementDate,
                                                    OfferingCode pOfferingCode) {
        DomainEntitySet<BillingDetail> response = new DomainEntitySet<BillingDetail>();

        DomainEntitySet<BillingDetail> billingDetails = createBillingDetail(pPayrollRun, pChargeType, pQuantity, null, pOfferingCode, true, pPayrollRun.calculateSalesTax());
        for (BillingDetail billingDetail : billingDetails) {
            billingDetail.createFinancialTransactionsForBillingDetail(pCompanyBankAccount, SettlementType.ACH, pSettlementDate, pOfferingCode, null);

            //
            // Finally set the Memo field
            //
            billingDetail.setDefaultMemoForFeeType(null);

            response.add(Application.save(billingDetail)) ;
        }

        return response;
    }

    /**
     * Adds a fee with optional unit-price override and applies any applicable offers.  If pUnitPrice is non-null, this
     * method uses that unit price in place of the configured price.  Otherwise, it looks up the configured price.
     * <p/>
     * Clients should call save() to compute sales tax (when necessary) and create financial transactions.
     * Settlement will be by ACH.
     * <p/>
     * NOTE: the returned BillingDetail instance is fresh when returned, but may become stale at any time after.
     * If there is any doubt, requery it by ID before using it.
     *
     * @param pChargeType
     * @param pQuantity
     * @param pUnitPriceOverride null means use configured pricing
     * @return the BillingDetail instance representing the fee
     */
    public static DomainEntitySet<BillingDetail> createBillingDetailWithPriceOverride(PayrollRun pPayrollRun,
                                                                     CompanyBankAccount pCompanyBankAccount,
                                                                     OfferingServiceChargeType pChargeType,
                                                                     int pQuantity,
                                                                     BigDecimal pUnitPriceOverride,
                                                                     OfferingCode pOfferingCode,
                                                                     String pMemo) {
        return createBillingDetailWithPriceAndSettlementDateOverride(pPayrollRun, pCompanyBankAccount, pChargeType, pQuantity, pUnitPriceOverride, null, pOfferingCode, pMemo);
    }

    public static DomainEntitySet<BillingDetail> createBillingDetailWithPriceAndSettlementDateOverride(PayrollRun pPayrollRun,
                                                                                      CompanyBankAccount pCompanyBankAccount,
                                                                                      OfferingServiceChargeType pChargeType,
                                                                                      int pQuantity,
                                                                                      BigDecimal pUnitPriceOverride,
                                                                                      SpcfCalendar pSettlementDate,
                                                                                      OfferingCode pOfferingCode,
                                                                                      String pMemo) {
        DomainEntitySet<BillingDetail> response = new DomainEntitySet<BillingDetail>();

        DomainEntitySet<BillingDetail> billingDetails = createBillingDetail(pPayrollRun, pChargeType, pQuantity, pUnitPriceOverride, pOfferingCode, true, pPayrollRun.calculateSalesTax());
        for (BillingDetail billingDetail : billingDetails) {
            billingDetail.createFinancialTransactionsForBillingDetail(pCompanyBankAccount, SettlementType.ACH, pSettlementDate, pOfferingCode, null);

            //
            // Finally set the Memo field
            //
            billingDetail.setDefaultMemoForFeeType(pMemo);

            response.add(Application.save(billingDetail)) ;
        }

        return response;
    }

    /**
     * Changes the quantity for the BillingDetail entity with the given OfferingServiceChargeType, if such a
     * BillingDetail exists that has not already been offloaded.
     *
     * @param pChargeType OfferingServiceChargeType
     * @param pQuantity   the new quantity... may be zero to remove the charge completely
     */
    public static void updateBillingDetail(PayrollRun pPayrollRun,
                                           CompanyBankAccount pCompanyBankAccount,
                                           OfferingServiceChargeType pChargeType,
                                           int pQuantity,
                                           OfferingCode pOfferingCode) {
        // quantity cannot be negative
        if (pQuantity < 0) {
            throw new RuntimeException("The quantity must be a positive number.");
        }

        // look for a BillingDetail for the given charge-type that can be updated
        SpcfCalendar feeSettlementDate = null;
        Map<OfferingServiceCharge, BillingDetail> billingDetails = new HashMap<OfferingServiceCharge, BillingDetail>();
        for (BillingDetail candidate : findBillingDetails(pPayrollRun, pChargeType).sort(BillingDetail.OfferingServiceChargePrice().OfferingServiceCharge().TierNumber().Descending())) {
            if (candidate.isUpdateable()) {
                feeSettlementDate = candidate.getFeeTransaction().getSettlementDate().toLocal();
                billingDetails.put(candidate.getOfferingServiceChargePrice().getOfferingServiceCharge(), candidate);
            }
        }

        // fail if no updateable detail was found
        if (billingDetails.isEmpty()) {
            String msg = "Attempt to update a '" + pChargeType + "' charge not yet added to payrollRun " + pPayrollRun.getId();
            logger.warn(msg);
            return;
        }

        // changing the quantity may change the price tier, when pricing is tiered, so we look up SKU and price again
        OfferingServiceChargeGroup group = null;
        if (pOfferingCode != null) {
            group = OfferingServiceChargeGroup.findOfferingServiceChargeGroup(pCompanyBankAccount.getCompany(), pOfferingCode, pChargeType);
        } else {
            group = OfferingServiceChargeGroup.findFirstOfferingServiceChargeGroup(pCompanyBankAccount.getCompany(), pChargeType);
        }

        int billedQuantity = 0;
        DomainEntitySet<OfferingServiceCharge> charges = group.selectTiers();
        for (OfferingServiceCharge charge : charges) {

            BillingDetail billingDetail = null;
            if (billingDetails.containsKey(charge)) {
                billingDetail = billingDetails.get(charge);

                //Cancel Fee Financial Transaction (save its settlement date first... PSRV000961)
                FinancialTransaction ftFee = billingDetail.getFeeTransaction();
                if (ftFee != null) {
                    feeSettlementDate = ftFee.getSettlementDate().toLocal();
                    ftFee.cancelFinancialTransaction();
                    billingDetail.removeFinancialTransaction(ftFee);
                    ftFee.setBillingDetail(null);
                }

                //Cancel Tax Financial Transaction
                FinancialTransaction ftTax = billingDetail.getTaxTransaction();
                if (ftTax != null) {
                    ftTax.cancelFinancialTransaction();
                    billingDetail.removeFinancialTransaction(ftTax);
                    ftTax.setBillingDetail(null);
                }
            } else {
                billingDetail = new BillingDetail();
                billingDetail.setPayrollRun(pPayrollRun);
                billingDetail.setOfferingServiceChargeType(pChargeType);
                billingDetail.setServiceDate(PSPDate.getPSPTime());
            }

            int adjustedQuantity = 0;
            if (charge.getIsTier()) {
                int adjustedTierUnits = charge.getTierUnits() > 0 ? charge.getTierUnits() - 1 : charge.getTierUnits();
                adjustedQuantity = pQuantity - billedQuantity - adjustedTierUnits;

                adjustedQuantity = adjustedQuantity < 0 ? 0 : adjustedQuantity;
            } else {
                adjustedQuantity = pQuantity;
            }

            billingDetail.setQuantity(adjustedQuantity);
            billingDetail.setItemName(group.getName());
            billingDetail.setItemSku(charge.getSKU());
            billingDetail.setBillingPeriod(pPayrollRun.getPaycheckDate());

            OfferingServiceChargePrice price = charge.getCurrentPrice();

            if (price != null) {
                billingDetail.setOfferingServiceChargePrice(price);
                billingDetail.setBasePrice(price.getBasePrice());
                billingDetail.setUnitPrice(price.getUnitPrice());
            } else {
                logger.error("No current price configured for Charge " + charge.getId());
                billingDetail.setOfferingServiceChargePrice(null);
                billingDetail.setBasePrice(SpcfMoney.ZERO);
                billingDetail.setUnitPrice(SpcfMoney.ZERO);
            }

            //
            // Calculate the total fees
            //
            SpcfMoney totalFees = billingDetail.calcTierAdjustedFees();

            billingDetail.setItemTotal(totalFees);

            //
            // Apply relevant Offer(s)
            // (any discounts must be applied before sales tax is calculated)
            //
            billingDetail.applyOffers();

            //
            // Calc sales tax
            //
            billingDetail.computeSalesTax();

            billingDetail = Application.save(billingDetail);

            if(adjustedQuantity > 0) {
                billingDetail.createFinancialTransactionsForBillingDetail(pCompanyBankAccount, SettlementType.ACH, feeSettlementDate, pOfferingCode, null);
            }

            //
            // Finally set the Memo field
            //
            billingDetail.setDefaultMemoForFeeType(null);

            Application.save(billingDetail);

            billedQuantity += billingDetail.getQuantity();
        }
    }

    /**
     * Adds a fee for non-ACH settlement.  No offers are applied and sales tax is never computed.  The financial
     * transaction is saved immediately.
     * <p/>
     * NOTE: the returned BillingDetail instance is fresh when returned, but may become stale at any time after.
     * If there is any doubt, requery it by ID before using it.
     *
     * @param pChargeType
     * @param pQuantity
     * @param pTotalAmount
     * @param pSettlementType
     * @param pSettlementDate if null, settlement date is computed based on DD offload group
     * @return the BillingDetail instance representing the fee
     */
    public static DomainEntitySet<BillingDetail> createNonACHFee(PayrollRun pPayrollRun,
                                                CompanyBankAccount pCompanyBankAccount,
                                                OfferingServiceChargeType pChargeType,
                                                int pQuantity,
                                                BigDecimal pTotalAmount,
                                                SettlementType pSettlementType,
                                                SpcfCalendar pSettlementDate) {
        // the settlement type cannot be ACH
        if (pSettlementType == null || pSettlementType == SettlementType.ACH) {
            throw new IllegalArgumentException("The settlement type cannot be ACH.");
        }

        DomainEntitySet<BillingDetail> response = new DomainEntitySet<BillingDetail>();

        // create the billing detail with configured unit price
        DomainEntitySet<BillingDetail> billingDetails = createBillingDetail(pPayrollRun, pChargeType, pQuantity, null, null, false, false);
        for (BillingDetail billingDetail : billingDetails) {
            // replace the unit price with an APPROXIMATE one based on the input total and quantity
            BigDecimal unitPrice = pTotalAmount.divide(new BigDecimal(pQuantity));
            billingDetail.setUnitPrice(SpcfUtils.convertToSpcfMoney(unitPrice));
            billingDetail.setBasePrice(SpcfMoney.ZERO);
            billingDetail.setOfferingServiceChargePrice(null);

            // don't apply offers
            billingDetail.setOfferCd(null);
            billingDetail.setOfferName(null);
            billingDetail.setDiscountAmount(SpcfMoney.ZERO);

            // don't compute sales tax - but make sure this BillingDetail will show up in the Sales Tax Exception Report
            billingDetail.setTaxAmount(SpcfMoney.ZERO);
            billingDetail.setTaxExceptionInd(true);

            // create the financial transactions
            billingDetail.createFinancialTransactionsForBillingDetail(pCompanyBankAccount, pSettlementType, pSettlementDate, null, pTotalAmount);

            // save it
            response.add(Application.save(billingDetail));
        }

        return response;
    }

    public static void createExtraStateFeesForPayrollRunIfMeetsCriteria(PayrollRun pPayrollRun,
                                                                        CompanyBankAccount pCompanyBankAccount,
                                                                        SpcfCalendar pSettlementDate,
                                                                        OfferingCode pOfferingCode) {
        //
        // If no payroll run, then we're done.
        //
        if (pPayrollRun == null || !pPayrollRun.hasImpoundDebit()) {
            return;
        }

        // no billing for fees that were already billed
        if(pPayrollRun.getCompany().getSourceSystemCd() == SourceSystemCode.QBDT &&
                pPayrollRun.getPaycheckDate().before(SystemParameter.findCalendarValue(SystemParameter.Code.QBDT_MONTHLY_BILLING_START_DATE))) {
            return;
        }

        //
        // Charge type not defined for this offering, so just return
        //
        if (!OfferingServiceChargeGroup.isChargeTypeValidForOffering(pPayrollRun.getCompany(), pOfferingCode, OfferingServiceChargeType.ExtraStateFee)) {
            return;
        }

        //
        // Retrieve list of distinct states having tax transactions for this payroll run
        //
        Set<String> pendingStateList = pPayrollRun.getStatesForTaxPayments();

        //
        // If no states to bill, then we're done.
        //
        if (pendingStateList.isEmpty()) {
            return;
        }

        //
        // Get list of all previously billed ExtraStateFees for the given billing period
        //
        DomainEntitySet<BillingDetail> bdSet = findBillingDetailsInBillingPeriod(pPayrollRun.getCompany(), pPayrollRun.getPaycheckDate(), OfferingServiceChargeType.ExtraStateFee);

        //
        // Iterate over all previously billed ExtraStateFees for the given billing period and save the states that we already billed for
        //
        Set<String> alreadyBilledStateList = new TreeSet<String>();

        for (BillingDetail detail : bdSet) {
            if (detail.isViable()) {
                String state = detail.extractStateFromMemoField();

                if (state != null) {
                    alreadyBilledStateList.add(state.toUpperCase());
                }
            }
        }

        for (String st : pendingStateList) {
            String state = st.toUpperCase();

            //
            // If the given state is billable and we haven't already billed for that state in the given billing period, then assess the ExtraStateFee for the state
            //

            if (!alreadyBilledStateList.contains(state) && BILLABLE_STATES.contains(state)) {
                DomainEntitySet<BillingDetail> details;

                //
                // If alreadyBilledStateList is empty, then assess the fee at $0.00 (the first state is free)
                // (still need SKU for GEMS reporting, even if $0)
                //
                
                if (alreadyBilledStateList.isEmpty()) {
                    details = createBillingDetail(pPayrollRun, OfferingServiceChargeType.ExtraStateFee, 1, BigDecimal.ZERO, pOfferingCode, false, false);
                } else {
                    details = createBillingDetail(pPayrollRun, OfferingServiceChargeType.ExtraStateFee, 1, null, pOfferingCode, true, pPayrollRun.calculateSalesTax());
                }

                for (BillingDetail detail : details) {
                    detail.createFinancialTransactionsForBillingDetail(pCompanyBankAccount, SettlementType.ACH, pSettlementDate, pOfferingCode, null);

                    //
                    // Finally set the Memo field for the given state fee
                    //
                    detail.setDefaultMemoForFeeType(state);

                    Application.save(detail);

                    //
                    // Record state as having been billed for later iterations
                    //
                    alreadyBilledStateList.add(state);
                }
            }
        }
    }

    public static void createMonthlyFeeForPayrollRunIfMeetsCriteria(PayrollRun pPayrollRun,
                                                                    CompanyBankAccount pCompanyBankAccount,
                                                                    SpcfCalendar pSettlementDate,
                                                                    OfferingCode pOfferingCode,
                                                                    boolean allowNoImpounds) {
        // only charge monthly fee if we are impounding tax or dd money
        if(!pPayrollRun.hasImpoundDebit() && !allowNoImpounds) {
            return;
        }

        // no billing for fees that were already billed
        if(pPayrollRun.getCompany().getSourceSystemCd() == SourceSystemCode.QBDT &&
                pPayrollRun.getPaycheckDate().before(SystemParameter.findCalendarValue(SystemParameter.Code.QBDT_MONTHLY_BILLING_START_DATE))) {
            return;
        }

        //
        // Charge type not defined for this offering, so just return
        //
        if (!OfferingServiceChargeGroup.isChargeTypeValidForOffering(pPayrollRun.getCompany(), pOfferingCode, OfferingServiceChargeType.MonthlyFee)) {
            return;
        }

        //
        // Check to ensure we haven't already billed the fee in the given billing period
        //
        boolean alreadyBilledForPeriod = false;
        DomainEntitySet<BillingDetail> bdSet = findBillingDetailsInBillingPeriod(pPayrollRun.getCompany(), pPayrollRun.getPaycheckDate(), OfferingServiceChargeType.MonthlyFee);

        for (BillingDetail detail : bdSet) {
            if (detail.isViable()) {
                alreadyBilledForPeriod = true;
                break;
            }
        }

        if (!alreadyBilledForPeriod) {
            DomainEntitySet<BillingDetail> details = createBillingDetail(pPayrollRun, OfferingServiceChargeType.MonthlyFee, 1, null, pOfferingCode, true, pPayrollRun.calculateSalesTax());

            for (BillingDetail detail : details) {
                detail.createFinancialTransactionsForBillingDetail(pCompanyBankAccount, SettlementType.ACH, pSettlementDate, pOfferingCode, null);

                //
                // Finally set the Memo field
                //
                detail.setDefaultMemoForFeeType(null);

                Application.save(detail);
            }
        }
    }

    public static DomainEntitySet<BillingDetail> createOtherFeeWithCustomMemo(PayrollRun pPayrollRun,
                                                             BigDecimal pFeeAmount,
                                                             int pQuantity,
                                                             CompanyBankAccount pCompanyBankAccount,
                                                             SpcfCalendar pSettlementDate,
                                                             OfferingCode pOfferingCode,
                                                             String pMemo) {
        //
        // Charge type not defined for this offering, so just return
        //
        if (!OfferingServiceChargeGroup.isChargeTypeValidForOffering(pPayrollRun.getCompany(), pOfferingCode, OfferingServiceChargeType.OtherFee)) {
            return null;
        }

        DomainEntitySet<BillingDetail> response = new DomainEntitySet<BillingDetail>();

        DomainEntitySet<BillingDetail> billingDetails = createBillingDetail(pPayrollRun, OfferingServiceChargeType.OtherFee, pQuantity, pFeeAmount, pOfferingCode, true, pPayrollRun.calculateSalesTax());
        for (BillingDetail billingDetail : billingDetails) {
            billingDetail.createFinancialTransactionsForBillingDetail(pCompanyBankAccount, SettlementType.ACH, pSettlementDate, pOfferingCode, null);

            //
            // Finally set the Memo field
            //
            billingDetail.setDefaultMemoForFeeType(pMemo);

            response.add(Application.save(billingDetail));
        }

        return response;
    }

    public static SalesTaxRequest createSalesTaxRequest(PayrollRun pPayrollRun) {
        Company company = pPayrollRun.getCompany();

        SalesTaxRequest taxRequest = new SalesTaxRequest();
        taxRequest.setDocumentId(pPayrollRun.getSourcePayRunId());
        taxRequest.setDocumentDateTime(Calendar.getInstance());
        taxRequest.setCompanyName(company.getLegalName());
        taxRequest.setAddressLine1(company.getLegalAddress().getAddressLine1());

        if (company.getLegalAddress().getAddressLine2() != null) {
            taxRequest.setAddressLine2(company.getLegalAddress().getAddressLine2());
        } else {
            taxRequest.setAddressLine2("");
        }

        if (company.getLegalAddress().getAddressLine3() != null) {
            taxRequest.setAddressLine3(company.getLegalAddress().getAddressLine3());
        } else {
            taxRequest.setAddressLine3("");
        }

        taxRequest.setCity(company.getLegalAddress().getCity());

        if (company.getLegalAddress().getCountry() != null) {
            taxRequest.setCountry(company.getLegalAddress().getCountry());
        } else {
            taxRequest.setCountry("");
        }

        taxRequest.setZipCode(company.getLegalAddress().getZipCode());
        taxRequest.setState(company.getLegalAddress().getState());
        taxRequest.setFirstName("");
        taxRequest.setLastName("");
        taxRequest.setEmail("");
        taxRequest.setPhoneNumber("");

        return taxRequest;
    }

    /**
     * Public method to get sales tax for an SpcfList of BillingDetail objects.
     *
     * @param pPayrollRun PayrollRun
     * @param pDetails    DomainEntitySet<BillingDetail>
     * @return SalesTaxResponse
     */
    public static SalesTaxResponse getSalesTaxInfo(PayrollRun pPayrollRun, DomainEntitySet<BillingDetail> pDetails) {
        SalesTaxRequest taxRequest = createSalesTaxRequest(pPayrollRun);

        for (BillingDetail billingDetail : pDetails) {
            taxRequest.addLine(build_SalesTaxRequestLine(billingDetail));
        }

        //todo - Retrieve this flag from IXP and give a proper name
        String salesTaxGatewayImplementationClassFlag = FeatureFlags.get().stringValue(FeatureFlags.Key.SALESTAX_GATEWAY_IMPL,"SalesTaxEOSImpl");
        ISalesTaxGateway taxGateway;

        switch (salesTaxGatewayImplementationClassFlag) {
            case "SalesTaxGSTImpl":
                taxGateway = PayrollApplicationBeanFactory.getBean(SalesTaxGSTImpl.class);
                break;
            case "SalesTaxGSTAndEOSComparisonImpl":
                taxGateway = PayrollApplicationBeanFactory.getBean(SalesTaxGSTAndEOSComparisonImpl.class);
                break;
            //In case of any issues we will switch to original implementation
            default:
                String salesTaxGatewayImplementationClass = SystemParameter.findStringValue(SystemParameter.Code.SALES_TAX_GATEWAY_IMPLEMENTATION_CLASS, null);
                taxGateway = SalesTaxGatewayFactory.createISalesTaxGateway(salesTaxGatewayImplementationClass);
                break;
        }
        return taxGateway.send(taxRequest);
    }

    /**
     * Private method to get sales tax for a collection of BillingDetail objects.
     *
     * @return SalesTaxRequest
     */
    private SalesTaxResponse getSalesTaxInfo() {
        DomainEntitySet<BillingDetail> billingDetails = new DomainEntitySet<BillingDetail>();
        billingDetails.add(this);
        return getSalesTaxInfo(getPayrollRun(), billingDetails);
    }

    /**
     * Builds a "line" in the sales-tax request
     *
     * @param pBillingDetail BillingDetail
     * @return SalesTaxRequestLine
     */
    private static SalesTaxRequestLine build_SalesTaxRequestLine(BillingDetail pBillingDetail) {
        SalesTaxRequestLine requestLine = new SalesTaxRequestLine();

        requestLine.setSKU(pBillingDetail.getItemSku());
        requestLine.setQuantity(1);

        requestLine.setAmount(SpcfUtils.convertToBigDecimal(pBillingDetail.getPretaxAmount()));

        return requestLine;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public BillingDetail() {
        super();
    }

    /**
     * Get the FeeTransaction associated with a BillingDetail.
     *
     * @return the FeeTransaction or null if none exists
     */
    public FinancialTransaction getFeeTransaction() {
        for (FinancialTransaction financialTransaction : getFinancialTransactionCollection()) {
            if (!financialTransaction.isSalesTaxTransaction()) {
                return financialTransaction;
            }
        }

        return null;
    }

    /**
     * Get the TaxTransaction (ServiceSalesAndUseTax) associated with a BillingDetail.
     *
     * @return the TaxTransaction or null if none exists
     */
    public FinancialTransaction getTaxTransaction() {
        for (FinancialTransaction financialTransaction : getFinancialTransactionCollection()) {
            if (financialTransaction.isSalesTaxTransaction()) {
                return financialTransaction;
            }
        }

        return null;
    }

    public SpcfMoney getPretaxAmount() {
        SpcfMoney fees = calcTierAdjustedFees();
        SpcfMoney discount = getDiscountAmount();

        if (discount == null) {
            discount = SpcfMoney.ZERO;
        }

        return (SpcfMoney) fees.subtract(discount);
    }

    public boolean isUpdateable() {
        FinancialTransaction ftFee = getFeeTransaction();
        FinancialTransaction ftTax = getTaxTransaction();

        return (ftFee == null || ftFee.calculateCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Created) &&
               (ftTax == null || ftTax.calculateCurrentTransactionState().getTransactionStateCd() == TransactionStateCode.Created);
    }

    public boolean isViable() {
        boolean viable = false;
        FinancialTransaction feeTxn = getFeeTransaction();

        if (feeTxn != null) {
            //
            // If the original EmployerFeeDebit FT was returned and a EmployerFeeRedebit FT was created, use the redebit FT as the reference
            //
            switch (feeTxn.calculateCurrentTransactionState().getTransactionStateCd()) {
                case Returned:
                    TransactionType feeRedebitType = TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeRedebit);
                    DomainEntitySet<FinancialTransaction> origSet = feeTxn.getAssociatedTransactionsCollection()
                                                                          .find(FinancialTransaction.TransactionType().equalTo(feeRedebitType));

                    if (!origSet.isEmpty()) {
                        feeTxn = origSet.get(0);
                    }
                    break;
            }

            //
            // Only show the BD as viable if the fee FT is Created/Executed/Completed
            // (or the original EmployerFeeDebit FT was Returned & the EmployerFeeRedebit FT is Created/Executed/Completed)
            //
            switch (feeTxn.calculateCurrentTransactionState().getTransactionStateCd()) {
                case Created:
                case Executed:
                case Completed:
                    viable = true;
                    break;
            }
        }

        return viable;
    }
    
    /**
     * Looks for an offer that applies to the SKU on the given BillingDetail.  If found, assigns offer-related
     * properties.  If no offer applies, it clears offer-related properties.
     */
    public void applyOffers() {
        OfferingServiceChargePrice price = getOfferingServiceChargePrice();

        setOfferCd(null);
        setOfferName(null);
        setDiscountAmount(SpcfMoney.ZERO);

        if (price != null) {
            OfferingServiceCharge charge = price.getOfferingServiceCharge();
            Offer offer = getPayrollRun().getCompany().getApplicableOffer(charge);

            if (offer != null) {
                setOfferCd(offer.getOfferCd());
                setOfferName(offer.getName());

                if (offer.getDiscountType() == DiscountType.AltPrice) {
                    OfferPrice offerPrice = offer.getAlternatePrice(getOfferingServiceChargeType());

                    if (offerPrice != null) {
                        setBasePrice(offerPrice.getAltBasePrice());
                        setUnitPrice(offerPrice.getAltUnitPrice());
                        setItemTotal(calcTierAdjustedFees(charge));
                    }
                } else { // offer is amount- or percent-off
                    SpcfMoney gross = calcTierAdjustedFees(charge);
                    SpcfDecimal discount = offer.getDiscount(gross);
                    setDiscountAmount(new SpcfMoney(discount));
                }
            }
        }
    }

    /**
     * Calls the SalesTaxGateway and assigns the TaxAmount, TaxComputedDate and TaxJurisdiction properties on the given
     * BillingDetail.
     */
    public void computeSalesTax() {
        initializeTaxCalculation();
        Company company = getPayrollRun().getCompany();
        
        // quick and dirty hack to prevent the tax calc for QBOE fees
        if (company.getSourceSystemCd() == SourceSystemCode.QBOE) {
            logger.info("Skipping tax calc for QBOE Company " + company.getId());
            return;
        }

        // if the company tax is exempt, skip the tax calc
        // the company is tax exempt if it has a tax-exempt expiration date, and that date is any time today or after today
        if (company.isTaxExempt()) {
            logger.info("Skipping tax calc for tax exempt Company " + company.getId());
            return;
        }

        // if the taxableAmount (quantity * unitPrice + basePrice - discount) is zero, skip the tax calc
        SpcfMoney taxableAmount = getPretaxAmount();

        if (taxableAmount.isLessThanEqualTo(SpcfMoney.ZERO)) {
            return;
        }

        //
        // Call the Sales Tax Gateway
        //
        SalesTaxResponse taxResponse = getSalesTaxInfo();

        if (taxResponse != null && taxResponse.isSuccess()) {
            // successful, so save the tax info
            if (taxResponse.getTotalTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
                setTaxAmount(SpcfUtils.convertToSpcfMoney(taxResponse.getTotalTaxAmount()));
                // NOTE: the tax amount may be zero
            }

            setTaxComputedDate(PSPDate.getPSPTime());
            setTaxJurisdiction(taxResponse.getTaxJurisdiction());
        } else {
            // not successful, so log the error details and attempt to calculate taxes from previously known rate.
            StringBuilder errorMessage = createSalesTaxErrorMessage(company.getSourceSystemCompanyId(), taxResponse);

            // try to calculate taxes from the last known rate for this company
            computeSalesTaxFromLastKnownRate(errorMessage);

            // log message as error
            logger.error(errorMessage.toString());
        }
    }

    public static void computeSalesTax(PayrollRun pPayrollRun) {
        if(pPayrollRun.getBillingDetailCollection().size() == 0) {
            return;
        }

        Company company = pPayrollRun.getCompany();

        StringBuilder errorMessage = null;
        SalesTaxResponse taxResponse = null;

        boolean calculateSalesTax = true;
        if(company.getSourceSystemCd() == SourceSystemCode.QBOE) {
            logger.info("Skipping tax calc for QBOE Company " + company.getId());
            calculateSalesTax = false;
        } else if (company.isTaxExempt()) {
            logger.info("Skipping tax calc for tax exempt Company " + company.getId());
            calculateSalesTax = false;
        } else {
            taxResponse = getSalesTaxInfo(pPayrollRun, pPayrollRun.getBillingDetailCollection());
            if (taxResponse == null || !taxResponse.isSuccess()) {
                // not successful, so log the error details and attempt to calculate taxes from previously known rate.
                errorMessage = createSalesTaxErrorMessage(pPayrollRun.getCompany().getSourceSystemCompanyId(), taxResponse);
            }
        }

        for (BillingDetail billingDetail : pPayrollRun.getBillingDetailCollection()) {
            // tax amount is zero until we know otherwise
            billingDetail.initializeTaxCalculation();

            if(!calculateSalesTax) {
                continue;
            }

            // if the taxableAmount (quantity * unitPrice + basePrice - discount) is zero, skip the tax calc
            SpcfMoney taxableAmount = billingDetail.getPretaxAmount();

            if (taxableAmount.isLessThanEqualTo(SpcfMoney.ZERO)) {
                continue;
            }

            if (taxResponse != null && errorMessage == null) {
                for (SalesTaxResponseLine salesTaxResponseLine : taxResponse.getSalesTaxResponseLineList()) {
                    if(salesTaxResponseLine.getSKU() != null && salesTaxResponseLine.getSKU().equals(billingDetail.getItemSku())) {
                        billingDetail.setTaxAmount(SpcfUtils.convertToSpcfMoney(salesTaxResponseLine.getTaxAmount()));
                        billingDetail.setItemTotal(new SpcfMoney(billingDetail.getPretaxAmount().add(billingDetail.getTaxAmount())));
                    }
                }

                billingDetail.setTaxComputedDate(PSPDate.getPSPTime());
                billingDetail.setTaxJurisdiction(taxResponse.getTaxJurisdiction());
            } else {
                // try to calculate taxes from the last known rate for this company
                billingDetail.computeSalesTaxFromLastKnownRate(errorMessage);
            }
        }

        if (errorMessage != null) {
            // log message as error
            logger.warn(errorMessage.toString());
        }
    }

    private void initializeTaxCalculation() {
        setTaxComputedDate(null);
        setTaxJurisdiction(null);
        setTaxAmount(SpcfMoney.ZERO);
    }

    private static StringBuilder createSalesTaxErrorMessage(String pCompanyId, SalesTaxResponse pTaxResponse) {
        StringBuilder stringBuilder = new StringBuilder();

        if (pTaxResponse == null) {
            stringBuilder.append("No response from Sales Tax Gateway (null).");
        } else {
            stringBuilder.append("Error response from Sales Tax Gateway.");

            ErrorMessage msgSummary = pTaxResponse.getSummaryErrorMessage();
            if (msgSummary != null) {
                stringBuilder.append(System.getProperty("line.separator"))
                      .append("> Sales Tax Gateway Summary Error ")
                      .append(msgSummary.getErrorCode())
                      .append(": ")
                      .append(msgSummary.getErrorDescription());
            }

            ArrayList<ErrorMessage> msgDetails = pTaxResponse.getDetailErrorMessageList();
            if (msgDetails != null) {
                for (ErrorMessage detail : msgDetails) {
                    stringBuilder.append(System.getProperty("line.separator"))
                          .append("> Sales Tax Gateway Detail Error ")
                          .append(detail.getErrorCode())
                          .append(": ")
                          .append(detail.getErrorDescription());
                }
            }
        }

        stringBuilder.append(System.getProperty("line.separator"))
              .append("Attempting to determine previous tax rate for company ")
              .append(pCompanyId)
              .append("...");

        return stringBuilder;
    }

    private void computeSalesTaxFromLastKnownRate(StringBuilder pMessage) {
        pMessage.append((pMessage.length() > 0) ? System.getProperty("line.separator") : "");

        // find the most recent known tax rate and use it
        BillingDetail other = findLastNonZeroSalesTaxRate(getPayrollRun().getCompany(), getItemSku());

        if (other != null) {
            SpcfDecimal pretax = other.getItemTotal().subtract(other.getTaxAmount());

            if (pretax.compareTo(SpcfDecimal.createInstance(0.00)) > 0) {
                // SpcfDecimal keeps at most 2 decimal places... scaling the percentage rate preserves some precision
                SpcfDecimal rate = other.getTaxAmount().multiply(BillingDetail.SCALE).divide(pretax);

                setTaxAmount(new SpcfMoney(rate.multiply(getPretaxAmount()).divide(BillingDetail.SCALE)));

                if (other.getTaxComputedDate() != null) {
                    setTaxComputedDate(other.getTaxComputedDate().toLocal());
                }

                setTaxJurisdiction(other.getTaxJurisdiction());

                pMessage.append("Charging tax at assumed rate of ")
                        .append(rate.multiply(SpcfDecimal.createInstance(100.0)).divide(BillingDetail.SCALE))
                        .append("% in effect as of ")
                        .append(other.getTaxComputedDate() != null ? other.getTaxComputedDate().format("yyyy-MM-dd") : "NULL");
            } else {
                // else tax amount == total amount, which can't actually happen... but better safe than divide-by-zero
                pMessage.append("Unable to determine previous tax rate, skipping tax calculation (assuming tax rate of zero).");
            }
        } else {
            // else no non-zero tax calcs, so proceed as if zero tax
            pMessage.append("No previous tax rates found, skipping tax calculation (assuming tax rate of zero).");
        }
    }

    private void createFinancialTransactionsForBillingDetail(CompanyBankAccount pCompanyBankAccount,
                                                             SettlementType pSettlementType,
                                                             SpcfCalendar pSettlementDate,
                                                             OfferingCode pOfferingCode,
                                                             BigDecimal pTotalAmountOverride) {
        boolean wantZeroDollarFeeTxn = false; // default to false for legacy compatibility
        OfferingCode offeringCode = pOfferingCode;

        //
        // If the OfferingCode is not provided, try to determine it with other available info
        //
        if (offeringCode == null) {
            OfferingServiceChargeGroup group;
            OfferingServiceChargePrice price = getOfferingServiceChargePrice();

            if (price != null) {
                group = price.getOfferingServiceCharge().getOfferingServiceChargeGroup();
            } else {
                group = OfferingServiceChargeGroup.getOfferingServiceChargeGroup(getPayrollRun().getCompany(), null, getOfferingServiceChargeType());
            }

            if (group != null) {
                offeringCode = group.getOffering().getOfferingCode();
            }
        }

        if (offeringCode != null) {
            //
            // The following OfferingCode(s) cannot have $0 fee transactions
            //
            switch (offeringCode) {
                case BillPaymentSTD3:
                    wantZeroDollarFeeTxn = false;
                    break;
                case BillPaymentSTD3FY14:
                    wantZeroDollarFeeTxn = false;
                    break;
                case BillPaymentSTDFY15:
                    wantZeroDollarFeeTxn = false;
                    break;
                case BillPaymentSTDFY16:
                    wantZeroDollarFeeTxn = false;
                    break;

                default:
                    wantZeroDollarFeeTxn = true;
                    break;
            }
        }

        FinancialTransaction.createFinancialTransactionsForBillingDetail(this, pCompanyBankAccount, pSettlementType, pSettlementDate, pTotalAmountOverride, wantZeroDollarFeeTxn);
    }

    public BillingDetail update(PayrollRun pPayrollRun, CompanyBankAccount pCompanyBankAccount, SettlementType pSettlementType, SpcfCalendar pSettlementDate) {
        //Cancel Fee Financial Transaction
        FinancialTransaction ftFee = getFeeTransaction();
        if (ftFee != null) {
            ftFee.cancelFinancialTransaction();
            removeFinancialTransaction(ftFee);
            ftFee.setBillingDetail(null);
        }

        //Cancel Tax Financial Transaction
        FinancialTransaction ftTax = getTaxTransaction();
        if (ftTax != null) {
            ftTax.cancelFinancialTransaction();
            removeFinancialTransaction(ftTax);
            ftTax.setBillingDetail(null);
        }

        setPayrollRun(pPayrollRun);

        //
        // Recreate the FTs for this BD (no need to update memo since BD metrics haven't changed)
        //
        createFinancialTransactionsForBillingDetail(pCompanyBankAccount, pSettlementType, pSettlementDate, null, null);

        return Application.save(this);
    }

    public String extractStateFromMemoField() {
        String state = null;
        String memo = getMemo();
        
        if (memo != null) {
            //
            // Extract the State from the current memo field
            // Example 'state fee' memo:    "State processing fee for UT for Feb 2013"
            // Example 'no state fee' memo: "No state fee for NV for Jan 2012"
            //

            Matcher m = STATE_FEE_MEMO_PATTERN.matcher(memo);
            
            if (m.matches()) {
                //
                // Group 1 = State
                // Group 2 = Month
                // Group 3 = Year
                //
                state = m.group(1);
            }
        }
        
        return state;
    }

    public void setDefaultMemoForFeeType(String pParam) {
        String memo = "";
        SpcfMoney itemTotal;
        SpcfMoney basePrice;
        SpcfMoney unitPrice;
        SpcfMoney discount;
        SpcfCalendar period;

        switch (getOfferingServiceChargeType()) {
            case PerPaycheck:
            case DirectDepositFee:
                itemTotal = getItemTotal();

                if (itemTotal == null) {
                    itemTotal = SpcfMoney.ZERO;
                }

                basePrice = getBasePrice();

                if (basePrice == null) {
                    basePrice = SpcfMoney.ZERO;
                }

                unitPrice = getUnitPrice();

                if (unitPrice == null) {
                    unitPrice = SpcfMoney.ZERO;
                }

                discount = getDiscountAmount();

                if (discount == null) {
                    discount = SpcfMoney.ZERO;
                }

                if (itemTotal.isLessThanEqualTo(SpcfMoney.ZERO)) {
                    if (discount.isZero()) {
                        memo = String.format(MEMOS.NO_FEE_FOR_DIRECT_DEPOSIT, getQuantity());
                    } else {
                        memo = String.format(MEMOS.NO_FEE_FOR_DIRECT_DEPOSIT + " " + MEMOS.INCLUDES_DISCOUNT, getQuantity(), discount);
                    }
                } else if (basePrice.isGreaterThan(SpcfMoney.ZERO) && unitPrice.isGreaterThan(SpcfMoney.ZERO)) {
                    if (discount.isZero()) {
                        memo = String.format(MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH_PLUS_PER_PAY_PERIOD, getQuantity(), unitPrice, basePrice);
                    } else {
                        memo = String.format(MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH_PLUS_PER_PAY_PERIOD + " " + MEMOS.INCLUDES_DISCOUNT, getQuantity(), unitPrice, basePrice, discount);
                    }
                } else if (basePrice.isGreaterThan(SpcfMoney.ZERO)) {
                    if (discount.isZero()) {
                        memo = String.format(MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_PER_PAY_PERIOD, getQuantity(), basePrice);
                    } else {
                        memo = String.format(MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_PER_PAY_PERIOD + " " + MEMOS.INCLUDES_DISCOUNT, getQuantity(), basePrice, discount);
                    }
                } else if (unitPrice.isGreaterThan(SpcfMoney.ZERO)) {
                    if (discount.isZero()) {
                        memo = String.format(MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, getQuantity(), unitPrice);
                    } else {
                        memo = String.format(MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH + " " + MEMOS.INCLUDES_DISCOUNT, getQuantity(), unitPrice, discount);
                    }
                } else {
                    logger.error(String.format("Unable to determine Base or Unit Price for DirectDepositFee memo field in Billing Detail %s", getId()));
                }
                break;

            case EmployeesPaid:
                itemTotal = getItemTotal();

                if (itemTotal == null) {
                    itemTotal = SpcfMoney.ZERO;
                }

                if (itemTotal.isLessThanEqualTo(SpcfMoney.ZERO)) {
                    memo = String.format(MEMOS.NO_FEE_FOR_EMPLOYEE_PAID, getQuantity());
                } else {
                    memo = String.format(MEMOS.FEE_FOR_EMPLOYEE_PAID, getQuantity());
                }
                break;

            case ExtraStateFee:
                String state = pParam;

                if (state == null) {
                    //
                    // If a State isn't provided we may be updating this BD, so try and retrieve the State previously used.
                    //
                    state = extractStateFromMemoField();
                }

                if (state == null) {
                    logger.error(String.format("No State specified for ExtraStateFee memo field in Billing Detail %s", getId()));
                } else {
                    period = getBillingPeriod();

                    if (period == null) {
                        logger.error(String.format("Unable to determine Billing Period for ExtraStateFee memo field in Billing Detail %s", getId()));
                    } else {
                        itemTotal = getItemTotal();

                        if (itemTotal == null) {
                            itemTotal = SpcfMoney.ZERO;
                        }

                        if (itemTotal.equals(SpcfMoney.ZERO)) {
                            memo = String.format(MEMOS.NO_STATE_FEE, state, MONTHS[period.getMonth() - 1], period.getYear());
                        } else {
                            memo = String.format(MEMOS.STATE_PROCESSING_FEE, state, MONTHS[period.getMonth() - 1], period.getYear());
                        }
                    }
                }
                break;

            case MonthlyFee:
                period = getBillingPeriod();

                if (period == null) {
                    logger.error(String.format("Unable to determine Billing Period for MonthlyFee memo field in Billing Detail %s", getId()));
                } else {
                    itemTotal = getItemTotal();

                    if (itemTotal == null) {
                        itemTotal = SpcfMoney.ZERO;
                    }

                    if (itemTotal.equals(SpcfMoney.ZERO)) {
                        memo = String.format(MEMOS.NO_MONTHLY_FEE, MONTHS[period.getMonth() - 1], period.getYear());
                    } else {
                        memo = String.format(MEMOS.MONTHLY_PROCESSING_FEE, MONTHS[period.getMonth() - 1], period.getYear());
                    }
                }
                break;

            case OtherFee:
                memo = pParam;

                //
                // If no memo is passed in, try to default to existing memo (in case we're just updating this BD)
                //

                if (memo == null) {
                    memo = getMemo();
                }

                if (memo != null) {
                    if (memo.length() > 30) {
                        memo = memo.substring(0, 30);
                    }
                } else {
                    logger.error(String.format("Unable to determine OtherFee memo field in Billing Detail %s", getId()));
                }
                break;

            case PerPayroll:
                memo = MEMOS.PAYROLL_FEE;
                break;

            case PerTransmission:
                memo = MEMOS.DIRECT_DEPOSIT_TRANSMISSION_FEE;
                break;

            case BackdatedPayroll:
                memo = MEMOS.BACK_DATE_FEE;
                break;

            case BankVerificationDebit:
                memo = MEMOS.ENROLLMENT_FEE;
                break;

            case BankVerificationCredit:
                memo = MEMOS.ENROLLMENT_FEE_REFUND;
                break;

            case DebitReturnFee:
                memo = MEMOS.NSF_FEE_FOR_RETURNED_PAYROLL;
                break;

            case W2Fee:
                memo = MEMOS.FEE_PER_W2;
                break;

            case W2BaseFee:
                memo = MEMOS.BASE_W2_FEE;
                break;

            case AmendedSSN:
                memo = MEMOS.AMENDED_RETURN_SS_CORRECTION_FEE;
                break;

            case Amendments:
                memo = MEMOS.AMENDED_RETURN_FEE;
                break;

            case ReversalFee:
                memo = MEMOS.DIRECT_DEPOSIT_REVERSAL_FEE;
                break;

            case EntityChange:
                memo = MEMOS.LEGAL_ENTITY_CHANGE_FEE;
                break;

            case ExtraCopies:
                memo = MEMOS.ADDITIONAL_COPY_OF_RETURN_FEE;
                break;

            case PayrollAdjustment:
                memo = MEMOS.PRIOR_PAYROLL_ADJUSTMENT_FEE;
                break;

            case W2Correction:
                memo = MEMOS.CORRECT_W_2_FEE;
                break;

            case PaymentArrangementFee:
                memo = MEMOS.PAYMENT_ARRANGEMENT_FEE;
                break;

            case PerBatch:
            case PerPayment:
            case CompanyUpdates:
            case EmployeesAdded:
            case EmployeesUpdated:
            case PenaltiesAndInterest:
            default:
                break;
        }

        setMemo(memo);
    }

    public SpcfMoney calcTierAdjustedFees() {
        //
        // Use the associated OfferingServiceChargePrice (if any)
        // OfferingServiceChargePrice may be null for legacy BillingDetails - this is ok since these legacy BDs only represent non-tiered OfferingServiceCharges (DIY+DD, etc.)
        //
        OfferingServiceChargePrice price = getOfferingServiceChargePrice();
        return (price == null) ? calcTierAdjustedFees(null) : calcTierAdjustedFees(price.getOfferingServiceCharge());
    }
    
    public SpcfMoney calcTierAdjustedFees(OfferingServiceCharge pCharge) {
        int tierUnitOffset = (pCharge == null) ? 0 : pCharge.getTierUnits();
        int tierAdjustedQuantity = getQuantity();

        if (tierAdjustedQuantity < 0) {
            tierAdjustedQuantity = 0;
        }

        //
        // Calculate the total fees based on units (adjusted for tier)
        //
        SpcfDecimal unitPrice = getUnitPrice();

        if (unitPrice == null) {
            unitPrice = SpcfMoney.ZERO;
        }

        SpcfDecimal totalFees = unitPrice.multiply(SpcfDecimal.createInstance(tierAdjustedQuantity));

        //
        // Then add on the base price for the tier
        //
        SpcfDecimal basePrice = getBasePrice();

        if (basePrice == null) {
            basePrice = SpcfMoney.ZERO;
        }

        totalFees = totalFees.add(basePrice);

        return new SpcfMoney(totalFees);
    }
}
