package com.intuit.sbd.payroll.psp.webservices.wsdto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Date;

/**
 * User: rnorian
 * Date: Jan 28, 2011
 * Time: 4:41:17 PM
 */
@XmlType(name = "TaxPayment")
public class TaxPaymentMoneyMovementTransactionWSDTO extends MoneyMovementTransactionWSDTO {
    public String guid;
    public String agencyTaxPayerId;
    public String depositFrequency;
    public String manualPaymentStatus;
    public Date paymentPeriodBegin;
    public Date paymentPeriodEnd;
    public String referenceNumber;
    public String taxPaymentStatus;
    public Date taxPaymentStatusEffectiveDate;

    private ArrayList<PaymentOnHoldReasonWSDTO> onHoldRecords;

    @XmlElementWrapper(name = "onHoldRecords")
    @XmlElement(name = "onHoldRecord")
    public ArrayList<PaymentOnHoldReasonWSDTO> getOnHoldRecords() {
        return onHoldRecords;
    }

    public void setOnHoldRecords(ArrayList<PaymentOnHoldReasonWSDTO> pOnHoldRecords) {
        onHoldRecords = pOnHoldRecords;
    }
}
