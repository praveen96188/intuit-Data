package com.intuit.sbd.payroll.psp.webservices.wsdto;

import com.intuit.sbd.payroll.psp.domain.PaycheckStatusCode;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Feb 19, 2008
 * Time: 9:08:17 AM
 * To change this template use File | Settings | File Templates.
 */
@XmlAccessorOrder(value = XmlAccessOrder.ALPHABETICAL)
public class TransactionWSDTO {
    public String id; // GUID
    public String transactionType;
    public String currentState;
    public BigDecimal transactionAmount;
    public String settlementType;
    public Date settlementDate;
    public String offloadStatus;
    public BankAccountWSDTO creditBankAccount;
    public BankAccountWSDTO debitBankAccount;
    public MoneyMovementTransactionWSDTO moneyMovementTransaction;
    public Collection<String> allowableActions;
    public PaycheckStatusCode paycheckStatus;
    public String action;
    public String template;
}
