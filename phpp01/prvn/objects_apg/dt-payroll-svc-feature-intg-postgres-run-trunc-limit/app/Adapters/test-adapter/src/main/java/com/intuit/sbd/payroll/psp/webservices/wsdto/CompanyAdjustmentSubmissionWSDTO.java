package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 * User: dweinberg
 * Date: Oct 25, 2010
 * Time: 5:11:53 PM
 */
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
public class CompanyAdjustmentSubmissionWSDTO {
    public String amendmentProcessingStatus;
    public BigDecimal amount;
    public boolean isVoid;
    public String sourceId;
    public Date submissionDate;

    public QbdtTransactionInfoWSDTO qbdtTransactionInfo;

    public Collection<LiabilityAdjustmentWSDTO> liabilityAdjustments;
}
