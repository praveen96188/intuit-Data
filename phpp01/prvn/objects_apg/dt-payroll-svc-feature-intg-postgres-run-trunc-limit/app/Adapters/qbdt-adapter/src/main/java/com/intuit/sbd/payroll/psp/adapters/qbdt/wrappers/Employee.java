package com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers;

import com.intuit.sbd.payroll.psp.common.ofx.request.ACCRUAL;
import com.intuit.sbd.payroll.psp.common.ofx.request.BANKACCT;
import com.intuit.sbd.payroll.psp.common.ofx.request.IADJ;
import com.intuit.sbd.payroll.psp.common.ofx.request.ICUSTOMFLD;
import com.intuit.sbd.payroll.psp.common.ofx.request.IDDACCT;
import com.intuit.sbd.payroll.psp.common.ofx.request.IEMP;
import com.intuit.sbd.payroll.psp.common.ofx.request.IEMPFIT;
import com.intuit.sbd.payroll.psp.common.ofx.request.IEMPOTHERTAX;
import com.intuit.sbd.payroll.psp.common.ofx.request.IEMPSDI;
import com.intuit.sbd.payroll.psp.common.ofx.request.IEMPSIT;
import com.intuit.sbd.payroll.psp.common.ofx.request.IEMPSUI;
import com.intuit.sbd.payroll.psp.common.ofx.request.IEMPTAX;
import com.intuit.sbd.payroll.psp.common.ofx.request.ISETTING;
import com.intuit.sbd.payroll.psp.common.ofx.request.IWAGE;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.AccrualPeriod;
import com.intuit.sbd.payroll.psp.domain.AccrualType;
import com.intuit.sbd.payroll.psp.domain.BankAccountType;
import com.intuit.sbd.payroll.psp.domain.EmployeeStatus;
import com.intuit.sbd.payroll.psp.domain.EmployeeTaxType;
import com.intuit.sbd.payroll.psp.domain.Gender;
import com.intuit.sbd.payroll.psp.domain.PayrollFrequencyCode;
import com.intuit.sbd.payroll.psp.domain.QbdtEmployeeType;
import com.intuit.sbd.payroll.psp.domain.QbdtNumericType;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.sbd.payroll.psp.domain.QbdtEmployeeSeasonal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 13, 2010
 * Time: 9:58:09 AM
 */
public class Employee {
    private static final String DEFAULT_SSN = "000000000";
    private static final Pattern SSN_PATTERN = Pattern.compile(".*?([0-9]{3}).*?([0-9]{2}).*?([0-9]{4}).*?");

    public static final String DEFAULT_QB_EMPLOYEE_ID = "0";

    protected IEMP mIEMP;

    private boolean hasAddressInfo = false;
    private boolean hasPayrollInfo = false;
    private boolean hasSickInfo = false;
    private boolean hasVacationInfo = false;
    private boolean hasTaxInfo = false;
    private boolean hasWagePlanInfo = false;
    private boolean hasBankAccountInfo = false;

    private boolean migratingEmployee = false;

    public Employee(IEMP pIEMP) {
        mIEMP = pIEMP;
        hasAddressInfo = mIEMP.getIADDRINFO() != null;
        hasPayrollInfo = mIEMP.getIPAYROLL() != null;
        hasSickInfo = mIEMP.getISICK() != null;
        hasTaxInfo = mIEMP.getIEMPTAX() != null;
        hasVacationInfo = mIEMP.getIVAC() != null;
        hasWagePlanInfo = mIEMP.getIEMPCOMPLIANCE() != null && !mIEMP.getIEMPCOMPLIANCE().getISETTING().isEmpty();
        hasBankAccountInfo = mIEMP.getIEMPDD() != null && mIEMP.getIEMPDD().getIDDACCT() != null;
    }

    public boolean isAccrualOnlyMod() {
        return hasSickInfo && hasVacationInfo && !(hasAddressInfo || hasPayrollInfo || hasTaxInfo || hasWagePlanInfo || hasBankAccountInfo);
    }

    public boolean isMigratingEmployee() {
        return migratingEmployee;
    }

    public void setMigratingEmployee(boolean pMigratingEmployee) {
        migratingEmployee = pMigratingEmployee;
    }

    public String getSourceId() {
        return mIEMP.getIEMPID();
    }

