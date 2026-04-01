package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import javax.xml.bind.annotation.*;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/QueryCompanyEmployeesWihPaycheckCountResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryCompanyEmployeesWihPaycheckCountResponseDISDTO",propOrder = {"employeeCount"})
public class QueryCompanyEmployeesWihPaycheckCountResponseDISDTO extends ResponseDISDTO {

    @XmlElement(name = "EmployeeCount")
    int employeeCount;

    public int getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(int employeeCount) {
        this.employeeCount = employeeCount;
    }

    @Override
    public void clearElements() {
        //@TODO Implement
    }

}