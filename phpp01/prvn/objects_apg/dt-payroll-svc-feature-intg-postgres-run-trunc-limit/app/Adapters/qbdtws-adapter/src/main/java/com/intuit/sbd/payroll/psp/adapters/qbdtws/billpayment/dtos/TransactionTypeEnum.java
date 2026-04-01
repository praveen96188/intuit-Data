package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "TransactionTypeEnum")
@XmlEnum()
public enum TransactionTypeEnum {
    PayBills,

    WriteChecks
}
