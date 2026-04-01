package com.intuit.sbd.payroll.psp.webservices.wsdto;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: Aug 19, 2008
 * Time: 11:44:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class RedebitWSDTO {
    public BigDecimal amount;
    public String originalTransactionId;
    public String transactionType;
}
