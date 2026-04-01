package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hand-written business logic
 */
public class ThirdParty401kPaycheck extends BaseThirdParty401kPaycheck {

	/**
	 * Default constructor.
	 */
	public ThirdParty401kPaycheck()
	{
		super();
	}

static final String DATE_FORMAT = "yyyyMMdd";;
    static final String TIME_FORMAT = "HH:mm";
    static final Pattern cutOffTimePattern = Pattern.compile("([0-2]?[0-9]:[0-5][0-9]).*");

    /**
     * Adds the TP401 paycheck information
     * @param paycheck The newly added paycheck
     * @return The newly created objects in a ThirdPartyPaycheckObjects
     */
    public static ThirdParty401kPaycheck addTP401K(Paycheck paycheck) {
        SpcfCalendar serviceStartDate = paycheck.getPayrollRun().getCompany().getCompanyService(ServiceCode.ThirdParty401k).getServiceStartDate();
        if (paycheck.getPayrollRun().getPaycheckDate().before(serviceStartDate)) {
            return null;
        }

        ThirdParty401kPaycheck tp401Paycheck = paycheck.getThirdParty401kPaycheck();
        if (tp401Paycheck == null) {
            tp401Paycheck = new ThirdParty401kPaycheck();
            tp401Paycheck.setCompany(paycheck.getCompany());
            paycheck.setThirdParty401kPaycheck(tp401Paycheck);
            tp401Paycheck.setPaycheck(paycheck);
        }

        tp401Paycheck.updatePaycheckStateCode();
        Application.save(tp401Paycheck);

        return tp401Paycheck;
    }

    /**
     * Updates the TP401 paycheck information
     * @param paycheck The newly updated paycheck
     */
    public static void update401K(Paycheck paycheck) {
        Company company = paycheck.getPayrollRun().getCompany();
        CompanyService k401Service = company.getCompanyService(ServiceCode.ThirdParty401k);
        if (k401Service == null) {
            return;
        }

        SpcfCalendar serviceStartDate = k401Service.getServiceStartDate();
        if (paycheck.getPayrollRun().getPaycheckDate().before(serviceStartDate)) {
            //todo_rhn: add a status code of PriorToServiceStart to track case of paycheck received for prior to service start
            return;
        }

        ThirdParty401kPaycheck k401Paycheck = paycheck.getThirdParty401kPaycheck();
        if (k401Paycheck == null) {
            k401Paycheck = addTP401K(paycheck);
            if (k401Paycheck == null) {
                return;
            }
        }

        k401Paycheck.updatePaycheckStateCode();
    }

    /**
    * Voids the TP401 paycheck information
    * @param voids The voided paychecks
    */
    public static void void401K(List<Paycheck> voids) {
        for (Paycheck paycheck : voids) {
            ThirdParty401kPaycheck k401Paycheck = paycheck.getThirdParty401kPaycheck();
            if (k401Paycheck != null) {
                k401Paycheck.voidPaycheck();
            }
        }
    }

    /**
    * Deletes the TP401 paycheck information
    * @param paycheck The deleted paycheck
    */
    public static void delete401K(Paycheck paycheck) {
        ThirdParty401kPaycheck k401Paycheck = paycheck.getThirdParty401kPaycheck();
        if (k401Paycheck != null) {
            k401Paycheck.voidPaycheck();
        }
    }

    /**
     * the newStatue == null ordering enforces requirements defined hierarchy of errors
     */
    public ThirdParty401kPaycheckStateCode updatePaycheckStateCode() {
        ThirdParty401kPaycheckStateCode newState = null;

        // TODO: Modify this Extend cut-off date to send back-dated payroll from 2 days to 10 days as a parameter.
        if (getCurrentStateCd() == ThirdParty401kPaycheckStateCode.None) {
            SpcfCalendar initiationDate = calculate401kBaseOffloadDate(getPaycheck().getPayrollRun().getPaycheckDate());
            if (PSPDate.getPSPTime().after(initiationDate)) {
                newState = ThirdParty401kPaycheckStateCode.Ineligible;
            }
        }

        // once a paycheck is ineligible (received after offload date or before service start date), always ineligible
        if (getCurrentStateCd().equals(ThirdParty401kPaycheckStateCode.Ineligible)) {
            newState = ThirdParty401kPaycheckStateCode.Ineligible;
        }

        // Is the employee's data invalid?
        if (newState == null && (getPaycheck().getSourceEmployee() == null || getPaycheck().getSourceEmployee().isValidForCensusFile().size() > 0)) {
            newState = ThirdParty401kPaycheckStateCode.InvalidEmployeeData;
        }

        // Check for negative values
        if (newState == null && getPayrollFilePaycheck().isValidForPayrollFile().size() > 0) {
            newState = ThirdParty401kPaycheckStateCode.InvalidPaycheckData;
        }

        if (newState == null) {
            newState = ThirdParty401kPaycheckStateCode.Pending;
        }

        updateCurrentStateCd(newState);
        return newState;
    }

