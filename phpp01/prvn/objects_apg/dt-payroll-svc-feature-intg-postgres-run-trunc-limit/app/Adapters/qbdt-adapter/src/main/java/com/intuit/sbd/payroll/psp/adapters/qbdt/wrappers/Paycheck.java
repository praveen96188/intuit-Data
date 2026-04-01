package com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers;

import com.intuit.sbd.payroll.psp.common.ofx.request.BANKACCT;
import com.intuit.sbd.payroll.psp.common.ofx.request.IADJLINE;
import com.intuit.sbd.payroll.psp.common.ofx.request.IDDLINE;
import com.intuit.sbd.payroll.psp.common.ofx.request.IHRLYWAGELINE;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYCHK;
import com.intuit.sbd.payroll.psp.common.ofx.request.ISALARYLINE;
import com.intuit.sbd.payroll.psp.common.ofx.request.ITAXLINE;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.BankAccountType;
import com.intuit.sbd.payroll.psp.domain.QbdtNumericType;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 15, 2010
 * Time: 12:56:20 PM
 */
public class Paycheck {
    public static Pattern NOT_ALL_DIGITS_PATTERN = Pattern.compile(".*\\D.*");

    private IPAYCHK mPaycheck;
    private boolean mIsModification;
    private String mEmployeeId;

    public Paycheck(IPAYCHK pPaycheck) {
        this(pPaycheck, false);
    }

    public Paycheck(IPAYCHK pPaycheck, boolean pIsModification) {
        mPaycheck = pPaycheck;
        mIsModification = pIsModification;
    }

    public boolean isModification() {
        return mIsModification;
    }

    public IPAYCHK getIPAYCHK() {
        return mPaycheck;
    }

    public String getSourceId() {
        return mPaycheck.getIPAYCHKID();
    }

    public String getListId() {
        return mPaycheck.getIQBUNIQUEID();
    }

    public String getSourceEmployeeId() {
        if (mEmployeeId == null) {
            String employeeId = mPaycheck.getIEMPID();
            if (employeeId == null || Employee.DEFAULT_QB_EMPLOYEE_ID.equals(employeeId)) {
                employeeId = mPaycheck.getIEMPNAME().trim();
                Matcher matcher = NOT_ALL_DIGITS_PATTERN.matcher(employeeId);
                //If the name is all digits, it could possibly match a cloud/Assisted source employee id, so add an underscore to make sure this never happens
                if (!matcher.matches()) {
                    employeeId += "_";
                }
            }

            mEmployeeId = employeeId;
        }

        return mEmployeeId;
    }

    public String getEmployeeName() {
        return mPaycheck.getIEMPNAME();
    }

    public boolean isYTDAdjustment() {
        String paycheckType = mPaycheck.getIPAYCHKTYPE();
        return paycheckType != null && paycheckType.equals("YTDADJ");
    }

    public boolean hasNoDetails() {
        Boolean zeroAdj = getAdjustments() == null || getAdjustments().size() == 0;
        Boolean zeroHourlyWages = getHourlyWages() == null || getHourlyWages().size() == 0;
        Boolean zeroSalaryWages = getSalaryWages() == null || getSalaryWages().size() == 0;
        Boolean zeroTaxes = getTaxes() == null || getTaxes().size() == 0;
        Boolean zeroDD = getDirectDeposits() == null || getDirectDeposits().size() == 0;
        return (zeroAdj && zeroHourlyWages && zeroSalaryWages && zeroTaxes && zeroDD);
    }

    public String getTrackingClass() {
        return QBOFX.truncateOFXString(mPaycheck.getICLASS(), 128);
    }

    public String getAccountName() {
        return QBOFX.truncateOFXString(mPaycheck.getIACCTNAME(), 128);
    }

    public SpcfMoney getNetAmount() {
        SpcfMoney netAmount = QBOFX.mapOFXStringToMoney(mPaycheck.getIAMT());
        if (netAmount != null) {
            return QBOFX.mapOFXStringToMoney(mPaycheck.getIAMT());
        } else {
            return SpcfMoney.ZERO;
        }
    }

    public boolean isVoid() {
        return QBOFX.mapOFXStringToBoolean(mPaycheck.getIVOID());
    }

    public Boolean isOnService() {
        return mPaycheck.getIONSERVICE() != null ? QBOFX.mapOFXStringToBoolean(mPaycheck.getIONSERVICE()) : null;
    }

    public Date getPeriodBeginDate() {
        return QBOFX.mapOFXStringToDate(mPaycheck.getIDTPAYPDBEGIN());
    }

