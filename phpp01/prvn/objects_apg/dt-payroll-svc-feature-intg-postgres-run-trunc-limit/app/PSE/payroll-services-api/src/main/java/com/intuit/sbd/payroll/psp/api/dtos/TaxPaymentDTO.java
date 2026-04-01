package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: mamin
 * Date: May 29, 2009
 * Time: 10:47:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class TaxPaymentDTO {
    private String lawId;
    private String payrollItemId;
    private DateDTO date;
    private SpcfMoney amount;
    private QBDTTransactionInfoDTO mQBDTTransactionInfoDTO;

    public String getLawId() {
        return lawId;
    }

    public void setLawId(String lawId) {
        this.lawId = lawId;
    }

    public String getPayrollItemId() {
        return payrollItemId;
    }

    public void setPayrollItemId(String pPayrollItemId) {
        payrollItemId = pPayrollItemId;
    }

    public DateDTO getDate() {
        return date;
    }

    public void setDate(DateDTO date) {
        this.date = date;
    }

    public SpcfMoney getAmount() {
        return amount;
    }

    public void setAmount(SpcfMoney amount) {
        this.amount = amount;
    }

    public QBDTTransactionInfoDTO getQBDTTransactionInfoDTO() {
        return mQBDTTransactionInfoDTO;
    }

    public void setQBDTTransactionInfoDTO(QBDTTransactionInfoDTO pQBDTTransactionInfoDTO) {
        mQBDTTransactionInfoDTO = pQBDTTransactionInfoDTO;
    }

}