    private void updateCurrentStateCd(ThirdParty401kPaycheckStateCode pNewState) {
        if (pNewState.equals(getCurrentStateCd()) || isInFinalState()) {
            return;
        }

        setCurrentStateCd(pNewState);
        Application.save(this);

        addStateCode(pNewState);

        if (pNewState.equals(ThirdParty401kPaycheckStateCode.Pending) || pNewState.equals(ThirdParty401kPaycheckStateCode.InvalidEmployeeData)) {
            if (getPaycheck() == null) {
                throw new RuntimeException("Cannot set ThirdParty401kPaycheck to Pending -- no Paycheck association has been set");
            }

            ThirdParty401kPaycheckPendingState tpPaycheckPendingState;
            if (getPaycheck().getThirdParty401kPaycheck().getThirdParty401kPaycheckPendingState() != null) {
                tpPaycheckPendingState = getPaycheck().getThirdParty401kPaycheck().getThirdParty401kPaycheckPendingState();
            } else {
                tpPaycheckPendingState = new ThirdParty401kPaycheckPendingState();
            }

            tpPaycheckPendingState.setStateCd(pNewState);
            tpPaycheckPendingState.setThirdParty401kPaycheck(this);
            Application.save(tpPaycheckPendingState);
            setThirdParty401kPaycheckPendingState(tpPaycheckPendingState);
        } else if (getThirdParty401kPaycheckPendingState() != null) {
            Application.delete(getThirdParty401kPaycheckPendingState());
            setThirdParty401kPaycheckPendingState(null);
        }

        // will update ThirdParty401kPaycheck and ThirdParty401kPaycheckPendingState
        if (!isInFinalState() && pNewState != ThirdParty401kPaycheckStateCode.Ineligible) {
            updateInitiationDate();
        }
    }

    /**
     * Only can be called from updatePaycheckStateCode() - addStateCode() is responsible for copying this date
     * over to the pending paycheck if it exists
     */
    private SpcfCalendar updateInitiationDate() {
        setInitiationDate(calculate401kAdjustedOffloadDate(getPaycheck().getPayrollRun().getPaycheckDate()));

        if (getThirdParty401kPaycheckPendingState() != null) {
            getThirdParty401kPaycheckPendingState().setInitiationDate(getInitiationDate());
        }

        return getInitiationDate();
    }

