package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;

import java.text.NumberFormat;
import java.util.Date;

/**
 * User: rnorian
 * Date: Feb 21, 2011
 * Time: 3:52:04 PM
 */
public class OFXPaycheckGenerator {
    private NumberFormat currencyFMT = NumberFormat.getNumberInstance();

    private PayrollItemRepository payrollItemRepository;
    private IPAYCHK paycheck = new IPAYCHK();

    public OFXPaycheckGenerator(PayrollItemRepository pPayrollItemRepository, String pEMPID, String pCheckDate, double pAmount) {
        payrollItemRepository = pPayrollItemRepository;
        newPaycheck(pEMPID, pCheckDate, pAmount);
    }

    public void newPaycheck(String pEMPID, String pCheckDate, double pAmount) {
        IPAYCHK ipaychk = new IPAYCHK();
        ipaychk.setIPAYCHKID(OFXRequestGenerator.getNextPaycheckId() + "");
        ipaychk.setIEMPID(pEMPID);
        ipaychk.setIAMT(currencyFMT.format(pAmount*-1));

        ipaychk.setIPAYCHKTYPE("PAYCHK");
        ipaychk.setIACCTNAME(OFXRequestGenerator.LIABILITY_ACCOUNT);
        ipaychk.setICLASS("Paycheck Class");
        ipaychk.setICLEARED("2");
        ipaychk.setIDTPAYPDBEGIN(QBOFX.getDTTXResponse(new Date(pCheckDate)));
        ipaychk.setIDTPAYPDEND(QBOFX.getDTTXResponse(new Date(pCheckDate)));
        ipaychk.setIDTTX(QBOFX.getDTTXResponse(new Date(pCheckDate)));
        ipaychk.setIMEMO("memo");
        ipaychk.setIONSERVICE(QBOFX.Y_N(true));
        ipaychk.setIVOID(QBOFX.Y_N(false));

        // this is not used, but must be included
        ipaychk.setIEMPNAME("");
        paycheck = ipaychk;
    }

    public PayrollItemRepository getPayrollItemRepository() {
        return payrollItemRepository;
    }

    public void setPayrollItemRepository(PayrollItemRepository pPayrollItemRepository) {
        payrollItemRepository = pPayrollItemRepository;
    }

    public ISALARYLINE addSalaryLine(int pHours, double pAmount, double pYTDAmount) {
        return addSalaryLine(pHours, pAmount, pYTDAmount, payrollItemRepository.getSalaryItem());
    }

    public ISALARYLINE addSalaryLine(int pHours, double pAmount, double pYTDAmount, IPITEM pSalaryItem) {
        ISALARYLINE isalaryline = new ISALARYLINE();
        isalaryline.setIPITEMID(pSalaryItem.getIPITEMID());
        isalaryline.setIHRS(Integer.toString(pHours));
        isalaryline.setIAMT(currencyFMT.format(pAmount));
        isalaryline.setIYTDAMT(currencyFMT.format(pYTDAmount));
        isalaryline.setICLASS("SALRY CLASS");
        isalaryline.setIITEM("SALRY ITEM");
        isalaryline.setIJOB("SALRY JOB");
        isalaryline.setIRATE("0.78%");
        isalaryline.setIWCCODE("SALRY WCCODE");

        paycheck.getISALARYLINE().add(isalaryline);
        return isalaryline;
    }

    public IADJLINE addAdditionLine(double pAmount, double pYTDAmount, String pQuantity, String pRate) {
        return addAdditionLine(pAmount, pYTDAmount, pQuantity, pRate, payrollItemRepository.getAdditionItem());
    }

    public IADJLINE addAdditionLine(double pAmount, double pYTDAmount, String pQuantity, String pRate, IPITEM pAdditionItem) {
        return addAdjustLine(pAmount, pYTDAmount, pQuantity, pRate, pAdditionItem);
    }

    public IADJLINE addBonusLine(double pAmount, double pYTDAmount) {
        return addBonusLine(pAmount, pYTDAmount, payrollItemRepository.getBonusItem());
    }

    public IADJLINE addBonusLine(double pAmount, double pYTDAmount, IPITEM pBonusItem) {
        return addAdjustLine(pAmount, pYTDAmount, null, null, pBonusItem);
    }

    public IADJLINE addCommissionLine(double pAmount, double pYTDAmount) {
        return addCommissionLine(pAmount, pYTDAmount, payrollItemRepository.getCommissionItem());
    }

    public IADJLINE addCommissionLine(double pAmount, double pYTDAmount, IPITEM pCommissionItem) {
        return addAdjustLine(pAmount, pYTDAmount, null, null, pCommissionItem);
    }

    public IADJLINE addDeductionLine(double pAmount, double pYTDAmount, String pQuantity, String pRate) {
        return addDeductionLine(pAmount, pYTDAmount, pQuantity, pRate, payrollItemRepository.getDeductionItem());
    }

    public IADJLINE addDeductionLine(double pAmount, double pYTDAmount, String pQuantity, String pRate, IPITEM pDeductionItem) {
        return addAdjustLine(pAmount, pYTDAmount, pQuantity, pRate, pDeductionItem);
    }