    public String getListId() {
        return mIEMP.getIQBUNIQUEID();
    }
    
    public String sessionId(){
    	return mIEMP.getISESSIONID();
    }

    public String getSSN() {
        String ssn = mIEMP.getISSN();

        if (ssn != null) {
            Matcher matcher = SSN_PATTERN.matcher(ssn.trim());
            if (matcher.matches()) {
                ssn = matcher.replaceAll("$1$2$3");
            } else {
                ssn = DEFAULT_SSN;
            }
        }

        return ssn;
    }

    public String getBillPayAccount() {
        return QBOFX.truncateOFXString(mIEMP.getIBILLPAYACCT(), 64);
    }

    public Date getHireDate() {
        return QBOFX.mapOFXStringToDate(mIEMP.getIDTHIRE());
    }

    public Date getReleaseDate() {
        return QBOFX.mapOFXStringToDate(mIEMP.getIDTRELEASE());
    }

    public Date getBirthDate() {
        return QBOFX.mapOFXStringToDate(mIEMP.getIBIRTHDATE());
    }

    public Gender getGender() {
        String gender = mIEMP.getIEMPGENDER();
        if(gender == null || gender.equals("") || gender.equals("UNKNOWN")){
            return null;
        }
        else if(gender.equals("MALE")) {
            return Gender.Male;
        } else if(gender.equals("FEMALE")) {
            return Gender.Female;
        }

        return null;
    }

    public QbdtEmployeeType getEmployeeType() {
        String employeeType = mIEMP.getIEMPTYPE();
        if(employeeType == null || employeeType.equals("")) {
            return null;
        }

        return QbdtEmployeeType.valueOf(employeeType);
    }

   

    public QbdtEmployeeSeasonal getIsSeasonal() {
        String isSeasonal = mIEMP.getIEMPSEASONAL();
        if(isSeasonal == null || isSeasonal.equals("")) {
            return null;
        }
        return QbdtEmployeeSeasonal.valueOf(isSeasonal);
    }

    public EmployeeStatus getEmployeeStatus() {
        return QBOFX.mapOFXStringToBoolean(mIEMP.getIINACTIVE()) ? EmployeeStatus.Inactive : EmployeeStatus.Active;
    }

    public String getTitle() {
        if(hasAddressInfo) {
            return QBOFX.truncateOFXString(mIEMP.getIADDRINFO().getITITLE(), 20);
        }
        return null;
    }

    public String getFirstName() {
        if(hasAddressInfo) {
            return QBOFX.truncateOFXString(mIEMP.getIADDRINFO().getIFIRST(), 80);
        }
        return null;
    }

    public String getMiddleInitial() {
        if(hasAddressInfo) {
            return QBOFX.truncateOFXString(mIEMP.getIADDRINFO().getIMI(), 80);
        }
        return null;
    }

    // contains suffix also
    public String getLastName() {
        if(hasAddressInfo) {
            return QBOFX.truncateOFXString(mIEMP.getIADDRINFO().getILAST(), 80);
        }
        return null;
    }

    public String getAddressLine1() {
        if(hasAddressInfo) {
            return QBOFX.truncateOFXString(mIEMP.getIADDRINFO().getIADDR1(), 80);
        }
        return null;
    }

    public String getAddressLine2() {
        if(hasAddressInfo) {
            return QBOFX.truncateOFXString(mIEMP.getIADDRINFO().getIADDR2(), 80);
        }
        return null;
    }

    public String getCity() {
        if(hasAddressInfo) {
            return QBOFX.truncateOFXString(mIEMP.getIADDRINFO().getICITY(), 256);
        }
        return null;
    }

    public String getState() {
        if(hasAddressInfo) {
            return QBOFX.truncateOFXString(mIEMP.getIADDRINFO().getISTATE(), 21);
        }
        return null;
    }

    public String getZipCode() {
        if(hasAddressInfo) {
            return QBOFX.truncateOFXString(mIEMP.getIADDRINFO().getIPOSTALCODE(), 13);
        }
        return null;
    }

    public String getPhone() {
        if(hasAddressInfo) {
            return QBOFX.truncateOFXString(mIEMP.getIADDRINFO().getIPHONE(), 100);
        }
        return null;
    }

