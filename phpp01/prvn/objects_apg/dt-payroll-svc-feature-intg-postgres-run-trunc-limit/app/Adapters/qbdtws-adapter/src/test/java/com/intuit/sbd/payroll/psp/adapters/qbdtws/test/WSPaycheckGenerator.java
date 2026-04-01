package com.intuit.sbd.payroll.psp.adapters.qbdtws.test;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * Allows easier creation of QBPaycheck and QBPaycheckLineItem.<br>
 *
 * Example usage:<br>
 * <pre>
 * PayrollItemRepository payrollItemRepository = new PayrollItemRepository(true);
 *
 * WSPaycheckGenerator paycheckGenerator = new WSPaycheckGenerator(payrollItemRepository);
 * paycheckGenerator().newPaycheck(emp, "05/02/2011", 102)
 *                    .addEarningLine(12, 120.50, 10200.48)
 *                    .addPreTaxLine(12, 100, 200);
 * request.getPaycheckList().getPaychecks().add(paycheckGenerator.getPaycheck());
 *
 * paycheckGenerator().newPaycheck(emp2, "05/02/2011", 102)
 *                    .addEarningLine(12, 120.50, 10200.48)
 *                    .addPreTaxLine(12, 100, 200);
 *
 * request.getPayrollItems().setPayrollItems(payrollItemRepository.getAllItems());
 * </pre>
 *
 * @author rnorian
 */
public class WSPaycheckGenerator {
    private NumberFormat currencyFMT = NumberFormat.getNumberInstance();

    private QBDTWSPayrollItemRepository payrollItemRepository;
    private QBPaycheck paycheck = new QBPaycheck();

    public WSPaycheckGenerator(QBDTWSPayrollItemRepository pPayrollItemRepository) {
        payrollItemRepository = pPayrollItemRepository;
    }

    public WSPaycheckGenerator newPaycheck(QBEmployee pQBEmployee, String pCheckDate, double pAmount) {
        QBPaycheck qbPaycheck = QBDTWSRequestCreator.createQBPaycheck(pQBEmployee);
        qbPaycheck.setOperation(QBPaycheckOperationEnum.ADD);
        qbPaycheck.setGrossPay(new BigDecimal(pAmount)); // calculate off of line items?
        qbPaycheck.setNetPay(new BigDecimal(pAmount));   // calculate as gross - deductions?

        SpcfCalendar payDate = SpcfCalendar.parse("MM/dd/yyyy", pCheckDate);
        SpcfCalendar payBeginDate = payDate.copy();
        payBeginDate.addDays(-14);

        qbPaycheck.setPayDate(QBDTWSRequestCreator.createQBDate(payDate));
        qbPaycheck.setPeriodStartDate(QBDTWSRequestCreator.createQBDate(payBeginDate));
        qbPaycheck.setPeriodEndDate(QBDTWSRequestCreator.createQBDate(payDate));

        int biWeeks = payDate.getDayOfYear() / 14;
        BigDecimal ytdGross = qbPaycheck.getGrossPay();
        ytdGross = ytdGross.multiply(new BigDecimal(biWeeks));
        BigDecimal ytdNet = qbPaycheck.getNetPay();
        ytdNet = ytdNet.multiply(new BigDecimal(biWeeks));

        qbPaycheck.setYTDGrossPay(ytdGross);
        qbPaycheck.setYTDNetPay(ytdNet);
        qbPaycheck.setCreatedTimeStamp(null);
        qbPaycheck.setModifiedTimeStamp(null);

        paycheck = qbPaycheck;

        return this;
    }

    //----- EarningItems => Compensation { OtherCompensation }
    public WSPaycheckGenerator addEarningLine(int pHours, double pAmount, double pYTDAmount) {
        return addEarningLine(pHours, pAmount, pYTDAmount, payrollItemRepository.getCompensationItem());
    }

    public WSPaycheckGenerator addEarningLine(int pHours, double pAmount, double pYTDAmount, QBPayrollItem pEarningItem) {
        payrollItemRepository.addItem(pEarningItem);
        QBEarningItem earningItem = QBDTWSRequestCreator.createQBEarningLine(pEarningItem.getID(), pAmount, pYTDAmount);
        paycheck.getEarningItems().add(earningItem);
        return this;
    }

    //----- AdjNetPayItems => Deductions { Tp401kRoth, Tp401kLoanPayment, OtherPostTaxDeduction }

    /**
     * Adds a loan payment line item into the paycheck's AdjNetPayItems collection.
     */
    public WSPaycheckGenerator addLoanPaymentLine(double pAmount, double pYTDAmount) {
        // find the loan payment payroll item: {agencyNumber = 401k, tax tracking type <> TAX_TRACKING_TYPE_ROTH("57")}
        QBPayrollItem loanPayrollItem = payrollItemRepository.getOtherPostTaxDeductionItem();
        // if one doesn't exist, add it
        return addAdjNetPayLine(pAmount, pYTDAmount, loanPayrollItem);
    }

