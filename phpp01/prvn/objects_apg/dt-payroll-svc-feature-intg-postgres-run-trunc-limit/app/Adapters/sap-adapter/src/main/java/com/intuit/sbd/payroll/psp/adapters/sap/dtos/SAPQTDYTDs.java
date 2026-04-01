package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: 1/30/11
 * Time: 9:34 AM
 */
public class SAPQTDYTDs {
    private double qtdLiability;
    private double qtdWages;
    private double ytdLiability;
    private double ytdWages;
    private double taxBalance;

    public double getQtdLiability() {
        return qtdLiability;
    }

    public void setQtdLiability(double qtdLiability) {
        this.qtdLiability = qtdLiability;
    }

    public double getQtdWages() {
        return qtdWages;
    }

    public void setQtdWages(double qtdWages) {
        this.qtdWages = qtdWages;
    }

    public double getYtdLiability() {
        return ytdLiability;
    }

    public void setYtdLiability(double ytdLiability) {
        this.ytdLiability = ytdLiability;
    }

    public double getYtdWages() {
        return ytdWages;
    }

    public void setYtdWages(double ytdWages) {
        this.ytdWages = ytdWages;
    }

    public double getTaxBalance() {
        return taxBalance;
    }

    public void setTaxBalance(double taxBalance) {
        this.taxBalance = taxBalance;
    }
}
