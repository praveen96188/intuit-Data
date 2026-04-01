package com.intuit.sbd.payroll.psp.adapters.lt.utilities.runables;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.DataUtilities;
import com.intuit.sbd.payroll.psp.adapters.lt.utilities.services.Marshalling;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.ProcessingResponse;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.SubmitPayrollRequest;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.webservices.QBPayrollWebServices;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;

import static com.intuit.sbd.payroll.psp.adapters.qbdtws.test.QBDTWSRequestCreator.*;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: Jun 8, 2010
 * Time: 1:57:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddPayrunForCompany {

    public static void main(String [] args){
        DataUtilities du = new DataUtilities();
        Marshalling m = new Marshalling();

        String pin = "abcdABCD1234";
        String companyId = "100000013";

        PayrollServices.beginUnitOfWork();
        PayrollServices.setCurrentPrincipal(SystemPrincipal.TestAdapter);
        Company foundCompany = Company.findCompany(companyId, SourceSystemCode.QBDT);

        ThirdParty401kCompanyServiceInfo tp401KServiceInfo = (ThirdParty401kCompanyServiceInfo) foundCompany.getService(ServiceCode.ThirdParty401k);
        String custodialId = tp401KServiceInfo.getCustodialId();


        SubmitPayrollRequest submitPayrollRequest = createSubmitPayrollRequest(foundCompany);
        submitPayrollRequest.getPayrollItemList().getPayrollItem().clear();
        submitPayrollRequest.setSubmitEmployeesRequest(null);
        submitPayrollRequest.setPayrollItemList(createDefaultPayrollItems(custodialId));
        submitPayrollRequest.setPIN(pin);

        //Clear existing paychecks
        submitPayrollRequest.getPaycheckList().getPaycheck().clear();
        //Add Paychecks for each employee
        DomainEntitySet<Employee> eeList = foundCompany.getCloudEmployees();

        for(Employee ee: eeList){
            submitPayrollRequest.getPaycheckList().getPaycheck().add(createQBPaycheck(ee.getSourceEmployeeId(), du.getRandomString(5)));
        }

        PayrollServices.rollbackUnitOfWork();

        String requestXML = m.outputAsString(submitPayrollRequest);
        System.out.println(requestXML);

            //Send Payroll Request for the company
            QBPayrollWebServices webServices = new QBPayrollWebServices();
            ProcessingResponse response = webServices.SubmitPayroll(submitPayrollRequest);
            System.out.println(m.outputAsString(response));
    }


}
