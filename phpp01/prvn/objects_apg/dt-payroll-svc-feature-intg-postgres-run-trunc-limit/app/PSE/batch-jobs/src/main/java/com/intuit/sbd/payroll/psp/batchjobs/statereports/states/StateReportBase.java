package com.intuit.sbd.payroll.psp.batchjobs.statereports.states;

import com.intuit.payroll.agency.api.IPaymentPeriod;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.collections.SpcfPair;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.paycycle.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;

import java.io.IOException;
import java.util.*;

/**
 * Base class for state coupons
 */
public abstract class StateReportBase {

    /**
     * The string to separate the fields in CSV file
     */
    protected static final String CSV_SEPARATOR = ",";

    protected static final SpcfLogger logger = Application.getLogger(StateReportBase.class);

    /**
     * The list of reports the class supports
     */
    protected String[] reportNamesList;

    protected PSPRequestContextManager pspRequestContextManager;

    public StateReportBase(){
        pspRequestContextManager= PayrollApplicationBeanFactory.getBean(PSPRequestContextManager.class);
    }

    /**
     * Gets the list of reports the class supports
     *
     * @return The list of reports the class supports
     */
    public String[] getReportNamesList() {
        return reportNamesList;
    }

    /**
     * Checks to see if the class handles the report name specified
     *
     * @param reportName The name of the report to check
     * @return True if the class handles the report
     */
    public boolean handlesReport(String reportName) {
        for (String arrayReportName : reportNamesList) {
            if (reportName.equals(arrayReportName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks to see if a state's report is scheduled for the current PSPDate
     *
     * @param paymentTemplateFrequency The frequency of the report to check
     * @param passedInDate             The passed in date for the report
     * @return True if it should be scheduled for current PSPDate and false if not
     */
    public abstract boolean isScheduled(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate);

    /**
     * Creates the report and saves it for the time specified
     *
     * @param paymentTemplateFrequency The name of the PaymentTemplateFrequency to run
     * @param passedInDate             The passed in date for the report
     * @throws IOException
     */
    public abstract void process(PaymentTemplateFrequency paymentTemplateFrequency, SpcfCalendar passedInDate);

    /**
     * Pads a whole integer with the specified number of digits
     *
     * @param number The number to pad
     * @param size   The number of digits
     * @return The padded number
     */
    public String getPaddedWholeNumber(int number, int size) {
        return StringUtil.leftPad(String.valueOf(number), "0", size);
    }

    /**
     * Pads a number with an implied decimal point
     *
     * @param number     The number to pad
     * @param dollarSize The size of dollars to pad
     * @param centsSize  The size of cents to pad
     * @return A number with an implied decimal point
     */
    public String getPaddedMoney(SpcfDecimal number, int dollarSize, int centsSize) {
        return getPaddedMoney(number, dollarSize, centsSize, false);
    }

    /**
     * Pads a number with a decimal point
     *
     * @param number     The number to pad
     * @param dollarSize The size of dollars to pad
     * @param centsSize  The size of cents to pad
     * @param addPeriod  Whether or not to imply the decimal place
     * @return The padded number
     */
    public String getPaddedMoney(SpcfDecimal number, int dollarSize, int centsSize, boolean addPeriod) {
        String paddedMoney = StringUtil.leftPad(String.valueOf(number.abs().getIntegerPart()), "0", dollarSize);

        if (addPeriod) {
            paddedMoney += ".";
        }

        if (centsSize != 0) {
            paddedMoney += StringUtil.leftPad(String.valueOf(number.getFractionalPart()), "0", centsSize);
        }

        return paddedMoney;
    }

    /**
     * Pads a number with an implied decimal point and a positive/negative sign.  The positive is simple a space " ".
     *
     * @param number     The number to pad
     * @param dollarSize The size of dollars to pad
     * @param centsSize  The size of cents to pad
     * @return A number with an implied decimal point and a positive/negative sign
     */
    public String getPaddedMoneyWithSign(SpcfDecimal number, int dollarSize, int centsSize) {
        String paddedMoneyWithSign = number.getSign() == -1 ? "-" : " ";
        paddedMoneyWithSign += getPaddedMoney(number, dollarSize, centsSize);
        return paddedMoneyWithSign;
    }

    /**
     * Crops or pads a string depending on the specified string size
     *
     * @param string The string to crop or pad
     * @param size   The maximum string size to crop or pad to
     * @return The cropped or padded string
     */
    public String cropOrPad(String string, int size) {
        if (string.length() > size) {
            return string.substring(0, size);
        } else {
            return StringUtil.rightPad(string, " ", size);
        }
    }
    /**
     * Crops  a string depending on the specified string size
     *
     * @param string The string to crop
     * @param size   The maximum string size to crop to
     * @return The cropped  string
     */
    public String crop(String string, int size) {
        if (string.length() > size) {
            return string.substring(0, size);
        } else  {
            return string;
        }
    }

    /**
     * Left pads a number with zeros.  If an id is too long, the return value is null
     *
     * @param input The input string to check
     * @param size  The maximum size
     * @return The padded number or null if too long
     */
    public String padAndSizeCheck(String input, int size) {
        if (input.length() > size) {
            return null;
        } else if (input.length() < size) {
            input = StringUtil.leftPad(input, "0", size);
        }

        return input;
    }

    /**
     * Gets all MoneyMovementTransactions for a time period and for a company
     *
     * @param paymentTemplateFrequency The frequency of the state report to run
     * @param startDate                The start date of the time period
     * @param endDate                  The end date of the time period
     * @return All MoneyMovementTransactions for a time period and for a company
     */
    public HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> getMoneyMovementTransactions(PaymentTemplateFrequency paymentTemplateFrequency,
                                                                                                                              SpcfCalendar startDate,
                                                                                                                              SpcfCalendar endDate) {
        // Since HQL doesn't allow substr, additional check and removal are done after fetching
        // can move this to native SQL if performance is not acceptable
        Criterion<MoneyMovementTransaction> paymentWhereClause = MoneyMovementTransaction.InitiationDate().greaterOrEqualThan(startDate)
                                                                                         .And(MoneyMovementTransaction.MoneyMovementPaymentMethod().in(PaymentMethod.ACHCredit, PaymentMethod.CheckPayment, PaymentMethod.ACHDirectDeposit))
                                                                                         .And(MoneyMovementTransaction.PaymentPeriodEnd().between(startDate, endDate))
                                                                                         .And(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.SentToAgency, TaxPaymentStatus.AcknowledgedByAgency, TaxPaymentStatus.ReadyToSend, TaxPaymentStatus.Ignore))
                                                                                         .And(MoneyMovementTransaction.PaymentTemplate().equalTo(paymentTemplateFrequency.getPaymentTemplate()));

        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class,
                                                                                               new Query<MoneyMovementTransaction>().Where(paymentWhereClause)
                                                                                                                                    .OrderBy(MoneyMovementTransaction.PaymentPeriodBegin(), MoneyMovementTransaction.Company().LegalName())
                                                                                                                                    .EagerLoad(MoneyMovementTransaction.Company(), MoneyMovementTransaction.PaymentFrequency()));

        HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>> companyToMoneyMovementTransactions = new
                HashMap<Company, SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>>();
        HashSet<Company> badCompanies = new HashSet<Company>();
        SpcfCalendar now = PSPDate.getPSPTime();

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            Company theCompany = moneyMovementTransaction.getCompany();
            if (!badCompanies.contains(theCompany)) {
                if (companyToMoneyMovementTransactions.containsKey(theCompany)) {
                    companyToMoneyMovementTransactions.get(theCompany).getValueItem().add(moneyMovementTransaction);
                } else {
                    EffectiveDepositFrequency companyEffectiveDepositFrequency = EffectiveDepositFrequency.findEffectiveDepositFrequencyAtDate(theCompany, paymentTemplateFrequency.getPaymentTemplate(), endDate);
                    if (companyEffectiveDepositFrequency.getPaymentTemplateFrequency().getPaymentFrequencyId() != paymentTemplateFrequency.getPaymentFrequencyId()) {
                        badCompanies.add(theCompany);
                    } else {
                        SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>> pair = new SpcfPair<DepositFrequencyCode, ArrayList<MoneyMovementTransaction>>();
                        pair.setKeyItem(paymentTemplateFrequency.getPaymentFrequencyId());
                        pair.setValueItem(new ArrayList<MoneyMovementTransaction>());
                        pair.getValueItem().add(moneyMovementTransaction);
                        companyToMoneyMovementTransactions.put(theCompany, pair);
                    }
                }
            }
        }

        return companyToMoneyMovementTransactions;
    }

    /**
     * Gets the total payments for all MoneyMovementTransaction passed in
     *
     * @param moneyMovementTransactions The MoneyMovementTransactions to total
     * @return The total payments for all MoneyMovementTransaction passed in
     */
    public SpcfMoney getTotalPayments(ArrayList<MoneyMovementTransaction> moneyMovementTransactions) {
        SpcfMoney total = SpcfMoney.ZERO;

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            total = (SpcfMoney) total.add(moneyMovementTransaction.getMoneyMovementTransactionAmount());
        }

        return total;
    }