    public String getEmail() {
        if(hasAddressInfo) {
            return mIEMP.getIADDRINFO().getIEMAIL();
        }
        return null;
    }

    public String getLiveState() {
        if(hasAddressInfo) {
            return mIEMP.getIADDRINFO().getISTATELIVED();
        }
        return null;
    }

    public String getWorkState() {
        if(hasAddressInfo) {
            return mIEMP.getIADDRINFO().getISTATEWORKED();
        }
        return null;
    }

    public String getInitials() {
        if(hasAddressInfo) {
            return mIEMP.getIADDRINFO().getIINITIALS();
        }
        return null;
    }

    public String getPrintAsName() {
        if(hasAddressInfo) {
            return QBOFX.truncateOFXString(mIEMP.getIADDRINFO().getIPRINTASNAME(), 50);
        }
        return null;
    }

    public String getAltPhone() {
        if(hasAddressInfo) {
            return QBOFX.truncateOFXString(mIEMP.getIADDRINFO().getIALTPHONE(), 21);
        }
        return null;
    }

    public PayrollFrequencyCode getPayPeriod() {
        if(hasPayrollInfo) {

            QBOFX.OFXPayrollFrequency ofxPayrollFrequency;

            try {
                ofxPayrollFrequency = QBOFX.OFXPayrollFrequency.valueOf(mIEMP.getIPAYROLL().getIPAYPD());
            } catch (Exception e) {
                // couldn't map the ofx value
                return null;
            }

            return QBOFX.mapPayrollFrequency(ofxPayrollFrequency);
        }
        return null;
    }

    public boolean getHasRetirementPlan() {
        return hasPayrollInfo && QBOFX.mapOFXStringToBoolean(mIEMP.getIPAYROLL().getIPPLANOVERRIDE());
    }

    public List<EmployeePayrollItem> getWages() {
        List<EmployeePayrollItem> wages = new ArrayList<EmployeePayrollItem>();
        if(hasPayrollInfo) {
            for (IWAGE iwage : mIEMP.getIPAYROLL().getIWAGE()) {
                wages.add(new EmployeePayrollItem(iwage.getIPITEMID(),
                                                  QBOFX.mapOFXStringToDouble(iwage.getIRATE()),
                                                  QBOFX.mapOFXStringNumericType(iwage.getIRATE())));
            }
        }
        return wages;
    }

    public List<EmployeePayrollItem> getAdjustments() {
        List<EmployeePayrollItem> adjustments = new ArrayList<EmployeePayrollItem>();
        if(hasPayrollInfo) {
            for (IADJ iadj : mIEMP.getIPAYROLL().getIADJ()) {
                adjustments.add(new EmployeePayrollItem(iadj.getIPITEMID(),
                                                        QBOFX.mapOFXStringToDouble(iadj.getIAMT()),
                                                        QBOFX.mapOFXStringNumericType(iadj.getIAMT()),
                                                        QBOFX.mapOFXStringToDouble(iadj.getILIMIT()),
                                                        QBOFX.mapOFXStringNumericType(iadj.getILIMIT())));
            }
        }
        return adjustments;
    }

    public String getClassTracking() {
        if(hasPayrollInfo) {
            return mIEMP.getIPAYROLL().getICLASS();
        }
        return null;
    }

    public boolean useDD() {
        return hasBankAccountInfo && QBOFX.mapOFXStringToBoolean(mIEMP.getIEMPDD().getIUSEDD());
    }

    public boolean useTime() {
        return hasPayrollInfo && QBOFX.mapOFXStringToBoolean(mIEMP.getIPAYROLL().getIUSETIME());
    }

    public boolean isDeceased() {
        return hasTaxInfo && QBOFX.mapOFXStringToBoolean(mIEMP.getIEMPTAX().getIDECEASED());
    }

    public boolean enforceSubjectTo() {
        return hasTaxInfo && QBOFX.mapOFXStringToBoolean(mIEMP.getIEMPTAX().getIENFORCESUBJECTTO());
    }

    public boolean qualifiesForAEIC() {
        return hasTaxInfo && QBOFX.mapOFXStringToBoolean(mIEMP.getIEMPTAX().getIQUALFORAEIC());
    }

