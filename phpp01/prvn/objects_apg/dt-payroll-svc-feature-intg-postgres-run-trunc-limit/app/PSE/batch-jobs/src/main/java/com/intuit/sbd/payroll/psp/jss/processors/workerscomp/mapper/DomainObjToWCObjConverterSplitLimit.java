package com.intuit.sbd.payroll.psp.jss.processors.workerscomp.mapper;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.context.PSPRequestContextManagerHelper;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.wc.util.QBDTPayrollItemCode;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.model.PayrollDTO;
import com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.*;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.springframework.util.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.JAXBElement;
import java.util.*;

public class DomainObjToWCObjConverterSplitLimit {
    private static final SpcfLogger logger = SpcfLogManager.getLogger(DomainObjToWCObjConverterSplitLimit.class);
    final static String EMPTY_NAME = "<EMPTY>";
    private static final String DATE_FORMAT_MM_DD_YYYY = "MM/dd/yyyy";
    private static DomainEntitySet<WcCompany> wcCompanyEntity = null;
    public static Business convert(Company in) {
        Business out = null;
        ObjectFactory obj = null;
        if (in != null) {
            obj= new ObjectFactory();
            out = obj.createBusiness();

             wcCompanyEntity= Application.find(WcCompany.class,WcCompany.Company().equalTo(in));
             if ( CollectionUtils.isEmpty(wcCompanyEntity) || StringUtils.isEmpty(((WcCompany)wcCompanyEntity.get(0)).getIntegrationId())) {
                throw new RuntimeException("WcCompany null or Integration id is missing for companyId "+in.getSourceCompanyId());
             }
//            out.getContent().add(obj.createBusinessCompanyId(in.getSourceCompanyId()));
            out.getContent().add(obj.createBusinessCompanyId(((WcCompany)wcCompanyEntity.get(0)).getIntegrationId()));
            out.getContent().add(obj.createFein(in.getFedTaxId()));
            out.getContent().add(obj.createBusinessName(in.getLegalName()));
            out.getContent().add(obj.createBusinessAddress(convert(in.getLegalAddress())));
            out.getContent().add(obj.createBusinessEmail(in.getNotificationEmail()));
            out.getContent().add(obj.createFax(null));
            out.getContent().add(obj.createFeatureSetName(null));
            out.getContent().add(obj.createBusinessPartner(null));
            if (in.getPayrollFrequency() != null) {
                out.getContent().add(obj.createPayrollFrequency(DomainObjToWCObjConverter.getPayFrequency(PayrollFrequencyCode.valueOf(in.getPayrollFrequency().getName()))));
            }
            out.getContent().add(obj.createBusinessPhone(in.getPhone()));
            out.getContent().add(obj.createBusinessProduct(null));
            out.getContent().add(obj.createBusinessSicCode(null));
            out.getContent().add(obj.createBusinessSource(in.getSourceSystemCd().name()));
            out.getContent().add(obj.createBusinessStatus(null));
            out.getContent().add(obj.createBusinessStatusDate(null));
        }
        return out;
    }

    public static com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Address convert(com.intuit.sbd.payroll.psp.domain.Address in) {
        com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Address out = null;
        if(in != null) {
            out = new ObjectFactory().createAddress();
            out.setAddressLineOne(in.getAddressLine1());
            out.setAddressLineTwo(in.getAddressLine2());
            out.setCity(in.getCity());
            out.setZip(in.getZipCode());
            out.setState(in.getState());
        }
        return out;
    }

