package com.intuit.sbd.payroll.psp.adapters.qbdtws.test;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.QBDTWSSubmitPayrollRequestProcess;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.*;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang.WordUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: rnorian
 * Date: Mar 26, 2010
 * Time: 10:52:17 AM
 */
public class QBDTWSRequestCreator {
    static private int sourceEmployeeId = 0;
    static private int sourceEmployeeSSN = 0;

    static private Integer paycheckId = 0;
    static private Integer checkNumber = 0;

    static private Integer payrollItemId = 8;
    static private Integer payrollItemTaxTrackingTypeId = 0;

    static private final Integer EARNING_ITEM_ID = 1;
    static private final Integer PRETAX_ITEM_ID = 2;
    static private final Integer COMPANY_CONTRIBUTION_ITEM_ID = 3;
    static private final Integer ADJ_NET_PAY_ITEM_ID = 4;
    static private final Integer TAX_COMPANY_ITEM_ID = 5;
    static private final Integer TAX_ITEM_ID = 6;
    static private final Integer DIRECT_DEPOSIT_ITEM_ID = 7;

    public static SubmitPayrollRequest createSubmitPayrollRequest() {
        return createSubmitPayrollRequest(null, null, null);
    }

    public static SubmitPayrollRequest createSubmitPayrollRequest(Company pCompany) {
        SubmitPayrollRequest submitPayrollRequest = createSubmitPayrollRequest(pCompany.getSourceCompanyId(), null, Long.toString(pCompany.getCurrentToken()));
        submitPayrollRequest.setUpdateCompanyRequest(new UpdateCompanyRequest());
        submitPayrollRequest.getUpdateCompanyRequest().setCompany(createQBCompany(pCompany));
        return submitPayrollRequest;
    }

    /**
     * Initializes the request with the PIN, the company's PSID, and token
     * @param request The request to initialize
     * @param pCompany The company creating the request
     */
    public static void initRequest(SubmitPayrollRequest request, Company pCompany) {
        request.setPIN(DataLoadServices.PIN);
        request.setPSID(pCompany.getSourceCompanyId());
        request.setCurrentToken(Long.toString(pCompany.getCloudCurrentToken()));
    }

    /**
     * Creates an UpdateCompanyRequest for the company
     * @param pCompany The company to use
     * @return An UpdateCompanyRequest for the company
     */
    public static UpdateCompanyRequest createUpdateCompanyRequest(Company pCompany) {
        UpdateCompanyRequest updateCompanyRequest = new UpdateCompanyRequest();
        updateCompanyRequest.setCompany(createQBCompany(pCompany));
        updateCompanyRequest.getCompany().setQBVersion("20.00.B.10");

        return updateCompanyRequest;
    }

    /**
     * Creates a SubmitEmployeesRequest using the company's cloud employees
     * @param pCompany The company to use
     * @return A SubmitEmployeesRequest using the company's cloud employees
     */
    public static SubmitEmployeesRequest createSubmitEmployeesRequest(Company pCompany) {
        SubmitEmployeesRequest submitEmployeesRequest  = new SubmitEmployeesRequest();
        QBEmployees qbEmployees = new QBEmployees();
        for (Employee employee : pCompany.getCloudEmployees().sort(Employee.SourceEmployeeId())) {
            qbEmployees.getEmployee().add(createQBEmployee(employee, pCompany.isCompanyOnService(ServiceCode.Tax)));
        }

        submitEmployeesRequest.setEmployees(qbEmployees);
        
        return submitEmployeesRequest;
    }

    /**
     * Creates a SubmitEmployeesRequest using the IEMPs and IEMPMODs in the OFX
     * @param pCompany The company to use
     * @param ofx The OFX containing the IEMPs and IEMPMODs
     * @return A SubmitEmployeesRequest using the IEMPs and IEMPMODs in the OFX
     */
    public static SubmitEmployeesRequest createSubmitEmployeesRequest(Company pCompany, OFX ofx) {
        SubmitEmployeesRequest submitEmployeesRequest  = new SubmitEmployeesRequest();
        QBEmployees qbEmployees = new QBEmployees();
        
        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP()) {
            QBEmployee qbEmployee = QBDTWSRequestCreator.createQBEmployee(iemp, pCompany.getSourceCompanyId());
            qbEmployees.getEmployee().add(qbEmployee);
        }

        for (IEMP iemp : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMPMOD()) {
            QBEmployee qbEmployee = QBDTWSRequestCreator.createQBEmployee(iemp, pCompany.getSourceCompanyId());
            qbEmployees.getEmployee().add(qbEmployee);
        }

        submitEmployeesRequest.setEmployees(qbEmployees);

