package com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.common.ofx.response.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Nov 3, 2011
 * Time: 9:25:13 PM
 */
public class PaycheckResponse {
    private IPAYCHKMOD mIPAYCHKMOD;
    private Company mCompany;

    private DomainEntitySet<Compensation> mCompensations = new DomainEntitySet<Compensation>();
    private DomainEntitySet<Deduction> mDeductions = new DomainEntitySet<Deduction>();
    private DomainEntitySet<EmployerContribution> mEmployerContributions = new DomainEntitySet<EmployerContribution>();
    private DomainEntitySet<Tax> mTaxes = new DomainEntitySet<Tax>();
    private DomainEntitySet<PaycheckSplit> mPaycheckSplits = new DomainEntitySet<PaycheckSplit>();

    public IPAYCHKMOD getIPAYCHKMOD() {
        addDetail();
        return mIPAYCHKMOD;
    }

    public PaycheckResponse(Paycheck pPaycheck, OFXAPPVERObject pAppVerion) {
        mCompany = pPaycheck.getCompany();

        mIPAYCHKMOD = new IPAYCHKMOD();
        mIPAYCHKMOD.setIPAYCHKID(pPaycheck.getSourcePaycheckId());

        if(pPaycheck.getNetAmount() != null) {
            // we do not update paychecks once they are submitted and QB zeros out voids
            if(pPaycheck.isVoidedOrRecalled()) {
                mIPAYCHKMOD.setIAMT(QBOFX.convertSpcfMoneyToOFXString(SpcfMoney.ZERO));
            } else {
                mIPAYCHKMOD.setIAMT(QBOFX.convertSpcfMoneyToOFXString(new SpcfMoney(pPaycheck.getNetAmount().negate())));
            }
        }
        mIPAYCHKMOD.setIPAYCHKTYPE(pPaycheck.getIsYTDAdjustment() ? "YTDADJ" : "PAYCHK");
        mIPAYCHKMOD.setIDTPAYPDBEGIN(QBOFX.convertToOFXDate(pPaycheck.getPayPeriodBeginDate()));
        mIPAYCHKMOD.setIDTPAYPDEND(QBOFX.convertToOFXDate(pPaycheck.getPayPeriodEndDate()));
        mIPAYCHKMOD.setIVOID(QBOFX.Y_N(pPaycheck.isVoidedOrRecalled()));

        if(pPaycheck.getPayrollRun() != null) {
            mIPAYCHKMOD.setIDTTX(QBOFX.convertToOFXDate(pPaycheck.getPayrollRun().getPaycheckDate()));
        }

        if(pPaycheck.getSourceEmployee() != null) {
            if(com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Paycheck.NOT_ALL_DIGITS_PATTERN.matcher(pPaycheck.getSourceEmployee().getSourceEmployeeId()).matches()) {
                mIPAYCHKMOD.setIEMPID(Employee.DEFAULT_QB_EMPLOYEE_ID);
                mIPAYCHKMOD.setIEMPNAME(pPaycheck.getSourceEmployee().getSourceEmployeeId());
            } else {
                mIPAYCHKMOD.setIEMPID(pPaycheck.getSourceEmployee().getSourceEmployeeId());
                mIPAYCHKMOD.setIEMPNAME(pPaycheck.getSourceEmployee().getFullName());
            }
        }

        mIPAYCHKMOD.setIPAYCHKINFO(new IPAYCHKINFO());
        if(pPaycheck.getQbdtPaycheckInfo() != null) {
            QbdtPaycheckInfo qbdtPaycheckInfo = pPaycheck.getQbdtPaycheckInfo();
            mIPAYCHKMOD.setIACCTNAME(QBOFX.convertNullToOFXString(qbdtPaycheckInfo.getAccountName()));
            mIPAYCHKMOD.setICLASS(QBOFX.convertNullToOFXString(qbdtPaycheckInfo.getTrackingClass()));
            mIPAYCHKMOD.setICLEARED(QBOFX.convertNullToOFXString(qbdtPaycheckInfo.getCleared()));
            mIPAYCHKMOD.setIMEMO(QBOFX.convertNullToOFXString(qbdtPaycheckInfo.getMemo()));
            mIPAYCHKMOD.setIONSERVICE(QBOFX.Y_N(qbdtPaycheckInfo.getOnService()));

            if(pAppVerion.listIdLoopBackSupported() && qbdtPaycheckInfo.getListId() != null) {
                mIPAYCHKMOD.setIQBUNIQUEID(qbdtPaycheckInfo.getListId());
            }

            // todo if QB sends null is it ok to send back 0
            mIPAYCHKMOD.getIPAYCHKINFO().setISICKACCRUED(QBOFX.convertDoubleToOFXString(qbdtPaycheckInfo.getSickHoursAccrued()));
            mIPAYCHKMOD.getIPAYCHKINFO().setIVACACCRUED(QBOFX.convertDoubleToOFXString(qbdtPaycheckInfo.getVacationHoursAccrued()));
            mIPAYCHKMOD.getIPAYCHKINFO().setICHKNUM(QBOFX.convertNullToOFXString(qbdtPaycheckInfo.getCheckNumber()));
            mIPAYCHKMOD.getIPAYCHKINFO().setIPRORATE(QBOFX.Y_N(qbdtPaycheckInfo.getProrate()));
        }
    }

