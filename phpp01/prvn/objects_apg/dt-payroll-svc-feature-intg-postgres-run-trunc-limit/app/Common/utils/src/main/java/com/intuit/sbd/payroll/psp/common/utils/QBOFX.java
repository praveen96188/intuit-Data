package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: jchickanosky
 * Date: Feb 21, 2008
 * Time: 2:46:17 PM
 */
public class QBOFX {
    private static final Pattern BANKACCTTO_PATTERN = Pattern.compile ("^\\s*<BANKACCTTO>.*$", Pattern.MULTILINE | Pattern.UNIX_LINES);
    private static final Pattern BANKACCTFROM_PATTERN = Pattern.compile ("\\s*<BANKACCTFROM>.*</BANKACCTFROM>\\s*", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNIX_LINES);
    private static final Pattern ACCTTYPE_PATTERN = Pattern.compile ("^\\s*<ACCTTYPE>\\s*$|^\\s*<ACCTTYPE>UNKNOWN.*$", Pattern.MULTILINE | Pattern.UNIX_LINES);
    private static final Pattern BANKID_PATTERN = Pattern.compile ("^\\s*<BANKID>\\s*$|^\\s*<BANKID>\\^@~\\*\\s*$", Pattern.MULTILINE | Pattern.UNIX_LINES);
    private static final Pattern ACCTID_PATTERN = Pattern.compile ("^\\s*<ACCTID>\\s*$|^\\s*<ACCTID>\\^@~\\*\\s*$", Pattern.MULTILINE | Pattern.UNIX_LINES);
    private static final Pattern decimalPattern = Pattern.compile("[^-\\.0-9]");
    private static final Pattern intPattern = Pattern.compile("[^-0-9]");
    private static final Pattern SEVERITY_ERROR_PATTERN = Pattern.compile ("^\\s*<SEVERITY>ERROR.*$", Pattern.MULTILINE | Pattern.UNIX_LINES);
    private static final Pattern TAXREADY_PATTERN = Pattern.compile ("^\\s*<I.TAXREADY>.*$", Pattern.MULTILINE | Pattern.UNIX_LINES);
    private static final Pattern TAXCANCELLED_PATTERN = Pattern.compile ("^\\s*<I\\.TAXSERVMODE>TERMINATED.*$", Pattern.MULTILINE | Pattern.UNIX_LINES);

    public static final String LANGUAGE = "ENG";

    public static final String NULL = "^@~*";
    public static final String EMPTY_STR = NULL;
    public static final String LEADING_SPACE = "[~ ";
    public static final String MISSING_FILE_ID = "<MissingFileId>";

    public static final String DEFAULT_CLEARED_RESPONSE_STR = "0";
    public static final String DEFAULT_PITEM_ID = "0";
    public static String SUCCESS_STATUS_CODE = "0";

    public static final String ZERO_DOLLAR_AMT_STR = "$0.00";

    public static final String OFX_DATE_FORMAT = "yyyyMMdd";
    public static final ThreadSafeSimpleDateFormat OFX_DATE_FORMATER = new ThreadSafeSimpleDateFormat(OFX_DATE_FORMAT);

    public static final String DATE_FORMAT = "MM/dd/yyyy";
    public static final String DATE_FORMAT_TIME = "yyyyMMddHHmmss";
    public static ThreadSafeSimpleDateFormat GMT_DATE_FORMATTER = new ThreadSafeSimpleDateFormat(DATE_FORMAT_TIME);
    public static ThreadSafeSimpleDateFormat CLIENT_DATE_FORMATTER = new ThreadSafeSimpleDateFormat(DATE_FORMAT_TIME);
    public static ThreadSafeSimpleDateFormat DATE_FORMATTER = new ThreadSafeSimpleDateFormat(DATE_FORMAT);

