package com.intuit.sbd.payroll.psp.webservices.wsdto;

import com.intuit.sbd.payroll.psp.domain.AnnualBillingItem;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 12/5/12
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnnualBillingBatchWSDTO {
    public String formType;
    public Integer formYear;
    public String status;
    public List<AnnualBillingItemWSDTO> annualBillingItems;
}
