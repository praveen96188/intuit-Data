package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Feb 3, 2010
 * Time: 3:19:34 PM
 */
public class SAPVendorInfo {
    private String name;
    private String email;
    private String phone;
    private String sourceId;

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String pEmail) {
        email = pEmail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String pPhone) {
        phone = pPhone;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String pSourceId) {
        sourceId = pSourceId;
    }
}
