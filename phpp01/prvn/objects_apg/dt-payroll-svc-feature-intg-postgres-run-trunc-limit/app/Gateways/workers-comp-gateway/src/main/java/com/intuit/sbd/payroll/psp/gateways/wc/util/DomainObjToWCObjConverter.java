package com.intuit.sbd.payroll.psp.gateways.wc.util;

import com.intuit.bp.wc.common.schema.Address;
import com.intuit.bp.wc.common.schema.*;
import com.intuit.bp.wc.common.schema.Employee;
import com.intuit.bp.wc.common.schema.Paycheck;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.*;

/**
 * Author: Sriram Nutakki
 * Date created: 11/14/12
 */
public class DomainObjToWCObjConverter {

    final static String EMPTY_NAME = "<EMPTY>";
    public static Business convert(com.intuit.sbd.payroll.psp.domain.Company in) {
        Business out = null;
        if (in != null) {
            out = new Business();
            out.setCompanyId(in.getSourceCompanyId());
            out.setFein(in.getFedTaxId());
            out.setName(in.getLegalName());
            out.setAddress(convert(in.getLegalAddress()));
            out.setEmail(in.getNotificationEmail());
            out.setFax(null);
            out.setFeatureSetName(null);
            out.setPartner(null);
            if (in.getPayrollFrequency() != null) {
                out.setPayrollFrequency(
                        getPayFrequency(PayrollFrequencyCode.valueOf(in.getPayrollFrequency().getName())));
            }
            out.setPhone(in.getPhone());
            out.setProduct(null);
            out.setSicCode(null);
            out.setSource(in.getSourceSystemCd().name());
            out.setStatus(null);
            out.setStatusDate(null);
        }
        return out;
    }

    public static Address convert(com.intuit.sbd.payroll.psp.domain.Address in) {
        Address out = null;
        if(in != null) {
            out = new Address();
            out.setAddressLineOne(in.getAddressLine1());
            out.setAddressLineTwo(in.getAddressLine2());
            out.setCity(in.getCity());
            out.setZip(in.getZipCode());
            out.setState(in.getState());
        }
        return out;
    }

    public static Employee convert(com.intuit.sbd.payroll.psp.domain.Employee in) {
        Employee out = null;
        if(in != null) {
            out = new Employee();
            out.setEmployeeID(in.getSourceEmployeeId());

            out.setFirstName(EMPTY_NAME.equals(in.getFirstName())?null:in.getFirstName());
            out.setLastName(EMPTY_NAME.equals(in.getLastName())?null:in.getLastName());
            out.setMiddleInitial(in.getMiddleName());
            out.setWorkState(in.getWorkState());
            //out.setAnnualGrossSalary(null); //TODO: set correct value
            out.setDepartment(null); //TODO: set correct value
            //out.setIsExempt(null); //TODO: set correct value
            //out.setIsOfficer(null); //TODO: set correct value
            out.setJobDesc(null); //TODO: set correct value
        }
        return out;
    }

    public static Paycheck convert(com.intuit.sbd.payroll.psp.domain.Paycheck in) {
        Paycheck out = null;
        if (in != null) {
            double totalHours = 0;
            List<PaycheckItem> paycheckLineItems = new ArrayList<PaycheckItem>();
            if (in.getCompensationCollection() != null) {
                for (Compensation compensation : in.getCompensationCollection()) {
                    CompanyPayrollItem companyPayrollItem = compensation.getCompanyPayrollItem();
                    QbdtPayrollItemInfo qbdtPayrollItem = companyPayrollItem != null ? companyPayrollItem.getQbdtPayrollItemInfo() : null;
                    PaycheckItemType paycheckItemType = PaycheckItemType.PAY;
                    SpcfMoney amount = compensation.getCompensationAmount();
                    PaycheckItem item = createPaycheckItem(companyPayrollItem, qbdtPayrollItem, paycheckItemType, amount);
                    paycheckLineItems.add(item);
                    totalHours += compensation.getHoursWorked();
                }
            }
            if (in.getDeductionCollection() != null) {
                for (Deduction deduction : in.getDeductionCollection()) {
                    CompanyPayrollItem companyPayrollItem = deduction.getCompanyPayrollItem();
                    QbdtPayrollItemInfo qbdtPayrollItem = companyPayrollItem != null ? companyPayrollItem.getQbdtPayrollItemInfo() : null;
                    PaycheckItemType paycheckItemType = PaycheckItemType.DEDUCTION;
                    SpcfMoney amount = deduction.getDeductionAmount();
                    PaycheckItem item = createPaycheckItem(companyPayrollItem, qbdtPayrollItem, paycheckItemType, amount);
                    paycheckLineItems.add(item);
                }
            }
            out = createPaycheck(
                    in.getSourcePaycheckId(),
                    in.getPayrollRun() != null ? in.getPayrollRun().getPaycheckDate() : null,
                    in.getPayPeriodBeginDate(),
                    in.getPayPeriodEndDate(),
                    totalHours,
                    paycheckLineItems);
        }
        return out;
    }

