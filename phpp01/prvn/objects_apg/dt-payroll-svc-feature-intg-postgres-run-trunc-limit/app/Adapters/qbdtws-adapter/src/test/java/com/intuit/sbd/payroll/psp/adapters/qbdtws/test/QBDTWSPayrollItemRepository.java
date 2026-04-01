package com.intuit.sbd.payroll.psp.adapters.qbdtws.test;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.QBDTWSSubmitPayrollRequestProcess;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.QBPayrollTranslator;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.QBPayrollItem;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.QBPayrollItemCategory;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.QBPayrollItems;
import com.intuit.sbd.payroll.psp.domain.PayrollItemCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: rnorian
 * Date: 6/20/11
 * Time: 10:37 AM
 */
public class QBDTWSPayrollItemRepository {
    HashMap<QBPayrollItemCategory, HashMap<PayrollItemCode, ArrayList<QBPayrollItem>>> payrollItems = new HashMap<QBPayrollItemCategory, HashMap<PayrollItemCode, ArrayList<QBPayrollItem>>>();
    HashMap<String,QBPayrollItem> payrollItemHashMap = new HashMap<String, QBPayrollItem>();

    public QBDTWSPayrollItemRepository() {
        this(true);
    }

    public QBDTWSPayrollItemRepository(boolean pUseDefaultPayrollItemList) {
        // initialize the map w/all categories
        for (QBPayrollItemCategory itemCategory : QBPayrollItemCategory.values()) {
            payrollItems.put(itemCategory, new HashMap<PayrollItemCode, ArrayList<QBPayrollItem>>());
        }

        if (pUseDefaultPayrollItemList) {
            //QBPayrollItems defaultPayrollItems = QBDTWSRequestCreator.createDefaultPayrollItems();
            //for (QBPayrollItem qbPayrollItem : defaultPayrollItems.getPayrollItem()) {
            //    addItem(qbPayrollItem);
            //}
            addCompensationItem();
            addSalaryItem();
            addHourlyItem();
            addCommissionItem();
            addBonusItem();
            addDirectDepositItem();
            addOtherPostTaxDeductionItem();
            addOtherTaxableEmployerContributionItem();
            addOtherNonTaxableEmployerContributionItem();
            addOtherAdditionPreTaxItem();
            add401kEmployeeDeferralDeductionItem();
            add401kEmployerSafeHarborContributionItem();
            add401kProfitSharingContributionItem();
            add401kLoanPaymentItem();
        }
    }

    public QBPayrollItem addItem(QBPayrollItem pPayrollItem) {
        PayrollItemCode defaultPayrollItemCode = QBPayrollTranslator.getDefaultPayrollItemCode(pPayrollItem.getPayrollItemCategory());
        return addItem(defaultPayrollItemCode, pPayrollItem);
    }

    private QBPayrollItem addItem(PayrollItemCode pPayrollItemCode, QBPayrollItem pPayrollItem) {
        if (payrollItemHashMap.containsKey(pPayrollItem.getPspPayrollItemId())) {
            return pPayrollItem;
        }

        HashMap<PayrollItemCode,ArrayList<QBPayrollItem>> categoryPayrollItems =
                payrollItems.get(pPayrollItem.getPayrollItemCategory());

        ArrayList<QBPayrollItem> items = categoryPayrollItems.get(pPayrollItemCode);
        if (items == null) {
            items = new ArrayList<QBPayrollItem>();
            categoryPayrollItems.put(pPayrollItemCode, items);
        }
        items.add(pPayrollItem);
        payrollItemHashMap.put(pPayrollItem.getPspPayrollItemId(), pPayrollItem);
        return pPayrollItem;
    }

    public static final String TAX_TRACKING_TYPE_ROTH = QBDTWSSubmitPayrollRequestProcess.TAX_TRACKING_TYPE_ROTH;

    //                                                                                      QB Value
    // ----------------------------------------------------------------------------------------------------
    //public static final String TAX_TRACKING_TYPE_ROTH                                     // 57
    private static final String TAX_TRACKING_TYPE_LOAN = "58";
    private static final String TAX_TRACKING_TYPE_OTHER_ADD_PRE_TAX = "100";                // > 0
    private static final String TAX_TRACKING_TYPE_OTHER_NON_TAXABLE_ER_CONTRIB = "0";       // 0
    private static final String TAX_TRACKING_TYPE_OTHER_TAXABLE_ER_CONTRIB = "101";         // > 0
    private static final String TAX_TRACKING_TYPE_DIRECT_DEPOSIT = "102";                   // ignored
    private static final String TAX_TRACKING_TYPE_OTHER_POST_TAX_DEDUCTION = "0";           // 0
    private static final String TAX_TRACKING_TYPE_OTHER_PRE_TAX_DEDUCTION = "0";            // > 0


