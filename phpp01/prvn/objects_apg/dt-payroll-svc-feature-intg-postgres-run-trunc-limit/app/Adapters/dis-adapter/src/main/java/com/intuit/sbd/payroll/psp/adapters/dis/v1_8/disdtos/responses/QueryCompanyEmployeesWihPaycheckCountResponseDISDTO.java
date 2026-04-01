package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import javax.xml.bind.annotation.*;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
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