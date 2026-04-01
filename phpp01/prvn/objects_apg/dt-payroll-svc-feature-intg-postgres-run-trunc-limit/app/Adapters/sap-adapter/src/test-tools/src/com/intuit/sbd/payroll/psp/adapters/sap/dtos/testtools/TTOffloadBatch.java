/*
 * : $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos.testtools;

import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.Date;

/**
 * TTOffloadBatch - DESCRIPTION
 *
 * @author Joe Warmelink
 */
public class TTOffloadBatch {
    private Date offloadDate;
    private String offloadGrpCd;
    private String statusCd;
    private Date insertDate;
    private Date statusChangeDate;
    private String gseq;

    public Date getOffloadDate() {
        return offloadDate;
    }

    public void setOffloadDate(Date offloadDate) {
        this.offloadDate = offloadDate;
    }

    public String getOffloadGrpCd() {
        return offloadGrpCd;
    }

    public void setOffloadGrpCd(String offloadGrpCd) {
        this.offloadGrpCd = offloadGrpCd;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public Date getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Date insertDate) {
        this.insertDate = insertDate;
    }

    public Date getStatusChangeDate() {
        return statusChangeDate;
    }

    public void setStatusChangeDate(Date statusChangeDate) {
        this.statusChangeDate = statusChangeDate;
    }

    public String getGseq() {
        return gseq;
    }

    public void setGseq(String gseq) {
        this.gseq = gseq;
    }
}
