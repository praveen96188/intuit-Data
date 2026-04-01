package com.intuit.sbd.payroll.psp.adapters.lt;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: Apr 3, 2008
 * Time: 2:20:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class LtEmployee {


        /**
     * purpose: Given a companyId, this will build and return an ArrayList<LtEmployeeDTO> of all the employees
     *          in the database for the requested company
     * @param pCompanyId - company who's employees to return
     * @param pSourceSystemId - the sourceSystem the company belongs to
     * @return ArrayList<LtEmployeeDTO> - the built list of employees
     * @throws Exception - Throws a runtime exception so it can be returned to the user through the web service
     */
    public ArrayList<LtEmployeeDTO> getEmployeeList(String pCompanyId, SourceSystemCode pSourceSystemId)throws Exception{
        //Create and empty ArrayList<LtEmployeeDTO>
        ArrayList<LtEmployeeDTO> dtoList = new ArrayList<LtEmployeeDTO>();

        try{
            PayrollServices.beginUnitOfWork();

        String[] paramNames = new String[2];
        paramNames[0] = "sourceCompanyId";
        paramNames[1] = "sourceSystemCd";

        Object[] paramValues = new Object[2];
        paramValues[0] = pCompanyId;
        paramValues[1] = pSourceSystemId;

        DomainEntitySet<EmployeeBankAccount> baList = Application.findByNamedQuery("findEmployeePayrollInfoListByCompany",paramNames,paramValues);


            //Process each employee in the list and build the WSDTO
            for (EmployeeBankAccount ba : baList){
                LtEmployeeDTO tempEmp = buildDTO(ba);

                //Add the company to the list
                dtoList.add(tempEmp);

            }
            PayrollServices.commitUnitOfWork();

        }catch(Exception ex){
            PayrollServices.rollbackUnitOfWork();
            throw new RuntimeException(ex.getMessage());
         }
        //Return the ArrayList<LtEmployeeWSDTO>
        return dtoList;
    }

    /**
     * Retreives a list of cloud employees for a company, builds a DTO and returns it
     * @param pCompanyId -
     * @return ArrayList<LtEmployeeDTO> - containing relevant info for each employee
     */
    public ArrayList<LtEmployeeDTO> getCloudEmployeeList(String pCompanyId){
        //Create and empty ArrayList<LtEmployeeDTO>
        ArrayList<LtEmployeeDTO> dtoList = new ArrayList<LtEmployeeDTO>();

        try{
            PayrollServices.beginUnitOfWork();
            Company foundCompany = Company.findCompany(pCompanyId, SourceSystemCode.QBDT);
            DomainEntitySet<Employee> eeList = foundCompany.getCloudEmployees();

            for (Employee ee: eeList){

                //Build the EmployeeDTO
                LtEmployeeDTO tempEmp = new LtEmployeeDTO();
                tempEmp.employeeId = ee.getSourceEmployeeId();
                tempEmp.eeName = ee.getFullName();
                tempEmp.eeFirstName = ee.getFirstName();
                tempEmp.eeLastName = ee.getLastName();

                dtoList.add(tempEmp);
            }
            PayrollServices.commitUnitOfWork();

        }catch(Exception ex){
            PayrollServices.rollbackUnitOfWork();
            throw new RuntimeException(ex.getMessage());
         }
        //Return the ArrayList<LtEmployeeWSDTO>
        return dtoList;

        }


    /**
     * Purpose: Builds a single LtEmployeeDTO from the EmployeeBankAccount object provided.
     * @param eba - EmployeeBankAccount object to build the LtEmployeeDTO
     * @return completed LtEmployeeDTO for the provided employee
     */
    public LtEmployeeDTO buildDTO(EmployeeBankAccount eba){
        LtEmployeeDTO eeDTO = new LtEmployeeDTO();

        eeDTO.employeeId = eba.getEmployee().getSourceEmployeeId();
        eeDTO.eeBankAccountId = eba.getSourceBankAccountId();
        eeDTO.eeName = eba.getEmployee().getFirstName() + " " + eba.getEmployee().getLastName();
        eeDTO.eeBankAccountNumber = eba.getBankAccount().getAccountNumber();
        eeDTO.eeFirstName = eba.getEmployee().getFirstName();
        eeDTO.eeLastName = eba.getEmployee().getLastName();
        eeDTO.eeStatus = eba.getEmployee().getStatusCd().toString();
        eeDTO.eeSequence = eba.getEmployee().getId().toString();
        
        return eeDTO;

    }

}
