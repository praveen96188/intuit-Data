package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlEnum;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 24, 2009
 * Time: 10:39:33 AM
 */
@XmlType(name = "TransactionStateEnum")
@XmlEnum()
public enum TransactionStateEnum {
    Created,

    Executed,

    Cancelled,

    Returned,

    Completed,

    Voided
}
