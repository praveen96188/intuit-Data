package com.intuit.sbd.payroll.psp.webservices.wsdto;

import com.intuit.sbd.payroll.psp.domain.CompanyAdjustmentSubmission;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 19, 2008
 * Time: 9:11:00 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlRootElement
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
public class PaycheckWSDTO {
    public String id; // GUID
    public String sourcePaycheckId;
    public String sourceEmployeeId;
    public String employeeDisplayName; // First name + last name
    public String nonDDEmployeeId;
    public String nonDDEmployeeDisplayName;
    public BigDecimal paycheckAmount;
    public String status;
    public Collection<TransactionWSDTO> financialTransactions;
    public Collection<CompensationWSDTO> compensationLineItems;
    public Collection<DeductionWSDTO> deductionLineItems;
    public Collection<EmployerContributionWSDTO> employerContributionLineItems;
    public Collection<TaxWSDTO> taxLiabilities;
    public Date payPeriodBeginDate;
    public Date payPeriodEndDate;
    public Boolean hasBeenOffloadedToTOK;

    public QbdtPaycheckInfoWSDTO qbdtPaycheckInfo;
    public boolean isYTDAdjustment;
    public CompanyAdjustmentSubmissionWSDTO companyAdjustmentSubmission;

    public BigDecimal netAmount;
}
