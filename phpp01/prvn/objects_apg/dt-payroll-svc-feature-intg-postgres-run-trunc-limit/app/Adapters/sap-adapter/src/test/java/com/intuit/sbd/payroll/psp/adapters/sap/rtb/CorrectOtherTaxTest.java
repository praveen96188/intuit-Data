package com.intuit.sbd.payroll.psp.adapters.sap.rtb;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeTaxDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.junit.*;
import static junit.framework.Assert.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: rgarg
 * Date: 1/20/16
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class CorrectOtherTaxTest {


    private static final SpcfLogger logger = PayrollServices.getLogger(CorrectOtherTaxTest.class);

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        Application.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 3, 7, 12, 26, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        Application.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        DataLoadServices.reinitialize();
    }


    @Test
    /**
     * @Description: Test to check for null company law while adding tax in employee response.
     */
    public void correctTaxItemsWithNull() {

        DataLoadServices.setPSPDate(2013, 2, 21);
        JobResult jobResult = new JobResult();

        List<Employee> employees = DataLoadServices.setupCompany("123456789");
        Company company = employees.get(0).getCompany();

        DataLoadServices.setPSPDate(2013, 2, 22);
        Employee employee = employees.get(0);


        //Create employee tax item DTO

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCompanyLawsWithAgencyId(null, company, "LA");


        Application.beginUnitOfWork();
        Application.refresh(company);
        Application.refresh(employee);
        CompanyLaw companylaw = company.getCompanyAgencyCollection().get(0).getCompanyLawCollection().get(0);
        EmployeeDTO employeeDTO = PayrollServices.dtoFactory.create(employee);
        // 1st Object
        EmployeeTaxDTO employeeTaxDTO = new EmployeeTaxDTO();
        employeeTaxDTO.setAllowances(99);
        employeeTaxDTO.setCompanyLawId(companylaw.getSourceId());
        employeeTaxDTO.setState("LA");
        employeeTaxDTO.setTaxType(EmployeeTaxType.FIT);
        //Tax Table misc data
        Map<Integer, String> miscDataMap = new HashMap<Integer, String>();
        miscDataMap.put(20, "Test");
        employeeTaxDTO.setTaxTableMiscData(miscDataMap);

        //2nd object
        EmployeeTaxDTO employeeTaxDTO2 = new EmployeeTaxDTO();
        employeeTaxDTO2.setAllowances(96);
        employeeTaxDTO2.setCompanyLawId("999");
        employeeTaxDTO2.setState("LA");
        employeeTaxDTO2.setTaxType(EmployeeTaxType.Other);
        //Tax Table misc data
        Map<Integer, String> miscDataMap2 = new HashMap<Integer, String>();
        miscDataMap2.put(20, "Test");
        employeeTaxDTO2.setTaxTableMiscData(miscDataMap2);

        //3nd object
        EmployeeTaxDTO employeeTaxDTO3 = new EmployeeTaxDTO();
        employeeTaxDTO3.setAllowances(96);
        employeeTaxDTO3.setCompanyLawId("8");
        employeeTaxDTO3.setState("LA");
        employeeTaxDTO3.setTaxType(EmployeeTaxType.Other);
        //Tax Table misc data
        Map<Integer, String> miscDataMap3 = new HashMap<Integer, String>();
        miscDataMap3.put(20, "Test");
        employeeTaxDTO2.setTaxTableMiscData(miscDataMap3);


        List<EmployeeTaxDTO> empTaxList = new ArrayList<EmployeeTaxDTO>();
        empTaxList.add(employeeTaxDTO);
        empTaxList.add(employeeTaxDTO2);
        empTaxList.add(employeeTaxDTO3);
        employeeDTO.setEmployeeTaxDTOs(empTaxList);
        Application.commitUnitOfWork();
        //Create employee tax item
        Application.beginUnitOfWork();
        ProcessResult<Employee> result = PayrollServices.employeeManager.updateEmployee(employee.getCompany().getSourceSystemCd(), employee.getCompany().getSourceCompanyId(), employeeDTO);
        assertSuccess(result);
        employee = result.getResult();
        Application.commitUnitOfWork();
        //Verify that tax item is created
        Application.beginUnitOfWork();
        Application.refresh(employee);
        // Assert.assertEquals("Tax Items", 3, employee.getEmployeeTaxCollection().size());
        Application.commitUnitOfWork();


        Exception ex = null;
        boolean doneProcess = false;
        try{

            Application.beginUnitOfWork();
            CorrectOtherTax cot = new CorrectOtherTax();
            cot.getOtherTaxDetail(company,jobResult);
            DomainEntitySet<EmployeeTax> empTax = EmployeeTax.findNullOtherTax(company);
            logger.debug(empTax.get(0).getId());
            Application.rollbackUnitOfWork();
            Application.beginUnitOfWork();
           doneProcess =   cot.doProcessing(empTax,"123456789",jobResult);
            Application.commitUnitOfWork();



        }catch(Exception e){
            e.printStackTrace();
            ex= e;
        }
        assertEquals(null, ex);
        assertEquals(true,doneProcess);


    }



}