    public QBPayrollItem addCompensationItem() {
        QBPayrollItem compensationItem = QBDTWSRequestCreator.createEarningPayrollItem();
        compensationItem.setPayrollItemTypeId("0");
        return addItem(PayrollItemCode.Compensation, compensationItem);
    }

    public QBPayrollItem getCompensationItem() {
        return getItem(PayrollItemCode.Compensation);
    }

    public QBPayrollItem getCompensationItem(String pItemName) {
        return getItem(PayrollItemCode.Compensation, pItemName);
    }


    public QBPayrollItem addSalaryItem() {
        QBPayrollItem salaryItem = QBDTWSRequestCreator.createEarningPayrollItem();
        salaryItem.setPayrollItemTypeId("0");
        return addItem(PayrollItemCode.Salary, salaryItem);
    }

    public QBPayrollItem getSalaryItem() {
        return getItem(PayrollItemCode.Salary);
    }

    public QBPayrollItem getSalaryItem(String pItemName) {
        return getItem(PayrollItemCode.Salary, pItemName);
    }

    public QBPayrollItem addHourlyItem() {
        QBPayrollItem hourlyItem = QBDTWSRequestCreator.createEarningPayrollItem();
        hourlyItem.setPayrollItemTypeId("1");
        return addItem(PayrollItemCode.Hourly, hourlyItem);
    }

    public QBPayrollItem getHourlyItem() {
        return getItem(PayrollItemCode.Hourly);
    }

    public QBPayrollItem getHourlyItem(String pName) {
        return getItem(PayrollItemCode.Hourly, pName);
    }

    public QBPayrollItem addCommissionItem() {
        QBPayrollItem commissionItem = QBDTWSRequestCreator.createEarningPayrollItem();
        commissionItem.setPayrollItemTypeId("2");
        return addItem(PayrollItemCode.Commission, commissionItem);
    }

    public QBPayrollItem getCommissionItem() {
        return getItem(PayrollItemCode.Commission);
    }

    public QBPayrollItem getCommissionItem(String pItemName) {
        return getItem(PayrollItemCode.Commission, pItemName);
    }

    public QBPayrollItem addBonusItem() {
        QBPayrollItem bonusItem = QBDTWSRequestCreator.createEarningPayrollItem();
        bonusItem.setPayrollItemTypeId("12");
        return addItem(PayrollItemCode.Bonus, bonusItem);
    }

    public QBPayrollItem getBonusItem() {
        return getItem(PayrollItemCode.Bonus);
    }

    public QBPayrollItem getBonusItem(String pName) {
        return getItem(PayrollItemCode.Bonus, pName);
    }

    public QBPayrollItem addOtherAdditionPreTaxItem() {
        QBPayrollItem bonusItem = QBDTWSRequestCreator.createEarningPayrollItem();
        bonusItem.setPayrollItemTypeId("3");
        bonusItem.setTaxTrackingTypeId(TAX_TRACKING_TYPE_OTHER_ADD_PRE_TAX);
        return addItem(PayrollItemCode.OtherAdditionPreTax, bonusItem);
    }

    public QBPayrollItem getOtherAdditionPreTaxItem() {
        return getItem(PayrollItemCode.OtherAdditionPreTax);
    }

    public QBPayrollItem getOtherAdditionPreTaxItem(String pName) {
        return getItem(PayrollItemCode.OtherAdditionPreTax, pName);
    }

    public QBPayrollItem addOtherNonTaxableEmployerContributionItem() {
        QBPayrollItem nonTaxERContribItem = QBDTWSRequestCreator.createEarningPayrollItem();
        nonTaxERContribItem.setPayrollItemTypeId("5");
        nonTaxERContribItem.setTaxTrackingTypeId(TAX_TRACKING_TYPE_OTHER_NON_TAXABLE_ER_CONTRIB);
        return addItem(PayrollItemCode.OtherNonTaxableEmployerContribution, nonTaxERContribItem);
    }

    public QBPayrollItem getOtherNonTaxableEmployerContributionItem() {
        return getItem(PayrollItemCode.OtherNonTaxableEmployerContribution);
    }

    public QBPayrollItem getOtherNonTaxableEmployerContributionItem(String pName) {
        return getItem(PayrollItemCode.OtherNonTaxableEmployerContribution, pName);
    }

