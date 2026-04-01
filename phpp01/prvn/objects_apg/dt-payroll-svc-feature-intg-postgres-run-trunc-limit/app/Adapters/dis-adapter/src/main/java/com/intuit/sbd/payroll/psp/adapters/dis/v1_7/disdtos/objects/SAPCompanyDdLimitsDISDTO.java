package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/SAPCompanyDdLimitsDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SAPCompanyDdLimits", propOrder = {"perPayrollLimit","perEmployeeLimit"})
public class SAPCompanyDdLimitsDISDTO {
    @XmlElement(name = "PerPayrollLimit")
    private double perPayrollLimit;

    public double getPerPayrollLimit() {
        return perPayrollLimit;
    }

    public void setPerPayrollLimit(double pPerPayrollLimit) {
        perPayrollLimit = pPerPayrollLimit;
    }

    @XmlElement(name = "PerEmployeeLimit")
    private double perEmployeeLimit;

    public double getPerEmployeeLimit() {
        return perEmployeeLimit;
    }

    public void setPerEmployeeLimit(double pPerEmployeeLimit) {
        perEmployeeLimit = pPerEmployeeLimit;
    }

}