    public static Paycheck convert(com.intuit.sbd.payroll.psp.domain.Company company, com.intuit.sbd.payroll.psp.domain.Paystub in) {
        Paycheck out = null;
        if (in != null) {
            double totalHours = 0;
            List<PaycheckItem> paycheckLineItems = new ArrayList<PaycheckItem>();
            Set<QbdtPayrollItemInfo> qbdtCompanyPayrollItems = QbdtPayrollItemInfo.findPayrollItemsByCompany(company);
            if (in.getPstubPayItemCollection() != null) {
                MAIN: for (PstubPayItem payItem : in.getPstubPayItemCollection()) {
                    PaycheckItemType paycheckItemType = null;
                    switch(payItem.getType()) {
                        case AdjNetPay: { paycheckItemType = PaycheckItemType.DEDUCTION; break;}
                        case Earnings: { paycheckItemType = PaycheckItemType.PAY; break;}
                        case NonTaxCompContri: { continue MAIN;}
                        case PreTaxDeduct: { paycheckItemType = PaycheckItemType.DEDUCTION; break;}
                        case Tax: { continue MAIN;}
                        case TaxCompContri: { paycheckItemType = PaycheckItemType.PAY; break;}
                        default: {continue MAIN;}
                    }
                    QbdtPayrollItemInfo qbdtPayrollItem = findQbdtPayrollItemInfo(payItem, qbdtCompanyPayrollItems);
                    CompanyPayrollItem companyPayrollItem = qbdtPayrollItem != null ?
                            qbdtPayrollItem.getCompanyPayrollItem() : null;
                    SpcfMoney amount = payItem.getCurAmt();
                    PaycheckItem item = null;
                    if (companyPayrollItem != null) {
                        item = createPaycheckItem(companyPayrollItem, qbdtPayrollItem, paycheckItemType, amount);
                    } else {
                        item = createPaycheckItem(payItem.getName(), payItem.getName(),
                                                  qbdtPayrollItem, paycheckItemType, amount);
                    }

                    paycheckLineItems.add(item);
                    try {
                        double totalMinutes = 0;
                        // This is in HH:MM format
                        if (payItem.getQtyTime() != null) {
                            String[] time = payItem.getQtyTime().split(":");
                            if (time != null && time.length > 0 && time[0] != null && time[0].trim().length() > 0) {
                                totalMinutes += Integer.valueOf(time[0]) * 60;
                            }
                            if (time != null && time.length > 1 && time[1] != null && time[1].trim().length() > 0) {
                                totalMinutes += Integer.valueOf(time[1]);
                            }
                        }
                        totalHours += totalMinutes / 60;
                    } catch (Exception ex) {
                        // Ignore
                    }
                }
            }
            out = createPaycheck(
                    in.getPaycheck().getSourcePaycheckId(),
                    in.getPaycheckDate(),
                    in.getPayBeginDate(),
                    in.getPayEndDate(),
                    totalHours,
                    paycheckLineItems);
        }
        return out;
    }

