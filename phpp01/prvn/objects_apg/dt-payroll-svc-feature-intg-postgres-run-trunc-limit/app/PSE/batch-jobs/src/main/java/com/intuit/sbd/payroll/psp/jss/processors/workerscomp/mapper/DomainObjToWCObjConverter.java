package com.intuit.sbd.payroll.psp.jss.processors.workerscomp.mapper;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.model.PayrollDTO;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.schema.*;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.sbd.payroll.psp.gateways.wc.util.QBDTPayrollItemCode;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.FlushMode;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

//cloned code from workers comp gateway
//going forward same class we will cleanup from workers comp gateway
public class DomainObjToWCObjConverter {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(DomainObjToWCObjConverter.class);
    final static String EMPTY_NAME = "<EMPTY>";
    private static final String DATE_FORMAT_MM_DD_YYYY = "MM/dd/yyyy";
    private static DomainEntitySet<WcCompany> wcCompanyEntity = null;
    public static Payroll.Businesses.Business convert(Company in) {
        Payroll.Businesses.Business out = null;
        if (in != null) {
            out = new ObjectFactory().createPayrollBusinessesBusiness();
            wcCompanyEntity= Application.find(WcCompany.class,WcCompany.Company().equalTo(in));
            if ( CollectionUtils.isEmpty(wcCompanyEntity) || StringUtils.isEmpty(((WcCompany)wcCompanyEntity.get(0)).getIntegrationId())) {
                throw new RuntimeException("WcCompany null or Integration id is missing for companyId "+in.getSourceCompanyId());
            }
            out.setCompanyId(((WcCompany)wcCompanyEntity.get(0)).getIntegrationId());
            out.setFein(in.getFedTaxId());
            out.setName(in.getLegalName());
            out.setAddress(convert(in.getLegalAddress()));
            out.setEmail(in.getNotificationEmail());
            out.setFax(null);
            out.setFeatureSetName(null);
            out.setPartner(null);
            if (in.getPayrollFrequency() != null) {
                out.setPayrollFrequency(getPayFrequency(PayrollFrequencyCode.valueOf(in.getPayrollFrequency().getName())));
            }
            out.setPhone(in.getPhone());
            out.setProduct(null);
            out.setSICCode(null);
            out.setSource(in.getSourceSystemCd().name());
            out.setStatus(null);
            out.setStatusDate(null);
        }
        return out;
    }

    public static Payroll.Businesses.Business.Address convert(Address in) {
        Payroll.Businesses.Business.Address out = null;
        if(in != null) {
            out = new ObjectFactory().createPayrollBusinessesBusinessAddress();
            out.setAddressLineOne(in.getAddressLine1());
            out.setAddressLineTwo(in.getAddressLine2());
            out.setCity(in.getCity());
            out.setZip(in.getZipCode());
            out.setState(in.getState());
        }
        return out;
    }

