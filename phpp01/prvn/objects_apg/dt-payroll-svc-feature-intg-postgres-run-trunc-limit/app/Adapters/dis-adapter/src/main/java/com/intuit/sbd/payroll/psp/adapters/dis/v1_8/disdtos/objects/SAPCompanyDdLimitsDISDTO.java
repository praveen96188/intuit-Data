package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
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
