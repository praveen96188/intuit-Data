package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.Request;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 22, 2009
 * Time: 8:14:34 AM
 */
@XmlRootElement()
@XmlType(name = "SubmitPaymentRequest")
public class SubmitPaymentRequest extends Request {
    private QBCompany mCompany;
    private List<PaymentTransaction> mPaymentTransactions;

    @XmlElement(name = "Company", required = true)
    public QBCompany getCompany() {
        return mCompany;
    }

    public void setCompany(QBCompany pCompany) {
        mCompany = pCompany;
    }

    @XmlElementWrapper(name = "PaymentTransactions")
    @XmlElement(name = "PaymentTransaction", required = true)
    public List<PaymentTransaction> getPaymentTransactions() {
         if (mPaymentTransactions == null) {
            mPaymentTransactions = new ArrayList<PaymentTransaction>();
        }
        return this.mPaymentTransactions;
    }
}