    public QBPayrollItem add401kEmployerSafeHarborContributionItem() {
        QBPayrollItem nonTaxERContribItem = QBDTWSRequestCreator.createEarningPayrollItem();
        nonTaxERContribItem.setPayrollItemTypeId("5");
        nonTaxERContribItem.setTaxTrackingTypeId(TAX_TRACKING_TYPE_OTHER_NON_TAXABLE_ER_CONTRIB);
        return addItem(PayrollItemCode.Tp401kSafeHarbor, nonTaxERContribItem);
    }

    public QBPayrollItem get401kEmployerSafeHarborContributionItem() {
        return getItem(PayrollItemCode.Tp401kSafeHarbor);
    }

    public QBPayrollItem get401kEmployerSafeHarborContributionItem(String pName) {
        return getItem(PayrollItemCode.Tp401kSafeHarbor, pName);
    }

    public QBPayrollItem add401kProfitSharingContributionItem() {
        QBPayrollItem nonTaxERContribItem = QBDTWSRequestCreator.createEarningPayrollItem();
        nonTaxERContribItem.setPayrollItemTypeId("5");
        nonTaxERContribItem.setTaxTrackingTypeId(TAX_TRACKING_TYPE_OTHER_NON_TAXABLE_ER_CONTRIB);
        return addItem(PayrollItemCode.Tp401kProfitSharing, nonTaxERContribItem);
    }

    public QBPayrollItem add401kLoanPaymentItem() {
        QBPayrollItem nonTaxERContribItem = QBDTWSRequestCreator.createAdjNetPayPayrollItem();
        nonTaxERContribItem.setAgencyNumber("401k");
        nonTaxERContribItem.setPayrollItemTypeId("6");
        nonTaxERContribItem.setOfxPayrollId("6");
        nonTaxERContribItem.setTaxTrackingTypeId(TAX_TRACKING_TYPE_OTHER_NON_TAXABLE_ER_CONTRIB);
        return addItem(PayrollItemCode.Tp401kLoanPayment, nonTaxERContribItem);
    }

    public QBPayrollItem get401kProfitSharingContributionItem() {
        return getItem(PayrollItemCode.Tp401kProfitSharing);
    }

    public QBPayrollItem get401kProfitSharingContributionItem(String pName) {
        return getItem(PayrollItemCode.Tp401kProfitSharing, pName);
    }

    public QBPayrollItem addOtherTaxableEmployerContributionItem() {
        QBPayrollItem taxableERContribItem = QBDTWSRequestCreator.createEarningPayrollItem();
        taxableERContribItem.setPayrollItemTypeId("5");
        taxableERContribItem.setTaxTrackingTypeId(TAX_TRACKING_TYPE_OTHER_TAXABLE_ER_CONTRIB);
        return addItem(PayrollItemCode.OtherTaxableEmployerContribution, taxableERContribItem);
    }

    public QBPayrollItem getOtherTaxableEmployerContributionItem() {
        return getItem(PayrollItemCode.OtherTaxableEmployerContribution);
    }

    public QBPayrollItem getOtherTaxableEmployerContributionItem(String pName) {
        return getItem(PayrollItemCode.OtherTaxableEmployerContribution, pName);
    }

    public QBPayrollItem addDirectDepositItem() {
        QBPayrollItem directDepositItem = QBDTWSRequestCreator.createDirectDepositPayrollItem();
        directDepositItem.setPayrollItemTypeId("11");
        directDepositItem.setTaxTrackingTypeId(TAX_TRACKING_TYPE_DIRECT_DEPOSIT);
        return addItem(PayrollItemCode.DirectDeposit, directDepositItem);
    }

    public QBPayrollItem getDirectDepositItem() {
        return getItem(PayrollItemCode.DirectDeposit);
    }

    public QBPayrollItem getDirectDepositItem(String pName) {
        return getItem(PayrollItemCode.DirectDeposit, pName);
    }

    public QBPayrollItem addOtherPostTaxDeductionItem() {
        QBPayrollItem otherPostTaxDeductionItem = QBDTWSRequestCreator.createTaxCompanyPayrollItem();
        otherPostTaxDeductionItem.setPayrollItemTypeId("4");
        otherPostTaxDeductionItem.setTaxTrackingTypeId(TAX_TRACKING_TYPE_OTHER_POST_TAX_DEDUCTION);
        return addItem(PayrollItemCode.OtherPostTaxDeduction, otherPostTaxDeductionItem);
    }

    public QBPayrollItem getOtherPostTaxDeductionItem() {
        return getItem(PayrollItemCode.OtherPostTaxDeduction);
    }

    public QBPayrollItem getOtherPostTaxDeductionItem(String pName) {
        return getItem(PayrollItemCode.OtherPostTaxDeduction, pName);
    }

