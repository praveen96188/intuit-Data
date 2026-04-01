package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 9, 2008
 * Time: 2:48:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class OffloadBatchWSDTO {
    public String id; // GUID
    public String offloadGroupCd;
    public String status;
    public Date insertDate;
    public Date statusChangeDate;
}
