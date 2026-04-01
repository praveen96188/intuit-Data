package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.EftpsEnrollmentStatus;
import com.intuit.sbd.payroll.psp.domain.RAFEnrollmentStatus;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: May 12, 2010
 * Time: 2:30:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class AS400EFTPSEnrollmentDTO {
    private String fein;
    private String legalName;
    private String legalZip;
    private EftpsEnrollmentStatus eftpsStatus;
    private String rejectReason;
    private String eftpsEnrollmentId;
    private String groupId;
    private String transactionSetId;
    private String transactionId;
    private DateDTO statusEffectiveDate;

    public String getFein() {
        return fein;
    }

    public void setFein(String fein) {
        this.fein = fein;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    public String getLegalZip() {
        return legalZip;
    }

    public void setLegalZip(String legalZip) {
        this.legalZip = legalZip;
    }

    public EftpsEnrollmentStatus getEftpsStatus() {
        return eftpsStatus;
    }

    public void setEftpsStatus(EftpsEnrollmentStatus eftpsStatus) {
        this.eftpsStatus = eftpsStatus;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getEftpsEnrollmentId() {
        return eftpsEnrollmentId;
    }

    public void setEftpsEnrollmentId(String eftpsEnrollmentId) {
        this.eftpsEnrollmentId = eftpsEnrollmentId;
    }

    public DateDTO getStatusEffectiveDate() {
        return statusEffectiveDate;
    }

    public void setStatusEffectiveDate(DateDTO statusEffectiveDate) {
        this.statusEffectiveDate = statusEffectiveDate;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getTransactionSetId() {
        return transactionSetId;
    }

    public void setTransactionSetId(String transactionSetId) {
        this.transactionSetId = transactionSetId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
