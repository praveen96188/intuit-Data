package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Collection;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: Aug 11, 2008
 * Time: 2:34:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmployeeWSDTO {
    public String id; //guid
    public String sourceEmployeeId; //source employee id
    public String statusCode;   //employee status
    public String firstName;    //first name
    public String lastName;     //last name
    public String middleName;   //middle name
    public String taxId;
    public String communicationTypePreference;
    public String email;
    public Collection<EmployeeBankAccountWSDTO> employeeBankAccountCollection;
    public Collection<EmployeeWagePlanWSDTO> employeeWagePlanCollection;
    public int fedAllowances;
    public String fedFilingStatus;
    public String genderCode;
    public boolean hasRetirementPlan;
    public boolean hasThirdPartySickPay;
    public Date hireDate;
    public boolean isStatutory;
    public AddressWSDTO mailingAddress;
    public String phone;
    public Date reHireDate;
    public int stateAllowances;
    public String stateFilingStatus;
    public Date statusEffectiveDate;
    public String suffix;
    public Date terminationDate;
    public String workState;
    public ThirdParty401kEmployeeInfoWSDTO tp401kEEInfo;

    public Collection<EmployeePayrollItemWSDTO> employeePayrollItemInfo;
    public Collection<EmployeeTaxWSDTO> employeeTaxInfo;
    public Collection<EmployeeAccrualWSDTO> employeeAccrualInfo;
    public Collection<EmployeeCustomFieldWSDTO> employeeCustomFieldInfo;
    public QBDTEmployeeInfoWSDO qbdtEmployeeInfo;
    public AssistedEmployeeInfoWSDTO assistedEmployeeInfo;
}