    public static com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Employee convert(com.intuit.sbd.payroll.psp.domain.Employee
                                                                                 in) {
        com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Employee out = null;
        if(in != null) {
            out = new ObjectFactory().createEmployee();

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

    public static com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Paycheck convert(com.intuit.sbd.payroll.psp.domain.Paycheck in) {
        com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Paycheck out = null;
        if (in != null) {
            double totalHours = 0;
            List<com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem> paycheckLineItems = new ArrayList<com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem>();
            if (in.getCompensationCollection() != null) {
                for (Compensation compensation : in.getCompensationCollection()) {
                    CompanyPayrollItem companyPayrollItem = compensation.getCompanyPayrollItem();
                    QbdtPayrollItemInfo qbdtPayrollItem = companyPayrollItem != null ? companyPayrollItem.getQbdtPayrollItemInfo() : null;
                    String paycheckItemType = "PAY";
                    SpcfMoney amount = compensation.getCompensationAmount();
                    com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem item = createPaycheckItem(companyPayrollItem, qbdtPayrollItem, paycheckItemType, amount);
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
                    com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem item = createPaycheckItem(companyPayrollItem, qbdtPayrollItem, paycheckItemType, amount);
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

    public static com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Paycheck convert(Company company, Paystub in) {
        com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Paycheck out = null;
        if (in != null) {
            double totalHours = 0;
            List<com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem> paycheckLineItems = new ArrayList<com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem>();
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
                    QbdtPayrollItemInfo qbdtPayrollItem = DomainObjToWCObjConverter.findQbdtPayrollItemInfo(payItem, qbdtCompanyPayrollItems);
                    CompanyPayrollItem companyPayrollItem = qbdtPayrollItem != null ?
                            qbdtPayrollItem.getCompanyPayrollItem() : null;
                    SpcfMoney amount = payItem.getCurAmt();
                    com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem item = null;
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

    private static com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem createPaycheckItem(CompanyPayrollItem companyPayrollItem,
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

    private static com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem createPaycheckItem(String payOrDeductionCode,
                                                                                                                                   String payCustomItemName,
                                                                                                                                   QbdtPayrollItemInfo qbdtPayrollItem,
                                                                                                                                   String paycheckItemType,
                                                                                                                                   SpcfMoney amount)
    {
        ObjectFactory obj = new ObjectFactory();
        com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem paycheckItem =  obj.createPaycheckItem();


        paycheckItem.setType(paycheckItemType);
        if (paycheckItemType == "PAY") {
            if (amount != null) {
                paycheckItem.setPayAmount(SpcfUtils.convertToBigDecimal(amount).doubleValue());
            }
            paycheckItem.setPayCode(payOrDeductionCode);
            paycheckItem.setPayCustomItemName(obj.createPaycheckItemPayCustomItemName(payCustomItemName));
            if (qbdtPayrollItem != null) {
                QBDTPayrollItemCode payrollItemCode = QBDTPayrollItemCode.getItemByCode(qbdtPayrollItem.getDetailType());
                double overTime = qbdtPayrollItem.getOvertimeMultiplier();
                if (payrollItemCode == QBDTPayrollItemCode.OVERTIMEHOURLY  || overTime > 1){
                    paycheckItem.setOverTimeMultiplier(obj.createPaycheckItemOverTimeMultiplier(String.valueOf(overTime)));
                }

                if (payrollItemCode != null && payrollItemCode != QBDTPayrollItemCode.NONE){
                    String mappedPaymentType = payrollItemCode.name();
                    if (mappedPaymentType != null){
                        paycheckItem.setPayItemName(obj.createPaycheckItemPayItemName(mappedPaymentType));
                    }
                }
            }
        }
        else if (paycheckItemType == "DEDUCTION") {
            if (amount != null) {
                paycheckItem.setDeductionAmount(SpcfUtils.convertToBigDecimal(amount).doubleValue());
            }
            paycheckItem.setDeductionCode(payCustomItemName);
            paycheckItem.setDeductionCustomItemName(obj.createPaycheckItemDeductionCustomItemName(payCustomItemName));
            if (qbdtPayrollItem != null) {
                QBDTPayrollItemCode payrollItemCode = QBDTPayrollItemCode.getItemByCode(qbdtPayrollItem.getDetailType());
                if (payrollItemCode != null && payrollItemCode != QBDTPayrollItemCode.NONE) {
                    String mappedPaymentType = payrollItemCode.name();
                    if (mappedPaymentType != null){
                        paycheckItem.setDeductionItemName(obj.createPaycheckItemDeductionItemName(mappedPaymentType));
                    }
                }
            }
        }

        return paycheckItem;
    }

    private static com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Paycheck createPaycheck(String paycheckId,
                                                                                                    SpcfCalendar paycheckDate,
                                                                                                    SpcfCalendar payPeriodBeginDate,
                                                                                                    SpcfCalendar payPeriodEndDate,
                                                                                                    double totalHours,
                                                                                                    List<com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem> paycheckLineItems) {
        ObjectFactory obj= new ObjectFactory();
        com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Paycheck out =  obj.createPaycheck();

        out.setPaycheckId(paycheckId);
        if (paycheckDate != null) {
            out.setCheckDate(DomainObjToWCObjConverter.toMMDDYYYY(paycheckDate));
        }
        if (payPeriodBeginDate != null) {
            out.setPeriodStartDate(DomainObjToWCObjConverter.toMMDDYYYY(payPeriodBeginDate));
        }
        if (payPeriodEndDate != null) {
            out.setPeriodEndDate(DomainObjToWCObjConverter.toMMDDYYYY(payPeriodEndDate));
        }
        out.setHours(totalHours);

        if (paycheckLineItems != null && paycheckLineItems.size() > 0) {
            // Group same pay item types
            Map<PaycheckPayLineItemKey, com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem> paycheckPayLineItemsByType = new HashMap<PaycheckPayLineItemKey, com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem>();
            Map<PaycheckDeductionLineItemKey, com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem> paycheckDeductionLineItemsByType = new HashMap<PaycheckDeductionLineItemKey, com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem>();
            for (com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem paycheckLineItem : paycheckLineItems) {
                if (paycheckLineItem.getType().equals("PAY")) {
                    PaycheckPayLineItemKey key = new PaycheckPayLineItemKey(paycheckLineItem);
                    com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem consolidatedPaycheckLineItem = paycheckPayLineItemsByType.get(key);
                    if (consolidatedPaycheckLineItem != null) {
                        consolidatedPaycheckLineItem.setPayAmount(consolidatedPaycheckLineItem.getPayAmount() + paycheckLineItem.getPayAmount());
                    } else {
                        paycheckPayLineItemsByType.put(key, paycheckLineItem);
                    }
                }
                else if (paycheckLineItem.getType().equals("DEDUCTION")) {
                    PaycheckDeductionLineItemKey key = new PaycheckDeductionLineItemKey(paycheckLineItem);
                    com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem consolidatedPaycheckLineItem = paycheckDeductionLineItemsByType.get(key);
                    if (consolidatedPaycheckLineItem != null) {
                        consolidatedPaycheckLineItem.setDeductionAmount(consolidatedPaycheckLineItem.getDeductionAmount() + paycheckLineItem.getDeductionAmount());
                    } else {
                        paycheckDeductionLineItemsByType.put(key, paycheckLineItem);
                    }
                }
            }

            out.setPaycheckItems(obj.createArrayOfPaycheckItem());


            out.getPaycheckItems().getItem().addAll(paycheckPayLineItemsByType.values());
            //  out.setPaycheckItems(paycheckPayLineItemsByType.values());
            out.getPaycheckItems().getItem().addAll(paycheckDeductionLineItemsByType.values());
        }
        return out;
    }

    private static class PaycheckPayLineItemKey {

        private String payCode;
        private JAXBElement<String> payItemName;
        private JAXBElement<String> payCustomItemName;
        private JAXBElement<String> overtimeMultiplier;

        public PaycheckPayLineItemKey(com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem item) {
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

            if (overtimeMultiplier != null ? !StringUtils.equals(overtimeMultiplier.getValue(),that.overtimeMultiplier.getValue()) : that.overtimeMultiplier != null)
                return false;
            if (payCode != null ? !payCode.equals(that.payCode) : that.payCode != null) return false;
            if (payCustomItemName != null ? !StringUtils.equals(payCustomItemName.getValue(),that.payCustomItemName.getValue()) : that.payCustomItemName != null)
                return false;
            if (payItemName != null ? !StringUtils.equals(payItemName.getValue(),that.payItemName.getValue()) : that.payItemName != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = payCode != null ? payCode.hashCode() : 0;
            result = 31 * result + (payItemName != null ? (payItemName.getValue()!=null?payItemName.getValue().hashCode():0): 0);
            result = 31 * result + (payCustomItemName != null ? (payCustomItemName.getValue()!=null?payCustomItemName.getValue().hashCode():0) : 0);
            result = 31 * result + (overtimeMultiplier != null ? (overtimeMultiplier.getValue()!=null?overtimeMultiplier.getValue().hashCode():0) : 0);
            return result;
        }
    }

    private static class PaycheckDeductionLineItemKey {

        private String deductionCode;
        private JAXBElement<String> deductionItemName;
        private JAXBElement<String> deductionCustomItemName;

        public PaycheckDeductionLineItemKey(com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.PaycheckItem item) {
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
            if (deductionCustomItemName != null ? !StringUtils.equals(deductionCustomItemName.getValue(),that.deductionCustomItemName.getValue()) : that.deductionCustomItemName != null)
                return false;
            if (deductionItemName != null ? !StringUtils.equals(deductionItemName.getValue(),that.deductionItemName.getValue()) : that.deductionItemName != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = deductionCode != null ? deductionCode.hashCode() : 0;
            result = 31 * result + (deductionItemName != null ? (deductionItemName.getValue()!=null?deductionItemName.getValue().hashCode():0) : 0);
            result = 31 * result + (deductionCustomItemName != null ? (deductionCustomItemName.getValue()!=null?deductionCustomItemName.getValue().hashCode():0)  : 0);
            return result;
        }
    }
    public static PayrollDTO<com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Payroll> createDomainObjectToPayrollDto(List<WorkersCompPaycheckPendingState> pendingPaychecks)
    {
        Map<com.intuit.sbd.payroll.psp.domain.Company, Set<com.intuit.sbd.payroll.psp.domain.Employee>> companyEmployees =new HashMap<com.intuit.sbd.payroll.psp.domain.Company, Set<com.intuit.sbd.payroll.psp.domain.Employee>>();
        Map<com.intuit.sbd.payroll.psp.domain.Employee, Set<WorkersCompPaycheckPendingState>> employeePendingPaychecks=new HashMap<com.intuit.sbd.payroll.psp.domain.Employee, Set<WorkersCompPaycheckPendingState>>();
        DomainObjToWCObjConverter.fillDomainObjectToEmployeePaycheckMap(pendingPaychecks, companyEmployees, employeePendingPaychecks);
        Payroll payroll = new Payroll();
        PayrollDTO dto = new PayrollDTO(payroll);

        for (Company company : companyEmployees.keySet()) {
            try {
                PSPRequestContextManagerHelper.getPSPRequestContextManager().setRequestContextCompany(company);
                Business business = DomainObjToWCObjConverterSplitLimit.convert(company);
                Set<com.intuit.sbd.payroll.psp.domain.Employee> employees = companyEmployees.get(company);
                com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.ArrayOfEmployee objEmployees=null;
                for (com.intuit.sbd.payroll.psp.domain.Employee employee : employees) {
                    com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Employee wcEmployee = DomainObjToWCObjConverterSplitLimit.convert(employee);
                    Set<WorkersCompPaycheckPendingState> pendingChecks = employeePendingPaychecks.get(employee);
                    for (WorkersCompPaycheckPendingState pendingCheck : pendingChecks) {
                        dto.addIncludedPaycheck(company.getSourceCompanyId(), pendingCheck.getWorkersCompPaycheck());
                        com.intuit.sbd.payroll.psp.domain.Paycheck paycheck = pendingCheck.getWorkersCompPaycheck().getPaycheck();
                        Paystub paystub = DomainObjToWCObjConverter.loadPaystub(paycheck);

                        com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.Paycheck wcPaycheck = null;
                        if (paystub != null) {
                            wcPaycheck = DomainObjToWCObjConverterSplitLimit.convert(company, paystub);
                        } else {
                            wcPaycheck = DomainObjToWCObjConverterSplitLimit.convert(paycheck);
                        }

                        switch (pendingCheck.getStateCd()) {
                            case PendingNew:
                                wcPaycheck.setCheckStatus("NEW");
                                break;
                            default:
                                throw new RuntimeException(
                                        "Invalid code for pending check status: " + pendingCheck.getStateCd());
                        }


                        // Set version
                     /*   Long version = pendingCheck.getWorkersCompPaycheck().getPaycheckVersion();
                        wcPaycheck.setPaycheckVersion(version.intValue());*/

                        if (wcEmployee.getPaychecks() == null) {
                            wcEmployee.setPaychecks(new ObjectFactory().createArrayOfPaycheck());
                        }

                        wcEmployee.getPaychecks().getItem().add(wcPaycheck);

                        logger.info("WC Gateway :: Company=" + company.getSourceCompanyId() +
                                ", Employee=" + wcEmployee.getEmployeeID() + ", PayCheck_ID=" +
                                wcPaycheck.getPaycheckId() + ", Status=" + wcPaycheck.getCheckStatus());
                    }

                    if (objEmployees == null) {
                        objEmployees=new ObjectFactory().createArrayOfEmployee();
                    }
                    objEmployees.getItem().add(wcEmployee);
                    //   business.getEmployees().getEmployee().add(wcEmployee);*/
                }
                business.getContent().add(new com.intuit.sbd.payroll.psp.jss.processors.workerscomp.trupay.schema.ObjectFactory().createBusinessEmployees(objEmployees));
                if (payroll.getBusinesses() == null) {
                    payroll.setBusinesses(new ObjectFactory().createArrayOfBusiness());
                }
                payroll.getBusinesses().getItem().add(business);
            } finally {
                PSPRequestContextManagerHelper.getPSPRequestContextManager().clearRequestContextCompany();
            }

        }
        return dto;
    }
}
