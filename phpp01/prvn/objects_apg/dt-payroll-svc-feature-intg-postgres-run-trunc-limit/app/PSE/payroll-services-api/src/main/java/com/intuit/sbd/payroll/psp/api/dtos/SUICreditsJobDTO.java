package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;

/**
 * User: dweinberg
 * Date: 9/26/13
 * Time: 1:53 PM
 */
public class SUICreditsJobDTO {
    private int year;
    private int quarter;
    private String paymentTemplateCd; //nullable

    public SUICreditsJobDTO(int pYear, int pQuarter, String pPaymentTemplateCd) {
        year = pYear;
        quarter = pQuarter;
        paymentTemplateCd = pPaymentTemplateCd;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (year < 1970 || quarter < 1 || quarter > 4) {
            validationResult.getMessages().InvalidYearQuarter(Integer.toString(year), Integer.toString(quarter));
        }

        return validationResult;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int pYear) {
        year = pYear;
    }

    public int getQuarter() {
        return quarter;
    }

    public void setQuarter(int pQuarter) {
        quarter = pQuarter;
    }

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String pPaymentTemplateCd) {
        paymentTemplateCd = pPaymentTemplateCd;
    }
}