    public static SpcfCalendar calculate401kBaseOffloadDate(SpcfCalendar pCheckDate) {
        // assumption: payroll runs contain multiple paychecks w/the same check date (optimize initiation date calculation)
        HashMap<SpcfCalendar,SpcfCalendar> paycheckDateBaseInitiationDateMap = Application.getSessionCache().getNonHibernateObject("PaycheckDateToBase401kInitiationDate");
        if (paycheckDateBaseInitiationDateMap == null) {
            paycheckDateBaseInitiationDateMap = new HashMap<SpcfCalendar, SpcfCalendar>(11);
            Application.getSessionCache().addNonHibernateObject("PaycheckDateToBase401kInitiationDate", paycheckDateBaseInitiationDateMap);
        }

        if (paycheckDateBaseInitiationDateMap.containsKey(pCheckDate)) {
            return paycheckDateBaseInitiationDateMap.get(pCheckDate);
        }


        SpcfCalendar checkDateCopy = pCheckDate.copy();

        String cutoffTimeStr = SourcePayrollParameter.findStringValue(SourceSystemCode.QBDT, SourcePayrollParameterCode.ThirdParty401kCutoffTime);
        Integer waitPeriod = SourcePayrollParameter.findIntValue(SourceSystemCode.QBDT, SourcePayrollParameterCode.ThirdParty401kOffloadWaitPeriod);

        Matcher matcher = cutOffTimePattern.matcher(cutoffTimeStr);
        if (matcher.matches()) {
            cutoffTimeStr = matcher.group(1);
            if (cutoffTimeStr.matches("[0-9]:[0-5][0-9]")) {
                cutoffTimeStr = "0" + cutoffTimeStr;
            }
        }

        CalendarUtils.addBusinessDays(checkDateCopy, waitPeriod);
        String currentDateStr = checkDateCopy.format(DATE_FORMAT);
        SpcfCalendar cal = SpcfCalendar.parse(DATE_FORMAT + TIME_FORMAT, currentDateStr + cutoffTimeStr);
        SpcfCalendar offloadDate = SpcfCalendar.createInstance(cal.getYear(),
                                           cal.getMonth(),
                                           cal.getDay(),
                                           cal.getHour(),
                                           cal.getMinute(),
                                           cal.getSecond(),
                                           cal.getMillisecond(),
                                           SpcfTimeZone.getLocalTimeZone());

        // cache the paycheckDate->baseInitiationDate calculation
        paycheckDateBaseInitiationDateMap.put(pCheckDate.copy(), offloadDate.copy());

        return offloadDate;
    }

    public static SpcfCalendar calculate401kAdjustedOffloadDate(SpcfCalendar pCheckDate) {
        // assumption: payroll runs contain multiple paychecks w/the same check date (optimize initiation date calculation)
        HashMap<SpcfCalendar,SpcfCalendar> paycheckDateInitiationDateMap = Application.getSessionCache().getNonHibernateObject("PaycheckDateTo401kInitiationDate");
        if (paycheckDateInitiationDateMap == null) {
            paycheckDateInitiationDateMap = new HashMap<SpcfCalendar, SpcfCalendar>(11);
            Application.getSessionCache().addNonHibernateObject("PaycheckDateToAdjusted401kInitiationDate", paycheckDateInitiationDateMap);
        }

        if (paycheckDateInitiationDateMap.containsKey(pCheckDate)) {
            return paycheckDateInitiationDateMap.get(pCheckDate);
        }

        // calculate expected offload date from paycheck date (paycheckDate + wait period sys parameter)
        SpcfCalendar offloadDate = calculate401kBaseOffloadDate(pCheckDate);

        // calculate next actual offload date
        String cutoffTimeStr = SourcePayrollParameter.findStringValue(SourceSystemCode.QBDT, SourcePayrollParameterCode.ThirdParty401kCutoffTime);
        SpcfCalendar cal = SpcfCalendar.parse(DATE_FORMAT + TIME_FORMAT, PSPDate.getPSPTime().toUtc().format(DATE_FORMAT) + cutoffTimeStr);
        SpcfCalendar nextAvailableOffload = SpcfCalendar.createInstance(cal.getYear(),
                                           cal.getMonth(),
                                           cal.getDay(),
                                           cal.getHour(),
                                           cal.getMinute(),
                                           cal.getSecond(),
                                           cal.getMillisecond(),
                                           SpcfTimeZone.getLocalTimeZone());

        if (PSPDate.getPSPTime().after(nextAvailableOffload)) {
            CalendarUtils.addBusinessDays(nextAvailableOffload, 1);
        }

        if (offloadDate.before(nextAvailableOffload)) {
            offloadDate = nextAvailableOffload;
        }

        // cache the paycheckDate->initiationDate calculation
        paycheckDateInitiationDateMap.put(pCheckDate.copy(), offloadDate.copy());

        return offloadDate;
    }

    /**
     * Checks if a ThirdParty401kPaycheckStateCode is a final or non-recoverable state
     * @param stateCode The current state code
     * @return True if a final or non-recoverable state, false otherwise
     */
    public static boolean isFinalStateCode(ThirdParty401kPaycheckStateCode stateCode) {
        return stateCode != null && stateCode == ThirdParty401kPaycheckStateCode.Cancelled || stateCode == ThirdParty401kPaycheckStateCode.Sent;
    }

