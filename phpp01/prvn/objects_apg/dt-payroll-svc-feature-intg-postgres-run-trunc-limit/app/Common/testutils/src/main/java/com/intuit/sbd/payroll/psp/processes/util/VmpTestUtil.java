package com.intuit.sbd.payroll.psp.processes.util;

import com.intuit.ems.dataservice.v1.exception.DataServiceException;
import com.intuit.ems.dataservice.v1.exception.ResourceNotFoundException;
import com.intuit.ems.dataservice.v1.resource.EmployeeIdentificationParams;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.managers.PayrollEmployeeManager;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.util.CdmHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaystubDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

//Utility methods for testing ViewMyPaycheck

public class VmpTestUtil {
    private static SpcfLogger logger = null;
    private static int payrollRunCount = 0;
    private static int paycheckCount = 0;

    static {
        logger = Application.getLogger(VmpTestUtil.class);
    }

    public static Employee setupCompanyCreateEmployeeWithPaystubAndAssociateWithConsumerRealmId(String psid, String realmId) {
        return setupCompanyCreateEmployeeWithPaystubAndAssociateWithConsumerRealmId(psid, realmId, "1000.00");
    }

    public static Employee setupCompanyCreateEmployeeWithPaystubAndAssociateWithConsumerRealmId(String psid, String realmId, String paystubNetAmount) {
        Employee employee = VmpTestUtil.setupCompanyCreateEmployee(psid);
        try {
            Application.beginUnitOfWork();
            PayrollRun payrollRun = VmpTestUtil.createPayrollRun(employee);
            SpcfCalendar paycheckDate = SpcfCalendar.createInstance(2013, 1, 1);
            VmpTestUtil.createAndSavePaystub(employee, paystubNetAmount, paycheckDate, payrollRun);
            Application.commitUnitOfWork();
        } finally {
            Application.rollbackUnitOfWork();
        }
        VmpTestUtil.associateEmployeeWithRealm(realmId, employee.getTaxId(), "iamEmailAddress@intuit.com", "1000.00");
        return employee;
    }

    public static void associateEmployeeWithRealm(String realmId, String taxId, String iamEmailAddress, String lastPaycheckAmount) {
        associateEmployeeWithRealm(realmId, taxId, iamEmailAddress, lastPaycheckAmount, null);
    }

    public static void associateEmployeeWithRealm(String realmId, String taxId, String iamEmailAddress, String lastPaycheckAmount, String lastName) {
        PayrollEmployeeManager payrollEmployeeManager = new PayrollEmployeeManager();
        EmployeeIdentificationParams employeeIdentificationParams = new EmployeeIdentificationParams();
        employeeIdentificationParams.setLastPaycheckNetAmount(lastPaycheckAmount);
        employeeIdentificationParams.setSsn(taxId);
        employeeIdentificationParams.setIamEmail(iamEmailAddress);
        employeeIdentificationParams.setLastName(lastName);
        payrollEmployeeManager.associateEmployeeWithRealm(realmId, employeeIdentificationParams);
    }

    public static Paystub createPaystub(Employee employee, String paystubAmount, SpcfCalendar paycheckDate) {
        Paystub paystub = null;
        PayrollRun payrollRun = VmpTestUtil.createPayrollRun(employee);
        paystub = VmpTestUtil.createAndSavePaystub(employee, paystubAmount, paycheckDate, payrollRun);
        return paystub;
    }

    public static Employee createEmployee(String ssn, Company company) {
        Employee employee = null;
        EmployeeDTO employeeDTO = DataLoadServices.createEE();
        employeeDTO.setSocialSecurityNumber(ssn);
        employee = DataLoadServices.addEE(company, employeeDTO);
        return employee;
    }

    public static PayrollRun createPayrollRun(Employee employee) {
        PayrollRun payrollRun = new PayrollRun();
        Company company = employee.getCompany();
        payrollRun.setCompany(company);
        payrollRun.setFundingModel(company.getFundingModel().getFundingModelCd());
        payrollRun.setSourcePayRunId("payroll run " + ++payrollRunCount);
        Application.save(payrollRun);
        return payrollRun;
    }

    private static Paystub createAndSavePaystub(Employee employee, String paystubNetPay, SpcfCalendar paycheckDate, PayrollRun payrollRun) {
        Paystub paystub = null;
        SpcfCalendar beginDate = SpcfCalendar.createInstance(2012, 2, 10);
        SpcfCalendar endDate = SpcfCalendar.createInstance(2012, 2, 10);
        Paycheck paycheck = createPaycheck(paystubNetPay, employee, beginDate, endDate, payrollRun);
        try {
            PaystubDTO paystubDto = DataLoadServices.createPaystubDto(employee, beginDate, endDate, paycheckDate, paystubNetPay);
            ProcessResult<Paystub> result = PayrollServices.paystubManager.addPaystub(paycheck, employee, paystubDto);
            if(result.isSuccess()) {
                paystub = result.getResult();
            }
        } catch (Exception e) {
            logger.error("Error creating paystub dto", e);
        }

        return paystub;
    }