    public DomainEntitySet<Compensation> getCompensations() {
        return mCompensations;
    }

    public DomainEntitySet<Deduction> getDeductions() {
        return mDeductions;
    }

    public DomainEntitySet<EmployerContribution> getEmployerContributions() {
        return mEmployerContributions;
    }

    public DomainEntitySet<Tax> getTaxes() {
        return mTaxes;
    }

    public DomainEntitySet<PaycheckSplit> getPaycheckSplits() {
        return mPaycheckSplits;
    }

    private void addDetail() {
        // keep adjustments in order
        Map<Long, IADJLINE> adjustmentLines = new TreeMap<Long, IADJLINE>();

        for (Compensation compensation : mCompensations.sort(Compensation.PayStubOrder())) {
            CompanyPayrollItem companyPayrollItem = compensation.getCompanyPayrollItem();
            PayrollItemCode payrollItemCode = companyPayrollItem.getPayrollItem().getPayrollItemCode();
            if(payrollItemCode == PayrollItemCode.Hourly || payrollItemCode == PayrollItemCode.Commission || payrollItemCode == PayrollItemCode.Bonus) {
                mIPAYCHKMOD.getIHRLYWAGELINE().add(buildIHRLYWAGELINE(compensation));
            } else if (payrollItemCode == PayrollItemCode.Salary) {
                mIPAYCHKMOD.getISALARYLINE().add(buildISALARYLINE(compensation));
            } else {
                adjustmentLines.put(compensation.getPayStubOrder(), buildIADJLINE(compensation));
            }
        }
        mCompensations = new DomainEntitySet<Compensation>();

        for (Deduction deduction : mDeductions.sort(Deduction.PayStubOrder())) {
            adjustmentLines.put(deduction.getPayStubOrder(), buildIADJLINE(deduction));
        }
        mDeductions = new DomainEntitySet<Deduction>();

        for (EmployerContribution employerContribution : mEmployerContributions.sort(EmployerContribution.PayStubOrder())) {
            adjustmentLines.put(employerContribution.getPayStubOrder(), buildIADJLINE(employerContribution));
        }
        mEmployerContributions = new DomainEntitySet<EmployerContribution>();

        mIPAYCHKMOD.getIADJLINE().addAll(adjustmentLines.values());

        for (PaycheckSplit paycheckSplit : mPaycheckSplits.sort(PaycheckSplit.PayStubOrder())) {
            mIPAYCHKMOD.getIDDLINE().add(buildIDDLINE(paycheckSplit));
        }
        mPaycheckSplits = new DomainEntitySet<PaycheckSplit>();

        for (Tax tax : mTaxes.sort(Tax.PayStubOrder())) {
            mIPAYCHKMOD.getITAXLINE().add(buildITAXLINE(tax));
        }
        mTaxes = new DomainEntitySet<Tax>();
    }

    private IHRLYWAGELINE buildIHRLYWAGELINE(Compensation pCompensation) {
        IHRLYWAGELINE ihrlywageline = new IHRLYWAGELINE();
        if(pCompensation.getCompanyPayrollItem() != null) {
            ihrlywageline.setIPITEMID(pCompensation.getCompanyPayrollItem().getLatestCompanyPayrollItem().getSourcePayrollItemId());
        }
        ihrlywageline.setIAMT(QBOFX.convertSpcfMoneyToOFXString(pCompensation.getCompensationAmount()));
        ihrlywageline.setIYTDAMT(QBOFX.convertSpcfMoneyToOFXString(pCompensation.getCompensationYTDAmount()));
        ihrlywageline.setIHRS(QBOFX.convertDoubleToOFXString(pCompensation.getHoursWorked()));

        if(pCompensation.getQbdtPaylineInfo() != null) {
            QbdtPaylineInfo qbdtPaylineInfo = pCompensation.getQbdtPaylineInfo();
            ihrlywageline.setICLASS(QBOFX.convertNullToOFXString(qbdtPaylineInfo.getTrackingClass()));
            ihrlywageline.setIITEM(QBOFX.convertNullToOFXString(qbdtPaylineInfo.getItem()));
            ihrlywageline.setIJOB(QBOFX.convertNullToOFXString(qbdtPaylineInfo.getJob()));
            ihrlywageline.setIRATE(QBOFX.mapNumericTypeToString(qbdtPaylineInfo.getRateType(), qbdtPaylineInfo.getRate()));
            ihrlywageline.setIWCCODE(qbdtPaylineInfo.getWcCode());
        }
        return ihrlywageline;
    }

