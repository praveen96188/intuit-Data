package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import java.util.Collection;
import java.math.BigDecimal;


public class CompanyWSDTO {
    public String sourceSystemCD;
    public String sourceCompanyID;
    public String pspCompanyID;
    public String legalName;
    public Collection<OnHoldReasonWSDTO> onHoldReasons;
    public String ddStatus;
    public String taxStatus;
    public String offloadGroup;
    public String fundingModel;
    public BigDecimal overrideCompanyLimitAmount;
    public BigDecimal overrideEmployeeLimitAmount;
    public boolean isFlaggedForFraud ;
    public long lastTransactionResponseToken;
    public String companyPSId;
    public Company401kInfoWSDTO company401kInfo;
    public CompanyQuickBooksInfoWSDTO quickBooksInfo;
    public Collection<PayrollItemWSDTO> companyPayrollItems;
    public String fedTaxId;
    public AddressWSDTO legalAddress;
    public String nextPayrollTransactionId;
    public String nextPaycheckId;
    public String nextEmployeeId;
    public String nextPaylineTransactionId;

    @XmlElementWrapper(name = "services")
    @XmlElement(name = "service")
    public Collection<CompanyServiceWSDTO> services;

    //assisted company setup elements
    @XmlElementWrapper(name = "assistedPayrollItems")
    @XmlElement(name = "assistedPayrollItem")    
    public Collection<CompanyPayrollItemWSDTO> assistedPayrollItems;

    public Collection<LiabilityCheckWSDTO> liabilityChecks;
}