    private static PaycheckItem createPaycheckItem(CompanyPayrollItem companyPayrollItem,
                                                   QbdtPayrollItemInfo qbdtPayrollItem,
                                                   PaycheckItemType paycheckItemType,
                                                   SpcfMoney amount)
    {
        return createPaycheckItem(
                companyPayrollItem != null ? companyPayrollItem.getPayrollItem().getPayrollItemCode().name() : null,
                companyPayrollItem != null ? companyPayrollItem.getSourceDescription() : null,
                qbdtPayrollItem,
                paycheckItemType,
                amount);
    }

    private static PaycheckItem createPaycheckItem(String payOrDeductionCode,
                                                   String payCustomItemName,
                                                   QbdtPayrollItemInfo qbdtPayrollItem,
                                                   PaycheckItemType paycheckItemType,
                                                   SpcfMoney amount)
    {
        PaycheckItem paycheckItem = new PaycheckItem();
        paycheckItem.setType(paycheckItemType);
        if (paycheckItemType == PaycheckItemType.PAY) {
            if (amount != null) {
                paycheckItem.setPayAmount(SpcfUtils.convertToBigDecimal(amount).doubleValue());
            }
            paycheckItem.setPayCode(payOrDeductionCode);
            paycheckItem.setPayCustomItemName(payCustomItemName);
            if (qbdtPayrollItem != null) {
                QBDTPayrollItemCode payrollItemCode = QBDTPayrollItemCode.getItemByCode(qbdtPayrollItem.getDetailType());
                double overTime = qbdtPayrollItem.getOvertimeMultiplier();
                if (payrollItemCode == QBDTPayrollItemCode.OVERTIMEHOURLY  || overTime > 1){
                    paycheckItem.setOverTimeMultiplier(overTime);
                }

                if (payrollItemCode != null && payrollItemCode != QBDTPayrollItemCode.NONE){
                    String mappedPaymentType = payrollItemCode.name();
                    if (mappedPaymentType != null){
                        paycheckItem.setPayItemName(mappedPaymentType);
                    }
                }
            }
        }
        else if (paycheckItemType == PaycheckItemType.DEDUCTION) {
            if (amount != null) {
                paycheckItem.setDeductionAmount(SpcfUtils.convertToBigDecimal(amount).doubleValue());
            }
            paycheckItem.setDeductionCode(payCustomItemName);
            paycheckItem.setDeductionCustomItemName(payCustomItemName);
            if (qbdtPayrollItem != null) {
                QBDTPayrollItemCode payrollItemCode = QBDTPayrollItemCode.getItemByCode(qbdtPayrollItem.getDetailType());
                if (payrollItemCode != null && payrollItemCode != QBDTPayrollItemCode.NONE) {
                    String mappedPaymentType = payrollItemCode.name();
                    if (mappedPaymentType != null){
                        paycheckItem.setDeductionItemName(mappedPaymentType);
                    }
                }
            }
        }

        return paycheckItem;
    }

    private static PayrollFrequencyType getPayFrequency(PayrollFrequencyCode payrollFrequencyCode) {
        switch(payrollFrequencyCode) {
            case Annually: return PayrollFrequencyType.ANNUALLY;
            case BiWeekly: return PayrollFrequencyType.BI_WEEKLY;
            case Daily: return PayrollFrequencyType.DAILY;
            case Monthly: return PayrollFrequencyType.MONTHLY;
            case Quarterly: return PayrollFrequencyType.QUARTERLY;
            case SemiAnnually: return PayrollFrequencyType.SEMI_ANNUALLY;
            case SemiMonthly: return PayrollFrequencyType.SEMI_MONTHLY;
            case Weekly: return PayrollFrequencyType.WEEKLY;
            default : return PayrollFrequencyType.MONTHLY;
        }
    }