    private ISALARYLINE buildISALARYLINE(Compensation pCompensation) {
        ISALARYLINE isalaryline = new ISALARYLINE();
        if(pCompensation.getCompanyPayrollItem() != null) {
            isalaryline.setIPITEMID(pCompensation.getCompanyPayrollItem().getLatestCompanyPayrollItem().getSourcePayrollItemId());
        }
        isalaryline.setIAMT(QBOFX.convertSpcfMoneyToOFXString(pCompensation.getCompensationAmount()));
        isalaryline.setIYTDAMT(QBOFX.convertSpcfMoneyToOFXString(pCompensation.getCompensationYTDAmount()));
        isalaryline.setIHRS(QBOFX.convertDoubleToOFXString(pCompensation.getHoursWorked()));

        if(pCompensation.getQbdtPaylineInfo() != null) {
            QbdtPaylineInfo qbdtPaylineInfo = pCompensation.getQbdtPaylineInfo();
            isalaryline.setICLASS(QBOFX.convertNullToOFXString(qbdtPaylineInfo.getTrackingClass()));
            isalaryline.setIITEM(qbdtPaylineInfo.getItem());
            isalaryline.setIJOB(qbdtPaylineInfo.getJob());
            isalaryline.setIRATE(QBOFX.mapNumericTypeToString(qbdtPaylineInfo.getRateType(), qbdtPaylineInfo.getRate()));
            isalaryline.setIWCCODE(qbdtPaylineInfo.getWcCode());
        }
        return isalaryline;
    }

    private IADJLINE buildIADJLINE(Compensation pCompensation) {
        IADJLINE iadjline = new IADJLINE();
        if(pCompensation.getCompanyPayrollItem() != null) {
            iadjline.setIPITEMID(pCompensation.getCompanyPayrollItem().getLatestCompanyPayrollItem().getSourcePayrollItemId());
        }
        iadjline.setIAMT(QBOFX.convertSpcfMoneyToOFXString(pCompensation.getCompensationAmount()));
        iadjline.setIYTDAMT(QBOFX.convertSpcfMoneyToOFXString(pCompensation.getCompensationYTDAmount()));

        if(pCompensation.getQbdtPaylineInfo() != null) {
            QbdtPaylineInfo qbdtPaylineInfo = pCompensation.getQbdtPaylineInfo();
            iadjline.setIEXPBYJOB(QBOFX.Y_N(qbdtPaylineInfo.getExpenseByJob()));
            String quantity = QBOFX.mapNumericTypeToString(qbdtPaylineInfo.getQuantityType(), qbdtPaylineInfo.getQuantity());
            if("0".equals(quantity)) {
                iadjline.setIQTY(QBOFX.NULL);
            } else {
                iadjline.setIQTY(quantity);
            }
            iadjline.setIRATE(QBOFX.mapNumericTypeToString(qbdtPaylineInfo.getRateType(), qbdtPaylineInfo.getRate()));
        }
        return iadjline;
    }

    private IADJLINE buildIADJLINE(Deduction pDeduction) {
        IADJLINE iadjline = new IADJLINE();
        if(pDeduction.getCompanyPayrollItem() != null) {
            iadjline.setIPITEMID(pDeduction.getCompanyPayrollItem().getLatestCompanyPayrollItem().getSourcePayrollItemId());
        }
        iadjline.setIAMT(QBOFX.convertSpcfMoneyToOFXString(new SpcfMoney(pDeduction.getDeductionAmount().negate())));
        iadjline.setIYTDAMT(QBOFX.convertSpcfMoneyToOFXString(new SpcfMoney(pDeduction.getDeductionYTDAmount().negate())));

        if(pDeduction.getQbdtPaylineInfo() != null) {
            QbdtPaylineInfo qbdtPaylineInfo = pDeduction.getQbdtPaylineInfo();
            iadjline.setIEXPBYJOB(QBOFX.Y_N(qbdtPaylineInfo.getExpenseByJob()));
            String quantity = QBOFX.mapNumericTypeToString(qbdtPaylineInfo.getQuantityType(), qbdtPaylineInfo.getQuantity());
            if("0".equals(quantity)) {
                iadjline.setIQTY(QBOFX.NULL);
            } else {
                iadjline.setIQTY(quantity);
            }
            iadjline.setIRATE(QBOFX.mapNumericTypeToString(qbdtPaylineInfo.getRateType(), qbdtPaylineInfo.getRate()));
        }
        return iadjline;
    }