    public void voidPaycheck() {
        updateCurrentStateCd(ThirdParty401kPaycheckStateCode.Cancelled);
    }

    /**
     * Marks a TP401Paycheck as having been offloaded
     */
    public void markAsSent() {
        updateCurrentStateCd(ThirdParty401kPaycheckStateCode.Sent);
    }

    private void addStateCode(ThirdParty401kPaycheckStateCode p401kPaycheckStateCode) {
        ThirdParty401kPaycheckState newPaycheckState = new ThirdParty401kPaycheckState();
        newPaycheckState.setStateEffectiveDate(PSPDate.getPSPTime());
        newPaycheckState.setStateCd(p401kPaycheckStateCode);
        newPaycheckState.setThirdParty401kPaycheck(this);
        Application.save(newPaycheckState);
    }

    public boolean isInFinalState() {
        return isFinalStateCode(getCurrentStateCd());
    }

    public BigDecimal getSalary() {
        return getTotalCompensation(PayrollItemType.Compensation);
    }

    public Double getTotalHours() {
        return getTotalHours(PayrollItemType.Compensation);
    }

    public BigDecimal getEmployeeDeferral() {
        return getTotalDeduction(PayrollItemCode.Tp401kEmployeeDeferral);
    }

    public BigDecimal getProfitSharing() {
        return getTotalEmployerContribution(PayrollItemCode.Tp401kProfitSharing);
    }

    public BigDecimal getRoth() {
        return getTotalDeduction(PayrollItemCode.Tp401kRoth);
    }

    public BigDecimal getLoan() {
        return getTotalDeduction(PayrollItemCode.Tp401kLoanPayment);
    }

    public BigDecimal getEmployerMatch() {
        return getTotalDeduction(PayrollItemCode.Tp401kEmployerMatch);
    }

    public BigDecimal getSafeHarbor() {
        return getTotalEmployerContribution(PayrollItemCode.Tp401kSafeHarbor);
    }

    public BigDecimal getTotalCompensation(PayrollItemType pPayrollItemType) {
        BigDecimal totalCompensation = BigDecimal.ZERO;

        Company company = getPaycheck().getPayrollRun().getCompany();
        DomainEntitySet<CompanyPayrollItem> companyPayrollItems = company.getCompanyPayrollItemCollection().find(CompanyPayrollItem.PayrollItem().PayrollItemType().equalTo(pPayrollItemType));
        for (CompanyPayrollItem companyPayrollItem : companyPayrollItems) {
            DomainEntitySet<Compensation> compensations = getPaycheck().getCompensationCollection().find(Compensation.CompanyPayrollItem().equalTo(companyPayrollItem));
            for (Compensation compensation : compensations) {
                totalCompensation = totalCompensation.add(SpcfUtils.convertToBigDecimal(compensation.getCompensationAmount()));
            }
        }
        return totalCompensation;
    }

    public BigDecimal getTotalDeduction(PayrollItemCode pPayrollItemCode) {
        BigDecimal totalDeduction = BigDecimal.ZERO;
        PayrollItem payrollItem = PayrollItem.findItemByPayrollItemCode(pPayrollItemCode);

        Company company = getPaycheck().getPayrollRun().getCompany();
        DomainEntitySet<CompanyPayrollItem> companyPayrollItems = company.getCompanyPayrollItemCollection().find(CompanyPayrollItem.PayrollItem().equalTo(payrollItem));
        for (CompanyPayrollItem companyPayrollItem : companyPayrollItems) {
            DomainEntitySet<Deduction> deductions = getPaycheck().getDeductionCollection().find(Deduction.CompanyPayrollItem().equalTo(companyPayrollItem));
            for (Deduction deduction : deductions) {
                totalDeduction = totalDeduction.add(SpcfUtils.convertToBigDecimal(deduction.getDeductionAmount()));
            }
        }

        return totalDeduction;
    }