    static{
        GMT_DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public class PAYROLL_TX_TYPE {
        public static final String PAYROLL_LIAB_CHECK = "LIABCHK";
        public static final String DD_RETURN = "DDRETURN";
    }

    public static class OFX_YESNO {
        public static final String YES = "YES";
        public static final String NO = "NO";
    }
    public static class OFX_YN {
        public static final String Y = "Y";
        public static final String N = "N";
    }
    public static class MESSAGE_SEVERITY {
        public static final String INFO = "INFO";
        public static final String ERROR = "ERROR";
    }

    public static class DD_MODES {
        public static final String ACTIVE = "ACTIVE";
        public static final String TERMINATED  = "TERMINATED";
    }

    public static class TAX_MODES {
        public static final String ACTIVE = "ACTIVE";
        public static final String TERMINATED  = "TERMINATED";
        public static final String VERIFIED  = "VERIFIED";
        public static final String NO = OFX_YESNO.NO;
    }

    public static class GUIDELINE_MODES {
        public static final String ACTIVE = "ACTIVE";
        public static final String CANCELLED  = "CANCELLED";
        public static final String PENDING_ENROLLMENT  = "PENDING_ENROLLMENT";
        public static final String PENDING_SETUP  = "PENDING_SETUP";
    }
    public static class MEMOS {
        public static final String CREATED_BY_PAYROLL_SERVICE = "Created by Payroll Service on ";
        public static final String EXPENSE_ACCOUNT_FOR = " Expense Account for ";
        public static final String SALES_TAX_MEMO = "Sales Tax for ";

        public static String getSalesTaxMemo(String salesTaxForStr) {
            return SALES_TAX_MEMO + salesTaxForStr;
        }

        public static String getCreatedByPayrollServiceMemo(Date currentDttm) {
            return CREATED_BY_PAYROLL_SERVICE + DATE_FORMATTER.format(currentDttm);
        }

        public static String getExpenseAccountForMemo(String sourceDescription) {
            return EXPENSE_ACCOUNT_FOR + sourceDescription;
        }

        public static String getVoidAfterOffloadMemoStr(String empName) {
            return VOID.DD_OVERPAYMENT + empName;
        }

        public static class VOID {
            public static final String ADJUSTED_FOR_VOIDED_PAYCHECK = "Adjusted for voided paycheck(s)";
            public static final String OVERPAYMENT_TO_TAXING_AGENCY = "Overpayment to taxing agency";
            public static final String EXCESS_TAX_RESULT_OF_VOID = "Excess tax as result of void or adjustment";
            public static final String DD_OVERPAYMENT = "Overpayment to ";
        }

        public static class DEBIT_REDUCED {
            public static final String APPLIED_EXCESS_TAX_FUNDS = "Applied Excess Tax Funds";
            public static final String APPLIED_OVERPAID_TAX_FUNDS = "Applied Overpaid Tax Funds";
        }
    }

    public static class AGENCIES {
        public static final String QUICKBOOKS_PAYROLL_SERVICE = "QuickBooks Payroll Service";
        public static final String INTERNAL_REVENUE_SERVICE = "Internal Revenue Service";
    }

    public static class ACCOUNTS {
        public static final String ASSET_ACCOUNT_NAME = "Payroll Service Customer Asset";
        public static final String DEFAULT_FEE_ACCOUNT_NAME = "Payroll Expenses";
        public static final String DEFAULT_SALES_TAX_ACCOUNT_NAME = DEFAULT_FEE_ACCOUNT_NAME;
    }

    public static class QB_APP_ID_VERSIONS {
        public static final String QBW = "QBW";
        public static final String QBWPRO = "QBWPRO";
    }

    /**
     * Return the DTTX OFX response String.
     *
     * @return - Response DTTM response string.
     */
    public static String getDTTXResponse(Date currentDttm) {
        return OFX_DATE_FORMATER.format(currentDttm);
    }

    /**
     * Get current date in java date string format passed in.
     *
     * @param javaDateFormatStr - Java date format.
     * @return - Current date string in format provided.
     */
    public static String getDate(String javaDateFormatStr, Date date) {
        DateFormat dfm = new SimpleDateFormat(javaDateFormatStr);
        return dfm.format(date);
    }

    /**
     * Returns the current GMT time in a yyyyMMddHHmmss string.
     *
     * @return - OFX server dttm in GMT.
     */
    public static String getOFXServerDTTM(Date currDate) {
        return GMT_DATE_FORMATTER.format(currDate);
    }

    /**
     * Returns the Client GMT time in a yyyyMMddHHmmss string.
     *
     * @return - OFX server dttm in GMT.
     */
    public static String getOFXClientDTTM(Calendar pCalendar) {
        Date currDate = pCalendar.getTime();
        return CLIENT_DATE_FORMATTER.format(currDate);
    }

    public static String convertToOFXDate(SpcfCalendar pSpcfCalendar) {
        if(pSpcfCalendar == null) {
            return NULL;
        }
        return OFX_DATE_FORMATER.format(new Date(pSpcfCalendar.toLocal().getTimeInMilliseconds()));
    }

    /**
     * Return int token value.
     *
     * @param token
     * @return
     */
    public static long tokenVal(String token) {
        if (token.compareTo("")==0) {
            return 0;
        }
        return Integer.parseInt(token);
    }

    /**
     * Returns true if the OFX response string contain the string <SEVERITY>ERROR.
     *
     * @param ofxString - OFX string to search
     * @return - true if found, false if not.
     */
    public static boolean ofxStringContainsErrorSeverity(String ofxString) {        
        Matcher m = SEVERITY_ERROR_PATTERN.matcher(ofxString);
        return m.find();
    }

    /**
     * Determines if the OFX is a balance file.  It does so by searching the OFX string for
     *    the tag <I.TAXREADY>.
     * @param ofxString - OFX Request String
     * @return - true if balance file, false otherwise.
     */
    public static boolean isOFXBalanceFile(String ofxString) {
        Matcher m = TAXREADY_PATTERN.matcher(ofxString);
        return m.find();
    }

    public static boolean isTermResponse(String ofxString) {
        Matcher m = TAXCANCELLED_PATTERN.matcher(ofxString);
        return m.find();
    }

    public static String getTaxFormLineFromValue(String ofxValue) {
        if (nullStringCheck(ofxValue) == null) {
            return null;
        }

        String taxFormLine = ofxValue.replace("_", "");
        if (taxFormLine.substring(0,1).matches("[0-9]")) {
            taxFormLine = "Q"+taxFormLine;
        }

        return taxFormLine;
    }

    public static String getTaxFormLineFromOFXValue(String value) {
        if (value == null) {
            return NULL;
        }

        if(value.equals(TaxFormLine.Q125MEDCARE.toString())) {
            return "125_MEDCARE";
        } else if(value.equals(TaxFormLine.Q125POP.toString())) {
            return "125_POP";
        } else if(value.equals(TaxFormLine.DPDNTCARECO.toString())) {
            return "DPDNTCARE_CO";
        } else if(value.equals(TaxFormLine.LTAXCO.toString())) {
            return "LTAX_CO";
        } else if(value.equals(TaxFormLine.SECLOCAL.toString())) {
            return "SEC_LOCAL";
        } else if (value.length() > 2 && value.substring(0,1).matches("Q") && value.substring(1,2).matches("[0-9]")) {
            value = value.substring(1,value.length());
        }

        return value;
    }

    public static QbdtSpecialType getSpecialTypeFromOFXValue(String ofxValue) {
        if (nullStringCheck(ofxValue) == null) {
            return null;
        }

        String specialType = ofxValue.trim().replace("_", "");

        QbdtSpecialType returnValue = null;

        try {
            returnValue = QbdtSpecialType.valueOf(specialType);
        } catch (Throwable t) {
            returnValue=null;
        }

        return returnValue;
    }

    public static String getOFXSpecialType(QbdtSpecialType value) {
        if(value == null) {
            return null;
        } else if(value == QbdtSpecialType.WORKERCOMP) {
            return "WORKER_COMP";
        } else if(value == QbdtSpecialType.VACHRLY) {
            return "VAC_HRLY";
        } else if(value == QbdtSpecialType.VACSALARY) {
            return "VAC_SALARY";
        } else if(value == QbdtSpecialType.SICKHRLY) {
            return "SICK_HRLY";
        } else if(value == QbdtSpecialType.SICKSALARY) {
            return "SICK_SALARY"; 
        } else {
            return value.toString();
        }
    }

    public static boolean mapOFXStringToBoolean(String ofxValue) {
        if (nullStringCheck(ofxValue) == null || ofxValue.equalsIgnoreCase("N")) {
            return false;
        } else {
            return true;
        }
    }

    public static double mapOFXStringToDouble(String ofxValue) {
        if (nullStringCheck(ofxValue) == null) {
            return 0;
        } else {
            try {
                ofxValue=decimalPattern.matcher(ofxValue).replaceAll("");
                if(ofxValue.indexOf(".") > 12) {
                    throw new LargeNumberException("Value " + ofxValue +" has too many digits, and is not supported. Supported format is (12,7).");
                }
                return Double.parseDouble(ofxValue);
            } catch (Exception e) {
                if(e instanceof LargeNumberException) {
                    throw (RuntimeException)e;
                } else  {
                    return 0;
                }
            }

        }
    }

    public static int mapOFXStringToInt(String ofxValue) {        
        if (nullStringCheck(ofxValue) == null) {
            return 0;
        } else {
            ofxValue = intPattern.matcher(ofxValue).replaceAll("");
            try {
                return Integer.parseInt(ofxValue);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    public static SpcfMoney mapOFXStringToMoney(String ofxValue) {
        if (nullStringCheck(ofxValue) == null) {
            return null;
        } else  {
            try {
                ofxValue=decimalPattern.matcher(ofxValue).replaceAll("");
                if(ofxValue.indexOf(".") > 12) {
                    throw new LargeNumberException("Value " + ofxValue +" has too many digits, and is not supported. Supported format is (12,2).");
                }
                return new SpcfMoney(ofxValue);
            } catch (Exception e) {
                if(e instanceof LargeNumberException) {
                    throw (RuntimeException)e;
                } else  {
                    return new SpcfMoney("0");
                }
            }
        }
    }

    public static String convertSpcfMoneyToOFXString(SpcfMoney pSpcfMoney) {
        if(pSpcfMoney == null) {
            return NULL;
        } else {
            return "$" + pSpcfMoney.toString();
        }
    }

    public static String convertSpcfDecimalToOFXString(SpcfDecimal pSpcfDecimal) {
        if(pSpcfDecimal == null) {
            return NULL;
        } else {
            return pSpcfDecimal.toString();
        }
    }

    public static String convertDoubleToOFXString(double pValue) {
        SpcfDecimal spcfDecimal = SpcfDecimal.createInstance(pValue);
        spcfDecimal = spcfDecimal.setScale(6, SpcfDecimal.SpcfRoundingType.HalfUp);
        return truncZeros(spcfDecimal.toString());
    }

    public static String mapNumericTypeToString(QbdtNumericType pQbdtNumericType, double pValue) {
        if(pQbdtNumericType == null) {
            if(pValue == 0) {
                return NULL;
            } else {
                return convertDoubleToOFXString(pValue);
            }
        }

        switch(pQbdtNumericType) {
            case MoneyType:
                return "$" + new SpcfMoney(SpcfDecimal.createInstance(pValue));
            case Percentage:
                return convertDoubleToOFXString(pValue) + "%";
        }

        return null;
    }

    public static QbdtNumericType mapOFXStringNumericType(String ofxValue) {
        if (nullStringCheck(ofxValue) == null) {
            return null;
        } else if(ofxValue.contains("$"))  {
            return QbdtNumericType.MoneyType;
        } else if(ofxValue.contains("%"))  {
            return QbdtNumericType.Percentage;
        }
        return null;
    }

    public static OFXPayrollItemType mapOFXPayrollItemType(PayrollItemCode pPayrollItemCode) {
        // employer contribution
        if(pPayrollItemCode == PayrollItemCode.OtherTaxableEmployerContribution ||
                pPayrollItemCode == PayrollItemCode.OtherNonTaxableEmployerContribution ||
                pPayrollItemCode == PayrollItemCode.Tp401kSafeHarbor ||
                pPayrollItemCode == PayrollItemCode.Tp401kProfitSharing) {
            return OFXPayrollItemType.EmployerContribution;
        }

        // compensation
        else if(pPayrollItemCode == PayrollItemCode.Bonus) {
            return OFXPayrollItemType.Bonus;
        } else if(pPayrollItemCode == PayrollItemCode.Commission) {
            return OFXPayrollItemType.Commission;
        } else if(pPayrollItemCode == PayrollItemCode.Hourly) {
            return OFXPayrollItemType.Hourly;
        } else if(pPayrollItemCode == PayrollItemCode.Salary) {
            return OFXPayrollItemType.Salary;
        }

        // deductions
        else if(pPayrollItemCode == PayrollItemCode.OtherAdditionPreTax ||
                pPayrollItemCode == PayrollItemCode.OtherAdditionPostTax) {
            return  OFXPayrollItemType.Addition;
        } else if(pPayrollItemCode == PayrollItemCode.OtherPreTaxDeduction ||
                pPayrollItemCode == PayrollItemCode.OtherPostTaxDeduction ||
                pPayrollItemCode == PayrollItemCode.Tp401kEmployeeDeferral ||
                pPayrollItemCode == PayrollItemCode.Tp401kRoth ||
                pPayrollItemCode == PayrollItemCode.Tp401kLoanPayment) {
            return OFXPayrollItemType.Deduction;
        } else if(pPayrollItemCode == PayrollItemCode.DirectDeposit) {
            return OFXPayrollItemType.DirectDeposit;
        }

        return null;
    }

    public static PayrollItemCode mapPayrollItemCode(OFXPayrollItemType pPayrollItemType, int pTaxableToItemCount) {

        // employer contribution
        if(pPayrollItemType == OFXPayrollItemType.EmployerContribution) {
            if(pTaxableToItemCount > 0) {
                return PayrollItemCode.OtherTaxableEmployerContribution;
            } else {
                return PayrollItemCode.OtherNonTaxableEmployerContribution;
            }
        }

        // compensation
        else if(pPayrollItemType == OFXPayrollItemType.Bonus) {
            return PayrollItemCode.Bonus;
        } else if(pPayrollItemType == OFXPayrollItemType.Commission) {
            return PayrollItemCode.Commission;
        } else if(pPayrollItemType == OFXPayrollItemType.Hourly) {
            return PayrollItemCode.Hourly;
        } else if(pPayrollItemType == OFXPayrollItemType.Salary) {
            return PayrollItemCode.Salary;
        }

        // deductions
        else if(pPayrollItemType == OFXPayrollItemType.Addition) {
            if(pTaxableToItemCount > 0) {
                return PayrollItemCode.OtherAdditionPreTax;
            } else {
                return PayrollItemCode.OtherAdditionPostTax;
            }
        } else if(pPayrollItemType == OFXPayrollItemType.Deduction) {
            if(pTaxableToItemCount > 0) {
                return PayrollItemCode.OtherPreTaxDeduction;
            } else {
                return PayrollItemCode.OtherPostTaxDeduction;
            }
        } else if(pPayrollItemType == OFXPayrollItemType.DirectDeposit) {
            return PayrollItemCode.DirectDeposit;
        }

        return null;
    }

    public static PayrollItemType mapPayrollItemType(OFXPayrollItemType pPayrollItemType) {
        if(pPayrollItemType == null) {
            return null;
        }

        switch (pPayrollItemType) {
            case Addition:
            case Deduction:
            case DirectDeposit:
                return PayrollItemType.Deduction;
            case EmployerContribution:
                return PayrollItemType.EmployerContribution;
            case Bonus:
            case Commission:
            case Hourly:
            case Salary:
                return PayrollItemType.Compensation;
            default:
                return null;
        }
    }

    public static PayrollFrequencyCode mapPayrollFrequency(OFXPayrollFrequency pOFXPayPeriod) {
        if(pOFXPayPeriod == null) {
            return null;
        }

        switch (pOFXPayPeriod) {
            case SEMIMONTHLY:
                return PayrollFrequencyCode.SemiMonthly;
            case BIWEEKLY:
                return PayrollFrequencyCode.BiWeekly;
            case WEEKLY:
                return PayrollFrequencyCode.Weekly;
            case MONTHLY:
                 return PayrollFrequencyCode.Monthly;
            case DAILY:
                return PayrollFrequencyCode.Daily;
            case YEARLY:
                return PayrollFrequencyCode.Annually;
            case QUARTERLY:
                return PayrollFrequencyCode.Quarterly;
            default:
                return null;
        }

    }

    public static String mapPayrollFrequencyToOFXValue(PayrollFrequencyCode pPayPeriod) {
        if(pPayPeriod == null) {
            return null;
        }

        switch (pPayPeriod) {
            case SemiMonthly:
                return OFXPayrollFrequency.SEMIMONTHLY.toString();
            case BiWeekly:
                return OFXPayrollFrequency.BIWEEKLY.toString();
            case Weekly:
                return OFXPayrollFrequency.WEEKLY.toString();
            case Monthly:
                return OFXPayrollFrequency.MONTHLY.toString();
            case Daily:
                return OFXPayrollFrequency.DAILY.toString();
            case Annually:
                return OFXPayrollFrequency.YEARLY.toString();
            case Quarterly:
                return OFXPayrollFrequency.QUARTERLY.toString();
            default:
                return null;
        }

    }

    public static Gender mapGender(OFXGender pGender) {
        if(pGender == null) {
            return null;
        }

        switch (pGender) {
            case M:
                return Gender.Male;
            case F:
                return Gender.Female;
            default:
                return null;
        }
    }

    public static String mapGender(Gender pGender) {
        if (pGender == null) {
            return "UNKNOWN";
        } else {
            return pGender.toString().toUpperCase();
        }
    }

    public static QbdtEmployeeType mapEmployeeType(OFXEmployeeType pEmployeeType) {
        if(pEmployeeType == null) {
            return null;
        }

        switch (pEmployeeType) {
            case REG:
                return QbdtEmployeeType.REG;
            case OFFICER:
                return QbdtEmployeeType.OFFICER;
            case OWNER:
                return QbdtEmployeeType.OWNER;
            case STATUTORY:
                return QbdtEmployeeType.STATUTORY;
            case REP:
                return QbdtEmployeeType.REP;
            default:
                return null;
        }

    }

    public static Date mapOFXStringToDate(String ofxValue) {
        if (nullStringCheck(ofxValue) == null) {
            return null;
        }

        try {
            return OFX_DATE_FORMATER.parse(ofxValue);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static AccrualPeriod mapOFXAccrualPeriod(String ofxValue) {
        if (nullStringCheck(ofxValue) == null) {
            return null;
        }

        if(ofxValue.equals("YEARLY")) {
            return AccrualPeriod.Yearly;
        } else if(ofxValue.equals("PAYROLL")) {
            return AccrualPeriod.Payroll;
        } else if(ofxValue.equals("HOURLY")) {
            return AccrualPeriod.Hourly;
        }

        return null;
    }

    public static WagePlanNameCode mapOFXWagePlanNameCode(String ofxValue) {
        if (nullStringCheck(ofxValue) == null) {
            return null;
        }

        return WagePlanNameCode.valueOf(ofxValue);
    }

    public static WagePlanDomainCode mapOFXWagePlanDomainCode(String ofxValue) {
        if (nullStringCheck(ofxValue) == null) {
            return null;
        }

        return WagePlanDomainCode.valueOf(ofxValue);
    }

    public static BankAccountType mapOFXStringToBankAccountType(String ofxValue) {
        if (nullStringCheck(ofxValue) == null) {
            return null;
        }
        
        switch (OFXBankAccountType.valueOf(ofxValue.trim())) {
            case CHECKING:
                return BankAccountType.Checking;
            case SAVINGS:
                return BankAccountType.Savings;
            default:
                return null;
        }
    }

    public static String mapOFXBankAccountType(BankAccountType pBankAccountType) {
        if (pBankAccountType == null) {
            return NULL;
        }

        switch (pBankAccountType) {
            case Checking:
                return OFXBankAccountType.CHECKING.toString();
            case Savings:
                return OFXBankAccountType.SAVINGS.toString();
            default:
                return OFXBankAccountType.UNKNOWN.toString();
        }
    }

    public static String nullStringCheck(String pValue) {
        if (pValue == null || pValue.trim().equals("")|| NULL.equals(pValue)) {
            pValue=null;
        }
        return pValue;
    }

    public static String truncateOFXString(String ofxValue, int maxLength) {
        if (nullStringCheck(ofxValue) == null) {
            return null;
        }

        // PSRV004181 - Leading spaces in QB are showing up as "[~ " in the OFX.
        if (ofxValue.startsWith(LEADING_SPACE)) {
            ofxValue = ofxValue.substring(LEADING_SPACE.length());
        }

        if(ofxValue.trim().length() > maxLength) {
            return ofxValue.trim().substring(0, maxLength);
        }

        return ofxValue;
    }

    /**
     * If the string object is null, an empty string is returned.
     * This is used to coinvert null OFX values to an empty string
     *    so that an empty OFX entry appears in JAXB and the field
     *    is not omitted.
     * @param str - String to convert to "" if NULL.
     * @return - Original string or "" if NULL.
     */
    public static String convertNULLToEmptyString(String str) {
        if(str == null) {
            return "";
        }
        return str;
    }

    public static String convertNullToOFXString(Object o) {
        if(o == null) {
            return NULL;
        }

        return o.toString();
    }

    public static String Y_N(boolean pFlag) {
        return pFlag ? QBOFX.OFX_YN.Y : QBOFX.OFX_YN.N;
    }
    public static boolean hasInvalidBankAccount(String requestStr) {
        Matcher m = BANKACCTTO_PATTERN.matcher(requestStr);
        if(m.find()) {
            m = BANKACCTFROM_PATTERN.matcher(requestStr);
            requestStr = m.replaceAll("");
            return hasUnknownBankAccountType(requestStr) ||
                    hasEmptyRoutingNumber(requestStr) ||
                    hasEmptyAccountNumber(requestStr);
        }

        return false;
    }

    public static boolean hasUnknownBankAccountType(String requestStr) {
        Matcher m = ACCTTYPE_PATTERN.matcher(requestStr);
        return m.find();
    }

    public static boolean hasEmptyRoutingNumber(String requestStr) {
        Matcher m = BANKID_PATTERN.matcher(requestStr);
        return m.find();
    }

    public static boolean hasEmptyAccountNumber(String requestStr) {        
        Matcher m = ACCTID_PATTERN.matcher(requestStr);
        return m.find();
    }

    public enum OFXPayrollItemType {
        Addition,
        Bonus,
        Commission,
        Deduction,
        DirectDeposit,
        EmployerContribution,
        Hourly,
        Salary,
        Tax
    }

    public enum OFXPayrollFrequency {
        SEMIMONTHLY,
        BIWEEKLY,
        WEEKLY,
        MONTHLY,
        DAILY,
        YEARLY,
        QUARTERLY
    }

    public enum OFXGender {
        M,
        F
    }

    public enum OFXEmployeeType {
        REG,
        OFFICER,
        OWNER,
        STATUTORY,
        REP,
        UNKNOWN;

        public static OFXEmployeeType getValue(String pValue) {
            try {
                return valueOf(pValue);
            } catch (Exception e) {
                return UNKNOWN;
            }
        }
    }

    public enum OFXBankAccountType {
        CHECKING,
        SAVINGS,
        UNKNOWN
    }

    public enum OFXPayrollTransactionTransactionType {
        DDRETURN,
        FUNDSTRANSFER,
        LIABADJ,
        LIABCHK,
        PRIORPMT,
        REFUND
    }

    public static String truncZeros(String pValue) {
        char[] characters = pValue.toCharArray();
        int index = characters.length;
        while(index > 0 && characters[index-1] == '0') {
            index--;
        }

        if(characters[index-1] == '.') {
            index--;
        }

        return pValue.substring(0, index);
    }

    public static String getQBFileId(String pQBFileId) {
        String fileId = nullStringCheck(pQBFileId);
        if(fileId == null) {
            return MISSING_FILE_ID;
        } else {
            return fileId;
        }
    }
}