    private static Paycheck createPaycheck(String paycheckId,
                                    SpcfCalendar paycheckDate,
                                    SpcfCalendar payPeriodBeginDate,
                                    SpcfCalendar payPeriodEndDate,
                                    double totalHours,
                                    List<PaycheckItem> paycheckLineItems) {
        Paycheck out = new Paycheck();
        out.setPaycheckId(paycheckId);
        if (paycheckDate != null) {
            out.setCheckDate(WCUtil.toMMDDYYYY(paycheckDate));
        }
        if (payPeriodBeginDate != null) {
            out.setPeriodStartDate(WCUtil.toMMDDYYYY(payPeriodBeginDate));
        }
        if (payPeriodEndDate != null) {
            out.setPeriodEndDate(WCUtil.toMMDDYYYY(payPeriodEndDate));
        }
        out.setHours(totalHours);

        if (paycheckLineItems != null && paycheckLineItems.size() > 0) {
            // Group same pay item types
            Map<PaycheckPayLineItemKey, PaycheckItem> paycheckPayLineItemsByType = new HashMap<PaycheckPayLineItemKey, PaycheckItem>();
            Map<PaycheckDeductionLineItemKey, PaycheckItem> paycheckDeductionLineItemsByType = new HashMap<PaycheckDeductionLineItemKey, PaycheckItem>();
            for (PaycheckItem paycheckLineItem : paycheckLineItems) {
                if (paycheckLineItem.getType() == PaycheckItemType.PAY) {
                    PaycheckPayLineItemKey key = new PaycheckPayLineItemKey(paycheckLineItem);
                    PaycheckItem consolidatedPaycheckLineItem = paycheckPayLineItemsByType.get(key);
                    if (consolidatedPaycheckLineItem != null) {
                        consolidatedPaycheckLineItem.setPayAmount(consolidatedPaycheckLineItem.getPayAmount() + paycheckLineItem.getPayAmount());
                    } else {
                        paycheckPayLineItemsByType.put(key, paycheckLineItem);
                    }
                }
                else if (paycheckLineItem.getType() == PaycheckItemType.DEDUCTION) {
                    PaycheckDeductionLineItemKey key = new PaycheckDeductionLineItemKey(paycheckLineItem);
                    PaycheckItem consolidatedPaycheckLineItem = paycheckDeductionLineItemsByType.get(key);
                    if (consolidatedPaycheckLineItem != null) {
                        consolidatedPaycheckLineItem.setDeductionAmount(consolidatedPaycheckLineItem.getDeductionAmount() + paycheckLineItem.getDeductionAmount());
                    } else {
                        paycheckDeductionLineItemsByType.put(key, paycheckLineItem);
                    }
                }
            }

            out.setPaycheckItems(new ArrayOfPaycheckItem());
            out.getPaycheckItems().getItem().addAll(paycheckPayLineItemsByType.values());
            out.getPaycheckItems().getItem().addAll(paycheckDeductionLineItemsByType.values());
        }
        return out;
    }