    public Double getTotalHours(PayrollItemType pPayrollItemType) {
        Double totalHours = 0.00;

        Company company = getPaycheck().getPayrollRun().getCompany();
        DomainEntitySet<CompanyPayrollItem> companyPayrollItems = company.getCompanyPayrollItemCollection().find(CompanyPayrollItem.PayrollItem().PayrollItemType().equalTo(pPayrollItemType));
        for (CompanyPayrollItem companyPayrollItem : companyPayrollItems) {
            DomainEntitySet<Compensation> compensations = getPaycheck().getCompensationCollection().find(Compensation.CompanyPayrollItem().equalTo(companyPayrollItem));
            for (Compensation compensation : compensations) {
                totalHours += compensation.getHoursWorked();
            }
        }

        return totalHours;
    }

    public BigDecimal getTotalEmployerContribution(PayrollItemCode pPayrollItemCode) {
        BigDecimal totalContribution = BigDecimal.ZERO;
        PayrollItem payrollItem = PayrollItem.findItemByPayrollItemCode(pPayrollItemCode);

        Company company = getPaycheck().getPayrollRun().getCompany();
        DomainEntitySet<CompanyPayrollItem> companyPayrollItems = company.getCompanyPayrollItemCollection().find(CompanyPayrollItem.PayrollItem().equalTo(payrollItem));
        for (CompanyPayrollItem companyPayrollItem : companyPayrollItems) {
            DomainEntitySet<EmployerContribution> contributions = getPaycheck().getEmployerContributionCollection().find(EmployerContribution.CompanyPayrollItem().equalTo(companyPayrollItem));
            for (EmployerContribution contribution : contributions) {
                totalContribution = totalContribution.add(SpcfUtils.convertToBigDecimal(contribution.getContributionAmount()));
            }
        }

        return totalContribution;
    }

    public PayrollFilePaycheck getPayrollFilePaycheck() {
        return new PayrollFilePaycheck(this);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        try {
            builder.append("Paycheck ")
                    .append("  CurrentStateCd:").append(getCurrentStateCd())
                    .append("  Initiation Date:").append(getInitiationDate());
        } catch (Throwable t) {}
        return builder.toString();
    }

    public class PayrollFilePaycheck {
        private ThirdParty401kPaycheck tp401kPaycheck;

        private String mSalary;
        private String mDeferral;
        private String mRoth;
        private String mLoan;
        private String mMatching;
        private String mProfitSharing;
        private String mSafeHarbor;
        private String mHours;
        private String mBeginPayPeriod;
        private String mEndPayPeriod;

        private boolean isSalaryValid = true;
        private boolean isDeferralValid = true;
        private boolean isRothValid = true;
        private boolean isLoanValid = true;
        private boolean isMatchingValid = true;
        private boolean isProfitSharingValid = true;
        private boolean isSafeHarborValid = true;

        private ArrayList<String> validationErrors = new ArrayList<String>();

        PayrollFilePaycheck(ThirdParty401kPaycheck pTp401kPaycheck) {
            tp401kPaycheck = pTp401kPaycheck;
             validate();
        }

        public void validate() {
            BigDecimal totalSalary =  tp401kPaycheck.getSalary();
            this.setSalary(totalSalary.doubleValue());

            Double totalHours =  tp401kPaycheck.getTotalHours();
            this.setHours(totalHours.intValue());

            BigDecimal totalDeferral =  tp401kPaycheck.getEmployeeDeferral();
            this.setDeferral(totalDeferral.doubleValue());

            BigDecimal totalRoth =  tp401kPaycheck.getRoth();
            this.setRoth(totalRoth.doubleValue());

            BigDecimal totalLoan =  tp401kPaycheck.getLoan();
            this.setLoan(totalLoan.doubleValue());

            BigDecimal totalMatching =  tp401kPaycheck.getEmployerMatch();
            this.setMatching(totalMatching.doubleValue());

            BigDecimal totalProfitSharing =  tp401kPaycheck.getProfitSharing();
            this.setProfitSharing(totalProfitSharing.doubleValue());

            BigDecimal totalSafeHarbor =  tp401kPaycheck.getSafeHarbor();
            this.setSafeHarbor(totalSafeHarbor.doubleValue());

            this.setBeginPayPeriod(tp401kPaycheck.getPaycheck().getPayPeriodBeginDate());
            this.setEndPayPeriod(tp401kPaycheck.getPaycheck().getPayPeriodEndDate());
        }

        public String getSalary() {
            return mSalary;
        }

