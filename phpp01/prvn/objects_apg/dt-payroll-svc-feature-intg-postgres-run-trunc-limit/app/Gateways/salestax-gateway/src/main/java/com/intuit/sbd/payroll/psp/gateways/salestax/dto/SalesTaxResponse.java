package com.intuit.sbd.payroll.psp.gateways.salestax.dto;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 3, 2008
 * Time: 11:57:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class SalesTaxResponse {
    private String taxJurisdiction;
    private BigDecimal totalTaxAmount;
    private ArrayList<SalesTaxResponseLine> salesTaxResponseLineList = new ArrayList<SalesTaxResponseLine>() ;
    private ArrayList detailErrorMessageList;
    private boolean success;

    private ErrorMessage summaryErrorMessage;

    public void addLine(SalesTaxResponseLine pLine){
        salesTaxResponseLineList.add(pLine);
    }
    
    public String getTaxJurisdiction() {
        return taxJurisdiction;
    }

    public void setTaxJurisdiction(String taxJurisdiction) {
        this.taxJurisdiction = taxJurisdiction;
    }

    public BigDecimal getTotalTaxAmount() {
        return totalTaxAmount;
    }

    public void setTotalTaxAmount(BigDecimal totalTaxAmount) {
        this.totalTaxAmount = totalTaxAmount;
    }

    public ArrayList<SalesTaxResponseLine> getSalesTaxResponseLineList() {
        return salesTaxResponseLineList;
    }

    public ArrayList<ErrorMessage> getDetailErrorMessageList() {
        return detailErrorMessageList;
    }

    public void setDetailErrorMessageList(ArrayList detailErrorMessageList) {
        this.detailErrorMessageList = detailErrorMessageList;
    }

    public ErrorMessage getSummaryErrorMessage() {
        return summaryErrorMessage;
    }

    public void setSummaryErrorMessage(ErrorMessage summaryErrorMessage) {
        this.summaryErrorMessage = summaryErrorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