    public List<EmployeeTax> getEmployeeTaxes() {
        IEMPTAX iemptax = mIEMP.getIEMPTAX();
        List<EmployeeTax> employeeTaxes = new ArrayList<EmployeeTax>();
        if(hasTaxInfo) {

            // FIT
            IEMPFIT iempfit = iemptax.getIEMPFIT();
            if(iempfit != null) {
                EmployeeTax fitEmployeeTax = new EmployeeTax(EmployeeTaxType.FIT,
                                                             QBOFX.mapOFXStringToBoolean(iemptax.getISUBJTOFIT()));

                int miscDataCount = 0;
                for (String data : iempfit.getITAXTBLMISCDATA()) {
                    fitEmployeeTax.getMiscData().put(miscDataCount++, data);
                }

                employeeTaxes.add(fitEmployeeTax);
            }

            // FICA
            EmployeeTax ficaEmployeeTax = new EmployeeTax(EmployeeTaxType.FICA,
                                                          QBOFX.mapOFXStringToBoolean(iemptax.getISUBJTOSS()));
            employeeTaxes.add(ficaEmployeeTax);

            //FUTA
            EmployeeTax futaEmployeeTax = new EmployeeTax(EmployeeTaxType.FUTA,
                                                          QBOFX.mapOFXStringToBoolean(iemptax.getISUBJTOFUTA()));
            employeeTaxes.add(futaEmployeeTax);

            // MED
            EmployeeTax medEmployeeTax = new EmployeeTax(EmployeeTaxType.MED,
                                                         QBOFX.mapOFXStringToBoolean(iemptax.getISUBJTOMCARE()));
            employeeTaxes.add(medEmployeeTax);

            // SIT
            IEMPSIT iempsit = iemptax.getIEMPSIT();
            if(iempsit != null) {
                EmployeeTax sitEmployeeTax = new EmployeeTax(EmployeeTaxType.SIT,
                                                             iempsit.getISTATE(),
                                                             iempsit.getITAXLAWVER());
                int miscDataCount = 0;
                for (String data : iempsit.getITAXTBLMISCDATA()) {
                    sitEmployeeTax.getMiscData().put(miscDataCount++, data);
                }
                employeeTaxes.add(sitEmployeeTax);
            }

            // SDI
            IEMPSDI iempsdi = iemptax.getIEMPSDI();
            if(iempsdi != null) {
                EmployeeTax sdiEmployeeTax = new EmployeeTax(EmployeeTaxType.SDI,
                                                             iempsdi.getISTATE());
                employeeTaxes.add(sdiEmployeeTax);
            }

            //SUI
            IEMPSUI iempsui = iemptax.getIEMPSUI();
            if(iempsui != null) {
                EmployeeTax suiEmployeeTax = new EmployeeTax(EmployeeTaxType.SUI,
                                                             iempsui.getISTATE());
                employeeTaxes.add(suiEmployeeTax);
            }

            // Other
            for (IEMPOTHERTAX iempothertax : iemptax.getIEMPOTHERTAX()) {
                EmployeeTax otherEmployeeTax = new EmployeeTax(iempothertax.getIPITEMID(),
                                                               iempothertax.getITAXLAWVER(),
                                                               iempothertax.getIW2NAME());

                int miscDataCount = 0;
                for (String data : iempothertax.getITAXTBLMISCDATA()) {
                    otherEmployeeTax.getMiscData().put(miscDataCount++, data);
                }

                employeeTaxes.add(otherEmployeeTax);
            }
        }
        return employeeTaxes;
    }

    public String getFedFilingStatus() {
        if(hasTaxInfo && mIEMP.getIEMPTAX().getIEMPFIT() != null) {
            return mIEMP.getIEMPTAX().getIEMPFIT().getIFEDFILESTATUS();
        }
        return null;
    }

    public int getFedFilingAllowances() {
        if(hasTaxInfo && mIEMP.getIEMPTAX().getIEMPFIT() != null) {
            return QBOFX.mapOFXStringToInt(mIEMP.getIEMPTAX().getIEMPFIT().getIALLOWANCES());
        }
        return 0;
    }

    public SpcfMoney getFedExtraWithholding() {
        if(hasTaxInfo && mIEMP.getIEMPTAX().getIEMPFIT() != null) {
            return QBOFX.mapOFXStringToMoney(mIEMP.getIEMPTAX().getIEMPFIT().getIEXTRAWITHHOLD());
        }
        return null;
    }