    public Date getPeriodEndDate() {
        return QBOFX.mapOFXStringToDate(mPaycheck.getIDTPAYPDEND());
    }

    public String getMemo() {
        return QBOFX.truncateOFXString(mPaycheck.getIMEMO(), 4000);
    }

    public String getCleared() {
        return QBOFX.truncateOFXString(mPaycheck.getICLEARED(), 1);
    }

    public boolean getProrate() {
        return mPaycheck.getIPAYCHKINFO() != null && QBOFX.mapOFXStringToBoolean(mPaycheck.getIPAYCHKINFO().getIPRORATE());
    }

    public String getCheckNumber() {
        if (mPaycheck.getIPAYCHKINFO() != null) {
            return QBOFX.truncateOFXString(mPaycheck.getIPAYCHKINFO().getICHKNUM(), 11);
        }
        return null;
    }

    public double getSickHoursAccrued() {
        if (mPaycheck.getIPAYCHKINFO() != null) {
            return QBOFX.mapOFXStringToDouble(mPaycheck.getIPAYCHKINFO().getISICKACCRUED());
        }
        return 0;
    }

    public double getVacationHoursAccrued() {
        if (mPaycheck.getIPAYCHKINFO() != null) {
            return QBOFX.mapOFXStringToDouble(mPaycheck.getIPAYCHKINFO().getIVACACCRUED());
        }
        return 0;
    }

    public boolean equals(com.intuit.sbd.payroll.psp.domain.Paycheck pPaycheck, Date pPaycheckDate, boolean pIsAssisted) {
        if (pPaycheck.getSourceEmployee() == null || !getSourceEmployeeId().equals(pPaycheck.getSourceEmployee().getSourceEmployeeId())) {
            return false;
        }

        // additional check for assisted or DD
        if (pIsAssisted || pPaycheck.isDDPaycheck()) {
            if (pPaycheck.getPayrollRun() != null && pPaycheckDate.getTime() != pPaycheck.getPayrollRun().getPaycheckDate().getTimeInMilliseconds()) {
                return false;
            } else if (getNetAmount().negate().compareTo(pPaycheck.getNetAmount()) != 0) {
                return false;
            }
        }

        return true;
    }

    public List<Compensation> getHourlyWages() {
        List<Compensation> compensations = new ArrayList<Compensation>();

        long payStubCounter = 0;
        for (IHRLYWAGELINE ihrlywageline : mPaycheck.getIHRLYWAGELINE()) {
            compensations.add(new Compensation(ihrlywageline.getIPITEMID(),
                                               QBOFX.mapOFXStringToMoney(ihrlywageline.getIAMT()),
                                               QBOFX.mapOFXStringToMoney(ihrlywageline.getIYTDAMT()),
                                               QBOFX.mapOFXStringToDouble(ihrlywageline.getIRATE()),
                                               QBOFX.mapOFXStringNumericType(ihrlywageline.getIRATE()),
                                               QBOFX.mapOFXStringToDouble(ihrlywageline.getIHRS()),
                                               QBOFX.truncateOFXString(ihrlywageline.getICLASS(), 128),
                                               QBOFX.truncateOFXString(ihrlywageline.getIJOB(), 128),
                                               QBOFX.truncateOFXString(ihrlywageline.getIITEM(), 128),
                                               QBOFX.truncateOFXString(ihrlywageline.getIWCCODE(), 20),
                                               payStubCounter++));
        }

        return compensations;
    }

    public List<Compensation> getSalaryWages() {
        List<Compensation> compensations = new ArrayList<Compensation>();

        long payStubCounter = 0;
        for (ISALARYLINE isalaryline : mPaycheck.getISALARYLINE()) {
            compensations.add(new Compensation(isalaryline.getIPITEMID(),
                                               QBOFX.mapOFXStringToMoney(isalaryline.getIAMT()),
                                               QBOFX.mapOFXStringToMoney(isalaryline.getIYTDAMT()),
                                               QBOFX.mapOFXStringToDouble(isalaryline.getIRATE()),
                                               QBOFX.mapOFXStringNumericType(isalaryline.getIRATE()),
                                               QBOFX.mapOFXStringToDouble(isalaryline.getIHRS()),
                                               QBOFX.truncateOFXString(isalaryline.getICLASS(), 128),
                                               QBOFX.truncateOFXString(isalaryline.getIJOB(), 128),
                                               QBOFX.truncateOFXString(isalaryline.getIITEM(), 128),
                                               QBOFX.truncateOFXString(isalaryline.getIWCCODE(), 20),
                                               payStubCounter++));
        }

        return compensations;
    }

