package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.Request;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 22, 2009
 * Time: 8:21:38 AM
 */
@XmlRootElement()
@XmlType(name = "VoidPaymentRequest")
public class VoidPaymentRequest extends Request {
    private QBCompany mQBCompany;
    private List<String> mPaymentGUIDs;
    private String sessionId;

    
    @XmlElement(name = "Company", required = true)
    public QBCompany getCompany() {
        return mQBCompany;
    }

    public void setCompany(QBCompany pCompany) {
        mQBCompany = pCompany;
    }

    @XmlElementWrapper(name = "PaymentGUIDs")
    @XmlElement(name = "PaymentGUID", required = true)
    public List<String> getPaymentGUIDs() {
         if (mPaymentGUIDs == null) {
            mPaymentGUIDs = new ArrayList<String>();
        }
        return this.mPaymentGUIDs;
    }

    @XmlElement(name = "SessionId")
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
}