    public String getStateFilingStatus() {
        if(hasTaxInfo && mIEMP.getIEMPTAX().getIEMPSIT() != null) {
            return mIEMP.getIEMPTAX().getIEMPSIT().getISTATEFILESTATUS();
        }
        return null;
    }

    public int getStateFilingAllowances() {
        if(hasTaxInfo && mIEMP.getIEMPTAX().getIEMPSIT() != null) {
            return QBOFX.mapOFXStringToInt(mIEMP.getIEMPTAX().getIEMPSIT().getIALLOWANCES());
        }
        return 0;
    }

    public double getStateExtraWithholding() {
        if(hasTaxInfo && mIEMP.getIEMPTAX().getIEMPSIT() != null) {
            return QBOFX.mapOFXStringToDouble(mIEMP.getIEMPTAX().getIEMPSIT().getIEXTRAWITHHOLD());
        }
        return 0;
    }

    public SpcfMoney getFedClaimDependents(){
        if(hasTaxInfo && mIEMP.getIEMPTAX().getIEMPFIT() != null && mIEMP.getIEMPTAX().getIEMPFIT().getICLAIMDEPENDENTS() != null)
        {
            return  QBOFX.mapOFXStringToMoney(mIEMP.getIEMPTAX().getIEMPFIT().getICLAIMDEPENDENTS());
        }
        return null;
    }

    public SpcfMoney getFedOtherIncome(){
        if(hasTaxInfo && mIEMP.getIEMPTAX().getIEMPFIT() != null && mIEMP.getIEMPTAX().getIEMPFIT().getIOTHERINCOME() != null)
        {
            return  QBOFX.mapOFXStringToMoney(mIEMP.getIEMPTAX().getIEMPFIT().getIOTHERINCOME());
        }
        return null;
    }

    public SpcfMoney getFedDeduction(){
        if(hasTaxInfo && mIEMP.getIEMPTAX().getIEMPFIT() != null && mIEMP.getIEMPTAX().getIEMPFIT().getIDEDUCTIONS() != null)
        {
            return  QBOFX.mapOFXStringToMoney(mIEMP.getIEMPTAX().getIEMPFIT().getIDEDUCTIONS());
        }
        return null;
    }

    public boolean getFedMultipleJobs(){
        if(hasTaxInfo &&  mIEMP.getIEMPTAX().getIEMPFIT() != null && mIEMP.getIEMPTAX().getIEMPFIT().getIMULTIPLEJOBS() != null)
        {
            return QBOFX.mapOFXStringToBoolean(mIEMP.getIEMPTAX().getIEMPFIT().getIMULTIPLEJOBS());
        }
        return false;
    }

    public String getFedW4EmployeePref(){
        if(hasTaxInfo && mIEMP.getIEMPTAX().getIEMPFIT() != null && mIEMP.getIEMPTAX().getIEMPFIT().getIFEDW4EMPLOYEEPREF() != null)
        {
            return QBOFX.truncateOFXString(mIEMP.getIEMPTAX().getIEMPFIT().getIFEDW4EMPLOYEEPREF(), 256);
        }
        return null;
    }

    public QbdtNumericType getStateExtraWithholdingType() {
        if(hasTaxInfo && mIEMP.getIEMPTAX().getIEMPSIT() != null) {
            return QBOFX.mapOFXStringNumericType(mIEMP.getIEMPTAX().getIEMPSIT().getIEXTRAWITHHOLD());
        }
        return null;
    }

    public EmployeeAccrual getSickAccrual() {
        if(hasSickInfo) {
            return new EmployeeAccrual(mIEMP.getISICK(), AccrualType.Sick);
        }

        return null;
    }

    public EmployeeAccrual getVacationAccrual() {
        if(hasVacationInfo) {
            return new EmployeeAccrual(mIEMP.getIVAC(), AccrualType.Vacation);
        }

        return null;
    }