    public List<Adjustment> getAdjustments() {
        List<Adjustment> adjustments = new ArrayList<Adjustment>();

        long payStubCounter = 0;
        for (IADJLINE iadjline : mPaycheck.getIADJLINE()) {
            adjustments.add(new Adjustment(iadjline.getIPITEMID(),
                                           QBOFX.mapOFXStringToMoney(iadjline.getIAMT()),
                                           QBOFX.mapOFXStringToMoney(iadjline.getIYTDAMT()),
                                           QBOFX.mapOFXStringToDouble(iadjline.getIRATE()),
                                           QBOFX.mapOFXStringNumericType(iadjline.getIRATE()),
                                           QBOFX.mapOFXStringToDouble(iadjline.getIQTY()),
                                           QBOFX.mapOFXStringNumericType(iadjline.getIQTY()),
                                           QBOFX.mapOFXStringToBoolean(iadjline.getIEXPBYJOB()),
                                           payStubCounter++));
        }

        return adjustments;
    }

    public List<DirectDeposit> getDirectDeposits() {
        List<DirectDeposit> directDeposits = new ArrayList<DirectDeposit>();

        long payStubCounter = 0;
        for (IDDLINE iddline : mPaycheck.getIDDLINE()) {
            BANKACCT bankacctto = iddline.getIDDACCT().getBANKACCTTO();
            if (bankacctto == null) {
                bankacctto = new BANKACCT();
            }
            directDeposits.add(new DirectDeposit(iddline.getIPITEMID(),
                                                 QBOFX.mapOFXStringToMoney(iddline.getIAMT()),
                                                 bankacctto.getBANKID(),
                                                 bankacctto.getACCTID(),
                                                 QBOFX.mapOFXStringToBankAccountType(bankacctto.getACCTTYPE()),
                                                 iddline.getIDDACCT().getIACCTNAME(),
                                                 payStubCounter++));
        }

        return directDeposits;
    }

    public List<Tax> getTaxes() {
        List<Tax> taxes = new ArrayList<Tax>();

        long payStubCounter = 0;
        for (ITAXLINE itaxline : mPaycheck.getITAXLINE()) {
            taxes.add(new Tax(itaxline.getIPITEMID(),
                              QBOFX.mapOFXStringToMoney(itaxline.getIAMT()),
                              QBOFX.mapOFXStringToMoney(itaxline.getIYTDAMT()),
                              QBOFX.mapOFXStringToMoney(itaxline.getIWB()),
                              QBOFX.mapOFXStringToMoney(itaxline.getITAXABLEWAGE()),
                              QBOFX.mapOFXStringToMoney(itaxline.getITIPSWB()),
                              payStubCounter++));
        }

        return taxes;
    }

    public class Compensation {
        private String mPayrollItemId;
        private SpcfMoney mAmount;
        private SpcfMoney mYTDAmount;
        private double mRate;
        private QbdtNumericType mRateType;
        private double mHours;
        private String mTrackingClass;
        private String mJob;
        private String mItem;
        private String mWCCode;
        private long mPayStubOrder;

        public Compensation(String pPayrollItemId, SpcfMoney pAmount, SpcfMoney pYTDAmount, double pRate, QbdtNumericType pRateType, double pHours, String pTrackingClass, String pJob, String pItem, String pWCCode, long pPayStubOrder) {
            mPayrollItemId = pPayrollItemId;
            mAmount = pAmount;
            mYTDAmount = pYTDAmount;
            mRate = pRate;
            mRateType = pRateType;
            mHours = pHours;
            mTrackingClass = pTrackingClass;
            mJob = pJob;
            mItem = pItem;
            mWCCode = pWCCode;
            mPayStubOrder = pPayStubOrder;
        }

        public String getPayrollItemId() {
            return mPayrollItemId;
        }

        public SpcfMoney getAmount() {
            return mAmount;
        }

        public SpcfMoney getYTDAmount() {
            return mYTDAmount;
        }

        public double getRate() {
            return mRate;
        }

        public QbdtNumericType getRateType() {
            return mRateType;
        }

        public double getHours() {
            return mHours;
        }

        public String getTrackingClass() {
            return mTrackingClass;
        }

        public String getJob() {
            return mJob;
        }

        public String getItem() {
            return mItem;
        }

        public String getWCCode() {
            return mWCCode;
        }