    public void addTp401kLoanPaymentItem() {
        QBPayrollItem payrollItem = QBDTWSRequestCreator.createAdjNetPayPayrollItem();
        payrollItem.setAgencyNumber("401k");
        payrollItem.setTaxTrackingTypeId(TAX_TRACKING_TYPE_LOAN);
        addItem(PayrollItemCode.Tp401kLoanPayment, payrollItem);
    }

    public QBPayrollItem getTp401kLoanPaymentItem() {
        List<QBPayrollItem> items = getItems(PayrollItemCode.Tp401kLoanPayment);
        for (QBPayrollItem item : items) {
            if (item.getAgencyNumber() == "401k" && item
                    .getTaxTrackingTypeId() != QBDTWSSubmitPayrollRequestProcess.TAX_TRACKING_TYPE_ROTH) {
                return item;
            }
        }
        return null;
    }

    public QBPayrollItem addOtherPreTaxDeductionItem() {
        QBPayrollItem otherPreTaxDeductionItem = QBDTWSRequestCreator.createPreTaxPayrollItem();
        otherPreTaxDeductionItem.setPayrollItemTypeId("4");
        otherPreTaxDeductionItem.setTaxTrackingTypeId(TAX_TRACKING_TYPE_OTHER_PRE_TAX_DEDUCTION);
        return addItem(PayrollItemCode.OtherPreTaxDeduction, otherPreTaxDeductionItem);
    }

    public QBPayrollItem getOtherPreTaxDeductionItem() {
        return getItem(PayrollItemCode.OtherPreTaxDeduction);
    }

    public QBPayrollItem getOtherPreTaxDeductionItem(String pName) {
        return getItem(PayrollItemCode.OtherPreTaxDeduction, pName);
    }

    public QBPayrollItem add401kEmployeeDeferralDeductionItem() {
        QBPayrollItem employeeDeferralDeductionItem = QBDTWSRequestCreator.createEEDeferralPreTaxPayrollItem();
        employeeDeferralDeductionItem.setPayrollItemTypeId("4");
        employeeDeferralDeductionItem.setTaxTrackingTypeId(QBDTWSSubmitPayrollRequestProcess.TAX_TRACKING_TYPE_401K);
        return addItem(PayrollItemCode.Tp401kEmployeeDeferral, employeeDeferralDeductionItem);
    }

    public QBPayrollItem get401kEmployeeDeferralDeductionItem() {
        return getItem(PayrollItemCode.Tp401kEmployeeDeferral);
    }

    public QBPayrollItem get401kEmployeeDeferralDeductionItem(String pName) {
        return getItem(PayrollItemCode.Tp401kEmployeeDeferral, pName);
    }

    public QBPayrollItem get401kLoanPaymentItem() {
        return getItem(PayrollItemCode.Tp401kLoanPayment);
    }

    private QBPayrollItem getItem(PayrollItemCode pItemCode) {
        List<QBPayrollItem> items = getItems(pItemCode);

        QBPayrollItem item = null;
        if (items != null && items.size() > 0) {
            item = items.get(0);
        }
        return item;
    }

    public List<QBPayrollItem> getItems(QBPayrollItemCategory pItemCategory) {
        ArrayList<QBPayrollItem> items = new ArrayList<QBPayrollItem>();
        for (ArrayList<QBPayrollItem> itemCodePayrollItems : payrollItems.get(pItemCategory).values()) {
            for (QBPayrollItem itemCodePayrollItem : itemCodePayrollItems) {
                items.add(itemCodePayrollItem);
            }
        }
        return items;
    }

    public List<QBPayrollItem> getItems(PayrollItemCode pItemCode) {
        QBPayrollItemCategory payrollItemCategory = QBPayrollTranslator.getPayrollItemCategory(pItemCode);
        return payrollItems.get(payrollItemCategory).get(pItemCode);
    }

    public QBPayrollItem getItem(PayrollItemCode pItemType, String pItemName) {
        return getByName(getItems(pItemType), pItemName);
    }

    private QBPayrollItem getByName(List<QBPayrollItem> pPayrollItems, String pItemName) {
        QBPayrollItem payrollItem = null;
        if (pPayrollItems != null && pPayrollItems.size() > 0) {
            for (QBPayrollItem pitem : pPayrollItems) {
                if (pitem.getName().equalsIgnoreCase(pItemName)) {
                    payrollItem = pitem;
                    break;
                }
            }
        }
        return payrollItem;
    }


    public QBPayrollItems getAllPayrollItems() {
        QBPayrollItems qbPayrollItems = new QBPayrollItems();

        for (QBPayrollItemCategory itemCategory : payrollItems.keySet()) {
            qbPayrollItems.getPayrollItem().addAll(getItems(itemCategory));
        }

        return qbPayrollItems;
    }
}