        private void setSalary(Double pSalary) {
            ThirdParty401kPayrollItemInfo tp401kPayrollItemInfo =
                    PayrollItem.findItemByPayrollItemCode(PayrollItemCode.Compensation).getThirdParty401kPayrollItemInfo();
            if (tp401kPayrollItemInfo.getIsProviderAccepted()) {
                if (pSalary < 0) {
                    if (tp401kPayrollItemInfo.getAllowsNegativeAmounts()) {
                        this.mSalary = pSalary.toString();
                    } else {
                        this.mSalary = null;
                        isSalaryValid = false;
                        addNegativeValueError("Salary", pSalary);
                    }
                } else {
                    this.mSalary = pSalary.toString();
                    if (pSalary == 0) {
                        validationErrors.add("Field 'Salary' does not contain a positive amount");
                    }
                }
            } else {
                this.mSalary = null;
            }
        }

        public String getDeferral() {
            return mDeferral;
        }

        private void setDeferral(Double pDeferral) {
            ThirdParty401kPayrollItemInfo tp401kPayrollItemInfo =
                    PayrollItem.findItemByPayrollItemCode(PayrollItemCode.Tp401kEmployeeDeferral).getThirdParty401kPayrollItemInfo();
            if (tp401kPayrollItemInfo.getIsProviderAccepted()) {
                if (pDeferral < 0) {
                    if (tp401kPayrollItemInfo.getAllowsNegativeAmounts()) {
                        this.mDeferral = pDeferral.toString();
                    } else {
                        this.mDeferral = null;
                        isDeferralValid = false;
                        addNegativeValueError("Deferral", pDeferral);
                    }
                } else {
                    this.mDeferral = pDeferral.toString();
                }
            } else {
                this.mDeferral = null;
            }
        }

        public String getRoth() {
            return mRoth;
        }

        private void setRoth(Double pRoth) {
            ThirdParty401kPayrollItemInfo tp401kPayrollItemInfo =
                    PayrollItem.findItemByPayrollItemCode(PayrollItemCode.Tp401kRoth).getThirdParty401kPayrollItemInfo();
            if (tp401kPayrollItemInfo.getIsProviderAccepted()) {
                if (pRoth < 0) {
                    if (tp401kPayrollItemInfo.getAllowsNegativeAmounts()) {
                        this.mRoth = pRoth.toString();
                    } else {
                        isRothValid = false;
                        this.mRoth = null;
                        addNegativeValueError("Roth", pRoth);
                    }
                } else {
                    this.mRoth = pRoth.toString();
                }
            } else {
                this.mRoth = null;
            }
        }

        public String getLoan() {
            return mLoan;
        }

        private void setLoan(Double pLoan) {
            ThirdParty401kPayrollItemInfo tp401kPayrollItemInfo =
                    PayrollItem.findItemByPayrollItemCode(PayrollItemCode.Tp401kLoanPayment).getThirdParty401kPayrollItemInfo();
            if (tp401kPayrollItemInfo.getIsProviderAccepted()) {
                if (pLoan < 0) {
                    if (tp401kPayrollItemInfo.getAllowsNegativeAmounts()) {
                        this.mLoan = pLoan.toString();
                    } else {
                        this.mLoan = null;
                        isLoanValid = false;
                        addNegativeValueError("Loan", pLoan);
                    }
                } else {
                    this.mLoan = pLoan.toString();
                }
            } else {
                this.mLoan = null;
            }
        }

        public String getMatching() {
            return mMatching;
        }

        private void setMatching(Double pMatching) {
            ThirdParty401kPayrollItemInfo tp401kPayrollItemInfo =
                    PayrollItem.findItemByPayrollItemCode(PayrollItemCode.Tp401kEmployerMatch).getThirdParty401kPayrollItemInfo();
            if (tp401kPayrollItemInfo.getIsProviderAccepted()) {
                if (pMatching < 0) {
                    if (tp401kPayrollItemInfo.getAllowsNegativeAmounts()) {
                        this.mMatching = pMatching.toString();
                    } else {
                        this.mMatching = null;
                        isMatchingValid = false;
                        addNegativeValueError("Employer Matching", pMatching);
                    }
                } else {
                    this.mMatching = pMatching.toString();
                }
            } else {
                this.mMatching = null;
            }
        }

