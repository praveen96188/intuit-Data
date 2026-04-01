package com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto;

/**
 * Created with IntelliJ IDEA.
 * User: afroza786
 * Date: 7/10/13
 * Time: 4:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContactDTO {
    private String name;
    private String phone;
    private String email;
    private String lastName;
    private String firstName;

    public void setFirstName(String pFirstName) {
        firstName = pFirstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String pLastName) {
        lastName = pLastName;
    }

    public String getFirstName() {
        return firstName;
    }



    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String pPhone) {
        phone = pPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String pEmail) {
        email = pEmail;
    }


}
