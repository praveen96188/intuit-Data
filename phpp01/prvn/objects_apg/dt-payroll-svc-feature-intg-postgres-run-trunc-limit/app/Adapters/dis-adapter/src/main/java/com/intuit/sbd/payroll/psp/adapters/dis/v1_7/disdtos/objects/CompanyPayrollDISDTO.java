package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPPayrollRun;
import com.intuit.sbd.payroll.psp.domain.CompanyEventStatus;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;
import com.intuit.sbd.payroll.psp.domain.PayrollStatus;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/CompanyPayrollDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 *
 * Company Events DIS DTO that will be returned by the WS
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType()
public class CompanyPayrollDISDTO {

    public CompanyPayrollDISDTO() {
    }

    public CompanyPayrollDISDTO(SAPPayrollRun pSapPayrollRun) {
        this.paycheckDate = pSapPayrollRun.getPaycheckDate();
        this.payrollNetAmount = pSapPayrollRun.getPayrollNetAmount();
        this.payrollRunDate = pSapPayrollRun.getPayrollRunDate();
        this.sourcePayRunId = pSapPayrollRun.getSourcePayRunId();
        this.id = pSapPayrollRun.getId();
        this.paycheckSettlementDate = pSapPayrollRun.getPaycheckSettlementDate();
        this.payrollRunStatus = pSapPayrollRun.getPayrollRunStatus();
        this.companyId = pSapPayrollRun.getCompanyId();
        this.sourceSystemId = pSapPayrollRun.getSourceSystemId();
    }

    @XmlElement
    private Date paycheckDate;

    @XmlElement
    private double payrollNetAmount;

    @XmlElement
    private Date payrollRunDate;

    @XmlElement
    private String sourcePayRunId;

    @XmlElement
    private String id;

    @XmlElement
    private Date paycheckSettlementDate;

    @XmlElement
    private PayrollStatus payrollRunStatus;

    @XmlElement
    private String companyId;

    @XmlElement
    private String sourceSystemId;

    public Date getPaycheckDate() {
        return paycheckDate;
    }

    public void setPaycheckDate(Date pPaycheckDate) {
        paycheckDate = pPaycheckDate;
    }

    public double getPayrollNetAmount() {
        return payrollNetAmount;
    }

    public void setPayrollNetAmount(double pPayrollNetAmount) {
        payrollNetAmount = pPayrollNetAmount;
    }

    public Date getPayrollRunDate() {
        return payrollRunDate;
    }

    public void setPayrollRunDate(Date pPayrollRunDate) {
        payrollRunDate = pPayrollRunDate;
    }

    public String getSourcePayRunId() {
        return sourcePayRunId;
    }

    public void setSourcePayRunId(String pSourcePayRunId) {
        sourcePayRunId = pSourcePayRunId;
    }

    public Date getPaycheckSettlementDate() {
        return paycheckSettlementDate;
    }

    public void setPaycheckSettlementDate(Date pPaycheckSettlementDate) {
        paycheckSettlementDate = pPaycheckSettlementDate;
    }

    public PayrollStatus getPayrollRunStatus() {
        return payrollRunStatus;
    }

    public void setPayrollRunStatus(PayrollStatus pPayrollRunStatus) {
        payrollRunStatus = pPayrollRunStatus;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String pCompanyId) {
        companyId = pCompanyId;
    }

    public String getSourceSystemId() {
        return sourceSystemId;
    }

    public void setSourceSystemId(String pSourceSystemId) {
        sourceSystemId = pSourceSystemId;
    }

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }
}