        public long getPayStubOrder() {
            return mPayStubOrder;
        }
    }

    public class Adjustment {
        private String mPayrollItemId;
        private SpcfMoney mAmount;
        private SpcfMoney mYTDAmount;
        private double mRate;
        private QbdtNumericType mRateType;
        private double mQuantity;
        private QbdtNumericType mQuantityType;
        private boolean mExpenseByJob;
        private long mPayStubOrder;

        public Adjustment(String pPayrollItemId, SpcfMoney pAmount, SpcfMoney pYTDAmount, double pRate, QbdtNumericType pRateType, double pQuantity, QbdtNumericType pQuantityType, boolean pExpenseByJob, long pPayStubOrder) {
            mPayrollItemId = pPayrollItemId;
            mAmount = pAmount;
            mYTDAmount = pYTDAmount;
            mRate = pRate;
            mRateType = pRateType;
            mQuantity = pQuantity;
            mQuantityType = pQuantityType;
            mExpenseByJob = pExpenseByJob;
            mPayStubOrder = pPayStubOrder;
        }

        public String getPayrollItemId() {
            return mPayrollItemId;
        }

        public SpcfMoney getAmount() {
            return mAmount;
        }

        public SpcfMoney getYTDAmount() {
            return mYTDAmount;
        }

        public double getRate() {
            return mRate;
        }

        public QbdtNumericType getRateType() {
            return mRateType;
        }

        public double getQuantity() {
            return mQuantity;
        }

        public QbdtNumericType getQuantityType() {
            return mQuantityType;
        }

        public boolean isExpenseByJob() {
            return mExpenseByJob;
        }

        public long getPayStubOrder() {
            return mPayStubOrder;
        }
    }

    public class DirectDeposit {
        private String mPayrollItemId;
        private SpcfMoney mAmount;
        private String mRoutingNumber;
        private String mAccountNumber;
        private BankAccountType mAccountType;
        private String mBankName;
        private long mPayStubOrder;

        public DirectDeposit(String pPayrollItemId, SpcfMoney pAmount, String pRoutingNumber, String pAccountNumber, BankAccountType pAccountType, String pBankName, long pPayStubOrder) {
            mPayrollItemId = pPayrollItemId;
            mAmount = pAmount;
            mRoutingNumber = pRoutingNumber;
            mAccountNumber = pAccountNumber;
            mAccountType = pAccountType;
            mBankName = pBankName;
            mPayStubOrder = pPayStubOrder;
        }

        public String getPayrollItemId() {
            return mPayrollItemId;
        }

        public SpcfMoney getAmount() {
            return mAmount;
        }

        public String getRoutingNumber() {
            return mRoutingNumber;
        }

        public String getAccountNumber() {
            return mAccountNumber;
        }

        public BankAccountType getAccountType() {
            return mAccountType;
        }

        public String getBankName() {
            return mBankName;
        }

        public long getPayStubOrder() {
            return mPayStubOrder;
        }
    }

    public class Tax {
        private String mPayrollItemId;
        private SpcfMoney mAmount;
        private SpcfMoney mYTDAmount;
        private SpcfMoney mTaxableWageAmount;
        private SpcfMoney mTotalWageAmount;
        private SpcfMoney mTipTaxableWageAmount;
        private long mPayStubOrder;

        public Tax(String pPayrollItemId, SpcfMoney pAmount, SpcfMoney pYTDAmount, SpcfMoney pTaxableWageAmount, SpcfMoney pTotalWageAmount, SpcfMoney pTipTaxableWageAmount, long pPayStubOrder) {
            mPayrollItemId = pPayrollItemId;
            mAmount = pAmount;
            mYTDAmount = pYTDAmount;
            mTaxableWageAmount = pTaxableWageAmount;
            mTotalWageAmount = pTotalWageAmount;
            mTipTaxableWageAmount = pTipTaxableWageAmount;
            mPayStubOrder = pPayStubOrder;
        }

        public String getPayrollItemId() {
            return mPayrollItemId;
        }

        public SpcfMoney getAmount() {
            return mAmount;
        }

        public SpcfMoney getYTDAmount() {
            return mYTDAmount;
        }

        public SpcfMoney getTaxableWageAmount() {
            return mTaxableWageAmount;
        }

        public SpcfMoney getTotalWageAmount() {
            return mTotalWageAmount;
        }

        public SpcfMoney getTipTaxableWageAmount() {
            return mTipTaxableWageAmount;
        }

        public long getPayStubOrder() {
            return mPayStubOrder;
        }
    }
}