    /**
     * Gets the total liabilities for all MoneyMovementTransaction passed in
     *
     * @param moneyMovementTransactions The MoneyMovementTransactions to total
     * @return The total liabilities for all MoneyMovementTransaction passed in
     */
    public SpcfMoney getTotalLiabilities(ArrayList<MoneyMovementTransaction> moneyMovementTransactions) {
        SpcfDecimal total = SpcfMoney.ZERO;

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            DomainEntitySet<FinancialTransaction> financialTransactions = moneyMovementTransaction.getFinancialTransactionCollection()
                                                                                                  .find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Created,
                                                                                                                                                                               TransactionStateCode.Executed,
                                                                                                                                                                               TransactionStateCode.Completed));

            for (FinancialTransaction financialTransaction : financialTransactions) {
                if (TransactionType.addsToPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                    total = total.add(financialTransaction.getFinancialTransactionAmount());
                } else if (TransactionType.subtractsFromPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                    total = total.subtract(financialTransaction.getFinancialTransactionAmount());
                }
            }
        }

        return new SpcfMoney(total);
    }

    /**
     * Gets the liabilities map for all MoneyMovementTransaction passed in for laws
     *
     * @param moneyMovementTransactions The MoneyMovementTransactions to total
     * @return The total liabilities for all MoneyMovementTransaction passed in
     */
    public Map<String, SpcfDecimal> getTotalLiabilitiesLawIdMap(ArrayList<MoneyMovementTransaction> moneyMovementTransactions) {
        Map<String, SpcfDecimal> liabilitiesMap = new HashMap<String, SpcfDecimal>();

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            DomainEntitySet<FinancialTransaction> financialTransactions = moneyMovementTransaction.getFinancialTransactionCollection()
                                                                                                  .find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().in(TransactionStateCode.Created,
                                                                                                                                                                               TransactionStateCode.Executed,
                                                                                                                                                                               TransactionStateCode.Completed));

            for (FinancialTransaction financialTransaction : financialTransactions) {

                Law law = financialTransaction.getLaw();
                if (law == null) {
                    logger.warn("Law is null for FT " + financialTransaction.getId() + " amount " + financialTransaction.getFinancialTransactionAmount() +
                                        " for company " + financialTransaction.getCompany().getSourceCompanyId());
                    continue;
                }
                String lawId = financialTransaction.getLaw().getLawId();
                if (StringUtils.isNotEmpty(lawId)) {
                    SpcfDecimal total = liabilitiesMap.containsKey(lawId) ? liabilitiesMap.get(lawId) : SpcfMoney.ZERO;
                    if (TransactionType.addsToPayment(financialTransaction.getTransactionType().getTransactionTypeCd())) {
                        total = total.add(financialTransaction.getFinancialTransactionAmount());
                    } else if (TransactionType.subtractsFromPayment(financialTransaction.getTransactionType().getTransactionTypeCd())
                            && financialTransaction.getRelatedTransactionsCollection().size() == 0) {
                        total = total.subtract(financialTransaction.getFinancialTransactionAmount());
                    }
                    liabilitiesMap.put(lawId, total);
                }
            }
        }

        return liabilitiesMap;
    }

    /**
     * Saves the StateCoupon object to the database
     *
     * @param builder                  The builder containing the output
     * @param startDate                The start date for the report
     * @param endDate
     * @param paymentTemplateFrequency
     */
    public void saveStateCoupon(StringBuilder builder, SpcfCalendar startDate, SpcfCalendar endDate, StateReportType stateReportType,
                                PaymentTemplateFrequency... paymentTemplateFrequency) {
        StateReportOutput reportOutput = new StateReportOutput();
        reportOutput.setBeginDate(startDate);
        reportOutput.setEndDate(endDate);
        reportOutput.setReportType(stateReportType);

        Application.save(reportOutput);
        reportOutput.setReportOutput(builder.toString());

        for (PaymentTemplateFrequency templateFrequency : paymentTemplateFrequency) {
            StateReportAssoc stateReportAssoc = new StateReportAssoc();
            stateReportAssoc.setPaymentTemplateFrequency(templateFrequency);
            stateReportAssoc.setStateReportOutput(reportOutput);

            Application.save(stateReportAssoc);
        }
    }

    /**
     * Gets the end day of the previous month
     *
     * @return The end day of the previous month
     */
    public SpcfCalendar getBeginningOfMonth(SpcfCalendar endDate) {
        SpcfCalendar startDate = endDate.copy();
        CalendarUtils.clearTime(startDate);
        startDate.addDays((startDate.getDay() - 1) * -1);
        return startDate;
    }

    /**
     * Gets the end day of the previous month
     *
     * @return The end day of the previous month
     */
    public SpcfCalendar getEndOfLastMonth(SpcfCalendar startDate) {
        SpcfCalendar endOfMonth = startDate.copy();
        endOfMonth.addDays((endOfMonth.getDay() - 1) * -1);
        CalendarUtils.clearTime(endOfMonth);
        endOfMonth.addMilliseconds(-1);

        return endOfMonth;
    }

    /**
     * Checks a day to see if the is scheduled to run on it
     *
     * @param paymentTemplateFrequency The frequency of the report to check
     * @param day                      The day that the report is scheduled to run
     * @param businessDays             The number of businessDays to go back
     * @param rollForward              If the due date moves forward or backward on a holiday
     * @return If the report should be run that day
     */
    protected boolean checkDay(PaymentTemplateFrequency paymentTemplateFrequency, int day, int businessDays, boolean rollForward) {
        SpcfCalendar now = PSPDate.getPSPTime().toUtc();
        CalendarUtils.clearTime(now);

        SpcfCalendar scheduledDate = now.copy();
        scheduledDate.addDays(scheduledDate.getDay() * -1);
        scheduledDate.addDays(day);

        if (CalendarUtils.isHoliday(scheduledDate)) {
            if (rollForward) {
                CalendarUtils.addBusinessDays(scheduledDate, 1);
            } else {
                CalendarUtils.addBusinessDays(scheduledDate, -1);
            }
        }

        CalendarUtils.addBusinessDays(scheduledDate, businessDays * -1);

        boolean isScheduled = now.equals(scheduledDate);

        if (isScheduled) {
            logger.info("Will run " + paymentTemplateFrequency.getPaymentFrequencyId().toString() + " for class " +
                                getClass().getSimpleName() + " and for today " + scheduledDate.format("yyyy/MM/dd"));
        } else {
            logger.info("Will not run " + paymentTemplateFrequency.getPaymentFrequencyId().toString() + " for class " +
                                getClass().getSimpleName() + " and scheduled date is " + scheduledDate.format("yyyy/MM/dd"));
        }

        return isScheduled;
    }

    /**
     * Gets the start of the previous quarter for date passed in
     *
     * @param passedInDate The date to use
     * @return The previous quarter for date passed in
     */
    protected SpcfCalendar getPreviousQuarter(SpcfCalendar passedInDate) {
        SpcfCalendar startDate;
        startDate = CalendarUtils.getFirstDayOfQuarter(passedInDate);
        startDate.addDays(-1);
        startDate = CalendarUtils.getFirstDayOfQuarter(startDate);
        return startDate;
    }

    /**
     * Gets all IA BEN Numbers mapped to a company
     *
     * @return A map containing all companies with a IA BEN Number.  The map is company PSID to IA BEN Number.
     */
    protected HashMap<String, String> getIABENNumber() {
        String[] paramNames = new String[0];
        Object[] paramValues = new Object[0];

        String namedQuery = "findIABENNumbersENC";

        List<Object[]> retList = Application.executeNamedQuery(namedQuery, paramNames, paramValues);

        HashMap<String, String> companyPSIDToIABENNumber = new HashMap<String, String>();

        for (Object[] objects : retList) {
            String benNumber = (String) objects[1];
            benNumber = EncryptionUtils.deterministicDecrypt(CompanyPaymentTemplateAgencyId.AgencyTaxPayerIdKeyName, benNumber);
            companyPSIDToIABENNumber.put((String) objects[0], benNumber);
        }
        return companyPSIDToIABENNumber;
    }

    /**
     * Gets the previous period start and end date for the passed in date
     *
     * @param passedInDate             The passed in start date
     * @param paymentTemplateFrequency The frequency for the report
     * @return An array with start date as index 0 and end date as index 1
     */
    protected SpcfCalendar[] getPreviousPeriodStartAndEnd(SpcfCalendar passedInDate, PaymentTemplateFrequency paymentTemplateFrequency) {
        // Get the current period start date
        IPaymentPeriod currentPaymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateFrequency.getPaymentTemplate().getPaymentTemplateCd(),
                paymentTemplateFrequency.getPaymentFrequencyId().toString(),
                CalendarUtils.convertToRulesCalendar(passedInDate));
        SpcfCalendar currentStartDate = CalendarUtils.convertToSpcfCalendar(currentPaymentPeriod.getFromAccrualDate());

        // Subtract one day to get into the previous period
        currentStartDate.addDays(-1);

        // Get the previous period start and end
        IPaymentPeriod previousPaymentPeriod = MoneyMovementTransaction.getPaymentPeriod(
                paymentTemplateFrequency.getPaymentTemplate().getPaymentTemplateCd(),
                paymentTemplateFrequency.getPaymentFrequencyId().toString(),
                CalendarUtils.convertToRulesCalendar(currentStartDate));
        SpcfCalendar startDate = CalendarUtils.convertToSpcfCalendar(previousPaymentPeriod.getFromAccrualDate());
        SpcfCalendar endDate = CalendarUtils.convertToSpcfCalendar(previousPaymentPeriod.getToAccrualDate());

        endDate.addDays(1);
        endDate.addMilliseconds(-1);

        return new SpcfCalendar[]{startDate, endDate};
    }


    /**
     * returns initiationDate by adding businessDays to passedInDate
     * @return start and end of initiationDate
     */
    public SpcfCalendar[] getStartEndDateByPassedInDate(SpcfCalendar passedInDate, int businessDays) {

        SpcfCalendar startDate = passedInDate.copy();
        CalendarUtils.clearTime(startDate);
        CalendarUtils.addBusinessDays(startDate, businessDays);
        SpcfCalendar endDate = startDate.copy();
        endDate.addDays(1);
        endDate.addMilliseconds(-1);

        return new SpcfCalendar[]{startDate, endDate};
    }

    /**
     * Prepares and checks the state agency id for a MMT
     *
     * @param mmt         The mmt containing the state agency id
     * @param maximumSize The maximum state agency id size or -1 for no maximum size
     * @return The padded state agency id or null if too long
     */
    protected String prepareStateAgencyId(MoneyMovementTransaction mmt, int maximumSize) {
        String stateAgencyId = mmt.getAgencyTaxpayerId();

        if (stateAgencyId == null) {
            stateAgencyId = "";
        }

        // Remove all except for letters and numbers from state agency id
        stateAgencyId = stateAgencyId.replaceAll("\\W", "");

        if (maximumSize == -1) {
            return stateAgencyId;
        } else {
            return padAndSizeCheck(stateAgencyId, maximumSize);
        }
    }

    /**
     * Adds a cell to the report
     *
     * @param builder The builder to append to
     * @param strings The strings to append
     */
    protected void addCell(StringBuilder builder, String... strings) {
        builder.append("=\"");

        for (String string : strings) {
            builder.append(string);
        }

        builder.append("\"").append(CSV_SEPARATOR);
    }
}