        return submitEmployeesRequest;
    }

    /**
     * Creates a QBPaycheck for each employee in the list
     * @param pCompany The company to use
     * @param employees The list of employees to create QBPaychecks
     * @param payDate The paydate for the paychecks
     * @return A QBPaycheck for each employee in the list
     */
    public static QBPaychecks createQBPaychecksForEmployees(Company pCompany, DomainEntitySet<Employee> employees,
                                                                     SpcfCalendar payDate) {
        QBPaychecks paychecks = new QBPaychecks();

        QBDate qbPayDate = QBDTWSRequestCreator.createQBDate(payDate);

        for (Employee employee : employees) {
            QBEmployee qbEmployee = createQBEmployee(employee, pCompany.isCompanyOnService(ServiceCode.Tax));
            QBPaycheck qbPaycheck = QBDTWSRequestCreator.createQBPaycheck(qbEmployee);
            qbPaycheck.setPayDate(qbPayDate);

            paychecks.getPaycheck().add(qbPaycheck);
        }
        

        return paychecks;
    }

    /**
     * Creates a list of QBPayrollItems
     * @param pCompany The company to use
     * @return The QBPayrollItems list
     */
    public static QBPayrollItems createPayrollItems(Company pCompany) {
        // TODO: This isn't actually using company for anything
        return createDefaultPayrollItems();
    }

    public static SubmitPayrollRequest createSubmitPayrollRequestFromCompany(Company pCompany) {
        SubmitPayrollRequest submitPayrollRequest = new SubmitPayrollRequest();
        submitPayrollRequest.setPSID(pCompany.getSourceCompanyId());
        submitPayrollRequest.setCurrentToken(Long.toString(pCompany.getCloudCurrentToken()));

        // ----- Company Info
        UpdateCompanyRequest updateCompanyRequest = new UpdateCompanyRequest();
        updateCompanyRequest.setCompany(createQBCompany(pCompany));
        submitPayrollRequest.setUpdateCompanyRequest(updateCompanyRequest);

        // ------ Employees
        SubmitEmployeesRequest submitEmployeesRequest  = new SubmitEmployeesRequest();
        QBEmployees qbEmployees = new QBEmployees();
        for (Employee employee : pCompany.getCloudEmployees().sort(Employee.SourceEmployeeId())) {
            qbEmployees.getEmployee().add(createQBEmployee(employee, pCompany.isCompanyOnService(ServiceCode.Tax)));
        }
        submitEmployeesRequest.setEmployees(qbEmployees);
        submitPayrollRequest.setSubmitEmployeesRequest(submitEmployeesRequest);

        // ------- Payroll Items
        QBPayrollItems qbPayrollItems = new QBPayrollItems();
        List<QBPayrollItem> payrollItems = qbPayrollItems.getPayrollItem();

        List<CompanyPayrollItem> pItems = new ArrayList<CompanyPayrollItem>(pCompany.getCompanyPayrollItemCollection());
        Collections.sort(pItems, new Comparator<CompanyPayrollItem>() {
            //Q. These are integers but stored as strings so the normal DES.sort will do string sorting which will be wrong for some values
            public int compare(CompanyPayrollItem o1, CompanyPayrollItem o2) {
                return Integer.valueOf(o1.getSourcePayrollItemId()).compareTo(Integer.valueOf(o2.getSourcePayrollItemId()));
            }
        });

        for (CompanyPayrollItem companyPayrollItem : pItems) {
            payrollItems.add(createQBPayrollItem(companyPayrollItem, pCompany.isCompanyOnService(ServiceCode.Tax)));
        }
        submitPayrollRequest.setPayrollItemList(qbPayrollItems);

        return submitPayrollRequest;
    }

    /**
     * Creates a SubmitPayrollRequest w/the following default data:
     * PSID: argument value or "1234567"
     * PIN:  argument value or "test1234!"
     * CurrentToken: argument value or "0"
     *
     * UpdateCompany: <null>
     * Employees: 1 employee, source employee id = 1, ssn = 999090001
     * Paychecks: 1 paycheck for employeeid = 1
     */
    public static SubmitPayrollRequest createSubmitPayrollRequest(String psid, String pin, String currentToken) {
        SubmitPayrollRequest submitPayrollRequest = new SubmitPayrollRequest();

        if (psid != null)
            submitPayrollRequest.setPSID(psid);
        else
            submitPayrollRequest.setPSID("1234567");

        if (pin != null)
            submitPayrollRequest.setPIN(pin);
        else
            submitPayrollRequest.setPIN("test1234!");

        if (currentToken != null)
            submitPayrollRequest.setCurrentToken(currentToken);
        else
            submitPayrollRequest.setCurrentToken("0");

        // employees
        SubmitEmployeesRequest employeesRequest = createSubmitEmployeesRequest(1, psid);
        submitPayrollRequest.setSubmitEmployeesRequest(employeesRequest);

        // paychecks
        QBPaychecks paychecks = new QBPaychecks();
        submitPayrollRequest.setPaycheckList(paychecks);
        paychecks.getPaycheck().add(createQBPaycheck(employeesRequest.getEmployees().getEmployee().get(0)));

        // payroll meta data
        submitPayrollRequest.setPayrollItemList(createDefaultPayrollItems());
        submitPayrollRequest.setDetailTypeList(createDetailTypes());
        submitPayrollRequest.setPayrollItemTypeList(createPayrollItemTypes());
        submitPayrollRequest.setSpecialPayrollItemTypeList(createSpecialItemTypes());
        submitPayrollRequest.setTaxTrackingtypeList(createTaxTrackingTypes());


        //submitPayrollRequest.setUpdateCompanyRequest();

        return submitPayrollRequest;
    }


    public static ArrayList<QBPayrollItem> findPayrollItems(SubmitPayrollRequest pRequest, QBPayrollItemCategory pCategory) {
        return findPayrollItems(pRequest.getPayrollItemList(), pCategory, null);
    }

    public static QBPayrollItem findPayrollItem(QBPayrollItems pPayrollItems, QBPayrollItemCategory pCategory, String taxTrackingTypeId) {
        ArrayList<QBPayrollItem> foundItems = findPayrollItems(pPayrollItems, pCategory, taxTrackingTypeId);
        if (foundItems.size() > 0)
            return foundItems.get(0);

        return null;
    }

    public static ArrayList<QBPayrollItem> findPayrollItems(QBPayrollItems pPayrollItems, QBPayrollItemCategory pCategory, String taxTrackingTypeId) {
        ArrayList<QBPayrollItem> foundItems = new ArrayList<QBPayrollItem>();

        List<QBPayrollItem> items = pPayrollItems.getPayrollItem();
        for (QBPayrollItem payrollItem : items) {
            if (payrollItem.getPayrollItemCategory() == pCategory) {
                if (taxTrackingTypeId == null) {
                    foundItems.add(payrollItem);
                } else if (payrollItem.getTaxTrackingTypeId() != null && payrollItem.getTaxTrackingTypeId().equals(taxTrackingTypeId)) {
                    foundItems.add(payrollItem);
                }
            }
        }

        return foundItems;
    }

    public static QBPayrollItems createDefaultPayrollItems() {
        return createDefaultPayrollItems("401K");
    }

    public static QBPayrollItems createDefaultPayrollItems(String custodialId) {
        QBPayrollItems qbPayrollItems = new QBPayrollItems();
        List<QBPayrollItem> payrollItems = qbPayrollItems.getPayrollItem();

        payrollItems.add(createEarningPayrollItem());

        QBPayrollItem pretaxItem = createEEDeferralPreTaxPayrollItem();
        if (custodialId != null)
            pretaxItem.setAgencyNumber(custodialId);
        payrollItems.add(pretaxItem);

        payrollItems.add(createCompanyContributionPayrollItem());
        payrollItems.add(createAdjNetPayPayrollItem());
        payrollItems.add(createTaxCompanyPayrollItem());
        payrollItems.add(createTaxPayrollItem());
        payrollItems.add(createDirectDepositPayrollItem());

        return qbPayrollItems;
    }

    public static QBPayrollItem createEarningPayrollItem() {
        return createPayrollItem(QBPayrollItemCategory.EARNING_ITEM, EARNING_ITEM_ID);
    }

    public static QBPayrollItem createPreTaxPayrollItem() {
        return createPayrollItem(QBPayrollItemCategory.PRE_TAX_ITEM, PRETAX_ITEM_ID, 0, null);
    }

    public static QBPayrollItem createEEDeferralPreTaxPayrollItem() {
        return createPayrollItem(QBPayrollItemCategory.PRE_TAX_ITEM, PRETAX_ITEM_ID, Integer.parseInt(QBDTWSSubmitPayrollRequestProcess.TAX_TRACKING_TYPE_401K), "401k");
    }

    public static QBPayrollItem createCompanyContributionPayrollItem() {
        return createPayrollItem(QBPayrollItemCategory.NON_TAX_COMPANY_ITEM, COMPANY_CONTRIBUTION_ITEM_ID);
    }

    public static QBPayrollItem createAdjNetPayPayrollItem() {
        return createPayrollItem(QBPayrollItemCategory.ADJ_NET_PAY_ITEM, ADJ_NET_PAY_ITEM_ID);
    }

    public static QBPayrollItem createTaxCompanyPayrollItem() {
        return createPayrollItem(QBPayrollItemCategory.TAX_COMPANY_ITEM, TAX_COMPANY_ITEM_ID);
    }

    public static QBPayrollItem createTaxPayrollItem() {
        return createPayrollItem(QBPayrollItemCategory.TAX_ITEM, TAX_ITEM_ID);
    }

    public static QBPayrollItem createDirectDepositPayrollItem() {
        return createPayrollItem(QBPayrollItemCategory.DIRECT_DEPOSIT_ITEM, DIRECT_DEPOSIT_ITEM_ID);
    }

    static int nativePayrollItemId = 1;
    public static QBPayrollItem createQBPayrollItem(CompanyPayrollItem pCompanyPayrollItem, boolean isAssisted) {
        QBPayrollItem qbPayrollItem = new QBPayrollItem();
        qbPayrollItem.setID(pCompanyPayrollItem.getSourcePayrollItemId());
        if (isAssisted) {
            qbPayrollItem.setOfxPayrollId(pCompanyPayrollItem.getSourcePayrollItemId());
        }
        qbPayrollItem.setName(pCompanyPayrollItem.getSourceDescription());

        PayrollItem payrollItem = pCompanyPayrollItem.getPayrollItem();
        switch (payrollItem.getPayrollItemCode()) {
            case Compensation:
            case Salary:
                qbPayrollItem.setPayrollItemCategory(QBPayrollItemCategory.EARNING_ITEM);
                qbPayrollItem.setPayrollItemTypeId("0");
                break;
            case Bonus:
                qbPayrollItem.setPayrollItemCategory(QBPayrollItemCategory.EARNING_ITEM);
                qbPayrollItem.setPayrollItemTypeId("12");
                break;
            case Commission:
                qbPayrollItem.setPayrollItemCategory(QBPayrollItemCategory.EARNING_ITEM);
                qbPayrollItem.setPayrollItemTypeId("2");
                break;
            case Hourly:
                qbPayrollItem.setPayrollItemCategory(QBPayrollItemCategory.EARNING_ITEM);
                qbPayrollItem.setPayrollItemTypeId("1");
                break;
            case Tp401kEmployeeDeferral:
            case OtherPreTaxDeduction:
            case Tp401kLoanPayment:
            case Tp401kRoth:
                qbPayrollItem.setPayrollItemCategory(QBPayrollItemCategory.PRE_TAX_ITEM);
                qbPayrollItem.setPayrollItemTypeId("4");
                qbPayrollItem.setTaxTrackingTypeId("1");
                break;
            case OtherPostTaxDeduction:
                qbPayrollItem.setPayrollItemCategory(QBPayrollItemCategory.NON_TAX_COMPANY_ITEM);
                qbPayrollItem.setPayrollItemTypeId("4");
                qbPayrollItem.setTaxTrackingTypeId("0");
                break;
            case DirectDeposit:
                qbPayrollItem.setPayrollItemCategory(QBPayrollItemCategory.ADJ_NET_PAY_ITEM);
                qbPayrollItem.setPayrollItemTypeId("11");
                qbPayrollItem.setTaxTrackingTypeId("0");
                break;
            case Tp401kSafeHarbor:
            case Tp401kProfitSharing:
            case OtherNonTaxableEmployerContribution:
            case OtherAdditionPostTax:
                qbPayrollItem.setPayrollItemCategory(QBPayrollItemCategory.NON_TAX_COMPANY_ITEM);
                qbPayrollItem.setPayrollItemTypeId("5");
                qbPayrollItem.setTaxTrackingTypeId("0");
                break;
            case Tp401kEmployerMatch:
            case OtherAdditionPreTax:
            case OtherTaxableEmployerContribution:
                qbPayrollItem.setPayrollItemCategory(QBPayrollItemCategory.TAX_COMPANY_ITEM);
                qbPayrollItem.setPayrollItemTypeId("5");
                qbPayrollItem.setTaxTrackingTypeId("1");
                break;
        }

        qbPayrollItem.setDetailTypeId("1");
        qbPayrollItem.setSpecialPayrollItemTypeId("1");
        if (qbPayrollItem.getTaxTrackingTypeId() == null) {
            qbPayrollItem.setTaxTrackingTypeId("0");
        }
        
        return qbPayrollItem;
    }

    public static QBPayrollItem createPayrollItem(QBPayrollItemCategory pItemCategory) {
        return createPayrollItem(pItemCategory, null, null, null);
    }

    public static QBPayrollItem createPayrollItem(QBPayrollItemCategory pItemCategory, Integer pPayrollItemId) {
        return createPayrollItem(pItemCategory, pPayrollItemId, null, null);
    }

    public static QBPayrollItem createPayrollItem(QBPayrollItemCategory pItemCategory, Integer pPayrollItemId, Integer pTaxTrackingTypeId, String pAgencyNumber) {
        QBPayrollItem qbPayrollItem = new QBPayrollItem();

        qbPayrollItem.setPayrollItemCategory(pItemCategory);

        if (pPayrollItemId != null) {
            qbPayrollItem.setID(pPayrollItemId.toString());
            qbPayrollItem.setOfxPayrollId(pPayrollItemId.toString());
        } else {
            qbPayrollItem.setID((++payrollItemId).toString());
            qbPayrollItem.setOfxPayrollId((payrollItemId).toString());
        }

        qbPayrollItem.setName("name-" + qbPayrollItem.getID());

        if (pAgencyNumber != null) {
            qbPayrollItem.setAgencyNumber(pAgencyNumber);
        }

        if (pTaxTrackingTypeId != null)
            qbPayrollItem.setTaxTrackingTypeId(pTaxTrackingTypeId.toString());
        else
            qbPayrollItem.setTaxTrackingTypeId((++payrollItemTaxTrackingTypeId).toString());

        qbPayrollItem.setDetailTypeId("0");
        
        qbPayrollItem.setName("Payroll Item Name");

        return qbPayrollItem;
    }

    public static SubmitEmployeesRequest createSubmitEmployeesRequest(QBEmployee... employees) {
        SubmitEmployeesRequest request = new SubmitEmployeesRequest();
        request.setEmployees(new QBEmployees());

        for (QBEmployee employee : employees) {
            request.getEmployees().getEmployee().add(employee);
        }

        return request;
    }

    public static SubmitEmployeesRequest createSubmitEmployeesRequest(int count, String sourceCompanyId) {
        SubmitEmployeesRequest request = new SubmitEmployeesRequest();
        request.setEmployees(createQBEmployees(count, sourceCompanyId));
        return request;
    }

    public static QBPaycheck createQBPaycheck(Paycheck pPaycheck, String checkNumber) {
        QBPaycheck qbPaycheck = new QBPaycheck();

        qbPaycheck.setCheckNumber(checkNumber);
        qbPaycheck.setPaycheckID(pPaycheck.getSourcePaycheckId());
        if (pPaycheck.getSourceEmployee() != null) {
            qbPaycheck.setPaycheckID(pPaycheck.getSourcePaycheckId() + "0000");
            qbPaycheck.setOfxPaycheckID(pPaycheck.getSourcePaycheckId());
        }
        qbPaycheck.setEmployeeID(pPaycheck.getSourceEmployee().getSourceEmployeeId());
        qbPaycheck.setOperation(QBPaycheckOperationEnum.EDIT);
        qbPaycheck.setPayDate(createQBDate(pPaycheck.getPayrollRun().getPaycheckDate()));
        qbPaycheck.setPeriodStartDate(createQBDate(pPaycheck.getPayPeriodBeginDate()));
        qbPaycheck.setPeriodEndDate(createQBDate(pPaycheck.getPayPeriodEndDate()));
        if (pPaycheck.getGrossAmount() != null)
            qbPaycheck.setGrossPay(SpcfUtils.convertToBigDecimal(pPaycheck.getGrossAmount()));
        else
            qbPaycheck.setGrossPay(new BigDecimal(0));

        if (pPaycheck.getNetAmount() != null)
            qbPaycheck.setNetPay(SpcfUtils.convertToBigDecimal(pPaycheck.getNetAmount()));
        else
            qbPaycheck.setNetPay(new BigDecimal(0));

        if (pPaycheck.getYTDGrossAmount() != null)
            qbPaycheck.setYTDGrossPay(SpcfUtils.convertToBigDecimal(pPaycheck.getYTDGrossAmount()));
        else
            qbPaycheck.setYTDGrossPay(new BigDecimal(0));
        
        if (pPaycheck.getYTDNetAmount() != null)
            qbPaycheck.setYTDNetPay(SpcfUtils.convertToBigDecimal(pPaycheck.getYTDNetAmount()));
        else
            qbPaycheck.setYTDNetPay(new BigDecimal(0));

        return qbPaycheck;
    }

    public static QBPaycheck createQBPaycheck(QBEmployee employee) {
        return createQBPaycheck(employee.getSourceEmployeeId(), employee.getSourceEmployeeId() + "_" + (++paycheckId).toString());
    }

    public static QBPaycheck createQBPaycheck(String sourceEmployeeId, String paycheckId) {
        QBPaycheck qbPaycheck = new QBPaycheck();
        qbPaycheck.setEmployeeID(sourceEmployeeId);
        qbPaycheck.setPaycheckID(paycheckId);
        qbPaycheck.setOfxPaycheckID(null);
        qbPaycheck.setOperation(QBPaycheckOperationEnum.ADD);
        qbPaycheck.setCheckNumber(((Integer)(++checkNumber)).toString());
        qbPaycheck.setGrossPay(new BigDecimal(1000));
        qbPaycheck.setYTDGrossPay(new BigDecimal(4000));
        qbPaycheck.setNetPay(new BigDecimal(800));
        qbPaycheck.setYTDNetPay(new BigDecimal(3200));

        SpcfCalendar cal = PSPDate.getPSPTime();
        CalendarUtils.getValidDate(cal, 1);
        qbPaycheck.setPayDate(createQBDate(cal.getYear(), cal.getMonth(), cal.getDay()));
        qbPaycheck.setPeriodStartDate(createQBDate(cal.getYear(), cal.getMonth(), cal.getDay()));
        cal.addDays(7);
        CalendarUtils.getValidDate(cal, 1);
        qbPaycheck.setPeriodEndDate(createQBDate(cal.getYear(), cal.getMonth(), cal.getDay()));
        return qbPaycheck;
    }

    public static QBPaycheckLineItem createQBAdjNetPayLine(String payrollItemId, Double currentAmt, Double ytdAmt) {
        return createQBPaycheckLineItem(payrollItemId, currentAmt, ytdAmt);
    }

    public static QBPaycheckLineItem createQBPreTaxLine(String payrollItemId, Double currentAmt, Double ytdAmt) {
        return createQBPaycheckLineItem(payrollItemId, currentAmt, ytdAmt);
    }

    public static QBPaycheckLineItem createQBNonTaxCompanyLine(String payrollItemId, Double currentAmt, Double ytdAmt) {
        return createQBPaycheckLineItem(payrollItemId, currentAmt, ytdAmt);
    }

    public static QBPaycheckLineItem createQBPaycheckLineItem(String payrollItemId, Double currentAmt, Double ytdAmt) {
        QBPaycheckLineItem paycheckLineItem = new QBPaycheckLineItem();

        if (currentAmt == null)
            currentAmt = new Double(100);
        if (ytdAmt == null)
            ytdAmt = new Double(400);

        paycheckLineItem.setCurrent(new BigDecimal(currentAmt));
        paycheckLineItem.setPayrollItemId(payrollItemId);
        paycheckLineItem.setYTD(new BigDecimal(ytdAmt));
        return paycheckLineItem;
    }

    public static QBEarningItem createQBEarningLine(String payrollItemId, Double currentAmt, Double qty) {
        return createQBEarningLine(payrollItemId, currentAmt, qty, currentAmt, 0.0);
    }

    public static QBEarningItem createQBEarningLine(String payrollItemId, Double currentAmt, Double qty, Double yearToDateAmt, Double rate) {
        QBEarningItem earningItem = new QBEarningItem();

        if (currentAmt == null)
            currentAmt = new Double(1000);
        if (qty == null)
            qty = new Double(10);
        if (yearToDateAmt == null)
            yearToDateAmt = currentAmt;

        earningItem.setCurrent(new BigDecimal(currentAmt));
        earningItem.setQty(new BigDecimal(qty));
        earningItem.setRate(new BigDecimal(rate));
        earningItem.setYTD(new BigDecimal(yearToDateAmt));
        earningItem.setPayrollItemId(payrollItemId);
        return earningItem;
    }

    public static QBDate createQBDate(int year, int month, int day) {
        QBDate date = new QBDate();
        date.setYear(year);
        date.setMonth(month);
        date.setDay(day);
        return date;
    }

    public static QBAddress createQBAddress(Address pAddress) {
        if (pAddress == null)
            return null;

        QBAddress qbAddress = new QBAddress();
        qbAddress.setAddressLine1(pAddress.getAddressLine1());
        qbAddress.setAddressLine2(pAddress.getAddressLine2());
        qbAddress.setAddressLine3(pAddress.getAddressLine3());
        qbAddress.setCity(pAddress.getCity());
        qbAddress.setState(pAddress.getState());
        qbAddress.setZipCode(pAddress.getZipCode());
        qbAddress.setZipCodeExtension(pAddress.getZipCodeExtension());
        qbAddress.setCountry(pAddress.getCountry());
        return qbAddress;
    }

    public static QBDate createQBDate(String pDate) {
        if (pDate == null)
            return null;

        return createQBDate(SpcfCalendar.parse("MM/dd/yyyy", pDate));
    }

    public static QBDate createQBDate(SpcfCalendar pCalendar) {
        if (pCalendar == null)
            return null;

        QBDate qbDate = new QBDate();
        qbDate.setYear(pCalendar.getYear());
        qbDate.setMonth(pCalendar.getMonth());
        qbDate.setDay(pCalendar.getDay());
        return qbDate;
    }

    public static QBEmployee createQBEmployee(Employee pEmployee, boolean isAssistedEmployee) {
        if (pEmployee == null)
            return null;

        QBEmployee employee = new QBEmployee();
        employee.setSourceCompanyId(pEmployee.getCompany().getSourceCompanyId());
        employee.setSourceEmployeeId(pEmployee.getSourceEmployeeId());
        if (isAssistedEmployee) {
            employee.setOfxEmployeeId(pEmployee.getSourceEmployeeId());
        }        
        employee.setSocialSecurityNumber(pEmployee.getTaxId() != null ? pEmployee.getTaxId().replace("-",""):null);
        employee.setFirstName(pEmployee.getFirstName());
        employee.setMiddleName(pEmployee.getMiddleName());
        employee.setLastName(pEmployee.getLastName());
        employee.setSuffix(pEmployee.getSuffix());
        employee.setActive(pEmployee.getStatusCd() == EmployeeStatus.Active);

        employee.setLiveAddress(createQBAddress(pEmployee.getMailingAddress()));

        if (pEmployee.getPhone() != null) {
            employee.setPhoneNumber(pEmployee.getPhone());
        } else {
            employee.setPhoneNumber("555-555-5555");
        }

        if (pEmployee.getEmail() != null) {
            employee.setEmailAddress(pEmployee.getEmail());
        } else {
            employee.setEmailAddress("none@example.com");
        }

        employee.setFedAllowances(pEmployee.getFedAllowances());
        if (!isAssistedEmployee) {
            if (pEmployee.getFedFilingStatus() != null) {
                employee.setFedFilingStatus(QBFilingStatusEnum.fromValue(pEmployee.getFedFilingStatus()));
            } else {
                employee.setFedFilingStatus(QBFilingStatusEnum.HEADOF_HOUSEHOLD);
            }
        }
        employee.setHasRetirementPlan(pEmployee.getHasRetirementPlan());
        employee.setHasThirdPartySickPay(pEmployee.getHasThirdPartySickPay());

        employee.setBirthDate(createQBDate(pEmployee.getBirthDate()));
        if (employee.getBirthDate() == null) {
            employee.setBirthDate(createQBDate(1971, 6, 21));
        }
        employee.setHireDate(createQBDate(pEmployee.getHireDate()));

        if (pEmployee.getThirdParty401kInfo() != null) {
            employee.setFamilyMember(pEmployee.getThirdParty401kInfo().getIsFamilyMember());
            employee.setHighlyCompensatedEmployee(pEmployee.getThirdParty401kInfo().getIsHighlyCompensated());
            employee.setOwnerPercent(pEmployee.getThirdParty401kInfo().getOwnershipPercentage());
        }

        employee.setStatutory(pEmployee.getIsStatutory());
        employee.setStateAllowances(0);
        if (!isAssistedEmployee) {
            if (pEmployee.getFedFilingStatus() != null) {
                employee.setStateFilingStatus(QBFilingStatusEnum.fromValue(pEmployee.getFedFilingStatus()));
            } else {
                employee.setStateFilingStatus(QBFilingStatusEnum.HEADOF_HOUSEHOLD);
            }
        }

        return employee;
    }

    public static QBEmployees createQBEmployees(int count, String sourceCompanyId) {
        QBEmployees qbEmployees = new QBEmployees();
        for (int i = 0; i < count; i++) {
            qbEmployees.getEmployee().add(createQBEmployee(sourceCompanyId));
        }
        return qbEmployees;
    }

    public static QBEmployee createQBEmployee(String sourceCompanyId) {
        return createQBEmployee(sourceCompanyId, Integer.toString(++sourceEmployeeId));
    }

    public static QBEmployee createQBEmployee(String sourceCompanyId, String eeSourceId) {
        String ssn = String.format("%04d", ++sourceEmployeeSSN);
        return createQBEmployee(sourceCompanyId, eeSourceId, "99909" + ssn);
    }

    public static QBEmployee createQBEmployee(String sourceCompanyId, String eeSourceId, String ssn) {
        QBEmployee employee = new QBEmployee();
        employee.setSourceCompanyId(sourceCompanyId);
        employee.setSourceEmployeeId(eeSourceId);
        employee.setSocialSecurityNumber(ssn);
        employee.setFirstName(eeSourceId);
        employee.setMiddleName("q");
        employee.setLastName("doe");
        employee.setSuffix("jr");
        employee.setActive(true);

        employee.setLiveAddress(new QBAddress());
        employee.getLiveAddress().setAddressLine1("Live Address Line 1");
        employee.getLiveAddress().setAddressLine2("Live Address Line 2");
        employee.getLiveAddress().setAddressLine3("Live Address Line 3");
        employee.getLiveAddress().setCity("LiveCity");
        employee.getLiveAddress().setState("CA");
        employee.getLiveAddress().setZipCode("89512");
        employee.getLiveAddress().setZipCodeExtension("7891");
        employee.getLiveAddress().setCountry("US");

        employee.setPhoneNumber("444-444-4444");
        employee.setEmailAddress("john.doe@gmail.com");
        employee.setFedAllowances(new Integer(1));
        employee.setFedFilingStatus(QBFilingStatusEnum.HEADOF_HOUSEHOLD);
        employee.setHasRetirementPlan(true);
        employee.setHasThirdPartySickPay(true);

        employee.setBirthDate(new QBDate());
        employee.getBirthDate().setMonth(1);
        employee.getBirthDate().setDay(1);
        employee.getBirthDate().setYear(1978);

        employee.setHireDate(new QBDate());
        employee.getHireDate().setMonth(12);
        employee.getHireDate().setDay(21);
        employee.getHireDate().setYear(2009);

        employee.setFamilyMember(true);
        employee.setHighlyCompensatedEmployee(true);
        employee.setStatutory(true);
        employee.setOwnerPercent(new Double("50"));

        employee.setStateAllowances(new Integer(1));
        employee.setStateFilingStatus(QBFilingStatusEnum.HEADOF_HOUSEHOLD);
        
        return employee;
    }

    /**
     * Converts an IEMP to a QBEmployee
     * @param iemp The IEMP to convert
     * @param sourceCompanyId The source company id
     * @return The IEMP converted to a QBEmployee
     */
    public static QBEmployee createQBEmployee(IEMP iemp, String sourceCompanyId) {
        QBEmployee employee = new QBEmployee();

        employee.setSourceCompanyId(sourceCompanyId);
        employee.setSourceEmployeeId(iemp.getIEMPID());
        employee.setOfxEmployeeId(iemp.getIEMPID());
        employee.setSocialSecurityNumber(iemp.getISSN());
        employee.setFirstName(iemp.getIADDRINFO().getIFIRST());
        employee.setMiddleName(iemp.getIADDRINFO().getIMI());
        employee.setLastName(iemp.getIADDRINFO().getILAST());
        employee.setSuffix("");
        employee.setActive(true);

        employee.setLiveAddress(new QBAddress());
        employee.getLiveAddress().setAddressLine1(iemp.getIADDRINFO().getIADDR1());
        employee.getLiveAddress().setAddressLine2(iemp.getIADDRINFO().getIADDR2());
        employee.getLiveAddress().setAddressLine3("");
        employee.getLiveAddress().setCity(iemp.getIADDRINFO().getICITY());
        employee.getLiveAddress().setState(iemp.getIADDRINFO().getISTATE());
        employee.getLiveAddress().setZipCode(iemp.getIADDRINFO().getIPOSTALCODE());
        employee.getLiveAddress().setZipCodeExtension("");
        employee.getLiveAddress().setCountry("US");

        employee.setPhoneNumber(iemp.getIADDRINFO().getIPHONE());
        employee.setEmailAddress(iemp.getIADDRINFO().getIEMAIL());
        employee.setFedAllowances(new Integer(1));
        employee.setFedFilingStatus(QBFilingStatusEnum.HEADOF_HOUSEHOLD);
        employee.setHasRetirementPlan(true);
        employee.setHasThirdPartySickPay(true);

        ICUSTOMFLD birthdayfld;

        if ((birthdayfld = getBirthday(iemp)) != null && birthdayfld.getIFLDVALUE() != null) {
            SpcfCalendar spcfCalendar = SpcfCalendar.parse("MM/dd/yyyy", birthdayfld.getIFLDVALUE());
            employee.setBirthDate(createQBDate(spcfCalendar));
        } else {
            employee.setBirthDate(null);
        }

        if (iemp.getIDTHIRE() != null) {
            SpcfCalendar spcfCalendar = SpcfCalendar.parse("yyyyMMdd", iemp.getIDTHIRE());
            employee.setHireDate(createQBDate(spcfCalendar));
        } else {
            employee.setHireDate(null);
        }
        
        employee.setFamilyMember(true);
        employee.setHighlyCompensatedEmployee(true);
        employee.setStatutory(true);
        employee.setOwnerPercent(new Double("50"));

        employee.setStateAllowances(new Integer(1));
        employee.setStateFilingStatus(QBFilingStatusEnum.HEADOF_HOUSEHOLD);

        return employee;
    }

    /**
     * Gets the birthday custom field for an IEMP
     * @param iemp The IEMP to find the birthday for
     * @return The birthday custom field for the IEMP or null
     */
    public static ICUSTOMFLD getBirthday(IEMP iemp) {
        for (ICUSTOMFLD icustomfld : iemp.getICUSTOMFLD()) {
            if (icustomfld.getIFLDNAME().equals("Birthday")) {
                return icustomfld;
            }
        }

        return null;
    }

    public static QBCompany createQBCompany(String sourceCompanyId) {
        QBCompany c = new QBCompany();
        c.setFein("123456789");
        c.setSourceCompanyId(sourceCompanyId);
        c.setLegalName("LegalName");
        c.setApplicationId("ApplicationId");
        c.setQBVersion("20.00.B.08");
        c.setTaxTableId("TaxTableId");

        c.setLegalAddress(new QBAddress());
        c.getLegalAddress().setAddressLine1("Legal Address Line 1");
        c.getLegalAddress().setAddressLine2("Legal Address Line 2");
        c.getLegalAddress().setAddressLine3("Legal Address Line 3");
        c.getLegalAddress().setCity("LegalCity");
        c.getLegalAddress().setState("NV");
        c.getLegalAddress().setZipCode("89511");
        c.getLegalAddress().setZipCodeExtension("1234");
        c.getLegalAddress().setCountry("US");
        return c;
    }

    public static QBCompany createQBCompany(Company pCompany) {
        QBCompany c = new QBCompany();
        c.setFein(pCompany.getFedTaxId());
        c.setSourceCompanyId(pCompany.getSourceCompanyId());
        c.setLegalName(pCompany.getLegalName());
        c.setApplicationId(pCompany.getQuickbooksInfo().getApplicationId());
        c.setQBVersion(pCompany.getQuickbooksInfo().getApplicationVersion());
        c.setTaxTableId(pCompany.getQuickbooksInfo().getTaxTableId());

        if (pCompany.getLegalAddress() != null) {
            c.setLegalAddress(new QBAddress());
            c.getLegalAddress().setAddressLine1(pCompany.getLegalAddress().getAddressLine1());
            c.getLegalAddress().setAddressLine2(pCompany.getLegalAddress().getAddressLine2());
            c.getLegalAddress().setAddressLine3(pCompany.getLegalAddress().getAddressLine3());
            c.getLegalAddress().setCity(pCompany.getLegalAddress().getCity());
            c.getLegalAddress().setState(pCompany.getLegalAddress().getState());
            c.getLegalAddress().setZipCode(pCompany.getLegalAddress().getZipCode());
            c.getLegalAddress().setZipCodeExtension(pCompany.getLegalAddress().getZipCodeExtension());
            c.getLegalAddress().setCountry(pCompany.getLegalAddress().getCountry());
        }

        QBBankAccount qbBankAccount = null;
        CompanyBankAccount companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(pCompany);
        if (companyBankAccount != null) {
            qbBankAccount = createQBBankAccount(companyBankAccount);
        } else {
            qbBankAccount = createQBBankAccount();
        }
        c.setBankAccount(qbBankAccount);

        return c;
    }

    public static QBBankAccount createQBBankAccount() {
        QBBankAccount ba = new QBBankAccount();
        ba.setAccountId("TEST_ACCT_1");
        ba.setName("TEST_ACCT_1_NAME");
        return ba;
    }

    public static QBBankAccount createQBBankAccount(CompanyBankAccount pCompanyBankAccount) {
        QBBankAccount qbBankAccount = new QBBankAccount();

        if (pCompanyBankAccount != null) {
            qbBankAccount.setAccountId(pCompanyBankAccount.getSourceBankAccountId());
            qbBankAccount.setName(pCompanyBankAccount.getSourceBankAccountName());
        }

        return qbBankAccount;
    }

    public static QBDetailTypes createDetailTypes() {
        QBDetailTypes qbDetailTypes = new QBDetailTypes();
        List<QBBaseItemType> detailTypes = qbDetailTypes.getDetailType();

        detailTypes.add(createBaseItemType(1, "Pre-Tax Employee-Paid Health"));
        detailTypes.add(createBaseItemType(2, "Mileage Reimbursement"));
        detailTypes.add(createBaseItemType(3, "Double-Time Hourly"));
        detailTypes.add(createBaseItemType(4, "Paycheck Tips"));
        detailTypes.add(createBaseItemType(5, "Bonus"));
        detailTypes.add(createBaseItemType(6, "401(k)"));
        detailTypes.add(createBaseItemType(7, "Pre-Tax Employee-Paid Dental"));
        detailTypes.add(createBaseItemType(8, "Pre-Tax Employee-Paid Vision"));
        detailTypes.add(createBaseItemType(9, "Pre-Tax Employee-Paid Ins."));
        detailTypes.add(createBaseItemType(10, "After-Tax Employee-Paid Health"));
        detailTypes.add(createBaseItemType(11, "After-Tax Employee-Paid Dental"));
        detailTypes.add(createBaseItemType(12, "Hourly"));
        detailTypes.add(createBaseItemType(13, "Salary"));
        detailTypes.add(createBaseItemType(14, "401(k) Company Match"));
        detailTypes.add(createBaseItemType(15, "Piecework"));
        detailTypes.add(createBaseItemType(16, "Employee-Paid Local Tax"));
        detailTypes.add(createBaseItemType(17, "Company-Paid Local Tax"));
        detailTypes.add(createBaseItemType(18, "Vacation Accrual"));
        detailTypes.add(createBaseItemType(19, "Vacation Taken"));
        detailTypes.add(createBaseItemType(20, "Sick Accrual"));
        detailTypes.add(createBaseItemType(21, "Sick Taken"));
        detailTypes.add(createBaseItemType(22, "Unpaid Leave"));
        detailTypes.add(createBaseItemType(23, "Sick Pay Out"));
        detailTypes.add(createBaseItemType(24, "Vacation Pay Out"));
        detailTypes.add(createBaseItemType(25, "Commission"));
        detailTypes.add(createBaseItemType(26, "Overtime Hourly"));
        detailTypes.add(createBaseItemType(27, "After-Tax Employee-Paid Vision"));
        detailTypes.add(createBaseItemType(28, "After-Tax Employee-Paid Ins"));
        detailTypes.add(createBaseItemType(29, "Taxable Group-Term Life"));
        detailTypes.add(createBaseItemType(30, "S Corp. Medical Benefit"));
        detailTypes.add(createBaseItemType(31, "Dependent Care FSA"));
        detailTypes.add(createBaseItemType(32, "Medical Care FSA"));
        detailTypes.add(createBaseItemType(33, "Generic After-Tax Deduction"));
        detailTypes.add(createBaseItemType(34, "Company-Paid Insurance"));
        detailTypes.add(createBaseItemType(35, "403(b"));
        detailTypes.add(createBaseItemType(36, "408(k)(6) SEP"));
        detailTypes.add(createBaseItemType(37, "Simple IRA"));
        detailTypes.add(createBaseItemType(38, "403(b) Company Match"));
        detailTypes.add(createBaseItemType(39, "408(k)(6)SEP Company Match"));
        detailTypes.add(createBaseItemType(40, "Simple IRA Company Match"));
        detailTypes.add(createBaseItemType(41, "Cash Tips"));
        detailTypes.add(createBaseItemType(42, "Cash Tips Out"));
        detailTypes.add(createBaseItemType(43, "Cash Advance Repayment"));
        detailTypes.add(createBaseItemType(44, "Cash Advance"));
        detailTypes.add(createBaseItemType(45, "Generic Reimbursement"));
        detailTypes.add(createBaseItemType(46, "Taxable Fringe Benefit"));
        detailTypes.add(createBaseItemType(47, "Taxable Fringe Benefit Out"));
        detailTypes.add(createBaseItemType(48, "Allocated Tips"));
        detailTypes.add(createBaseItemType(50, "Wage Garnishment"));
        detailTypes.add(createBaseItemType(51, "Union Dues"));
        detailTypes.add(createBaseItemType(52, "Charity Donation"));
        detailTypes.add(createBaseItemType(56, "Non-Taxable Group-Term Life"));
        detailTypes.add(createBaseItemType(57, "HSA Co. (Nontaxable"));
        detailTypes.add(createBaseItemType(58, "HSA Emp. (Taxable"));
        detailTypes.add(createBaseItemType(59, "Company-Paid Dental"));
        detailTypes.add(createBaseItemType(60, "Company-Paid Health"));
        detailTypes.add(createBaseItemType(61, "Company-Paid Vision"));
        detailTypes.add(createBaseItemType(63, "Company-paid Dependent Care FSA"));
        detailTypes.add(createBaseItemType(64, "Company-paid Medical Care FSA"));
        detailTypes.add(createBaseItemType(65, "Adoption Benefit"));
        detailTypes.add(createBaseItemType(66, "501(c)(18)(D"));
        detailTypes.add(createBaseItemType(67, "457 Plan"));
        detailTypes.add(createBaseItemType(68, "457 Plan Distribution"));
        detailTypes.add(createBaseItemType(69, "Non-Qualified Plan Distribution"));
        detailTypes.add(createBaseItemType(70, "Non-Taxable Sick Pay"));
        detailTypes.add(createBaseItemType(71, "Qualified Moving Expense"));
        detailTypes.add(createBaseItemType(72, "Company-Paid Other (W2 Box 14"));
        detailTypes.add(createBaseItemType(73, "Other (W2 Box 14) Deduction"));
        detailTypes.add(createBaseItemType(74, "Other (W2 Box 14) Payment"));
        detailTypes.add(createBaseItemType(75, "Other Moving Expense Reimbursement"));
        detailTypes.add(createBaseItemType(76, "Other Moving Expense"));
        detailTypes.add(createBaseItemType(77, "Other Moving Expense Out"));
        detailTypes.add(createBaseItemType(78, "Employee-Paid Custom Tax"));
        detailTypes.add(createBaseItemType(79, "Company-Paid Custom Tax"));
        detailTypes.add(createBaseItemType(84, "Roth 401(k"));
        detailTypes.add(createBaseItemType(85, "Roth 403(b"));
        detailTypes.add(createBaseItemType(86, "Other Pre-Tax Deduction (W2 Box 14"));
        detailTypes.add(createBaseItemType(87, "HSA Co. (Taxable"));
        detailTypes.add(createBaseItemType(88, "HSA Emp. (Pretax"));
        return qbDetailTypes;
    }

    public static QBSpecialPayrollItemTypes createSpecialItemTypes() {
        QBSpecialPayrollItemTypes qbSpecialItemTypes = new QBSpecialPayrollItemTypes();
        List<QBBaseItemType> specialItemTypes = qbSpecialItemTypes.getSpecialPayrollItemType();
        specialItemTypes.add(createBaseItemType(0, "NONE"));
        specialItemTypes.add(createBaseItemType(1, "COMCARE"));
        specialItemTypes.add(createBaseItemType(2, "COSSEC"));
        specialItemTypes.add(createBaseItemType(3, "EEMCARE"));
        specialItemTypes.add(createBaseItemType(4, "EESSEC"));
        specialItemTypes.add(createBaseItemType(5, "FEDTAX"));
        specialItemTypes.add(createBaseItemType(6, "FUTA"));
        specialItemTypes.add(createBaseItemType(7, "SALARY"));
        specialItemTypes.add(createBaseItemType(8, "SICK_SALARY"));
        specialItemTypes.add(createBaseItemType(9, "VAC_SALARY"));
        specialItemTypes.add(createBaseItemType(10, "SICK_HOURLY"));
        specialItemTypes.add(createBaseItemType(11, "VAC_HOURLY"));
        specialItemTypes.add(createBaseItemType(12, "AEIC"));
        specialItemTypes.add(createBaseItemType(13, "DIRDEP"));
        specialItemTypes.add(createBaseItemType(14, "WORKERS_COMP"));
        specialItemTypes.add(createBaseItemType(32767, "DUMMY"));
        return qbSpecialItemTypes;
    }

    public static QBPayrollItemTypes createPayrollItemTypes() {
        QBPayrollItemTypes qbPayrollItemTypes = new QBPayrollItemTypes();
        List<QBBaseItemType> payrollItemTypes = qbPayrollItemTypes.getPayrollItemType();

        payrollItemTypes.add(createBaseItemType(0, "SALARY"));
        payrollItemTypes.add(createBaseItemType(1, "HOURLY"));
        payrollItemTypes.add(createBaseItemType(2, "COMMISSION"));
        payrollItemTypes.add(createBaseItemType(3, "ADDITION"));
        payrollItemTypes.add(createBaseItemType(4, "DEDUCTION"));
        payrollItemTypes.add(createBaseItemType(5, "COMPANY"));
        payrollItemTypes.add(createBaseItemType(6, "FEDTAX"));
        payrollItemTypes.add(createBaseItemType(7, "STATETAX"));
        payrollItemTypes.add(createBaseItemType(8, "SDI"));
        payrollItemTypes.add(createBaseItemType(9, "SUI"));
        payrollItemTypes.add(createBaseItemType(10, "LOCALTAX"));
        payrollItemTypes.add(createBaseItemType(11, "DIRDEP"));
        payrollItemTypes.add(createBaseItemType(12, "BONUS"));
        payrollItemTypes.add(createBaseItemType(13, "CUSTOM"));
        payrollItemTypes.add(createBaseItemType(14, "WAGES"));
        payrollItemTypes.add(createBaseItemType(15, "LIABILITY"));
        payrollItemTypes.add(createBaseItemType(16, "NONWAGES"));
        payrollItemTypes.add(createBaseItemType(17, "EMPCUSTOM"));
        payrollItemTypes.add(createBaseItemType(18, "CUSTOM_OTHER"));
        payrollItemTypes.add(createBaseItemType(19, "SALARY_HOURLY"));
        payrollItemTypes.add(createBaseItemType(20, "COMM_BONUS"));
        payrollItemTypes.add(createBaseItemType(21, "OVERTIME"));

        return qbPayrollItemTypes;
    }

    public static QBTaxTrackingTypes createTaxTrackingTypes() {
        QBTaxTrackingTypes qbTaxTrackingTypes = new QBTaxTrackingTypes();
        List<QBBaseItemType> taxTrackingTypes = qbTaxTrackingTypes.getTaxTrackingType();

        taxTrackingTypes.add(createBaseItemType(0, "None"));
        taxTrackingTypes.add(createBaseItemType(1, "Compensation"));
        taxTrackingTypes.add(createBaseItemType(2, "Non-taxable Sick Pay"));
        taxTrackingTypes.add(createBaseItemType(3, "Other"));
        taxTrackingTypes.add(createBaseItemType(4, "Reported Tips"));
        taxTrackingTypes.add(createBaseItemType(5, "Advance EIC Payment"));
        taxTrackingTypes.add(createBaseItemType(6, "Dependent Care FSA"));
        taxTrackingTypes.add(createBaseItemType(48, "Co. Paid Dep. Care"));
        taxTrackingTypes.add(createBaseItemType(7, "SEC 457 Distribution"));
        taxTrackingTypes.add(createBaseItemType(8, "Non-qual. Plan Distr"));
        taxTrackingTypes.add(createBaseItemType(9, "Fringe Benefits"));
        taxTrackingTypes.add(createBaseItemType(10, "Other Moving Expense"));
        taxTrackingTypes.add(createBaseItemType(11, "401(k"));
        taxTrackingTypes.add(createBaseItemType(57, "Roth 401(k"));
        taxTrackingTypes.add(createBaseItemType(12, "403(b"));
        taxTrackingTypes.add(createBaseItemType(58, "Roth 403(b"));
        taxTrackingTypes.add(createBaseItemType(13, "408(k)(6) SEP"));
        taxTrackingTypes.add(createBaseItemType(14, "Elective 457(b"));
        taxTrackingTypes.add(createBaseItemType(15, "501(c)(18)(D"));
        taxTrackingTypes.add(createBaseItemType(17, "Local Income Tax"));
        taxTrackingTypes.add(createBaseItemType(19, "Loc. 2 Income Tax"));
        taxTrackingTypes.add(createBaseItemType(55, "Secondary Loc. Tax"));
        taxTrackingTypes.add(createBaseItemType(49, "Co. Paid Other Tax"));
        taxTrackingTypes.add(createBaseItemType(20, "Federal"));
        taxTrackingTypes.add(createBaseItemType(1022));
        taxTrackingTypes.add(createBaseItemType(22, "SS Tax"));
        taxTrackingTypes.add(createBaseItemType(3022, "SS Tips"));
        taxTrackingTypes.add(createBaseItemType(1023));
        taxTrackingTypes.add(createBaseItemType(23, "Medicare"));
        taxTrackingTypes.add(createBaseItemType(1026));
        taxTrackingTypes.add(createBaseItemType(26, "SWH"));
        taxTrackingTypes.add(createBaseItemType(27, "SDI"));
        taxTrackingTypes.add(createBaseItemType(28, "SUI"));
        taxTrackingTypes.add(createBaseItemType(1017, "Local 1"));
        taxTrackingTypes.add(createBaseItemType(1019, "Local 2"));
        taxTrackingTypes.add(createBaseItemType(1055, "Local 3"));
        taxTrackingTypes.add(createBaseItemType(29, "Qual. Moving Expense"));
        taxTrackingTypes.add(createBaseItemType(1031));
        taxTrackingTypes.add(createBaseItemType(31));
        taxTrackingTypes.add(createBaseItemType(40, "Comp. SS Tax"));
        taxTrackingTypes.add(createBaseItemType(41, "Comp. Medicare"));
        taxTrackingTypes.add(createBaseItemType(42, "Comp. SDI"));
        taxTrackingTypes.add(createBaseItemType(43, "Comp. SUI"));
        taxTrackingTypes.add(createBaseItemType(44, "FUTA"));
        taxTrackingTypes.add(createBaseItemType(45, "Co. Paid Med Savngs"));
        taxTrackingTypes.add(createBaseItemType(46, "SIMPLE IRA"));
        taxTrackingTypes.add(createBaseItemType(47, "Adoption Benefits"));
        taxTrackingTypes.add(createBaseItemType(50, "Taxable Grp Trm Life"));
        taxTrackingTypes.add(createBaseItemType(51, "Allocated Tips"));
        taxTrackingTypes.add(createBaseItemType(52, "Med Care Flex Spend"));
        taxTrackingTypes.add(createBaseItemType(53, "Premium Only/125"));
        taxTrackingTypes.add(createBaseItemType(54, "SCorp Pd Med Premium"));
        taxTrackingTypes.add(createBaseItemType(56, "HSA � DO NOT USE"));
        taxTrackingTypes.add(createBaseItemType(60, "HSA Co. (Taxable"));
        taxTrackingTypes.add(createBaseItemType(61, "HSA Co. (Nontaxable"));
        taxTrackingTypes.add(createBaseItemType(62, "HSA Emp. (Taxable"));
        taxTrackingTypes.add(createBaseItemType(63, "HSA Emp. (Pretax"));
        taxTrackingTypes.add(createBaseItemType(1027));
        taxTrackingTypes.add(createBaseItemType(1028));
        taxTrackingTypes.add(createBaseItemType(1042));
        taxTrackingTypes.add(createBaseItemType(1043));
        taxTrackingTypes.add(createBaseItemType(2000));
        taxTrackingTypes.add(createBaseItemType(2001));
        taxTrackingTypes.add(createBaseItemType(2002));
        taxTrackingTypes.add(createBaseItemType(2003));
        taxTrackingTypes.add(createBaseItemType(2004));
        taxTrackingTypes.add(createBaseItemType(2005));
        taxTrackingTypes.add(createBaseItemType(2006));
        taxTrackingTypes.add(createBaseItemType(2019));
        taxTrackingTypes.add(createBaseItemType(2007));
        taxTrackingTypes.add(createBaseItemType(2008));
        taxTrackingTypes.add(createBaseItemType(2009));
        taxTrackingTypes.add(createBaseItemType(2010));
        taxTrackingTypes.add(createBaseItemType(2011));
        taxTrackingTypes.add(createBaseItemType(2012));
        taxTrackingTypes.add(createBaseItemType(2013));
        taxTrackingTypes.add(createBaseItemType(2014));
        taxTrackingTypes.add(createBaseItemType(2015));
        taxTrackingTypes.add(createBaseItemType(1040));
        taxTrackingTypes.add(createBaseItemType(1041));
        taxTrackingTypes.add(createBaseItemType(1020));
        taxTrackingTypes.add(createBaseItemType(1044));
        taxTrackingTypes.add(createBaseItemType(1005));
        taxTrackingTypes.add(createBaseItemType(2016));
        taxTrackingTypes.add(createBaseItemType(2017));
        taxTrackingTypes.add(createBaseItemType(2018));

        return qbTaxTrackingTypes;
    }

    public static QBBaseItemType createBaseItemType(int pDetailTypeId) {
        return createBaseItemType(pDetailTypeId, null);
    }

    public static QBBaseItemType createBaseItemType(int pDetailTypeId, String name) {
        QBBaseItemType detailType = new QBBaseItemType();
        detailType.setID(Integer.toString(pDetailTypeId));
        detailType.setName(name);
        return detailType;
    }

    public static void updatePayrollItems(OFX ofx, SubmitPayrollRequest pRequest){
        List<IPITEM> ipitems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        List<QBPayrollItem> qbPayrollItems = pRequest.getPayrollItemList().getPayrollItem();
        for (IPITEM ipitem : ipitems) {
            com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem payrollItem = new com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem(ipitem);
            if(payrollItem.isTaxItem()){
                QBPayrollItem qbPayrollItem = createPayrollItem(QBPayrollItemCategory.TAX_ITEM, QBOFX.mapOFXStringToInt(ipitem.getIPITEMID()));
                qbPayrollItem.setDetailTypeId(SourceSystemLawAssoc.findLawBySourceSystemAndSourceId(SourceSystemCode.QBDT, payrollItem.getSourceLawId()).getLawId());
                qbPayrollItems.add(qbPayrollItem);
            }
        }
    }

    public static void createPayrollItems(OFX ofx, SubmitPayrollRequest pRequest){
        List<IPITEM> ipitems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        pRequest.setPayrollItemList(new QBPayrollItems());
        List<QBPayrollItem> qbPayrollItems = pRequest.getPayrollItemList().getPayrollItem();

        for (IPITEM ipitem : ipitems) {
            com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem payrollItem = new com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem(ipitem);
            if(payrollItem.isTaxItem()){
                QBPayrollItem qbPayrollItem = createPayrollItem(QBPayrollItemCategory.TAX_ITEM, QBOFX.mapOFXStringToInt(ipitem.getIPITEMID()));
                qbPayrollItem.setDetailTypeId(SourceSystemLawAssoc.findLawBySourceSystemAndSourceId(SourceSystemCode.QBDT, payrollItem.getSourceLawId()).getLawId());
                qbPayrollItems.add(qbPayrollItem);
            }else{
                if(payrollItem.getPayrollItemType().equals(PayrollItemType.Deduction)){
                    QBPayrollItem qbPayrollItem = createPayrollItem(QBPayrollItemCategory.PRE_TAX_ITEM, QBOFX.mapOFXStringToInt(ipitem.getIPITEMID()));
                    qbPayrollItems.add(qbPayrollItem);

                    if (ipitem.getIDEDUCTITEM() != null && ipitem.getIDEDUCTITEM().getICOMPID().equals("401K") &&
                            ipitem.getIDEDUCTITEM().getITAXFORMLINE().equals("401K")) {
                        // This is a 401K item, manually add agency number back in because it is null otherwise
                        qbPayrollItem.setAgencyNumber("401K");
                        qbPayrollItem.setTaxTrackingTypeId(QBDTWSSubmitPayrollRequestProcess.TAX_TRACKING_TYPE_401K);
                    }
                } else if(payrollItem.getPayrollItemType().equals(PayrollItemType.Compensation)){
                    qbPayrollItems.add(createPayrollItem(QBPayrollItemCategory.EARNING_ITEM, QBOFX.mapOFXStringToInt(ipitem.getIPITEMID())));
                } else if(payrollItem.getPayrollItemType().equals(PayrollItemType.EmployerContribution)){
                    qbPayrollItems.add(createPayrollItem(QBPayrollItemCategory.TAX_COMPANY_ITEM, QBOFX.mapOFXStringToInt(ipitem.getIPITEMID())));
                }
            }
        }
    }

    public static QBPaychecks createQBPaycheckFromOFX(OFX ofx, String checkNumber){
        QBPaychecks qbPaychecks = new QBPaychecks();
        List<IPAYROLLRUN> iPayrollRuns = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN();
        List<IPITEM> ipitems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        
        for (IPAYROLLRUN iPayrollRun : iPayrollRuns) {
            for (IPAYCHK ipaychk : iPayrollRun.getIPAYCHK()) {
                QBPaycheck qbPaycheck = getQbPaycheck(checkNumber, ipitems, ipaychk);

                if(QBOFX.mapOFXStringToBoolean(ipaychk.getIVOID())){
                    qbPaycheck.setOperation(QBPaycheckOperationEnum.VOID);
                } else {
                    qbPaycheck.setOperation(QBPaycheckOperationEnum.ADD);
                }

                qbPaychecks.getPaycheck().add(qbPaycheck);                
            }
            for (IPAYCHK ipaychk : iPayrollRun.getIPAYCHKMOD()) {
                QBPaycheck qbPaycheck = getQbPaycheck(checkNumber, ipitems, ipaychk);                
                if(QBOFX.mapOFXStringToBoolean(ipaychk.getIVOID())){
                    qbPaycheck.setOperation(QBPaycheckOperationEnum.VOID);
                } else {
                    qbPaycheck.setOperation(QBPaycheckOperationEnum.EDIT);
                }
                qbPaychecks.getPaycheck().add(qbPaycheck);
            }
        }
        return qbPaychecks;
    }

    private static QBPaycheck getQbPaycheck(String checkNumber, List<IPITEM> ipitems, IPAYCHK ipaychk) {
        QBPaycheck qbPaycheck = new QBPaycheck();
        qbPaycheck.setCheckNumber(checkNumber);
        qbPaycheck.setPaycheckID(ipaychk.getIPAYCHKID());
        if (ipaychk.getIEMPID() != null) {
            qbPaycheck.setPaycheckID(ipaychk.getIPAYCHKID() + "0000");
            qbPaycheck.setOfxPaycheckID(ipaychk.getIPAYCHKID());
        }
        qbPaycheck.setEmployeeID(ipaychk.getIEMPID());
        qbPaycheck.setOperation(QBPaycheckOperationEnum.EDIT);
        qbPaycheck.setPayDate(createQBDate(CalendarUtils.convertToSpcfCalendar(QBOFX.mapOFXStringToDate(ipaychk.getIDTTX()))));
        qbPaycheck.setPeriodStartDate(createQBDate(CalendarUtils.convertToSpcfCalendar(QBOFX.mapOFXStringToDate(ipaychk.getIDTPAYPDBEGIN()))));
        qbPaycheck.setPeriodEndDate(createQBDate(CalendarUtils.convertToSpcfCalendar(QBOFX.mapOFXStringToDate(ipaychk.getIDTPAYPDEND()))));
        if (ipaychk.getIAMT() != null){
            qbPaycheck.setNetPay(SpcfUtils.convertToBigDecimal(QBOFX.mapOFXStringToMoney(ipaychk.getIAMT())).multiply(new BigDecimal("-1")));
        } else{
            qbPaycheck.setNetPay(new BigDecimal(0));
        }

        qbPaycheck.setGrossPay(new BigDecimal(0));
        qbPaycheck.setYTDGrossPay(new BigDecimal(0));
        qbPaycheck.setYTDNetPay(new BigDecimal(0));

        for (ITAXLINE itaxline : ipaychk.getITAXLINE()) {
            IPITEM ipitem = findIPITEM(ipitems, itaxline.getIPITEMID());
            com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem payrollItem = new com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem(ipitem);
            if(payrollItem.isTaxItem()){
                QBPaycheckLineTaxItem qbPaycheckLineTaxItem = new QBPaycheckLineTaxItem();
                qbPaycheckLineTaxItem.setPayrollItemId(itaxline.getIPITEMID());
                if(payrollItem.getIsEmployeePaid()){
                    qbPaycheckLineTaxItem.setCurrent(getOFXAmount(itaxline.getIAMT()));
                    qbPaycheckLineTaxItem.setYTD(getOFXAmount(itaxline.getIYTDAMT()));
                } else {    
                    qbPaycheckLineTaxItem.setCurrent(getOFXAmount(itaxline.getIAMT()).multiply(new BigDecimal("-1")));
                    qbPaycheckLineTaxItem.setYTD(getOFXAmount(itaxline.getIYTDAMT()).multiply(new BigDecimal("-1")));
                }
                qbPaycheckLineTaxItem.setIncomeSubjectToTax(SpcfUtils.convertToBigDecimal(QBOFX.mapOFXStringToMoney(itaxline.getITAXABLEWAGE())));
                qbPaycheckLineTaxItem.setEmployeePaid(payrollItem.getIsEmployeePaid());
                qbPaycheckLineTaxItem.setWageBase(SpcfUtils.convertToBigDecimal(QBOFX.mapOFXStringToMoney(itaxline.getIWB())));
                qbPaycheck.getTaxItems().add(qbPaycheckLineTaxItem);
            }
        }

        for (ISALARYLINE isalaryline : ipaychk.getISALARYLINE()) {
            QBEarningItem qbEarningItem = new QBEarningItem();
            qbEarningItem.setPayrollItemId(isalaryline.getIPITEMID());
            qbEarningItem.setName(isalaryline.getIITEM());
            qbEarningItem.setCurrent(SpcfUtils.convertToBigDecimal(QBOFX.mapOFXStringToMoney(isalaryline.getIAMT())));
            qbEarningItem.setQty(new BigDecimal(isalaryline.getIHRS()));
            qbEarningItem.setRate(SpcfUtils.convertToBigDecimal(QBOFX.mapOFXStringToMoney(isalaryline.getIRATE())));
            qbEarningItem.setYTD(SpcfUtils.convertToBigDecimal(QBOFX.mapOFXStringToMoney(isalaryline.getIYTDAMT())));
            qbPaycheck.getEarningItems().add(qbEarningItem);
        }
        // ADJLINE
        for (IADJLINE iadjline : ipaychk.getIADJLINE()) {
            IPITEM ipitem = findIPITEM(ipitems, iadjline.getIPITEMID());
            updateQBPaycheck(qbPaycheck, ipitem, iadjline);
        }
        //DDLINE
        for (IDDLINE iddline : ipaychk.getIDDLINE()) {
            QBDirectDepositItem qbDirectDepositItem = new QBDirectDepositItem();
            qbDirectDepositItem.setPayrollItemId(iddline.getIPITEMID());
            qbDirectDepositItem.setName(iddline.getIPITEMNAME());
            IDDACCT iddacct = iddline.getIDDACCT();
            if(iddacct != null){
                qbDirectDepositItem.setBankAccountName(iddacct.getIACCTNAME());
                qbDirectDepositItem.setCurrent(SpcfUtils.convertToBigDecimal(QBOFX.mapOFXStringToMoney(iddline.getIAMT())).negate());
                BANKACCT bankacct = iddacct.getBANKACCTTO();
                if(bankacct != null){
                    qbDirectDepositItem.setBankAccountNumber(bankacct.getACCTID());
                    // ACCTTYPE can come in all caps but should be regular English capitalization
                    qbDirectDepositItem.setBankAccountType(QBBankAccountTypeEnum.fromValue(WordUtils.capitalize(bankacct.getACCTTYPE().toLowerCase())));
                    qbDirectDepositItem.setBankRoutingNumber(bankacct.getBANKID());
                }
            }
            qbPaycheck.getDDItems().add(qbDirectDepositItem);
        }
        return qbPaycheck;
    }

    /**
     * Removes the dollars sign and creates a BigDecimal object with the new amount
     * @param amount The OFX amount
     * @return A BigDecimal object initialized to the amount
     */
    public static BigDecimal getOFXAmount(String amount) {
        return new BigDecimal(amount.replaceAll("\\$", ""));
    }

    private static IPITEM findIPITEM(List<IPITEM> pIpitems, String pIpitemId){
        for (IPITEM pIpitem : pIpitems) {
            if(pIpitem.getIPITEMID().equals(pIpitemId)){
                return pIpitem;
            }
        }
        return null;
    }

    private static void updateQBPaycheck(QBPaycheck pQbPaycheck, IPITEM pIpitem, IADJLINE pIadjline){
        if(pIpitem.getIDEDUCTITEM() != null){
            QBPaycheckLineItem qbPaycheckLineItem = new QBPaycheckLineItem();
            qbPaycheckLineItem.setCurrent(SpcfUtils.convertToBigDecimal(QBOFX.mapOFXStringToMoney(pIadjline.getIAMT())));
            qbPaycheckLineItem.setPayrollItemId(pIadjline.getIPITEMID());
            qbPaycheckLineItem.setYTD(SpcfUtils.convertToBigDecimal(QBOFX.mapOFXStringToMoney(pIadjline.getIYTDAMT())));
            if(pIpitem.getIDEDUCTITEM().getITAXAFFECTED().size() > 0){
                pQbPaycheck.getPreTaxItems().add(qbPaycheckLineItem);                
            } else {
                pQbPaycheck.getAdjNetPayItems().add(qbPaycheckLineItem);
            }
        } else if(pIpitem.getICONTRIBITEM() != null){
            if(pIpitem.getICONTRIBITEM().getITAXAFFECTED().size() > 0){
                QBPaycheckLineTaxCompanyItem qbPaycheckLineTaxCompanyItem = new QBPaycheckLineTaxCompanyItem();
                qbPaycheckLineTaxCompanyItem.setCurrent(SpcfUtils.convertToBigDecimal(QBOFX.mapOFXStringToMoney(pIadjline.getIAMT())));
                qbPaycheckLineTaxCompanyItem.setYTD(SpcfUtils.convertToBigDecimal(QBOFX.mapOFXStringToMoney(pIadjline.getIYTDAMT())));
                qbPaycheckLineTaxCompanyItem.setPayrollItemId(pIadjline.getIPITEMID());
                pQbPaycheck.getTaxCompanyItems().add(qbPaycheckLineTaxCompanyItem);
            } else {
                QBPaycheckLineItem qbPaycheckLineItem = new QBPaycheckLineItem();
                qbPaycheckLineItem.setCurrent(SpcfUtils.convertToBigDecimal(QBOFX.mapOFXStringToMoney(pIadjline.getIAMT())));
                qbPaycheckLineItem.setPayrollItemId(pIadjline.getIPITEMID());
                qbPaycheckLineItem.setYTD(SpcfUtils.convertToBigDecimal(QBOFX.mapOFXStringToMoney(pIadjline.getIYTDAMT())));
                pQbPaycheck.getNonTaxCompanyItems().add(qbPaycheckLineItem);
            }
        }
    }
}
