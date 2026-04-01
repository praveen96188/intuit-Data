package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/SAPQuickbooksInfoDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SAPQuickbooksInfo")
public class SAPQuickbooksInfoDISDTO {
    @XmlElement(name = "LicenseNumber")
    private String licenseNumber;

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    @XmlElement(name = "ApplicationVersion")
    private String applicationVersion;

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    @XmlElement(name = "TaxTable")
    private String taxTable;

    public String getTaxTable() {
        return taxTable;
    }

    public void setTaxTable(String taxTable) {
        this.taxTable = taxTable;
    }

    @XmlElement(name = "CoaFeeAccountName")
    private String coaFeeAccountName;

    public String getCoaFeeAccountName() {
        return coaFeeAccountName;
    }

    public void setCoaFeeAccountName(String coaFeeAccountName) {
        this.coaFeeAccountName = coaFeeAccountName;
    }

    @XmlElement(name = "CoaSalesTaxAccountName")
    private String coaSalesTaxAccountName;

    public String getCoaSalesTaxAccountName() {
        return coaSalesTaxAccountName;
    }

    public void setCoaSalesTaxAccountName(String coaSalesTaxAccountName) {
        this.coaSalesTaxAccountName = coaSalesTaxAccountName;
    }

}
