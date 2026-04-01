package com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.util.CdmHelper;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.VmpEmployeeInfo;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.kafka.common.protocol.types.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EmployeeFinder {

    private static SpcfLogger logger = Application.getLogger(EmployeeFinder.class);

    public static List<Employee> findEmployees(String consumerRealmId) {
        logger.info(String.format("v4log findEmployees started consumerRealmId=%s",consumerRealmId));
        Expression<Employee> query = new Query<Employee>()
                        .Where(Employee.ConsumerRealmId().equalTo(consumerRealmId).And(Employee.Company().isNotNull()))
                        .EagerLoad(Employee.MailingAddress());
        DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
        List<Employee> employeesList = new ArrayList<Employee>();
        employeesList.addAll(employees);
        logger.info(String.format("v4log findEmployees finished consumerRealmId=%s numEmployees=%s",consumerRealmId, employeesList.size()));
        return employeesList;
    }

    public static List<VmpEmployeeInfo> findVmpEmployeeInfo(String consumerRealmId){
        logger.info(String.format("v4log findEVmpEmployeeInfo started consumerRealmId=%s",consumerRealmId));
        Expression<VmpEmployeeInfo> query = new Query<VmpEmployeeInfo>()
                .Where(VmpEmployeeInfo.ConsumerRealmId().equalTo(consumerRealmId));
        DomainEntitySet<VmpEmployeeInfo> employees = Application.find(VmpEmployeeInfo.class, query);
        List<VmpEmployeeInfo> employeesList = new ArrayList<VmpEmployeeInfo>();
        employeesList.addAll(employees);
        logger.info(String.format("v4log findEVmpEmployeeInfo finished consumerRealmId=%s numVmpEmployeeInfo=%s",consumerRealmId, employeesList.size()));
        return employeesList;
    }

    public static List<Employee> findEmployeesByTaxId(String taxId) {
        List<Employee> employeesList = new ArrayList<Employee>();
        if(taxId != null) {
            Expression<Employee> query = null;
            List<String> encTaxIdList = EncryptionUtils.deterministicEncryptWithAllKeys(Employee.TaxIdKeyName, taxId);
            query = new Query<Employee>()
                    .Where(Employee.TaxIdEnc().in(encTaxIdList));

            DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
            employeesList.addAll(employees);
        }
        return employeesList;
    }

    public static List<Employee> findEmployeesByCompanyRealm(String companyRealm) {
        Expression<Employee> query = new Query<Employee>()
            .Where(Employee.Company().IAMRealmId().equalTo(companyRealm)
            .And(Employee.SourceEmployeeId().regexpLike("^[0-9]+$"))); //PSP-5556 Need to filter out pre symphony employees who had non numeric ids
        DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
        List<Employee> employeesList = new ArrayList<Employee>();
        employeesList.addAll(employees);
        return employeesList;
    }

    public static List<VmpEmployeeInfo> findVmpEmployeeInfosByCompanyRealm(String companyRealm) {
        List<VmpEmployeeInfo> employeesList = new ArrayList<VmpEmployeeInfo>();
        Expression<VmpEmployeeInfo> query = new Query<VmpEmployeeInfo>()
                .Where(VmpEmployeeInfo.Company().IAMRealmId().equalTo(companyRealm));
        DomainEntitySet<VmpEmployeeInfo> employees = Application.find(VmpEmployeeInfo.class, query);
        employeesList.addAll(employees);
        return employeesList;
    }

    public static Employee findEmployeeByCompanyRealmAndEmployeeId(String companyRealm, SpcfUniqueId employeeId) {
        Employee employee = null;
        Expression<Employee> query = new Query<Employee>()
                .Where(Employee.Company().IAMRealmId().equalTo(companyRealm)
                .And(Employee.Id().equalTo(employeeId)));
        DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
        if(employees != null && employees.size() == 1) {
            employee = employees.getFirst();
        }
        return employee;
    }

    public static Employee findEmployeeByCompanyRealmIdAndConsumerRealmId(String companyRealmId, String consumerRealmId) {
        Employee employee = null;
        Expression<Employee> query = new Query<Employee>()
                .Where(Employee.Company().IAMRealmId().equalTo(companyRealmId)
                .And(Employee.ConsumerRealmId().equalTo(consumerRealmId)));
        DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
        if (employees != null && employees.size() == 1) {
            employee = employees.getFirst();
        }
        return employee;
    }

    public static VmpEmployeeInfo findVmpEmployeeInfoByCompanyRealmIdAndConsumerRealmId(String companyRealmId, String consumerRealmId) {
        VmpEmployeeInfo vmpEmployee = null;
        Expression<VmpEmployeeInfo> query = new Query<VmpEmployeeInfo>()
                .Where(VmpEmployeeInfo.Company().IAMRealmId().equalTo(companyRealmId)
                        .And(VmpEmployeeInfo.ConsumerRealmId().equalTo(consumerRealmId)));
        DomainEntitySet<VmpEmployeeInfo> vmpEmployees = Application.find(VmpEmployeeInfo.class, query);
        if (vmpEmployees != null && vmpEmployees.size() == 1) {
            vmpEmployee = vmpEmployees.getFirst();
        }
        return vmpEmployee;
    }

    /**
     * VMP Hot-Fix for v4 APIs
     * corresponding to findVmpEmployeeInfoByCompanyRealmIdAndConsumerRealmId
     * @param companyUniqueId- can be either company seq or company realm id
     * @param consumerRealmId
     * @return
     */
    public static VmpEmployeeInfo findVmpEmployeeInfoByCompanyUniqueIdAndConsumerRealmId(String companyUniqueId, String consumerRealmId) {
        VmpEmployeeInfo vmpEmployee = null;
        Criterion<VmpEmployeeInfo> employeeCriterion;

        if(CdmHelper.isSpcfUniqueId(companyUniqueId)) {
            employeeCriterion = VmpEmployeeInfo.Company().Id().equalTo(SpcfUniqueId.createInstance(companyUniqueId));
        }else{
            employeeCriterion = VmpEmployeeInfo.Company().IAMRealmId().equalTo(companyUniqueId);
        }

        Expression<VmpEmployeeInfo> query = new Query<VmpEmployeeInfo>()
                .Where(employeeCriterion
                        .And(VmpEmployeeInfo.ConsumerRealmId().equalTo(consumerRealmId)));

        DomainEntitySet<VmpEmployeeInfo> vmpEmployees = Application.find(VmpEmployeeInfo.class, query);
        if (vmpEmployees != null && vmpEmployees.size() == 1) {
            vmpEmployee = vmpEmployees.getFirst();
        }
        return vmpEmployee;
    }

    /**
     * VMP Hot-Fix for v4 APIs
     * corresponding to findEmployeeByCompanyRealmIdAndConsumerRealmId
     * @param companyUniqueId- can be either company seq or company realm id
     * @param consumerRealmId
     * @return
     */
    public static Employee findEmployeeByCompanyUniqueIdAndConsumerRealmId(String companyUniqueId, String consumerRealmId) {
        Employee employee = null;
        Criterion<Employee> employeeCriterion;

        if(CdmHelper.isSpcfUniqueId(companyUniqueId)){
          employeeCriterion=Employee.Company().Id().equalTo(SpcfUniqueId.createInstance(companyUniqueId));
        }else{
          employeeCriterion=Employee.Company().IAMRealmId().equalTo(companyUniqueId);
        }

        Expression<Employee> query = new Query<Employee>()
                .Where(employeeCriterion
                        .And(Employee.ConsumerRealmId().equalTo(consumerRealmId)));

        //There can be 2 employees having same CFR and 2 companies having same realm id (1 company is inactive). Return VMP enabled company's employee
        DomainEntitySet<Employee> employees = Application.find(Employee.class, query);
        Predicate<Employee> isVmpEmployee = emp -> emp.getCompany().isCompanyOnService(ServiceCode.ViewMyPaycheck);
        Set<Employee> vmpEmployeeSet = employees.stream().filter(isVmpEmployee).collect(Collectors.<Employee>toSet());
        logger.info("Company Unique Id = " + companyUniqueId + ". consumerRealmId=" + consumerRealmId + ". VMP Employees count = "+ vmpEmployeeSet.size());

        if (vmpEmployeeSet != null && vmpEmployeeSet.size() == 1) {
            employee = vmpEmployeeSet.iterator().next();
            logger.info("Company Unique Id = " + companyUniqueId + ". consumerRealmId=" + consumerRealmId + ". Exactly 1 employee found. Employee Id = " + employee.getId());
        }
        return employee;
    }

}
