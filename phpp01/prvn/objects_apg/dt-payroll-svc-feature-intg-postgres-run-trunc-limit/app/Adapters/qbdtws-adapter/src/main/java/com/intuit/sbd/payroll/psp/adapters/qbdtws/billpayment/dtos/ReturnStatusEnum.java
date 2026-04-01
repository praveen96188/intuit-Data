package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlEnum;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 28, 2009
 * Time: 1:32:10 PM
 */
@XmlType(name = "ReturnStatusEnum")
@XmlEnum()
public enum ReturnStatusEnum {
    Created,

    Error,

    Open,

    Resolved
}