    /**
     * Adds a roth payment line item into the paycheck's AdjNetPayItems collection.
     */
    public WSPaycheckGenerator addRothLine(double pAmount, double pYTDAmount) {
        // find the loan payment payroll item: {agencyNumber = 401k, tax tracking type == TAX_TRACKING_TYPE_ROTH("57")}
        QBPayrollItem rothPayrollItem = null;
        // if one doesn't exist, add it
        return addAdjNetPayLine(pAmount, pYTDAmount, rothPayrollItem);
    }


    public WSPaycheckGenerator addAdjNetPayLine(double pAmount, double pYTDAmount, QBPayrollItem pPostTaxDeductionPayrollItem) {
        payrollItemRepository.addItem(pPostTaxDeductionPayrollItem);
        QBPaycheckLineItem postTaxDeductionLineItem = QBDTWSRequestCreator.createQBAdjNetPayLine(pPostTaxDeductionPayrollItem.getID(), pAmount, pYTDAmount);
        paycheck.getAdjNetPayItems().add(postTaxDeductionLineItem);
        return this;
    }

    //----- PreTaxItems => Deductions { Tp401kEmployeeDeferral, OtherPreTaxDeduction }
    public WSPaycheckGenerator add401kEmployeeDeferralLine(double pAmount, double pYTDAmount) {
        return addPreTaxLine(pAmount, pYTDAmount, payrollItemRepository.get401kEmployeeDeferralDeductionItem());
    }

    public WSPaycheckGenerator add401kLoanPaymentLine(double pAmount, double pYTDAmount) {
        return addAdjNetPayLine(pAmount, pYTDAmount, payrollItemRepository.get401kLoanPaymentItem());
    }

    public WSPaycheckGenerator addPreTaxLine(double pAmount, double pYTDAmount) {
        return addPreTaxLine(pAmount, pYTDAmount, payrollItemRepository.getOtherPreTaxDeductionItem());
    }

    public WSPaycheckGenerator addPreTaxLine(double pAmount, double pYTDAmount, QBPayrollItem pPreTaxDeductionPayrollItem) {
        payrollItemRepository.addItem(pPreTaxDeductionPayrollItem);
        QBPaycheckLineItem preTaxDeductionLineItem = QBDTWSRequestCreator.createQBPreTaxLine(pPreTaxDeductionPayrollItem.getID(), pAmount, pYTDAmount);
        paycheck.getPreTaxItems().add(preTaxDeductionLineItem);
        return this;
    }

    //----- NonTaxCompanyItems => EmployerContributions { Tp401kSafeHarbor, Tp401kProfitSharing, NonTaxableEmployerContribution }
    public WSPaycheckGenerator addNonTaxCompanyLine(double pAmount, double pYTDAmount) {
        return addNonTaxCompanyLine(pAmount, pYTDAmount, payrollItemRepository.getOtherNonTaxableEmployerContributionItem());
    }

    public WSPaycheckGenerator addSafeHarborCompanyLine(double pAmount, double pYTDAmount) {
        return addNonTaxCompanyLine(pAmount, pYTDAmount, payrollItemRepository.get401kEmployerSafeHarborContributionItem());
    }

    public WSPaycheckGenerator addProfitSharingCompanyLine(double pAmount, double pYTDAmount) {
        return addNonTaxCompanyLine(pAmount, pYTDAmount, payrollItemRepository.get401kProfitSharingContributionItem());
    }

    public WSPaycheckGenerator addNonTaxCompanyLine(double pAmount, double pYTDAmount, QBPayrollItem pNonTaxCompanyPayrollItem) {
        payrollItemRepository.addItem(pNonTaxCompanyPayrollItem);
        QBPaycheckLineItem nonTaxLineItem = QBDTWSRequestCreator.createQBNonTaxCompanyLine(pNonTaxCompanyPayrollItem.getID(), pAmount, pYTDAmount);
        paycheck.getNonTaxCompanyItems().add(nonTaxLineItem);
        return this;
    }

    // TaxCompanyItems => EmployerContributions { TaxableEmployerContribution }
    public WSPaycheckGenerator addTaxCompanyLine(double pAmount, double pYTDAmount, QBPayrollItem pTaxCompanyPayrollItem) {
        //todo_rhn: fix tax company payroll item creation
        //QBPaycheckLineTaxItem taxLineItem = QBDTWSRequestCreator.createTaxCompanyPayrollItem();
        //setLineItemValues(taxLineItem, pAmount, pYTDAmount, pTaxCompanyPayrollItem);
        //paycheck.getTaxCompanyItems().add(taxLineItem);
        return this;
    }

    // TaxItems => Liabilities
    public WSPaycheckGenerator addTaxItem() {
        return this;
    }

    public QBPaycheck getPaycheck() {
        return paycheck;
    }

    private QBPaycheckLineItem setLineItemValues(QBPaycheckLineItem pLineItem, double pAmount, double pYTDAmount, QBPayrollItem pPayrollItem) {
        QBPaycheckLineItem lineItem = new QBPaycheckLineItem();
        lineItem.setPayrollItemId(pPayrollItem.getID());
        lineItem.setName(pPayrollItem.getName());
        lineItem.setCurrent(new BigDecimal(pAmount));
        lineItem.setYTD(new BigDecimal(pYTDAmount));
        return lineItem;
    }

}