    public List<EmployeeWagePlan> getEmployeeWagePlans() {
        List<EmployeeWagePlan> employeeWagePlans = new ArrayList<EmployeeWagePlan>();

        if(hasWagePlanInfo) {
            EmployeeWagePlan employeeWagePlan;
            for (ISETTING isetting : mIEMP.getIEMPCOMPLIANCE().getISETTING()) {
                employeeWagePlan = new EmployeeWagePlan(isetting.getIDOMAIN(),
                                                        isetting.getISTATE(),
                                                        isetting.getINAME(),
                                                        isetting.getIVALUE(),
                                                        isetting.getIRULESVERSION(),
                                                        QBOFX.truncateOFXString(isetting.getIDESCRIPTION(), 100));
                employeeWagePlans.add(employeeWagePlan);
            }
        }

        return employeeWagePlans;
    }

    public Map<String, String> getCustomFields() {
        Map<String, String> customFields = new HashMap<String, String>();
        if(QBOFX.mapOFXStringToBoolean(mIEMP.getIHASCUSTOMFLD())) {
            for (ICUSTOMFLD icustomfld : mIEMP.getICUSTOMFLD()) {
                customFields.put(icustomfld.getIFLDNAME(), icustomfld.getIFLDVALUE());
            }
        }
        return customFields;
    }

    public List<EmployeeBankAccount> getEmployeeBankAccounts() {
        List<EmployeeBankAccount> employeeBankAccounts = new ArrayList<EmployeeBankAccount>();

        if(hasBankAccountInfo) {
            for (IDDACCT iddacct : mIEMP.getIEMPDD().getIDDACCT()) {
                employeeBankAccounts.add(new EmployeeBankAccount(iddacct));
            }
        }

        return employeeBankAccounts;
    }

    public class EmployeeTax {
        private String mCompanyLawId;
        private EmployeeTaxType mTaxType;
        private String mState;
        private boolean mSubjectTo;
        private String mTaxLawVersion;
        private String mW2Name;
        private Map<Integer, String> mMiscData;

        // constructor for SIT
        public EmployeeTax(EmployeeTaxType pTaxType, String pState,String pTaxLawVersion) {
            mTaxType = pTaxType;
            mState = pState;
            mSubjectTo = true;
            mTaxLawVersion = pTaxLawVersion;
        }

        // constructor for FUTA, MED, FICA, FIT
        public EmployeeTax(EmployeeTaxType pTaxType, boolean pSubjectTo) {
            mTaxType = pTaxType;
            mSubjectTo = pSubjectTo;
        }

        // constructor for SUI and SDI
        public EmployeeTax(EmployeeTaxType pTaxType, String pState) {
            mTaxType = pTaxType;
            mState = pState;
            mSubjectTo = true;
        }

        // constructor for other tax
        public EmployeeTax(String pCompanyLawId, String pTaxLawVersion, String pW2Name) {
            mCompanyLawId = pCompanyLawId;
            mTaxType = EmployeeTaxType.Other;
            mTaxLawVersion = pTaxLawVersion;
            mW2Name = pW2Name;
            mSubjectTo = true;
        }

        public String getCompanyLawId() {
            return mCompanyLawId;
        }

        public EmployeeTaxType getTaxType() {
            return mTaxType;
        }

        public String getState() {
            return mState;
        }

        public boolean isSubjectTo() {
            return mSubjectTo;
        }

        public String getTaxLawVersion() {
            return mTaxLawVersion;
        }

        public String getW2Name() {
            return mW2Name;
        }

        public Map<Integer, String> getMiscData() {
            if(mMiscData == null) {
                mMiscData = new HashMap<Integer, String>();
            }
            return mMiscData;
        }
    }

    public class EmployeePayrollItem {
        private String mPayrollItemId;
        private double mAmount;
        private QbdtNumericType mAmountType;
        private double mItemLimit;
        private QbdtNumericType mLimitType;

        public EmployeePayrollItem(String pPayrollItemId, double pAmount, QbdtNumericType pAmountType) {
            mPayrollItemId = pPayrollItemId;
            mAmount = pAmount;
            mAmountType = pAmountType;
        }

        public EmployeePayrollItem(String pPayrollItemId, double pAmount, QbdtNumericType pAmountType, double pLimit, QbdtNumericType pLimitType) {
            mPayrollItemId = pPayrollItemId;
            mAmount = pAmount;
            mAmountType = pAmountType;
            mItemLimit = pLimit;
            mLimitType = pLimitType;
        }

        public String getPayrollItemId() {
            return mPayrollItemId;
        }

        public double getAmount() {
            return mAmount;
        }

