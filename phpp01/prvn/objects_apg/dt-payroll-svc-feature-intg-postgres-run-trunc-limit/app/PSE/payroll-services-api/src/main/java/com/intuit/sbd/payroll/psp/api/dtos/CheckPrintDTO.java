package com.intuit.sbd.payroll.psp.api.dtos;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Jan 18, 2010
 * Time: 11:40:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class CheckPrintDTO {
    private String senderName;
    private CheckPrintAddressDTO senderAddress;
    private String companyId;
    private String payrollAdminName;
    private String payrollAdminPhoneNumber;
    private String payrollAdminEmail;
    private String companyLegalName;
    private  CheckPrintAddressDTO companyLegalAddress;
    private String companyBankRoutingNumber;
    private String companyBankAccountNumber;
    private String companyBankName;
    private byte[] checkSignature;
    private List<CheckPrintPaycheckDTO> paychecks = new ArrayList<CheckPrintPaycheckDTO>();

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public CheckPrintAddressDTO getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(CheckPrintAddressDTO senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getPayrollAdminName() {
        return payrollAdminName;
    }

    public void setPayrollAdminName(String payrollAdminName) {
        this.payrollAdminName = payrollAdminName;
    }

    public CheckPrintAddressDTO getCompanyLegalAddress() {
        return companyLegalAddress;
    }

    public void setCompanyLegalAddress(CheckPrintAddressDTO companyLegalAddress) {
        this.companyLegalAddress = companyLegalAddress;
    }

    public List<CheckPrintPaycheckDTO> getPaychecks() {
        return paychecks;
    }

    public void setPaychecks(List<CheckPrintPaycheckDTO> paychecks) {
        this.paychecks = paychecks;
    }

    public String getCompanyLegalName() {
        return companyLegalName;
    }

    public void setCompanyLegalName(String companyLegalName) {
        this.companyLegalName = companyLegalName;
    }

    public String getCompanyBankRoutingNumber() {
        return companyBankRoutingNumber;
    }

    public void setCompanyBankRoutingNumber(String companyBankRoutingNumber) {
        this.companyBankRoutingNumber = companyBankRoutingNumber;
    }

    public String getCompanyBankAccountNumber() {
        return companyBankAccountNumber;
    }

    public void setCompanyBankAccountNumber(String companyBankAccountNumber) {
        this.companyBankAccountNumber = companyBankAccountNumber;
    }

    public byte[] getCheckSignature() {
        return checkSignature;
    }

    public void setCheckSignature(byte[] checkSignature) {
        this.checkSignature = checkSignature;
    }

    public String getCompanyBankName() {
        return companyBankName;
    }

    public void setCompanyBankName(String companyBankName) {
        this.companyBankName = companyBankName;
    }

    public String getPayrollAdminPhoneNumber() {
        return payrollAdminPhoneNumber;
    }

    public void setPayrollAdminPhoneNumber(String payrollAdminPhoneNumber) {
        this.payrollAdminPhoneNumber = payrollAdminPhoneNumber;
    }

    public String getPayrollAdminEmail() {
        return payrollAdminEmail;
    }

    public void setPayrollAdminEmail(String payrollAdminEmail) {
        this.payrollAdminEmail = payrollAdminEmail;
    }
}