    private static Paycheck createPaycheck(String netAmount, Employee employee, SpcfCalendar beginDate, SpcfCalendar endDate, PayrollRun payrollRun) {
        Paycheck paycheck = new Paycheck();
        paycheck.setSourceEmployee(employee);
        paycheck.setSourcePaycheckId("paycheck " + paycheckCount++);
        paycheck.setCompany(employee.getCompany());
        paycheck.setNetAmount(new SpcfMoney(netAmount));
        paycheck.setPayPeriodBeginDate(beginDate);
        paycheck.setPayPeriodEndDate(endDate);
        paycheck.setPayrollRun(payrollRun);
        return Application.save(paycheck);
    }

    public static Paystub findPaystubForCompany(String companyRealmId, String id) {
        Expression<Paystub> query = new Query<Paystub>()
                .Where(Paystub.PstubEmployeeInfo().Employee().Company().IAMRealmId().equalTo(companyRealmId)
                              .And(Paystub.Id().equalTo(CdmHelper.createSpcfUniqueId(id, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND))));
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class, query);

        Paystub paystub = paystubs.getFirst();
        return paystub;
    }

    public static PstubMsg findPaystubForMsg(String id){

        Expression<PstubMsg> query = new Query<PstubMsg>()
                .Where(Paystub.Id().equalTo(CdmHelper.createSpcfUniqueId(id, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND)));
        DomainEntitySet<PstubMsg> pstubsMsg = Application.find(PstubMsg.class, query);

        PstubMsg pstubMsg = pstubsMsg.getFirst();
        return pstubMsg;
    }


    public static PstubDDItem findPstubDDItemForCompany( String id){
        Expression<PstubDDItem> query = new Query<PstubDDItem>().Where(Paystub.Id().equalTo(CdmHelper.createSpcfUniqueId(id, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND)));


        DomainEntitySet<PstubDDItem> pstubDDItems = Application.find(PstubDDItem.class, query);

        PstubDDItem pstubDDItem = pstubDDItems.getFirst();
        return  pstubDDItem;
    }

    public static PstubPaidTimeoffItem findPstubPaidTimeoffItem(String id)
    {
        Expression<PstubPaidTimeoffItem> query = new Query<PstubPaidTimeoffItem>().Where(PstubPaidTimeoffItem.Paystub().Id().equalTo(CdmHelper.createSpcfUniqueId(id, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND)));

        DomainEntitySet<PstubPaidTimeoffItem> pstubPaidTimeoffItems = Application.find(PstubPaidTimeoffItem.class, query);
        PstubPaidTimeoffItem pstubPaidTimeoffItem = pstubPaidTimeoffItems.getFirst();

        return pstubPaidTimeoffItem;
    }

    public static PstubPayItem findPayItem(String id){
        Expression<PstubPayItem> query = new Query<PstubPayItem>().Where(PstubPayItem.Paystub().Id().equalTo(CdmHelper.createSpcfUniqueId(id, DataServiceException.ERRNUM_PAYSTUB_RESOURCE_NOT_FOUND)));

        DomainEntitySet<PstubPayItem> pstubPayItems = Application.find(PstubPayItem.class, query);
        PstubPayItem pstubPayItem = pstubPayItems.getFirst();

        return pstubPayItem;
    }

    public static Employee setupCompanyCreateEmployee(String psid) {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Cloud, ServiceCode.ViewMyPaycheck);
        Expression<Employee> query = new Query<Employee>()
            .Where(Employee.Company().equalTo(company))
            .EagerLoad(Employee.MailingAddress(),
                       Employee.Company(),
                       Employee.Company().CompanyServiceSet());
        DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
        return  employees.getFirst();
    }


    public static Employee setupCompanyWithEinCreateEmployee(String psid, String ein) {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, ein, true, ServiceCode.Cloud, ServiceCode.ViewMyPaycheck);
        Expression<Employee> query = new Query<Employee>()
                .Where(Employee.Company().equalTo(company))
                .EagerLoad(Employee.MailingAddress(),
                           Employee.Company(),
                           Employee.Company().CompanyServiceSet());
        DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
        return  employees.getFirst();
    }
}