        public QbdtNumericType getAmountType() {
            return mAmountType;
        }

        public double getItemLimit() {
            return mItemLimit;
        }

        public QbdtNumericType getLimitType() {
            return mLimitType;
        }
    }

    public class EmployeeAccrual {
        private AccrualType mAccrualType;
        private AccrualPeriod mAccrualPeriod;
        private double mHours;
        private double mHoursPerPeriod;
        private double mMaxHours;
        private boolean mNewYearReset;

        public EmployeeAccrual(ACCRUAL pACCRUAL, AccrualType pAccrualType) {
            mAccrualType = pAccrualType;
            mAccrualPeriod = QBOFX.mapOFXAccrualPeriod(pACCRUAL.getIACCRUALPD());
            mHours = QBOFX.mapOFXStringToDouble(pACCRUAL.getIHRS());
            mHoursPerPeriod = QBOFX.mapOFXStringToDouble(pACCRUAL.getIHRSPERPD());
            mMaxHours = QBOFX.mapOFXStringToDouble(pACCRUAL.getIMAXHRS());
            mNewYearReset = QBOFX.mapOFXStringToBoolean(pACCRUAL.getINEWYRRESET());
        }

        public AccrualType getAccrualType() {
            return mAccrualType;
        }

        public AccrualPeriod getAccrualPeriod() {
            return mAccrualPeriod;
        }

        public double getHours() {
            return mHours;
        }

        public double getHoursPerPeriod() {
            return mHoursPerPeriod;
        }

        public double getMaxHours() {
            return mMaxHours;
        }

        public boolean isNewYearReset() {
            return mNewYearReset;
        }
    }

    public class EmployeeWagePlan {
        private String mDomain;
        private String mState;
        private String mName;
        private String mValue;
        private String mRulesVersion;
        private String mDescription;

        public EmployeeWagePlan(String pDomain, String pState, String pName, String pValue, String pRulesVersion, String pDescription) {
            mDomain = pDomain;
            mState = pState;
            mName = pName;
            mValue = pValue;
            mRulesVersion = pRulesVersion;
            mDescription = pDescription;
        }

        public String getDomain() {
            return mDomain;
        }

        public String getState() {
            return mState;
        }

        public String getName() {
            return mName;
        }

        public String getValue() {
            return mValue;
        }

        public String getRulesVersion() {
            return mRulesVersion;
        }

        public String getDescription() {
            return mDescription;
        }
    }

    public class EmployeeBankAccount {
        private String mAccountNumber;
        private String mRoutingNumber;
        private String mBankName;
        private BankAccountType mAccountType;
        private QbdtNumericType mAmountType;
        private double mAmount;
        
        public EmployeeBankAccount(IDDACCT pIDDACCT) {
            if(pIDDACCT.getBANKACCTTO() != null) {
                BANKACCT bankacct = pIDDACCT.getBANKACCTTO();
                mAccountNumber = QBOFX.nullStringCheck(bankacct.getACCTID());
                mAccountType = QBOFX.mapOFXStringToBankAccountType(bankacct.getACCTTYPE());
                mRoutingNumber = QBOFX.nullStringCheck(bankacct.getBANKID());
            }
            mBankName = QBOFX.nullStringCheck(pIDDACCT.getIACCTNAME());
            mAmountType = QBOFX.mapOFXStringNumericType(pIDDACCT.getIAMT());
            mAmount = QBOFX.mapOFXStringToDouble(pIDDACCT.getIAMT());
        }

        public String getAccountNumber() {
            return mAccountNumber;
        }

        public String getRoutingNumber() {
            return mRoutingNumber;
        }

        public String getBankName() {
            return mBankName;
        }

        public BankAccountType getAccountType() {
            return mAccountType;
        }

        public QbdtNumericType getAmountType() {
            return mAmountType;
        }

        public double getAmount() {
            return mAmount;
        }

        @Override
        public String toString() {
            return "EmployeeBankAccount{" +
                    "mAccountNumber='" + mAccountNumber + '\'' +
                    ", mRoutingNumber='" + mRoutingNumber + '\'' +
                    ", mBankName='" + mBankName + '\'' +
                    ", mAccountType=" + mAccountType +
                    ", mAmountType=" + mAmountType +
                    ", mAmount=" + mAmount +
                    '}';
        }
    }
}
