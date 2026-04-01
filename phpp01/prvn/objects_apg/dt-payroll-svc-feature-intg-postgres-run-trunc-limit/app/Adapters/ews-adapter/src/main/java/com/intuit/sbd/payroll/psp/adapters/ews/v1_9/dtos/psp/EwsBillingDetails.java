package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsCreditCardType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsPaymentMethod;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Calendar;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "subscriptionNextBillDate",
    "paymentMethod",
    "creditCardType",
    "creditCardNumber",
    "creditCardExp"            
})
public class EwsBillingDetails implements Cloneable {

    @XmlElement(name = "SubscriptionNextBillDate", required = false)
    protected Calendar subscriptionNextBillDate;

    @XmlElement(name = "PaymentMethod", required = true)
    protected EwsPaymentMethod paymentMethod;

    @XmlElement(name = "CreditCardType", required = true)
    protected EwsCreditCardType creditCardType;

    @XmlElement(name = "CreditCardNumber", required = true)
    protected String creditCardNumber;

    // credit card style: MM/YYYY
    @XmlElement(name = "CreditCardExp", required = true)
    protected String creditCardExp;

    public EwsBillingDetails clone() throws CloneNotSupportedException {
        return (EwsBillingDetails) super.clone();
    }

    public Calendar getSubscriptionNextBillDate() {
        return subscriptionNextBillDate;
    }

    public void setSubscriptionNextBillDate(Calendar subscriptionNextBillDate) {
        this.subscriptionNextBillDate = subscriptionNextBillDate;
    }

    public EwsPaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(EwsPaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public EwsCreditCardType getCreditCardType() {
        return creditCardType;
    }

    public void setCreditCardType(EwsCreditCardType creditCardType) {
        this.creditCardType = creditCardType;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getCreditCardExp() {
        return creditCardExp;
    }

    public void setCreditCardExp(String creditCardExp) {
        this.creditCardExp = creditCardExp;
    }

    public void validate() throws EwsException {
        if (!Validation.validateValue(this.creditCardNumber, false, "\\p{Digit}{4,4}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("CreditCardNumber", "BillingDetails"));
        }

        if (!Validation.validateValue(this.creditCardExp, false, "^((0[1-9])|(1[0-2]))\\/((2009)|(20[1-2][0-9]))$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("CreditCardExp", "BillingDetails"));
        }
    }
}
