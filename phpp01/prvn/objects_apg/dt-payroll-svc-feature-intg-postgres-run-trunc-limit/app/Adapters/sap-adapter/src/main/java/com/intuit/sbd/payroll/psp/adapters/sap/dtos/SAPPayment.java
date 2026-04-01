package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Jan 11, 2011
 * Time: 12:56:46 PM
 */
public class SAPPayment {
    private List<String> holds=new ArrayList<String>();
    private String manualHoldReason;
    private String manualHoldCreator;
    private String status;
    private String companyName;
    private SAPCompanyKey companyKey;
    private String agencyName;
    private String agencyId;
    private String paymentType;
    private Date settlementDate;
    private Date initiationDate;
    private Date dueDate;
    private Number amount;
    private String paymentMethod;
    private String paymentId;
    private String psId;
    private String ein;
    private String paymentFrequency;
    private boolean isPending;
    private SAPQuarter quarter;
    private String highestPriorityPaymentMethod;
    private boolean isNotPriorityPaymentMethod = false;
    private List<String> notPriorityPaymentMethodReasons;
    private Date periodBegin;
    private Date periodEnd;
    private boolean crossesQuarters;

    //these fields are presently used only by WS
    private String taxPaymentStatus;
    private String mmtStatus;

    public List<String> getHolds() {
        return holds;
    }

    public String getStatus() {
        return status;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public Date getSettlementDate() {
        return settlementDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public Number getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setHolds(List<String> hold) {
        this.holds = hold;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public void setSettlementDate(Date settlementDate) {
        this.settlementDate = settlementDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setAmount(Number amount) {
        this.amount = amount;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public SAPCompanyKey getCompanyKey() {
        return companyKey;
    }

    public void setCompanyKey(SAPCompanyKey companyKey) {
        this.companyKey = companyKey;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPsId() {
        return psId;
    }

    public String getEin() {
        return ein;
    }

    public void setPsId(String psId) {
        this.psId = psId;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    public void addHold(String pHold){
        holds.add(pHold);
    }

    public String getPaymentFrequency() {
        return paymentFrequency;
    }

    public void setPaymentFrequency(String paymentFrequency) {
        this.paymentFrequency = paymentFrequency;
    }

    public boolean getIsPending() {
        return isPending;
    }

    public void setIsPending(boolean isPending) {
        this.isPending = isPending;
    }

    public Date getInitiationDate() {
        return initiationDate;
    }

    public void setInitiationDate(Date initiationDate) {
        this.initiationDate = initiationDate;
    }

    public SAPQuarter getQuarter() {
        return quarter;
    }

    public void setQuarter(SAPQuarter quarter) {
        this.quarter = quarter;
    }

    public Date getPeriodBegin() {
        return periodBegin;
    }

    public void setPeriodBegin(Date periodBegin) {
        this.periodBegin = periodBegin;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    public String getTaxPaymentStatus() {
        return taxPaymentStatus;
    }

    public void setTaxPaymentStatus(String taxPaymentStatus) {
        this.taxPaymentStatus = taxPaymentStatus;
    }

    public String getMmtStatus() {
        return mmtStatus;
    }

    public void setMmtStatus(String mmtStatus) {
        this.mmtStatus = mmtStatus;
    }

    public String getManualHoldReason() {
        return manualHoldReason;
    }

    public void setManualHoldReason(String manualHoldReason) {
        this.manualHoldReason = manualHoldReason;
    }

    public String getManualHoldCreator() {
        return manualHoldCreator;
    }

    public void setManualHoldCreator(String manualHoldCreator) {
        this.manualHoldCreator = manualHoldCreator;
    }

    public String getHighestPriorityPaymentMethod() {
        return highestPriorityPaymentMethod;
    }

    public void setHighestPriorityPaymentMethod(String highestPriorityPaymentMethod) {
        this.highestPriorityPaymentMethod = highestPriorityPaymentMethod;
    }

    public boolean getIsNotPriorityPaymentMethod() {
        return isNotPriorityPaymentMethod;
    }

    public void setIsNotPriorityPaymentMethod(boolean priorityPaymentMethod) {
        isNotPriorityPaymentMethod = priorityPaymentMethod;
    }

    public List<String> getNotPriorityPaymentMethodReasons() {
        return notPriorityPaymentMethodReasons;
    }

    public void setNotPriorityPaymentMethodReasons(List<String> notPriorityPaymentMethodReasons) {
        this.notPriorityPaymentMethodReasons = notPriorityPaymentMethodReasons;
    }

    public boolean getCrossesQuarters() {
        return crossesQuarters;
    }

    public void setCrossesQuarters(boolean pCrossesQuarters) {
        crossesQuarters = pCrossesQuarters;
    }
}
