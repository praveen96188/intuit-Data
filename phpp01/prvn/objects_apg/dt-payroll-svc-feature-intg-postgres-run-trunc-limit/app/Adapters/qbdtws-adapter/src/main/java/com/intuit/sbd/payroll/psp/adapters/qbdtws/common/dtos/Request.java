package com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos;

import com.intuit.sbd.payroll.psp.adapters.qbdtws.marshalling.FieldMaskAdapter;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 23, 2009
 * Time: 2:50:48 PM
 */
@XmlRootElement
@XmlType(name = "Request")
public class Request {

    private String psid;
    private String pin;

    @XmlElement(name = "PSID", required = true)
    public String getPSID() {
        return psid;
    }

    public void setPSID(String pPSID) {
        psid = pPSID;
    }

    @XmlJavaTypeAdapter(value = FieldMaskAdapter.class)
    @XmlElement(name = "PIN", required = true)
    public String getPIN() {
        return pin;
    }

    public void setPIN(String pPIN) {
        pin = pPIN;
    }
}