    private static QbdtPayrollItemInfo findQbdtPayrollItemInfo(PstubPayItem payItem,
                                                               Set<QbdtPayrollItemInfo> qbdtCompanyPayrollItems) {

        // Match by list-id
        for (QbdtPayrollItemInfo qbdtPayrollItemInfo : qbdtCompanyPayrollItems) {
            if (payItem.getPayrollItemListId().equals(qbdtPayrollItemInfo.getListId())) {
                return qbdtPayrollItemInfo;
            }
        }

        // Match by Source-Description of Company-Payroll-Item
        for (QbdtPayrollItemInfo qbdtPayrollItemInfo : qbdtCompanyPayrollItems) {
            if (payItem.getName() != null &&
                    qbdtPayrollItemInfo.getCompanyPayrollItem() != null &&
                    qbdtPayrollItemInfo.getCompanyPayrollItem().getSourceDescription() != null &&
                    payItem.getName().trim().equals(
                            qbdtPayrollItemInfo.getCompanyPayrollItem().getSourceDescription().trim()))
            {
                QbdtPayrollItemInfo qbdtPayrollItem = qbdtPayrollItemInfo;
                if (qbdtPayrollItemInfo.getCompanyPayrollItem() != null &&
                        qbdtPayrollItemInfo.getCompanyPayrollItem().getLatestCompanyPayrollItem() != null &&
                        qbdtPayrollItemInfo.getCompanyPayrollItem().getLatestCompanyPayrollItem().getQbdtPayrollItemInfo() != null
                        )
                {
                    qbdtPayrollItem = qbdtPayrollItemInfo.getCompanyPayrollItem().getLatestCompanyPayrollItem().getQbdtPayrollItemInfo();
                }
                return qbdtPayrollItem;
            }
        }

        // Match by Source-Description of Company-Law
        for (QbdtPayrollItemInfo qbdtPayrollItemInfo : qbdtCompanyPayrollItems) {
            if (payItem.getName() != null &&
                    qbdtPayrollItemInfo.getCompanyLaw() != null &&
                    qbdtPayrollItemInfo.getCompanyLaw().getSourceDescription() != null &&
                    payItem.getName().trim().equals(
                            qbdtPayrollItemInfo.getCompanyLaw().getSourceDescription().trim()))
            {
                QbdtPayrollItemInfo qbdtPayrollItem = qbdtPayrollItemInfo;
                if (qbdtPayrollItemInfo.getCompanyLaw() != null &&
                        qbdtPayrollItemInfo.getCompanyLaw().getLatestCompanyLaw() != null &&
                        qbdtPayrollItemInfo.getCompanyLaw().getLatestCompanyLaw().getQbdtPayrollItemInfo() != null
                        )
                {
                    qbdtPayrollItem = qbdtPayrollItemInfo.getCompanyLaw().getLatestCompanyLaw().getQbdtPayrollItemInfo();
                }
                return qbdtPayrollItem;
            }
        }

        return null;
    }

    private static class PaycheckPayLineItemKey {

        private String payCode;
        private String payItemName;
        private String payCustomItemName;
        private Double overtimeMultiplier;

        public PaycheckPayLineItemKey(PaycheckItem item) {
            this.payCode = item.getPayCode();
            this.payItemName = item.getPayItemName();
            this.payCustomItemName = item.getPayCustomItemName();
            this.overtimeMultiplier = item.getOverTimeMultiplier();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PaycheckPayLineItemKey that = (PaycheckPayLineItemKey) o;

            if (overtimeMultiplier != null ? !overtimeMultiplier.equals(that.overtimeMultiplier) : that.overtimeMultiplier != null)
                return false;
            if (payCode != null ? !payCode.equals(that.payCode) : that.payCode != null) return false;
            if (payCustomItemName != null ? !payCustomItemName.equals(that.payCustomItemName) : that.payCustomItemName != null)
                return false;
            if (payItemName != null ? !payItemName.equals(that.payItemName) : that.payItemName != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = payCode != null ? payCode.hashCode() : 0;
            result = 31 * result + (payItemName != null ? payItemName.hashCode() : 0);
            result = 31 * result + (payCustomItemName != null ? payCustomItemName.hashCode() : 0);
            result = 31 * result + (overtimeMultiplier != null ? overtimeMultiplier.hashCode() : 0);
            return result;
        }


    }

    private static class PaycheckDeductionLineItemKey {

        private String deductionCode;
        private String deductionItemName;
        private String deductionCustomItemName;

        public PaycheckDeductionLineItemKey(PaycheckItem item) {
            this.deductionCode = item.getDeductionCode();
            this.deductionItemName = item.getDeductionItemName();
            this.deductionCustomItemName = item.getDeductionCustomItemName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            PaycheckDeductionLineItemKey that = (PaycheckDeductionLineItemKey) o;

            if (deductionCode != null ? !deductionCode.equals(that.deductionCode) : that.deductionCode != null)
                return false;
            if (deductionCustomItemName != null ? !deductionCustomItemName.equals(that.deductionCustomItemName) : that.deductionCustomItemName != null)
                return false;
            if (deductionItemName != null ? !deductionItemName.equals(that.deductionItemName) : that.deductionItemName != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = deductionCode != null ? deductionCode.hashCode() : 0;
            result = 31 * result + (deductionItemName != null ? deductionItemName.hashCode() : 0);
            result = 31 * result + (deductionCustomItemName != null ? deductionCustomItemName.hashCode() : 0);
            return result;
        }
    }
}
