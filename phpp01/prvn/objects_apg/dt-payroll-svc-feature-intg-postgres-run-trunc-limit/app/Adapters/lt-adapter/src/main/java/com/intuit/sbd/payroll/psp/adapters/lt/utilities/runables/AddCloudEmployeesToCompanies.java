package com.intuit.sbd.payroll.psp.adapters.lt.utilities.runables;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.DataUtilities;
import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.address.AddressDTO;
import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.address.AddressFactory;
import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.dates.CloudDates;
import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.dates.DateDTO;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.*;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.test.QBDTWSRequestCreator;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.webservices.QBPayrollWebServices;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: Jun 5, 2010
 * Time: 10:24:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class AddCloudEmployeesToCompanies {


    public static void main(String [] args){
        DataUtilities du = new DataUtilities();

        AddCloudEmployeesToCompanies addEEs = new AddCloudEmployeesToCompanies();

        //Get list of companies
        ArrayList<Company> companies = addEEs.getCompanyList();

        //For each company, retrieve list of DD employees
        for (Company co : companies){

            PayrollServices.beginUnitOfWork();
            PayrollServices.setCurrentPrincipal(SystemPrincipal.TestAdapter);
            co = Company.findCompany(co.getSourceCompanyId(), co.getSourceSystemCd());

            SubmitPayrollRequest submitPayrollRequest = QBDTWSRequestCreator.createSubmitPayrollRequest(co);
            
            //Set the pin
            submitPayrollRequest.setPIN("abcdABCD1234");
            SubmitEmployeesRequest employeeRequest = submitPayrollRequest.getSubmitEmployeesRequest();
            //Clear Existing employees created by default
            
            employeeRequest.getEmployees().getEmployee().clear();

            //Get the list of employees
            DomainEntitySet<Employee> eeList = co.getDirectDepositEmployees();

            System.out.println("Company Id: " + co.getSourceCompanyId());
            System.out.println("Employee Count: " + eeList.size());

                for (Employee ee : eeList){
                    QBEmployee cloudEE = QBDTWSRequestCreator.createQBEmployee(co.getSourceCompanyId(),du.getRandomString(16));
                    cloudEE.setActive(true);

                    //Build the address and set as EE's live address
                    AddressDTO address = AddressFactory.getAddress();
                    QBAddress qbAddress = new QBAddress();
                    qbAddress.setAddressLine1(address.getAddress1());
                    qbAddress.setCity(address.getCity());
                    qbAddress.setState(address.getState());
                    qbAddress.setZipCode(address.getZipCode());
                    cloudEE.setLiveAddress(qbAddress);

                    //Set Required Date Info
                    CloudDates dates = new CloudDates();
                    DateDTO birthDayDTO = dates.generateDateOfBirth();
                    QBDate dobDTO = new QBDate();
                        dobDTO.setDay(birthDayDTO.getDay());
                        dobDTO.setMonth(birthDayDTO.getMonth());
                        dobDTO.setYear(birthDayDTO.getYear());
                    cloudEE.setBirthDate(dobDTO);

                    DateDTO hireDayDTO = dates.generateHireDate();
                    QBDate hireDTO = new QBDate();
                        hireDTO.setDay(hireDayDTO.getDay());
                        hireDTO.setMonth(hireDayDTO.getMonth());
                        hireDTO.setYear(hireDayDTO.getYear());
                    cloudEE.setHireDate(hireDTO);

                    cloudEE.setTerminationDate(null);
                    
                    //Set other required info
                    cloudEE.setFirstName(ee.getFirstName());
                    cloudEE.setLastName(ee.getLastName());
                    cloudEE.setMiddleName(ee.getMiddleName());
                    cloudEE.setSocialSecurityNumber(du.getRandomString(9));
                    cloudEE.setPhoneNumber(du.getPhoneNumber());
                    cloudEE.setEmailAddress(du.getEmailAddress());
                    cloudEE.setPaySchedule(QBPayScheduleEnum.BI_WEEKLY);

                    //Build the Employee Request
                    employeeRequest.getEmployees().getEmployee().add(cloudEE);

                }
            PayrollServices.rollbackUnitOfWork();

            //Send Payroll Request for the company
            QBPayrollWebServices webServices = new QBPayrollWebServices();
            //submitPayrollRequest.setSubmitEmployeesRequest(employeeRequest);
            webServices.SubmitPayroll(submitPayrollRequest);

        }

    }



    public  ArrayList<Company> getCompanyList(){

        //Request Parameters
        SourceSystemCode pSourceSystem = SourceSystemCode.QBDT;
        ServiceCode pServiceCode = ServiceCode.ThirdParty401k;
        ServiceSubStatusCode pStatusCode = ServiceSubStatusCode.PendingFirstPayroll;

        PayrollServices.beginUnitOfWork();

            Expression<CompanyService> query =
                    new Query<CompanyService>()
                            .Where(CompanyService.Service().ServiceCd().equalTo(pServiceCode)
                                    .And(CompanyService.StatusCd().equalTo(pStatusCode))
                                    .And(CompanyService.Company().SourceSystemCd().equalTo(pSourceSystem)))
                            .EagerLoad(CompanyService.Company(), CompanyService.Company().CompanyBankAccountSet());

            DomainEntitySet<CompanyService> coServiceList = Application.find(CompanyService.class, query);

        //Build an ArrayList<Company>
        ArrayList<Company> companies = new ArrayList<Company>();
        for (CompanyService cs : coServiceList){
            Company co = cs.getCompany();

            //Skip companies with no employees
            if (co.getDirectDepositEmployees().size() == 0)
                continue;

            companies.add(co);
        }

        PayrollServices.rollbackUnitOfWork();
        return companies;
    }



}