    public IADJLINE addEmployerContributionLine(double pAmount, double pYTDAmount) {
        return addEmployerContributionLine(pAmount, pYTDAmount, payrollItemRepository.getEmployerContributionItem());
    }

    public IADJLINE addEmployerContributionLine(double pAmount, double pYTDAmount, IPITEM pERContributionItem) {
        return addAdjustLine(pAmount, pYTDAmount, null, null, pERContributionItem);
    }

    private IADJLINE addAdjustLine(double pAmount, double pYTDAmount, String pQuantity, String pRate, IPITEM pAdjustItem) {
        IADJLINE iadjline = new IADJLINE();
        PayrollItem payrollItem = new PayrollItem(pAdjustItem);
        switch (payrollItem.getItemType()) {
            case Addition:
            case Bonus:
            case Commission:
            case Deduction:
            case EmployerContribution:
                iadjline.setIAMT(currencyFMT.format(pAmount));
                iadjline.setIEXPBYJOB(QBOFX.Y_N(true));
                iadjline.setIPITEMID(payrollItem.getSourceId());
                if (payrollItem.getItemType() == QBOFX.OFXPayrollItemType.Addition ||
                        payrollItem.getItemType() == QBOFX.OFXPayrollItemType.Deduction) {
                    iadjline.setIQTY(pQuantity);
                    iadjline.setIRATE(pRate);
                }
                iadjline.setIYTDAMT(currencyFMT.format(pYTDAmount));
                break;
            default:
                throw new IllegalArgumentException("Invalid IPITEM type for Adjustment Line");
        }

        paycheck.getIADJLINE().add(iadjline);
        return iadjline;
    }

    public IDDLINE addDirectDepositLine(double pAmount, double pYTDAmount) {
        return addDirectDepositLine(pAmount, pYTDAmount, payrollItemRepository.getDeductionItem());
    }

    public IDDLINE addDirectDepositLine(double pAmount, double pYTDAmount, IPITEM pDirectDepositItem) {
        IDDLINE iddline = new IDDLINE();
        iddline.setIAMT("$3.00");
        // todo when we do dd
        IDDACCT iddacct = new IDDACCT();
        BANKACCT bankacct = new BANKACCT();
        bankacct.setACCTID("1");
        bankacct.setACCTTYPE("CHECKING");
        bankacct.setBANKID("1");
        iddacct.setBANKACCTTO(bankacct);
        iddacct.setIACCTNAME("blah");
        iddacct.setIAMT(currencyFMT.format(pAmount));
        iddline.setIDDACCT(iddacct);
        iddline.setIPITEMID(pDirectDepositItem.getIPITEMID());
        // ignored
        iddline.setIPITEMNAME("");

        paycheck.getIDDLINE().add(iddline);
        return iddline;
    }

    public IHRLYWAGELINE addHourlyWageLine(int pHours, double pAmount, double pYTDAmount) {
        return addHourlyWageLine(pHours, pAmount, pYTDAmount, payrollItemRepository.getHourlyItem());
    }

    public IHRLYWAGELINE addHourlyWageLine(int pHours, double pAmount, double pYTDAmount, IPITEM pHourlyWageItem) {
        IHRLYWAGELINE ihrlywageline = new IHRLYWAGELINE();
        ihrlywageline.setIHRS(Integer.toString(pHours));
        ihrlywageline.setIAMT(currencyFMT.format(pAmount));
        ihrlywageline.setIYTDAMT(currencyFMT.format(pYTDAmount));
        ihrlywageline.setICLASS("HRLY CLASS");
        ihrlywageline.setIITEM("HRLY ITEM");
        ihrlywageline.setIJOB("HRLY JOB");
        ihrlywageline.setIPITEMID(pHourlyWageItem.getIPITEMID());
        ihrlywageline.setIRATE("0.80%");
        ihrlywageline.setIWCCODE("HRLY WCCODE");

        paycheck.getIHRLYWAGELINE().add(ihrlywageline);
        return ihrlywageline;
    }

    public ITAXLINE addTaxLine(double pAmount, double pYTDAmount, PayrollItemRepository.Tax pTax) {
        return addTaxLine(pAmount, pYTDAmount, payrollItemRepository.getTaxItem(pTax));
    }

    public ITAXLINE addTaxLine(double pAmount, double pYTDAmount, IPITEM pTaxItem) {
        PayrollItem payrollItem = new PayrollItem(pTaxItem);

        ITAXLINE itaxline = new ITAXLINE();
        itaxline.setIAMT(payrollItem.getIsEmployeePaid() ? currencyFMT.format(pAmount * -1) : currencyFMT.format(
                pAmount));
        itaxline.setIYTDAMT(payrollItem.getIsEmployeePaid() ? currencyFMT.format(pYTDAmount * -1) : currencyFMT.format(
                pYTDAmount));
        itaxline.setIPITEMID(payrollItem.getSourceId());
        itaxline.setITAXABLEWAGE("$9.00");
        itaxline.setITIPSWB("$10.00");
        itaxline.setIWB("$11.00");

        paycheck.getITAXLINE().add(itaxline);
        return itaxline;
    }

    public IPAYCHK getPaycheck() {
        return paycheck;
    }
}