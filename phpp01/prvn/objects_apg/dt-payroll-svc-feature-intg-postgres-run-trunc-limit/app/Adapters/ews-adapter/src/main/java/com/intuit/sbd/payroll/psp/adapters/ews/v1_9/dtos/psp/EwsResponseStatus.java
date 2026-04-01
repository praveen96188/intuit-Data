package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "code",
        "message"
})
public class EwsResponseStatus implements Cloneable {

    @XmlElement(name = "Code", required = false)
    protected int code;

    @XmlElement(name = "Message", required = false)
    protected String message;

    public EwsResponseStatus() {
        this.code = 0;
        this.message = "Success";
    }

    public EwsResponseStatus clone() throws CloneNotSupportedException {
        return (EwsResponseStatus) super.clone();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void validate() throws Exception {
    }

}
