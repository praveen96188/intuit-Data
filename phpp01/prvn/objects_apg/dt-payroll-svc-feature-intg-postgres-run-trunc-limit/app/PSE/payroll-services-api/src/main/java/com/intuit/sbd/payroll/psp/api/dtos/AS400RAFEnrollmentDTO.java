package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.RAFEnrollmentStatus;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: May 12, 2010
 * Time: 2:30:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class AS400RAFEnrollmentDTO {
    private String fein;
    private String legalName;
    private String legalStreet;
    private String legalCity;
    private String legalState;
    private String legalZip;
    private RAFEnrollmentStatus rafStatus;
    private DateDTO statusEffectiveDate;
    private DateDTO f940DepositPeriod;
    private DateDTO f941DepositPeriod;

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

    public String getLegalStreet() {
        return legalStreet;
    }

    public void setLegalStreet(String legalStreet) {
        this.legalStreet = legalStreet;
    }

    public String getLegalCity() {
        return legalCity;
    }

    public void setLegalCity(String legalCity) {
        this.legalCity = legalCity;
    }

    public String getLegalZip() {
        return legalZip;
    }

    public void setLegalZip(String legalZip) {
        this.legalZip = legalZip;
    }

    public RAFEnrollmentStatus getRafStatus() {
        return rafStatus;
    }

    public void setRafStatus(RAFEnrollmentStatus rafStatus) {
        this.rafStatus = rafStatus;
    }

    public DateDTO getF940DepositPeriod() {
        return f940DepositPeriod;
    }

    public void setF940DepositPeriod(DateDTO f940DepositPeriod) {
        this.f940DepositPeriod = f940DepositPeriod;
    }

    public DateDTO getF941DepositPeriod() {
        return f941DepositPeriod;
    }

    public void setF941DepositPeriod(DateDTO f941DepositPeriod) {
        this.f941DepositPeriod = f941DepositPeriod;
    }

    public String getLegalState() {
        return legalState;
    }

    public void setLegalState(String legalState) {
        this.legalState = legalState;
    }

    public DateDTO getStatusEffectiveDate() {
        return statusEffectiveDate;
    }

    public void setStatusEffectiveDate(DateDTO statusEffectiveDate) {
        this.statusEffectiveDate = statusEffectiveDate;
    }
}
