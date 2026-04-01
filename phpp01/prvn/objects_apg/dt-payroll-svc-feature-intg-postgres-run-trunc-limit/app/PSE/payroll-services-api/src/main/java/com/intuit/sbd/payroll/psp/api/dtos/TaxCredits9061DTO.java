package com.intuit.sbd.payroll.psp.api.dtos;

/**
 * User: dweinberg
 * Date: Sep 27, 2010
 * Time: 2:09:53 PM
 */
public class TaxCredits9061DTO {
    private byte[] form9061;
    private String employeeName;
    private String ein;
    private String ssn;
    private String employeeEmail;
    private String employerEmail;

    private String packetDocumentKey;
    private String packetPassword;
    private byte[] unsignedPacket;

    public TaxCredits9061DTO(byte[] form9061, String employeeName, String ein, String ssn, String employeeEmail, String employerEmail, String packetDocumentKey, String packetPassword, byte[] unsignedPacket) {
        this.form9061 = form9061;
        this.employeeName = employeeName;
        this.ein = ein;
        this.ssn = ssn;
        this.employeeEmail = employeeEmail;
        this.employerEmail = employerEmail;
        this.packetDocumentKey = packetDocumentKey;
        this.packetPassword = packetPassword;
        this.unsignedPacket = unsignedPacket;
    }

    public byte[] getForm9061() {
        return form9061;
    }

    public void setForm9061(byte[] form9061) {
        this.form9061 = form9061;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getEmployerEmail() {
        return employerEmail;
    }

    public void setEmployerEmail(String employerEmail) {
        this.employerEmail = employerEmail;
    }

    public String getPacketDocumentKey() {
        return packetDocumentKey;
    }

    public void setPacketDocumentKey(String packetDocumentKey) {
        this.packetDocumentKey = packetDocumentKey;
    }

    public String getPacketPassword() {
        return packetPassword;
    }

    public void setPacketPassword(String packetPassword) {
        this.packetPassword = packetPassword;
    }

    public byte[] getUnsignedPacket() {
        return unsignedPacket;
    }

    public void setUnsignedPacket(byte[] unsignedPacket) {
        this.unsignedPacket = unsignedPacket;
    }
}