    public static Payroll.Businesses.Business.Employees.Employee convert(Employee
                                                                                                                              in) {
        Payroll.Businesses.Business.Employees.Employee out = null;
        if(in != null) {
            out = new ObjectFactory().createPayrollBusinessesBusinessEmployeesEmployee();

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

    public static Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck convert(Paycheck in) {
        Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck out = null;
        if (in != null) {
            double totalHours = 0;
            List<Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem> paycheckLineItems = new ArrayList<Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem>();
            if (in.getCompensationCollection() != null) {
                for (Compensation compensation : in.getCompensationCollection()) {
                    CompanyPayrollItem companyPayrollItem = compensation.getCompanyPayrollItem();
                    QbdtPayrollItemInfo qbdtPayrollItem = companyPayrollItem != null ? companyPayrollItem.getQbdtPayrollItemInfo() : null;
                    String paycheckItemType = "PAY";
                    SpcfMoney amount = compensation.getCompensationAmount();
                    Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem item = createPaycheckItem(companyPayrollItem, qbdtPayrollItem, paycheckItemType, amount);
                    paycheckLineItems.add(item);
                    totalHours += compensation.getHoursWorked();
                }
            }
            if (in.getDeductionCollection() != null) {
                for (Deduction deduction : in.getDeductionCollection()) {
                    CompanyPayrollItem companyPayrollItem = deduction.getCompanyPayrollItem();
                    QbdtPayrollItemInfo qbdtPayrollItem = companyPayrollItem != null ? companyPayrollItem.getQbdtPayrollItemInfo() : null;
                    String paycheckItemType = "DEDUCTION";
                    SpcfMoney amount = deduction.getDeductionAmount();
                    Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem item = createPaycheckItem(companyPayrollItem, qbdtPayrollItem, paycheckItemType, amount);
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

    public static Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck convert(Company company, Paystub in) {
        Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck out = null;
        if (in != null) {
            double totalHours = 0;
            List<Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem> paycheckLineItems = new ArrayList<Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem>();
            Set<QbdtPayrollItemInfo> qbdtCompanyPayrollItems = QbdtPayrollItemInfo.findPayrollItemsByCompany(company);
            if (in.getPstubPayItemCollection() != null) {
                MAIN: for (PstubPayItem payItem : in.getPstubPayItemCollection()) {
                    String paycheckItemType = null;
                    switch(payItem.getType()) {
                        case AdjNetPay: { paycheckItemType = "DEDUCTION"; break;}
                        case Earnings: { paycheckItemType = "PAY"; break;}
                        case NonTaxCompContri: { continue MAIN;}
                        case PreTaxDeduct: { paycheckItemType = "DEDUCTION"; break;}
                        case Tax: { continue MAIN;}
                        case TaxCompContri: { paycheckItemType = "PAY"; break;}
                        default: {continue MAIN;}
                    }
                    QbdtPayrollItemInfo qbdtPayrollItem = findQbdtPayrollItemInfo(payItem, qbdtCompanyPayrollItems);
                    CompanyPayrollItem companyPayrollItem = qbdtPayrollItem != null ?
                            qbdtPayrollItem.getCompanyPayrollItem() : null;
                    SpcfMoney amount = payItem.getCurAmt();
                    Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem item = null;
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

    private static Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem createPaycheckItem(CompanyPayrollItem companyPayrollItem,
                                                   QbdtPayrollItemInfo qbdtPayrollItem,
                                                   String paycheckItemType,
                                                   SpcfMoney amount)
    {
        return createPaycheckItem(
                companyPayrollItem != null ? companyPayrollItem.getPayrollItem().getPayrollItemCode().name() : null,
                companyPayrollItem != null ? companyPayrollItem.getSourceDescription() : null,
                qbdtPayrollItem,
                paycheckItemType,
                amount);
    }

    private static Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem createPaycheckItem(String payOrDeductionCode,
                                                   String payCustomItemName,
                                                   QbdtPayrollItemInfo qbdtPayrollItem,
                                                   String paycheckItemType,
                                                   SpcfMoney amount)
    {
        Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem paycheckItem = new ObjectFactory().createPayrollBusinessesBusinessEmployeesEmployeePaychecksPaycheckPaycheckItemsPaycheckItem();


            paycheckItem.setType(paycheckItemType);
        if (paycheckItemType == "PAY") {
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
        else if (paycheckItemType == "DEDUCTION") {
            if (amount != null) {
                paycheckItem.setDeductionAmount(SpcfUtils.convertToBigDecimal(amount).floatValue());
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

    public static String getPayFrequency(PayrollFrequencyCode payrollFrequencyCode) {
        switch(payrollFrequencyCode) {
            case Annually: return "ANNUALLY";
            case BiWeekly: return "BI_WEEKLY";
            case Daily: return "DAILY";
            case Monthly: return "MONTHLY";
            case Quarterly: return "QUARTERLY";
            case SemiAnnually: return "SEMI_ANNUALLY";
            case SemiMonthly: return "SEMI_MONTHLY";
            case Weekly: return "WEEKLY";
            default : return "MONTHLY";
        }
    }

    private static Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck createPaycheck(String paycheckId,
                                    SpcfCalendar paycheckDate,
                                    SpcfCalendar payPeriodBeginDate,
                                    SpcfCalendar payPeriodEndDate,
                                    double totalHours,
                                    List<Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem> paycheckLineItems) {
        Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck out = new ObjectFactory().createPayrollBusinessesBusinessEmployeesEmployeePaychecksPaycheck();

            out.setPaycheckId(paycheckId);
        if (paycheckDate != null) {
            out.setCheckDate(toMMDDYYYY(paycheckDate));
        }
        if (payPeriodBeginDate != null) {
            out.setPeriodStartDate(toMMDDYYYY(payPeriodBeginDate));
        }
        if (payPeriodEndDate != null) {
            out.setPeriodEndDate(toMMDDYYYY(payPeriodEndDate));
        }
        out.setHours(totalHours);

        if (paycheckLineItems != null && paycheckLineItems.size() > 0) {
            // Group same pay item types
            Map<PaycheckPayLineItemKey, Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem> paycheckPayLineItemsByType = new HashMap<PaycheckPayLineItemKey, Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem>();
            Map<PaycheckDeductionLineItemKey, Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem> paycheckDeductionLineItemsByType = new HashMap<PaycheckDeductionLineItemKey, Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem>();
            for (Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem paycheckLineItem : paycheckLineItems) {
                if (paycheckLineItem.getType().equals("PAY")) {
                    PaycheckPayLineItemKey key = new PaycheckPayLineItemKey(paycheckLineItem);
                    Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem consolidatedPaycheckLineItem = paycheckPayLineItemsByType.get(key);
                    if (consolidatedPaycheckLineItem != null) {
                        consolidatedPaycheckLineItem.setPayAmount(consolidatedPaycheckLineItem.getPayAmount() + paycheckLineItem.getPayAmount());
                    } else {
                        paycheckPayLineItemsByType.put(key, paycheckLineItem);
                    }
                }
                else if (paycheckLineItem.getType().equals("DEDUCTION")) {
                    PaycheckDeductionLineItemKey key = new PaycheckDeductionLineItemKey(paycheckLineItem);
                    Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem consolidatedPaycheckLineItem = paycheckDeductionLineItemsByType.get(key);
                    if (consolidatedPaycheckLineItem != null) {
                        consolidatedPaycheckLineItem.setDeductionAmount(consolidatedPaycheckLineItem.getDeductionAmount() + paycheckLineItem.getDeductionAmount());
                    } else {
                        paycheckDeductionLineItemsByType.put(key, paycheckLineItem);
                    }
                }
            }

            out.setPaycheckItems(new ObjectFactory().createPayrollBusinessesBusinessEmployeesEmployeePaychecksPaycheckPaycheckItems());
            out.getPaycheckItems().getPaycheckItem().addAll(paycheckPayLineItemsByType.values());
            //  out.setPaycheckItems(paycheckPayLineItemsByType.values());
            out.getPaycheckItems().getPaycheckItem().addAll(paycheckDeductionLineItemsByType.values());
        }
        return out;
    }

    public static String toMMDDYYYY(SpcfCalendar spcfCal) {
        String result = null;
        if (spcfCal != null) {
            GregorianCalendar c = new GregorianCalendar();
            c.clear();
            c.set(Calendar.YEAR, spcfCal.getYear());
            c.set(Calendar.MONTH, spcfCal.getMonth() - 1);
            c.set(Calendar.DAY_OF_MONTH, spcfCal.getDay());
            SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_MM_DD_YYYY);
            result = formatter.format(c.getTime());
        }
        return result;
    }

    public static QbdtPayrollItemInfo findQbdtPayrollItemInfo(PstubPayItem payItem,
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

        public PaycheckPayLineItemKey(Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem item) {
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

        public PaycheckDeductionLineItemKey(Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck.PaycheckItems.PaycheckItem item) {
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
    public static PayrollDTO<com.intuit.sbd.payroll.psp.jss.processors.workerscomp.schema.Payroll> createDomainObjectToPayrollDto(List<WorkersCompPaycheckPendingState> pendingPaychecks)
    {
        Map<com.intuit.sbd.payroll.psp.domain.Company, Set<com.intuit.sbd.payroll.psp.domain.Employee>> companyEmployees =new HashMap<com.intuit.sbd.payroll.psp.domain.Company, Set<com.intuit.sbd.payroll.psp.domain.Employee>>();
        Map<com.intuit.sbd.payroll.psp.domain.Employee, Set<WorkersCompPaycheckPendingState>> employeePendingPaychecks=new HashMap<com.intuit.sbd.payroll.psp.domain.Employee, Set<WorkersCompPaycheckPendingState>>();
        fillDomainObjectToEmployeePaycheckMap(pendingPaychecks, companyEmployees, employeePendingPaychecks);
        Payroll payroll = new Payroll();
        PayrollDTO dto = new PayrollDTO(payroll);

        for (Company company : companyEmployees.keySet()) {
            try {
                PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(company);
                Payroll.Businesses.Business business = DomainObjToWCObjConverter.convert(company);
                Set<Employee> employees = companyEmployees.get(company);
                for (Employee employee : employees) {
                    Payroll.Businesses.Business.Employees.Employee wcEmployee = DomainObjToWCObjConverter.convert(employee);
                    Set<WorkersCompPaycheckPendingState> pendingChecks = employeePendingPaychecks.get(employee);
                    for (WorkersCompPaycheckPendingState pendingCheck : pendingChecks) {
                        dto.addIncludedPaycheck(company.getSourceCompanyId(), pendingCheck.getWorkersCompPaycheck());
                        Paycheck paycheck = pendingCheck.getWorkersCompPaycheck().getPaycheck();
                        Paystub paystub = loadPaystub(paycheck);

                        Payroll.Businesses.Business.Employees.Employee.Paychecks.Paycheck wcPaycheck = null;
                        if (paystub != null) {
                            wcPaycheck = DomainObjToWCObjConverter.convert(company, paystub);
                        } else {
                            wcPaycheck = DomainObjToWCObjConverter.convert(paycheck);
                        }

                        switch (pendingCheck.getStateCd()) {
                            case PendingNew:
                                wcPaycheck.setCheckStatus("NEW");
                                break;
                            case PendingDelete:
                                wcPaycheck.setCheckStatus("DELETE");
                                break;
                            case PendingEdit:
                                wcPaycheck.setCheckStatus("UPDATE");
                                break;
                            default:
                                throw new RuntimeException(
                                        "Invalid code for pending check status: " + pendingCheck.getStateCd());
                        }


                        // Set version
                     /*   Long version = pendingCheck.getWorkersCompPaycheck().getPaycheckVersion();
                        wcPaycheck.setPaycheckVersion(version.intValue());*/

                        if (wcEmployee.getPaychecks() == null) {
                            wcEmployee.setPaychecks(new ObjectFactory().createPayrollBusinessesBusinessEmployeesEmployeePaychecks());
                        }

                        wcEmployee.getPaychecks().getPaycheck().add(wcPaycheck);

                        logger.info("WC Gateway :: Company=" + company.getSourceCompanyId() +
                                ", Employee=" + wcEmployee.getEmployeeID() + ", PayCheck_ID=" +
                                wcPaycheck.getPaycheckId() + ", Status=" + wcPaycheck.getCheckStatus());
                    }
                    if (business.getEmployees() == null) {
                        business.setEmployees(new ObjectFactory().createPayrollBusinessesBusinessEmployees());
                    }
                    business.getEmployees().getEmployee().add(wcEmployee);
                }
                if (payroll.getBusinesses() == null) {
                    payroll.setBusinesses(new Payroll.Businesses());
                }
                payroll.getBusinesses().getBusiness().add(business);
            } finally {
                PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
            }
        }
        return dto;
    }
    public static Paystub loadPaystub(Paycheck paycheck) {

        Paystub paystub = null;

        Expression<Paystub> query =
                new Query<Paystub>()
                        .Where(Paystub.Paycheck().equalTo(paycheck));

        ((Query<Paystub>)query).EagerLoad(
                Paystub.Paycheck(),
                Paystub.PstubPayItemSet()
        );

        List<Paystub> paystubs = Application.executeQuery(Paystub.class, query);
        if (paystubs != null && paystubs.size() > 0) {
            paystub = paystubs.get(0);
        }

        return paystub;
    }
    public static void fillDomainObjectToEmployeePaycheckMap(List<WorkersCompPaycheckPendingState> pendingPaychecks,Map<com.intuit.sbd.payroll.psp.domain.Company, Set<com.intuit.sbd.payroll.psp.domain.Employee>> companyEmployees,
    Map<com.intuit.sbd.payroll.psp.domain.Employee, Set<WorkersCompPaycheckPendingState>> employeePendingPaychecks)
    {
        for (WorkersCompPaycheckPendingState pendingCheck : pendingPaychecks) {
            WorkersCompPaycheck wcPaycheck = pendingCheck.getWorkersCompPaycheck();
            Paycheck paycheck = wcPaycheck.getPaycheck();
            Employee employee = paycheck.getSourceEmployee();
            Company company = paycheck.getCompany();

            Set<Employee> employees = companyEmployees.get(company);
            if (employees == null) {
                employees = new HashSet<Employee>();
                companyEmployees.put(company, employees);
            }
            employees.add(employee);

            Set<WorkersCompPaycheckPendingState> pendingChecks = employeePendingPaychecks.get(employee);
            if (pendingChecks == null) {
                pendingChecks = new HashSet<WorkersCompPaycheckPendingState>();
                employeePendingPaychecks.put(employee, pendingChecks);
            }
            pendingChecks.add(pendingCheck);
        }
    }
}
