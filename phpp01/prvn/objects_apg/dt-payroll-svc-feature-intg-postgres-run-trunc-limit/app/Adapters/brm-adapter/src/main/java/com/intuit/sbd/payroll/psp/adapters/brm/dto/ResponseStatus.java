package com.intuit.sbd.payroll.psp.adapters.brm.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created with IntelliJ IDEA.
 * User: VidhyaK689
 * Date: 9/25/12
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */

@XmlType(name = "ResponseStatus",propOrder = {"success","code", "message"})
public class ResponseStatus {

    private String code = null;
    private String message = null;
    private String success = "Y";

    public ResponseStatus() {
    }

    @XmlElement(name = "Success", nillable = false, required = true)
    public String getSuccess() {
        return success;
    }

    public void setSuccess(String pSuccess) {
        success = pSuccess;
    }

    @XmlElement(name = "Code", nillable = false)
    public String getCode() {
        return code;
    }

    public void setCode(String pCode) {
        code = pCode;
    }

    public void setMessage(String pMessage) {
        message = pMessage;
    }

    @XmlElement(name = "Message", nillable = false)
    public String getMessage() {
        return    message;
    }

}
