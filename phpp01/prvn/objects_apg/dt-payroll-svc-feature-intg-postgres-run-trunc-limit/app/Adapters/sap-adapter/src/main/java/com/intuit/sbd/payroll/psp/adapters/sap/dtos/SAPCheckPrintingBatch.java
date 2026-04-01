package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Feb 16, 2010
 * Time: 1:06:40 PM
 */
public class SAPCheckPrintingBatch {
    private String printBatchId;
    private String ein;
    private String psid;
    private String legalName;
    private Date paycheckDate;
    private Date sentToPrinterDate;
    private long paycheckCount;
    private String printStatus;
    private String printMessage;
    private String minPaycheckId;
    private String maxPaycheckId;    
    private SAPCompanyKey companyKey;

    public String getPrintBatchId() {
        return printBatchId;
    }

    public void setPrintBatchId(String pPrintBatchId) {
        printBatchId = pPrintBatchId;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String pEin) {
        ein = pEin;
    }

    public String getPsid() {
        return psid;
    }

    public void setPsid(String pPsid) {
        psid = pPsid;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String pLegalName) {
        legalName = pLegalName;
    }

    public Date getPaycheckDate() {
        return paycheckDate;
    }

    public void setPaycheckDate(Date pPaycheckDate) {
        paycheckDate = pPaycheckDate;
    }

    public Date getSentToPrinterDate() {
        return sentToPrinterDate;
    }

    public void setSentToPrinterDate(Date pSentToPrinterDate) {
        sentToPrinterDate = pSentToPrinterDate;
    }

    public long getPaycheckCount() {
        return paycheckCount;
    }

    public void setPaycheckCount(long pPaycheckCount) {
        paycheckCount = pPaycheckCount;
    }

    public String getPrintStatus() {
        return printStatus;
    }

    public void setPrintStatus(String pPrintStatus) {
        printStatus = pPrintStatus;
    }

    public String getPrintMessage() {
        return printMessage;
    }

    public void setPrintMessage(String pPrintMessage) {
        printMessage = pPrintMessage;
    }

    public String getMinPaycheckId() {
        return minPaycheckId;
    }

    public void setMinPaycheckId(String pMinPaycheckId) {
        minPaycheckId = pMinPaycheckId;
    }

    public String getMaxPaycheckId() {
        return maxPaycheckId;
    }

    public void setMaxPaycheckId(String pMaxPaycheckId) {
        maxPaycheckId = pMaxPaycheckId;
    }

    public SAPCompanyKey getCompanyKey() {
        return companyKey;
    }

    public void setCompanyKey(SAPCompanyKey pCompanyKey) {
        companyKey = pCompanyKey;
    }
}