    private IADJLINE buildIADJLINE(EmployerContribution pEmployerContribution) {
        IADJLINE iadjline = new IADJLINE();
        if(pEmployerContribution.getCompanyPayrollItem() != null) {
            iadjline.setIPITEMID(pEmployerContribution.getCompanyPayrollItem().getLatestCompanyPayrollItem().getSourcePayrollItemId());
        }
        iadjline.setIAMT(QBOFX.convertSpcfMoneyToOFXString(pEmployerContribution.getContributionAmount()));
        iadjline.setIYTDAMT(QBOFX.convertSpcfMoneyToOFXString(pEmployerContribution.getContributionYTDAmount()));

        if(pEmployerContribution.getQbdtPaylineInfo() != null) {
            QbdtPaylineInfo qbdtPaylineInfo = pEmployerContribution.getQbdtPaylineInfo();
            iadjline.setIEXPBYJOB(QBOFX.Y_N(qbdtPaylineInfo.getExpenseByJob()));
            String quantity = QBOFX.mapNumericTypeToString(qbdtPaylineInfo.getQuantityType(), qbdtPaylineInfo.getQuantity());
            if("0".equals(quantity)) {
                iadjline.setIQTY(QBOFX.NULL);
            } else {
                iadjline.setIQTY(quantity);
            }
            iadjline.setIRATE(QBOFX.mapNumericTypeToString(qbdtPaylineInfo.getRateType(), qbdtPaylineInfo.getRate()));
        }
        return iadjline;
    }

    private IDDLINE buildIDDLINE(PaycheckSplit pPaycheckSplit) {
        IDDLINE iddline = new IDDLINE();
        CompanyPayrollItem ddItem = CompanyPayrollItem.findDirectDepositPayrollItem(mCompany);
        if(ddItem != null) {
            iddline.setIPITEMID(ddItem.getSourcePayrollItemId());
        } else {
            iddline.setIPITEMID(QBOFX.DEFAULT_PITEM_ID);
        }
        iddline.setIAMT(QBOFX.convertSpcfMoneyToOFXString(new SpcfMoney(pPaycheckSplit.getPaycheckSplitAmount().negate())));

        IDDACCT iddacct = new IDDACCT();
        // this is used for setting up an ee account, not used in the paycheck, but is required.
        iddacct.setIAMT("");

        if(pPaycheckSplit.getEmployeeBankAccount() != null) {
            BankAccount bankAccount = pPaycheckSplit.getEmployeeBankAccount().getBankAccount();
            iddacct.setIACCTNAME(bankAccount.getBankName());
            BANKACCT bankacct = new BANKACCT();
            bankacct.setACCTID(bankAccount.getAccountNumber());
            bankacct.setACCTTYPE(QBOFX.mapOFXBankAccountType(bankAccount.getAccountTypeCd()));
            bankacct.setBANKID(bankAccount.getRoutingNumber());
            iddacct.setBANKACCTTO(bankacct);
        }
        iddline.setIDDACCT(iddacct);
        return iddline;
    }

    private ITAXLINE buildITAXLINE(Tax pTax) {
        ITAXLINE itaxline = new ITAXLINE();
        boolean employeePaidTax = false;
        if(pTax.getCompanyLaw() != null) {
            CompanyLaw companyLaw = pTax.getCompanyLaw().getLatestCompanyLaw();
            itaxline.setIPITEMID(companyLaw.getSourceId());
            employeePaidTax = pTax.getCompanyLaw().getQbdtPayrollItemInfo().getIsEmployeePaid();
        }

        if(employeePaidTax) {
            itaxline.setIAMT(QBOFX.convertSpcfMoneyToOFXString(new SpcfMoney(pTax.getTaxLiabilityAmount().negate())));
            itaxline.setIYTDAMT(QBOFX.convertSpcfMoneyToOFXString(new SpcfMoney(pTax.getTaxLiabilityYTDAmount().negate())));
        } else {
            itaxline.setIAMT(QBOFX.convertSpcfMoneyToOFXString(pTax.getTaxLiabilityAmount()));
            itaxline.setIYTDAMT(QBOFX.convertSpcfMoneyToOFXString(pTax.getTaxLiabilityYTDAmount()));
        }

        itaxline.setITAXABLEWAGE(QBOFX.convertSpcfMoneyToOFXString(pTax.getTotalWagesAmount()));
        itaxline.setITIPSWB(QBOFX.convertSpcfMoneyToOFXString(pTax.getTipsTaxableWageAmount()));
        itaxline.setIWB(QBOFX.convertSpcfMoneyToOFXString(pTax.getTaxableWagesAmount()));
        return itaxline;
    }
}