        public String getProfitSharing() {
            return mProfitSharing;
        }

        private void setProfitSharing(Double pProfitSharing) {
            ThirdParty401kPayrollItemInfo tp401kPayrollItemInfo =
                    PayrollItem.findItemByPayrollItemCode(PayrollItemCode.Tp401kProfitSharing).getThirdParty401kPayrollItemInfo();
            if (tp401kPayrollItemInfo.getIsProviderAccepted()) {
                if (pProfitSharing < 0) {
                    if (tp401kPayrollItemInfo.getAllowsNegativeAmounts()) {
                        this.mProfitSharing = pProfitSharing.toString();
                    } else {
                        this.mProfitSharing = null;
                        isProfitSharingValid = false;
                        addNegativeValueError("Profit Sharing", pProfitSharing);
                    }
                } else {
                    this.mProfitSharing = pProfitSharing.toString();
                }
            } else {
                this.mProfitSharing = null;
            }
        }

        public String getSafeHarbor() {
            return mSafeHarbor;
        }

        private void setSafeHarbor(Double pSafeHarbor) {
            ThirdParty401kPayrollItemInfo tp401kPayrollItemInfo =
                    PayrollItem.findItemByPayrollItemCode(PayrollItemCode.Tp401kSafeHarbor).getThirdParty401kPayrollItemInfo();
            if (tp401kPayrollItemInfo.getIsProviderAccepted()) {
                if (pSafeHarbor < 0) {
                    if (tp401kPayrollItemInfo.getAllowsNegativeAmounts()) {
                        this.mSafeHarbor = pSafeHarbor.toString();
                    } else {
                        this.mSafeHarbor = null;
                        isSafeHarborValid = false;
                        addNegativeValueError("Safe Harbor", pSafeHarbor);
                    }
                } else {
                    this.mSafeHarbor = pSafeHarbor.toString();
                }
            } else {
                this.mSafeHarbor = null;
            }
        }

        public String getHours() {
            return mHours;
        }

        private void setHours(Integer pHours) {
            if (pHours >= 0 && pHours <= 9999) {
                this.mHours = pHours.toString();
            } else {
                this.mHours = null;
            }
        }

        public String getBeginPayPeriod() {
            return mBeginPayPeriod;
        }

        private void setBeginPayPeriod(SpcfCalendar pBeginPayPeriod) {
            if (pBeginPayPeriod != null) {
                this.mBeginPayPeriod = pBeginPayPeriod.toLocal().format("yyyyMMdd");
            } else {
                validationErrors.add("Field 'Beginning Pay Period' contains invalid 401k data");
            }
        }

        public String getEndPayPeriod() {
            return mEndPayPeriod;
        }

        private void setEndPayPeriod(SpcfCalendar pEndPayPeriod) {
            if (pEndPayPeriod != null) {
                this.mEndPayPeriod = pEndPayPeriod.toLocal().format("yyyyMMdd");
            } else {
                validationErrors.add("Field 'Ending Pay Period' contains invalid 401k data");
            }
        }

        public boolean isSalaryValid() {
            return isSalaryValid;
        }

        public boolean isDeferralValid() {
            return isDeferralValid;
        }

        public boolean isRothValid() {
            return isRothValid;
        }

        public boolean isLoanValid() {
            return isLoanValid;
        }

        public boolean isMatchingValid() {
            return isMatchingValid;
        }

        public boolean isProfitSharingValid() {
            return isProfitSharingValid;
        }

        public boolean isSafeHarborValid() {
            return isSafeHarborValid;
        }

        private void addNegativeValueError(String pField, Double pAmount) {
            validationErrors.add("Field '" + pField + "' contains a negative amount (" + pAmount + ") and the provide does not accept negative values");
        }

        public ArrayList<String> isValidForPayrollFile() {
            return validationErrors;
        }

        protected String key() {
            String key = "";
            key += getBeginPayPeriod();
            key += getEndPayPeriod();
            key += getSalary();
            key += getDeferral();
            key += getRoth();
            key += getLoan();
            key += getMatching();
            key += getProfitSharing();
            key += getSafeHarbor();
            key += getHours();
            return key;
        }
    }

}