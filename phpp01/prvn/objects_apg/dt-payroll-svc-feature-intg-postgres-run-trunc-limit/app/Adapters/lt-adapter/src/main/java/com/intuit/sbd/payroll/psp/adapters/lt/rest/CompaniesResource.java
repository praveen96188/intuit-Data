package com.intuit.sbd.payroll.psp.adapters.lt.rest;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.util.VmpTestUtil;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Path("/companies")
public class CompaniesResource {

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<VmpCompany> createCompanies(@QueryParam("numberOfCompanies")int numberOfCompanies, @QueryParam("numberOfEmployeesPerCompany")int numberOfEmployeesPerCompany) {
        List<VmpCompany> vmpCompanies = new ArrayList<VmpCompany>();
        DataLoadServices.setEmployeeCount(numberOfEmployeesPerCompany);
        Random randomGenerator = new Random();
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        double paystubAmount = 0;
        int day;
        int totalEmployeeCount = 0;

        for(int i = 0; i < numberOfCompanies; i++) {
            VmpCompany vmpCompany = new VmpCompany();
            List<VmpEmployee> vmpEmployees = new ArrayList<VmpEmployee>();
            try {
                String psid = "209906" + i;
                System.out.println("Creating company: " + psid);
                Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Cloud);
                //Company created with two employees by default
                DataLoadServices.addEEs(company, numberOfEmployeesPerCompany - 2);
                vmpCompany.id = company.getSourceCompanyId();
                vmpCompany.companyRealmId = company.getIAMRealmId();
                for(Employee employee : company.getEmployees()) {
                    System.out.println("Company " + psid + " has employee " + employee.getTaxId());
                    totalEmployeeCount++;
                    VmpEmployee vmpEmployee = new VmpEmployee();
                    vmpEmployee.id = employee.getId().toString();
                    vmpEmployees.add(vmpEmployee);
                    Application.beginUnitOfWork();
                    Application.refresh(employee);
                    //Create 1 paystub a month for each employee
                    for(int month = 1; month < 13; month++) {
                        paystubAmount = 123.45;
                        day = randomGenerator.nextInt(27) + 1;
                        VmpTestUtil.createPaystub(employee, decimalFormat.format(paystubAmount), SpcfCalendar.createInstance(2012, month, day));
//                        //Create 2nd paystub
//                        paystubAmount = 123.45;
//                        day = randomGenerator.nextInt(27) + 1;
//                        VmpTestUtil.createPaystub(employee, decimalFormat.format(paystubAmount), SpcfCalendar.createInstance(2012, month, day));
                    }
                    Application.commitUnitOfWork();
                    //After creating all the paystubs, associate the employee using the last one
                    VmpTestUtil.associateEmployeeWithRealm(String.valueOf(totalEmployeeCount), employee.getTaxId(), "iamemail@intuit.com", decimalFormat.format(paystubAmount));
                    Application.beginUnitOfWork();
                    Application.refresh(employee);
                    vmpEmployee.consumerRealmId = employee.getConsumerRealmId();
                    vmpEmployee.ssn = employee.getTaxId();
                    vmpEmployee.paystubAmount = decimalFormat.format(paystubAmount);
                    Application.rollbackUnitOfWork();
                }
                vmpCompany.employees = vmpEmployees;
                vmpCompanies.add(vmpCompany);
            } finally {
                Application.rollbackUnitOfWork();
            }
        }
        return vmpCompanies;
    }
}
