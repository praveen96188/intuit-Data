package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: nchandrasekaran
 * Date: Jul 28, 2008
 * Time: 11:58:58 AM
 */

public class SAPNachaFile {
    private String fileName;
	private Double totalCredits;
	private Double totalDebits;
	private String confirmationCode;
	private Date finalizedTime;
	private Date transmissionTime;
    private String fileId;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Double getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(Double totalCredits) {
        this.totalCredits = totalCredits;
    }

    public Double getTotalDebits() {
        return totalDebits;
    }

    public void setTotalDebits(Double totalDebits) {
        this.totalDebits = totalDebits;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public Date getFinalizedTime() {
        return finalizedTime;
    }

    public void setFinalizedTime(Date finalizedTime) {
        this.finalizedTime = finalizedTime;
    }

    public Date getTransmissionTime() {
        return transmissionTime;
    }

    public void setTransmissionTime(Date transmissionTime) {
        this.transmissionTime = transmissionTime;
    }

}
