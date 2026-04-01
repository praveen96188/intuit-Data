package com.intuit.sbd.payroll.psp.batchjobs.statereports.states;

import com.intuit.payroll.agency.api.*;
import com.intuit.payroll.agency.dao.FrequencyData;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.paycycle.util.StringUtil;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Generates the output for state coupon files
 * @author janderson
 */
public class AllCoupons extends StateReportBase {

    /** The name of the report when manually scheduled */
    public static final String REPORT_NAME = "ALL-COUPONS";

    //State report required QTD, MTD are generated separately
    public static final String[] QTD_MTD_REPORTS_LIST = {"HI-VP1-PAYMENT"};

    //State report required MOQB are generated separately
    public static final String MOQM_PMT_TEMPLATE = "MO-941-PAYMENT";

    public static final String MOQM_REPORT_EMAIL = "tax_eservice@intuit.com";

    public static final String MOQM_COMPANY_NAME = "Computing Resource I";


    //State report required MTD based on Deposit Frequency -
    static List<PaymentTemplateFrequency> mtdPaymentTemplateFrequencies = new ArrayList<PaymentTemplateFrequency>();

    static Map<String, DepositFrequencyCode> mtdReportsMap = new HashMap<String, DepositFrequencyCode>();

    static {
        mtdReportsMap.put("OK-OW9A-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        mtdReportsMap.put("MD-MW506-PAYMENT", DepositFrequencyCode.ACCELERATED);
        mtdReportsMap.put("MI-MW106-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);

        for (String paymentTemplateCd : mtdReportsMap.keySet()) {
             mtdPaymentTemplateFrequencies.add(PaymentTemplateFrequency.getPaymentTemplateFrequency(paymentTemplateCd, mtdReportsMap.get(paymentTemplateCd)));
        }
    }

    /**
     * Constructor
     */
    public AllCoupons() {

    }

    @Override
    public boolean handlesReport(String reportName) {
        // This report is added directly
        return false;
    }

    @Override
    public boolean isScheduled(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {
        return !CalendarUtils.isHoliday(passedInDate);
    }

    @Override
    public void process(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {
        // Figure out previous quarter and current month
        SpcfCalendar startOfPreviousMonth = getBeginningOfMonth(passedInDate);
        startOfPreviousMonth.addMonths(-1);
        SpcfCalendar endOfPreviousMonth = startOfPreviousMonth.copy();
        endOfPreviousMonth.addMonths(1);
        endOfPreviousMonth.addMilliseconds(-1);

        SpcfCalendar startOfPreviousQuarter = getPreviousQuarter(passedInDate);
        SpcfCalendar endOfPreviousQuarter = startOfPreviousQuarter.copy();
        endOfPreviousQuarter.addMonths(3);
        endOfPreviousQuarter.addMilliseconds(-1);

        //Create Report for MOQM on first day of every month irrespective of Weekend or Holidays which includes previous month data
        if(isFirstDayOfTheMonth(passedInDate)){
            createPaymentReportMOQM(passedInDate);
        }

        if (BatchUtils.isWeekendOrHoliday(passedInDate)) {
            logger.warn(getClass().getSimpleName() + " skipped (weekend or bank holiday) ");
            return;
        }

        HashMap<String, String> iaBENList = getIABENNumber();

        createPaymentReport(true, passedInDate, startOfPreviousMonth, endOfPreviousMonth, startOfPreviousQuarter,
                endOfPreviousQuarter, iaBENList);
        createPaymentReport(false, passedInDate, startOfPreviousMonth, endOfPreviousMonth, startOfPreviousQuarter,
                endOfPreviousQuarter, iaBENList);

        //QTD, MTD reports - (HI) - report
        createPaymentReportWithQTDAndMTD(passedInDate, startOfPreviousMonth, endOfPreviousMonth, startOfPreviousQuarter, endOfPreviousQuarter);

        //Reports with MTD - (OK - SW, MD -  and MO) report
        createPaymentReportWithMTD(passedInDate, startOfPreviousMonth, endOfPreviousMonth, startOfPreviousQuarter, endOfPreviousQuarter);

    }

    /**
     * Creates the regular or zero payment report
     * @param isRegularReport Should the report be a regular report or a zero report
     * @param passedInDate The date to run the report for
     * @param startOfPreviousMonth The start date of the previous month
     * @param endOfPreviousMonth The end date of the previous month
     * @param startOfPreviousQuarter The start date of the previous quarter
     * @param endOfPreviousQuarter The end date of the previous quarter
     * @param iaBENList A map of company's PSID to its IA BEN number
     */
    private void createPaymentReport(boolean isRegularReport, SpcfCalendar passedInDate, SpcfCalendar startOfPreviousMonth,
                                     SpcfCalendar endOfPreviousMonth, SpcfCalendar startOfPreviousQuarter,
                                     SpcfCalendar endOfPreviousQuarter, HashMap<String, String> iaBENList) {
        StringBuilder builder = new StringBuilder();

        addHeader(builder, false, false); // Do not include MTD and QTD information

        HashMap<Company, HashMap<PaymentTemplateFrequency, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions;
        StateReportType stateReportType;

        StopWatch stopWatch = new StopWatch("Coupon Report Query");
        stopWatch.start();

        if (isRegularReport) {
            companyToMoneyMovementTransactions = getMoneyMovementTransactionsForInitiationDate(getMMTs(passedInDate, false));

            stateReportType = StateReportType.Coupon;
        } else {
            companyToMoneyMovementTransactions = getMoneyMovementTransactionsForInitiationDate(getMMTs(passedInDate, true));

            stateReportType = StateReportType.ZeroCoupon;
        }

        stopWatch.stop();

        logger.info("Running AllCoupons " + stateReportType + " report for initiation date " + passedInDate.format("yyyy/MM/dd") +
                " with " + companyToMoneyMovementTransactions.size() + " companies.  Query took " + stopWatch.getElapsedTimeString() );

        HashSet<PaymentTemplateFrequency> paymentTemplateFrequencies = new HashSet<PaymentTemplateFrequency>();

        int totalProcessed = processMMTs(companyToMoneyMovementTransactions, builder, paymentTemplateFrequencies, passedInDate,
                startOfPreviousMonth, endOfPreviousMonth, startOfPreviousQuarter, endOfPreviousQuarter, iaBENList, false, false); // Do not include MTD and QTD information

        PaymentTemplateFrequency[] paymentTemplateFrequenciesArray = paymentTemplateFrequencies.toArray(
                new PaymentTemplateFrequency[paymentTemplateFrequencies.size()]);
        saveStateCoupon(builder, passedInDate, passedInDate, stateReportType, paymentTemplateFrequenciesArray);

        BatchUtils.createStateReportEmail(builder, totalProcessed, passedInDate, passedInDate, stateReportType,
                paymentTemplateFrequenciesArray);
    }

    /**
     * Creates the regular or zero payment report
     * @param passedInDate The date to run the report for
     * @param startOfPreviousMonth The start date of the previous month
     * @param endOfPreviousMonth The end date of the previous month
     * @param startOfPreviousQuarter The start date of the previous quarter
     * @param endOfPreviousQuarter The end date of the previous quarter
     */
    private void createPaymentReportWithQTDAndMTD(SpcfCalendar passedInDate, SpcfCalendar startOfPreviousMonth,
                                     SpcfCalendar endOfPreviousMonth, SpcfCalendar startOfPreviousQuarter,
                                     SpcfCalendar endOfPreviousQuarter) {
        StringBuilder builder = new StringBuilder();

        addHeader(builder, true, true); // Include MTD and QTD information

        HashMap<Company, HashMap<PaymentTemplateFrequency, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions;
        StateReportType stateReportType;

        StopWatch stopWatch = new StopWatch("Coupon Report Query");
        stopWatch.start();

        companyToMoneyMovementTransactions = getMoneyMovementTransactionsForInitiationDate(getMMTsForQTDMTD(passedInDate));

        stateReportType = StateReportType.Coupon;

        stopWatch.stop();

        logger.info("Running Coupons report that needs QTD, MTD " + stateReportType + " report for initiation date " + passedInDate.format("yyyy/MM/dd") +
                " with " + companyToMoneyMovementTransactions.size() + " companies.  Query took " + stopWatch.getElapsedTimeString() );

        HashSet<PaymentTemplateFrequency> paymentTemplateFrequencies = new HashSet<PaymentTemplateFrequency>();

        int totalProcessed = processMMTs(companyToMoneyMovementTransactions, builder, paymentTemplateFrequencies, passedInDate,
                startOfPreviousMonth, endOfPreviousMonth, startOfPreviousQuarter, endOfPreviousQuarter, null, true, true); // Include MTD and QTD information

        PaymentTemplateFrequency[] paymentTemplateFrequenciesArray = paymentTemplateFrequencies.toArray(
                new PaymentTemplateFrequency[paymentTemplateFrequencies.size()]);
        saveStateCoupon(builder, passedInDate, passedInDate, stateReportType, paymentTemplateFrequenciesArray);

        BatchUtils.createStateReportEmail(builder, totalProcessed, passedInDate, passedInDate, stateReportType, QTD_MTD_REPORTS_LIST,
                paymentTemplateFrequenciesArray);
    }

    /**
     * Creates the regular or zero payment report
     * @param passedInDate The date to run the report for
     * @param startOfPreviousMonth The start date of the previous month
     * @param endOfPreviousMonth The end date of the previous month
     * @param startOfPreviousQuarter The start date of the previous quarter
     * @param endOfPreviousQuarter The end date of the previous quarter
     */
    private void createPaymentReportWithMTD(SpcfCalendar passedInDate, SpcfCalendar startOfPreviousMonth,
                                     SpcfCalendar endOfPreviousMonth, SpcfCalendar startOfPreviousQuarter,
                                     SpcfCalendar endOfPreviousQuarter) {
        StringBuilder builder = new StringBuilder();

        addHeader(builder, true, false); // Include MTD and Not QTD information

        HashMap<Company, HashMap<PaymentTemplateFrequency, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions;
        StateReportType stateReportType;

        StopWatch stopWatch = new StopWatch("Coupon Report Query");
        stopWatch.start();

        companyToMoneyMovementTransactions = getMoneyMovementTransactionsForInitiationDate(getMMTsForMTD(passedInDate));

        stateReportType = StateReportType.Coupon;

        stopWatch.stop();

        logger.info("Running Coupons report that needs QTD, MTD " + stateReportType + " report for initiation date " + passedInDate.format("yyyy/MM/dd") +
                " with " + companyToMoneyMovementTransactions.size() + " companies.  Query took " + stopWatch.getElapsedTimeString() );

        HashSet<PaymentTemplateFrequency> paymentTemplateFrequencies = new HashSet<PaymentTemplateFrequency>();

        int totalProcessed = processMMTs(companyToMoneyMovementTransactions, builder, paymentTemplateFrequencies, passedInDate,
                startOfPreviousMonth, endOfPreviousMonth, startOfPreviousQuarter, endOfPreviousQuarter, null, true, false); // Include MTD and Not QTD information

        PaymentTemplateFrequency[] paymentTemplateFrequenciesArray = paymentTemplateFrequencies.toArray(
                new PaymentTemplateFrequency[paymentTemplateFrequencies.size()]);

        saveStateCoupon(builder, passedInDate, passedInDate, stateReportType, paymentTemplateFrequenciesArray);

        BatchUtils.createStateReportEmail(builder, totalProcessed, passedInDate, passedInDate, stateReportType, mtdReportsMap,
                paymentTemplateFrequenciesArray);
    }

    /**
     * Creates the regular or zero payment report
     * @param passedInDate The date to run the report for
     */
    private void createPaymentReportMOQM(SpcfCalendar passedInDate) {
        StringBuilder builder = new StringBuilder();

        HashMap<Company, HashMap<PaymentTemplateFrequency, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions;
        StateReportType stateReportType;

        StopWatch stopWatch = new StopWatch("Coupon Report Query");
        stopWatch.start();

        companyToMoneyMovementTransactions = getMoneyMovementTransactionsForInitiationDate(getMMTsForMOQM(passedInDate));

        stateReportType = StateReportType.Coupon;

        stopWatch.stop();

        logger.info("Running Coupons report for " + MOQM_PMT_TEMPLATE + " " + stateReportType + " report for initiation date " + passedInDate.format("yyyy/MM/dd") +
                " with " + companyToMoneyMovementTransactions.size() + " companies.  Query took " + stopWatch.getElapsedTimeString() );


        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(MOQM_PMT_TEMPLATE, DepositFrequencyCode.QUARTERMONTHLY);


        int totalProcessed = processMMTsForMOQM(companyToMoneyMovementTransactions, builder, paymentTemplateFrequency, passedInDate); // Include Only MO-941-PAYMENT QUARTERMONTHLY


        saveStateCoupon(builder, passedInDate, passedInDate, stateReportType, paymentTemplateFrequency);


        BatchUtils.createStateReportEmail(builder, totalProcessed, passedInDate, passedInDate, stateReportType, paymentTemplateFrequency);
    }


    /**
     * Checks if the zero payment report is required for the state for the PaymentTemplateFrequency
     * @param rulesInfo The rules
     * @param paymentTemplateFrequency The PaymentTemplateFrequency object to check
     * @return If the zero payment report is required
     */
    private boolean isZeroPaymentRequired(IRulesInfo rulesInfo, PaymentTemplateFrequency paymentTemplateFrequency) {
        DepositFrequencyCode depositFrequencyCode = paymentTemplateFrequency.getPaymentFrequencyId();
        IRulesPaymentTemplate paymentTemplate = rulesInfo.getPaymentTemplate(paymentTemplateFrequency.getPaymentTemplate().getPaymentTemplateCd());

        if (!depositFrequencyCode.equals(DepositFrequencyCode.NOCALC)) {
            IPaymentFrequency paymentFrequency = paymentTemplate.getPaymentFrequency(depositFrequencyCode.toString());

            FrequencyData freq = (FrequencyData) paymentFrequency;
            if (freq != null && freq.isZeroPaymentRequired()) {
                // Zero payment required
                return true;
            }
        }

        return false;
    }

    /**
     * Creates the report based on the MMTs passed in
     * @param companyToMoneyMovementTransactions The MMTs to use
     * @param builder The builder to append the text to
     * @param paymentTemplateFrequencies The Set of frequencies to add the list of frequencies to
     * @param passedInDate The date to use
     * @param startOfPreviousMonth The start date of the previous month
     * @param endOfPreviousMonth The end date of the previous month
     * @param startOfPreviousQuarter The start date of the previous quarter
     * @param endOfPreviousQuarter The end date of the previous quarter
     * @param iaBENList A map of company's PSID to its IA BEN number
     * @return The number of reports processed
     */
    private int processMMTs(HashMap<Company, HashMap<PaymentTemplateFrequency, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions,
                             StringBuilder builder, HashSet<PaymentTemplateFrequency> paymentTemplateFrequencies,
                             SpcfCalendar passedInDate, SpcfCalendar startOfPreviousMonth, SpcfCalendar endOfPreviousMonth,
                             SpcfCalendar startOfPreviousQuarter, SpcfCalendar endOfPreviousQuarter, HashMap<String, String> iaBENList, boolean pIncludeMTD, boolean pIncludeQTD) {

        int totalProcessed = 0;
        int totalCompaniesProcessed = 0;

        for (Company company : companyToMoneyMovementTransactions.keySet()) {
            try{
                pspRequestContextManager.setRequestContextCompany(company);
            HashMap<PaymentTemplateFrequency, ArrayList<MoneyMovementTransaction>> paymentTemplateFrequencyForCompany =
                    companyToMoneyMovementTransactions.get(company);

            totalCompaniesProcessed++;

            for (PaymentTemplateFrequency paymentTemplateFrequency : paymentTemplateFrequencyForCompany.keySet()) {

                paymentTemplateFrequencies.add(paymentTemplateFrequency);

                String stateToPay = paymentTemplateFrequency.getPaymentTemplate().getPaymentTemplateAbbrev();

                ArrayList<MoneyMovementTransaction> mmts = paymentTemplateFrequencyForCompany.get(paymentTemplateFrequency);

                for (MoneyMovementTransaction mmt : mmts) {
                    totalProcessed++;
                    builder.append("\n");

                    // State Paying Taxes to Abbreviation
                    addCell(builder, stateToPay);

                    // State Tax ID
                    String stateTaxId = mmt.getAgencyTaxpayerId();

                    if (stateTaxId == null) {
                        stateTaxId = "";
                    }

                    addCell(builder, stateTaxId);

                    // IA BEN Number
                    String iaBEN;

                    if (stateToPay.equals("IA-44105-PAYMENT")) {
                        iaBEN = iaBENList.get(company.getSourceCompanyId());

                        if (iaBEN == null) {
                            iaBEN = "";
                        }
                    } else {
                        iaBEN = "";
                    }

                    addCell(builder, iaBEN);

                    // FEIN with dash
                    addCell(builder, company.getFedTaxId().substring(0, 2), "-", company.getFedTaxId().substring(2));
                    // PSID
                    addCell(builder, company.getSourceCompanyId());
                    // Company Legal Name
                    String legalName = company.getLegalName();

                    if (legalName != null) {
                        legalName = legalName.replaceAll(",", "");

                        if (stateToPay.equals("MI-MW106-PAYMENT")) {
                            legalName = legalName.substring(0, Math.min(legalName.length(), 32));
                        }

                    } else {
                        legalName = "";
                    }

                    addCell(builder, legalName);
                    // Company Legal Address
                    String addressLine1 = "";
                    String addressLine2 = "";

                    if (company.getLegalAddress() != null) {
                        if (company.getLegalAddress().getAddressLine1() != null) {
                            addressLine1 = company.getLegalAddress().getAddressLine1();
                        }

                        if (company.getLegalAddress().getAddressLine2() != null) {
                            addressLine2 = company.getLegalAddress().getAddressLine2();
                        }
                    }

                    addCell(builder, addressLine1, " ", addressLine2);
                    // Company City
                    addCell(builder, company.getLegalAddress().getCity());
                    // Company State
                    addCell(builder, company.getLegalAddress().getState());
                    // Company Zip
                    addCell(builder, company.getLegalAddress().getZipCode());

                    // State Filing Frequency
                    addCell(builder, mmt.getPaymentFrequency().getPaymentFrequencyId().toString());
                    // Payment Method	ACH	ACH or Check. For 0 dollar payments null unless it was sent via ach
                    addCell(builder, mmt.getMoneyMovementPaymentMethod().toString());

                    // Payment Amount with decimal
                    addCell(builder, getPaddedMoney(mmt.getMoneyMovementTransactionAmount(), 1, 2, true));
                    // Quarter
                    addCell(builder, String.valueOf(getQuarter(mmt)));
                    // Period Year
                    addCell(builder, mmt.getPaymentPeriodBegin().format("yyyy"));
                    // Period Begin Date MM/DD/YYYYY
                    addCell(builder, mmt.getPaymentPeriodBegin().format("MM/dd/yyyy"));
                    // Period End Date MM/DD/YYYY
                    addCell(builder, mmt.getPaymentPeriodEnd().format("MM/dd/yyyy"));
                    // Payment Due Date MM/DD/YYYY
                    addCell(builder, mmt.getDueDate().format("MM/dd/yyyy"));

                    // Append individual laws
                    int lawsOutput = 0;

                    HashMap<Law, SpcfMoney> balances = mmt.getLiabilityBalances();

                    for (Law law : balances.keySet()) {
                        addCell(builder, law.getDescription());
                        addCell(builder, getPaddedMoney(balances.get(law), 1, 2, true));

                        lawsOutput++;
                    }

                    // Append empty columns
                    for (int i = lawsOutput; i < 5; i++) {
                        builder.append(CSV_SEPARATOR).append(CSV_SEPARATOR);
                    }

                    // *************************************************
                    // NOTE: "month/quarter to date", it is previous month/quarter period start and end dates of process date
                    // *************************************************

                    if (pIncludeMTD || pIncludeQTD) {
                        SpcfDecimal quarterLiabilities = SpcfMoney.ZERO;
                        SpcfDecimal monthLiabilities = SpcfMoney.ZERO;

                        SpcfDecimal quarterWages = SpcfMoney.ZERO;
                        SpcfDecimal monthWages = SpcfMoney.ZERO;

                        for (Law tempLaw : mmt.getPaymentTemplate().getLawCollection()) {

                            if (pIncludeQTD) {
                                Object[] quarterTotals = getLawTaxTotals(company, tempLaw, startOfPreviousQuarter, endOfPreviousQuarter);
                                Object[] quarterAdjustmentTotals = getLawTaxAdjustmentTotals(company, tempLaw, startOfPreviousQuarter, endOfPreviousQuarter);

                                if (quarterTotals != null && quarterTotals[0] != null) {
                                    quarterLiabilities = quarterLiabilities.add((SpcfDecimal) quarterTotals[0]);
                                    quarterWages = quarterWages.add((SpcfDecimal) quarterTotals[2]);
                                }

                                if (quarterAdjustmentTotals != null) {
                                    if (quarterAdjustmentTotals[0] != null) {
                                        quarterLiabilities = quarterLiabilities.add((SpcfDecimal) quarterAdjustmentTotals[0]);
                                    }

                                    if (quarterAdjustmentTotals[2] != null) {
                                        quarterWages = quarterWages.add((SpcfDecimal) quarterAdjustmentTotals[1]);
                                    }
                                }
                            }

                            if (pIncludeMTD) {
                                Object[] monthTotals = getLawTaxTotals(company, tempLaw, startOfPreviousMonth, endOfPreviousMonth);
                                Object[] monthAdjustmentTotals = getLawTaxAdjustmentTotals(company, tempLaw, startOfPreviousMonth, endOfPreviousMonth);

                                if (monthTotals != null && monthTotals[0] != null) {
                                    monthLiabilities = monthLiabilities.add((SpcfDecimal) monthTotals[0]);
                                    monthWages = monthWages.add((SpcfDecimal) monthTotals[2]);
                                }

                                if (monthAdjustmentTotals != null) {
                                    if (monthAdjustmentTotals[0] != null) {
                                        monthLiabilities = monthLiabilities.add((SpcfDecimal) monthAdjustmentTotals[0]);
                                    }

                                    if (monthAdjustmentTotals[2] != null) {
                                        monthWages = monthWages.add((SpcfDecimal) monthAdjustmentTotals[1]);
                                    }
                                }
                            }
                        }

                        if (pIncludeMTD) {
                            // Month to date liabilities with decimal - Payments made to this state on behalf of the customer month to date
                            addCell(builder, getPaddedMoney(monthLiabilities, 1, 2, true));
                            // Month to date Wages with decimal
                            addCell(builder, getPaddedMoney(monthWages, 1, 2, true));
                        }
                        if (pIncludeQTD) {
                            // Quarter to date liabilities with decimal - Payments made to this state on behalf of the customer quarter to date
                            addCell(builder, getPaddedMoney(quarterLiabilities, 1, 2, true));
                            // Quarter to date wages with decimal
                            addCell(builder, getPaddedMoney(quarterWages, 1, 2, true));
                        }
                    }

                    // Get payroll contact
                    DomainEntitySet<Contact> payrollAdminContacts = company.getContactCollection().find(
                            Contact.ContactRoleCd().equalTo(ContactRole.PayrollAdmin));

                    String fullName = "";
                    String phone = "";

                    if (payrollAdminContacts.size() == 1 && payrollAdminContacts.get(0) != null) {
                        if (payrollAdminContacts.get(0).getFirstName() != null && payrollAdminContacts.get(0).getLastName() != null) {
                            fullName = payrollAdminContacts.get(0).getFirstName() + " " + payrollAdminContacts.get(0).getLastName();
                        }

                        if (payrollAdminContacts.get(0).getPhone() != null) {
                            phone = payrollAdminContacts.get(0).getPhone();
                        }
                    }

                    // Payroll Admin Name
                    addCell(builder, fullName);
                    // Payroll Admin Phone Number
                    addCell(builder, phone);
                }

            }

            if (totalCompaniesProcessed % 50 == 0) {
                logger.info("Processed " + totalCompaniesProcessed + " companies out of " +
                        companyToMoneyMovementTransactions.size() + ".");
            }
        }finally {
                pspRequestContextManager.clearRequestContextCompany();
            }
        }

        return totalProcessed;
    }

    /**
     * Creates the report based on the MMTs passed in
     * @param companyToMoneyMovementTransactions The MMTs to use
     * @param builder The builder to append the text to
     * @param paymentTemplateFrequency The Set of frequencies to add the list of frequencies to
     * @return The number of reports processed
     */
    private int processMMTsForMOQM(HashMap<Company, HashMap<PaymentTemplateFrequency, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions,
                            StringBuilder builder, PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate) {

        int totalProcessed = 0;
        int totalCompaniesProcessed = 0;

        SpcfCalendar reportMonth = passedInDate.copy();
        reportMonth.addMonths(-1);

        // Header ID
        builder.append("B");

        // Transaction Date
        builder.append(passedInDate.format("yyyyMMdd"));

        // Company Legal Name
        builder.append(MOQM_COMPANY_NAME);

        // Email Address
        builder.append(MOQM_REPORT_EMAIL);

        //
        String spaces = "";
        spaces = StringUtil.leftPad(spaces," ",28);
        builder.append(spaces);

        builder.append(StringUtil.newLine());


        for (Company company : companyToMoneyMovementTransactions.keySet()) {
                HashMap<PaymentTemplateFrequency, ArrayList<MoneyMovementTransaction>> paymentTemplateFrequencyForCompany =
                        companyToMoneyMovementTransactions.get(company);

                totalCompaniesProcessed++;

                ArrayList<MoneyMovementTransaction> mmts = paymentTemplateFrequencyForCompany.get(paymentTemplateFrequency);

                totalProcessed++;

                // Detail ID
                builder.append("D");

                //Transaction Date
                builder.append(passedInDate.format("yyyyMMdd"));

                // State Tax ID
                CompanyAgencyPaymentTemplate stateTaxId = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(company, PaymentTemplate.findPaymentTemplate(MOQM_PMT_TEMPLATE));
                builder.append(stateTaxId.getAgencyTaxpayerId());

                // File Year
                builder.append(passedInDate.format("yyyy"));

                // File Month
                builder.append(reportMonth.format("MM"));

                SpcfDecimal amount = SpcfMoney.ZERO;

                for (MoneyMovementTransaction mmt : mmts) {

                    amount = amount.add(mmt.getMoneyMovementTransactionAmount());
                }

                // WithHolding Amount
                String whAmount = changeAmountFormat(amount,2);
                whAmount = StringUtil.leftPad(whAmount,"0",11);
                builder.append(whAmount);

                // Compensation Deduction
                String compensation = "";
                compensation = StringUtil.leftPad(compensation,"0",11);
                builder.append(compensation);

                // Previous Overpay/Payments
                String overPayment = "";
                overPayment = StringUtil.leftPad(overPayment,"0",11);
                builder.append(overPayment);

                // Total Amount Due
                String totalAmount = changeAmountFormat(amount,2);
                totalAmount = StringUtil.leftPad(totalAmount,"0",11);
                builder.append(totalAmount);

                // Filler
                String filler = "";
                filler = StringUtil.leftPad(filler," ",13);
                builder.append(filler);

                builder.append(StringUtil.newLine());


                if (totalCompaniesProcessed % 50 == 0) {
                    logger.info("Processed " + totalCompaniesProcessed + " companies out of " +
                            companyToMoneyMovementTransactions.size() + ".");
                }
        }


        return totalProcessed;
    }

    /**
     * Adds the header
     * @param builder The object to append the header to
     */
    private void addHeader(StringBuilder builder, boolean pIncludeMTD, boolean pIncludeQTD) {
        builder.append("State Abrv").append(CSV_SEPARATOR);
        builder.append("State Tax ID").append(CSV_SEPARATOR);
        builder.append("IA BEN").append(CSV_SEPARATOR);
        builder.append("FEIN").append(CSV_SEPARATOR);
        builder.append("PSID").append(CSV_SEPARATOR);
        builder.append("Company Legal Name").append(CSV_SEPARATOR);
        builder.append("Company Legal Address").append(CSV_SEPARATOR);
        builder.append("City").append(CSV_SEPARATOR);
        builder.append("State").append(CSV_SEPARATOR);
        builder.append("Zip").append(CSV_SEPARATOR);
        builder.append("State Payment Frequency").append(CSV_SEPARATOR);
        builder.append("Payment Method").append(CSV_SEPARATOR);
        builder.append("Payment Amt with Decimal").append(CSV_SEPARATOR);
        builder.append("Quarter").append(CSV_SEPARATOR);
        builder.append("Period Year").append(CSV_SEPARATOR);
        builder.append("Period Begin Date").append(CSV_SEPARATOR);
        builder.append("Period End Date").append(CSV_SEPARATOR);
        builder.append("Payment Due Date").append(CSV_SEPARATOR);
        builder.append("Tax Name 1").append(CSV_SEPARATOR);
        builder.append("Amount 1").append(CSV_SEPARATOR);
        builder.append("Tax Name 2").append(CSV_SEPARATOR);
        builder.append("Amount 2").append(CSV_SEPARATOR);
        builder.append("Tax Name 3").append(CSV_SEPARATOR);
        builder.append("Amount 3").append(CSV_SEPARATOR);
        builder.append("Tax Name 4").append(CSV_SEPARATOR);
        builder.append("Amount 4").append(CSV_SEPARATOR);
        builder.append("Tax Name 5").append(CSV_SEPARATOR);
        builder.append("Amount 5").append(CSV_SEPARATOR);
        if(pIncludeMTD) {
            builder.append("Month to Date Liabilities").append(CSV_SEPARATOR);
            builder.append("Month to Date Wages").append(CSV_SEPARATOR);
        }
        if(pIncludeQTD) {
            builder.append("Qtr to Date Liabilities").append(CSV_SEPARATOR);
            builder.append("Qtr to Date Wages").append(CSV_SEPARATOR);
        }
        builder.append("Payroll Admin Name").append(CSV_SEPARATOR);
        builder.append("Payroll Admin Phone Number");
        // Append new line character later on
    }

    /**
     * Gets the totals for a law
     * @param pCompany The company to calculate
     * @param law The law to calculate
     * @param pFromDate The start date
     * @param pToDate The end date
     * @return An array with index 0 TaxLiabilityAmount, index 1 TaxableWagesAmount, index 2 TotalWagesAmount
     */
    private Object[] getLawTaxTotals(Company pCompany, Law law, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        String[] paramNames = new String[5];
        paramNames[0] = "company";
        paramNames[1] = "payCheckStatusCd";
        paramNames[2] = "payChkFromDt";
        paramNames[3] = "payChkToDt";
        paramNames[4] = "law";

        Object[] paramValues = new Object[5];
        paramValues[0] = pCompany;
        paramValues[1] = PaycheckStatusCode.Active;
        paramValues[2] = pFromDate.toUtc();
        paramValues[3] = pToDate.toUtc();
        paramValues[4] = law;

        List<Object> retList = Application.executeNamedQuery("findTaxTotalsByLaw", paramNames, paramValues);

        if (retList.size() > 0) {
            if (retList.get(0) != null) {
                return (Object[]) retList.get(0);
            }
        }

        return null;
    }

    /**
     * Gets the totals for a law
     * @param pCompany The company to calculate
     * @param law The law to calculate
     * @param pFromDate The start date
     * @param pToDate The end date
     * @return An array with index 0 Amount, index 1 TaxableWages, index 2 TotalWages
     */
    private Object[] getLawTaxAdjustmentTotals(Company pCompany, Law law, SpcfCalendar pFromDate, SpcfCalendar pToDate) {
        String[] paramNames = new String[4];
        paramNames[0] = "company";
        paramNames[1] = "payChkFromDt";
        paramNames[2] = "payChkToDt";
        paramNames[3] = "law";

        Object[] paramValues = new Object[4];
        paramValues[0] = pCompany;
        paramValues[1] = pFromDate.toUtc();
        paramValues[2] = pToDate.toUtc();
        paramValues[3] = law;

        List<Object> retList = Application.executeNamedQuery("findAdjustmentTotalsByLaw", paramNames, paramValues);

        if (retList.size() > 0) {
            if (retList.get(0) != null) {
                return (Object[]) retList.get(0);
            }
        }

        return null;
    }

    /**
     * Gets the quarter number for a MMT
     * @param mmt The mmt to use
     * @return The quarter number for a MMT
     */
    private int getQuarter(MoneyMovementTransaction mmt) {
        int quarter;

        int month = mmt.getPaymentPeriodEnd().getMonth();
        if (month < 4) {
            quarter = 1;
        } else if (month < 7) {
            quarter = 2;
        } else if (month < 10) {
            quarter = 3;
        } else {
            quarter = 4;
        }

        return quarter;
    }

    /**
     * Gets all MoneyMovementTransactions for a time period and for a company
     * @param pMoneyMovementTransactions List
     * @return All MoneyMovementTransactions with an initiation date and for a company and payment frequency
     */
    public HashMap<Company, HashMap<PaymentTemplateFrequency, ArrayList<MoneyMovementTransaction>>>
                getMoneyMovementTransactionsForInitiationDate(DomainEntitySet<MoneyMovementTransaction> pMoneyMovementTransactions) {

        HashMap<Company, HashMap<PaymentTemplateFrequency, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions = new
                HashMap<Company, HashMap<PaymentTemplateFrequency, ArrayList<MoneyMovementTransaction>>>();

        IRulesInfo rulesInfo = RulesObjectBroker.getInstance().getRulesInfo();
        int skipped = 0;
        for (MoneyMovementTransaction moneyMovementTransaction : pMoneyMovementTransactions) {

            if(moneyMovementTransaction.getMoneyMovementTransactionAmount().equals(SpcfMoney.ZERO)) {
                // Skip all MMTs for states that don't require zero payments
                boolean required = isZeroPaymentRequired(rulesInfo, moneyMovementTransaction.getPaymentFrequency());
                // Besides the zero payment required frequencies that come from the Agency Rules
                // we need to include the payment templates specified in System Parameter to be included as well
                required = required || PaymentTemplate.getPaymentTemplatesFromSystemParameter(moneyMovementTransaction.getInitiationDate(), true, false).contains(moneyMovementTransaction.getPaymentTemplate());
                CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(moneyMovementTransaction.getCompany(),moneyMovementTransaction.getPaymentTemplate());
                required = required && companyAgencyPaymentTemplate.hasActiveLaw();
                if (!required) {
                    skipped++;
                    continue;
                }
            }

            HashMap<PaymentTemplateFrequency, ArrayList<MoneyMovementTransaction>> paymentTemplateFrequencyForCompany =
                    companyToMoneyMovementTransactions.get(moneyMovementTransaction.getCompany());

            if (paymentTemplateFrequencyForCompany == null) {
                paymentTemplateFrequencyForCompany = new HashMap<PaymentTemplateFrequency, ArrayList<MoneyMovementTransaction>>();
                companyToMoneyMovementTransactions.put(moneyMovementTransaction.getCompany(), paymentTemplateFrequencyForCompany);
            }

            ArrayList<MoneyMovementTransaction> moneyMovementTransactionsForCompany =
                    paymentTemplateFrequencyForCompany.get(moneyMovementTransaction.getPaymentFrequency());

            if (moneyMovementTransactionsForCompany == null) {
                moneyMovementTransactionsForCompany = new ArrayList<MoneyMovementTransaction>();
                paymentTemplateFrequencyForCompany.put(moneyMovementTransaction.getPaymentFrequency(), moneyMovementTransactionsForCompany);
            }

            moneyMovementTransactionsForCompany.add(moneyMovementTransaction);
        }

        logger.info("Skipped " + skipped + " MMTs because state does not require zero payments.");

        return companyToMoneyMovementTransactions;
    }

    /**
     * Gets and creates the MMT query
     * @param initiationDate The initiation date to run on
     * @param zeroOnly Should the query
     */
    private DomainEntitySet<MoneyMovementTransaction> getMMTs(SpcfCalendar initiationDate, boolean zeroOnly) {
        // Confirmed that this the correct query with David and Marcela
        SpcfCalendar begin = initiationDate.copy();
        CalendarUtils.clearTime(begin);
        SpcfCalendar end = begin.copy();
        end.addDays(1);
        end.addMilliseconds(-1);

        Criterion<MoneyMovementTransaction> achCreditPaymentMethodsCriteria = null;
        String achCreditCouponPaymentTemplates = SystemParameter.findStringValue(SystemParameter.Code.COUPON_PAYMENT_TEMPLATE_INCLUDE_ACH_CREDIT);
        if (achCreditCouponPaymentTemplates != null && achCreditCouponPaymentTemplates.trim().length() > 0) {
            ArrayList<String> paymentTemplateCds = new ArrayList<String>();
            for (String paymentTemplate : achCreditCouponPaymentTemplates.split(",")) {
                if (paymentTemplate.trim().length() > 0) {
                    paymentTemplateCds.add(paymentTemplate.trim());
                }
            }

            achCreditPaymentMethodsCriteria =
                MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHCredit)
                .And(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in(paymentTemplateCds.toArray(new String[paymentTemplateCds.size()])));
        }

        Criterion<MoneyMovementTransaction> paymentMethodsCriteria =
                MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.CheckPayment);
        if (achCreditPaymentMethodsCriteria != null) {
                paymentMethodsCriteria = paymentMethodsCriteria.Or(achCreditPaymentMethodsCriteria);
        }

        for (PaymentTemplateFrequency mtdPaymentTemplateFrequency : mtdPaymentTemplateFrequencies) {
            paymentMethodsCriteria = paymentMethodsCriteria.And(MoneyMovementTransaction.PaymentFrequency().notEqualTo(mtdPaymentTemplateFrequency));
        }
        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(MOQM_PMT_TEMPLATE, DepositFrequencyCode.QUARTERMONTHLY);
        paymentMethodsCriteria = paymentMethodsCriteria.And(MoneyMovementTransaction.PaymentFrequency().notEqualTo(paymentTemplateFrequency));


        Criterion<MoneyMovementTransaction> paymentWhereClause =
                paymentMethodsCriteria
                .And(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().notIn(QTD_MTD_REPORTS_LIST))
                .And(MoneyMovementTransaction.InitiationDate().between(begin, end))
                .And(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend,
                        TaxPaymentStatus.Ignore,TaxPaymentStatus.SentToAgency, TaxPaymentStatus.AcknowledgedByAgency));

        if (zeroOnly) {
            paymentWhereClause = paymentWhereClause.And(
                    MoneyMovementTransaction.MoneyMovementTransactionAmount().equalTo(SpcfMoney.ZERO));
        } else {
            paymentWhereClause = paymentWhereClause.And(
                    MoneyMovementTransaction.MoneyMovementTransactionAmount().notEqualTo(SpcfMoney.ZERO));
        }

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                new Query<MoneyMovementTransaction>().Where(paymentWhereClause)
                        .EagerLoad(MoneyMovementTransaction.Company(),
                            MoneyMovementTransaction.Company().LegalAddress(),
                            MoneyMovementTransaction.PaymentFrequency(),
                            MoneyMovementTransaction.PaymentTemplate(),
                            MoneyMovementTransaction.PaymentTemplate().Agency()));

        logger.info("Retrieved " + moneyMovementTransactions.size() + " MMTs");

        return moneyMovementTransactions;
    }

