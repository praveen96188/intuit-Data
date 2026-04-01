package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsPaymentMethod;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Calendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "subscriptionNextBillDate",
        "paymentMethod",
        "creditCardType",
        "creditCardNumber",
        "creditCardExp"
})
public class EwsSubscriptionBillingInfo implements Cloneable {

    @XmlElement(name = "SubscriptionNextBillDate", required = false)
    protected Calendar subscriptionNextBillDate;

    @XmlElement(name = "PaymentMethod", required = false)
    protected EwsPaymentMethod paymentMethod;

    // can be hand entered, therefore not enumerated
    @XmlElement(name = "CreditCardType", required = false)
    protected String creditCardType;

    @XmlElement(name = "CreditCardNumber", required = false)
    protected String creditCardNumber;

    // credit card style: MM/YYYY
    @XmlElement(name = "CreditCardExp", required = false)    
    protected String creditCardExp;

    public EwsSubscriptionBillingInfo clone() throws CloneNotSupportedException {
        return (EwsSubscriptionBillingInfo) super.clone();
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

    public String getCreditCardType() {
        return creditCardType;
    }

    public void setCreditCardType(String creditCardType) {
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
}