    /**
     * Gets and creates the MMT query
     * @param initiationDate The initiation date to run on
     */
    private DomainEntitySet<MoneyMovementTransaction> getMMTsForQTDMTD(SpcfCalendar initiationDate) {
        SpcfCalendar begin = initiationDate.copy();
        CalendarUtils.clearTime(begin);
        SpcfCalendar end = begin.copy();
        end.addDays(1);
        end.addMilliseconds(-1);


        Criterion<MoneyMovementTransaction> paymentMethodsCriteria =
                MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.CheckPayment, PaymentMethod.ACHCredit);

        Criterion<MoneyMovementTransaction> paymentWhereClause =
                paymentMethodsCriteria
                .And(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().in(QTD_MTD_REPORTS_LIST))
                .And(MoneyMovementTransaction.InitiationDate().between(begin, end))
                .And(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend,
                        TaxPaymentStatus.SentToAgency, TaxPaymentStatus.AcknowledgedByAgency, TaxPaymentStatus.Ignore));


        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                new Query<MoneyMovementTransaction>().Where(paymentWhereClause)
                        .EagerLoad(MoneyMovementTransaction.Company(),
                            MoneyMovementTransaction.Company().LegalAddress(),
                            MoneyMovementTransaction.PaymentFrequency(),
                            MoneyMovementTransaction.PaymentTemplate(),
                            MoneyMovementTransaction.PaymentTemplate().Agency()));

        logger.info("Retrieved " + moneyMovementTransactions.size() + " MMTs");

        return moneyMovementTransactions;
    }

    /**
     * Gets and creates the MMT query
     * @param initiationDate The initiation date to run on
     */
    private DomainEntitySet<MoneyMovementTransaction> getMMTsForMTD(SpcfCalendar initiationDate) {
        SpcfCalendar begin = initiationDate.copy();
        CalendarUtils.clearTime(begin);
        SpcfCalendar end = begin.copy();
        end.addDays(1);
        end.addMilliseconds(-1);

        Criterion<MoneyMovementTransaction> paymentMethodsCriteria =
                MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.CheckPayment, PaymentMethod.ACHCredit);

        PaymentTemplateFrequency paymentTemplateFrequencies[] = new PaymentTemplateFrequency[mtdPaymentTemplateFrequencies.size()];
        paymentTemplateFrequencies = mtdPaymentTemplateFrequencies.toArray(paymentTemplateFrequencies);

        paymentMethodsCriteria = paymentMethodsCriteria.And(MoneyMovementTransaction.PaymentFrequency().in(paymentTemplateFrequencies));

        Criterion<MoneyMovementTransaction> paymentWhereClause =
                paymentMethodsCriteria
                .And(MoneyMovementTransaction.InitiationDate().between(begin, end))
                .And(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend,
                        TaxPaymentStatus.SentToAgency, TaxPaymentStatus.AcknowledgedByAgency, TaxPaymentStatus.Ignore));


        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                new Query<MoneyMovementTransaction>().Where(paymentWhereClause)
                        .EagerLoad(MoneyMovementTransaction.Company(),
                            MoneyMovementTransaction.Company().LegalAddress(),
                            MoneyMovementTransaction.PaymentFrequency(),
                            MoneyMovementTransaction.PaymentTemplate(),
                            MoneyMovementTransaction.PaymentTemplate().Agency()));

        logger.info("Retrieved " + moneyMovementTransactions.size() + " MMTs");

        return moneyMovementTransactions;
    }

    /**
     * Gets and creates the MMT query
     * @param reportDate The reportDate date to run on
     */
    private DomainEntitySet<MoneyMovementTransaction> getMMTsForMOQM(SpcfCalendar reportDate) {

        SpcfCalendar todaysDate =  reportDate.copy();
        todaysDate.addMonths(-1);
        SpcfCalendar startDate = CalendarUtils.getFirstDayOfMonth(todaysDate);
        SpcfCalendar endDate = CalendarUtils.getLastDayOfMonth(todaysDate);
        logger.info("Began date of report for MOQM: " + startDate.toString());
        logger.info("End date of report for MOQM: " + endDate.toString());



        Criterion<MoneyMovementTransaction> paymentMethodsCriteria =
                MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHCredit);


        PaymentTemplateFrequency paymentTemplateFrequency = PaymentTemplateFrequency.getPaymentTemplateFrequency(MOQM_PMT_TEMPLATE, DepositFrequencyCode.QUARTERMONTHLY);

        paymentMethodsCriteria = paymentMethodsCriteria.And(MoneyMovementTransaction.PaymentFrequency().in(paymentTemplateFrequency));

        Criterion<MoneyMovementTransaction> paymentWhereClause =
                paymentMethodsCriteria
                        .And(MoneyMovementTransaction.PaymentTemplate().PaymentTemplateCd().equalTo(MOQM_PMT_TEMPLATE))
                        .And(MoneyMovementTransaction.PaymentPeriodBegin().greaterOrEqualThan(startDate))
                        .And(MoneyMovementTransaction.PaymentPeriodEnd().lessOrEqualThan(endDate))
                        .And(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.ReadyToSend,
                                TaxPaymentStatus.SentToAgency, TaxPaymentStatus.AcknowledgedByAgency, TaxPaymentStatus.Ignore));


        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                new Query<MoneyMovementTransaction>().Where(paymentWhereClause)
                        .EagerLoad(MoneyMovementTransaction.Company(),
                                MoneyMovementTransaction.Company().LegalAddress(),
                                MoneyMovementTransaction.PaymentFrequency(),
                                MoneyMovementTransaction.PaymentTemplate(),
                                MoneyMovementTransaction.PaymentTemplate().Agency()));

        logger.info("Retrieved " + moneyMovementTransactions.size() + " MMTs");

        return moneyMovementTransactions;
    }

    public String changeAmountFormat(SpcfDecimal amount, int dollarsDigits) {
        StringBuffer dollarPattern = new StringBuffer(dollarsDigits);

        for (int i = 0; i < dollarsDigits; i++) {
            dollarPattern.append( "0" );
        }

        DecimalFormat dollarsFormat = new DecimalFormat(dollarPattern.toString());
        String dollarsOutput = dollarsFormat.format(amount.getIntegerPart());
        DecimalFormat centsFormat = new DecimalFormat("00");
        String centsOutput = centsFormat.format(amount.getFractionalPart());

        return dollarsOutput + centsOutput;
    }

    private static boolean isFirstDayOfTheMonth(SpcfCalendar pSpcfCalendar) {
        SpcfCalendar firstDay = CalendarUtils.getFirstDayOfMonth(pSpcfCalendar);
        CalendarUtils.clearTime(pSpcfCalendar);
        return firstDay.equals(pSpcfCalendar);
    }

}
